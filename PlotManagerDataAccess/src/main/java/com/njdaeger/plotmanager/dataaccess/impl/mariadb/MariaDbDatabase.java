package com.njdaeger.plotmanager.dataaccess.impl.mariadb;

import com.njdaeger.exceptionpublisher.IExceptionPublisher;
import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.pdk.config.ISection;
import com.njdaeger.plotmanager.dataaccess.*;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MariaDbDatabase implements IDatabase<MariaDbTransaction, Connection> {

    private final Plugin plugin;
    private final IProcedure procedures;
    private final IExceptionPublisher exceptionPublisher;
    private final ISection credentials;

    public MariaDbDatabase(Plugin plugin, IConfig config, IProcedure procedures, IExceptionPublisher exceptionPublisher) {
        this.plugin = plugin;
        this.procedures = procedures;
        this.exceptionPublisher = exceptionPublisher;
        this.credentials = config.getSection("database.credentials");
    }

    @Override
    public Connection createConnection() throws Exception {
        Connection connection = null;

        var host = credentials.getString("host");
        var port = credentials.getInt("port");
        var name = credentials.getString("name");
        var user = credentials.getString("user");
        var pass = credentials.getString("password");

        connection = DriverManager.getConnection("jdbc:mariadb://" + host + ":" + port + "/" + name, user, pass);
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
        return DatabaseType.MARIADB;
    }

    @Override
    public MariaDbTransaction createTransaction() throws Exception {
        return new MariaDbTransaction(createConnection());
    }

    @Override
    public boolean commitTransaction(MariaDbTransaction transaction) {
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
    public void rollbackTransaction(MariaDbTransaction transaction) {
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
            var attributeTable = Util.getJarResource("sql.mariadb.tables.Attribute.sql");
            var attributeTableQuery = new String(attributeTable.readAllBytes(), StandardCharsets.UTF_8);
            var plotTable = Util.getJarResource("sql.mariadb.tables.Plot.sql");
            var plotTableQuery = new String(plotTable.readAllBytes(), StandardCharsets.UTF_8);
            var plotAttributeTable = Util.getJarResource("sql.mariadb.tables.PlotAttribute.sql");
            var plotAttributeTableQuery = new String(plotAttributeTable.readAllBytes(), StandardCharsets.UTF_8);
            var plotGroupTable = Util.getJarResource("sql.mariadb.tables.PlotGroup.sql");
            var plotGroupTableQuery = new String(plotGroupTable.readAllBytes(), StandardCharsets.UTF_8);
            var plotGroupsTable = Util.getJarResource("sql.mariadb.tables.PlotGroups.sql");
            var plotGroupsTableQuery = new String(plotGroupsTable.readAllBytes(), StandardCharsets.UTF_8);
            var plotUsersTable = Util.getJarResource("sql.mariadb.tables.PlotUsers.sql");
            var plotUsersTableQuery = new String(plotUsersTable.readAllBytes(), StandardCharsets.UTF_8);
            var schemaVersionTable = Util.getJarResource("sql.mariadb.tables.SchemaVersion.sql");
            var schemaVersionTableQuery = new String(schemaVersionTable.readAllBytes(), StandardCharsets.UTF_8);
            var userTable = Util.getJarResource("sql.mariadb.tables.User.sql");
            var userTableQuery = new String(userTable.readAllBytes(), StandardCharsets.UTF_8);

            transaction.execute(attributeTableQuery);
            transaction.execute(plotTableQuery);
            transaction.execute(plotAttributeTableQuery);
            transaction.execute(plotGroupTableQuery);
            transaction.execute(plotGroupsTableQuery);
            transaction.execute(plotUsersTableQuery);
            transaction.execute(schemaVersionTableQuery);
            transaction.execute(userTableQuery);

            commitTransaction(transaction);
        } catch (Exception e) {
            exceptionPublisher.publishException(e, e.getMessage(), "Failed to create databases");
        }

    }

}
