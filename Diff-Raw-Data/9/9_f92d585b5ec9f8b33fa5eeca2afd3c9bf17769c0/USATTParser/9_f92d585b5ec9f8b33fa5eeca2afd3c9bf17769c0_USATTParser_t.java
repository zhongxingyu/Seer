 package wei.mark.tabletennisratingsserver.util;
 
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import javax.persistence.EntityManager;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import wei.mark.tabletennisratingsserver.model.PlayerModel;
 import wei.mark.tabletennisratingsserver.model.PlayerModelCache;
 
 public class USATTParser implements ProviderParser {
 	private static USATTParser mParser;
 	private static final String provider = "usatt";
 
 	private USATTParser() {
 	}
 
 	public static synchronized USATTParser getParser() {
 		if (mParser == null)
 			mParser = new USATTParser();
 		return mParser;
 	}
 
 	@Override
 	public ArrayList<PlayerModel> playerNameSearch(String query, boolean fresh) {
 		ArrayList<PlayerModel> players;
 		String firstName = getFirstName(query);
 		String lastName = getLastName(query);
 
 		EntityManager em = EMF.get().createEntityManager();
 		try {
 			if (!fresh) {
 				// first check cache
 				Object cache = em.find(PlayerModelCache.class,
 						PlayerModelCache.calculateKey(provider, query));
 				if (cache != null) {
 					ArrayList<PlayerModel> cachedPlayers = ((PlayerModelCache) cache)
 							.getPlayers();
 					return cachedPlayers;
 				}
 			}
 
 			URL url = new URL(
 					"http://www.usatt.org/history/rating/history/Allplayers.asp?NSearch="
 							+ URLEncoder.encode(lastName, "UTF-8"));
 
 			Document doc = Jsoup.connect(url.toString()).get();
 
 			Elements rows = doc.select("tr");
 			players = new ArrayList<PlayerModel>();
 
 			// 0th child is headers
 			for (int i = 1; i < rows.size(); i++) {
 				Elements row = rows.get(i).children();
 
 				String playerName = row.get(2).text().trim();
 				// match last name && first name
 				if (lastName.equalsIgnoreCase(getLastName(playerName))
 						&& (firstName.equals("") || firstName
 								.equalsIgnoreCase(getFirstName(playerName)))) {
 					PlayerModel player = new PlayerModel();
 
 					player.setProvider(provider);
 					player.setId(row.get(0).text().trim());
 					player.setExpires(row.get(1).text().trim());
 					player.setName(playerName);
 					player.setRating(row.get(3).text().trim());
 					player.setState(row.get(4).text().trim());
 					player.setLastPlayed(row.get(5).text().trim());
 					players.add(player);
 				}
 			}
 
 			PlayerModelCache cache = new PlayerModelCache(provider, lastName,
 					players);
 
 			Object oldCache = em.find(PlayerModelCache.class,
 					PlayerModelCache.calculateKey(provider, query));
 			if (oldCache != null) {
 				ArrayList<PlayerModel> cachedPlayers = ((PlayerModelCache) oldCache)
 						.getPlayers();
 				for (PlayerModel playerModel : cachedPlayers) {
 					em.remove(playerModel);
 				}
 				em.remove(oldCache);
 			}
 
 			em.persist(cache);
 
 			return players;
 		} catch (Exception ex) {
 			Object cache = em.find(PlayerModelCache.class,
 					PlayerModelCache.calculateKey(provider, query));
 			if (cache != null) {
 				ArrayList<PlayerModel> cachedPlayers = ((PlayerModelCache) cache)
 						.getPlayers();
 				return cachedPlayers;
 			} else {
 				return null;
 			}
 		} finally {
 			em.close();
 		}
 	}
 
 	private String getFirstName(String fullName) {
 		int commaIndex = fullName.indexOf(",");
 		if (commaIndex != -1)
			return fullName.substring(commaIndex + 1).trim();
 		else
 			return "";
 	}
 
 	private String getLastName(String fullName) {
 		int commaIndex = fullName.indexOf(",");
 		if (commaIndex != -1)
 			return fullName.substring(0, commaIndex).trim();
 		else
 			return fullName;
 	}
 }
