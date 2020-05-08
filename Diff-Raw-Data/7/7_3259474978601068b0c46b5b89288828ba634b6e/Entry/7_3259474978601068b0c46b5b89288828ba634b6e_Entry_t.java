 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb, ipk, izpack)
  * Copyright (C) 2000-2008 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License,version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'Maven Packaging Plugin'
  * Signature of Elmar Geese, 11 March 2008
  * Elmar Geese, CEO tarent GmbH.
  */
 
   /**
  * 
  */
 package de.tarent.maven.plugins.pkg.map;
 
 import java.util.Set;
 
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.maven.artifact.versioning.VersionRange;
 
 /**
  * An <code>Entry<code> instance denotes a single mapping between a Maven2 artifact
  * and a package in the target distribution.
  * 
  * <p>It gives information on the package name, the Jar files which belong to it
  * and whether it should be part of the classpath or boot classpath.</p>
  * 
  * @author Robert Schuster (robert.schuster@tarent.de)
  *
  */
 public class Entry
 {
   public String artifactSpec;
   
   public String dependencyLine;
   
   public Set<String> jarFileNames;
   
   public boolean isBootClasspath;
   
   public VersionRange versionRange;
   
   public boolean bundleEntry;
   
   public boolean ignoreEntry;
 
   private Entry()
   {
     // For internal instances only.
   }
   
   static Entry createBundleEntry(String artifactSpec, VersionRange versionRange)
   {
 	  Entry e = new Entry();
 	  e.artifactSpec = artifactSpec;
 	  e.versionRange = versionRange;
 	  e.bundleEntry = true;
 	  
 	  return e; 
   }
   
   static Entry createIgnoreEntry(String artifactSpec, VersionRange versionRange)
   {
 	  Entry e = new Entry();
 	  e.artifactSpec = artifactSpec;
 	  e.versionRange = versionRange;
 	  e.ignoreEntry = true;
 	  
 	  return e; 
   }
 
   Entry(String artifactSpec, VersionRange versionRange, String packageName, Set<String> jarFileNames, boolean isBootClasspath)
   {
     this.artifactSpec = artifactSpec;
     this.versionRange = versionRange;
     this.dependencyLine = packageName;
     this.jarFileNames = jarFileNames;
     this.isBootClasspath = isBootClasspath;
   }
   
   public boolean equals(Object o)
   {
 	  if (o instanceof Entry)
 	  {
 		Entry that = (Entry) o;
		
		return ((this.artifactSpec == null && that.artifactSpec == null) || 
				(this.artifactSpec != null && that.artifactSpec != null && this.artifactSpec.equals(that.artifactSpec)))
			&& ((this.versionRange == null && that.versionRange == null) || 
					(this.versionRange !=null && that.versionRange !=null && this.versionRange.equals(that.versionRange)));
 	  }
 	  
 	  return false;
   }
   
   public int hashCode()
   {
 	  HashCodeBuilder hb = new HashCodeBuilder();
 	  hb.append(artifactSpec)
 	  // Workaround for Java5 compatibility:
 	  // On JDK5 the hashcode for two VersionRange instances
 	  // that have the same restriction (e.g. "[3.0,4.0)" is different.
 	  // That would break the merge operation in the Mapping class. 
 	  	.append((versionRange == null ? null : versionRange.toString()));
 	  
 	  return hb.toHashCode();
   }
   
   public String toString()
   {
 	  ToStringBuilder tsb = new ToStringBuilder(this);
 	  tsb.append("artifactSpec", artifactSpec)
 	  	.append("versionRange", versionRange)
 	  	.append("dependencyLine", dependencyLine)
 	  	.append("isBootClasspath", isBootClasspath)
 	  	.append("ignoreEntry", ignoreEntry)
 	  	.append("bundleEntry", bundleEntry)
 	  	.append("hashCode", hashCode());
 	  
 	  return tsb.toString();
   }
 }
