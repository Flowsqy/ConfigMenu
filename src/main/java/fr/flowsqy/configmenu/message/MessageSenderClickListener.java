package fr.flowsqy.configmenu.message;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class MessageSenderClickListener implements Consumer<InventoryClickEvent> {

    private final BaseComponent[] message;

    public MessageSenderClickListener(@NotNull BaseComponent[] message) {
        this.message = message;
    }

    @Override
    public void accept(InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.getWhoClicked().spigot().sendMessage(message);
    }

}
