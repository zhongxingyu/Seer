 package wei.mark.tabletennisratingsserver.util;
 
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import javax.persistence.EntityManager;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import wei.mark.tabletennisratingsserver.model.PlayerModel;
 import wei.mark.tabletennisratingsserver.model.PlayerModelCache;
 
 public class RatingsCentralParser implements ProviderParser {
 	private static RatingsCentralParser mParser;
 	private static final String provider = "rc";
 
 	private RatingsCentralParser() {
 	}
 
 	public static synchronized RatingsCentralParser getParser() {
 		if (mParser == null)
 			mParser = new RatingsCentralParser();
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
 				if (cache != null)
 					return ((PlayerModelCache) cache).getPlayers();
 			}
 
 			URL url = new URL(
 					"http://www.ratingscentral.com/PlayerList.php?SortOrder=Name&PlayerName="
 							+ URLEncoder.encode(query, "UTF-8"));
 
 			Document doc = Jsoup.connect(url.toString()).get();
 
 			Elements rows = doc.select("td[class=ContentSection] tbody > tr");
 			players = new ArrayList<PlayerModel>();
 
 			for (int i = 0; i < rows.size(); i++) {
 				Elements row = rows.get(i).children();
 
 				String playerName = row.get(1).text().trim();
 				// match last name && first name
 				if (lastName.equalsIgnoreCase(getLastName(playerName))
 						&& (firstName.equals("") || firstName
 								.equalsIgnoreCase(getFirstName(playerName)))) {
 					PlayerModel player = new PlayerModel();
 
 					player.setProvider(provider);
 					player.setRating(row.get(0).text().trim());
 					player.setName(playerName);
 					player.setId(row.get(2).text().trim());
 					Elements clubElements = row.get(3).children();
 					ArrayList<String> clubs = new ArrayList<String>();
 					for (Element clubElement : clubElements) {
 						String club = clubElement.text().trim();
 						if (club != null && !club.equals(""))
 							clubs.add(club);
 					}
 					player.setClubs(clubs.toArray(new String[0]));
 					player.setState(row.get(4).text().trim());
 					player.setCountry(row.get(5).text().trim());
 					player.setLastPlayed(row.get(6).text().trim());
 					players.add(player);
 				}
 			}
 
 			PlayerModelCache cache = new PlayerModelCache(provider, query,
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
 			Object cache = em.find(
 					PlayerModelCache.class,
 					PlayerModelCache.calculateKey(provider,
 							PlayerModelCache.calculateKey(provider, query)));
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
