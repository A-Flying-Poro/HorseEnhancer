package com.nevakanezah.horseenhancer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import com.nevakanezah.horseenhancer.util.StorableHashMap;

/**
 * HorseEnhancer:
 * All-natural enhancements to equine husbandry in Minecraft.
 * 
 * @author Nevakanezah
 *
 */

public class HorseEnhancerPlugin extends JavaPlugin {
	
	// Global list of custom horse data
	private StorableHashMap<UUID, HorseData> horses = null;
	
	private Logger logger = this.getLogger();
	
    public HorseEnhancerPlugin()
    {
        super();
    }

    protected HorseEnhancerPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file)
    {
        super(loader, description, dataFolder, file);
    }

	@Override
	public void onDisable() {
		purgeInvalidHorses(true);
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		loadConfig();
		
		try { horses = new StorableHashMap<>(this.getDataFolder(), "Horses"); }
		catch (IOException e)
		{
			getLogger().log(Level.WARNING, "Error: Failed to create horse save file: ", e);
		}
		
	    try{ horses.loadFromFile(); } 
	    catch (ClassNotFoundException | IOException e)
	    {
	        getLogger().log(Level.WARNING, "Error: Failed to load horse data: ", e);
	    }
	    
	    String msg = "Successfully loaded [" + horses.size() +"] horses.";
	    getLogger().log(Level.INFO, msg);
	    
		getServer().getPluginManager().registerEvents(new PlayerAttackHorseEventHandler(this), this);
		getServer().getPluginManager().registerEvents(new HorseTameEventHandler(this), this);
		getServer().getPluginManager().registerEvents(new HorseDeathEventHandler(this), this);
		getServer().getPluginManager().registerEvents(new HorseSpawnEventHandler(this), this);
		
		this.getCommand("he").setExecutor(new CommandHandler(this));
		this.getCommand("horseenhancer").setExecutor(new CommandHandler(this));
		this.getCommand("he").setTabCompleter(new TabComplete());
		this.getCommand("horseenhancer").setTabCompleter(new TabComplete());
	}

	public List<String> loadConfig() {
		
		ArrayList<String> msg = new ArrayList<>();
		
		this.reloadConfig();
		
		// Populate any options missing from config
		ArrayList<String> configNames = new ArrayList<>();
		configNames.add("gelding-tool");
		configNames.add("inspection-tool");
		configNames.add("childskew-upper");
		configNames.add("childskew-lower");
		configNames.add("gender-ratio");
		configNames.add("enable-inspector");
		configNames.add("enable-inspector-attributes");
		configNames.add("enable-equicide-protection");
		
		for(String item : configNames) {
			if(!this.getConfig().isSet(item)) {
				logger.log(Level.INFO, "Setting config: [" + item + "]");
				this.getConfig().set(
						item, this.getConfig().getDefaults().getString(item));
				this.saveConfig();
			}
		}
		
		// Clamp the skew values between -1 and 1, and ensure that upper is >= lower.
		double upper = this.getConfig().getDouble("childskew-upper");
		double lower = this.getConfig().getDouble("childskew-lower");
		
		if(upper > 1.0)
			this.getConfig().set("childskew-upper", Math.min( 1.0, upper));
		
		if(lower < -1.0)
			this.getConfig().set("childskew-lower", Math.max( -1.0, lower));
		
		if(lower > upper) {
			if(lower <= 0)
			   upper = lower;
			else
			  lower = upper;
		}
		
		logger.log(Level.INFO, "Loaded stat skew: [" + lower + " - " + upper + "]");
		
		double genderRatio = Math.max( 0.0, Math.min( 1.0, this.getConfig().getDouble("gender-ratio")));
		this.getConfig().set("gender-ratio", genderRatio);
		
		logger.log(Level.INFO, "Loaded gender ratio with value of [" + genderRatio + "]");
		logger.log(Level.INFO, "Gelding tool is [" + this.getConfig().getString("gelding-tool") + "]");
		logger.log(Level.INFO, "Inspection tool is [" + this.getConfig().getString("inspection-tool") + "]");
		
		msg.add("[" + this.getDescription().getName() + "] configuration loaded.");
		
		return msg;
	}
	
	
	public StorableHashMap<UUID, HorseData> getHorses(){
		return horses;
	}
	
	public void purgeInvalidHorses() {
		purgeInvalidHorses(false);
	}
	
	public void purgeInvalidHorses(Boolean doLogOutput)
	{
		int invalidHorses = 0;
		Iterator<UUID> horseKeys = horses.keySet().iterator();
		while(horseKeys.hasNext()) {
			UUID horseId = horseKeys.next();
			if(this.getServer().getEntity(horseId) == null || this.getServer().getEntity(horseId).isDead()) {
				horseKeys.remove();
				invalidHorses++;
			}
		}
		
		if(invalidHorses > 0 && Boolean.TRUE.equals(doLogOutput)) {
			String msg = "Unloading [" + invalidHorses +"] invalid horses.";
			getLogger().log(Level.INFO, msg);
		}
		
	    try { horses.saveToFile(); } 
	    catch (IOException e)
	    {
	    	getLogger().log(Level.WARNING, "Error: Failed to save horse data!", e);
	    }
	}
}
