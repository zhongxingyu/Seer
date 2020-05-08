 /**
  * Copyright (C) 2000 - 2012 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
  * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
  * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
  * text describing the FLOSS exception, and it is also available here:
  * "http://www.silverpeas.org/legal/licensing"
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.silverpeas.tags.kmelia;
 
 import java.rmi.NoSuchObjectException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.silverpeas.attachment.AttachmentServiceFactory;
 import org.silverpeas.attachment.model.DocumentType;
 import org.silverpeas.attachment.model.SimpleDocument;
 import org.silverpeas.attachment.model.SimpleDocumentPK;
 import org.silverpeas.search.SearchEngineFactory;
 import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
 import org.silverpeas.search.searchEngine.model.ParseException;
 import org.silverpeas.search.searchEngine.model.QueryDescription;
 import org.silverpeas.wysiwyg.WysiwygException;
 import org.silverpeas.wysiwyg.control.WysiwygController;
 
 import com.silverpeas.comment.model.CommentPK;
 import com.silverpeas.comment.model.CommentedPublicationInfo;
 import com.silverpeas.comment.service.CommentService;
 import com.silverpeas.comment.service.CommentServiceFactory;
 import com.silverpeas.form.importExport.XMLField;
 import com.silverpeas.notation.ejb.NotationBm;
 import com.silverpeas.notation.model.Notation;
 import com.silverpeas.notation.model.NotationPK;
 import com.silverpeas.tags.ComponentTagUtil;
 import com.silverpeas.tags.util.SiteTagUtil;
 import com.silverpeas.tags.util.VisibilityException;
 import com.silverpeas.util.FileUtil;
 import com.silverpeas.util.ForeignPK;
 import com.silverpeas.util.StringUtil;
 
 import com.stratelia.silverpeas.silvertrace.SilverTrace;
 import com.stratelia.webactiv.beans.admin.ComponentInst;
 import com.stratelia.webactiv.beans.admin.SpaceInst;
 import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
 import com.stratelia.webactiv.kmelia.model.KmeliaPublication;
 import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
 import com.stratelia.webactiv.util.EJBUtilitaire;
 import com.stratelia.webactiv.util.FileServerUtils;
 import com.stratelia.webactiv.util.JNDINames;
 import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
 import com.stratelia.webactiv.util.node.control.NodeBm;
 import com.stratelia.webactiv.util.node.model.NodeDetail;
 import com.stratelia.webactiv.util.node.model.NodeI18NDetail;
 import com.stratelia.webactiv.util.node.model.NodePK;
 import com.stratelia.webactiv.util.publication.control.PublicationBm;
 import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
 import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
 import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;
 import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
 import com.stratelia.webactiv.util.publication.model.CompletePublication;
 import com.stratelia.webactiv.util.publication.model.PublicationDetail;
 import com.stratelia.webactiv.util.publication.model.PublicationI18N;
 import com.stratelia.webactiv.util.publication.model.PublicationPK;
 
 public class KmeliaTagUtil extends ComponentTagUtil {
 
   private static final String BEGIN_EDITO = "@";
   private String componentId = null;
   private String spaceId = null;
   private String visibilityFilter = null;
   private KmeliaBm kscEjb = null;
   private PublicationBm publicationBm = null;
   private NodeBm nodeBm = null;
   private CommentService commentService = null;
   private NotationBm notationBm = null;
 
   public KmeliaTagUtil(String spaceId, String componentId, String userId) {
     super(componentId, userId);
 
     this.spaceId = spaceId;
     this.componentId = componentId;
   }
 
   public KmeliaTagUtil(String componentId, String userId) {
     super(componentId, userId);
 
     this.componentId = componentId;
   }
 
   public KmeliaTagUtil(String spaceId, String componentId, String userId, boolean checkAuthorization) {
     super(componentId, userId, checkAuthorization);
 
     this.spaceId = spaceId;
     this.componentId = componentId;
   }
 
   private void initEJB() {
     SilverTrace.info("kmelia", "KMeliaTagUtil.initEJB()", "root.MSG_GEN_ENTER_METHOD",
         "componentId = " + componentId);
 
     kscEjb = null;
     try {
       getKmeliaBm();
     } catch (Exception e) {
       throw new KmeliaRuntimeException("KmeliaTagUtil.initEJB()", SilverpeasRuntimeException.ERROR,
           "root.EX_CANT_GET_REMOTE_OBJECT", e);
     }
   }
 
   private void initPublicationEJB() {
     SilverTrace.info("kmelia", "KmeliaTagUtil.initPublicationEJB()", "root.MSG_GEN_ENTER_METHOD");
     publicationBm = null;
     try {
       getPublicationBm();
     } catch (Exception e) {
       throw new KmeliaRuntimeException("KmeliaTagUtil.initPublicationEJB()",
           SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
     }
   }
 
   private KmeliaBm getKmeliaBm() {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getKmeliaBm()", "root.MSG_GEN_ENTER_METHOD");
     if (kscEjb == null) {
       try {
         SilverTrace.info("kmelia", "KMeliaTagUtil.getKmeliaBm()", "root.MSG_GEN_PARAM_VALUE",
             "Try to access to EJBHome " + JNDINames.KMELIABM_EJBHOME);
         kscEjb = EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBm.class);
       } catch (Exception e) {
         throw new KmeliaRuntimeException("KmeliaTagUtil.getKmeliaBm()",
             SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
       }
     }
     return kscEjb;
   }
 
   private CommentService getCommentService() {
     if (commentService == null) {
       CommentServiceFactory serviceFactory = CommentServiceFactory.getFactory();
       commentService = serviceFactory.getCommentService();
     }
     return commentService;
   }
 
   private NotationBm getNotationBm() {
     if (notationBm == null) {
       try {
         notationBm = EJBUtilitaire.getEJBObjectRef(JNDINames.NOTATIONBM_EJBHOME, NotationBm.class);
       } catch (Exception e) {
         throw new KmeliaRuntimeException("KmeliaTagUtil.getNotationBm",
             SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
       }
     }
     return notationBm;
   }
 
   public void setVisibilityFilter(String visibilityFilter) {
     this.visibilityFilter = visibilityFilter;
   }
 
   public String getVisibilityFilter() {
     return visibilityFilter;
   }
 
   private String getSpaceId() {
     return this.spaceId;
   }
 
   private String getComponentId() {
     return this.componentId;
   }
 
   private String getSiteLanguage() {
     return SiteTagUtil.getLanguage();
   }
 
   public SpaceInst getSpaceInst() throws Exception {
     SpaceInst spaceInst = null;
     ComponentInst componentInst = getComponentInst();
     if (componentInst != null) {
       String fatherSpaceId = componentInst.getDomainFatherId();
       spaceInst = getAdmin().getSpaceInst(fatherSpaceId);
     }
     return spaceInst;
   }
 
   public ComponentInst getComponentInst() throws Exception {
     ComponentInst compoInst = getAdmin().getComponentInst(this.componentId);
     return compoInst;
   }
 
   public String getComponentLabel() throws Exception {
     return getComponentInst().getLabel();
   }
 
   /**
    * ***********************************************************************************
    */
   /* KMelia - Gestion des publications */
   /**
    * ***********************************************************************************
    */
   private boolean checkPublicationStatus(String pubId) throws RemoteException, VisibilityException {
     PublicationDetail pubDetail = getPublicationDetail(pubId);
     return checkPublicationStatus(pubDetail);
   }
 
   private boolean checkPublicationStatus(PublicationDetail pubDetail) throws VisibilityException {
     String pubStatus = pubDetail.getStatus();
     if (SiteTagUtil.isProdMode()) {
       if (!"Valid".equalsIgnoreCase(pubDetail.getStatus())) {
         throw new VisibilityException();
       }
     } else if (SiteTagUtil.isRecetteMode()) {
       if (!"Valid".equalsIgnoreCase(pubStatus) && !"ToValidate".equalsIgnoreCase(pubStatus)) {
         throw new VisibilityException();
       }
     } else {
       // Site's mode is dev : all publications are visible
     }
     return true;
   }
 
   /**
    * Get filtered publications
    *
    * @param publicationDetails
    * @return Collection
    */
   private Collection<PublicationDetail> filterPublications(
       Collection<PublicationDetail> publicationDetails) {
     List<PublicationDetail> filteredPublications = new ArrayList<PublicationDetail>();
     for (PublicationDetail pubDetail : publicationDetails) {
       try {
         if (visibilityFilter != null) {
           if (pubDetail.getName().startsWith(visibilityFilter)) {
             continue;
           }
         }
         checkPublicationStatus(pubDetail);
         checkPublicationLocation(pubDetail);
         filteredPublications.add(getTranslatedPublication(pubDetail, null));
       } catch (VisibilityException ae) {
         // this publication cannot be display according its status and site's mode
       }
     }
     return filteredPublications;
   }
 
   private void checkPublicationLocation(String pubId) throws RemoteException, VisibilityException {
     PublicationDetail pubDetail = getPublicationDetail(pubId);
     checkPublicationLocation(pubDetail);
   }
 
   private void checkPublicationLocation(PublicationDetail pubDetail) throws VisibilityException {
    //instanceId must correspond to componentId set in the kmeliaTag
    if(! pubDetail.getInstanceId().equals(getComponentId())){
      throw new VisibilityException();
    }
    
    //publication must not be in basket (node 1)
     List fathers = (List) getKmeliaBm().getPublicationFathers(pubDetail.getPK());
     if (fathers == null || fathers.isEmpty() || (fathers.size() == 1
         && "1".equals(((NodePK) fathers.get(0)).getId()))) {
       throw new VisibilityException();
     }
   }
 
   public Integer getPublicationCommentsCount(String pubId) throws RemoteException,
       VisibilityException {
     Integer commentsCount = new Integer(0);
     PublicationPK publicationKey = new PublicationPK(pubId, this.getComponentId());
     int count = getCommentService().getCommentsCountOnPublication(PublicationDetail.
         getResourceType(), publicationKey);
     if (count > 0) {
       commentsCount = new Integer(count);
     }
     return commentsCount;
   }
 
   /**
    * Get publicationDetail
    *
    * @param pubId
    * @return PublicationDetail
    * @throws RemoteException
    * @throws VisibilityException
    */
   public PublicationDetail getPublicationDetail(String pubId) throws RemoteException,
       VisibilityException {
     return getPublicationDetail(pubId, null);
   }
 
   /**
    * Get publicationDetail
    *
    * @param pubId
    * @param language
    * @return PublicationDetail
    * @throws RemoteException
    * @throws VisibilityException
    */
   public PublicationDetail getPublicationDetail(String pubId, String language) throws
       VisibilityException {
     SilverTrace.
         info("kmelia", "KMeliaTagUtil.getPublicationDetail()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     PublicationDetail pubDetail = getPublicationBm().getDetail(getPublicationPK(pubId));
     // check status according to site's mode
     checkPublicationStatus(pubDetail);
     checkPublicationLocation(pubDetail);
     pubDetail = getTranslatedPublication(pubDetail, language);
     return pubDetail;
   }
 
   public Collection<PublicationDetail> getLinkedPublications(String pubId) throws
       VisibilityException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getLinkedPublications()",
         "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
     CompletePublication pubComplete = getPublicationBm().getCompletePublication(getPublicationPK(
         pubId));
     List<ForeignPK> targets = pubComplete.getLinkList();
     SilverTrace.info("kmelia", "KMeliaTagUtil.getLinkedPublications()",
         "root.MSG_GEN_ENTER_METHOD", "nb linked publications = " + targets.size());
     List<PublicationPK> targetPKs = new ArrayList<PublicationPK>();
     for (ForeignPK foreignPk : targets) {
       targetPKs.add(new PublicationPK(foreignPk.getId(), foreignPk.getInstanceId()));
     }
     return filterPublications(getPublicationBm().getPublications(targetPKs));
   }
 
   public Collection<PublicationDetail> getPublicationsOnSameSubject(String pubId) throws
       VisibilityException, ParseException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getPublicationsOnSameSubject()",
         "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
 
     // get name and keywords of the publication to launch a request.
     PublicationDetail pubDetail = getPublicationBm().getDetail(getPublicationPK(pubId));
 
     // launch the search
     QueryDescription query = new QueryDescription(pubDetail.getName() + " " + pubDetail.
         getKeywords());
     query.setSearchingUser(getUserId());
     query.addSpaceComponentPair(getSpaceId(), getComponentId());
     List<MatchingIndexEntry> searchResult = SearchEngineFactory.getSearchEngine().search(query).
         getEntries();
     // get each publication according to result's list
     List<PublicationPK> pubPKs = new ArrayList<PublicationPK>(searchResult.size());
     for (MatchingIndexEntry mie : searchResult) {
       if (mie != null && !mie.getObjectId().equals(pubId)) {
         if ("Publication".equalsIgnoreCase(mie.getObjectType())) {
           pubPKs.add(getPublicationPK(mie.getObjectId()));
         }
       }
     }
     return filterPublications(getPublicationBm().getPublications(pubPKs));
   }
 
   public InfoDetail getInfoDetail(String pubId) throws RemoteException, VisibilityException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getInfoDetail()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     try {
       checkPublicationStatus(pubId);
       checkPublicationLocation(pubId);
       return getPublicationBm().getInfoDetail(getPublicationPK(pubId));
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getInfoDetail(pubId);
     }
   }
 
   @Deprecated
   public CompletePublication getCompletePublication(String pubId) throws VisibilityException {
     SilverTrace.
         info("kmelia", "KMeliaTagUtil.getCompletePublication()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     CompletePublication cPublication = getPublicationBm().getCompletePublication(getPublicationPK(
         pubId));
 
     PublicationDetail pub = cPublication.getPublicationDetail();
     checkPublicationStatus(pub);
     checkPublicationLocation(pub);
     return cPublication;
 
   }
 
   public KmeliaPublication getPublication(String pubId) throws VisibilityException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getFullPublication()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     KmeliaPublication publication = getKmeliaBm().getPublication(new PublicationPK(pubId,
         componentId));
     PublicationDetail pub = publication.getDetail();
     checkPublicationStatus(pub);
     checkPublicationLocation(pub);
     return publication;
   }
 
   public Collection<PublicationDetail> getAllPublications() {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getAllPublications()", "root.MSG_GEN_ENTER_METHOD");
     return filterPublications(getPublicationBm().getAllPublications(new PublicationPK("useless",
         componentId), "P.pubCreationDate desc"));
   }
 
   /**
    * The parameter must be split to get 3 parameters. The delimiter is a ,
    */
   public Collection<PublicationDetail> getPublicationsByTopic(String topicIdAndColumnNameAndSort) {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getPublicationsByTopic()",
         "root.MSG_GEN_ENTER_METHOD", "topicIdAndColumnNameAndSort = " + topicIdAndColumnNameAndSort);
     return getPublications(topicIdAndColumnNameAndSort, false);
   }
 
   public Collection<PublicationDetail> getPublicationsBySubTree(String topicIdAndColumnNameAndSort) {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getPublicationsBySubTree()",
         "root.MSG_GEN_ENTER_METHOD", "topicIdAndColumnNameAndSort = " + topicIdAndColumnNameAndSort);
     return getPublications(topicIdAndColumnNameAndSort, true);
   }
 
   /**
    * The parameter must be split to get 3 parameters. The delimiter is a ,
    */
   private Collection<PublicationDetail> getPublications(String topicIdAndColumnNameAndSort,
       boolean recursive) {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getPublications()", "root.MSG_GEN_ENTER_METHOD",
         "topicIdAndColumnNameAndSort = " + topicIdAndColumnNameAndSort);
     StringTokenizer tokenizer = new StringTokenizer(topicIdAndColumnNameAndSort, ",");
     int i = 1;
     String param;
     String topicId = "";
     String columnName = null;
     String sort = null;
     while (tokenizer.hasMoreTokens()) {
       param = tokenizer.nextToken();
       if (i == 1) {
         topicId = param;
       } else if (i == 2) {
         columnName = param;
       } else if (i == 3) {
         sort = param;
       }
       i++;
     }
 
     SilverTrace.info("kmelia", "KMeliaTagUtil.getPublications()", "root.MSG_GEN_PARAM_VALUE",
         "topicId = " + topicId);
     SilverTrace.info("kmelia", "KMeliaTagUtil.getPublications()", "root.MSG_GEN_PARAM_VALUE",
         "sort = " + sort);
 
     String sortString = getSortString(columnName, sort);
 
     // NodePK nodePK = new NodePK(topicId, spaceId, componentId);
     NodePK nodePK = getNodePK(topicId);
     Collection<PublicationDetail> publications;
     List<String> nodeIds = new ArrayList<String>();
     if (recursive) {
       // get all nodes of the subtree where root is topicId
       List<NodeDetail> nodes = getNodeBm().getSubTree(nodePK);
       for (NodeDetail nodeDetail : nodes) {
         if (NodeDetail.STATUS_VISIBLE.equals(nodeDetail.getStatus())) {
           nodeIds.add(nodeDetail.getNodePK().getId());
         }
       }
     } else {
       // get only publications linked to the topic identified by topicId
       nodeIds.add(nodePK.getId());
     }
 
     PublicationPK pubPK = new PublicationPK("useless", componentId);
     if (SiteTagUtil.isDevMode()) {
       // all publications are shown
       // we do nothing
       publications = getPublicationBm().getDetailsByFatherIds(nodeIds, pubPK, sortString);
     } else if (SiteTagUtil.isRecetteMode()) {
       // all publications are shown except the ones which are in draft
       List<String> statusList = new ArrayList<String>();
       statusList.add("Valid");
       statusList.add("ToValidate");
       publications = getPublicationBm().getDetailsByFatherIdsAndStatusList(nodeIds, pubPK,
           sortString, statusList);
     } else {
       // Production mode is enabled. Only validated publications are shown
       String status = "Valid";
       publications = getPublicationBm().getDetailsByFatherIdsAndStatus(nodeIds, pubPK,
           sortString, status);
     }
     if (visibilityFilter != null) {
       Iterator<PublicationDetail> it = publications.iterator();
       while (it.hasNext()) {
         PublicationDetail pubDetail = it.next();
         if (pubDetail.getName().startsWith(visibilityFilter)) {
           it.remove();
         }
       }
     }
     if (!StringUtil.isDefined(getSiteLanguage())) {
       return publications;
     }
     if (publications != null && StringUtil.isDefined(getSiteLanguage())) {
       for (PublicationDetail publi : publications) {
         publi.setName(publi.getName(getSiteLanguage()));
         publi.setDescription(publi.getDescription(getSiteLanguage()));
         publi.setKeywords(publi.getKeywords(getSiteLanguage()));
       }
     }
     return publications;
   }
 
   private String getSortString(String columnName, String sort) {
     if (columnName != null) {
       // first letter must be in upper case
       String firstLetter = columnName.substring(0, 1);
       columnName = firstLetter.toUpperCase() + columnName.substring(1, columnName.length());
     }
 
     SilverTrace.info("kmelia", "KMeliaTagUtil.getSortString()", "root.MSG_GEN_PARAM_VALUE",
         "columnName = " + columnName);
     String sortString = null;
     if (columnName != null) {
       if (columnName.equals("Order")) {
         sortString = "F.pub" + columnName;
       } else {
         sortString = "P.pub" + columnName;
       }
       if (sort != null) {
         sortString += " ";
         sortString += sort;
       }
     }
     SilverTrace.info("kmelia", "KMeliaTagUtil.getSortString()", "root.MSG_GEN_PARAM_VALUE",
         "sortString = " + sortString);
     return sortString;
   }
 
   public String getPublicationHTMLContent(String pubId) throws RemoteException,
       VisibilityException,
       WysiwygException {
 
     // check publication
     checkPublicationStatus(pubId);
     checkPublicationLocation(pubId);
 
     String htmlContent = "";
     String wysiwygContent = getWysiwyg(pubId);
     if (wysiwygContent == null) {
       // the publication have no content of type wysiwyg
       // The content is maybe stored in a database model
       // CompletePublication completePublication = getKmeliaBm().getCompletePublication(pubId);
       CompletePublication completePublication = getPublicationBm().
           getCompletePublication(getPublicationPK(
           pubId));
       InfoDetail infoDetail = completePublication.getInfoDetail();
       ModelDetail modelDetail = completePublication.getModelDetail();
       if (infoDetail != null && modelDetail != null) {
         // the publication have some content
         // we merge the content with the model's displayer template
         htmlContent = getModelHtmlContent(modelDetail, infoDetail);
       }
     } else {
       htmlContent = wysiwygContent;
     }
     return parseHtmlContent(htmlContent);
   }
 
   public Collection<NodeDetail> getPublicationPath(String pubId) throws RemoteException {
     Collection<NodeDetail> path = new ArrayList<NodeDetail>();
     SilverTrace.info("kmelia", "KMeliaTagUtil.getPathList()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     List<Collection<NodeDetail>> paths = new ArrayList<Collection<NodeDetail>>(getKmeliaBm()
         .getPathList(getPublicationPK(pubId)));
     if (paths.size() > 0) {
       // get only the first path
       List<NodeDetail> pathInReverse = new ArrayList<NodeDetail>(paths.get(0));
       Collections.reverse(pathInReverse);// reverse the path from root to leaf
       path = pathInReverse;
     }
     return path;
   }
 
   public NodeDetail getTopic(String topicId) throws RemoteException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getTopic()", "root.MSG_GEN_ENTER_METHOD",
         "topicId = " + topicId);
     NodeDetail topic = getNodeBm().getDetail(getNodePK(topicId));
     topic = getTranslatedNode(topic, null);
     if (SiteTagUtil.isDevMode()) {
       // Web site is in developpement mode
       // We get all sub topics (visibles and invisibles)
       topic.setChildrenDetails(getTranslatedSubTopics(topic, false));
     } else {
       // Web site is in 'recette' or 'production' mode
       // We get only visible subtopics
       topic.setChildrenDetails(getTranslatedSubTopics(topic, true));
     }
     return topic;
 
   }
 
   /**
    * Used to get the most commented publication
    *
    * @param condition number,formName,topicId for for example 10,fiche_produit,30 or
    * -1,fiche_article,22
    * @return Collection of PublicationDetail
    */
   public Collection getMostCommentedPublication(String condition) {
     StringTokenizer tokenizer = new StringTokenizer(condition, ",");
     String number = tokenizer.nextToken();
     int numberPublication = Integer.parseInt(number);
     String formName = tokenizer.nextToken();
     String topicId = tokenizer.nextToken();
     List comments = new ArrayList();
     try {
       if (topicId.equals("-1")) {
         ArrayList commentsCountSorted = new ArrayList();
         commentsCountSorted.addAll(this.getCommentService().getAllMostCommentedPublicationsInfo());
         Iterator iter = commentsCountSorted.iterator();
         while (iter.hasNext() && comments.size() < numberPublication) {
           CommentedPublicationInfo commentInfo = (CommentedPublicationInfo) iter.next();
           PublicationDetail publication = this.getPublicationDetail(commentInfo.getPublicationId());
           if (publication.getInfoId().equals(formName)) {
             comments.add(commentInfo);
           }
         }
         return comments;
       } else {
         Collection publications = getPublications(topicId, true);
         Iterator iter = publications.iterator();
         List commentsPks = new ArrayList();
         PublicationDetail publication;
         PublicationPK publicationPK;
         while (iter.hasNext()) {
           publication = (PublicationDetail) iter.next();
           if (publication.getInfoId().equals(formName)) {
             publicationPK = publication.getPK();
             commentsPks.add(
                 new CommentPK(publicationPK.getId(), null, publicationPK.getInstanceId()));
           }
         }
         if (!commentsPks.isEmpty()) {
           comments = getCommentService().getMostCommentedPublicationsInfo(PublicationDetail.
               getResourceType(), commentsPks);
           if (comments.size() > numberPublication) {
             comments = comments.subList(0, numberPublication);
           }
         }
       }
     } catch (Exception e) {
       // TODO Auto-generated catch block
       Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
     }
     return comments;
   }
 
   public Collection getMostPopularPublicationsNotations(String topicIdAndNotationsCount) throws
       RemoteException {
     StringTokenizer st = new StringTokenizer(topicIdAndNotationsCount, ",");
     String topicId = st.nextToken();
     if (st.hasMoreTokens()) {
       int notationsCount = Integer.parseInt(st.nextToken());
       if (notationsCount > 0) {
         Collection publications = getPublications(topicId, true);
         Iterator iter = publications.iterator();
         ArrayList notationsPks = new ArrayList();
         PublicationDetail publication;
         PublicationPK publicationPK;
         while (iter.hasNext()) {
           publication = (PublicationDetail) iter.next();
           publicationPK = publication.getPK();
           notationsPks.add(new NotationPK(publicationPK.getId(),
               publicationPK.getComponentName(), Notation.TYPE_PUBLICATION));
         }
 
         if (!notationsPks.isEmpty()) {
           return getNotationBm().getBestNotations(notationsPks, notationsCount);
         }
       }
     }
     return new ArrayList();
   }
 
   public PublicationDetail getTopicEdito(String topicId) throws RemoteException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getTopicEdito()", "root.MSG_GEN_ENTER_METHOD",
         "topicId = " + topicId);
     // get all publications of topicId
     String topicIdAndColumnNameAndSort = topicId + ",CreationDate,Desc";
     List publications = (List) getPublicationsByTopic(topicIdAndColumnNameAndSort);
     PublicationDetail publi, publiEdito = null;
     for (int p = 0; p < publications.size(); p++) {
       publi = (PublicationDetail) publications.get(p);
       if (publi.getName().substring(0, BEGIN_EDITO.length()).equals(BEGIN_EDITO)) {
         publiEdito = publi;
         break;
       }
     }
     return publiEdito;
 
   }
 
   private List getTranslatedSubTopics(NodeDetail node, boolean showOnlyVisibles) {
     List result = new ArrayList();
 
     List subTopics = (List) node.getChildrenDetails();
     if (subTopics != null && subTopics.size() > 0) {
       NodeDetail subTopic;
       for (int i = 0; i < subTopics.size(); i++) {
         subTopic = (NodeDetail) subTopics.get(i);
         subTopic = getTranslatedNode(subTopic, null);
         if (showOnlyVisibles) {
           if (NodeDetail.STATUS_VISIBLE.equals(subTopic.getStatus())) {
             result.add(subTopic);
           }
         } else {
           result.add(subTopic);
         }
       }
     }
     return result;
   }
 
   private boolean checkTopicStatus(String nodeId) throws RemoteException, VisibilityException {
     NodeDetail nodeDetail = getTopic(nodeId);
     return checkTopicStatus(nodeDetail);
   }
 
   private boolean checkTopicStatus(NodeDetail nodeDetail) throws VisibilityException {
     String status = nodeDetail.getStatus();
     if (!SiteTagUtil.isDevMode()) {
       if (NodeDetail.STATUS_INVISIBLE.equalsIgnoreCase(status)) {
         throw new VisibilityException();
       }
     }
     return true;
   }
 
   public Collection<NodeDetail> getTopicPath(String topicId) {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getTopicPath()", "root.MSG_GEN_ENTER_METHOD",
         "topicId = " + topicId);
     List<NodeDetail> pathInReverse = new ArrayList(getNodeBm().getPath(getNodePK(topicId)));
     Collections.reverse(pathInReverse);
     return pathInReverse;
   }
 
   public Collection getTreeView(String topicId) throws RemoteException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getTreeView()", "root.MSG_GEN_ENTER_METHOD",
         "topicId = " + topicId);
     List<NodeDetail> tree = getNodeBm().getSubTree(getNodePK(topicId));
     // if topicId is the root, remove "basket" and "declassified zone"
     if (NodePK.ROOT_NODE_ID.equals(topicId)) {
       tree = removeSpecificNodes(tree);
     }
 
     if (SiteTagUtil.isDevMode()) {
       // Web site is in developpement mode
       // We get all topics (visibles and invisibles)
       if (StringUtil.isDefined(SiteTagUtil.getLanguage())) {
         for (NodeDetail node : tree) {
           getTranslatedNode(node, SiteTagUtil.getLanguage());
         }
       } else {
         return tree;
       }
     } else {
       // Web site is in 'recette' or 'production' mode
       // We get only visible topics
       tree = getVisibleTreeView(tree);
     }
     return tree;
   }
 
   private List<NodeDetail> removeSpecificNodes(List<NodeDetail> tree) {
     List<NodeDetail> nodes = new ArrayList<NodeDetail>(tree.size());
     for (NodeDetail node : tree) {
       NodePK id = node.getNodePK();
       if (!id.isTrash() && !id.isUnclassed()) {
         nodes.add(node);
       }
     }
     return nodes;
   }
 
   private List<NodeDetail> getVisibleTreeView(List<NodeDetail> tree) {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getVisibleTreeView()", "root.MSG_GEN_ENTER_METHOD");
     List<NodeDetail> visibleNodes = new ArrayList<NodeDetail>();
     List<String> invisibleNodes = new ArrayList<String>();
     for (int i = 0; i < tree.size(); i++) {
       NodeDetail node = tree.get(i);
       node = getTranslatedNode(node, null);
       if ((NodeDetail.STATUS_INVISIBLE.equals(node.getStatus())) || ((visibilityFilter != null)
           && node.getName(getSiteLanguage()).startsWith(visibilityFilter))) {
         if (i == 0) {
           return visibleNodes;
         } else {
           // the node is invisible. We do not add it to the result treeview
           invisibleNodes.add(node.getNodePK().getId());
         }
       } else {
         if (invisibleNodes.contains(node.getFatherPK().getId())) {
           // the father is invisible. Even if the node is Visible, we do not add it to the result
           // treeview.
           invisibleNodes.add(node.getNodePK().getId());
         } else {
           node.setChildrenDetails(getTranslatedSubTopics(node, true));
           visibleNodes.add(node);
         }
       }
     }
     return visibleNodes;
   }
 
   public String getTopicHTMLContent(String topicId) throws RemoteException, VisibilityException,
       WysiwygException {
 
     // check topic status
     checkTopicStatus(topicId);
 
     String wysiwygContent = getWysiwyg("Node_" + topicId);
     if (wysiwygContent == null) {
       wysiwygContent = "";
     }
     return parseHtmlContent(wysiwygContent);
   }
 
   public int getSilverObjectId(String pubId) throws RemoteException {
     SilverTrace.info("kmelia", "KmeliaTagUtil.getSilverObjectId()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     return getKmeliaBm().getSilverObjectId(getPublicationPK(pubId));
   }
 
   /**
    * renvoit l'URL de la vignette de la publication, ou chaine vide s'il n'y a pas de vignette
    *
    * @param pubId
    * @return String not null
    */
   public String getVignetteURL(String pubId) throws RemoteException, VisibilityException {
     SilverTrace.info("kmelia", "KmeliaTagUtil.getVignetteURL()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     String imageURL = "";
     PublicationDetail pubDetail = getPublicationDetail(pubId);
     if (pubDetail.getImage() != null) {
       if (pubDetail.getImage().startsWith("/")) {// image provenant de la photothèque
         imageURL = pubDetail.getImage();
       } else {// image téléchargée
         imageURL = "vignette?ComponentId=" + pubDetail.getInstanceId() + "&SourceFile=" + pubDetail.
             getImage() + "&MimeType=image/jpeg&Directory=images";
       }
     }
     return imageURL;
   }
 
   /**
    * ***********************************************************************************
    */
   /* KMelia - Gestion des validations */
   /**
    * ***********************************************************************************
    */
   public Collection getPublicationsToValidate() throws RemoteException {
     SilverTrace.info("kmelia", "KmeliaTagUtil.getPublicationsToValidate()",
         "root.MSG_GEN_ENTER_METHOD");
     return getPublicationBm().getPublicationsByStatus("ToValidate", getPublicationPK("useless"));
   }
 
   /**
    * ***********************************************************************************
    */
   /* Gestion des fichiers joints */
   /**
    * ***********************************************************************************
    */
   public Collection getAttachments(String pubId) throws RemoteException, VisibilityException {
     SilverTrace.info("kmelia", "KmeliaTagUtil.getAttachments()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     // check publication
     checkPublicationStatus(pubId);
     checkPublicationLocation(pubId);
     return AttachmentServiceFactory.getAttachmentService().listDocumentsByForeignKeyAndType(
         getPublicationPK(pubId), DocumentType.attachment, SiteTagUtil.getLanguage());
   }
 
   /**
    * Get an attachmentDetail
    *
    * @param attachmentId
    * @return AttachmentDetail
    */
   public SimpleDocument getAttachment(String attachmentId) throws VisibilityException {
     SilverTrace.info("kmelia", "KmeliaTagUtil.getAttachment()", "root.MSG_GEN_ENTER_METHOD",
         "attachmentId = " + attachmentId);
     SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().searchDocumentById(
         new SimpleDocumentPK(attachmentId), getSiteLanguage());
     return attachment;
   }
 
   public String getWysiwyg(String pubId) throws RemoteException, VisibilityException,
       WysiwygException {
     SilverTrace.info("kmelia", "KmeliaTagUtil.getWysiwyg()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     try {
       if (!pubId.startsWith("Node")) {
         // check publication
         checkPublicationStatus(pubId);
         checkPublicationLocation(pubId);
       }
       String wysiwyg = WysiwygController.load(componentId, pubId, getSiteLanguage());
       return parseHtmlContent(wysiwyg);
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getWysiwyg(pubId);
     }
   }
 
   private String getModelHtmlContent(ModelDetail model, InfoDetail infos) {
     String toParse = model.getHtmlDisplayer();
     Iterator textIterator = infos.getInfoTextList().iterator();
     Iterator imageIterator = infos.getInfoImageList().iterator();
     StringBuilder htmlContent = new StringBuilder();
 
     int posit = toParse.indexOf("%WA");
     while (posit != -1) {
       if (posit > 0) {
         htmlContent.append(toParse.substring(0, posit));
         toParse = toParse.substring(posit);
       }
       if (toParse.startsWith("%WATXTDATA%")) {
         if (textIterator.hasNext()) {
           InfoTextDetail textDetail = (InfoTextDetail) textIterator.next();
           htmlContent.append(encode(textDetail.getContent()));
         }
         toParse = toParse.substring(11);
       } else if (toParse.startsWith("%WAIMGDATA%")) {
         if (imageIterator.hasNext()) {
           InfoImageDetail imageDetail = (InfoImageDetail) imageIterator.next();
           String logicalName = imageDetail.getLogicalName();
           String physicalName = imageDetail.getPhysicalName();
           String mimeType = imageDetail.getType();
           String type = logicalName.
               substring(logicalName.lastIndexOf(".") + 1, logicalName.length());
 
           if (FileUtil.isImage(logicalName)) {
             String url = FileServerUtils.getUrl(imageDetail.getPK().getComponentName(), logicalName,
                 physicalName, mimeType, "images");
             url = "http://fakeServer:fakePort" + url;
             htmlContent.append("<IMG BORDER=\"0\" SRC=\"").append(url).append("\">");
           }
         }
         toParse = toParse.substring(11);
       }
 
       // et on recommence
       posit = toParse.indexOf("%WA");
     }
     return htmlContent.toString();
   }
 
   private String encode(String javastring) {
     StringBuilder res = new StringBuilder();
     if (javastring == null) {
       return res.toString();
     }
     for (int i = 0; i < javastring.length(); i++) {
       switch (javastring.charAt(i)) {
         case '\n':
           res.append("<br>");
           break;
         case '\t':
           res.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
           break;
         default:
           res.append(javastring.charAt(i));
       }
     }
     return res.toString();
   }
 
   /**
    * @throws VisibilityException
    * @throws RemoteException
    */
   public String getParsedFieldValue(String pubIdAndFieldName) throws RemoteException,
       VisibilityException {
     StringTokenizer tokenizer = new StringTokenizer(pubIdAndFieldName, ",");
 
     String pubId = "";
     String fieldName = "";
 
     int i = 1;
     while (tokenizer.hasMoreTokens()) {
       String param = tokenizer.nextToken();
       if (i == 1) {
         pubId = param;
       } else if (i == 2) {
         fieldName = param;
       }
       i++;
     }
 
     PublicationDetail detail = getPublicationDetail(pubId);
     String fieldValue = detail.getFieldValue(fieldName);
     if (fieldValue == null) {
       fieldValue = "";
     }
 
     // Particular case of Image Field (in the XML forms)
     String webContext = SiteTagUtil.getServerContext();
     List<XMLField> xmlFields = detail.getXmlFields();
     XMLField xmlField;
     for (int x = 0; x < xmlFields.size(); x++) {
       xmlField = xmlFields.get(x);
       if (fieldName.equals(xmlField.getName())) {
         if (xmlField.getValue() != null && (xmlField.getValue().startsWith("image_") || xmlField.
             getValue().startsWith("file_"))) {
           // prefix the value with the webContext
           fieldValue = webContext + fieldValue;
           break;
         }
       }
     }
     return parseHtmlContent(fieldValue);
   }
 
   /**
    * There's a problem with images. Links to images are like
    * http://server_name:port/silverpeas/FileServer/image.jpg?... The servlet FileServer is used.
    * This works in the traditional context of silverpeas. But, it does not works in the taglibs
    * context because the FileServer is securised. In taglibs context, the servlet WebFileServer must
    * be used. The string http://server_name:port/silverpeas/FileServer must be replaced by
    * http://webServer_name:port/webContext/attached_file The web context is provided by the tag
    * Site.
    *
    * @param htmlContent not null
    */
   public String parseHtmlContent(String htmlContent) {
     if (htmlContent != null && htmlContent.length() > 0) {
       String content = htmlContent;
       String webContext = SiteTagUtil.getServerContext() + SiteTagUtil.getFileServerName() + "/";
       content = convertToWebUrl(content, "/FileServer/", webContext);
       content = convertToWebUrl(content, "/GalleryInWysiwyg/", webContext);
       content = convertRestToWebUrl(content, "/attached_file/", webContext);
 
       int finPath = 0;
       int debutPath;
       StringBuilder newWysiwygText = new StringBuilder();
       String link;
       while (content.indexOf("href=\"", finPath) > -1) {
         debutPath = content.indexOf("href=\"", finPath);
         debutPath += 6;
 
         newWysiwygText.append(content.substring(finPath, debutPath));
 
         finPath = content.indexOf("\"", debutPath);
         link = content.substring(debutPath, finPath);
 
         int d = link.indexOf("../../");
         if (d != -1) {
           // C'est un lien relatif
           d += 6;
           newWysiwygText.append("/silverpeas/");
           newWysiwygText.append(link.substring(d, link.length()));
         } else {
           newWysiwygText.append(link);
         }
       }
       newWysiwygText.append(content.substring(finPath, content.length()));
       return newWysiwygText.toString().replaceAll("&amp;", "&");
     }
     return htmlContent;
   }
 
   private PublicationPK getPublicationPK(String pubId) {
     return new PublicationPK(pubId, getComponentId());
   }
 
   private NodePK getNodePK(String nodeId) {
     return new NodePK(nodeId, getComponentId());
   }
 
   /**
    * Replace the String "/xxx/servletMapping par "attachmentUrl Used in the Wysiwyg field of the XML
    * Forms
    *
    * @param content : the String to replace, for example the String
    * /silverpeas/attached_file/componentId
    * /kmelia24/attachmentId/19578/lang/fr/name/ESAT_Rhone-Alpes_synthese.jpg
    * @param servletMapping : for example the String /attached_file/
    * @param attachmentUrl : the webContext, for example the String /webContext/attached_file/ For
    * example replace the String
    * /silverpeas/attached_file/componentId/kmelia24/attachmentId/19578/lang
    * /fr/name/ESAT_Rhone-Alpes_synthese.jpg
    * @return the String replaced, in the example, return
    * /webContext/attached_file/componentId/kmelia24
    * /attachmentId/19578/lang/fr/name/ESAT_Rhone-Alpes_synthese.jpg
    */
   public String convertRestToWebUrl(String content, String servletMapping, String attachmentUrl) {
     return content.replaceAll("\"/[^/]*" + servletMapping, '"' + attachmentUrl);
   }
 
   public String convertToWebUrl(String content, String servletMapping, String webContext) {
     String htmlContent = content;
     while (htmlContent.indexOf(servletMapping) > -1) {
       int place = htmlContent.indexOf(servletMapping);
       String debut = htmlContent.substring(0, place);
       int srcPlace = debut.lastIndexOf('\"');
       debut = debut.substring(0, srcPlace + 1);
       String suite = htmlContent.substring(place + servletMapping.length(), htmlContent.length());
       int srcEndPlace = suite.indexOf('\"', srcPlace + 1);
       String url = "";
       if (srcEndPlace > 0) {
         url = suite.substring(0, srcEndPlace);
         url = url.replaceAll("&amp;", "&");
       } else {
         srcEndPlace = 0;
 
       }
       htmlContent = debut + webContext + url + suite.substring(srcEndPlace);
     }
     return htmlContent;
   }
 
   /**
    * Get translated Publication in current site lang or lang as parameter
    *
    * @param pubDetail
    * @return PublicationDetail
    */
   private PublicationDetail getTranslatedPublication(PublicationDetail pubDetail, String language) {
     String lang = null;
     if (StringUtil.isDefined(language)) {
       lang = language;
     } else if (StringUtil.isDefined(getSiteLanguage())) {
       lang = getSiteLanguage();
     }
     if (StringUtil.isDefined(lang)) {
       PublicationI18N pubDetaili18n = (PublicationI18N) pubDetail.getTranslation(getSiteLanguage());
       if (pubDetaili18n != null) {
         pubDetail.setName(pubDetaili18n.getName());
         pubDetail.setDescription(pubDetaili18n.getDescription());
       }
     }
     return pubDetail;
   }
 
   /**
    * Get translated Node in current site lang or lang if parameter
    *
    * @param nodeDetail
    * @param lang
    * @return NodeDetail
    */
   private NodeDetail getTranslatedNode(NodeDetail nodeDetail, String language) {
     String lang = null;
     if (StringUtil.isDefined(language)) {
       lang = language;
     } else if (StringUtil.isDefined(getSiteLanguage())) {
       lang = getSiteLanguage();
     }
     if (StringUtil.isDefined(lang)) {
       NodeI18NDetail nodeDetaili18n = (NodeI18NDetail) nodeDetail.getTranslation(getSiteLanguage());
       if (nodeDetaili18n != null) {
         nodeDetail.setName(nodeDetaili18n.getName());
         nodeDetail.setDescription(nodeDetaili18n.getDescription());
       }
     }
     return nodeDetail;
   }
 
   private PublicationBm getPublicationBm() {
     if (publicationBm == null) {
       try {
         publicationBm = EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
             PublicationBm.class);
       } catch (Exception e) {
         throw new KmeliaRuntimeException("KmeliaTagUtil.getPublicationBm",
             SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
       }
     }
     return publicationBm;
   }
 
   private NodeBm getNodeBm() {
     if (nodeBm == null) {
       try {
         nodeBm = EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBm.class);
       } catch (Exception e) {
         throw new KmeliaRuntimeException("KmeliaTagUtil.getNodeBm",
             SilverpeasRuntimeException.ERROR,
             "root.EX_CANT_GET_REMOTE_OBJECT", e);
       }
     }
     return nodeBm;
   }
 }
