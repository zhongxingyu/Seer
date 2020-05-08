 package net.cyklotron.cms.catalogue;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.queryParser.QueryParser.Operator;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.Searcher;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.util.Version;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.objectledge.coral.datatypes.ResourceList;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.comparator.NameComparator;
 
 import net.cyklotron.cms.CmsNodeResourceImpl;
 import net.cyklotron.cms.catalogue.IndexCard.Property;
 import net.cyklotron.cms.category.CategoryException;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.related.RelatedService;
 import net.cyklotron.cms.search.PoolResource;
 import net.cyklotron.cms.search.SearchConstants;
 import net.cyklotron.cms.search.SearchException;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.site.SiteResource;
 
 public class CatalogueService
 {
     private final SearchService searchService;
 
     private final RelatedService relatedService;
 
     private final CategoryService categoryService;
 
     private static final String[] SEARCH_FIELDS = { "index_title", "index_abbreviation",
                     "index_content", "keywords", "authors", "sources" };
 
     /**
      * No-arg constructor for mocking.
      */
     protected CatalogueService()
     {
         this.searchService = null;
         this.relatedService = null;
         this.categoryService = null;
     }
 
     /**
      * Create CatalogueService instance.
      * 
      * @param searchService site service.
      * @param relatedService related resources service.
      * @param categoryService category service.
      */
     public CatalogueService(SearchService searchService, RelatedService relatedService,
         CategoryService categoryService)
     {
         this.searchService = searchService;
         this.relatedService = relatedService;
         this.categoryService = categoryService;
     }
 
     /**
      * Returns root node of catalogue configuration in a given site,
      * 
      * @param site a site.
      * @param coralSession Coral session.
      * @return root node of of catalogue configuration.
      */
     public Resource getConfigRoot(SiteResource site, CoralSession coralSession)
     {
         Resource[] res;
         res = coralSession.getStore().getResourceByPath(site, "applications/catalogue");
         if(res.length > 1)
         {
             throw new IllegalStateException("multiple catalogue parent nodes exist under site "
                 + site.getName());
         }
         else if(res.length == 1)
         {
             return res[0];
         }
         else
         {
             res = coralSession.getStore().getResourceByPath(site, "applications");
             if(res.length != 1)
             {
                 throw new IllegalStateException("applications node under site " + site.getName()
                     + " missing or duplicated");
             }
             try
             {
                 return CmsNodeResourceImpl.createCmsNodeResource(coralSession, "catalogue", res[0]);
             }
             catch(InvalidResourceNameException e)
             {
                 throw new RuntimeException("internal error", e);
             }
         }
     }
 
     /**
      * Create a new catalogue.
      * 
      * @param site the site where catalogue belongs.
      * @param name name of the catalogue.
      * @param category marker category.
      * @param searchPool search index pool.
      * @param requiredProperties TODO
      * @param coralSession Coral session
      * @return catalogue configuration resource.
      * @throws InvalidResourceNameException when name contains disallowed characters.
      */
     public CatalogueConfigResource createCatalogue(SiteResource site, String name,
         CategoryResource category, PoolResource searchPool, Set<Property> requiredProperties,
         CoralSession coralSession)
         throws InvalidResourceNameException
     {
         Resource configRoot = getConfigRoot(site, coralSession);
         CatalogueConfigResource config = CatalogueConfigResourceImpl.createCatalogueConfigResource(
             coralSession, name, configRoot);
         config.setCategory(category);
         config.setSearchPool(searchPool);
         config.setRequiredProperties(requiredProperties);
         config.update();
         return config;
     }
 
     /**
      * Update catalogue config.
      * 
      * @param config catalogue configuration resource.
      * @param name name of the catalogue.
      * @param category marker category.
      * @param searchPool search index pool.
      * @param requiredProperties TODO
      * @param coralSession Coral session
      * @throws InvalidResourceNameException when name contains disallowed characters.
      */
     public void updateCatalogue(CatalogueConfigResource config, String name,
         CategoryResource category, PoolResource searchPool, Set<Property> requiredProperties,
         CoralSession coralSession)
         throws InvalidResourceNameException
     {
         if(!config.getName().equals(name))
         {
             coralSession.getStore().setName(config, name);
         }
         config.setCategory(category);
         config.setSearchPool(searchPool);
         config.setRequiredProperties(requiredProperties);
         config.update();
     }
 
     /**
      * Validate a resource as candidate for an index item.
      * 
      * @param res the resource, either {@link DocumentNodeResource} or {@link FileResource}.
      * @param config catalogue configuration resource.
      * @param coralSession coral session
      * @return set of problems, hopefully empty.
      * @throws NotConfiguredException when configuration for catalogue in given site is missing.
      */
     public void validateIndexCardCandidate(Resource res, CatalogueConfigResource config,
         Set<Problem> problems, Set<IndexCard.Property> missingProperties, CoralSession coralSession)
         throws NotConfiguredException
     {
         if(res instanceof DocumentNodeResource)
         {
             validateDocumentIndexCardCandidate((DocumentNodeResource)res, config, problems, missingProperties,
                 coralSession);
         }
         else if(res instanceof FileResource)
         {
             validateFileIndexCardCandidate((FileResource)res, config, problems, missingProperties, coralSession);
         }
         else
         {
             problems.add(Problem.INVALID_CLASS);
         }
     }
 
     /**
      * Create an index card for a given resource.
      * 
      * @param res the resource, either {@link DocumentNodeResource} or {@link FileResource}. It is
      *        expected to be problem-free accoding to
      *        {@link #validateIndexCardCandidate(Resource, SiteResource, CoralSession)}.
      * @param config catalogue configuration resource.
      * @param coralSession coral session
      * @param locale locale used for sorting downloads by name, when manual ordering is not used.
      * @return an IndexCard
      * @throws IllegalArgumentException when resource has problems.
      * @throws NotConfiguredException when configuration for catalogue in given site is missing.
      */
     public IndexCard getIndexCard(Resource res, CatalogueConfigResource config,
         CoralSession coralSession, Locale locale)
         throws IllegalArgumentException, NotConfiguredException
     {
         Set<Problem> problems = EnumSet.noneOf(Problem.class);
         Set<Property> missingProperties = EnumSet.noneOf(Property.class);
         validateIndexCardCandidate(res, config, problems, missingProperties, coralSession);
         if(!problems.isEmpty())
         {
             throw new IllegalArgumentException("resource " + res.getIdString() + " has problems "
                 + problems.toString());
         }
         DocumentNodeResource doc;
         FileResource file;
         List<FileResource> downloads;
         if(res instanceof DocumentNodeResource)
         {
             doc = (DocumentNodeResource)res;
             downloads = findDownloads(doc, config, coralSession, locale);
             return new IndexCard(doc, downloads);
         }
         if(res instanceof FileResource)
         {
             file = (FileResource)res;
             List<DocumentNodeResource> docCandidates = findDescriptionDocs(file, config,
                 coralSession);
             if(docCandidates.size() == 1)
             {
                 doc = docCandidates.get(0);
             }
             else
             {
                 throw new IllegalArgumentException("multiple description documents for file "
                     + file.toString());
             }
             downloads = findDownloads(doc, config, coralSession, locale);
             return new IndexCard(doc, downloads);
         }
         throw new IllegalArgumentException("resource " + res.getIdString() + " has invalid class");
     }
 
     /**
      * Create a problem report for catalogue in a given site.
      * 
      * @param config catalogue configuration.
      * @param coralSession coral session.
      * @param locale locale used for sorting downloads by name, when manual ordering is not used.
      * @return report contents.
      * @throws NotConfiguredException when configuration for catalogue in given site is missing.
      * @throws CategoryException when category service error occurs.
      */
     public List<ProblemReportItem> getProblemReport(CatalogueConfigResource config,
         CoralSession coralSession, Locale locale)
         throws NotConfiguredException, CategoryException
     {
         List<ProblemReportItem> report = new ArrayList<ProblemReportItem>();
         CategoryResource markerCategory = config.getCategory();
         if(markerCategory == null)
         {
             throw new NotConfiguredException("marker category is not set");
         }
         Resource[] all = categoryService.getResources(coralSession, markerCategory, false);
         for(Resource res : all)
         {
             Set<Problem> problems = EnumSet.noneOf(Problem.class);
             Set<Property> missingProperties = EnumSet.noneOf(Property.class);
             validateIndexCardCandidate(res, config, problems, missingProperties, coralSession);
             if(!problems.isEmpty())
             {
                 List<DocumentNodeResource> descriptionDocCandidates = null;
                 List<FileResource> downloads = null;
                 if(res instanceof DocumentNodeResource)
                 {
                     downloads = findDownloads((DocumentNodeResource)res, config, coralSession,
                         locale);
                 }
                 if(res instanceof FileResource)
                 {
                     descriptionDocCandidates = findDescriptionDocs((FileResource)res, config,
                         coralSession);
                 }
                 report
                     .add(new ProblemReportItem(res, descriptionDocCandidates, downloads, problems, missingProperties));
             }
         }
         return report;
     }
 
     /**
      * Filter a problem report so that only items that have one or more problems from a predefined
      * set are included.
      * 
      * @param in a list of problem report items.
      * @param problemTypes a set of problems that should be included in the report.
      * @return a filtered list of problem report items.
      */
     public List<ProblemReportItem> filterProblemReport(List<ProblemReportItem> in,
         Set<Problem> problemTypes)
     {
         List<ProblemReportItem> out = new ArrayList<ProblemReportItem>(in.size());
         itemLoop: for(ProblemReportItem item : in)
         {
             for(Problem problem : item.getProblems())
             {
                 if(problemTypes.contains(problem))
                 {
                     out.add(item);
                     continue itemLoop;
                 }
             }
         }
         return out;
     }
 
     /**
      * Retrieve all catalogue index items.
      * 
      * @param config catalogue configuration resource.
      * @param coralSession coral session.
      * @param locale locale used for sorting downloads by name, when manual ordering is not used.
      * @return unordered list of index items, intended to be used with {@link IndexCardTableModel}.
      * @throws NotConfiguredException when configuration for catalogue in given site is missing.
      * @throws CategoryException when category service error occurs.
      */
     public List<IndexCard> getAllItems(CatalogueConfigResource config, CoralSession coralSession,
         Locale locale)
         throws NotConfiguredException, CategoryException
     {
         CategoryResource markerCategory = config.getCategory();
         if(markerCategory == null)
         {
             throw new NotConfiguredException("marker category is not set");
         }
         Resource[] all = categoryService.getResources(coralSession, markerCategory, false);
         // a set is used to make sure we have a single index card per description document
         Set<IndexCard> indexCards = new HashSet<IndexCard>();
         for(Resource res : all)
         {
             Set<Problem> problems = EnumSet.noneOf(Problem.class);
             Set<Property> missingProperties = EnumSet.noneOf(Property.class);
             validateIndexCardCandidate(res, config, problems, missingProperties, coralSession);
             if(problems.isEmpty())
             {
                 indexCards.add(getIndexCard(res, config, coralSession, locale));
             }
         }
         return new ArrayList<IndexCard>(indexCards);
     }
 
     /**
      * Retrieve index items through full text search.
      * 
      * @param queryString text query.
      * @param config catalogue configuration resource.
      * @param coralSession coral session.
      * @param locale locale used for sorting downloads by name, when manual ordering is not used.
      * @return unordered list of index items, intended to be used with {@link IndexCardTableModel}.
      * @throws NotConfiguredException
      * @throws SearchException
      */
     public List<IndexCard> search(String queryString, CatalogueConfigResource config,
         CoralSession coralSession, Locale locale)
         throws NotConfiguredException, SearchException
     {
         PoolResource searchPool = config.getSearchPool();
         if(searchPool == null)
         {
             throw new NotConfiguredException("searchPool is not set");
         }
         Analyzer analyzer = searchService.getAnalyzer(locale);
        QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_30, SEARCH_FIELDS, analyzer);
        parser.setDefaultOperator(Operator.AND);
         Set<Long> uniqueIds = new HashSet<Long>();
         Searcher searcher = null;
         try
         {
             Query query = parser.parse(queryString);
             searcher = searchService.getSearchingFacility().getSearcher(
                 new PoolResource[] { searchPool }, coralSession.getUserSubject());
             int numHits = searcher.maxDoc() > 0 ? searcher.maxDoc() : 1;
             TopDocs hits = searcher.search(query, null, numHits);
             for(ScoreDoc hit : hits.scoreDocs)
             {
                 org.apache.lucene.document.Document doc = searcher.doc(hit.doc);
                 uniqueIds.add(Long.parseLong(doc.get(SearchConstants.FIELD_ID)));
             }
         }
         catch(Exception e)
         {
             throw new SearchException("full text search failed", e);
         }
         finally
         {
             if(searcher != null)
             {
                 searchService.getSearchingFacility().returnSearcher(searcher);
             }
         }
         Set<IndexCard> indexCards = new HashSet<IndexCard>();
         for(long resId : uniqueIds)
         {
             try
             {
                 Resource res = coralSession.getStore().getResource(resId);
                 Set<Problem> problems = EnumSet.noneOf(Problem.class);
                 Set<Property> missingProperties = EnumSet.noneOf(Property.class);
                 validateIndexCardCandidate(res, config, problems, missingProperties, coralSession);
                 if(problems.isEmpty())
                 {
                     indexCards.add(getIndexCard(res, config, coralSession, locale));
                 }
             }
             catch(EntityDoesNotExistException e)
             {
                 // id of deleted resource in stale index, most probably
             }
         }
         return new ArrayList<IndexCard>(indexCards);
     }
 
     /**
      * Validate a document as candidate for an index item.
      * 
      * @param doc the document.
      * @param config catalogue configuration resource.
      * @param coralSession coral session
      * @return set of problems, hopefully empty.
      * @throws NotConfiguredException when configuration for catalogue in given site is missing.
      */
     private void validateDocumentIndexCardCandidate(DocumentNodeResource doc,
         CatalogueConfigResource config, Set<Problem> problems,
         Set<IndexCard.Property> missingProperties, CoralSession coralSession)
         throws NotConfiguredException
     {
         CategoryResource markerCategory = config.getCategory();
         if(markerCategory == null)
         {
             throw new NotConfiguredException("marker category is not set");
         }
         List<CategoryResource> categories = Arrays.asList(categoryService.getCategories(
             coralSession, doc, true));
         if(!categories.contains(markerCategory))
         {
             problems.add(Problem.REQUIRED_CATEGORY_MISSING);
         }
         try
         {
             Subject anonymous = coralSession.getSecurity().getSubject(Subject.ANONYMOUS);
             if(!doc.canView(coralSession, anonymous, new Date()))
             {
                 problems.add(Problem.DOCUMENT_NOT_ACCESSIBLE);
             }
         }
         catch(EntityDoesNotExistException e)
         {
             throw new RuntimeException("internal error", e);
         }
 
         try
         {
             // ensure that metadata is well formed, or missing all together
             if(doc.isMetaDefined())
             {
                 DocumentHelper.parseText(doc.getMeta());
             }
             IndexCard tempIndexCard = new IndexCard(doc, null);
             missingProperties.addAll(config.getRequiredProperties());
             missingProperties.removeAll(tempIndexCard.getDefinedProperties());
             if(!missingProperties.isEmpty())
             {
                 problems.add(Problem.MISSING_PROPERTIES);
             }
         }
         catch(org.dom4j.DocumentException e)
         {
             problems.add(Problem.INVALID_METADATA);
         }
     }
 
     /**
      * Validate a file as candidate for an index item.
      * 
      * @param file the file.
      * @param config catalogue configuration resource.
      * @param coralSession coral session
      * @return set of problems, hopefully empty.
      * @throws NotConfiguredException when configuration for catalogue in given site is missing.
      */
     private void validateFileIndexCardCandidate(FileResource file, CatalogueConfigResource config,
         Set<Problem> problems, Set<IndexCard.Property> missingProperties, CoralSession coralSession)
         throws NotConfiguredException
     {
         CategoryResource markerCategory = config.getCategory();
         if(markerCategory == null)
         {
             throw new NotConfiguredException("marker category is not set");
         }
         List<CategoryResource> categories = Arrays.asList(categoryService.getCategories(
             coralSession, file, true));
         if(!categories.contains(markerCategory))
         {
             problems.add(Problem.REQUIRED_CATEGORY_MISSING);
         }
 
         try
         {
             Subject anonymous = coralSession.getSecurity().getSubject(Subject.ANONYMOUS);
             if(!file.canView(coralSession, anonymous))
             {
                 problems.add(Problem.FILE_NOT_ACCESSIBLE);
             }
         }
         catch(EntityDoesNotExistException e)
         {
             throw new RuntimeException("internal error", e);
         }
 
         List<DocumentNodeResource> descriptionDocCandidates = findDescriptionDocs(file, config,
             coralSession);
         if(descriptionDocCandidates.size() < 1)
         {
             problems.add(Problem.MISSING_DESCRIPTION_DOC);
         }
         else if(descriptionDocCandidates.size() > 1)
         {
             problems.add(Problem.MULTIPLE_DESCRIPTION_DOCS);
         }
         else
         {
             validateDocumentIndexCardCandidate(descriptionDocCandidates.get(0),
                 config, problems, missingProperties, coralSession);
         }
     }
 
     /**
      * Find description document for a file.
      * 
      * @param file the file
      * @param config catalogue configuration resource.
      * @param coralSession coral session
      * @return a set of resources, may be empty.
      * @throws NotConfiguredException when configuration for catalogue in given site is missing.
      */
     private List<DocumentNodeResource> findDescriptionDocs(FileResource file,
         CatalogueConfigResource config, CoralSession coralSession)
         throws NotConfiguredException
     {
         CategoryResource markerCategory = config.getCategory();
         if(markerCategory == null)
         {
             throw new NotConfiguredException("marker category is not set");
         }
 
         Resource relatedFrom[] = relatedService.getRelatedFrom(coralSession, file);
 
         List<DocumentNodeResource> descriptionDocCandidates = new ArrayList<DocumentNodeResource>();
         for(Resource res : relatedFrom)
         {
             if(res instanceof DocumentNodeResource)
             {
                 List<CategoryResource> categories = Arrays.asList(categoryService.getCategories(
                     coralSession, res, true));
                 if(categories.contains(markerCategory))
                 {
                     descriptionDocCandidates.add((DocumentNodeResource)res);
                 }
             }
         }
         return descriptionDocCandidates;
     }
 
     /**
      * Find downloadable files for a description document.
      * 
      * @param doc description document
      * @param config catalogue configuration resource.
      * @param coralSession coral session
      * @return a set of files, may be empty
      * @throws NotConfiguredException when configuration for catalogue in given site is missing.
      */
     private List<FileResource> findDownloads(DocumentNodeResource doc,
         CatalogueConfigResource config, CoralSession coralSession, Locale locale)
         throws NotConfiguredException
     {
         CategoryResource markerCategory = config.getCategory();
         if(markerCategory == null)
         {
             throw new NotConfiguredException("marker category is not set");
         }
 
         Comparator<Resource> autoSequence = null;
         if(locale != null)
         {
             autoSequence = new NameComparator<Resource>(locale);
         }
         @SuppressWarnings("unchecked")
         ResourceList<Resource> maualSequence = doc.getRelatedResourcesSequence();
         Resource relatedTo[] = relatedService.getRelatedTo(coralSession, doc, maualSequence,
             autoSequence);
 
         List<FileResource> downloads = new ArrayList<FileResource>();
         for(Resource res : relatedTo)
         {
             if(res instanceof FileResource)
             {
                 List<CategoryResource> categories = Arrays.asList(categoryService.getCategories(
                     coralSession, res, true));
                 if(categories.contains(markerCategory))
                 {
                     downloads.add((FileResource)res);
                 }
             }
         }
         return downloads;
     }
 }
