 /*
  * JAHM - Java Advanced Hierarchical Model 
  * 
  * TxtAssociation.java
  * 
  * Copyright 2009 Robert Arvin Dunnagan
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.xmodel.external.caching;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.xmodel.IModelObject;
 import org.xmodel.ModelObject;
 import org.xmodel.external.CachingException;
 
 /**
  * An IFileAssociation for comma-separator value text files with the .csv extension. These
  * files are parsed into "entry" elements with one attribute for each field. The attributes
  * are enumerated "f1" through "fN", where N is the number of fields.
  */
 public class CsvAssociation implements IFileAssociation
 {
   /* (non-Javadoc)
    * @see org.xmodel.external.caching.IFileAssociation#getAssociations()
    */
   public String[] getExtensions()
   {
     return extensions;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.external.caching.IFileAssociation#apply(org.xmodel.IModelObject, java.lang.String, java.io.InputStream)
    */
   public void apply( IModelObject parent, String name, InputStream stream) throws CachingException
   {
     try
     {
       BufferedReader reader = new BufferedReader( new InputStreamReader( stream));
       int lnum = 1;
       while( reader.ready())
       {
         String line = reader.readLine();
         
         IModelObject object = new ModelObject( "entry", Integer.toString( lnum++));
         parseFields( line, object);
         
         parent.addChild( object);
       }
     }
     catch( Exception e)
     {
       throw new CachingException( "Unable read text file: "+name, e);
     }
   }
   
   /**
    * Parse the fields in the specified line and add them to the specified parent.
    * @param line The line.
    * @param parent The parent.
    */
   private void parseFields( String line, IModelObject parent)
   {
     boolean quoting = false;
     IModelObject child = new ModelObject( "field");
     int index = 0;
     for( int i=0; i<line.length(); i++)
     {
       if ( !quoting && line.charAt( i) == ',')
       {
        if ( index < i) child.setValue( line.substring( index, i));
         parent.addChild( child);
         child = new ModelObject( "field");
        index = i+1;
       }
       else if ( line.charAt( i) == '\"')
       {
         quoting = !quoting;
       }
     }
     
     if ( index != line.length() - 1)
     {
       child.setValue( line.substring( index));
       parent.addChild( child);
     }
   }
   
   private final static String[] extensions = { ".csv"};
 }
