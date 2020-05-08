 
 package edu.wustl.query.action;
 
 import java.io.Writer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.wustl.common.query.factory.AbstractQueryUIManagerFactory;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.query.querymanager.Count;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.querysuite.AbstractQueryUIManager;
import edu.wustl.cider.util.CiderQueryUIManager;
 import edu.wustl.query.util.querysuite.QueryModuleException;
 
 /**
  * @author chitra_garg
  *handles ajax call for recent queries
  */
 public class RecentQueriesAjaxHandlerAction extends Action
 {
 	private static org.apache.log4j.Logger logger =Logger.getLogger(RecentQueriesAjaxHandlerAction.class);
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		Writer writer = response.getWriter();
 		int queryExecutionId = Integer.valueOf(request.getParameter(Constants.EXECUTION_LOG_ID));
 		String index = request.getParameter(Constants.INDEX);
		Count countObject = populateCountObject(queryExecutionId);
 		//create json object by count object adding Query status, count and execution id
		JSONObject resultObject = createResultJSON(countObject, index);
 		response.setContentType(Constants.CONTENT_TYPE_TEXT);
 		writer.write(new JSONObject().put(Constants.RESULT_OBJECT, resultObject).toString());

 		return null;
 
 	}
 
 	/**
 	 * 
 	 * @param queryExecutionId= query execution id
 	 * @return count object for given execution id
 	 * @throws QueryModuleException
 	 */
	private Count populateCountObject(int queryExecutionId) throws QueryModuleException
 	{
 		//retrieve count with query execution id
 		AbstractQueryUIManager qUIManager = AbstractQueryUIManagerFactory
 				.getDefaultAbstractUIQueryManager();
		Count countObject = ((CiderQueryUIManager) qUIManager).getCount(queryExecutionId);
 		try
         {
             Thread.sleep(5000);
         }
         catch (InterruptedException ie)
         {
            Logger.out.debug(ie.getMessage(),ie);
         } 
 		return countObject;
 	}
 
 	/**
 	 * Creates a json object and appends query count, status, execution id to it.
 	 * @param countObject
 	 * @return
 	 */
	private JSONObject createResultJSON(Count countObject, String index)
 	{
 		JSONObject resultObject = new JSONObject();
 		try
 		{
 			resultObject.append(Constants.QUERY_COUNT, countObject.getCount());
 			resultObject.append(Constants.GET_COUNT_STATUS, countObject.getStatus());
 			resultObject.append(Constants.EXECUTION_ID, countObject.getQueryExectionId());
 			resultObject.append(Constants.INDEX, index);
 		}
 		catch (JSONException e)
 		{
 			logger.error("error in initializing json object " + e);
 		}
 		return resultObject;
 	}
 
}
