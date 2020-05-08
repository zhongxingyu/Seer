 package com.slashserver.dummydata;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 import org.hibernate.Criteria;
 import org.hibernate.FetchMode;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.stereotype.Repository;
 
 import com.slashserver.model.Page;
 import com.slashserver.model.PageSnippet;
 import com.slashserver.model.PageTemplate;
 import com.slashserver.model.Role;
 import com.slashserver.model.Sitemap;
 import com.slashserver.model.SitemapPage;
 import com.slashserver.model.Snippet;
 import com.slashserver.model.SnippetGroup;
 import com.slashserver.model.User;
 import com.slashserver.model.UserData;
 import com.slashserver.model.UserRole;
 import com.slashserver.persistence.PageTemplateStore;
 import com.slashserver.persistence.RoleStore;
 import com.slashserver.persistence.SitemapStore;
 import com.slashserver.persistence.SnippetStore;
 import com.slashserver.persistence.UserStore;
 
 @Repository
 @Transactional
 public class DummyDataHelper {
 
 	private SessionFactory sessionFactory;
 	private SnippetStore snippetStore;
 	private RoleStore roleStore;
 	private SitemapStore sitemapStore;
 	private UserStore userStore;
 	private PageTemplateStore pageTemplateStore;
 
 	@Autowired
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 
 	@Transactional
 	public void clear(){
 
 		Session currentSession = sessionFactory.getCurrentSession();
 		currentSession.createQuery("delete from UserData").executeUpdate();
 		currentSession.createQuery("delete from PageSnippet").executeUpdate();
 		currentSession.createQuery("delete from Snippet").executeUpdate();
 		currentSession.createQuery("delete from SnippetGroup").executeUpdate();
 		currentSession.createQuery("delete from SitemapPage").executeUpdate();
 		currentSession.createQuery("delete from Sitemap").executeUpdate();
 
 		currentSession.createQuery("delete from Page").executeUpdate();
 		currentSession.createQuery("delete from UserRole").executeUpdate();
 		currentSession.createQuery("delete from PageTemplate").executeUpdate();
 		currentSession.createQuery("delete from User").executeUpdate();
 		currentSession.createQuery("delete from Role").executeUpdate();
 	}
 
 
 	public void refData(){
 		//AM: Role,Sitemap,PageTemplate,SnippetGroup,Snippet,User,UserRole
 		Session currentSession = sessionFactory.getCurrentSession();
 
 		//Roles..
 		Role role = new Role();
 		role.setName("/server admin");
 		currentSession.merge(role);
 		role = new Role();
 		role.setName("user");
 		currentSession.merge(role);
 
 		//Sitemap..
 		Sitemap sitemap = new Sitemap();
 		sitemap.setName("Dummy sitemap");
 		currentSession.merge(sitemap);
 
 		//PageTemplate
 		PageTemplate template = new PageTemplate();
 		template.setName("Dummy page template");
 		template.setContent("\n"+
 				"<!-- paulirish.com/2008/conditional-stylesheets-vs-css-hacks-answer-neither/ -->\n"+
 				"<!--[if lt IE 7]> <html class='no-js lt-ie9 lt-ie8 lt-ie7' lang='en'> <![endif]-->\n"+
 				"<!--[if IE 7]>  <html class='no-js lt-ie9 lt-ie8' lang='en'> <![endif]-->\n"+
 				"<!--[if IE 8]>  <html class='no-js lt-ie9' lang='en'> <![endif]-->\n"+
 				"<!--[if gt IE 8]><!-->\n"+
 				"<html lang='en'>\n"+
 				"<!--<![endif]-->\n"+
 				"<head>\n"+
 				"<meta charset='utf-8' />\n"+
 				"<META HTTP-EQUIV='Cache-Control' CONTENT='max-age=0'>\n"+
 				"<META HTTP-EQUIV='Cache-Control' CONTENT='no-cache'>\n"+
 				"<META http-equiv='expires' content='0'>\n"+
 				"<META HTTP-EQUIV='Expires' CONTENT='Tue, 01 Jan 1980 1:00:00 GMT'>\n"+
 				"<META HTTP-EQUIV='Pragma' CONTENT='no-cache'>\n"+
 				"<!-- Set the viewport width to device width for mobile -->\n"+
 				"<meta name='viewport' content='width=device-width' />\n"+
 				"<!-- Included CSS Files -->\n"+
 				"<link rel='stylesheet' href='/server/stylesheets/grid.css'>\n"+
 				"<link rel='stylesheet' href='/server/stylesheets/ui.css'>\n"+
 				"<link rel='stylesheet' href='/server/stylesheets/app.css'>\n"+
 				"<link rel='stylesheet' href='/server/stylesheets/typography.css'>\n"+
 				"<link rel='stylesheet' href='/server/stylesheets/globals.css'>\n"+
 				"<link rel='stylesheet' href='/server/stylesheets/ui.dynatree.css'>\n"+
 				"<link rel='stylesheet' href='/server/stylesheets/forms.css'>\n"+
 				"<link rel='stylesheet' href='/server/stylesheets/reveal.css'>\n"+
 				"<link rel='stylesheet' href='/server/stylesheets/snippet_api.css'>\n"+
 				"<!--[if lt IE 9]>\n"+
 				"        <link rel='stylesheet' href='stylesheets/ie.css'>\n"+
 				"    <![endif]-->\n"+
 				"<!-- IE Fix for HTML5 Tags -->\n"+
 				"<!--[if lt IE 9]>\n"+
 				"        <script src='http://html5shiv.googlecode.com/svn/trunk/html5.js'></script>\n"+
 				"   <![endif]-->\n"+
 				" +\n"+
				" <slash-server-header/></head><h1>This header is in template</h1><slash-server/></div>" +
 				"<!-- Included JS Files -->\n"+
 				"<script src='/server/javascripts/jquery.min.js'></script>\n"+
 				"<script src='/server/javascripts/jquery.reveal.js'></script>\n"+
 				"<script src='/server/javascripts/jquery.customforms.js'></script>\n"+
 				"<script src='/server/javascripts/jquery.placeholder.min.js'></script>\n"+
 				"<script src='/server/javascripts/modernizr.foundation.js'></script>\n"+
 				"<script src='/server/javascripts/jquery.tooltips.js'></script>\n"+
 				"<script src='/server/javascripts/app.js'></script>\n"+
 				"</server-footer/></html>");
 		currentSession.merge(template);
 
 		//		Page page = new Page();
 		//		page.setPageTemplate(template);
 		//		page.setMarkup("<html><h1>This is the page header</h1>snippet:<br><snippet/><br>:end snippet</html>");
 		//		page.setName("Test page");
 		//		page.setRole(role);
 		//		page.setUrl("testpage");
 		//		currentSession.merge(page);
 
 		//SnippetGroup
 		SnippetGroup snippetGroup = new SnippetGroup();
 		snippetGroup.setName("Dummy group");
 		currentSession.merge(snippetGroup);
 
 		//Snippet
 		Snippet snippet = new Snippet();
 		snippet.setName("Dummy snippet");
 		snippet.setUrl("http://localhost:8080/server/dummydata/dummysnippet");
 		snippet.setSnippetGroup(snippetGroup);
 		currentSession.merge(snippet);
 		
 		snippet = new Snippet();
 		snippet.setName("Other Dummy snippet");
 		snippet.setUrl("http://localhost:8080/server/dummydata/otherdummysnippet");
 		snippet.setSnippetGroup(snippetGroup);
 		currentSession.merge(snippet);
 
 		//		PageSnippet pageSnippet = new PageSnippet();
 		//		pageSnippet.setPage(page);
 		//		pageSnippet.setSnippet(snippet);
 		//		currentSession.merge(pageSnippet);
 		//		
 		//		SitemapPage sitemapPage = new SitemapPage();
 		//		sitemapPage.setIndex(1);
 		//		sitemapPage.setPageByPageId(page);
 		//		sitemapPage.setSitemap(sitemap);
 		//		currentSession.merge(sitemapPage);
 
 		//User
 		User user = new User();
 		user.setName("peter");
 		currentSession.merge(user);
 
 		//UserRole
 		UserRole userRole = new UserRole();
 		userRole.setRole(role);
 		userRole.setUser(user);
 		currentSession.merge(userRole);
 
 
 
 	}
 
 
 	public void pageData(){
 		Snippet snippet = snippetStore.getAll().iterator().next();
 		Role role = roleStore.getByName("user");
 
 		//AM: Page,PageSnippet,SitemapPage,UserData
 		Session currentSession = sessionFactory.getCurrentSession();
 		Page page =new Page();
 		//page.setMarkup("<html><h1>Dummy page markup</h1><snippet id='"+snippet.getId()+"'></html>");
 		page.setMarkup("<html><h1>This is the page title</h1>snippet:<br><snippet uuid=\"123\"></snippet><br>:end snippet</html>");
 		page.setName("Andy's Dummy page");
 		page.setRole(role);
 		page.setUrl("demoPage");
 		page.setPageTemplate(pageTemplateStore.getAll().iterator().next());
 		currentSession.merge(page);
 
 		PageSnippet pageSnippet = new PageSnippet();
 		pageSnippet.setPage(page);
 		pageSnippet.setUuid("123");
 		pageSnippet.setSnippet(snippet);
 		currentSession.merge(pageSnippet);
 
 		Sitemap sitemap = sitemapStore.getAll().iterator().next();
 
 		SitemapPage sitemapPage = new SitemapPage();
 		sitemapPage.setIndex(1);
 		sitemapPage.setPageByPageId(page);
 		sitemapPage.setSitemap(sitemap);
 		currentSession.merge(sitemapPage);
 
 		UserData userData = new UserData();
 		userData.setUser(userStore.getByName("peter"));
 		userData.setPageSnippet(pageSnippet);
 		userData.setName("param1");
 		userData.setValue("paramValue");
 		currentSession.merge(userData);
 
 	}
 
 
 	@Autowired
 	public void setRoleStore(RoleStore roleStore) {
 		this.roleStore = roleStore;
 	}
 
 	@Autowired
 	public void setSnippetStore(SnippetStore snippetStore) {
 		this.snippetStore = snippetStore;
 	}
 
 
 	@Autowired
 	public void setSitemapStore(SitemapStore sitemapStore) {
 		this.sitemapStore = sitemapStore;
 	}
 
 	@Autowired
 	public void setUserStore(UserStore userStore) {
 		this.userStore = userStore;
 	}
 
 	@Autowired
 	public void setPageTemplateStore(PageTemplateStore store) {
 		this.pageTemplateStore = store;
 	}
 
 }
