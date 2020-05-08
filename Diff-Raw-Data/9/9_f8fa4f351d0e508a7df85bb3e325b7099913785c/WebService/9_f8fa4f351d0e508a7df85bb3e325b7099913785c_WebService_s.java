 package colorLib.webServices;
 
 import java.io.BufferedReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import processing.core.PApplet;
 import processing.data.XML;
 import colorLib.Palette;
 
 public abstract class WebService
 {
 	protected PApplet p;
 	protected boolean printXML = false;
 	protected int numResults, resultOffset;
 	
 	protected boolean debugMode = true;
 	
 	protected XML getXML(String url, String filename)
 	{
 		XML xml = null;
 		if (filename != null) {
 			try {
 				xml = new XML(p, filename + ".xml");
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 			}
 		}
 		
 		if (xml == null) {
 			if (debugMode == true) {
 				p.println(url);
 			}
 			
 //			xml = new XML(p, url);
 			
 			try {
 				URL mo = new URL(url);
 				xml = new XML (new BufferedReader(new InputStreamReader(mo.openStream())));
 			} catch (Exception e1) {
 				throw new RuntimeException(e1);
 			}
 	        
 	        
 			if (printXML) {
 				printXML(url.toString());
 			}
 			if (filename != null) {
 				try {
 					FileWriter writer = new FileWriter(filename + ".xml"); //filename + ".xml"), "UTF-8"
					writer.write(xml.toString(2));
 					writer.flush();
 					writer.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return xml;
 	}
 	
 	protected void printXML(String url)
 	{
 		if (debugMode == true) {
 			String lines[] = p.loadStrings(url);
 			for (int i = 0; i < lines.length; i++) {
 				p.println(lines[i]);
 			}
 		}
 	}
 	
 	public Palette[] searchForThemes(String tag)
 	{
 		return searchForThemes(tag, null);
 	}
 	
 	public Palette[] searchForThemes(String tag, String filename)
 	{
 		return null;
 	}
 
 	public Palette[] getPopular()
 	{
 		return getPopular(null);
 	}
 	
 	public Palette[] getPopular(final String filename)
 	{
 		throw new UnsupportedOperationException();
 	}
 	
 	public Palette[] getLatest()
 	{
 		return getLatest(null);
 	}
 	
 	public Palette[] getLatest(final String filename)
 	{
 		return null;
 	}
 	
 	public Palette[] getRandom()
 	{
 		return getRandom(null);
 	}
 	
 	public Palette[] getRandom(final String filename)
 	{
 		return null;
 	}
 
 	
 	/**
 	 * Use this methode to print the result xml in the console.
 	 * 
 	 * @param b
 	 *            boolean: set true if you want the result xml printed in the
 	 *            console
 	 */
 	public void printXML(boolean b)
 	{
 		printXML = b;
 	}
 	
 	/**
 	 * Returns the number of results.
 	 * @return numResults: the Number of results
 	 */
 	public int getNumResults() {
 		return numResults;
 	}
 
 	/**
 	 * Sets the number of results to query.
 	 * The value of the integer should not be bigger than 100.
 	 * Default is 20.
 	 * @param numResults
 	 */
 	public void setNumResults(int numResults)
 	{
 		this.numResults = PApplet.constrain(numResults, 0, 100);
 	}
 
 	/**
 	 * A 0-based index into the list that specifies the first item to display.
 	 * Default is 0, which displays the first item in the list.
 	 * @return resultOffset: Result Offset
 	 */
 	public int getResultOffset()
 	{
 		return resultOffset;
 	}
 
 	/**
 	 * Sets the result offset.
 	 * @param resultOffset
 	 */
 	public void setResultOffset(int resultOffset)
 	{
 		this.resultOffset = resultOffset;
 	}
 
 }
