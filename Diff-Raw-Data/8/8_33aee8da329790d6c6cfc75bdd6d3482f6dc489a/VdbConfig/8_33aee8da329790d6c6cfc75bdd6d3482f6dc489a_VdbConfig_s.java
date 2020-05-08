 /*
  * Copyright (c) 2008-2012 Vrije Universiteit, The Netherlands All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * Neither the name of the Vrije Universiteit nor the names of its contributors
  * may be used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package interdroid.vdb.content;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.content.Context;
 
 /**
  * A class to represent the configuration for a repository which is
  * stored in an XML file.
  *
  * This file support ORM type repositories and other system internals.
  *
  * @author nick &lt;palmer@cs.vu.nl&gt;
  *
  */
 public class VdbConfig {
 
     /**
      * The name of the configuration file.
      */
     private static final String CONFIG_XML = "vdbconfig.xml";
 
     /**
      * The list of repositories.
      */
     private final List<RepositoryConf> repositories =
             new ArrayList<RepositoryConf>();
 
     /**
      * @return the list of repositories.
      */
     public final List<RepositoryConf> getRepositories() {
         return repositories;
     }
 
     /**
      * A class to represent the information for a repository.
      * This class supports both ORM and Avro based repositories.
      *
      * @author nick &lt;palmer@cs.vu.nl&gt;
      *
      */
     public static class RepositoryConf {
         /**
          * The name of the repository.
          */
         private String mName;
         /**
          * The content provider class name for this repository.
          */
         private String mContentProvider;
         /**
          * The avro schema for the repository if there is one.
          */
         private String mAvroSchema = null;
 
         /**
          * Can only be constructed with no arguments here.
          */
         private RepositoryConf() {
             // Nothing to do. Will be initted in the parser.
         }
 
         public String toString() {
            return "Name: " + mName + "Provider: " + mContentProvider;
         }
 
         /**
          * Construct with a name contentProvider and schema.
          * @param name the name of the repo
          * @param contentProvider the content provider for the repo
          * @param avroSchema the avro schema for the repo
          */
         public RepositoryConf(final String name, final String contentProvider,
                 final String avroSchema) {
             mName = name;
             mContentProvider = contentProvider;
             mAvroSchema = avroSchema;
         }
 
         /**
          * Construct without a known content provider.
          * @param name the name of the repository
          * @param avroSchema the schema for the repository
          */
         public RepositoryConf(final String name, final String avroSchema) {
             mName = name;
             mAvroSchema = avroSchema;
         }
 
         /**
          * Parses a snipit of XML for an ORM repository.
          *
          * @param xpp the parser
          * @return the configuration
          * @throws XmlPullParserException if the parse fails
          * @throws IOException if IO fails
          */
         public static RepositoryConf parseFromStartTag(final XmlPullParser xpp)
                 throws XmlPullParserException, IOException {
             RepositoryConf obj = new RepositoryConf();
 
             obj.mName = xpp.getAttributeValue(/* namespace */ null, "name");
             obj.mContentProvider = xpp.getAttributeValue(/* namespace */ null,
                     "contentProvider");
             if (obj.getName() == null || obj.getContentProvider() == null) {
                 throw new XmlPullParserException("Missing mandatory attributes"
                         + " for repository.");
             }
             if (xpp.next() != XmlPullParser.END_TAG) {
                 throw new XmlPullParserException(
                         "Expected end tag for Repository."
                        + "Found " + xpp.getEventType());
             }
             return obj;
                }
 
         /**
          * @return the Name
          */
         public final String getName() {
             return mName;
         }
 
         /**
          * @return the ContentProvider
          */
         public final String getContentProvider() {
             return mContentProvider;
         }
 
         /**
          * @return the Avro Schema
          */
         public final String getAvroSchema() {
             return mAvroSchema;
         }
 
     }
 
     /**
      * Constructs a configuration for the given context, reading the
      * xml configuration for ORM repositories.
      *
      * @param ctx the context to work in.
      */
     public VdbConfig(final Context ctx) {
         try {
             XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
             factory.setNamespaceAware(false);
             XmlPullParser xpp = factory.newPullParser();
             xpp.setInput(ctx.getAssets().open(CONFIG_XML), null);
 
             int eventType = xpp.getEventType();
             /* skip root element */
             while (xpp.getEventType() != XmlPullParser.START_TAG) {
                 eventType = xpp.next();
             }
             eventType = xpp.next();
 
             while (eventType != XmlPullParser.END_DOCUMENT) {
                 switch(eventType) {
                 case XmlPullParser.START_TAG:
                     if ("repository".equals(xpp.getName())) {
                         repositories.add(
                                 RepositoryConf.parseFromStartTag(xpp));
                     } else {
                         throw new XmlPullParserException(
                                 "Unexpected element type: "
                                         + xpp.getName());
                     }
                 default:
                     // ignored
                 }
                 eventType = xpp.next();
             }
         } catch (IOException e) {
             throw new RuntimeException("Cannot open " + CONFIG_XML, e);
         } catch (XmlPullParserException e) {
             throw new RuntimeException("Could not parse " + CONFIG_XML, e);
         }
     }
 }
