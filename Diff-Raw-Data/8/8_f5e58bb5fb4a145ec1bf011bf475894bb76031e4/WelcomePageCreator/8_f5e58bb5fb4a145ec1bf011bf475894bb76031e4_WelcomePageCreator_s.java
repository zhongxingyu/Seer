 package collector.desktop.gui.browser;
 
 import collector.desktop.Collector;
 import collector.desktop.filesystem.FileSystemAccessWrapper;
 import collector.desktop.gui.GuiConstants;
 import collector.desktop.gui.managers.AlbumManager;
 import collector.desktop.gui.managers.WelcomePageManager;
 import collector.desktop.internationalization.DictKeys;
 import collector.desktop.internationalization.Translator;
 
 public class WelcomePageCreator {
 	private static final int NUMBER_OF_FAVORITES_SHOWN = 5;
 
 	static void loadWelcomePage() {
 		Collector.getAlbumItemSWTBrowser().setText(
 				"<!DOCTYPE HTML>" +
 				"<html>" +
 				"  <head>" +
 				"    <title>" + Translator.get(DictKeys.TITLE_MAIN_WINDOW) + "</title>" +
 				"    <meta " + GuiConstants.META_PARAMS + ">" + 
 				"    <link rel=stylesheet href=\"" + GuiConstants.STYLE_CSS + "\" />" +
 				"    <script src=\"" + GuiConstants.EFFECTS_JS + "\"></script>" +
 				"  </head>" +
 				"  <body>" +
 				"    <font face=\"" + Utilities.getDefaultSystemFont() + "\">" +
 				"	 <br>" +
 				"	 <table>" +
 				"      <tr>" +
 				"	     <td align=\"center\">" +
				"          <img height=\"466\" src=\" " + FileSystemAccessWrapper.LOGO + " \">" +
 				"        </td>" +
 				"        <td width=\"430px\">" +
		        "          <div style=\"padding:10px; background-color:#" + Utilities.getBackgroundColorOfWidgetInHex() + "\">" +
 						     generateAlbumInformation() +
 						     generateFavorites() +
 			    "          </div>" +
 				"        </td>" +
 				"      </tr>" +
 				"	 </table>" +
 				"    </font>" +
 				"  </body>" +
 				"</html>");
 	}
 	
 	private static String generateAlbumInformation() {
 		StringBuilder albumInformationBuilder = new StringBuilder("<h4>" + Translator.get(DictKeys.BROWSER_ALBUM_INFORMATION) + "</h4><ul>");
 
 		if (AlbumManager.getInstance().getAlbums().isEmpty()) {
 			albumInformationBuilder.append("<li>" + 
 										      Translator.get(DictKeys.BROWSER_NO_INFORMATION_AVAILABLE) + 
 										   "</li>");
 		} else {
 			for (String album : AlbumManager.getInstance().getAlbums()) {
 				albumInformationBuilder.append("<li>" +
 											   "  Album <b>" + album + "</b><br>" +
 											   "  <font size=-1>" +
											   "  <i>(" + 
 													Translator.get(DictKeys.BROWSER_NUMBER_OF_ITEMS_AND_LAST_UPDATED, 
 															WelcomePageManager.getInstance().getNumberOfItemsInAlbum(album),
 															WelcomePageManager.getInstance().getLastModifiedDate(album)) +
 											   "  </i>" +
 											   "  </font>" +
 											   "</li>");
 			}
 		}
 
 		return albumInformationBuilder.append("</ul>").toString();
 	}
 
 	private static String generateFavorites() {
 		StringBuilder favoriteBuilder = new StringBuilder("<h4>" + Translator.get(DictKeys.BROWSER_FAVORITE_ALBUMS_AND_VIEWS) + "</h4><ol>");
 
 		int favCounter = 0;
 		if (WelcomePageManager.getInstance().getAlbumAndViewsSortedByClicks().keySet().isEmpty()) {
 			favoriteBuilder.append("<li>" + 
 									  Translator.get(DictKeys.BROWSER_NO_INFORMATION_AVAILABLE) + 
 								   "</li>");
 		} else {
 			for (String albumOrViewName : WelcomePageManager.getInstance().getAlbumAndViewsSortedByClicks().keySet()) {
 				favoriteBuilder.append("<li>" +
 										  albumOrViewName + 
 										  "<font size=-1>" +
 										    "<i>" + 
 										      Translator.get(DictKeys.BROWSER_CLICKS_FOR_ALBUM, 
 										  			WelcomePageManager.getInstance().getAlbumAndViewsSortedByClicks().get(albumOrViewName)) + 
 										    "</i>" +
 										  "</font>" +
 										"</li>");
 	
 				if (++favCounter == NUMBER_OF_FAVORITES_SHOWN) {
 					break;
 				}
 			}
 		}
 
 		return favoriteBuilder.append("</ol>").toString();	
 	}
 }
