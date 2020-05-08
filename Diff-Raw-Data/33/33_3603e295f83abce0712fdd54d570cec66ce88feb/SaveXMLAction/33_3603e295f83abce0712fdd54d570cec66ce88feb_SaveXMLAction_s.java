 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.action;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.geotools.validation.dto.PlugInDTO;
 import org.geotools.validation.dto.TestSuiteDTO;
 import org.geotools.validation.xml.XMLWriter;
 import org.vfny.geoserver.global.ConfigurationException;
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 import org.vfny.geoserver.global.UserContainer;
 import org.vfny.geoserver.global.dto.DataDTO;
 import org.vfny.geoserver.global.dto.GeoServerDTO;
 import org.vfny.geoserver.global.dto.WFSDTO;
 import org.vfny.geoserver.global.dto.WMSDTO;
 import org.vfny.geoserver.global.xml.XMLConfigWriter;
 
 
 /**
  * Save GeoServer state to XML.
  * 
  * <p>
  * This is a propert ConfigAction - you need to be logged in for this to work.
  * </p>
  */
 public class SaveXMLAction extends ConfigAction {
     public ActionForward execute(ActionMapping mapping, ActionForm form,
         UserContainer user, HttpServletRequest request,
         HttpServletResponse response) throws IOException, ServletException {
         ActionForward r1 = saveGeoserver(mapping, form, request, response);
         ActionForward r2 = saveValidation(mapping, form, request, response);
 
         getApplicationState(request).notifiySaveXML();
 
         return mapping.findForward("config");
     }
 
     private ActionForward saveGeoserver(ActionMapping mapping, ActionForm form,
         
     //UserContainer user,
     HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
         ServletContext sc = request.getSession().getServletContext();
        
         //File rootDir = new File(sc.getRealPath("/"));
         File rootDir = GeoserverDataDirectory.getGeoserverDataDirectory(sc);
         try {
             XMLConfigWriter.store((WMSDTO) getWMS(request).toDTO(),
                 (WFSDTO) getWFS(request).toDTO(),
                 (GeoServerDTO) getWFS(request).getGeoServer().toDTO(),
                 (DataDTO) getWFS(request).getData().toDTO(), rootDir);
         } catch (ConfigurationException e) {
             e.printStackTrace();
             throw new ServletException(e);
         }
 
         // We need to stash the current page?
         // or can we use null or something?
         //
         return mapping.findForward("config");
     }
 
     private ActionForward saveValidation(ActionMapping mapping,
         ActionForm form, 
     //UserContainer user,
     HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
         ServletContext sc = request.getSession().getServletContext();
         //CH: changed for geoserver_data_dir, forgotten first round.
         File rootDir = GeoserverDataDirectory.getGeoserverDataDirectory(sc);
 
 	File dataDir;
 	if (GeoserverDataDirectory.isTrueDataDir()){
 	    dataDir = rootDir;
 	} else {
 	    dataDir = new File(rootDir, "data/");
	}		
	//should probably use the XMLConfigWriter's initFile utility methods 
	//here.  But they're in a protected static class.  Should really just
	//move this method to XMLConfigWriter.
        File plugInDir = new File(dataDir, "plugIns");
        File validationDir = new File(dataDir, "validation");

         Map plugIns = (Map) getWFS(request).getValidation().toPlugInDTO();
         Map testSuites = (Map) getWFS(request).getValidation().toTestSuiteDTO();
 
         Iterator i = null;
 
         if (plugIns != null) {
             i = plugIns.keySet().iterator();
 
             while (i.hasNext()) {
                 PlugInDTO dto = null;
                 Object key = null;
 
                 try {
                     key = i.next();
                     dto = (PlugInDTO) plugIns.get(key);

                    FileWriter fw = new FileWriter(new File(plugInDir,
                                dto.getName().replaceAll(" ", "") + ".xml"));
                     XMLWriter.writePlugIn(dto, fw);
                     fw.close();
                 } catch (Exception e) {
                     e.printStackTrace();
                     throw new ServletException(e);
                 }
             }
         }
 
         // deletes of plug ins here
 
         /*File[] pluginFL = plugInDir.listFiles();
            for(int j=0;j<pluginFL.length;j++){
                    String flName = pluginFL[j].getName();
                    flName = flName.substring(0,flName.length()-4);
                    if(plugIns.get(flName)==null){
                            // delete this
                            pluginFL[j].delete();
                    }
            }*/
         if (testSuites != null) {
             i = testSuites.keySet().iterator();
 
             while (i.hasNext()) {
                 TestSuiteDTO dto = null;
 
                 try {
                     dto = (TestSuiteDTO) testSuites.get(i.next());

                    FileWriter fw = new FileWriter(new File(validationDir,
                                dto.getName().replaceAll(" ", "") + ".xml"));
                     XMLWriter.writeTestSuite(dto, fw);
                     fw.close();
                 } catch (Exception e) {
                     System.err.println(dto.getClass());
                     e.printStackTrace();
                     throw new ServletException(e);
                 }
             }
         }
 
         // deletes of testSuites here
         File[] testsFL = validationDir.listFiles();
 
         if (testsFL != null) {
             for (int j = 0; j < testsFL.length; j++) {
 				boolean found = false;
 				i = testSuites.keySet().iterator();
 				while(!found && i.hasNext())
 					found = (((TestSuiteDTO) testSuites.get(i.next())).getName().replaceAll(" ", "") + ".xml").equals(testsFL[j].getName());
 
                 if (!found) {
                     testsFL[j].delete();
                 }
             }
         }
 
         getApplicationState(request).notifiySaveXML();
 
         // We need to stash the current page?
         // or can we use null or something?
         //
         return mapping.findForward("config.validation");
     }
 }
