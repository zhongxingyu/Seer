 package com.github.derwisch.paperMail;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.configuration.InvalidConfigurationException;
 //import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class PaperMailGUI {
 
 	public static final String RECIPIENT_TITLE = ChatColor.RED + "Recipient" + ChatColor.RESET;
 	public static final String SEND_BUTTON_ON_TITLE = ChatColor.WHITE + "Send" + ChatColor.RESET;
 	public static final String CANCEL_BUTTON_TITLE = ChatColor.WHITE + "Cancel" + ChatColor.RESET;
 	public static final String ENDERCHEST_BUTTON_TITLE = ChatColor.WHITE + "Open Enderchest" + ChatColor.RESET;
 	public static final String MONEY_SEND_BUTTON_TITLE = ChatColor.WHITE + "Send Money" + ChatColor.RESET;
 	public static final String BANK_NOTE_DISPLAY = ChatColor.GREEN + "Bank Note";
 	
 	private static ArrayList<PaperMailGUI> itemMailGUIs = new ArrayList<PaperMailGUI>();
 	private static Map<String, PaperMailGUI> openGUIs = new HashMap<String, PaperMailGUI>();
 	
 	public Inventory Inventory;
 	public Player Player;
 	public static boolean cancel = false;
 
 	private ItemStack recipientMessage; 
 	private ItemStack sendButtonEnabled;
 	private ItemStack cancelButton; 
 	private ItemStack enderChestButton;
 	private ItemStack sendMoneyButton;
 	private boolean paperSent;
 	
 	public SendingGUIClickResult Result = SendingGUIClickResult.CANCEL;
 	
 	public static void RemoveGUI(String playerName) {
 		openGUIs.put(playerName, null);
 		openGUIs.remove(playerName);
 	}
 	
 	public static PaperMailGUI GetOpenGUI(String playerName) {
 		return openGUIs.get(playerName);
 	}
 	
 	public PaperMailGUI(Player player) {
 		this.paperSent = false;
 		Player = player;
 		Inventory = Bukkit.createInventory(player, Settings.MailWindowRows * 9, PaperMail.NEW_MAIL_GUI_TITLE);
 		initializeButtons();
     	itemMailGUIs.add(this);
 	}
 
 	public PaperMailGUI(Player player, boolean paperSent) {
 		this.paperSent = paperSent;
 		Player = player;
 		Inventory = Bukkit.createInventory(player, Settings.MailWindowRows * 9, PaperMail.NEW_MAIL_GUI_TITLE);
 		initializeButtons();
     	itemMailGUIs.add(this);
 	}
 	
 	private void initializeButtons() {
 		Inventory.setMaxStackSize(127);
 		recipientMessage = new ItemStack(Material.PAPER);
 		sendButtonEnabled = new ItemStack(Material.WOOL);
 		cancelButton = new ItemStack(Material.WOOL);
 		enderChestButton = new ItemStack(Material.ENDER_CHEST);
 		sendMoneyButton = new ItemStack(Material.GOLD_INGOT);
 
     	sendButtonEnabled.setDurability((short)5);
     	cancelButton.setDurability((short)14);
 
     	ItemMeta recipientMessageMeta = recipientMessage.getItemMeta();
     	ItemMeta sendButtonEnabledMeta = sendButtonEnabled.getItemMeta();
     	ItemMeta cancelButtonMeta = cancelButton.getItemMeta();
     	ItemMeta enderChestButtonMeta = enderChestButton.getItemMeta();
     	ItemMeta sendMoneyButtonMeta = sendMoneyButton.getItemMeta();
     	
     	ArrayList<String> recipientMessageLore = new ArrayList<String>();
     	ArrayList<String> sendButtonDisabledLore = new ArrayList<String>();
     	ArrayList<String> enderChestButtonLore = new ArrayList<String>();
     	ArrayList<String> sendMoneyButtonLore = new ArrayList<String>();
 
     	recipientMessageLore.add(ChatColor.GRAY + "Add a written book named" + ChatColor.RESET);
     	recipientMessageLore.add(ChatColor.GRAY + "like a player to define" + ChatColor.RESET);
     	recipientMessageLore.add(ChatColor.GRAY + "the recipient." + ChatColor.RESET);
 
     	sendButtonDisabledLore.add(ChatColor.GRAY + "State a recipient before" + ChatColor.RESET);
     	sendButtonDisabledLore.add(ChatColor.GRAY + "sending" + ChatColor.RESET);
 
     	enderChestButtonLore.add(ChatColor.GRAY + "Grants access to your" + ChatColor.RESET);
     	enderChestButtonLore.add(ChatColor.GRAY + "enderchest." + ChatColor.RESET);
     	enderChestButtonLore.add(ChatColor.GRAY + "You return to the mail after" + ChatColor.RESET);
     	enderChestButtonLore.add(ChatColor.GRAY + "closing the enderchest" + ChatColor.RESET);
     	
     	sendMoneyButtonLore.add(ChatColor.GREEN + "Left-Clicking" + ChatColor.GRAY + " this button will" + ChatColor.RESET);
     	sendMoneyButtonLore.add(ChatColor.GRAY + "increase the amount of money" + ChatColor.RESET);
     	sendMoneyButtonLore.add(ChatColor.GRAY + "(if any) that you wish to" + ChatColor.RESET);
     	sendMoneyButtonLore.add(ChatColor.GRAY + "send by increments of " + Settings.Increments + "." + ChatColor.RESET);
     	sendMoneyButtonLore.add(ChatColor.BLUE + "Right-Clicking" + ChatColor.GRAY + " this button will" + ChatColor.RESET);
     	sendMoneyButtonLore.add(ChatColor.GRAY + "decrease this amount by " + Settings.Increments + "." + ChatColor.RESET);
     	sendMoneyButtonLore.add(ChatColor.GRAY + "Minimum send amount of 2. Max" + ChatColor.RESET);
     	sendMoneyButtonLore.add(ChatColor.GRAY + "send amount of 64." + ChatColor.RESET);
     	
     	recipientMessageMeta.setDisplayName(RECIPIENT_TITLE);
     	recipientMessageMeta.setLore(recipientMessageLore);
 
     	sendButtonEnabledMeta.setDisplayName(SEND_BUTTON_ON_TITLE);
 
     	cancelButtonMeta.setDisplayName(CANCEL_BUTTON_TITLE);
     	
     	enderChestButtonMeta.setDisplayName(ENDERCHEST_BUTTON_TITLE);
     	enderChestButtonMeta.setLore(enderChestButtonLore);
     	
     	sendMoneyButtonMeta.setDisplayName(MONEY_SEND_BUTTON_TITLE);
     	sendMoneyButtonMeta.setLore(sendMoneyButtonLore);
 
     	recipientMessage.setItemMeta(recipientMessageMeta);
     	sendButtonEnabled.setItemMeta(sendButtonEnabledMeta);
     	cancelButton.setItemMeta(cancelButtonMeta);
     	enderChestButton.setItemMeta(enderChestButtonMeta);
     	sendMoneyButton.setItemMeta(sendMoneyButtonMeta);
 
     	Inventory.setItem(0, recipientMessage);
     	if (Settings.EnableEnderchest) {
     		Inventory.setItem(8, enderChestButton);
     	}
     	Inventory.setItem(((Settings.MailWindowRows - 1) * 9) - 1, sendButtonEnabled);
     	Inventory.setItem((Settings.MailWindowRows * 9) - 1, cancelButton);
     	if ((Settings.EnableSendMoney == true) && (PaperMail.economy != null)){
     		if(Settings.MailWindowRows > 3){
     		Inventory.setItem(((Settings.MailWindowRows - 2) * 9) -1, sendMoneyButton);
     		}else{
     			Inventory.setItem(7, sendMoneyButton);
     		}
     	}
 	}
 	
 	public void Show() {
 		if (Settings.EnableItemMail) {
 			Player.openInventory(Inventory);
 			openGUIs.put(Player.getName(), this);
 		}
 	}
 	
 	public void SetClosed() {
 		RemoveGUI(Player.getName());
 		itemMailGUIs.remove(this);
 	}
 		
 	public void close() {
 		Player.closeInventory();
 	}
 	
 	@SuppressWarnings("null")
 	public void SendContents() throws IOException, InvalidConfigurationException {
 		Player player = this.Player;
 		ArrayList<ItemStack> sendingContents = new ArrayList<ItemStack>();
 		String playerName = "";
 		int numItems = 0;
 		double itemCost = Settings.ItemCost;
 		double amount = 0;
 		ItemStack CraftStack;
 		for (int i = 0; i < Inventory.getSize(); i++) {
 			
 			CraftStack = Inventory.getItem(i);
 			if (CraftStack == null)
 				continue;
 			
 			ItemMeta itemMeta = CraftStack.getItemMeta();
 			if (itemMeta.getDisplayName() != SEND_BUTTON_ON_TITLE && 
 				itemMeta.getDisplayName() != CANCEL_BUTTON_TITLE && 
 				itemMeta.getDisplayName() != ENDERCHEST_BUTTON_TITLE &&
 				itemMeta.getDisplayName() != RECIPIENT_TITLE &&
 				CraftStack.getType() != Material.WRITTEN_BOOK &&
 				itemMeta.getDisplayName() != MONEY_SEND_BUTTON_TITLE) {
 				sendingContents.add(CraftStack);
 				numItems = numItems + CraftStack.getAmount();
 			}
 			if (CraftStack.getType() == Material.WRITTEN_BOOK && playerName == "") {
 				BookMeta bookMeta = (BookMeta)itemMeta;
 				Player p = Bukkit.getPlayer(bookMeta.getTitle());
 				if (p != null) {
 					playerName = p.getName();
 				    } else {
 				    OfflinePlayer op = Bukkit.getOfflinePlayer(bookMeta.getTitle());
 				    if (op != null) {
 				    	playerName = op.getName();
 				        } else {
 				        	playerName = bookMeta.getTitle();
 				        	player.sendMessage(ChatColor.DARK_RED + "Player "  + playerName + " may not exist or doesn't have an Inbox yet. Creating Inbox for player " + playerName + ChatColor.RESET);
 				        }
 				    }
 			}
 			//   STILL NEED TO CHECK TO SEE IF SENDING MONEY AND MAILCOSTS IS ENABLED IF THERE IS MONEY TO DO BOTH
 			//If Sending Money is enabled, count the amount the player wants to send and convert it to Bank Note.
 			if((itemMeta.getDisplayName() == MONEY_SEND_BUTTON_TITLE) && (Settings.EnableSendMoney == true)){
 				if (CraftStack.getAmount() > 1){
 				amount = CraftStack.getAmount();
 				}
 			}
 		}
 		//Take the money for each item sent if PerItemCosts is enabled
 		if ((Settings.EnableMailCosts == true) && (Settings.PerItemCosts == true) && (Settings.ItemCost != 0) && (!this.Player.hasPermission(Permissions.COSTS_EXEMPT))){
 				itemCost = numItems * itemCost;		
 		}
		//itemCost will always equal what it is set to in config at this point, even if there are no items to be sent. Need to fix this.
 		if(((Settings.EnableMailCosts == true && itemCost != 0 && (!this.Player.hasPermission(Permissions.COSTS_EXEMPT))) && (Settings.EnableSendMoney == true && amount > 1)) || ((Settings.EnableMailCosts == true) && (Settings.ItemCost != 0) && (!this.Player.hasPermission(Permissions.COSTS_EXEMPT))) || (Settings.EnableSendMoney == true && amount > 1)){
 			if((Settings.EnableMailCosts == true && itemCost != 0) && (Settings.EnableSendMoney == true && amount > 1)){
 				double totalcost = itemCost + amount;
 				if(PaperMailEconomy.hasMoney(totalcost, player)){
 					PaperMailEconomy.takeMoney(itemCost, player);
 					CraftStack = PaperMailEconomy.getBankNote(amount, player);
 					sendingContents.add(CraftStack);
 				}
 			}else if(PaperMailEconomy.hasMoney(itemCost, player) && (Settings.EnableMailCosts == true) && (itemCost != 0) && ((Settings.EnableSendMoney == false) || (amount < 2))){
 				PaperMailEconomy.takeMoney(itemCost, player);
 			}
 		}
 		if(Settings.EnableSendMoney && (this.Player.hasPermission(Permissions.COSTS_EXEMPT)) || (Settings.EnableMailCosts == false) || (itemCost == 0)){
 			CraftStack = PaperMailEconomy.getBankNote(amount, player);
 			sendingContents.add(CraftStack);
 		}
 		
 			Inbox inbox = Inbox.GetInbox(playerName);
 			inbox.AddItems(sendingContents, Player);
 		
 		if (paperSent) {
 			ItemStack itemInHand = Player.getInventory().getItemInHand();
 			itemInHand.setAmount(itemInHand.getAmount() - 1);
 			Player.setItemInHand(itemInHand);
 		}
 		Player.sendMessage(ChatColor.DARK_GREEN + "Message sent!" + ChatColor.RESET);
 	}
 	
 	public static PaperMailGUI GetGUIfromPlayer(Player player) {
 		for (PaperMailGUI gui : itemMailGUIs) {
 			if (gui.Player == player)
 				return gui;
 		}
 		return null;
 	}
 }
