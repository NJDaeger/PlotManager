package com.njdaeger.taskmanager.dataaccess.databases.mysql;

import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.pdk.config.ISection;
import com.njdaeger.taskmanager.dataaccess.DatabaseType;
import com.njdaeger.taskmanager.dataaccess.IDatabase;
import com.njdaeger.taskmanager.dataaccess.IProcedure;
import com.njdaeger.taskmanager.dataaccess.Util;
import com.njdaeger.pluginlogger.IPluginLogger;
import com.njdaeger.taskmanager.dataaccess.models.SchemaVersionEntity;
import com.njdaeger.taskmanager.dataaccess.transactional.AbstractDatabaseTransaction;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MySqlDatabase implements IDatabase<MySqlTransaction, Connection> {

    private static final int SCHEMA_VERSION = 1;

    private final IProcedure procedures;
    private final IPluginLogger logger;
    private boolean successfulLoad;
    private final DataSource dataSource;
    private final String schemaName;

    public MySqlDatabase(IConfig config, IProcedure procedures, IPluginLogger logger) {
        this.procedures = procedures;
        this.logger = logger;
        ISection credentials = config.getSection("database.credentials");
        this.schemaName = credentials.getString("name");
        this.successfulLoad = false;
        this.dataSource = getDataSource(credentials);
    }

    private DataSource getDataSource(ISection credentials) {
        logger.info("Creating connection pool for MySQL database.");
        var dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" + credentials.getString("host") + ":" + credentials.getInt("port") + "/" + credentials.getString("name"));
        dataSource.setUsername(credentials.getString("user"));
        dataSource.setPassword(credentials.getString("password"));
        dataSource.setInitialSize(5);
        dataSource.setMaxTotal(10);
        dataSource.setMaxIdle(10);
        dataSource.setMinIdle(5);
        dataSource.setMaxWaitMillis(5000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(true);
        dataSource.setTestWhileIdle(true);
        dataSource.setNumTestsPerEvictionRun(10);
        dataSource.setTimeBetweenEvictionRunsMillis(30000);
        dataSource.setMinEvictableIdleTimeMillis(60000);
        dataSource.setRemoveAbandonedOnBorrow(true);
        dataSource.setRemoveAbandonedOnMaintenance(true);
        dataSource.setRemoveAbandonedTimeout(60);
        dataSource.setLogAbandoned(true);
        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        dataSource.setAutoCommitOnReturn(false);
        dataSource.setDefaultAutoCommit(false);
        logger.info("Connection pool created.");
        return dataSource;
    }

    @Override
    public Connection createConnection() throws Exception {
        return dataSource.getConnection();
    }

    @Override
    public IProcedure getProcedures() {
        return procedures;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MYSQL;
    }

    @Override
    public MySqlTransaction createTransaction() throws Exception {
        return new MySqlTransaction(createConnection(), logger, schemaName);
    }

    @Override
    public boolean commitTransaction(MySqlTransaction transaction) {
        var conn = transaction.getTransaction();
        try {
            logger.debug("Transaction commit: " + transaction.getTransactionId());
            conn.commit();
            return true;
        } catch (SQLException e) {
            rollbackTransaction(transaction);
            logger.exception(e, "Failed to commit transaction: " + transaction.getTransactionId());
        }
        return false;
    }

    @Override
    public void rollbackTransaction(MySqlTransaction transaction) {
        var conn = transaction.getTransaction();
        try {
            logger.debug("Transaction rollback: " + transaction.getTransactionId());
            conn.rollback();
        } catch (SQLException e) {
            logger.exception(e, "Failed to rollback transaction: " + transaction.getTransactionId());
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean initializeDatabase() {
        MySqlTransaction transaction = null;
        try {
            transaction = createTransaction();
            var currentVersion = currentSchemaVersion(transaction);
            if (currentVersion == -1) {
                runInitScript(transaction, "InitializeV" + SCHEMA_VERSION + ".sql");
                var setVersion = procedures.insertSchemaVersion(SCHEMA_VERSION);
                transaction.execute(setVersion.getFirst(), setVersion.getSecond());
                var systemUserProc = procedures.insertSystemUser();
                transaction.execute(systemUserProc.getFirst(), systemUserProc.getSecond());
            } else if (currentVersion < SCHEMA_VERSION) {
                logger.info("Database is out of date. Current version: " + currentVersion + " - Expected version: " + SCHEMA_VERSION);
                for (int i = currentVersion + 1; i <= SCHEMA_VERSION; i++) {
                    var updateScript = "UpdateV" + i + ".sql";
                    logger.info("Running update script: " + updateScript);
                    runUpdateScript(transaction, updateScript);
                    var setVersion = procedures.insertSchemaVersion(currentVersion);
                    transaction.execute(setVersion.getFirst(), setVersion.getSecond());
                }
                logger.info("Database updated to version: " + SCHEMA_VERSION);
            } else {
                logger.info("Database is up to date. Version: " + currentVersion);
            }
            commitTransaction(transaction);

            successfulLoad = true;
        } catch (Exception e) {
            if (transaction == null) return false;
            rollbackTransaction(transaction);
            logger.exception(e, "Failed to create databases");
            successfulLoad = false;
        }
        try {
            transaction.close();
        } catch (Exception e) {
            logger.exception(e, "Failed to close transaction.");
            successfulLoad = false;
        }
        return successfulLoad;
    }

    @Override
    public boolean isLoaded() {
        return successfulLoad;
    }

    private List<String> splitScript(String script, String delimiter, String commentPrefix) {
        script = script.replaceAll(commentPrefix + ".*", "");
        script = script.replaceAll("DELIMITER.*", "");
        return List.of(script.split(delimiter));
    }

    private void runUpdateScript(MySqlTransaction transaction, String script) {
        try (var update = Util.getJarResource("sql/mysql/update/" + script)) {
            var updateQuery = new String(update.readAllBytes(), StandardCharsets.UTF_8);
            var queries = splitScript(updateQuery, "//", "--");
            for (String query : queries) {
                if (query.isBlank()) continue;
                transaction.execute(query);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load file " + script + " - " + e.getMessage(), e);
        }
    }

    private void runInitScript(MySqlTransaction transaction, String script) {
        try (var init = Util.getJarResource("sql/mysql/init/" + script)){
            var initQuery = new String(init.readAllBytes(), StandardCharsets.UTF_8);
            var queries = splitScript(initQuery, "//", "--");
            for (String query : queries) {
                if (query.isBlank()) continue;
                transaction.execute(query);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load file " + script + " - " + e.getMessage(), e);
        }
    }

    private int currentSchemaVersion(MySqlTransaction transaction) {
        try {
            var query = procedures.selectSchemaVersion();
            var result = transaction.queryScalar(query.getFirst(), query.getSecond(), SchemaVersionEntity.class);
            return result.getId();
        } catch (Exception e) {
            logger.warning("Failed to get current schema version.");
            return -1;
        }
    }
}
