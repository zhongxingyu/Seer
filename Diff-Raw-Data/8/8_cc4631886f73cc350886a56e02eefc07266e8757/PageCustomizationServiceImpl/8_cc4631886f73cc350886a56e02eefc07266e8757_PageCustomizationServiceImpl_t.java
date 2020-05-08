 package com.mpower.service.customization;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.beanutils.BeanComparator;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.mpower.dao.customization.PageCustomizationDAO;
 import com.mpower.domain.customization.PageDefinition;
 import com.mpower.domain.customization.SectionDefinition;
 import com.mpower.domain.customization.SectionField;
 import com.mpower.type.AccessType;
 import com.mpower.type.PageType;
 import com.mpower.type.RoleType;
 
 @Service("pageCustomizationService")
 public class PageCustomizationServiceImpl implements PageCustomizationService {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     private static final Integer ZERO = Integer.valueOf(0);
 
     @Resource(name = "pageCustomizationDao")
     private PageCustomizationDAO pageCustomizationDao;
 
     /*
      * (non-Javadoc)
      * @see com.mpower.service.customization.PageCustomizationService#readSectionDefinitionsBySiteAndPageType(java.lang.String, com.mpower.type.PageType, java.util.List)
      */
     @SuppressWarnings("unchecked")
     @Override
     public List<SectionDefinition> readSectionDefinitionsBySiteAndPageType(String siteName, PageType pageType, List<String> roles) {
         List<SectionDefinition> sectionDefinitions = pageCustomizationDao.readSectionDefinitions(siteName, pageType, roles);
         List<SectionDefinition> returnSections = removeDuplicateSectionDefinitions(sectionDefinitions);
         Collections.sort(returnSections, new BeanComparator("sectionOrder"));
         return returnSections;
     }
 
     /*
      * (non-Javadoc)
      * @see com.mpower.service.customization.PageCustomizationService#readSectionFieldsBySiteAndSectionName(java.lang.String, com.mpower.domain.customization.SectionDefinition)
      */
     @SuppressWarnings("unchecked")
     @Override
     public List<SectionField> readSectionFieldsBySiteAndSectionName(String siteName, SectionDefinition sectionDefinition) {
         List<SectionField> returnFields;
         List<SectionField> outOfBoxSectionFields = pageCustomizationDao.readOutOfBoxSectionFields(sectionDefinition.getSectionName());
         List<SectionField> customSectionFields = pageCustomizationDao.readCustomizedSectionFields(siteName, sectionDefinition.getId());
         if (customSectionFields.isEmpty()) {
             returnFields = outOfBoxSectionFields;
         } else {
             returnFields = new ArrayList<SectionField>(outOfBoxSectionFields);
             for (SectionField customizedField : customSectionFields) {
                 removeMatchingOutOfBoxField(returnFields, customizedField);
                 if (!ZERO.equals(customizedField.getFieldOrder())) {
                     returnFields.add(customizedField);
                 }
             }
         }
         Collections.sort(returnFields, new BeanComparator("fieldOrder"));
         return returnFields;
     }
 
     private void removeMatchingOutOfBoxField(List<SectionField> sectionFields, SectionField customizedField) {
         for (int i = 0; i < sectionFields.size(); i++) {
             SectionField currentField = sectionFields.get(i);
             String currentFieldName = currentField.getFieldPropertyName();
             String customFieldName = customizedField.getFieldPropertyName();
             if (currentFieldName.equals(customFieldName)) {
                 sectionFields.remove(i);
                 break;
             }
         }
     }
 
     /**
      * Takes a <code>List</code> of <code>SectionDefinition</code> objects and keeps the section name with the highest role
      * @param sectionDefinitions the original <code>List</code> to filter
      * @return
      */
     private List<SectionDefinition> removeDuplicateSectionDefinitions(List<SectionDefinition> sectionDefinitions) {
         if (sectionDefinitions != null) {
             Map<String, SectionDefinition> nameDefinitionMap = new HashMap<String, SectionDefinition>();
             for (SectionDefinition sectionDefinition : sectionDefinitions) {
                 SectionDefinition sd = nameDefinitionMap.get(sectionDefinition.getSectionName());
                 if (sd == null) {
                     nameDefinitionMap.put(sectionDefinition.getSectionName(), sectionDefinition);
                 } else {
                     Integer sectionDefinitionRoleRank = sectionDefinition.getRole() == null ? -1 : RoleType.valueOf(sectionDefinition.getRole()).getRoleRank();
                     Integer sdRoleRank = sd.getRole() == null ? -1 : RoleType.valueOf(sd.getRole()).getRoleRank();
                     if ((sd.getSite() == null && sectionDefinition.getSite() != null) || sdRoleRank < sectionDefinitionRoleRank) {
                         nameDefinitionMap.put(sectionDefinition.getSectionName(), sectionDefinition);
                     }
                 }
             }
             return new ArrayList<SectionDefinition>(nameDefinitionMap.values());
         }
         return null;
     }
 
     /*
      * (non-Javadoc)
      * @see com.mpower.service.customization.PageCustomizationService#readPageAccess(java.lang.String, java.util.List)
      */
     @Override
     @Transactional
     public Map<String, AccessType> readPageAccess(String siteName, List<String> roles) {
         Map<String, PageDefinition> pageDefinitionMap = new HashMap<String, PageDefinition>(); // pageType, PageDefinition
         List<PageDefinition> pages = pageCustomizationDao.readPageDefinitions(siteName);
         if (pages != null) {
             for (PageDefinition pageDefinition : pages) {
                if (roles.contains(pageDefinition.getRole())) {
                    PageDefinition pd = pageDefinitionMap.get(pageDefinition.getPageType().name());
                    if (pd == null || pd.getSite() == null || (RoleType.valueOf(pd.getRole()).getRoleRank() < RoleType.valueOf(pageDefinition.getRole()).getRoleRank())) {
                        pageDefinitionMap.put(pageDefinition.getPageType().getPageName(), pageDefinition);
                    }
                 }
             }
         }
 
         Map<String, AccessType> pageAccessMap = new HashMap<String, AccessType>(); // pageType, AccessType
         for (String key : pageDefinitionMap.keySet()) {
             pageAccessMap.put(key, pageDefinitionMap.get(key).getAccessType());
         }
         return pageAccessMap;
     }
 }
