 package info.plagiatsjaeger.onlinesearch;
 
 import info.plagiatsjaeger.ConfigReader;
 import info.plagiatsjaeger.SourceLoader;
 import info.plagiatsjaeger.WordProcessing;
 import info.plagiatsjaeger.interfaces.IOnlineSearch;
 import info.plagiatsjaeger.interfaces.OnLinkFoundListener;
 import info.plagiatsjaeger.types.Settings;
 
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
 
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 
 
 /**
  * Diese Abstrakte Klasse stellt den Klassen fuer die einzelnen Suchmaschinen
  * entsprechende Methoden zur Verfuegung
  * 
  * @author FischerC
  */
 public class OnlineSearch implements IOnlineSearch
 {
 	private static final String	URL_PATTERN_1		= ConfigReader.getPropertyString("URLPATTERN1");
 	private static final String	URL_PATTERN_2		= ConfigReader.getPropertyString("URLPATTERN2");
 
 	private ArrayList<String>	_allSearchResults	= new ArrayList<String>();
	private int			SEARCH_SENTENCELENGTH;
	private int			SEARCH_JUMPLENGTH;
 	private OnLinkFoundListener	_onLinkFoundListener;
	private int			MAX_URLS			= ConfigReader.getPropertyInt("NUMURLS");
 	private static final String	CHARSET				= "UTF-8";
 
	private String		URL;
	private String		URL_ARGS;
	private String		URL_ARG_AUTH;
	private String		URL_ARG_SEARCH;
 
 	private static final Logger	_logger				= Logger.getLogger(OnlineSearch.class.getName());
 
 	public OnlineSearch(String url, String searchArg, String urlArgs, String authArg)
 	{
 		Settings settings = Settings.getInstance();
 		SEARCH_JUMPLENGTH = settings.getSearchJumpLength();
 		SEARCH_SENTENCELENGTH = settings.getCompareSentenceLength();
 		MAX_URLS = settings.getSearchNumLinks();
 		URL = url;
 		URL_ARGS = urlArgs;
 		URL_ARG_AUTH = authArg;
 		URL_ARG_SEARCH = searchArg;
 
 		_logger.info("URL: " + URL);
 		_logger.info("URL_ARGS: " + URL_ARGS);
 		_logger.info("URL_ARG_AUTH: " + URL_ARG_AUTH);
 		_logger.info("URL_ARG_SEARCH: " + URL_ARG_SEARCH);
 	}
 
 	public ArrayList<String> search(String searchString, URL _URL)
 	{
 		ArrayList<String> result = null;
 		try
 		{
 			InputStreamReader reader = new InputStreamReader(_URL.openStream(), CHARSET);
 
 			BufferedReader bufferedReader = new BufferedReader(reader);
 
 			StringBuilder stringBuilder = new StringBuilder();
 			String line = bufferedReader.readLine();
 			while (line != null)
 			{
 				stringBuilder.append(line);
 				line = bufferedReader.readLine();
 			}
 			result = getUrlsFromJson(stringBuilder.toString());
 		}
 
 		catch (IOException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	@Override
 	public void searchAsync(final String completeString)
 	{
 		WordProcessing wordProcessing = new WordProcessing();
 		String[] words = wordProcessing.splitToWords(completeString);
 
 		// i wird immer um die satzlaenge + sprungweie erhoeht
 		for (int i = 0; i < words.length - SEARCH_SENTENCELENGTH; i += SEARCH_SENTENCELENGTH + SEARCH_JUMPLENGTH)
 		{
 			Thread thread = new Thread(new Runnable()
 			{
 
 				@Override
 				public void run()
 				{
 					// Sicherstellen, dass nur 1 mal pro Sekunde eine
 					// Suchabfrage gestartet wird.
 					try
 					{
 						Thread.sleep(1000);
 					}
 					catch (InterruptedException e)
 					{
 						_logger.fatal(e.getMessage(), e);
 						e.printStackTrace();
 					}
 				}
 			});
 			thread.start();
 			String searchString = "";
 			for (int j = 0; j < SEARCH_SENTENCELENGTH; j++)
 			{
 				if (searchString.length() > 0)
 				{
 					searchString += " ";
 				}
 				searchString += words[i + j];
 			}
 			_logger.info(i + "/" + (words.length - SEARCH_SENTENCELENGTH) + " Suche nach: " + searchString);
 			try
 			{
 				// Auf beendigung des Threads mit 1s warten
 				thread.join();
 			}
 			catch (InterruptedException e)
 			{
 				_logger.fatal(e.getMessage(), e);
 				e.printStackTrace();
 			}
 			search(searchString, buildSearchString(searchString));
 		}
 	}
 
 	/**
 	 * Extrahiert die Links aus dem eingegebenen String. Wenn ein
 	 * {@link OnLinkFoundListener} registriert ist werden diesem die Links
 	 * Uebermittelt.
 	 * 
 	 * @param searchResult
 	 * @return result
 	 */
 	protected ArrayList<String> getUrlsFromJson(String searchResult)
 	{
 		ArrayList<String> alUrlList = new ArrayList<String>();
 		// Matchpattern
 		// Altes JSON
 		Pattern patPattern = Pattern.compile(URL_PATTERN_1);// "\"url\"\\s*?:\\s*?\"([^\"]+?)\"");
 		// Neues JSON
 		Pattern patPatternNew = Pattern.compile(URL_PATTERN_2);// "\"displayUrl\"\\s*?:\\s*?\"([^\"]+?)\"");
 
 		Matcher matMatcher;
 
 		// Und schliesslich in der for schleife//
 		matMatcher = patPattern.matcher(searchResult);
 
 		if (matMatcher.find())
 		{
 			// Falls matcher nicht leer ist
 			matMatcher.reset();
 
 			int numURL = 0;
 			while (numURL < MAX_URLS && matMatcher.find())
 			{
 				numURL++;
 				String strLink = SourceLoader.cleanUrl(Jsoup.parse(matMatcher.group(1)).text());
 				// Falls Link bereits in _serchResults vorhanden nicht nochmal
 				// schicken
 				if (!_allSearchResults.contains(strLink))
 				{
 					alUrlList.add(strLink);
 					// System.out.println(strLink);
 					if (_onLinkFoundListener != null) _onLinkFoundListener.onLinkFound(strLink);
 				}
 			}
 		}
 		else
 		{
 			matMatcher = patPatternNew.matcher(searchResult);
 			matMatcher.reset();
 			int numURL = 0;
 			while (numURL < MAX_URLS && matMatcher.find())
 			{
 				numURL++;
 				String strLink = SourceLoader.cleanUrl(Jsoup.parse(matMatcher.group(1)).text());
 				// Falls Link bereits in _serchResults vorhanden nicht nochmal
 				// schicken
 				if (!_allSearchResults.contains(strLink))
 				{
 					alUrlList.add(strLink);
 					// System.out.println(strLink);
 				}
 			}
 		}
 		_allSearchResults.addAll(alUrlList);
 		return alUrlList;
 	}
 
 	private URL buildSearchString(String searchString)
 	{
 		URL result = null;
 		try
 		{
 			searchString = URLEncoder.encode(searchString, CHARSET).replaceAll("[ \t\n\f\r]", "+");
 			String url = URL + URL_ARG_SEARCH + searchString + URL_ARGS + URL_ARG_AUTH;
 			_logger.info(url);
 			result = new URL(url);
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		catch (MalformedURLException e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	@Override
 	public ArrayList<String> search(String searchString)
 	{
 		return search(searchString, buildSearchString(searchString));
 	}
 
 	@Override
 	public void setOnLinkFoundListener(OnLinkFoundListener listener)
 	{
 		_onLinkFoundListener = listener;
 	}
 
 }
