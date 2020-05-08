 package ted.epguides;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Vector;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.text.SimpleDateFormat;
 
 import ted.TedSerie;
 import ted.TedXMLParser;
 import ted.datastructures.DailyDate;
 import ted.datastructures.SeasonEpisode;
 import ted.datastructures.StandardStructure;
 import ted.datastructures.StandardStructure.AirDateUnknownException;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /* 
  * TedEpguidesParser parses an epguides.com webpage and retrieves 
  * informations that match a regular expression (regex) pattern.
  * 
  * @orignal author: bororo
  * Refactored by: Joost
  * 
  */
 
 
 public class ScheduleParser 
 {      
 	public class CouldNotConstructEpisodeException extends Exception 
 	{
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -7064312325315795974L;
 
 	}
 
 	private Vector<StandardStructure> parseEpguides (String seriesName, Date from, Date to, boolean isDaily)
     {
 		// The general pattern that epguides follows for their show lists
 	    // Format:   1.   1- 1        100     22 Sep 04   <a target="_blank" href="http://www.tv.com/lost/pilot-1/episode/334467/summary.html">Pilot (1)</a>
 	    String regex       = "(\\d+)(\\-|\\-\\s)(\\d+)(\\s+)(\\w*+)(\\s+)(\\d+)(\\s+)(\\w+)(\\s+)(\\d+)";
 	    String regexNoDate = "(\\d+)(\\-|\\-\\s)(\\d+)(\\s+)(\\w*+)(\\s+)";
 	    String regexName   = "(>)(.+)(</a>)(\\s*)$";
 	    
         Date parsedAirDate=null;
         String DATE_FORMAT = "d/MMM/yy";        
        
         SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
         
         Pattern pattern       = Pattern.compile(regex);
         Pattern patternNoDate = Pattern.compile(regexNoDate); 
         Pattern patternName   = Pattern.compile(regexName);
         
         Vector<StandardStructure> episodes = new Vector<StandardStructure>();
         
         try 
         {
             URL epguides = new URL("http://www.epguides.com/" + seriesName + "/");
             BufferedReader in = new BufferedReader( new InputStreamReader(epguides.openStream())); 
                 
             String inputLine;
             while ((inputLine = in.readLine()) != null) 
             {               
                 Matcher matcher       = pattern      .matcher(inputLine);      
                 Matcher matcherNoDate = patternNoDate.matcher(inputLine);
                 Matcher matcherName   = patternName  .matcher(inputLine);
                 
                 boolean date   = matcher.find();
                 boolean noDate = matcherNoDate.find();
                 
                 int season   = 0;
                 int episode  = 0;
                 String title = "";
                 Date airdate = null;
                 
                 if (matcherName.find())
                 {
                	 title = matcherName.group(2);
                 }                
                 
                 if (date) 
                 { 
                     try
                     {
                         // Convert between String and Date
                         parsedAirDate = sdf.parse(matcher.group(7)+"/" + matcher.group(9) + "/" + matcher.group(11));
                          
                          if (parsedAirDate.after(from) && parsedAirDate.before(to))
                          {
                         	 season   = Integer.parseInt(matcher.group(1));
                              episode  = Integer.parseInt(matcher.group(3));
                              airdate = parsedAirDate;                                                                
                          }
                     }
                     catch (java.text.ParseException pe) 
                     {
                             // ParseAirDate or Season / episode failed
                             // TODO: Add Handling Code
                     }                       
                 } // matcher.find() ends 
                 else if (noDate)
                 {
                 	// We've found an episode without a date.                	
                     season  = Integer.parseInt(matcherNoDate.group(1));
                     episode = Integer.parseInt(matcherNoDate.group(3));
                 }
                 
 
                 if (date || noDate)
                 {
                 	try 
                 	{
 						episodes.add(constructEpisode(isDaily, season, episode, title, airdate));
 					} 
                 	catch (CouldNotConstructEpisodeException e) 
 					{
 						// do nothing
 					}
                 }
             } // while ends
             in.close();
            
         } catch (MalformedURLException e) {
             // new URL() failed     
             // TODO: Add Handling Code
 
         } catch (IOException e) { 
             // openConnection() failed 
             // TODO: Add Handling Code
         }
         
         // sort the seasons and episodes in ascending order
         Collections.sort(episodes);
         
         return episodes;
         
     }
 	
 	private StandardStructure constructEpisode(boolean isDaily, int season, int episode, String title, Date airdate) throws CouldNotConstructEpisodeException
 	{
 		StandardStructure result = null;
 		if (isDaily)
     	{
     		if (airdate != null)
     		{           			
     			result = new DailyDate(airdate, title);
     		}
     	}
 		else
 		{
 			result = new SeasonEpisode(season, episode, airdate, title);	
 		}
 		
 		if (result == null)
 		{
 			CouldNotConstructEpisodeException e = new CouldNotConstructEpisodeException();
 			throw e;
 		}
 		else
 		{
 			return result;
 		}
 	}
 	
 	private Vector<StandardStructure> parseTvRage(String showName, Date from, Date to, boolean isDaily)
 	{
         Vector<StandardStructure> episodes = new Vector<StandardStructure>();
         
         // First we want to detect the id of this show on tvrage. For this we need
         // to parse the search results on the name of the show.
     	String url = "http://www.tvrage.com/feeds/search.php?show=" + showName;
 		TedXMLParser parser = new TedXMLParser();
 		Element foundShowsElement = parser.readXMLFromURL(url);
 		
 		NodeList foundShowsList = foundShowsElement.getElementsByTagName("show");
 		
 		// Only continue if we've found at least one show with this name.
 		int showId = -1;
 		if(foundShowsList != null && foundShowsList.getLength() > 0)
 		{
 			// For every show...
 			for(int i = 0; i < foundShowsList.getLength(); i++)
 			{
 				// ... see if it's the exact same name as the show we're looking for.
 				Element show = (Element)foundShowsList.item(i);
 				
 				String foundName = parser.getTextValue(show, "name");
 				
 				// If not then try the next show.
 				if (!foundName.toLowerCase().equals(showName.toLowerCase()))
 				{
 					continue;
 				}
 				
 				// Otherwise we've found the show id
 				showId = parser.getIntValue(show, "showid");
 				
 				// No need to search further
 				break;
 			}
 		}
 
 		// If we've found the show id
 		if (showId != -1)
 		{
 			// The date format tvrage uses
 			Date parsedAirDate=null;
 	        String DATE_FORMAT = "yy-MM-dd";        
 	       
 	        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
 	        
 	        url = "http://www.tvrage.com/feeds/episode_list.php?sid=" + showId;	
 			Element foundShowElement = parser.readXMLFromURL(url);
 			
			// No information available
			if (foundShowElement == null)
			{
				return episodes;
			}
			
 			// The xml file consist out of an episodelist 
 			NodeList foundSeasonsList = foundShowElement.getElementsByTagName("Episodelist");
 			
 			if (foundSeasonsList != null && foundSeasonsList.getLength() > 0)
 			{
 				// There is only on episode list
 				Element episodeList = (Element)foundSeasonsList.item(0);
 				boolean searchForNextSeason = true;
 				int seasonNumber = 1;
 				while (searchForNextSeason)
 				{
 					// Which has multiple seasons
 					NodeList seasonEpisodes = episodeList.getElementsByTagName("Season" + seasonNumber);
 					
 					if (seasonEpisodes != null && seasonEpisodes.getLength() > 0)
 					{
 						for (int i = 0; i < seasonEpisodes.getLength(); ++i)
 						{
 							// For every season 
 							Element allSeasons = (Element)seasonEpisodes.item(i);
 						
 							// Get all the episodes of that season
 							NodeList episodesOfSeason = allSeasons.getElementsByTagName("episode");
 
 							// Retrieve all the needed info
 							if (seasonEpisodes != null && seasonEpisodes.getLength() > 0)
 							{
 								for (int z = 0; z < episodesOfSeason.getLength(); z++)
 								{
 									Element episodeInfo = (Element)episodesOfSeason.item(z);
 									
 									int episode = parser.getIntValue(episodeInfo, "seasonnum");
 									String title = parser.getTextValue(episodeInfo, "title");
 									Date airdate = null;
 									
 									try
 				                    {
 										String date = parser.getTextValue(episodeInfo, "airdate");
 				                        parsedAirDate = sdf.parse(date);
 				                         
 				                         if (parsedAirDate.after(from) && parsedAirDate.before(to))
 				                         {
 				                             airdate = parsedAirDate;                                                                
 				                         }
 				                    }
 				                    catch (java.text.ParseException pe) 
 				                    {
 				                    	continue;
 				                    }  
 									
 				                	try 
 				                	{
 				                		StandardStructure standardEpisode = constructEpisode(isDaily, seasonNumber, episode, title, airdate);
 										episodes.add(standardEpisode);
 									} 
 				                	catch (CouldNotConstructEpisodeException e) 
 									{
 										// do nothing
 									}
 								}
 							}
 						}
 						
 						seasonNumber++;
 					}
 					else
 					{
 						searchForNextSeason = false;
 					}
 				}
 			}
 		}
 			            
         // sort the seasons and episodes in ascending order
         Collections.sort(episodes);
         
         return episodes;	
 	}    
 	
 	private void detectDoubleEpisodes(Vector<StandardStructure> episodes)
 	{
 		// Finally detect double episodes
         // Walk backwards over all episodes, if the previous episode (the one with the
         // higher episode number) has the same air date than we've found a double episode.
         if (episodes.size() > 0)
         {
         	Date airDate = new Date(0);
         	Date previousAirDate = new Date(0);
 			for (int i = 0; i < episodes.size(); i++)
 	        {
 				try 
 				{
 					airDate = episodes.get(i).getAirDate();
 				} 
 				catch (AirDateUnknownException e) 
 				{
 					continue;
 				}
 								
 				if (airDate.getTime() == previousAirDate.getTime())
 	        	{
 	        		episodes.get(i).setDouble(true);
 	        	}
 				
 				previousAirDate = airDate;
 			}	        	
     	}
 	}
 	
 	private Vector<StandardStructure> combineLists(Vector<StandardStructure> firstList, Vector<StandardStructure> secondList)
 	{
 		// If one of both lists is empty return the other.
 		int firstSize  = firstList.size();
 		int secondSize = secondList.size();		
 		if (firstSize == 0)
 		{
 			return secondList;
 		}
 		else if (secondSize == 0)
 		{
 			return firstList;
 		}
 		
 		// Combine both lists into one new list
 		Vector<StandardStructure> combinedList = new Vector<StandardStructure>();
 			
 		// Take all the elements of both lists
 		int maxListSize = Math.max(firstSize, secondSize);
 		
 		// As the lists are sorted walk through them step by step
 		int firstPos  = 0;
 		int secondPos = 0;
 		StandardStructure s1 = null;
 		StandardStructure s2 = null;
 		for (int i = 0; i < maxListSize; i++)
 		{			
 			if (firstPos < firstSize)
 			{
 				s1 = firstList.get(firstPos);
 			}
 			
 			if (secondPos < secondSize)
 			{
 				s2 = secondList.get(secondPos);
 			}
 			
 			int compare = s1.compareTo(s2);
 			if (compare == 0)
 			{
 				++firstPos;
 				++secondPos;
 				
 				// Compare the air dates, should be identical
 				try 
 				{
 					if (s1.getAirDate() != s2.getAirDate())
 					{
 						// Do what?
 					}
 				} 
 				catch (AirDateUnknownException e) 
 				{
 					continue;
 				}
 				
 				// Get as much information as possible from both sources.
 				if (s1.getTitle().equals(""))
 				{
 					combinedList.add(s2);
 				}
 				else
 				{
 					combinedList.add(s1);
 				}
 			}
 			else if (compare == -1)
 			{
 				++firstPos;
 				combinedList.add(s1);
 			}
 			else
 			{
 				++secondPos;
 				combinedList.add(s2);
 			}			
 		}
 		
 		return combinedList;
 	}
     
     public Vector<StandardStructure> getPastSeasonEpisodes(TedSerie serie)
     {
     	Date systemDate = new Date();   // Get time and date from system
         Date past = new Date();   // Get time and date from system
         past.setTime(0);
                
         return getEpisodes(serie, past, systemDate);
     }
     
     public Vector<StandardStructure> getFutureSeasonEpisodes(TedSerie serie)
     {
     	// system date
        	Date systemDate = new Date();
        
         // one year from now
         Calendar yearFromNow = Calendar.getInstance();
         yearFromNow.add(Calendar.YEAR, 1);
                
         return getEpisodes(serie, systemDate, yearFromNow.getTime()); 
     }
     
     public Vector<StandardStructure> getScheduledSeasonEpisodes(TedSerie serie)
     {
     	Date past = new Date();   // Get time and date from system
         past.setTime(0);
        
         // one year from now
         Calendar yearFromNow = Calendar.getInstance();
         yearFromNow.add(Calendar.YEAR, 1);
         
         return getEpisodes(serie, past, yearFromNow.getTime());   
     }
 
 	private Vector<StandardStructure> getEpisodes(TedSerie serie, Date from,
 			Date to) 
 	{
 		Vector<StandardStructure> episodes1 = this.parseTvRage(serie.getName(), from, to, serie.isDaily());
         Vector<StandardStructure> episodes2 = this.parseEpguides(serie.getEpguidesName(), from, to, serie.isDaily());
         Vector<StandardStructure> episodes  = this.combineLists(episodes1, episodes2);
         detectDoubleEpisodes(episodes);
         
         return episodes;
 	}
     
 } 
