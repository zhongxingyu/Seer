 package org.jboss.pressgang.ccms.filter;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jboss.pressgang.ccms.filter.structures.FilterFieldStringData;
 import org.jboss.pressgang.ccms.utils.constants.CommonFilterConstants;
 
 /**
  * This class represents the options used by the objects that extend the
  * ExtendedTopicList class to filter a query to retrieve Topic org.jboss.pressgang.ccms.contentspec.entities.
  */
 public class TranslatedTopicFieldFilter extends TopicFieldFilter {
     /**
      * A map of the base filter field names that can not have multiple
      * mappings
      */
     private static final Map<String, String> singleFilterNames = Collections.unmodifiableMap(new HashMap<String, String>() {
         private static final long serialVersionUID = -6343139695468503659L;
 
         {
             putAll(TopicFieldFilter.singleFilterNames);
             put(CommonFilterConstants.ZANATA_IDS_FILTER_VAR, CommonFilterConstants.ZANATA_IDS_FILTER_VAR_DESC);
             put(CommonFilterConstants.ZANATA_IDS_NOT_FILTER_VAR, CommonFilterConstants.ZANATA_IDS_NOT_FILTER_VAR_DESC);
         }
     });
 
     private FilterFieldStringData zanataIds;
     private FilterFieldStringData notZanataIds;
 
     public TranslatedTopicFieldFilter() {
         resetAllValues();
     }
 
     @Override
     protected void resetAllValues() {
         super.resetAllValues();
 
         /* Zanata ID's */
         zanataIds = new FilterFieldStringData(CommonFilterConstants.ZANATA_IDS_FILTER_VAR,
                 CommonFilterConstants.ZANATA_IDS_FILTER_VAR_DESC);
         notZanataIds = new FilterFieldStringData(CommonFilterConstants.ZANATA_IDS_NOT_FILTER_VAR,
                 CommonFilterConstants.ZANATA_IDS_NOT_FILTER_VAR_DESC);
 
         setupSingleFilterVars();
     }
 
    @Override
    protected void setupSingleFilterVars() {
         addFilterVar(zanataIds);
         addFilterVar(notZanataIds);
     }
 
     /**
      * @return A map of the expanded filter field names (i.e. with regular
      *         expressions) mapped to their descriptions
      */
     @Override
     public Map<String, String> getFieldNames() {
         final Map<String, String> retValue = super.getFieldNames();
         retValue.putAll(singleFilterNames);
         return retValue;
     }
 
     /**
      * @return A map of the base filter field names (i.e. with no regular
      *         expressions) mapped to their descriptions
      */
     @Override
     public Map<String, String> getBaseFieldNames() {
         final Map<String, String> retValue = super.getBaseFieldNames();
         retValue.putAll(singleFilterNames);
         return retValue;
     }
 
     public FilterFieldStringData getZanataIds() {
         return zanataIds;
     }
 
     public FilterFieldStringData getNotZanataIds() {
         return notZanataIds;
     }
 }
