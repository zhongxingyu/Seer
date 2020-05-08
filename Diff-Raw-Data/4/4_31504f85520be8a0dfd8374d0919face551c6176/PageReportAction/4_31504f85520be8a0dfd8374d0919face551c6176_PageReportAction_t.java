 package crussell52.poi.actions;
 
 import java.util.ArrayList;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 
import crussell52.poi.Config;
 import crussell52.poi.PagedPoiList;
 import crussell52.poi.PoiManager;
 
 public class PageReportAction extends ActionHandler {
 	
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @param poiManager
 	 */
 	public PageReportAction(PoiManager poiManager) {
 		super(poiManager);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void handleAction(CommandSender sender, String action, String[] args) {
 		// need a player to take this action
 		if (!this._playerCheck(sender)) {
 			return;
 		}
 		
 		// make sure we don't have extra arguments
 		if (args.length > 1) {
 			this._actionUsageError(sender, "Too much info! This action only accepts a page number.", action);
 			return;
 		}
 		
 		// make sure this player has a result set in the current world.
 		PagedPoiList results = this._poiManager.getRecentResults((Player)sender);
 		if (results == null) {
 			sender.sendMessage("You do not have any recent results in this World.");
 			return;
 		}
 		
 		// try to handle the first argument as a page number
 		try {
 			int pageNum = Integer.parseInt(args[0]);
 			
 			// set the new page
 			if (!results.setPage(pageNum)) {
 				sender.sendMessage("\u00a74Can't display page \u00a7e" + pageNum + "\u00a74...");
 				sender.sendMessage("\u00a74There are only \u00a7e" + results.getNumPages() + "\u00a74 page(s) available.");
 				return;
 			}
 		}
 		catch (IndexOutOfBoundsException ex) {
 			// let this condition slide, we'll use the current page.
 		}
 		catch (NumberFormatException ex) {
 			// didn't get a number... check for special navigation values.
 			if (args[0].equals(">")) {
 				results.nextPage();
 			}
 			else if (args[0].equals(">>")) {
 				results.lastPage();
 			}
 			else if (args[0].equals("<")) {
 				results.previousPage();
 			}
 			else if (args[0].equals("<<")) {
 				results.firstPage();
 			}
 			else {
 				sender.sendMessage("Invalid page indicator, expecting: a number, >, >>, <, or <<");
 				return;
 			}
 		}
 		
 		// if we made it this far, we can show the report.
 		ArrayList<String> report;
 		if (results.getListType() == PagedPoiList.TYPE_AREA_SEARCH) {
			report = results.getPageReport(((Player)sender).getLocation(), Config.getDistanceThreshold());
 		}
 		else {
 			report = results.getPageReport();
 		}
 		
 		// send the report to the command sender
 		sender.sendMessage("");
 		for (String message : report) {
 			sender.sendMessage(message);
 		}
 	}
 
 }
