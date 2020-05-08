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
 
 public final class AuburysRunes extends ScriptablePlug implements ShopInterface, TalkToNpcExecutiveListener, TalkToNpcListener {
 
	private final Shop shop = new Shop(false, 30000, 100, 50, new InvItem(31, 50), new InvItem(32, 50), new InvItem(33, 50), new InvItem(34, 50), new InvItem(35, 50), new InvItem(36, 50));
 	
 	@Override
 	public void onTalkToNpc(Player p, final Npc n) {
 		if (n.getID() == 54) {
 			p.setBusy(true);
 			n.blockedBy(p);
 			
 			playMessages(p, n, false, "Do you want to buy some runes?");
 			
 			String[] options = new String[] { "Yes please", "Oh it's a rune shop. No thank you, then" };
 			p.setMenuHandler(new MenuHandler(options) {
 				@Override
 				public void handleReply(int option, String reply) {
 					owner.setBusy(true);
 					playMessages(owner, n, true, reply);
 					
 					switch (option) {
 					case 0:
 						owner.setAccessingShop(shop);
 						owner.getActionSender().showShop(shop);
 						break;
 					case 1:
 						playMessages(owner, n, false, "Well if you do find someone who does want runes,", "send them my way");
 						break;
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
 		return n.getID() == 54;
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
