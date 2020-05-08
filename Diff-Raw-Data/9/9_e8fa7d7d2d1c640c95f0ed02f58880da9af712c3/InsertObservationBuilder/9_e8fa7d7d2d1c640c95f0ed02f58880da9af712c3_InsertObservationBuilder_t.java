 package com.axiomalaska.sos.xmlbuilder;
 
 import java.util.Calendar;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.axiomalaska.sos.data.Phenomenon;
 import com.axiomalaska.sos.data.Station;
 import com.axiomalaska.sos.data.ObservationCollection;
 
 /**
  * Builds a SOS InsertObservation XML String with the ObservationCollection and station
  * 
  * @author Lance Finfrock
  */
 public class InsertObservationBuilder extends SosXmlBuilder {
 
 	// -------------------------------------------------------------------------
 	// Private Data
 	// -------------------------------------------------------------------------
 	
 	private Station station;
 	private ObservationCollection values;
 	
 	// -------------------------------------------------------------------------
 	// Constructor
 	// -------------------------------------------------------------------------
 	
 	public InsertObservationBuilder(Station station, ObservationCollection values){
 		this.station = station;
 		this.values = values;
 	}
 	
 	// -------------------------------------------------------------------------
 	// Override Members
 	// -------------------------------------------------------------------------
 	
 	/**
 	 * Create InsertObservation XML
 	 * example:
 	 * <InsertObservation service="SOS" version="1.0.0" xsi:schemaLocation="http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosInsert.xsd http://www.opengis.net/sampling/1.0 http://schemas.opengis.net/sampling/1.0.0/sampling.xsd http://www.opengis.net/om/1.0 http://schemas.opengis.net/om/1.0.0/extensions/observationSpecialization_override.xsd">
 	 * 	<AssignedSensorId>urn:ogc:object:feature:Sensor:3234</AssignedSensorId>
 	 * 	<om:Observation>
 	 * 		<om:samplingTime>
 	 * 			<gml:TimePeriod xsi:type="gml:TimePeriodType">
 	 * 				<gml:beginPosition>2012-04-17T12:02:05-0000</gml:beginPosition>
 	 * 				<gml:endPosition>2012-04-17T20:02:05-0000</gml:endPosition>
 	 * 			</gml:TimePeriod>
 	 * 		</om:samplingTime>
 	 * 		<om:procedure xlink:href="urn:ogc:object:feature:Sensor:3234"/>
 	 * 		<om:featureOfInterest>
 	 * 			<gml:FeatureCollection>
 	 * 				<gml:featureMember>
 	 * 					<sa:SamplingPoint gml:id="foi_3234">
 	 * 						<gml:name>The sampling point at station: Anchorage Hillside - AOOS</gml:name>
 	 * 						<sa:sampledFeature xlink:href=""/>
 	 * 						<sa:position>
 	 * 							<gml:Point>
 	 * 								<gml:pos srsName="urn:ogc:def:crs:EPSG::4326">-143.0 63.0</gml:pos>
 	 * 							</gml:Point>
 	 * 						</sa:position>
 	 * 					</sa:SamplingPoint>
 	 * 				</gml:featureMember>
 	 * 			</gml:FeatureCollection>
 	 * 		</om:featureOfInterest>
 	 * 		<om:result>
 	 * 			<swe:DataArray>
 	 * 				<swe:elementCount>
 	 * 					<swe:Count>
 	 * 						<swe:value>3</swe:value>
 	 * 					</swe:Count>
 	 * 				</swe:elementCount>
 	 * 				<swe:elementType name="Components">
 	 * 					<swe:DataRecord>
 	 * 						<swe:field name="feature">
 	 * 							<swe:Text definition="urn:ogc:dahttp://www.opengis.net/def/property/OGC/0/FeatureOfInterestta:feature"/>
 	 * 						</swe:field>
 	 * 						<swe:field name="Time">
 	 * 							<swe:Time definition="urn:ogc:data:time:iso8601"/>
 	 * 						</swe:field>
 	 * 						<swe:field name="Air Temperature">
 	 * 							<swe:Quantity definition="urn:x-ogc:def:phenomenon:IOOS:0.0.1:air_temperature">
 	 * 								<swe:uom code="C"/>
 	 * 							</swe:Quantity>
 	 * 						</swe:field>
 	 * 					</swe:DataRecord>
 	 * 				</swe:elementType>
 	 * 				<swe:encoding>
 	 * 					<swe:TextBlock blockSeparator=";" decimalSeparator="." tokenSeparator=","/>
 	 * 				</swe:encoding>
 	 * 				<swe:values>
 	 * 					foi_3234,2012-04-17T20:02:05-0000,10.0;foi_3234,2012-04-17T16:02:05-0000,11.0;foi_3234,2012-04-17T12:02:05-0000,12.0;
 	 * 				</swe:values>
 	 * 			</swe:DataArray>
 	 * 		</om:result>
 	 * 	</om:Observation>
 	 * </InsertObservation>
 	 */
 	public String build() {
 		try{
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 		
 		Document doc = docBuilder.newDocument();
 		Element insertObservation = doc.createElement("InsertObservation");
 		insertObservation.setAttribute("xmlns", "http://www.opengis.net/sos/1.0");
 		insertObservation.setAttribute("xmlns:ows", "http://www.opengis.net/ows/1.1");
 		insertObservation.setAttribute("xmlns:ogc", "http://www.opengis.net/ogc");
 		insertObservation.setAttribute("xmlns:om", "http://www.opengis.net/om/1.0");
 		insertObservation.setAttribute("xmlns:sos", "http://www.opengis.net/sos/1.0");
 		insertObservation.setAttribute("xmlns:sa", "http://www.opengis.net/sampling/1.0");
 		insertObservation.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
 		insertObservation.setAttribute("xmlns:swe", "http://www.opengis.net/swe/1.0.1");
 		insertObservation.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
 		insertObservation.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
 		insertObservation.setAttribute("xsi:schemaLocation", "http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosInsert.xsd http://www.opengis.net/sampling/1.0 http://schemas.opengis.net/sampling/1.0.0/sampling.xsd http://www.opengis.net/om/1.0 http://schemas.opengis.net/om/1.0.0/extensions/observationSpecialization_override.xsd");
 		insertObservation.setAttribute("service", "SOS");
 		insertObservation.setAttribute("version", "1.0.0");
 		doc.appendChild(insertObservation);
 		
 		String truncatedProcedureId = "";
 		if(station.getProcedureId().length() > 100){
 			truncatedProcedureId = station.getProcedureId().substring(0, 100);
 		}
 		else{
 			truncatedProcedureId = station.getProcedureId();
 		}
 		
 		Element assignedSensorId = doc.createElement("AssignedSensorId");
 		assignedSensorId.appendChild(doc.createTextNode(truncatedProcedureId));
 		insertObservation.appendChild(assignedSensorId);
 		
 		Element observation = doc.createElement("om:Observation");
 		observation.appendChild(getSamplingTime(doc, values));
 		insertObservation.appendChild(observation);
 		
 		Element procedure = doc.createElement("om:procedure");
 		procedure.setAttribute("xlink:href", truncatedProcedureId);
 		observation.appendChild(procedure);
 		
 		observation.appendChild(createFeatureOfInterest(doc, station));
 		
 		observation.appendChild(createResult(doc, station, values));
 		
 		String xmlString = getString(doc);
         return xmlString;
 	  } catch (Exception ex) {
 		System.err.println(ex.getMessage());
 	  }
 		return null;
 	}
 	
 	// -------------------------------------------------------------------------
 	// Private Members
 	// -------------------------------------------------------------------------
 	
 	/**
 	 * Create the Result node
 	 * example:
 	 * <om:result>
 	 * 	<swe:DataArray>
 	 * 		<swe:elementCount>
 	 * 			<swe:Count>
 	 * 				<swe:value>3</swe:value>
 	 * 			</swe:Count>
 	 * 		</swe:elementCount>
 	 * 		<swe:elementType name="Components">
 	 * 			<swe:DataRecord>
 	 * 				<swe:field name="feature">
 	 * 					<swe:Text definition="urn:ogc:dahttp://www.opengis.net/def/property/OGC/0/FeatureOfInterestta:feature"/>
 	 * 				</swe:field>
 	 * 				<swe:field name="Time">
 	 * 					<swe:Time definition="urn:ogc:data:time:iso8601"/>
 	 * 				</swe:field>
 	 * 				<swe:field name="Air Temperature">
 	 * 					<swe:Quantity definition="urn:x-ogc:def:phenomenon:IOOS:0.0.1:air_temperature">
 	 * 						<swe:uom code="C"/>
 	 * 					</swe:Quantity>
 	 * 				</swe:field>
 	 * 			</swe:DataRecord>
 	 * 		</swe:elementType>
 	 * 		<swe:encoding>
 	 * 			<swe:TextBlock blockSeparator=";" decimalSeparator="." tokenSeparator=","/>
 	 * 		</swe:encoding>
 	 * 		<swe:values>
 	 * 			foi_3234,2012-04-17T20:02:05-0000,10.0;foi_3234,2012-04-17T16:02:05-0000,11.0;foi_3234,2012-04-17T12:02:05-0000,12.0;
 	 * 		</swe:values>
 	 * 	</swe:DataArray>
 	 * </om:result>
 	 */
 	private Node createResult(Document doc, Station station,
 			ObservationCollection valuesCollection) {
 		Element result = doc.createElement("om:result");
 		
 		Element dataArray = doc.createElement("swe:DataArray");
 		result.appendChild(dataArray);
 		
 		Element elementCount = doc.createElement("swe:elementCount");
 		dataArray.appendChild(elementCount);
 		
 		Element count = doc.createElement("swe:Count");
 		elementCount.appendChild(count);
 		
 		Element value = doc.createElement("swe:value");
 		value.appendChild(doc.createTextNode(getNumberOfValues(valuesCollection) + ""));
 		count.appendChild(value);
 		
 		dataArray.appendChild(buildElementTypeComponents(doc, valuesCollection));
 		
 		Element encoding = doc.createElement("swe:encoding");
 		dataArray.appendChild(encoding);
 		
 		Element textBlock = doc.createElement("swe:TextBlock");
 		textBlock.setAttribute("decimalSeparator", ".");
 		textBlock.setAttribute("tokenSeparator", ",");
 		textBlock.setAttribute("blockSeparator", ";");
 		encoding.appendChild(textBlock);
 		
 		Element values = doc.createElement("swe:values");
 		values.appendChild(buildValues(doc, station, valuesCollection));
 		dataArray.appendChild(values);
 		
 		return result;
 	}
 
 	/**
 	 * Get the number of values to be entered. 
 	 */
 	private int getNumberOfValues(ObservationCollection valuesCollection) {
 		return valuesCollection.getObservationDates().size();
 	}
 
 	/**
 	 * Builds the values list. Each set of values is separated by a semicolon. 
 	 * Each value in the set of values is separated by a comma. 
 	 * example:
 	 * foi_3234,2012-04-17T20:02:05-0000,10.0;foi_3234,2012-04-17T16:02:05-0000,11.0;foi_3234,2012-04-17T12:02:05-0000,12.0;
 	 */
 	private Node buildValues(Document doc, Station station,
 			ObservationCollection valuesCollection) {
 		
 		List<Calendar> dates = valuesCollection.getObservationDates();
 		List<Double> values = 
 				valuesCollection.getObservationValues();
 		
 		String text = "";
 		int size = getNumberOfValues(valuesCollection);
 		
 		String truncatedFeatureOfInterestId = "";
 		if(station.getFeatureOfInterestId().length() > 100){
 			truncatedFeatureOfInterestId = station.getFeatureOfInterestId().substring(0, 100);
 		}
 		else{
 			truncatedFeatureOfInterestId = station.getFeatureOfInterestId();
 		}
 		
 		for(int index = 0; index < size; index++){
 			Calendar date = dates.get(index);
 			Double value = values.get(index);
 			
 			text += truncatedFeatureOfInterestId + "," + 
 					formatCalendarIntoGMTTime(date) + "," + value + ";"; 
 		}
 		
 		return doc.createTextNode(text);
 	}
 
 	/**
 	 * Build the Element Type Components node
 	 * 
 	 * <swe:elementType name="Components">
 	 * 	<swe:DataRecord>
 	 * 		<swe:field name="feature">
 	 * 			<swe:Text definition="urn:ogc:dahttp://www.opengis.net/def/property/OGC/0/FeatureOfInterestta:feature"/>
 	 * 		</swe:field>
 	 * 		<swe:field name="Time">
 	 * 			<swe:Time definition="urn:ogc:data:time:iso8601"/>
 	 * 		</swe:field>
 	 * 		<swe:field name="Air Temperature">
 	 * 			<swe:Quantity definition="urn:x-ogc:def:phenomenon:IOOS:0.0.1:air_temperature">
 	 * 			<swe:uom code="C"/></swe:Quantity>
 	 * 		</swe:field>
 	 * 	</swe:DataRecord>
 	 * </swe:elementType>
 	 */
 	private Node buildElementTypeComponents(Document doc,
 			ObservationCollection valuesCollection) {
 		Element elementType = doc.createElement("swe:elementType");
 		elementType.setAttribute("name", "Components");
 		
 		Element dataRecord = doc.createElement("swe:DataRecord");
 		elementType.appendChild(dataRecord);
 		
 		Element fieldFeature = doc.createElement("swe:field");
 		fieldFeature.setAttribute("name", "feature");
 		dataRecord.appendChild(fieldFeature);
 		
 		Element text = doc.createElement("swe:Text");
 		text.setAttribute("definition", "urn:ogc:dahttp://www.opengis.net/def/property/OGC/0/FeatureOfInterestta:feature");
 		fieldFeature.appendChild(text);
 		
 		Element fieldTime = doc.createElement("swe:field");
 		fieldTime.setAttribute("name", "Time");
 		dataRecord.appendChild(fieldTime);
 		
 		Element time = doc.createElement("swe:Time");
 		time.setAttribute("definition", "urn:ogc:data:time:iso8601");
 		fieldTime.appendChild(time);
 		
 		Phenomenon phenomenon = valuesCollection.getPhenomenon();
 		
 		Element field = doc.createElement("swe:field");
 		field.setAttribute("name", phenomenon.getName());
 		dataRecord.appendChild(field);
 
 		Element quantity = doc.createElement("swe:Quantity");
 		if(station.getProcedureId().length() > 100){
 			String truncatedTag = phenomenon.getTag().substring(0, 100);
 			quantity.setAttribute("definition", truncatedTag);
 		}
 		else{
 			quantity.setAttribute("definition", phenomenon.getTag());
 		}
 		field.appendChild(quantity);
 
 		Element uom = doc.createElement("swe:uom");
		if(phenomenon.getUnits().length() > 30){
 			String truncatedUnits = phenomenon.getUnits().substring(0, 30);
 			uom.setAttribute("code", truncatedUnits);
 		}
 		else{
 			uom.setAttribute("code", phenomenon.getUnits());
 		}
 		quantity.appendChild(uom);
 		
 		return elementType;
 	}
 
 	/**
 	 * Create the Feature of Interest Node
 	 * example
 	 * <om:featureOfInterest>
 	 * 		<gml:FeatureCollection>
 	 * 			<gml:featureMember>
 	 * 				<sa:SamplingPoint gml:id="foi_3234">
 	 * 					<gml:name>The sampling point at station: Anchorage Hillside - AOOS</gml:name>
 	 * 					<sa:sampledFeature xlink:href=""/>
 	 * 					<sa:position>
 	 * 						<gml:Point>
 	 * 							<gml:pos srsName="urn:ogc:def:crs:EPSG::4326">-143.0 63.0</gml:pos>
 	 * 						</gml:Point>
 	 * 					</sa:position>
 	 * 				</sa:SamplingPoint>
 	 * 			</gml:featureMember>
 	 * 		</gml:FeatureCollection>
 	 * </om:featureOfInterest>
 	 * 
 	 * @param station - station to get information from
 	 */
 	private Node createFeatureOfInterest(Document doc, Station station) {
 		Element featureOfInterest = doc.createElement("om:featureOfInterest");
 		Element featureCollection = doc.createElement("gml:FeatureCollection");
 		featureOfInterest.appendChild(featureCollection);
 		
 		Element featureMember = doc.createElement("gml:featureMember");
 		featureCollection.appendChild(featureMember);
 		
 		Element samplingPoint = doc.createElement("sa:SamplingPoint");
 
 		if(station.getFeatureOfInterestId().length() > 100){
 			String truncatedFeatureOfInterestId = station.getFeatureOfInterestId().substring(0, 100);
 			samplingPoint.setAttribute("gml:id", truncatedFeatureOfInterestId);
 		}
 		else{
 			samplingPoint.setAttribute("gml:id", station.getFeatureOfInterestId());
 		}
 		featureMember.appendChild(samplingPoint);
 		
 		Element gmlName = doc.createElement("gml:name");
 		if(station.getFoiDescription().length() > 100){
 			String truncatedFoiDescription = station.getFoiDescription().substring(0, 100);
 			gmlName.appendChild(doc.createTextNode(truncatedFoiDescription));
 		}
 		else{
 			gmlName.appendChild(doc.createTextNode(station.getFoiDescription()));
 		}
 		samplingPoint.appendChild(gmlName);
 		
 		Element sampledFeature = doc.createElement("sa:sampledFeature");
 		sampledFeature.setAttribute("xlink:href", "");
 		samplingPoint.appendChild(sampledFeature);
 		
 		Element position = doc.createElement("sa:position");
 		samplingPoint.appendChild(position);
 		
 		Element point = doc.createElement("gml:Point");
 		position.appendChild(point);
 		
 		Element pos = doc.createElement("gml:pos");
 		pos.setAttribute("srsName", "urn:ogc:def:crs:EPSG::4326");
 		pos.appendChild(doc.createTextNode(station.getLongitude() + " " + station.getLatitude()));
 		point.appendChild(pos);
 		
 		return featureOfInterest;
 	}
 
 	/**
 	 * Create the samplingTime section
 	 * example:
 	 * <om:samplingTime>
 	 * 		<gml:TimePeriod xsi:type="gml:TimePeriodType">
 	 * 			<gml:beginPosition>2012-04-17T12:02:05-0000</gml:beginPosition>
 	 * 			<gml:endPosition>2012-04-17T20:02:05-0000</gml:endPosition>
 	 * 		</gml:TimePeriod>
 	 * </om:samplingTime>
 	 * @param doc
 	 * @param values
 	 * @return
 	 */
 	private Node getSamplingTime(Document doc, ObservationCollection values){
 		List<Calendar> dates = values.getObservationDates();
 		
 		Calendar firstDate = dates.get(0);
 		Calendar lastDate = dates.get(dates.size() - 1);
 		
 		Calendar startDate = null;
 		if (firstDate.after(lastDate)){
 			startDate = lastDate;
 		}
 		else{
 			startDate = firstDate;
 		}
 		
 		Calendar endDate = null;
 		if (firstDate.after(lastDate)) {
 			endDate = firstDate;
 		}
 		else {
 			endDate = lastDate;
 		}
 		
 		Element samplingTime = doc.createElement("om:samplingTime");
 		
 		Element timePeriod = doc.createElement("gml:TimePeriod");
 		timePeriod.setAttribute("xsi:type", "gml:TimePeriodType");
 		samplingTime.appendChild(timePeriod);
 		
 		Element beginPosition = doc.createElement("gml:beginPosition");
 		beginPosition.appendChild(doc.createTextNode(formatCalendarIntoGMTTime(startDate)));
 		timePeriod.appendChild(beginPosition);
 		
 		Element endPosition = doc.createElement("gml:endPosition");
 		endPosition.appendChild(doc.createTextNode(formatCalendarIntoGMTTime(endDate)));
 		timePeriod.appendChild(endPosition);
 		
 		return samplingTime;
 	}
 }
