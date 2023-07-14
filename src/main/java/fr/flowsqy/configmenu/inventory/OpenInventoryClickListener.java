package fr.flowsqy.configmenu.inventory;

import fr.flowsqy.abstractmenu.inventory.EventInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record OpenInventoryClickListener(@NotNull EventInventory inventory) implements Consumer<InventoryClickEvent> {
    @Override
    public void accept(@NotNull InventoryClickEvent event) {
        inventory.open((Player) event.getWhoClicked());
    }
}

