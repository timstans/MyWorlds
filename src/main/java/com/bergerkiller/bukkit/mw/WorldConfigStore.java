package com.bergerkiller.bukkit.mw;

import java.util.Collection;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.bergerkiller.bukkit.common.collections.StringMapCaseInsensitive;
import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.bergerkiller.bukkit.common.utils.WorldUtil;

public class WorldConfigStore {
	protected static StringMapCaseInsensitive<WorldConfig> worldConfigs = new StringMapCaseInsensitive<WorldConfig>();
	private static FileConfiguration defaultProperties;

	public static WorldConfig get(String worldname) {
		WorldConfig c = worldConfigs.get(worldname);
		if (c == null) {
			c = new WorldConfig(worldname);
			if (defaultProperties != null) {
				// Load using a clone to prevent altering the original
				c.load(defaultProperties.clone());
			}
			c.loadNew();
		}
		return c;
	}
	public static WorldConfig get(World world) {
		return get(world.getName());
	}
	public static WorldConfig get(Entity entity) {
		return get(entity.getWorld());
	}
	public static WorldConfig get(Location location) {
		return get(location.getWorld());
	}
	public static WorldConfig get(Block block) {
		return get(block.getWorld());
	}

	/**
	 * Copies all information from one world configuration to a (new?) world configuration
	 * 
	 * @param config to copy from
	 * @param toWorldName to which has to be copied
	 * @return The (new) world configuration to which was written
	 */
	public static WorldConfig copy(WorldConfig config, String toWorldName) {
		WorldConfig newConfig = get(toWorldName);
		newConfig.load(config);
		return newConfig;
	}

	public static Collection<WorldConfig> all() {
		return worldConfigs.values();
	}
	public static boolean exists(String worldname) {
		return worldConfigs.containsKey(worldname.toLowerCase());
	}
	public static void init() {
		// Default configuration
		defaultProperties = new FileConfiguration(MyWorlds.plugin, "defaultproperties.yml");
		defaultProperties.setHeader("This file contains the default world properties applied when loading or creating completely new worlds");
		defaultProperties.addHeader("All the nodes found in the worlds.yml can be set here");
		if (defaultProperties.exists()) {
			defaultProperties.load();
		} else {
			// Generate new properties
			new WorldConfig(null).save(defaultProperties);
			defaultProperties.save();
		}

		// Worlds configuration
		worldConfigs.clear();
		FileConfiguration config = new FileConfiguration(MyWorlds.plugin, "worlds.yml");
		config.load();
		for (ConfigurationNode node : config.getNodes()) {
			String worldname = node.get("name", node.getName());
			if (WorldManager.worldExists(worldname)) {
				WorldConfig wc = new WorldConfig(worldname);
				wc.load(node);
				if (node.get("loaded", false)) {
					wc.loadWorld();
				}
			} else {
				MyWorlds.plugin.log(Level.WARNING, "World: " + node.getName() + " no longer exists, data will be wiped when disabling!");
			}
		}

		// Update any remaining worlds
		for (World world : WorldUtil.getWorlds()) {
			get(world).onWorldLoad(world);
		}
	}
	public static void saveAll() {
		FileConfiguration cfg = new FileConfiguration(MyWorlds.plugin, "worlds.yml");
		for (WorldConfig wc : all()) {
			wc.save(cfg.getNode(wc.getConfigName()));
		}
		cfg.save();
	}
	public static void deinit() {
		// Tell all loaded worlds to unload (for MyWorlds) to properly handle disabling
		for (World world : WorldUtil.getWorlds()) {
			get(world).onWorldUnload(world);
		}
		// Save the current world configurations
		saveAll();
		// De-initialize some data
		defaultProperties = null;
	}

	public static void remove(String worldname) {
		worldConfigs.remove(worldname.toLowerCase());
	}
}
