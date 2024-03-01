package com.njdaeger.taskmanager.dataaccess.databases.mysql;

import com.njdaeger.taskmanager.dataaccess.Identifiable;
import com.njdaeger.taskmanager.dataaccess.models.Column;
import com.njdaeger.taskmanager.dataaccess.transactional.AbstractDatabaseTransaction;
import com.njdaeger.taskmanager.dataaccess.transactional.ExecutionConstants;
import com.njdaeger.pluginlogger.IPluginLogger;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MySqlTransaction extends AbstractDatabaseTransaction<Connection> {

    private final Connection connection;
    private final String schemaName;

    public MySqlTransaction(Connection connection, IPluginLogger logger, String schemaName) {
        super(logger);
        this.connection = connection;
        this.schemaName = schemaName;
    }

    @Override
    public int execute(String query, Map<String, Object> params) throws Exception {
        try (PreparedStatement statement = query.toLowerCase().startsWith("call")
                ? connection.prepareCall("{" + query.replaceAll("TaskManager\\.", schemaName + ".") + "}")
                : connection.prepareStatement(query.replaceAll("TaskManager\\.", schemaName + "."), Statement.RETURN_GENERATED_KEYS)) {

            for (var param : params.entrySet()) {
                statement.setObject(Integer.parseInt(param.getKey()), param.getValue());
            }
            logger.debug("Executing statement: " + query.replaceAll("TaskManager\\.", schemaName + ".").replace("\n", " ").replace("\r", " "));
            var changes = statement.executeUpdate();
            if (changes == 0) return ExecutionConstants.NO_ROWS_AFFECTED;

            var generatedKeys = statement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                return ExecutionConstants.NO_ID_RETURNED;
            }
            return generatedKeys.getInt(1);
        }

    }

    @Override
    public <R extends Identifiable> List<R> query(String query, Map<String, Object> params, Class<R> clazz) throws Exception {
        try (PreparedStatement statement = query.toLowerCase().startsWith("call")
                ? connection.prepareCall("{" + query.replaceAll("TaskManager\\.", schemaName + ".") + "}")
                : connection.prepareStatement(query.replaceAll("TaskManager\\.", schemaName + "."))) {

            for (var param : params.entrySet()) {
                statement.setObject(Integer.parseInt(param.getKey()), param.getValue());
            }
            logger.debug("Executing query: " + query.replaceAll("TaskManager\\.", schemaName + ".").replace("\n", " ").replace("\r", " "));
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
            return res;
        }
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
