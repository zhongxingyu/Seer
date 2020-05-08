 /*******************************************************************************
  * Copyleft 2013 Massimiliano Leone - massimiliano.leone@iubris.net .
  * 
  * SocratesActivityTest.java is part of 'Socrates'.
  * 
  * 'Socrates' is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * 'Socrates' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with 'Socrates'; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
  ******************************************************************************/
 package socrates.test.activity;
 
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import net.iubris.socrates.engines.details.DetailsRetriever;
 import net.iubris.socrates.engines.details.exception.DetailsRetrieverException;
 import net.iubris.socrates.engines.search.Searcher;
 import net.iubris.socrates.engines.search.exception.PlacesSearcherException;
 import net.iubris.socrates.model.http.response.data.details.Details;
 import net.iubris.socrates.model.http.response.data.details.OpeningHours;
 import net.iubris.socrates.model.http.response.data.details.Period;
 import net.iubris.socrates.model.http.response.data.details.review.AspectRating;
 import net.iubris.socrates.model.http.response.data.details.review.Review;
 import net.iubris.socrates.model.http.response.data.events.Event;
 import net.iubris.socrates.model.http.response.data.search.Place;
 import net.iubris.socrates.model.http.response.details.DetailsResponse;
 import net.iubris.socrates.model.http.response.search.SearchResponse;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import socrates.test.runner.InjectedTestRunner;
 import android.location.Location;
 
 
 //@RunWith(RobolectricTestRunner.class)
 @RunWith(InjectedTestRunner.class)
 public class SocratesActivityTest {
 
 	@Inject
 	private Searcher placeSearcher;
 	@Inject
 	private DetailsRetriever placeDetailsRetriever;
 	
 //	@Inject @Config ConfigOptional configOptional;
 	
 //	private SearchRequestUrlBuilder placesSearchRequestUrlBuilder;
 	//private DetailsRequestUrlBuilder placeDetailstRequestUrlBuilder;
 	
 	private Location location;
 	
 	private SearchResponse searchResponse;
 
 	private int placeNumber =1;
 	
 	public SocratesActivityTest() {
 		location = new Location("GPS");
 		location.setLatitude(44.4937382342);
 		location.setLongitude(11.3769477681);
 	}
 		
 	@Before
 	public void searchLocations() {
 		try {
 //			placeSearcher.initFromConfig(configOptional);
 //			System.out.println( "RequestUrl: "+placeSearcher.getRequestUrl() );
 			searchResponse = placeSearcher.search(location);
 			System.out.println( "RequestUrl: "+placeSearcher.getRequestUrl() );
 			System.out.println( "status reason:" +searchResponse.getStatus().getReason());			
 		} catch (PlacesSearcherException e) {
 			e.printStackTrace();
 		}/* catch (LocationNullException e) {
 			e.printStackTrace();
 		}*/
 	}
 	
 	
 	@Test
 	public void findLocationsWithDetails() {
 		System.out.println("\n");
 		printCalledMethod("");
 		try {
 			printResultsWithDetail(searchResponse.getResults(),placeNumber);
 		} catch (DetailsRetrieverException e) {
 			e.printStackTrace();
 		}
 		//System.out.println("-- findLocationsDetails --\n");
 		printCalledMethod("\n");
 	}
 	
 	private void printCalledMethod(String newLine){
 		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();		
 		System.out.println("--"+stackTrace [ 2 ].getMethodName()+" --"+newLine);		
 	}
 	
 	
 	private void printResults(List<Place> places,int i) {
 		for (Place p: places) {
 			System.out.print(i+" ");
 			System.out.print(p.getName()+" ");
 			//System.out.println(p.getGeometry().getGeoPoint());
 			net.iubris.socrates.model.http.response.data.geocoding.Location loc = p.getGeometry().getLocation();
 			System.out.println(loc.getLatitude()+","+loc.getLongitude());
 			i++;
 		}
 	}
 	
 	
 	
 	private void printResultsWithDetail(List<Place> places,int i) throws DetailsRetrieverException {
 		for (Place p: places) {
 			System.out.print(i+" ");
 			System.out.print(p.getName()+" ");			
 			//System.out.println(p.getGeometry().getGeoPoint());
 			System.out.print(p.getGeometry().getLocation().getLatitude()+" "+p.getGeometry().getLocation().getLongitude()+" ");
 			
 			String reference = p.getReference();
 			//System.out.println(reference);
 			DetailsResponse placeDetailsResponse = placeDetailsRetriever.retrieve(reference);
 			
 			Details result = placeDetailsResponse.getResult();
 			
 			System.out.println( result.getUri()+" "+result.getFormattedAddress() +" "+result.getWebsite()+" ");
 			printOpening( result.getOpeningHours() );
 			printEvents( result.getEvents() );			
 			printReviews(result.getReviews());
 			
 			System.out.println("");
 			
 			i++;
 		}
 	}
 	
 	private void printOpening(OpeningHours openingHours) {
 		if (openingHours == null) return;
 		System.out.println("Opening:");
 		for (Period p: openingHours.getPeriods()) {
 			try {
 			System.out.println( "\t"+p.getOpen().getDay() + ": " +p.getOpen().getTime() + " - "+p.getClose().getTime() );
 			} catch (NullPointerException e) {}
 		}		
 	}
 
 	private void printEvents(List<Event> events){
 		if (events==null) return;
 		if (events.isEmpty()) return;
 		System.out.println("Events:");
 		Iterator<Event> iterator = events.iterator();
 		while(iterator.hasNext()) {
 			Event event = iterator.next();
 			System.out.println( "\t"+event.getSummary()+" "+event.getStartTime() );
 		}
 	}
 	
 	private void printReviews(List<Review> reviews){
 		if (reviews==null) return;
 		if (reviews.isEmpty()) return;
 		System.out.println("Reviews:");
 		Iterator<Review> iterator = reviews.iterator();
 		while(iterator.hasNext()) {
 			Review review = iterator.next();
 			System.out.println( "\t"+review.getAuthorName()+": "+review.getText() );
 			List<AspectRating> aspects = review.getAspects();
 			
 			if(aspects==null) break;
 			//System.out.println(aspects.size());
 			Iterator<AspectRating> iterator2 = aspects.iterator();
 			while (iterator2.hasNext() ){
 				AspectRating aspectRating = iterator2.next();
 				System.out.println( "\t\t"+aspectRating.getType() + " " +aspectRating.getRating() );
 			}
 		}
 	}
 	
 	@Test
 	public void findNext() throws InterruptedException {
 		System.out.println("\n");
 		printCalledMethod("");
 		System.out.println( "searchResponse: "+searchResponse.toString() );
 		String nextPageToken = searchResponse.getNextPageToken();
 		if (nextPageToken!=null) {
 			Thread.sleep(10*1000);
 			//System.out.println("token not null");
 			SearchResponse nextSearchResponse =null;
 			try {
 				nextSearchResponse = placeSearcher.search(nextPageToken);
 				System.out.println( "nextSearchResponse.getStatus(): "+ nextSearchResponse.getStatus() );
 			} catch (PlacesSearcherException e) {
 				e.printStackTrace();
 			}
 			printResults(nextSearchResponse.getResults(),placeNumber);
 		}
 		printCalledMethod("\n");
 	}
 	
 	
 	
 	//@Test
 	public void retrieveDetails() {
 		System.out.println("-- findLocationsDetails --");			
 		try {
 			SearchResponse  placeList = placeSearcher.search(location);
 			for (Place place: placeList.getResults()) {
 				String reference = place.getReference();
 				DetailsResponse placeDetailsResponse = placeDetailsRetriever.retrieve(reference);
 				System.out.print("InternationalPhoneNumber: "+placeDetailsResponse.getResult().getInternationalPhoneNumber()+" ");
 				System.out.println("details: "+placeDetailsResponse.getResult().getUri());
 				
 				List<Event> events = placeDetailsResponse.getResult().getEvents();
 				while (events != null && events.iterator().hasNext()) {
 					System.out.println("event: "+events.iterator().next());
 				}
 			}
 		} catch (PlacesSearcherException e) {				
 			e.printStackTrace();
 		} catch (DetailsRetrieverException e) {				
 			e.printStackTrace();
 		}/* catch (LocationNullException e) {
 			e.printStackTrace();
 		}*/
 		System.out.println("-- findLocationsDetails --\n");
 	}
 	/*
 	@Test
 	public void findLocationsOtherType() {
 		System.out.println("-- findLocationsOtherType --");
 		try {
 			Set<PlaceType> placeTypes = new HashSet<PlaceType>();
 			placeTypes.add( PlaceType.hospital);
 			PlacesSearchResponse placeList = placeFinder.findPlaces(location,placeTypes);
 			System.out.println(placeList.getStatus().getReason());
 			for (Place p: placeList.getResults()) {
 				System.out.println(p);
 			}
 		} catch (PlacesFinderException e) {
 			e.printStackTrace();
 		}
 		System.out.println("-- findLocationsOtherType --\n");
 	}
 	*/
 	
 	/*
 	public void cryptOld() {
 		String message = "some text to be XOR-ed" ;
 		//System.out.println(txt.length());
 		String key = "key phrase used for XOR-ing";
 		String xored = XOREncrypter.xorStringOld(message, key);
 System.out.println(message +" - xor -> " +xored);
 		String encoded = Base64.encodeBytes(xored.getBytes());
 System.out.println("encoded: "+encoded);
 		byte[] decodedBytes = null;
 		try {
 			decodedBytes = Base64.decode(encoded);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 System.out.println("decodedBytes: "+decodedBytes);
 		String decoded = new String(decodedBytes);
 System.out.println("decoded: "+decoded);
 String rexored = XOREncrypter.xorStringOld(decoded, key);
 System.out.println("rexored: "+rexored);
 		
 		System.out.println();		
 	}
 	*/
 	/*
 	@Test
 	public void crypt()  {		
		//String key="AIzaSyAfOlNmRr5G-4BPDd1faYsn9kvkV5ebBRk";
 		//final static String DEFAULT_ENCODING = Charset.defaultCharset().toString().equals("UTF-8") ? Charset.defaultCharset().toString() : "UTF-8";
 		String txt = "some text to be XOR-ed" ;
 		//System.out.println(txt.length());
 		String key = "key phrase used for XOR-ing";
 		String txtXored = null;
 		try {
 			//txtXored = XOREncrypter.xorString( txt, key );
 			txtXored = XOREncrypter.xorBytes(txt.getBytes("UTF-8"), key);
 		} catch (UnsupportedEncodingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 System.out.println("'"+txt+"'"+" XOR-ed to: "+ txtXored+"\n");
 //System.out.println(Charset.defaultCharset() +" " +txtXored);
 		String encoded = null;
 		try {
 			encoded = Base64.encodeBytes(txtXored.getBytes("UTF-8"));
 		} catch (UnsupportedEncodingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}// (txtXored);
 		//encodeBytes( txtXored.getBytes(DEFAULT_ENCODING) );
 		//System.out.println(encoded +" "+encoded.length());
 		//encoded = encoded+"___";
 		//System.out.println(encoded +" "+encoded.toCharArray().length);
 		byte[] decoded = null;
 		try {
 			decoded = Base64.decode(encoded);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}   //.decodeString( encoded);// ( encoded.getBytes(DEFAULT_ENCODING))  ;
 		System.out.println(txtXored+" is encoded to: "+encoded+" and that is decoding to: "+ decoded);
 		try {
 			System.out.print( "XOR-ing back to original: "+XOREncrypter.xorBytes(decoded, key) );
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}*/
 	
 }
