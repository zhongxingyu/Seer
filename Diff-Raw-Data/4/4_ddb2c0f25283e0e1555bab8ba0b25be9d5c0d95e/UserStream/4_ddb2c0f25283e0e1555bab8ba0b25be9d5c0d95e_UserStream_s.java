 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 /**
  * 
  */
 package cc.warlock.rcp.views;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 import cc.warlock.core.client.IStreamFilter;
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.WarlockString.WarlockStringStyleRange;
 import cc.warlock.core.client.internal.StreamFilter;
 
 /**
  * @author Will Robertson
  * UserStreams
  * ViewPart/Stream View Class that shows user configurable content filtered from the main window.
  */
 public class UserStream extends StreamView {
 	public static final String VIEW_ID = "cc.warlock.rcp.views.rightView.userStream";
 	protected static ArrayList<UserStream> openStreams = new ArrayList<UserStream>();
 	private IStreamFilter[] filters = null;
 	private String name = "Stream";
 	private ArrayList<String> styles;
 	
 	public void setFilters(IStreamFilter[] filters) {
 		this.filters = filters;
 	}
 	
 	@Override
 	protected synchronized void appendText (IWarlockClient client, WarlockString string)
 	{
 		// Process filters on the complete lines
 		WarlockString ret = new WarlockString();
 		bufferLoop: for (WarlockString buffer : string.split("\\r?\\n")) {
 			if(styles != null)
 				for(WarlockStringStyleRange style : buffer.getStyles()) {
 					String name = style.style.getName();
 					if(name != null && styles.contains(name)) {
 						ret.append(buffer);
 						ret.append("\n");
 						continue bufferLoop;
 					}
 				}
 			for (IStreamFilter filter : this.filters) {
 				if (filter == null) continue;
 				if (filter.match(buffer)) {
 					// If a filter matches, we go ahead and display the chunk
 					ret.append(buffer);
 					ret.append("\n");
 					continue bufferLoop;
 				}
 			}
 		}
 		if (ret.length() > 0) {
 			super.appendText(client, ret);
 		}
 	}
 	
 	public void clientConnected(IWarlockClient client) {	
 		super.clientConnected(client);
 		addStream(client.getDefaultStream());
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		String streamName = getViewSite().getSecondaryId().substring(getViewSite().getSecondaryId().lastIndexOf('.')+1);
 		setStreamTitled(false);
 		setName(streamName);
 		setStreamName(IWarlockClient.DEFAULT_STREAM_NAME);
 		super.createPartControl(parent);
 		//scanClients();
 		if (streamName.equals("Events")) {
 			this.filters = getEventsFilters();
 		} else if (streamName.equals("Conversations")) {
 			this.filters = getConversationsFilters();
 			this.styles = new ArrayList<String>();
 			styles.add("speech");
 			styles.add("whisper");
 		} else if (streamName.equals("Healing")) {
 			this.filters = getHealingFilters();
 		} else {
 			System.err.println("Not a stream name we recognize! ("+streamName+")");
 		}
 	}
 	
 	protected IStreamFilter[] getEventsFilters ()
 	{
 		ArrayList<IStreamFilter> filters = new ArrayList<IStreamFilter>();
 		filters.add(new StreamFilter("^You've gained a new rank in .+\\.", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("^Announcement: .+$", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("^System Announcement: .+$", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("^(Xibar|Katamba|Yavash) slowly rises above the horizon\\.", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("^(Xibar|Katamba|Yavash) sets, slowly dropping below the horizon\\.", IStreamFilter.type.regex));
 		
 		return filters.toArray(new IStreamFilter[filters.size()]);
 	}
 	
 	protected IStreamFilter[] getConversationsFilters ()
 	{
 		ArrayList<IStreamFilter> filters = new ArrayList<IStreamFilter>();
 		filters.add(new StreamFilter("\\bthoughts in your head\\b", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("^\\w+ (nod|lean|stretch|smile|yawn|chuckle|chortle|beam|hug|applaud|babble|blink|bow|cackle|cringe|cower|weep|mumble|wave|ponder|peers quizzically|snort|snuggle|cuddle|smirk|laugh|grumble|dance|grin)s?( (at|to|with) \\w+)?\\.$", IStreamFilter.type.regex));
		filters.add(new StreamFilter("^\\((?!You ).+\\)$", IStreamFilter.type.regex));	//act
		filters.add(new StreamFilter("^\\((?!You|Type|ASK|Roundtime ).+\\)$", IStreamFilter.type.regex));	// GM sends
 		filters.add(new StreamFilter("^(You belt out, .*|You hear \\w+ yell, .*|\\w+ yells, )$", IStreamFilter.type.regex));	// Yells
 		
 		
 
 		return filters.toArray(new IStreamFilter[filters.size()]);
 	}
 	
 	protected IStreamFilter[] getHealingFilters ()
 	{
 		ArrayList<IStreamFilter> filters = new ArrayList<IStreamFilter>();
 		filters.add(new StreamFilter("^You sense that .+\\.", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("^The bandages binding your .+\\.", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("^You don't have enough experience to transfer .+\\.", IStreamFilter.type.regex));
 		
 		return filters.toArray(new IStreamFilter[filters.size()]);
 	}
 
 	public static UserStream getViewForUserStream (String streamName) {
 		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		
 		for (UserStream view : openStreams)
 		{
 			if (view.getName().equals(streamName))
 			{
 				page.activate(view);
 				return view;
 			}
 		}
 		
 		// none of the already created views match, create a new one
 		try {
 			return (UserStream) page.showView(VIEW_ID , "rightFolder."+ streamName, IWorkbenchPage.VIEW_ACTIVATE);
 		} catch (PartInitException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 		this.setViewTitle(name);
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 
 	public UserStream() {
 		super();
 	}
 }
