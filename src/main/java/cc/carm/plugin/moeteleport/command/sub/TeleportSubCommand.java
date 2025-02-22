package cc.carm.plugin.moeteleport.command.sub;

import cc.carm.lib.easyplugin.command.SubCommand;
import cc.carm.plugin.moeteleport.MoeTeleport;
import cc.carm.plugin.moeteleport.command.parent.TeleportCommands;
import cc.carm.plugin.moeteleport.storage.UserData;
import cc.carm.plugin.moeteleport.teleport.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class TeleportSubCommand extends SubCommand<TeleportCommands> {

    public TeleportSubCommand(@NotNull TeleportCommands parent, String name, String... aliases) {
        super(parent, name, aliases);
    }

    public @NotNull UserData getData(Player player) {
        return MoeTeleport.getUserManager().getData(player);
    }

    public @Nullable UserData getData(UUID player) {
        return MoeTeleport.getUserManager().getData(player);
    }

    public @NotNull List<String> listRequests(CommandSender sender, String input) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        return getReceivedRequests((Player) sender).keySet().stream()
                .map(Bukkit::getPlayer).filter(Objects::nonNull).map(HumanEntity::getName)
                .filter(s -> StringUtil.startsWithIgnoreCase(s, input))
                .limit(10).collect(Collectors.toList());
    }

    public @NotNull Map<UUID, TeleportRequest> getReceivedRequests(Player player) {
        return MoeTeleport.getRequestManager().getUserReceivedRequests(player.getUniqueId());
    }

}