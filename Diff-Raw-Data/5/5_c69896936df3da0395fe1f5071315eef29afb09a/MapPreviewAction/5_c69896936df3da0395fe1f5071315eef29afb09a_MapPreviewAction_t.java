 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 
 package org.vfny.geoserver.action;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.DynaActionForm;
 
 import org.geoserver.util.ReaderUtils;
 import org.geotools.feature.FeatureType;
 
 import org.vfny.geoserver.global.ConfigurationException;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 import org.vfny.geoserver.global.WMS;
 
 import org.vfny.geoserver.util.requests.CapabilitiesRequest;
 import org.vfny.geoserver.wms.servlets.Capabilities;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * <b>MapPreviewAction</b><br>
  * Sep 26, 2005<br>
  * 
  * <b>Purpose:</b><br>
  * Gathers up all the FeatureTypes in use and returns the informaion to the .jsp .<br>
  * It will also generate three files per FeatureType that will be used for MapBuilder to
  * render the data. The files are labeled as such:<br>
  * - [FeatureTypeName].html<br>
  * - [FeatureTypeName]Config.xml<br>
  * - [FeatureTypeName].xml <br>
  * <br>
  * This will communicate to a .jsp and return it three arrays of strings that contain:<br>
  * - The Featuretype's name<br>
  * - The DataStore name of the FeatureType<br>
  * - The bounding box of the FeatureType<br>
  * 
  * To change what data is output to the .jsp, you must change <b>struts-config.xml</b>.<br>
  * Look for the line:<br>
  * &lt;form-bean <br>
  *  	name="mapPreviewForm"<br>
  * 	    
  * 
  * @author Brent Owens (The Open Planning Project)
  * @version 
  */
 public class MapPreviewAction extends GeoServerAction
 {
 
 	/* (non-Javadoc)
 	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward execute(ActionMapping mapping, 
 					 			ActionForm form,
 						        HttpServletRequest request, 
 						        HttpServletResponse response)
 	throws IOException, ServletException 
 	{
 		ArrayList dsList = new ArrayList();
 		ArrayList ftList = new ArrayList();
 		ArrayList bboxList = new ArrayList();
 		ArrayList ftnsList = new ArrayList();
 		
 		
 		// 1) get the capabilities info so we can find out our feature types
 		WMS wms = getWMS(request);
 		Capabilities caps = new Capabilities(wms);
 		CapabilitiesRequest capRequest = new CapabilitiesRequest("WMS",caps);
 		capRequest.setHttpServletRequest(request);
 		
 		Data catalog = wms.getData();
 		Collection ftypes = catalog.getFeatureTypeInfos().values();
 		FeatureTypeInfo layer;
 		
 		// 2) delete any existing generated files in the generation directory
 		ServletContext sc = request.getSession().getServletContext();
 		//File rootDir =  GeoserverDataDirectory.getGeoserverDataDirectory(sc);
 		//File previewDir = new File(sc.getRealPath("data/generated"));
		if (sc.getRealPath("preview") == null) {
			//There's a problem here, since we can't get a real path for the "preview" directory.
			//On tomcat this happens when "unpackWars=false" is set for this context.
			throw new RuntimeException("Couldn't populate preview directory...is the war running in unpacked mode?");
		}
 		File previewDir = new File(sc.getRealPath("preview"));
 		//File previewDir = new File(rootDir, "data/generated");
 		if (!previewDir.exists())
 			previewDir.mkdirs();
 		
 		try {
 			emptyGeneratedDirectory(previewDir);	// clear the contents of the directory
 		} catch (ConfigurationException e) {
 			e.printStackTrace();
 		}
        
 		// 3) Go through each FeatureType and collect information && write out config files
 		for (Iterator it = ftypes.iterator(); it.hasNext();) 
 		{
 			layer = (FeatureTypeInfo) it.next();
 			Envelope bbox = layer.getLatLongBoundingBox();
 			if (layer.isEnabled()) 
 			{
 				// prepare strings for web output
 				ftList.add(layer.getNameSpace().getPrefix()+"_"+layer.getFeatureType().getTypeName());	// FeatureType name
 				ftnsList.add(layer.getNameSpace().getPrefix()+":"+layer.getFeatureType().getTypeName() );
 				dsList.add(layer.getDataStoreInfo().getId());	// DataStore info
 				// bounding box of the FeatureType
 				bboxList.add(bbox.getMinX()+", "+bbox.getMinY()+", "+bbox.getMaxX()+", "+bbox.getMaxY());
 				//save out the mapbuilder files
 				makeMapBuilderFiles(previewDir, layer, bbox);
 
 			}
 		}
 		
 		// 4) send off gathered information to the .jsp
 		DynaActionForm myForm = (DynaActionForm) form;
 		
 		myForm.set("DSNameList",dsList.toArray(new String[dsList.size()]) );
 		myForm.set("FTNameList",ftList.toArray(new String[ftList.size()]) );
 		myForm.set("BBoxList",bboxList.toArray(new String[bboxList.size()]) );
 		myForm.set("FTNamespaceList",ftnsList.toArray(new String[ftnsList.size()]) );
 		
 		return mapping.findForward("success");
 	}
 
 
 	/**
 	 * emptyGeneratedDirectory
 	 * 
 	 * Description:
 	 * Deletes all files in the supplied directory (file). The directory itself will
 	 * not be deleted. It will also recursively go in all sub directories and delete
 	 * their files as well.
 	 * 
 	 * @param dir The directory to be cleaned
 	 * @throws FileNotFoundException
 	 * @throws ConfigurationException
 	 */
 	private void emptyGeneratedDirectory(File dir) 
 		throws FileNotFoundException, ConfigurationException 
 	{
 		try {
 			dir = ReaderUtils.checkFile(dir, true);
 		} 
 		catch (Exception e) {
 			throw new ConfigurationException( e );
 		}
 		
 		String[] files = dir.list();
 		if (files == null || files.length == 0)
 			return;
 		
 		for (int i=0; i<files.length; i++)
 		{
 			File f = new File(dir, files[i]);
 			if (f.isDirectory())
 				emptyGeneratedDirectory(f);
 			else
 			{
 				if (f.exists())
 					f.delete();
 //					if (!f.delete())	// try to delete the file
 //						throw new FileNotFoundException("could not delete file: "+f.getName());
 			}
 		}
 		
 	}
 
 
 	/**
 	 * makeMapBuilderFiles
 	 * 
 	 * Description:
 	 * Generates the three required files to show the data in MapBuilder.
 	 * The first file is the FeatureTypeName.html file that points to the xml
 	 * configuration to load the data.
 	 * The second file is the configuration file, FeatureTypeNameConfig.xml that
 	 * contains all the tools that MapBuilder will be using (zoom widgets, 
 	 * preview map, etc...).
 	 * The third file points to the FeatureType, FeatureTypeName.xml.
 	 * 
 	 * 
 	 * @param previewDir the root generated directory where the files will be written to
 	 * @param layer the FeatureType in question
 	 * @param bbox the bounding box of the FeatureType
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private void makeMapBuilderFiles(File previewDir, 
 									FeatureTypeInfo layer, 
 									Envelope bbox) 
 		throws FileNotFoundException, IOException 
 	{
 		FeatureType featureType = layer.getFeatureType();
 		String ft_name = featureType.getTypeName();
 		String ft_namespace = layer.getNameSpace().getPrefix();
 		
 		File html_file = new File(previewDir, ft_namespace + "_"+ ft_name+".html");
 		File config_file = new File(previewDir, ft_namespace + "_"+ ft_name+"Config.xml");
 		File xml_file = new File(previewDir, ft_namespace + "_"+ ft_name+".xml");
 		
 		//JD: making the exists check, since they dont have to be generated
 		// every time
 		
 		// *.html
 		if ( ! html_file.exists() ) {
 			FileOutputStream html_fos = new FileOutputStream(html_file);
 			PrintStream html_out = new PrintStream(html_fos);
 			createIndexHTML(html_out, ft_name, ft_namespace);
 			html_out.close();	
 		}
 		
 		if ( ! config_file.exists() ) {
 			//*Config.xml
 			FileOutputStream config_fos = new FileOutputStream(config_file);
 			PrintStream config_out = new PrintStream(config_fos);
 			createConfigXML(config_out, ft_name, ft_namespace);
 			config_out.close();	
 		}
 		
 		if ( !xml_file.exists() ) {
 			//*.xml
 			FileOutputStream xml_fos = new FileOutputStream(xml_file);
 			PrintStream xml_out = new PrintStream(xml_fos);
 			createLayersXML(xml_out, ft_name, ft_namespace, bbox);
 			xml_out.close();	
 		}
 		
 	}
 	
 	
 	private void createIndexHTML(PrintStream out, String ft_name, String ft_namespace)
 	{
 		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">");
 		out.println("<!--");
 		out.println("Description: A Browser based Transactional Web Feature Server client");
 		out.println("             based on javascript and XSL");
 		out.println("             libraries from http://mapbuilder.sourceforge.net .");
 		out.println("Licence:     GPL as per: http://www.gnu.org/copyleft/gpl.html");
 		out.println("-->");
 		out.println("");
 		out.println("<html>");
 		out.println("<head>");
 		// CHANGE HERE
 		out.println("<title>"+ft_namespace+":"+ft_name+" Preview</title>");
 		out.println("");
 		out.println("<link rel=\"stylesheet\" href=\"../../style.css\"");
 		out.println("	type=\"text/css\">");
 		out.println("<link rel=\"stylesheet\" href=\"../mb/lib/skin/default/html.css\"");
 		out.println("	type=\"text/css\">");
 		out.println("");
 		out.println("<script type=\"text/javascript\">");
 		out.println("      // URL of Mapbuilder configuration file.");
 		// CHANGE HERE
 		out.println("      var mbConfigUrl='"+ft_namespace+"_"+ft_name+"Config.xml"+"';");
 		out.println("    </script>");
 		out.println("<script type=\"text/javascript\" src=\"../mb/lib/Mapbuilder.js\"></script>");
 		out.println("</head>");
 		out.println("");
 		out.println("<body onload=\"mbDoLoad()\">");
 		out.println("<!-- Layout mapbuilder widgets and HTML -->");
 		out.println("<h2><a href=\"http://geoserver.sf.net\">GeoServer</a>/<a");
 		out.println("	href=\"http://mapbuilder.sourceforge.net\">Map Builder</a>");
 		// CHANGE HERE
 		out.println(""+ft_namespace+":"+ft_name+" preview</h2>");
 		out.println("");
 		out.println("<table border=\"0\">");
 		out.println("	<tr>");
 		out.println("		<td valign=\"top\" id=\"locatorMap\" style=\"background-color: white;\" />");
 		out.println("		<td rowspan=\"2\" valign=\"top\" />");
 		out.println("		<table border=\"0\">");
 		out.println("			<tr>");
 		out.println("				<td align=\"left\" id=\"mainButtonBar\"/>");
 		out.println("				<td align=\"right\" id=\"cursorTrack\" />");
 		out.println("			</tr>");
 		out.println("			<tr>");
 		out.println("				<td colspan=\"2\" id=\"mainMapPane\" style=\"background-color: white;\" />");
 		out.println("			</tr>");
 		out.println("	<tr align=\"right\">");
 		out.println("		<td colspan=\"2\">");
 		out.println("		<table>");
 		out.println("			<tr>");
 		out.println("        <td align=\"left\" id=\"mapScaleText\"/>");
 		out.println("				<td align=\"right\">Powered by <a href=\"http://mapbuilder.sourceforge.net\">Community Map");
 		out.println("				Builder</a></td>");
 		out.println("				<td><img src=\"../mb/lib/skin/default/images/Icon.gif\" alt=\"\" /></td>");
 		out.println("			</tr>");
 		out.println("		</table>");
 		out.println("		</td>");
 		out.println("	</tr>");
 		out.println("		</table>");
 		out.println("		</td>");
 		out.println("	</tr>");
 		out.println("	<tr>");
 		out.println("		<td id=\"legend\" />");
 		out.println("	</tr>");
 		out.println("	<tr>");
 		out.println("		<td colspan=\"3\" id=\"featureList\" />");
 		out.println("	</tr>");
 		out.println("	<tr>");
 		out.println("		<td colspan=\"3\" id=\"transactionResponse\" />");
 		out.println("	</tr>");
 		out.println("	<tr>");
 		out.println("		<td colspan=\"3\">");
 		out.println("		<div id=\"eventLog\" />");
 		out.println("		</td>");
 		out.println("	</tr>");
 		out.println("</table>");
 		out.println("</body>");
 		out.println("</html>");
 
 	}
 	
 	private void createConfigXML(PrintStream out, String ft_name, String ft_namespace)
 	{
 		
 		out.println("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>");
 		out.println("<MapbuilderConfig version=\"0.2.1\" id=\"referenceTemplate\" xmlns=\"http://mapbuilder.sourceforge.net/mapbuilder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://mapbuilder.sourceforge.net/mapbuilder ../../mapbuilder/lib/schemas/config.xsd\">");
 		out.println("  <!--");
 		out.println("    Description: This configuration file determines what components from the");
 		out.println("                 Mapbuilder library are to be included in a Main Mapbuilder web");
 		out.println("                 page.");
 		out.println("    Licence:     GPL as per: http://www.gnu.org/copyleft/gpl.html ");
 		out.println("");
 		out.println("    $Id$");
 		out.println("  -->");
 		out.println("  <!--");
 		out.println("    All static images should be relative to this URL.");
 		out.println("    This dir is relative the dir of the main html file.");
 		out.println("  -->");
 		out.println("  <models>");
 		out.println("    <Context id=\"mainMap\">");
 		// CHANGE HERE
 		out.println("      <defaultModelUrl>"+ft_namespace+"_"+ft_name+".xml</defaultModelUrl>");
 		out.println("      <widgets>");
 		out.println("        <MapPane id=\"mainMapWidget\">");
 		out.println("          <htmlTagId>mainMapPane</htmlTagId>");
 		out.println("          <mapContainerId>mainMapContainer</mapContainerId>");
 		out.println("        </MapPane>");
 		out.println("        <AoiBoxWZ id=\"aoiBox2\">");
 		out.println("          <htmlTagId>mainMapPane</htmlTagId>");
 		out.println("          <stylesheet>../mb/lib/widget/GmlRenderer.xsl</stylesheet>");
 		out.println("          <lineColor>#FF0000</lineColor>");
 		out.println("          <lineWidth>1</lineWidth>");
 		out.println("          <crossSize>15</crossSize>");
 		out.println("          <mapContainerId>mainMapContainer</mapContainerId>");
 		out.println("        </AoiBoxWZ>");
 		out.println("        <CursorTrack id=\"cursorTrack\">");
 		out.println("          <mouseHandler>mainMap</mouseHandler>");
 		out.println("        </CursorTrack>");
 		out.println("        <Legend id=\"legend\">");
 		out.println("        </Legend>");
 		out.println("        <MapScaleText id=\"mapScaleText\">");
 		out.println("        </MapScaleText>");
 		out.println("      </widgets>");
 		out.println("      <tools>");
 		out.println("        <AoiMouseHandler id=\"mainAoi\"/>");
 		out.println("        <DragPanHandler id=\"mainDragPan\">");
 		out.println("          <enabled>false</enabled>");
 		out.println("        </DragPanHandler>");
 		out.println("        <MouseClickHandler id=\"mainMouseClick\"/>");
 		out.println("      </tools>");
 		out.println("    </Context>");
 		out.println("");    
 		out.println("    <Context id=\"locator\">");
 		// CHANGE HERE
 		out.println("      <defaultModelUrl>"+ft_namespace+"_"+ft_name+".xml</defaultModelUrl>");
 		out.println("      <widgets>");
 		out.println("        <MapPane id=\"locatorWidget\">");
 		out.println("          <htmlTagId>locatorMap</htmlTagId>");
 		out.println("          <targetModel>mainMap</targetModel>");
 		out.println("          <mapContainerId>locatorContainer</mapContainerId>");
 		out.println("          <fixedWidth>180</fixedWidth>");
 		out.println("        </MapPane>");
 		out.println("        <AoiBoxWZ id=\"aoiBox2\">");
 		out.println("          <htmlTagId>locatorMap</htmlTagId>");
 		out.println("          <stylesheet>../mb/lib/widget/GmlRenderer.xsl</stylesheet>");
 		out.println("          <lineColor>#FF0000</lineColor>");
 		out.println("          <lineWidth>1</lineWidth>");
 		out.println("          <crossSize>15</crossSize>");
 		out.println("          <mapContainerId>locatorContainer</mapContainerId>");
 		out.println("        </AoiBoxWZ>");
 		out.println("      </widgets>");
 		out.println("      <tools>");
 		out.println("        <AoiMouseHandler id=\"locatorAoi\"/>");
 		out.println("        <ZoomToAoi id=\"locatorZoomToAoi\">");
 		out.println("          <targetModel>mainMap</targetModel>");
 		out.println("        </ZoomToAoi>");
 		out.println("      </tools>");
 		out.println("    </Context>");
 		out.println("");
 		out.println("    <Transaction id=\"transaction\">");
 		out.println("      <widgets>");
 		out.println("        <TransactionResponse id=\"transactionResponse\">");
 		out.println("        </TransactionResponse>");
 		out.println("      </widgets>");
 		out.println("    </Transaction>");
 		out.println("    <FeatureCollection id=\"featureCollection\">");
 		out.println("      <namespace>xmlns:gml='http://www.opengis.net/gml' xmlns:wfs='http://www.opengis.net/wfs' xmlns:topp='http://www.openplans.org/topp'</namespace>");
 		out.println("      <widgets>");
 		out.println("        <GmlRendererWZ id=\"testGmlRenderer\">");
 		out.println("          <htmlTagId>mainMapPane</htmlTagId>");
 		out.println("          <targetModel>mainMap</targetModel>");
 		out.println("          <mapContainerId>mainMapContainer</mapContainerId>");
 		out.println("          <lineColor>#FF0000</lineColor>");
 		out.println("          <lineWidth>1</lineWidth>");
 		out.println("          <pointDiameter>10</pointDiameter>");
 		out.println("        </GmlRendererWZ>");
 		out.println("        <FeatureList id=\"featureList\">");
 		out.println("        </FeatureList>");
 		out.println("      </widgets>");
 		out.println("    </FeatureCollection>");
 		out.println("  </models>");
 		out.println("  <widgets>");
 		out.println("    <ZoomIn id=\"zoomIn\">");
 		out.println("      <buttonBar>mainButtonBar</buttonBar>");
 		out.println("      <targetModel>mainMap</targetModel>");
 		out.println("      <mouseHandler>mainAoi</mouseHandler>");
 		out.println("      <class>RadioButton</class>");
 		out.println("      <selected>true</selected>");
 		out.println("      <enabledSrc>/images/ZoomInEnable.gif</enabledSrc>");
 		out.println("      <disabledSrc>/images/ZoomInDisable.gif</disabledSrc>");
 		out.println("      <tooltip xml:lang=\"en\">click and drag to zoom in</tooltip>");
 		out.println("      <tooltip xml:lang=\"fr\">cliquer et faire glisser la souris pour agrandir</tooltip>");
 		out.println("    </ZoomIn>");
 		out.println("    <ZoomOut id=\"zoomOut\">");
 		out.println("      <buttonBar>mainButtonBar</buttonBar>");
 		out.println("      <targetModel>mainMap</targetModel>");
 		out.println("      <mouseHandler>mainAoi</mouseHandler>");
 		out.println("      <class>RadioButton</class>");
 		out.println("      <enabledSrc>/images/ZoomOutEnable.gif</enabledSrc>");
 		out.println("      <disabledSrc>/images/ZoomOutDisable.gif</disabledSrc>");
 		out.println("      <tooltip xml:lang=\"en\">click to zoom out</tooltip>");
 		out.println("      <tooltip xml:lang=\"fr\">cliquer pour r???e</tooltip>");
 		out.println("    </ZoomOut>");
 		out.println("    <DragPan id=\"dragPan\">");
 		out.println("      <buttonBar>mainButtonBar</buttonBar>");
 		out.println("      <targetModel>mainMap</targetModel>");
 		out.println("      <mouseHandler>mainDragPan</mouseHandler>");
 		out.println("      <class>RadioButton</class>");
 		out.println("      <enabledSrc>/images/PanEnable.gif</enabledSrc>");
 		out.println("      <disabledSrc>/images/PanDisable.gif</disabledSrc>");
 		out.println("      <tooltip xml:lang=\"en\">click and drag to pan</tooltip>");
 		out.println("      <tooltip xml:lang=\"fr\">cliquer et faire glisser la souris pour voir un autre parti de la carte</tooltip>");
 		out.println("    </DragPan>");
 		out.println("    <Reset id=\"reset\">");
 		out.println("      <buttonBar>mainButtonBar</buttonBar>");
 		out.println("      <targetModel>mainMap</targetModel>");
 		out.println("      <class>Button</class>");
 		out.println("      <disabledSrc>/images/ResetExtentDisable.gif</disabledSrc>");
 		out.println("      <tooltip xml:lang=\"en\">reset the map to full extent</tooltip>");
 		out.println("      <tooltip xml:lang=\"fr\">redonner la carte ses dimensions compl???ts</tooltip>");
 		out.println("    </Reset>");
 		out.println("  </widgets>");
 		out.println("  <skinDir>../mb/lib/skin/default</skinDir>");
 		out.println("</MapbuilderConfig>");
 
 
 	}
 	
 	private void createLayersXML(PrintStream out, 
 								String ft_name, 
 								String ft_namespace,  
 								Envelope bbox) 
 		throws IOException
 	{
 		
 		out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>");
 		out.println("<ViewContext version=\"1.0.0\" id=\"atlas_world\" xmlns=\"http://www.opengis.net/context\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/context http://schemas.opengis.net/context/1.0.0/context.xsd\">");
 		out.println("  <General>");
 		out.println("    <Window width=\"500\" height=\"285\"/>");
 		out.println("    <BoundingBox SRS=\"EPSG:4326\" minx=\""+bbox.getMinX()+"\" miny=\""+bbox.getMinY()+"\" maxx=\""+bbox.getMaxX()+"\" maxy=\""+bbox.getMaxY()+"\"/>");
 		// CHANGE HERE
 		out.println("    <Title>"+ft_namespace+":"+ft_name+" Map</Title>");
 		out.println("    <KeywordList>");
 		// CHANGE HERE
 		out.println("      <Keyword>"+ft_namespace+":"+ft_name+"</Keyword>");
 		out.println("    </KeywordList>");
 		out.println("    <Abstract></Abstract>");
 		out.println("  </General>");
 		out.println("  <LayerList>");
 		out.println("    <Layer queryable=\"1\" hidden=\"0\">");
 		// CHANGE HERE
 		out.println("      <Server service=\"OGC:WMS\" version=\"1.1.1\" title=\""+ft_namespace+":"+ft_name+" Preview\">");
 		out.println("        <OnlineResource xlink:type=\"simple\" xlink:href=\"../wms\"/>");
 		out.println("      </Server>");
 		// CHANGE HERE
 		out.println("      <Name>"+ft_namespace+":"+ft_name+"</Name>");
 		// CHANGE HERE
 		out.println("      <Title>"+ft_namespace+":"+ft_name+"</Title>");
 		out.println("      <SRS>EPSG:4326</SRS>");
 		out.println("      <FormatList><Format current=\"1\">image/png</Format></FormatList>");
 		out.println("    </Layer>");
 		out.println("  </LayerList>");
 		out.println("</ViewContext>");
 		
 	}
 }
