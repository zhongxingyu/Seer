 package fr.jules_cesar.Shop;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ShopCommand extends JavaPlugin implements CommandExecutor {
 	
 	public static Plugin plugin = null;
 	
 	private static String shop_message = ChatColor.GOLD + "[SHOP] ";
 	
 	public static void load(Plugin p){
 		plugin = p;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String argument_1, String[] arguments) {
 		if(sender instanceof Player){
 			Player joueur = (Player)sender;
 			if(arguments.length < 2) return liste_commandes(joueur);
 			// INFORMATION SUR UN SHOP
 			else if(arguments.length == 2 && arguments[0].equalsIgnoreCase("information")) return information(joueur, arguments[1], "0");
 			else if(arguments.length == 3 && arguments[0].equalsIgnoreCase("information")) return information(joueur, arguments[1], arguments[2]);
 			// ACHAT
 			else if(arguments.length == 3 && arguments[0].equalsIgnoreCase("acheter")) return acheter(joueur, arguments[1], "0", arguments[2]);
 			else if(arguments.length == 4 && arguments[0].equalsIgnoreCase("acheter")) return acheter(joueur, arguments[1], arguments[2], arguments[3]);
 			// VENTE
 			else if(arguments.length == 3 && arguments[0].equalsIgnoreCase("vendre")) return vendre(joueur, arguments[1], "0", arguments[2]);
 			else if(arguments.length == 4 && arguments[0].equalsIgnoreCase("vendre")) return vendre(joueur, arguments[1], arguments[2], arguments[3]);
 			// CREER
 			else if(arguments.length == 3 && arguments[0].equalsIgnoreCase("creer")) return creer(joueur, arguments[1], "0",arguments[2]);
 			else if(arguments.length == 4 && arguments[0].equalsIgnoreCase("creer")) return creer(joueur, arguments[1], arguments[2], arguments[3]);
 			// AUTRE : AFFICHAGE LISTE COMMANDES
 			else return liste_commandes(joueur);
 		}
 		
 		else{
 			sender.sendMessage("[SHOP] Les commandes sont rservs aux joueurs");
 			return true;
 		}
 	}
 	
 	// Permet de crer le magasin de id
 	private boolean creer(Player joueur, String id, String data, String prix) {
 		if(Vault.perms.has(joueur, "shop.modification")){
 			if(estunNombre(id) && estunNombre(data)){
 				try {
 					Class.forName("com.mysql.jdbc.Driver");
 					 
 					Connection conn = DriverManager.getConnection(main.getUrl(), main.getUser(), main.getPass());
 				      
 				      Statement state = conn.createStatement();
 				      //L'objet ResultSet contient le rsultat de la requte SQL
 				      ResultSet result = state.executeQuery("SELECT COUNT(*) FROM shop WHERE id = " + id + " AND data = " + data);
 				      result.next();
 				      int existe = Integer.parseInt(result.getObject(1).toString()); result.close();
 				      if(existe == 0){
 				    	  state.executeUpdate("INSERT INTO shop (id, data, prix, stock, difference) VALUES ("+id+", "+data+", "+prix+", 0, 0)");
 				    	  state.close();
 				    	  joueur.sendMessage(shop_message + "Le shop de " + Material.getMaterial(Integer.parseInt(id)).toString() + ":" + data + " est disponible !");
 				      }
 				      else{
 				    	  state.close();
 				    	  joueur.sendMessage(shop_message + ChatColor.RED + "Ce shop existe dj !");
 				      }
 				      
 				}
 				catch (ClassNotFoundException e) { e.printStackTrace(); }
 				catch (SQLException e) { e.printStackTrace(); }
 				return true;
 			}
 			else{
 				joueur.sendMessage(shop_message + ChatColor.RED + "L'id, la data et le prix doivent tre des nombres");
 				return true;
 			}
 		}
 		else{
 			joueur.sendMessage(shop_message + ChatColor.RED + "Vous n'avez pas la permission de crer un shop");
 			return true;
 		}
 	}
 	
 	// Permet au joueur de vendre quantite de id
 	private boolean vendre(Player joueur, String id, String data, String quantite) {
 		if(estunNombre(id) && estunNombre(data) && estunNombre(quantite)){
 			if(Integer.parseInt(quantite) > 0){
 				if(joueur.getGameMode() == GameMode.SURVIVAL){
 					try {
 						Class.forName("com.mysql.jdbc.Driver");
 						 
 						Connection conn = DriverManager.getConnection(main.getUrl(), main.getUser(), main.getPass());
 					      
 					      Statement state = conn.createStatement();
 					      //L'objet ResultSet contient le rsultat de la requte SQL
 					      ResultSet result = state.executeQuery("SELECT COUNT(*) FROM shop WHERE id = " + id + " AND data = " + data);
 					      result.next();
 					      int existe = Integer.parseInt(result.getObject(1).toString()); result.close(); state.close(); conn.close();
 					      if(existe != 0){
 					    	  Shop.vendre(Integer.parseInt(quantite), joueur, Integer.parseInt(id), Byte.parseByte(data));
 					      }
 					      else{
 					    	  joueur.sendMessage(shop_message + ChatColor.RED + "Ce shop n'est pas encore configur");
 					      }
 					      
 					}
 					catch (ClassNotFoundException e) { e.printStackTrace(); }
 					catch (SQLException e) { e.printStackTrace(); }
 				}
 				else{
 					joueur.sendMessage(shop_message + ChatColor.RED + "Vous devez tre en mode survie pour vendre");
 				}
 			}
 			else{
 				joueur.sendMessage(shop_message + ChatColor.RED + "La quantit doit tre suprieure  0");
 			}
 		}
 		else{
 			joueur.sendMessage(shop_message + ChatColor.RED + "L'id, la data et la quantit doivent tre des nombres");
 		}
 		return true;
 	}
 	
 	// Permet au joueur d'acheter quantite de id
 	private boolean acheter(Player joueur, String id, String data, String quantite) {
 		if(estunNombre(id) && estunNombre(data) && estunNombre(quantite)){
 			if(Integer.parseInt(quantite) > 0){
 				if(joueur.getGameMode() == GameMode.SURVIVAL){
 					try {
 						Class.forName("com.mysql.jdbc.Driver");
 						 
 						Connection conn = DriverManager.getConnection(main.getUrl(), main.getUser(), main.getPass());
 					      
 					      Statement state = conn.createStatement();
 					      //L'objet ResultSet contient le rsultat de la requte SQL
 					      ResultSet result = state.executeQuery("SELECT COUNT(*) FROM shop WHERE id = " + id + " AND data = " + data);
 					      result.next();
 					      int existe = Integer.parseInt(result.getObject(1).toString()); result.close(); state.close(); conn.close();
 					      if(existe != 0){
 					    	  Shop.acheter(Integer.parseInt(quantite), joueur, Integer.parseInt(id), Byte.parseByte(data));
 					      }
 					      else{
 					    	  joueur.sendMessage(shop_message + ChatColor.RED + "Ce shop n'est pas encore configur");
 					      }
 					      
 					}
 					catch (ClassNotFoundException e) { e.printStackTrace(); }
 					catch (SQLException e) { e.printStackTrace(); }
 				}
 				else{
 					joueur.sendMessage(shop_message + ChatColor.RED + "Vous devez tre en mode survie pour acheter");
 				}
 			}
 			else{
 				joueur.sendMessage(shop_message + ChatColor.RED + "La quantit doit tre suprieure  0");
 			}
 		}
 		else{
 			joueur.sendMessage(shop_message + ChatColor.RED + "L'id, la data et la quantit doivent tre des nombres");
 		}
 		return true;
 	}
 
 	// Dtermine si une chaine de caractre est un nombre
 	private boolean estunNombre(String string){
 		Pattern pattern = Pattern.compile("[^0-9]");
 		Matcher matcher = pattern.matcher(string);
 		if(matcher.find()) return false;
 		return true;
 	}
 
 	// Affiche la liste de commandes du plugin
 	private boolean liste_commandes(Player joueur){
 		joueur.sendMessage(shop_message + "Liste des commandes");
 		joueur.sendMessage(ChatColor.AQUA + "/shop information [id] <data>: Indique le prix actuel et le stock disponible de [id]:<data>");
 		joueur.sendMessage(ChatColor.AQUA + "/shop acheter [id] <data> [quantite] : Acheter [quantite] de [id]:<data>");
 		joueur.sendMessage(ChatColor.AQUA + "/shop vendre [id] <data> [quantite] : Vendre [quantite] de [id]:<data>");
 		return true;
 	}
 	
 	// Affiche les informations sur un item (stock, prix).
 	private boolean information(Player joueur, String id, String data){
 		if(estunNombre(id) && estunNombre(data)){
 			double prix = 0;
 			int stock = 0;
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 				 
 				Connection conn = DriverManager.getConnection(main.getUrl(), main.getUser(), main.getPass());
 			      
 			      Statement state = conn.createStatement();
 			      //L'objet ResultSet contient le rsultat de la requte SQL
 			      ResultSet result = state.executeQuery("SELECT prix, stock FROM shop WHERE id = " + id + " AND data = " + data);
 			      while(result.next()){
 			    	  prix = result.getDouble(1);
 			    	  stock = result.getInt(2);
 			      }
 			      state.close();
 			      conn.close();
 			      
 			}
 			catch (ClassNotFoundException e) { e.printStackTrace(); }
 			catch (SQLException e) { e.printStackTrace(); }
 			if(!(prix == 0 && stock == 0)){
 				ItemStack info = new ItemStack(Integer.parseInt(id), 0, Byte.parseByte(data));
 				joueur.sendMessage(ChatColor.GOLD + "----- " + info.getType().toString() + ":" + data + " -----");
 				joueur.sendMessage(ChatColor.GOLD + "Prix : " + formatDouble(prix, 2) + " EIG");
 				joueur.sendMessage(ChatColor.RED + "Prix sans stock : " + formatDouble(prix * 1.1, 2) + " EIG");
 				joueur.sendMessage(ChatColor.GOLD + "Stock : " + stock);
 			}
 			else{
 				joueur.sendMessage(shop_message + ChatColor.RED + "Ce shop n'est pas encore configur");
 			}
 		}
 		else{
 			joueur.sendMessage(ChatColor.GOLD + "[SHOP] " + ChatColor.RED + "L'id, la data et le prix doivent tre des nombres");
 		}
 		return true;
 	}
 	
	public static String formatDouble(double val, int nb) { 
 		String s = ""+val; 
 		int idx = s.indexOf('.'); 
 		if (idx != -1 && idx < s.length() - nb) 
 		s = s.substring(0, idx + nb+(nb==0 ? 0 : 1)); 
		return s; 
 		}
 }
