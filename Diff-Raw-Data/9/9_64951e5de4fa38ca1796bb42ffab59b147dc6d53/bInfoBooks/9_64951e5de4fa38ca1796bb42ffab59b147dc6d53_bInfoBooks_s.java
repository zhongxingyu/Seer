 package uk.codingbadgers.binfobooks;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.meta.BookMeta;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import uk.codingbadgers.bFundamentals.module.Module;
 import uk.codingbadgers.binfobooks.commands.CommandBook;
 
 public class bInfoBooks extends Module implements Listener {
 	
 	private HashMap<String, InfoBook> m_books = new HashMap<String, InfoBook>();
 
 	/**
 	 * Called when the module is disabled.
 	 */
 	public void onDisable() {
 	}
 
 	/**
 	 * Called when the module is loaded.
 	 */
 	public void onEnable() {
 		loadBooks();
 		
 		register(this);
 		registerCommand(new CommandBook(this));
 	}
 	
 	@EventHandler (priority=EventPriority.NORMAL)
 	public void onInventoryClick(InventoryClickEvent event) {
 		
 		ItemStack item = event.getCurrentItem();
 		if (!isItemInfoBook(item)) {
 			return;
 		}
 		
 		if (event.getView().getTitle().contains("inventory")) {
 			return;
 		}
 		
 		Module.sendMessage("bInfoBooks", (Player)event.getWhoClicked(), "You can't store InfoBooks. Please drop the InfoBook to remove it from your inventory.");
 		Module.sendMessage("bInfoBooks", (Player)event.getWhoClicked(), "You can get another copy of the book via the '/book' command.");
 		event.setCancelled(true);
 	}
 	
 	@EventHandler (priority=EventPriority.NORMAL)
     public void onPlayerDropItem(PlayerDropItemEvent event) {
 		
 		ItemStack item = event.getItemDrop().getItemStack();
 		if (!isItemInfoBook(item)) {
 			return;
 		}
 
 		event.getItemDrop().remove();
 	}
 	
 	private boolean isItemInfoBook(ItemStack item) {
 		
 		if (item == null) {
 			return false;
 		}
 		
 		if (item.getType() != Material.WRITTEN_BOOK) {
 			return false;
 		}
 		
 		final BookMeta bookmeta = (BookMeta)item.getItemMeta();
 		if (!bookmeta.getDisplayName().equalsIgnoreCase("InfoBook")) {
 			return false;
 		}
 		
 		return true;
 		
 	}
 	
 	private void loadBooks() {
 		
 		m_books.clear();
 		
 		File bookFolder = new File(this.getDataFolder() + File.separator + "books");
 		if (!bookFolder.exists()) {
 			// make the 'books' folder
 			bookFolder.mkdirs();
 			
 			// make a default book
 			makeExampleBook(bookFolder);
 		}
 		
 		for (File file : bookFolder.listFiles()) {
 			final String fileName = file.getName();
 			if (fileName.substring(fileName.lastIndexOf(".")).contains("json")) {
 				
 				this.log(Level.INFO, "Loading Book: " + fileName);
 				
 				String jsonContents = "";
 				try {
 					BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
 				
 					String inputLine;
 		            while ((inputLine = in.readLine()) != null) {
 		                jsonContents += inputLine;
 		            }
 		            
 		            in.close();
 				
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				
 				JSONObject bookJSON = (JSONObject)JSONValue.parse(jsonContents);
 				InfoBook newBook = new InfoBook(bookJSON);
 				m_books.put(newBook.getName().toLowerCase(), newBook);
 			}
 		}
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	private void makeExampleBook(File booksfolder) {
 		
 		JSONObject book = new JSONObject();
 		
 		book.put("name", "Example Book");
 		book.put("author", "McBadgerCraft");
 		
 		JSONArray pages = new JSONArray();
 		pages.add("This is an example book");
 		pages.add("Using bInfoBooks created by");
 		pages.add("TheCodingBadgers");
 		book.put("pages", pages);
 		
 		JSONArray taglines = new JSONArray();
 		taglines.add("bInfoBooks");
 		taglines.add("By TheCodingBadgers");
 		book.put("taglines", taglines);
 		
 		String contents = book.toJSONString();
 		try {
 			 BufferedWriter out = new BufferedWriter(new FileWriter(booksfolder.getAbsolutePath() + File.separator + "examplebook.json"));
 			 out.write(contents);
 			 out.close();		
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public InfoBook bookExists(String bookName) {
 		if (m_books.containsKey(bookName.toLowerCase()))
			return m_books.get(bookName);
 		
 		for (InfoBook book : m_books.values())
 		{
			if (book.getName().toLowerCase().startsWith(bookName.toLowerCase())) {
 				return book;
 			}
 		}
 		
 		return null;
 	}
 	
 	public void listBooks(Player player) {
 		for (InfoBook book : m_books.values()) {
 			Module.sendMessage("bInfoBooks", player, " - " + book.getName() + " by " + book.getAuthor());
 		}
 	}
 	
 	public boolean playerHasBook(Player player, InfoBook infobook) {
 		
 		final PlayerInventory invent = player.getInventory();
 		
 		if (invent.contains(Material.WRITTEN_BOOK)) {
 			for (ItemStack item : invent.getContents()) {
 				if (item == null) {
 					continue;
 				}
 				
 				if (item.getType() == Material.WRITTEN_BOOK) {
 					final BookMeta bookmeta = (BookMeta)item.getItemMeta();
 					
 					if (!bookmeta.getDisplayName().equalsIgnoreCase("InfoBook")) {
 						continue;
 					}
 					
 					if (!infobook.getAuthor().equalsIgnoreCase(bookmeta.getAuthor())) {
 						continue;
 					}
 					
 					if (!infobook.getName().equalsIgnoreCase(bookmeta.getTitle())) {
 						continue;
 					}
 					
 					// The player already has a copy of the given book
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	public boolean givePlayerBook(Player player, InfoBook infobook) {
 		
 		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
 		BookMeta bookmeta = (BookMeta)book.getItemMeta();
 		
 		// fill in book info meta
 		bookmeta.setAuthor(infobook.getAuthor());
 		bookmeta.setTitle(infobook.getName());
 		bookmeta.setLore(infobook.getTagLines());
 		bookmeta.setPages(infobook.getPages(player));		
 		bookmeta.setDisplayName("InfoBook");
 		
 		book.setItemMeta(bookmeta);
 		player.getInventory().addItem(book);
 		return true;
 	}
 }
