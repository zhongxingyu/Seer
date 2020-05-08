 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
 package org.infoglue.cms.util.workflow;
 
 import com.opensymphony.workflow.FactoryException;
 import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
 import com.opensymphony.workflow.loader.AbstractWorkflowFactory;
 import com.opensymphony.workflow.loader.WorkflowDescriptor;
 import com.opensymphony.workflow.loader.WorkflowLoader;
 
 import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowDefinitionController;
 import org.infoglue.cms.entities.management.RepositoryVO;
 import org.infoglue.cms.entities.workflow.WorkflowDefinitionVO;
 import org.infoglue.deliver.util.CacheController;
 
 import java.io.*;
 
 import java.util.*;
 
 
 /**
  * @author Mattias Bogeblad
  */
 
 public class InfoGlueWorkflowFactory extends AbstractWorkflowFactory
 {
     //protected Map workflows;
     protected boolean reload;
 
     public void setLayout(String workflowName, Object layout) 
     {
     }
 
     public Object getLayout(String workflowName) 
     {
         return null;
     }
 
     public boolean removeWorkflow(String name) throws FactoryException 
     {
         throw new FactoryException("remove workflow not supported");
     }
 
     public void renameWorkflow(String oldName, String newName) 
     {
     }
 
     public void save() 
     {
     }
 
     public boolean isModifiable(String name) 
     {
         return true;
     }
 
     public String getName() 
     {
         return "";
     }
 
     public WorkflowDescriptor getWorkflow(String name, boolean validate) throws FactoryException 
     {
         Map workflows = (Map)CacheController.getCachedObject("workflowCache", "workflowMap");
 		
         if(workflows == null)
             initDone(); 
 
         workflows = (Map)CacheController.getCachedObject("workflowCache", "workflowMap");
 
         WorkflowConfig c = (WorkflowConfig) workflows.get(name);
 
         if (c == null) {
             throw new FactoryException("Unknown workflow name \"" + name + "\"");
         }
 
         if (c.descriptor != null) 
         {
             loadWorkflow(c, validate);
         } 
         else 
         {
             loadWorkflow(c, validate);
         }
 
         c.descriptor.setName(name);
 
         return c.descriptor;
     }
 
     public void reload() throws FactoryException
     {
         initDone();   
     }
 
     public String[] getWorkflowNames() 
     {
         Map workflows = (Map)CacheController.getCachedObject("workflowCache", "workflowMap");
 		
         if(workflows == null)
         {
             try
             {
                 initDone();
             } 
         	catch (FactoryException e)
             {
                 e.printStackTrace();
             } 
         }
 
         workflows = (Map)CacheController.getCachedObject("workflowCache", "workflowMap");
 
         int i = 0;
         String[] res = new String[workflows.keySet().size()];
         Iterator it = workflows.keySet().iterator();
 
         while (it.hasNext()) 
         {
             res[i++] = (String) it.next();
         }
 
         return res;
     }
 
     public void createWorkflow(String name) 
     {
         try
         {
             initDone();
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
     }
 
     public void initDone() throws FactoryException 
     {
         try
         {
             Map workflows = new HashMap();
             
             List list = WorkflowDefinitionController.getController().getWorkflowDefinitionVOList();
 
             Iterator listIterator = list.iterator();
             while(listIterator.hasNext())
             {
                 WorkflowDefinitionVO workflowDefinitionVO = (WorkflowDefinitionVO)listIterator.next();
                 WorkflowConfig config = new WorkflowConfig(workflowDefinitionVO);
                 workflows.put(workflowDefinitionVO.getName(), config);
             }
 
             CacheController.cacheObject("workflowCache", "workflowMap", workflows);
         }
         catch (Exception e) 
         {
             throw new InvalidWorkflowDescriptorException("Error in workflow config", e);
         }
         
         /*
         reload = getProperties().getProperty("reload", "false").equals("true");
 
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         InputStream is = null;
         String name = getProperties().getProperty("resource", "workflows.xml");
 
         if ((name != null) && (name.indexOf(":/") > -1)) {
             try {
                 is = new URL(name).openStream();
             } catch (Exception e) {
             }
         }
 
         if (is == null) {
             try {
                 is = classLoader.getResourceAsStream(name);
             } catch (Exception e) {
             }
         }
 
         if (is == null) {
             try {
                 is = classLoader.getResourceAsStream("/" + name);
             } catch (Exception e) {
             }
         }
 
         if (is == null) {
             try {
                 is = classLoader.getResourceAsStream("META-INF/" + name);
             } catch (Exception e) {
             }
         }
 
         if (is == null) {
             try {
                 is = classLoader.getResourceAsStream("/META-INF/" + name);
             } catch (Exception e) {
             }
         }
 
         if (is == null) {
             throw new FactoryException("Unable to find workflows file '" + name + "' in classpath");
         }
 
         try {
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             dbf.setNamespaceAware(true);
 
             DocumentBuilder db;
 
             try {
                 db = dbf.newDocumentBuilder();
             } catch (ParserConfigurationException e) {
                 throw new FactoryException("Error creating document builder", e);
             }
 
             Document doc = db.parse(is);
 
             Element root = (Element) doc.getElementsByTagName("workflows").item(0);
             workflows = new HashMap();
 
             List list = XMLUtil.getChildElements(root, "workflow");
 
             for (int i = 0; i < list.size(); i++) {
                 Element e = (Element) list.get(i);
                 WorkflowConfig config = new WorkflowConfig(e.getAttribute("type"), e.getAttribute("location"));
                 workflows.put(e.getAttribute("name"), config);
             }
         } catch (Exception e) {
             throw new InvalidWorkflowDescriptorException("Error in workflow config", e);
         }
         */
     }
 
 
     public boolean saveWorkflow(String name, WorkflowDescriptor descriptor, boolean replace) throws FactoryException 
     {
         throw new FactoryException("Not supported...");
     }
 
 
     private void loadWorkflow(WorkflowConfig c, boolean validate) throws FactoryException 
     {
         try 
         {
             //System.out.println("c.workflowDefinitionVO.getValue():\n" + c.workflowDefinitionVO.getValue());
            //c.descriptor = WorkflowLoader.load(new ByteArrayInputStream(c.workflowDefinitionVO.getValue().getBytes("ISO-8859-1")) , validate);
            c.descriptor = WorkflowLoader.load(new ByteArrayInputStream(c.workflowDefinitionVO.getValue().getBytes("UTF-8")) , validate);
         } 
         catch (Exception e) 
         {
             throw new FactoryException("Error in workflow descriptor: " + c.workflowDefinitionVO.getName(), e);
         }
     }
 
     //~ Inner Classes //////////////////////////////////////////////////////////
 
     static class WorkflowConfig 
     {
         WorkflowDescriptor descriptor;
         WorkflowDefinitionVO workflowDefinitionVO;
 
         public WorkflowConfig(WorkflowDefinitionVO workflowDefinitionVO) 
         {
             this.workflowDefinitionVO = workflowDefinitionVO;
         }
     }
 
 /*    
     public WorkflowDescriptor getWorkflow(String name) throws FactoryException 
     {
         WorkflowConfig c = (WorkflowConfig) workflows.get(name);
 
         if (c == null) 
         {
             throw new FactoryException("Unknown workflow name \"" + name + "\"");
         }
 
         if (c.descriptor != null) 
         {
             if (reload) 
             {
                 File file = new File(c.url.getFile());
 
                 if (file.exists() && (file.lastModified() > c.lastModified)) 
                 {
                     c.lastModified = file.lastModified();
                     loadWorkflow(c);
                 }
             }
         } 
         else 
         {
             loadWorkflow(c);
         }
 
         return c.descriptor;
     }
 
     public String[] getWorkflowNames() 
     {
         int i = 0;
         String[] res = new String[workflows.keySet().size()];
         Iterator it = workflows.keySet().iterator();
 
         while (it.hasNext()) 
         {
             res[i++] = (String) it.next();
         }
 
         return res;
     }
 
     public void initDone() throws FactoryException 
     {
         reload = getProperties().getProperty("reload", "false").equals("true");
 
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         InputStream is = null;
         String name = getProperties().getProperty("resource", "workflows.xml");
 
         if ((name != null) && (name.indexOf(":/") > -1)) 
         {
             try 
             {
                 is = new URL(name).openStream();
             } 
             catch (Exception e) 
             {
             }
         }
 
         if (is == null) 
         {
             try 
             {
                 is = classLoader.getResourceAsStream(name);
             } 
             catch (Exception e) 
             {
             }
         }
 
         if (is == null) 
         {
             try 
             {
                 is = classLoader.getResourceAsStream("/" + name);
             } 
             catch (Exception e) 
             {
             }
         }
 
         if (is == null) 
         {
             try 
             {
                 is = classLoader.getResourceAsStream("META-INF/" + name);
             } 
             catch (Exception e) 
             {
             }
         }
 
         if (is == null) 
         {
             try {
                 is = classLoader.getResourceAsStream("/META-INF/" + name);
             } catch (Exception e) {
             }
         }
 
         try {
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             dbf.setNamespaceAware(true);
 
             DocumentBuilder db = null;
 
             try {
                 db = dbf.newDocumentBuilder();
             } catch (ParserConfigurationException e) {
                 throw new FactoryException("Error creating document builder", e);
             }
 
             Document doc = db.parse(is);
 
             Element root = (Element) doc.getElementsByTagName("workflows").item(0);
             workflows = new HashMap();
 
             List list = XMLUtil.getChildElements(root, "workflow");
 
             for (int i = 0; i < list.size(); i++) {
                 Element e = (Element) list.get(i);
                 WorkflowConfig config = new WorkflowConfig(e.getAttribute("type"), e.getAttribute("location"));
                 workflows.put(e.getAttribute("name"), config);
             }
         } catch (Exception e) {
             throw new InvalidWorkflowDescriptorException("Error in workflow config", e);
         }
     }
 
     public boolean removeWorkflow(String name) throws FactoryException 
     {
         throw new FactoryException("remove workflow not supported");
     }
 
     public boolean saveWorkflow(String name, WorkflowDescriptor descriptor, boolean replace) throws FactoryException 
     {
         WorkflowConfig c = (WorkflowConfig) workflows.get(name);
 
         if ((c != null) && !replace) {
             return false;
         }
 
         if (c == null) {
             throw new UnsupportedOperationException("Saving of new workflow is not currently supported");
         }
 
         Writer out;
         descriptor.validate();
 
         try {
             out = new OutputStreamWriter(new FileOutputStream(c.url.getFile() + ".new"), "utf-8");
         } catch (FileNotFoundException ex) {
             throw new FactoryException("Could not create new file to save workflow " + c.url.getFile());
         } catch (UnsupportedEncodingException ex) {
             throw new FactoryException("utf-8 encoding not supported, contact your JVM vendor!");
         }
 
         writeXML(descriptor, out);
 
         //write it out to a new file, to ensure we don't end up with a messed up file if we're interrupted halfway for some reason
         //now lets rename
         File original = new File(c.url.getFile());
         File backup = new File(c.url.getFile() + ".bak");
         File updated = new File(c.url.getFile() + ".new");
         boolean isOK = !original.exists() || original.renameTo(backup);
 
         if (!isOK) {
             throw new FactoryException("Unable to backup original workflow file " + original + " to " + backup + ", aborting save");
         }
 
         isOK = updated.renameTo(original);
 
         if (!isOK) {
             throw new FactoryException("Unable to rename new  workflow file " + updated + " to " + original + ", aborting save");
         }
 
         backup.delete();
 
         return true;
     }
 
     protected void save() 
     {
     }
 
 
     private void loadWorkflow(WorkflowConfig c) throws FactoryException 
     {
         try 
         {
             c.descriptor = WorkflowLoader.load(c.url);
         } 
         catch (Exception e) 
         {
             throw new FactoryException("Error in workflow descriptor: " + c.url, e);
         }
     }
 
 
     class WorkflowConfig 
     {
         String location;
         String type;
         URL url;
         WorkflowDescriptor descriptor;
         long lastModified;
 
         public WorkflowConfig(String type, String location) 
         {
             if ("URL".equals(type)) {
                 try {
                     url = new URL(location);
 
                     File file = new File(url.getFile());
 
                     if (file.exists()) {
                         lastModified = file.lastModified();
                     }
                 } catch (Exception ex) {
                 }
             } else if ("file".equals(type)) {
                 try {
                     File file = new File(location);
                     url = file.toURL();
                     lastModified = file.lastModified();
                 } catch (Exception ex) {
                 }
             } else {
                 url = Thread.currentThread().getContextClassLoader().getResource(location);
             }
 
             this.type = type;
             this.location = location;
         }
     }
     */
 
 } 
