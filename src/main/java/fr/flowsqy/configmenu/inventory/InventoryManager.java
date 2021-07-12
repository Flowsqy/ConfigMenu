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

    public List<CommandManager.LinkedCommand> setup(Plugin plugin, Logger logger, File rootFolder, List<CommandManager.UnlinkedCommand> unlinkedCommands) {
        final MenuFactory factory = new MenuFactory(plugin);
        final List<CommandManager.LinkedCommand> linkedCommands = new ArrayList<>();
        final HashMap<String, EventInventory> inventoryCache = new HashMap<>();
        for (CommandManager.UnlinkedCommand unlinkedCommand : unlinkedCommands) {
            final EventInventory inventory = inventoryCache.getOrDefault(
                    unlinkedCommand.inventoryPath(),
                    createInventory(factory, logger, rootFolder, unlinkedCommand.inventoryPath(), inventoryCache)
            );
            if (inventory == null)
                continue;
            linkedCommands.add(new CommandManager.LinkedCommand(unlinkedCommand.command(), inventory));
        }
        return linkedCommands;
    }

    public EventInventory createInventory(MenuFactory factory, Logger logger, File rootFolder, String inventoryPath, HashMap<String, EventInventory> inventoryCache) {
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
            file = new File(rootFolder, pathParts[0].replace('/', File.separatorChar));
            inventoryId = pathParts[1];
        } else {
            inventoryId = inventoryPath;
            file = new File(rootFolder, "inventories.yml");
        }

        // File loading
        if (!file.exists()) {
            logger.log(Level.WARNING, "The file '" + file.getPath() + "' does not exist");
            return null;
        }
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        final ConfigurationSection inventorySection = configuration.getConfigurationSection(inventoryId);
        if (inventorySection == null) {
            logger.log(Level.WARNING, "The inventory section '" + inventoryId + "' does not exist in file " + file.getPath());
            return null;
        }

        // Inventory deserialization recursively
        return EventInventory.deserialize(
                inventorySection,
                factory,
                new RecursiveRegisterHandler(
                        factory,
                        logger,
                        rootFolder,
                        inventorySection,
                        inventoryCache,
                        inventoryPath
                )
        );
    }

    private record OpenInventoryConsumer(EventInventory inventory) implements Consumer<InventoryClickEvent> {

        @Override
        public void accept(InventoryClickEvent event) {
            inventory.open((Player) event.getWhoClicked());
        }
    }

    private final class RecursiveRegisterHandler implements EventInventory.RegisterHandler {

        private final MenuFactory factory;
        private final Logger logger;
        private final File rootFolder;
        private final ConfigurationSection section;
        private final HashMap<String, EventInventory> inventoryCache;
        private final String currentPath;

        public RecursiveRegisterHandler(MenuFactory factory, Logger logger, File rootFolder, ConfigurationSection section, HashMap<String, EventInventory> inventoryCache, String currentPath) {
            this.factory = factory;
            this.logger = logger;
            this.rootFolder = rootFolder;
            this.section = section;
            this.inventoryCache = inventoryCache;
            this.currentPath = currentPath;
        }

        @Override
        public void handle(EventInventory inventory, String id, ItemBuilder item, List<Integer> slots) {
            final String inventoryPath = section.getString("items." + id + ".action");
            final EventInventory eventInventory;
            if (inventoryPath != null) {
                if (currentPath.equals(inventoryPath)) {
                    eventInventory = null;
                    logger.log(Level.WARNING, "Try to open the currently opened inventory in " + currentPath);
                } else {
                    eventInventory = inventoryCache.getOrDefault(
                            inventoryPath,
                            createInventory(factory, logger, rootFolder, inventoryPath, inventoryCache)
                    );
                }
            } else {
                eventInventory = null;
            }
            if (eventInventory == null) {
                inventory.register(item, slots);
            } else {
                inventory.register(item, new OpenInventoryConsumer(eventInventory), slots);
            }
        }
    }

}
