package fr.flowsqy.configmenu.inventory;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class InventoryLocationParser {

    @NotNull
    public InventoryLocation parse(@NotNull String inventoryPath, @NotNull File rootFolder, @NotNull File currentFile) {
        final File file;
        final String inventoryId;
        if (inventoryPath.isBlank()) {
            throw new IllegalArgumentException("The path '" + inventoryPath + "' is incorrect. It should not be blank");
        }
        if (inventoryPath.contains(":")) {
            final String[] pathParts = inventoryPath.split(":");
            if (pathParts.length != 2) {
                throw new IllegalArgumentException("The path '" + inventoryPath + "' is incorrect. It should contains only one ':'");
            }
            if (pathParts[0].isBlank()) {
                throw new IllegalArgumentException("The path '" + pathParts[0] + "' is incorrect (blank, no file specified)");
            }
            if (pathParts[1].isBlank()) {
                throw new IllegalArgumentException("The id '" + pathParts[1] + "' is incorrect (blank, no inventory id specified)");
            }
            file = new File(rootFolder, pathParts[0].trim().replace('/', File.separatorChar));
            inventoryId = pathParts[1].trim();
        } else {
            inventoryId = inventoryPath.trim();
            file = currentFile;
        }

        return new InventoryLocation(file, inventoryId);
    }

}
