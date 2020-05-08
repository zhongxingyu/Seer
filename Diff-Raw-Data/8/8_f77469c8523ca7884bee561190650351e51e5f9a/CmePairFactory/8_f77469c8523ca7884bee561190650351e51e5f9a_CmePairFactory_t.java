 package pkgCMEF;
 
 import java.awt.Image;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 
 import com.sun.tools.javac.util.List;
 
 //=============================================================================
 /**
  * This class implements the Image Factory which loads all the images
  * used in the experiment.
  * @author Rick Coleman
  * @version 1.0
  */
 //=============================================================================
 
 public class CmePairFactory
 {
 	/** Pointer to app instance */
 	private CmeApp m_App;
 	
 	/** Vector of PCLE_Image objects */
 	private CmeGroupedVector<CmePair> m_vPairs;
 	
 	private Vector<String> m_TypeList;
 	
 	//-------------------------------------------------
 	/** Default constructor 
 	 * @throws Exception */
 	//-------------------------------------------------
 	public CmePairFactory(CmeApp app, String file)
 	{
 		m_App = app;
 		
 		if (m_TypeList == null)
 			m_TypeList = new Vector<String>();
 		m_TypeList.clear();
 		
 		// Create the vector to hold all images
 		m_vPairs = new CmeGroupedVector<CmePair>();
 		
 		// Open the ImageList.txt file
 		FileReader		instFile;
 		BufferedReader	bufReader = null;
 		String 			line;
 		// Open the file
 		try
 		{
 			instFile = new FileReader(file);
 		}
 		catch(FileNotFoundException e1) // If we failed to opened it
 		{
 			JOptionPane.showMessageDialog(null, 
 					"Error: Unable to open " + file + " file", 
 					"Error Opening File", JOptionPane.ERROR_MESSAGE);
 			System.out.println(ClassLoader.getSystemClassLoader().getSystemResource(".").toString());
 			System.exit(1);
 			return;
 		}
 		// Read the text strings and add them to the text area
 		try
 		{
 			bufReader = new BufferedReader(instFile);
 			CmePair img;
 			
 			// For each line in the file
 			while((line = bufReader.readLine()) != null)
 			{
 				line = line.trim();
 				
 				if (line.length() == 0)
 					continue;
 				
 				if (line.charAt(0) == '#')
 					continue;
 				
 				int val = 0;
 				// Parse the name key and image name
				String[] strs = line.split(",", -1);
 				try {
 					val = Integer.valueOf(strs[0]);
 				} catch(Exception ex) {
 					System.out.println("Failed to parse image value:" + line);
 				}


 				String clss = strs[1].trim();
 				for (int x=0; x<m_TypeList.size(); x++) {
 					String cc = m_TypeList.get(x);
 					if (cc != null && !cc.equals(clss) && x == m_TypeList.size()-1)
 						m_TypeList.add(clss);	
 				}
 				
 				if (m_TypeList.size() == 0)
 					m_TypeList.add(clss);
 								
 				// Create the image and add to the vector of images
 				img = new CmePair(m_App);
 				img.setPairValue(val);
 				img.setPairGroup(clss);
 				img.setNameA(strs[2].trim());

 				img.setImageA(strs[3].trim());
 				img.setNameB(strs[4].trim());
 				String ib = null;
 				
 				if (strs.length > 5) {
 					ib = strs[5];
 					if (strs.length > 6) {
 						for (int x=6; x<strs.length; x++) {
 							img.addExtraInfo(strs[x]);
 						}
 						m_App.setProperty(Integer.toString(m_vPairs.groupSize()) + "EX", img.getExtraInfoVector());
 					}
 				} else {
 					ib = "";
 				}
 				
 				img.setImageB(ib);
 				
 				m_App.setProperty(Integer.toString(m_vPairs.groupSize()) + "V", strs[0].trim());
 				m_App.setProperty(Integer.toString(m_vPairs.groupSize()) + "D", clss);
 				m_App.setProperty(Integer.toString(m_vPairs.groupSize()) + "A", strs[2].trim());
 				m_App.setProperty(Integer.toString(m_vPairs.groupSize()) + "IA", strs[3].trim());
 				m_App.setProperty(Integer.toString(m_vPairs.groupSize()) + "B", strs[4].trim());
 				m_App.setProperty(Integer.toString(m_vPairs.groupSize()) + "IB", ib.trim());
 				
 				m_vPairs.add(img);
 			}
 		}
 		catch(IOException e)
 		{
 			JOptionPane.showMessageDialog(null, 
 					"Error: Unable to read '" + file + "' file.", 
 					"Error Reading File", JOptionPane.ERROR_MESSAGE);
 			e.printStackTrace();
 			return;
 		}
 		catch(Exception e) {
 			JOptionPane.showMessageDialog(null, 
 					"Error: Failed to parse '" + file + "'.",
 					"Error Reading File", JOptionPane.ERROR_MESSAGE);
 			e.printStackTrace();
 			return;
 		}
 	}
 
 	public Vector<String> getTypeList() {
 		return m_TypeList;
 	}
 
 	//-------------------------------------------------------------
 	/** Get the first matched pair value, based on the provided
 	 * input, and field */
 	//-------------------------------------------------------------
 	public CmePair getPairByValue(String dataset, String field, String input, int matchcount)
 	{
 		if(input == null || field == null) return null;
 		if(input.length() < matchcount) return null;
 
 		input = input.replace("ie", "ee").replace("ei", "ee");
 
 		field = field.toLowerCase();
 		int _field = -2;
 		
 		if (field == "pairb") {
 			_field = -1;
 		} else if (field.startsWith("extra")) {
 			try {
 				_field = Integer.valueOf(field.substring(5));
 			} catch (Exception e) {
 				_field = 0;
 				System.err.println("Invalid substring for field value. Using '0'.");
 			}
 		}
 
 		for (CmePair obj : m_vPairs) {
 			if (!obj.getPairGroup().equals(dataset)) continue;
 
 			String value = null;
 			switch(_field) {
 			case -2:
 				value = obj.getNameA();
 				break;
 			case -1:
 				value = obj.getNameB();
 				break;
 			default:
 				value = obj.getExtraInfo(_field);
 				break;
 			}
 			
 			// Do we match this one?
 			if (value instanceof String) {
 				if (value.trim().startsWith("<")) {
 					value = value.trim().replaceAll("<[^>]*>", "");
 				}
 				
 				if (value.contains(input.substring(0, matchcount))) {
 					return obj;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	//-------------------------------------------------------------
 	/** Get an image from the factory by vector index */
 	//-------------------------------------------------------------
 	public Image getImageA(int idx)
 	{
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return null;
 		return ((CmePair)m_vPairs.elementAt(idx)).getImageA();
 	}
 	
 	//-------------------------------------------------------------
 	/** Get an image from the factory by vector index */
 	//-------------------------------------------------------------
 	public Image getImageB(int idx)
 	{
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return null;
 		return ((CmePair)m_vPairs.elementAt(idx)).getImageB();
 	}
 	
 	//-------------------------------------------------------------
 	/** Get a file from the factory by vector index */
 	//-------------------------------------------------------------
 	public String getFileA(int idx)
 	{
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return null;
 		return ((CmePair)m_vPairs.elementAt(idx)).getFileA();
 	}
 	
 	//-------------------------------------------------------------
 	/** Get a file from the factory by vector index */
 	//-------------------------------------------------------------
 	public String getFileB(int idx)
 	{
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return null;
 		return ((CmePair)m_vPairs.elementAt(idx)).getFileB();
 	}
 
 	//-------------------------------------------------------------
 	/** Get the name of an image from the factory by vector index */
 	//-------------------------------------------------------------
 	public String getNameA(int idx)
 	{
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return null;
 		return ((CmePair)m_vPairs.elementAt(idx)).getNameA();
 	}
 
 	//-------------------------------------------------------------
 	/** Get the name of an image from the factory by vector index */
 	//-------------------------------------------------------------
 	public String getNameB(int idx)
 	{
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return null;
 		return ((CmePair)m_vPairs.elementAt(idx)).getNameB();
 	}
 
 	//-------------------------------------------------------------
 	/** Get the name of an image from the factory by vector index */
 	//-------------------------------------------------------------
 	public String getFeedbackA(int idx, int scale)
 	{
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return "<h1>Experiment Error: Please Notify Proctor</h1>";
 		return ((CmePair)m_vPairs.elementAt(idx)).getFeedbackA(scale);
 	}
 
 	//-------------------------------------------------------------
 	/** Get the name of an image from the factory by vector index */
 	//-------------------------------------------------------------
 	public String getFeedbackB(int idx, int scale)
 	{
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return "<h1>Experiment Error: Please Notify Proctor (" + Integer.toString(idx) + ")</h1>";
 		return ((CmePair)m_vPairs.elementAt(idx)).getFeedbackB(scale);
 	}
 	
 	public String getExtraInfo(int idx, int val) {
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return "Invalid Parameter";
 		return ((CmePair)m_vPairs.elementAt(idx)).getExtraInfo(val);
 	}
 
 	//-------------------------------------------------------------
 	/** Get the string value of the image at index idx */
 	//-------------------------------------------------------------
 	public String getPairValue(int idx)
 	{
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return null;
 		return String.valueOf(((CmePair)m_vPairs.elementAt(idx)).getPairValue());
 	}
 	
 	//-------------------------------------------------------------
 	/** Get the integer value of idx */
 	//-------------------------------------------------------------
 	public int getIntValue(int idx)
 	{
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return -1;
 		return ((CmePair)m_vPairs.elementAt(idx)).getPairValue();
 	}
 	
 	//-------------------------------------------------------------
 	/** Get the string value of the image at index idx */
 	//-------------------------------------------------------------
 	public String getPairGroup(int idx)
 	{
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return null;
 		return String.valueOf(((CmePair)m_vPairs.elementAt(idx)).getPairGroup());
 	}
 	
 	public void setDataSet(String name) {
 		m_vPairs.setActiveGroup(name);
 	}
 	
 	public void clearDataSet() {
 		m_vPairs.clearActiveGroup();
 	}
 	
 	public int getCount() {
 		return m_vPairs.groupSize();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Vector<String> getPairExtraInfoVector(int idx) {
 		if((idx < 0) || (idx >= m_vPairs.groupSize())) return null;
 		return (Vector<String>) ((CmePair)m_vPairs.elementAt(idx)).getExtraInfoVector();
 	}
 	
 	//-------------------------------------------------------------
 	/** Get the image vector. */
 	//-------------------------------------------------------------
 	public Vector<CmePair> getImagesVector()
 	{
 		return this.m_vPairs;
 	}
 
 	public int getTrueIndex(int index) {
 		CmePair mypair = m_vPairs.elementAt(index);
 		return m_vPairs.indexOf(mypair);
 	}
 
 	public int getTrueIndex(CmePair mypair) {
 		return m_vPairs.indexOf(mypair);
 	}
 
 }
