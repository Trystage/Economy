package me.scruffyboy13.ArcadesEconomy.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.scruffyboy13.ArcadesEconomy.ArcadesEconomyMain;

public class PlayerJoinListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		Player player = event.getPlayer();
		
		if (!ArcadesEconomyMain.getEco().hasAccount(player.getUniqueId())) {
			new BukkitRunnable() {

				@Override
				public void run() {
					ArcadesEconomyMain.getEco().createAccount(player.getUniqueId());
				}
			}.runTaskAsynchronously(ArcadesEconomyMain.getInstance());
		}
		
	}
	
}
