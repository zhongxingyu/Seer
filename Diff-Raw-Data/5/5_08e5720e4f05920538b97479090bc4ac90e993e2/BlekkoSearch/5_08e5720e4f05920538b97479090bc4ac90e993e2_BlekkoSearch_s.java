 package plagiatssoftware;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 
 
 /**
  * Enthlt Funktionen zum Suchen in der Suchmaschine <a
  * href="http://blekko.com">Blekko</a>
  * 
  * @author Andreas
  */
 public class BlekkoSearch
 {
 	private static final String	URL				= "http://blekko.com/ws/?";
 	private static final String	URL_ARG_JSON	= "+%2Fjson";
 	private static final String	URL_ARG_SEARCH	= "q=";
 
 	private static final String	CHARSET			= "UTF-8";
 
 	private ArrayList<String>	_searchResults	= new ArrayList<String>();
 
 	public BlekkoSearch()
 	{
 
 	}
 
 	/**
 	 * Sucht auf der Suchmaschine Blekko nach Treffern fr den gegebenen Text
 	 * 
 	 * @param textToSearch
 	 * @return ArrayList mit den Ergebnis-Links
 	 */
 	public ArrayList<String> search(String textToSearch)
 	{
 		ArrayList<String> result = null;
 
 		try
 		{
 			textToSearch = URLEncoder.encode(textToSearch, CHARSET).replaceAll("[ \t\n\f\r]", "+");
 
 			URL url = new URL(URL + URL_ARG_SEARCH + textToSearch + URL_ARG_JSON);
 			InputStreamReader reader = new InputStreamReader(url.openStream(), CHARSET);
 
 			BufferedReader bufferedReader = new BufferedReader(reader);
 
 			StringBuilder stringBuilder = new StringBuilder();
 			String line = bufferedReader.readLine();
 			while (line != null)
 			{
 				stringBuilder.append(line);
 				line = bufferedReader.readLine();
 			}
 			result = getUrlFromJson(stringBuilder.toString());
 		}
 		catch (MalformedURLException e)
 		{
 			e.printStackTrace();
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Holt URLs aus json
 	 * 
 	 * @param strSearchLink
 	 * @return Gibt Liste der URLs zurueck
 	 */
 	private ArrayList<String> getUrlFromJson(String strSearchLink)
 	{
 		ArrayList<String> alUrlList = new ArrayList<String>();
 		// Matchpattern
 		//Altes JSON
 		Pattern patPattern = Pattern.compile("\"url\"\\s*?:\\s*?\"([^\"]+?)\"");
 		//Neues JSON
 		Pattern patPatternNew = Pattern.compile("\"displayUrl\"\\s*?:\\s*?\"([^\"]+?)\"");
 		
 		Matcher matMatcher;
 
 		// Und schlielich in der for schleife//
 		matMatcher = patPattern.matcher(strSearchLink);
 		
 		if(matMatcher.find())
 		{
 			//Falls matcher nicht leer ist
 			matMatcher.reset();
 			
 			while (matMatcher.find())
 			{
 				String strLink = Jsoup.parse(matMatcher.group(1)).text();
 				strLink = strLink.replaceAll("www.", "");
 				strLink = strLink.replaceAll("http://", "");
 				strLink = "http://"+strLink;
 				System.out.println(strLink);
 				//Falls Link bereits in _serchResults vorhanden nicht nochmal schicken
				if (_searchResults.contains(matMatcher.group(1)))
 				{
 
 				}
 				else
 				{
 					alUrlList.add(strLink);
 				}
 			}
 		}
 		else
 		{
 			matMatcher = patPatternNew.matcher(strSearchLink);
 			matMatcher.reset();
 			while (matMatcher.find())
 			{
 				String strLink = Jsoup.parse(matMatcher.group(1)).text();
 				strLink = strLink.replaceAll("www.", "");
 				strLink = strLink.replaceAll("http://", "");
 				strLink = "http://"+strLink;
 				System.out.println(strLink);
 				//Falls Link bereits in _serchResults vorhanden nicht nochmal schicken
				if (_searchResults.contains(matMatcher.group(1)))
 				{
 
 				}
 				else
 				{
 					alUrlList.add(strLink);
 				}
 			}
 			
 		}
 		_searchResults.addAll(alUrlList);
 		return alUrlList;
 	}
 
 }
