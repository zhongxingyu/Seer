 package org.monitoring.queryapi;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MapReduceCommand;
 import com.mongodb.MapReduceOutput;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import java.util.TimeZone;
 
 /**
  * Class for composing complex queries.
  *
  * @author Michal Dubravcik
  */
 public class Query {
 
     private DBCollection col;
     private BasicDBObjectBuilder query = new BasicDBObjectBuilder();
     private BasicDBObjectBuilder sort = new BasicDBObjectBuilder();
     private int limit = 0;
     private Date start = null, end = null;
     private int step = 1000;
     private CachePointMapper dbmapper = new CachePointMapper();
     public static final String ID_DATA = "data";
     public static final String ID_TIME = "time";
     public static final String ID_FLAG = CachePointMapper.CACHE_FLAG;
     private final String CACHE = Manager.CACHE;
     private final String CACHE_FLAGS = Manager.CACHE_FLAGS;
 
     public Query(DBCollection col) {
         this.col = col;
     }
 
     /**
      * Append pair {field:value} into matching DBObject
      *
      * @param field
      * @param value
      */
     public void append(String field, Object value) {
         query.add(field, value);
     }
 
     /**
      * Access field operations on specified field
      *
      * @see Field
      * @param field
      * @return Field
      */
     public Field field(String field) {
         return new Field((Query) this, ID_DATA + "." + field);
     }
 
     /**
      * Sort documents on output by field ascending
      *
      * @param field
      * @return Query for chaining
      */
     public Query orderAsc(String field) {
         sort.append(field, 1);
         return this;
     }
 
     /**
      * Sort documents on output by field descending
      *
      * @param field
      * @return Query for chaining
      */
     public Query orderDesc(String field) {
         sort.append(field, -1);
         return this;
     }
 
     /**
      * Restricts the number of output documents
      *
      * @param num
      * @return Query for chaining
      */
     public Query limit(int num) {
         limit = num;
         return this;
     }
 
     /**
      * Get matching DBObject set by append method, Field operations and date boundaries
      *
      * @return
      */
     private DBObject getMatchQuery() {
         BasicDBObjectBuilder queryLocal = new BasicDBObjectBuilder();
         if (start != null) {
             queryLocal.push(ID_TIME).append(Field.GTE, start);
         }
         if (end != null) {
             if (queryLocal.isEmpty()) {
                 queryLocal.push(ID_TIME).append(Field.LTE, end);
             } else {
                 queryLocal.append(Field.LTE, end);
             }
         }
         DBObject out = query.get();
         out.putAll(queryLocal.get());
         return out;
     }
 
     /**
      * Get matching DBObject set by append method, Field operations and append specified explicit
      * date boundaries - omit date set by dateFrom(),dateTo()
      *
      * @param qStart start boundary of explicit time interval
      * @param qEnd end boundary
      * @return
      */
     private DBObject getMatchQueryWithSubTime(Date qStart, Date qEnd) {
         BasicDBObjectBuilder queryLocal = new BasicDBObjectBuilder();
         if (qStart != null) {
             queryLocal.push(ID_TIME).append(Field.GTE, qStart);
         }
         if (qEnd != null) {
             if (queryLocal.isEmpty()) {
                 queryLocal.push(ID_TIME).append(Field.LT, qEnd);
             } else {
                 queryLocal.append(Field.LT, qEnd);
             }
         }
         DBObject out = query.get();
         out.putAll(queryLocal.get());
         return out;
     }
 
     /**
      * Set matching starting date (left time boundary)
      *
      * @param date
      * @return Query for chaining
      */
     public Query fromDate(Date date) {
         start = date;
         return this;
     }
 
     /**
      * Set matching ending date (right time boundary)
      *
      * @param date
      * @return Query for chaining
      */
     public Query toDate(Date date) {
         end = date;
         return this;
     }
 
     /**
      * Set length of grouping time interval
      *
      * @param step length of interval in milliseconds
      * @return Query for chaining
      */
     public Query setStep(int step) {
         this.step = step;
         return this;
     }
 
     /**
      * Get documents from DB taking into account match, date boundary, limit and order
      *
      * @return documents in DBObject
      */
     public DBObject find() {
         return wrap("result", col.find(getMatchQuery()).sort(sort.get()).limit(limit));
     }
 
     /**
      * Get all variants saved in given field value
      *
      * @param field
      * @return Query for chaining
      */
     public DBObject distinct(String field) {
         return wrap("result", col.distinct(ID_DATA + "." + field, getMatchQuery()));
     }
 
     /**
      * Get overall number of matched documents taking into account match and date boundaries
      *
      * @return count
      */
     public int countAll() {
         return col.find(getMatchQuery()).count();
     }
 
     /**
      * Get number of documents in interval specified by step taking into account match, date
      * boundaries and limit
      *
      * @return DBObject with time and counts in array on key result
      */
     public DBObject count() {
         String map = "count_map(this)";
         String reduce = "function(id,values){ return count_reduce(id, values);}";
         Map<String, Object> scope = getScope(step);
         return wrap("result", mapReduce(map, reduce, scope));
     }
 
     /**
      * Compute average from values on specified field in interval specified by step taking into account
      * match, date boundaries and limit
      *
      * @param field
      * @return DBObject with times and avgs in array on key result
      */
     public DBObject avg(String field) {
         String map = "map(this)";
         String reduce = "function(id, values){ return avg_reduce(id, values);}";
         Map<String, Object> scope = getScope(field, step);
         return wrap("result", mapReduce(map, reduce, scope));
     }
 
     /**
      * Compute sum of values on specified field in interval specified by step taking into account
      * match, date boundaries and limit
      *
      * @param field
      * @return DBObject with times and sums in array on key result
      */
     public DBObject sum(String field) {
         String map = "map(this)";
         String reduce = "function(id,values){ return sum_reduce(id,values);}";
         Map<String, Object> scope = getScope(field, step);
         return wrap("result", mapReduce(map, reduce, scope));
     }
 
     /**
      * Find minimal value from values of specified field in interval specified by step taking into account
      * match, date boundaries and limit
      *
      * @param field
      * @return DBObject with times and mins in array on key result
      */
     public DBObject min(String field) {
         String map = "map(this)";
         String reduce = "function(id,values){ return min_reduce(id, values);}";
         Map<String, Object> scope = getScope(field, step);
         return wrap("result", mapReduce(map, reduce, scope));
     }
 
     /**
      * Find maximal value from values on specified field in interval specified by step taking into account
      * match, date boundaries and limit
      *
      * @param field
      * @return DBObject with times and maxs in array on key result
      */
     public DBObject max(String field) {
         String map = "map(this)";
         String reduce = "function(id,values){ return max_reduce(id, values);}";
         Map<String, Object> scope = getScope(field, step);
         return wrap("result", mapReduce(map, reduce, scope));
     }
 
     /**
      * Compute median on specified field in interval specified by step taking into account match,
      * date boundary and limit
      *
      * @param field
      * @return DBObject with times and medians in array on key result
      */
     public DBObject median(String field) {
         String map = "map(this)";
         String reduce = "function(id,values){ return median_reduce(id, values);}";
         /* bind global variables for map-reduce on serve-side */
         Map<String, Object> scope = getScope(field, step);
         return wrap("result", mapReduce(map, reduce, scope));
     }
 
     public DBObject countCached() {
         String map = "count_map_cached(this)";
         String reduce = "function(id,values){ return count_reduce(id, values);}";
         String finalize = "";
         /* create cache identifier */
         String field = ""; //count does not need field
         CacheMatcher cm = new CacheMatcher("count", field, query.get().toString(), step);
         /* bind global variables for map-reduce on serve-side */
         Map<String, Object> scope = getScope(field, step, cm.getMD5());
         return cache(cm, map, reduce, finalize, scope);
     }
 
     public DBObject avgCached(String field) {
         /* map, reduce, finalize JS functions (preferably stored in Mongo system.js) */
         String map = "map_cached(this)";
         String reduce = "function(id,values){ return avg_reduce(id, values);}";
         String finalize = "";
         /* create cache identifier */
         CacheMatcher cm = new CacheMatcher("avg", field, query.get().toString(), step);
         /* bind global variables for map-reduce on serve-side */
         Map<String, Object> scope = getScope(field, step, cm.getMD5());
         return cache(cm, map, reduce, finalize, scope);
     }
 
     public DBObject sumCached(String field) {
         /* map, reduce, finalize JS functions (preferably stored in Mongo system.js) */
         String map = "map_cached(this)";
         String reduce = "function(id,values){ return sum_reduce(id, values);}";
         String finalize = "";
         /* create cache identifier */
         CacheMatcher cm = new CacheMatcher("sum", field, query.get().toString(), step);
         /* bind global variables for map-reduce on serve-side */
         Map<String, Object> scope = getScope(field, step, cm.getMD5());
         return cache(cm, map, reduce, finalize, scope);
     }
 
     public DBObject minCached(String field) {
         /* map, reduce, finalize JS functions (preferably stored in Mongo system.js) */
         String map = "map_cached(this)";
         String reduce = "function(id,values){ return min_reduce(id, values);}";
         String finalize = "";
         /* create cache identifier */
         CacheMatcher cm = new CacheMatcher("min", field, query.get().toString(), step);
         /* bind global variables for map-reduce on serve-side */
         Map<String, Object> scope = getScope(field, step, cm.getMD5());
         return cache(cm, map, reduce, finalize, scope);
     }
 
     public DBObject maxCached(String field) {
         /* map, reduce, finalize JS functions (preferably stored in Mongo system.js) */
         String map = "map_cached(this)";
         String reduce = "function(id,values){ return max_reduce(id, values);}";
         String finalize = "";
         /* create cache identifier */
         CacheMatcher cm = new CacheMatcher("max", field, query.get().toString(), step);
         /* bind global variables for map-reduce on serve-side */
         Map<String, Object> scope = getScope(field, step, cm.getMD5());
         return cache(cm, map, reduce, finalize, scope);
     }
 
     public DBObject medianCached(String field) {
         /* map, reduce, finalize JS functions (preferably stored in Mongo system.js) */
         String map = "map_cached(this)";
         String reduce = "function(id,values){ return median_reduce(id, values);}";
         String finalize = "";
         /* create cache identifier */
         CacheMatcher cm = new CacheMatcher("median", field, query.get().toString(), step);
         /* bind global variables for map-reduce on serve-side */
         Map<String, Object> scope = getScope(field, step, cm.getMD5());
         return cache(cm, map, reduce, finalize, scope);
     }
 
     /**
      * Find most occured documents having time before time of documents searchable by matching with
      * effect document (designed for finding reasons of effects)
      *
      * @param effect document identifing many documents in db
      * @param limit restrict returned number of documents
      * @param timeBefore time before effects (in milliseconds)
      * @param groupBy fields for grouping documents
      * @return Query for chaining
      */
     public DBObject reasonFor(DBObject effect, int limit, int timeBefore, String... groupBy) {
         BasicDBList dates = new BasicDBList();
         BasicDBObjectBuilder builder = new BasicDBObjectBuilder();
         Iterable<DBObject> reasons;
         int num = 0;
 
         Iterable<DBObject> results = col.find(effect);
 
         for (DBObject result : results) {
             dates.add(
                     BasicDBObjectBuilder.start()
                     .push(ID_TIME)
                     .append(Field.LTE, (Date) result.get(ID_TIME))
                     .append(Field.GTE, new Date(((Date) result.get(ID_TIME)).getTime() - timeBefore))
                     .get());
             num++;
         }
         if (num > 0) {
             DBObject match = BasicDBObjectBuilder.start()
                     .append(Aggregation.MATCH, new BasicDBObject(Aggregation.OR, dates)).get();
 
             for (String groupKey : groupBy) {
                builder.append(groupKey, "$" + groupKey);
             }
 
             DBObject groupInner = builder.get();
 
             DBObject group = BasicDBObjectBuilder.start()
                     .push(Aggregation.GROUP).append("_id", groupInner)
                     .push("count").append(Aggregation.SUM, 1)
                     .get();
             DBObject order = BasicDBObjectBuilder.start()
                     .push(Aggregation.SORT).append("count", -1)
                     .get();
             DBObject project = BasicDBObjectBuilder.start()
                     .push(Aggregation.PROJECT)
                     .append("_id", 0).append("count", 1)
                     .append("group", "$_id")
                     .get();
             DBObject limiter = new BasicDBObject(Aggregation.LIMIT, limit);
 
             reasons = col.aggregate(match, group, order, limiter, project)
                     .results();
         } else {
             reasons = new ArrayList<DBObject>();
         }
         return wrap("founded effects", num, "result", reasons);
     }
 
     /**
      * Perform Map Reduce command taking into account order and limit
      *
      * @param map map JS function
      * @param reduce reduce JS function
      * @param finalize finalize JS function
      * @param output name of output collection
      * @see MapReduceCommand.OutputType
      * @param type type of job on output
      * @param scope global JS variables
      * @param qStart
      * @param qEnd
      */
     private Iterable<DBObject> mapReduce(String map, String reduce, String finalize, String output,
             MapReduceCommand.OutputType type, Map<String, Object> scope, Date qStart, Date qEnd) {
 
         MapReduceCommand mapReduceCmd;
 
         if (qEnd == null && qStart == null) {
             mapReduceCmd =
                     new MapReduceCommand(col, map, reduce, output, type, getMatchQuery());
         } else {
             mapReduceCmd =
                     new MapReduceCommand(col, map, reduce, output, type, getMatchQueryWithSubTime(qStart, qEnd));
         }
 
         if (!finalize.isEmpty()) {
             mapReduceCmd.setFinalize(finalize);
         }
         if (!sort.isEmpty()) {
             mapReduceCmd.setSort(sort.get());
         } else {
             mapReduceCmd.setSort(new BasicDBObject("_id", 1));
         }
 
         if (limit != 0) {
             mapReduceCmd.setLimit(limit);
         }
 
         if (scope != null) {
             mapReduceCmd.setScope(scope);
         }
 
         MapReduceOutput out = col.mapReduce(mapReduceCmd);
 
         //System.out.println(out.getCommandResult());
         //System.out.println(out.getCommand());
 
         return out.results();
     }
 
     private Iterable<DBObject> mapReduce(String map, String reduce, String finalize, String output, MapReduceCommand.OutputType type, Map<String, Object> scope) {
         return mapReduce(map, reduce, finalize, output, type, scope, null, null);
     }
 
     private Iterable<DBObject> mapReduce(String map, String reduce, String finalize, String output, MapReduceCommand.OutputType type, Date qStart, Date qEend) {
         return mapReduce(map, reduce, finalize, output, type, null, qStart, qEend);
     }
 
     private Iterable<DBObject> mapReduce(String map, String reduce, String finalize) {
         return mapReduce(map, reduce, finalize, "", MapReduceCommand.OutputType.INLINE, null);
     }
 
     private Iterable<DBObject> mapReduce(String map, String reduce) {
         return mapReduce(map, reduce, "", "", MapReduceCommand.OutputType.INLINE, null);
     }
 
     private Iterable<DBObject> mapReduce(String map, String reduce, Map<String, Object> scope) {
         return mapReduce(map, reduce, "", "", MapReduceCommand.OutputType.INLINE, scope);
     }
 
     /**
      * Wrap 2 pairs into new DBObject
      */
     private DBObject wrap(String firstKey, Object firstValue, String secondKey, Object secondValue) {
         return BasicDBObjectBuilder.start().append(firstKey, firstValue).append(secondKey, secondValue).get();
     }
 
     /**
      * Wrap pair into new DBObject
      */
     private DBObject wrap(String firstKey, Object firstValue) {
         return new BasicDBObject(firstKey, firstValue);
     }
 
     /**
      * Perform Map-Reduce with parameters and cache the result. Computed results are saved in
      * special collection. In next cache call with same identifier and date boundaries
      * (dateFrom(),dateTo()), result is returned from cache. In cache call with same identifier and
      * date boundaries intersected with cache date boundaries, only in not computed date intervals
      * is map reduce performed. At the end it returns result from overall interval.
      *
      * @param cm cache identifier
      * @param map JS map function
      * @param reduce JS reduce function
      * @param finalize JS finalize function
      * @param scope global variables for mr server-side
      * @return map reduce result
      */
     private DBObject cache(CacheMatcher cm, String map, String reduce, String finalize, Map<String, Object> scope) {
         if(start == null){
             Calendar cal = new GregorianCalendar(1970, 0, 0);
             start = cal.getTime();
         }
         if(end == null){
             Calendar cal = new GregorianCalendar(2050, 0, 0);
             end = cal.getTime();
         }
         final CachePoint CACHE_POINT_START = new CachePoint(start, cm.getOperation(), cm.getField(), query.get().toString(), CachePoint.Flag.START, step);
         final CachePoint CACHE_POINT_END = new CachePoint(end, cm.getOperation(), cm.getField(), query.get().toString(), CachePoint.Flag.END, step);
 
         DBCollection cacheFlags = col.getDB().getCollection(CACHE_FLAGS);
         DBCollection cache = col.getDB().getCollection(CACHE);
 
         BasicDBList or = new BasicDBList();
         or.add(BasicDBObjectBuilder.start().push("_id." + CachePoint.ID_TIME)
                 .append(Field.NE, start).get());
         or.add(BasicDBObjectBuilder.start().push(ID_FLAG)
                 .append(Field.NE, CachePoint.Flag.START.get()).get());
 
         DBObject match = BasicDBObjectBuilder.start()
                 .push("_id.time").append(Field.GTE, start)
                 .append(Field.LT, end).pop()
                 .append("_id.match", cm.getMD5())
                 .append("_id.step", step)
                 .append("$or", or)
                 .get();
 
         DBCursor cursor = cacheFlags.find(match);
 
         if (cursor.hasNext()) { //not empty response -> partially cached
             while (cursor.hasNext()) {
                 CachePoint point1, point2;
                 point1 = dbmapper.fromDB(cursor.next());
                 if (point1.getFlag() == CachePoint.Flag.START
                         && point1.getDate() == start) {
                 } else if (point1.getFlag() == CachePoint.Flag.START) {
                     mapReduce(map, reduce, finalize, CACHE,
                             MapReduceCommand.OutputType.MERGE, scope, start, point1.getDate());
                 } else if (cursor.hasNext()) {
                     point2 = dbmapper.fromDB(cursor.next());
                     mapReduce(map, reduce, finalize, CACHE, MapReduceCommand.OutputType.MERGE, scope, point1.getDate(), point2.getDate());
                 } else {
                     mapReduce(map, reduce, finalize, CACHE, MapReduceCommand.OutputType.MERGE, scope, point1.getDate(), end);
                 }
             }
 
             CachePoint.Flag beforeStart = getInclusiveBeforePoint(start);
             if (beforeStart == CachePoint.Flag.START) {
                 //do nothing with start
             } else if (beforeStart == CachePoint.Flag.END) {
                 //remove old end
                 cacheFlags.remove(new BasicDBObject("_id." + CachePoint.ID_TIME, start));
             } else {
                 //insert start
                 cacheFlags.save(dbmapper.toDB(CACHE_POINT_START));
             }
 
             cacheFlags.remove(BasicDBObjectBuilder.start()
                     .push("_id." + CachePoint.ID_TIME)
                     .append(Field.GT, start)
                     .append(Field.LTE, end).get());
 
             CachePoint.Flag afterEnd = getInclusiveAfterPoint(end);
             if (afterEnd == CachePoint.Flag.END) {
                 //do not add end
             } else {
                 //insert end
                 cacheFlags.save(dbmapper.toDB(CACHE_POINT_END));
             }
 
         } else { //empty response -> all cached or nothing cached
             if (isStartInclusiveBeforePoint(start)) {
                 //all cached, ready for query from cache"
             } else if (getAtPoint(end) == CachePoint.Flag.START) {
                 cacheFlags.remove(new BasicDBObject("_id." + CachePoint.ID_TIME, end));
                 cacheFlags.save(dbmapper.toDB(CACHE_POINT_START));
                 //nothing cached, remove end
                 mapReduce(map, reduce, finalize, CACHE, MapReduceCommand.OutputType.MERGE, scope, start, end);
             } else {
                 //nothing cached, need to recompute
                 mapReduce(map, reduce, finalize, CACHE, MapReduceCommand.OutputType.MERGE, scope, start, end);
                 cacheFlags.save(dbmapper.toDB(CACHE_POINT_START));
                 cacheFlags.save(dbmapper.toDB(CACHE_POINT_END));
             }
         }
         return wrap("result", cache.find(match).toArray());
     }
 
     /**
      * Cache assistant method. Get CachePoint flag having date before (inclusively) date specified
      */
     private CachePoint.Flag getInclusiveBeforePoint(Date date) {
         DBCollection cache = col.getDB().getCollection(CACHE);
         DBObject beforeStartQuery = BasicDBObjectBuilder.start()
                 .push("_id." + CachePoint.ID_TIME)
                 .append(Field.LTE, date)
                 .get();
         DBObject order = new BasicDBObject("_id." + CachePoint.ID_TIME, -1);
         List<DBObject> beforeStartResponseList = cache
                 .find(beforeStartQuery).sort(order).limit(1).toArray();
         if (beforeStartResponseList.isEmpty()) {
             return CachePoint.Flag.NONE;
         } else {
             return dbmapper.fromDB(beforeStartResponseList.get(0)).getFlag();
         }
     }
 
     private boolean isStartInclusiveBeforePoint(Date date) {
         return getInclusiveBeforePoint(date) == CachePoint.Flag.START;
     }
 
     private CachePoint.Flag getInclusiveAfterPoint(Date date) {
         DBCollection cache = col.getDB().getCollection(CACHE);
         DBObject afterEndQuery = BasicDBObjectBuilder.start()
                 .push("_id." + CachePoint.ID_TIME)
                 .append(Field.GTE, date).get();
         DBObject order = new BasicDBObject("_id." + CachePoint.ID_TIME, 1);
         List<DBObject> afterEndResponseList = cache.find(afterEndQuery)
                 .sort(order).limit(1).toArray();
         if (afterEndResponseList.isEmpty()) {
             return CachePoint.Flag.NONE;
         } else {
             return dbmapper.fromDB(afterEndResponseList.get(0)).getFlag();
         }
     }
 
     private boolean isEndInclusiveAfterPoint(Date date) {
         return getInclusiveAfterPoint(date) == CachePoint.Flag.START;
     }
 
     private CachePoint.Flag getAtPoint(Date date) {
         DBCollection cache = col.getDB().getCollection(CACHE);
         DBObject atQuery = BasicDBObjectBuilder.start()
                 .append("_id." + CachePoint.ID_TIME, date)
                 .get();
         List<DBObject> atResponseList = cache.find(atQuery).limit(1).toArray();
         if (atResponseList.isEmpty()) {
             return CachePoint.Flag.NONE;
         } else {
             return dbmapper.fromDB(atResponseList.get(0)).getFlag();
         }
     }
 
     private Map<String, Object> getScope(int step) {
         Map<String, Object> scope = new HashMap<String, Object>();
         scope.put("step", step);
         return scope;
     }
 
     private Map<String, Object> getScope(String field, int step) {
         Map<String, Object> scope = getScope(step);
         scope.put("field", field);
         return scope;
     }
 
     private Map<String, Object> getScope(String field, int step, String hash) {
         Map<String, Object> scope = getScope(field, step);
         scope.put("hash", hash);
         return scope;
     }
 }
