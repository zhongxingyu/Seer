 package com.hansson.rento.apartments.multiple;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hansson.rento.apartments.ApartmentsInterface;
 import com.hansson.rento.entities.Apartment;
 import com.hansson.rento.utils.HtmlUtil;
 
 public class PBAFastigheter implements ApartmentsInterface {
 
 	private static final String LANDLORD = "PBA Karlskrona Malm&ouml; AB";
 	private static final String BASE_URL = "http://www.pba.se/page/18/lediga-lagenheterlokaler.aspx";
 	private static final Logger mLog = LoggerFactory.getLogger("rento");
 
 	@Override
 	public List<Apartment> getAvailableApartments() {
 		List<Apartment> apartmentList = new LinkedList<Apartment>();
 		try {
 			Document doc = Jsoup.connect(BASE_URL).get();
 			Elements apartments = doc.getElementById("content").getElementsByClass("entry");
 			for (Element element : apartments) {
 				try {
 					Apartment apartment = new Apartment(LANDLORD);
 					apartment.setUrl(element.getElementsByTag("h2").get(0).getElementsByTag("a").attr("href"));
 					apartment.setIdentifier(apartment.getUrl().split("/")[apartment.getUrl().split("/").length - 1]);
 
 					String[] areaAndCity = element.getElementsByTag("h2").text().replaceAll("Hy.* i ", "").trim().split("[ ,]");
 					apartment.setArea(areaAndCity[0]);
 					apartment.setCity(areaAndCity[2]);
 					String informationText = element.getElementsByTag("span").get(2).text();
 
 					Pattern p = Pattern.compile("Adress: .+,");
 					Matcher matcher = p.matcher(informationText);
 					matcher.find();
 					apartment.setAddress(matcher.group().replace("Adress: ", "").replace(",", ""));
 
 					p = Pattern.compile("Antal Rum: \\d+");
 					matcher = p.matcher(informationText);
 					matcher.find();
 					apartment.setRooms(Double.valueOf(matcher.group().replaceAll("Antal Rum: ", "")));
 
 					doc = Jsoup.connect(apartment.getUrl()).get();
 					Elements infoElements = doc.getElementsByTag("tbody").get(0).getElementsByTag("tr");
 					for (Element currentInfo : infoElements) {
 						if (currentInfo.getElementsByTag("th").text().equals("Avgift")) {
 							apartment.setRent(Integer.valueOf(currentInfo.getElementsByTag("td").text().replaceAll("\\D", "")));
 						} else if (currentInfo.getElementsByTag("th").text().equals("Boarea")) {
							p = Pattern.compile("[\\d]+[,\\d]+");
							matcher = p.matcher(currentInfo.getElementsByTag("td").text());
							matcher.find();
							apartment.setSize(Integer.valueOf(matcher.group().replaceAll(",\\d", "")));
 						}
 					}
 					apartmentList.add(apartment);
 				} catch (Exception e) {
 					mLog.error(LANDLORD + " error on element #" + apartments.indexOf(element));
 					e.printStackTrace();
 				}
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return apartmentList;
 	}
 
 	@Override
 	public String getLandlord() {
 		return LANDLORD;
 	}
 }
