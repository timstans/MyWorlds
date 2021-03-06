package com.bergerkiller.bukkit.mw;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.utils.CommonUtil;
import com.bergerkiller.bukkit.common.utils.MaterialUtil;
import com.bergerkiller.bukkit.common.utils.ParseUtil;
import com.bergerkiller.bukkit.common.utils.WorldUtil;

public class WorldConfig extends WorldConfigStore {
	public String worldname;
	public boolean keepSpawnInMemory = true;
	public WorldMode worldmode;
	private String chunkGeneratorName;
	public Difficulty difficulty = Difficulty.NORMAL;
	public Position spawnPoint;
	public GameMode gameMode = null;
	public boolean holdWeather = false;
	public boolean pvp = true;
	public final SpawnControl spawnControl = new SpawnControl();
	public final TimeControl timeControl = new TimeControl(this);
	private String defaultNetherPortal;
	private String defaultEndPortal;
	public List<String> OPlist = new ArrayList<String>();
	public boolean autosave = true;
	public boolean reloadWhenEmpty = false;
	public boolean formSnow = true;
	public boolean formIce = true;
	public boolean showRain = true;
	public boolean showSnow = true;
	public boolean clearInventory = false;
	public boolean forcedRespawn = false;
	public WorldInventory inventory;

	public WorldConfig(String worldname) {
		this.worldname = worldname;
		if (worldname == null) {
			return;
		}
		worldname = worldname.toLowerCase();
		worldConfigs.put(worldname, this);
		World world = this.getWorld();
		if (world != null) {
			this.keepSpawnInMemory = world.getKeepSpawnInMemory();
			this.worldmode = WorldMode.get(world);
			this.difficulty = world.getDifficulty();
			this.spawnPoint = new Position(world.getSpawnLocation());
			this.pvp = world.getPVP();
			this.autosave = world.isAutoSave();
			this.getChunkGeneratorName();
		} else {
			this.worldmode = WorldMode.get(worldname);
			this.spawnPoint = new Position(worldname, 0, 128, 0);
		}
		if (MyWorlds.useWorldOperators) {
			for (OfflinePlayer op : Bukkit.getServer().getOperators()) {
				this.OPlist.add(op.getName());
			}
		}
		this.inventory = new WorldInventory(this.worldname).add(worldname);
	}

	/**
	 * Sets the generator name and arguments for this World.
	 * Note that this does not alter the generator for a possible loaded world.
	 * Only after re-loading does this take effect.
	 * 
	 * @param name to set to
	 */
	public void setChunkGeneratorName(String name) {
		this.chunkGeneratorName = name;
	}

	/**
	 * Gets the generator name and arguments of this World
	 * 
	 * @return Chunk Generator name and arguments
	 */
	public String getChunkGeneratorName() {
		if (this.chunkGeneratorName == null) {
			World world = this.getWorld();
			if (world != null) {
				ChunkGenerator gen = world.getGenerator();
				if (gen != null) {
					Plugin genPlugin = CommonUtil.getPluginByClass(gen.getClass());
					if (genPlugin != null) {
						this.chunkGeneratorName = genPlugin.getName();
					}
				}
			}
		}
		return this.chunkGeneratorName;
	}

	/**
	 * Handles the case of this configuration being made for a new world
	 */
	public void loadNew() {
		this.gameMode = Bukkit.getDefaultGameMode();
	}

