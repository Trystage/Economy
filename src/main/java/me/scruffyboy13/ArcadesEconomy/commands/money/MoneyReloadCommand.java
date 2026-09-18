package me.scruffyboy13.ArcadesEconomy.commands.money;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.scruffyboy13.ArcadesEconomy.ArcadesEconomyMain;
import me.scruffyboy13.ArcadesEconomy.commands.CommandExecutor;
import me.scruffyboy13.ArcadesEconomy.data.ConfigHandler;
import me.scruffyboy13.ArcadesEconomy.runnables.BalanceTopRunnable;
import me.scruffyboy13.ArcadesEconomy.utils.StringUtils;

public class MoneyReloadCommand extends CommandExecutor {

	public MoneyReloadCommand() {
		this.setName("reload");
		this.setPermission("arcadeseconomy.command.reload");
		this.setUsage(ConfigHandler.getMessage("money.reload.usage"));
		this.setBoth(true);
		this.setLengths(Arrays.asList(1));
		this.setAliases(Arrays.asList("rl"));
	}

	@Override
	public void execute(CommandSender sender, String[] args) {

		ArcadesEconomyMain.getInstance().reloadConfig();
		ArcadesEconomyMain.setSuffixes(ConfigHandler.getSuffixes());
		
		ArcadesEconomyMain.getBalanceTopRunnable().cancel();
		ArcadesEconomyMain.setBalanceTopRunnable(new BalanceTopRunnable());
		ArcadesEconomyMain.getBalanceTopRunnable().start(ConfigHandler.getBalanceTopInterval());
		
		StringUtils.sendConfigMessage(sender, "messages.money.reload.reloaded");
		
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
