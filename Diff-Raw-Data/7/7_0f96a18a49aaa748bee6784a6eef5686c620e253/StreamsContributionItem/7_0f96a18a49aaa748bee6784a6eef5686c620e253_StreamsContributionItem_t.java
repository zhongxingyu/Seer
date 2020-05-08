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
		filters.add(new StreamFilter("^\\w+ (\\bnod|\\blean|\\bstretch|\\bsmile|\\byawn|\\bchuckle|\\bchortle|\\bbeam|\\bhug|\\bapplaud|\\bbabble|\\bblink|\\bbow|\\bcackle|\\bcringe|\\bcower|\\bweep|\\bmumble|\\bwave|\\bponder|\\bpeers quizzically|\\bsnort|\\bsnuggle|\\bcuddle|\\bsmirk|\\blaugh|\\bjumps back from|\\bwhistles? a merry tune.)", IStreamFilter.type.regex));
 		filters.add(new StreamFilter("accusatory", IStreamFilter.type.string));
 		filters.add(new StreamFilter("flush", IStreamFilter.type.string));
 		filters.add(new StreamFilter("\"Boo\"", IStreamFilter.type.string));
		// filters.add(new StreamFilter("dance", IStreamFilter.type.string));
 		filters.add(new StreamFilter("^\\((?!You ).+\\)$", IStreamFilter.type.regex));	//act
 		filters.add(new StreamFilter("^(You tickle |As you reach out to tickle ).+$", IStreamFilter.type.regex));	// tickle: 1st person
 		filters.add(new StreamFilter("^\\w+ just tickled you", IStreamFilter.type.regex));							// tickle: 2nd person
 		filters.add(new StreamFilter("^\\w+ just tickled (?!you).+$", IStreamFilter.type.regex));					// tickle: 3rd person
 		filters.add(new StreamFilter("^(You hug |.+to avoid your hug.|You try to give \\w+ a hug, but).*$", IStreamFilter.type.regex));	// hug: 1st person
 		filters.add(new StreamFilter("^\\w+ hugs you.+$", IStreamFilter.type.regex));													// hug: 2nd person
 		filters.add(new StreamFilter("^\\w+ (just hugged |hugs )(?!you).+$", IStreamFilter.type.regex));								// hug: 3rd person
 		filters.add(new StreamFilter("^You\\b (?:.(?!\\bat\\b))*?\\bgrin\\b(?:.(?!\\bat\\b))*?", IStreamFilter.type.regex));													// grin: 1st person, no target
 		filters.add(new StreamFilter("^(?!You )\\w+ (?!\\bgives\\b)(?:.(?!\\bat\\b))*?\\bgrin(?:.(?!\\bat\\b))*?", IStreamFilter.type.regex));									// grin: 3rd person, no target
 		filters.add(new StreamFilter("^\\bYou\\b.*\\bgrin\\b.*\\bat\\b.+$", IStreamFilter.type.regex));																			// grin: 1st person
 		filters.add(new StreamFilter("^.+(\\bgrin.*\\byou\\b|\\byou\\b.*grin).*$", IStreamFilter.type.regex));																	// grin: 2nd person
 		filters.add(new StreamFilter("^(?!You ).+(\\bgrin.*\\bat (?!\\byou\\b)|\\bat (?!\\byou\\b).*\\bgrin|\\bgives (?!\\byou\\b).*\\bgrin).*$", IStreamFilter.type.regex));	// grin: 3rd person
 		
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
