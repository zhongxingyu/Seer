 package workers;
 
 import googleMapsDirections.Directions;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Trip;
 import models.TripOffer;
 import models.TripRequest;
 import models.User;
 import models.Waypoint;
 
 public class ModelFactory {
 
     private static ModelFactory instance;
     private ArrayList<Waypoint> cities;
     private final long THREE_HOURS = (long) (Math.random() * 60 * 60 * 3);
 
     private ModelFactory() {
 	cities = new ArrayList<Waypoint>();
 	loadCityDataFromCsv();
     }
 
     public static ModelFactory getInstance() {
 	if (instance == null) {
 	    instance = new ModelFactory();
 	}
 	return instance;
     }
 
     public TripOffer getRandomTripOffer() {
 	TripOffer offer = new TripOffer();
 	fillTripWithRandomData(offer);
 	return offer;
     }
 
     private void fillTripWithRandomData(Trip trip) {
 	Waypoint origin = getRandomCity();
 	Waypoint destination = getRandomCity();
 	Directions directions = new Directions();
 	directions.addWaypoint(origin);
 	directions.addWaypoint(destination);
 	long currentTime = System.currentTimeMillis() / 1000L;
 
 	trip.setOriginAddress(origin.getAddress());
 	trip.setDestinationAddress(destination.getAddress());
 	trip.setOriginLat(origin.getLatitude());
 	trip.setOriginLong(origin.getLongitude());
 	trip.setDestinationLat(destination.getLatitude());
 	trip.setDestinationLong(destination.getLongitude());
 	trip.setStartTimeMin(currentTime + (long) (Math.random() * 60 * 60 * 24 * 2));
 	trip.setStartTimeMax(trip.getStartTimeMin() + THREE_HOURS);
 	trip.setEndTimeMin(trip.getStartTimeMin() + directions.getApproximateTravelTimeInSeconds());
 	trip.setEndTimeMax(trip.getEndTimeMin() + THREE_HOURS);
 	trip.setNumberOfSeats((int) (Math.random() * 6) + 1);
 	
 	User u = getRandomUser();
 	u.save();
 	trip.setUser(u);
     }
 
     private Waypoint getRandomCity() {
 	return cities.get((int) (Math.random() * cities.size()));
     }
 
     public TripRequest getRandomTripRequest() {
 	TripRequest request = new TripRequest();
 	fillTripWithRandomData(request);
 	return request;
     }
 
     public User getRandomUser()
     {
 	List<User> users = User.find.all();
 	if(users.size() == 0)
 	{
 	    return generateNewRandomUser();
 	}
 	return users.get((int) (Math.random() * users.size()));
     }
     
     public User generateNewRandomUser() {
 	User u = new User();
 
 	u.setFirstName(getRandomFirstName());
 	u.setLastName(getRandomLastName());
 	u.setAddress("Heerestraat 1");
 	u.setPostalCode("9741ED");
 	u.setCity("Groningen");
 	u.setCountryCode("NL");
 	u.setEmail(u.getFirstName() + "." + u.getLastName() + "@itract.com");
 
 	u.setDateOfBirth("2012/09/02");
 
 	u.setGender("M");
 	u.setPassword(u.getFirstName().toLowerCase());
 	u.setProfilePicture("http://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50");
 	
 	return u;
     }
 
     private String getRandomFirstName() {
 	String[] firstNames = { "Peter", "Wim", "Kris", "Marthyn", "Erwin", "Jurre", "Marcel", "Ron", "Stefan", "Siemen", "Taede", "Bas", "Piet",
 		"Sander", "Stephan" };
 	return firstNames[(int) (Math.random() * firstNames.length)];
     }
 
     private String getRandomLastName() {
 	String[] lastNames = { "Faber", "Dijkman", "Buist", "Elzes", "Oldenkamp", "Stender", "Horlings", "Mulder", "Prins", "Boelkens", "Sibma",
 		"Boven", "Fransen", "Koerkmap", "Boomker" };
 	return lastNames[(int) (Math.random() * lastNames.length)];
     }
 
     public void loadCityDataFromCsv() {
 	BufferedReader reader;
 	try {
 	    reader = new BufferedReader(new FileReader("public/cities.csv"));
 
 	    String[] headerRow = reader.readLine().split(",");
 	    if (headerRow.length != 5) {
 		reader.close();
 		return;
 	    }
 
 	    if (!headerRow[0].equals("Woonplaats") || !headerRow[1].equals("Gemeente") || !headerRow[2].equals("Provincie")
 		    || !headerRow[3].equals("Latitude") || !headerRow[4].equals("Longitude")) {
 		reader.close();
 		return;
 	    }
 
 	    String line;
 
 	    while ((line = reader.readLine()) != null) {
 		String[] cityValues = line.split(",");
 		if (cityValues.length == 5) {
		    String address = String.format("%s, %s", cityValues[0], cityValues[2]);
		    double latitude = Double.parseDouble(cityValues[3]);
		    double longitude = Double.parseDouble(cityValues[4]);
 		    Waypoint loc = new Waypoint(longitude, latitude, address, 0, 0);
 		    cities.add(loc);
 		}
 	    }
 
 	    reader.close();
 	} catch (FileNotFoundException e) {
 	    e.printStackTrace();
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
     }
 }
