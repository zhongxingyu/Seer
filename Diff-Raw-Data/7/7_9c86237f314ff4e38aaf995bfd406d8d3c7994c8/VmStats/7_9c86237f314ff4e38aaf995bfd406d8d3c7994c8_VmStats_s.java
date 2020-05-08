 package org.mock.extension.resource;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONObject;
 import org.mock.resource.ExtendedResource;
 
 public class VmStats implements ExtendedResource {
     public Response process(UriInfo uriInfo) throws Exception {
         if (uriInfo.getQueryParameters().get("target") == null)
             throw new Exception("target is null");
         if (uriInfo.getQueryParameters().get("from") == null)
             throw new Exception("from is null");
         if (uriInfo.getQueryParameters().get("to") == null)
             throw new Exception("to is null");
         if (uriInfo.getQueryParameters().get("samples") == null)
             throw new Exception("samples is null");
        String targets = uriInfo.getQueryParameters().get("target").get(0);
        DateFormat df = new SimpleDateFormat("yyyy-MM-DD");
        Date from = df.parse(uriInfo.getQueryParameters().get("from").get(0));
        Date to = df.parse(uriInfo.getQueryParameters().get("to").get(0));
         Integer samples = Integer.getInteger(uriInfo.getQueryParameters()
                 .get("samples").get(0));
         if (samples == null) {
             samples = 5;
         }
 
         JSONArray statsArray = new JSONArray();
         for (String t : targets.split(",")) {
             JSONObject obj = new JSONObject();
             obj.put("target", t);
             obj.put("stats", new JSONArray(findStat(t, from, to, samples)));
             obj.put("dates", new JSONArray(findPoints(from, to, samples)));
             statsArray.put(obj);
         }
         return Response.ok(statsArray.toString()).build();
     }
 
     public List<Integer> findStat(String target, Date from, Date to, int samples) {
         // TODO : read real stats instead of random generated values
         Random r = new Random();
         List<Integer> stats = new ArrayList<Integer>();
         for (int i = 0; i < samples; i++) {
             stats.add(r.nextInt(100));
         }
         return stats;
     }
 
     public List<String> findPoints(Date from, Date to, int samples) {
         long diff = to.getTime() - from.getTime();
         double dt = diff * 1.0 / samples;
         long d0 = from.getTime();
 
         List<String> points = new ArrayList<String>();
         for (int i = 0; i < samples; i++) {
             points.add(String.valueOf((long) (d0 + i * dt)));
         }
         return points;
     }
 }
