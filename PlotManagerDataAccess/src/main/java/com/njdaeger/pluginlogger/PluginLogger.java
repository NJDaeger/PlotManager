package com.njdaeger.pluginlogger;

import com.njdaeger.pdk.config.IConfig;
import org.bukkit.plugin.Plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class PluginLogger implements IPluginLogger {

    private static final DateFormat YMD_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat YMDHMS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Plugin plugin;
    private final IConfig config;
    private BufferedWriter writer;

    public PluginLogger(Plugin plugin, IConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void exception(Exception exception, String... additionalInfo) {
        plugin.getLogger().severe("== ExceptionPublisher Start ==");
        plugin.getLogger().severe("Exception: " + exception.getMessage());
        plugin.getLogger().severe("Stacktrace:");
        for (StackTraceElement element : exception.getStackTrace()) {
            plugin.getLogger().severe(element.toString());
        }
        writeToFile(exception, additionalInfo);
        plugin.getLogger().severe("== ExceptionPublisher End ==");
    }

    @Override
    public void exception(Exception exception) {
        plugin.getLogger().severe("== ExceptionPublisher Start ==");
        plugin.getLogger().severe("Exception: " + exception.getMessage());
        plugin.getLogger().severe("Stacktrace:");
        for (StackTraceElement element : exception.getStackTrace()) {
            plugin.getLogger().severe(element.toString());
        }
        writeToFile(exception);
        plugin.getLogger().severe("== ExceptionPublisher End ==");
    }

    @Override
    public void warning(String message, String... additionalInfo) {
        plugin.getLogger().warning(message);
        for (String info : additionalInfo) {
            plugin.getLogger().warning(info);
        }
    }

    @Override
    public void warning(String message) {
        plugin.getLogger().warning(message);
    }

    @Override
    public void info(String message, String... additionalInfo) {
        plugin.getLogger().info(message);
        for (String info : additionalInfo) {
            plugin.getLogger().info(info);
        }
    }

    @Override
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void debug(String message, String... additionalInfo) {
        if (!isDebugEnabled()) return;
        plugin.getLogger().info("[Debug] " + message);
        for (String info : additionalInfo) {
            plugin.getLogger().info("[Debug] " + info);
        }
    }

    @Override
    public void debug(String message) {
        if (!isDebugEnabled()) return;
        plugin.getLogger().info("[Debug] " + message);
    }

    private File getCurrentExceptionFile() {
        var exceptionFolder = new File(plugin.getDataFolder() + File.separator + "ExceptionPublisher");
        if (!exceptionFolder.exists()) exceptionFolder.mkdirs();
        return new File(exceptionFolder, "errors_" + YMD_FORMAT.format(System.currentTimeMillis()) + ".txt");
    }

    private void initializeWriter(File file) {
        if (writer != null) return;
        try {
            writer = new BufferedWriter(new FileWriter(file, true));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize exception writer.");
            throw new RuntimeException(e);
        }
    }

    private boolean isFileLoggingEnabled() {
        return config.getBoolean("logging.write-exceptions-to-file");
    }

    private boolean isDebugEnabled() {
        return config.getBoolean("logging.debug");
    }

    private void writeToFile(Exception exception, String... additionalInfo) {
        if (!isFileLoggingEnabled()) return;
        var file = getCurrentExceptionFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to create exception file.");
                throw new RuntimeException(e);
            }
        }

        initializeWriter(file);

        try {
            writer.write("======== Exception ========\n");
            writer.write("Timestamp: " + YMDHMS_FORMAT.format(System.currentTimeMillis()) + "\n");
            writer.write("Plugin: " + plugin.getName() + " Version:" + plugin.getDescription().getVersion() + "\n");
            if (exception != null) {
                writer.write("Exception: " + exception.getMessage() + "\n");
                writer.write("Stacktrace:\n");
                for (StackTraceElement element : exception.getStackTrace()) {
                    writer.write("\t" + element.toString() + "\n");
                }
            }
            if (additionalInfo.length > 0) {
                writer.write("Additional Info:\n");
                for (String info : additionalInfo) {
                    writer.write("\t" + info + "\n");
                }
            }
            writer.flush();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to write exception to file.");
            throw new RuntimeException(e);
        }


    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }
}