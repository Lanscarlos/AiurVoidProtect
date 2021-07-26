package top.lanscarlos.aiurvoidprotect;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

public class AiurVoidProtect extends JavaPlugin implements Listener {
	
	protected List<String> disableWorlds = new ArrayList<>();
	protected int globalCooldown = 600;
	protected int functionCooldown = 20;
	protected List<Mechanic> mechanics = new ArrayList<>();
	
	protected HashMap<String, Long> globalData = new HashMap<>();
	protected HashMap<String, Long> functionData = new HashMap<>();
	protected HashMap<String, Integer> countData = new HashMap<>();
	
	private static AiurVoidProtect plugin;
	
	public void onLoad() {}
	
	public void onEnable() {
		plugin = this;
		this.loadConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable() {}
	
	public void loadConfig() {
		if (!new File(this.getDataFolder(), "config.yml").exists()) {
			this.saveDefaultConfig();
		}
		this.reloadConfig();
		FileConfiguration config = this.getConfig();
		mechanics.clear();
		
		disableWorlds = config.isList("Disable-Worlds") ? config.getStringList("Disable-Worlds") : disableWorlds;
		globalCooldown = config.isInt("Cooldown.Global") ? config.getInt("Cooldown.Global") : globalCooldown;
		functionCooldown = config.isInt("Cooldown.Function") ? config.getInt("Cooldown.Function") : globalCooldown;
		int maxCount = config.isInt("MaxCount") ? config.getInt("MaxCount") : 1;
		for(int i=0;i<maxCount;i++) {
			mechanics.add(new Mechanic(config.getConfigurationSection("Functions."+i)));
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onDamaged(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player) || !e.getCause().equals(DamageCause.VOID) || disableWorlds.contains(e.getEntity().getWorld().getName())) {
			return;
		}
		
		Player player = (Player) e.getEntity();
		
		if(!player.hasPermission("aiurvoidprotect.protect")) {
			return;
		}
		
		String key = player.getUniqueId().toString();
		
		if(globalCooldown > 0 && globalData.containsKey(key) && System.currentTimeMillis() < globalData.get(player.getUniqueId().toString()) + globalCooldown * 50 ) {
			
			if(functionCooldown > 0 && functionData.containsKey(key) && System.currentTimeMillis() < functionData.get(player.getUniqueId().toString()) + functionCooldown * 50 ) {
				return;
			}
			
			countData.put(key, countData.get(key)+1);
			
			if(countData.get(key) >= mechanics.size()) {
				return;
			}
			
		}else {
			globalData.put(key, System.currentTimeMillis());
			countData.put(key, 0);
		}
		
		mechanics.get(countData.get(key)).run(e, player);
		
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onDeath(EntityDeathEvent e) {
		if(!(e.getEntity() instanceof Player) || disableWorlds.contains(e.getEntity().getWorld().getName())) {
			return;
		}
		String key = ((Player) e.getEntity()).getUniqueId().toString();
		globalData.remove(key);
		functionData.remove(key);
		countData.remove(key);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("aiurvoidprotect")) {
			
			if(!sender.hasPermission("aiurvoidprotect.admin")) {
				sender.sendMessage("§7[§bAiurVoidSpawn§7] §c你没有权限使用该命令！");
				return true;
			}
			
			if(args.length != 1){
				sender.sendMessage("§7[§bAiurVoidSpawn§7] §a/avp reload  §b- 重载配置文件");
				return true;
			}
			
			if(args[0].equalsIgnoreCase("reload")) {
				this.loadConfig();
				sender.sendMessage("§7[§bAiurVoidSpawn§7] 配置文件重载完毕~");
			}
			
			return true;
		}
		return false;
	}
	
	public static AiurVoidProtect init() {
		return plugin;
	}
}
