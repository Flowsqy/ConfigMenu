package fr.flowsqy.configmenu.commands;

import fr.flowsqy.configmenu.ConfigMenuPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements CommandExecutor {

    public final static TabCompleter EMPTY_TAB_COMPLETER = new EmptyTabCompleter();

    private final String reloadMessage;

    public ReloadCommand(ConfigMenuPlugin plugin, YamlConfiguration messages) {
        final PluginCommand reloadCommand = plugin.getCommand("configmenureload");
        assert reloadCommand != null;
        reloadCommand.setTabCompleter(EMPTY_TAB_COMPLETER);
        reloadCommand.setExecutor(this);
        final String rawReloadMessage = messages.getString("commands.reload");
        reloadMessage = rawReloadMessage == null ? null : ChatColor.translateAlternateColorCodes('&', rawReloadMessage);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //TODO Link to reload method
        if(reloadMessage != null){
            sender.sendMessage(reloadMessage);
        }
        return true;
    }

    // TODO Move it in an appropriate class
    private final static class EmptyTabCompleter implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            return Collections.emptyList();
        }
    }

}
