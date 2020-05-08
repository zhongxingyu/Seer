 package com.celements.photo.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Vector;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.VelocityContext;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.model.EntityType;
 import org.xwiki.model.reference.AttachmentReference;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.EntityReference;
 import org.xwiki.model.reference.EntityReferenceResolver;
 import org.xwiki.model.reference.SpaceReference;
 import org.xwiki.model.reference.WikiReference;
 
 import com.celements.common.classes.IClassCollectionRole;
 import com.celements.navigation.NavigationClasses;
 import com.celements.navigation.service.ITreeNodeService;
 import com.celements.photo.container.ImageDimensions;
 import com.celements.photo.container.ImageLibStrings;
 import com.celements.photo.image.GenerateThumbnail;
 import com.celements.photo.utilities.ImportFileObject;
 import com.celements.photo.utilities.Unzip;
 import com.celements.search.lucene.ILuceneSearchService;
 import com.celements.search.lucene.LuceneSearchException;
 import com.celements.search.lucene.LuceneSearchResult;
 import com.celements.search.lucene.query.LuceneQuery;
 import com.celements.web.classcollections.OldCoreClasses;
 import com.celements.web.plugin.cmd.AttachmentURLCommand;
 import com.celements.web.plugin.cmd.NextFreeDocNameCommand;
 import com.celements.web.service.IWebUtilsService;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Attachment;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.web.Utils;
 
 @Component
 public class ImageService implements IImageService {
 
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       ImageService.class);
 
   @Requirement
   EntityReferenceResolver<String> stringRefResolver;
 
   @Requirement("celements.oldCoreClasses")
   IClassCollectionRole oldCoreClasses;
 
   @Requirement("celements.celNavigationClasses")
   IClassCollectionRole navigationClasses;
 
   @Requirement
   IWebUtilsService webUtilsService;
 
   @Requirement
   ITreeNodeService treeNodeService;
 
   NextFreeDocNameCommand nextFreeDocNameCmd;
 
   @Requirement
   private ILuceneSearchService searchService;
 
   @Requirement
   Execution execution;
 
   AttachmentURLCommand attURLCmd;
   
   private XWikiContext getContext() {
     return (XWikiContext) execution.getContext().getProperty("xwikicontext");
   }
 
   private OldCoreClasses getOldCoreClasses() {
     return (OldCoreClasses) oldCoreClasses;
   }
 
   private NavigationClasses getNavigationClasses() {
     return (NavigationClasses) navigationClasses;
   }
 
   public BaseObject getPhotoAlbumObject(DocumentReference galleryDocRef
       ) throws XWikiException {
     XWikiDocument galleryDoc = getContext().getWiki().getDocument(galleryDocRef,
         getContext());
     BaseObject galleryObj = galleryDoc.getXObject(getOldCoreClasses(
         ).getPhotoAlbumClassRef(getContext().getDatabase()));
     return galleryObj;
   }
 
   public BaseObject getPhotoAlbumNavObject(DocumentReference galleryDocRef
       ) throws XWikiException, NoGalleryDocumentException {
     XWikiDocument galleryDoc = getContext().getWiki().getDocument(galleryDocRef,
         getContext());
     BaseObject navObj = galleryDoc.getXObject(getNavigationClasses(
         ).getNavigationConfigClassRef(getContext().getDatabase()));
     if (navObj == null) {
       throw new NoGalleryDocumentException();
     }
     return navObj;
   }
 
   public SpaceReference getPhotoAlbumSpaceRef(DocumentReference galleryDocRef
       ) throws NoGalleryDocumentException {
     try {
       String spaceName = getPhotoAlbumNavObject(galleryDocRef).getStringValue(
           NavigationClasses.MENU_SPACE_FIELD);
       return new SpaceReference(spaceName, webUtilsService.getWikiRef(
           galleryDocRef));
     } catch (XWikiException exp) {
       LOGGER.error("Failed to getPhotoAlbumSpaceRef.", exp);
     }
     return null;
   }
 
   public int getPhotoAlbumMaxHeight(DocumentReference galleryDocRef
       ) throws NoGalleryDocumentException {
     try {
       int maxImageHeight = getPhotoAlbumObject(galleryDocRef).getIntValue("height2");
       //TODO allow template to reduce height
       if (!StringUtils.isEmpty(getContext().getRequest().getParameter("slideContent"))) {
         maxImageHeight = Math.max(maxImageHeight - 20, 0);
       }
       return maxImageHeight;
     } catch (XWikiException exp) {
       LOGGER.error("Failed to getPhotoAlbumSpaceRef.", exp);
     }
     return 2000;
   }
 
   public int getPhotoAlbumMaxWidth(DocumentReference galleryDocRef
       ) throws NoGalleryDocumentException {
     try {
       return getPhotoAlbumObject(galleryDocRef).getIntValue("photoWidth");
     } catch (XWikiException exp) {
       LOGGER.error("Failed to getPhotoAlbumSpaceRef.", exp);
     }
     return 2000;
   }
 
   private DocumentReference getDocRefFromFullName(String collDocName) {
     DocumentReference eventRef = new DocumentReference(stringRefResolver.resolve(
         collDocName, EntityType.DOCUMENT));
     eventRef.setWikiReference(new WikiReference(getContext().getDatabase()));
     LOGGER.debug("getDocRefFromFullName: for [" + collDocName + "] got reference ["
         + eventRef + "].");
     return eventRef;
   }
 
   public ImageDimensions getDimension(String imageFullName)
       throws XWikiException {
     String fullName = imageFullName.split(";")[0];
     String imageFileName = imageFullName.split(";")[1];
     DocumentReference docRef = getDocRefFromFullName(fullName);
     AttachmentReference imgRef = new AttachmentReference(imageFileName, docRef);
     return getDimension(imgRef);
   }
 
   public ImageDimensions getDimension(AttachmentReference imgRef) throws XWikiException {
     DocumentReference docRef = (DocumentReference) imgRef.getParent();
     XWikiDocument theDoc = getContext().getWiki().getDocument(docRef, getContext());
     XWikiAttachment theAttachment = theDoc.getAttachment(imgRef.getName());
     ImageDimensions imageDimensions = null;
     GenerateThumbnail genThumbnail = new GenerateThumbnail();
     InputStream imageInStream = null;
     try {
       imageInStream = theAttachment.getContentInputStream(getContext());
       imageDimensions = genThumbnail.getImageDimensions(imageInStream);
     } finally {
       if(imageInStream != null) {
         try {
           imageInStream.close();
         } catch (IOException ioe) {
           LOGGER.error("Error closing InputStream.", ioe);
         }
       } else {
         imageDimensions = new ImageDimensions();
       }
     }
     return imageDimensions;
   }
   
   /**
    * getRandomImages computes a set of <num> randomly chosen images
    * from the given AttachmentList. It chooses the Images without duplicates if
    * possible.
    */
   public List<Attachment> getRandomImages(DocumentReference galleryRef, int num) {
     try {
       Document imgDoc = getContext().getWiki().getDocument(galleryRef, getContext()
           ).newDocument(getContext());
       List<Attachment> allImagesList = webUtilsService.getAttachmentListSorted(imgDoc,
           "AttachmentAscendingNameComparator", true);
       if (allImagesList.size() > 0) {
         List<Attachment> preSetImgList = prepareMaxCoverSet(num, allImagesList);
         List<Attachment> imgList = new ArrayList<Attachment>(num);
         Random rand = new Random();
         for (int i=1; i<=num ; i++) {
           int nextimg = rand.nextInt(preSetImgList.size());
           imgList.add(preSetImgList.remove(nextimg));
         }
         return imgList;
       }
     } catch (XWikiException e) {
       LOGGER.error(e);
     }
     return Collections.emptyList();
   }
   
   <T> List<T> prepareMaxCoverSet(int num, List<T> allImagesList) {
     List<T> preSetImgList = new Vector<T>(num);
     preSetImgList.addAll(allImagesList);
     for(int i=2; i <= coveredQuotient(allImagesList.size(), num); i++) {
       preSetImgList.addAll(allImagesList);
     }
     return preSetImgList;
   }
   
   int coveredQuotient(int divisor, int dividend) {
     if (dividend >= 0) {
       return ( (dividend + divisor - 1) / divisor);
     } else {
       return (dividend / divisor);
     }
   }
 
   private NextFreeDocNameCommand getNextFreeDocNameCmd() {
     if (this.nextFreeDocNameCmd == null) {
       this.nextFreeDocNameCmd = new NextFreeDocNameCommand();
     }
     return this.nextFreeDocNameCmd;
   }
 
   private AttachmentURLCommand getAttURLCmd() {
     if (attURLCmd == null) {
       attURLCmd = new AttachmentURLCommand();
     }
     return attURLCmd;
   }
 
   public boolean checkAddSlideRights(DocumentReference galleryDocRef) {
     try {
       DocumentReference newSlideDocRef = getNextFreeDocNameCmd().getNextTitledPageDocRef(
           getPhotoAlbumSpaceRef(galleryDocRef).getName(), "Testname", getContext());
       String newSlideDocFN = webUtilsService.getRefDefaultSerializer().serialize(
           newSlideDocRef);
       return (getContext().getWiki().getRightService().hasAccessLevel("edit",
           getContext().getUser(), newSlideDocFN, getContext()));
     } catch (XWikiException exp) {
       LOGGER.error("failed to checkAddSlideRights for [" + galleryDocRef + "].", exp);
     } catch (NoGalleryDocumentException exp) {
       LOGGER.debug("failed to checkAddSlideRights for no gallery document ["
           + galleryDocRef + "].", exp);
     }
     return false;
   }
 
   public boolean addSlideFromTemplate(DocumentReference galleryDocRef,
       String slideBaseName, String attFullName) {
     try {
       DocumentReference slideTemplateRef = getImageSlideTemplateRef();
       String gallerySpaceName = getPhotoAlbumSpaceRef(galleryDocRef).getName();
       String filename = attFullName.replaceAll("^.*;(.*)$", "$1");
       String clearedAttName = getContext().getWiki().clearName(filename.replaceAll(
           "^(.*)\\.[a-zA-Z]{3,4}$", "$1"), true, true, 
           getContext());
       String slideDocName = slideBaseName + clearedAttName;
       DocumentReference newSlideDocRef = getNextFreeDocNameCmd().getNextTitledPageDocRef(
           gallerySpaceName, slideDocName, getContext());
       if (getContext().getWiki().copyDocument(slideTemplateRef, newSlideDocRef, true,
           getContext())) {
         XWikiDocument newSlideDoc = getContext().getWiki().getDocument(newSlideDocRef,
             getContext());
         newSlideDoc.setDefaultLanguage(webUtilsService.getDefaultLanguage(
             gallerySpaceName));
         //TODO refactor and use com.celements.web.plugin.cmd.CreateDocumentCommand instead
         Date creationDate = new Date();
         newSlideDoc.setLanguage("");
         newSlideDoc.setCreationDate(creationDate);
         newSlideDoc.setContentUpdateDate(creationDate);
         newSlideDoc.setDate(creationDate);
         newSlideDoc.setCreator(getContext().getUser());
         newSlideDoc.setAuthor(getContext().getUser());
         newSlideDoc.setTranslation(0);
         String imgURL = getAttURLCmd().getAttachmentURL(attFullName, "download",
             getContext());
         String resizeParam = "celwidth=" + getPhotoAlbumMaxWidth(galleryDocRef)
             + "&celheight=" + getPhotoAlbumMaxHeight(galleryDocRef);
         String fullImgURL = imgURL + ((imgURL.indexOf("?") < 0)?"?":"&") + resizeParam;
         VelocityContext vcontext = (VelocityContext)getContext().get("vcontext");
         vcontext.put("imageURL", fullImgURL);
         vcontext.put("attFullName", attFullName);
         Map<String, String> metaTagMap = Collections.emptyMap();
         DocumentReference attDocRef = webUtilsService.resolveDocumentReference(
             attFullName.replaceAll("^(.*);.*$", "$1"));
         DocumentReference centralFBDocRef = null;
         String centralFB = getContext().getWiki().getWebPreference("cel_centralfilebase", 
             getContext());
         if((centralFB != null) && !"".equals(centralFB.trim())) {
           centralFBDocRef = webUtilsService.resolveDocumentReference(centralFB);
         }
         if(getContext().getWiki().exists(attDocRef, getContext()) 
             && !attDocRef.equals(centralFBDocRef)) {
           LOGGER.debug("get meta tags from attachment document " + attDocRef);
           XWikiDocument attDoc = getContext().getWiki().getDocument(attDocRef, getContext(
               ));
           metaTagMap = getMetaTagObjectsFromDoc(attDoc);
         } else if(attDocRef.equals(centralFBDocRef)) {
           LOGGER.debug("get meta tags for central file base image" + attDocRef);
           LuceneQuery query = searchService.createQuery();
           query.add(searchService.createRestriction("Celements2.PageType.page_type", 
               "\"DMS-Document\""));
           query.add(searchService.createRestriction("Classes.PhotoMetainfoClass.name", 
               "\"cleared_filename\""));
           query.add(searchService.createRestriction("Classes.PhotoMetainfoClass." + 
               "description", "\"" + filename + "\""));
           LuceneSearchResult searchResult = searchService.search(query, null, null);
           List<EntityReference> resultList = Collections.emptyList();
           try {
             resultList = searchResult.getResults();
           } catch (LuceneSearchException lse) {
             LOGGER.error("Exception searching for imported images", lse);
           }
           LOGGER.debug("addSlideFromTemplate: lucene query = '" + query.getQueryString() +
               "'");
           LOGGER.debug("addSlideFromTemplate: DMS-Document for " + filename + " found: " +
               resultList.size());
           if(resultList.size() > 0) {
             XWikiDocument separateDoc = getContext().getWiki().getDocument(
                 new DocumentReference(resultList.get(0)), getContext());
            metaTagMap.putAll(getMetaTagObjectsFromDoc(separateDoc));
           } else {
             LOGGER.debug("getting meta tags for file [" + filename + "] on " + attDocRef);
             Map<String, String> map = getMetaInfoService().getAllTags(attDocRef, filename);
             for(String key : map.keySet()) {
               metaTagMap.put(cleanMetaTagKey(key), cleanMetaTagValue(key, map.get(key)));
             }
           }
         } else {
           LOGGER.debug("don't get meta tags attachment doc [" + attDocRef + "] does not" +
               "exist and is not central file base " + centralFBDocRef);
         }
         vcontext.put("metaTagMap", metaTagMap);
         DocumentReference slideContentRef = new DocumentReference(getContext(
             ).getDatabase(), "Templates", "ImageSlideImportContent");
         String slideContent = webUtilsService.renderInheritableDocument(slideContentRef, 
             getContext().getLanguage(), webUtilsService.getDefaultLanguage());
         newSlideDoc.setContent(slideContent);
         fixMenuItemPosition(newSlideDoc);
         getContext().getWiki().saveDocument(newSlideDoc, "add default image slide"
             + " content", true, getContext());
         return true;
       } else {
         LOGGER.warn("failed to copy slideTemplateRef [" + slideTemplateRef
             + "] to new slide doc [" + newSlideDocRef + "].");
       }
     } catch (NoGalleryDocumentException exp) {
       LOGGER.error("failed to addSlideFromTemplate because no gallery doc.", exp);
     } catch (XWikiException exp) {
       LOGGER.error("failed to addSlideFromTemplate.", exp);
     }
     return false;
   }
 
   Map<String, String> getMetaTagObjectsFromDoc(XWikiDocument attDoc) {
     Map<String, String> metaTagMap = new HashMap<String, String>();
     DocumentReference tagClassRef = webUtilsService.resolveDocumentReference(
         "Classes.PhotoMetainfoClass");
     List<BaseObject> metaObjs = attDoc.getXObjects(tagClassRef);
     if(metaObjs != null) {
       for(BaseObject tag : metaObjs) {
         if(tag != null) {
           metaTagMap.put(tag.getStringValue("name"), tag.getStringValue(
               "description"));
         }
       }
     }
     return metaTagMap;
   }
 
   private boolean fixMenuItemPosition(XWikiDocument newSlideDoc) {
     if (treeNodeService.isTreeNode(newSlideDoc.getDocumentReference())) {
       BaseObject menuItemObj = newSlideDoc.getXObject(getNavigationClasses(
           ).getMenuItemClassRef(getContext().getDatabase()));
       if (menuItemObj != null) {
         int numElem = treeNodeService.getSubNodesForParent(
             newSlideDoc.getDocumentReference().getLastSpaceReference(), "").size();
         menuItemObj.setIntValue(NavigationClasses.MENU_POSITION_FIELD, numElem);
         return true;
       }
     }
     return false;
   }
 
   String cleanMetaTagValue(String key, String value) {
     key = key.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
     key = key.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
     key = key.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
     System.out.println(key);
     return value.replaceAll("^.*?" + key + " - (.*)$", "$1");
   }
 
   String cleanMetaTagKey(String key) {
     return key.replaceAll("^(\\[.*\\] )?(.*)$", "$2");
   }
 
   public DocumentReference getImageSlideTemplateRef() {
     DocumentReference slideTemplateRef = new DocumentReference(
         getContext().getDatabase(), "ImageGalleryTemplates", "NewImageGallerySlide");
     if(!getContext().getWiki().exists(slideTemplateRef, getContext())) {
       slideTemplateRef = new DocumentReference("celements2web", "ImageGalleryTemplates",
           "NewImageGallerySlide");
     }
     return slideTemplateRef;
   }
 
   public Map<String, String> getImageURLinAllAspectRatios(XWikiAttachment ximage) {
     ImageDimensions dim = null;
     try {
       dim = (new GenerateThumbnail()).getThumbnailDimensions(ximage.getContentInputStream(
           getContext()), -1, -1, false, null);
     } catch (XWikiException xwe) {
       LOGGER.error("Exception reading image dimensions", xwe);
     }
     Map<String, String> urlMap = new HashMap<String, String>();
     if(dim != null) {
       String baseURL = ximage.getDoc().getExternalAttachmentURL(ximage.getFilename(), 
           "download", getContext());
       urlMap.put("orig", baseURL);
       if(baseURL.indexOf("?") < 0) {
         baseURL += "?";
       } else if(!baseURL.endsWith("&")) {
         baseURL += "&";
       }
       urlMap.put("1:1", baseURL + getFixedAspectURL(dim, 1, 1));
       urlMap.put("3:4", baseURL + getFixedAspectURL(dim, 3, 4));
       urlMap.put("4:3", baseURL + getFixedAspectURL(dim, 4, 3));
       urlMap.put("16:9", baseURL + getFixedAspectURL(dim, 16, 9));
       urlMap.put("16:10", baseURL + getFixedAspectURL(dim, 16, 10));
     }
     return urlMap;
   }
 
   String getFixedAspectURL(ImageDimensions dim, int xFact, int yFact) {
     double width = dim.getWidth();
     double height = dim.getHeight();
     double isAspRatio = width / (double)height;
     double targetAspRatio = xFact / (double)yFact;
     double epsylon = 0.00001;
     String urlParams = "";
     //only crop if dim does not matche target aspect ratio
     if(Math.abs(isAspRatio - targetAspRatio) > epsylon) {
       if(isAspRatio < targetAspRatio) {
         urlParams += "cropX=0&cropW=" + (int)width;
         int newHeight = (int)Math.floor(width * (1 / targetAspRatio));
         int top = (int)Math.floor((height - (double)newHeight) / 2);
         urlParams += "&cropY=" + top + "&cropH=" + newHeight;
       } else {
         int newWidth = (int)Math.floor(height * targetAspRatio);
         int left = (int)Math.floor((width - (double)newWidth) / 2);
         urlParams += "cropX=" + left + "&cropW=" + newWidth;
         urlParams += "&cropY=0&cropH=" + (int)height;
       }
     }
     return urlParams;
   }
   
   private IMetaInfoService getMetaInfoService() {
     return (IMetaInfoService) Utils.getComponent(IMetaInfoService.class);
   }
 
   /**
    * Get a List of all attachments in the specified archive and the suggested 
    * action when importing.
    * 
    * @param importFile Zip archive to check the files for the existence in the gallery.
    * @param galleryDoc Gallery Document to check for the files.
    * @param context XWikiContext
    * @return List of {@link ImportFileObject} for each file.
    * @throws XWikiException
    */
   public List<ImportFileObject>getAttachmentFileListWithActions(
       XWikiAttachment importFile, XWikiDocument galleryDoc) throws XWikiException {
     List<ImportFileObject> resultList = new ArrayList<ImportFileObject>();
     
     if(importFile != null){
       if(isZipFile(importFile)){
         List<String> fileList;
         try {
           fileList = (new Unzip()).getZipContentList(
               IOUtils.toByteArray(importFile.getContentInputStream(getContext())));
           String fileSep = System.getProperty("file.separator");
           for (Iterator<String> fileIterator = fileList.iterator(); fileIterator.hasNext();) {
             String fileName = (String) fileIterator.next();
             if(!fileName.endsWith(fileSep)
                 && !fileName.startsWith(".") && !fileName.contains(fileSep + ".")){
               ImportFileObject file = new ImportFileObject(fileName, getActionForFile(
                   fileName, galleryDoc));
               resultList.add(file);
             }
           }
         } catch (IOException ioe) {
           LOGGER.error("Error reading file.", ioe);
         }
       } else if(isImgFile(importFile)){
         ImportFileObject file = new ImportFileObject(importFile.getFilename(), 
             getActionForFile(importFile.getFilename(), galleryDoc));
         resultList.add(file);
       }
     } else{
       LOGGER.error("zipFile='null' - galleryDoc='" + galleryDoc.getDocumentReference() + 
           "'");
     }
     
     return resultList;
   }
 
   /**
    * For a given filename return if, in the specified gallery, its import 
    * should be added, overwritten or skiped.
    * 
    * @param fileName Filename of the file to check.
    * @param galleryDoc Document of the gallery to check if the file already exists.
    * @return action when importing: -1 skip, 0 overwrite, 1 add
    */
   private short getActionForFile(String fileName, XWikiDocument galleryDoc) {
     short action = ImportFileObject.ACTION_SKIP;
     DocumentReference importClassRef = webUtilsService.resolveDocumentReference(
         "Classes.ImportClass");
     boolean isImportToFilebase = getContext().getDoc().getXObjectSize(importClassRef) == 0;
     LOGGER.debug("getActionForFile [" + fileName + "] on gallery doc [" + galleryDoc 
         + "], isImportToFilebase [" + isImportToFilebase + "]");
     if(isImgFile(fileName) || isImportToFilebase){
       fileName = fileName.replace(System.getProperty("file.separator"), ".");
       fileName = getContext().getWiki().clearName(fileName, false, true, getContext());
       XWikiAttachment attachment = galleryDoc.getAttachment(fileName);
       if(attachment == null){
         action = ImportFileObject.ACTION_ADD;
       } else{
         action = ImportFileObject.ACTION_OVERWRITE;
       }
     }
     
     return action;
   }
 
   private boolean isZipFile(XWikiAttachment file) {
     return file.getMimeType(getContext()).equalsIgnoreCase(ImageLibStrings.MIME_ZIP) 
         || file.getMimeType(getContext()).equalsIgnoreCase(
             ImageLibStrings.MIME_ZIP_MICROSOFT);
   }
 
   private boolean isImgFile(String fileName) {
     return fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_BMP)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_GIF)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPE)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPG)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPEG)
         || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_PNG);
   }
 
   private boolean isImgFile(XWikiAttachment file) {
     return file.getMimeType(getContext()).equalsIgnoreCase("image/"
         + ImageLibStrings.MIME_BMP)
         || file.getMimeType(getContext()).equalsIgnoreCase("image/"
             + ImageLibStrings.MIME_GIF)
         || file.getMimeType(getContext()).equalsIgnoreCase("image/"
             + ImageLibStrings.MIME_JPE)
         || file.getMimeType(getContext()).equalsIgnoreCase("image/"
             + ImageLibStrings.MIME_JPG)
         || file.getMimeType(getContext()).equalsIgnoreCase("image/"
             + ImageLibStrings.MIME_JPEG)
         || file.getMimeType(getContext()).equalsIgnoreCase("image/"
             + ImageLibStrings.MIME_PNG);
   }
 
 }
