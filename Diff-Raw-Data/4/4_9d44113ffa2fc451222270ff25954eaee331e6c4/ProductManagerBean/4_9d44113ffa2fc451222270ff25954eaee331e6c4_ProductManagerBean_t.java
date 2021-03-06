 /*
  * DocDoku, Professional Open Source
  * Copyright 2006 - 2013 DocDoku SARL
  *
  * This file is part of DocDokuPLM.
  *
  * DocDokuPLM is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * DocDokuPLM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.docdoku.server;
 
 import com.docdoku.core.common.BinaryResource;
 import com.docdoku.core.common.User;
 import com.docdoku.core.common.Workspace;
 import com.docdoku.core.document.DocumentIteration;
 import com.docdoku.core.document.DocumentIterationKey;
 import com.docdoku.core.document.DocumentLink;
 import com.docdoku.core.meta.InstanceAttribute;
 import com.docdoku.core.meta.InstanceAttributeTemplate;
 import com.docdoku.core.product.*;
 import com.docdoku.core.product.PartIteration.Source;
 import com.docdoku.core.services.*;
 import com.docdoku.core.util.NamingConvention;
 import com.docdoku.core.util.Tools;
 import com.docdoku.core.workflow.Task;
 import com.docdoku.core.workflow.Workflow;
 import com.docdoku.core.workflow.WorkflowModel;
 import com.docdoku.core.workflow.WorkflowModelKey;
 import com.docdoku.server.dao.*;
 import com.docdoku.server.vault.DataManager;
 import com.docdoku.server.vault.filesystem.DataManagerImpl;
 
 import java.io.File;
 import java.text.ParseException;
 import java.util.*;
 import java.util.logging.Logger;
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import javax.annotation.security.RolesAllowed;
 import javax.ejb.Local;
 import javax.ejb.SessionContext;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.annotation.security.DeclareRoles;
 import javax.ejb.EJB;
 import javax.jws.WebService;
 
 @DeclareRoles("users")
 @Local(IProductManagerLocal.class)
 @Stateless(name = "ProductManagerBean")
 @WebService(endpointInterface = "com.docdoku.core.services.IProductManagerWS")
 public class ProductManagerBean implements IProductManagerWS, IProductManagerLocal {
 
     @PersistenceContext
     private EntityManager em;
     @Resource
     private SessionContext ctx;
     @Resource(name = "vaultPath")
     private String vaultPath;
     @EJB
     private IMailerLocal mailer;
     @EJB
     private IUserManagerLocal userManager;
     private final static Logger LOGGER = Logger.getLogger(ProductManagerBean.class.getName());
     private DataManager dataManager;
 
 
 
     @PostConstruct
     private void init() {
         dataManager = new DataManagerImpl(new File(vaultPath));
     }
 
     @RolesAllowed("users")
     @Override
     public List<PartUsageLink[]> findPartUsages(ConfigurationItemKey pKey, PartMasterKey pPartMKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
         User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
         PartUsageLinkDAO linkDAO = new PartUsageLinkDAO(new Locale(user.getLanguage()), em);
         List<PartUsageLink[]> usagePaths = linkDAO.findPartUsagePaths(pPartMKey);
         //TODO filter by configuration item
         return usagePaths;
     }
 
     @RolesAllowed("users")
     @Override
     public List<PartMaster> findPartMasters(String pWorkspaceId, String pPartNumber, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
         User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
         PartMasterDAO partMDAO = new PartMasterDAO(new Locale(user.getLanguage()), em);
         return partMDAO.findPartMasters(pWorkspaceId, pPartNumber, pMaxResults);
     }
 
     @RolesAllowed("users")
     @Override
     public PartUsageLink filterProductStructure(ConfigurationItemKey pKey, ConfigSpec configSpec, Integer partUsageLink, Integer depth) throws ConfigurationItemNotFoundException, WorkspaceNotFoundException, NotAllowedException, UserNotFoundException, UserNotActiveException, PartUsageLinkNotFoundException {
         User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
         PartUsageLink rootUsageLink;
 
 
         if (partUsageLink == null || partUsageLink == -1) {
             ConfigurationItem ci = new ConfigurationItemDAO(new Locale(user.getLanguage()), em).loadConfigurationItem(pKey);
             rootUsageLink = new PartUsageLink();
             rootUsageLink.setId(-1);
             rootUsageLink.setAmount(1d);
             List<CADInstance> cads = new ArrayList<CADInstance>();
             cads.add(new CADInstance(0d, 0d, 0d, 0d, 0d, 0d, CADInstance.Positioning.ABSOLUTE));
             rootUsageLink.setCadInstances(cads);
             rootUsageLink.setComponent(ci.getDesignItem());
         } else {
             rootUsageLink = new PartUsageLinkDAO(new Locale(user.getLanguage()), em).loadPartUsageLink(partUsageLink);
         }
 
         if (configSpec instanceof LatestConfigSpec) {
             if (depth == null) {
                 filterLatestConfigSpec(rootUsageLink.getComponent(), -1);
             } else {
                 filterLatestConfigSpec(rootUsageLink.getComponent(), depth);
             }
         }
         return rootUsageLink;
     }
 
     private PartMaster filterLatestConfigSpec(PartMaster root, int depth) {
         PartRevision partR = root.getLastRevision();
         PartIteration partI = null;
 
         if (partR != null) {
             partI = partR.getLastIteration();
         }
 
         if (partI != null) {
             if (depth != 0) {
                 depth--;
                 for (PartUsageLink usageLink : partI.getComponents()) {
                     filterLatestConfigSpec(usageLink.getComponent(), depth);
 
                     for (PartSubstituteLink subLink : usageLink.getSubstitutes()) {
                         filterLatestConfigSpec(subLink.getSubstitute(), 0);
                     }
                 }
             }
         }
 
         for (PartAlternateLink alternateLink : root.getAlternates()) {
             filterLatestConfigSpec(alternateLink.getAlternate(), 0);
         }
 
         em.detach(root);
         if (root.getPartRevisions().size() > 1) {
             root.getPartRevisions().retainAll(Collections.singleton(partR));
         }
         if (partR != null && partR.getNumberOfIterations() > 1) {
             partR.getPartIterations().retainAll(Collections.singleton(partI));
         }
 
         return root;
     }
 
     @RolesAllowed("users")
     @Override
     public ConfigurationItem createConfigurationItem(String pWorkspaceId, String pId, String pDescription, String pDesignItemNumber) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, ConfigurationItemAlreadyExistsException, CreationException, PartMasterNotFoundException {
 
         User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
         if (!NamingConvention.correct(pId)) {
             throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
         }
 
         ConfigurationItem ci = new ConfigurationItem(user.getWorkspace(), pId, pDescription);
 
         try {
             PartMaster designedPartMaster = new PartMasterDAO(new Locale(user.getLanguage()), em).loadPartM(new PartMasterKey(pWorkspaceId, pDesignItemNumber));
             ci.setDesignItem(designedPartMaster);
             new ConfigurationItemDAO(new Locale(user.getLanguage()), em).createConfigurationItem(ci);
             return ci;
         } catch (PartMasterNotFoundException e) {
             throw new PartMasterNotFoundException(new Locale(user.getLanguage()),pDesignItemNumber);
         }
 
     }
 
     @RolesAllowed("users")
     @Override
     public PartMaster createPartMaster(String pWorkspaceId, String pNumber, String pName, String pPartMasterDescription, boolean pStandardPart, String pWorkflowModelId, String pPartRevisionDescription, String templateId) throws NotAllowedException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, PartMasterAlreadyExistsException, CreationException, PartMasterTemplateNotFoundException, FileAlreadyExistsException {
 
         User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
         if (!NamingConvention.correct(pNumber)) {
             throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
         }
 
         PartMaster pm = new PartMaster(user.getWorkspace(), pNumber, user);
         pm.setName(pName);
         pm.setStandardPart(pStandardPart);
         pm.setDescription(pPartMasterDescription);
         Date now = new Date();
         pm.setCreationDate(now);
         PartRevision newRevision = pm.createNextRevision(user);
 
         if (pWorkflowModelId != null) {
             WorkflowModel workflowModel = new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(new WorkflowModelKey(user.getWorkspaceId(), pWorkflowModelId));
             Workflow workflow = workflowModel.createWorkflow();
             newRevision.setWorkflow(workflow);
 
             Collection<Task> runningTasks = workflow.getRunningTasks();
             for (Task runningTask : runningTasks) {
                 runningTask.start();
             }
             //TODO adapt to Part
             //mailer.sendApproval(runningTasks, newRevision);
         }
         newRevision.setCheckOutUser(user);
         newRevision.setCheckOutDate(now);
         newRevision.setCreationDate(now);
         newRevision.setDescription(pPartRevisionDescription);
         PartIteration ite = newRevision.createNextIteration(user);
         ite.setCreationDate(now);
 
         if(templateId != null){
 
             PartMasterTemplate partMasterTemplate = new PartMasterTemplateDAO(new Locale(user.getLanguage()),em).loadPartMTemplate(new PartMasterTemplateKey(pWorkspaceId,templateId));
             pm.setType(partMasterTemplate.getPartType());
 
             Map<String, InstanceAttribute> attrs = new HashMap<String, InstanceAttribute>();
             for (InstanceAttributeTemplate attrTemplate : partMasterTemplate.getAttributeTemplates()) {
                 InstanceAttribute attr = attrTemplate.createInstanceAttribute();
                 attrs.put(attr.getName(), attr);
             }
             ite.setInstanceAttributes(attrs);
 
             BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
             BinaryResource sourceFile = partMasterTemplate.getAttachedFile();
 
             if(sourceFile != null){
                 String fileName = sourceFile.getName();
                 long length = sourceFile.getContentLength();
                 String fullName = pWorkspaceId + "/parts/" + pm.getNumber() + "/A/1/" + fileName;
                 BinaryResource targetFile = new BinaryResource(fullName, length);
                 binDAO.createBinaryResource(targetFile);
                 ite.setNativeCADFile(targetFile);
                 dataManager.copyData(sourceFile, targetFile);
             }
 
         }
 
         PartMasterDAO partMDAO = new PartMasterDAO(new Locale(user.getLanguage()), em);
         partMDAO.createPartM(pm);
 
         return pm;
     }
 
     @RolesAllowed("users")
     @Override
     public PartRevision undoCheckOutPart(PartRevisionKey pPartRPK) throws NotAllowedException, PartRevisionNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
         User user = userManager.checkWorkspaceReadAccess(pPartRPK.getPartMaster().getWorkspace());
         PartRevisionDAO partRDAO = new PartRevisionDAO(new Locale(user.getLanguage()), em);
         PartRevision partR = partRDAO.loadPartR(pPartRPK);
         if (partR.isCheckedOut() && partR.getCheckOutUser().equals(user)) {
             PartIteration partIte = partR.removeLastIteration();
             for (Geometry file : partIte.getGeometries()) {
                 dataManager.delData(file);
             }
 
             for (BinaryResource file : partIte.getAttachedFiles()) {
                 dataManager.delData(file);
             }
 
             BinaryResource nativeCAD = partIte.getNativeCADFile();
             if (nativeCAD != null) {
                 dataManager.delData(nativeCAD);
             }
 
             PartIterationDAO partIDAO = new PartIterationDAO(em);
             partIDAO.removeIteration(partIte);
             partR.setCheckOutDate(null);
             partR.setCheckOutUser(null);
             return partR;
         } else {
             throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException19");
         }
     }
 
     @RolesAllowed("users")
     @Override
     public PartRevision checkOutPart(PartRevisionKey pPartRPK) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, PartRevisionNotFoundException, NotAllowedException, FileAlreadyExistsException, CreationException {
         User user = userManager.checkWorkspaceWriteAccess(pPartRPK.getPartMaster().getWorkspace());
         PartRevisionDAO partRDAO = new PartRevisionDAO(new Locale(user.getLanguage()), em);
         PartRevision partR = partRDAO.loadPartR(pPartRPK);
         //Check access rights on partR
         Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pPartRPK.getPartMaster().getWorkspace());
         boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
         if (partR.isCheckedOut()) {
             throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException37");
         }
 
         PartIteration beforeLastPartIteration = partR.getLastIteration();
 
         PartIteration newPartIteration = partR.createNextIteration(user);
         //We persist the doc as a workaround for a bug which was introduced
         //since glassfish 3 that set the DTYPE to null in the instance attribute table
         em.persist(newPartIteration);
         Date now = new Date();
         newPartIteration.setCreationDate(now);
         partR.setCheckOutUser(user);
         partR.setCheckOutDate(now);
 
         if (beforeLastPartIteration != null) {
             BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
             for (BinaryResource sourceFile : beforeLastPartIteration.getAttachedFiles()) {
                 String fileName = sourceFile.getName();
                 long length = sourceFile.getContentLength();
                 String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + newPartIteration.getIteration() + "/" + fileName;
                 BinaryResource targetFile = new BinaryResource(fullName, length);
                 binDAO.createBinaryResource(targetFile);
                 newPartIteration.addFile(targetFile);
             }
 
             List<PartUsageLink> components = new LinkedList<PartUsageLink>();
             for (PartUsageLink usage : beforeLastPartIteration.getComponents()) {
                 PartUsageLink newUsage = usage.clone();
                 components.add(newUsage);
             }
             newPartIteration.setComponents(components);
 
             for (Geometry sourceFile : beforeLastPartIteration.getGeometries()) {
                 String fileName = sourceFile.getName();
                 long length = sourceFile.getContentLength();
                 int quality = sourceFile.getQuality();
                 String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + newPartIteration.getIteration() + "/" + fileName;
                 Geometry targetFile = new Geometry(quality, fullName, length);
                 binDAO.createBinaryResource(targetFile);
                 newPartIteration.addGeometry(targetFile);
             }
 
             BinaryResource nativeCADFile = beforeLastPartIteration.getNativeCADFile();
             if (nativeCADFile != null) {
                 String fileName = nativeCADFile.getName();
                 long length = nativeCADFile.getContentLength();
                 String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + newPartIteration.getIteration() + "/nativecad/" + fileName;
                 BinaryResource targetFile = new BinaryResource(fullName, length);
                 binDAO.createBinaryResource(targetFile);
                 newPartIteration.setNativeCADFile(targetFile);
             }
 
             Set<DocumentLink> links = new HashSet<DocumentLink>();
             for (DocumentLink link : beforeLastPartIteration.getLinkedDocuments()) {
                 DocumentLink newLink = link.clone();
                 links.add(newLink);
             }
             newPartIteration.setLinkedDocuments(links);
 
             InstanceAttributeDAO attrDAO = new InstanceAttributeDAO(em);
             Map<String, InstanceAttribute> attrs = new HashMap<String, InstanceAttribute>();
             for (InstanceAttribute attr : beforeLastPartIteration.getInstanceAttributes().values()) {
                 InstanceAttribute newAttr = attr.clone();
                 //newAttr.setDocument(newDoc);
                 //Workaround for the NULL DTYPE bug
                 attrDAO.createAttribute(newAttr);
                 attrs.put(newAttr.getName(), newAttr);
             }
             newPartIteration.setInstanceAttributes(attrs);
         }
 
         return partR;
     }
 
     @RolesAllowed("users")
     @Override
     public PartRevision checkInPart(PartRevisionKey pPartRPK) throws PartRevisionNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException {
         User user = userManager.checkWorkspaceWriteAccess(pPartRPK.getPartMaster().getWorkspace());
         PartRevisionDAO partRDAO = new PartRevisionDAO(new Locale(user.getLanguage()), em);
         PartRevision partR = partRDAO.loadPartR(pPartRPK);
         //Check access rights on docM
         Workspace wks = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(pPartRPK.getPartMaster().getWorkspace());
         boolean isAdmin = wks.getAdmin().getLogin().equals(user.getLogin());
         if (partR.isCheckedOut() && partR.getCheckOutUser().equals(user)) {
             partR.setCheckOutDate(null);
             partR.setCheckOutUser(null);
 
             return partR;
         } else {
             throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException20");
         }
     }
 
     @RolesAllowed("users")
     @Override
     public File getDataFile(String pFullName) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FileNotFoundException, NotAllowedException {
         User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
         Locale userLocale = new Locale(user.getLanguage());
         BinaryResourceDAO binDAO = new BinaryResourceDAO(userLocale, em);
         BinaryResource file = binDAO.loadBinaryResource(pFullName);
 
         PartIteration partIte = binDAO.getPartOwner(file);
         if (partIte != null) {
             PartRevision partR = partIte.getPartRevision();
 
             if ((partR.isCheckedOut() && !partR.getCheckOutUser().equals(user) && partR.getLastIteration().equals(partIte))) {
                 throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException34");
             } else {
                 return dataManager.getDataFile(file);
             }
         } else {
             throw new FileNotFoundException(userLocale, pFullName);
         }
     }
 
     @RolesAllowed("users")
     @Override
     public File saveGeometryInPartIteration(PartIterationKey pPartIPK, String pName, int quality, long pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException {
         User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
         if (!NamingConvention.correct(pName)) {
             throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
         }
 
         PartRevisionDAO partRDAO = new PartRevisionDAO(em);
         PartRevision partR = partRDAO.loadPartR(pPartIPK.getPartRevision());
         PartIteration partI = partR.getIteration(pPartIPK.getIteration());
         if (partR.isCheckedOut() && partR.getCheckOutUser().equals(user) && partR.getLastIteration().equals(partI)) {
             Geometry file = null;
             String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + partI.getIteration() + "/" + pName;
 
             for (Geometry geo : partI.getGeometries()) {
                 if (geo.getFullName().equals(fullName)) {
                     file = geo;
                     break;
                 }
             }
             if (file == null) {
                 file = new Geometry(quality, fullName, pSize);
                 new BinaryResourceDAO(em).createBinaryResource(file);
                 partI.addGeometry(file);
             } else {
                 file.setContentLength(pSize);
                 file.setQuality(quality);
             }
             return dataManager.getVaultFile(file);
         } else {
             throw new NotAllowedException(Locale.getDefault(), "NotAllowedException4");
         }
     }
 
     @RolesAllowed("users")
     @Override
     public File saveNativeCADInPartIteration(PartIterationKey pPartIPK, String pName, long pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException {
         User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
         if (!NamingConvention.correct(pName)) {
             throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
         }
 
         BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
         PartRevisionDAO partRDAO = new PartRevisionDAO(em);
         PartRevision partR = partRDAO.loadPartR(pPartIPK.getPartRevision());
         PartIteration partI = partR.getIteration(pPartIPK.getIteration());
         if (partR.isCheckedOut() && partR.getCheckOutUser().equals(user) && partR.getLastIteration().equals(partI)) {
             String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + partI.getIteration() + "/nativecad/" + pName;
             BinaryResource file = partI.getNativeCADFile();
             if (file == null) {
                 file = new BinaryResource(fullName, pSize);
                 binDAO.createBinaryResource(file);
                 partI.setNativeCADFile(file);
             } else if (file.getFullName().equals(fullName)) {
                 file.setContentLength(pSize);
             } else {
                 dataManager.delData(file);
                 partI.setNativeCADFile(null);
                 binDAO.removeBinaryResource(file);
                 //Delete converted files if any
                 List<Geometry> geometries = new ArrayList<>(partI.getGeometries());
                 for(Geometry geometry : geometries){
                     dataManager.delData(geometry);
                     partI.removeGeometry(geometry);
                 }
                 Set<BinaryResource> attachedFiles = new HashSet<>(partI.getAttachedFiles());
                 for(BinaryResource attachedFile : attachedFiles){
                     dataManager.delData(attachedFile);
                     partI.removeFile(attachedFile);
                 }
 
                 file = new BinaryResource(fullName, pSize);
                 binDAO.createBinaryResource(file);
                 partI.setNativeCADFile(file);
             }
             return dataManager.getVaultFile(file);
         } else {
             throw new NotAllowedException(Locale.getDefault(), "NotAllowedException4");
         }
 
     }
 
     @RolesAllowed("users")
     @Override
     public File saveFileInPartIteration(PartIterationKey pPartIPK, String pName, long pSize) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, PartRevisionNotFoundException, FileAlreadyExistsException, CreationException {
         User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
         if (!NamingConvention.correct(pName)) {
             throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
         }
 
         PartRevisionDAO partRDAO = new PartRevisionDAO(em);
         PartRevision partR = partRDAO.loadPartR(pPartIPK.getPartRevision());
         PartIteration partI = partR.getIteration(pPartIPK.getIteration());
         if (partR.isCheckedOut() && partR.getCheckOutUser().equals(user) && partR.getLastIteration().equals(partI)) {
             BinaryResource file = null;
             String fullName = partR.getWorkspaceId() + "/parts/" + partR.getPartNumber() + "/" + partR.getVersion() + "/" + partI.getIteration() + "/" + pName;
 
             for (BinaryResource bin : partI.getAttachedFiles()) {
                 if (bin.getFullName().equals(fullName)) {
                     file = bin;
                     break;
                 }
             }
             if (file == null) {
                 file = new BinaryResource(fullName, pSize);
                 new BinaryResourceDAO(em).createBinaryResource(file);
                 partI.addFile(file);
             } else {
                 file.setContentLength(pSize);
             }
             return dataManager.getVaultFile(file);
         } else {
             throw new NotAllowedException(Locale.getDefault(), "NotAllowedException4");
         }
     }
 
     @RolesAllowed("users")
     @Override
     public List<ConfigurationItem> getConfigurationItems(String pWorkspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
         User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
         return new ConfigurationItemDAO(new Locale(user.getLanguage()), em).findAllConfigurationItems(pWorkspaceId);
     }
 
     /*
     * give pUsageLinks null for no modification, give an empty list for removing them
     * give pAttributes null for no modification, give an empty list for removing them
     * */
     @RolesAllowed("users")
     @Override
     public PartRevision updatePartIteration(PartIterationKey pKey, String pIterationNote, Source source, List<PartUsageLink> pUsageLinks, List<InstanceAttribute> pAttributes, DocumentIterationKey[] pLinkKeys) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, PartRevisionNotFoundException, PartMasterNotFoundException {
         User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
         PartMasterDAO partMDAO = new PartMasterDAO(new Locale(user.getLanguage()), em);
         PartRevisionDAO partRDAO = new PartRevisionDAO(new Locale(user.getLanguage()), em);
         DocumentLinkDAO linkDAO = new DocumentLinkDAO(new Locale(user.getLanguage()), em);
 
         PartRevision partRev = partRDAO.loadPartR(pKey.getPartRevision());
         PartIteration partIte = partRev.getLastIteration();
         //check access rights on partM ?
         if (partRev.isCheckedOut() && partRev.getCheckOutUser().equals(user) && partIte.getKey().equals(pKey)) {
             if (pLinkKeys != null) {
                 Set<DocumentIterationKey> linkKeys = new HashSet<DocumentIterationKey>(Arrays.asList(pLinkKeys));
                 Set<DocumentIterationKey> currentLinkKeys = new HashSet<DocumentIterationKey>();
 
                 Set<DocumentLink> currentLinks = new HashSet<DocumentLink>(partIte.getLinkedDocuments());
 
                 for (DocumentLink link : currentLinks) {
                     DocumentIterationKey linkKey = link.getTargetDocumentKey();
                     if (!linkKeys.contains(linkKey)) {
                         partIte.getLinkedDocuments().remove(link);
                     } else
                         currentLinkKeys.add(linkKey);
                 }
 
                 for (DocumentIterationKey link : linkKeys) {
                     if (!currentLinkKeys.contains(link)) {
                         DocumentLink newLink = new DocumentLink(em.getReference(DocumentIteration.class, link));
                         linkDAO.createLink(newLink);
                         partIte.getLinkedDocuments().add(newLink);
                     }
                 }
             }
             if (pUsageLinks != null) {
                 List<PartUsageLink> usageLinks = new LinkedList<PartUsageLink>();
                 for (PartUsageLink usageLink : pUsageLinks) {
                     PartUsageLink ul = new PartUsageLink();
                     ul.setAmount(usageLink.getAmount());
                     ul.setCadInstances(usageLink.getCadInstances());
                     ul.setComment(usageLink.getComment());
                     ul.setReferenceDescription(usageLink.getReferenceDescription());
                     ul.setUnit(usageLink.getUnit());
                     PartMaster pm = usageLink.getComponent();
                     PartMaster component = partMDAO.loadPartM(new PartMasterKey(pm.getWorkspaceId(), pm.getNumber()));
                     ul.setComponent(component);
                     List<PartSubstituteLink> substitutes = new LinkedList<PartSubstituteLink>();
                     for (PartSubstituteLink substitute : usageLink.getSubstitutes()) {
                         PartSubstituteLink sub = new PartSubstituteLink();
                         sub.setCadInstances(substitute.getCadInstances());
                         sub.setComment(substitute.getComment());
                         sub.setReferenceDescription(substitute.getReferenceDescription());
                         PartMaster pmSub = substitute.getSubstitute();
                         sub.setSubstitute(partMDAO.loadPartM(new PartMasterKey(pmSub.getWorkspaceId(), pmSub.getNumber())));
                         substitutes.add(sub);
                     }
                     ul.setSubstitutes(substitutes);
                     usageLinks.add(ul);
                 }
 
                 partIte.setComponents(usageLinks);
             }
             if (pAttributes != null) {
                 // set doc for all attributes
                 Map<String, InstanceAttribute> attrs = new HashMap<String, InstanceAttribute>();
                 for (InstanceAttribute attr : pAttributes) {
                     attrs.put(attr.getName(), attr);
                 }
 
                 Set<InstanceAttribute> currentAttrs = new HashSet<InstanceAttribute>(partIte.getInstanceAttributes().values());
                 for (InstanceAttribute attr : currentAttrs) {
                     if (!attrs.containsKey(attr.getName())) {
                         partIte.getInstanceAttributes().remove(attr.getName());
                     }
                 }
 
                 for (InstanceAttribute attr : attrs.values()) {
                     if(!partIte.getInstanceAttributes().containsKey(attr.getName())){
                         partIte.getInstanceAttributes().put(attr.getName(), attr);
                     }else if(partIte.getInstanceAttributes().get(attr.getName()).getClass() != attr.getClass()){
                         partIte.getInstanceAttributes().remove(attr.getName());
                         partIte.getInstanceAttributes().put(attr.getName(), attr);
                     }else{
                         partIte.getInstanceAttributes().get(attr.getName()).setValue(attr.getValue());
                     }
                 }
             }
 
             partIte.setIterationNote(pIterationNote);
 
             partIte.setSource(source);
 
             return partRev;
 
         } else {
             throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException25");
         }
 
     }
 
     @RolesAllowed("users")
     @Override
     public PartRevision getPartRevision(PartRevisionKey pPartRPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartRevisionNotFoundException {
         User user = userManager.checkWorkspaceReadAccess(pPartRPK.getPartMaster().getWorkspace());
         PartRevision partR = new PartRevisionDAO(new Locale(user.getLanguage()), em).loadPartR(pPartRPK);
 
         if ((partR.isCheckedOut()) && (!partR.getCheckOutUser().equals(user))) {
             em.detach(partR);
             partR.removeLastIteration();
         }
         return partR;
     }
 
     @RolesAllowed("users")
     @Override
     public List<PartUsageLink> getComponents(PartIterationKey pPartIPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException, NotAllowedException {
         User user = userManager.checkWorkspaceReadAccess(pPartIPK.getWorkspaceId());
         PartIteration partI = new PartIterationDAO(new Locale(user.getLanguage()), em).loadPartI(pPartIPK);
         PartRevision partR = partI.getPartRevision();
 
         if ((partR.isCheckedOut()) && (!partR.getCheckOutUser().equals(user)) && partR.getLastIteration().equals(partI)) {
             throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException34");
         }
         List<PartUsageLink> usageLinks = partI.getComponents();
         return usageLinks;
     }
 
     @RolesAllowed("users")
     @Override
     public boolean partMasterExists(PartMasterKey partMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
         User user = userManager.checkWorkspaceReadAccess(partMasterKey.getWorkspace());
         try {
             new PartMasterDAO(new Locale(user.getLanguage()), em).loadPartM(partMasterKey);
             return true;
         } catch (PartMasterNotFoundException e) {
             return false;
         }
     }
 
     @Override
     public void deleteConfigurationItem(ConfigurationItemKey configurationItemKey) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, NotAllowedException, UserNotActiveException, ConfigurationItemNotFoundException, LayerNotFoundException {
         User user = userManager.checkWorkspaceReadAccess(configurationItemKey.getWorkspace());
         new ConfigurationItemDAO(new Locale(user.getLanguage()),em).removeConfigurationItem(configurationItemKey);
     }
 
     @Override
     public void deleteLayer(String workspaceId, int layerId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException {
         User user = userManager.checkWorkspaceReadAccess(workspaceId);
         new LayerDAO(new Locale(user.getLanguage()),em).deleteLayer(layerId);
     }
 
     @Override
     public void removeCADFileFromPartIteration(PartIterationKey partIKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartIterationNotFoundException {
         User user = userManager.checkWorkspaceReadAccess(partIKey.getWorkspaceId());
 
         PartIteration partIteration = new PartIterationDAO(new Locale(user.getLanguage()),em).loadPartI(partIKey);
         BinaryResource br = partIteration.getNativeCADFile();
         if(br != null){
             dataManager.delData(br);
             partIteration.setNativeCADFile(null);
         }
 
         List<Geometry> geometries = new ArrayList<>(partIteration.getGeometries());
         for(Geometry geometry : geometries){
             dataManager.delData(geometry);
             partIteration.removeGeometry(geometry);
         }
 
         Set<BinaryResource> attachedFiles = new HashSet<>(partIteration.getAttachedFiles());
         for(BinaryResource attachedFile : attachedFiles){
             dataManager.delData(attachedFile);
             partIteration.removeFile(attachedFile);
         }
     }
 
     @RolesAllowed("users")
     @Override
     public PartMaster getPartMaster(PartMasterKey pPartMPK) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterNotFoundException {
         User user = userManager.checkWorkspaceReadAccess(pPartMPK.getWorkspace());
         PartMaster partM = new PartMasterDAO(new Locale(user.getLanguage()), em).loadPartM(pPartMPK);
 
         for (PartRevision partR : partM.getPartRevisions()) {
             if ((partR.isCheckedOut()) && (!partR.getCheckOutUser().equals(user))) {
                 em.detach(partR);
                 partR.removeLastIteration();
             }
         }
         return partM;
     }
 
     @RolesAllowed("users")
     @Override
     public List<Layer> getLayers(ConfigurationItemKey pKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
         User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspace());
         return new LayerDAO(new Locale(user.getLanguage()), em).findAllLayers(pKey);
     }
 
     @RolesAllowed("users")
     @Override
     public Layer getLayer(int pId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, LayerNotFoundException {
         Layer layer = new LayerDAO(em).loadLayer(pId);
         User user = userManager.checkWorkspaceReadAccess(layer.getConfigurationItem().getWorkspaceId());
         return layer;
     }
 
     @RolesAllowed("users")
     @Override
     public Layer createLayer(ConfigurationItemKey pKey, String pName) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException {
         User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspace());
         ConfigurationItem ci = new ConfigurationItemDAO(new Locale(user.getLanguage()), em).loadConfigurationItem(pKey);
         Layer layer = new Layer(pName, user, ci);
         Date now = new Date();
         layer.setCreationDate(now);
 
         new LayerDAO(new Locale(user.getLanguage()), em).createLayer(layer);
         return layer;
     }
 
     @Override
     public Layer updateLayer(ConfigurationItemKey pKey, int pId, String pName) throws UserNotFoundException, WorkspaceNotFoundException, AccessRightException, ConfigurationItemNotFoundException, LayerNotFoundException, UserNotActiveException {
         User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspace());
         Layer layer = getLayer(pId);
         layer.setName(pName);
         return layer;
     }
 
     @Override
     public Marker createMarker(int pLayerId, String pTitle, String pDescription, double pX, double pY, double pZ) throws LayerNotFoundException, UserNotFoundException, WorkspaceNotFoundException, AccessRightException {
         Layer layer = new LayerDAO(em).loadLayer(pLayerId);
         User user = userManager.checkWorkspaceWriteAccess(layer.getConfigurationItem().getWorkspaceId());
         Marker marker = new Marker(pTitle, user, pDescription, pX, pY, pZ);
         Date now = new Date();
         marker.setCreationDate(now);
 
         new MarkerDAO(new Locale(user.getLanguage()), em).createMarker(marker);
         layer.addMarker(marker);
         return marker;
     }
 
     @Override
     public void deleteMarker(int pLayerId, int pMarkerId) throws WorkspaceNotFoundException, UserNotActiveException, LayerNotFoundException, UserNotFoundException, AccessRightException, MarkerNotFoundException {
         Layer layer = new LayerDAO(em).loadLayer(pLayerId);
         User user = userManager.checkWorkspaceWriteAccess(layer.getConfigurationItem().getWorkspaceId());
         Locale locale = new Locale(user.getLanguage());
         Marker marker = new MarkerDAO(locale, em).loadMarker(pMarkerId);
 
         if (layer.getMarkers().contains(marker)) {
             layer.removeMarker(marker);
             em.flush();
             new MarkerDAO(locale, em).removeMarker(pMarkerId);
         } else {
             throw new MarkerNotFoundException(locale, pMarkerId);
         }
 
     }
 
     //
     // ############################## PARTS
     // TODO : crud for part templates
 
 
     @RolesAllowed("users")
     @Override
     public PartMasterTemplate[] getPartMasterTemplates(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
         User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
         return new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).findAllPartMTemplates(pWorkspaceId);
     }
 
     @RolesAllowed("users")
     @Override
     public PartMasterTemplate getPartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, PartMasterTemplateNotFoundException, UserNotFoundException, UserNotActiveException {
         User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
         return new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).loadPartMTemplate(pKey);
     }
 
 
     @RolesAllowed("users")
     @Override
     public PartMasterTemplate createPartMasterTemplate(String pWorkspaceId, String pId, String pPartType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateAlreadyExistsException, UserNotFoundException, NotAllowedException, CreationException {
         User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
         if (!NamingConvention.correct(pId)) {
             throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException9");
         }
         PartMasterTemplate template = new PartMasterTemplate(user.getWorkspace(), pId, user, pPartType, pMask);
         Date now = new Date();
         template.setCreationDate(now);
         template.setIdGenerated(idGenerated);
 
         Set<InstanceAttributeTemplate> attrs = new HashSet<InstanceAttributeTemplate>();
         for (InstanceAttributeTemplate attr : pAttributeTemplates) {
             attrs.add(attr);
         }
         template.setAttributeTemplates(attrs);
 
         new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).createPartMTemplate(template);
         return template;
     }
 
     @RolesAllowed("users")
     @Override
     public PartMasterTemplate updatePartMasterTemplate(PartMasterTemplateKey pKey, String pPartType, String pMask, InstanceAttributeTemplate[] pAttributeTemplates, boolean idGenerated) throws WorkspaceNotFoundException, WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException {
         User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
 
         PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em);
         PartMasterTemplate template = templateDAO.loadPartMTemplate(pKey);
         Date now = new Date();
         template.setCreationDate(now);
         template.setAuthor(user);
         template.setPartType(pPartType);
         template.setMask(pMask);
         template.setIdGenerated(idGenerated);
 
         Set<InstanceAttributeTemplate> attrs = new HashSet<InstanceAttributeTemplate>();
         for (InstanceAttributeTemplate attr : pAttributeTemplates) {
             attrs.add(attr);
         }
 
         Set<InstanceAttributeTemplate> attrsToRemove = new HashSet<InstanceAttributeTemplate>(template.getAttributeTemplates());
         attrsToRemove.removeAll(attrs);
 
         InstanceAttributeTemplateDAO attrDAO = new InstanceAttributeTemplateDAO(em);
         for (InstanceAttributeTemplate attrToRemove : attrsToRemove) {
             attrDAO.removeAttribute(attrToRemove);
         }
 
         template.setAttributeTemplates(attrs);
         return template;
     }
 
     @RolesAllowed("users")
     @Override
     public void deletePartMasterTemplate(PartMasterTemplateKey pKey) throws WorkspaceNotFoundException, AccessRightException, PartMasterTemplateNotFoundException, UserNotFoundException {
         User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
         PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em);
         PartMasterTemplate template = templateDAO.removePartMTemplate(pKey);
         BinaryResource file = template.getAttachedFile();
        if(file != null){
            dataManager.delData(file);
        }
     }
 
 
     @RolesAllowed("users")
     @Override
     public File saveFileInTemplate(PartMasterTemplateKey pPartMTemplateKey, String pName, long pSize) throws WorkspaceNotFoundException, NotAllowedException, PartMasterTemplateNotFoundException, FileAlreadyExistsException, UserNotFoundException, UserNotActiveException, CreationException {
         User user = userManager.checkWorkspaceReadAccess(pPartMTemplateKey.getWorkspaceId());
         //TODO checkWorkspaceWriteAccess ?
         if (!NamingConvention.correct(pName)) {
             throw new NotAllowedException(Locale.getDefault(), "NotAllowedException9");
         }
 
         PartMasterTemplateDAO templateDAO = new PartMasterTemplateDAO(em);
         PartMasterTemplate template = templateDAO.loadPartMTemplate(pPartMTemplateKey);
         BinaryResource file = null;
         String fullName = template.getWorkspaceId() + "/part-templates/" + template.getId() + "/" + pName;
 
         BinaryResource bin = template.getAttachedFile();
         if(bin != null){
             if (bin.getFullName().equals(fullName)) {
                 file = bin;
             }
         }
 
         if (file == null) {
             file = new BinaryResource(fullName, pSize);
             new BinaryResourceDAO(em).createBinaryResource(file);
             template.setAttachedFile(file);
         } else {
             file.setContentLength(pSize);
         }
 
         return dataManager.getVaultFile(file);
     }
 
     @RolesAllowed("users")
     @Override
     public PartMasterTemplate removeFileFromTemplate(String pFullName) throws WorkspaceNotFoundException, PartMasterTemplateNotFoundException, AccessRightException, FileNotFoundException, UserNotFoundException, UserNotActiveException {
         User user = userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(pFullName));
         //TODO checkWorkspaceWriteAccess ?
         BinaryResourceDAO binDAO = new BinaryResourceDAO(new Locale(user.getLanguage()), em);
         BinaryResource file = binDAO.loadBinaryResource(pFullName);
 
         PartMasterTemplate template = binDAO.getPartTemplateOwner(file);
         dataManager.delData(file);
         template.setAttachedFile(null);
         binDAO.removeBinaryResource(file);
         return template;
     }
 
     @RolesAllowed("users")
     @Override
     public List<PartMaster> getPartMasters(String pWorkspaceId, int start, int pMaxResults) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException {
         User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
         return new PartMasterDAO(new Locale(user.getLanguage()), em).getParts(pWorkspaceId,start,pMaxResults);
     }
 
     @RolesAllowed("users")
     @Override
     public int getPartMastersCount(String pWorkspaceId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException {
         User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
         return new PartMasterDAO(new Locale(user.getLanguage()), em).getPartsCount(pWorkspaceId);
     }
 
     @RolesAllowed("users")
     @Override
     public void deletePartMaster(PartMasterKey partMasterKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterNotFoundException, EntityConstraintException {
 
         User user = userManager.checkWorkspaceReadAccess(partMasterKey.getWorkspace());
 
         PartMasterDAO partMasterDAO = new PartMasterDAO(new Locale(user.getLanguage()), em);
         PartUsageLinkDAO partUsageLinkDAO = new PartUsageLinkDAO(new Locale(user.getLanguage()), em);
         ConfigurationItemDAO configurationItemDAO = new ConfigurationItemDAO(new Locale(user.getLanguage()),em);
         PartMaster partMaster = partMasterDAO.loadPartM(partMasterKey);
 
         // check if part is linked to a product
         if(configurationItemDAO.isPartMasterLinkedToConfigurationItem(partMaster)){
             throw new EntityConstraintException(new Locale(user.getLanguage()),"EntityConstraintException1");
         }
 
         // check if this part is in a partUsage
         if(partUsageLinkDAO.hasPartUsages(partMasterKey.getWorkspace(),partMaster.getNumber())){
             throw new EntityConstraintException(new Locale(user.getLanguage()),"EntityConstraintException2");
         }
 
         // ok to delete
         partMasterDAO.removePartM(partMaster);
     }
 
     @RolesAllowed("users")
     @Override
     public String generateId(String pWorkspaceId, String pPartMTemplateId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, PartMasterTemplateNotFoundException {
 
         User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
         PartMasterTemplate template = new PartMasterTemplateDAO(new Locale(user.getLanguage()), em).loadPartMTemplate(new PartMasterTemplateKey(user.getWorkspaceId(), pPartMTemplateId));
 
         String newId = null;
         try {
             String latestId = new PartMasterDAO(new Locale(user.getLanguage()), em).findLatestPartMId(pWorkspaceId, template.getPartType());
             String inputMask = template.getMask();
             String convertedMask = Tools.convertMask(inputMask);
             newId = Tools.increaseId(latestId, convertedMask);
         } catch (ParseException ex) {
             //may happen when a different mask has been used for the same document type
         } catch (NoResultException ex) {
             //may happen when no document of the specified type has been created
         }
         return newId;
 
     }
 
 
 
 }
 //TODO when using layers and markers, check for concordance
 //TODO add a method to update a marker
 //TODO use dozer
