 /*
  * Services
  * Copyright (C) 2012 John Pritchard
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as
  * published by the Free Software Foundation; either version 2 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301 USA.
  */
 package services;
 
 import java.io.Closeable;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.LogRecord;
 
 /**
  * A services resource reader that intializes classes.
  * 
  * <h3>Resource</h3>
  * 
  * <p> A service resource resides at the class path source location
  * 
  * <pre>
  * /META-INF/services/class.Name </pre>
  * 
  * or
  * 
  * <pre>
  * /WEB-INF/services/class.Name </pre>. </p>
  * 
  * <h3>Format</h3>
  * 
  * <p> A service resource lists class names in dot notation
  * (<code>Class.forName</code>), one per line. </p>
  * 
  * @author jdp
  */
 public class Classes
     extends lxl.ArrayList<Class>
 {
     public final static String NamePrefix_JAR = "META-INF/services/";
     public final static String NamePrefix_WAR = "WEB-INF/services/";
 
     protected final static Logger Log = Logger.getLogger(Classes.class.getName());
 
     public static lxl.List<String> Read(Class service)
         throws IOException
     {
         lxl.List<String> re = new lxl.ArrayList<String>();
         String name = service.getName();
         {
             Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(NamePrefix_JAR+name);
             while (resources.hasMoreElements()){
                 URL resource = resources.nextElement();
                 InputStream in = resource.openStream();
                 if (null != in){
                     Read(service,re,new BufferedReader(new InputStreamReader(in)));
                 }
             }
         }
         {
             Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(NamePrefix_WAR+name);
             while (resources.hasMoreElements()){
                 URL resource = resources.nextElement();
                 InputStream in = resource.openStream();
                 if (null != in){
                     Read(service,re,new BufferedReader(new InputStreamReader(in)));
                 }
             }
         }
         return re;
     }
     public static lxl.List<String> Read(Class service, lxl.List<String> re, BufferedReader in)
         throws IOException
     {
         try {
             String line;
             while (null != (line = in.readLine())){
                 int comment = line.indexOf('#');
                 if (-1 != comment)
                     line = line.substring(0,comment);
                 line = line.trim();
                 if (0 != line.length())
                     re.add(line);
             }
         }
         finally {
             in.close();
         }
         return re;
     }
     public static lxl.List<String> Read(java.io.File file)
         throws IOException
     {
         lxl.List<String> re = new lxl.ArrayList<String>();
         InputStream in = new java.io.FileInputStream(file);
         if (null != in){
             BufferedReader strin = new BufferedReader(new InputStreamReader(in));
             try {
                 String line;
                 while (null != (line = strin.readLine())){
                     int comment = line.indexOf('#');
                     if (-1 != comment)
                         line = line.substring(0,comment);
                     line = line.trim();
                     if (0 != line.length())
                         re.add(line);
                 }
             }
             finally {
                 in.close();
             }
         }
         return re;
     }
 
 
    private final lxl.List<String> names = new lxl.ArrayList();

     private final Class service;
 
     private final String name;
 
 
     public Classes(Class service){
         super();
         if (null != service){
             this.service = service;
             this.name = service.getName();
             try {
                 this.fill();
             }
             catch (IOException exc){
                 LogRecord rec = new LogRecord(Level.SEVERE,this.name);
                 rec.setThrown(exc);
                 Log.log(rec);
             }
         }
         else
             throw new IllegalArgumentException();
     }
 
 
     public final Class getService(){
         return this.service;
     }
     public final String getServiceName(){
         return this.service.getName();
     }
     public final String getName(){
         return this.name;
     }
    public final lxl.List<String> getNames(){
        return this.names;
    }
     /**
      * Called from constructor.
      */
     public void fill()
         throws IOException
     {
         this.clear();
         {
             Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(NamePrefix_JAR+this.name);
             while (resources.hasMoreElements()){
                 URL resource = resources.nextElement();
                 InputStream in = resource.openStream();
                 if (null != in){
                     this.fill( new BufferedReader(new InputStreamReader(in)));
                 }
             }
         }
         {
             Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(NamePrefix_WAR+this.name);
             while (resources.hasMoreElements()){
                 URL resource = resources.nextElement();
                 InputStream in = resource.openStream();
                 if (null != in){
                     this.fill( new BufferedReader(new InputStreamReader(in)));
                 }
             }
         }
     }
     protected void fill(BufferedReader in)
         throws IOException
     {
         try {
             String line;
             while (null != (line = in.readLine())){
                 int comment = line.indexOf('#');
                 if (-1 != comment)
                     line = line.substring(0,comment);
                 line = line.trim();
                 if (0 != line.length()){
                    this.names.add(line);
                     try {
                         Class clas = Class.forName(line);
                         if (!this.contains(clas)){
                             this.add(clas);
                         }
                     }
                     catch (ClassNotFoundException exc){
                         LogRecord rec = new LogRecord(Level.SEVERE,line);
                         rec.setThrown(exc);
                         Log.log(rec);
                     }
                 }
             }
         }
         finally {
             in.close();
         }
     }
     /**
      * Indempotent initializer will get called more than once, but
      * should only eval once.
      */
     public Classes init(){
         return this;
     }
 }
