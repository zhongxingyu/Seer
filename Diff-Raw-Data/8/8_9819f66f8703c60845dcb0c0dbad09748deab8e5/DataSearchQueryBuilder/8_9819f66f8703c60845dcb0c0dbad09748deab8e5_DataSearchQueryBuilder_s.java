 package org.iplantc.core.uicommons.client.services.impl;
 
 import java.util.Date;
 
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.models.search.DiskResourceQueryTemplate;
 import org.iplantc.core.uicommons.client.widgets.search.SearchFieldDecorator;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 
 /**
  * This class uses a builder pattern to construct a search query from a given query template.
  * 
  * If a field in the given query template is null or empty, the corresponding search term will be omitted
  * from the final query.
  * 
  * @author jstroot
  * 
  */
 @SuppressWarnings("nls")
 public class DataSearchQueryBuilder {
 
     private final DiskResourceQueryTemplate dsf;
     private final Splittable queryList;
 
     public DataSearchQueryBuilder(DiskResourceQueryTemplate dsf) {
         this.dsf = dsf;
         queryList = StringQuoter.createIndexed();
     }
 
     public String buildFullQuery() {
         ownedBy().createdWithin().file().fileSizeRange().metadataAttribute().metadataValue().modifiedWithin().negatedFile().sharedWith();
         return toString();
     }
 
     /**
      * {"nested":{"path":"userPermissions",
      *            "query":{"bool":{"must":[{"term":{"permission":"own"}},
      *                                     {"wildcard":{"user":(some query)}}]}}}}
      * 
      * @return
      */
     public DataSearchQueryBuilder ownedBy() {
         String queryContent = dsf.getOwnedBy();
         if (!Strings.isNullOrEmpty(queryContent)) {
             appendArrayItem(queryList, createOwnerQuery(queryContent));
         }
         return this;
     }
 
     /**
      * {"range": {"dateModified": {"gte":(some query),"lte":(some query)}}}
      * 
      * @return
      */
     public DataSearchQueryBuilder createdWithin() {
         if ((dsf.getCreatedWithin() != null)) {
             Date dateFrom = dsf.getCreatedWithin().getFrom();
             Date dateTo = dsf.getCreatedWithin().getTo();
             if ((dateFrom != null) && (dateTo != null)) {
                 // {"range": {"dateCreated": {"gte":"1380559151","lte":"1390511909"}}}
                 Splittable range = createRangeQuery("dateCreated", dateFrom.getTime() / 1000,
                         dateTo.getTime() / 1000);
                 appendArrayItem(queryList, range);
             } else if (dateFrom != null) {
                 // {"range": {"dateModified": {"gte":"1380559151"}}}
                 Splittable range = createMinRangeQuery("dateCreated", dateFrom.getTime() / 1000);
                 appendArrayItem(queryList, range);
             } else if (dateTo != null) {
                 // {"range": {"dateModified": {"lte":"1390511909"}}}
                 Splittable range = createMaxRangeQuery("dateCreated", dateTo.getTime() / 1000);
                 appendArrayItem(queryList, range);
             }
         }
         return this;
     }
 
     /**
      * {"wildcard":{"label":(some query)}}
      * 
      * @return
      */
     public DataSearchQueryBuilder file() {
         String content = dsf.getFileQuery();
         if (!Strings.isNullOrEmpty(content)) {
             // {"wildcard": {"label": "*txt*"}}
             content = SearchFieldDecorator.applyImplicitAsteriskSearchText(content);
             appendArrayItem(queryList, createWildcard("label", content));
         }
         return this;
     }
 
     /**
      * {"range": {"fileSize": {"gte":(some query),"lte":(some query)}}}
      * 
      * @return
      */
     public DataSearchQueryBuilder fileSizeRange() {
         if ((dsf.getFileSizeRange() != null)) {
             Double minSize = dsf.getFileSizeRange().getMin();
             Double maxSize = dsf.getFileSizeRange().getMax();
 
             if ((minSize != null) && (maxSize != null)) {
                 // {"range": {"fileSize": {"gte":"1000","lte":"100000"}}}
                 appendArrayItem(queryList,
                         createRangeQuery("fileSize", minSize.longValue(), maxSize.longValue()));
             } else if (minSize != null) {
                 // {"range": {"fileSize": {"gte":"1000"}}}
                 Splittable range = createMinRangeQuery("fileSize", minSize.longValue());
                 appendArrayItem(queryList, range);
             } else if (maxSize != null) {
                 // {"range": {"fileSize": {"lte":"100000"}}}
                 Splittable range = createMaxRangeQuery("fileSize", maxSize.longValue());
                 appendArrayItem(queryList, range);
             }
         }
         return this;
     }
 
     /**
      * @return the currently constructed query.
      */
     public String getQuery() {
         return toString();
     }
 
     /**
      * @return
      */
     public DataSearchQueryBuilder metadataAttribute() {
         String content = dsf.getMetadataAttributeQuery();
         if (!Strings.isNullOrEmpty(content)) {
             content = SearchFieldDecorator.applyImplicitAsteriskSearchText(content);
 
             // {"nested":{"path":"metadata","query":{"wildcard":{"attribute":"*content*"}}}}
             Splittable metadata = StringQuoter.createSplittable();
 
             Splittable nested = addChild(metadata, "nested");
             StringQuoter.create("metadata").assign(nested, "path");
 
             createWildcard("attribute", content).assign(nested, "query");
 
             appendArrayItem(queryList, metadata);
         }
         return this;
     }
 
     public DataSearchQueryBuilder metadataValue() {
         String content = dsf.getMetadataValueQuery();
         if (!Strings.isNullOrEmpty(content)) {
             content = SearchFieldDecorator.applyImplicitAsteriskSearchText(content);
 
             // {"nested":{"path":"metadata","query":{"wildcard":{"value":"*content*"}}}}
             Splittable metadata = StringQuoter.createSplittable();
 
             Splittable nested = addChild(metadata, "nested");
             StringQuoter.create("metadata").assign(nested, "path");
 
             createWildcard("value", content).assign(nested, "query");
 
             appendArrayItem(queryList, metadata);
         }
         return this;
     }
 
     /**
      * {"range": {"dateModified": {"gte":(some query),"lte":(some query)}}}
      * 
      * @return
      */
     public DataSearchQueryBuilder modifiedWithin() {
         if ((dsf.getModifiedWithin() != null)) {
             Date dateFrom = dsf.getModifiedWithin().getFrom();
             Date dateTo = dsf.getModifiedWithin().getTo();
 
             if ((dateFrom != null) && (dateTo != null)) {
                 // {"range": {"dateModified": {"gte":"1380559151","lte":"1390511909"}}}
                 Splittable range = createRangeQuery("dateModified", dateFrom.getTime() / 1000,
                         dateTo.getTime() / 1000);
                 appendArrayItem(queryList, range);
             } else if (dateFrom != null) {
                 // {"range": {"dateModified": {"gte":"1380559151"}}}
                 Splittable range = createMinRangeQuery("dateModified", dateFrom.getTime() / 1000);
                 appendArrayItem(queryList, range);
             } else if (dateTo != null) {
                 // {"range": {"dateModified": {"lte":"1390511909"}}}
                 Splittable range = createMaxRangeQuery("dateModified", dateTo.getTime() / 1000);
                 appendArrayItem(queryList, range);
             }
         }
         return this;
     }
 
     /**
      * {"field":{"label":(-some -query -fldjf)}}
      * 
      * @return
      */
     public DataSearchQueryBuilder negatedFile() {
         String content = dsf.getNegatedFileQuery();
         if (!Strings.isNullOrEmpty(content)) {
             content = SearchFieldDecorator.applyImplicitAsteriskSearchText(content);
 
             // Split the query and reassemble with a "-" slapped onto the front.
             Iterable<String> split = Splitter.on(" ").split(content);
             content = "-" + Joiner.on(" -").join(split);
 
             // {"field": {"label": "-*txt*"}}
             appendArrayItem(queryList, createField("label", content));
         }
         return this;
     }
 
     public DataSearchQueryBuilder sharedWith() {
         String content = applyImplicitUsernameWildcard(dsf.getSharedWith());
         if (!Strings.isNullOrEmpty(content)) {
             // {"bool":{"must":[{"nested":{"path":"userPermissions","query":{"bool":{"must":[{"term":{"permission":"own"}},{"wildcard":{"user":"currentUser#*"}}]}}}},{"nested":{"path":"userPermissions","query":{"bool":{"must":[{"wildcard":{"user":queryContent}}]}}}}]}}
             Splittable query = StringQuoter.createSplittable();
             Splittable bool = addChild(query, "bool");
             Splittable must = addArray(bool, "must");
 
             appendArrayItem(must, createOwnerQuery(UserInfo.getInstance().getUsername()));
 
             Splittable sharedWith = StringQuoter.createSplittable();
 
             Splittable nested = addChild(sharedWith, "nested");
             StringQuoter.create("userPermissions").assign(nested, "path");
 
             createWildcard("user", content).assign(nested, "query");
 
             appendArrayItem(must, sharedWith);
 
             appendArrayItem(queryList, query);
         }
         return this;
     }
 
     /**
      * @return the currently constructed query.
      */
     @Override
     public String toString() {
         // {"bool":{"must":[queryList]}}
         Splittable query = StringQuoter.createSplittable();
         Splittable bool = addChild(query, "bool");
         queryList.assign(bool, "must");
         return query.getPayload();
     }
 
     private Splittable createWildcard(String field, String content) {
         // {"wildcard": {field: content}}
		// Use lowercase values since wildcard queries are not analyzed by the
		// service and values are indexed as lowercase.
		content = Strings.isNullOrEmpty(content) ? null : content.toLowerCase();
		return createQuery("wildcard", field, content);
     }
 
     private Splittable createField(String field, String content) {
         // {"field": {field: content}}
         return createQuery("field", field, content);
     }
 
     private Splittable createQuery(String queryType, String field, String content) {
         // {queryType: {field: content}}
         Splittable query = StringQuoter.createSplittable();
 
         Splittable wildcard = addChild(query, queryType);
         StringQuoter.create(content).assign(wildcard, field);
 
         return query;
     }
 
     private Splittable createRangeQuery(String field, long lowerLimit, long upperLimit) {
         // {"range": {field: {"gte": lowerLimit,"lte": upperLimit}}}
         Splittable query = StringQuoter.createSplittable();
 
         Splittable range = addChild(addChild(query, "range"), field);
 
         StringQuoter.create(String.valueOf(lowerLimit)).assign(range, "gte");
         StringQuoter.create(String.valueOf(upperLimit)).assign(range, "lte");
 
         return query;
     }
 
     private Splittable createMinRangeQuery(String field, long lowerLimit) {
         // {"range": {field: {"gte": lowerLimit}}}
         Splittable query = StringQuoter.createSplittable();
 
         Splittable range = addChild(addChild(query, "range"), field);
 
         StringQuoter.create(String.valueOf(lowerLimit)).assign(range, "gte");
 
         return query;
     }
 
     private Splittable createMaxRangeQuery(String field, long upperLimit) {
         // {"range": {field: {"lte": upperLimit}}}
         Splittable query = StringQuoter.createSplittable();
 
         Splittable range = addChild(addChild(query, "range"), field);
 
         StringQuoter.create(String.valueOf(upperLimit)).assign(range, "lte");
 
         return query;
     }
 
     private Splittable createOwnerQuery(String user) {
         // {"nested":{"path":"userPermissions","query":{"bool":{"must":[{"term":{"permission":"own"}},{"wildcard":{"user":queryContent}}]}}}}
         Splittable ownedBy = StringQuoter.createSplittable();
 
         Splittable nested = addChild(ownedBy, "nested");
         StringQuoter.create("userPermissions").assign(nested, "path");
 
         Splittable query = addChild(nested, "query");
         Splittable bool = addChild(query, "bool");
         Splittable must = addArray(bool, "must");
 
         appendArrayItem(must, createQuery("term", "permission", "own"));
         appendArrayItem(must, createWildcard("user", applyImplicitUsernameWildcard(user)));
 
         return ownedBy;
     }
 
     private String applyImplicitUsernameWildcard(String user) {
         // usernames are formatted as user#zone
         if (!Strings.isNullOrEmpty(user) && !user.endsWith("*") && !user.contains("#")) {
             user += "#*";
         }
 
         return user;
     }
 
     private Splittable addChild(Splittable parent, String key) {
         Splittable child = StringQuoter.createSplittable();
         child.assign(parent, key);
         return child;
     }
 
     private Splittable addArray(Splittable parent, String key) {
         Splittable child = StringQuoter.createIndexed();
         child.assign(parent, key);
         return child;
     }
 
     private void appendArrayItem(Splittable array, Splittable item) {
         item.assign(array, array.size());
     }
 }
