 package net.cyklotron.cms.syndication.internal;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import net.cyklotron.cms.CmsTool;
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.LinkRenderer;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.syndication.CannotCreateFeedsRootException;
 import net.cyklotron.cms.syndication.CannotCreateSyndicationRootException;
 import net.cyklotron.cms.syndication.CannotExecuteQueryException;
 import net.cyklotron.cms.syndication.CannotGenerateFeedException;
 import net.cyklotron.cms.syndication.EmptyDescriptionException;
 import net.cyklotron.cms.syndication.EmptyFeedNameException;
 import net.cyklotron.cms.syndication.FeedAlreadyExistsException;
 import net.cyklotron.cms.syndication.FeedCreationException;
 import net.cyklotron.cms.syndication.FeedDateFormatter;
 import net.cyklotron.cms.syndication.OutgoingFeedResource;
 import net.cyklotron.cms.syndication.OutgoingFeedResourceImpl;
 import net.cyklotron.cms.syndication.OutgoingFeedUtil;
 import net.cyklotron.cms.syndication.OutgoingFeedsManager;
 import net.cyklotron.cms.syndication.SyndicationException;
 import net.cyklotron.cms.syndication.SyndicationService;
 import net.cyklotron.cms.syndication.TooManyFeedsRootsException;
 import net.cyklotron.cms.syndication.TooManySyndicationRootsException;
 import net.cyklotron.cms.util.OfflineLinkRenderingService;
 import net.cyklotron.cms.util.ProtectedValidityFilter;
 import net.cyklotron.cms.util.ProtectedViewFilter;
 
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.ResourceListTableModel;
 import org.objectledge.encodings.HTMLEntityEncoder;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;
 import org.objectledge.parameters.DefaultParameters;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableRow;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableTool;
 import org.objectledge.templating.MergingException;
 import org.objectledge.templating.Template;
 import org.objectledge.templating.TemplateNotFoundException;
 import org.objectledge.templating.Templating;
 import org.objectledge.templating.TemplatingContext;
 
 import com.sun.syndication.feed.synd.SyndCategory;
 import com.sun.syndication.feed.synd.SyndCategoryImpl;
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndContentImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedOutput;
 import com.sun.syndication.io.WireFeedOutput;
 
 /**
  * Implementation of OutgoingFeedsManager.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: DefaultOutgoingFeedsManager.java,v 1.1 2005-06-16 11:14:21 zwierzem Exp $
  */
 public class DefaultOutgoingFeedsManager
 extends BaseFeedsManager
 implements OutgoingFeedsManager
 {
     private static final String BASE_TEMPLATES_DIR = "outgoing-feeds-templates";
     private static final String TEMPLATES_APP = "cms";
     private static final String TEMPLATES_DIR =
         "/templates/"+TEMPLATES_APP+"/"+BASE_TEMPLATES_DIR;
 
     private CoralSessionFactory coralSessionFactory;
     private CategoryQueryService categoryQueryService;
     private OfflineLinkRenderingService offlineLinkRenderingService;
     private Templating templating;
 
     public DefaultOutgoingFeedsManager(CoralSessionFactory coralSessionFactory, SyndicationService syndicationService,
         FileSystem fileSystem, CategoryQueryService categoryQueryService,
         OfflineLinkRenderingService offlineLinkRenderingService, 
         Templating templating)
     {
         super(syndicationService, fileSystem);
         this.categoryQueryService = categoryQueryService;
         this.offlineLinkRenderingService = offlineLinkRenderingService;
         this.templating = templating;
         
         this.coralSessionFactory = coralSessionFactory;
         
         if(!fileSystem.exists(TEMPLATES_DIR))
         {
             try
             {
                 fileSystem.mkdirs(TEMPLATES_DIR);
             }
             catch(IOException e)
             {
                 throw new ComponentInitializationError("cannot create templates dir", e);
             }
             catch(UnsupportedCharactersInFilePathException e)
             {
                 throw new ComponentInitializationError(
                     "templates dir path has unsupported characters", e);
             }
         }
     }
 
     public OutgoingFeedResource createFeed(CoralSession coralSession, String name, String description,
         int interval, CategoryQueryResource categoryQuery, String generationTemplate,
         boolean publik, SiteResource site)
     throws EmptyFeedNameException, FeedCreationException,
         FeedAlreadyExistsException, EmptyDescriptionException, InvalidResourceNameException
     {
         Resource parent = prepareCreateFeed(coralSession, name, site);
 
         checkDescription(description);
         
         OutgoingFeedResource feed = OutgoingFeedResourceImpl.createOutgoingFeedResource(
             coralSession, name, parent);
         
         feed.setInterval(-100);
         feed.setPublic(false);
         
         updateFeedInternal(coralSession, feed, name, description, interval,
             categoryQuery, generationTemplate, publik);
 
         return feed;
     }
 
     public void updateFeed(CoralSession coralSession, OutgoingFeedResource feed, String name,
         String description, int interval, CategoryQueryResource categoryQuery, String template,
         boolean publik)
     throws EmptyFeedNameException, FeedAlreadyExistsException, EmptyDescriptionException, InvalidResourceNameException
     {
         prepareRenameFeedResource(coralSession, feed, name);
 
         checkDescription(description);
 
         updateFeedInternal(coralSession, feed, name, description, interval, categoryQuery, template,
             publik);
     }
 
     private void checkDescription(String description) throws EmptyDescriptionException
     {
         if(description == null || description.length() == 0)
         {
             throw new EmptyDescriptionException();
         }
     }
 
     private void updateFeedInternal(CoralSession coralSession, OutgoingFeedResource feed, String name,
         String description, int interval, CategoryQueryResource categoryQuery, String template,
         boolean publik) throws InvalidResourceNameException
     {
         template = fixTemplate(template);
         
         // rename
         if(!feed.getName().equals(name))
         {
             coralSession.getStore().setName(feed, name);
         }
 
         if(feed.getDescription() == null
             || !feed.getDescription().equals(description))
         {
             feed.setDescription(description);
         }
         if(feed.getCategoryQuery() == null
             || !feed.getCategoryQuery().equals(categoryQuery))
         {
             feed.setCategoryQuery(categoryQuery);
             feed.setContents(null);
         }
         if(feed.getInterval() != interval)
         {
             feed.setInterval(interval);
         }
         if(feed.getGenerationTemplate() == null
             || !feed.getGenerationTemplate().equals(template))
         {
             feed.setGenerationTemplate(template);
             feed.setContents(null);
         }
         if(feed.getPublic() != publik)
         {
             feed.setPublic(publik);
         }
     
         //feed.setCategory(category);
         //feed.setCopyright(copyright);
         //feed.setLanguage(lang);
         //feed.setManagingEditor(managingEditor);
         //feed.setWebMaster(webmaster);
         
         // and update
         feed.update();
     }
 
     public void deleteFeed(CoralSession coralSession, OutgoingFeedResource feed)
     throws EntityInUseException
     {
         deleteFeedResource(coralSession, feed);
     }
 
     public OutgoingFeedResource[] getFeeds(CoralSession coralSession, SiteResource site)
     throws SyndicationException
     {
         Resource parent = getFeedsParent(coralSession, site);
         Resource[] res = coralSession.getStore().getResource(parent);
         OutgoingFeedResource[] feeds = new OutgoingFeedResource[res.length];
         System.arraycopy(res, 0, feeds, 0, res.length);
         return feeds;
     }
 
     public synchronized Resource getFeedsParent(CoralSession coralSession, SiteResource site)
     throws TooManySyndicationRootsException, CannotCreateSyndicationRootException,
         TooManyFeedsRootsException, CannotCreateFeedsRootException
     {
         return getFeedsParent(site, OUTGOING_FEEDS_ROOT, coralSession);
     }
 
     public void refreshFeed(CoralSession coralSession, OutgoingFeedResource feed)
         throws CannotExecuteQueryException, TableException, CannotGenerateFeedException,
         TemplateNotFoundException, MergingException
     {
         //0. retrieve documents for a feed
         Resource[] resources = null;
         try
         {
             resources = categoryQueryService.forwardQuery(coralSession, feed.getCategoryQuery().getQuery());
         }
         catch(Exception e)
         {
             throw new CannotExecuteQueryException("Query for feed '"+
                 feed+"' could not be executed", e);
         }
         //1. apply filters
         TableState state = new TableState(1);
         TableModel model = null;
         try
         {
             model = new ResourceListTableModel(resources, new Locale(feed.getLanguage()));
         }
         catch(TableException e)
         {
             throw e;
         }
         List filters = new ArrayList(5);
         CoralSession anonSession = coralSessionFactory.getAnonymousSession();
         filters.add(new ProtectedValidityFilter(null, new Date()));
         filters.add(new ProtectedViewFilter(anonSession, anonSession.getUserSubject()));
         List rows = null;
         try
         {
             TableTool tableTool = new TableTool(state, filters, model);
             rows = tableTool.getRows();
         }
         catch(TableException e)
         {
             throw e;
         }
         
         //3. create document collection
         DocumentNodeResource[] documents = new DocumentNodeResource[rows.size()];
         int i = 0;
         for (Iterator iter = rows.iterator(); iter.hasNext(); i++)
         {
             TableRow row = (TableRow)iter.next();
             documents[i] = (DocumentNodeResource) row.getObject();
         }
         
         //2. create the feed object
         SyndFeed syndFeed = createFeedObject(coralSession, feed, documents);
         
         //4. render using rome rendered or a velocity template
         String contents = null;
         if(feed.getGenerationTemplate().endsWith(".vt"))
         {
             contents = generateFeedContent(coralSession, feed, documents);
         }
         else
         {
             try
             {
                 contents = generateFeedContent(syndFeed);
             }
             catch(Exception e)
             {
                 throw new CannotGenerateFeedException("Feed '"+
                     feed+"' could not be generated", e);
             }
         }
         //5. save the content
         if(feed.getContents() == null || !feed.getContents().equals(contents))
         {
             feed.setContents(contents);
             feed.setLastUpdate(new Date());
             feed.update();
         }
     }
 
     private SyndFeed createFeedObject(CoralSession coralSession, OutgoingFeedResource feedRes, DocumentNodeResource[] documents)
     {
         SyndFeed feed = new SyndFeedImpl();
         
         feed.setFeedType(feedRes.getGenerationTemplate());
 
         feed.setTitle(feedRes.getName());
         feed.setLink(getFeedLink(coralSession, feedRes));
         feed.setDescription(feedRes.getDescription());
         feed.setPublishedDate(new Date());
         feed.setEncoding("UTF-8");
         feed.setAuthor(feedRes.getManagingEditor());
         
         if(feedRes.getCategory() != null)
         {
             List categories = new ArrayList();
             SyndCategory cat = new SyndCategoryImpl();
             cat.setName(feedRes.getCategory());
             categories.add(cat);
             feed.setCategories(categories);
         }
         if(feedRes.getCopyright() != null)
         {
             feed.setCopyright(feedRes.getCopyright());
         }
         if(feedRes.getLanguage() != null)
         {
             feed.setLanguage(feedRes.getLanguage());
         }
         if(feedRes.getWebMaster() != null)
         {
             feed.setAuthor(feedRes.getWebMaster());
         }
         
         List entries = new ArrayList();
         SyndEntry entry;
         SyndContent description;
 
         for (int i = 0; i < documents.length; i++)
         {
             DocumentNodeResource doc = documents[i];
             entry = new SyndEntryImpl();
             entry.setTitle(doc.getTitle());
             entry.setLink(getDocLink(coralSession, doc));
             if(doc.getValidityStart() == null)
             {
                 entry.setPublishedDate(doc.getCreationTime());
             }
             else
             {
                 entry.setPublishedDate(doc.getValidityStart());
             }
             String desc = "";
             if(doc.getAbstract() != null)
             {
                 desc = doc.getAbstract();
             }
             description = new SyndContentImpl();
             description.setType("text/plain");
             description.setValue(doc.getAbstract());
             entry.setDescription(description);
             entries.add(entry);
         }
 
         feed.setEntries(entries);
 
         return feed;
     }
     
     private String getDocLink(CoralSession coralSession, DocumentNodeResource doc)
     {
         LinkRenderer linkRenderer = offlineLinkRenderingService.getLinkRenderer();
         try
         {
             return linkRenderer.getNodeURL(coralSession, doc);
         }
         catch(ProcessingException e)
         {
             return null;
         }
     }
 
     private String getFeedLink(CoralSession coralSession, OutgoingFeedResource feedRes)
     {
         SiteResource site = CmsTool.getSite(feedRes);
         Parameters pathinfoParameters = new DefaultParameters();
         pathinfoParameters.set(OutgoingFeedUtil.FEED_ID_PARAM, feedRes.getIdString());
         
         return offlineLinkRenderingService.getViewURL(coralSession, site,
             "syndication,OutgoingFeedView", pathinfoParameters, null);
     }
 
     protected String generateFeedContent(SyndFeed feed)
     throws IOException, FeedException
     {
         StringWriter writer = new StringWriter();
         SyndFeedOutput output = new SyndFeedOutput();
         try
         {
             output.output(feed, writer);
             writer.close();
             return writer.toString();
         }
         catch(IOException e)
         {
             throw e;
         }
         catch(FeedException e)
         {
             throw e;
         }
     }
 
     public String generateFeedContent(CoralSession coralSession, OutgoingFeedResource feed, DocumentNodeResource[] documents)
     throws TemplateNotFoundException, MergingException
     {
         String templateName = feed.getGenerationTemplate();
         templateName = templateName.substring(0, templateName.length() - ".vt".length());
         String path = BASE_TEMPLATES_DIR+"/"+templateName;
         Template template = templating.getTemplate(path);
         TemplatingContext templatingContext = templating.createContext();
         templatingContext.put("feed", feed);
         templatingContext.put("documents", documents);
         templatingContext.put("feedLinkTool", new FeedLinkTool(coralSession));
         templatingContext.put("feedDateFormat", new FeedDateFormatter());
         templatingContext.put("htmlEntityEncoder", new HTMLEntityEncoder());
         return template.merge(templatingContext);
     }
     
     public List getGenerationTemplates() throws IOException
     {
         String[] fileTemplates = fileSystem.list(TEMPLATES_DIR);
         List templates = new ArrayList(fileTemplates.length+1);
         templates.addAll(Arrays.asList(fileTemplates));
         Arrays.sort(fileTemplates);
         List feedTypes = WireFeedOutput.getSupportedFeedTypes();
         ArrayList modifiableFeedTypes = new ArrayList(feedTypes); 
         Collections.sort(modifiableFeedTypes);
         templates.addAll(0, modifiableFeedTypes);
         return templates;
     }
 
     public class FeedLinkTool
     {
         private CoralSession coralSession;
 
         public FeedLinkTool(CoralSession coralSession)
         {
             this.coralSession = coralSession;
         }
         
         public String link(DocumentNodeResource doc)
         {
             return DefaultOutgoingFeedsManager.this.getDocLink(coralSession, doc);
         }
 
         public String link(OutgoingFeedResource feedRes)
         {
             return DefaultOutgoingFeedsManager.this.getFeedLink(coralSession, feedRes);
         }
     }
 }
