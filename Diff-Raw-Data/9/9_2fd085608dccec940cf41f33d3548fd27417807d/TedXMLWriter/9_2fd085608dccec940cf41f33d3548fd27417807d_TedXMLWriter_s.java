 package ted;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Vector;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 public class TedXMLWriter 
 {
 
 	public TedXMLWriter(Vector series) 
 	{
 		this.writeXML(series);
 	}
 	
 	private void writeXML(Vector series)
 	{
 		TedSerie serie;
 		TedSerieFeed feed;
 		Vector feeds;
 		
 		String s;
 		String location;
 		File standardFile = new File("shows.xml");
 		
 	    JFileChooser chooser = new JFileChooser();
 	    TedExampleFileFilter filter = new TedExampleFileFilter();
 	    filter.addExtension("xml");
 	    filter.setDescription("XML-file");
 	    chooser.setFileFilter(filter);
 	    chooser.setCurrentDirectory(standardFile);
 	    chooser.setDialogTitle("Save show to XML-file...");
 	    int returnVal = chooser.showSaveDialog(chooser);
 	    if(returnVal == JFileChooser.APPROVE_OPTION) 
 	    {
 	    	location = chooser.getSelectedFile().getPath();
 		    
 	    	if(!location.endsWith(".xml"))
 	    		location += ".xml";
 	    	
 			File file = new File(location);
 			
 			//ask user when a file with that name already exists
 			int answer = JOptionPane.YES_OPTION;
 			if(file.exists())
 			{
 				
 				answer = JOptionPane.showOptionDialog(null, 
 						Lang.getString("XMLWriter.FileExists"),
 						"ted",
 		                JOptionPane.YES_NO_CANCEL_OPTION,
 		                JOptionPane.QUESTION_MESSAGE, null, Lang.getYesNoCancelLocale(), Lang.getYesNoCancelLocale()[0]);
 			}
 			
 			if(answer == JOptionPane.YES_OPTION)
 			{
 				
 				FileWriter fw;
 				try 
 				{
 					fw = new FileWriter(file);
 					String newline = System.getProperty("line.separator");
 					
 					fw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+newline);
 					fw.append("<ted>"+newline);
 					fw.append("<version>##</version>"+newline);
 					fw.append("<shows>"+newline);
 					
 					for(int i=0; i<series.size(); i++)
 					{
 						serie = (TedSerie)series.get(i);
 		
 						fw.append("\t<show>"+newline);
 						
 						/* write the name */
 						fw.append("\t\t<name>" + serie.getName() + "</name>"+newline);
 			
 						/* write the feeds */
 						feeds = serie.getFeeds();
 						fw.append("\t\t<feeds>"+newline);	
 						for(int j=0; j<feeds.size(); j++)
 						{
 							feed = (TedSerieFeed)feeds.get(j);
 							fw.append("\t\t\t<feed>" + escapeXML(feed.getUrl()) + "</feed>"+newline);
 						}
 						fw.append("\t\t</feeds>"+newline);
 						
 						/* write the rest of the show information */
 						//fw.append("\t\t<daily>False</daily>"+newline);
 						if(serie.getMinSize()!=0)
 							fw.append("\t\t<minimumsize>" + serie.getMinSize() + "</minimumsize>"+newline);
 						if(serie.getMaxSize()!=0)
 							fw.append("\t\t<maximumsize>" + serie.getMaxSize() + "</maximumsize>"+newline);
 						if(serie.getMinNumOfSeeders()!=0)
 							fw.append("\t\t<seeders>" + serie.getMinNumOfSeeders() + "</seeders>"+newline);
 						if(!serie.getKeywords().equals(""))
 							fw.append("\t\t<keywords>" + serie.getKeywords() + "</keywords>"+newline);
 						
 						/* write the episode schedule */
 						String[] wd = {"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};
 						boolean[] days = serie.getDays();
 						String releasedays = "";
 						
 						if(serie.isUseEpisodeSchedule())
 						{
 							for(int l=0; l<7; l++)
 							{
 								if(days[l])
 								{
 									if(releasedays.equals(""))
 										releasedays += wd[l];
 									else releasedays += "," + wd[l];
 								}
 							}
 						}
 						
 						if(!releasedays.equals(""))
 							fw.append("\t\t<releaseday>" + releasedays + "</releaseday>"+newline);
 												
 						/* write the break schedule */
 						if(serie.isUseBreakSchedule())
 						{
 							fw.append("\t\t<break>"+newline);
 							
 							if(serie.isUseBreakScheduleEpisode() && serie.getBreakEpisode()!=0)
 								fw.append("\t\t\t<episode>" + serie.getBreakEpisode() + "</episode>"+newline);
 							
 							if(serie.isUseBreakScheduleFrom())
 								fw.append("\t\t\t<from>" + 
 										CalendarToString(serie.getBreakFrom()) + "</from>"+newline);
 							
 							//until is always needed for the break schedule
 							fw.append("\t\t\t<until>" +
 										CalendarToString(serie.getBreakUntil()) + "</until>"+newline);
 							
 							fw.append("\t\t</break>"+newline);
 						}
 						
 						fw.append("\t</show>"+newline);
 					}	
 					
 					fw.append("</shows>"+newline);
 					fw.append("</ted>");
 					
 					fw.close();
 					
 					s = "Shows succesfully exported to " + location;
 					TedLog.debug(s);
 				}
 				catch (IOException e) 
 				{
 					s = "Exporting the shows to xml file has failed";
 					TedLog.error(e, s);
 					JOptionPane.showMessageDialog(null, s, null, 0);
 				}
 			}
 			else if(answer == JOptionPane.NO_OPTION)
 			{
 				this.writeXML(series);
 			}
 	    }
 	}
 	
 	private String CalendarToString(long date)
 	{
 		String dateString;
 		Calendar c = new GregorianCalendar();
 		c.setTimeInMillis(date);
 		
 		int k = c.get(Calendar.DAY_OF_MONTH);
 		
 		if(k<10)
 			dateString = "0" + k;
 		else
 			dateString = "" + k;
 		
 		k = c.get(Calendar.MONTH)+1;
 		
 		if(k<10)
 			dateString += "-0" + k;
 		else
			dateString += "00";
 		
 		k = c.get(Calendar.YEAR);
 		
 		dateString += "-" + k;
 		
 		return dateString;
 	}
 		
 	private String escapeXML(String s) 
 	{
 		StringBuffer str = new StringBuffer();
 		int len = (s != null) ? s.length() : 0;
 
 		for (int i=0; i<len; i++) 
 		{
 			char ch = s.charAt(i);
 			switch (ch) 
 			{
 				case '<': str.append("&lt;"); break;
 				case '>': str.append("&gt;"); break;
 				case '&': str.append("&amp;"); break;
 				case '"': str.append("&quot;"); break;
 				case '\'': str.append("&apos;"); break;
 				
 				default: str.append(ch);
 			}
 		}
 		return str.toString();
 	}
 }
