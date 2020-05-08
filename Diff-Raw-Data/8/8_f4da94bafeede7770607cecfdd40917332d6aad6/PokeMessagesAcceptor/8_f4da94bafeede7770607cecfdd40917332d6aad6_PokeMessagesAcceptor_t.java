 package il.technion.ewolf.server;
 
 import il.technion.ewolf.ewolf.WolfPack;
 import il.technion.ewolf.msg.PokeMessage;
 import il.technion.ewolf.msg.SocialMessage;
 import il.technion.ewolf.server.cache.ICache;
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.socialfs.exception.ProfileNotFoundException;
 import il.technion.ewolf.stash.exception.GroupNotFoundException;
 
 import java.util.List;
 import java.util.Map;
 
 import com.google.inject.Inject;
 
 public class PokeMessagesAcceptor implements Runnable {
 	private final ICache<List<SocialMessage>> inboxCache;
 	private final ICache<Map<String,List<Profile>>> wolfpacksMembersCache;
 	private final ICache<Map<String, WolfPack>> wolfpacksCache;
 
 	private static final String INVITERS_WOLFPACK = "inviters";
 
 	@Inject
 	public PokeMessagesAcceptor(ICache<List<SocialMessage>> inboxCache,
 			ICache<Map<String,List<Profile>>> wolfpacksMembersCache,
 			ICache<Map<String, WolfPack>> wolfpacksCache) {
 		this.inboxCache = inboxCache;
 		this.wolfpacksMembersCache = wolfpacksMembersCache;
 		this.wolfpacksCache = wolfpacksCache;
 	}
 
 	@Override
 	public void run() {
 		try {
 			Map<String, WolfPack> wolfpacksMap = wolfpacksCache.get();
 			WolfPack inviters = wolfpacksMap.get(INVITERS_WOLFPACK);
 			while (true) {
 				List<SocialMessage> messages = inboxCache.get();
 				Map<String,List<Profile>> wolfpacksMembersMap = wolfpacksMembersCache.get();
 				wolfpacksMap = wolfpacksCache.get();
 				for (SocialMessage m : messages) {
 					if (m.getClass() == PokeMessage.class) {
 
 						List<Profile> invitersList = wolfpacksMembersMap.get(INVITERS_WOLFPACK);
 
 						try {
 							Profile inviter = m.getSender();
 							if (invitersList != null && !invitersList.contains(inviter)) {
								//FIXME adding to inviters sends Poke message too
 								inviters.addMember(inviter);
								wolfpacksMembersCache.update();
								wolfpacksMembersMap = wolfpacksMembersCache.get();
 							}
 						} catch (GroupNotFoundException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (ProfileNotFoundException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						((PokeMessage)m).accept();
 					}
 				}
 				Thread.sleep(2000);
 			}
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			//TODO what can I do here?
 		}
 
 	}
 
 }
