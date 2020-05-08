 /*
  * Copyright (C) 2003-2013 eXo Platform SAS.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.estudy.learning.storage.impl;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jcr.ItemExistsException;
 import javax.jcr.ItemNotFoundException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
 import javax.jcr.Value;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 
 import org.estudy.learning.Util;
 import org.estudy.learning.model.Attachment;
 import org.estudy.learning.model.ECategory;
 import org.estudy.learning.model.EQuestion;
 import org.estudy.learning.model.ESession;
 import org.estudy.learning.model.ETesting;
 import org.estudy.learning.storage.DataStorage;
 import org.exoplatform.container.ExoContainer;
 import org.exoplatform.container.ExoContainerContext;
 import org.exoplatform.services.jcr.RepositoryService;
 import org.exoplatform.services.jcr.access.AccessControlEntry;
 import org.exoplatform.services.jcr.access.PermissionType;
 import org.exoplatform.services.jcr.core.ExtendedNode;
 import org.exoplatform.services.jcr.ext.app.SessionProviderService;
 import org.exoplatform.services.jcr.ext.common.SessionProvider;
 import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.services.organization.User;
 import org.exoplatform.services.security.IdentityConstants;
 
 /**
  * Created by The eXo Platform SAS
  * Author : eXoPlatform
  *          exo@exoplatform.com
  * May 2, 2013  
  */
 public class JcrDataStorage implements DataStorage {
   private NodeHierarchyCreator nodeHierarchyCreator_;
   private RepositoryService repoService_;
   private SessionProviderService sessionProviderService_;
   private static final Log       log                 = ExoLogger.getLogger("org.estudy.learning.storage.JcrDataStorage");
   public JcrDataStorage(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService repoService){
     nodeHierarchyCreator_ = nodeHierarchyCreator;
     repoService_ = repoService;
     ExoContainer container = ExoContainerContext.getCurrentContainer();
     sessionProviderService_ = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
   }
 
   public Node getEStorageHome() throws RepositoryException, Exception {
     SessionProvider sProvider = createSessionProvider();
     Node publicApp = nodeHierarchyCreator_.getPublicApplicationNode(sProvider); 
     try {
       return publicApp.getNode(Util.E_STUDY_APP);
     } catch (PathNotFoundException e) {
       Node eApp = publicApp.addNode(Util.E_STUDY_APP, Util.NT_UNSTRUCTURED);
       publicApp.getSession().save();
       return eApp;
     }
   }
 
 
   private Node setSessionProp(ESession e, Node ses){
     try {
       ses.setProperty(ESession.P_TITLE, e.getTitle());
       ses.setProperty(ESession.P_CAT, e.getCat());
       ses.setProperty(ESession.P_QUEST, e.getQuest().toArray(new String[]{}));
       ses.setProperty(ESession.P_DEC, e.getDec());
       ses.setProperty(ESession.P_RFLINK, e.getRflink());
       ses.setProperty( ESession.P_VLINK, e.getVlink());
     } catch (Exception e2) {
       log.info(e2.getMessage());
       return null ;
     }
     return ses;
   }
 
   private Node setQuestionProp(EQuestion e, Node ques){
     try {
       ques.setProperty(EQuestion.P_TITLE, e.getTitle());
       ques.setProperty(EQuestion.P_ANSWER, e.getAnswers().toArray(new String[]{}));
       ques.setProperty(EQuestion.P_CORRECT, e.getCorrect().toArray(new String[]{}));
       ques.setProperty(EQuestion.P_POINT, e.getPoint());
     } catch (Exception e2) {
       e2.printStackTrace();
       log.info(e2.getMessage());
       return null ;
     }
     return ques;
   }
 
   private Node setTestingProp(ETesting e, Node tes){
     try {
       tes.setProperty(ETesting.P_TIME, e.getTime());
       tes.setProperty(ETesting.P_NOTE, e.getNote());
       long point = 0 ;
       for(String q : e.getQuestId()){
         EQuestion question = getQuestion(q);
         Collection<String> answered = e.getAnswered(q);
         if(answered != null && answered.size() == question.getCorrect().size()
             && question.getCorrect().containsAll(answered)) point+=question.getPoint();
       }
       tes.setProperty(ETesting.P_POINT, point);
       tes.setProperty(ETesting.P_RESULT,  mapToStrings(e.getQuest(), Util.SEMI_COLON, Util.COLON).toArray(new String[]{}));
     } catch (Exception e2) {
       e2.printStackTrace() ;
       log.info(e2.getMessage());
       return null ;
     }
     return tes;
   }
 
   private ETesting getTestingProp(Node test){
     ETesting e = new ETesting();
     try {
       e.setId(test.getName()); 
       if(test.hasProperty(ETesting.P_NOTE)) 
         e.setNote(test.getProperty(ETesting.P_NOTE).getString());
       long point = 0 ;
       long totalpoint = 0 ;
       if(test.hasProperty(ETesting.P_TIME)) 
         e.setPoint(test.getProperty(ETesting.P_TIME).getLong());
       if(test.hasProperty(ETesting.P_RESULT)) {
         e.setQuest(valuesToMap(test.getProperty(ETesting.P_RESULT).getValues(), Util.SEMI_COLON, Util.COLON));
       }
       for (String key : e.getQuest().keySet()) {
         EQuestion eq = getQuestion(key);
         Collection<String> answered = e.getAnswered(key);
         if(answered!= null && answered.size() == eq.getCorrect().size() && eq.getCorrect().containsAll(answered))  {
           point+= eq.getPoint();
         }
         totalpoint+= eq.getPoint();
       }
       if(test.hasProperty(ETesting.P_POINT)) 
         e.setPoint(point);
       e.setTotalPoint(totalpoint);
     }catch (Exception e2) {
       log.info(e2.getMessage());
       return null ;
     }
     return e;
   }
 
   private EQuestion getQuestionProp(Node ques){
     EQuestion e = new EQuestion();
     try {
       e.setId(ques.getName());
       if(ques.hasProperty(EQuestion.P_TITLE)) 
         e.setTitle(ques.getProperty(EQuestion.P_TITLE).getString());
       if(ques.hasProperty(EQuestion.P_ANSWER)) 
       {
         Value[] values = ques.getProperty(EQuestion.P_ANSWER).getValues();
         e.setAnswers(valuesToString(values, Util.SEMI_COLON));
       }
 
       if(ques.hasProperty(EQuestion.P_ANSWERED)) 
       {
         Value[] values = ques.getProperty(EQuestion.P_ANSWERED).getValues();
         e.setAnswered(valuesToString(values, Util.SEMI_COLON));
       }
 
       if(ques.hasProperty(EQuestion.P_CORRECT)) 
       {
         Value[] values = ques.getProperty(EQuestion.P_CORRECT).getValues();
         e.setCorrect(valuesToString(values, Util.SEMI_COLON));
       }
       if(ques.hasProperty(EQuestion.P_POINT)) {
         e.setPoint(ques.getProperty(EQuestion.P_POINT).getLong());
       }
     } catch (Exception e2) {
       e2.printStackTrace();
       log.info(e2.getMessage());
       return null ;
     }
     return e;
   }
 
   private ESession getSessionProp(Node ses){
     ESession e = new ESession();
     try {
       e.setId(ses.getName());
       if(ses.hasProperty(ESession.P_TITLE)) 
         e.setTitle(ses.getProperty(ESession.P_TITLE).getString());
       if(ses.hasProperty(ESession.P_CAT)) 
         e.setCat(ses.getProperty(ESession.P_CAT).getString());
       if(ses.hasProperty(ESession.P_QUEST)) 
       {
         Value[] values = ses.getProperty(ESession.P_QUEST).getValues();
         e.setQuest(valuesToString(values, Util.SEMI_COLON));
       }
       if(ses.hasProperty(ESession.P_DEC)) 
         e.setDec(ses.getProperty(ESession.P_DEC).getString());
       if(ses.hasProperty(ESession.P_RFLINK)) 
         e.setRflink(ses.getProperty(ESession.P_RFLINK).getString());
       if(ses.hasProperty(ESession.P_VLINK)) 
         e.setVlink(ses.getProperty(ESession.P_VLINK).getString());
     } catch (Exception e2) {
       log.info(e2.getMessage());
       return null ;
     }
     return e;
   }
 
   private Node setCategoryProp(ECategory c, Node cat){
     try {
       cat.setProperty(ECategory.P_NAME, c.getName());
     } catch (Exception e2) {
       log.info(e2.getMessage());
       return null ;
     }
     return cat;
   }
 
   private ECategory getCategoryProp(Node cat){
     ECategory c = new ECategory();
     try {
       c.setId(cat.getName());
       if(cat.hasProperty(ECategory.P_NAME)) 
         c.setName(cat.getProperty(ECategory.P_NAME).getString());
     } catch (Exception e2) {
       log.info(e2.getMessage());
       return null ;
     }
     return c;
   }
 
   private Node getESessionHome() throws Exception {
     Node eHome = getEStorageHome();
     try {
       return  eHome.getNode(Util.E_STUDY_SES);
     } catch (PathNotFoundException e) {
       eHome.addNode(Util.E_STUDY_SES, Util.NT_UNSTRUCTURED);
       eHome.getSession().save();
       return eHome.getNode(Util.E_STUDY_SES);
     }  
   }
 
   private boolean isExists(String nt, String proName, String value){
     try {
       QueryManager qm = getEStorageHome().getSession().getWorkspace().getQueryManager();
       Query q = qm.createQuery("SELECT "+proName+" FROM " + nt + " WHERE " + proName + " = '" + value +"'", Query.SQL);
       return q.execute().getRows().getSize() != 0;
     } catch (Exception e) {
       log.info(e.getMessage());
       return false ;
     }
 
 
   }
 
 
   private SessionProvider createSessionProvider() {
     SessionProvider provider = sessionProviderService_.getSessionProvider(null);
     if (provider == null) {
       //log.info("No user session provider was available, trying to use a system session provider");
       provider = sessionProviderService_.getSystemSessionProvider(null);
     }
     return SessionProvider.createSystemProvider();
   }
 
   private String valuesToString (Value[] values, String regex) throws Exception{
     StringBuffer sb = new StringBuffer();
     for (Value v : values){
       sb.append(regex).append(v.getString());
     }
     return sb.toString().replaceFirst(regex, "");
   }
 
   private Collection<String> valuesToStrings (Value[] values) throws Exception{
     Collection<String> list = new ArrayList<String>();
     for (Value v : values){
       list.add(v.getString());
     }
     return list;
   }
 
   private Map<String, Collection<String>> valuesToMap(Value[] values, String regex1, String regex2) throws Exception{
     Map<String, Collection<String>> result = new HashMap<String, Collection<String>>();
     for(Value val:values){
       String value = val.getString();
       String[] arr =  value.split(regex1);
       String k =arr[0];
       String v =arr[1]; 
       result.put(k, Arrays.asList(v.split(regex2)));
     }
     return result;
   }
 
   private Collection<String> mapToStrings(Map<String, Collection<String>> quest, String regex1, String regex2){
     Collection<String> values = new ArrayList<String>();
     for(String k : quest.keySet()) {
       StringBuffer sb = new StringBuffer(k).append(regex1);
       for(String s : quest.get(k)){
         sb.append(s).append(regex2);
       }
       values.add(sb.toString());
     }
     return values;
   }
 
   private Node getECategoryHome() throws RepositoryException, Exception {
     Node eHome = getEStorageHome();
     try {
       return  eHome.getNode(Util.E_STUDY_CAT);
     } catch (PathNotFoundException e) {
       eHome.addNode(Util.E_STUDY_CAT, Util.NT_UNSTRUCTURED);
       eHome.getSession().save();
       return eHome.getNode(Util.E_STUDY_CAT);
     }  
 
 
   }
   private Node getEQestionHome() throws RepositoryException, Exception {
     Node eHome = getEStorageHome();
     try {
       return  eHome.getNode(Util.E_STUDY_QUEST);
     } catch (PathNotFoundException e) {
       eHome.addNode(Util.E_STUDY_QUEST, Util.NT_UNSTRUCTURED);
       eHome.getSession().save();
       return eHome.getNode(Util.E_STUDY_QUEST);
     }  
   }
 
   private Node getETestingHome(String uid) throws RepositoryException, Exception {
     Node uHome = nodeHierarchyCreator_.getUserNode(createSessionProvider(), uid);
     try {
       return  uHome.getNode(Util.E_STUDY_TEST);
     } catch (PathNotFoundException e) {
       uHome.addNode(Util.E_STUDY_TEST, Util.NT_UNSTRUCTURED);
       uHome.getSession().save();
       return uHome.getNode(Util.E_STUDY_TEST);
     }  
   }
 
   private Node getEMediaHome() throws RepositoryException, Exception {
     Node uHome = getEStorageHome();
     try {
       return  uHome.getNode(Util.E_STUDY_MED);
     } catch (PathNotFoundException e) {
       uHome.addNode(Util.E_STUDY_MED, Util.NT_UNSTRUCTURED);
       uHome.getSession().save();
       return uHome.getNode(Util.E_STUDY_MED);
     }
   }
 
 
   @Override
   public void saveCategory(ECategory category, boolean isNew) throws ItemExistsException, Exception{
     Node catHome = getECategoryHome();
     Node cat;
     if(isNew){
       if(isExists(ECategory.NT_NAME, ECategory.P_NAME, category.getName())) throw new ItemExistsException();
       try {
         cat = setCategoryProp(category, catHome.addNode(category.getId(), ECategory.NT_NAME));
         cat.addMixin(Util.MIX_REFERENCEABLE);
         cat.getSession().save();
       } catch (Exception e) {
         e.printStackTrace();
         log.info(e.getMessage());
         throw e;
       }
     } else {
       cat = setCategoryProp(category, catHome.getNode(category.getId()));
       cat.save();
     }
 
   }
 
   @Override
   public Collection<ECategory> getCategories() throws Exception {
     try {
       Node catHome = getECategoryHome();
       NodeIterator it = catHome.getNodes();
       Collection<ECategory> list = new ArrayList<ECategory>();
       while (it.hasNext()) {
         list.add(getCategoryProp(it.nextNode()));
       }
       return list;
     } catch (Exception e){
       log.info(e.getMessage());
     }
     return null;
   }
 
   @Override
   public ECategory getCategory(String id) throws ItemNotFoundException {
     try {
       Node catHome = getECategoryHome();
       return getCategoryProp(catHome.getNode(id));
     } catch (PathNotFoundException e) {
       throw new ItemNotFoundException();
     } catch (Exception e) {
       log.info(e.getMessage());
       return null;
     }
   }
 
   @Override
   public void removeCategory(String id) throws Exception {
     Node catHome = getECategoryHome();
     try {
       Node cat = catHome.getNode(id);
       cat.remove();
       catHome.save();
     } catch (PathNotFoundException e) {
     } catch (Exception e) {
       log.info(e.getMessage());
       throw e;
     }
 
   }
 
   @Override
   public void saveSession(ESession session, boolean isNew) throws ItemExistsException, Exception {
     Node sesHome = getESessionHome();
     Node ses;
     if(isNew){
       if(isExists(ESession.NT_NAME, ESession.P_TITLE, session.getTitle())) throw new ItemExistsException();
       try {
         ses = setSessionProp(session, sesHome.addNode(session.getId(), ESession.NT_NAME));
         ses.getSession().save();
       } catch (Exception e) {
         log.info(e.getMessage());
         throw e;
       }
     } else {
       ses = setSessionProp(session, sesHome.getNode(session.getId()));
       ses.save();
     } 
   }
 
 
   @Override
   public Collection<ESession> getSessions() throws Exception {
     try {
       Node sesHome = getESessionHome();
       NodeIterator it = sesHome.getNodes();
       Collection<ESession> list = new ArrayList<ESession>();
       while (it.hasNext()) {
         list.add(getSessionProp(it.nextNode()));
       }
       return list;
     } catch (Exception e){
       log.info(e.getMessage());
     }
     return null;
   }
 
   @Override
   public ESession getSession(String id) throws ItemNotFoundException {
     try {
       Node sesHome = getESessionHome();
       return getSessionProp(sesHome.getNode(id));
     } catch (PathNotFoundException e) {
       throw new ItemNotFoundException();
     } catch (Exception e) {
       log.info(e.getMessage());
       return null;
     }
   }
   @Override
   public void removeSession(String id) throws Exception {
     Node sesHome = getESessionHome();
     try {
       Node ses = sesHome.getNode(id);
       ses.remove();
       sesHome.save();
     } catch (PathNotFoundException e) {
     } catch (Exception e) {
       log.info(e.getMessage());
       throw e;
     }
   }
 
   @Override
   public void saveQuestion(EQuestion qestion, boolean isNew) throws ItemExistsException,
   Exception {
     Node qHome = getEQestionHome();
     Node ques ;
     if(isNew){
       if(isExists(EQuestion.NT_NAME, EQuestion.P_TITLE, qestion.getTitle())) throw new ItemExistsException();
       try {
         ques = setQuestionProp(qestion, qHome.addNode(qestion.getId(), EQuestion.NT_NAME));
         ques.getSession().save();
       } catch (Exception e) {
         log.info(e.getMessage());
         throw e;
       }
     } else {
       ques = setQuestionProp(qestion, qHome.getNode(qestion.getId()));
       ques.save();
     } 
 
   }
 
   @Override
   public Collection<EQuestion> getQuestions() throws Exception {
 
     try {
       Node sesHome = getEQestionHome();
       NodeIterator it = sesHome.getNodes();
       Collection<EQuestion> list = new ArrayList<EQuestion>();
       while (it.hasNext()) {
         list.add(getQuestionProp(it.nextNode()));
       }
       return list;
     } catch (Exception e){
       log.info(e.getMessage());
     }
     return null;
   }
 
   @Override
   public EQuestion getQuestion(String id) throws ItemNotFoundException {
     try {
       Node sesHome = getEQestionHome();
       return getQuestionProp(sesHome.getNode(id));
     } catch (PathNotFoundException e) {
       throw new ItemNotFoundException();
     } catch (Exception e) {
       log.info(e.getMessage());
       return null;
     }
   }
 
   @Override
   public void removeQuestion(String id) throws Exception {
     Node sesHome = getEQestionHome();
     try {
       Node ses = sesHome.getNode(id);
       ses.remove();
       sesHome.save();
     } catch (PathNotFoundException e) {
     } catch (Exception e) {
       log.info(e.getMessage());
       throw e;
     }
   }
 
   @Override
   public void saveTesting(User user, ETesting test, boolean isNew)
       throws Exception {
     Node qHome = getETestingHome(user.getUserName());
     Node testing ;
     if(isNew){
       try {
         testing = setTestingProp(test, qHome.addNode(test.getId(), ETesting.NT_NAME));
         testing.getSession().save();
       } catch (Exception e) {
         log.info(e.getMessage());
         throw e;
       }
     } else {
       testing = setTestingProp(test, qHome.getNode(test.getId()));
       testing.save();
     } 
 
   }
 
   @Override
   public Collection<ETesting> getTestingScore(String uId,
                                               Collection<String> tIds) throws RepositoryException {
     try {
       Node sesHome = getETestingHome(uId);
       NodeIterator it = sesHome.getNodes();
       Collection<ETesting> list = new ArrayList<ETesting>();
       while (it.hasNext()) {
         Node test = it.nextNode();
         if(tIds.contains(test.getName()))
           list.add(getTestingProp(test));
       }
       return list;
     } catch (Exception e) {
       log.info(e.getMessage());
 
     }
     return null;
   }
 
   public Collection<ETesting> getTestingScore(String uId) throws RepositoryException {
     try {
       Node sesHome = getETestingHome(uId);
       NodeIterator it = sesHome.getNodes();
       Collection<ETesting> list = new ArrayList<ETesting>();
       while (it.hasNext()) {
         list.add(getTestingProp(it.nextNode()));
       }
       return list;
     } catch (Exception e) {
       log.info(e.getMessage());
 
     }
     return null;
   }
 
   public void removeTesting(String uid, String id) throws Exception {
     Node sesHome = getETestingHome(uid);
     try {
       Node ses = sesHome.getNode(id);
       ses.remove();
       sesHome.save();
     } catch (PathNotFoundException e) {
     } catch (Exception e) {
       log.info(e.getMessage());
       throw e;
     }
     
   }
 
   private static List<Attachment> getAttachments(Node eventNode) throws Exception {
     List<Attachment> attachments = new ArrayList<Attachment>();
     if (eventNode.hasNode(Util.ATTACHMENT_NODE)) {
       Node attachHome = eventNode.getNode(Util.ATTACHMENT_NODE);
       NodeIterator iter = attachHome.getNodes();
       while (iter.hasNext()) {
         Node attchmentNode = iter.nextNode();
         if (attchmentNode.isNodeType(Util.EXO_ATTACHMENT)) {
           Attachment attachment = new Attachment();
           attachment.setId(attchmentNode.getPath());
           if (attchmentNode.hasProperty(Util.EXO_FILE_NAME))
             attachment.setName(attchmentNode.getProperty(Util.EXO_FILE_NAME).getString());
           Node contentNode = attchmentNode.getNode(Util.JCR_CONTENT);
           if (contentNode != null) {
             if (contentNode.hasProperty(Util.JCR_LASTMODIFIED))
               attachment.setLastModified(contentNode.getProperty(Util.JCR_LASTMODIFIED).getDate());
             if (contentNode.hasProperty(Util.JCR_MIMETYPE))
               attachment.setMimeType(contentNode.getProperty(Util.JCR_MIMETYPE).getString());
             if (contentNode.hasProperty(Util.JCR_DATA)) {
               InputStream inputStream = contentNode.getProperty(Util.JCR_DATA).getStream();
               attachment.setSize(inputStream.available());
               attachment.setInputStream(inputStream);
             }
           }
           attachment.setWorkspace(attchmentNode.getSession().getWorkspace().getName());
           attachments.add(attachment);
         }
       }
     }
     return attachments;
   }
 
   private String addAttachment(Node eventNode, Attachment attachment, boolean isNew) throws Exception {
     Node attachHome;
     Node attachNode;
     // fix load image on IE6 UI
     ExtendedNode extNode = (ExtendedNode) eventNode;
     if (extNode.canAddMixin("exo:privilegeable"))
       extNode.addMixin("exo:privilegeable");
     String[] arrayPers = { PermissionType.READ, PermissionType.ADD_NODE,
             PermissionType.SET_PROPERTY, PermissionType.REMOVE };
     extNode.setPermission(IdentityConstants.ANY, arrayPers);
     List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries();
     for (AccessControlEntry accessControlEntry : permsList) {
       extNode.setPermission(accessControlEntry.getIdentity(), arrayPers);
     }
     try {
       attachHome = eventNode.getNode(Util.ATTACHMENT_NODE);
     } catch (PathNotFoundException e) {
       attachHome = eventNode.addNode(Util.ATTACHMENT_NODE, Util.NT_UNSTRUCTURED);
      //eventNode.save();
     }
     String name = attachment.getId().substring(attachment.getId().lastIndexOf(Util.SLASH) + 1);
     try {
       attachNode = attachHome.getNode(name);
     } catch (PathNotFoundException e) {
       attachNode = attachHome.addNode(name, Util.EXO_ATTACHMENT);
      //attachHome.save();
     }
     attachNode.setProperty(Util.EXO_FILE_NAME, attachment.getName());
     Node nodeContent = null;
     try {
       nodeContent = attachNode.getNode(Util.JCR_CONTENT);
     } catch (Exception e) {
       nodeContent = attachNode.addNode(Util.JCR_CONTENT, Util.NT_RESOURCE);
      //attachNode.save();
     }
     nodeContent.setProperty(Util.JCR_LASTMODIFIED, java.util.Calendar.getInstance()
             .getTimeInMillis());
     nodeContent.setProperty(Util.JCR_MIMETYPE, attachment.getMimeType());
     nodeContent.setProperty(Util.JCR_DATA, attachment.getInputStream());
     if(attachHome.isNew()) attachHome.getSession().save();
     else attachHome.save();
     return attachNode.getPath();
   }
 
   @Override
   public String uploadMedia(Attachment media) throws Exception{
     Node node = getEMediaHome();
     return addAttachment(node, media, true);
   }
 
   @Override
   public Collection<Attachment> getMedias() throws Exception {
     Node node = getEMediaHome();
     return getAttachments(node);
   }
 }
