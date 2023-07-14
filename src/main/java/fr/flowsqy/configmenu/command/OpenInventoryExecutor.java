package fr.flowsqy.configmenu.command;

import fr.flowsqy.abstractmenu.inventory.EventInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenInventoryExecutor implements CommandExecutor {

    private final EventInventory inventory;

    public OpenInventoryExecutor(@NotNull EventInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            inventory.open(player);
        }
        return true;
    }

}
