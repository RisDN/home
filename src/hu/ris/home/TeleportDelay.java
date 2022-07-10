package hu.ris.home;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TeleportDelay implements Listener {

	static Main plugin;
	public TeleportDelay(Main main) {
		plugin = main;
	}
	
	private ArrayList<Player> inDelayPlayers = new ArrayList<Player>();
	public void setPlayerDelay(Player p, Boolean state) {
		if(state) {
			inDelayPlayers.add(p);
			return;
		}
		
		if(inDelayPlayers.contains(p)) {
			inDelayPlayers.remove(p);
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(!inDelayPlayers.contains(p)) {
			return;
		}
		
		if(e.getFrom().getX() == e.getTo().getX() && e.getFrom().getZ() == e.getTo().getZ() && e.getFrom().getY() == e.getTo().getY()) {
			return;
		}

		Bukkit.getScheduler().cancelTask(plugin.getTeleportShed());
		setPlayerDelay(p, false);
		p.sendMessage(plugin.messageFormatter(plugin.getConfig().getString("uzenetek.megmozdult")));
		
		
	}
	
}