	public void load(WorldConfig config) {
		this.keepSpawnInMemory = config.keepSpawnInMemory;
		this.worldmode = config.worldmode;
		this.chunkGeneratorName = config.chunkGeneratorName;
		this.difficulty = config.difficulty;
		this.spawnPoint = config.spawnPoint.clone();
		this.gameMode = config.gameMode;
		this.holdWeather = config.holdWeather;
		this.pvp = config.pvp;
		this.spawnControl.deniedCreatures.clear();
		this.spawnControl.deniedCreatures.addAll(config.spawnControl.deniedCreatures);
		this.timeControl.setLocking(config.timeControl.isLocked());
		this.timeControl.setTime(timeControl.getTime());
		this.autosave = config.autosave;
		this.reloadWhenEmpty = config.reloadWhenEmpty;
		this.formSnow = config.formSnow;
		this.formIce = config.formIce;
		this.showRain = config.showRain;
		this.showSnow = config.showSnow;
		this.clearInventory = config.clearInventory;
		this.forcedRespawn = config.forcedRespawn;
		this.inventory = config.inventory.add(this.worldname);
	}

	public void load(ConfigurationNode node) {
		this.keepSpawnInMemory = node.get("keepSpawnLoaded", this.keepSpawnInMemory);
		this.worldmode = node.get("environment", this.worldmode);
		this.chunkGeneratorName = node.get("chunkGenerator", String.class, this.chunkGeneratorName);
		this.difficulty = node.get("difficulty", Difficulty.class, this.difficulty);
		this.gameMode = node.get("gamemode", GameMode.class, this.gameMode);
		this.clearInventory = node.get("clearInventory", this.clearInventory);
		String worldspawn = node.get("spawn.world", String.class);
		if (worldspawn != null) {
			double x = node.get("spawn.x", 0.0);
			double y = node.get("spawn.y", 64.0);
			double z = node.get("spawn.z", 0.0);
			double yaw = node.get("spawn.yaw", 0.0);
			double pitch = node.get("spawn.pitch", 0.0);
			this.spawnPoint = new Position(worldspawn, x, y, z, (float) yaw, (float) pitch);
		}
		this.holdWeather = node.get("holdWeather", this.holdWeather);
		this.formIce = node.get("formIce", this.formIce);
		this.formSnow = node.get("formSnow", this.formSnow);
		this.showRain = node.get("showRain", this.showRain);
		this.showSnow = node.get("showSnow", this.showSnow);
		this.pvp = node.get("pvp", this.pvp);
		this.forcedRespawn = node.get("forcedRespawn", this.forcedRespawn);
		this.reloadWhenEmpty = node.get("reloadWhenEmpty", this.reloadWhenEmpty);
		for (String type : node.getList("deniedCreatures", String.class)) {
			type = type.toUpperCase();
			if (type.equals("ANIMALS")) {
				this.spawnControl.setAnimals(true);
			} else if (type.equals("MONSTERS")) {
				this.spawnControl.setMonsters(true);
			} else {
				EntityType t = ParseUtil.parseEnum(EntityType.class, type, null);
				if (t != null) {
					this.spawnControl.deniedCreatures.add(t);
				}
			}
		}
    	long time = node.get("lockedtime", Integer.MIN_VALUE);
    	if (time != Integer.MIN_VALUE) {
			this.timeControl.setTime(time);
			this.timeControl.setLocking(true);
    	}
		this.defaultNetherPortal = node.get("defaultNetherPortal", String.class, this.defaultNetherPortal);
		this.defaultEndPortal = node.get("defaultEndPortal", String.class, this.defaultEndPortal);
    	if (node.contains("defaultPortal")) {
    		// Compatibility mode
    		this.defaultNetherPortal = node.get("defaultPortal", String.class, this.defaultNetherPortal);
    		node.set("defaultPortal", null);
    	}
    	this.OPlist = node.getList("operators", String.class, this.OPlist);
	}

