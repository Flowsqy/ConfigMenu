package fr.flowsqy.configmenu.inventory;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public record InventoryLocation(@NotNull File file, @NotNull String id) {

}
