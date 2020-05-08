 package gov.nih.nci.ncicb.cadsr.loader.persister;
 
 import gov.nih.nci.ncicb.cadsr.dao.*;
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.spring.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.defaults.UMLDefaults;
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 
 public class UMLPersister implements Persister {
   private static Logger logger = Logger.getLogger(UMLPersister.class.getName());
 
   private static Map vdMapping = new HashMap();
 
   protected static AdminComponentDAO adminComponentDAO = DAOAccessor.getAdminComponentDAO();
   protected static DataElementDAO dataElementDAO = DAOAccessor.getDataElementDAO();
   protected static DataElementConceptDAO dataElementConceptDAO = DAOAccessor.getDataElementConceptDAO();
   protected static ValueDomainDAO valueDomainDAO = DAOAccessor.getValueDomainDAO();
   protected static PropertyDAO propertyDAO = DAOAccessor.getPropertyDAO();
   protected static ObjectClassDAO objectClassDAO = DAOAccessor.getObjectClassDAO();
   protected static ObjectClassRelationshipDAO objectClassRelationshipDAO = DAOAccessor.getObjectClassRelationshipDAO();
   protected static ClassificationSchemeDAO classificationSchemeDAO = DAOAccessor.getClassificationSchemeDAO();
   protected static ClassificationSchemeItemDAO classificationSchemeItemDAO = DAOAccessor.getClassificationSchemeItemDAO();
   protected static ClassSchemeClassSchemeItemDAO classSchemeClassSchemeItemDAO = DAOAccessor.getClassSchemeClassSchemeItemDAO();
   protected static ConceptDAO conceptDAO = DAOAccessor.getConceptDAO();
 
   protected ElementsLists elements = null;
 
   private Map valueDomains = new HashMap();
 
   protected UMLDefaults defaults = UMLDefaults.getInstance();
 
   protected static final String CSI_PACKAGE_TYPE = "UML_PACKAGE";
 
   static {
     vdMapping.put("int", "java.lang.Integer");
     vdMapping.put("float", "java.lang.Float");
     vdMapping.put("boolean", "java.lang.Boolean");
     vdMapping.put("short", "java.lang.Short");
     vdMapping.put("double", "java.lang.Double");
     vdMapping.put("char", "java.lang.Char");
     vdMapping.put("byte", "java.lang.Byte");
     vdMapping.put("long", "java.lang.Long");
     // !!! TODO Remove
     vdMapping.put("String", "java.lang.String");
   }
 
   public UMLPersister() {
     
   }
 
   public UMLPersister(ElementsLists list) {
     this.elements = list;
   }
 
   public void persist() throws PersisterException {
 
     new PackagePersister(elements).persist();
     new ConceptPersister(elements).persist();
     new PropertyPersister(elements).persist();
     new ObjectClassPersister(elements).persist();
     new DECPersister(elements).persist();
     new DEPersister(elements).persist();
     new OcRecPersister(elements).persist();
   }
 
 //   protected void addProjectCs(AdminComponent ac) throws PersisterException {
 //     List l = adminComponentDAO.getClassSchemeClassSchemeItems(ac);
 
 //     // is projectCs linked?
 //     boolean found = false;
 
 //     for (ListIterator it = l.listIterator(); it.hasNext();) {
 //       ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem) it.next();
 
 //       if (csCsi.getCs().getLongName().equals(defaults.getProjectCs().getLongName())) {
 // 	if (csCsi.getCsi().getName().equals(defaults.getDomainCsi().getName())) {
 // 	  found = true;
 // 	}
 //       }
 //     }
 
 //     List csCsis = new ArrayList();
 
 //     if (!found) {
 //       logger.info(PropertyAccessor.
 //                   getProperty("attach.project.classification"));
 //       csCsis.add(defaults.getProjectCsCsi());
 //       adminComponentDAO.addClassSchemeClassSchemeItems(ac, csCsis);
 //     }
 //   }
 
 
 
 
   protected ValueDomain lookupValueDomain(ValueDomain vd)
     throws PersisterException {
 
     // replace this name if:
     if(vdMapping.containsKey(vd.getPreferredName().trim()))
       vd.setPreferredName((String)vdMapping.get(vd.getPreferredName().trim()));
 
     ValueDomain result = (ValueDomain) valueDomains.get(vd.getPreferredName());
 
     if (result == null) { // not in cache -- go to db
       List l = valueDomainDAO.find(vd);
 
       if (l.size() == 0) {
 	throw new PersisterException("Value Domain " +
 				     vd.getPreferredName() + " does not exist.");
       }
 
       result = (ValueDomain) l.get(0);
       valueDomains.put(result.getPreferredName(), result);
     }
 
     return result;
   }
 
   protected void addAlternateName(AdminComponent ac, String newName, String type, String packageName) {
 
     List altNames = adminComponentDAO.getAlternateNames(ac);
     boolean found = false;
     ClassSchemeClassSchemeItem packageCsCsi = (ClassSchemeClassSchemeItem)defaults.getPackageCsCsis().get(packageName);
     for(Iterator it = altNames.iterator(); it.hasNext(); ) {
       AlternateName an = (AlternateName)it.next();
       if(an.getType().equals(type) && an.getName().equals(newName)) {
         found = true;
         logger.info(PropertyAccessor.getProperty(
                       "existed.altName", newName));
 
         if(packageName == null)
           return;
         
         boolean csFound = false;
         for(Iterator it2 = an.getCsCsis().iterator(); it2.hasNext();) {
           ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem)it2.next();
          if(csCsi.getCs().getId().equals(defaults.getProjectCs().getId())
             && csCsi.getCsi().getId().equals(packageCsCsi.getId())) {
             csFound = true;
           }
         }
         if(!csFound) {
           classSchemeClassSchemeItemDAO.addCsCsi(an, packageCsCsi);
           logger.info(
             PropertyAccessor.getProperty(
               "linked.to.package",
               "Alternate Name"
               ));
         }
         
       }
     }
     
     if(!found) {
       AlternateName altName = DomainObjectFactory.newAlternateName();
       altName.setContext(defaults.getContext());
       altName.setName(newName);
       altName.setType(type);
       altName.setId(adminComponentDAO.addAlternateName(ac, altName));
       logger.info(PropertyAccessor.getProperty(
                     "added.altName", 
                     new String[] {
                       altName.getName(),
                       ac.getLongName()
                     }));
       
       if(packageName != null) {
         classSchemeClassSchemeItemDAO.addCsCsi(altName, packageCsCsi);
         logger.info(
           PropertyAccessor.getProperty(
             "linked.to.package",
             "Alternate Name"
             ));
       }      
 
     } 
   }
 
   protected void addAlternateDefinition(AdminComponent ac, String newDef, String type, String packageName) {
 
     List altDefs = adminComponentDAO.getDefinitions(ac);
     boolean found = false;
     ClassSchemeClassSchemeItem packageCsCsi = (ClassSchemeClassSchemeItem)defaults.getPackageCsCsis().get(packageName);
 
     for(Iterator it = altDefs.iterator(); it.hasNext(); ) {
       Definition def = (Definition)it.next();
       if(def.getType().equals(type) && def.getDefinition().equals(newDef)) {
         found = true;
         logger.info(PropertyAccessor.getProperty(
                       "existed.altDef", newDef));
         
         boolean csFound = false;
         for(Iterator it2 = def.getCsCsis().iterator(); it2.hasNext();) {
           ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem)it2.next();
           if(csCsi.getCs().getId().equals(defaults.getProjectCs().getId())
              && csCsi.getCsi().getId().equals(packageCsCsi.getId())) {
             csFound = true;
           }
         }
         if(!csFound) {
           classSchemeClassSchemeItemDAO.addCsCsi(def, packageCsCsi);
           logger.info(
             PropertyAccessor.getProperty(
               "linked.to.package",
               "Alternate Definition"
               ));
         }
         
       }
     }
     
     if(!found) {
       Definition altDef = DomainObjectFactory.newDefinition();
       altDef.setContext(defaults.getContext());
       altDef.setDefinition(newDef);
       altDef.setAudit(defaults.getAudit());
       altDef.setType(type);
       altDef.setId(adminComponentDAO.addDefinition(ac, altDef));
       logger.info(PropertyAccessor.getProperty(
                     "added.altDef", 
                     new String[] {
                       altDef.getId(),
                       altDef.getDefinition(),
                       ac.getLongName()
                     }));
       
       classSchemeClassSchemeItemDAO.addCsCsi(altDef, packageCsCsi);
       logger.info(
         PropertyAccessor.getProperty(
           "linked.to.package",
           "Alternate Definition"
           ));
       
     } 
   }
 
   protected Concept lookupConcept(String conceptCode) {
     List concepts = (List) elements.getElements(DomainObjectFactory.newConcept().getClass());
 
     for (Iterator it = concepts.iterator(); it.hasNext();) {
       Concept con = (Concept)it.next();
       if(con.getPreferredName().equals(conceptCode))
         return con;
     }
     return null;
 
   }
 
   protected Property lookupProperty(String preferredName) {
     List props = (List) elements.getElements(DomainObjectFactory.newProperty().getClass());
     
     for (Iterator it = props.iterator(); it.hasNext(); ) {
       Property o = (Property) it.next();
 
       if (o.getPreferredName().equals(preferredName)) {
         return o;
       }
     }
     return null;
   }
 
   protected ObjectClass lookupObjectClass(String preferredName) {
     List ocs = (List) elements.getElements(DomainObjectFactory.newObjectClass().getClass());
     
     for (Iterator it = ocs.iterator(); it.hasNext(); ) {
       ObjectClass o = (ObjectClass) it.next();
 
       if (o.getPreferredName().equals(preferredName)) {
         return o;
       }
     }
     return null;
   }
 
   protected DataElementConcept lookupDec(String id) {
     List decs = (List) elements.getElements(DomainObjectFactory.newDataElementConcept().getClass());
     
     for (Iterator it = decs.iterator(); it.hasNext(); ) {
       DataElementConcept o = (DataElementConcept) it.next();
 
       if (o.getId().equals(id)) {
         return o;
       }
     }
     return null;
   }
 
   protected String longNameFromConcepts(Concept[] concepts) {
     StringBuffer sb = new StringBuffer();
     
     for(int i=0; i < concepts.length; i++) {
       if(sb.length() > 0)
         sb.append(" ");
       sb.append(StringUtil.upperFirst(concepts[i].getLongName()));
     }
 
     return sb.toString();
 
   }
 
   protected String preferredDefinitionFromConcepts(Concept[] concepts) {
     StringBuffer sb = new StringBuffer();
     
     for(int i=0; i < concepts.length; i++) {
       if(sb.length() > 0)
         sb.append("\n");
       sb.append(concepts[i].getPreferredName());
       sb.append(":");
       sb.append(concepts[i].getPreferredDefinition());
     }
 
     return sb.toString();
   }
 
   protected void addPackageClassification(AdminComponent ac, String packageName) {
 
     List l = adminComponentDAO.getClassSchemeClassSchemeItems(ac);
 
     ClassSchemeClassSchemeItem packageCsCsi = (ClassSchemeClassSchemeItem)defaults.getPackageCsCsis().get(packageName);
 
     // is projectCs linked?
     boolean found = false;
 
     for (ListIterator it = l.listIterator(); it.hasNext();) {
       ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem) it.next();
 
       if(csCsi.getId().equals(packageCsCsi.getId()))
         found = true;
     }
 
     List csCsis = new ArrayList();
 
     if (!found) {
       logger.info(PropertyAccessor.
                   getProperty("attach.package.classification"));
       
       if (packageCsCsi != null) {
         csCsis.add(packageCsCsi);
 
         adminComponentDAO.addClassSchemeClassSchemeItems(ac, csCsis);
         logger.info(PropertyAccessor
                     .getProperty("added.package",
                                  new String[] {
                                    packageName, 
                                    ac.getLongName()}));
       } else {
         // PersistPackages should have taken care of it. 
         // We should not be here.
         logger.error(PropertyAccessor.getProperty("missing.package", new String[] {packageName, ac.getLongName()}));
       }
       
     }
   }
 
   protected String getPackageName(AdminComponent ac) {
     return 
       ((AdminComponentClassSchemeClassSchemeItem)ac.getAcCsCsis().get(0)).getCsCsi().getCsi().getComments();
   }
 
 }
