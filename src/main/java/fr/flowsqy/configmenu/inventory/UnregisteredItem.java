package fr.flowsqy.configmenu.inventory;

import fr.flowsqy.abstractmenu.inventory.EventInventory;
import fr.flowsqy.abstractmenu.item.ItemBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record UnregisteredItem(@NotNull EventInventory eventInventory, @NotNull ItemBuilder builder,
                               @NotNull List<Integer> slots, @NotNull ActionData actionData) {
}
