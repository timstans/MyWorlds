package com.bergerkiller.bukkit.mw;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;

public class Permission {
	private static PermissionHandler permissionHandler = null; //Permissions 3.* ONLY
	public static void init(JavaPlugin plugin) {
		if (MyWorlds.usePermissions) {
			Plugin permissionsPlugin = plugin.getServer().getPluginManager().getPlugin("Permissions");
			if (permissionsPlugin == null) {
				MyWorlds.log(Level.WARNING, "Permission system not detected, defaulting to build-in permissions!");
			} else {
				permissionHandler = ((Permissions) permissionsPlugin).getHandler();
				MyWorlds.log(Level.INFO, "Found and will use permissions plugin "+((Permissions)permissionsPlugin).getDescription().getFullName());
			}
		} else {
			MyWorlds.log(Level.INFO, "Using build-in 'Bukkit SuperPerms' as permissions plugin!");;
		}
	}
	public static boolean has(CommandSender sender, String command) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (permissionHandler != null) {
				//Permissions 3.*
				return permissionHandler.has(player, "myworlds." + command);
			} else {
				//Build-in permissions
				return ((Player) sender).hasPermission("myworlds." + command);
			}
		} else {
			if (command.equalsIgnoreCase("world.spawn")) return false;
			if (command.equalsIgnoreCase("tpp")) return false;
			return true;
		}
	}
		
	public static boolean handleTeleport(Entity entity, Portal portal) {
		return handleTeleport(entity, portal.getName(), portal.getDestinationName(), portal.getDestinationDisplayName(), portal.getDestination());
	}
	public static boolean handleTeleport(Entity entity, String toportalname, Location portalloc) {
		return handleTeleport(entity, null, toportalname, toportalname, portalloc);
	}
	public static boolean handleTeleport(Entity entity, String fromportalname, String toportalname, String toportaldispname, Location portalloc) {
		if (toportaldispname == null || portalloc == null) return false;
		if (entity instanceof Player) {
			Player p = (Player) entity;
			if (fromportalname != null && !canEnterPortal(p, fromportalname)) {
				Localization.message(p, "portal.noaccess");
				return false;
			} else {
				p.sendMessage(Localization.getPortalEnter(toportalname, toportaldispname));
			}
		}
		return handleTeleport(entity, portalloc, false);
	}
	
	public static boolean handleTeleport(Entity entity, Location to) {
		return handleTeleport(entity, to, true);
	}
	public static boolean handleTeleport(Entity entity, Location to, boolean showworld) {
		if (to == null) return false;
		if (entity instanceof Player) {
			Player p = (Player) entity;
			if (!canEnter(p, to.getWorld())) {
				Localization.message(p, "world.noaccess");
				return false;
			} else if (showworld) {
				p.sendMessage(Localization.getWorldEnter(to.getWorld()));
			}
		}
		entity.teleport(to);
		return true;
	}
		
	public static boolean canEnter(Player player, Portal portal) {
		return canEnterPortal(player, portal.getName());
	}
	public static boolean canEnter(Player player, World world) {
		return canEnterWorld(player, world.getName());
	}
	public static boolean canEnterPortal(Player player, String portalname) {
		if (!has(player, "portal.use")) return false;
		if (!MyWorlds.usePortalEnterPermissions) return true;	
		return has(player, "portal.enter." + portalname) || has(player, "portal.enter.*");
	}
	public static boolean canEnterWorld(Player player, String worldname) {
		if (!MyWorlds.useWorldEnterPermissions) return true;
		return has(player, "world.enter." + worldname) || has(player, "world.enter.*");
	}
	
	public static boolean canTeleportPortal(Player player, String portalname) {
		if (!MyWorlds.usePortalTeleportPermissions) return true;
		return has(player, "portal.teleport." + portalname) || has(player, "portal.teleport.*");
	}
	public static boolean canTeleportWorld(Player player, String worldname) {
		if (!MyWorlds.useWorldTeleportPermissions) return true;
		return has(player, "world.teleport." + worldname) || has(player, "world.teleport.*");
	}

	public static boolean canBuild(Player player) {
		if (player == null) return true;
		return canBuild(player, player.getWorld().getName());
	}
	public static boolean canUse(Player player) {
		if (player == null) return true;
		return canUse(player, player.getWorld().getName());
	}
	public static boolean canBuild(Player player, String worldname) {
		if (player == null) return true;
		if (!MyWorlds.useWorldBuildPermissions) return true;
		return has(player, "world.build." + worldname) || has(player, "world.build.*");
	}
	public static boolean canUse(Player player, String worldname) {
		if (player == null) return true;
		if (!MyWorlds.useWorldUsePermissions) return true;
		return has(player, "world.use." + worldname) || has(player, "world.use.*");
	}
}