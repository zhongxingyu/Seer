 package com.fusioncharts;
 
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.pentaho.platform.engine.core.system.PentahoSystem;
 
 
 import com.generationjava.io.WritingException;
 import com.generationjava.io.xml.XmlWriter;
 
 
 /*******************************************************************************
  * The ChartFactory class allows a chart object to be created and to associate
  * graphs and series.
  * 
  * It is made flexible so that we will be able to re-use this factory to build charts
  * for multiple chart packages (ie: FusionCharts, Jfreechart) in the future.
  * 
  * 20090325 - Implementation of methods to generate chart info for fusion charts
  * 				David Lai
  *
  ********************************************************************************/
 public class ChartFactory extends Object {
 
 	//****************************************************************************
 	// MEMBERS
 	//****************************************************************************	
 	private Map<String,Graph> graph;
 
 	private boolean isFreeVersion;//render to free version of fusion charts?
 	
 	private boolean chartXMLMode=false;// is to render only xml code chart data?
 
 
 	//****************************************************************************
 	// CONSTRUCTORS / METHODS
 	//****************************************************************************
 
 	/*****************************************************************************
 	 * Constructs a ChartFactory object.
 	 * 
 	 * 	@param 	isFree 
 	 *         	Render to free version of fusion charts or not
 	 * 
 	 * @throws Exception
 	 *         If the application parameters could not be retrieved.
 	 *****************************************************************************/
 	public ChartFactory(boolean isFree )
 	throws Exception
 	{	      	      
 		//initialize the object members
 		this.graph = new HashMap<String,Graph>();
 
 		setFreeVersion(isFree);
 
 	}//ChartFactory
 
 	/***************************************************************************
 	 * Attaches a Graph object to a Map member and returns the Graph object
 	 * 
 	 * @param  categories
 	 *         The list of category labels.
 	 *         
 	 *         idFree 
 	 *         Render to free version of fusion charts or not
 	 *         
 	 *         
 	 *         
 	 * @return graph
 	 * 		  Returns the graph object that was inserted
 	 ***************************************************************************/
 	public Graph insertGraph(Graph graph) 
 	{	
 
 		this.graph.put(graph.getGraphId(), graph);
 		return graph;
 	}
 
 	/***************************************************************************
 	 * Constructor for a FusionGraph object.
 	 *
 	 * @param  graphName
 	 *         The graph name.
 	 *         
 	 * @return the xml string for the Fusion Chart
 	 *         
 	 * @throws WritingException
 	 *         If the xml string that we are building is invalid. Ie: Unclosed Entities
 	 *         
 	 * @throws IllegalArgumentException
 	 *         If the Graph Name does not exist
 	 ***************************************************************************/
 	public String buildFusionChart(String graphName)
 	throws WritingException, IllegalArgumentException
 	{				
 		//instantiate the xml writer
 		Writer writer = new java.io.StringWriter();
 		XmlWriter xmlwriter = new XmlWriter(writer);
 
 		FusionGraph graph = startChartBuild(graphName, xmlwriter);
 
 		// declare the holder for the series in the graph
 		Series singleSeries = graph.getSeries(0);
 
 		// setup either single series or multi series
 		if(graph.getGraphType().isSingleSeries())			
 			attachFusionSeries(xmlwriter, singleSeries, graph);
 		else
 			buildFusionMultiSeries(xmlwriter, graph);
 
 		//set up trend lines		
 		attachFusionTrendLines(xmlwriter, graph.getTrendLineSeries());
 
 		//set to html style so that tooltips can show on multilines
 		attachFusionStyles(xmlwriter, graph);
 
 
 		endChartBuild(xmlwriter);
 		return writer.toString();
 	}
 
 	/**
 	 * 
 	 * starts the chart xml
 	 * 
 	 * renders all chart properties
 	 * 
 	 * @param graphName Chart name
 	 * @param xmlwriter cml writer
 	 * @return 
 	 * @throws WritingException
 	 */
 	protected FusionGraph startChartBuild(String graphName, XmlWriter xmlwriter)
 			throws WritingException {
 		//check if graphName is valid
 		if(!this.graph.containsKey(graphName))
 			throw new IllegalArgumentException("graph name specified does not exist");
 
 		//test if is free version
 		if(isFreeVersion())
 			xmlwriter.writeEntity("graph"); //start graph is free
 		else
 			xmlwriter.writeEntity("chart"); //start graph isn't free
 
 		//We need to get the graph
 		FusionGraph graph = (FusionGraph)this.graph.get(graphName);
 		
 		
 		// attach all chart properties
 		for (String key : graph.getChartProperties().keySet())
 		{
 			xmlwriter.writeAttribute(key, escapeGoofyCharacters(graph.getChartProperties().get(key)));
 		}
 		return graph;
 	}
 
 	
 	/**
 	 * 
 	 * End's the charts xml
 	 * 
 	 * @param xmlwriter xml writer 
 	 * @throws WritingException
 	 */
 	protected void endChartBuild(XmlWriter xmlwriter)
 			throws WritingException {
 		xmlwriter.endEntity(); //end graph
 		//put this in finally block; put try finally
 		xmlwriter.close();
 		return ;
 	}
 	
 	
 	/***************************************************************************
 	 * Method used to construct the xml data for multiple series.  Used for Fusion Charts
 	 *
 	 * @param  xmlwriter
 	 *         The xml writer object passed in will attach the xml for multiple series to it
 	 *         
 	 * @param  graph
 	 *         The graph object we will be taking the series from
 	 *         
 	 * @throws WritingException
 	 *         If the xml string that we are building is invalid. Ie: Unclosed Entities
 	 *         
 	 ***************************************************************************/
 	private void buildFusionMultiSeries(XmlWriter xmlwriter, FusionGraph graph)
 	throws WritingException
 	{
 		//Declare temporary holders for series and iterator
 		Series singleSeries;
 		Iterator<Series> itr;
 
 		//loop to enter all category items
 		xmlwriter.writeEntity("categories");
 		for (int i=0; i < graph.getNumberOfCategories(); i++) {
 			xmlwriter.writeEntity("category");
			xmlwriter.writeAttribute(isFreeVersion()?"name":"label", escapeGoofyCharacters(graph.getCategory(i).getLable()));
 			if(graph.getCategory(i).getxValue()!=null)
 				xmlwriter.writeAttribute("x", escapeGoofyCharacters(graph.getCategory(i).getxValue().toString()));
 			xmlwriter.writeAttribute("showVerticalLine", escapeGoofyCharacters(graph.getCategory(i).getShowVerticalLine().toString()));
 			xmlwriter.endEntity();
 		}
 		xmlwriter.endEntity();
 
 		//create list iterator to loop through all series to place on the chart
 		itr = graph.getNonTrendLineSeries().listIterator();
 		while(itr.hasNext())
 		{
 			//grab an individual series 
 			singleSeries = itr.next();
 
 			//set up dataset info
 			xmlwriter.writeEntity("dataset");
 			xmlwriter.writeAttribute("seriesName", escapeGoofyCharacters(singleSeries.getLabel()));
 			if(singleSeries.getColor(0)!=null)
 				xmlwriter.writeAttribute("color", singleSeries.getColor(0));
 			xmlwriter.writeAttribute("renderAs", singleSeries.getSeriesType().toString());
 
 			if(singleSeries.getParentYAxis()!=null)
 				xmlwriter.writeAttribute("parentYAxis", singleSeries.getParentYAxis());	
 			
 			//set up the selected series
 			attachFusionSeries(xmlwriter, singleSeries, graph);
 
 			xmlwriter.endEntity();						
 		}		
 	}
 
 	/***************************************************************************
 	 * Method used to construct the xml data for a particular series.
 	 * Used for Fusion Charts
 	 *
 	 * @param  xmlwriter
 	 *         The xml writer object passed in will attach the xml for the series
 	 *         
 	 * @param  singleSeries
 	 * 		  The series that we will be building the xml for
 	 *         
 	 * @param  graph
 	 *         The graph object we will be taking the series from
 	 *         
 	 * @throws WritingException
 	 *         If the xml string that we are building is invalid. Ie: Unclosed Entities
 	 *         
 	 ***************************************************************************/
 	private void attachFusionSeries(XmlWriter xmlwriter, Series singleSeries, FusionGraph graph)
 	throws WritingException
 	{
 		//loop through and insert all values from the series
 		for (int i = 0; i < singleSeries.getNumberOfValues(); i++) {
 			xmlwriter.writeEntity("set");
 
 			//for single series charts
 			if(graph.getGraphType().isSingleSeries())
 			{
				xmlwriter.writeAttribute(isFreeVersion()?"name":"label", escapeGoofyCharacters(graph.getCategory(i).getLable()));				
 
 				//Make sure we don't put null attributes
 				if(singleSeries.getColor(i) != null)
 					xmlwriter.writeAttribute("color", singleSeries.getColor(i));				
 			}
 			//the Bubble chart have a special treatment
 			if(graph.getGraphType()==ChartType.BUBBLE)
 			{
 				xmlwriter.writeAttribute("name", escapeGoofyCharacters(graph.getCategory(i).getLable()));
 				// get values
 				Double x=singleSeries.getXValue(i);
 				Double y=singleSeries.getYValue(i);
 				Double z=singleSeries.getZValue(i);
 				
 				if(x==null)
 					throw new NullPointerException("The x Value Is null at index:"+i);
 				if(y==null)
 					throw new NullPointerException("The y Value Is null at index:"+i);			
 				// attribute values
 				xmlwriter.writeAttribute("x", x.toString());
 				xmlwriter.writeAttribute("y", y.toString());
 				
 				if(z!=null)
 					xmlwriter.writeAttribute("z", z.toString());
 				else
 				{
 					xmlwriter.writeAttribute("z", "1");
 				}
 				
 			}
 			else
 			{
 				xmlwriter.writeAttribute("value", (singleSeries.getValue(i) == null)?"null":singleSeries.getValue(i).toString());
 			}
 
 			if(singleSeries.getEvent(i) != null)
 				xmlwriter.writeAttribute("link", singleSeries.getEvent(i));
 
 			if(singleSeries.getToolText(i) != null)
 				xmlwriter.writeAttribute("tooltext", escapeGoofyCharacters(singleSeries.getToolText(i)));
 
 			xmlwriter.endEntity();				
 		}				
 	}
 
 	/***************************************************************************
 	 * Method used to construct the xml data for trend lines.
 	 * Used for Fusion Charts
 	 *
 	 * @param  xmlwriter
 	 *         The xml writer object passed in will attach the xml for trend lines
 	 *         
 	 * @param  seriesList
 	 * 		  The list of series that we must create trend line xml data for         
 	 *         
 	 * @throws WritingException
 	 *         If the xml string that we are building is invalid. Ie: Unclosed Entities
 	 *         
 	 ***************************************************************************/
 	private void attachFusionTrendLines(XmlWriter xmlwriter, LinkedList<Series> seriesList) 
 	throws WritingException{
 		Series trendSeries;
 		Iterator<Series> itr = seriesList.listIterator();
 
 		//loop through the list of trendlines and construct the appropriate xml
 		while (itr.hasNext())
 		{
 			trendSeries = itr.next();
 			xmlwriter.writeEntity("trendLines");
 			xmlwriter.writeEntity("line");
 			xmlwriter.writeAttribute("startValue", new Double(trendSeries.getValue(0).doubleValue()).toString());
 			xmlwriter.writeAttribute("displayvalue", escapeGoofyCharacters(trendSeries.getLabel()));
 
 			//set thickness and make sure it is on top
 			xmlwriter.writeAttribute("thickness", "2");
 			xmlwriter.writeAttribute("showOnTop", "1");
 			xmlwriter.writeAttribute("valueOnRight", "1");
 			xmlwriter.writeAttribute("toolText", new Integer(trendSeries.getValue(0).intValue()).toString());  
 
 			//put color if the color field exists
 			if (trendSeries.getColor(0) != null)
 				xmlwriter.writeAttribute("color", trendSeries.getColor(0));
 			xmlwriter.endEntity();
 			xmlwriter.endEntity();
 		}
 	}
 
 	/***************************************************************************
 	 * Method used to construct the xml data for styles that a user wants to apply
 	 * for the graph.  Refer to the FusionCharts API for more information on using styles.
 	 * {@link http://www.fusioncharts.com/docs/}   
 	 * Used for Fusion Charts
 	 *
 	 * @param  xmlwriter
 	 *         The xml writer object passed in will attach the xml for styles used
 	 *         
 	 * @param  graph
 	 *         The graph object we will be taking the styles from
 	 *         
 	 * @throws WritingException
 	 *         If the xml string that we are building is invalid. Ie: Unclosed Entities
 	 *         
 	 ***************************************************************************/	
 	private void attachFusionStyles(XmlWriter xmlwriter, FusionGraph graph)
 	throws WritingException {
 		FusionGraph.FusionStyle fusionStyle;
 
 		xmlwriter.writeEntity("styles");
 		//setup definition tag
 		xmlwriter.writeEntity("definition");
 
 		//attach all the styles to the xml
 		for (String key : graph.getFusionStyles().keySet())
 		{
 			fusionStyle = graph.getFusionStyles().get(key);
 			xmlwriter.writeEntity("style");
 			xmlwriter.writeAttribute("name", fusionStyle.getStyleName());
 			xmlwriter.writeAttribute("type", fusionStyle.getStyleType().toString());
 
 			for (String styleKey : fusionStyle.getStyleProperties().keySet())
 			{
 				xmlwriter.writeAttribute(styleKey, fusionStyle.getStyleProperties().get(styleKey));
 			}
 			xmlwriter.endEntity();
 		}
 		xmlwriter.endEntity(); //end of definition
 
 		//Set up application tag
 		xmlwriter.writeEntity("application");
 
 		//construct the xml that links the styles to a particular Fusion object
 		for (FusionGraph.FusionStyleObject objectKey : graph.getFusionStyleObjects().keySet())
 		{	
 			String styleNameList = graph.getFusionStyleObjects().get(objectKey).toString();
 			xmlwriter.writeEntity("apply");
 			xmlwriter.writeAttribute("toObject", objectKey.toString());
 			xmlwriter.writeAttribute("styles", styleNameList.substring(1, styleNameList.length()-1));
 			xmlwriter.endEntity();
 		}			
 		xmlwriter.endEntity(); //end of application
 		xmlwriter.endEntity(); //end of styles
 	}
 	
 	/***************************************************************************
 	 * Method that wraps the dataxml string so that we can use it with FusionChartsDOM.js
 	 * This allows us to customize when the chart is rendered and using the DOM gives the advantage
 	 * of less parameters being used
 	 *
 	 *         
 	 * @param  graphName
 	 *         The name of the graph which we will wrap
 	 *         
 	 * @return domWrapper
 	 * 		  The dataXML string wrapped in a usable tag
 	 *         
 	 * @throws WritingException
 	 *         If the xml string that we are building is invalid. Ie: Unclosed Entities
 	 *         
 	 * @throws IllegalArgumentException
 	 *         If the Graph Name does not exist
 	 ***************************************************************************/		
 	public String buildDOMFusionChart(String graphName)
 	throws WritingException, IllegalArgumentException
 	{		
 		return this.buildFusionChart(graphName);
 	}
 
 	protected String escapeGoofyCharacters(String value)
 	{
 		String result = value;
 		if (result != null)
 		{
 			result = result.replaceAll("\\+", "%2B");
 			result = result.replaceAll("\\\\n", "%0A");
 			result = result.replaceAll("\\\\r", "%0D");
 		}
 		else result = new String();
 		return result;
 	}
 
 	/***********************************************************************
 	 * 
 	 *  Getters and setters
 	 *  
 	 **********************************************************************/
 
 	public boolean isFreeVersion() {
 		return isFreeVersion;
 	}
 
 	public void setFreeVersion(boolean isFreeVersion) {
 		this.isFreeVersion = isFreeVersion;
 	}
 
 	public boolean isChartXMLMode() {
 		return chartXMLMode;
 	}
 
 	public void setChartXMLMode(boolean chartXMLMode) {
 		this.chartXMLMode = chartXMLMode;
 	}
 }
