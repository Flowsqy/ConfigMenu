package fr.flowsqy.configmenu.inventory;

import fr.flowsqy.abstractmenu.factory.MenuFactory;
import fr.flowsqy.abstractmenu.inventory.EventInventory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InventoryLoader {

    private final Map<File, Optional<YamlConfiguration>> configurationCache;
    private final Map<InventoryLocation, Optional<EventInventory>> computedInventories;
    private final List<UnregisteredItem> unregisteredItems;
    private final Queue<InventoryLocation> locationQueue;

    public InventoryLoader() {
        configurationCache = new HashMap<>();
        computedInventories = new HashMap<>();
        unregisteredItems = new LinkedList<>();
        locationQueue = new LinkedList<>();
    }

    public Map<InventoryLocation, Optional<EventInventory>> load(@NotNull Plugin plugin, @NotNull File rootFolder) {
        final Logger logger = plugin.getLogger();
        final MenuFactory factory = new MenuFactory(plugin);
        final CMRegisterHandler registerHandler = new CMRegisterHandler(logger, this, rootFolder);
        while (!locationQueue.isEmpty()) {
            final InventoryLocation location = Objects.requireNonNull(locationQueue.remove());
            if (computedInventories.containsKey(location)) {
                continue;
            }
            final Optional<YamlConfiguration> optionalConfiguration;
            try {
                optionalConfiguration = checkConfiguration(location.file());
            } catch (Exception e) {
                computedInventories.put(location, Optional.empty());
                logger.warning(e.getMessage());
                continue;
            }
            if (optionalConfiguration.isEmpty()) {
                computedInventories.put(location, Optional.empty());
                continue;
            }
            final ConfigurationSection inventorySection = optionalConfiguration.get().getConfigurationSection(location.id());
            if (inventorySection == null) {
                computedInventories.put(location, Optional.empty());
                logger.log(Level.WARNING, "The inventory section '" + location.id() + "' does not exist in file " + location.file().getPath());
                continue;
            }
            registerHandler.setCurrent(location, inventorySection);
            final EventInventory inventory = EventInventory.deserialize(
                    inventorySection,
                    factory,
                    registerHandler
            );
            computedInventories.put(location, Optional.ofNullable(inventory));
        }
        return computedInventories;
    }

    private Optional<YamlConfiguration> checkConfiguration(@NotNull File file) throws IOException, InvalidConfigurationException {
        final Optional<YamlConfiguration> cachedConfiguration = configurationCache.get(file);
        if (cachedConfiguration != null) {
            return cachedConfiguration;
        }
        if (!file.exists()) {
            configurationCache.put(file, Optional.empty());
            throw new IllegalArgumentException("The file '" + file.getPath() + "' does not exist");
        }
        if (!file.isFile()) {
            configurationCache.put(file, Optional.empty());
            throw new IllegalArgumentException("The path '" + file.getPath() + "' does not refer to a file");
        }
        final YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            configurationCache.put(file, Optional.empty());
            throw e;
        }
        final Optional<YamlConfiguration> config = Optional.of(configuration);
        configurationCache.put(file, config);
        return config;
    }

    public void addToProcess(@NotNull InventoryLocation... locations) {
        locationQueue.addAll(Arrays.asList(locations));
    }

    public void queueItem(@NotNull UnregisteredItem unregisteredItem) {
        unregisteredItems.add(unregisteredItem);
    }

    public void linkAll() {
        final ClickListenerHandler clickListenerHandler = new ClickListenerHandler(computedInventories);
        for (UnregisteredItem item : unregisteredItems) {
            item.eventInventory().register(item.builder(), clickListenerHandler.handle(item.actionData()), item.slots());
        }
    }

}
