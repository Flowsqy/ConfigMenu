package fr.flowsqy.configmenu;

import fr.flowsqy.abstractmenu.inventory.EventInventory;
import fr.flowsqy.configmenu.command.*;
import fr.flowsqy.configmenu.command.internal.ReloadCommand;
import fr.flowsqy.configmenu.config.ConfigLoader;
import fr.flowsqy.configmenu.inventory.InventoryLoader;
import fr.flowsqy.configmenu.inventory.InventoryLocation;
import fr.flowsqy.dynamiccommand.DynamicCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigMenuPlugin extends JavaPlugin {

    private CommandManager commandManager;

    @Override
    public void onEnable() {
        final Logger logger = getLogger();
        final File dataFolder = getDataFolder();
        final ConfigLoader configLoader = new ConfigLoader();

        if (!configLoader.checkDataFolder(dataFolder)) {
            logger.log(Level.WARNING, "Can not write in the directory : " + dataFolder.getAbsolutePath());
            logger.log(Level.WARNING, "Disable the plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        commandManager = new CommandManager();

        load(false);

        final String messagesFileName = "messages.yml";
        final YamlConfiguration messages = YamlConfiguration.loadConfiguration(configLoader.initFile(dataFolder, Objects.requireNonNull(getResource(messagesFileName)), messagesFileName));
        new ReloadCommand(this, messages);
    }

    @Override
    public void onDisable() {
        unload();
    }

    public void unload() {
        commandManager.unregisterCommands();
    }

    public void load(boolean sync) {
        final ConfigLoader configLoader = new ConfigLoader();
        final String commandsFileName = "commands.yml";
        final CommandsLoader commandsLoader = new CommandsLoader();
        commandsLoader.load(configLoader, this, commandsFileName);
        final List<UnlinkedCommand> commands = commandsLoader.getCommands(this, getDataFolder());
        final InventoryLocation[] locations = commands.stream().map(UnlinkedCommand::inventoryLocation).toArray(InventoryLocation[]::new);
        final InventoryLoader inventoryLoader = new InventoryLoader();
        inventoryLoader.addToProcess(locations);
        final Map<InventoryLocation, Optional<EventInventory>> inventories = inventoryLoader.load(this, getDataFolder());
        inventoryLoader.linkAll();
        final CommandsLinker linker = new CommandsLinker();
        final List<LinkedCommand> linkedCommands = linker.link(inventories, commands);
        commandManager.register(this, linkedCommands);
        if (sync) {
            DynamicCommand.synchronizeTabCompleter();
        }
    }

}