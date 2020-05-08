 package core;
 
 import assistant.Constants;
 import assistant.SharedHandler;
 import assistant.pair.NullableExtendedParam;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
 import models.*;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: OMMatte
  * Date: 2013-03-13
  * Time: 11:57
  */
 public class ForumRequestHandler extends SharedHandler {
 
     public ForumRequestHandler() {
         super();
     }
 
     @Override
     public String[] handledRequests() {
         return new String[]{
             Constants.Method.GET_FORUM,
             Constants.Method.GET_CATEGORY,
             Constants.Method.GET_THREAD,
             Constants.Method.CREATE_THREAD,
             Constants.Method.CREATE_REPLY,
         };
     }
 
     @Override
     public void process(Map<String, Object> jsonrpc2Params) throws Exception {
         if(method.equals(Constants.Method.GET_FORUM)){
             getForum(jsonrpc2Params);
         }
         else if(method.equals(Constants.Method.GET_CATEGORY)){
             getCategory(jsonrpc2Params);
         }
         else if(method.equals(Constants.Method.GET_THREAD)){
             getThread(jsonrpc2Params);
         }
         else if(method.equals(Constants.Method.CREATE_THREAD)){
             createThread(jsonrpc2Params);
         }
         else if(method.equals(Constants.Method.CREATE_REPLY)){
             createReply(jsonrpc2Params);
         }
         throwJSONRPC2Error(JSONRPC2Error.METHOD_NOT_FOUND, Constants.Errors.METHOD_NOT_FOUND, method);
     }
 
     /**
      * Returns the categories by the given forum type.
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void getForum(Map<String, Object> jsonrpc2Params) throws Exception {
         Map<String, Object> getForumParams = getParams(jsonrpc2Params,
                 Constants.Param.Name.TYPE);
 
         String type = (String) getForumParams.get(Constants.Param.Name.TYPE);
         List<Category> categories = null;
 
         if(type.equals(Constants.Param.Value.PUBLIC)){
             categories = getAllColumns(Category.class);
         }
         else if(type.equals(Constants.Param.Value.PRIVATE)){
 
             categories = getAllColumns(Category.class);
 
         }else{
             throwJSONRPC2Error(JSONRPC2Error.INVALID_PARAMS, Constants.Errors.PARAM_VALUE_NOT_ALLOWED, "Type: " + type);
         }
 
         responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         responseParams.put(Constants.Param.Name.CATEGORIES, serialize((Serializable) categories));
     }
 
     /**
      * Creates a reply by the given threadId.
      * Returns the created reply if successful.
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void createReply(Map<String, Object> jsonrpc2Params) throws Exception {
 
         Map<String, Object> createReplyParams = getParams(jsonrpc2Params,
                 Constants.Param.Name.THREAD_ID,
                 Constants.Param.Name.TEXT);
 
         int parentThreadId = (Integer)createReplyParams.get(Constants.Param.Name.THREAD_ID);
         String text = (String) createReplyParams.get(Constants.Param.Name.TEXT);
 
         models.Thread parentThread = em.find(models.Thread.class, parentThreadId);
         Date currentDate = new Date();
 
         Reply reply = new Reply(parentThread, em.find(Account.class, accountId), text, new Timestamp(currentDate.getTime()));
 
         persistObjects(reply);
 
         responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         responseParams.put(Constants.Param.Name.REPLY, serialize(reply));
     }
 
 
     /**
      * Creates a Thread by the given categoryId.
      * Returns the created Thread.
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void createThread(Map<String, Object> jsonrpc2Params) throws Exception {
 
         Map<String, Object> createThreadParams = getParams(jsonrpc2Params,
                 Constants.Param.Name.CATEGORY_ID,
                 Constants.Param.Name.HEADLINE,
                 Constants.Param.Name.TEXT);
 
 
         models.Thread thread = new models.Thread(
                                 em.find(Category.class, createThreadParams.get(Constants.Param.Name.CATEGORY_ID)),
                                 em.find(Account.class, accountId),
                 (String)        createThreadParams.get(Constants.Param.Name.HEADLINE),
                 (String)        createThreadParams.get(Constants.Param.Name.TEXT)
                 );
 
         persistObjects(thread);
         responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         responseParams.put(Constants.Param.Name.CATEGORY, serialize(thread));
 
     }
 
     /**
      * Returns a list of replies given a threadId.
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void getThread(Map<String, Object> jsonrpc2Params) throws Exception {
 
         Map<String, Object> getForumParams = getParams(jsonrpc2Params,
                 new NullableExtendedParam(Constants.Param.Name.THREAD_ID, false),
                 new NullableExtendedParam(Constants.Param.Name.SORT_TYPE, true));
 
         String threadId = (String) getForumParams.get(Constants.Param.Name.THREAD_ID);
         Integer sortType = (Integer) getForumParams.get(Constants.Param.Name.SORT_TYPE);
 
         models.Thread thread = em.find(models.Thread.class, threadId);
         List replies = thread.getReplies();
 
 
         if(sortType == null || sortType == 0){
             // Sort using standard sorting (aka time)
             Collections.sort(replies, ReplyComparator.getComparator(ReplyComparator.TIME_SORT));
 
         }else if(sortType == 1){
             //Sort after time
             Collections.sort(replies, ReplyComparator.getComparator(ReplyComparator.TIME_SORT));
 
         }else if(sortType == 2){
             //Sort after date, and secondly time
             Collections.sort(replies, ReplyComparator.getComparator( ReplyComparator.VOTE_SORT, ReplyComparator.TIME_SORT));
 
         }
 
             responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
             responseParams.put(Constants.Param.Name.CATEGORIES, serialize((Serializable) replies));
     }
 
     /**
      * Returns a list of threads by the given categoryId.
      * @param jsonrpc2Params
      * @throws Exception
      */
     private void getCategory(Map<String, Object> jsonrpc2Params) throws Exception {
 
         Map<String, Object> createReplyParams = getParams(jsonrpc2Params,
                 new NullableExtendedParam(Constants.Param.Name.CATEGORY_ID, false));
 
        String categoryId = (String) createReplyParams.get(Constants.Param.Name.CATEGORY_ID);
 
         Category category = em.find(Category.class, categoryId);
         List<models.Thread> threadList = category.getThreads();
 
 
         responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         responseParams.put(Constants.Param.Name.CATEGORIES, serialize((Serializable) threadList));
     }
 }
