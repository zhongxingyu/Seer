 package com.axiomalaska.sos.xmlbuilder;
 
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.axiomalaska.sos.data.Phenomenon;
 import com.axiomalaska.sos.data.Station;
 
 /**
  * Builds a SOS RegisterSensor XML String used to add a station to the SOS server
  * 
  * @author Lance Finfrock
  */
 public class RegisterSensorBuilder extends SosXmlBuilder  {
 
   // ---------------------------------------------------------------------------
   // Private Data
   // ---------------------------------------------------------------------------
 
 	private Station station;
 	
   // ---------------------------------------------------------------------------
   // Constructor
   // ---------------------------------------------------------------------------
 
 	public RegisterSensorBuilder(Station station){
 		this.station = station;
 	}
 	
   // ---------------------------------------------------------------------------
   // Public Members
   // ---------------------------------------------------------------------------
 	
 	/**
 	 * Build the XML String
 	 */
 	public String build() {
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
 			Document doc = docBuilder.newDocument();
 			Element registerSensor = doc.createElement("RegisterSensor");
 			registerSensor.setAttribute("service", "SOS");
 			registerSensor.setAttribute("version", "1.0.0");
 			registerSensor.setAttribute("xmlns", "http://www.opengis.net/sos/1.0");
 			registerSensor.setAttribute("xmlns:swe", "http://www.opengis.net/swe/1.0.1");
 			registerSensor.setAttribute("xmlns:ows", "http://www.opengeospatial.net/ows");
 			registerSensor.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
 			registerSensor.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
 			registerSensor.setAttribute("xmlns:ogc", "http://www.opengis.net/ogc");
 			registerSensor.setAttribute("xmlns:om", "http://www.opengis.net/om/1.0");
 			registerSensor.setAttribute("xmlns:sml", "http://www.opengis.net/sensorML/1.0.1");
 			registerSensor.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
 			registerSensor.setAttribute("xsi:schemaLocation", "http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosRegisterSensor.xsd http://www.opengis.net/om/1.0 http://schemas.opengis.net/om/1.0.0/extensions/observationSpecialization_override.xsd");
 			doc.appendChild(registerSensor);
 
 			Element sensorDescription = doc.createElement("SensorDescription");
 			registerSensor.appendChild(sensorDescription);
 			
 			Element sensorML = doc.createElement("sml:SensorML");
 			sensorML.setAttribute("version", "1.0.1");
 			sensorDescription.appendChild(sensorML);
 			
 			Element description = doc.createElement("gml:description");
 			description.appendChild(doc.createTextNode(station.getDescription()));
 			sensorML.appendChild(description);
 			
 			Element member = doc.createElement("sml:member");
 			sensorML.appendChild(member);
 			
 			Element system = doc.createElement("sml:System");
 			system.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
 			member.appendChild(system);
 			
 			system.appendChild(createIdentificationNode(doc, station));
 			
 			system.appendChild(createCapabilitiesNode(doc));
 			
 			system.appendChild(createPositionNode(doc, station));
 			
 			system.appendChild(createInputsNode(doc, station));
 			
 			system.appendChild(createOutputsNode(doc, station));
 			
 			registerSensor.appendChild(createObservationTemplate(doc));
 			
 			String xmlString = getString(doc);
 			
 			return xmlString;
 		} catch (Exception ex) {
 			System.err.println(ex.getMessage());
 		}
 		return null;
 	}
 	
   // ---------------------------------------------------------------------------
   // Private Members
   // ---------------------------------------------------------------------------
 
 	/**
 	 *  <ObservationTemplate>
           <om:Measurement>
             <om:samplingTime/>
             <om:procedure/>
             <om:observedProperty/>
             <om:featureOfInterest></om:featureOfInterest>
             <om:result uom=""></om:result>
           </om:Measurement>
         </ObservationTemplate>
 	 */
 	private Node createObservationTemplate(Document doc) {
 		Element observationTemplate = doc.createElement("ObservationTemplate");
 		
 		Element measurement = doc.createElement("om:Measurement");
 		observationTemplate.appendChild(measurement);
 		
 		Element samplingTime = doc.createElement("om:samplingTime");
 		measurement.appendChild(samplingTime);
 		
 		Element procedure = doc.createElement("om:procedure");
 		measurement.appendChild(procedure);
 		
 		Element observedProperty = doc.createElement("om:observedProperty");
 		measurement.appendChild(observedProperty);
 		
 		Element featureOfInterest = doc.createElement("om:featureOfInterest");
 		measurement.appendChild(featureOfInterest);
 		
 		Element result = doc.createElement("om:result");
 		result.setAttribute("uom", "");
 		measurement.appendChild(result);
 		return observationTemplate;
 	}
 
 	private Node createOutputsNode(Document doc, Station station) {
 		List<Phenomenon> phenomenons = station.getPhenomena();
 		Element outputs = doc.createElement("sml:outputs");
 		
 		Element outputList = doc.createElement("sml:OutputList");
 		outputs.appendChild(outputList);
 		
 		for(Phenomenon phenomenon : phenomenons){
 			Element output = doc.createElement("sml:output");
 			output.setAttribute("name", phenomenon.getName());
 			outputList.appendChild(output);
 			
 			Element quantity = doc.createElement("swe:Quantity");
 			
 			if(station.getProcedureId().length() > 100){
 				String truncatedTag = phenomenon.getTag().substring(0, 100);
 				quantity.setAttribute("definition", truncatedTag);
 			}
 			else{
 				quantity.setAttribute("definition", phenomenon.getTag());
 			}
 			
 			output.appendChild(quantity);
 			
 			Element metaDataProperty = doc.createElement("gml:metaDataProperty");
 			quantity.appendChild(metaDataProperty);
 			
 			Element offering = doc.createElement("offering");
 			metaDataProperty.appendChild(offering);
 			
 			Element id = doc.createElement("id");
 			id.appendChild(doc.createTextNode("network-All"));
 			offering.appendChild(id);
 			
 			Element name = doc.createElement("name");
 			name.appendChild(doc.createTextNode("Includes all the sensors in the network"));
 			offering.appendChild(name);
 			
 			Element uom = doc.createElement("swe:uom");
			if(phenomenon.getUnits().length() > 30){
 				String truncatedUnits = phenomenon.getUnits().substring(0, 30);
 				uom.setAttribute("code", truncatedUnits);
 			}
 			else{
 				uom.setAttribute("code", phenomenon.getUnits());
 			}
 			quantity.appendChild(uom);
 		}
 		
 		return outputs;
 	}
 
 	private Node createInputsNode(Document doc, Station station) {
 		List<Phenomenon> phenomenons = station.getPhenomena();
 		
 		Element inputs = doc.createElement("sml:inputs");
 		
 		Element inputList = doc.createElement("sml:InputList");
 		inputs.appendChild(inputList);
 		
 		for(Phenomenon phenomenon : phenomenons){
 			Element input = doc.createElement("sml:input");
 			input.setAttribute("name", phenomenon.getName());
 			inputList.appendChild(input);
 			
 			Element observableProperty = doc.createElement("swe:ObservableProperty");
 			if(station.getProcedureId().length() > 100){
 				String truncatedTag = phenomenon.getTag().substring(0, 100);
 				observableProperty.setAttribute("definition", truncatedTag);
 			}
 			else{
 				observableProperty.setAttribute("definition", phenomenon.getTag());
 			}
 			
 			input.appendChild(observableProperty);
 		}
 		
 		return inputs;
 	}
 
 	private Node createPositionNode(Document doc, Station station) {
 		Element position = doc.createElement("sml:position");
 		position.setAttribute("name", "sensorPosition");
 		
 		Element swePosition = doc.createElement("swe:Position");
 		swePosition.setAttribute("referenceFrame", "urn:ogc:def:crs:EPSG::4326");
 		position.appendChild(swePosition);
 		
 		Element location = doc.createElement("swe:location");
 		swePosition.appendChild(location);
 		
 		Element vector = doc.createElement("swe:Vector");
 		vector.setAttribute("gml:id", "STATION_LOCATION");
 		location.appendChild(vector);
 		
 		Element eastingCoordinate = doc.createElement("swe:coordinate");
 		eastingCoordinate.setAttribute("name", "easting");
 		vector.appendChild(eastingCoordinate);
 		
 		Element eastingQuantity = doc.createElement("swe:Quantity");
 		eastingCoordinate.appendChild(eastingQuantity);
 		
 		Element eastingUom = doc.createElement("swe:Quantity");
 		eastingUom.setAttribute("code", "degree");
 		eastingQuantity.appendChild(eastingUom);
 		
 		Element eastingValue = doc.createElement("swe:value");
 		eastingValue.appendChild(doc.createTextNode(station.getLongitude() + ""));
 		eastingQuantity.appendChild(eastingValue);
 		
 		Element northingCoordinate = doc.createElement("swe:coordinate");
 		northingCoordinate.setAttribute("name", "northing");
 		vector.appendChild(northingCoordinate);
 		
 		Element northingQuantity = doc.createElement("swe:Quantity");
 		northingCoordinate.appendChild(northingQuantity);
 		
 		Element northingUom = doc.createElement("swe:Quantity");
 		northingUom.setAttribute("code", "degree");
 		northingQuantity.appendChild(northingUom);
 		
 		Element northingValue = doc.createElement("swe:value");
 		northingValue.appendChild(doc.createTextNode(station.getLatitude() + ""));
 		northingQuantity.appendChild(northingValue);
 		
 		Element altitudeCoordinate = doc.createElement("swe:coordinate");
 		altitudeCoordinate.setAttribute("name", "altitude");
 		vector.appendChild(altitudeCoordinate);
 		
 		Element altitudeQuantity = doc.createElement("swe:Quantity");
 		altitudeCoordinate.appendChild(altitudeQuantity);
 		
 		Element altitudeUom = doc.createElement("swe:Quantity");
 		altitudeUom.setAttribute("code", "m");
 		altitudeQuantity.appendChild(altitudeUom);
 		
 		Element altitudeValue = doc.createElement("swe:value");
 		altitudeValue.appendChild(doc.createTextNode("0.0"));
 		altitudeQuantity.appendChild(altitudeValue);
 		
 		return position;
 	}
 
 	private Node createCapabilitiesNode(Document doc) {
 		Element capabilities = doc.createElement("sml:capabilities");
 		
 		Element simpleDataRecord = doc.createElement("swe:SimpleDataRecord");
 		capabilities.appendChild(simpleDataRecord);
 		
 		Element statusField = doc.createElement("swe:field");
 		statusField.setAttribute("name", "status");
 		simpleDataRecord.appendChild(statusField);
 		
 		Element statusBoolean = doc.createElement("swe:Boolean");
 		statusField.appendChild(statusBoolean);
 		
 		Element statusValue = doc.createElement("swe:value");
 		statusValue.appendChild(doc.createTextNode("true"));
 		statusBoolean.appendChild(statusValue);
 		
 		Element mobileField = doc.createElement("swe:field");
 		mobileField.setAttribute("name", "mobile");
 		simpleDataRecord.appendChild(mobileField);
 		
 		Element mobileBoolean = doc.createElement("swe:Boolean");
 		mobileField.appendChild(mobileBoolean);
 		
 		Element mobileValue = doc.createElement("swe:value");
 		mobileValue.appendChild(doc.createTextNode("false"));
 		mobileBoolean.appendChild(mobileValue);
 		
 		return capabilities;
 	}
 
 	/**
 	 * Produces the XML below
         <sml:capabilities>
           <swe:SimpleDataRecord>
             <swe:field name="status">
               <swe:Boolean>
                 <swe:value>true</swe:value>
               </swe:Boolean>
             </swe:field>
             <swe:field name="mobile">
               <swe:Boolean>
                 <swe:value>false</swe:value>
               </swe:Boolean>
             </swe:field>
           </swe:SimpleDataRecord>
         </sml:capabilities>
 	 */
 	private Node createIdentificationNode(Document doc, Station station) {
 		Element identification = doc.createElement("sml:identification");
 	
 		Element identifierList = doc.createElement("sml:IdentifierList");
 		identification.appendChild(identifierList);
 		
 		Element identifier = doc.createElement("sml:identifier");
 		identifierList.appendChild(identifier);
 		
 		Element term = doc.createElement("sml:Term");
 		term.setAttribute("definition", "urn:ogc:def:identifier:OGC:uniqueID");
 		identifier.appendChild(term);
 		
 		Element value = doc.createElement("sml:value");
 		if(station.getProcedureId().length() > 100){
 			String truncatedProcedureId = station.getProcedureId().substring(0, 100);
 			value.appendChild(doc.createTextNode(truncatedProcedureId));
 		}
 		else{
 			value.appendChild(doc.createTextNode(station.getProcedureId()));
 		}
 		term.appendChild(value);
 		
 		return identification;
 	}
 }
