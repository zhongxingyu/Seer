 package fr.epsi.i4.bookmark;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Singleton;
 
 @Singleton
 public class BookmarkRepository {
 
 	private final Map<String, Bookmark> map = new LinkedHashMap<String, Bookmark>() {
 		private static final long serialVersionUID = -4842188442391736903L;
 
 		protected boolean removeEldestEntry(Map.Entry<String,Bookmark> eldest) {
 			return this.size() > 100;
 		};
 	};
	private String latestId;
 
 	public synchronized void add(String id, Bookmark bookmark) throws InvalidBookmarkException {
 		bookmark.validate();
 		map.put(id, bookmark);
		latestId = id;
 	}
 
 	public synchronized String getLatestId() {
		return latestId;
 	}
 
 	public synchronized Bookmark get(String id) {
 		return map.get(id);
 	}
 
 	public synchronized void delete(String id) {
 		map.remove(id);
 	}
 
 	public synchronized String[] getIds(int startIndex, int itemCount) {
 		String[] emptyArray = new String[0];
 		if (startIndex >= map.size()) {
 			return emptyArray;
 		}
 		String[] keys = map.keySet().toArray(emptyArray);
 		return Arrays.copyOfRange(keys, startIndex, Math.min(startIndex + itemCount, keys.length));
 	}
 
 	@PostConstruct
 	public void populate() {
 		List<Bookmark> bookmarks = new ArrayList<>();
 		bookmarks.add(new Bookmark("restcookbook", "quelques articles intéressants sur REST", "http://restcookbook.com/"));
 		bookmarks.add(new Bookmark("Richardson Maturity Model", "évaluer le niveau RESTful d'une API", "http://martinfowler.com/articles/richardsonMaturityModel.html"));
 		bookmarks.add(new Bookmark("freegeoip", "Un exemple de web service RESTful", "http://freegeoip.net/"));
 		bookmarks.add(new Bookmark("OpenBeer", "Un exemple de web service RESTful", "http://openbeerdatabase.com/"));
 		bookmarks.add(new Bookmark("API WoW", "Un exemple de web service RESTful", "http://blizzard.github.io/api-wow-docs/"));
 		bookmarks.add(new Bookmark("Annuaire Web services RESTful", "Pour trouvez des web services publics", "http://www.programmableweb.com/apis/directory"));
 		bookmarks.add(new Bookmark("Document Jersey", "La documentation du framework Jersey", "https://jersey.java.net/documentation/latest/"));
 		bookmarks.add(new Bookmark("How to get a cup of coffee", "Starbuck en RESTfule", "http://www.infoq.com/articles/webber-rest-workflow"));
 		bookmarks.add(new Bookmark("HTTP RFC", "Le document décrivant HTTP", "http://www.w3.org/Protocols/rfc2616/rfc2616.html"));
 		bookmarks.add(new Bookmark("Java EE 6", "Un article introductif sur Java EE 6", "http://blog.ippon.fr/2011/03/21/java-ee-6-ici-et-maintenant/"));
 		bookmarks.add(new Bookmark("Spring vs. Java EE 6", "Un article sur l'évolution du Spring Framework par rapport à Java EE", "http://blog.ippon.fr/2010/03/30/les-rendez-vous-manques-de-spring/"));
 		bookmarks.add(new Bookmark("TomEE", "Le site officiel de tomee", "http://tomee.apache.org"));
 		bookmarks.add(new Bookmark("TomEE and Maven", "Post sur l'utilisation de Maven avec TomEE", "http://werpublogs.blogspot.fr/2012/11/tomee-and-maven.html"));
 
 		try {
 			for (Bookmark bookmark : bookmarks) {
 				add(UUID.randomUUID().toString(), bookmark);
 			}
 		} catch (InvalidBookmarkException e) {
 		}
 	}
 
 }
