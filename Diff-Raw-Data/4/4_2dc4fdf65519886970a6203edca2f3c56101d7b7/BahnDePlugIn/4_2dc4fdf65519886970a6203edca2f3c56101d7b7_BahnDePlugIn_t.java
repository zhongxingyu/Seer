 package org.teleportr.plugin;
 
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Scanner;
 import java.util.regex.MatchResult;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import org.teleportr.R;
 import org.teleportr.model.Place;
 import org.teleportr.model.Ride;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.util.Log;
 
 public class BahnDePlugIn implements ITeleporterPlugIn {
     
     private static final String TAG = "PlugIn";
     private DefaultHttpClient client;
     private ArrayList<Ride> rides;
 
     public BahnDePlugIn() {
         rides = new ArrayList<Ride>();
         client = new DefaultHttpClient();
     }
 
     /* (non-Javadoc)
      * @see org.teleportr.ITeleporterPlugIn#find(org.teleportr.Place, org.teleportr.Place, java.util.Date)
      */
     public ArrayList<Ride> find(Place orig, Place dest, Date time) {
         
         StringBuilder url = new StringBuilder();
         url.append("http://mobile.bahn.de/bin/mobil/query.exe/dox?");
         url.append("n=1");
         
         if (orig.address != null)
         	url.append("&f=2&s=").append(URLEncoder.encode(orig.address+", "+orig.city+"!"));
         else
         	url.append("&f=1&s=").append(URLEncoder.encode(orig.name+", "+orig.city+"!"));
         
         if (dest.address != null)
         	url.append("&o=2&z=").append(URLEncoder.encode(dest.address+", "+dest.city+"!"));
         else
         	url.append("&o=1&z=").append(URLEncoder.encode(dest.name+", "+dest.city+"!"));
 
         url.append("&d="); // date
         url.append((new SimpleDateFormat("ddMMyy")).format(time));
         url.append("&t="); // time
         url.append((new SimpleDateFormat("HHmm")).format(time));
         url.append("&start=Suchen");
 //        Log.d(TAG, "url: "+url.toString());
         
         // fetch
         try {
             Ride r;
             MatchResult m;
             rides.clear();
             Scanner scanner = new Scanner(client.execute(new HttpGet(url.toString())).getEntity().getContent(), "iso-8859-1");
         Log.d(TAG, "scanned url: "+url.toString());
             while (scanner.findWithinHorizon("<a href=\"([^\"]*)\">(\\d\\d):(\\d\\d)<br />(\\d\\d):(\\d\\d)", 10000) != null) {
                 m = scanner.match();
                 Log.d(TAG, "found :) "+m);
                 Date dep = parseDate(m.group(2), m.group(3));
                 if (dep.getTime() - time.getTime() > 100000) {
                     r = new Ride();
                     r.orig = orig;
                     r.dest = dest;
                     r.mode = Ride.MODE_TRANSIT;
                     r.dep = dep;
                     r.arr = parseDate(m.group(4), m.group(5));
                     r.price = 240;
                     r.fun = 3;
                     r.eco = 3;
                     r.fast = 1;
                     r.social = 2;
                     r.green = 4;
                     String uriString = m.group(1).replace("&amp;", "&");
                     Log.d(TAG, "uri: "+uriString);
                     r.intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                     
                     rides.add(r);
                 }
             }
         } catch (Exception e) {
             Log.e(TAG, "Mist!");
             e.printStackTrace();
         }
         return rides;
     }
 
 
     private Date parseDate(String hours, String minutes) {
         Date date = new Date();
         date.setHours(Integer.parseInt(hours));
         date.setMinutes(Integer.parseInt(minutes));
         date.setSeconds(0);
        //date.setTime((date.getTime() / 1000) * 1000);
        if (System.currentTimeMillis() - date.getTime() > 300000) { // Mitternacht..
             long oneDay = (long) 1000.0 * 60 * 60 * 24;
             date.setTime(date.getTime() + oneDay);
         }
         return date;
     }
 
 }
