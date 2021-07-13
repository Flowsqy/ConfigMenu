package fr.flowsqy.configmenu;

import fr.flowsqy.configmenu.commands.CommandManager;
import fr.flowsqy.configmenu.commands.internal.ReloadCommand;
import fr.flowsqy.configmenu.inventory.InventoryManager;
import fr.flowsqy.dynamiccommand.DynamicCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigMenuPlugin extends JavaPlugin {

    private CommandManager commandManager;

    @Override
    public void onEnable() {
        final Logger logger = getLogger();
        final File dataFolder = getDataFolder();

        if (!checkDataFolder(dataFolder)) {
            logger.log(Level.WARNING, "Can not write in the directory : " + dataFolder.getAbsolutePath());
            logger.log(Level.WARNING, "Disable the plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        commandManager = new CommandManager();

        load(false);

        new ReloadCommand(this, initFile(dataFolder, "messages.yml"));
    }

    @Override
    public void onDisable() {
        unload();
    }

    public void unload() {
        commandManager.unregisterCommands();
    }

    public void load(boolean sync) {
        final String commandsFileName = "commands.yml";
        final List<CommandManager.UnlinkedCommand> unlinkedCommandList = commandManager.setup(this, initFile(getDataFolder(), commandsFileName));
        final List<CommandManager.LinkedCommand> linkedCommands = new InventoryManager().setup(
                this,
                new File(getDataFolder(), commandsFileName),
                getDataFolder(),
                unlinkedCommandList
        );
        commandManager.register(this, linkedCommands);
        if (sync) {
            DynamicCommand.synchronizeTabCompleter();
        }
    }

    private boolean checkDataFolder(File dataFolder) {
        if (dataFolder.exists())
            return dataFolder.canWrite();
        return dataFolder.mkdirs();
    }

    private YamlConfiguration initFile(File dataFolder, String fileName) {
        final File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            try {
                Files.copy(Objects.requireNonNull(getResource(fileName)), file.toPath());
            } catch (IOException ignored) {
            }
        }

        return YamlConfiguration.loadConfiguration(file);
    }

}