 package edu.cmu.tourout;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.Filter;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.gson.Gson;
 
 @SuppressWarnings("serial")
 public class GetToursServlet extends HttpServlet {
 
     private static final double D2R = Math.PI / 180;
 
     private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
     public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws IOException {
 
        double userLatitude = Double.parseDouble(request.getParameter("latitude"));
         double userLongitude = Double.parseDouble(request.getParameter("longitude"));
 
         Query query = new Query("Tour");
         Filter isFinishedFilter = new FilterPredicate("isFinished", FilterOperator.EQUAL, false);
         query.setFilter(isFinishedFilter);
         PreparedQuery pq = datastore.prepare(query);
         Iterator<Entity> entities = pq.asIterator();
         List<Entity> results = new ArrayList<Entity>();
         while (entities.hasNext()) {
             Entity entity = entities.next();
             String coordinate = entity.getProperty("coordinates").toString();
            double placeLatitude = Double.parseDouble(coordinate.split(",")[0]);
             double placeLongitude = Double.parseDouble(coordinate.split(",")[1]);
            double miles = distanceInMiles(userLatitude, userLongitude, placeLatitude, placeLongitude);
             if (miles < 1000) {
                 results.add(entity);
             }
         }
         response.setContentType("text/json");
         Gson gson = new Gson();
         response.getWriter().println(gson.toJson(results));
     }
 
     double distanceInMiles(double lat1, double long1, double lat2, double long2) {
         double dlong = (long2 - long1) * D2R;
         double dlat = (lat2 - lat1) * D2R;
         double a = Math.pow(Math.sin(dlat / 2.0), 2) + Math.cos(lat1 * D2R) * Math.cos(lat2 * D2R)
                 * Math.pow(Math.sin(dlong / 2.0), 2);
         double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
         double d = 3956 * c;
 
         return d;
     }
 
 }
