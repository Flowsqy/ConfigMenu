package fr.flowsqy.configmenu.command.internal;

import fr.flowsqy.configmenu.ConfigMenuPlugin;
import fr.flowsqy.configmenu.command.EmptyTabCompleter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final String reloadMessage;

    public ReloadCommand(@NotNull ConfigMenuPlugin plugin, @NotNull YamlConfiguration messages) {
        final PluginCommand reloadCommand = plugin.getCommand("configmenureload");
        assert reloadCommand != null;
        reloadCommand.setTabCompleter(EmptyTabCompleter.INSTANCE);
        reloadCommand.setExecutor(this);
        final String rawReloadMessage = messages.getString("commands.reload");
        reloadMessage = rawReloadMessage == null ? null : ChatColor.translateAlternateColorCodes('&', rawReloadMessage);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final ConfigMenuPlugin plugin = JavaPlugin.getPlugin(ConfigMenuPlugin.class);
        plugin.unload();
        plugin.load(true);
        if (reloadMessage != null) {
            sender.sendMessage(reloadMessage);
        }
        return true;
    }

}
