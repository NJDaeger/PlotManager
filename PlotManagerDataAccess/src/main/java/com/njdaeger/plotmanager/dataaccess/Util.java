package com.njdaeger.plotmanager.dataaccess;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class Util {

    public static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Get a resource from the jar file.
     * @param path The path to the resource
     * @return The resource as an input stream
     */
    public static InputStream getJarResource(String path) {
        return Util.class.getClassLoader().getResourceAsStream(path);
    }

    public static List<String> getListOfFilesInJar(String path) {
        List<String> fileList = new ArrayList<>();

        try {
            // Create a JarFile object for the specified JAR file
            var jarFile = new JarFile(Util.class.getProtectionDomain().getCodeSource().getLocation().getPath());

            // Get the entries (files and directories) in the JAR file
            var entries = jarFile.entries();

            // Iterate through the entries and add file names to the list
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().startsWith(path)) {
                    fileList.add(entry.getName());
                }
            }

            // Close the JarFile when done
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    public static <R> R await(CompletableFuture<R> future) {
        try {
            return future.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void awaitAll(CompletableFuture<?>... futures) {
        Stream.of(futures).parallel().forEach(Util::await);
    }

    //async
    public static <R> CompletableFuture<R> async(Supplier<R> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }
}
