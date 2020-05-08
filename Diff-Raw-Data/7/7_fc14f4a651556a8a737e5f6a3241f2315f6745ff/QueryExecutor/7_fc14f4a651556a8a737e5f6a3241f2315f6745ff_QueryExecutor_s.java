 package com.imaginea.mongodb.utils;
 
 import com.imaginea.mongodb.exceptions.ApplicationException;
 import com.imaginea.mongodb.exceptions.DatabaseException;
 import com.imaginea.mongodb.exceptions.ErrorCodes;
 import com.imaginea.mongodb.exceptions.InvalidMongoCommandException;
 import com.mongodb.*;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.*;
 
 /**
  * User: venkateshr
  */
 public class QueryExecutor {
 
     public static JSONObject executeQuery(DB db, DBCollection dbCollection, String command, String queryStr, String fields, String sortByStr, int limit, int skip, boolean allKeys) throws JSONException, ApplicationException {
         StringTokenizer strtok = new StringTokenizer(fields, ",");
         DBObject keysObj = new BasicDBObject("_id", 1);
         while (strtok.hasMoreElements()) {
             keysObj.put(strtok.nextToken(), 1);
         }
         DBObject sortObj = (DBObject) JSON.parse(sortByStr);
         if (command.equals("aggregate")) {
             return executeAggregate(dbCollection, queryStr);
         }
         if (command.equals("count")) {
             return executeCount(dbCollection, queryStr);
         }
         if (command.equals("distinct")) {
             return executeDistinct(dbCollection, queryStr);
         }
         if (command.equals("drop")) {
             return executeDrop(dbCollection);
         }
         if (command.equals("dropIndex")) {
             return executeDropIndex(dbCollection, queryStr);
         }
         if (command.equals("dropIndexes")) {
             return executeDropIndexes(dbCollection);
         }
         if (command.equals("ensureIndex")) {
             return executeEnsureIndex(dbCollection, queryStr);
         }
         if (command.equals("find")) {
             return executeFind(dbCollection, queryStr, keysObj, sortObj, limit, skip, allKeys);
         }
         if (command.equals("findOne")) {
             return executeFindOne(dbCollection, queryStr);
         }
         if (command.equals("findAndModify")) {
             return executeFindAndModify(dbCollection, queryStr, keysObj);
         }
         if (command.equals("group")) {
             return executeGroup(dbCollection, queryStr);
         }
         if (command.equals("getIndexes")) {
             return executeGetIndexes(dbCollection);
         }
         if (command.equals("insert")) {
             return executeInsert(dbCollection, queryStr);
         }
         if (command.equals("mapReduce")) {
            return executeMapReduce(dbCollection, queryStr, limit);
         }
         if (command.equals("remove")) {
             return executeRemove(dbCollection, queryStr);
         }
         if (command.equals("stats")) {
             return executeStats(dbCollection);
         }
         if (command.equals("storageSize")) {
             return executeStorageSize(dbCollection);
         }
         if (command.equals("totalIndexSize")) {
             return executeTotalIndexSize(dbCollection);
         }
         if (command.equals("update")) {
             return executeUpdate(dbCollection, queryStr);
         }
         throw new InvalidMongoCommandException(ErrorCodes.COMMAND_NOT_SUPPORTED, "Command is not yet supported");
     }
 
     private static JSONObject executeAggregate(DBCollection dbCollection, String queryStr) throws DatabaseException, JSONException {
         DBObject queryObj = (DBObject) JSON.parse("[" + queryStr + "]");
         if (queryObj instanceof List) {
             List<DBObject> listOfAggregates = (List) queryObj;
             int size = listOfAggregates.size();
             AggregationOutput aggregationOutput = dbCollection.aggregate(listOfAggregates.get(0), listOfAggregates.subList(1, size).toArray(new DBObject[size - 1]));
             Iterator<DBObject> resultIterator = aggregationOutput.results().iterator();
             List<DBObject> results = new ArrayList<DBObject>();
             while (resultIterator.hasNext()) {
                 results.add(resultIterator.next());
             }
             return ApplicationUtils.constructResponse(false, results.size(), results);
         }
         throw new DatabaseException(ErrorCodes.INVALID_AGGREGATE_COMMAND, "Aggregate command is ill formed");
     }
 
     private static JSONObject executeCount(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         long count = dbCollection.count(queryObj);
         return ApplicationUtils.constructResponse(false, new BasicDBObject("count", count));
     }
 
     private static JSONObject executeDistinct(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse("[" + queryStr + "]");
         List distinctValuesList = null;
         if (queryObj.get("1") == null) {
             distinctValuesList = dbCollection.distinct((String) queryObj.get("0"));
         } else {
             distinctValuesList = dbCollection.distinct((String) queryObj.get("0"), (DBObject) queryObj.get("1"));
         }
         return ApplicationUtils.constructResponse(false, distinctValuesList.size(), distinctValuesList);
     }
 
     private static JSONObject executeDrop(DBCollection dbCollection) throws JSONException {
         dbCollection.drop();
         JSONObject jsonObject = new JSONObject();
         jsonObject.put("success", true);
         return jsonObject;
     }
 
     private static JSONObject executeDropIndex(DBCollection dbCollection, String queryStr) throws JSONException, DatabaseException {
         DBObject indexInfo = (DBObject) JSON.parse(queryStr);
         if (indexInfo == null) {
             throw new DatabaseException(ErrorCodes.INDEX_EMPTY, "Index is null");
         }
         dbCollection.dropIndex(indexInfo);
         return executeGetIndexes(dbCollection);
     }
 
     private static JSONObject executeDropIndexes(DBCollection dbCollection) throws JSONException {
         dbCollection.dropIndexes();
         return executeGetIndexes(dbCollection);
     }
 
     private static JSONObject executeEnsureIndex(DBCollection dbCollection, String queryStr) throws JSONException, DatabaseException {
         DBObject queryObj = (DBObject) JSON.parse("[" + queryStr + "]");
         DBObject keys = (DBObject) queryObj.get("0");
         if (keys == null) {
             throw new DatabaseException(ErrorCodes.KEYS_EMPTY, "Index Keys are null");
         }
         if (keys.equals("")) {
             throw new DatabaseException(ErrorCodes.KEYS_EMPTY, "Index keys are Empty");
         }
         DBObject options = (DBObject) queryObj.get("1");
         if (options != null) {
             dbCollection.ensureIndex(keys, options);
         } else {
             dbCollection.ensureIndex(keys);
         }
         return executeGetIndexes(dbCollection);
     }
 
     private static JSONObject executeFindOne(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse("[" + queryStr + "]");
         DBObject matchedRecord = dbCollection.findOne((DBObject) queryObj.get("0"), (DBObject) queryObj.get("1"));
         return ApplicationUtils.constructResponse(true, matchedRecord);
     }
 
     private static JSONObject executeFind(DBCollection dbCollection, String queryStr, DBObject keysObj, DBObject sortObj, int limit, int skip, boolean allKeys) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         DBCursor cursor = null;
         if (allKeys) {
             cursor = dbCollection.find(queryObj);
         } else {
             cursor = dbCollection.find(queryObj, keysObj);
         }
         cursor = cursor.sort(sortObj).skip(skip).limit(limit);
         ArrayList<DBObject> dataList = new ArrayList<DBObject>();
         if (cursor.hasNext()) {
             while (cursor.hasNext()) {
                 dataList.add(cursor.next());
             }
         }
         return ApplicationUtils.constructResponse(true, dbCollection.count(queryObj), dataList);
     }
 
     private static JSONObject executeFindAndModify(DBCollection dbCollection, String queryStr, DBObject keysObj) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         DBObject criteria = (DBObject) queryObj.get("query");
         DBObject sort = (DBObject) queryObj.get("sort");
         DBObject update = (DBObject) queryObj.get("update");
         keysObj = queryObj.get("fields") != null ? (DBObject) queryObj.get("") : keysObj;
         boolean returnNew = queryObj.get("new") != null ? (Boolean) queryObj.get("new") : false;
         boolean upsert = queryObj.get("upsert") != null ? (Boolean) queryObj.get("upsert") : false;
         boolean remove = queryObj.get("remove") != null ? (Boolean) queryObj.get("remove") : false;
 
         DBObject queryResult = dbCollection.findAndModify(criteria, keysObj, sort, remove, update, returnNew, upsert);
         return ApplicationUtils.constructResponse(false, queryResult);
     }
 
     private static JSONObject executeGetIndexes(DBCollection dbCollection) throws JSONException {
         List<DBObject> indexInfo = dbCollection.getIndexInfo();
         return ApplicationUtils.constructResponse(false, indexInfo.size(), indexInfo);
     }
 
     private static JSONObject executeInsert(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         WriteResult writeResult;
         if (queryObj instanceof List) {
             writeResult = dbCollection.insert((List<DBObject>) queryObj);
         } else {
             writeResult = dbCollection.insert(queryObj);
         }
         if (writeResult.getLastError().get("err") == null) {
             return ApplicationUtils.constructResponse(false, queryObj);
         }
         return ApplicationUtils.constructResponse(false, writeResult.getLastError());
     }
 
     private static JSONObject executeGroup(DBCollection dbCollection, String queryString) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryString);
 
         DBObject key = (DBObject) queryObj.get("key");
         DBObject cond = (DBObject) queryObj.get("cond");
         String reduce = (String) queryObj.get("reduce");
         DBObject initial = (DBObject) queryObj.get("initial");
         //There is no way to specify this.
         //DBObject keyf = (DBObject) queryObj.get("keyf");
         String finalize = (String) queryObj.get("finalize");
 
         DBObject groupQueryResult = dbCollection.group(key, cond, initial, reduce, finalize);
         return ApplicationUtils.constructResponse(false, groupQueryResult);
     }
 
    private static JSONObject executeMapReduce(DBCollection dbCollection, String queryString, int limit) throws JSONException, InvalidMongoCommandException {
         DBObject queryObj = (DBObject) JSON.parse("[" + queryString + "]");
 
         String map = (String) queryObj.get("0");
         String reduce = (String) queryObj.get("1");
         DBObject params = (DBObject) queryObj.get("2");
         String outputDb = null;
         MapReduceCommand.OutputType outputType = MapReduceCommand.OutputType.REPLACE;
         String outputCollection = null;
 
         if (params.get("out") instanceof DBObject) {
             DBObject out = (DBObject) params.get("out");
             if (out.get("sharded") != null) {
                 throw new InvalidMongoCommandException(ErrorCodes.COMMAND_NOT_SUPPORTED, "sharded is not yet supported. Please remove it and run again");
             }
             if (out.get("replace") != null) {
                 if (out.get("nonAtomic") != null) {
                     throw new InvalidMongoCommandException(ErrorCodes.COMMAND_NOT_SUPPORTED, "nonAtomic is not supported in replace mode. Please remove it and run again");
                 }
                 outputCollection = (String) out.get("replace");
                 outputDb = (String) out.get("db");
                 outputType = MapReduceCommand.OutputType.REPLACE;
             } else if (out.get("merge") != null) {
                 outputCollection = (String) out.get("merge");
                 outputDb = (String) out.get("db");
                 outputType = MapReduceCommand.OutputType.INLINE;
             } else if (out.get("reduce") != null) {
                 outputCollection = (String) out.get("reduce");
                 outputDb = (String) out.get("db");
                 outputType = MapReduceCommand.OutputType.INLINE;
             } else if (out.get("inline") != null) {
                 outputType = MapReduceCommand.OutputType.INLINE;
                 if (out.get("nonAtomic") != null) {
                     throw new InvalidMongoCommandException(ErrorCodes.COMMAND_NOT_SUPPORTED, "nonAtomic is not supported in inline mode. Please remove it and run again");
                 }
             }
         } else if (params.get("out") instanceof String) {
             outputCollection = (String) params.get("out");
         }
 
         DBObject query = (DBObject) params.get("query");
         DBObject sort = (DBObject) params.get("sort");
         if (params.get("limit") != null) {
             limit = (Integer) params.get("limit");
         }
         String finalize = (String) params.get("finalize");
         Map scope = (Map) params.get("scope");
         if (params.get("jsMode") != null) {
             throw new InvalidMongoCommandException(ErrorCodes.COMMAND_NOT_SUPPORTED, "jsMode is not yet supported. Please remove it and run again");
         }
         boolean verbose = true;
         if (params.get("verbose") != null) {
             verbose = (Boolean) params.get("verbose ");
         }
 
         MapReduceCommand mapReduceCommand = new MapReduceCommand(dbCollection, map, reduce, outputCollection, outputType, query);
         mapReduceCommand.setSort(sort);
         mapReduceCommand.setLimit(limit);
         mapReduceCommand.setFinalize(finalize);
         mapReduceCommand.setScope(scope);
         mapReduceCommand.setVerbose(verbose);
         mapReduceCommand.setOutputDB(outputDb);
 
         MapReduceOutput mapReduceOutput = dbCollection.mapReduce(mapReduceCommand);
         return ApplicationUtils.constructResponse(false, mapReduceOutput.getCommandResult());
     }
 
     private static JSONObject executeRemove(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         WriteResult result = dbCollection.remove(queryObj);
         return ApplicationUtils.constructResponse(false, result.getLastError());
     }
 
     private static JSONObject executeUpdate(DBCollection dbCollection, String queryStr) throws JSONException, InvalidMongoCommandException {
         String reconstructedUpdateQuery = "{updateQueryParams:[" + queryStr + "]}";
         DBObject queryObj = (DBObject) JSON.parse(reconstructedUpdateQuery);
 
         List queryParams = (List) queryObj.get("updateQueryParams");
         if (queryParams.size() < 2) {
             throw new InvalidMongoCommandException(ErrorCodes.COMMAND_ARGUMENTS_NOT_SUFFICIENT, "Requires atleast 2 params");
         }
         DBObject criteria = (DBObject) queryParams.get(0);
         DBObject updateByValuesMap = (DBObject) queryParams.get(1);
         boolean upsert = false, multi = false;
         if (queryParams.size() > 2) {
             upsert = (Boolean) queryParams.get(2);
             if (queryParams.size() > 3) {
                 multi = (Boolean) queryParams.get(3);
             }
         }
         WriteResult updateResult = dbCollection.update(criteria, updateByValuesMap, upsert, multi);
         CommandResult commandResult = updateResult.getLastError();
         return ApplicationUtils.constructResponse(false, commandResult);
     }
 
     private static JSONObject executeStats(DBCollection dbCollection) throws JSONException {
         CommandResult stats = dbCollection.getStats();
         return ApplicationUtils.constructResponse(false, stats);
     }
 
     private static JSONObject executeStorageSize(DBCollection dbCollection) throws JSONException {
         Integer storageSize = (Integer) dbCollection.getStats().toMap().get("storageSize");
         return ApplicationUtils.constructResponse(false, new BasicDBObject("storageSize", storageSize));
     }
 
     private static JSONObject executeTotalIndexSize(DBCollection dbCollection) throws JSONException {
         Integer totalIndexSize = (Integer) dbCollection.getStats().toMap().get("totalIndexSize");
         return ApplicationUtils.constructResponse(false, new BasicDBObject("totalIndexSize", totalIndexSize));
     }
 
 }
