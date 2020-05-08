 package com.hansson.rento.apartments.multiple;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.text.WordUtils;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hansson.rento.apartments.ApartmentUtils;
 import com.hansson.rento.apartments.ApartmentsInterface;
 import com.hansson.rento.entities.Apartment;
 
 public class Heimstaden extends ApartmentUtils implements ApartmentsInterface {
 
 	private final static String LANDLORD = "Heimstaden";
 	private final static String BASE_URL = "http://www.heimstaden.com";
 	
 	private static final Logger mLog = LoggerFactory.getLogger("rento");
 
 	@Override
 	public List<Apartment> getAvailableApartments() {
 		List<Apartment> apartmentList = new LinkedList<Apartment>();
 		// Get html for first page
 		Document doc = connect(BASE_URL + "/For_sokande/Lediga_bostader");
 		if (doc != null) {
 			Elements apartments = doc.getElementsByClass("listItem");
 			for (Element element : apartments) {
 				try {
 					Apartment apartment = new Apartment(LANDLORD);
 					apartment.setIdentifier(element.attr("id"));
 					apartment.setUrl(BASE_URL + element.getElementsByTag("a").attr("href"));
 					apartment.setAddress(element.getElementsByTag("h5").text());
					apartment.setRooms(Double.valueOf(element.getElementsByClass("listItemLeftBottom").text().replaceAll("\\D", "")));
 					apartment.setRent(Integer.valueOf(element.getElementsByClass("listItemRightBottom").text().replaceAll("\\D", "")));
 					doc = connect(apartment.getUrl());
					Pattern sizePattern = Pattern.compile("\\d{2,}(,\\d){0,1} m2");
					Matcher matcher = sizePattern.matcher(doc.getElementsByTag("h1").text());
 					while (matcher.find()) {
 						apartment.setSize(Integer.valueOf(matcher.group().replaceAll("m2| |,\\d", "")));
 					}
 					Elements elementsByTag = doc.getElementsByTag("h3");
 					for (Element infoElement : elementsByTag) {
 						if (infoElement.text().equalsIgnoreCase("Område")) {
 							apartment.setArea(infoElement.nextElementSibling().text());
 						} else if (infoElement.text().equalsIgnoreCase("Ort")) {
 							apartment.setCity(WordUtils.capitalize(infoElement.nextElementSibling().text().toLowerCase()));
 						}
 					}
 					apartmentList.add(apartment);
 				} catch (Exception e) {
 					mLog.error(LANDLORD + " error on element #" + apartments.indexOf(element));
 					e.printStackTrace();
 				}
 			}
 		}
 		return apartmentList;
 	}
 
 	@Override
 	public String getLandlord() {
 		return LANDLORD;
 	}
 }
