 
 package axirassa.tools;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.hibernate.Session;
 
 import axirassa.dao.PingerDAO;
 import axirassa.dao.UserDAO;
 import axirassa.ioc.IocStandalone;
 import axirassa.model.PingerEntity;
import axirassa.model.PingerFrequency;
 import axirassa.model.UserEntity;
 
 public class CreatePingersFromFile {
 
 	@Inject
 	private Session database;
 
 	@Inject
 	private PingerDAO pingerDAO;
 
 	@Inject
 	private UserDAO userDAO;
 
 	private String filename;
 
 	private String email;
 
 
 	private void insert () throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
 		System.out.println("User's primary e-mail: ");
 		email = br.readLine().trim();
 
 		UserEntity user = userDAO.getUserByEmail(email);
 
 		if (user == null) {
 			System.err.println("No user with primary e-mail: " + email);
 			return;
 		}
 
 		System.out.println("CSV containing addresses: ");
 		filename = br.readLine().trim();
 
 		System.out.println("Creating pingers for user w/ ID: " + user.getId());
 
 		BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
 		String line = null;
 		while ((line = fileReader.readLine()) != null) {
 			if (line.contains(","))
 				line = line.split(",", 2)[1].trim();
 
 			line = "http://www." + line;
 			System.out.println("\t" + line);
 			PingerEntity pinger = new PingerEntity();
 			pinger.setUrl(line);
 			pinger.setUser(user);
			pinger.setFrequency(PingerFrequency.MINUTE);
 
 			database.persist(pinger);
 		}
 
 		System.out.println("Commiting transaction");
 		database.getTransaction().commit();
 		System.out.println("Done.");
 	}
 
 
 	public static void main (String[] args) throws IOException {
 		CreatePingersFromFile tool = IocStandalone.autobuild(CreatePingersFromFile.class);
 		tool.insert();
 	}
 }
