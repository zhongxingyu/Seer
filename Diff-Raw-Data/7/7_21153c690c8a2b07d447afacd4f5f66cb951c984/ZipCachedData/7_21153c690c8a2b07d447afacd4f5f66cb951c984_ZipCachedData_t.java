 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.timezones.common;
 
 import org.bedework.timezones.common.Differ.DiffListEntry;
 
 import edu.rpi.cmt.calendar.XcalUtil;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 /** Cached data affected by the source data.
  *
  * @author douglm
  */
 public class ZipCachedData  extends AbstractCachedData {
   private String tzdataUrl;
 
   private ZipFile tzDefsZipFile;
 
   private File tzDefsFile;
 
   /**
    * @param tzdataUrl
    * @throws TzException
    */
   public ZipCachedData(final String tzdataUrl) throws TzException {
     super("Zip");
     this.tzdataUrl = tzdataUrl;
     loadData();
   }
 
   @Override
   public void stop() throws TzException {
   }
 
   @Override
   public void setPrimaryUrl(final String val) throws TzException {
     // Ignore
   }
 
   @Override
   public String getPrimaryUrl() throws TzException {
     return TzServerUtil.getInitialPrimaryUrl();
   }
 
   @Override
   public void setPrimaryServer(final boolean val) throws TzException {
     // Ignore
   }
 
   @Override
   public boolean getPrimaryServer() throws TzException {
     return TzServerUtil.getInitialPrimaryServer();
   }
 
   @Override
   public void setRefreshInterval(final long val) throws TzException {
     // Ignore
   }
 
   @Override
   public long getRefreshInterval() throws TzException {
     return TzServerUtil.getInitialRefreshInterval();
   }
 
   @Override
   public void checkData() throws TzException {
     loadData();
   }
 
   @Override
   public void updateData(final String dtstamp,
                          final List<DiffListEntry> dles) throws TzException {
     // XXX ??
   }
 
   @Override
   public List<String> findIds(final String val) throws TzException {
     List<String> ids = new ArrayList<String>();
 
     return ids;
   }
 
   private synchronized void loadData() throws TzException {
     try {
       long smillis = System.currentTimeMillis();
 
       /* ======================== First get the data file =================== */
       File f = getdata();
 
       /* ============================ open a zip file ======================= */
       ZipFile zf = new ZipFile(f);
 
       if (tzDefsZipFile != null) {
         try {
           tzDefsZipFile.close();
         } catch (Throwable t) {
         }
       }
 
       if (tzDefsFile != null) {
         try {
           tzDefsFile.delete();
         } catch (Throwable t) {
         }
       }
 
       tzDefsFile = f;
       tzDefsZipFile = zf;
 
       TzServerUtil.lastDataFetch = System.currentTimeMillis();
 
       /* ========================= get the data info ======================== */
 
       ZipEntry ze = tzDefsZipFile.getEntry("info.txt");
 
       String info = entryToString(ze);
 
       String[] infoLines = info.split("\n");
 
       for (String s: infoLines) {
         if (s.startsWith("buildTime=")) {
          String bt = s.substring("buildTime=".length());
          if (!bt.endsWith("Z")) {
            // Pretend it's UTC
            bt += "Z";
          }
          dtstamp = XcalUtil.getXmlFormatDateTime(bt);
         }
       }
 
       /* ===================== Rebuild the alias maps ======================= */
 
       aliasMaps = buildAliasMaps(tzDefsZipFile);
 
       /* ===================== All tzs into the table ======================= */
 
       unzipTzs(tzDefsZipFile, dtstamp);
       expansions.clear();
 
       TzServerUtil.reloadsMillis += System.currentTimeMillis() - smillis;
       TzServerUtil.reloads++;
     } catch (Throwable t) {
       throw new TzException(t);
     }
   }
 
   private AliasMaps buildAliasMaps(final ZipFile tzDefsZipFile) throws TzException {
     try {
       ZipEntry ze = tzDefsZipFile.getEntry("aliases.txt");
 
       AliasMaps maps = new AliasMaps();
       maps.aliasesStr = entryToString(ze);
 
       maps.byTzid = new HashMap<String, SortedSet<String>>();
       maps.byAlias = new HashMap<String, String>();
       maps.aliases = new Properties();
 
       StringReader sr = new StringReader(maps.aliasesStr);
 
       maps.aliases.load(sr);
 
       for (String a: maps.aliases.stringPropertyNames()) {
         String id = maps.aliases.getProperty(a);
 
         maps.byAlias.put(a, id);
 
         SortedSet<String> as = maps.byTzid.get(id);
 
         if (as == null) {
           as = new TreeSet<String>();
           maps.byTzid.put(id, as);
         }
 
         as.add(a);
       }
 
       return maps;
     } catch (Throwable t) {
       throw new TzException(t);
     }
   }
 
   private void unzipTzs(final ZipFile tzDefsZipFile,
                         final String dtstamp) throws TzException {
     try {
       resetTzs();
 
       Enumeration<? extends ZipEntry> zes = tzDefsZipFile.entries();
 
       while (zes.hasMoreElements()) {
         ZipEntry ze = zes.nextElement();
 
         if (ze.isDirectory()) {
           continue;
         }
 
         String n = ze.getName();
 
         if (!(n.startsWith("zoneinfo/") && n.endsWith(".ics"))) {
           continue;
         }
 
         String id = n.substring(9, n.length() - 4);
 
         processSpec(id, entryToString(ze), null);
       }
     } catch (Throwable t) {
       throw new TzException(t);
     }
   }
 
   /** Retrieve the data and store in a temp file. Return the file object.
    *
    * @return File
    * @throws TzException
    */
   private File getdata() throws TzException {
     try {
       String dataUrl = tzdataUrl;
       if (dataUrl == null) {
         throw new TzException("No data url defined");
       }
 
       if (!dataUrl.startsWith("http:")) {
         return new File(dataUrl);
       }
 
       /* Fetch the data */
       HttpClient client = new DefaultHttpClient();
 
       HttpRequestBase get = new HttpGet(dataUrl);
 
       HttpResponse resp = client.execute(get);
 
       InputStream is = null;
       FileOutputStream fos = null;
 
       try {
         is = resp.getEntity().getContent();
 
         File f = File.createTempFile("bwtzserver", "zip");
 
         fos = new FileOutputStream(f);
 
         byte[] buff = new byte[4096];
 
         for (;;) {
           int num = is.read(buff);
 
           if (num < 0) {
             break;
           }
 
           if (num > 0) {
             fos.write(buff, 0, num);
           }
         }
 
         return f;
       } finally {
         try {
           fos.close();
         } finally {}
 
         try {
           is.close();
         } finally {}
       }
     } catch (Throwable t) {
       throw new TzException(t);
     }
   }
 
   private String entryToString(final ZipEntry ze) throws Throwable {
     InputStreamReader is = new InputStreamReader(tzDefsZipFile.getInputStream(ze),
                                                  "UTF-8");
 
     StringWriter sw = new StringWriter();
 
     char[] buff = new char[4096];
 
     for (;;) {
       int num = is.read(buff);
 
       if (num < 0) {
         break;
       }
 
       if (num > 0) {
         sw.write(buff, 0, num);
       }
     }
 
     is.close();
 
     return sw.toString();
   }
 }
