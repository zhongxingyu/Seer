 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.domain;
 
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.instanceOf;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import nl.surfnet.bod.domain.ConnectionV2.NotificationBaseTypeUserType;
 import nl.surfnet.bod.domain.ConnectionV2.PathTypeUserType;
 import nl.surfnet.bod.domain.ConnectionV2.ServiceAttributesUserType;
 import nl.surfnet.bod.util.XmlUtils;
 
import org.dom4j.dom.DOMElement;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.Test;
 import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStateChangeRequestType;
 import org.ogf.schemas.nsi._2013._04.connection.types.DataPlaneStatusType;
 import org.ogf.schemas.nsi._2013._04.connection.types.DirectionalityType;
 import org.ogf.schemas.nsi._2013._04.connection.types.NotificationBaseType;
 import org.ogf.schemas.nsi._2013._04.connection.types.PathType;
 import org.ogf.schemas.nsi._2013._04.connection.types.ServiceAttributesType;
 import org.ogf.schemas.nsi._2013._04.connection.types.StpType;
import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairType;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class ConnectionV2Test {
 
   private static final String PATH_TYPE_XML =
       "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
     + "<ns2:path xmlns:ns2=\"http://schemas.ogf.org/nsi/2013/04/connection/types\">"
     + "<directionality>Bidirectional</directionality><symmetricPath>true</symmetricPath>"
     + "<sourceSTP><networkId>surfnet.nl</networkId><localId>1</localId></sourceSTP>"
     + "<destSTP><networkId>surfnet.nl</networkId><localId>2</localId></destSTP></ns2:path>";
 
   @Test
   public void should_deserialize_path_type_from_xml_string() {
     PathType result = new PathTypeUserType().fromXmlString(PATH_TYPE_XML);
 
     assertNotNull(result);
     assertThat(result.getDestSTP().getNetworkId(), is("surfnet.nl"));
     assertThat(result.getDestSTP().getLocalId(), is("2"));
     assertThat(result.getDirectionality(), is(DirectionalityType.BIDIRECTIONAL));
   }
 
   @Test
   public void should_serialize_path_type_to_xml_string() {
     PathType path = new PathType()
       .withSourceSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("1"))
       .withDestSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("2"))
       .withSymmetricPath(true)
       .withDirectionality(DirectionalityType.BIDIRECTIONAL);
 
     String xml = new PathTypeUserType().toXmlString(path);
 
     assertThat(xml, is(PATH_TYPE_XML));
   }
 
   private static final String SERVICE_ATTRIBUTES_TYPE_XML =
       "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
     + "<ns2:serviceAttributes xmlns:ns2=\"http://schemas.ogf.org/nsi/2013/04/connection/types\">"
     + "<surf:sNCP xmlns:surf=\"http://schemas.surfnet.nl/nsi/2013/04/services\">Protected</surf:sNCP>"
     + "</ns2:serviceAttributes>";
 
   @Test
   public void should_deserialize_servcice_parameters_from_xml_string() {
     ServiceAttributesType serviceAttributes = new ServiceAttributesUserType().fromXmlString(SERVICE_ATTRIBUTES_TYPE_XML);
 
     assertNotNull(serviceAttributes);
     assertThat(serviceAttributes.getAny(), hasSize(1));
     Element element = (Element) serviceAttributes.getAny().get(0);
     assertThat(element.getLocalName(), is("sNCP"));
   }
 
   @Test
   public void should_serialize_service_parameters_to_xml_string() throws ParserConfigurationException {
     Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
     Element element = document.createElementNS("http://schemas.surfnet.nl/nsi/2013/04/services", "surf:sNCP");
     element.appendChild(document.createTextNode("Protected"));
     ServiceAttributesType serviceParameters = new ServiceAttributesType().withAny(element);
 
     String xml = new ServiceAttributesUserType().toXmlString(serviceParameters);
 
     assertThat(xml, is(SERVICE_ATTRIBUTES_TYPE_XML));
   }
 
   private static final String DATA_PLANE_STATE_CHANGE_REQUEST_TYPE_XML =
       "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
       + "<ns2:notificationBaseType xsi:type=\"ns2:DataPlaneStateChangeRequestType\" xmlns:ns2=\"http://schemas.ogf.org/nsi/2013/04/connection/types\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
       + "<connectionId>ConnectionId</connectionId>"
       + "<notificationId>22</notificationId><timeStamp>2013-06-17T13:10:14Z</timeStamp>"
       + "<dataPlaneStatus><active>true</active><version>0</version><versionConsistent>true</versionConsistent></dataPlaneStatus>"
       + "</ns2:notificationBaseType>";
 
   @Test
   public void shoud_serialize_data_plane_state_change_request_type_to_xml_string() {
     DataPlaneStateChangeRequestType notification = new DataPlaneStateChangeRequestType()
       .withConnectionId("ConnectionId")
       .withDataPlaneStatus(new DataPlaneStatusType().withActive(true).withVersion(0).withVersionConsistent(true))
       .withNotificationId(22)
       .withTimeStamp(XmlUtils.toGregorianCalendar(new DateTime(2013, 6, 17, 13, 10, 14, DateTimeZone.UTC)));
 
     String xml = new NotificationBaseTypeUserType().toXmlString(notification);
 
     assertThat(xml, is(DATA_PLANE_STATE_CHANGE_REQUEST_TYPE_XML));
   }
 
   @Test
   public void should_deserialize_data_plane_state_change_request_type_from_xml_string() {
     NotificationBaseType dataPlaneNotification = new NotificationBaseTypeUserType().fromXmlString(DATA_PLANE_STATE_CHANGE_REQUEST_TYPE_XML);
 
     assertThat(dataPlaneNotification, is(instanceOf(DataPlaneStateChangeRequestType.class)));
   }
 }
