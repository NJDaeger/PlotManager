package com.njdaeger.plotmanager.dataaccess.databases.mysql;

import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.pdk.config.ISection;
import com.njdaeger.plotmanager.dataaccess.DatabaseType;
import com.njdaeger.plotmanager.dataaccess.IDatabase;
import com.njdaeger.plotmanager.dataaccess.IProcedure;
import com.njdaeger.plotmanager.dataaccess.Util;
import com.njdaeger.pluginlogger.IPluginLogger;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

public class MySqlDatabase implements IDatabase<MySqlTransaction, Connection> {

    private final IProcedure procedures;
    private final IPluginLogger logger;
    private boolean successfulLoad;
    private final DataSource dataSource;

    public MySqlDatabase(IConfig config, IProcedure procedures, IPluginLogger logger) {
        this.procedures = procedures;
        this.logger = logger;
        ISection credentials = config.getSection("database.credentials");
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
        return new MySqlTransaction(createConnection(), logger);
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
        try (var transaction = createTransaction()) {
            loadTable(transaction, "SchemaVersion.sql");
            loadTable(transaction, "User.sql");
            loadTable(transaction, "Attribute.sql");
            loadTable(transaction, "World.sql");
            loadTable(transaction, "Group.sql");
            loadTable(transaction, "Plot.sql");
            loadTable(transaction, "PlotAttribute.sql");
            loadTable(transaction, "PlotGroupAttribute.sql");
            loadTable(transaction, "PlotUsers.sql");
            commitTransaction(transaction);

            loadTrigger(transaction, "UserTrigger.sql");
            loadTrigger(transaction, "AttributeTrigger.sql");
            loadTrigger(transaction, "WorldTrigger.sql");
            loadTrigger(transaction, "GroupTrigger.sql");
            loadTrigger(transaction, "PlotTrigger.sql");
            loadTrigger(transaction, "PlotAttributeTrigger.sql");
            loadTrigger(transaction, "PlotGroupAttributeTrigger.sql");
            loadTrigger(transaction, "PlotUsersTrigger.sql");
            commitTransaction(transaction);

            var systemUserProc = procedures.insertSystemUser();
            transaction.execute(systemUserProc.getFirst(), systemUserProc.getSecond());
            commitTransaction(transaction);

            successfulLoad = true;
        } catch (Exception e) {
            logger.exception(e, "Failed to create databases");
            successfulLoad = false;
        }
        return successfulLoad;
    }

    @Override
    public boolean isLoaded() {
        return successfulLoad;
    }

    private void loadTable(MySqlTransaction transaction, String tableFile) {
        try {
            var table = Util.getJarResource("sql/mysql/tables/" + tableFile);
            var tableQuery = new String(table.readAllBytes(), StandardCharsets.UTF_8);
            transaction.execute(tableQuery);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load file " + tableFile + " - " + e.getMessage(), e);
        }
    }

    private void loadTrigger(MySqlTransaction transaction, String triggerFile) {
        try {
            var trigger = Util.getJarResource("sql/mysql/triggers/" + triggerFile);
            var triggerQuery = new String(trigger.readAllBytes(), StandardCharsets.UTF_8);
            transaction.execute(triggerQuery);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load file " + triggerFile + " - " + e.getMessage(), e);
        }
    }


}
