 package com.flashofsilver.emeraldmarket;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.text.DecimalFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class EmeraldMarket extends JavaPlugin {
 	// enum declaration
 	public enum OfferType {
 		BUY, SELL
 	}
 
 	// Important plugin objects
 	private static Server server;
 	private static Logger logger;
 	private static PluginManager pluginManager;
 	private static EmeraldMarket plugin;
 	private static PluginDescriptionFile description;
 	public static Economy econ = null;
 	//
 	public boolean verbose;
 	// sql vars
 	private String URL;
 	private String dbUser;
 	private String dbPass;
 	// currency format DON'T TOUCH THIS
 	DecimalFormat currency = new DecimalFormat("#.##");
 
 	private Connection connection;
 
 	// Getters
 
 	public static Logger getBukkitLogger() {
 		return logger;
 	}
 
 	public static Server getBukkitServer() {
 		return server;
 	}
 
 	public static String getVersion() {
 		return description.getVersion();
 	}
 
 	public static EmeraldMarket getPlugin() {
 		return plugin;
 	}

 	public static String getPluginName() {
 		return description.getName();
 	}
 
 	public String matchPartialUser(CommandSender sender, String input) {
 		List<Player> list = server.matchPlayer(input);
 		if (list.size() == 1) {
 			Player player = list.get(0);
 			// There is only one player by that name, handle as normal
 			return player.getName();
 		} else if (list.size() > 1) {
 			// Multiple players were found by that name, warn the sender
 			sender.sendMessage(ChatColor.RED + "Multiple returns for '" + input + "'");
 			return null;
 		} else {
 			//
 			// ---- START OPTIONAL CODE ----
 			// No online players found - try the iConomy database
 			// #TODO Remove this before release - iCo SQL compat only.
 
 			try {
 				Statement statement;
 				statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_READ_ONLY);
 				ResultSet resultset;
 				// try to get the alias from the table
 				resultset = statement.executeQuery("SELECT username FROM iConomy WHERE username LIKE '%"
 						+ input + "%';");
 				if (resultset == null || !resultset.first()) {
 					// if nothing turns, oh well. Good try.
 					if (statement != null) {
 						statement.close();
 					}
 					if (resultset != null) {
 						resultset.close();
 					}
 				} else {
 					String result = resultset.getString("username");
 					if (getResultSetNumRows(resultset) == 1) {
 						// Only one player by that name, handle as
 						// normal
 						if (statement != null) {
 							statement.close();
 						}
 						if (resultset != null) {
 							resultset.close();
 						}
 						return result;
 					} else {
 						if (statement != null) {
 							statement.close();
 						}
 						if (resultset != null) {
 							resultset.close();
 						}
 						// Multiple players were found, warn the sender
 						sender.sendMessage(ChatColor.RED + "Multiple returns for '" + input + "'");
 						return null;
 					}
 				}
 				// close things down.
 				if (statement != null) {
 					statement.close();
 				}
 				if (resultset != null) {
 					resultset.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			//
 			// ---- END OPTIONAL CODE ----
 			//
 
 			// No online players found - try the EM aliases database
 
 			try {
 				Statement statement;
 				statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_READ_ONLY);
 				ResultSet resultset;
 				// try to get the alias from the table
 				resultset = statement
 						.executeQuery("SELECT user FROM emeraldmarket_aliases WHERE user LIKE '%" + input
 								+ "%';");
 				if (resultset == null || !resultset.first()) {
 					// if nothing turns, oh well. Good try, tell the user.
 					sender.sendMessage(ChatColor.RED + "No returns for '" + input + "' (user offline?)");
 					if (statement != null) {
 						statement.close();
 					}
 					if (resultset != null) {
 						resultset.close();
 					}
 					return null;
 				} else {
 					String result = resultset.getString("user");
 					if (getResultSetNumRows(resultset) == 1) {
 						// Only one player by that name, handle as
 						// normal
 						if (statement != null) {
 							statement.close();
 						}
 						if (resultset != null) {
 							resultset.close();
 						}
 						return result;
 					} else {
 						if (statement != null) {
 							statement.close();
 						}
 						if (resultset != null) {
 							resultset.close();
 						}
 						// Multiple players were found, warn the sender
 						sender.sendMessage(ChatColor.RED + "Multiple returns for '" + input + "'");
 						return null;
 					}
 				}
 				// close things down.
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			// Nothing found. Tell the user.
 			sender.sendMessage(ChatColor.RED + "No returns for '" + input + "' (user offline?)");
 			return null;
 		}
 	}
 
 	public String getAlias(String input) {
 		// THIS VERSION CANNOT NOTIFY IF GENERATION FAILED - USE WITH CAUTION
 		ResultSet resultset;
 		String testalias = "";
 		try {
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			// checks if user has an alias already - if not, autocreates one.
 			// try to get the alias from the table
 			resultset = statement.executeQuery("SELECT masteralias FROM emeraldmarket_aliases "
 					+ "WHERE user = '" + input + "';");
 			// if not set then use the uppercased first 4 letters of their name
 			if (resultset == null || !resultset.first()) {
 				if (verbose) {
 					logger.info("Alias for user '" + input + "' not found. Generating...");
 				}
 				testalias = input.substring(0, 4).toUpperCase();
 				// check whether the alias has been used before...
 				resultset = statement.executeQuery("SELECT user FROM emeraldmarket_aliases "
 						+ "WHERE masteralias = '" + testalias + "';");
 				// if nothing, then it hasn't been used before.
 				if (resultset == null || !resultset.first()) {
 					statement.executeUpdate("INSERT INTO emeraldmarket_aliases (user, masteralias) "
 							+ "VALUES ('" + input + "', '" + testalias + "');");
 					if (verbose) {
 						logger.info("Added user '" + input + "' to alias table under '" + testalias + "'");
 					}
 					// close things down.
 					if (statement != null) {
 						statement.close();
 					}
 					if (resultset != null) {
 						resultset.close();
 					}
 					return testalias;
 				} else {
 					// if generation fails then then only thing that can be done
 					// is return null
 					//
 					// You'll need to add them manually using the admin
 					// commands.
 					//
 					// close things down.
 					if (statement != null) {
 						statement.close();
 					}
 					if (resultset != null) {
 						resultset.close();
 					}
 					return null;
 				}
 			} else {
 				// if the query returns an alias, return that.
 				testalias = resultset.getString("masteralias");
 
 				if (statement != null) {
 					statement.close();
 				}
 				if (resultset != null) {
 					resultset.close();
 				}
 				return testalias;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public String getAlias(CommandSender sender) {
 		ResultSet resultset;
 		String testalias = "";
 		try {
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			// checks if user has an alias already - if not, autocreates one.
 			// try to get the alias from the table
 			resultset = statement.executeQuery("SELECT masteralias FROM emeraldmarket_aliases "
 					+ "WHERE user = '" + sender.getName() + "';");
 			// if not set then use the uppercased first 4 letters of their name
 			if (resultset == null || !resultset.first()) {
 				if (verbose) {
 					logger.info("Alias for user '" + sender.getName() + "' not found. Generating...");
 				}
 				testalias = sender.getName().substring(0, 4).toUpperCase();
 				// check whether the alias has been used before...
 				resultset = statement.executeQuery("SELECT user FROM emeraldmarket_aliases "
 						+ "WHERE masteralias = '" + testalias + "';");
 				// if nothing, then it hasn't been used before.
 				if (resultset == null || !resultset.first()) {
 					statement.executeUpdate("INSERT INTO emeraldmarket_aliases (user, masteralias) "
 							+ "VALUES ('" + sender.getName() + "', '" + testalias + "');");
 					if (verbose) {
 						logger.info("Added user '" + sender.getName() + "' to alias table under '"
 								+ testalias + "'");
 					}
 					// close things down.
 					if (statement != null) {
 						statement.close();
 					}
 					if (resultset != null) {
 						resultset.close();
 					}
 					return testalias;
 				} else {
 					// if generation fails then tell the user and return null.
 					sender.sendMessage(ChatColor.RED + "Alias generation failed. Ask an admin.");
 					//
 					//
 					// You'll need to add them manually using the admin
 					// commands.
 					//
 					//
 					// close things down.
 					if (statement != null) {
 						statement.close();
 					}
 					if (resultset != null) {
 						resultset.close();
 					}
 					return null;
 				}
 			} else {
 				// if the query returns an alias, return that.
 				testalias = resultset.getString("masteralias");
 
 				if (statement != null) {
 					statement.close();
 				}
 				if (resultset != null) {
 					resultset.close();
 				}
 				return testalias;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public String getAlias(CommandSender admin, String input) {
 		// This is the admin version, for one user autogenerating another user's
 		// THIS IS NOT THE VERSION FOR FORCE SETTING ALIASES
 		ResultSet resultset;
 		String testalias = "";
 		try {
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			// checks if user has an alias already - if not, autocreates one.
 			// try to get the alias from the table
 			resultset = statement.executeQuery("SELECT masteralias FROM emeraldmarket_aliases "
 					+ "WHERE user = '" + input + "';");
 			// if not set then use the uppercased first 4 letters of their name
 			if (resultset == null || !resultset.first()) {
 				if (verbose) {
 					logger.info("Alias for user '" + input + "' not found. Generating...");
 				}
 				testalias = input.substring(0, 4).toUpperCase();
 				// check whether the alias has been used before...
 				resultset = statement.executeQuery("SELECT user FROM emeraldmarket_aliases "
 						+ "WHERE masteralias = '" + testalias + "';");
 				// if nothing, then it hasn't been used before.
 				if (resultset == null || !resultset.first()) {
 					statement.executeUpdate("INSERT INTO emeraldmarket_aliases (user, masteralias) "
 							+ "VALUES ('" + input + "', '" + testalias + "');");
 					if (verbose) {
 						logger.info("Added user '" + input + "' to alias table under '" + testalias + "'");
 					}
 					// close things down.
 					if (statement != null) {
 						statement.close();
 					}
 					if (resultset != null) {
 						resultset.close();
 					}
 					return testalias;
 				} else {
 					// if generation fails then tell the user and return null.
 					admin.sendMessage(ChatColor.RED + "Alias generation failed. Try setting manually.");
 					//
 					//
 					// You'll need to add them manually using the admin
 					// commands.
 					//
 					//
 					// close things down.
 					if (statement != null) {
 						statement.close();
 					}
 					if (resultset != null) {
 						resultset.close();
 					}
 					return null;
 				}
 			} else {
 				// if the query returns an alias, return that.
 				testalias = resultset.getString("masteralias");
 
 				if (statement != null) {
 					statement.close();
 				}
 				if (resultset != null) {
 					resultset.close();
 				}
 				return testalias;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public boolean forceAlias(CommandSender sender, String[] args) {
 		// checks if user has an alias already.
 		// if so, change it to the new one.
 		// if not, create one.
 		ResultSet resultset;
 		try {
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			// try to get the alias from the table
 			resultset = statement.executeQuery("SELECT masteralias FROM emeraldmarket_aliases "
 					+ "WHERE user = '" + args[0] + "';");
 			// if the user is not in the table then INSERT into table.
 			// if nothing, then it hasn't been used before.
 			if (resultset == null || !resultset.first()) {
 				statement.executeUpdate("INSERT INTO emeraldmarket_aliases (user, masteralias) "
 						+ "VALUES ('" + args[0] + "', '" + args[1].toUpperCase() + "');");
 				if (verbose) {
 					logger.info(sender.getName() + " forcibly added user '" + args[0]
 							+ "' to alias table under '" + args[1].toUpperCase() + "'");
 				}
 				// close things down.
 				if (statement != null) {
 					statement.close();
 				}
 				if (resultset != null) {
 					resultset.close();
 				}
 				return true;
 			} else {
 				// if the user is in the table already then UPDATE table.
 				statement.executeUpdate("UPDATE emeraldmarket_aliases SET masteralias = '"
 						+ args[1].toUpperCase() + "' WHERE user = '" + args[0] + "';");
 				if (verbose) {
 					logger.info(sender.getName() + " forcibly changed user '" + args[0]
 							+ "' in alias table to '" + args[1].toUpperCase() + "'");
 				}
 				if (statement != null) {
 					statement.close();
 				}
 				if (resultset != null) {
 					resultset.close();
 				}
 				return true;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private boolean createSQL() throws SQLException, ClassNotFoundException {
 		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 				ResultSet.CONCUR_READ_ONLY);
 		try {
 			// statement.executeUpdate("DROP emeraldmarket_aliases IF EXISTS emeraldmarket_aliases;");
 			statement.executeUpdate("CREATE TABLE emeraldmarket_aliases ( user VARCHAR( 32 )" + ""
 					+ "  PRIMARY KEY NOT NULL UNIQUE, masteralias VARCHAR( 4 ) UNIQUE);");
 		} catch (SQLException e) {
 			logger.info(" SQL Exception: " + e);
 			return false;
 		}
 		try {
 			// statement.executeUpdate("DROP emeraldmarket_buy IF EXISTS emeraldmarket_buy;");
 			statement.executeUpdate("CREATE TABLE emeraldmarket_buy ( user VARCHAR( 32 ) NOT NULL,"
 					+ "alias  VARCHAR( 4 ) NOT NULL REFERENCES emeraldmarket_aliases( masteralias ) "
 					+ "ON DELETE RESTRICT ON UPDATE CASCADE MATCH FULL, "
 					+ "price  DOUBLE( 64, 2 ) NOT NULL, amount INT( 5 ) NOT NULL,"
 					+ "date DATETIME NOT NULL, PRIMARY KEY (user, date) );");
 		} catch (SQLException e) {
 			logger.info(" SQL Exception: " + e);
 			return false;
 		}
 		try {
 			// statement.executeUpdate("DROP emeraldmarket_sell IF EXISTS emeraldmarket_sell;");
 			statement.executeUpdate("CREATE TABLE emeraldmarket_sell ( user VARCHAR( 32 ) NOT NULL,"
 					+ "alias  VARCHAR( 4 ) NOT NULL REFERENCES emeraldmarket_aliases( masteralias ) "
 					+ "ON DELETE RESTRICT ON UPDATE CASCADE MATCH FULL, "
 					+ "price  DOUBLE( 64, 2 )  NOT NULL, amount INT( 5 ) NOT NULL,"
 					+ "date DATETIME NOT NULL, PRIMARY KEY (user, date) );");
 		} catch (SQLException e) {
 			logger.info(" SQL Exception: " + e);
 			return false;
 		}
 		try {
 			// statement.executeUpdate("DROP emeraldmarket_refunds IF EXISTS emeraldmarket_refunds;");
 			statement.executeUpdate("CREATE TABLE emeraldmarket_refunds ( user VARCHAR( 32 ) NOT NULL,"
 					+ "alias  VARCHAR( 4 ) NOT NULL REFERENCES emeraldmarket_aliases( masteralias ) "
 					+ "ON DELETE RESTRICT ON UPDATE CASCADE MATCH FULL, money  DOUBLE( 64, 2 )  "
 					+ "NOT NULL, emeralds INT( 5 ) NOT NULL, date DATETIME NOT NULL, "
 					+ "datecomplete DATETIME DEFAULT NULL, PRIMARY KEY (user, date) );");
 		} catch (SQLException e) {
 			logger.info(" SQL Exception: " + e);
 			return false;
 		}
 		try {
 			// statement.executeUpdate("DROP emeraldmarket_deals IF EXISTS emeraldmarket_deals;");
 			statement.executeUpdate("CREATE TABLE emeraldmarket_deals ( buyer VARCHAR( 32 ) NOT NULL, "
 					+ "buyalias  VARCHAR( 4 ) NOT NULL REFERENCES emeraldmarket_aliases( masteralias ) "
 					+ "ON DELETE RESTRICT ON UPDATE CASCADE MATCH FULL, seller VARCHAR( 32 ) NOT NULL, "
 					+ "sellalias  VARCHAR( 4 ) NOT NULL REFERENCES emeraldmarket_aliases( masteralias ) "
 					+ "ON DELETE RESTRICT ON UPDATE CASCADE MATCH FULL, price  DOUBLE( 64, 2 )  NOT NULL, "
 					+ "amount INT( 5 ) NOT NULL, dateaccepted DATETIME PRIMARY KEY NOT NULL, "
 					+ "datelisted DATETIME DEFAULT NULL, datecompleted DATETIME DEFAULT NULL, "
 					+ "buyernotified BIT NOT NULL DEFAULT 0, sellernotified BIT NOT NULL DEFAULT 0 );");
 		} catch (SQLException e) {
 			logger.info(" SQL Exception: " + e);
 			return false;
 		}
 		return true;
 	}
 
 	@SuppressWarnings("unused")
 	private String getDateDiff(Timestamp input) {
 		// returns the elapsed time from a Timestamp in String form until now.
 		// ----
 		// get current date/time for comparison.
 		java.util.Date currtime = new Date();
 		// convert both to milliseconds and subtract them
 		long diffInMS = Math.abs(currtime.getTime() - input.getTime());
 		// if it's been under a minute, use seconds.
 		if (diffInMS <= 60000) {
 			int diffInS = (int) (diffInMS / 1000);
 			if (diffInS == 1) {
 				return (diffInS + " second");
 			} else {
 				return (diffInS + " seconds");
 			}
 		} else {
 			// if it's been under an hour, use minutes.
 			if (diffInMS <= 3600000) {
 				int diffInMin = (int) (diffInMS / 60000);
 				if (diffInMin == 1) {
 					return (diffInMin + " minute");
 				} else {
 					return (diffInMin + " minutes");
 				}
 			} else {
 				// if it's been under a day, use hours.
 				if (diffInMS <= 86400000) {
 					int diffInH = (int) (diffInMS / 3600000);
 					if (diffInH == 1) {
 						return (diffInH + " hour");
 					} else {
 						return (diffInH + " hours");
 					}
 				} else {
 					// if it's been under a month, use days.
 					// note - capital L at the end of the number below denotes a
 					// LongInt
 					if (diffInMS <= 2628000000L) {
 						int diffInD = (int) (diffInMS / 86400000);
 						if (diffInD == 1) {
 							return (diffInD + " day");
 						} else {
 							return (diffInD + " days");
 						}
 					} else {
 						// if it's bigger than all those, use months (unlikely!)
 						// note - capital L at the end of the number below
 						// denotes a LongInt
 						int diffInMonths = (int) (diffInMS / 2628000000L);
 						if (diffInMonths == 1) {
 							return (diffInMonths + "month");
 						} else {
 							return (diffInMonths + " months");
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private int getResultSetNumRows(ResultSet res) {
 		try {
 			// get row at beginning so as to not affect it
 			int originalPlace = res.getRow();
 			res.last();
 			// Get the row number of the last row which is also the row count
 			int rowCount = res.getRow();
 			// move row back to original position
 			res.absolute(originalPlace);
 			return rowCount;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	private int runBuyOffer(CommandSender sender, Double price, int amountToSell) {
 		// returns 0 if offer was accepted successfully, -1 if it failed,
 		// and the amount REMAINING if there weren't enough offers to satisfy.
 		// this method TAKES A BUY OFFER (sells emeralds)
 		//
 		// database resultset object declaration
 		ResultSet offerRS;
 		// amountremaining continues over into the next offer(s) in
 		// the event that a single offer cannot satisfy buyer demand
 		Integer amountRemaining = amountToSell;
 		// ^ this is declared here so that we can check at the end to see if it
 		// has been initialised
 		try {
 			Statement statementOffers = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			// query for buy listings
 			offerRS = statementOffers.executeQuery("SELECT user, alias, price, amount, date "
 					+ "FROM emeraldmarket_buy WHERE price = '" + currency.format(price) + "' ORDER BY date;");
 			if ((offerRS == null) || (!offerRS.first())) {
 				// if nothing, something has gone wrong.
 				sender.sendMessage(ChatColor.RED + "Your request could not be processed.");
 				return -1;
 			} else {
 				// we are only interested in the topmost offer at this point -
 				// the oldest offer
 				offerRS.beforeFirst(); // move cursor before first so we can
 										// step into it
 				// transactionsmade tracks the number of offers accepted
 				int transactionsMade = 0;
 				// retrieve the date so that we can stamp all relevant DB
 				// entries - done out-of-loop to avoid unnecessary load
 				Object datestamp = new java.sql.Timestamp((new Date()).getTime());
 				// while the amount the user has requested has not been
 				// satisfied, continue buying.
 				while (amountRemaining > 0) {
 					if (offerRS.next()) {
 						// currentSell stores the current demand (for this
 						// amountRS row)
 						int currentSell;
 						// currentBuy is equal to the lowest integer value
 						// out of remaining demand and available supply
 						if (amountRemaining > offerRS.getInt("amount")) {
 							currentSell = offerRS.getInt("amount");
 						} else {
 							currentSell = amountRemaining;
 						}
 						// In order to process the offer we start by
 						// inserting the a row to emeraldmarket_deals so the
 						// database can track the progress of the deal
 						try {
 							Statement statement = connection.createStatement(
 									ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							statement.executeUpdate("INSERT INTO emeraldmarket_deals (buyer, buyalias, "
 									+ "seller, sellalias, price, amount, datelisted, dateaccepted, "
 									+ "buyernotified, sellernotified) VALUES ('" + offerRS.getString("user")
 									+ "', '" + getAlias(offerRS.getString("user")) + "', '"
 									+ sender.getName() + "', '" + getAlias(sender) + "', '" + price + "', '"
 									+ currentSell + "', '" + offerRS.getString("date") + "', '" + datestamp
 									+ "', 0, 0);");
 							// note that both buyernotified and
 							// sellernotifed are both set to FALSE - this is
 							// so that a single method called by the deal
 							// complete event can handle giving money or
 							// emeralds to both the buyer and the seller
 						} catch (SQLException e) {
 							sender.sendMessage(ChatColor.RED + "Your request could not be processed.");
 							logger.info(" SQL Exception: " + e);
 							return -1;
 						}
 
 						// Remove/update the row from emeraldmarket_buy
 						// REMOVE if we have completely accepted it
 						// (if demand >= the offer)
 						// UPDATE if we haven't finished the offer (if
 						// demand was less than the offer)
 						Statement statementUpdates = connection.createStatement(
 								ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 
 						// just check that the amount to modify by is not
 						// negative, because that would be bad
 						if ((offerRS.getInt("amount") - currentSell) <= 0) {
 							// if it is, something has gone wrong.
 							sender.sendMessage(ChatColor.RED + "Your request could not be processed.");
 							return -1;
 						} else {
 							try {
 								statementUpdates.executeUpdate("UPDATE emeraldmarket_buy SET amount = '"
 										+ (offerRS.getInt("amount") - currentSell) + "' WHERE price = '"
 										+ currency.format(price) + "' AND user = '"
 										+ offerRS.getString("user") + "';");
 							} catch (SQLException e) {
 								sender.sendMessage(ChatColor.RED + "Your request could not be processed.");
 								logger.info(" SQL Exception: " + e);
 								return -1;
 							}
 						}
 
 						// after use, close the statement
 						statementUpdates.close();
 
 						// change amountremaining and transactionsmade
 						amountRemaining = amountRemaining - currentSell;
 						transactionsMade = transactionsMade + 1;
 
 						sender.sendMessage(ChatColor.GRAY + Integer.toString(transactionsMade) + ". "
 								+ ChatColor.WHITE + "SOLD " + currentSell + " emeralds to "
 								+ offerRS.getString("alias") + " at " + currency.format(price) + " "
 								+ econ.currencyNamePlural() + ChatColor.GRAY + "/emerald");
 					} else {
 						// if we've run out of offers but we still have demand,
 						// let the user know
 						if (amountRemaining > 0) {
 							sender.sendMessage(ChatColor.RED + "Insufficient demand. Placing offers.");
 						}
 						return amountRemaining;
 					}
 				}
 				// it never hurts to double check
 				if (amountRemaining == 0) {
 					// close statement object, return 0
 					statementOffers.close();
 					return 0;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		if ((amountRemaining == 0) || (amountRemaining == null)) {
 			return 0;
 		} else {
 			// if we have run to this point and not returned true (and
 			// amountRemaining is 0 or null) then something is wrong
 			sender.sendMessage(ChatColor.RED + "Your request could not be processed.");
 			return -1;
 		}
 	}
 
 	private int runSellOffer(CommandSender sender, Double price, int amountToBuy) {
 		// returns true if offer was accepted successfully, false otherwise.
 		// this method TAKES A SELL OFFER (buy emeralds)
 		//
 		// database resultset object declaration
 		ResultSet offerRS;
 		// amountremaining continues over into the next offer(s) in
 		// the event that a single offer cannot satisfy buyer demand
 		Integer amountRemaining = amountToBuy;
 		// ^ this is declared here so that we can check at the end to see if it
 		// has been initialised
 		try {
 			Statement statementOffers = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			// query for buy listings
 			offerRS = statementOffers.executeQuery("SELECT user, alias, price, amount, date "
 					+ "FROM emeraldmarket_sell" + " WHERE price = '" + currency.format(price)
 					+ "' ORDER BY date;");
 			if ((offerRS == null) || (!offerRS.first())) {
 				// if nothing, something has gone wrong.
 				sender.sendMessage(ChatColor.RED + "Your request could not be processed.");
 				return -1;
 			} else {
 				// we are only interested in the topmost offer at this point -
 				// the oldest offer
 				offerRS.beforeFirst(); // move cursor before first so we can
 										// step into it
 				// transactionsmade tracks the number of offers accepted
 				int transactionsMade = 0;
 				// retrieve the date so that we can stamp all relevant DB
 				// entries - done out-of-loop to avoid unnecessary load
 				Object datestamp = new java.sql.Timestamp((new Date()).getTime());
 				// while the amount the user has requested has not been
 				// satisfied, continue buying.
 				while (amountRemaining > 0) {
 					if (offerRS.next()) {
 						// currentBuy stores the current demand (for this
 						// amountRS row)
 						int currentBuy;
 						// currentBuy is equal to the lowest integer value
 						// out of remaining demand and available supply
 						if (amountRemaining > offerRS.getInt("amount")) {
 							currentBuy = offerRS.getInt("amount");
 						} else {
 							currentBuy = amountRemaining;
 						}
 						// In order to process the offer we start by
 						// inserting the a row to emeraldmarket_deals so the
 						// database can track the progress of the deal
 						try {
 							Statement statement = connection.createStatement(
 									ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							statement.executeUpdate("INSERT INTO emeraldmarket_deals (buyer, buyalias, "
 									+ "seller, sellalias, price, amount, datelisted, dateaccepted, "
 									+ "buyernotified, sellernotified) VALUES ('" + sender.getName() + "', '"
 									+ getAlias(sender) + "', '" + offerRS.getString("user") + "', '"
 									+ getAlias(offerRS.getString("user")) + "', '" + price + "', '"
 									+ currentBuy + "', '" + offerRS.getString("date") + "', '" + datestamp
 									+ "', 0, 0);");
 							// note that both buyernotified and
 							// sellernotifed are both set to FALSE - this is
 							// so that a single method called by the deal
 							// complete event can handle giving money or
 							// emeralds to both the buyer and the seller
 						} catch (SQLException e) {
 							sender.sendMessage(ChatColor.RED + "Your request could not be processed.");
 							logger.info(" SQL Exception: " + e);
 							return -1;
 						}
 
 						// Remove/update the row from emeraldmarket_buy
 						// REMOVE if we have completely accepted it
 						// (if demand >= the offer)
 						// UPDATE if we haven't finished the offer (if
 						// demand was less than the offer)
 						Statement statementUpdates = connection.createStatement(
 								ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 
 						// just check that the amount to modify by is not
 						// negative, because that would be bad
 						if ((offerRS.getInt("amount") - currentBuy) <= 0) {
 							// if it is, something has gone wrong.
 							sender.sendMessage(ChatColor.RED + "Your request could not be processed.");
 							return -1;
 						} else {
 							try {
 								statementUpdates.executeUpdate("UPDATE emeraldmarket_buy SET amount = '"
 										+ (offerRS.getInt("amount") - currentBuy) + "' WHERE price = '"
 										+ currency.format(price) + "' AND user = '"
 										+ offerRS.getString("user") + "';");
 							} catch (SQLException e) {
 								sender.sendMessage(ChatColor.RED + "Your request could not be processed.");
 								logger.info(" SQL Exception: " + e);
 								return -1;
 							}
 						}
 
 						// if the offer completes, initialise and call an event
 						OfferAcceptedEvent OAevent = new OfferAcceptedEvent(OfferType.BUY,
 								offerRS.getString("user"), sender.getName(), price, currentBuy);
 						// Call the event
 						Bukkit.getServer().getPluginManager().callEvent(OAevent);
 
 						// after use, close the statement
 						statementUpdates.close();
 
 						// change amountremaining and transactionsmade
 						amountRemaining = amountRemaining - currentBuy;
 						transactionsMade = transactionsMade + 1;
 
 						sender.sendMessage(ChatColor.GRAY + Integer.toString(transactionsMade) + ". "
 								+ ChatColor.WHITE + "BOUGHT " + currentBuy + " emeralds from "
 								+ offerRS.getString("alias") + " at " + currency.format(price) + " "
 								+ econ.currencyNamePlural() + ChatColor.GRAY + "/emerald");
 					} else {
 						// if we've run out of offers but we still have demand,
 						// let the user know
 						if (amountRemaining > 0) {
 							sender.sendMessage(ChatColor.RED + "Insufficient demand. Placing offers.");
 						}
 						return amountRemaining;
 					}
 				}
 				// it never hurts to double check
 				if (amountRemaining == 0) {
 					// close statement object, return true
 					statementOffers.close();
 					return 0;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		if ((amountRemaining == 0) || (amountRemaining == null)) {
 			return 0;
 		} else {
 			// if we have run to this point and not returned true (and
 			// amountRemaining is 0 or null) then something is wrong
 			sender.sendMessage(ChatColor.RED + "Your request could not be processed.");
 			return -1;
 		}
 	}
 
 	private void displayBuyOffers(CommandSender sender) {
 		ResultSet resultset;
 		try {
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			// query for buy listings
 			resultset = statement.executeQuery("SELECT DISTINCT price FROM emeraldmarket_buy "
 					+ "ORDER BY price ASC;");
 			if (resultset == null || !resultset.first()) {
 				// if none, tell the user so.
 				sender.sendMessage(ChatColor.DARK_GREEN + "========= " + ChatColor.WHITE + "OFFERS TO BUY"
 						+ ChatColor.DARK_GREEN + " =========");
 				sender.sendMessage(ChatColor.WHITE + "None found.");
 				sender.sendMessage(ChatColor.DARK_GREEN + "=================================");
 			} else {
 				// continue querying to find the offer at the "top of the pile"
 				// defined by "oldest offer" for that particular price point.
 				// write the header, then write the rest as they come in.
 				sender.sendMessage(ChatColor.DARK_GREEN + "========= " + ChatColor.WHITE + "OFFERS TO BUY"
 						+ ChatColor.DARK_GREEN + " =========");
 				// find the number of repeats to do
 				// 5 (arbitrary low number)
 				// or the number of rows in the result set, whichever is lower.
 				int numreps;
 				if (getResultSetNumRows(resultset) < 5) {
 					numreps = getResultSetNumRows(resultset);
 				} else {
 					numreps = 5;
 				}
 				// move cursor before line 1
 				resultset.beforeFirst();
 				for (int i = 1; i < (numreps + 1); i++) {
 					// cycle to next row
 					if (resultset.next()) {
 						double currprice = resultset.getDouble("price");
 						Statement s = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
 								ResultSet.CONCUR_READ_ONLY);
 						ResultSet amountRS = s.executeQuery("SELECT SUM(amount) FROM emeraldmarket_buy "
 								+ "WHERE price = " + Double.toString(currprice) + ";");
 						amountRS.first(); // move to first row
 						sender.sendMessage(ChatColor.GRAY + Integer.toString(i) + ". " + ChatColor.WHITE
 								+ currency.format(currprice) + " " + econ.currencyNamePlural()
 								+ ChatColor.GRAY + " (" + ChatColor.DARK_GREEN + amountRS.getString(1)
 								+ " in demand" + ChatColor.GRAY + ") ");
 						s.close();
 					}
 				}
 				sender.sendMessage(ChatColor.DARK_GREEN + "=================================");
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void displaySellOffers(CommandSender sender) {
 		ResultSet resultset;
 		try {
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			// query for buy listings
 			resultset = statement.executeQuery("SELECT DISTINCT price FROM emeraldmarket_sell "
 					+ "ORDER BY price;");
 			if (resultset == null || !resultset.first()) {
 				// if none, tell the user so.
 				sender.sendMessage(ChatColor.DARK_GREEN + "======== " + ChatColor.WHITE + "EMERALDS FOR SALE"
 						+ ChatColor.DARK_GREEN + " ========");
 				sender.sendMessage(ChatColor.WHITE + "None found.");
 				sender.sendMessage(ChatColor.DARK_GREEN + "=================================");
 			} else {
 				// continue querying to find the offer at the "top of the pile"
 				// defined by "oldest offer" for that particular price point.
 				// write the header, then write the rest as they come in.
 				sender.sendMessage(ChatColor.DARK_GREEN + "======== " + ChatColor.WHITE + "EMERALDS FOR SALE"
 						+ ChatColor.DARK_GREEN + " ========");
 				// find the number of repeats to do
 				// 5 (arbitrary low number)
 				// or the number of rows in the result set, whichever is lower.
 				int numreps;
 				if (getResultSetNumRows(resultset) < 5) {
 					numreps = getResultSetNumRows(resultset);
 				} else {
 					numreps = 5;
 				}
 				resultset.beforeFirst();
 				for (int i = 1; i < (numreps + 1); i++) {
 					// cycle to next row
 					if (resultset.next()) {
 						double currprice = resultset.getDouble("price");
 						Statement s = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
 								ResultSet.CONCUR_READ_ONLY);
 						ResultSet amountRS = s.executeQuery("SELECT SUM(amount) FROM emeraldmarket_sell "
 								+ "WHERE price = " + Double.toString(currprice) + ";");
 						amountRS.first(); // move to first row
 						sender.sendMessage(ChatColor.GRAY + Integer.toString(i) + ". " + ChatColor.WHITE
 								+ currency.format(currprice) + " " + econ.currencyNamePlural()
 								+ ChatColor.GRAY + " (" + ChatColor.DARK_GREEN + amountRS.getString(1)
 								+ " on offer" + ChatColor.GRAY + ") ");
 						s.close();
 					}
 				}
 				sender.sendMessage(ChatColor.DARK_GREEN + "=================================");
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private boolean setupEconomy() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			return false;
 		}
 		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(
 				Economy.class);
 		if (rsp == null) {
 			return false;
 		}
 		econ = rsp.getProvider();
 		return econ != null;
 	}
 
 	@Override
 	public void onEnable() {
 		// static reference to this plugin
 		plugin = this;
 		// start the logger
 		logger = getLogger();
 		pluginManager = getServer().getPluginManager();
 		description = getDescription();
 		server = getServer();
 		logger = getLogger();
 
 		// register events
		pluginManager.registerEvents(new EmeraldMarketEventListener(plugin), plugin);
 		// save config to default location if not already there
 		this.saveDefaultConfig();
 		// verbose logging? retrieve value from config file.
 		verbose = this.getConfig().getBoolean("verboselogging");
 		if (verbose) {
 			logger.info("Verbose logging enabled.");
 		} else {
 			logger.info("Verbose logging disabled.");
 		}
 		// enable command executor
 		EmeraldMarketCommandExecutor EmeraldMarketCommandExecutor = new EmeraldMarketCommandExecutor(this);
 		getCommand("emeraldmarket").setExecutor(EmeraldMarketCommandExecutor);
 		getCommand("emeraldmarketbuy").setExecutor(EmeraldMarketCommandExecutor);
 		getCommand("emeraldmarketbuyaccept").setExecutor(EmeraldMarketCommandExecutor);
 		getCommand("emeraldmarketsell").setExecutor(EmeraldMarketCommandExecutor);
 		getCommand("emeraldmarketsellaccept").setExecutor(EmeraldMarketCommandExecutor);
 		getCommand("emeraldmarketadmin").setExecutor(EmeraldMarketCommandExecutor);
 		// retrieve SQL variables from config
 		URL = this.getConfig().getString("URL");
 		dbUser = this.getConfig().getString("Username");
 		dbPass = this.getConfig().getString("Password");
 		// create database connection
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			connection = DriverManager.getConnection("jdbc:" + URL + "?user=" + dbUser + "&password="
 					+ dbPass);
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		} catch (ClassNotFoundException e2) {
 			e2.printStackTrace();
 		}
 		// first-run initialisation
 		final boolean firstrun = this.getConfig().getBoolean("firstrun");
 		if (firstrun) {
 			try {
 				boolean SQLsuccess = this.createSQL();
 				if (verbose && SQLsuccess) {
 					logger.info("Tables created successfully.");
 				}
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			this.getConfig().set("firstrun", false);
 			this.saveConfig();
 			if (verbose) {
 				logger.info("First-run initialisation complete.");
 			}
 		}
 		if (!setupEconomy()) {
 			logger.info("Vault dependency unsatisfied (Vault could not be found)!");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		// check the database for odds and ends to remove
 		checkDatabase();
 	}
 
 	@Override
 	public void onDisable() {
 		// check the database for odds and ends to remove
 		checkDatabase();
 		// then close the database connection before shutting down
 		if (connection != null) {
 			try {
 				connection.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		if (verbose) {
 			logger.info("Plugin shutdown complete.");
 		}
 	}
 
 	public void acceptsell(CommandSender sender, String[] args) {
 		// /emba PRICE AMOUNT
 		if (args.length == 2) {
 			// this ACCEPTS A SELL OFFER (buys emeralds)
 			runSellOffer(sender, Double.parseDouble(args[0]), Integer.parseInt(args[1]));
 		}
 	}
 
 	public void acceptbuy(CommandSender sender, String[] args) {
 		// /emsa RANK AMOUNT
 		if (args.length == 2) {
 			// this ACCEPTS A BUY OFFER (sells emeralds)
 			runBuyOffer(sender, Double.parseDouble(args[0]), Integer.parseInt(args[1]));
 		}
 	}
 
 	public void sell(CommandSender sender, String[] args) {
 		if (args.length != 2) {
 			if (args.length < 1) {
 				// if no arguments supplied, query for the "buy" trade listings.
 				this.displayBuyOffers(sender);
 			} else {
 				if (args.length < 2) {
 					// sell a single emerald if nothing else written.
 					args[1] = Integer.toString(1);
 				} else {
 					sender.sendMessage(ChatColor.RED + "Wrong number of arguments.");
 					sender.sendMessage(ChatColor.RED + "/ems [price] [amount]");
 				}
 			}
 		} else {
 
 			if (args.length == 2) {
 				// check the player actually has enough emeralds
 				double price = Double.parseDouble(args[0]);
 				int amount = Integer.parseInt(args[1]);
 				// first, retrieve the player's inventory
 				Player player = (Player) sender;
 				PlayerInventory inventory = player.getInventory();
 				ItemStack itemstack = new ItemStack(Material.EMERALD, amount);
 				// if the inventory contains the right amount of emeralds...
 				// begin.
 				if (inventory.contains(itemstack)) {
 					// prep data - USER, PRICE, AMOUNT and DATE.
 					// Also retrieve or create ALIAS, a 4-letter alias.
 					String useralias = getAlias(sender);
 					if (useralias != null) {
 						try {
 							Statement statement = connection.createStatement(
 									ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							// check for matching OFFER TO BUY - if found,
 							// accept it.
 							ResultSet offerRS = statement.executeQuery("SELECT price "
 									+ "FROM emeraldmarket_buy" + " WHERE price = '" + currency.format(price)
 									+ "' ORDER BY date;");
 							int amountremaining = amount;
 							if (!(offerRS == null) && (offerRS.first())) {
 								// if there's a result, accept it.
								int deltaCredit = runSellOffer(sender, price, amount);
								if (deltaCredit > 0) {
								amountremaining = amountremaining - deltaCredit;
								}
 							}
 							// get timestamp for entry to DB
 							Object datestamp = new java.sql.Timestamp((new Date()).getTime());
 							statement.executeUpdate("INSERT INTO emeraldmarket_sell "
 									+ "(user, alias, price, amount, date) VALUES ('" + sender.getName()
 									+ "', (SELECT masteralias from emeraldmarket_aliases where user = '"
 									+ sender.getName() + "'), " + currency.format(price) + ", "
 									+ amountremaining + ", '" + datestamp + "');");
 							if (statement != null) {
 								statement.close();
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						} finally {
 							// when all has been added successfully, remove the
 							// items from the user.
 							inventory.removeItem(itemstack);
 							econ.withdrawPlayer(sender.getName(), Integer.parseInt(args[1]));
 							sender.sendMessage(ChatColor.DARK_GREEN + "Placed sell offer for "
 									+ ChatColor.WHITE + args[1] + " emeralds " + ChatColor.DARK_GREEN + "at "
 									+ ChatColor.WHITE + args[0] + " per emerald.");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Database Error: "
 								+ "Get an admin to set an alias for you.");
 						return;
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "You can't sell emeralds you don't have!");
 					return;
 				}
 			}
 		}
 	}
 
 	public void buy(CommandSender sender, String[] args) {
 		if (args.length != 2) {
 			if (args.length < 1) {
 				// if no arguments supplied, query for the "sell" trade
 				// listings.
 				this.displaySellOffers(sender);
 			} else {
 				if (args.length < 2) {
 					// buy a single emerald if nothing else written.
 					args[1] = Integer.toString(1);
 				} else {
 					sender.sendMessage(ChatColor.RED + "Wrong number of arguments.");
 					sender.sendMessage(ChatColor.RED + "/emb [price] [amount]");
 				}
 			}
 		} else {
 			if (args.length == 2) {
 				// check the player actually has enough money for the
 				// transaction - if so, begin.
 				double price = Double.parseDouble(args[0]);
 				int amount = Integer.parseInt(args[1]);
 				if (price <= econ.getBalance(sender.getName())) {
 					// prep data - USER, PRICE, AMOUNT and DATE.
 					// Also retrieve or create ALIAS, a 4-letter alias.
 					String useralias = getAlias(sender);
 					if (useralias != null) {
 						try {
 							Statement statement = connection.createStatement(
 									ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							// check for matching OFFER TO SELL - if found,
 							// accept it.
 							ResultSet offerRS = statement.executeQuery("SELECT price "
 									+ "FROM emeraldmarket_sell" + " WHERE price = '" + currency.format(price)
 									+ "' ORDER BY date;");
 							int amountremaining = amount;
 							if (!(offerRS == null) && (offerRS.first())) {
 								// if there's a result, accept it.
								int deltaCredit = runSellOffer(sender, price, amount);
								if (deltaCredit > 0) {
								amountremaining = amountremaining - deltaCredit;
								}
 							}
 							// get timestamp for entry to DB
 							Object datestamp = new java.sql.Timestamp((new Date()).getTime());
 							statement.executeUpdate("INSERT INTO emeraldmarket_buy "
 									+ "(user, alias, price, amount, date) VALUES ('" + sender.getName()
 									+ "', (SELECT masteralias from emeraldmarket_aliases where user = '"
 									+ sender.getName() + "'), " + currency.format(price) + ", "
 									+ amountremaining + ", '" + datestamp + "');");
 							if (statement != null) {
 								statement.close();
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						} finally {
 							// when all has been added successfully, remove the
 							// money from the user.
 							EconomyResponse r = econ.withdrawPlayer(sender.getName(),
 									Double.parseDouble(args[0]));
 							if (r.transactionSuccess()) {
 								sender.sendMessage(ChatColor.DARK_GREEN + "Placed buy offer for "
 										+ ChatColor.WHITE + args[1] + " emeralds " + ChatColor.DARK_GREEN
 										+ "at " + ChatColor.WHITE
 										+ currency.format(Double.parseDouble(args[0])) + " "
 										+ econ.currencyNamePlural() + " per emerald.");
 
 							} else {
 								sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Database Error: "
 								+ "Get an admin to set an alias for you.");
 						return;
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "You don't have enough money!");
 					return;
 				}
 			}
 		}
 
 	}
 
 	public void notify(Player player) {
 		// this method runs a check on the database to see if there are any
 		// outstanding deals in the database that need to be credited
 		try {
 			// refunds first
 			ResultSet resultset;
 			// query SQL table for accepted deals and refunds regarding this
 			// player
 			Statement statementRefunds = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			resultset = statementRefunds.executeQuery("SELECT * FROM emeraldmarket_refunds WHERE user = '"
 					+ player.getDisplayName() + "';");
 			if (resultset != null) {
 				// if there is something, execute the refund.
 				// move pointer before the first line ready for cycling through.
 				resultset.beforeFirst();
 				while (resultset.next()) {
 					// #DEVNOTE assumed only emeralds or money are refunded
 					// during any one transaction.
 					int emeralds = resultset.getInt("emeralds");
 					double money = resultset.getDouble("money");
 					if ((emeralds > 0) && (money == 0)) {
 						// open the player's inventory
 						PlayerInventory inventory = player.getInventory();
 						// initialise the itemstack object
 						ItemStack emstack = new ItemStack(Material.EMERALD, emeralds);
 						inventory.addItem(emstack);
 						player.sendMessage(ChatColor.DARK_GREEN + "You have been refunded " + ChatColor.WHITE
 								+ emeralds + " emeralds " + ChatColor.DARK_GREEN + "by an admin.");
 					}
 					if ((emeralds == 0) && (money > 0)) {
 						// add the money.
 						econ.depositPlayer(player.getName(), money);
 						player.sendMessage(ChatColor.DARK_GREEN + "You have been refunded " + ChatColor.WHITE
 								+ currency.format(money) + " " + ChatColor.DARK_GREEN
 								+ econ.currencyNamePlural() + " by an admin.");
 					}
 				}
 			}
 		} catch (SQLException e) {
 			player.sendMessage(ChatColor.RED + "There was a problem.");
 			e.printStackTrace();
 		}
 
 		// deals next
 		try {
 			// completed BUY offers first
 			Statement statementDeals = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			ResultSet resultset = statementDeals
 					.executeQuery("SELECT * FROM emeraldmarket_deals WHERE buyer = '"
 							+ player.getDisplayName() + "' AND buyernotified = 0;");
 			// variable to store the total emeralds gained
 			int totalEmeraldsGained = 0;
 			// variable to store the total money gained
 			double totalMoneyGained = 0d;
 			if (resultset != null) {
 				// if there is something, complete the buy.
 				// move pointer before the first line ready for cycling through.
 				resultset.beforeFirst();
 				while (resultset.next()) {
 					int credit = resultset.getInt("amount");
 					if (credit > 0) {
 						// increment total
 						totalEmeraldsGained = totalEmeraldsGained + credit;
 						// open the player's inventory
 						PlayerInventory inventory = player.getInventory();
 						// initialise the itemstack object
 						ItemStack emStack = new ItemStack(Material.EMERALD, credit);
 						inventory.addItem(emStack);
 						player.sendMessage(ChatColor.DARK_GREEN + "You have bought " + ChatColor.WHITE
 								+ credit + " emeralds " + ChatColor.DARK_GREEN + "from "
 								+ resultset.getString("sellalias") + " at " + ChatColor.WHITE
 								+ resultset.getDouble("price") + " per emerald.");
 						// set sellernotified to true now that we're done
 						Statement statementDealsClosed = connection.createStatement(
 								ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 						statementDealsClosed
								.executeUpdate("UPDATE emeraldmarket_deals SET buyernotified = 1 "
 										+ "WHERE seller = '" + player.getDisplayName()
 										+ "' AND dateaccepted = '" + resultset.getString("dateaccepted")
 										+ "';");
 					}
 				}
 				if (totalEmeraldsGained >= 1) {
 					// summarise if something gained
 					player.sendMessage(ChatColor.DARK_GREEN + "You have received " + ChatColor.WHITE
 							+ totalEmeraldsGained + ChatColor.DARK_GREEN + " emeralds in total.");
 				}
 			}
 			// clear resultset for next round.
 			if (resultset != null) {
 				resultset.close();
 			}
 			// completed SELL offers next
 			resultset = statementDeals.executeQuery("SELECT * FROM emeraldmarket_deals WHERE seller = '"
 					+ player.getDisplayName() + "' AND sellernotified = 0;");
 			if (resultset != null) {
 				// if there is something, complete the sell.
 				// move pointer before the first line ready for cycling through.
 				resultset.beforeFirst();
 				while (resultset.next()) {
 					// money earned = emeralds sold * price per emerald
 					double credit = (resultset.getInt("amount") * resultset.getDouble("price"));
 					if (credit > 0) {
 						// increment total
 						totalMoneyGained = totalMoneyGained + credit;
 						// add the money.
 						econ.depositPlayer(player.getName(), credit);
 						player.sendMessage(ChatColor.DARK_GREEN + "You have sold " + ChatColor.WHITE
 								+ resultset.getInt("amount") + " emeralds " + ChatColor.DARK_GREEN + "to "
 								+ resultset.getString("sellalias") + " at " + ChatColor.WHITE
 								+ resultset.getDouble("price") + " per emerald.");
 						// set sellernotified to true now that we're done
 						Statement statementDealsClosed = connection.createStatement(
 								ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 						statementDealsClosed
 								.executeUpdate("UPDATE emeraldmarket_deals SET sellernotified = 1 "
 										+ "WHERE seller = '" + player.getDisplayName()
 										+ "' AND dateaccepted = '" + resultset.getString("dateaccepted")
 										+ "';");
 					}
 				}
 				if (totalMoneyGained > 0) {
 					// summarise if something gained
 					player.sendMessage(ChatColor.DARK_GREEN + "You have earned " + ChatColor.WHITE
 							+ currency.format(totalMoneyGained) + " " + ChatColor.DARK_GREEN
 							+ econ.currencyNamePlural() + " in total.");
 				}
 				// close statement
 				statementDeals.close();
 			}
 
 		} catch (SQLException e) {
 			player.sendMessage(ChatColor.RED + "There was a problem.");
 			e.printStackTrace();
 		}
 	}
 
 	public void markComplete() {
 		// this method adds a date of completion to deals when
 		// both the seller and the buyer have been notified
 		try {
 			// get timestamp for entry to DB
 			Object datestamp = new java.sql.Timestamp((new Date()).getTime());
 			// start database connection
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			statement.executeUpdate("UPDATE emeraldmarket_deals SET datecompleted = '" + datestamp + "';");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void removeCompletedDeals(Timestamp input) {
 
 	}
 
 	public void removeEmptyOffers() {
 		// this method deletes offers with no supply remaining
 		try {
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_READ_ONLY);
 			statement.executeUpdate("DELETE FROM emeraldmarket_buy WHERE amount = '0';");
 			statement.executeUpdate("DELETE FROM emeraldmarket_sell WHERE amount = '0';");
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void checkDatabase() {
 		// trims the database so only required things are stored
 		// add completion datestamps to completed deals
 		markComplete();
 		// delete all deals older than 1 week (168 hours)
 		// removeCompletedDeals();
 		// remove all offers with 0 amount remaining
 		removeEmptyOffers();
 	}
 }
