 package controllers;
 
 import play.*;
 import play.libs.WS;
 import play.libs.WS.HttpResponse;
 import play.libs.WS.WSRequest;
 import play.libs.XPath;
 import play.mvc.*;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import org.h2.util.New;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import com.google.gson.JsonElement;
 import com.sun.xml.internal.fastinfoset.util.StringArray;
 
 import common.FBSecure;
 import common.FBUtil;
 
 import models.*;
 
 @With(FBSecure.class)
 public class Application extends FBController {
 //laptop hp^^
 	public static final String SEARCH_URL = "http://services.tvrage.com/myfeeds/search.php?key=wO0oUUcze0VMpBWopBij&show=";
 	public static final String EPISODE_LIST = "http://services.tvrage.com/myfeeds/episode_list.php?key=wO0oUUcze0VMpBWopBij&sid=";
 
 	public static void index(){
 		User currentUser = getUser();
 		String oauthToken = getOAuthToken();
 
 		JsonElement je = FBUtil.getInstance().executeGraphRequest("/me/friends", oauthToken);
 
 		
 		//String temp = "tt0606110-tt0606111-tt0606117-tt0606112-tt0606109-tt0606116-tt0606108-tt0606113-tt0606104-tt0606115-tt0606114-tt0606118-tt0606106-tt0606119-tt0606107-tt0606105-tt0756504-tt0606103-tt0788623-tt0760776-tt0801608-tt0774239-tt0863640-tt0869673-tt0873024-tt0858000-tt0866188-tt0865115-tt0875360-tt0885871-tt0885872-tt0892578-tt0904645-tt0897111-tt0924673-tt0933496-tt0946316-tt0943657-tt0969965-tt0978049-tt0966112-tt0994245-tt0982196-tt1017766-tt1084790-tt1115217-tt1097293-tt1094834-tt1121411-tt1117436-tt1120228-tt1140904-tt1142665-tt1155839-tt1154186-tt1203040-tt1204516-tt1206872-tt1206866-tt1206867-tt1206868-tt1206869-tt1206870-tt1206871-tt1256606-tt1256181-tt1256185-tt1256186-tt1256187-tt1256188-tt1256189-tt1256190-tt1256191-tt1256171-tt1256172-tt1256173-tt1256174-tt1256175-tt1256176-tt1256177-tt1256178-tt1256179-tt1256180-tt1256182-tt1256184-tt1256183-tt1410619-tt1425523-tt1491272-tt1510422-tt1520584-tt1523038-tt1523745-tt1531287-tt1536724-tt1541289-tt1541288-tt1553522-tt1559709-tt1570154-tt1570155-tt1576524-tt1589779-tt1595724-tt1609315-tt1610780-tt1624286-tt1631001-tt1632014-tt1632015-tt1640994-tt1648494-tt1701804-tt1713733-tt1723669-tt1733369-tt1737327-tt1737328-tt1746026-tt1757176-tt1763611-tt1777828-tt1779155-tt1778997-tt1795967-tt1795956-tt1795957-tt1795958-tt1795959-tt1795960-tt1795961-tt1795962-tt1795963-tt1795964-tt1795965-tt1795966-tt1982341-tt2039807-tt2049132-tt2049980-tt2071800-tt2072524-tt2072525-tt2072526-tt2094413-tt2102769-tt2108075-tt2121966";
		
 		String seriesname = "How I met your mother";
 
 		WSRequest request = WS.url(SEARCH_URL+seriesname);
 		request.timeout("2min");
 		HttpResponse response = request.get();
 		Document document = response.getXml();
 
 		Node showNode = XPath.selectNode("Results/show[1]", document);
 
 		String id = XPath.selectText("showid", showNode);
 		String name = XPath.selectText("name", showNode); 
 		String country = XPath.selectText("country",showNode);
 		Integer startDate = Integer.valueOf(XPath.selectText("started", showNode));
  
 		
 		Integer totalSeasons = Integer.valueOf(XPath.selectText("seasons", showNode));
 		String status = XPath.selectText("status", showNode);
 
 		ArrayList<Long> genreList = new ArrayList<Long>();
 		
 		Series series = new Series(id,name,startDate,totalSeasons,country,status);
 		series.save();
 		for(Node node: XPath.selectNodes("Results/show[1]/genres", document)) {
 			String genreName = XPath.selectText("genre", node);
 			Genre genre = Genre.find("byGenreName","Comedy").first();
 			SeriesGenre sg = new SeriesGenre(series.getId(),genre.getId());
 			sg.save();
 		}
 
 		//series.save();
 
 		WSRequest request2 = WS.url(EPISODE_LIST+series.seriesID);
 		request2.timeout("2min");
 		HttpResponse response2 = request2.get();
 		Document document2 = response2.getXml();
 
 		for (int i = 1; i <= series.totalSeasons; i++) {
 			Season season = new Season(i, series.getId());
 			season.save();
 			for(Node showNode2 : XPath.selectNodes("Show/Episodelist/Season[@no='"+i+"']/episode", document2)){
 				//epnumber 1- 190..
 				Integer epnum = Integer.valueOf(XPath.selectText("epnum", showNode2));
 				//sezon numarası S02 E02 gibi
 				Integer seasonnum = Integer.valueOf(XPath.selectText("seasonnum", showNode2));
 				
 				String date = XPath.selectText("airdate", showNode2);
 				SimpleDateFormat smf = new SimpleDateFormat("yyyy-MM-dd");
 				Date airdate = null;
 				try {
 					airdate = smf.parse(date);
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					System.err.println("airdatei parse edemedim.");
 				}
 				
 				String title = XPath.selectText("title", showNode2);
 				Float rating = Float.valueOf(XPath.selectText("rating", showNode2));
 				
 				Episode ep = new Episode(title, seasonnum,epnum, airdate, rating, season.getId());
 				ep.save();
 			}
 		}

 
 		render();
 	}
 
 	public static void series() {
 		render();
 	}
 
 }
