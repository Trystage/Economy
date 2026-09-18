package me.scruffyboy13.ArcadesEconomy.commands.money;

import java.util.Arrays;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import me.scruffyboy13.ArcadesEconomy.ArcadesEconomyMain;
import me.scruffyboy13.ArcadesEconomy.commands.CommandExecutor;
import me.scruffyboy13.ArcadesEconomy.data.ConfigHandler;
import me.scruffyboy13.ArcadesEconomy.utils.StringUtils;

public class MoneyTakeCommand extends CommandExecutor {

	public MoneyTakeCommand() {
		this.setName("take");
		this.setPermission("arcadeseconomy.command.take");
		this.setUsage(ConfigHandler.getMessage("money.take.usage"));
		this.setBoth(true);
		this.setLengths(Arrays.asList(3));
		this.setAliases(Arrays.asList("remove"));
	}

	@Override
	public void execute(CommandSender sender, String[] args) {

		List<OfflinePlayer> others = ArcadesEconomyMain.getPlayersFromString(sender, args[1]);
		
		if (others.isEmpty() && !args[1].equals("@a")) {
			StringUtils.sendConfigMessage(sender, "messages.money.take.otherDoesntExist", ImmutableMap.of(
					"%player%", args[1]));
			return;
		}
		
		double amount = 0;
		try {
			amount = ArcadesEconomyMain.getAmountFromString(args[2]);
		}
		catch (NumberFormatException e){
			StringUtils.sendConfigMessage(sender, "messages.money.take.invalidAmount", ImmutableMap.of(
					"%amount%", args[2]));
			return;
		}
		if (amount < 0) {
			StringUtils.sendConfigMessage(sender, "messages.money.take.invalidAmount", ImmutableMap.of(
					"%amount%", args[2]));
			return;
		}
		
		int total = 0;
		boolean failed = false;
		
		for (OfflinePlayer other : others) {
		
			if (!ArcadesEconomyMain.getEco().hasAccount(other.getUniqueId())) {
				StringUtils.sendConfigMessage(sender, "messages.money.take.otherNoAccount", ImmutableMap.of(
						"%player%", other.getName()));
				failed = true;
				continue;
			}
			
			if (!ArcadesEconomyMain.getEco().has(other.getUniqueId(), amount)) {
				StringUtils.sendConfigMessage(sender, "messages.money.take.insufficientFunds", ImmutableMap.of(
						"%player%", other.getName()));
				failed = true;
				continue;
			}
			
			ArcadesEconomyMain.getEco().withdraw(other.getUniqueId(), amount);
			
			if (other instanceof Player) {
				if (!(sender instanceof Player && ((Player) sender).equals((Player) other))) {
					StringUtils.sendConfigMessage((Player) other, "messages.money.take.taken", ImmutableMap.of(
							"%amount%", ArcadesEconomyMain.format(amount)));
				}
			}
			
			total += 1;
		
		}
		
		if (others.size() == 1) {
			
			if (!failed) {
			
				StringUtils.sendConfigMessage(sender, "messages.money.take.take", ImmutableMap.of(
						"%amount%", ArcadesEconomyMain.format(amount),
						"%player%", others.get(0).getName()));
				
			}
			
		}
		
		else {
			
			StringUtils.sendConfigMessage(sender, "messages.money.take.takeMultiple", ImmutableMap.of(
					"%total%", total + "",
					"%amount%", ArcadesEconomyMain.format(amount)));
			
		}
		
		return;
		
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