	public void save(ConfigurationNode node) {
		//Set if the world can be directly accessed
		World w = this.getWorld();
		if (w != null) {
	        this.difficulty = w.getDifficulty();
	        this.keepSpawnInMemory = w.getKeepSpawnInMemory();
	        this.autosave = w.isAutoSave();
		}
		if (this.worldname == null || this.worldname.equals(this.getConfigName())) {
			node.remove("name");
		} else {
			node.set("name", this.worldname);
		}
		node.set("loaded", w != null);
		node.set("keepSpawnLoaded", this.keepSpawnInMemory);
		node.set("environment", this.worldmode);
		node.set("chunkGenerator", this.getChunkGeneratorName());
		node.set("clearInventory", this.clearInventory ? true : null);
		node.set("gamemode", this.gameMode);

		if (this.timeControl.isLocked()) {
			node.set("lockedtime", this.timeControl.getTime());
		} else {
			node.remove("lockedtime");
		}

		ArrayList<String> creatures = new ArrayList<String>();
		for (EntityType type : this.spawnControl.deniedCreatures) {
			creatures.add(type.name());
		}
		node.set("forcedRespawn", this.forcedRespawn);
		node.set("pvp", this.pvp);
		node.set("defaultNetherPortal", this.defaultNetherPortal);
		node.set("defaultEndPortal", this.defaultEndPortal);
		node.set("operators", this.OPlist);
		node.set("deniedCreatures", creatures);
		node.set("holdWeather", this.holdWeather);
		node.set("formIce", this.formIce);
		node.set("formSnow", this.formSnow);
		node.set("showRain", this.showRain);
		node.set("showSnow", this.showSnow);
		node.set("difficulty", this.difficulty.toString());
		node.set("reloadWhenEmpty", this.reloadWhenEmpty);
		if (this.spawnPoint == null) {
			node.remove("spawn");
		} else {
			node.set("spawn.world", this.spawnPoint.getWorldName());
			node.set("spawn.x", this.spawnPoint.getX());
			node.set("spawn.y", this.spawnPoint.getY());
			node.set("spawn.z", this.spawnPoint.getZ());
			node.set("spawn.yaw", (double) this.spawnPoint.getYaw());
			node.set("spawn.pitch", (double) this.spawnPoint.getPitch());
		}
	}

	/**
	 * Regenerates the spawn point for a world if it is not properly set<br>
	 * Also updates the spawn position in the world configuration
	 * 
	 * @param world to regenerate the spawn point for
	 */
	public void fixSpawnLocation() {
		// Obtain the configuration and the set spawn position from it
		World world = spawnPoint.getWorld();
		if (world == null) {
			return;
		}

		Environment env = world.getEnvironment();
		if (env == Environment.NETHER || env == Environment.THE_END) {
			// Use a portal agent to generate the world spawn point
			Location loc = WorldUtil.findSpawnLocation(spawnPoint);
			if (loc == null) {
				return; // Failure?
			}
			spawnPoint = new Position(loc);
		} else {
			spawnPoint.setY(world.getHighestBlockYAt(spawnPoint));
		}

		// Minor offset
		spawnPoint.setX(0.5 + (double) spawnPoint.getBlockX());
		spawnPoint.setY(0.5 + (double) spawnPoint.getBlockY());
		spawnPoint.setZ(0.5 + (double) spawnPoint.getBlockZ());

		// Apply position to the world if same world
		if (!isOtherWorldSpawn()) {
			world.setSpawnLocation(spawnPoint.getBlockX(), spawnPoint.getBlockY(), spawnPoint.getBlockZ());
		}
	}
	
	public boolean isOtherWorldSpawn() {
		return !spawnPoint.getWorldName().equalsIgnoreCase(worldname);
	}

	public void setNetherPortal(String destination) {
		this.defaultNetherPortal = destination;
	}

	public void setEndPortal(String destination) {
		this.defaultEndPortal = destination;
	}

