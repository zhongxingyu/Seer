 /*
  * Copyright 2000-2005 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  *
  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  *
  * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
  *
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
  *
  * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
  *
  * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
  *
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
  */
 package gov.nih.nci.ncicb.cadsr.loader.defaults;
 
 import gov.nih.nci.ncicb.cadsr.dao.*;
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 
 import gov.nih.nci.ncicb.cadsr.loader.persister.PersisterException;
 import gov.nih.nci.ncicb.cadsr.loader.persister.LogUtil;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 /**
  * <code>UMLDefaults</code> is a placeholder for UMLLoader's defaults.
  * <br/> Implements the Singleton pattern.
  *
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
  */
 public class UMLDefaults {
   private static Logger logger = Logger.getLogger(UMLDefaults.class.getName());
 
   private static UMLDefaults singleton = new UMLDefaults();
 
   private Context context;
   private ConceptualDomain conceptualDomain;
   private ClassificationScheme projectCs;
   private Context mainContext;
 
   // default Audit holds username
   private Audit audit;
   private Lifecycle lifecycle;
   private Map packageCsCsis = new HashMap();
 
   private Map params = new HashMap();
 
   private Map packageFilter = new HashMap();
   private String defaultPackage, packageFilterString = null;
 
   private LoaderDefault loaderDefault = null;
 
   private String filename;
   private String origin;
 
   private UMLDefaults() {
   }
 
   /**
    *
    * @return the singleton
    */
   public static UMLDefaults getInstance() {
     return singleton;
   }
 
   
   /**
    * Called by the main class to initialize defaults.
    *
    * @param projectName the project name
    * @param username the authenticated username
    * @exception PersisterException if an error occurs
    */
   public void initParams(String filename) throws PersisterException  {
     this.filename = filename;
 
     loaderDefault = new AttachedFileDefaultsLoader().loadDefaults(filename);
 
     if(loaderDefault == null) 
       loaderDefault = new EmptyDefaultsLoader().loadDefaults();
 
     initParams();
   }
 
   public void initParams(String projectName, Float projectVersion, String username) throws PersisterException {
     loaderDefault = new DBDefaultsLoader().loadDefaults(projectName, projectVersion);
 
     initParams();
 
   }
 
   private void initParams() throws PersisterException {
 
     audit = DomainObjectFactory.newAudit();
     lifecycle = DomainObjectFactory.newLifecycle();
     lifecycle.setBeginDate(new java.util.Date(System.currentTimeMillis()));
 
     origin = PropertyAccessor.getProperty("default.origin");
 
     if (loaderDefault == null) {
       throw new PersisterException(
 	"Defaults not found. Please create a profile first.");
     }
 
     String cName = loaderDefault.getContextName();
     
     if (cName == null) {
       throw new PersisterException("Context Name not Set.");
     }
     
     context = DomainObjectFactory.newContext();
     context.setName(cName);
 
     
     if (loaderDefault.getWorkflowStatus() == null) {
       throw new PersisterException("WorkflowStatus not Set.");
     }
     
     conceptualDomain = DomainObjectFactory.newConceptualDomain();
     conceptualDomain.setPreferredName(loaderDefault.getCdName());
 
     Context cdContext = DomainObjectFactory.newContext();
     cdContext.setName(loaderDefault.getCdContextName());
 
     conceptualDomain.setContext(cdContext);
 
     projectCs = DomainObjectFactory.newClassificationScheme();
 
     projectCs.setPreferredName(loaderDefault.getProjectName());
     projectCs.setVersion(loaderDefault.getProjectVersion());
     projectCs.setContext(context);
     projectCs.setLongName(loaderDefault.getProjectLongName());
     projectCs.setWorkflowStatus(projectCs.WF_STATUS_DRAFT_NEW);
     projectCs.setPreferredDefinition(loaderDefault.getProjectDescription());
     
     projectCs.setType("Project");
     projectCs.setLabelType(ClassificationScheme.LABEL_TYPE_ALPHA);
 
 
     logger.info(PropertyAccessor.getProperty("listOfPackages"));
     String filt = loaderDefault.getPackageFilter();
     packageFilterString = filt;
     if(!StringUtil.isEmpty(filt)) {
       String[] pkgs = filt.split(",");
       for(int i=0; i<pkgs.length; i++) {
         String s = pkgs[i].trim();
         int ind = s.indexOf(">");
         
         String alias = null; 
         String pkg = null;
         if(ind > 0) {
           alias = s.substring(1, ind).trim();
           pkg = s.substring(ind+1).trim();
           if((pkg.length() == 0) && (pkgs.length == 1)) {
             defaultPackage = alias;
             logger.info(PropertyAccessor.getProperty("packageAlias.default", alias));
           }
         } else {
           alias = pkg = s;
         }
         
         packageFilter.put(pkg, alias);
         
         if(defaultPackage == null)
           logger.info(PropertyAccessor.getProperty("packageAlias", new String[] {pkg, alias}));
       }
       logger.info(PropertyAccessor.getProperty("endOfList"));
     } else {
       logger.info(PropertyAccessor.getProperty("no.package.filter"));
     }
   }
 
   public void initWithDB() throws PersisterException {
     ContextDAO contextDAO = DAOAccessor.getContextDAO();
     ConceptualDomainDAO conceptualDomainDAO = DAOAccessor.getConceptualDomainDAO();
 
     context = contextDAO.findByName(context.getName());
     if (context == null) {
       logger.fatal(PropertyAccessor.getProperty("context.not.found", context.getName()));
       System.exit(1);
     }
     
     mainContext = contextDAO.findByName(PropertyAccessor.getProperty("context.main.name"));
     if (mainContext == null) {
       logger.fatal(PropertyAccessor.getProperty("context.not.found", PropertyAccessor.getProperty("context.main.name")));
       System.exit(1);
     } 
 
     Context cdContext = contextDAO.findByName(conceptualDomain.getContext().getName());
     if (cdContext == null) {
       throw new PersisterException("CD Context not found.");
     }
     conceptualDomain.setContext(cdContext);
       
     try {
       conceptualDomain = (ConceptualDomain) conceptualDomainDAO.find(conceptualDomain)
         .get(0);
     } catch (NullPointerException e) {
       throw new PersisterException("CD: " +
                                    conceptualDomain.getPreferredName() + " not found.");
     }
     
     initClassifications();
 
   }
 
   /**
    * Creates or loads classifications and CSI that will be used.
    *
    * @exception PersisterException if an error occurs
    */
   private void initClassifications() throws PersisterException {
     
     ClassificationSchemeDAO classificationSchemeDAO = DAOAccessor.getClassificationSchemeDAO();
 
     ClassificationScheme cs = DomainObjectFactory.newClassificationScheme();
     cs.setPreferredName(projectCs.getPreferredName());
     cs.setVersion(projectCs.getVersion());
     cs.setContext(context);
 
     ArrayList eager = new ArrayList();
     eager.add(EagerConstants.CS_CSI);
     List result = classificationSchemeDAO.find(cs, eager);
 
     if (result.size() == 0) { // need to add projectName CS
      projectCs.setContext(context);
      projectCs.setAudit(audit);
      projectCs.setId(classificationSchemeDAO.create(projectCs));
 
       cs = DomainObjectFactory.newClassificationScheme();
       cs.setLongName(projectCs.getLongName());
       List newResult = classificationSchemeDAO.find(cs);
       if(newResult.size() > 0) {
         logger.error(PropertyAccessor.getProperty("cs.longName.already.exists"));
         System.exit(1);
       }
 
       logger.info(PropertyAccessor.getProperty("created.project.cs"));
       LogUtil.logAc(projectCs, logger);
       logger.info(PropertyAccessor.getProperty("type", projectCs.getType()));
 
     } 
     else { 
       logger.info(PropertyAccessor.getProperty("existed.project.cs"));
 
       String csLongName = projectCs.getLongName();
       projectCs = (ClassificationScheme) result.get(0);
 
       if(!csLongName.equals(projectCs.getLongName())) {
         logger.error(PropertyAccessor.getProperty("cs.longName.already.exists"));
         System.exit(1);
       }
       
     }
 
   }
 
   /**
    * @return default workflow status
    */
   public String getWorkflowStatus() {
     return loaderDefault.getWorkflowStatus();
   }
   /**
    * @return default context
    */
   public Context getContext() {
 
     return context;
   }
   /**
    * @return default version
    */
   public Float getVersion() {
     return new Float(loaderDefault.getVersion().toString());
   }
   /**
    * @return default CD
    */
   public ConceptualDomain getConceptualDomain() {
     return conceptualDomain;
   }
   /**
    * @return the project CS
    */
   public ClassificationScheme getProjectCs() {
     return projectCs;
   }
 
   public void refreshProjectCs() {
     ClassificationSchemeDAO classificationSchemeDAO = DAOAccessor.getClassificationSchemeDAO();
 
     ArrayList eager = new ArrayList();
     eager.add(EagerConstants.CS_CSI);
     projectCs = (ClassificationScheme)(classificationSchemeDAO.find(projectCs, eager).get(0));
     
   }
   
   /**
    * @return default Audit object -- contains username.
    */
   public Audit getAudit() {
     return audit;
   }
 
   public Lifecycle getLifecycle() {
     return lifecycle;
   }
 
 
   /**
    * @return package cs_csi
    */
   public Map getPackageCsCsis() {
     return packageCsCsis;
   }
   /**
    * The package filter contains a list of packages that will be loaded by UML Loader. If empty, all packages are to be loaded.
    * @return the package filter.
    */
   public Map getPackageFilter() {
     return packageFilter;
   }
   
   /**
    * Sets the packageFilter to the filter passed in
    */
   public void setPackageFilter(Map packageFilter) {
     this.packageFilter = packageFilter;
   }
 
   public String getDefaultPackageAlias() {
     return defaultPackage;
   }
 
   public String getPackageDisplay(String packageName) {
     if(packageFilter.containsKey(packageName)) {
       return (String)packageFilter.get(packageName);
     } else if(defaultPackage != null)
       return defaultPackage;
     else
       return packageName;
   }
 
   public String getPackageFilterString() {
     return packageFilterString;
   }
 
   public Context getMainContext() {
     return mainContext;
   }
  
   public void setUsername(String username) {
     audit.setCreatedBy(username);
   }
   
   public String getOrigin() {
     return origin;
   }
 
   public void updateDefaults(LoaderDefault defaults) {
     try {
       loaderDefault = defaults;
       initParams();
 
       new AttachedFileDefaultsLoader().saveDefaults(defaults, filename);
 
     } catch (PersisterException e){
     } // end of try-catch
   }
 
 
 }
