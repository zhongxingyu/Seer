 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package}.html;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.backends.gwt.GwtApplication;
 import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import ${package}.core.Game;
 
 public class HtmlDriver extends GwtApplication {
 
 	@Override
 	public void onModuleLoad() {
 		super.onModuleLoad();
 		Game.instance().setPostInit(new WebPageSetup());
 	}
 
 	@Override
	public ApplicationListener getApplicationListener() {
 		return Game.instance();
 	}
 
 	@Override
	public GwtApplicationConfiguration getConfig() {
 		return new GwtApplicationConfiguration(Game.WIDTH, Game.HEIGHT);
 	}
 
 	private static class WebPageSetup implements Runnable {
 		@Override
 		public void run() {
 			removeLoadingMessage();
 			setVersionString();
 			centreGameCanvas();
 		}
 
 		private static void removeLoadingMessage() {
 			final Element loadingElement = Document.get().getElementById("loading");
 			if (loadingElement != null) {
 				loadingElement.removeFromParent();
 			}
 		}
 
 		private static void setVersionString() {
 			final Element versionElement = Document.get().getElementById("version");
 			if (versionElement != null) {
 				versionElement.setInnerText(Game.instance().getVersion());
 			}
 		}
 
 		private static void centreGameCanvas() {
 			final Element tableElement = Document.get().getElementsByTagName("table").getItem(0);
 			if (tableElement != null) {
 				final String style = tableElement.getAttribute("style")
 						+ " position: absolute;"
 						+ " top: 50%; margin-top: -" + Game.HEIGHT / 2 + "px;"
 						+ " left: 50%; margin-left: -" + Game.WIDTH / 2 + "px;";
 				tableElement.setAttribute("style", style);
 			}
 			final Element bodyElement = Document.get().getElementsByTagName("body").getItem(0);
 			if (bodyElement != null) {
 				bodyElement.removeAttribute("style");
 				bodyElement.addClassName("loaded");
 			}
 		}
 	}
 
 }