	public String getNetherPortal() {
		if (this.defaultNetherPortal == null) {
			final String wname = this.worldname.toLowerCase();
			if (this.worldmode == WorldMode.NETHER) {
				// From nether to overworld
				if (wname.endsWith("_nether")) {
					this.defaultNetherPortal = this.worldname.substring(0, wname.length() - 7);
				} else if (wname.equals("dim-1")) {
					this.defaultNetherPortal = MyWorlds.getMainWorld().getName();
				}
			} else if (this.worldmode == WorldMode.THE_END) {
				// From the_end to nether
				if (wname.endsWith("_the_end")) {
					this.defaultNetherPortal = this.worldname.substring(0, wname.length() - 8) + "_nether";
				} else if (wname.equals("dim1")) {
					this.defaultNetherPortal = "DIM-1";
				}
			} else {
				// From overworld to nether
				if (this.getWorld() == MyWorlds.getMainWorld() && WorldManager.worldExists("DIM-1")) {
					this.defaultNetherPortal = "DIM-1";
				} else {
					this.defaultNetherPortal = this.worldname + "_nether";
				}
			}
		}
		return this.defaultNetherPortal;
	}

	public String getEndPortal() {
		if (this.defaultEndPortal == null) {
			final String wname = this.worldname.toLowerCase();
			if (this.worldmode == WorldMode.NETHER) {
				// From nether to the_end
				if (wname.endsWith("_nether")) {
					this.defaultEndPortal = this.worldname.substring(0, wname.length() - 7) + "_the_end";
				} else if (wname.equals("dim-1")) {
					this.defaultEndPortal = "DIM1";
				}
			} else if (this.worldmode == WorldMode.THE_END) {
				// From the_end to overworld
				if (wname.endsWith("_the_end")) {
					this.defaultEndPortal = this.worldname.substring(0, wname.length() - 8);
				} else if (wname.equals("dim1")) {
					this.defaultEndPortal = MyWorlds.getMainWorld().getName();
				}
			} else {
				// From overworld to the_end
				if (this.getWorld() == MyWorlds.getMainWorld() && WorldManager.worldExists("DIM1")) {
					this.defaultEndPortal = "DIM1";
				} else {
					this.defaultEndPortal = this.worldname + "_the_end";
				}
			}
		}
		return this.defaultEndPortal;
	}

	public void onWorldLoad(World world) {
		// Fix spawn point if needed
		if (MaterialUtil.SUFFOCATES.get(this.spawnPoint.getBlock())) {
			this.fixSpawnLocation();
		} else if (!isOtherWorldSpawn()) {
			world.setSpawnLocation(this.spawnPoint.getBlockX(), this.spawnPoint.getBlockY(), this.spawnPoint.getBlockZ());
		}
		// Update world settings
		updatePVP(world);
		updateKeepSpawnInMemory(world);
		updateDifficulty(world);
		updateAutoSave(world);
	}

	public void onWorldUnload(World world) {
		// If the actual World spawnpoint changed, be sure to update accordingly
		if (!isOtherWorldSpawn()) {
			Location spawn = world.getSpawnLocation();
			if (spawnPoint.getBlockX() != spawn.getBlockX() || spawnPoint.getBlockY() != spawn.getBlockY() 
					|| spawnPoint.getBlockZ() != spawn.getBlockZ()) {
				spawnPoint = new Position(spawn);
			}
		}

		// Disable time control
		timeControl.updateWorld(null);
	}

	public World loadWorld() {
		if (WorldManager.worldExists(this.worldname)) {
			World w = WorldManager.getOrCreateWorld(this.worldname);
			if (w == null) {
				MyWorlds.plugin.log(Level.SEVERE, "Failed to (pre)load world: " + worldname);
			} else {
				return w;
			}
		} else {
			MyWorlds.plugin.log(Level.WARNING, "World: " + worldname + " could not be loaded because it no longer exists!");
		}
		return null;
	}
	public boolean unloadWorld() {
		return WorldManager.unload(this.getWorld());
	}
	
