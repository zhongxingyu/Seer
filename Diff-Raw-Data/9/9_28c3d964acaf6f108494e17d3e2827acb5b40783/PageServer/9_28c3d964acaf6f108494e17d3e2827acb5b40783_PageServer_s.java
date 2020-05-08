 package com.slashserver.engine;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.slashserver.model.Page;
 import com.slashserver.model.PageTemplate;
 import com.slashserver.model.Role;
 import com.slashserver.persistence.PageStore;
 
 
 @Controller
 @RequestMapping("/page/")
 public class PageServer {
 
 	@Autowired
 	private PageStore pageStore;
 
 	@Autowired
 	private SnippetContentFetcher fetcher;
 	
 	private SnippetInjector injector = new SnippetInjector();
 
 
 	@RequestMapping(value="{pageUrl1}/{pageUrl2}",method = RequestMethod.GET)
 	public void serveNested(@PathVariable("pageUrl1") String pageUrl,@PathVariable("pageUrl2") String pageUrl2, 
 			HttpServletRequest request,HttpServletResponse response) throws Exception {
 		//AMDO: we can allow configured parameters for url part 2
 		serve(pageUrl+"/"+pageUrl2,request,response);
 	}
 
 	@RequestMapping(value="{pageUrl}",method = RequestMethod.GET)
 	public void serve(@PathVariable("pageUrl") String pageUrl, HttpServletRequest request,HttpServletResponse response) throws Exception {
 
 		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 
 		UserDetails details = ((UserDetails)principal); 
 
 		//Fetch page and related entities -< roles, plus -< page_snippet -< snippet, plus -< page_template
 		Page page = pageStore.getFullyPopulated(pageUrl);	
 
 		boolean authorised =authorise(details, page);
 		if(!authorised){
 			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
 			return;
 		}
 
 		//Fetch required snippet content from source locations, in parallel. Produce set of head, body and footer content for each.
 		Set<SnippetRenderingInfo> fetchContent = fetcher.fetchContent(page.getPageSnippets(), request, details, true);
 
 		//Using page template prepare completed header and footer content.
 		PageTemplate pageTemplate = page.getPageTemplate();
 		String markup = page.getMarkup();
 		// mash snippet content into page!
 		String populatedPage = injector.popSnippetsIntoPage(markup, fetchContent);
 
 		String template = pageTemplate.getContent();
 		//mash markup into template
 
 
 		//AM: header and footer content too.
 		StringBuilder headerContent = new StringBuilder();
 		StringBuilder footerContent = new StringBuilder();
 		for(SnippetRenderingInfo snippet:fetchContent){
 			headerContent.append("\n");
 			String headContent = snippet.getHeadContent();
 			if(headContent!=null&&headContent.length()>0){
 				headerContent.append("\n<!-- Head content for ").append(snippet.getPageSnippetId()).append(" -->\n");
 				headerContent.append(headContent).append("\n<!-- end head content -->\n");
 			}
 
 			String footContent = snippet.getBottomContent();
 			if(footContent!=null&&footContent.length()>0){
 				footerContent.append("\n<!-- bottom content for ").append(snippet.getPageSnippetId()).append(" -->\n");
 				footerContent.append(footContent).append("\n<!-- end bottom content -->\n");
 			}
 
 		}
 		
 		//Parse page content and embed snippet body content in appropriate locations.
 		response.getWriter().print(stickThePageIntoTemplate(populatedPage,headerContent.toString(),footerContent.toString(), template));
 
 	}
 
 	
 
 	private String stickThePageIntoTemplate(String page,String headerContent,String footerContent,String template){
		return splitAndInject(page, "</server/>",splitAndInject(headerContent, "</server-header/>", splitAndInject(footerContent,"</server-footer/>",template)));
 		
 	}
 	
 	private String splitAndInject(String inject,String split,String template){
 		String[] splits = template.split(split);
 		if(splits.length!=2){
 			throw new RuntimeException("Cant find "+split+" in page template (or perhaps more than once?)");
 		}
 		return splits[0]+inject+splits[1];
 		
 	}
 
 
 
 	private boolean authorise( UserDetails details,Page page) {
 		Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
 		Role role = page.getRole();
 		if(role!=null){
 			boolean authorised = false;
 			//Authorise user to view this page.
 			for(GrantedAuthority userRole:authorities){
 				if(userRole.getAuthority().equals(role.getName())){
 					authorised=true;
 					break;
 				}
 			}
 			return authorised;
 		}else{
 			return true;
 		}
 	}
 
 	public void setPageStore(PageStore pageStore) {
 		this.pageStore = pageStore;
 	}
 
 	public void setFetcher(SnippetContentFetcher fetcher) {
 		this.fetcher = fetcher;
 	}
 }
