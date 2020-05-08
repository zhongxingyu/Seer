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
 
 package edu.cmu.cs.in.hoop.hoops.transform;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 import edu.cmu.cs.in.base.HoopLink;
 import edu.cmu.cs.in.base.kv.HoopKV;
 import edu.cmu.cs.in.base.kv.HoopKVInteger;
 import edu.cmu.cs.in.base.kv.HoopKVTools;
 import edu.cmu.cs.in.hoop.HoopProjectPanel;
 import edu.cmu.cs.in.hoop.HoopStopWordEditor;
 import edu.cmu.cs.in.hoop.hoops.base.HoopBase;
 import edu.cmu.cs.in.hoop.hoops.base.HoopInterface;
 import edu.cmu.cs.in.hoop.hoops.base.HoopTransformBase;
 import edu.cmu.cs.in.hoop.project.HoopProject;
 import edu.cmu.cs.in.hoop.project.HoopStopWords;
 import edu.cmu.cs.in.hoop.properties.types.HoopURISerializable;
 
 /**
 * 
 */
 public class HoopFilterStopWords extends HoopTransformBase implements HoopInterface
 {    	
 	private static final long serialVersionUID = -3243831355471606975L;
 	private HoopStopWordEditor propPanel=null;
 	private HoopStopWords vocab=null;
 	
 	private HoopURISerializable URI=null;
 	
 	/**
 	 *
 	 */
     public HoopFilterStopWords () 
     {
 		setClassName ("HoopFilterStopWords");
 		debug ("HoopFilterStopWords ()");
 				
 		setHoopDescription ("Remove stopwords from input KVs");
 		
 		HoopProject proj=HoopLink.project;
 		
 		if (proj!=null)
 		{
 			if (proj.getFileByClass("HoopStopWords")==null)
 			{
 				vocab=new HoopStopWords ();
 				proj.addFile (vocab);
 			
 				HoopProjectPanel projectPanel=(HoopProjectPanel) HoopLink.getWindow("Project");
 				
 				if (projectPanel!=null)
 				{
 					projectPanel.updateContents();
 				}
 			}	
 			else
 				vocab=(HoopStopWords) proj.getFileByClass("HoopStopWords");
 		}
 		
 		URI=new HoopURISerializable (this,"URI","<PROJECTPATH>/vocabulary.xml");		
     }
 	/**
 	 *
 	 */
 	public Boolean runHoop (HoopBase inHoop)
 	{		
 		debug ("runHoop ()");
 				
 		ArrayList <HoopKV> inData=inHoop.getData();
 		
 		if (inData!=null)
 		{					
 			for (int i=0;i<inData.size();i++)
 			{
				HoopKVInteger aKV=(HoopKVInteger) inData.get(i);
 				
 				Integer keyString=i;
 				
 				if (aKV.getValueSize()>1)
 				{
 					// Processing horizontal value model ...
 					
 					ArrayList<Object> aValues=aKV.getValuesRaw();
 					
 					HoopKV newKV=HoopKVTools.getLikeKVType(aKV);				
 					newKV.setKeyString (keyString.toString());
 					
 					for (int t=0;t<aValues.size();t++)
 					{							
 						//debug ("Checking " + aValues.size () + " tokens ...");
 						
 						String test=(String) aValues.get(t);
 						
 						for (int j=0;j<HoopLink.stops.length;j++)
 						{	
 							// Copy the value/token if it's not a stop word
 							
 							if (test.toLowerCase().equals(HoopLink.stops [j])==false)
 							{							
 								newKV.setValue(test,t);
 							}
 							//else
 							//	debug ("Tossing stopword: " + test);
 						}						
 					}
 					
 					if (newKV.getValueSize()>0)
 					{
 						addKV (newKV);
 					}
 					else
 						toss (newKV);
 				}
 				else
 				{
 					// Processing vertical value model ...
 					
 					Boolean isStop=false;
 				
 					for (int j=0;j<HoopLink.stops.length;j++)
 					{				
						if (aKV.getValue().toLowerCase().equals(HoopLink.stops [j])==true)
 						{
 							isStop=true;
 						}	
 					}	
 												
 					HoopKV newKV=HoopKVTools.getLikeKVType(aKV);				
 					newKV.setKeyString (keyString.toString());
 					newKV.setValue(aKV.getValue(),0);
 				
 					if (isStop==false)
 					{
 						addKV (newKV);
 					}
 					else
 					{
 						toss (newKV);
 					}
 				}	
 				
 				updateProgressStatus (i,inData.size());
 			}						
 		}
 		else
 			return (false);		
 				
 		return (true);
 	}	 
 	/**
 	 * 
 	 */
 	public HoopBase copy ()
 	{
 		return (new HoopFilterStopWords ());
 	}
 	/**
 	 * 
 	 */
 	@Override
 	public JPanel getPropertiesPanel() 
 	{
 		debug ("getPropertiesPanel ()");
 		
 		if (propPanel==null)
 			propPanel=new HoopStopWordEditor ();
 		
 		// Doesn't make a difference, probably because there is no vertical glue in the scrollpane
 		
 		propPanel.setPreferredSize(new Dimension (150,200)); 
 		
 		return (propPanel);
 	}	
 }
