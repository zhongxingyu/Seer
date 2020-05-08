 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.client;
 
 import com.mycompany.allinone.capi.*;
 import com.mycompany.allinone.types.*;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.text.ParseException;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.client.DataElement;
 import org.xins.client.InvalidRequestException;
 import org.xins.client.UnacceptableRequestException;
 import org.xins.client.UnsuccessfulXINSCallException;
 import org.xins.client.XINSCallRequest;
 import org.xins.client.XINSCallResult;
 import org.xins.client.XINSServiceCaller;
 
 import org.xins.common.collections.PropertyReader;
 
 import org.xins.common.http.HTTPMethod;
 import org.xins.common.http.StatusCodeHTTPCallException;
 
 import org.xins.common.service.TargetDescriptor;
 
 import org.xins.common.types.standard.Date;
 import org.xins.common.types.standard.Timestamp;
 
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementBuilder;
 
 /**
  * Tests the allinone functions using the generated CAPI.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class AllInOneAPITests extends TestCase {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(AllInOneAPITests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>AllInOneAPITests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public AllInOneAPITests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The target descriptor to use in all tests.
     */
    private TargetDescriptor _target;
 
    private CAPI _capi;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void setUp() throws Exception {
       _target = new TargetDescriptor("http://127.0.0.1:8080/", 5000, 4000, 4000);
       _capi    = new CAPI(_target);
    }
 
    /**
     * Tests CAPI regarding pre-defined types.
     */
    public void testSimpleTypes() throws Exception {
       SimpleTypesResult result =
          _capi.callSimpleTypes((byte) 8,              // _int8
                                (Short) null,          // _int16
                                65,                    // _int32
                                88l,                   // _int64
                                32.5f,                 // _float32
                                new Double(37.2),      // _float64
                                "text",                // _text
                                null,
                                null,
                                Date.fromStringForRequired("20041213"),
                                Timestamp.fromStringForOptional("20041225153255"),
                                new byte[] {25,88,66}  // _base64
          );
       assertNull(result.getOutputByte());
       assertEquals((short) -1, result.getOutputShort());
       assertEquals(16,         result.getOutputInt());
       assertEquals(14l,        result.getOutputLong());
       assertEquals("hello",    result.getOutputText());
       assertNull(result.getOutputText2());
       assertNull(result.getOutputProperties());
       assertEquals(Date.fromStringForRequired("20040621"), result.getOutputDate());
       assertNull(result.getOutputTimestamp());
       assertEquals(3, result.getOutputBinary().length);
       assertEquals((byte) 25, result.getOutputBinary()[0]);
       assertEquals((byte) 88, result.getOutputBinary()[1]);
       assertEquals((byte) 66, result.getOutputBinary()[2]);
    }
 
    /**
     * Tests CAPI regarding pre-defined types, using the new (XINS 1.2) CAPI
     * call methods.
     */
    public void testSimpleTypes2() throws Exception {
 
       try {
          _capi.callSimpleTypes(null);
          fail("Expected IllegalArgumentException.");
       } catch (IllegalArgumentException exception) {
          // as expected
       }
 
       SimpleTypesRequest request = new SimpleTypesRequest();
       try {
          request.validate();
          fail("Expected UnacceptableRequestException.");
       } catch (UnacceptableRequestException exception) {
          // as expected
       }
 
       try {
          _capi.callSimpleTypes(request);
          fail("Expected UnacceptableRequestException.");
       } catch (InvalidRequestException exception) {
          fail("Expected the client to detect unacceptable request, instead of the server.");
       } catch (UnacceptableRequestException exception) {
          // as expected
       }
 
       request.setInputByte((byte) 8);
       // request.setInputShort(null);
       request.setInputInt(65);
       request.setInputLong(88L);
       request.setInputFloat(32.5F);
       request.setInputDouble(37.2);
       request.setInputText("text");
       request.setInputText2(null);
       // request.setInputProperties(null);
       request.setInputDate(Date.fromStringForRequired("20041213"));
       request.setInputTimestamp(Timestamp.fromStringForOptional("20041225153255"));
       request.setInputBinary(new byte[] {25,88,66});
 
       // Check the request
       assertEquals(new Byte((byte) 8), request.getInputByte());
       assertEquals(null,               request.getInputShort());
       assertEquals(new Integer(65),    request.getInputInt());
       assertEquals(new Long(88L),      request.getInputLong());
       assertEquals(new Float(32.5F),   request.getInputFloat());
       assertEquals(new Double(37.2),   request.getInputDouble());
       assertEquals("text",             request.getInputText());
       assertEquals(null,               request.getInputText2());
       assertEquals(null,               request.getInputProperties());
       assertEquals(Date.fromStringForRequired("20041213"),            request.getInputDate());
       assertEquals(Timestamp.fromStringForOptional("20041225153255"), request.getInputTimestamp());
       assertEquals(3,                  request.getInputBinary().length);
       assertEquals((byte) 25,          request.getInputBinary()[0]);
       assertEquals((byte) 88,          request.getInputBinary()[1]);
       assertEquals((byte) 66,          request.getInputBinary()[2]);
 
       // Make the call
       SimpleTypesResult result = _capi.callSimpleTypes(request);
       assertNotNull("Result returned from CAPI.callSimpleTypes(SimpleTypesRequest) is null.", result);
 
       // Check the result
       assertNull(result.getOutputByte());
       assertEquals((short) -1, result.getOutputShort());
       assertEquals(16,         result.getOutputInt());
       assertEquals(14l,        result.getOutputLong());
       assertEquals("hello",    result.getOutputText());
       assertNull(result.getOutputText2());
       assertNull(result.getOutputProperties());
       assertEquals(Date.fromStringForRequired("20040621"), result.getOutputDate());
       assertNull(result.getOutputTimestamp());
       assertEquals(3, result.getOutputBinary().length);
       assertEquals((byte) 25, result.getOutputBinary()[0]);
       assertEquals((byte) 88, result.getOutputBinary()[1]);
       assertEquals((byte) 66, result.getOutputBinary()[2]);
    }
 
    /**
     * Tests a function called with some missing parameters.
     */
    public void testMissingParam() throws Exception {
       try {
          SimpleTypesResult result = _capi.callSimpleTypes((byte)8, null, 65, 88l, 72.5f, new Double(37.2),
             null, null, null, Date.fromStringForRequired("20041213"), Timestamp.fromStringForOptional("20041225153222"), null);
          fail("The request is invalid, the function should throw an exception");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          DataElement dataSection = exception.getDataElement();
          assertNotNull(dataSection);
          DataElement missingParam = (DataElement) dataSection.getChildElements().get(0);
          assertEquals("missing-param", missingParam.getName());
          assertEquals("inputText", missingParam.get("param"));
          assertEquals(0, missingParam.getChildElements().size());
          assertNull(missingParam.getText());
       }
    }
 
    /**
     * Tests CAPI and defined types.
     */
    public void testDefinedTypes() throws Exception {
       TextList.Value textList = new TextList.Value();
       textList.add("hello");
       textList.add("world");
       DefinedTypesResult result = _capi.callDefinedTypes("198.165.0.1", Salutation.LADY, (byte)28, textList);
       assertEquals("127.0.0.1", result.getOutputIP());
       assertEquals(Salutation.LADY, result.getOutputSalutation());
       assertEquals(Byte.decode("35"), result.getOutputAge());
       assertEquals(2, result.getOutputList().getSize());
       assertEquals(2, result.getOutputProperties().size());
    }
 
    /**
     * Tests a function called with some invalid parameters.
     */
    public void testInvalidParams() throws Exception {
       TextList.Value textList = new TextList.Value();
       textList.add("Hello");
       textList.add("Test");
       try {
          DefinedTypesResult result = _capi.callDefinedTypes("not an IP", Salutation.LADY, (byte)8, textList);
          fail("The request is invalid, the function should throw an exception");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          DataElement dataSection = exception.getDataElement();
          assertNotNull(dataSection);
          List invalidParams = dataSection.getChildElements();
          DataElement invalidParam1 = (DataElement) invalidParams.get(0);
          assertEquals("invalid-value-for-type", invalidParam1.getName());
          assertEquals("inputIP", invalidParam1.get("param"));
          assertEquals(0, invalidParam1.getChildElements().size());
          assertNull(invalidParam1.getText());
          DataElement invalidParam2 = (DataElement) invalidParams.get(1);
          assertEquals("invalid-value-for-type", invalidParam2.getName());
          assertEquals("inputAge", invalidParam2.get("param"));
          assertEquals(0, invalidParam2.getChildElements().size());
          assertNull(invalidParam2.getText());
       }
    }
 
    /**
     * Tests a function that should returned a defined result code.
     */
    public void testResultCode() throws Exception {
       String result1 = _capi.callResultCode("hello").getOutputText();
       assertEquals("The first call to ResultCode returned an incorrect result", "hello added.", result1);
       try {
          _capi.callResultCode("hello");
          fail("The second call with the same parameter should return an AlreadySet error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("AlreadySet", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNotNull(exception.getParameters());
          assertEquals("Incorrect value for the count parameter.", "1", exception.getParameter("count"));
          assertNull(exception.getDataElement());
       }
    }
 
    /**
     * Tests a function that writes messages to the Logdoc.
     */
    public void testLogdoc() throws Exception {
       // This method write some text using the logdoc
       // This test doesn't check that the data written in the logs are as expected
       try {
          _capi.callLogdoc("hello");
          fail("The logdoc call should return an InvalidNumber error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("InvalidNumber", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNull(exception.getDataElement());
       }
       _capi.callLogdoc("12000");
    }
 
    /**
     * Tests a function that get values from the runtime property file.
     */
    public void testRuntimeProps() throws Exception {
       RuntimePropsResult result = _capi.callRuntimeProps(100);
       assertEquals(20.6f, result.getTaxes(), 0.01f);
       assertEquals("Euros", result.getCurrency());
    }
 
    /**
     * Tests a function that returns a data section containing elements with
     * PCDATA.
     */
    public void testDataSection() throws Exception {
       DataElement element = _capi.callDataSection("Doe").dataElement();
       List users = element.getChildElements();
       assertTrue("No users found.", users.size() > 0);
       DataElement su = (DataElement) users.get(0);
       assertEquals("Incorrect elements.", "user", su.getName());
       assertEquals("Incorrect name for su.", "superuser", su.get("name"));
       assertEquals("Incorrect address.", "12 Madison Avenue", su.get("address"));
       assertEquals("Incorrect PCDATA.", "This user has the root authorisation.", su.getText());
       assertEquals(0, su.getChildElements().size());
       DataElement doe = (DataElement) users.get(1);
       assertEquals("Incorrect elements.", "user", doe.getName());
       assertEquals("Incorrect name for Doe.", "Doe", doe.get("name"));
       assertEquals("Incorrect address.", "Unknown", doe.get("address"));
       assertNull(doe.getText());
       assertEquals(0, doe.getChildElements().size());
    }
 
    /**
     * Tests a function that returns a data section with elements that contain
     * other elements.
     */
    public void testDataSection2() throws Exception {
       DataElement element = _capi.callDataSection2("hello").dataElement();
       List packets = element.getChildElements();
       assertTrue("No destination found.", packets.size() > 0);
       DataElement packet1 = (DataElement) packets.get(0);
       assertEquals("Incorrect elements.", "packet", packet1.getName());
       assertNotNull("No destination specified.", packet1.get("destination"));
       List products = packet1.getChildElements();
       assertTrue("No product specified.", products.size() > 0);
       DataElement product1 = (DataElement) products.get(0);
       assertEquals("Incorrect price for product1", "12", product1.get("price"));
 
       DataElement packet2 = (DataElement) packets.get(1);
       assertEquals("Incorrect elements.", "packet", packet2.getName());
       assertNotNull("No destination specified.", packet2.get("destination"));
       List products2 = packet2.getChildElements();
       assertTrue("No product specified.", products2.size() > 0);
       DataElement product21 = (DataElement) products2.get(0);
       assertEquals("Incorrect price for product1", "12", product21.get("price"));
       assertTrue(product21.getChildElements().size() == 0);
    }
 
   /**
    * Tests a function that passes a data section as input and an output data
    * section with mutiple data element types.
    */
    public void testDataSection3() throws Exception {
       ElementBuilder builder1 = new ElementBuilder();
       builder1.startElement("address");
       builder1.setAttribute("company", "McDo");
       builder1.setAttribute("postcode", "1234");
       Element address1 = builder1.createElement();
       ElementBuilder builder2 = new ElementBuilder();
       builder2.startElement("address");
       builder2.setAttribute("company", "Hello");
       builder2.setAttribute("postcode", "5678");
       Element address2 = builder2.createElement();
       ElementBuilder builder3 = new ElementBuilder();
       builder3.startElement("data");
       builder3.addChild(address1);
       builder3.addChild(address2);
       Element dataSection = builder3.createElement();
 
       DataElement element = _capi.callDataSection3("hello", dataSection).dataElement();
       List packets = element.getChildElements();
       assertTrue("No packets or envelopes found.", packets.size() == 4);
 
       DataElement envelope1 = (DataElement) packets.get(0);
       assertEquals("Incorrect elements.", "envelope", envelope1.getLocalName());
       assertEquals("1234", envelope1.getAttribute("destination"));
 
       DataElement envelope2 = (DataElement) packets.get(1);
       assertEquals("Incorrect elements.", "envelope", envelope2.getLocalName());
       assertEquals("5678", envelope2.getAttribute("destination"));
 
       DataElement packet1 = (DataElement) packets.get(2);
       assertEquals("Incorrect elements.", "packet", packet1.getLocalName());
       assertNotNull("No destination specified.", packet1.getAttribute("destination"));
 
       DataElement envelope3 = (DataElement) packets.get(3);
       assertEquals("Incorrect elements.", "envelope", envelope3.getLocalName());
       assertNotNull("No destination specified.", envelope3.getAttribute("destination"));
    }
 
   /**
    * Tests the param-combos using the old-style (XINS 1.0/1.1) call methods.
    */
    public void testParamCombo1() throws Exception {
 
       // Test 'inclusive-or' and 'exclusive-or'
       try {
          _capi.callParamCombo(null, null, null, null, null, null, null);
          fail("The param-combo call should return an _InvalidRequest error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("inclusive-or", paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
          if (itParamCombos.hasNext()) {
             DataElement paramCombo2 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo2.getLocalName());
             assertEquals("exclusive-or", paramCombo2.getAttribute("type"));
          } else {
             fail("Just one param combo element was found.");
          }
       }
 
       // Test 'all-or-none'
       try {
          _capi.callParamCombo(null, null, new Integer(5), null, "Paris", null, new Byte((byte)33));
          fail("The param-combo call should return an _InvalidRequest error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidRequest", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("all-or-none", paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
       }
    }
 
   /**
    * Tests the param-combo constraints using the new-style (XINS 1.2) call
    * methods, which should throw an UnacceptableRequestException.
    */
    public void testParamCombo2() throws Exception {
 
       ParamComboRequest request = new ParamComboRequest();
       UnacceptableRequestException exception;
       try {
          _capi.callParamCombo(request);
          fail("Expected UnacceptableRequestException.");
          return;
       } catch (UnacceptableRequestException e) {
          exception = e;
       }
 
       // TODO
    }
 
   /**
    * Tests the param-combo for the output section.
    */
    public void testParamCombo3() throws Exception {
       // Test 'all-or-none'
       try {
          _capi.callParamCombo(null, new Integer(2006), new Integer(5), new Integer(20), "France", "Paris", null);
          fail("The param-combo call should return an _InvalidResponse error code.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidResponse", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          assertNotNull(exception.getDataElement());
          DataElement dataSection = exception.getDataElement();
          Iterator itParamCombos = dataSection.getChildElements().iterator();
          if (itParamCombos.hasNext()) {
             DataElement paramCombo1 = (DataElement)itParamCombos.next();
             assertEquals("param-combo", paramCombo1.getLocalName());
             assertEquals("exclusive-or", paramCombo1.getAttribute("type"));
          } else {
             fail("No param combo element found.");
          }
       }
    }
 
    /**
     * Tests the getXINSVersion() CAPI method.
     */
    public void testCAPIVersion() throws Exception {
       assertNotNull("No XINS version specified.", _capi.getXINSVersion());
       assertTrue("The version does not starts with '1.'", _capi.getXINSVersion().startsWith("1."));
    }
 
    /**
     * Tests a function that does not exists
     */
    public void testUnknownFunction() throws Exception {
       XINSCallRequest request = new XINSCallRequest("Unknown");
       XINSServiceCaller caller = new XINSServiceCaller(_target);
       try {
          XINSCallResult result = caller.call(request);
       } catch (StatusCodeHTTPCallException exception) {
          assertEquals("Incorrect status code found.", 404, exception.getStatusCode());
       }
    }
 
    /**
     * Tests invalid responses from the server.
     */
    public void testInvalidResponse() throws Exception {
       XINSCallRequest request = new XINSCallRequest("InvalidResponse");
       XINSServiceCaller caller = new XINSServiceCaller(_target);
       try {
          caller.call(request);
          fail("No invalid response received as expected.");
       } catch (UnsuccessfulXINSCallException exception) {
          assertEquals("_InvalidResponse", exception.getErrorCode());
          assertEquals(_target, exception.getTarget());
          assertNull(exception.getParameters());
          DataElement dataSection = exception.getDataElement();
          assertNotNull(dataSection);
          DataElement missingParam = (DataElement) dataSection.getChildElements().get(0);
          assertEquals("missing-param", missingParam.getName());
          assertEquals("outputText1", missingParam.get("param"));
          assertEquals(0, missingParam.getChildElements().size());
          assertNull(missingParam.getText());
          DataElement invalidParam = (DataElement) dataSection.getChildElements().get(1);
          assertEquals("invalid-value-for-type", invalidParam.getName());
          assertEquals("pattern", invalidParam.get("param"));
          assertEquals(0, invalidParam.getChildElements().size());
          assertNull(invalidParam.getText());
       }
    }
 }
