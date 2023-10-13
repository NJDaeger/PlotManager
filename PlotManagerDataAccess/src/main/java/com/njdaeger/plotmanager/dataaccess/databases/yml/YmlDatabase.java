package com.njdaeger.plotmanager.dataaccess.databases.yml;

import com.njdaeger.pdk.config.IConfig;
import com.njdaeger.plotmanager.dataaccess.DatabaseType;
import com.njdaeger.plotmanager.dataaccess.IDatabase;
import com.njdaeger.plotmanager.dataaccess.IProcedure;
import com.njdaeger.plotmanager.dataaccess.transactional.AbstractDatabaseTransaction;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YmlDatabase implements IDatabase {

    private final Plugin plugin;
    private IProcedure procedures;
    private Map<String, IConfig> configurations;

    public YmlDatabase(Plugin plugin, IProcedure procedures) {
        this.plugin = plugin;
        this.procedures = procedures;
        this.configurations = new ConcurrentHashMap<>();
    }

    @Override
    public IProcedure getProcedures() {
        return procedures;
    }

    @Override
    public Object createConnection() throws Exception {
        return null;
    }

    @Override
    public void initializeDatabase() {

    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.YML;
    }

    @Override
    public AbstractDatabaseTransaction createTransaction() throws Exception {
        return null;
    }

    @Override
    public void rollbackTransaction(AbstractDatabaseTransaction transaction) {

    }

    @Override
    public boolean commitTransaction(AbstractDatabaseTransaction transaction) {
        return false;
    }

//    @Override
//    public void commit() {
//        configurations.values().forEach(IConfig::save);
//    }
//
//    @Override
//    public void createDatabase(String database) {
//        configurations.put(database, new YmlConfig(plugin, "data" + File.separator + database));
//    }
//
//    @Override
//    public boolean insert(String statement, Identifiable object) {
//        //the statement is formatted like [database].key.to.entry.{key}
//        //grab the database name, and use the rest as the path
//        var database = statement.substring(0, statement.indexOf('.')).replaceAll("[\\[\\]]", "");
//        var path = statement.substring(statement.indexOf('.') + 1).replace("{key}", String.valueOf(object.getId()));
//        if (!configurations.containsKey(database)) throw new IllegalArgumentException("Database " + database + " does not exist or has not been created/loaded.");
//        var config = configurations.get(database);
//        if (config.hasSection(path)) return false;
//        var columns = getObjectColumns(object);
//        for (var column : columns) {
//            try {
//                var field = object.getClass().getDeclaredField(column);
//                field.setAccessible(true);
//
//                config.addEntry(path + "." + column, field.get(object));
//            } catch (NoSuchFieldException | IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public boolean update(String statement, Identifiable object) {
//        var database = statement.substring(0, statement.indexOf('.')).replaceAll("[\\[\\]]", "");
//        var path = statement.substring(statement.indexOf('.') + 1).replace("{key}", String.valueOf(object.getId()));
//        if (!configurations.containsKey(database)) throw new IllegalArgumentException("Database " + database + " does not exist or has not been created/loaded.");
//        var config = configurations.get(database);
//        if (!config.hasSection(path)) return false;
//        var columns = getObjectColumns(object);
//        for (var column : columns) {
//            try {
//                var field = object.getClass().getDeclaredField(column);
//                field.setAccessible(true);
//                config.setEntry(path + "." + column, field.get(object));
//            } catch (NoSuchFieldException | IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public boolean delete(String statement, Identifiable object) {
//        var database = statement.substring(0, statement.indexOf('.')).replaceAll("[\\[\\]]", "");
//        var path = statement.substring(statement.indexOf('.') + 1).replace("{key}", String.valueOf(object.getId()));
//        if (!configurations.containsKey(database)) throw new IllegalArgumentException("Database " + database + " does not exist or has not been created/loaded.");
//        var config = configurations.get(database);
//        if (!config.hasSection(path)) return false;
//        config.removeEntry(path);
//        return true;
//    }
//
//    @Override
//    public <R extends Identifiable> List<R> select(Class<R> entityClass, String query) {
//        var database = query.substring(0, query.indexOf('.')).replaceAll("[\\[\\]]", "");
//        var path = query.substring(query.indexOf('.') + 1);
//        if (!configurations.containsKey(database)) throw new IllegalArgumentException("Database " + database + " does not exist or has not been created/loaded.");
//        var config = configurations.get(database);
//        if (!config.hasSection(path)) return List.of();
//
//        var results = new ArrayList<R>();
//        var columns = getColumnsOfClass(entityClass);
//
//        try {
//            var obj = entityClass.getConstructor((Class<?>) null).newInstance();
//            var section = config.getSection(path);
//            section.getKeys(false).forEach(key -> {
////                obj.setId();
////                config.getValueAs()
//            });
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        }
//        return null;
//    }
//
//    private List<String> getObjectColumns(Object object) {
//        return getColumnsOfClass(object.getClass());
//    }
//
//    private List<Field> getColumnFieldsOfClass(Class<?> cls) {
//        return Stream.of(cls.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Column.class)).toList();
//    }
//
//    private List<String> getColumnsOfClass(Class<?> cls) {
//        return Stream.of(cls.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Column.class)).map(f -> {
//            var col = f.getAnnotation(Column.class);
//            return col.dbName().isEmpty() ? f.getName() : col.dbName();
//        }).toList();
//    }

}
