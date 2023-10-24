package com.njdaeger.plotmanager.dataaccess.databases.mysql;

import com.njdaeger.plotmanager.dataaccess.Identifiable;
import com.njdaeger.plotmanager.dataaccess.models.Column;
import com.njdaeger.plotmanager.dataaccess.transactional.AbstractDatabaseTransaction;
import com.njdaeger.pluginlogger.IPluginLogger;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MySqlTransaction extends AbstractDatabaseTransaction<Connection> {

    private final Connection connection;

    public MySqlTransaction(Connection connection, IPluginLogger logger) {
        super(logger);
        this.connection = connection;
    }

    @Override
    public int execute(String query, Map<String, Object> params) throws Exception {
        var statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        for (var param : params.entrySet()) {
            statement.setObject(Integer.parseInt(param.getKey()), param.getValue());
        }
        logger.debug("Executing query: " + query.replace("\n", " ").replace("\r", " "));
        var changes = statement.executeUpdate();
        if (changes == 0) return -1;

        var generatedKeys = statement.getGeneratedKeys();
        if (!generatedKeys.next()) return -1;
        var id = generatedKeys.getInt(1);
        statement.close();

        return id;
    }

    @Override
    public <R extends Identifiable> List<R> query(String query, Map<String, Object> params, Class<R> clazz) throws Exception {
        var statement = connection.prepareStatement(query);
        for (var param : params.entrySet()) {
            statement.setObject(Integer.parseInt(param.getKey()), param.getValue());
        }
        logger.debug("Executing query: " + query.replace("\n", " ").replace("\r", " "));
        var rSet = statement.executeQuery();
        var res = new ArrayList<R>();
        while (rSet.next()) {
            var obj = clazz.getConstructor().newInstance();
            var columns = getColumnsOfClass(clazz);

            for (var column : columns.entrySet()) {
                var field = obj.getClass().getDeclaredField(column.getKey());
                field.setAccessible(true);
                field.set(obj, rSet.getObject(column.getKey(), column.getValue()));
            }

            res.add(obj);
        }
        statement.close();
        return res;
    }

    @Override
    public Connection getTransaction() {
        return connection;
    }

    private Map<String, Class<?>> getColumnsOfClass(Class<?> cls) {
        return Stream.of(cls.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Column.class)).collect(Collectors.toMap(f -> {
            var col = f.getAnnotation(Column.class);
            return col.dbName().isEmpty() ? f.getName() : col.dbName();
        }, Field::getType));
    }

    @Override
    public void closeTransaction() throws Exception {
        connection.close();
    }
}
