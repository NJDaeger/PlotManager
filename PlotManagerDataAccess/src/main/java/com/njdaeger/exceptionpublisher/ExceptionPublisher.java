package com.njdaeger.exceptionpublisher;

import com.njdaeger.pdk.config.IConfig;
import org.bukkit.plugin.Plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ExceptionPublisher implements IExceptionPublisher {

    private static final DateFormat YMD_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat YMDHMS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Plugin plugin;
    private final IConfig config;
    private BufferedWriter writer;

    public ExceptionPublisher(Plugin plugin, IConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void publishException(Exception exception, String message, String... additionalInfo) {
        plugin.getLogger().warning("== ExceptionPublisher Start ==");
        plugin.getLogger().warning(message);
        plugin.getLogger().warning("Exception: " + exception.getMessage());
        for (String info : additionalInfo) {
            plugin.getLogger().warning(info);
        }
        plugin.getLogger().warning("Stacktrace:");
        for (StackTraceElement element : exception.getStackTrace()) {
            plugin.getLogger().warning(element.toString());
        }
        writeToFile(exception, message, additionalInfo);
        plugin.getLogger().warning("== ExceptionPublisher End ==");
    }

    @Override
    public void publishException(Exception exception, String message) {
        plugin.getLogger().warning("== ExceptionPublisher Start ==");
        plugin.getLogger().warning(message);
        plugin.getLogger().warning("Exception: " + exception.getMessage());
        plugin.getLogger().warning("Stacktrace:");
        for (StackTraceElement element : exception.getStackTrace()) {
            plugin.getLogger().warning(element.toString());
        }
        writeToFile(exception, message);
        plugin.getLogger().warning("== ExceptionPublisher End ==");
    }

    @Override
    public void publishException(Exception exception) {
        plugin.getLogger().warning("== ExceptionPublisher Start ==");
        plugin.getLogger().warning("Exception: " + exception.getMessage());
        plugin.getLogger().warning("Stacktrace:");
        for (StackTraceElement element : exception.getStackTrace()) {
            plugin.getLogger().warning(element.toString());
        }
        writeToFile(exception, null);
        plugin.getLogger().warning("== ExceptionPublisher End ==");
    }

    @Override
    public void publishMessage(String message, String... additionalInfo) {
        plugin.getLogger().warning("== ExceptionPublisher Start ==");
        plugin.getLogger().warning(message);
        for (String info : additionalInfo) {
            plugin.getLogger().warning(info);
        }
        writeToFile(null, message, additionalInfo);
        plugin.getLogger().warning("== ExceptionPublisher End ==");
    }

    @Override
    public void publishMessage(String message) {
        plugin.getLogger().warning("== ExceptionPublisher Start ==");
        plugin.getLogger().warning(message);
        writeToFile(null, message);
        plugin.getLogger().warning("== ExceptionPublisher End ==");
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
        return config.getBoolean("write-exceptions-to-file");
    }

    private void writeToFile(Exception exception, String message, String... additionalInfo) {
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
            if (message != null) writer.write("Message: " + message + "\n");
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
