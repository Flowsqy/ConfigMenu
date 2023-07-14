package fr.flowsqy.configmenu.inventory;

import org.jetbrains.annotations.Nullable;

public record ActionData(@Nullable InventoryLocation location, @Nullable String message) {

}
