package com.njdaeger.plotmanager.dataaccess.impl.mariadb;

import com.njdaeger.plotmanager.dataaccess.Identifiable;
import com.njdaeger.plotmanager.dataaccess.models.Column;
import com.njdaeger.plotmanager.dataaccess.transactional.ITransaction;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MariaDbTransaction implements ITransaction<Connection> {

    private final Connection connection;

    public MariaDbTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int execute(String query, Map<String, Object> params) throws Exception {
        var statement = connection.prepareStatement(query);
        for (var param : params.entrySet()) {
            statement.setObject(Integer.parseInt(param.getKey()), param.getValue());
        }
        var changes = statement.executeUpdate();
        if (changes == 0) return -1;

        var generatedKeys = statement.getGeneratedKeys();
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
        var rSet = statement.executeQuery();
        var res = new ArrayList<R>();
        while (rSet.next()) {
            var columns = getColumnsOfClass(clazz);

            //grab the column from the result set and get the value from it and use that to create an instance of the entity class provided
            //then add that instance to the list of results

            var objects = new ArrayList<>();

            for (var column : columns.entrySet()) {
                var value = rSet.getObject(column.getKey(), column.getValue());
                objects.add(value);
            }

            var obj = clazz.getConstructor(columns.values().toArray(Class[]::new)).newInstance(objects.toArray());
            res.add(obj);
        }
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
    public void close() throws Exception {
        connection.close();
    }
}
