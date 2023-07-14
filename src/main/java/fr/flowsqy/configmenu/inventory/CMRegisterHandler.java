package fr.flowsqy.configmenu.inventory;

import fr.flowsqy.abstractmenu.inventory.EventInventory;
import fr.flowsqy.abstractmenu.item.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class CMRegisterHandler implements EventInventory.RegisterHandler {

    private final Logger logger;
    private final InventoryLocationParser inventoryLocationParser;
    private final InventoryLoader inventoryLoader;
    private final File rootFolder;
    private InventoryLocation currentLocation;
    private ConfigurationSection currentSection;

    public CMRegisterHandler(@NotNull Logger logger, @NotNull InventoryLoader inventoryLoader, @NotNull File rootFolder) {
        this.logger = logger;
        inventoryLocationParser = new InventoryLocationParser();
        this.inventoryLoader = inventoryLoader;
        this.rootFolder = rootFolder;
    }

    public void setCurrent(@NotNull InventoryLocation location, @NotNull ConfigurationSection section) {
        currentLocation = location;
        currentSection = section;
    }

    @Override
    public void handle(EventInventory inventory, String id, ItemBuilder item, List<Integer> slots) {
        InventoryLocation newLocation = null;
        try {
            newLocation = handleLocation(id);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }

        final String messageId = currentSection.getString("items." + id + ".message");

        final ActionData actionData = new ActionData(newLocation, messageId);
        final UnregisteredItem unregisteredItem = new UnregisteredItem(inventory, item, slots, actionData);
        inventoryLoader.queueItem(unregisteredItem);
    }

    @Nullable
    private InventoryLocation handleLocation(@NotNull String id) {
        final String specifiedLocation = currentSection.getString("items." + id + ".inventory");
        if (specifiedLocation == null) {
            return null;
        }
        final InventoryLocation inventoryLocation = inventoryLocationParser.parse(specifiedLocation, rootFolder, currentLocation.file());
        if (currentLocation.equals(inventoryLocation)) {
            throw new IllegalArgumentException("Try to open the currently opened inventory in '" + inventoryLocation.file().getPath() + "', id '" + inventoryLocation.id() + "'");
        }
        inventoryLoader.addToProcess(inventoryLocation);
        return inventoryLocation;
    }

}
