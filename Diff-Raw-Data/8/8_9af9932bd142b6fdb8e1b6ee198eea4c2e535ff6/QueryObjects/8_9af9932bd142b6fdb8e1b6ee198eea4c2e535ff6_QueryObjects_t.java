 package org.bonitasoft.sugarcrm;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bonitasoft.engine.connector.Connector;
 import org.bonitasoft.engine.connector.ConnectorException;
 import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.json.simple.JSONObject;
 
 public class QueryObjects implements Connector {
     public static final String LOGIN = "login";
     public static final String PASSWORD = "password";
     public static final String BASE_URL = "baseURL";
     public static final String OBJECT_TYPE = "objectType";
     public static final String QUERY = "query";
     public static final String OFFSET = "offset";
     public static final String MAX_RESULT = "max_result";
 
     SugarCRMUtils sugarCRMUtils;
 
     String login;
     String password;
     String baseURL;
     String objectType;
 
     String sessionId;
     private String query;
     private String offset;
     private List<String> selectFields;
     private String maxResults;
 
     private Logger LOGGER = Logger.getLogger(this.getClass().getName());
 
     @Override
     public void setInputParameters(Map<String, Object> parameters) {
         login = (String) parameters.get(LOGIN);
         LOGGER.info(LOGIN + " " + login);
         password = (String) parameters.get(PASSWORD);
         LOGGER.info(PASSWORD + " ******");
         baseURL = (String) parameters.get(BASE_URL);
         LOGGER.info(BASE_URL + " " + baseURL);
         objectType = (String) parameters.get(OBJECT_TYPE);
         LOGGER.info(OBJECT_TYPE + " " + objectType);
         query = (String) parameters.get(QUERY);
         LOGGER.info(QUERY + " " + query);
         offset = (String) parameters.get(OFFSET);
         LOGGER.info(OFFSET + " " + offset);
         maxResults = (String) parameters.get(MAX_RESULT);
         LOGGER.info(MAX_RESULT + " " + maxResults);
        selectFields = (List<String>) parameters.get("selectFields");
         for (String field : selectFields) {
             LOGGER.info("Select field " + field);
         }
     }
 
     @Override
     public void validateInputParameters() throws ConnectorValidationException {
 
     }
 
     @Override
     public Map<String, Object> execute() throws ConnectorException {
         Map<String, Object> result = new HashMap<String, Object>();
         try {
            JSONObject jsonObject = sugarCRMUtils.query(sessionId, objectType, query, offset, selectFields, maxResults);
            result.put("jsonObject", jsonObject);
             return result;
         } catch (IOException e) {
             throw new ConnectorException(e);
         }
 
     }
 
     @Override
     public void connect() throws ConnectorException {
         sugarCRMUtils = new SugarCRMUtils(baseURL);
         try {
             sessionId = sugarCRMUtils.login(login, password);
         } catch (NoSuchAlgorithmException e) {
             throw new ConnectorException(e);
         } catch (IOException e) {
             throw new ConnectorException(e);
         }
     }
 
     @Override
     public void disconnect() throws ConnectorException {
         try {
             sessionId = sugarCRMUtils.logout(sessionId);
         } catch (IOException e) {
             throw new ConnectorException(e);
         }
     }
 }
