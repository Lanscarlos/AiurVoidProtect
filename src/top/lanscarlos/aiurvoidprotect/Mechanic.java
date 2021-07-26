package top.lanscarlos.aiurvoidprotect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.clip.placeholderapi.PlaceholderAPI;

public class Mechanic {
	
	protected boolean cancelVoidDamage = true;
	protected boolean resetFallDistance = true;
	protected Vector vector;
	protected List<String> playerCommands = new ArrayList<>();
	protected List<String> consoleCommands = new ArrayList<>();
	protected int potionDelay = 0;
	protected List<PotionEffect> potionEffects = new ArrayList<>();
	protected Location safeLocation;
	protected String message;
	protected String actionMessage;
	protected String title;
	protected String subTitle;
	protected int fadeIn;
	protected int stay;
	protected int fadeOut;
	
	public void run(EntityDamageEvent e, Player player) {
		
		if(cancelVoidDamage) {
			e.setDamage(0);
		}
		
		if(vector != null) {
			player.setVelocity(vector);
		}
		
		if(resetFallDistance) {
			player.setFallDistance(0);
		}
		OfflinePlayer offline = Bukkit.getOfflinePlayer(player.getUniqueId());
		if(playerCommands != null && !playerCommands.isEmpty()) {
			for(String cmd : playerCommands) {
				player.chat("/"+PlaceholderAPI.setPlaceholders(offline, cmd));
			}
		}
		if(consoleCommands != null && !consoleCommands.isEmpty()) {
			for(String cmd : consoleCommands) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(offline, cmd));
			}
		}
		
		if(potionEffects != null && !potionEffects.isEmpty()) {
			if(potionDelay > 0) {
				new BukkitRunnable() {
					
					@Override
					public void run() {
						for(PotionEffect effect : potionEffects) {
							player.addPotionEffect(effect, true);
						}
					}
				}.runTaskLater(AiurVoidProtect.init(), potionDelay);
			}else {
				player.sendMessage("°¡az");
				for(PotionEffect effect : potionEffects) {
					player.addPotionEffect(effect, true);
				}
			}
		}
		
		if(message != null && !message.equals("null")) {
			player.sendMessage(PlaceholderAPI.setPlaceholders(offline, message));
		}
		if(actionMessage != null && !actionMessage.equals("null")) {
			player.sendActionBar(PlaceholderAPI.setPlaceholders(offline, actionMessage));
		}
		if(title != null) {
			player.sendTitle(PlaceholderAPI.setPlaceholders(offline, title), PlaceholderAPI.setPlaceholders(offline, subTitle), fadeIn, stay, fadeOut);
		}
		
		if(safeLocation != null) {
			player.teleport(safeLocation);
		}
	}
	
	public Mechanic(ConfigurationSection config) {
		
		cancelVoidDamage = config.isBoolean("Cancel-Void-Damaged") ? config.getBoolean("Cancel-Void-Damaged") : cancelVoidDamage;
		resetFallDistance = config.isBoolean("Reset-Fall-Distance") ? config.getBoolean("Reset-Fall-Distance") : resetFallDistance;
		
		if(config.isList("Velocity")) {
			List<Integer> list = config.getIntegerList("Velocity");
			if(list.size() == 3) {
				vector = new Vector(list.get(0), list.get(1), list.get(2));
			}
		}
		
		playerCommands = config.isList("Player-Commands") ? config.getStringList("Player-Commands") : playerCommands;
		consoleCommands = config.isList("Console-Commands") ? config.getStringList("Console-Commands") : consoleCommands;
		
		potionDelay = config.isInt("PotionEffect.Delay") ? config.getInt("PotionEffect.Delay") : potionDelay;
		if(config.isList("PotionEffect.Effects")) {
			PotionEffectType type;
			int duration;
			int amplifier;
			String[] s;
			for(String str : config.getStringList("PotionEffect.Effects")) {
				s = str.split("\\-");
				type = PotionEffectType.getByName(s[0].toUpperCase());
				duration = Integer.parseInt(s[1]);
				amplifier = Integer.parseInt(s[2]);
				potionEffects.add(new PotionEffect(type, duration, amplifier));
			}
		}
		
		message = config.isString("Message.Common-Message") ? config.getString("Message.Common-Message") : message;
		actionMessage = config.isString("Message.Action-Message") ? config.getString("Message.Action-Message") : actionMessage;
		if(config.isBoolean("Message.Title-Message.Enable") ? config.getBoolean("Message.Title-Message.Enable") : false) {
			title = config.isString("Message.Title-Message.Title") ? config.getString("Message.Title-Message.Title") : title;
			subTitle = config.isString("Message.Title-Message.SubTitle") ? config.getString("Message.Title-Message.SubTitle") : subTitle;
			fadeIn = config.isInt("Message.Title-Message.FadeIn") ? config.getInt("Message.Title-Message.FadeIn") : fadeIn;
			stay = config.isInt("Message.Title-Message.Stay") ? config.getInt("Message.Title-Message.Stay") : stay;
			fadeOut = config.isInt("Message.Title-Message.FadeOut") ? config.getInt("Message.Title-Message.FadeOut") : fadeOut;
		}
		
		if(config.isBoolean("Safe-Teleport.Enable") ? config.getBoolean("Safe-Teleport.Enable") : false) {
			World world = Bukkit.getWorld(config.isString("Safe-Teleport.Location.World") ? config.getString("Safe-Teleport.Location.World") : "world");
			double x = config.isDouble("Safe-Teleport.Location.X") ? config.getDouble("Safe-Teleport.Location.X") : 0;
			double y = config.isDouble("Safe-Teleport.Location.Y") ? config.getDouble("Safe-Teleport.Location.Y") : 0;
			double z = config.isDouble("Safe-Teleport.Location.Z") ? config.getDouble("Safe-Teleport.Location.Z") : 0;
			double yaw = config.isDouble("Safe-Teleport.Location.Yaw") ? config.getDouble("Safe-Teleport.Location.Yaw") : 0;
			double pitch = config.isDouble("Safe-Teleport.Location.Pitch") ? config.getDouble("Safe-Teleport.Location.Pitch") : 0;
			safeLocation = new Location(world, x, y, z, (float) yaw, (float) pitch);
		}
	}
}
