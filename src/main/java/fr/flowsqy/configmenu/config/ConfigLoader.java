package fr.flowsqy.configmenu.config;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigLoader {

    public boolean checkDataFolder(@NotNull File dataFolder) {
        if (dataFolder.exists()) {
            return dataFolder.canWrite();
        }
        return dataFolder.mkdirs();
    }

    @NotNull
    public File initFile(@NotNull File dataFolder, @NotNull InputStream resource, @NotNull String fileName) {
        final File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            try {
                Files.copy(resource, file.toPath());
            } catch (IOException ignored) {
            }
        }

        return file;
    }

}
