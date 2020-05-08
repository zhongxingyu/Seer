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
 
 import org.apache.maven.artifact.Artifact;
 
 /**
  * Small interface that is used as an argument for the
 * {@link PackageMap#iterateDependencyArtifacts(org.apache.maven.plugin.logging.Log, java.util.Collection, Visitor, boolean)}
  * method.
  * 
  * <p>The method iterates through the dependencies and calls the interface's
  * two methods accordingly.</p>
  * 
  * @author Robert Schuster (robert.schuster@tarent.de)
  *
  */
 public interface Visitor
 {
   /**
    * If called it denotes that this is a normal dependency which will be
    * provided through the target system's package management.
    * 
    * @param artifact
    * @param entry
    */
   public void visit(Artifact artifact, Entry entry);
   
   /**
    *  If called it means that the dependency will be bundled along
    *  with the application.
    *  
    * @param artifact
    */
   public void bundle(Artifact artifact);
}
