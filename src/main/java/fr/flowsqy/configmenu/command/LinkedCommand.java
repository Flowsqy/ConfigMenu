package fr.flowsqy.configmenu.command;

import fr.flowsqy.abstractmenu.inventory.EventInventory;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

public record LinkedCommand(@NotNull PluginCommand command, @NotNull EventInventory inventory) {
}
