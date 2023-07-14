package fr.flowsqy.configmenu.inventory;

import fr.flowsqy.abstractmenu.inventory.EventInventory;
import fr.flowsqy.configmenu.message.MessageSenderClickListener;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ClickListenerHandler {

    private final Map<InventoryLocation, Optional<EventInventory>> inventories;
    private final Map<String, BaseComponent[]> messages;

    public ClickListenerHandler(@NotNull Map<InventoryLocation, Optional<EventInventory>> inventories, @NotNull Map<String, BaseComponent[]> messages) {
        this.inventories = inventories;
        this.messages = messages;
    }

    @Nullable
    public Consumer<InventoryClickEvent> handle(@NotNull ActionData data) {
        Consumer<InventoryClickEvent> onClickListener = null;
        final InventoryLocation location = data.location();
        if (location != null) {
            final Optional<EventInventory> inventory = inventories.get(location);
            if (inventory.isPresent()) {
                onClickListener = compose(onClickListener, new OpenInventoryClickListener(inventory.get()));
            }
        }

        final String messageId = data.message();
        if (messageId != null) {
            final BaseComponent[] message = messages.get(messageId);
            if (message != null) {
                onClickListener = compose(onClickListener, new MessageSenderClickListener(message));
            }
        }
        return onClickListener;
    }

    @NotNull
    private Consumer<InventoryClickEvent> compose(@Nullable Consumer<InventoryClickEvent> current, @NotNull Consumer<InventoryClickEvent> then) {
        return current == null ? then : current.andThen(then);
    }

}