	public static void updateReload(Player player) {
		updateReload(player.getWorld());
	}
	public static void updateReload(Location loc) {
		updateReload(loc.getWorld());
	}
	public static void updateReload(World world) {
		updateReload(world.getName());
	}
	public static void updateReload(final String worldname) {
		CommonUtil.nextTick(new Runnable() {
			public void run() {
				get(worldname).updateReload();
			}
		});
	}
	public void updateSpoutWeather(World world) {
		if (!MyWorlds.isSpoutPluginEnabled) return;
		for (Player p : world.getPlayers()) updateSpoutWeather(p);
	}
	public void updateSpoutWeather(Player player) {
		if (MyWorlds.isSpoutPluginEnabled) {
			SpoutPluginHandler.setPlayerWeather(player, showRain, showSnow);
		}
	}
	public void updateReload() {
		World world = this.getWorld();
		if (world == null) return;
		if (!this.reloadWhenEmpty) return;
		if (world.getPlayers().size() > 0) return;
		//reload world
		MyWorlds.plugin.log(Level.INFO, "Reloading world '" + worldname + "' - world became empty");
		if (!this.unloadWorld()) {
			MyWorlds.plugin.log(Level.WARNING, "Failed to unload world: " + worldname + " for reload purposes");
		} else if (this.loadWorld() == null) {
			MyWorlds.plugin.log(Level.WARNING, "Failed to load world: " + worldname + " for reload purposes");
		} else {
			MyWorlds.plugin.log(Level.INFO, "World reloaded successfully");
		}
	}
	public void updateAutoSave(World world) {
		if (world != null && world.isAutoSave() != this.autosave) {
			world.setAutoSave(this.autosave);
		}
	}
	public void updateOP(Player player) {
		if (MyWorlds.useWorldOperators) {
			boolean op = this.isOP(player);
			if (op != player.isOp()) {
				player.setOp(op);
				if (op) {
					player.sendMessage(ChatColor.YELLOW + "You are now op!");
				} else {
					player.sendMessage(ChatColor.RED + "You are no longer op!");
				}
			}
		}
	}
	public void updateOP(World world) {
		if (MyWorlds.useWorldOperators) {
			for (Player p : world.getPlayers()) updateOP(p);
		}
	}
	public void updateGamemode(Player player) {
		if (this.gameMode != null && !Permission.GENERAL_IGNOREGM.has(player)) {
			player.setGameMode(this.gameMode);
		}
	}
	public void updatePVP(World world) {
		if (world != null && this.pvp != world.getPVP()) {
			world.setPVP(this.pvp);
		}
	}
	public void updateKeepSpawnInMemory(World world) { 
		if (world != null && world.getKeepSpawnInMemory() != this.keepSpawnInMemory) {
			world.setKeepSpawnInMemory(this.keepSpawnInMemory);
		}
	}
	public void updateDifficulty(World world) {
		if (world != null && world.getDifficulty() != this.difficulty) {
			world.setDifficulty(this.difficulty);
		}
	}

	public void update(Player player) {
		updateOP(player);
		updateGamemode(player);
		updateSpoutWeather(player);
	}

	/**
	 * Gets a safe configuration name for this World Configuration<br>
	 * Unsafe characters, such as dots, are replaced
	 * 
	 * @return Safe config world name
	 */
	public String getConfigName() {
		if (this.worldname == null) {
			return "";
		}
		return this.worldname.replace('.', '_').replace(':', '_');
	}

	/**
	 * Gets the loaded World of this world configuration<br>
	 * If the world is not loaded, null is returned
	 * 
	 * @return the World
	 */
	public World getWorld() {
		return this.worldname == null ? null : WorldManager.getWorld(this.worldname);
	}

	public boolean isOP(Player player) {
		for (String playername : OPlist) {
			if (playername.equals("\\*")) return true;
			if (player.getName().equalsIgnoreCase(playername)) return true;
		}
		return false;
	}
	public void setGameMode(GameMode mode) {
		if (this.gameMode != mode) {
			this.gameMode = mode;
			if (mode != null) {
				World world = this.getWorld();
				if (world != null) {
					for (Player p : world.getPlayers()) {
						this.updateGamemode(p);
					}
				}
			}
		}
	}
}
