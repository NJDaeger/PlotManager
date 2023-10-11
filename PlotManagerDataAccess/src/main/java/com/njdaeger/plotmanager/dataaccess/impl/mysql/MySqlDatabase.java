package com.njdaeger.plotmanager.dataaccess.impl.mysql;

import com.njdaeger.exceptionpublisher.IExceptionPublisher;
import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.pdk.config.ISection;
import com.njdaeger.pdk.utils.Pair;
import com.njdaeger.plotmanager.dataaccess.*;
import com.njdaeger.serviceprovider.IServiceProvider;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlDatabase implements IDatabase<MySqlTransaction, Connection> {

    private final Plugin plugin;
    private final IProcedure procedures;
    private final IExceptionPublisher exceptionPublisher;
    private final IServiceProvider provider;
    private final ISection credentials;
    private boolean successfulLoad;

    public MySqlDatabase(Plugin plugin, IConfig config, IServiceProvider provider, IProcedure procedures, IExceptionPublisher exceptionPublisher) {
        this.plugin = plugin;
        this.procedures = procedures;
        this.exceptionPublisher = exceptionPublisher;
        this.provider = provider;
        this.credentials = config.getSection("database.credentials");
        this.successfulLoad = false;
    }

    @Override
    public Connection createConnection() throws Exception {
        Connection connection = null;

        var host = credentials.getString("host");
        var port = credentials.getInt("port");
        var name = credentials.getString("name");
        var user = credentials.getString("user");
        var pass = credentials.getString("password");

        connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + name, user, pass);
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        return connection;

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
        return new MySqlTransaction(createConnection());
    }

    @Override
    public boolean commitTransaction(MySqlTransaction transaction) {
        var conn = transaction.getTransaction();
        try {
            conn.commit();
            return true;
        } catch (SQLException e) {
            rollbackTransaction(transaction);
            exceptionPublisher.publishException(e, e.getMessage(), "Failed to commit transaction.");
        }
        return false;
    }

    @Override
    public void rollbackTransaction(MySqlTransaction transaction) {
        var conn = transaction.getTransaction();
        try {
            conn.rollback();
        } catch (SQLException e) {
            exceptionPublisher.publishException(e, e.getMessage(), "Failed to rollback transaction.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createDatabases() {
        try (var transaction = createTransaction()) {
            loadTable(transaction, "SchemaVersion.sql");
            loadTable(transaction, "User.sql");
            loadTable(transaction, "Attribute.sql");
            loadTable(transaction, "World.sql");
            loadTable(transaction, "Group.sql");
            loadTable(transaction, "Plot.sql");
            loadTable(transaction, "PlotAttribute.sql");
            loadTable(transaction, "PlotGroups.sql");
            loadTable(transaction, "PlotUsers.sql");
            commitTransaction(transaction);

            loadTrigger(transaction, "UserTrigger.sql");
            loadTrigger(transaction, "AttributeTrigger.sql");
            loadTrigger(transaction, "WorldTrigger.sql");
            loadTrigger(transaction, "GroupTrigger.sql");
            loadTrigger(transaction, "PlotTrigger.sql");
            loadTrigger(transaction, "PlotAttributeTrigger.sql");
            loadTrigger(transaction, "PlotGroupsTrigger.sql");
            loadTrigger(transaction, "PlotUsersTrigger.sql");
            commitTransaction(transaction);

            var systemUserProc = procedures.insertSystemUser();
            transaction.execute(systemUserProc.getFirst(), systemUserProc.getSecond());
            commitTransaction(transaction);

            successfulLoad = true;
        } catch (Exception e) {
            exceptionPublisher.publishException(e, e.getMessage(), "Failed to create databases");
            successfulLoad = false;
        }
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
