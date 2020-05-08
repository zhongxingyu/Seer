 package com.hansson.rento.apartments.multiple;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.jsoup.nodes.Document;
 
 import com.google.gson.Gson;
 import com.hansson.rento.KonradssonsFastigheter;
 import com.hansson.rento.apartments.ApartmentUtils;
 import com.hansson.rento.apartments.ApartmentsInterface;
 import com.hansson.rento.apartments.blekinge.Hermanssonbolagen;
 import com.hansson.rento.apartments.blekinge.TrossoWamoFastigheter;
 import com.hansson.rento.apartments.blekinge.karlshamn.KjellsonsSkogOchFastighetsForvaltning;
 import com.hansson.rento.apartments.blekinge.karlshamn.StrandbergsFastigheter;
 import com.hansson.rento.apartments.blekinge.karlshamn.ThernstromsForvaltning;
 import com.hansson.rento.apartments.blekinge.karlskrona.BengtAkessonFastigheter;
 import com.hansson.rento.apartments.blekinge.karlskrona.HansAkessonFastigheter;
 import com.hansson.rento.apartments.blekinge.karlskrona.KSFastigheter;
 import com.hansson.rento.apartments.blekinge.karlskrona.Karlskronahem;
 import com.hansson.rento.apartments.blekinge.karlskrona.KarlskronahemStudent;
 import com.hansson.rento.apartments.blekinge.karlskrona.LindebergFastigheter;
 import com.hansson.rento.apartments.blekinge.karlskrona.MagistratusFastigheter;
 import com.hansson.rento.apartments.blekinge.karlskrona.PBAStudent;
 import com.hansson.rento.apartments.blekinge.karlskrona.Utklippan;
 import com.hansson.rento.apartments.skane.BroBizForvaltningsAB;
 import com.hansson.rento.apartments.smaland.vaxjo.BoplatsVaxjo;
 import com.hansson.rento.apartments.smaland.vaxjo.BoplatsVaxjoStudent;
 import com.hansson.rento.entities.Apartment;
 import com.hansson.rento.utils.apartments.BaseJsonApartment;
 import com.hansson.rento.utils.apartments.BaseJsonResult;
 
 public class Akelius extends ApartmentUtils implements ApartmentsInterface {
 
 	private final static String LANDLORD = "Akelius";
 	private final static String REQUEST_URL = "http://marknad.akelius.se/API/Service/SearchServiceHandler.ashx";
	private final static String BASE_URL = "http://www.akelius.se/web/soklagenhet.asp?seekAreas=";
 
 	private List<Integer> mCities = new LinkedList<Integer>() {
 
 		private static final long serialVersionUID = 2494119335581660680L;
 		{
 			add(901000);
 			add(202000);
 			add(903000);
 			add(904000);
 			add(700000);
 			add(907000);
 			add(908000);
 			add(909000);
 			add(714000);
 			add(918000);
 			add(119000);
 			add(920000);
 			add(100000);
 			add(722000);
 			add(224000);
 			add(725000);
 			add(226000);
 			add(227000);
 			add(928000);
 			add(200000);
 			add(229000);
 			add(130000);
 			add(931000);
 			add(232000);
 			add(233000);
 			add(934000);
 			add(935000);
 			add(210010);
 			add(236000);
 			add(238000);
 			add(939000);
 			add(941000);
 		}
 	};
 
 	@Override
 	public List<Apartment> getAvailableApartments() {
 		List<Apartment> apartmentList = new LinkedList<Apartment>();
 		for (Integer city : mCities) {
 			apartmentList.addAll(handleCity(city));
 		}
 		return apartmentList;
 	}
 
 	private Collection<? extends Apartment> handleCity(Integer city) {
 		List<Apartment> apartmentList = new LinkedList<Apartment>();
 		Map<String, String> headers = new HashMap<String, String>();
 		headers.put("X-Momentum-API-KEY", "qt4LomI5O+4H6sCQMgmG4sbaQQD0rOIq3ZEYuBcqTr0=");
 		headers.put("Content-Type", "application/x-www-form-urlencoded");
 		headers.put("Accept", "application/json");
 		Map<String, String> postData = new HashMap<String, String>();
 		postData.put(
 				"Parm1",
 				"{\"CompanyNo\":0,\"SyndicateNo\":1,\"ObjectMainGroupNo\":1,\"ObjectSeekAreas\":[{\"No\":"+ city +"}],\"RentLimit\":null,\"AreaLimit\":null,\"Page\":1,\"Take\":5,\"SortOrder\":\"CompanyNo,SeekAreaDescription,StreetName asc\",\"ReturnParameters\":[\"ObjectNo\",\"FirstEstateImageUrl\",\"Street\",\"SeekAreaDescription\",\"PlaceName\",\"ObjectSubDescription\",\"ObjectArea\",\"RentPerMonth\",\"MarketPlaceDescription\",\"CountInterest\",\"FirstInfoTextShort\",\"FirstInfoText\",\"EndPeriodMP\",\"FreeFrom\",\"SeekAreaUrl\",\"Latitude\",\"Longitude\",\"BoardNo\"]}");
 		postData.put("CallbackMethod", "PostObjectSearch");
 		postData.put("CallbackParmCount", "1");
 		postData.put("__WWEVENTCALLBACK", "null");
 		Document connect = connect(REQUEST_URL, postData, headers, true);
 		Gson gson = new Gson();
 		List<BaseJsonApartment> results = gson.fromJson(connect.text(), BaseJsonResult.class).getResult();
 		for (BaseJsonApartment result : results) {
 			Apartment apartment = new Apartment(LANDLORD);
 			apartment.setStudent(false);
			apartment.setUrl(BASE_URL + city);
 			apartment.setAddress(result.getStreet());
 			apartment.setCity(result.getPlaceName());
 			apartment.setRent(result.getRentPerMonthSort());
 			String rooms = result.getObjectSubDescription().replaceAll("\\D", "");
 			apartment.setRooms(Double.valueOf(!rooms.trim().equals("") ? rooms : "0"));
 			apartment.setSize((int) result.getObjectAreaSort());
 			apartmentList.add(apartment);
 		}
 		return apartmentList;
 	}
 
 	@Override
 	public String getLandlord() {
 		return LANDLORD;
 	}
 
 }
