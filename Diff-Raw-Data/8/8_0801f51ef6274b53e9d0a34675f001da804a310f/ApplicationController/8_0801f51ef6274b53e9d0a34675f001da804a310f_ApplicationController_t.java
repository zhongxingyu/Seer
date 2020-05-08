 package com.smash.revolance.ui.server.controller;
 
 import com.smash.revolance.ui.comparator.application.ApplicationComparator;
 import com.smash.revolance.ui.comparator.application.ApplicationComparison;
 import com.smash.revolance.ui.comparator.application.IApplicationComparator;
 import com.smash.revolance.ui.comparator.page.IPageComparator;
 import com.smash.revolance.ui.comparator.page.PageComparator;
 import com.smash.revolance.ui.comparator.page.PageComparison;
 import com.smash.revolance.ui.database.FileSystemStorage;
 import com.smash.revolance.ui.database.IStorage;
 import com.smash.revolance.ui.database.StorageException;
 import com.smash.revolance.ui.model.element.api.ElementBean;
 import com.smash.revolance.ui.model.page.api.PageBean;
 import com.smash.revolance.ui.model.sitemap.SiteMap;
 import com.smash.revolance.ui.server.model.Application;
 import com.smash.revolance.ui.server.renderable.MergerRenderable;
 import com.smash.revolance.ui.server.renderable.PageComparisonRenderable;
 import com.smash.revolance.ui.server.renderable.PageRenderable;
 import org.rendersnake.HtmlCanvas;
 import org.springframework.stereotype.Controller;
 import org.springframework.stereotype.Service;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.rendersnake.HtmlAttributesFactory.*;
 
 @Service
 @Controller
 /**
  * Created by wsmash on 27/10/13.
  */
 public class ApplicationController
 {
     IStorage repository = new FileSystemStorage("applications");
 
     IApplicationComparator sitemapComparator = new ApplicationComparator();
 
     IPageComparator pageComparator = new PageComparator();
 
     @RequestMapping(value = "/applications", method = RequestMethod.POST)
     public ModelAndView addContent(@Valid Application application, BindingResult result) throws IOException, StorageException
     {
         if ( result.hasErrors() )
         {
             return new ModelAndView( "ContentForm" );
         }
         else
         {
             repository.store( application.getTag(), application.getContent() );
             return new ModelAndView( "ApplicationList", "contentList", getApplications() );
         }
 
     }
 
     private List<Application> getApplications() throws StorageException, IOException
     {
         List<Application> applications = new ArrayList<Application>();
 
         for ( String key : repository.retrieveAll().keySet() )
         {
             Application app = new Application();
             app.setTag( key );
 
             SiteMap sitemap = SiteMap.fromJson( repository.retrieve( key ) );
             app.setSitemap( sitemap );
             app.setUser( sitemap.getUser() );
 
             applications.add( app );
         }
 
         return applications;
     }
 
     @RequestMapping(value = "/applications/declare", method = RequestMethod.GET)
     public ModelAndView displayContentForm(Application content, BindingResult result)
     {
         return new ModelAndView( "ApplicationForm" );
     }
 
     @RequestMapping(value = "/applications/{applicationId}", method = RequestMethod.DELETE)
     public ModelAndView displayContentForm(@PathVariable String applicationId) throws StorageException, IOException
     {
         repository.delete( applicationId );
         return new ModelAndView( "ApplicationList", "contentList", getApplications() );
     }
 
     @RequestMapping(value = "/applications/merge/{contentTagRef}/{contentTagNew}/{pageRefId}/{pageNewId}", method = RequestMethod.POST, headers = "Content-Type=application/json")
     public void displayContentForm(@PathVariable String contentTagRef, @PathVariable String contentTagNew, @PathVariable String pageRefId, @PathVariable String pageNewId, @RequestBody List<String> contentFilter) throws StorageException, IOException
     {
         PageBean refPage = SiteMap.fromJson( repository.retrieve( contentTagRef ) ).findPageByInternalId( pageRefId );
         PageBean newPage = SiteMap.fromJson( repository.retrieve( contentTagNew ) ).findPageByInternalId( pageNewId );
 
         updateContent( refPage, contentFilter, newPage.getContent() );
     }
 
     public void updateContent(PageBean page, List<String> contentFilter, List<ElementBean> content)
     {
         List<ElementBean> elements = new ArrayList<ElementBean>();
 
         for ( ElementBean element : page.getContent() )
         {
             if ( contentFilter.contains( element.getInternalId() ) )
             {
                 elements.add( element );
             }
         }
 
         for ( ElementBean element : content )
         {
             if ( contentFilter.contains( element.getInternalId() ) )
             {
                 elements.add( element );
             }
         }
 
         page.setContent( elements );
     }
 
     @RequestMapping(value = "/applications", method = RequestMethod.GET)
     public ModelAndView displayContentList(@ModelAttribute("model") ModelMap model) throws IOException, StorageException
     {
         return new ModelAndView( "ApplicationList", "contentList", getApplications() );
     }
 
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView displayRoot(@ModelAttribute("model") ModelMap model) throws IOException, StorageException
    {
        return displayContentList(model);
    }

     @RequestMapping(value = "/applications/{contentTag}", method = RequestMethod.GET)
     public ModelAndView displayContentDetails(@PathVariable String contentTag, @ModelAttribute("model") ModelMap model, HttpServletRequest request) throws StorageException, IOException
     {
         model.addAttribute( "contentTag", contentTag );
 
         List<PageBean> pages = new ArrayList<PageBean>();
         for ( PageBean page : SiteMap.fromJson( repository.retrieve( contentTag ) ).getPages() )
         {
             pages.add( page );
             if ( page.hasVariants() )
             {
                 pages.addAll( page.getVariants() );
             }
         }
         model.addAttribute( "pages", renderPagesWithRenderables( pages, request.getSession() ) );
         return new ModelAndView( "ApplicationDetail", model );
     }
 
     private List<String> renderPagesWithRenderables(List<PageBean> pages, HttpSession session)
     {
         List<String> renderedPages = new ArrayList<String>();
 
         for ( PageBean pageBean : pages )
         {
             try
             {
                 HtmlCanvas canvas = new HtmlCanvas();
                 new PageRenderable( pageBean ).renderWithContext( canvas, session );
                 renderedPages.add( canvas.toHtml() );
             }
             catch (IOException e)
             {
                 e.printStackTrace();
             }
         }
 
         return renderedPages;
     }
 
     @RequestMapping(value = "/applications/compare/{contentTagRef}/{contentTagNew}", method = RequestMethod.GET)
     public ModelAndView displayContentDetails(@PathVariable String contentTagRef, @PathVariable String contentTagNew, @ModelAttribute("model") ModelMap model, HttpServletRequest request) throws StorageException, IOException
     {
 
         model.addAttribute( "refAppTag", contentTagRef );
         model.addAttribute( "newAppTag", contentTagNew );
 
         SiteMap refSitemap = SiteMap.fromJson( repository.retrieve( contentTagRef ) );
         SiteMap newSitemap = SiteMap.fromJson( repository.retrieve( contentTagNew ) );
 
         ApplicationComparison comparison = sitemapComparator.compare( refSitemap, newSitemap );
 
         model.addAttribute( "pageComparisons", renderComparisonsWithRenderables( contentTagRef, contentTagNew, comparison.getPageComparisons(), request ) );
         model.addAttribute( "pageAdded", comparison.getAddedPages() );
         model.addAttribute( "pageRemoved", comparison.getRemovedPages() );
 
         return new ModelAndView( "ApplicationComparison", model );
     }
 
     private List<String> renderComparisonsWithRenderables(String contentTagRef, String contentTagNew, List<PageComparison> pageComparisons, HttpServletRequest request) throws IOException
     {
         List<String> values = new ArrayList<String>();
 
         int idx = 0;
         for ( PageComparison comparison : pageComparisons )
         {
             StringWriter html = new StringWriter();
             HtmlCanvas canvas = new HtmlCanvas( html );
 
             canvas.tr( data( "id", String.valueOf( idx ) ).data( "variant-idx", "0" ) )
                     .td( colspan( "2" ) );
             canvas.render( new PageComparisonRenderable( comparison ) );
             if ( !comparison.getPageDifferencies().isEmpty() )
             {
                 canvas.div( class_( "span10" ) )
                         .div( class_( "pull-right" ) )
                         .a( type( "button" )
                                     .class_( "btn btn-info btn-small" )
                                     .data( "row-id", String.valueOf( idx ) )
                                     .data( "new-page-id", comparison.getMatch().getId() )
                                     .data( "ref-page-id", comparison.getReference().getId() )
                                     .href( request.getContextPath() + "/applications/compare/" + contentTagRef + "/" + contentTagNew + "/" + comparison.getReference().getId() + "/" + comparison.getMatch().getId() ) )
                         .content( "Inspect" )
                         ._div()
                         ._div();
             }
 
             canvas._td()._tr();
             idx++;
 
             values.add( canvas.toHtml() );
         }
 
         return values;
     }
 
     @RequestMapping(value = "/applications/compare/{contentTagRef}/{contentTagNew}/{pageRefId}/{pageNewId}", method = RequestMethod.GET)
     public ModelAndView renderMergeView(@PathVariable String contentTagRef, @PathVariable String contentTagNew, @PathVariable String pageRefId, @PathVariable String pageNewId, @ModelAttribute("model") ModelMap model) throws StorageException, IOException
     {
 
         model.addAttribute( "refAppTag", contentTagRef );
         model.addAttribute( "newAppTag", contentTagNew );
 
         PageBean refPage = SiteMap.fromJson( repository.retrieve( contentTagRef ) ).findPageByInternalId( pageRefId );
         PageBean newPage = SiteMap.fromJson( repository.retrieve( contentTagNew ) ).findPageByInternalId( pageNewId );
 
         HtmlCanvas canvas = new HtmlCanvas();
         if ( refPage != null && newPage != null )
         {
             PageComparison comparison = pageComparator.compare( newPage, refPage );
             MergerRenderable merge = new MergerRenderable( comparison );
             merge.setRefApp(contentTagRef);
             merge.setNewApp(contentTagNew);
             merge.renderOn( canvas );
         }
         else
         {
             canvas.h1().content( "Oups an Internal error occured. Unable to do this comparison." )._h1();
         }
         model.addAttribute( "content", canvas.toHtml() );
 
         return new ModelAndView( "PageMerger", model );
     }
 
 }
