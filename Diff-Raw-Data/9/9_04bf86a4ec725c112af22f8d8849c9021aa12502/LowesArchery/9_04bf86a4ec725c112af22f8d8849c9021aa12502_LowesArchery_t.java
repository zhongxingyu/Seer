 package org.darkquest.gs.plugins.shops;
 
 import org.darkquest.gs.model.InvItem;
 import org.darkquest.gs.model.MenuHandler;
 import org.darkquest.gs.model.Npc;
 import org.darkquest.gs.model.Player;
 import org.darkquest.gs.plugins.ScriptablePlug;
 import org.darkquest.gs.plugins.ShopInterface;
 import org.darkquest.gs.plugins.listeners.action.TalkToNpcListener;
 import org.darkquest.gs.plugins.listeners.executive.TalkToNpcExecutiveListener;
 import org.darkquest.gs.world.Shop;
 
 public final class LowesArchery extends ScriptablePlug implements ShopInterface, TalkToNpcExecutiveListener, TalkToNpcListener {
 
	private final Shop shop = new Shop(false, 3000, 100, 50, new InvItem(11, 200), new InvItem(190, 150), new InvItem(189, 4), new InvItem(188, 2), new InvItem(60, 2));
 	
 	@Override
 	public void onTalkToNpc(Player p, final Npc n) {
 		if (n.getID() == 58) {
 			p.setBusy(true);
 			n.blockedBy(p);
 			
 			playMessages(p, n, false, "Welcome to Lowe's Archery Store", "Do you want to see my wares?");
 			
 			String[] options = new String[] { "Yes please", "No, I prefer to bash things close up" };
 			p.setMenuHandler(new MenuHandler(options) {
 				@Override
 				public void handleReply(int option, String reply) {
 					owner.setBusy(true);
 					playMessages(owner, n, true, reply);
 					
 					if (option == 0) {
 						owner.setAccessingShop(shop);
 						owner.getActionSender().showShop(shop);
 					}
 					owner.setBusy(false);
 					n.unblock();
 				}
 			});
 			p.getActionSender().sendMenu(options);
 			p.setBusy(false);
 		}
 	}
 
 	@Override
 	public boolean blockTalkToNpc(Player p, Npc n) {
 		return n.getID() == 58;
 	}
 
 	@Override
 	public Shop[] getShops() {
 		return new Shop[] { shop };
 	}
 
 	@Override
 	public boolean isMembers() {
 		return false;
 	}
 
 }
