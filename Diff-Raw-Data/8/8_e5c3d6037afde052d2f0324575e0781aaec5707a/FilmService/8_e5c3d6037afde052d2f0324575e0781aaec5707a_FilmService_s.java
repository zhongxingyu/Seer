 package agileexplained;
 
import java.sql.SQLException;

 public class FilmService {
 	private FilmDb db;
 
 	public FilmService() {
 		setupDb();
 	}
 	
 	private void setupDb() {
 		try {
 			db = new FilmDb();
 			db.connect(); 
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public String[] getFilmData(String title) {
 		try {
 			Film film = db.retrieveFilm(title);
 			return FilmDataReport.getStrings(film);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new String[0];
 		}
 	}
 }
