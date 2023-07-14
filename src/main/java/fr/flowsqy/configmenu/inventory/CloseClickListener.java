package fr.flowsqy.configmenu.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class CloseClickListener implements Consumer<InventoryClickEvent> {

    @Override
    public void accept(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.getWhoClicked().closeInventory();
    }

}
