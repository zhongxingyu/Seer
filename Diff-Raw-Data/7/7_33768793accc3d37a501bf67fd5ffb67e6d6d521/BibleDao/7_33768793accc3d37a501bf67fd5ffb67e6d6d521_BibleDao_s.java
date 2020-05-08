 package net.todd.bible.scripturelookup.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 public class BibleDao implements IBibleDao {
 	private final PersistenceManagerFactory persistenceManagerFactory;
 
 	public BibleDao(PersistenceManagerFactory persistenceManagerFactory) {
 		this.persistenceManagerFactory = persistenceManagerFactory;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Verse> getAllVerses() {
 		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
 		List<Verse> results = new ArrayList<Verse>();
 		try {
 			Query query = persistenceManager.newQuery(Verse.class);
 			results.addAll((List<Verse>) query.execute());
 			for (Verse verse : results) {
 				verse.getText();
 			}
 		} finally {
 			persistenceManager.close();
 		}
 		return results;
 	}
 
 	public void loadData(InputStream inputStream) {
 		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
 		try {
 			String line;
 			while ((line = reader.readLine()) != null) {
 				Verse verse = parse(line);
 				System.out.println("adding verse: " + verse.toString());
 				persistenceManager.makePersistent(verse);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			persistenceManager.close();
 			try {
 				inputStream.close();
 				reader.close();
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void deleteData() {
 		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
 		try {
 			Query query = persistenceManager.newQuery(Verse.class);
 			List<Verse> results = (List<Verse>) query.execute();
 			for (Verse verse : results) {
 				persistenceManager.deletePersistent(verse);
 			}
 		} finally {
 			persistenceManager.close();
 		}
 	}
 
 	private static Verse parse(String line) {
 		String[] splitLine = line.split("\\|");
 
 		String book = splitLine[0];
 		String chapter = splitLine[1];
 		String verse = splitLine[2];
 		String text = splitLine[3];
 
 		return new Verse(book, chapter, verse, text);
 	}
 
 	@Override
 	public Verse getVerse(String id) {
 		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
 		try {
			return persistenceManager.getObjectById(Verse.class, id);
 		} finally {
 			persistenceManager.close();
 		}
 	}
 }
