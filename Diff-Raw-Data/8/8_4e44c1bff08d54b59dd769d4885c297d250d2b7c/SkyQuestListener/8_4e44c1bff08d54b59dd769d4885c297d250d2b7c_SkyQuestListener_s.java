 package net.skycraftmc.SkyQuest;
 
 import net.skycraftmc.SkyQuest.objective.ObjectiveType;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class SkyQuestListener implements Listener
 {
 	SkyQuestPlugin plugin;
 	QuestManager qm;
 	public SkyQuestListener(SkyQuestPlugin plugin)
 	{
 		this.plugin = plugin;
 		qm = plugin.getQuestManager();
 	}
 	@EventHandler
 	public void killObj(EntityDeathEvent ev)
 	{
 		LivingEntity ent = ev.getEntity();
 		if(ent.getKiller() == null)return;
 		Player player = ent.getKiller();
 		PlayerQuestLog log = qm.getQuestLog(player.getName());
 		for(Quest q:log.getAssigned())
 		{
 			QuestData qd = log.getProgress(q);
 			for(Objective o:q.getObjectives())
 			{
 				if(o.getType() != ObjectiveType.KILL)continue;
 				if(!qd.isAssigned(o.getID()))continue;
 				String prog = qd.getProgress(o.getID());
 				String targ = o.getTarget();
 				if(!ObjectiveType.KILL.isSimilarType(targ, prog))continue;
 				if(!ObjectiveType.KILL.getEntityType(targ).equals(ent.getType().name()))continue;
 				int p = ObjectiveType.KILL.getKills(prog) + 1;
 				int t = ObjectiveType.KILL.getKills(targ);
 				player.sendMessage(ChatColor.GREEN + o.getName() + " (" + p + "/" + t + ")");
 				qd.setProgress(o.getID(), p + " " + ent.getType().name());
 			}
 		}
 	}
 	@EventHandler
 	public void move(PlayerMoveEvent event)
 	{
 		Player player = event.getPlayer();
 		PlayerQuestLog log = qm.getQuestLog(player.getName());
 		for(Quest q:log.getAssigned())
 		{
 			QuestData qd = log.getProgress(q);
 			for(Objective o:q.getObjectives())
 			{
 				if(o.getType() != ObjectiveType.TRAVEL)continue;
 				if(!qd.isAssigned(o.getID()))continue;
 				String targ = o.getTarget();
 				Location loc = player.getLocation();
 				qd.setProgress(o.getID(), ObjectiveType.TRAVEL.getRadius(targ) + " " +
 					ObjectiveType.TRAVEL.getType(targ) + " " + loc.getX() + " " + loc.getZ() + " "
 					+ loc.getWorld().getName());
 			}
 		}
 	}
 	@EventHandler
 	public void join(PlayerJoinEvent event)
 	{
 		String name = event.getPlayer().getName();
 		if(qm.getQuestLog(name) == null)
 		{
 			PlayerQuestLog log = new PlayerQuestLog(name);
 			qm.addQuestLog(log);
 			
 			//Testing
 			event.getPlayer().getInventory().addItem(plugin.createQuestLogItem());
 			//End Testing
 			for(Quest q:qm.getQuests())
 			{
 				if(q.isFirstAssigned())log.assign(q);
 			}
 		}
 	}
	private final String bookTitle = plugin.bookTitle;
	private final String bookAuthor = plugin.bookAuthor;
 	@EventHandler
 	public void interact(PlayerInteractEvent event)
 	{
 		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)return;
 		ItemStack hand = event.getPlayer().getItemInHand();
 		if(hand == null)return;
 		if(hand.getType() != Material.WRITTEN_BOOK)return;
 		if(!hand.hasItemMeta())return;
 		ItemMeta im = hand.getItemMeta();
 		if(!(im instanceof BookMeta))return;
 		BookMeta bm = (BookMeta)im;
 		if(!bm.hasAuthor() || !bm.hasTitle())return;
 		if(!bm.getTitle().equals(bookTitle))return;
 		if(!bm.getAuthor().equals(bookAuthor))return;
 		event.setUseItemInHand(Result.DENY);
 		plugin.getQuestLogManager().openQuestLog(event.getPlayer());
 	}
 	
 }
