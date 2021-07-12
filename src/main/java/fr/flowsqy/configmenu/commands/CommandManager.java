package fr.flowsqy.configmenu.commands;

import fr.flowsqy.abstractmenu.inventory.EventInventory;
import fr.flowsqy.dynamiccommand.DynamicCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandManager {

    public final static TabCompleter EMPTY_TAB_COMPLETER = new EmptyTabCompleter();

    private String[] registeredCommands;

    public void unregisterCommands() {
        if (registeredCommands != null) {
            DynamicCommand.unregisterCommands(registeredCommands);
            registeredCommands = null;
        }
    }

    public List<UnlinkedCommand> setup(Plugin plugin, YamlConfiguration configuration) {
        final List<UnlinkedCommand> commandDataList = new ArrayList<>();
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
            final List<String> aliases = commandSection.getStringList("aliases");
            if (!aliases.isEmpty())
                command.setAliases(aliases);
            final String permission = commandSection.getString("permission");
            if (permission != null && !permission.isBlank()) {
                command.setPermission(permission);
                final String permissionMessage = commandSection.getString("permission-message");
                if (permissionMessage != null && !permissionMessage.isBlank()) {
                    command.setPermissionMessage(ChatColor.translateAlternateColorCodes('&', permissionMessage));
                }
            }

            commandDataList.add(new UnlinkedCommand(command, inventory));
        }
        return commandDataList;
    }

    public void register(Plugin plugin, List<LinkedCommand> linkedCommands) {
        if (registeredCommands != null)
            throw new RuntimeException("Try to register new commands but the older one are not unregistered");
        for (LinkedCommand linkedCommand : linkedCommands) {
            final PluginCommand command = linkedCommand.command();
            command.setUsage("/<command>");
            command.setDescription("A custom command of ConfigGui to open an inventory");
            command.setTabCompleter(EMPTY_TAB_COMPLETER);
            command.setExecutor(new OpenInventoryExecutor(linkedCommand.inventory()));
        }
        registeredCommands = DynamicCommand.registerCommands(
                plugin,
                linkedCommands.stream().map(LinkedCommand::command).toArray(PluginCommand[]::new)
        ).toArray(new String[0]);
    }

    public record UnlinkedCommand(PluginCommand command, String inventoryPath) {
    }

    public record LinkedCommand(PluginCommand command, EventInventory inventory) {
    }

    private final static class EmptyTabCompleter implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            return Collections.emptyList();
        }
    }

}
