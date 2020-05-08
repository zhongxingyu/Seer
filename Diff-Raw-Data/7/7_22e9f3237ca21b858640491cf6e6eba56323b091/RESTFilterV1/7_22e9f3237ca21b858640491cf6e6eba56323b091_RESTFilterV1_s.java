 package org.jboss.pressgang.ccms.rest.v1.entities;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTFilterCategoryCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTFilterCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTFilterFieldCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTFilterLocaleCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTFilterTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTFilterCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBasePrimaryEntityV1;
 
 public class RESTFilterV1 extends RESTBasePrimaryEntityV1<RESTFilterV1, RESTFilterCollectionV1, RESTFilterCollectionItemV1> {
     public static final String NAME_NAME = "name";
     public static final String DESCRIPTION_NAME = "description";
     public static final String FILTER_TAGS_NAME = "filterTags_OTM";
     public static final String FILTER_LOCALES_NAME = "filterLocales_OTM";
     public static final String FILTER_CATEGORIES_NAME = "filterCategories_OTM";
     public static final String FILTER_FIELDS_NAME = "filterFields_OTM";
 
     private String name = null;
     private String description = null;
     private RESTFilterTagCollectionV1 filterTags_OTM = null;
     private RESTFilterLocaleCollectionV1 filterLocales_OTM = null;
     private RESTFilterCategoryCollectionV1 filterCategories_OTM = null;
     private RESTFilterFieldCollectionV1 filterFields_OTM = null;
 
     private RESTFilterCollectionV1 revisions = null;
 
     @Override
     public RESTFilterCollectionV1 getRevisions() {
         return revisions;
     }
 
     @Override
     public void setRevisions(final RESTFilterCollectionV1 revisions) {
         this.revisions = revisions;
     }
 
     @Override
     public RESTFilterV1 clone(boolean deepCopy) {
         final RESTFilterV1 retValue = new RESTFilterV1();
 
         this.cloneInto(retValue, deepCopy);
 
         return retValue;
     }
 
     public void cloneInto(final RESTFilterV1 clone, final boolean deepCopy) {
         super.cloneInto(clone, deepCopy);
 
         clone.name = this.name;
         clone.description = this.description;
 
         if (deepCopy) {
             if (this.revisions != null) {
                 clone.revisions = new RESTFilterCollectionV1();
                 revisions.cloneInto(clone.revisions, deepCopy);
             }
 
             if (this.filterCategories_OTM != null) {
                 clone.filterCategories_OTM = new RESTFilterCategoryCollectionV1();
                 filterCategories_OTM.cloneInto(clone.filterCategories_OTM, deepCopy);
             }
 
             if (this.filterLocales_OTM != null) {
                 clone.filterLocales_OTM = new RESTFilterLocaleCollectionV1();
                 filterLocales_OTM.cloneInto(clone.filterLocales_OTM, deepCopy);
             }
 
             if (this.filterTags_OTM != null) {
                 clone.filterTags_OTM = new RESTFilterTagCollectionV1();
                 filterTags_OTM.cloneInto(clone.filterTags_OTM, deepCopy);
             }
         } else {
             clone.revisions = this.revisions;
             clone.filterCategories_OTM = this.filterCategories_OTM;
             clone.filterLocales_OTM = this.filterLocales_OTM;
             clone.filterTags_OTM = this.filterTags_OTM;
         }
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(final String name) {
         this.name = name;
     }
 
     public void explicitSetName(final String name) {
         this.name = name;
         this.setParameterToConfigured(NAME_NAME);
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(final String description) {
         this.description = description;
     }
 
     public void explicitSetDescription(final String description) {
         this.description = description;
         this.setParameterToConfigured(DESCRIPTION_NAME);
     }
 
     public RESTFilterTagCollectionV1 getFilterTags_OTM() {
         return filterTags_OTM;
     }
 
     public void setFilterTags_OTM(final RESTFilterTagCollectionV1 filterTags) {
         this.filterTags_OTM = filterTags;
     }
 
     public void explicitSetFilterTags_OTM(final RESTFilterTagCollectionV1 filterTags) {
         this.filterTags_OTM = filterTags;
         this.setParameterToConfigured(FILTER_TAGS_NAME);
     }
 
     public RESTFilterLocaleCollectionV1 getFilterLocales_OTM() {
         return filterLocales_OTM;
     }
 
     public void setFilterLocales_OTM(final RESTFilterLocaleCollectionV1 filterLocales) {
         this.filterLocales_OTM = filterLocales;
     }
 
     public void explicitSetFilterLocales_OTM(final RESTFilterLocaleCollectionV1 filterLocales) {
         this.filterLocales_OTM = filterLocales;
         this.setParameterToConfigured(FILTER_LOCALES_NAME);
     }
 
     public RESTFilterCategoryCollectionV1 getFilterCategories_OTM() {
         return filterCategories_OTM;
     }
 
     public void setFilterCategories_OTM(final RESTFilterCategoryCollectionV1 filterCategories) {
         this.filterCategories_OTM = filterCategories;
     }
 
     public void explicitSetFilterCategories_OTM(final RESTFilterCategoryCollectionV1 filterCategories) {
         this.filterCategories_OTM = filterCategories;
         this.setParameterToConfigured(FILTER_CATEGORIES_NAME);
     }
 
     public RESTFilterFieldCollectionV1 getFilterFields_OTM() {
         return filterFields_OTM;
     }
 
     public void setFilterFields_OTM(final RESTFilterFieldCollectionV1 filterFields) {
         this.filterFields_OTM = filterFields;
     }
 
     public void explicitSetFilterFields_OTM(final RESTFilterFieldCollectionV1 filterFields) {
         this.filterFields_OTM = filterFields;
         this.setParameterToConfigured(FILTER_FIELDS_NAME);
     }
 
     @Override
     public boolean equals(final Object other) {
         if (other == null) return false;
         if (this == other) return true;
         if (!(other instanceof RESTFilterV1)) return false;
 
         return super.equals(other);
     }
 }
