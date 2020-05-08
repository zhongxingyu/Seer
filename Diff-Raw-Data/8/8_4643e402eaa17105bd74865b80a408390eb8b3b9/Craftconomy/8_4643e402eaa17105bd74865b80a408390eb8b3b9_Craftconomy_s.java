 package me.greatman.Craftconomy;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Timer;
 
 import me.greatman.Craftconomy.commands.*;
 import me.greatman.Craftconomy.commands.bank.*;
 import me.greatman.Craftconomy.commands.config.*;
 import me.greatman.Craftconomy.commands.money.*;
 import me.greatman.Craftconomy.listeners.CCPlayerListener;
 import me.greatman.Craftconomy.utils.Config;
 import me.greatman.Craftconomy.utils.DatabaseHandler;
 import me.greatman.Craftconomy.utils.DatabaseType;
 import me.greatman.Craftconomy.utils.Metrics;
 import me.greatman.Craftconomy.utils.PayDayConfig;
 import me.greatman.Craftconomy.utils.SQLLibrary;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Craftconomy extends JavaPlugin
 {
 
 	public static String name, version;
 	public List<BaseCommand> commands = new ArrayList<BaseCommand>();
 	public List<BaseCommand> bankCommands = new ArrayList<BaseCommand>();
 	public List<BaseCommand> configCommands = new ArrayList<BaseCommand>();
 	public CCPlayerListener playerListener = new CCPlayerListener();
 	public Timer payDay;
 	public static Craftconomy plugin;
 	public List<Timer> timerMap = new ArrayList<Timer>();
 
 	public void onEnable()
 	{
 		name = this.getDescription().getName();
 		version = this.getDescription().getVersion();
 		ILogger.info("Starting");
 		Config.load(this);
 		plugin = this;
 		if (!DatabaseHandler.load(this))
 		{
 			ILogger.error("A error occured while trying to open the database. Please check your configuration.");
 			onDisable();
 			return;
 		}
 		convert();
 		new AccountHandler();
 
 		// Enable the plugin stats Metrics
 		try
 		{
 			// create a new metrics object
 			Metrics metrics = new Metrics();
 
 			// 'this' in this context is the Plugin object
 			metrics.beginMeasuringPlugin(this);
 		} catch (IOException e)
 		{
 			ILogger.error("A error occured while starting the plugin stats");
 		}
 
 		// Insert all /money commands
 		commands.add(new PayCommand());
 		commands.add(new CreateCommand());
 		commands.add(new RemoveCommand());
 		commands.add(new GiveCommand());
 		commands.add(new TakeCommand());
 		commands.add(new SetCommand());
 		// TODO: Find a safer way for this
 		// commands.add(new PurgeCommand());
 		commands.add(new EmptyCommand());
 		commands.add(new ExchangeCommand());
 		commands.add(new ExchangeCalcCommand());
 		commands.add(new MoneyHelpCommand());
 
 		for (BaseCommand CraftconomyCommand : this.commands)
 		{
 			CraftconomyCommand.setBaseCommand("/money");
 		}
 
 		// Insert all config commands
 		configCommands.add(new ConfigCurrencyAddCommand());
 		configCommands.add(new ConfigCurrencyModifyCommand());
 		configCommands.add(new ConfigCurrencyRemoveCommand());
 		configCommands.add(new ConfigCurrencyExchangeCommand());
 		configCommands.add(new ConfigHelpCommand());
 
 		for (BaseCommand CraftconomyCommand : this.configCommands)
 		{
 			CraftconomyCommand.setBaseCommand("/craftconomy");
 		}
 		// Insert all /bank commands
 		// TODO: Create/Delete/Rename bank account commands
 		bankCommands.add(new BankOtherBalanceCommand());
 		bankCommands.add(new BankDepositCommand());
 		bankCommands.add(new BankWithdrawCommand());
 		bankCommands.add(new BankGiveCommand());
 		bankCommands.add(new BankTakeCommand());
 		bankCommands.add(new BankSetCommand());
 		bankCommands.add(new BankHelpCommand());
 		bankCommands.add(new BankCreateCommand());
 		bankCommands.add(new BankDeleteCommand());
 		bankCommands.add(new BankAddCommand());
 		bankCommands.add(new BankRemoveCommand());
 
 		for (BaseCommand CraftconomyCommand : this.bankCommands)
 		{
 
 			CraftconomyCommand.setBaseCommand("/bank");
 		}
 
 		// Payday System
 		//TODO: Use a better system (Aka use player online timer. Not only server uptime)
 		new PayDayConfig();
 		if (!Config.payDayList.isEmpty())
 		{
 			Permission perm;
 			String groupName;
 			Iterator<String> iterator = Config.payDayList.iterator();
 			while (iterator.hasNext())
 			{
 				groupName = iterator.next();
 				if (PayDayConfig.exists(groupName))
 				{
 					perm = new Permission("Craftconomy.payday." + groupName);
 					perm.setDefault(PermissionDefault.FALSE);
 					getServer().getPluginManager().addPermission(perm);
 					payDay = new Timer();
 					long time = (PayDayConfig.getInterval(groupName) * 60) * 1000L;
 					payDay.schedule(new PayDay(groupName), time, time);
 					timerMap.add(payDay);
 					ILogger.info("Payday for " + groupName + " loaded!");
 				}
 			}
 			ILogger.info("PayDay system loaded.");
 		}
 
 		// Loads the players after a /reload;
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvents(playerListener, this);
 		Player[] playerList = Craftconomy.plugin.getServer().getOnlinePlayers();
 		if (playerList.length != 0)
 		{
 			for (int i = 0; i < playerList.length; i++)
 				AccountHandler.getAccount(playerList[i]);
 		}
 		ILogger.info("Started!");
 
 	}
 
 	@Override
 	public void onDisable()
 	{
 
 		if (name != null)
 			name = null;
 		if (version != null)
 			version = null;
 		if (AccountHandler.thread != null)
 			AccountHandler.thread.cancel();
 		commands.clear();
 		if (!timerMap.isEmpty())
 		{
 			Iterator<Timer> iterator = timerMap.iterator();
 			while (iterator.hasNext())
 			{
 				iterator.next().cancel();
 			}
 		}
 		ILogger.info("Craftconomy unloaded!");
 		getServer().getPluginManager().disablePlugin(this);
 
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
 	{
 		List<String> parameters = new ArrayList<String>(Arrays.asList(args));
 		if (cmd.getLabel().equals("money"))
 			this.handleMoneyCommand(cmd, sender, parameters);
 		if (cmd.getLabel().equals("bank"))
 			this.handleBankCommand(cmd, sender, parameters);
 		if (cmd.getLabel().equals("craftconomy"))
 			this.handleConfigCommand(cmd, sender, parameters);
 
 		return true;
 
 	}
 
 	// Put that in a single command
 	public void handleConfigCommand(Command cmd, CommandSender sender, List<String> parameters)
 	{
 		if (parameters.size() == 0)
 		{
 			ConfigHelpCommand command = new ConfigHelpCommand();
 			command.execute(sender, parameters);
 			return;
 		}
 		String commandName = parameters.get(0);
 		for (BaseCommand CraftconomyCommand : this.configCommands)
 		{
 			if (CraftconomyCommand.getCommands().contains(commandName))
 			{
 				CraftconomyCommand.execute(sender, parameters);
 				return;
 			}
 		}
 	}
 
 	public void handleMoneyCommand(Command cmd, CommandSender sender, List<String> parameters)
 	{
 		if (parameters.size() == 0)
 		{
 			OwnMoneyCommand command = new OwnMoneyCommand();
 			command.execute(sender, parameters);
 			return;
 		}
 		String commandName = parameters.get(0);
 		for (BaseCommand CraftconomyCommand : this.commands)
 		{
 			if (CraftconomyCommand.getCommands().contains(commandName))
 			{
 				CraftconomyCommand.execute(sender, parameters);
 				return;
 			}
 		}
 		OtherMoneyCommand command = new OtherMoneyCommand();
 		command.execute(sender, parameters);
 	}
 
 	public void handleBankCommand(Command cmd, CommandSender sender, List<String> parameters)
 	{
 		if (parameters.size() == 0)
 		{
 			BankHelpCommand command = new BankHelpCommand();
 			command.execute(sender, parameters);
 			return;
 		}
 		String commandName = parameters.get(0);
 		for (BaseCommand CraftconomyCommand : this.bankCommands)
 		{
 			if (CraftconomyCommand.getCommands().contains(commandName))
 			{
 				CraftconomyCommand.execute(sender, parameters);
 				return;
 			}
 		}
 		BankOtherBalanceCommand command = new BankOtherBalanceCommand();
 		command.execute(sender, parameters);
 	}
 
 	public static List<String> format(List<BalanceCollection> list)
 	{
 		DecimalFormat decimalFormat = new DecimalFormat("#0.00");
 		BalanceCollection balance;
 		List<String> result = new ArrayList<String>();
 		if (list == null)
 			return result;
 		Iterator<BalanceCollection> iterator = list.iterator();
 
 		while (iterator.hasNext())
 		{
 			balance = iterator.next();
 			result.add(balance.getWorldName() + ": " + decimalFormat.format(balance.getBalance()) + " "
 					+ balance.getCurrencyName());
 		}
 
 		return result;
 	}
 
 	public static String format(double amount, Currency currency)
 	{
 		// TODO: Add plural format
 		DecimalFormat decimalFormat = new DecimalFormat("#0.00");
 		return decimalFormat.format(amount) + " " + currency.getName();
 	}
 
 	public static boolean isValidAmount(String amount)
 	{
 
 		try
 		{
 			double amountParsed = Double.parseDouble(amount);
 			if (amountParsed > 0.00)
 				return true;
 			else return false;
 		} catch (NumberFormatException e)
 		{
 			return false;
 		}
 	}
 	
 	public static void convert()
 	{
 		Account account = null;
 		SQLLibrary database = null;
 		ResultSet result = null;
 		File dbFile = null;
 		String[] info, balance;
 		if (Config.convertEnabled)
 		{
 			//Iconomy support
 			//TODO: Flatfile (minidb) support
 			if (Config.convertType.equalsIgnoreCase("iconomy"))
 			{
 				if (Config.convertDatabaseType.equalsIgnoreCase("mysql"))
 				{
 					database = new SQLLibrary("jdbc:mysql://" + Config.convertDatabaseAddress + ":" + Config.convertDatabasePort + "/"
 							+ Config.convertDatabaseDb, Config.convertDatabaseUsername, Config.convertDatabasePassword, DatabaseType.MYSQL);
 				}
 				else if (Config.convertDatabaseType.equalsIgnoreCase("sqlite"))
 				{
 					database = new SQLLibrary("jdbc:sqlite:" + Config.convertDatabaseAddress, "", "", DatabaseType.SQLITE);
 				}
 				else if (Config.convertDatabaseType.equalsIgnoreCase("minidb"))
 				{
 					try
 					{
 						dbFile = new File(Config.convertDatabaseAddress);
 						
 						BufferedReader in = new BufferedReader(new FileReader(dbFile));
 					    String str;
 					    while ((str = in.readLine()) != null) {
					        info = str.split("");
					        balance = info[2].split(":");
					        account = AccountHandler.getAccount(info[1]);
 					        account.setBalance(Double.parseDouble(balance[1]));
 					    }
 					    in.close();
 					} catch (FileNotFoundException e)
 					{
 						e.printStackTrace();
 					} catch (IOException e)
 					{
 						e.printStackTrace();
 					}
 				}
 				if (database.getType().equals(DatabaseType.MYSQL) || database.getType().equals(DatabaseType.SQLITE))
 				{
 					try
 					{
 						result = database.query("SELECT * FROM " + Config.convertTableName, true);
 						if (result != null)
 						{
 							int i = 0;
 							while (result.next())
 							{
 								account = AccountHandler.getAccount(result.getString("username"));
 								account.setBalance(result.getDouble("balance"));
 								i++;
 							}
 							ILogger.info(i + " accounts converted from the iConomy database to the Craftconomy database");
 							return;
 						}
 					} catch (SQLException e)
 					{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			
 		}
 	}
 }
