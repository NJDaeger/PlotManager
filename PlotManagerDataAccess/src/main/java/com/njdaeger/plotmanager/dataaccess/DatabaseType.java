package com.njdaeger.plotmanager.dataaccess;

public enum DatabaseType {

    MYSQL("MySql", "com.njdaeger.plotmanager.dataaccess.impl.mysql.MySqlDatabase", true, true),
    YML("Yml", "com.njdaeger.plotmanager.dataaccess.impl.yml.YmlDatabase", false, false);

    private final String name;
    private final String initializer;
    private final boolean supportsSprocs;
    private final boolean isSqlBased;

    DatabaseType(String name, String initializer, boolean supportsStoredProcedures, boolean isSqlBased) {
        this.name = name;
        this.initializer = initializer;
        this.supportsSprocs = supportsStoredProcedures;
        this.isSqlBased = isSqlBased;
    }

    public String getTypeName() {
        return name;
    }

    public String getInitializer() {
        return initializer;
    }

    public boolean supportsSprocs() {
        return supportsSprocs;
    }

    public boolean isSqlBased() {
        return isSqlBased;
    }

}
