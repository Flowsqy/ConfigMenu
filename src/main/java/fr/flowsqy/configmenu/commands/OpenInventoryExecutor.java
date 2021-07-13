package fr.flowsqy.configmenu.commands;

import fr.flowsqy.abstractmenu.inventory.EventInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenInventoryExecutor implements CommandExecutor {

    private final EventInventory inventory;

    public OpenInventoryExecutor(EventInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            inventory.open(player, inventory.toString());
        }
        return true;
    }
}
