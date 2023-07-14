package fr.flowsqy.configmenu.command;

import fr.flowsqy.configmenu.config.ConfigLoader;
import fr.flowsqy.configmenu.inventory.InventoryLocation;
import fr.flowsqy.configmenu.inventory.InventoryLocationParser;
import fr.flowsqy.dynamiccommand.DynamicCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CommandsLoader {

    private File file;
    private YamlConfiguration configuration;

    public void load(@NotNull ConfigLoader configLoader, @NotNull JavaPlugin javaPlugin, @NotNull String fileName) {
        file = configLoader.initFile(javaPlugin.getDataFolder(), Objects.requireNonNull(javaPlugin.getResource(fileName)), fileName);
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public List<UnlinkedCommand> getCommands(@NotNull Plugin plugin, @NotNull File rootFolder) {
        final InventoryLocationParser inventoryLocationParser = new InventoryLocationParser();
        final List<UnlinkedCommand> commandDataList = new LinkedList<>();
        final String[] commandsNames = configuration.getKeys(false).toArray(new String[0]);
        final PluginCommand[] commands = DynamicCommand.createCommands(plugin, commandsNames);
        for (PluginCommand command : commands) {
            final ConfigurationSection commandSection = configuration.getConfigurationSection(command.getName());
            if (commandSection == null) {
                continue;
            }
            final String inventory = commandSection.getString("inventory");
            if (inventory == null || inventory.isBlank()) {
                continue;
            }
            final InventoryLocation inventoryLocation;
            try {
                inventoryLocation = inventoryLocationParser.parse(inventory, rootFolder, file);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning(e.getMessage());
                continue;
            }
            final List<String> aliases = commandSection.getStringList("aliases");
            if (!aliases.isEmpty()) {
                command.setAliases(aliases);
            }
            final String permission = commandSection.getString("permission");
            if (permission != null && !permission.isBlank()) {
                command.setPermission(permission);
                final String permissionMessage = commandSection.getString("permission-message");
                if (permissionMessage != null && !permissionMessage.isBlank()) {
                    command.setPermissionMessage(ChatColor.translateAlternateColorCodes('&', permissionMessage));
                }
            }

            commandDataList.add(new UnlinkedCommand(command, inventoryLocation));
        }
        return commandDataList;
    }

}
