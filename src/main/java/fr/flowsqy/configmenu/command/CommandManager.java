package fr.flowsqy.configmenu.command;

import fr.flowsqy.dynamiccommand.DynamicCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandManager {

    private String[] registeredCommands;

    public void unregisterCommands() {
        if (registeredCommands == null) {
            return;
        }
        DynamicCommand.unregisterCommands(registeredCommands);
        registeredCommands = null;
    }

    public void register(@NotNull Plugin plugin, @NotNull List<LinkedCommand> linkedCommands) {
        if (registeredCommands != null) {
            throw new RuntimeException("Try to register new commands but the older one are not unregistered");
        }
        for (LinkedCommand linkedCommand : linkedCommands) {
            final PluginCommand command = linkedCommand.command();
            command.setUsage("/<command>");
            command.setDescription("A custom command of ConfigGui to open an inventory");
            command.setTabCompleter(EmptyTabCompleter.INSTANCE);
            command.setExecutor(new OpenInventoryExecutor(linkedCommand.inventory()));
        }
        registeredCommands = DynamicCommand.registerCommands(
                plugin,
                linkedCommands.stream().map(LinkedCommand::command).toArray(PluginCommand[]::new)
        ).toArray(new String[0]);
    }

}
