 /**
  * Copyright 2013 MicMun
  *
  * This program is free software: you can redistribute it and/or modify it under 
  * the terms of the GNU >General Public License as published by the 
  * Free Software Foundation, either version 3 of the License, or >
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; >without even the implied warranty of MERCHANTABILITY 
  * or FITNESS FOR A PARTICULAR PURPOSE. >See the GNU General Public License 
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along with 
  * this program. If not, see >http://www.gnu.org/licenses/.
  */
 package de.micmun.android.miwotreff.utils;
 
 import android.util.Log;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 
 /**
  * Parser for the html-table of the program.
  *
  * @author Michael Munzert
  * @version 2.0, 18.01.2013
  */
 public class HtmlParser {
    private static final Locale DEFAULT = Locale.getDefault();
    private final String TABLE_START = "<table class='contenttable'>";
    private final String TABLE_END = "</table>";
    private final String TAG = "MiWoTreff.HtmlParser";
 
    /**
     * Creates a new HtmlParser.
     */
    public HtmlParser() {
    }
 
    /**
     * Returns the table as a String.
     *
     * @param u URL of the html-Page.
     * @return Table with program.
     */
    public String getHtmlFromUrl(String u) {
       String line = null;
 
       try {
          URL url = new URL(u);
          HttpURLConnection con = (HttpURLConnection) url.openConnection();
          con.setUseCaches(false);
          con.setRequestMethod("GET");
          con.connect();
          BufferedReader in = new BufferedReader
                (new InputStreamReader(con.getInputStream(),
                      Charset.forName("iso-8859-1")));
 
 
          while ((line = in.readLine()) != null) {
             if (line.contains(TABLE_START)) {
                break;
             }
          }
       } catch (MalformedURLException e) {
          Log.e(TAG, e.getLocalizedMessage());
       } catch (IOException e) {
          Log.e(TAG, "ERROR: " + e.getLocalizedMessage());
       }
 
       return line;
    }
 
    /**
     * Parses the table and returns the list of rows (Maps).
     *
     * @param t table in html.
     * @return List of Maps (every row a map).
     */
    public ArrayList<HashMap<String, Object>> getProg(String t) {
       ArrayList<HashMap<String, Object>> prog =
             new ArrayList<HashMap<String, Object>>(50);
 
       int start = t.indexOf(TABLE_START);
       start += TABLE_START.length();
       int end = t.indexOf(TABLE_END, start);
       String s = t.substring(start, end);
       s = s.replace("&nbsp;", " ");
 
       String[] rows = s.split("<tr>");
 
       for (int i = 1; i < rows.length; ++i) {
          HashMap<String, Object> map = new HashMap<String, Object>();
          String[] cols = rows[i].split("<td>");
          String datum = cols[1].replace("</td>", "");
          start = datum.indexOf(' ') + 1;
          datum = datum.substring(start).trim();
          SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", DEFAULT);
          Date d;
          try {
             String[] split = datum.split("\\.");
             if (split.length == 2) {
                datum = String.format("%02d.%02d.%04d",
                      Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                      Calendar.getInstance().get(Calendar.YEAR));
 
             } else if (split.length == 3) {
                datum = String.format("%02d.%02d.%04d",
                      Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                     Integer.parseInt(split[2]));
             }
             d = sdf.parse(datum);
          } catch (ParseException e) {
             Log.e(TAG, e.getLocalizedMessage());
             return null;
          }
          String thema = cols[2].replace("</span>", "");
          thema = thema.replace("<br />", "\n");
          start = thema.indexOf('>') + 1;
          end = thema.indexOf("</td>", start);
          thema = thema.substring(start, end).trim();
 
          String person = cols[3].replace("</td></tr>", "").trim();
 
          map.put(DBConstants.KEY_DATUM, d);
          map.put(DBConstants.KEY_THEMA, thema);
          map.put(DBConstants.KEY_PERSON, person);
          prog.add(map);
       }
 
       return prog;
    }
 }
