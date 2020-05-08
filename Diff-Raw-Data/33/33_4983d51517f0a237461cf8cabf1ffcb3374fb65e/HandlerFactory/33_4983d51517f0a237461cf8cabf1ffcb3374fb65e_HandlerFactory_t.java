 package ncl.military.controller.handle;
 
 import ncl.military.controller.exceptions.HandlerException;
 import ncl.military.dao.DAO;
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletConfig;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author gural
  * @version 1.0
  *          Date: 13.04.12
  *          Time: 13:52
  */
 public class HandlerFactory {
 
     private static final Logger log = Logger.getLogger("controller");
 
     private static Map<String, Executable> executors = new HashMap<String, Executable>();
 
     // path
     public static final String PATH_SOLDIER = "/viewSoldiers";
     public static final String PATH_UNIT = "/viewUnits";
     public static final String PATH_LOCATION = "/viewLocations";
 
     // common actions
     public static final String GET_ALL = "getAll";
     public static final String GET_TOP = "getTop";
     public static final String EDIT_SOLDIER = "editSoldier";
     public static final String EDIT_UNIT = "editUnit";
     public static final String EDIT_LOCATION = "editLocation";
     public static final String GET_SEARCH_RESULTS = "getSearchResults";
 
     // actions for soldiers
     public static final String GET_SUBS_OF_SOLDIER = "getSubsOfSoldier";
     public static final String ADD_SOLDIER = "addSoldier";
     public static final String DELETE_SOLDIER = "deleteSoldier";
     public static final String MOVE_SOLDIER = "moveSoldier";
     public static final String MOVE_UNDER_THIS_SOLDIER = "moveUnderThisSoldier";
 
     // actions for units
     public static final String GET_SOLDIERS_OF_UNIT = "getSoldiersOfUnit";
 
     // actions for locations
     public static final String GET_UNITS_OF_LOCATION = "getUnitsOfLocations";
 
     //view
     public static final String VIEW_SOLDIER_MAIN = "/viewSoldiers.jsp";
     public static final String VIEW_UNIT_MAIN = "/viewUnits.jsp";
     public static final String VIEW_LOCATION_MAIN = "/viewLocations.jsp";
     public static final String VIEW_SOLDIER_EDIT = "/editorSoldier.jsp";
     public static final String VIEW_UNIT_EDIT = "/editorUnit.jsp";
     public static final String VIEW_LOCATION_EDIT = "/editorLocation.jsp";
     public static final String VIEW_HOME = "/index.jsp";
     public static final String VIEW_ERROR = "/error.jsp";
 
     public static Handlable getHandler(DAO dao, ServletConfig config, Map<String, Object> params) throws HandlerException {
 
         // <userPath>:<action> - get class name
         // <userPath>: - get action name
         // <userPath> - get view name
         // :<action> - get view name
 
         Executable executable = null;
         String view = null;
         log.debug("onGetHandler");
         log.debug("userPath => " + params.get("userPath"));
         log.debug("action => " + params.get("action"));
         if (params.get("action") != null) {
             view = config.getInitParameter(":" + (String) params.get("action"));
         } else {
             params.put("action", config.getInitParameter((String) params.get("userPath") + ":"));
             log.debug("action got => " + params.get("action"));
             view = config.getInitParameter((String) params.get("userPath"));
         }
        log.debug("PATIENT!!!!!!!!!!!!!!!!!!!!!!view got => " + view);
 
         String executorSpec = params.get("userPath") + ":" + params.get("action");
         log.debug("executorSpec => " + executorSpec);
         try {
             String executorClass = config.getInitParameter(executorSpec);
             if (executorClass == null)
                 throw new IllegalStateException("Descriptor have no match for given param: " + executorSpec);
             log.debug("executorClass => " + executorClass);
             if (executors.containsKey(executorClass)) {
                 executable = executors.get(executorClass);
             } else {
                 Class executor = Class.forName(executorClass);
                 Constructor executorConstructor = executor.getConstructor(DAO.class);
                 executable = (Executable) executorConstructor.newInstance(dao);
             }
         } catch (IllegalStateException e) {
             log.error(e.getMessage(), e);
             throw new HandlerException(e.getMessage(), e);
         } catch (ClassNotFoundException e) {
             log.error("Could not load class with given name.", e);
             throw new HandlerException("Could not load class with given name.", e);
         } catch (NoSuchMethodException e) {
             log.error("There is no such constructor in loaded class.", e);
             throw new HandlerException("There is no such constructor in loaded class.", e);
         } catch (InvocationTargetException e) {
             log.error(e.getMessage(), e);
             throw new HandlerException(e.getMessage(), e);
         } catch (InstantiationException e) {
             log.error(e.getMessage(), e);
             throw new HandlerException(e.getMessage(), e);
         } catch (IllegalAccessException e) {
             log.error(e.getMessage(), e);
             throw new HandlerException(e.getMessage(), e);
         }
 
         if (executable == null)
             throw new IllegalStateException("Did not matched executor");
         return new Handler(executable, (String) params.get("userPath"), (String) params.get("action"), view);
     }
 }
