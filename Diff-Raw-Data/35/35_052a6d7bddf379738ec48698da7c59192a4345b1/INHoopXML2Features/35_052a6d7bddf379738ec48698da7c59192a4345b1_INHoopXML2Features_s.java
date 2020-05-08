 /** 
  * Author: Martin van Velsen <vvelsen@cs.cmu.edu>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation, either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package edu.cmu.cs.in.hoop.hoops;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import javax.swing.JPanel;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import edu.cmu.cs.in.base.INXMLBase;
 import edu.cmu.cs.in.base.kv.INKV;
 import edu.cmu.cs.in.controls.INHoopXPathFeatureEditor;
 import edu.cmu.cs.in.hoop.base.INHoopBase;
 import edu.cmu.cs.in.hoop.base.INHoopInterface;
 import edu.cmu.cs.in.hoop.base.INHoopTransformBase;
 import edu.cmu.cs.in.hoop.properties.types.INHoopStringSerializable;
 
 /**
 * http://www.ibm.com/developerworks/library/x-javaxpathapi/index.html
 */
 public class INHoopXML2Features extends INHoopTransformBase implements INHoopInterface
 {    	
 	private ArrayList <INHoopStringSerializable>features=null;
 	
 	private INHoopXPathFeatureEditor featureEditor=null;
 	
 	/**
 	 *
 	 */
     public INHoopXML2Features () 
     {
 		setClassName ("INHoopXML2Features");
 		debug ("INHoopXML2Features ()");
 										
 		setHoopDescription ("Parse Text as XML and extract Features");			
     }
 	/**
 	 *
 	 */
 	public Boolean runHoop (INHoopBase inHoop)
 	{		
 		debug ("runHoop ()");
 		
 		ArrayList <INKV> inData=inHoop.getData();
 		
 		if (inData!=null)
 		{			
 			if (inData.size()==0)
 			{
 				this.setErrorString("Error: data size is 0");
 				return (false);
 			}
 			
 			for (int t=0;t<inData.size();t++)
 			{
 				INKV aKV=inData.get(t);
 
 				String aFullText=aKV.getValueAsString();
 					
 				if (aFullText.indexOf("<?xml")==-1) // Quick check
 				{
 					this.setErrorString("Error: loaded text does not contain xml signature");
 					return (false);
 				}
 				else
 				{
 					INXMLBase xmlTools=new INXMLBase ();
 					Document root=xmlTools.loadXMLFromString (aFullText);
 				
 					for (int i=0;i<features.size();i++)
 					{
 						INHoopStringSerializable aFeature=features.get(i);
 					
 						try
 						{
 							XPath xPath = XPathFactory.newInstance().newXPath();
 							//Node node = (Node) xPath.evaluate("/Request/@name", root, XPathConstants.NODE);
 							Node node = (Node) xPath.evaluate(aFeature.getName(), root, XPathConstants.NODE);
 							aFeature.setValue(node.getNodeValue());						
 						} 
 						catch (Exception e) 
 						{
 							e.printStackTrace();
 						}					
 					}
 				}	
 			}		
 		}		
 		else
 		{
 			this.setErrorString("Error: no data found in incoming hoop");
 			return (false);
 		}	
 						
 		return (true);
 	}	
 	/**
 	 * 
 	 */
 	public INHoopBase copy ()
 	{
 		return (new INHoopXML2Features ());
 	}		
 	/**
 	 * 
 	 */
 	@Override
 	public JPanel getPropertiesPanel() 
 	{
 		debug ("getPropertiesPanel ()");
 		
 		if (featureEditor==null)
			featureEditor=new INHoopXPathFeatureEditor ();
 		
 		// Doesn't make a difference, probably because there is no vertical glue in the scrollpane
 		
 		featureEditor.setPreferredSize(new Dimension (150,200)); 
 		
 		return (featureEditor);
 	}		
 }
