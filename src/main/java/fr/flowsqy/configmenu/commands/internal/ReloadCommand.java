package fr.flowsqy.configmenu.commands.internal;

import fr.flowsqy.configmenu.ConfigMenuPlugin;
import fr.flowsqy.configmenu.commands.CommandManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadCommand implements CommandExecutor {

    private final String reloadMessage;

    public ReloadCommand(ConfigMenuPlugin plugin, YamlConfiguration messages) {
        final PluginCommand reloadCommand = plugin.getCommand("configmenureload");
        assert reloadCommand != null;
        reloadCommand.setTabCompleter(CommandManager.EMPTY_TAB_COMPLETER);
        reloadCommand.setExecutor(this);
        final String rawReloadMessage = messages.getString("commands.reload");
        reloadMessage = rawReloadMessage == null ? null : ChatColor.translateAlternateColorCodes('&', rawReloadMessage);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final ConfigMenuPlugin plugin = JavaPlugin.getPlugin(ConfigMenuPlugin.class);
        plugin.unload();
        plugin.load(true);
        if (reloadMessage != null) {
            sender.sendMessage(reloadMessage);
        }
        return true;
    }

}
