 package ted.datastructures;
 
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import ted.Lang;
 import ted.TedConfig;
 
 public class StandardStructure implements Serializable, Comparable<StandardStructure>
 {
 	public class AirDateUnknownException extends Exception 
 	{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -3174854029126884504L;
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6353477437638502291L;
 	
 	          int     quality      	  = 0;
 	protected Date    publishDate  	  = null; // date torrent was published online
 	protected Date    airDate      	  = null; // date episode was aired on tv
 	protected String  title     	  = "";   // episode title
 	protected String  summaryURL   	  = "";
 	protected boolean isDouble   	  = false;
 	protected int     publishTimeZone = -1;
 	
 	public int getPublishTimeZone() 
 	{
 		return publishTimeZone;
 	}
 	public void setPublishTimeZone(int publishTimeZone) 
 	{
 		this.publishTimeZone = publishTimeZone;
 	}
 	public boolean isDouble() 
 	{
 		return isDouble;
 	}
 	public void setDouble(boolean isDouble) 
 	{
 		this.isDouble = isDouble;
 	}
 	/**
 	 * @return Returns the quality.
 	 */
 	public int getQuality()
 	{
 		return quality;
 	}
 	/**
 	 * @param quality The quality to set.
 	 */
 	public void setQuality(int quality)
 	{
 		this.quality = quality;
 	}
 	
 	/**
 	 * @return Returns the publishDate.
 	 */
 	public Date getPublishDate()
 	{
 		return publishDate;
 	}
 	
 	/**
 	 * @return Returns the publishDate.
 	 */
 	public String getFormattedPublishDate()
 	{
 		String result;
 		if (this.publishDate != null)
 		{
 			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
 			result = df.format(this.publishDate);
 		}
 		else
 		{
 			result = Lang.getString("TedAddShowDialog.EpisodesTable.Upcoming");
 		}
 		return result;
 	}
 	
 	/**
 	 * @param publishDate The publishDate to set.
 	 */
 	public void setPublishDate(long publishDate)
 	{
 		Calendar c = new GregorianCalendar();
 		c.setTimeInMillis(publishDate);
 		this.publishDate = c.getTime();
 	}
 	/**
 	 * @param publishDate The Pubish date
 	 */
 	public void setPublishDate(Date publishDate)
 	{
 		this.publishDate = publishDate;
 	}
 	
 	public void setSummaryURL(String url)
 	{
 		this.summaryURL = url;
 	}
 	
 	public URL getSummaryURL() throws MalformedURLException
 	{
 		return new URL(this.summaryURL);
 	}
 	public String getSummaryURLString()
 	{
 		return this.summaryURL;
 	}
 	
 	/**
 	 * @return The airdate of this structure, with time zone correction applied
 	 * @throws AirDateUnknownException When the airdate is not known
 	 */
 	public Date getAirDate() throws AirDateUnknownException
 	{
 		if (this.airDate != null)
 		{
 			long dateTime = this.airDate.getTime();
 			int  offset   = TedConfig.getInstance().getTimeZoneOffset();
 						
 			// shows in the us are aired one day later in the
 			// rest of the world. So for every country right
 			// of the median one day should be added to a
 			// us show.			
 			if (offset >= 0
 			 && this.publishTimeZone < 0)
 			{
 				dateTime += 86400000; 
 			}
 			
 			Date returnDate = new Date(dateTime);
 			return returnDate;
 		}
 		else
 		{
 			AirDateUnknownException e = new AirDateUnknownException();
 			throw e;
 		}
 	}
 	public void setAirDate(Date airDate) 
 	{
 		this.airDate = airDate;
 	}
 	public String getTitle() 
 	{
 		return title;
 	}
 	public void setTitle(String title) 
 	{
 		this.title = title;
 	}
 	/**
 	 * addAiredText indicates if the formatted airdate should be pre/post appended with 'will air on' or 'aired on' strings
 	 * @return The formatted air date. When no airdate is known: formatted torrent publish date.
 	 */
 	public String getFormattedAirDate(boolean addAiredText) 
 	{
 		String day = "";
 		String result = "";
 		try 
 		{
 			long airTime = this.getAirDate().getTime();
 						
 			// compare the breakUntil date to the current date 
 			int diff = (int) Math.ceil((airTime - new Date().getTime())/86400000.0);
 			switch(diff)
 			{
 				case 0:
 					day = Lang.getString("TedSerie.Today");
 					break;
 				case 1:
 					day = Lang.getString("TedSerie.Tomorow");
 					break;
 				case 2:
 					day = Lang.getString("TedSerie.DayAfterTomorow");
 					break;
 				// if its within the next week just display the day of the week
 				case 3:
 				case 4:
 				case 5:
 				case 6:
 				{
 					SimpleDateFormat dayFormat = new SimpleDateFormat("EEEEE");
 					day = dayFormat.format(airTime);
 					break;
 				}
 				// if its more than a week away display the day and date
 				default:
 				{
 					DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
 					day = df.format(airTime);
 					break;
 				}
 			}
 			if (addAiredText)
 			{
 				switch(diff)
 				{
 					case 0:
 					case 1:
 					case 2:					// if its within the next week just display the day of the week
 					case 3:
 					case 4:
 					case 5:
 					case 6:
 					{
 						result = Lang.getString("StandardStructure.WillAirOnPart1") + " " + day + " " + Lang.getString("StandardStructure.WillAirOnPart2");
 						break;
 					}
 					// if its more than a week away display the day and date
 					default:
 					{
 						if (this.airedBeforeOrOnToday())
 						{
 							result = Lang.getString("StandardStructure.AiredOn") + " " + day;
 						}
 						else
 						{
 							result = Lang.getString("StandardStructure.WillAirOn") + " " + day;	
 						}
 						break;
 					}
 				}
 			}
 			else
 			{
 				result = day;
 			}
 		} 
 		catch (AirDateUnknownException e)
 		{
 			result = Lang.getString("StandardStructure.UnknownAirdate");
 		}
 		
 		return result;
 	}
 	
 	public String getSearchString() 
 	{
 		return "";
 	}
 	
 	
 	/**
 	 * @return If this episode has been aired on or before today
 	 */
 	public boolean airedBeforeOrOnToday()
 	{
 		Date current = new Date();
 		
 		try 
 		{
 			return this.getAirDate().before(current);
 		} catch (AirDateUnknownException e) 
 		{
 			return false;
 		}
 	}
 	
 	public String getFormattedAirDateWithText()
 	{
 		return this.getFormattedAirDate(true);
 	}
 	
 	public int compareTo(StandardStructure arg0)
 	{
 		return this.compareDateTo(arg0);
 	}
 		
 	public StandardStructure guessNextEpisode() 
 	{
 		return new StandardStructure();
 	}
 	
 	public String getEpisodeChooserTitle()
 	{
 		return this.toString();
 	}
 	public String getSubscribtionOptionsTitle()
 	{
 		return this.getTitle();
 	}
 	public int compareDateTo(StandardStructure secondSStructure) 
 	{
 		int result = 0;
 		
 		Date thisDate;
 		Date secondDate;
 		
 		try 
 		{
 			thisDate = this.getAirDate();
 		} 
 		catch (AirDateUnknownException e) 
 		{
 			thisDate = this.getPublishDate();
 		}
 		
 		try 
 		{
 			secondDate = secondSStructure.getAirDate();
 		} 
 		catch (AirDateUnknownException e) 
 		{
 			secondDate = secondSStructure.getPublishDate();
 		}
 	
 		if (thisDate.before(secondDate))
 		{
 			result = -1;
 		}
 		else if (thisDate.after(secondDate))
 		{
 			result = 1;
 		}
 		
 		return result;
 	}
 }
