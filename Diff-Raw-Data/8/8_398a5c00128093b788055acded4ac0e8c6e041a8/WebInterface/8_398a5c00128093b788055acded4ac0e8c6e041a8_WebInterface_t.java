 /*
  * WoTNS - WebInterface.java - Copyright © 2011 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.wotns.ui.web;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.pterodactylus.util.template.ClassPathTemplateProvider;
 import net.pterodactylus.util.template.CollectionSortFilter;
 import net.pterodactylus.util.template.HtmlFilter;
 import net.pterodactylus.util.template.ReflectionAccessor;
 import net.pterodactylus.util.template.Template;
 import net.pterodactylus.util.template.TemplateContextFactory;
 import net.pterodactylus.util.template.TemplateParser;
 import net.pterodactylus.util.web.StaticPage;
 import net.pterodactylus.wotns.freenet.wot.Identity;
 import net.pterodactylus.wotns.main.IdentityComparator;
 import net.pterodactylus.wotns.main.WoTNSPlugin;
 import net.pterodactylus.wotns.template.HttpRequestAccessor;
 import net.pterodactylus.wotns.template.IdentityAccessor;
 import net.pterodactylus.wotns.web.FreenetRequest;
 import net.pterodactylus.wotns.web.PageToadlet;
 import net.pterodactylus.wotns.web.PageToadletFactory;
 import freenet.clients.http.ToadletContainer;
 import freenet.support.api.HTTPRequest;
 
 /**
  * TODO
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class WebInterface {
 
 	private final WoTNSPlugin wotNSPlugin;
 
 	private final TemplateContextFactory templateContextFactory = new TemplateContextFactory();
 
 	/** The registered toadlets. */
 	private final List<PageToadlet> pageToadlets = new ArrayList<PageToadlet>();
 
 	public WebInterface(WoTNSPlugin woTNSPlugin) {
 		this.wotNSPlugin = woTNSPlugin;
 
 		templateContextFactory.addAccessor(Object.class, new ReflectionAccessor());
 		templateContextFactory.addAccessor(Identity.class, new IdentityAccessor());
 		templateContextFactory.addAccessor(HTTPRequest.class, new HttpRequestAccessor());
 		templateContextFactory.addFilter("html", new HtmlFilter());
 		CollectionSortFilter sortFilter = new CollectionSortFilter();
 		sortFilter.addComparator(Identity.class, IdentityComparator.NAME);
 		templateContextFactory.addFilter("sort", sortFilter);
 		templateContextFactory.addProvider(new ClassPathTemplateProvider(WebInterface.class));
 	}
 
 	//
 	// ACCESSORS
 	//
 
 	public WoTNSPlugin getWoTNSPlugin() {
 		return wotNSPlugin;
 	}
 
 	public TemplateContextFactory getTemplateContextFactory() {
 		return templateContextFactory;
 	}
 
 	//
 	// ACTIONS
 	//
 
 	public void start() {
 		registerToadlets();
 	}
 
 	public void stop() {
 		unregisterToadlets();
 	}
 
 	//
 	// PRIVATE METHODS
 	//
 
 	private void registerToadlets() {
 		Template indexTemplate = TemplateParser.parse(createReader("/templates/index.html"));
 		Template unknownTemplate = TemplateParser.parse(createReader("/templates/unknown.html"));
 		Template manageTemplate = TemplateParser.parse(createReader("/templates/manage.html"));
 		Template addTargetTemplate = TemplateParser.parse(createReader("/templates/addTarget.html"));
 
 		PageToadletFactory pageToadletFactory = new PageToadletFactory(wotNSPlugin.getHighLevelSimpleClient(), "/tns/");
 		pageToadlets.add(pageToadletFactory.createPageToadlet(new ResolverPage(unknownTemplate, this, wotNSPlugin.getResolver())));
 		pageToadlets.add(pageToadletFactory.createPageToadlet(new IndexPage(indexTemplate, this), "Index"));
 		pageToadlets.add(pageToadletFactory.createPageToadlet(new ManagePage(manageTemplate, this)));
 		pageToadlets.add(pageToadletFactory.createPageToadlet(new EnableIdentityPage(new Template(), this)));
 		pageToadlets.add(pageToadletFactory.createPageToadlet(new AddTargetPage(addTargetTemplate, this)));
 		pageToadlets.add(pageToadletFactory.createPageToadlet(new EditTargetPage(new Template(), this)));
 		pageToadlets.add(pageToadletFactory.createPageToadlet(new StaticPage<FreenetRequest>("css/", "/static/css/", "text/css")));
 
 		ToadletContainer toadletContainer = wotNSPlugin.getToadletContainer();
		toadletContainer.getPageMaker().addNavigationCategory("/tns/index.html", "Navigation.Menu.WoTNS.Name", "Navigation.Menu.WoTNS.Tooltip", wotNSPlugin);
 		for (PageToadlet toadlet : pageToadlets) {
 			String menuName = toadlet.getMenuName();
 			if (menuName != null) {
				toadletContainer.register(toadlet, "Navigation.Menu.WoTNS.Name", toadlet.path(), true, "Navigation.Menu.WoTNS.Item." + menuName + ".Name", "Navigation.Menu.WoTNS.Item." + menuName + ".Tooltip", false, toadlet);
 			} else {
 				toadletContainer.register(toadlet, null, toadlet.path(), true, false);
 			}
 		}
 	}
 
 	/**
 	 * Unregisters all toadlets.
 	 */
 	private void unregisterToadlets() {
 		ToadletContainer toadletContainer = wotNSPlugin.getToadletContainer();
 		for (PageToadlet pageToadlet : pageToadlets) {
 			toadletContainer.unregister(pageToadlet);
 		}
		toadletContainer.getPageMaker().removeNavigationCategory("Navigation.Menu.WoTNS.Name");
 	}
 
 	/**
 	 * Creates a {@link Reader} from the {@link InputStream} for the resource
 	 * with the given name.
 	 *
 	 * @param resourceName
 	 *            The name of the resource
 	 * @return A {@link Reader} for the resource
 	 */
 	private Reader createReader(String resourceName) {
 		try {
 			return new InputStreamReader(getClass().getResourceAsStream(resourceName), "UTF-8");
 		} catch (UnsupportedEncodingException uee1) {
 			return null;
 		}
 	}
 
 }
