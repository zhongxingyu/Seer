 package ru.ufatos.chat;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.configuration.file.FileConfiguration;
 
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 
 public class Chat implements Listener {
 
 	private double RangeMain;
 	private double RangeWhispering;
 	private double RangeShout;
 	private double RangeAction;
 	private boolean DeathMessage;
 	private boolean itemToGlobal;
 	private double ConfigChance;
 	private int globalId; // Id вещи для вещания глобально
 	private int globalSubId;// метадата вещи для вещания глобально
 	private Random rand = new Random();
 	private String luck = ChatColor.GREEN + "(удачно)" + ChatColor.LIGHT_PURPLE;
 	private String unluck = ChatColor.RED + "(не удачно)" + ChatColor.LIGHT_PURPLE;
 	private int randrolldef = 6;
 	private int defchanse = 50; // шанс
 	private int minroll = 5;
 	protected static Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-F])");
 	protected static Pattern chatMagicPattern = Pattern.compile("(?i)&([K])");
 	protected static Pattern chatBoldPattern = Pattern.compile("(?i)&([L])");
 	protected static Pattern chatStrikethroughPattern = Pattern.compile("(?i)&([M])");
 	protected static Pattern chatUnderlinePattern = Pattern.compile("(?i)&([N])");
 	protected static Pattern chatItalicPattern = Pattern.compile("(?i)&([O])");
 	protected static Pattern chatResetPattern = Pattern.compile("(?i)&([R])");
 	protected static Pattern spaceF = Pattern.compile("^\\s+");
 	protected static Pattern nick = Pattern.compile("[%2](\\D[\\d\\w_]+)\\s(.+)");// Извлекаем ник
 	protected static Pattern _number = Pattern.compile("^\\*{3}(\\d+)$");// число?
 
 	public Chat(FileConfiguration config) {
 		this.RangeMain = config.getDouble("Range.main", this.RangeMain);
 		this.RangeWhispering = config.getDouble("Range.whispering", this.RangeWhispering);
 		this.RangeShout = config.getDouble("Range.shout", this.RangeShout);
 		this.RangeAction = config.getDouble("Range.action", this.RangeAction);
 		this.DeathMessage = config.getBoolean("disableDeathMessage", this.DeathMessage);
 		this.ConfigChance = config.getDouble("ConfigChance", this.ConfigChance);
 		this.globalId = config.getInt("Id.globalId", this.globalId);// вещь для вечания в глобальный чат
 		this.globalSubId = config.getInt("Id.globalSubId", this.globalSubId);// метадата
 		this.itemToGlobal = config.getBoolean("itemToGlobal", this.itemToGlobal);
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		updateDisplayName(event.getPlayer());
 		event.setJoinMessage(null);
 	}
 
 	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		Player player = event.getPlayer();
 		String message = "%1$s: %2$s";
 		String chatMessage = event.getMessage();
 		boolean ranged = true; // я не знаю как это назвать, поэтому ranged (органичен ли чат)
 		double range = RangeMain;
 		//TODO: FIX IT
 		if (chatMessage.startsWith("#") && chatMessage.length() > 1) {
 			if (player.hasPermission("rpchat.global")) {
 				if (itemToGlobal) {
 					ItemStack inHand = player.getItemInHand();
 					if (player.hasPermission("rpchat.global")) {
 						message = "%1$s [GlobalChat]: " + ChatColor.YELLOW + "%2$s";
 						range = RangeWhispering;
 						if (player.hasPermission("rpchat.no_item")) {
 							ranged = false; // отключить ограничения для чата
 						} else if (inHand != null && inHand.getTypeId() == globalId && inHand.getDurability() == globalSubId) {
 							ranged = false; // флаг глобально, межмирового чата
 							int AmoutHand = inHand.getAmount() - 1;
 							ItemStack inHandrem = new ItemStack(inHand.getTypeId(), inHand.getAmount() - 1);
 							if (AmoutHand <= 0) {
 								inHandrem = null;
 							}
 							player.setItemInHand(inHandrem);
 						} else {
 							player.sendMessage(ChatColor.GRAY + "У вас нет нужного предмета");
 							event.setCancelled(true);
 						}
 					} else {
 						ranged = false;
 						message = "%1$s [GlobalChat]: %2$s";
 						chatMessage = ChatColor.GOLD + delSpace(chatMessage.substring(1));
 					}
 				}
 			} else {
 				player.sendMessage("У вас нет прав писать в глобальный чат");
 				event.setCancelled(true);
 			}
 		}
 
 
 		if (chatMessage.startsWith("!") && chatMessage.length() > 1) {
 			range = RangeShout;
 			message = "%1$s кричит: %2$s";
 			chatMessage = ChatColor.RED + delSpace(chatMessage.substring(1));
 		}
 
 		if (chatMessage.startsWith("@") && chatMessage.length() > 1) {
 			range = RangeWhispering;
 			message = "%1$s шепчет: %2$s";
 			chatMessage = ChatColor.ITALIC + delSpace(chatMessage.substring(1));
 			chatMessage = ChatColor.GRAY + chatMessage;
 		}
 
 		// Действие с вероятностью
 		if (chatMessage.startsWith("***") && chatMessage.length() > 3) {//если сообщение начинается с *** и длинее 3
 			range = RangeAction;// дальность локального чата
 			Matcher m = _number.matcher(chatMessage);//фильтруем сообщение
 			int i;//иницилизируем локальную переменную
 			if (m.find()) {//если фильтр прошёл
 				i = Integer.parseInt(m.group(1));//смотрим сколько подано в рандрол
 				i = i > minroll ? i : randrolldef;//если слишком мало, то даём значение по умолчанию
 				int j = rand.nextInt(i) + 1;//генерируем рандомролл
 				message = ChatColor.LIGHT_PURPLE + "**" + player.getDisplayName() + ChatColor.LIGHT_PURPLE + " выбрасывает " + j + " из " + i + "**";//выводибм бросок кубика
 			} else {//если фильтр не прошёл, значит действие
 				chatMessage = ChatColor.LIGHT_PURPLE + (chatMessage.substring(3));//всё что дальше 3 символа - действие
 				int chance = rand.nextInt(100);//генерируем шанс
 				message = ChatColor.LIGHT_PURPLE + "** " + player.getDisplayName() + " " + chatMessage + ((chance > defchanse) ? luck : unluck) + " **";//выводим действие
 			}
 		}
 
 		if (chatMessage.startsWith("**") && chatMessage.length() > 2) {
 			range = RangeAction;
 			message = ChatColor.LIGHT_PURPLE + "**%1$s %2$s**";
 			chatMessage = delSpace(chatMessage.substring(2));
 		}
 
 		if ((chatMessage.startsWith("%") || chatMessage.startsWith("2")) && chatMessage.length() > 1) {//если сообщение начинается с % или 2 и длинее 1 
 
 			Matcher m = nick.matcher(chatMessage);//ищим ник
 			if (m.find()) {//если нашли
 				range = -1;// флаг особого чата
 				if (Bukkit.getServer().getPlayer(m.group(1)) != null) {//то проверяем, есть ли игрок в сети
 					Player recipient = Bukkit.getServer().getPlayer(m.group(1));//если есть, то присваеваем его переменной
 					if (!recipient.equals(player))//проверяем не пишет ли игрок сам себе (да, такое бывает)
 						recipient.sendMessage(ChatColor.GRAY + "pm:" + player.getDisplayName() + ": "
 								+ ChatColor.DARK_PURPLE + m.group(2));//оформляем текст сообщения и отправляем его цели, еси цель не отправитель
 					player.sendMessage(ChatColor.GRAY + "pm:" + player.getDisplayName() + "->"
 							+ recipient.getDisplayName() + ": " + ChatColor.DARK_PURPLE + m.group(2));//дублируем отправителю сообщение, даже если цель сам он
 					//** этого кода нет в данной версии
 					//	if (!main.hasPermission(player, "rpchat.nospy"))//проверяем, есть права на защиту от прослушки
 					//		getPMRecipientsSpy(player, recipient, m.group(2));// если нет, формируем слушателей
 					//		**//
 					// event.setCancelled(true);// глушим вывод сообщения, забыл, глушим ниже. Убрать
 					Bukkit.getConsoleSender().sendMessage(
 							ChatColor.GRAY + "spy:" + player.getDisplayName() + "->" + recipient.getDisplayName()
 									+ ": " + m.group(2));//выводим ЛС в консоль
 				} else {
 					player.sendMessage("Игрока " + m.group(1) + " нет в сети");//если игрока нет в сети 
 				}
 				event.setCancelled(true);
 			}
 		}
 
 		if (ranged && range > 0) {
 			event.getRecipients().clear();
 			event.getRecipients().addAll(this.getLocalRecipients(player, message, range));
 		}
 		event.setFormat(message);
 		event.setMessage(chatMessage);
 	}
 
 	/*убираем сообщение о смерти*/
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityDeath(EntityDeathEvent event) {
 		if (DeathMessage) {
 			if (event instanceof PlayerDeathEvent) {
 				PlayerDeathEvent deathEvent = (PlayerDeathEvent) event;
 				deathEvent.setDeathMessage(null);
 			}
 		}
 	}
 
 	//Тут немного взял у ChatManager
 	protected List<Player> getLocalRecipients(Player sender, String message, double range) {
 		Location playerLocation = sender.getLocation();
 		List<Player> recipients = new LinkedList<Player>();
 		double squaredDistance = Math.pow(range, 2);
 		for (Player recipient : Bukkit.getServer().getOnlinePlayers()) {
 			if (recipient.getWorld().equals(sender.getWorld()) && playerLocation.distanceSquared(recipient.getLocation()) < squaredDistance) {
 				recipients.add(recipient);
 			} else if (recipient.hasPermission("rpchat.spy")) {
 				recipients.add(recipient);
 			} else {
 				continue;
 			}
 		}
 		return recipients;
 	}
 
 	protected static String translateColorCodes(String string) {
 		if (string == null) {
 			return "";
 		}
 
 		String newstring = string;
 		newstring = chatColorPattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatMagicPattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatBoldPattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatStrikethroughPattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatUnderlinePattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatItalicPattern.matcher(newstring).replaceAll("\u00A7$1");
 		newstring = chatResetPattern.matcher(newstring).replaceAll("\u00A7$1");
 		return newstring;
 	}
 
 	static void updateDisplayNames() {
 		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 			updateDisplayName(player);
 		}
 	}
 
 	public String delSpace(String chatMessage) {
 		Matcher space = spaceF.matcher(chatMessage);
 		chatMessage = space.replaceFirst("");
 		return chatMessage;
 	}
 
 	static void updateDisplayName(Player player) {
 		PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
 		if (user == null) {
 			return;
 		}
 		player.setDisplayName(Chat.translateColorCodes(user.getPrefix() + player.getName()));
 	}
 }
