 package com.xpandit.fusionplugin;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.pentaho.commons.connection.IPentahoMetaData;
 import org.pentaho.commons.connection.IPentahoResultSet;
 
 import com.fusioncharts.Category;
 import com.fusioncharts.ChartFactoryChart;
 import com.fusioncharts.ChartFactoryMaps;
 import com.fusioncharts.ChartType;
 import com.fusioncharts.Entity;
 import com.fusioncharts.FusionGraph;
 import com.fusioncharts.Series;
 import com.xpandit.fusionplugin.exception.InvalidDataResultSetException;
 import com.xpandit.fusionplugin.exception.InvalidParameterException;
 import com.xpandit.fusionplugin.util.ScaleConverter;
 
 /**
  * 
  * This class is responsible for manage all the chart special behavior. Generic processes should be kept on abstract
  * class FCItem.
  * 
  * @author dduque
  * 
  */
 public class FCMaps extends FCItem {
 
     // get link values
     // the chart link template
     String chartLink = null;
     // seriesParam to replace in template link
     String entityParam = null;
     // valueParam to replace in template link
     String valueParam = null;
 
     /**
      * Constructor for Charts
      * 
      * @param chartType Type of chart to be created.
      * @param resultSets Results sets containig data to display.
      * @throws InvalidDataResultSetException
      */
     public FCMaps(ChartType chartType, Map<String, ArrayList<IPentahoResultSet>> resultSets,TreeMap<String, String> params)
             throws InvalidDataResultSetException {
 
        // set category length 
         int categoryLength = 0;
         ArrayList<IPentahoResultSet> results = resultSets.get("results");
         categoryLength = results.get(0).getRowCount();
 
         // initialize chart
         graph = new FusionGraph("chart", chartType, categoryLength);
         
         //set chart properties
         setChartProperties(params);
         
         // get nodes
         chartLink = graph.getChartProperties().get("chartLink");
         entityParam = graph.getChartProperties().get("entityParam");
        //if entityParam is null try series param 
        if(entityParam==null)
        	entityParam = graph.getChartProperties().get("seriesParam");
         valueParam = graph.getChartProperties().get("valueParam");
         
         //set the Data on the chart
         setData(resultSets);
     }
 
     /**
      * 
      * Set data on chart
      * 
      * @param resultSets Pentaho ResultSet with multi result sets from a query multi queries
      * @throws InvalidDataResultSetException when reult set is invalid
      */
     @Override
     public void setData(Map<String, ArrayList<IPentahoResultSet>> resultSets) throws InvalidDataResultSetException {
         if (resultSets == null)
             throw new InvalidDataResultSetException(InvalidDataResultSetException.ERROR_001, "Result Set is null");
 
         setData(resultSets.get("results").get(0));
 
         // get Data Set Metadata
         IPentahoMetaData metadata = getMetaData();
         // verify meta data
         int metadataSize = metadata.getColumnCount();
 
         if (metadataSize < 2)
             throw new InvalidDataResultSetException(InvalidDataResultSetException.ERROR_001, "less than 2");
 
         
         // set the categories
         int rowCount = getRowCount();
         for (int i = 0; i < rowCount; i++) {
             try {
                 // set category label
                 Entity ent = new Entity();
                 ent.setID(getDataValue(i, 0).toString());
                 ent.setValue(Double.parseDouble((getDataValue(i, 1).toString())));
                 if (chartLink != null) {
                     setChartLink(ent,i);
                 }
                 setEntityColor(ent,i);
                 
                 // set category in chart
                 graph.setEntity(i, ent);
             } catch (Exception e) {
                 log.error("Problem in result set. Null values found at index:" + i, e);
             }
         }
         
         // process the dial range values
         try {
 			setRangeValues(resultSets);
 		} catch (InvalidParameterException e) {
 			 log.error("Problem in result set of setRangeValues.", e);
 		}
         
     }
     
     
     
     /**
      * Set the Entity Color
      * 
      * if the parameter color was set in this chart, is used the value index X=seriesIndex
      * 
      * @param entity to set type
      * @param entityIndex entity index
      */
     protected void setEntityColor(Entity entity, int entityIndex) {
         String value = graph.getChartProperties().get(ENTITYCOLOR);
 
         // have the property ENTITYCOLOR
         if (value != null) {
             // split the values
             String color = (value.split(";"))[entityIndex];
             if (graph.getGraphType().isSingleSeries())
             	entity.setColor(color);
 
         }
     }
     
 
     /**
      * Allows setting chart links.
      * @param series Series where to set the link.
      * @param seriesIndex Index where to change.
      */
     private void setChartLink(Entity ent, Integer seriesIndex) {
 
         String serieChartLink = chartLink;
 
         // set entityParam
         if (entityParam != null) {
             serieChartLink = chartLink.replace("{" + entityParam + "}",ent.getId());
         }
         // set the value
         if (valueParam != null) {
             serieChartLink = serieChartLink.replace("{" + valueParam + "}", ent.getValue().toString());
         }
         
         ent.setEvent(serieChartLink);
     }
     
     /**
      * Render the chart XML.
      * @return XML for the chart.
      * @throws Exception
      */
     public String generateChart() throws Exception {
     	ChartFactoryMaps chart	= new ChartFactoryMaps(false);   
 		//attach graph to chart factory
 		chart.insertGraph(graph);
 		return chart.buildDOMFusionChart(graph.getGraphId()); 
     }
 
 }
