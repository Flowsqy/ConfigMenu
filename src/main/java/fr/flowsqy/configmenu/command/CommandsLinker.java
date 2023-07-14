package fr.flowsqy.configmenu.command;

import fr.flowsqy.abstractmenu.inventory.EventInventory;
import fr.flowsqy.configmenu.inventory.InventoryLocation;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommandsLinker {

    public List<LinkedCommand> link(@NotNull Map<InventoryLocation, Optional<EventInventory>> inventories, @NotNull List<UnlinkedCommand> commands) {
        final List<LinkedCommand> toRegisterCommands = new LinkedList<>();
        for (UnlinkedCommand command : commands) {
            final PluginCommand pluginCommand = command.command();
            final Optional<EventInventory> inventory = inventories.get(command.inventoryLocation());
            if (inventory.isEmpty()) {
                continue;
            }
            toRegisterCommands.add(new LinkedCommand(pluginCommand, inventory.get()));
        }
        return toRegisterCommands;
    }

}
