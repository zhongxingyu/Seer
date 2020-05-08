 package com.axiomalaska.sos.xmlbuilder;
 
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.axiomalaska.sos.data.PublisherInfo;
 import com.axiomalaska.sos.data.SosNetwork;
 import com.axiomalaska.sos.data.SosSource;
 import com.axiomalaska.sos.data.SosStation;
 import com.axiomalaska.sos.tools.IdCreator;
 
 public class StationRegisterSensorBuilder extends SosXmlBuilder  {
 
   // ---------------------------------------------------------------------------
   // Private Data
   // ---------------------------------------------------------------------------
 
 	private SosStation station;
 	private IdCreator idCreator;
 	private PublisherInfo publisherInfo;
 	
   // ---------------------------------------------------------------------------
   // Constructor
   // ---------------------------------------------------------------------------
 
 	public StationRegisterSensorBuilder(SosStation station, IdCreator idCreator, 
 			PublisherInfo publisherInfo){
 		this.station = station;
 		this.idCreator = idCreator;
 		this.publisherInfo = publisherInfo;
 	}
 	
   // ---------------------------------------------------------------------------
   // Public Members
   // ---------------------------------------------------------------------------
 	
 	/**
 	 * Build the XML String
 	 * 
 		<RegisterSensor service="SOS" version="1.0.0"
 		  xmlns="http://www.opengis.net/sos/1.0"
 		  xmlns:swe="http://www.opengis.net/swe/1.0.1"
 		  xmlns:ows="http://www.opengeospatial.net/ows"
 		  xmlns:xlink="http://www.w3.org/1999/xlink"
 		  xmlns:gml="http://www.opengis.net/gml"
 		  xmlns:ogc="http://www.opengis.net/ogc"
 		  xmlns:om="http://www.opengis.net/om/1.0"
 		  xmlns:sml="http://www.opengis.net/sensorML/1.0.1"
 		  xmlns:sa="http://www.opengis.net/sampling/1.0"
 		  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 		  xsi:schemaLocation="http://www.opengis.net/sos/1.0
 		  http://schemas.opengis.net/sos/1.0.0/sosRegisterSensor.xsd
 		  http://www.opengis.net/om/1.0
 		  http://schemas.opengis.net/om/1.0.0/extensions/observationSpecialization_override.xsd">        
 		  <SensorDescription>
 		    <sml:SensorML xmlns:sml="http://www.opengis.net/sensorML/1.0.1" 
 		    			xmlns="http://www.opengis.net/sos/1.0" 
 		    			xmlns:gml="http://www.opengis.net/gml" 
 		    			xmlns:ogc="http://www.opengis.net/ogc" 
 		    			xmlns:om="http://www.opengis.net/om/1.0" 
 		    			xmlns:ows="http://www.opengeospatial.net/ows" 
 		    			xmlns:swe="http://www.opengis.net/swe/1.0.1" 
 		    			xmlns:xlink="http://www.w3.org/1999/xlink" 
 		    			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
 		    			version="1.0.1" xsi:schemaLocation="http://www.opengis.net/sensorML/1.0.1 http://schemas.opengis.net/sensorML/1.0.1/sensorML.xsd">
 		      <sml:member>
 		        <sml:System>
 		           <gml:description/>
                    <gml:name>urn:ioos:station:hads:172C7508</gml:name>
                    <!-- ======= STATION IDENTIFIERS ======= -->
 				   <sml:identification>
 				      <sml:IdentifierList>
 					     <sml:identifier name="stationID">
 					        <sml:Term definition="urn:ogc:def:identifier:OGC:uniqueID">
 					           <sml:value>urn:ioos:station:hads:172C7508</sml:value>
 					        </sml:Term>
 					     </sml:identifier>
 					     <sml:identifier name="shortName">
 					        <sml:Term definition="urn:ogc:def:identifier:OGC:shortName">
 					           <sml:value>172C7508</sml:value>
 					        </sml:Term>
 					     </sml:identifier>
 					     <sml:identifier name="longName">
 					        <sml:Term definition="urn:ogc:def:identifier:OGC:longName">
 					           <sml:value>BEAVER CREEK ABOVE VICTORIA CREEK NEAR BEAVER 43SE</sml:value>
 					        </sml:Term>
 					     </sml:identifier>
 					  </sml:IdentifierList>
 				   </sml:identification>
 				   <!-- ======= STATION CLASSIFIERS ======= -->
                    <sml:classification>
 				      <sml:ClassifierList>
 					     <sml:classifier name="platformType">
 					        <sml:Term definition="urn:ioos:def:classifier:IOOS:platformType">
 					           <sml:codeSpace xlink:href="http://mmisw.org/ont/ioos/platform"/>
 					           <sml:value>FIXED MET STATION</sml:value>
 					        </sml:Term>
 					     </sml:classifier>
 					     <sml:classifier name="operatorSector">
 					        <sml:Term definition="urn:ioos:def:classifier:IOOS:operatorSector">
 					           <sml:codeSpace xlink:href="http://mmisw.org/ont/ioos/sector"/>
 					           <sml:value>Government–Federal</sml:value>
 					        </sml:Term>
 					     </sml:classifier>
 					     <sml:classifier name="publisher">
 					        <sml:Term definition="urn:ioos:def:classifier:IOOS:publisher">
 					           <sml:codeSpace xlink:href="http://mmisw.org/ont/ioos/organization"/>
 					           <sml:value>HADS</sml:value>
 					        </sml:Term>
 					     </sml:classifier>
 					     <sml:classifier name="parentNetwork">
 					        <sml:Term definition="urn:ioos:def:classifier:IOOS:parentNetwork">
 					           <sml:codeSpace xlink:href="http://mmisw.org/ont/ioos/organization"/>
 					           <sml:value>AOOS</sml:value>
 					        </sml:Term>
 					     </sml:classifier>
 					  </sml:ClassifierList>
 				   </sml:classification>
 				   <!-- ======= CONTACTS ======= -->
 				   <sml:contact xlink:role="urn:ogc:def:classifier:OGC:contactType:operator">
 				      <sml:ResponsibleParty>
 				         <sml:organizationName>HADS</sml:organizationName>
 				         <sml:contactInfo>
 				            <sml:address>
 				               <sml:country>USA</sml:country>
 				               <sml:electronicMailAddress>HDSC.questions@noaa.gov</sml:electronicMailAddress>
 				            </sml:address>
 				            <sml:onlineResource xlink:href="http://dipper.nws.noaa.gov/hdsc/pfds/"/>
 				         </sml:contactInfo>
 				      </sml:ResponsibleParty>
 				   </sml:contact>
 				   <sml:contact xlink:role="urn:ogc:def:classifier:OGC:contactType:publisher">
 				      <sml:ResponsibleParty>
 				         <sml:organizationName>AOOS</sml:organizationName>
 				         <sml:contactInfo>
 				            <sml:address>
 				               <sml:country>USA</sml:country>
 				               <sml:electronicMailAddress>lance@axiomalaska.com</sml:electronicMailAddress>
 				            </sml:address>
 				            <sml:onlineResource xlink:href="http://www.aoos.org"/>
 				         </sml:contactInfo>
 				      </sml:ResponsibleParty>
 				   </sml:contact>
 				   <!-- ======= LOCATION ======= -->
                    <sml:location>
                      <gml:Point srsName="http://www.opengis.net/def/crs/EPSG/0/4326">
                        <gml:pos>34.7 -72.73</gml:pos>
                      </gml:Point>
                    </sml:location>				   
 			    </sml:System>
 		      </sml:member>
 		    </sml:SensorML>
 		  </SensorDescription>
 		  <!-- ObservationTemplate parameter; this has to be an empty measurement at the moment, as the 52N SOS only supports Measurements to be inserted -->
 		  <ObservationTemplate>
 		    <om:Measurement>
 		      <om:samplingTime/>
 		      <om:procedure/>
 		      <om:observedProperty/>
 		      <om:featureOfInterest>
 		        <sa:SamplingPoint gml:id="foi-pilot-rock">
 		          <gml:description>Pilot Rock Station, AK</gml:description>
 		          <gml:name>Pilot Rock, AK</gml:name>
 		          <sa:sampledFeature/>
 		          <sa:position>
 		            <gml:Point>
 		              <gml:pos srsName="http://www.opengis.net/def/crs/EPSG/0/4326">59.742 -149.470</gml:pos>
 		            </gml:Point>
 		          </sa:position>
 		        </sa:SamplingPoint>
 		      </om:featureOfInterest>
 		      <om:result xsi:type="gml:MeasureType" uom="">0.0</om:result>
 		    </om:Measurement>
 		  </ObservationTemplate>
 		</RegisterSensor>
 	 */
 	public String build() {
 		try {
 			DocumentBuilderFactory docFactory = 
 					DocumentBuilderFactory.newInstance();
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
 			registerSensor.setAttribute("xmlns:sa", "http://www.opengis.net/sampling/1.0");
 			registerSensor.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
 			registerSensor.setAttribute("xsi:schemaLocation", "http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosRegisterSensor.xsd http://www.opengis.net/om/1.0 http://schemas.opengis.net/om/1.0.0/extensions/observationSpecialization_override.xsd");
 			doc.appendChild(registerSensor);
 
 			Element sensorDescription = doc.createElement("SensorDescription");
 			registerSensor.appendChild(sensorDescription);
 			
 			Element sensorML = doc.createElement("sml:SensorML");
 			sensorML.setAttribute("xmlns:sml", "http://www.opengis.net/sensorML/1.0.1");
 			sensorML.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
 			sensorML.setAttribute("xmlns:swe", "http://www.opengis.net/swe/1.0.1");
 			sensorML.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
 			sensorML.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
 			sensorML.setAttribute("xsi:schemaLocation", "http://www.opengis.net/sensorML/1.0.1 http://schemas.opengis.net/sensorML/1.0.1/sensorML.xsd");
 			sensorML.setAttribute("version", "1.0.1");
 			sensorDescription.appendChild(sensorML);
 			
 			Element member = doc.createElement("sml:member");
 			sensorML.appendChild(member);
 			
 			Element system = doc.createElement("sml:System");
 			member.appendChild(system);
 			
 			system.appendChild(createDescriptionNode(doc, station));
 			
 			system.appendChild(createNameNode(doc, station));
 			
 			system.appendChild(doc.createComment("======= STATION IDENTIFIERS ======="));
 			
 			system.appendChild(createIdentificationNode(doc, station));
 			
 			system.appendChild(doc.createComment("======= STATION CLASSIFIERS ======="));
 			system.appendChild(createClassificationNode(doc, station, station.getSource()));
 			
 			if(station.getNetworks().size() > 0){
 				system.appendChild(createParentProcedures(doc, station.getNetworks()));
 			}
 			
 			system.appendChild(doc.createComment("======= CONTACTS ======="));
 			system.appendChild(createContactOperatorNode(doc, station.getSource()));
 			
 			system.appendChild(createContactPublisherNode(doc));
 			
 			system.appendChild(doc.createComment("======= LOCATION ======="));
 			
 			system.appendChild(createLocationNode(doc, station));
 			
 			registerSensor.appendChild(createObservationTemplate(doc, station));
 			
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
 	    <sml:contact xlink:role="http://mmisw.org/ont/ioos/definition/operator">
 		   <sml:ResponsibleParty>
 		      <sml:organizationName>OPERATOR ORGANIZATION</sml:organizationName>
 		      <sml:contactInfo>
 		         <sml:address>
 		            <sml:country>COUNTRY [USA, COUNTRY NAME, OR "NON-USA"]</sml:country>
 		            <sml:electronicMailAddress>EMAIL</sml:electronicMailAddress>
 		         </sml:address>
 		         <sml:onlineResource xlink:href="http://pnw.buoyoperator.org"/>
 		      </sml:contactInfo>
 		   </sml:ResponsibleParty>
 	    </sml:contact>
 	 */
 	private Node createContactOperatorNode(Document doc, SosSource source){
 		Element contact = doc.createElement("sml:contact");
 		contact.setAttribute("xlink:role", "http://mmisw.org/ont/ioos/definition/operator");
 
 		Element responsibleParty = doc.createElement("sml:ResponsibleParty");
 		contact.appendChild(responsibleParty);
 		
 		Element organizationName = doc.createElement("sml:organizationName");
 		responsibleParty.appendChild(organizationName);
 		organizationName.appendChild(doc.createTextNode(source.getName()));
 
 		Element contactInfo = doc.createElement("sml:contactInfo");
 		responsibleParty.appendChild(contactInfo);
 		
 		Element address = doc.createElement("sml:address");
 		contactInfo.appendChild(address);
 		
 		Element deliveryPoint = doc.createElement("sml:deliveryPoint");
 		deliveryPoint.appendChild(doc.createTextNode(source.getAddress()));
 		address.appendChild(deliveryPoint);
 		
 		Element city = doc.createElement("sml:city");
 		city.appendChild(doc.createTextNode(source.getCity()));
 		address.appendChild(city);
 		
 		Element state = doc.createElement("sml:administrativeArea");
 		state.appendChild(doc.createTextNode(source.getState()));
 		address.appendChild(state);
 		
 		Element zipcode = doc.createElement("sml:postalCode");
 		zipcode.appendChild(doc.createTextNode(source.getZipcode()));
 		address.appendChild(zipcode);
 		
 		Element country = doc.createElement("sml:country");
 		country.appendChild(doc.createTextNode(source.getCountry()));
 		address.appendChild(country);
 		
 		Element electronicMailAddress = doc.createElement("sml:electronicMailAddress");
 		electronicMailAddress.appendChild(doc.createTextNode(source.getEmail()));
 		address.appendChild(electronicMailAddress);
 		
 		Element onlineResource = doc.createElement("sml:onlineResource");
 		onlineResource.setAttribute("xlink:href", source.getWebAddress());
 		contactInfo.appendChild(onlineResource);
 
 		return contact;
 	}
 	
 	/**
     <sml:contact xlink:role="http://mmisw.org/ont/ioos/definition/publisher">
 	   <sml:ResponsibleParty>
 	      <sml:organizationName>NANOOS</sml:organizationName>
 	      <sml:contactInfo>
 	         <sml:address>
 	            <sml:country>USA</sml:country>
 	            <sml:electronicMailAddress>mayorga@apl.washington.edu</sml:electronicMailAddress>
 	         </sml:address>
 	         <sml:onlineResource xlink:href="http://nanoos.org"/>
 	      </sml:contactInfo>
        </sml:ResponsibleParty>
     </sml:contact>
 	 */
 	private Node createContactPublisherNode(Document doc){
 		Element contact = doc.createElement("sml:contact");
 		contact.setAttribute("xlink:role", "http://mmisw.org/ont/ioos/definition/publisher");
 
 		Element responsibleParty = doc.createElement("sml:ResponsibleParty");
 		contact.appendChild(responsibleParty);
 		
 		Element organizationName = doc.createElement("sml:organizationName");
 		responsibleParty.appendChild(organizationName);
 		organizationName.appendChild(doc.createTextNode(publisherInfo.getName()));
 
 		Element contactInfo = doc.createElement("sml:contactInfo");
 		responsibleParty.appendChild(contactInfo);
 		
 		Element address = doc.createElement("sml:address");
 		contactInfo.appendChild(address);
 		
 		Element country = doc.createElement("sml:country");
 		country.appendChild(doc.createTextNode(publisherInfo.getCountry()));
 		address.appendChild(country);
 		
 		Element electronicMailAddress = doc.createElement("sml:electronicMailAddress");
 		electronicMailAddress.appendChild(doc.createTextNode(publisherInfo.getEmail()));
 		address.appendChild(electronicMailAddress);
 		
 		Element onlineResource = doc.createElement("sml:onlineResource");
 		onlineResource.setAttribute("xlink:href", publisherInfo.getWebAddress());
 		contactInfo.appendChild(onlineResource);
 
 		return contact;
 	}
 	
 	/**
 	 * Produces the XML below
 		 <sml:classification>
 			<sml:ClassifierList>
 			  <sml:classifier name="platformType">
 			    <sml:Term definition="http://mmisw.org/ont/ioos/definition/platformType">
 			      <sml:codeSpace xlink:href="http://mmisw.org/ont/ioos/platform"/>
 			      <sml:value>buoy</sml:value>
 			    </sml:Term>
 			  </sml:classifier>
 			  <sml:classifier name="operatorSector">
 			    <sml:Term definition="http://mmisw.org/ont/ioos/definition/operatorSector">
 			      <sml:codeSpace xlink:href="http://mmisw.org/ont/ioos/sector"/>
 			      <sml:value>academic</sml:value>
 			    </sml:Term>
 			  </sml:classifier>
 			  <sml:classifier name="publisher">
 			    <sml:Term definition="http://mmisw.org/ont/ioos/definition/publisher">
 			      <sml:codeSpace xlink:href="http://mmisw.org/ont/ioos/organizationm"/>
 			      <sml:value>RAWS</sml:value>
 			    </sml:Term>
 			  </sml:classifier>
 			  <sml:classifier name="parentNetwork">
 			    <sml:Term definition="http://mmisw.org/ont/ioos/definition/parentNetwork">
 			      <sml:codeSpace xlink:href="http://mmisw.org/ont/ioos/organization"/>
 			      <sml:value>AOOS</sml:value>
 			    </sml:Term>
 			  </sml:classifier>
 			</sml:ClassifierList>
 		 </sml:classification>
 	 */
 	private Node createClassificationNode(Document doc, SosStation station, SosSource source) {
 		Element classification = doc.createElement("sml:classification");
 	
 		Element classifierList = doc.createElement("sml:ClassifierList");
 		classification.appendChild(classifierList);
 		
 		classifierList.appendChild(createClassifierNode(doc, "platformType", 
 				"http://mmisw.org/ont/ioos/definition/platformType", "http://mmisw.org/ont/ioos/platform",
 				station.getPlatformType()));
 		
 
 		classifierList.appendChild(createClassifierNode(doc, "operatorSector", 
 				"http://mmisw.org/ont/ioos/definition/operatorSector", "http://mmisw.org/ont/ioos/sector",
 				source.getOperatorSector()));
 		
 		classifierList.appendChild(createClassifierNode(doc, "publisher", 
 				"http://mmisw.org/ont/ioos/definition/publisher", "http://mmisw.org/ont/ioos/organization",
 				source.getName()));
 		
 		classifierList.appendChild(createClassifierNode(doc, "parentNetwork", 
 				"http://mmisw.org/ont/ioos/definition/parentNetwork", "http://mmisw.org/ont/ioos/organization",
 				publisherInfo.getName()));
 		
 		return classification;
 	}
 	
 	/**
 	  <sml:classifier name="platformType">
 	    <sml:Term definition="http://mmisw.org/ont/ioos/definition/platformType">
 	      <sml:codeSpace xlink:href="http://mmisw.org/ont/ioos/platform"/>
 	      <sml:value>buoy</sml:value>
 	    </sml:Term>
 	  </sml:classifier>
 	 */
 	private Node createClassifierNode(Document doc, String name,
 			String definition, String codeSpaceXlinkHref, String value) {
 		Element classifier = doc.createElement("sml:classifier");
 
 		classifier.setAttribute("name", name);
 
 		Element term = doc.createElement("sml:Term");
 		term.setAttribute("definition", definition);
 		classifier.appendChild(term);
 
 		Element codeSpace = doc.createElement("sml:codeSpace");
 		codeSpace.setAttribute("xlink:href", codeSpaceXlinkHref);
 		term.appendChild(codeSpace);
 		
 		Element valueElement = doc.createElement("sml:value");
 		valueElement.appendChild(doc.createTextNode(value));
 		term.appendChild(valueElement);
 
 		return classifier;
 	}
 	
 	/**
 	 * <gml:description>STATION DESCRIPTION</gml:description>
 	 */
 	private Node createDescriptionNode(Document doc, SosStation station) {
 		Element description = doc.createElement("gml:description");
 		description.appendChild(doc.createTextNode(station.getDescription()));
 		return description;
 	}
 	
 	/**
 	 * <gml:name>urn:ogc:object:feature:Sensor:IFGI:ifgi-sensor-90</gml:name>
 	 */
 	private Node createNameNode(Document doc, SosStation station) {
 		Element name = doc.createElement("gml:name");
 		name.appendChild(doc.createTextNode(idCreator.createStationId(station)));
 		return name;
 	}
 	
 	/**
 	  <!-- ObservationTemplate parameter; this has to be an empty measurement at the moment, as the 52N SOS only supports Measurements to be inserted -->
 	  <ObservationTemplate>
 	    <om:Measurement>
 	      <om:samplingTime/>
 	      <om:procedure/>
 	      <om:observedProperty/>
 	      <om:featureOfInterest>
 	        <sa:SamplingPoint gml:id="foi-pilot-rock">
 	          <gml:description>Pilot Rock Station, AK</gml:description>
 	          <gml:name>Pilot Rock, AK</gml:name>
 	          <sa:sampledFeature/>
 	          <sa:position>
 	            <gml:Point>
 	              <gml:pos srsName="http://www.opengis.net/def/crs/EPSG/0/4326">59.742 -149.470</gml:pos>
 	            </gml:Point>
 	          </sa:position>
 	        </sa:SamplingPoint>
 	      </om:featureOfInterest>
 	      <om:result xsi:type="gml:MeasureType" uom="">0.0</om:result>
 	    </om:Measurement>
 	  </ObservationTemplate>
 	 */
 	private Node createObservationTemplate(Document doc, SosStation station) {
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
 		
 		Element samplingPoint = doc.createElement("sa:SamplingPoint");
 		samplingPoint.setAttribute("gml:id", "foi-pilot-rock");
 		featureOfInterest.appendChild(samplingPoint);
 		
 		Element description = doc.createElement("gml:description");
 		description.appendChild(doc.createTextNode(station.getDescription()));
 		samplingPoint.appendChild(description);
 		
 		Element name = doc.createElement("gml:name");
 		name.appendChild(doc.createTextNode(station.getName()));
 		samplingPoint.appendChild(name);
 		
 		Element sampledFeature = doc.createElement("sa:sampledFeature");
 		samplingPoint.appendChild(sampledFeature);
 		
 		Element position = doc.createElement("sa:position");
 		samplingPoint.appendChild(position);
 		
 		Element point = doc.createElement("gml:Point");
 		position.appendChild(point);
 		
 		Element pos = doc.createElement("gml:pos");
 		pos.setAttribute("srsName", "http://www.opengis.net/def/crs/EPSG/0/4326");
 		pos.appendChild(doc.createTextNode(station.getLocation().getLatitude() + " " + station.getLocation().getLongitude()));
 		point.appendChild(pos);
 		
 		Element result = doc.createElement("om:result");
 		result.setAttribute("uom", "");
 		result.setAttribute("xsi:type", "gml:MeasureType");
 		result.appendChild(doc.createTextNode("0.0"));
 		measurement.appendChild(result);
 		
 		return observationTemplate;
 	}
 	
 	/**
     <sml:location>
       <gml:Point srsName="http://www.opengis.net/def/crs/EPSG/0/4326">
         <gml:pos>34.7 -72.73</gml:pos>
       </gml:Point>
     </sml:location>                 
 	 */
 	private Node createLocationNode(Document doc, SosStation station) {
 		Element smlLocation = doc.createElement("sml:location");
 
 		Element gmlPoint = doc.createElement("gml:Point");
 		gmlPoint.setAttribute("srsName", "http://www.opengis.net/def/crs/EPSG/0/4326");
 		smlLocation.appendChild(gmlPoint);
 		
		Element gmlPos = doc.createElement("gml:pos");
 		gmlPoint.appendChild(gmlPos);
 		
		gmlPos.setTextContent( station.getLocation().getLatitude() + " " + station.getLocation().getLongitude() );
 		
 		return smlLocation;
 	}
 
 	/**
 	 * Produces the XML below
         <sml:identification>
            <sml:IdentifierList>
                 <sml:identifier name="stationID">
                    <sml:Term definition="http://mmisw.org/ont/ioos/definition/stationID">
                      <sml:value>urn:ogc:object:feature:Sensor:global_hawk_24</sml:value>
                    </sml:Term>
                 </sml:identifier>
 			    <sml:identifier name="shortName">
 			       <sml:Term definition="urn:ogc:def:identifier:OGC:shortName">
 			         <sml:value>shortName</sml:value>
 			       </sml:Term>
 			    </sml:identifier>
 			    <sml:identifier name="longName">
 			       <sml:Term definition="urn:ogc:def:identifier:OGC:longName">
 			         <sml:value>longName</sml:value>
 			       </sml:Term>
 			    </sml:identifier>
            </sml:IdentifierList>
         </sml:identification>
 	 */
 	private Node createIdentificationNode(Document doc, SosStation station) {
 		Element identification = doc.createElement("sml:identification");
 	
 		Element identifierList = doc.createElement("sml:IdentifierList");
 		identification.appendChild(identifierList);
 		
 		identifierList.appendChild(createIdentifierNode(doc, "stationID", 
 				"http://mmisw.org/ont/ioos/definition/stationID", 
 				idCreator.createStationId(station)));
 		
 		identifierList.appendChild(createIdentifierNode(doc, "shortName", 
 				"http://mmisw.org/ont/ioos/definition/shortName", 
 				station.getId()));
 		
 		identifierList.appendChild(createIdentifierNode(doc, "longName", 
 				"http://mmisw.org/ont/ioos/definition/longName", 
 				station.getName()));
 		
 		return identification;
 	}
 	
 	/**
 	    <sml:identifier name="shortName">
 	       <sml:Term definition="urn:ogc:def:identifier:OGC:shortName">
 	         <sml:value>shortName</sml:value>
 	       </sml:Term>
 	    </sml:identifier>
 	 */
 	private Node createIdentifierNode(Document doc, String name, String definition, String value){
 		Element identifier = doc.createElement("sml:identifier");
 		
 		if(name != null && name.length() != 0){
 			identifier.setAttribute("name", name);
 		}
 		
 		Element term = doc.createElement("sml:Term");
 		term.setAttribute("definition", definition);
 		identifier.appendChild(term);
 		
 		Element valueElement = doc.createElement("sml:value");
 		valueElement.appendChild(doc.createTextNode(value));
 		term.appendChild(valueElement);
 		
 		return identifier;
 	}
 	
 	/**
 	 * Produces the XML below
         <sml:capabilities name="parentProcedures">
             <swe:SimpleDataRecord definition="urn:ogc:def:property:capabilities">
                 <gml:metaDataProperty xlink:title="urn:ogc:object:feature:Station:IFGI:ifgi-station-1" />
                 <gml:metaDataProperty xlink:title="urn:ogc:object:feature:Network:IFGI:ifgi-network-1" />
             </swe:SimpleDataRecord>
         </sml:capabilities>
 	 */
 	private Node createParentProcedures(Document doc, List<SosNetwork> networks){
 		
 		Element capabilities = doc.createElement("sml:capabilities");
 		capabilities.setAttribute("name", "parentProcedures");
 		
 		Element simpleDataRecord = doc.createElement("swe:SimpleDataRecord");
 		simpleDataRecord.setAttribute("definition", "urn:ogc:def:property:capabilities");
 		capabilities.appendChild(simpleDataRecord);
 		
 		for(SosNetwork network : networks){
 			Element metaDataProperty = doc.createElement("gml:metaDataProperty");
 			metaDataProperty.setAttribute("link:title", idCreator.createNetworkId(network));
 			simpleDataRecord.appendChild(metaDataProperty);
 		}
 		
 		return capabilities;
 	}
 }
