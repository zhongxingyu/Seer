 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.deployers.plugins.structure.vfs.file;
 
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.jboss.deployers.plugins.structure.vfs.AbstractStructureDeployer;
 import org.jboss.deployers.spi.structure.DeploymentContext;
 import org.jboss.virtual.VirtualFile;
 
 /**
  * WARStructure.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class FileStructure extends AbstractStructureDeployer
 {
    /** The file suffixes */
    private static Set<String> fileSuffixes = new CopyOnWriteArraySet<String>();
 
    // Initialise known suffixes
    static
    {
       fileSuffixes.add("-service.xml");
       fileSuffixes.add("-beans.xml");
       fileSuffixes.add("-ds.xml");
    }
 
    /**
    * Set the suffixes that are recognised as files
     * 
    * @param suffixes A list of suffixes, e.g. {"-service.xml", "-ds.xml"}
     */
    public void setSuffixes(Set<String> suffixes)
    {
       if (suffixes != null)
       {
          fileSuffixes.clear();
          for (String suffix : suffixes)
          {
             addFileSuffix(suffix);
          }
       }
    }
    
    /**
    * Gets the list of suffixes recognised as files
     * 
     * @return the list of suffixes
     */
    public Set<String> getSuffixes()
    {
       return fileSuffixes;      
    }
    
    
    /**
     * Add a file suffix
     * 
     * @param suffix the suffix
     * @return true when added
     * @throws IllegalArgumentException for a null suffix
     */
    public static boolean addFileSuffix(String suffix)
    {
       if (suffix == null)
          throw new IllegalArgumentException("Null suffix");
       return fileSuffixes.add(suffix);
    }
 
    /**
     * Remove a file suffix
     * 
     * @param suffix the suffix
     * @return true when removed
     * @throws IllegalArgumentException for a null suffix
     */
    public static boolean removeFileSuffix(String suffix)
    {
       if (suffix == null)
          throw new IllegalArgumentException("Null suffix");
       return fileSuffixes.remove(suffix);
    }
    
    /**
     * Whether this is an archive
     *
     * @param name the name
     * @return true when an archive
     * @throws IllegalArgumentException for a null name
     */
    public static boolean isKnownFile(String name)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
       
       int index = name.lastIndexOf('-');
       if (index == -1)
          return false;
       String suffix = name.substring(index);
       return fileSuffixes.contains(suffix);
    }
 
    public boolean determineStructure(DeploymentContext context)
    {
       try
       {
          VirtualFile root = context.getRoot();
          if (root.isLeaf())
          {
             // It must be top level
             if (context.isTopLevel() == false)
             {
                if (isKnownFile(root.getName()) == false)
                {
                   log.trace("... no - it is not a top level file and not a known name");
                   return false;
                }
                else
                {
                   log.trace("... ok - not a top level file but it is a known name");
                }
             }
             else
             {
                log.trace("... ok - it is a top level file");
             }
 
             // There are no subdeployments for files
             return true;
          }
          else
          {
             log.trace("... no - not a file.");
             return false;
          }
       }
       catch (Exception e)
       {
          log.warn("Error determining structure: " + context.getName(), e);
          return false;
       }
    }
 }
