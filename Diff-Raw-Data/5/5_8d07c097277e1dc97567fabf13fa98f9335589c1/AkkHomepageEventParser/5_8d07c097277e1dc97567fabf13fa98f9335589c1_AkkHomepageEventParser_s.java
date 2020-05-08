 package org.akk.akktuell.Model.downloader;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 
 import org.akk.akktuell.R;
 import org.akk.akktuell.Model.AkkEvent;
 import org.akk.akktuell.Model.AkkEvent.AkkEventType;
 import org.akk.akktuell.Model.InfoManager;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.Context;
 import android.text.Html;
 import android.util.Log;
 
 public class AkkHomepageEventParser implements Runnable, EventDownloader {
 	
 	private LinkedList<AkkEvent> eventsWaitingForDescription;
 	private LinkedList<AkkEvent> eventsWaitingForDBPush;
 	private ThreadGroup getDescThreads;
 	private Context context;
 	private boolean updateRequested = false;
 	private ArrayList<EventDownloadListener> listeners = new ArrayList<EventDownloadListener>();
 	private Thread mainThread;
 	private boolean allEventsParsed;
 	private String AkkHpAddr = "http://www.akk.org/chronologie.php";
 	
 	public AkkHomepageEventParser(Context ctx) {
 		mainThread = Thread.currentThread();
 
 		this.context = ctx;
 		this.eventsWaitingForDescription = new LinkedList<AkkEvent>();
 		this.eventsWaitingForDBPush = new LinkedList<AkkEvent>();
 		getDescThreads = new ThreadGroup("EventUpdateThreads");
 	}
 	
 	private String getAkkHpSource() throws IOException {
 		allEventsParsed = false;
 		//create DescriptionUpdateThreads
 
 		for (int i = 0; i < 3; i++) {
 			new Thread(getDescThreads, this).start();
 		}
 		
 		
 		HttpClient client = new DefaultHttpClient();
 		HttpGet request = new HttpGet(AkkHpAddr);
 		HttpResponse response = client.execute(request);
 
 		InputStream in = response.getEntity().getContent();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		StringBuilder str = new StringBuilder();
 		String line = null;
 		while((line = reader.readLine()) != null)
 		{
 		    str.append(line);
 		}
 		in.close();
 		return str.toString();
 	}
 	
 	private String getDescriptionSource(String link) throws IOException {
 		HttpClient client = new DefaultHttpClient();
 		HttpGet request = new HttpGet(link);
 		HttpResponse response = client.execute(request);
 
 		InputStream in = response.getEntity().getContent();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		StringBuilder str = new StringBuilder();
 		String line = null;
 		while((line = reader.readLine()) != null)
 		{
 		    str.append(line);
 		}
 		in.close();
 		return str.toString();
 	}
 	
 	@Override
 	public AkkEvent[] updateEvents() {
 		if (!this.updateRequested) {
 			this.updateRequested = true;
 			String htmlSource;
 			try {
 				htmlSource = getAkkHpSource();
 			} catch (IOException e) {
 				Log.d("AkkHomepageParser", "error while downloading akk source code");
 				e.printStackTrace();
 				return null;
 			}
 			
 			LinkedList<String> singleEventhtmlSource = getSingleEventSources(htmlSource);
 			
 			if (singleEventhtmlSource.size() < 3) {
 				//this is not a correct version of the akk homepage
 				return null;
 			}
 			
 			//remove first part
 			singleEventhtmlSource.removeFirst();
 			
 			//save last part for last_modified string
 			String htmlContainingLastModified = singleEventhtmlSource.getLast();
 			singleEventhtmlSource.removeLast();
 			
 			
 			//remove part after "</TD></TR>"
 			for (int i = 0; i < singleEventhtmlSource.size(); i++) {
 				String currentLine = singleEventhtmlSource.get(i).split("</TD></TR>")[0];
 				singleEventhtmlSource.remove(i);
 				singleEventhtmlSource.add(i, currentLine);
 				//equivalent to string.matches but faster for multiple operations
 				
 				
 			}
 			 
 			
 			//produce events from the lines
 			AkkEvent newAkkEvent;
 			String newAkkEventName;
 			String newAkkEventPlace;
 			Boolean hasDescription;
 			GregorianCalendar newAkkEventDate;
 			AkkEvent.AkkEventType newAkkEventType;
 			for (String currentEventString : singleEventhtmlSource) {
 				
 				//check event type
 				if (currentEventString.contains("Veranstaltungshinweis")) {
 					newAkkEventType = AkkEventType.Veranstaltungshinweis;
 				} else if (currentEventString.contains("Sonderveranstaltung")) {
 					newAkkEventType = AkkEventType.Sonderveranstaltung;
 				} else if (currentEventString.contains("Workshop")) {
 					newAkkEventType = AkkEventType.Workshop;
 				} else if (currentEventString.contains("Schlonz") || currentEventString.contains("Liveschlonz")) {
 					newAkkEventType = AkkEventType.Schlonz;
 				} else {
 					newAkkEventType = AkkEventType.Tanzen;
 				}
 				
 				//get eventDate
 				newAkkEventDate = getEventDateFromString(currentEventString.substring(0,11));
 				
 				//parse schlonze
 				
 				/*
 				 * example source String:
 				 * 	<TR><TD>Do. 19. Apr.</TD><TD>20<SPAN class="min-alt">:</SPAN><SPAN class="min">00</SPAN> Uhr</TD><TD>
 		        	<A HREF="/schlonze/schlonz.php?Kochduell">Kochduell Schlonz</A></TD><TD><A HREF="/adresse.php">Altes Stadion</A></TD></TR>
 				 
 				 *
 				 *ohne desc:
 				 *
 				 *<TR><TD>Di. 10. Jul.</TD><TD>20<SPAN class="min-alt">:</SPAN><SPAN class="min">00</SPAN> Uhr</TD><TD>
 		        	Reggae-Ska-Punk-Trash Schlonz</TD><TD><A HREF="/adresse.php">Altes Stadion</A></TD></TR>
 	
 				 */
 				if (newAkkEventType == AkkEventType.Schlonz) {
 					if (currentEventString.contains("HREF=\"/schlonze")) {
 						hasDescription = true;
 					} else {
 						hasDescription = false;
 					}
 					
 					
 					
 					try {
 						if (hasDescription) {
 							String source = currentEventString.split("/schlonze")[1];
 							newAkkEventName = source.split("\">")[1];
 							newAkkEventName = newAkkEventName.split("<")[0];
 							newAkkEventName = Html.fromHtml(newAkkEventName).toString();
 							
 							newAkkEventPlace = source.split("adresse.php\">")[1];
 							newAkkEventPlace = newAkkEventPlace.split("<")[0];
 						
 							newAkkEvent = new AkkEvent(newAkkEventName, newAkkEventDate, newAkkEventPlace);
 							newAkkEvent.setDescription(currentEventString);
 							newAkkEvent.setType(AkkEventType.Schlonz);
 							this.addElementToWaitingList(newAkkEvent);
 							synchronized (this) {
 								notify();
 							}
 						} else {
 							String source = currentEventString.split("</SPAN>")[2];
 							newAkkEventName = source.split("</TD><TD>")[1];
 							newAkkEventName = Html.fromHtml(newAkkEventName).toString();
 						
 							newAkkEventPlace = source.split("adresse.php\">")[1];
 							newAkkEventPlace = newAkkEventPlace.split("<")[0];
 						
 							newAkkEvent = new AkkEvent(newAkkEventName, newAkkEventDate, newAkkEventPlace);
 							newAkkEvent.setDescription(context.getResources().getString(R.string.hello));
 							newAkkEvent.setType(AkkEventType.Schlonz);
 						}
 						
 						
 					} catch (ArrayIndexOutOfBoundsException e) {
 						Log.d("HPParser", "Seems this is not a normal String: " + currentEventString);
 						e.printStackTrace();
 					}
 				} else if (newAkkEventType == AkkEventType.Veranstaltungshinweis) {
 					/*example source
 					 * Mo. 16. Apr.</TD><TD></TD><TD>	Veranstaltungshinweis: Rektor: Vorlesungsbeginn</TD><TD><A HREF="http://www.uni-karlsruhe.de/info/campusplan/">Campus</A>
 					 * 
 					 * 
 					 * Fr. 6. Jul.</TD><TD>15<SPAN class="min-alt">:</SPAN><SPAN class="min">00</SPAN> Uhr</TD><TD>
 	        			<A HREF="http://www.z10.info/">Veranstaltungshinweis: Z10: Sommerfest</A></TD><TD><A HREF="http://www.z10.info/?topic">Z10</A></TD></TR>
 					 
 					 *	Sa. 21. Apr.</TD><TD>20<SPAN class="min-alt">:</SPAN><SPAN class="min">00</SPAN> Uhr</TD><TD>	<A HREF="http://www.z10.info/">Veranstaltungshinweis: Z10: Konzert - Montreal, Liedfett, Ill</A></TD><TD><A HREF="http://www.z10.info/?topic">Z10</A>
 					 */
 					
 					if (currentEventString.contains("Rektor")) {
 						
 					} else {
 						if (currentEventString.endsWith("</A>")) {
 							String source = currentEventString.split("\">")[3];
 							newAkkEventName = source.split("</A>")[0];
 							newAkkEventName = Html.fromHtml(newAkkEventName).toString();
 							
							newAkkEventPlace = currentEventString.split("\">")[4];
							newAkkEventPlace = newAkkEventPlace.substring(0, newAkkEventPlace.length()-3);
 						} else {
 							newAkkEventName = currentEventString.split("</TD><TD>")[2];;
 							newAkkEventName = Html.fromHtml(newAkkEventName).toString();
 							
 							newAkkEventPlace = currentEventString.split("</TD><TD>")[3];
 						}
 						if (newAkkEventName.startsWith("\t")) {
 							newAkkEventName = newAkkEventName.substring(1, newAkkEventName.length() -1);
 						}
 						newAkkEvent = new AkkEvent(newAkkEventName, newAkkEventDate, newAkkEventPlace);
 						newAkkEvent.setDescription(context.getResources().getString(R.string.hello));
 						newAkkEvent.setType(AkkEventType.Veranstaltungshinweis);
 						this.addElementToDBPushList(newAkkEvent);
 					}
 				}	
 			}
 			allEventsParsed = true;
 			while(elementsWaitingForDesc() || getDescThreads.activeCount() > 0) {
 				synchronized (this) {
 					try {
 						wait(100);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			if (this.eventsWaitingForDBPush.size() == 0) {
 				return null;
 			}
 			
 			AkkEvent[] result = new AkkEvent[this.eventsWaitingForDBPush.size()];
 			for (int i = 0; i < eventsWaitingForDBPush.size(); i++) {
 				result[i] = eventsWaitingForDBPush.get(i);
 			}
 			notifyOnDownloadFinished(result);
 			return result;
 		}
 		return null;
 	}
 
 	private GregorianCalendar getEventDateFromString(String substring) {
 		GregorianCalendar calendar = new GregorianCalendar();
 		if (substring.contains("Jan")) {
 			calendar.set(GregorianCalendar.MONTH, 0);
 		} else if (substring.contains("Feb")) {
 			calendar.set(GregorianCalendar.MONTH, 1);
 		} else if (substring.contains("Mar")) {
 			calendar.set(GregorianCalendar.MONTH, 2);
 		} else if (substring.contains("Apr")) {
 			calendar.set(GregorianCalendar.MONTH, 3);
 		} else if (substring.contains("Mai")) {
 			calendar.set(GregorianCalendar.MONTH, 4);
 		} else if (substring.contains("Jun")) {
 			calendar.set(GregorianCalendar.MONTH, 5);
 		} else if (substring.contains("Jul")) {
 			calendar.set(GregorianCalendar.MONTH, 6);
 		} else if (substring.contains("Aug")) {
 			calendar.set(GregorianCalendar.MONTH, 7);
 		} else if (substring.contains("Sep")) {
 			calendar.set(GregorianCalendar.MONTH, 8);
 		} else if (substring.contains("Okt")) {
 			calendar.set(GregorianCalendar.MONTH, 9);
 		} else if (substring.contains("Nov")) {
 			calendar.set(GregorianCalendar.MONTH, 10);
 		} else if (substring.contains("Dez")) {
 			calendar.set(GregorianCalendar.MONTH, 11);
 		} else {
 			Log.d("Halt", "STOP!");
 		}
 		return calendar;
 	}
 
 	private LinkedList<String> getSingleEventSources(String htmlSource) {
 		LinkedList<String> returnStrings = new LinkedList<String>();
 		String[] eventSourceSequence = htmlSource.split("<TR><TD>");
 		for (String s : eventSourceSequence) {
 			returnStrings.addLast(s);
 		}
 		return returnStrings;
 	}
 	
 	
 	public void run() {
 		AkkEvent event = null;
 		while (true) {
 			if (this.elementsWaitingForDesc()) {
 				synchronized (this) {
 					if (this.elementsWaitingForDesc()) {
 						event = this.popElementFromwaitingList();
 					}
 				}
 				if (event != null) {
 					addDescriptionToEvent(event);
 					this.addElementToDBPushList(event);
 				}
 			} else {
 				synchronized (mainThread) {
 					if (mainThread.getState() == Thread.State.WAITING) {
 						mainThread.notify();
 					}
 				}
 				if (!allEventsParsed) {
 					synchronized (this) {
 						try {
 							wait();
 						} catch (InterruptedException e) {
 							Log.d("AkkHomepageParse", "Thread got InterrupterException.");
 						} 
 					}
 				} else {
 					break;
 				}
 			}
 		}	
 	}
 
 	/**
 	 * Notifies the attached listeners when a download has started.
 	 */
 	private void notifyOnDownloadStarted() {
 		for (EventDownloadListener l : this.listeners) {
 			l.downloadStarted();
 		}
 	}
 
 	/**
 	 * Notifies the attached listeners when the download has finished
 	 * and returns the downloaded {@link AkkEvent AkkEvents}.
 	 * @param events the downloaded events
 	 */
 	private void notifyOnDownloadFinished(AkkEvent[] events) {
 		for (EventDownloadListener l : this.listeners) {
 			l.downloadFinished(events);
 		}
 	}
 	
 	private void addDescriptionToEvent(AkkEvent event) {
 		String eventSource = event.getEventDescription();
 		eventSource = eventSource.split("<A HREF=\"")[1];
 		String eventDescriptionSource = eventSource.split("\">")[0];
 		eventDescriptionSource = "http://www.akk.org" + eventDescriptionSource;
 		try {
 			eventDescriptionSource = getDescriptionSource(eventDescriptionSource);
 			String eventDescription = eventDescriptionSource.split("<P>")[1];
 			eventDescription = Html.fromHtml(eventDescription.split("</P>")[0]).toString();
 			event.setDescription(eventDescription);
 		} catch (IOException e) {
 			Log.d("HPParser", "Could not get event Description...");
 			e.printStackTrace();
 			event.setDescription("Error fetching Description");
 			return;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			Log.d("HPParser", "Could not get event Description...");
 			e.printStackTrace();
 			event.setDescription("Error fetching Description");
 			return;
 		}
 	}
 
 	private boolean elementsWaitingForDBPush() {
 		synchronized (this) {
 			return !this.eventsWaitingForDBPush.isEmpty();
 		}
 	}
 
 	private boolean elementsWaitingForDesc() {
 		synchronized (this) {
 			return !this.eventsWaitingForDescription.isEmpty();
 		}
 	}
 
 	private void addElementToWaitingList(AkkEvent event) {
 		synchronized (this) {
 			this.eventsWaitingForDescription.addLast(event);
 		}
 	}
 	
 	private AkkEvent popElementFromwaitingList() {
 		AkkEvent event;
 		synchronized(this) {
 			 event = this.eventsWaitingForDescription.getFirst();
 			this.eventsWaitingForDescription.removeFirst();
 		}
 		return event;
 	}
 	
 	private void addElementToDBPushList(AkkEvent event) {
 		synchronized (this) {
 			this.eventsWaitingForDBPush.addLast(event);
 		}
 	}
 	
 	private AkkEvent popElementFromDBPushList() {
 		AkkEvent event;
 		synchronized (this) {
 			event = this.eventsWaitingForDBPush.getFirst();
 			this.eventsWaitingForDBPush.removeFirst();
 		}
 		return event;
 	}
 
 	@Override
 	public boolean isUpdating() {
 		return this.updateRequested;
 	}
 
 	@Override
 	public void setUrl(String url) {
 		this.AkkHpAddr = url;
 	}
 	
 
 	public void addEventDownloadListener(InfoManager infoManager) {
 		listeners.add(infoManager);
 	}
 
 }
