package fr.flowsqy.configmenu.inventory;

import fr.flowsqy.abstractmenu.factory.MenuFactory;
import fr.flowsqy.abstractmenu.inventory.EventInventory;
import fr.flowsqy.abstractmenu.item.ItemBuilder;
import fr.flowsqy.configmenu.commands.CommandManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InventoryManager {

    public List<CommandManager.LinkedCommand> setup(Plugin plugin, File commandFile, File rootFolder, List<CommandManager.UnlinkedCommand> unlinkedCommands) {
        final Logger logger = plugin.getLogger();
        final MenuFactory factory = new MenuFactory(plugin);
        final List<CommandManager.LinkedCommand> linkedCommands = new ArrayList<>();
        final HashMap<InventoryLocation, EventInventory> inventoryCache = new HashMap<>();
        final HashMap<InventoryLocation, List<ToLinkItem>> toLinkInventories = new HashMap<>();
        for (CommandManager.UnlinkedCommand unlinkedCommand : unlinkedCommands) {
            final InventoryLocation inventoryLocation = parseInventoryLocation(unlinkedCommand.inventoryPath(), logger, rootFolder, commandFile);
            if (inventoryLocation == null) {
                continue;
            }
            final EventInventory inventory = inventoryCache.getOrDefault(
                    inventoryLocation,
                    createInventory(
                            factory,
                            logger,
                            rootFolder,
                            inventoryLocation,
                            inventoryCache,
                            toLinkInventories
                    )
            );
            if (inventory == null)
                continue;
            linkedCommands.add(new CommandManager.LinkedCommand(unlinkedCommand.command(), inventory));
        }
        return linkedCommands;
    }

    private EventInventory createInventory(
            MenuFactory factory,
            Logger logger,
            File rootFolder,
            InventoryLocation inventoryLocation,
            HashMap<InventoryLocation, EventInventory> inventoryCache,
            HashMap<InventoryLocation, List<ToLinkItem>> toLinkInventories
    ) {
        if (inventoryLocation == null) {
            return null;
        }
        // Check configuration section
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(inventoryLocation.file());
        final ConfigurationSection inventorySection = configuration.getConfigurationSection(inventoryLocation.id());
        if (inventorySection == null) {
            logger.log(Level.WARNING, "The inventory section '" + inventoryLocation.id() + "' does not exist in file " + inventoryLocation.file().getPath());
            return null;
        }

        toLinkInventories.put(inventoryLocation, new ArrayList<>());
        // Inventory deserialization recursively
        final EventInventory inventory = EventInventory.deserialize(
                inventorySection,
                factory,
                new RecursiveRegisterHandler(
                        factory,
                        logger,
                        rootFolder,
                        inventoryLocation,
                        inventoryCache,
                        toLinkInventories,
                        inventorySection
                )
        );

        final List<ToLinkItem> toLinkItems = toLinkInventories.remove(inventoryLocation);
        if (inventory != null) {
            inventoryCache.put(inventoryLocation, inventory);
            for (ToLinkItem toLinkItem : toLinkItems) {
                toLinkItem.inventory().register(toLinkItem.builder(), new OpenInventoryConsumer(inventory), toLinkItem.slots());
            }
        } else {
            for (ToLinkItem toLinkItem : toLinkItems) {
                toLinkItem.inventory().register(toLinkItem.builder(), toLinkItem.slots());
            }
        }

        return inventory;
    }

    private InventoryLocation parseInventoryLocation(String inventoryPath, Logger logger, File rootFolder, File currentFile) {
        // Path parsing
        final File file;
        final String inventoryId;
        if (inventoryPath == null || inventoryPath.isBlank())
            return null;
        if (inventoryPath.contains(":")) {
            final String[] pathParts = inventoryPath.split(":");
            if (pathParts.length != 2) {
                logger.log(Level.WARNING, "The path '" + inventoryPath + "' is incorrect. It should contains only one ':'");
                return null;
            }
            if (pathParts[0].isBlank()) {
                logger.log(Level.WARNING, "The path '" + pathParts[0] + "' is incorrect (blank, no file specified)");
                return null;
            }
            if (pathParts[1].isBlank()) {
                logger.log(Level.WARNING, "The id '" + pathParts[1] + "' is incorrect (blank, no inventory id specified)");
                return null;
            }
            file = new File(rootFolder, pathParts[0].trim().replace('/', File.separatorChar));
            inventoryId = pathParts[1].trim();
        } else {
            inventoryId = inventoryPath.trim();
            file = currentFile;
        }

        // File checking
        if (!file.exists()) {
            logger.log(Level.WARNING, "The file '" + file.getPath() + "' does not exist");
            return null;
        }
        if (!file.isFile()) {
            logger.log(Level.WARNING, "The path '" + file.getPath() + "' does not refer to a file");
            return null;
        }

        return new InventoryLocation(file, inventoryId);
    }

    private record InventoryLocation(File file, String id) {
    }

    private record ToLinkItem(EventInventory inventory, ItemBuilder builder, List<Integer> slots) {
    }

    private record OpenInventoryConsumer(EventInventory inventory) implements Consumer<InventoryClickEvent> {

        @Override
        public void accept(InventoryClickEvent event) {
            inventory.open((Player) event.getWhoClicked(), inventory.toString());
        }
    }

    private final class RecursiveRegisterHandler implements EventInventory.RegisterHandler {

        private final MenuFactory factory;
        private final Logger logger;
        private final File rootFolder;
        private final InventoryLocation currentLocation;
        private final HashMap<InventoryLocation, EventInventory> inventoryCache;
        private final HashMap<InventoryLocation, List<ToLinkItem>> toLinkInventories;
        private final ConfigurationSection section;

        public RecursiveRegisterHandler(
                MenuFactory factory,
                Logger logger,
                File rootFolder,
                InventoryLocation currentLocation,
                HashMap<InventoryLocation, EventInventory> inventoryCache,
                HashMap<InventoryLocation, List<ToLinkItem>> toLinkInventories,
                ConfigurationSection section
        ) {
            this.factory = factory;
            this.logger = logger;
            this.rootFolder = rootFolder;
            this.currentLocation = currentLocation;
            this.inventoryCache = inventoryCache;
            this.toLinkInventories = toLinkInventories;
            this.section = section;
        }

        @Override
        public void handle(EventInventory inventory, String id, ItemBuilder item, List<Integer> slots) {
            // Parse path
            final InventoryLocation inventoryLocation = parseInventoryLocation(
                    section.getString("items." + id + ".action"),
                    logger,
                    rootFolder,
                    currentLocation.file()
            );
            if (inventoryLocation != null) {
                if (inventoryLocation.equals(currentLocation)) {
                    logger.log(Level.WARNING, "Try to open the currently opened inventory in " + inventoryLocation.file().getPath() + ", id " + inventoryLocation.id());
                } else {
                    final List<ToLinkItem> toLinkItems = toLinkInventories.get(inventoryLocation);
                    // Report link operation to avoid infinite loop (because inventory is already in deserialize process)
                    if (toLinkItems != null) {
                        toLinkItems.add(new ToLinkItem(inventory, item, slots));
                    } else {
                        final EventInventory eventInventory = inventoryCache.getOrDefault(
                                inventoryLocation,
                                createInventory(
                                        factory,
                                        logger,
                                        rootFolder,
                                        inventoryLocation,
                                        inventoryCache,
                                        toLinkInventories
                                )
                        );
                        inventory.register(item, new OpenInventoryConsumer(eventInventory), slots);
                        return;
                    }
                }
            }
            inventory.register(item, slots);
        }
    }

}
