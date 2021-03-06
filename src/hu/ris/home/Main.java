package hu.ris.home;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Main extends JavaPlugin {
	
	
	FileManager fs;
	TeleportDelay td;
	public void onEnable() {
		saveDefaultConfig();
		fs = new FileManager(this, "userdata.yml");
		td = new TeleportDelay(this);
;		getLogger().info("Home indul v" + getDescription().getVersion());
		getServer().getPluginManager().registerEvents(td, this);
	}
	
	private int teleportSched;
	private int teleportTimer;
	private Set<String> savedHomes = new HashSet<String>();
	
	public boolean hasHomes(Player p) {
		if(fs.getConfig("usetdata.yml").getString(String.valueOf(p.getUniqueId())) == null || fs.getConfig("userdata.yml").getConfigurationSection(String.valueOf(p.getUniqueId())).getKeys(false).size() == 0) {
			return false;
		}
		return true;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {		
			return true;
		}
		Player player = (Player) sender;

		
		if(!player.hasPermission("home.use")) {
			player.sendMessage(messageFormatter(getConfig().getString("uzenetek.nincsjogod")));
			return true;
		}	
		
		
		
		if(cmd.getLabel().equalsIgnoreCase("listhome")) {
			if(!hasHomes(player)) {
				player.sendMessage(messageFormatter(getConfig().getString("uzenetek.nincsegyse")));
				return true;
			}
			savedHomes = fs.getConfig("userdata.yml").getConfigurationSection(String.valueOf(player.getUniqueId())).getKeys(false);
			player.sendMessage(messageFormatter(getConfig().getString("uzenetek.mentettek")));
			for(String cica : savedHomes) {
		        TextComponent component = new TextComponent(TextComponent.fromLegacyText(messageFormatter("&f - &e" + cica)));
		        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + cica));
		        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(messageFormatter("&dKattints a teleport??ci??hoz."))));
		        player.spigot().sendMessage(component);
			}
			
			return true;
		}
		
		if(args.length == 0) {
			player.sendMessage(messageFormatter("&5------------- &dHome &5-------------"));
			player.sendMessage(messageFormatter("&e Parancsok: "));
			player.sendMessage(messageFormatter("&7  - &f/home <n??v> &7??? &eOtthonhoz teleport??l??s"));
			player.sendMessage(messageFormatter("&7  - &f/sethome <n??v> &7??? &eOtthon lerak??sa"));
			player.sendMessage(messageFormatter("&7  - &f/delhome <n??v> &7??? &eOtthon t??rl??se"));
			player.sendMessage(messageFormatter("&7  - &f/listhome &7??? &eMentett otthonok list??ja"));
			player.sendMessage(messageFormatter("&5-------------------------------"));
			return true;
		}

		if(cmd.getLabel().equalsIgnoreCase("sethome")) {
			if(hasHomes(player)) {
				savedHomes = fs.getConfig("userdata.yml").getConfigurationSection(String.valueOf(player.getUniqueId())).getKeys(false);
				if(savedHomes.contains(args[0])) {
					player.sendMessage(messageFormatter(getConfig().getString("uzenetek.letezik")
					.replace("%nev%", args[0])));
					return true;
				}
			}

			player.sendMessage(messageFormatter(getConfig().getString("uzenetek.sikereslerakas")
			.replace("%nev%", args[0])));
			fs.getConfig("userdata.yml").set(player.getUniqueId() + "." + args[0], player.getLocation());
			fs.saveConfig("userdata.yml");
			fs.reloadConfig("userdata.yml");
			return true;
		}
		
		
		if(cmd.getLabel().equalsIgnoreCase("delhome")) {
			if(hasHomes(player)) {
				savedHomes = fs.getConfig("userdata.yml").getConfigurationSection(String.valueOf(player.getUniqueId())).getKeys(false);
				if(!savedHomes.contains(args[0])) {
					player.sendMessage(messageFormatter(getConfig().getString("uzenetek.nincsilyen")
					.replace("%nev%", args[0])));
					return true;
				}
			}

			
			player.sendMessage(messageFormatter(getConfig().getString("uzenetek.sikerestorles")
			.replace("%nev%", args[0])));
			fs.getConfig("userdata.yml").set(player.getUniqueId() + "." + args[0], null);
			fs.saveConfig("userdata.yml");
			fs.reloadConfig("userdata.yml");
			return true;
		}
		
		if(cmd.getLabel().equalsIgnoreCase("home")) {
			if(!hasHomes(player)) {
				savedHomes = fs.getConfig("userdata.yml").getConfigurationSection(String.valueOf(player.getUniqueId())).getKeys(false);
				if(!savedHomes.contains(args[0])) {
					player.sendMessage(messageFormatter(getConfig().getString("uzenetek.nincsilyen")
					.replace("%nev%", args[0])));
					return true;
				}
			}
			
			teleportTimer = getConfig().getInt("varakozasiido");
			teleportSched = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				@Override
				public void run() {
					player.sendMessage(messageFormatter(getConfig().getString("uzenetek.nemozdulj")
					.replace("%mp%", String.valueOf(teleportTimer))));
					td.setPlayerDelay(player, true);
					if(teleportTimer == 0) {
						player.teleport(fs.getConfig("userdata.yml").getLocation(String.valueOf(player.getUniqueId() + "." + args[0])));
						player.sendMessage(messageFormatter(getConfig().getString("uzenetek.sikeresteleport")
						.replace("%nev%", args[0])));
						td.setPlayerDelay(player, false);
						Bukkit.getScheduler().cancelTask(teleportSched);
					}
					teleportTimer--;
				}
				
			}, 0, 20);

			return true;
		}	
		
		
		return true;
		
	}
	
	public int getTeleportShed() {
		return teleportSched;
	}
	
	public String messageFormatter(String message) {
		return ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", getConfig().getString("prefix")));
	}
}
