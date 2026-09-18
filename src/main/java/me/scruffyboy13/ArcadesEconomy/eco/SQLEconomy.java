package me.scruffyboy13.ArcadesEconomy.eco;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.scruffyboy13.ArcadesEconomy.ArcadesEconomyMain;
import me.scruffyboy13.ArcadesEconomy.data.ConfigHandler;
import me.scruffyboy13.ArcadesEconomy.data.MySQL;

public class SQLEconomy implements Economy {
	
	private MySQL sql;
	
	public SQLEconomy() {
		
		this.sql = new MySQL(
				ConfigHandler.getHost(), 
				ConfigHandler.getPort(), 
				ConfigHandler.getDatabase(), 
				ConfigHandler.getUsername(), 
				ConfigHandler.getPassword());
		
		connectToSQL();
		
		if (sql.isConnected()) {
			
			try {
				Statement statement = sql.getConnection().createStatement();
				DatabaseMetaData md = sql.getConnection().getMetaData();
				statement.execute("CREATE TABLE IF NOT EXISTS ArcadesEconomy (UUID VARCHAR(36) NOT NULL);");
				for (Map.Entry<String, String> column : ArcadesEconomyMain.getSQLColumns().entrySet()) {
					if (!md.getColumns(null, null, "ArcadesEconomy", column.getKey()).next()) {
						statement.execute("ALTER TABLE ArcadesEconomy ADD " + column.getKey() + " " + column.getValue() + ";");
					}
				}
				statement.close();
			} catch (SQLException e) {
				ArcadesEconomyMain.disable("There was an error with creating the database table.");
				return;
			}
			
			try {
				PreparedStatement statement = sql.getConnection().prepareStatement("ALTER TABLE ArcadesEconomy "
						+ "MODIFY COLUMN Balance " + ArcadesEconomyMain.getSQLColumns().get("Balance"));
				statement.executeUpdate();
				statement.close();
			} catch (SQLException e) {
				ArcadesEconomyMain.disable("There was an error updating the sql balance from 1dp to 2dp.");
				return;
			}
		
		}
	
	}
	
	private void connectToSQL() {
		try {
			sql.connect();
            ArcadesEconomyMain.warn("Successfully connected to mysql database.");
        } 
        catch (SQLException e) {
        	ArcadesEconomyMain.warn("There was an error connecting to the database. " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(ArcadesEconomyMain.getInstance());
            return;
        }
        catch (ClassNotFoundException e) {
        	ArcadesEconomyMain.getInstance().getLogger().warning("The MySQL driver class could not be found.");
        	Bukkit.getPluginManager().disablePlugin(ArcadesEconomyMain.getInstance());
        	return;
        }
	}

	@Override
	public boolean createAccount(UUID uuid) {
		PlayerBalance playerBalance = new PlayerBalance(uuid, ConfigHandler.getStartingBalance());
		try {
			PreparedStatement statement = sql.getConnection().prepareStatement("INSERT INTO ArcadesEconomy "
					+ "(UUID, Balance) VALUES (?, ?);");
			statement.setString(1, playerBalance.getUUID().toString());
			statement.setDouble(2, playerBalance.getBalance());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			ArcadesEconomyMain.warn(e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public boolean hasAccount(UUID uuid) {
		for (PlayerBalance pb : getPlayers()) {
			if (pb.getUUID().equals(uuid)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean delete(UUID uuid) {
		try {
			PreparedStatement statement = sql.getConnection().prepareStatement("DELETE FROM ArcadesEconomy "
					+ "WHERE UUID=?");
			statement.setString(1, uuid.toString());
			statement.executeUpdate();
			statement.close();
		} catch(SQLException e) {
			ArcadesEconomyMain.warn(e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public boolean withdraw(UUID uuid, double amount) {
		return set(uuid, getBalance(uuid).getBalance() - amount);
	}

	@Override
	public boolean deposit(UUID uuid, double amount) {
		return set(uuid, getBalance(uuid).getBalance() + amount);
	}

	@Override
	public boolean set(UUID uuid, double amount) {
		if (amount < 0)
			return false;
		try {
			PreparedStatement statement = sql.getConnection().prepareStatement("UPDATE ArcadesEconomy SET "
					+ "UUID=?, Balance=? WHERE UUID=?");
			statement.setString(1, uuid.toString());
			statement.setDouble(2, amount);
			statement.setString(3, uuid.toString());
			statement.executeUpdate();
			statement.close();
		} catch(SQLException e) {
			ArcadesEconomyMain.warn(e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public boolean has(UUID uuid, double amount) {
		return getBalance(uuid).getBalance() >= amount;
	}

	@Override
	public PlayerBalance getBalance(UUID uuid) {
		try {
			PreparedStatement statement = sql.getConnection().prepareStatement("SELECT * FROM ArcadesEconomy "
					+ "WHERE UUID=?");
		statement.setString(1, uuid.toString());
		ResultSet result = statement.executeQuery();
		result.next();
		double balance = result.getDouble("Balance");
		return new PlayerBalance(uuid, balance);
		} catch (SQLException e) {
			return new PlayerBalance(uuid, 0);
		}
	}
	
	@Override
	public List<PlayerBalance> getPlayers() {
		try {
			List<PlayerBalance> playerData = new ArrayList<PlayerBalance>();
			Statement statement = sql.getConnection().createStatement();
			ResultSet result = statement.executeQuery("SELECT * FROM ArcadesEconomy;");
			while (result.next()) {
				UUID uuid = UUID.fromString(result.getString("UUID"));
				double balance = result.getDouble("Balance");
				playerData.add(new PlayerBalance(uuid, balance));
			}
			return playerData;
		} catch (SQLException e) {
			ArcadesEconomyMain.warn(e.getMessage());
			return null;
		}
	}

}
