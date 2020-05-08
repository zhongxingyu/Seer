 /**
  * 
  */
 package cc.warlock.rcp.userstreams.ui.menu;
 
 import java.util.ArrayList;
 
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.ui.actions.CompoundContributionItem;
 
 import cc.warlock.rcp.userstreams.IStreamFilter;
 import cc.warlock.rcp.userstreams.internal.StreamFilter;
 import cc.warlock.rcp.userstreams.ui.actions.StreamShowAction;
 
 /**
  * @author Will Robertson
  * Streams Menu Contribution - Adds all menu items to preferences.
  */
 public class StreamsContributionItem extends CompoundContributionItem  {
 
 	protected ArrayList<IStreamFilter> getEventsFilters ()
 	{
 		ArrayList<IStreamFilter> filters = new ArrayList<IStreamFilter>();
 		filters.add(new StreamFilter("^You've gained a new rank in .+\\.", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("^Announcement: .+$", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("^System Announcement: .+$", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("^(Xibar|Katamba|Yavash) slowly rises above the horizon\\.", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("^(Xibar|Katamba|Yavash) sets, slowly dropping below the horizon\\.", IStreamFilter.type.regex));
 		
 		return filters;
 	}
 	
 	protected ArrayList<IStreamFilter> getConversationsFilters ()
 	{
 		ArrayList<IStreamFilter> filters = new ArrayList<IStreamFilter>();
 		// (say|ask|exclaim|whisper)
 		filters.add(new StreamFilter("^\\w+ \\w+( (at|to) \\w+)?, \".+\"$", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("thoughts in your head", IStreamFilter.type.string));
 		filters.add(new StreamFilter("^\\w+ (nod|lean|stretch|smile|yawn|chuckle|chortle|grin|beam|hug|applaud|babble|blink|bow|cackle|cringe|cower|weep|mumble|wave|ponder|peers quizzically|snort|snuggle|cuddle|smirk|laugh|jumps back from|nods? slightly|whistles? a merry tune.)", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("accusatory", IStreamFilter.type.string));
 		filters.add(new StreamFilter("flush", IStreamFilter.type.string));
 		filters.add(new StreamFilter("\"Boo\"", IStreamFilter.type.string));
 		filters.add(new StreamFilter("weep", IStreamFilter.type.string));
 		filters.add(new StreamFilter("mumble", IStreamFilter.type.string));
 		filters.add(new StreamFilter("dance", IStreamFilter.type.string));
		filters.add(new StreamFilter("tickle", IStreamFilter.type.string));
		filters.add(new StreamFilter("^\\(.+\\)$", IStreamFilter.type.regex));
 		
 		return filters;
 	}
 
 	protected IContributionItem createStreamContributionItem (String name, ArrayList<IStreamFilter> filters)
 	{
 		return new ActionContributionItem(new StreamShowAction(name, filters.toArray(new IStreamFilter[filters.size()])));
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
 	 */
 	@Override
 	protected IContributionItem[] getContributionItems() {
 		// Add Menu Items
 		ArrayList<IContributionItem> items = new ArrayList<IContributionItem>();
 		items.add(createStreamContributionItem("Events", getEventsFilters()));
 		items.add(createStreamContributionItem("Conversations", getConversationsFilters()));
 		
 		return items.toArray(new IContributionItem[items.size()]); 
 	}
 }
