package fr.flowsqy.configmenu.inventory;

import fr.flowsqy.abstractmenu.inventory.EventInventory;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ClickListenerHandler {

    private final Map<InventoryLocation, Optional<EventInventory>> inventories;

    public ClickListenerHandler(@NotNull Map<InventoryLocation, Optional<EventInventory>> inventories) {
        this.inventories = inventories;
    }

    @Nullable
    public Consumer<InventoryClickEvent> handle(@NotNull ActionData data) {
        final InventoryLocation location = data.location();
        if (location == null) {
            return null;
        }
        final Optional<EventInventory> inventory = inventories.get(data.location());
        return inventory.map(OpenInventoryClickListener::new).orElse(null);
    }

}
