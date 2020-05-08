 package net.cyklotron.cms.modules.views.structure;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.comparator.NameComparator;
 import org.objectledge.diff.DetailElement;
 import org.objectledge.diff.DiffUtil;
 import org.objectledge.diff.Sequence;
 import org.objectledge.diff.Splitter;
 import org.objectledge.diff.Element.State;
 import org.objectledge.html.HTMLService;
 import org.objectledge.html.HTMLWordBoundarySplitter;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.DocumentNodeResourceImpl;
 import net.cyklotron.cms.modules.views.documents.BaseSkinableDocumentScreen;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.structure.internal.OrganizationData;
 import net.cyklotron.cms.structure.internal.ProposedDocumentData;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  *
  */
 public class ReviewProposedChanges
     extends BaseStructureScreen
 {
     protected UserManager userManager;
 
     private CategoryService categoryService;
 
     private HTMLService htmlService;
 
     public ReviewProposedChanges(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager, StructureService structureService,
         StyleService styleService, SiteService siteService, RelatedService relatedService,
         UserManager userManager, CategoryService categoryService, HTMLService htmlService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager,
                         structureService, styleService, siteService, relatedService);
         this.userManager = userManager;
         this.categoryService = categoryService;
         this.htmlService = htmlService;
     }
 
     @Override
     public void process(Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext,
         CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             long docId = parameters.getLong("doc_id");
             templatingContext.put("doc_id", docId);
 
             DocumentNodeResource node = DocumentNodeResourceImpl.getDocumentNodeResource(
                 coralSession, docId);
 
             boolean isDocEquals;
             Sequence<DetailElement<String>> title;
             Sequence<Sequence<DetailElement<String>>> docAbstract;
             Sequence<Sequence<DetailElement<String>>> content;
             Sequence<DetailElement<String>> validityStart;
             Sequence<DetailElement<String>> validityEnd;
             Sequence<DetailElement<String>> eventStart;
             Sequence<DetailElement<String>> eventEnd;
             Sequence<DetailElement<String>> eventPlace;
             Sequence<DetailElement<String>> eventProvince;
             Sequence<DetailElement<String>> eventPostCode;
             Sequence<DetailElement<String>> eventCity;
             Sequence<DetailElement<String>> eventStreet;;
             Sequence<DetailElement<String>> sourceName;
             Sequence<DetailElement<String>> sourceUrl;
             Sequence<DetailElement<String>> proposerCredentials;
             Sequence<DetailElement<String>> proposerEmail;
             Sequence<Sequence<DetailElement<String>>> description;
 
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             ProposedDocumentData publishedData = new ProposedDocumentData(logger);
             ProposedDocumentData proposedData = new ProposedDocumentData(logger);
 
             proposedData.fromProposal(node, coralSession);
             Parameters screenConfig = cmsData.getEmbeddedScreenConfig(proposedData.getOrigin());
             proposedData.setConfiguration(screenConfig);
             publishedData.setConfiguration(screenConfig);
             publishedData.fromNode(node, categoryService, relatedService, coralSession);
 
             NameComparator<CategoryResource> comparator = new NameComparator<CategoryResource>(
                 i18nContext.getLocale());
             long root_category_1 = screenConfig.getLong("category_id_1", -1);
             long root_category_2 = screenConfig.getLong("category_id_2", -1);
             int categoryDepth = screenConfig.getInt("category_depth", 1);
 
             Set<CategoryResource> availableCategories = new HashSet<CategoryResource>();
             BaseSkinableDocumentScreen.getCategoryList(root_category_1, categoryDepth, true,
                 coralSession, availableCategories);
             BaseSkinableDocumentScreen.getCategoryList(root_category_2, categoryDepth, true,
                 coralSession, availableCategories);
 
             Set<CategoryResource> noAvalilableCategories = new HashSet<CategoryResource>();
             if(publishedData.getSelectedCategories() != null)
             {
                 noAvalilableCategories.addAll(publishedData.getSelectedCategories());
             }
             noAvalilableCategories.removeAll(availableCategories);
 
             isDocEquals = true;
             if(!equals(publishedData.getTitle(), proposedData.getTitle()))
             {
                 title = DiffUtil.diff(proposedData.getTitle(), publishedData.getTitle(),
                     Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("title", title);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getAbstract(), proposedData.getAbstract()))
             {
                 docAbstract = DiffUtil.diff(proposedData.getAbstract(),
                     publishedData.getAbstract(), Splitter.NEWLINE_SPLITTER,
                     Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("docAbstract", docAbstract);
                 if(proposedData.getAbstract() != null)
                 {
                     templatingContext.put("proposedHTMLAbstract", proposedData.getAbstract());
                 }
                 if(publishedData.getAbstract() != null)
                 {
                     templatingContext.put("publishedHTMLAbstract", publishedData.getAbstract());
                 }
                 isDocEquals = false;
             }
 
             String publishedContent = publishedData.getContent() != null ? publishedData
                 .getContent() : "";
             String proposedContent = proposedData.getContent() != null ? proposedData.getContent()
                 : "";
             String publishedContentForComparison = ProposedDocumentData.cleanupContent(
                 publishedContent, htmlService);
             content = DiffUtil.diff(proposedContent, publishedContentForComparison,
                 HTMLWordBoundarySplitter.INSTANCE, Splitter.SP_SPLITTER); // HTMLParagraphSplitter.INSTANCE
             if(!content.getState().equals(State.EQUAL))
             {
                 templatingContext.put("content", content);
                 if(proposedData.getContent() != null)
                 {
                     templatingContext.put("proposedHTMLContent", proposedData.getContent());
                 }
                 if(publishedData.getContent() != null)
                 {
                     templatingContext.put("publishedHTMLContent", publishedData.getContent());
                 }
                 isDocEquals = false;
             }
             if(!equals(publishedData.getEventPlace(), proposedData.getEventPlace()))
             {
                 eventPlace = DiffUtil.diff(proposedData.getEventPlace(), publishedData
                     .getEventPlace(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("eventPlace", eventPlace);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getEventProvince(), proposedData.getEventProvince()))
             {
                 eventProvince = DiffUtil.diff(proposedData.getEventProvince(), publishedData
                     .getEventProvince(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("eventProvince", eventProvince);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getEventPostCode(), proposedData.getEventPostCode()))
             {
                 eventPostCode = DiffUtil.diff(proposedData.getEventPostCode(), publishedData
                     .getEventPostCode(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("eventPostCode", eventPostCode);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getEventCity(), proposedData.getEventCity()))
             {
                 eventCity = DiffUtil.diff(proposedData.getEventCity(),
                     publishedData.getEventCity(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("eventCity", eventCity);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getEventStreet(), proposedData.getEventStreet()))
             {
                 eventStreet = DiffUtil.diff(proposedData.getEventStreet(), publishedData
                     .getEventStreet(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("eventStreet", eventStreet);
                 isDocEquals = false;
             }
             if(!equals(proposedData.getEventStart(), publishedData.getEventStart()))
             {
                 String proposedEventStart = proposedData.getEventStart() != null ? proposedData
                     .getEventStart().toString() : null;
                 String publishedEventStart = publishedData.getEventStart() != null ? publishedData
                     .getEventStart().toString() : null;
                 eventStart = DiffUtil.diff(proposedEventStart, publishedEventStart,
                     Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("eventStart", eventStart);
                 isDocEquals = false;
             }
 
             if(!equals(proposedData.getEventEnd(), publishedData.getEventEnd()))
             {
                 String proposedEventEnd = proposedData.getEventEnd() != null ? proposedData
                     .getEventEnd().toString() : null;
                 String publishedEventEnd = publishedData.getEventEnd() != null ? publishedData
                     .getEventEnd().toString() : null;
                 eventEnd = DiffUtil.diff(proposedEventEnd, publishedEventEnd,
                     Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("eventEnd", eventEnd);
                 isDocEquals = false;
             }
 
             if(!equals(proposedData.getValidityStart(), publishedData.getValidityStart()))
             {
                 String proposedValidityStart = proposedData.getValidityStart() != null ? proposedData
                     .getValidityStart().toString()
                     : null;
                 String publishedValidityStart = publishedData.getValidityStart() != null ? publishedData
                     .getValidityStart().toString()
                     : null;
                 validityStart = DiffUtil.diff(proposedValidityStart, publishedValidityStart,
                     Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("validityStart", validityStart);
                 isDocEquals = false;
             }
 
             if(!equals(proposedData.getValidityEnd(), publishedData.getValidityEnd()))
             {
                 String proposedValidityEnd = proposedData.getValidityEnd() != null ? proposedData
                     .getValidityEnd().toString() : null;
                 String publishedValidityEnd = publishedData.getValidityEnd() != null ? publishedData
                     .getValidityEnd().toString()
                     : null;
                 validityEnd = DiffUtil.diff(proposedValidityEnd, publishedValidityEnd,
                     Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("validityEnd", validityEnd);
                 isDocEquals = false;
             }
 
             isDocEquals = compareOrganizationData(templatingContext, isDocEquals, publishedData,
                 proposedData);
 
             if(!equals(publishedData.getSourceName(), proposedData.getSourceName()))
             {
                 sourceName = DiffUtil.diff(proposedData.getSourceName(), publishedData
                     .getSourceName(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("sourceName", sourceName);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getSourceUrl(), proposedData.getSourceUrl()))
             {
                 sourceUrl = DiffUtil.diff(proposedData.getSourceUrl(),
                     publishedData.getSourceUrl(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("sourceUrl", sourceUrl);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getProposerCredentials(), proposedData
                 .getProposerCredentials()))
             {
                 proposerCredentials = DiffUtil.diff(proposedData.getProposerCredentials(),
                     publishedData.getProposerCredentials(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("proposerCredentials", proposerCredentials);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getProposerEmail(), proposedData.getProposerEmail()))
             {
                 proposerEmail = DiffUtil.diff(proposedData.getProposerEmail(), publishedData
                     .getProposerEmail(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("proposerEmail", proposerEmail);
                 isDocEquals = false;
             }
             if(!equals(publishedData.getDescription(), proposedData.getDescription()))
             {
                 description = DiffUtil.diff(proposedData.getDescription(), publishedData
                     .getDescription(), Splitter.NEWLINE_SPLITTER, Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put("description", description);
                 isDocEquals = false;
             }
 
             List<CategoryResource> publishedDocCategories = new ArrayList<CategoryResource>(
                 publishedData.getSelectedCategories());
             List<CategoryResource> proposedDocCategories = new ArrayList<CategoryResource>(
                 proposedData.getSelectedCategories());
             proposedDocCategories.addAll(noAvalilableCategories);
 
             if(!equals(publishedDocCategories, proposedDocCategories))
             {
                 Collections.sort(publishedDocCategories, comparator);
                 Collections.sort(proposedDocCategories, comparator);
                 templatingContext.put("proposedDocCategories", proposedDocCategories);
                 templatingContext.put("publishedDocCategories", publishedDocCategories);
                 isDocEquals = false;
             }
 
             if(proposedData.isAttachmentsEnabled())
             {
                 if(!equals(proposedData.getAttachments(), publishedData.getAttachments())
                     || !equals(proposedData.getAttachmentDescriptions(), publishedData
                         .getAttachmentDescriptions()))
                 {
                     // Create a map of sequences for attachments descriptions
                     Map<Long, Sequence<DetailElement<String>>> attachmentsDesc = new HashMap<Long, Sequence<DetailElement<String>>>();
 
                     for(Resource attachment : publishedData.getAttachments())
                     {
                         int proposedAttachIn = proposedData.getAttachments().indexOf(attachment);
                         int publishedAttachIn = publishedData.getAttachments().indexOf(attachment);
 
                         if(proposedAttachIn != -1 && publishedAttachIn != -1)
                         {
                             attachmentsDesc.put(attachment.getId(), DiffUtil.diff(proposedData
                                 .getAttachmentDescription(proposedAttachIn), publishedData
                                 .getAttachmentDescription(publishedAttachIn),
                                 Splitter.WORD_BOUNDARY_SPLITTER));
                         }
                         else
                         {
                             attachmentsDesc.put(attachment.getId(), DiffUtil.diff("", publishedData
                                 .getAttachmentDescription(publishedAttachIn),
                                 Splitter.WORD_BOUNDARY_SPLITTER));
                         }
                     }
                     for(Resource attachment : proposedData.getAttachments())
                     {
                         if(!attachmentsDesc.containsKey(attachment.getId()))
                         {
                             int proposedAttachIn = proposedData.getAttachments()
                                 .indexOf(attachment);
                             attachmentsDesc.put(attachment.getId(), DiffUtil.diff(proposedData
                                 .getAttachmentDescription(proposedAttachIn), "",
                                 Splitter.WORD_BOUNDARY_SPLITTER));
                         }
                     }
 
                     templatingContext.put("attachmentsDesc", attachmentsDesc);
                     templatingContext.put("proposedDocAttachments", proposedData.getAttachments());
                     templatingContext
                         .put("publishedDocAttachments", publishedData.getAttachments());
 
                     isDocEquals = false;
                 }
             }
             if(!equals(proposedData.getEditorialNote(), ""))
             {
                 templatingContext.put("editorial_note", proposedData.getEditorialNote());
             }
             templatingContext.put("redactors_note", node.getRedactorsNote());
             templatingContext.put("remove_request", parameters.getLong("remove_request", 0L));
             templatingContext.put("isDocEquals", isDocEquals);
         }
         catch(Exception e)
         {
             throw new ProcessingException("Exception occured", e);
         }
     }
 
     private boolean compareOrganizationData(TemplatingContext templatingContext,
         boolean docsAreEqual, ProposedDocumentData publishedData, ProposedDocumentData proposedData)
     {
         Sequence<DetailElement<String>> diff;
         int maxOrgsCount = Math.max(publishedData.getOrganizations().size(), proposedData
             .getOrganizations().size());
         templatingContext.put("organizations_count", maxOrgsCount);
         
         for(int i = 0; i < maxOrgsCount; i++)
         {
             OrganizationData publishedOrg = OrganizationData.get(publishedData.getOrganizations(), i);
             OrganizationData proposedOrg = OrganizationData.get(proposedData.getOrganizations(), i);
             String prefix = "organization_" + (i+1) + "_";
             
            if(!equals(publishedOrg.getName(), proposedData.getName()))
             {
                 diff = DiffUtil.diff(proposedOrg.getName(), publishedOrg
                     .getName(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put(prefix + "name", diff);
                 docsAreEqual = false;
             }
             if(!equals(publishedOrg.getProvince(), proposedOrg.getProvince()))
             {
                 diff = DiffUtil.diff(proposedOrg.getProvince(),
                     publishedOrg.getProvince(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put(prefix + "province", diff);
                 docsAreEqual = false;
             }
             if(!equals(publishedOrg.getPostCode(), proposedOrg.getPostCode()))
             {
                 diff = DiffUtil.diff(proposedOrg.getPostCode(),
                     publishedOrg.getPostCode(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put(prefix + "postCode", diff);
                 docsAreEqual = false;
             }
             if(!equals(publishedOrg.getCity(), proposedOrg.getCity()))
             {
                 diff = DiffUtil.diff(proposedOrg.getCity(), publishedOrg
                     .getCity(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put(prefix + "city", diff);
                 docsAreEqual = false;
             }
             if(!equals(publishedOrg.getStreet(), proposedOrg.getStreet()))
             {
                 diff = DiffUtil.diff(proposedOrg.getStreet(), publishedOrg
                     .getStreet(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put(prefix + "street", diff);
                 docsAreEqual = false;
             }
             if(!equals(publishedOrg.getFax(), proposedOrg.getFax()))
             {
                 diff = DiffUtil.diff(proposedOrg.getFax(), publishedOrg
                     .getFax(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put(prefix + "fax", diff);
                 docsAreEqual = false;
             }
             if(!equals(publishedOrg.getEmail(), proposedOrg.getEmail()))
             {
                 diff = DiffUtil.diff(proposedOrg.getEmail(), publishedOrg
                     .getEmail(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put(prefix + "email", diff);
                 docsAreEqual = false;
             }
             if(!equals(publishedOrg.getPhone(), proposedOrg.getPhone()))
             {
                 diff = DiffUtil.diff(proposedOrg.getPhone(), publishedOrg
                     .getPhone(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put(prefix + "phone", diff);
                 docsAreEqual = false;
             }
             if(!equals(publishedOrg.getWww(), proposedOrg.getWww()))
             {
                 diff = DiffUtil.diff(proposedOrg.getWww(), publishedOrg
                     .getWww(), Splitter.WORD_BOUNDARY_SPLITTER);
                 templatingContext.put(prefix + "www", diff);
                 docsAreEqual = false;
             }
         }
         return docsAreEqual;
     }
 
     private boolean equals(String proposed, String published)
     {
         if(proposed == null && published == null)
         {
             return true;
         }
         else if(proposed != null && published != null && proposed.equals(published))
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     private boolean equals(Date proposed, Date published)
     {
         if(proposed == null && published == null)
         {
             return true;
         }
         else if(proposed != null && published != null && proposed.compareTo(published) == 0)
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     private <T> boolean equals(List<T> proposed, List<T> published)
     {
         if(proposed == null && published == null)
         {
             return true;
         }
         else if(proposed != null && published != null && proposed.containsAll(published)
             && published.containsAll(proposed))
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     @Override
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = context.getAttribute(CoralSession.class);
         return coralSession.getUserSubject().hasRole(getSite().getTeamMember());
     }
 }
