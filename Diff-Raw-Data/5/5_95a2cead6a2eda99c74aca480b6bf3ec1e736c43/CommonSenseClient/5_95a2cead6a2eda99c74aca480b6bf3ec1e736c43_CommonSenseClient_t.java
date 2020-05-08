 package nl.sense_os.commonsense.lib.client.communication;
 
 import java.util.List;
 import java.util.Map.Entry;
 
 import nl.sense_os.commonsense.lib.client.util.Md5Hasher;
 
 import com.google.gwt.core.client.JsonUtils;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestBuilder.Method;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.http.client.UrlBuilder;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONBoolean;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.Window.Location;
 
 public class CommonSenseClient {
 
     public static class Urls {
         private static final String PATH_PREFIX = Constants.STABLE_MODE || Constants.RC_MODE
                 || Constants.DEV_MODE ? "api/" : "";
         public static final String HOST = Constants.STABLE_MODE ? "common.sense-os.nl"
                 : Constants.RC_MODE ? "rc.sense-os.nl"
                         : Constants.DEV_MODE ? "common.dev.sense-os.nl" : "api.sense-os.nl";
         public static final String PROTOCOL = "http";
 
         // main paths
         public static final String PATH_DOMAINS = PATH_PREFIX + "domains";
         public static final String PATH_SENSORS = PATH_PREFIX + "sensors";
         public static final String PATH_GROUPS = PATH_PREFIX + "groups";
         public static final String PATH_USERS = PATH_PREFIX + "users";
         public static final String PATH_ENVIRONMENTS = PATH_PREFIX + "environments";
         public static final String PATH_LOGIN = PATH_PREFIX + "login";
         public static final String PATH_LOGIN_GOOGLE = "login/openID/google"; // no prefix!
         public static final String PATH_LOGOUT = PATH_PREFIX + "logout";
         public static final String PATH_PW_RESET = PATH_PREFIX + "resetPassword";
         public static final String PATH_FORGOT_PASSWORD = PATH_PREFIX + "requestPasswordReset";
 
         // domains paths
        public static final String PATH_DOMAIN_USERS = PATH_DOMAINS + "/%1/users";
 
         // sensors paths
         public static final String PATH_AVAIL_SERVICES = PATH_SENSORS + "/services/available";
         public static final String PATH_SENSOR_DATA = PATH_SENSORS + "/%1/data";
         public static final String PATH_SERVICE = PATH_SENSORS + "/%1/services/%2";
         public static final String PATH_SERVICE_METHODS = PATH_SERVICE + "/methods";
         public static final String PATH_CONNECTED_SENSORS = PATH_SENSORS + "/%1/sensors";
 
         // users paths
         public static final String PATH_CURRENT_USER = PATH_USERS + "/current";
 
         // groups paths
         public static final String PATH_GROUP_USERS = PATH_GROUPS + "/%1/users";
 
         private Urls() {
             // do not instantiate
         }
     }
 
     private static CommonSenseClient instance;
     private static final String JSON_TYPE = "application/json";
     private static final String WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
 
     /**
      * @return A CommonSenseClient instance, using singleton pattern
      */
     public static final CommonSenseClient getClient() {
         if (null == instance) {
             instance = new CommonSenseClient();
         }
         return instance;
     }
 
     /**
      * 
      * @param method
      * @param url
      * @param sessionId
      * @param data
      * @param callback
      */
     private static void sendRequest(Method method, String url, String sessionId, String data,
             RequestCallback callback) {
         try {
             RequestBuilder builder = new RequestBuilder(method, url);
             if (null != sessionId) {
                 builder.setHeader("X-SESSION_ID", sessionId);
             }
             if (null != data) {
                 // set content type header
                 if (data.startsWith("{") && data.endsWith("}")) {
                     builder.setHeader("Content-Type", JSON_TYPE);
                 } else if (data.contains("=")) {
                     builder.setHeader("Content-Type", WWW_FORM_URLENCODED);
                 }
             }
             builder.setHeader("Accept", JSON_TYPE);
             builder.sendRequest(data, callback);
         } catch (Exception e) {
             callback.onError(null, e);
         }
     }
 
     private String sessionId;
 
     private CommonSenseClient() {
         // private constructor to prevent direct instantiation
     }
 
     /**
      * 
      * @param callback
      * @param domainId
      * @param userId
      * @param username
      */
     public void addDomainUser(RequestCallback callback, String domainId, String userId,
             String username) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_DOMAIN_USERS.replace("%1", domainId));
         String url = urlBuilder.buildString();
 
         // prepare request data
         String user = "";
         if (null != userId && null != username) {
             user = "{\"id\":\"" + userId + "\",\"username\":\"" + username + "\"}";
         } else if (null != userId) {
             user = "{\"id\":\"" + userId + "\"}";
         } else if (null != username) {
             user = "{\"username\":\"" + username + "\"}";
         } else {
             callback.onError(null, new Throwable("Cannot find user ID or user name"));
         }
         String data = "{\"users\":[" + user + "]}";
 
         // send request
         sendRequest(method, url, sessionId, data, callback);
     }
 
     /**
      * 
      * @param callback
      * @param email
      */
     public void checkEmailAvailability(RequestCallback callback, String email) {
 
         // prepare request
         Method httpMethod = RequestBuilder.GET;
         String url = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_USERS + "/check/email/" + email).buildString();
 
         sendRequest(httpMethod, url, null, null, callback);
     }
 
     /**
      * 
      * @param callback
      * @param username
      */
     public void checkUsernameAvailability(RequestCallback callback, String username) {
 
         // prepare request
         Method httpMethod = RequestBuilder.GET;
         String url = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_USERS + "/check/username/" + username).buildString();
 
         sendRequest(httpMethod, url, null, null, callback);
     }
 
     /**
      * 
      * @param callback
      * @param name
      * @param description
      * @param displayName
      * @param dataType
      * @param dataStructure
      */
     public void createSensor(RequestCallback callback, String name, String description,
             String displayName, String dataType, String dataStructure) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_GROUPS);
         String url = urlBuilder.buildString();
 
         JSONObject sensor = new JSONObject();
         sensor.put("name", new JSONString(name));
         if (null != displayName) {
             sensor.put("display_name", new JSONString(displayName));
         }
         if (null != description) {
             sensor.put("device_type", new JSONString(description));
         }
         if (null != dataType) {
             sensor.put("data_type", new JSONString(dataType));
         }
         if (null != dataStructure) {
             sensor.put("display_name", new JSONString(dataStructure));
         }
         String data = "{\"sensor\":" + sensor.toString() + "}";
 
         // send request
         sendRequest(method, url, sessionId, data, callback);
     }
 
     /**
      * 
      * @param callback
      * @param name
      * @param email
      * @param username
      * @param password
      * @param description
      * @param publik
      * @param hidden
      * @param anonymous
      * @param accessPassword
      * @param requiredSensors
      * @param optionalSensors
      * @param defaultListUsers
      * @param defaultAddUsers
      * @param defaultRemoveUsers
      * @param defaultListSensors
      * @param defaultAddSensors
      * @param defaultRemoveSensors
      * @param reqShowUsername
      * @param reqShowName
      * @param reqShowSurname
      * @param reqShowEmail
      * @param reqShowMobile
      * @param reqShowId
      * @param reqShowZip
      * @param reqShowAddress
      * @param reqShowCountry
      */
     public void createGroup(RequestCallback callback, String name, String email, String username,
             String password, String description, Boolean publik, Boolean hidden, Boolean anonymous,
             String accessPassword, List<String> requiredSensors, List<String> optionalSensors,
             Boolean defaultListUsers, Boolean defaultAddUsers, Boolean defaultRemoveUsers,
             Boolean defaultListSensors, Boolean defaultAddSensors, Boolean defaultRemoveSensors,
             Boolean reqShowUsername, Boolean reqShowName, Boolean reqShowSurname,
             Boolean reqShowEmail, Boolean reqShowMobile, Boolean reqShowId, Boolean reqShowZip,
             Boolean reqShowAddress, Boolean reqShowCountry) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_GROUPS);
         String url = urlBuilder.buildString();
 
         // prepare request data
         JSONObject group = new JSONObject();
         group.put("name", new JSONString(name));
         if (null != email) {
             group.put("email", new JSONString(email));
         }
         if (null != username) {
             group.put("username", new JSONString(username));
         }
         if (null != password) {
             String hashedPass = Md5Hasher.hash(password);
             group.put("password", new JSONString(hashedPass));
         }
         if (null != description) {
             group.put("description", new JSONString(description));
         }
         if (null != publik) {
             group.put("public", JSONBoolean.getInstance(publik));
         }
         if (null != hidden) {
             group.put("hidden", JSONBoolean.getInstance(hidden));
         }
         if (null != anonymous) {
             group.put("anonymous", JSONBoolean.getInstance(anonymous));
         }
         if (null != accessPassword) {
             group.put("access_password", new JSONString(accessPassword));
         }
         if (null != requiredSensors) {
             JSONArray array = new JSONArray();
             for (int i = 0; i < requiredSensors.size(); i++) {
                 array.set(i, new JSONString(requiredSensors.get(i)));
             }
             group.put("required_sensors", array);
         }
         if (null != optionalSensors) {
             JSONArray array = new JSONArray();
             for (int i = 0; i < optionalSensors.size(); i++) {
                 array.set(i, new JSONString(optionalSensors.get(i)));
             }
             group.put("optional_sensors", array);
         }
         if (null != defaultListUsers) {
             group.put("default_list_users", JSONBoolean.getInstance(defaultListUsers));
         }
         if (null != defaultAddUsers) {
             group.put("default_add_users", JSONBoolean.getInstance(defaultAddUsers));
         }
         if (null != defaultRemoveUsers) {
             group.put("default_remove_users", JSONBoolean.getInstance(defaultRemoveUsers));
         }
         if (null != defaultListSensors) {
             group.put("default_list_sensors", JSONBoolean.getInstance(defaultListSensors));
         }
         if (null != defaultAddSensors) {
             group.put("default_add_sensors", JSONBoolean.getInstance(defaultAddSensors));
         }
         if (null != defaultRemoveSensors) {
             group.put("default_remove_sensors", JSONBoolean.getInstance(defaultRemoveSensors));
         }
         if (null != reqShowUsername) {
             group.put("required_show_username", JSONBoolean.getInstance(reqShowUsername));
         }
         if (null != reqShowName) {
             group.put("required_show_first_name", JSONBoolean.getInstance(reqShowName));
         }
         if (null != reqShowSurname) {
             group.put("required_show_surname", JSONBoolean.getInstance(reqShowSurname));
         }
         if (null != reqShowEmail) {
             group.put("required_show_email", JSONBoolean.getInstance(reqShowEmail));
         }
         if (null != reqShowId) {
             group.put("required_show_id", JSONBoolean.getInstance(reqShowId));
         }
         if (null != reqShowAddress) {
             group.put("required_show_address", JSONBoolean.getInstance(reqShowAddress));
         }
         if (null != reqShowZip) {
             group.put("required_show_zipcode", JSONBoolean.getInstance(reqShowZip));
         }
         if (null != reqShowCountry) {
             group.put("required_show_country", JSONBoolean.getInstance(reqShowCountry));
         }
         if (null != reqShowMobile) {
             group.put("required_show_phone_number", JSONBoolean.getInstance(reqShowMobile));
         }
         String data = "{\"group\":" + group.toString() + "}";
 
         // send request
         sendRequest(method, url, sessionId, data, callback);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param value
      * @param timestamp
      */
     public void createSensorData(RequestCallback callback, String sensorId, String value,
             long timestamp) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_SENSOR_DATA.replace("%1", sensorId));
         String url = urlBuilder.buildString();
 
         // prepare request data
         String date = NumberFormat.getFormat("#.000").format(timestamp / 1000d);
         String data = "{\"data\":{\"value\":\"" + value + "\",\"date\":" + date + "}}";
 
         // send request
         sendRequest(method, url, sessionId, data, callback);
     }
 
     /**
      * 
      * @param callback
      * @param username
      * @param password
      * @param name
      * @param surname
      * @param phone
      * @param email
      * @param address
      * @param zipCode
      * @param country
      * @param disableMail
      */
     public void createUser(RequestCallback callback, String username, String password, String name,
             String surname, String phone, String email, String address, String zipCode,
             String country, Boolean disableMail) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_USERS);
         String url = urlBuilder.buildString();
 
         // prepare data
         JSONObject user = new JSONObject();
         user.put("username", new JSONString(username));
         user.put("password", new JSONString(Md5Hasher.hash(password)));
         if (null != email) {
             user.put("email", new JSONString(email));
         }
         if (null != name) {
             user.put("name", new JSONString(name));
         }
         if (null != surname) {
             user.put("surname", new JSONString(surname));
         }
         if (null != phone) {
             user.put("mobile", new JSONString(phone));
         }
         if (null != address) {
             user.put("address", new JSONString(address));
         }
         if (null != zipCode) {
             user.put("zipcode", new JSONString(zipCode));
         }
         if (null != country) {
             user.put("country", new JSONString(country));
         }
         if (null != disableMail) {
             user.put("disable_mail", new JSONString(disableMail ? "1" : "0"));
         }
         JSONObject jso = new JSONObject();
         jso.put("user", user);
         String data = jso.toString();
 
         // send request
         sendRequest(method, url, sessionId, data, callback);
     }
 
     /**
      * 
      * @param callback
      * @param environmentId
      */
     public void deleteEnvironment(RequestCallback callback, int environmentId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.DELETE;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_ENVIRONMENTS + "/" + environmentId);
         String url = urlBuilder.buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param dataId
      */
     public void deleteSensorData(RequestCallback callback, String sensorId, String dataId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.DELETE;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_SENSOR_DATA.replace("%1", sensorId) + "/" + dataId);
         String url = urlBuilder.buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param serviceId
      */
     public void disconnectService(RequestCallback callback, String sensorId, String serviceId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.DELETE;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_SERVICE.replace("%1", sensorId).replace("%2", serviceId));
         String url = urlBuilder.buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * @param callback
      *            RequestCallback to handle HTTP response
      * @param username
      * @param email
      */
     public void forgotPassword(RequestCallback callback, String username, String email) {
 
         // prepare request details
         Method method = RequestBuilder.POST;
         String url = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_FORGOT_PASSWORD).buildString();
 
         // prepare request data
         String requestData = null != username ? "username=" + URL.encode(username) : "email="
                 + URL.encode(email);
 
         // send request
         sendRequest(method, url, null, requestData, callback);
     }
 
     /**
      * 
      * @param callback
      * @param perPage
      * @param page
      * @param groupId
      */
     public void getAvailableServices(RequestCallback callback, String perPage, String page,
             String groupId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_AVAIL_SERVICES);
         if (null != page) {
             urlBuilder.setParameter("page", page);
         }
         if (null != perPage) {
             urlBuilder.setParameter("per_page", perPage);
         }
         if (null != groupId) {
             urlBuilder.setParameter("group_id", groupId);
         }
         String url = urlBuilder.buildString();
 
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      */
     public void getConnectedSensors(RequestCallback callback, String sensorId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_CONNECTED_SENSORS.replace("%1", sensorId));
         String url = urlBuilder.buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * @param callback
      *            RequestCallback to handle HTTP response
      */
     public void getCurrentUser(RequestCallback callback) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         Method method = RequestBuilder.GET;
         String url = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_CURRENT_USER).buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * 
      * @param callback
      * @param perPage
      * @param page
      */
     public void getEnvironments(RequestCallback callback, String perPage, String page) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_ENVIRONMENTS);
         if (null != page) {
             urlBuilder.setParameter("page", page);
         }
         if (null != perPage) {
             urlBuilder.setParameter("per_page", perPage);
         }
         String url = urlBuilder.buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * 
      * @param callback
      * @param perPage
      * @param page
      */
     public void getGroups(RequestCallback callback, String perPage, String page) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_GROUPS);
         if (null != page) {
             urlBuilder.setParameter("page", page);
         }
         if (null != perPage) {
             urlBuilder.setParameter("per_page", perPage);
         }
         String url = urlBuilder.buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * 
      * @param callback
      * @param groupId
      * @param perPage
      * @param page
      */
     public void getGroupUsers(RequestCallback callback, String groupId, String perPage, String page) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_GROUP_USERS.replace("%1", groupId));
         if (null != page) {
             urlBuilder.setParameter("page", page);
         }
         if (null != perPage) {
             urlBuilder.setParameter("per_page", perPage);
         }
         String url = urlBuilder.buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * @param callback
      *            RequestCallback to handle HTTP response
      * @param sensorId
      *            ID of the sensor to request data for
      * @param startDate
      * @param endDate
      * @param date
      * @param perPage
      * @param page
      * @param interval
      * @param next
      * @param last
      */
     public void getSensorData(RequestCallback callback, String sensorId, Long startDate,
             Long endDate, Long date, Integer perPage, Integer page, Integer interval, Boolean next,
             Boolean last, String sort) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_SENSOR_DATA.replace("%1", sensorId));
         if (null != startDate) {
             String dateString = NumberFormat.getFormat("#.000").format(
                     startDate.longValue() / 1000d);
             urlBuilder.setParameter("start_date", dateString);
         }
         if (null != endDate) {
             String dateString = NumberFormat.getFormat("#.000").format(endDate.longValue() / 1000d);
             urlBuilder.setParameter("end_date", dateString);
         }
         if (null != date) {
             String dateString = NumberFormat.getFormat("#.000").format(date.longValue() / 1000d);
             urlBuilder.setParameter("date", dateString);
         }
         if (null != perPage) {
             urlBuilder.setParameter("per_page", perPage.toString());
         }
         if (null != page) {
             urlBuilder.setParameter("page", page.toString());
         }
         if (null != interval) {
             urlBuilder.setParameter("interval", interval.toString());
         }
         if (null != last) {
             urlBuilder.setParameter("last", last ? "1" : "0");
         }
         if (null != next) {
             urlBuilder.setParameter("next", next ? "1" : "0");
         }
         if (null != sort) {
             urlBuilder.setParameter("sort", sort);
         }
         String url = urlBuilder.buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * @param callback
      *            RequestCallback to handle HTTP response
      * @param perPage
      * @param page
      * @param shared
      * @param owned
      * @param physical
      * @param details
      * @param groupId
      */
     public void getSensors(RequestCallback callback, Integer perPage, Integer page, Boolean shared,
             Boolean owned, Boolean physical, String details, String groupId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_SENSORS);
         if (null != page) {
             urlBuilder.setParameter("page", page.toString());
         }
         if (null != perPage) {
             urlBuilder.setParameter("per_page", perPage.toString());
         }
         if (null != shared) {
             urlBuilder.setParameter("shared", shared ? "1" : "0");
         }
         if (null != owned) {
             urlBuilder.setParameter("owned", owned ? "1" : "0");
         }
         if (null != physical) {
             urlBuilder.setParameter("physical", physical ? "1" : "0");
         }
         if (null != details) {
             urlBuilder.setParameter("details", details);
         }
         if (null != groupId) {
             urlBuilder.setParameter("group_id", groupId);
         }
         String url = urlBuilder.buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param serviceId
      * @param method
      */
     public void getServiceMethodDetails(RequestCallback callback, String sensorId,
             String serviceId, String method) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method httpMethod = RequestBuilder.GET;
         UrlBuilder urlBuilder = new UrlBuilder()
                 .setProtocol(Urls.PROTOCOL)
                 .setHost(Urls.HOST)
                 .setPath(
                         Urls.PATH_SERVICE.replace("%1", sensorId).replace("%2", serviceId) + "/"
                                 + method);
         String url = urlBuilder.buildString();
 
         // send request
         sendRequest(httpMethod, url, sessionId, null, callback);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param serviceId
      */
     public void getServiceMethods(RequestCallback callback, String sensorId, String serviceId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         UrlBuilder urlBuilder = new UrlBuilder()
                 .setProtocol(Urls.PROTOCOL)
                 .setHost(Urls.HOST)
                 .setPath(Urls.PATH_SERVICE_METHODS.replace("%1", sensorId).replace("%2", serviceId));
         String url = urlBuilder.buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * @return The session ID, or null if it was not set
      */
     public String getSessionId() {
         return sessionId;
     }
 
     /**
      * Tries to connect the current user account with his or her Google account
      */
     public void googleConnect() {
 
         // set the current location as the callback
         UrlBuilder callbackBuilder = new UrlBuilder();
         callbackBuilder.setProtocol(Location.getProtocol());
         callbackBuilder.setHost(Location.getHost());
         callbackBuilder.setPath(Location.getPath());
         for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
             if (entry.getKey().equals("session_id") || entry.getKey().equals("error")) {
                 // do not include these parameters
             } else {
                 callbackBuilder.setParameter(entry.getKey(), entry.getValue()
                         .toArray(new String[0]));
             }
         }
         String callbackUrl = callbackBuilder.buildString();
 
         // relocate to OpenID handler
         Location.replace(new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_LOGIN_GOOGLE).setParameter("callback_url", callbackUrl)
                 .setParameter("session_id", sessionId).buildString());
     }
 
     /**
      * Relocates the browser to the Google Open ID login page.
      */
     public void googleLogin() {
 
         // set the current location as the callback
         UrlBuilder callbackBuilder = new UrlBuilder();
         callbackBuilder.setProtocol(Location.getProtocol());
         callbackBuilder.setHost(Location.getHost());
         callbackBuilder.setPath(Location.getPath());
         for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
             if (entry.getKey().equals("session_id") || entry.getKey().equals("error")) {
                 // do not include these parameters
             } else {
                 callbackBuilder.setParameter(entry.getKey(), entry.getValue()
                         .toArray(new String[0]));
             }
         }
         String callbackUrl = callbackBuilder.buildString();
 
         // relocate to OpenID handler
         Location.replace(new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost("api.sense-os.nl")
                 .setPath(Urls.PATH_LOGIN_GOOGLE).setParameter("callback_url", callbackUrl)
                 .buildString());
     }
 
     /**
      * @param callback
      *            RequestCallback to handle HTTP response
      * @param username
      *            Username
      * @param password
      *            Password (unhashed)
      */
     public void login(RequestCallback callback, String username, String password) {
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_LOGIN);
         String url = urlBuilder.buildString();
 
         // prepare request data
         String hashedPass = Md5Hasher.hash(password);
         String requestData = "username=" + username + "&password=" + hashedPass;
 
         // send request
         sendRequest(method, url, null, requestData, callback);
     }
 
     /**
      * @param callback
      */
     public void logout(RequestCallback callback) {
 
         Method method = RequestBuilder.GET;
         String url = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_LOGOUT).buildString();
 
         // send request
         sendRequest(method, url, sessionId, null, callback);
     }
 
     /**
      * 
      * @param callback
      * @param password
      * @param token
      */
     public void resetPassword(RequestCallback callback, String password, String token) {
 
         // prepare request details
         Method method = RequestBuilder.POST;
         String url = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_PW_RESET).buildString();
 
         // prepare request data
         String hashedPass = Md5Hasher.hash(password);
         String data = "password=" + hashedPass + "&token=" + token;
 
         sendRequest(method, url, null, data, callback);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param serviceId
      * @param value
      * @param timestamp
      */
     public void setServiceManualData(RequestCallback callback, String sensorId, String serviceId,
             String value, long timestamp) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method httpMethod = RequestBuilder.POST;
         UrlBuilder urlBuilder = new UrlBuilder()
                 .setProtocol(Urls.PROTOCOL)
                 .setHost(Urls.HOST)
                 .setPath(
                         Urls.PATH_SERVICE.replace("%1", sensorId).replace("%2", serviceId)
                                 + "/manualData");
         String url = urlBuilder.buildString();
 
         // prepare request data
         String date = NumberFormat.getFormat("#.000").format(timestamp / 1000d);
         String data = "{\"data\":{\"date\":" + date + ",\"value\":\"" + value + "\"}}";
 
         // send request
         sendRequest(httpMethod, url, sessionId, data, callback);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param serviceId
      * @param method
      * @param parameters
      */
     public void setServiceMethodDetails(RequestCallback callback, String sensorId,
             String serviceId, String method, String... parameters) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method httpMethod = RequestBuilder.POST;
         UrlBuilder urlBuilder = new UrlBuilder()
                 .setProtocol(Urls.PROTOCOL)
                 .setHost(Urls.HOST)
                 .setPath(
                         Urls.PATH_SERVICE.replace("%1", sensorId).replace("%2", serviceId)
                                 + "/" + method);
         String url = urlBuilder.buildString();
         
         // prepare request data
         String data = "{\"parameters\":[";
         String params = "";
         for (String param : parameters) {
             params += JsonUtils.escapeValue(param) + ",";
         }
         if (params.length() > 1) {
             params = params.substring(0, params.length() - 1);
         }
         data += params + "]}";
 
         // send request
         sendRequest(httpMethod, url, sessionId, data, callback);
     }
 
     /**
      * @param sessionId
      *            The session ID to set
      */
     public void setSessionId(String sessionId) {
         this.sessionId = sessionId;
     }
 }
