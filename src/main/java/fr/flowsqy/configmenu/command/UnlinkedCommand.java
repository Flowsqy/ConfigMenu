package fr.flowsqy.configmenu.command;

import fr.flowsqy.configmenu.inventory.InventoryLocation;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

public record UnlinkedCommand(@NotNull PluginCommand command, @NotNull InventoryLocation inventoryLocation) {
}
