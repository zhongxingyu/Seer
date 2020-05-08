 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.runtime.tests.binding.jaxb;
 
 import java.io.ByteArrayOutputStream;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 import junit.framework.Assert;
 
 import org.ebayopensource.turmeric.runtime.binding.BindingConstants;
 import org.ebayopensource.turmeric.runtime.binding.BindingFacade;
 import org.ebayopensource.turmeric.runtime.binding.IDeserializationContext;
 import org.ebayopensource.turmeric.runtime.binding.IDeserializerFactory;
 import org.ebayopensource.turmeric.runtime.binding.ISerializationContext;
 import org.ebayopensource.turmeric.runtime.binding.ISerializerFactory;
 import org.ebayopensource.turmeric.runtime.binding.impl.jaxb.json.JSONDeserializerFactory;
 import org.ebayopensource.turmeric.runtime.binding.impl.jaxb.json.JSONSerializerFactory;
 import org.ebayopensource.turmeric.runtime.binding.impl.jaxb.nv.NVDeserializerFactory;
 import org.ebayopensource.turmeric.runtime.binding.impl.jaxb.nv.NVSerializerFactory;
 import org.ebayopensource.turmeric.runtime.binding.impl.jaxb.xml.XMLDeserializerFactory;
 import org.ebayopensource.turmeric.runtime.binding.impl.jaxb.xml.XMLSerializerFactory;
 import org.ebayopensource.turmeric.runtime.binding.schema.DataElementSchema;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.schema.DataElementSchemaImpl;
 import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
 import org.junit.Test;
 
 
 /**
  * This is the testcase class for using BindingFwk interface to perform
  * Serialization/Deserialization outside of SOA Runtime.  BindingFwk
  * provided a facade to access this framework BindingFacade.
  *
  * The following testcases serialize and deserialize an ojbect class
  * defined in MyObject.
  *
  * @author wdeng
  *
  */
 public class StandaloneSerDeserTest extends BaseSerDeserTest {
 
 	private static final String TEST_NS = BindingFacade.DEFAULT_NAMESPACE;
 
 	public StandaloneSerDeserTest() {
 		super(new Class[] { MyObject.class,StructureWithAttribute.class });
 	}
 
 	private static final String GOLD_JaxbXmlOutOfProcess = "<?xml version='1.0' encoding='" +
 		Charset.defaultCharset().displayName() +
 		"'?><ns2:MyObject xmlns:ns2=\"urn:default\"><emailAddress>dp ebay com</emailAddress><id>1000</id><multipleObjects><emailAddress>middle1@ebay.com</emailAddress><id>1007</id><names>Linda</names></multipleObjects><names>Dave</names><names>David</names><names>D</names><singleObject><emailAddress>middle@ebay.com</emailAddress><id>1001</id><multipleObjects><emailAddress>inner1@ebay.com</emailAddress><id>1004</id><names>Amy</names><names>Anna</names><names>Apple</names></multipleObjects><multipleObjects><emailAddress>inner2@ebay.com</emailAddress><id>1005</id><names>Bob</names><names>Becky</names><names>Bluce</names></multipleObjects><multipleObjects><emailAddress>inner3@ebay.com</emailAddress><id>1006</id></multipleObjects><names>Mike</names><names>Mia</names><names>Meg</names><singleObject><emailAddress>inner@ebay.com</emailAddress><id>1002</id><names>Simon</names><names>Sam</names><names>Selena</names></singleObject></singleObject></ns2:MyObject>";
 	@Test	
 	public void testJaxbXmlOutOfProcess() throws Exception {
 		MyObject msg = createTestObject();
 
 		ISerializerFactory serFactory = new XMLSerializerFactory();
 		serFactory.init(new TestSerInitContext());
 		IDeserializerFactory deserFactory = new XMLDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext());
 
 		doTest(msg, BindingConstants.PAYLOAD_XML, SOAConstants.MIME_XML,
 				serFactory, deserFactory, GOLD_JaxbXmlOutOfProcess);
 	}
 
 	@Test	
 	public void testJaxbXmlNoRoot() throws Exception {
 		MyObject msg = new MyObject();
 		msg.setId(12345);
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("noRoot", "true");
 
 		ISerializerFactory serFactory = new XMLSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new XMLDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 
 		doTest(msg, BindingConstants.PAYLOAD_XML, SOAConstants.MIME_XML,
				serFactory, deserFactory, "<?xml version='1.0' encoding='windows-1252'?><id xmlns=\"urn:default\">12345</id>");
 	}
 
 	@Test	
 	public void testJaxbXmlNoRootFalse() throws Exception {
 		MyObject msg = new MyObject();
 		msg.setId(12345);
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("noRoot", "false");
 
 		ISerializerFactory serFactory = new XMLSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new XMLDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 
 		doTest(msg, BindingConstants.PAYLOAD_XML, SOAConstants.MIME_XML,
				serFactory, deserFactory, "<?xml version='1.0' encoding='windows-1252'?><MyObject xmlns=\"urn:default\"><id>12345</id></MyObject>");
 	}
 
 /*	private static final String GOLD_JaxbXmlCharacterEscaping = "<?xml version='1.0' encoding='" +
 		Charset.defaultCharset().displayName() +
 		"'?>";
 	@Test	
 	public void testJaxbXmlCharacterEscaping() throws Exception {
 		System.out.println("**** Starting testJaxbXmlCharacterEscaping");
 		MyObject msg = new MyObject();
 		byte[] charsBytes = new byte[257];
 		for (int i=1; i<256; i++) {
 			charsBytes[i] = (byte) (i);
 		}
 		charsBytes[256] = 0;
 		String charsUpto255 = new String(charsBytes, "ISO-8859-1");
 		msg.setNames(new String[]{charsUpto255});
 
 		ISerializerFactory serFactory = new XMLSerializerFactory();
 		serFactory.init(new TestSerInitContext());
 		IDeserializerFactory deserFactory = new XMLDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext());
 
 		doTest(msg, BindingConstants.PAYLOAD_XML, SOAConstants.MIME_XML,
 				serFactory, deserFactory, GOLD_JaxbXmlCharacterEscaping);
 		System.out.println("**** Ending testJaxbXmlCharacterEscaping");
 	}
 */
 	@Test	
 	public void testJaxbNVOutOfProcess() throws Exception {
 		MyObject msg = createTestObject();
 
 		ISerializerFactory serFactory = new NVSerializerFactory();
 		serFactory.init(new TestSerInitContext());
 		IDeserializerFactory deserFactory = new NVDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext());
 		doTest(msg, BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, null);
 	}
 
 	@Test	
 	public void testJaxbJSONOutOfProcess() throws Exception {
 		MyObject msg = createTestObject();
 
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext());
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext());
 		doTest(msg, BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, null);
 	}
 
 	private static final String GOLD_JSONJSONBasicUseSchemaInfo = "{\n	\n	\"MyObject\":{\n		\"emailAddress\":\"dp ebay com\",\n		\"id\":\"1000\",\n		\"multipleObjects\":[{\n			\"emailAddress\":\"middle1@ebay.com\",\n			\"id\":\"1007\",\n			\"names\":[\"Linda\"]\n		}],\n		\"names\":[\"Dave\",\n		\"David\",\n		\"D\"],\n		\"singleObject\":{\n			\"emailAddress\":\"middle@ebay.com\",\n			\"id\":\"1001\",\n			\"multipleObjects\":[{\n				\"emailAddress\":\"inner1@ebay.com\",\n				\"id\":\"1004\",\n				\"names\":[\"Amy\",\n				\"Anna\",\n				\"Apple\"]\n			},\n			{\n				\"emailAddress\":\"inner2@ebay.com\",\n				\"id\":\"1005\",\n				\"names\":[\"Bob\",\n				\"Becky\",\n				\"Bluce\"]\n			},\n			{\n				\"emailAddress\":\"inner3@ebay.com\",\n				\"id\":\"1006\"\n			}],\n			\"names\":[\"Mike\",\n			\"Mia\",\n			\"Meg\"],\n			\"singleObject\":{\n				\"emailAddress\":\"inner@ebay.com\",\n				\"id\":\"1002\",\n				\"names\":[\"Simon\",\n				\"Sam\",\n				\"Selena\"]\n			}\n		}\n	}\n}\n";
 	@Test
 	public void testJSONBasicUseSchemaInfo () throws Exception {
 		MyObject msg = createTestObject();
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "true");
 		options.put("formatOutput", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(msg, BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, GOLD_JSONJSONBasicUseSchemaInfo);
 	}
 
 	private static final String GOLD_JSONAttribute = "{\n	\n	\"StructureWithAttribute\":[{\n		\"@count\":\"1\",\n		\"myObjects\":[{\n			\"id\":[\"1\"]\n		}]\n	}]\n}\n";
 	@Test
 	public void testJSONAttribute () throws Exception {
 		StructureWithAttribute structure = createStructureWithAttribute(1);
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "false");
 		options.put("formatOutput", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(structure, StructureWithAttribute.class, createSchemaInfoForStructureWithAttribute(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, GOLD_JSONAttribute);
 	}
 
 	private static final String GOLD_JSONAttributeUseSchemaInfo = "{\n	\n	\"StructureWithAttribute\":{\n		\"@count\":\"2\",\n		\"myObjects\":[{\n			\"id\":\"1\"\n		},\n		{\n			\"id\":\"2\"\n		}]\n	}\n}\n";
 	@Test
 	public void testJSONAttributeUseSchemaInfo () throws Exception {
 		StructureWithAttribute structure = createStructureWithAttribute(2);
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "true");
 		options.put("formatOutput", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(structure, StructureWithAttribute.class, createSchemaInfoForStructureWithAttribute(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, GOLD_JSONAttributeUseSchemaInfo);
 	}
 
 	private static final String GOLD_JSONAttributeNoSubElements = "{\n	\n	\"StructureWithAttribute\":[{\n		\"@count\":\"0\"\n	}]\n}\n";
 	@Test
 	public void testJSONAttributeNoSubElements () throws Exception {
 		StructureWithAttribute structure = createStructureWithAttribute(0);
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "false");
 		options.put("formatOutput", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(structure, StructureWithAttribute.class, createSchemaInfoForStructureWithAttribute(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, GOLD_JSONAttributeNoSubElements);
 	}
 
 	private static final String GOLD_JSONAttributeNoSubElementsUseSchemaInfo = "{\n	\n	\"StructureWithAttribute\":{\n		\"@count\":\"0\"\n	}\n}\n";
 	@Test
 	public void testJSONAttributeNoSubElementsUseSchemaInfo () throws Exception {
 		StructureWithAttribute structure = createStructureWithAttribute(0);
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "true");
 		options.put("formatOutput", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(structure, StructureWithAttribute.class, createSchemaInfoForStructureWithAttribute(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, GOLD_JSONAttributeNoSubElementsUseSchemaInfo);
 	}
 
 	private static final String GOLD_JSONBasicUseSchemaInfoNoRoot = "{\n	\n		\"emailAddress\":\"dp ebay com\",\n		\"id\":\"1000\",\n		\"multipleObjects\":[{\n			\"emailAddress\":\"middle1@ebay.com\",\n			\"id\":\"1007\",\n			\"names\":[\"Linda\"]\n		}],\n		\"names\":[\"Dave\",\n		\"David\",\n		\"D\"],\n		\"singleObject\":{\n			\"emailAddress\":\"middle@ebay.com\",\n			\"id\":\"1001\",\n			\"multipleObjects\":[{\n				\"emailAddress\":\"inner1@ebay.com\",\n				\"id\":\"1004\",\n				\"names\":[\"Amy\",\n				\"Anna\",\n				\"Apple\"]\n			},\n			{\n				\"emailAddress\":\"inner2@ebay.com\",\n				\"id\":\"1005\",\n				\"names\":[\"Bob\",\n				\"Becky\",\n				\"Bluce\"]\n			},\n			{\n				\"emailAddress\":\"inner3@ebay.com\",\n				\"id\":\"1006\"\n			}],\n			\"names\":[\"Mike\",\n			\"Mia\",\n			\"Meg\"],\n			\"singleObject\":{\n				\"emailAddress\":\"inner@ebay.com\",\n				\"id\":\"1002\",\n				\"names\":[\"Simon\",\n				\"Sam\",\n				\"Selena\"]\n			}\n		}\n}\n";
 	@Test
 	public void testJSONBasicUseSchemaInfoNoRoot () throws Exception {
 		MyObject msg = createTestObject();
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "true");
 		options.put("formatOutput", "true");
 		options.put("noRoot", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(msg, BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, GOLD_JSONBasicUseSchemaInfoNoRoot);
 	}
 	
 	private static final String GOLD_JSONBASIC_NO_ROOT = "{\n	\n		\"emailAddress\":[\"dp ebay com\"],\n		\"id\":[\"1000\"],\n		\"multipleObjects\":[{\n			\"emailAddress\":[\"middle1@ebay.com\"],\n			\"id\":[\"1007\"],\n			\"names\":[\"Linda\"]\n		}],\n		\"names\":[\"Dave\",\n		\"David\",\n		\"D\"],\n		\"singleObject\":[{\n			\"emailAddress\":[\"middle@ebay.com\"],\n			\"id\":[\"1001\"],\n			\"multipleObjects\":[{\n				\"emailAddress\":[\"inner1@ebay.com\"],\n				\"id\":[\"1004\"],\n				\"names\":[\"Amy\",\n				\"Anna\",\n				\"Apple\"]\n			},\n			{\n				\"emailAddress\":[\"inner2@ebay.com\"],\n				\"id\":[\"1005\"],\n				\"names\":[\"Bob\",\n				\"Becky\",\n				\"Bluce\"]\n			},\n			{\n				\"emailAddress\":[\"inner3@ebay.com\"],\n				\"id\":[\"1006\"]\n			}],\n			\"names\":[\"Mike\",\n			\"Mia\",\n			\"Meg\"],\n			\"singleObject\":[{\n				\"emailAddress\":[\"inner@ebay.com\"],\n				\"id\":[\"1002\"],\n				\"names\":[\"Simon\",\n				\"Sam\",\n				\"Selena\"]\n			}]\n		}]\n}\n";
 	@Test
 	public void testJSONBasicNoRoot () throws Exception {
 		MyObject msg = createTestObject();
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "false");
 		options.put("formatOutput", "true");
 		options.put("noRoot", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(msg, BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, GOLD_JSONBASIC_NO_ROOT);
 	}
 	
 	@Test
 	public void testJSONSimpleStringObject () throws Exception {
 		Object msg = "This is a test string.";
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("formatOutput", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(msg, String.class, createStringElementSchema(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, "{\n	\n	\"String\":\"This is a test string.\"\n}\n");
 	}
 	
 	@Test
 	public void testJSONSimpleStringObjectUseSchemaInfo () throws Exception {
 		Object msg = "This is a test string.";
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "true");
 		options.put("formatOutput", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(msg, String.class, createStringElementSchema(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, "{\n	\n	\"String\":\"This is a test string.\"\n}\n");
 	}
 	
 	@Test
 	public void testJSONSimpleStringObjectUseSchemaInfoNoRoot () throws Exception {
 		Object msg = "This is a test string.";
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "true");
 		options.put("formatOutput", "true");
 		options.put("noRoot", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(msg, String.class, createStringElementSchema(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, "{\n	\"This is a test string.\"\n}\n");
 	}
 	
 	@Test
 	public void testJSONSimpleStringObjectNoRoot () throws Exception {
 		Object msg = "This is a test string.";
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "false");
 		options.put("formatOutput", "true");
 		options.put("noRoot", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(msg, String.class, createStringElementSchema(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, "{\n	[\"This is a test string.\"]\n}\n");
 	}
 	
 	@Test
 	public void testJSONNullObject () throws Exception {
 		MyObject msg = null;
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("formatOutput", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(msg, MyObject.class, createMyObjectElementSchema(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, "{\n	\n	\"MyObject\":null\n}\n");
 	}
 	
 	@Test
 	public void testJSONNullObjectUseSchemaInfo () throws Exception {
 		MyObject msg = null;
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "true");
 		options.put("formatOutput", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(msg, MyObject.class, createMyObjectElementSchema(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, "{\n	\n	\"MyObject\":null\n}\n");
 	}
 	
 	@Test
 	public void testJSONNullObjectUseSchemaInfoNoRoot () throws Exception {
 		MyObject msg = null;
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "true");
 		options.put("formatOutput", "true");
 		options.put("noRoot", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(msg, MyObject.class, createMyObjectElementSchema(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, "{\n	null\n}\n");
 	}
 	
 	@Test
 	public void testJSONNullObjectNoRoot () throws Exception {
 		MyObject msg = null;
 
 		HashMap<String, String> options = new HashMap<String, String>();
 		options.put("useSchemaInfo", "false");
 		options.put("formatOutput", "true");
 		options.put("noRoot", "true");
 		ISerializerFactory serFactory = new JSONSerializerFactory();
 		serFactory.init(new TestSerInitContext(options));
 		IDeserializerFactory deserFactory = new JSONDeserializerFactory();
 		deserFactory.init(new TestDeserInitContext(options));
 		doTest(msg, MyObject.class, createMyObjectElementSchema(), BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON,
 				serFactory, deserFactory, "{\n	[null]\n}\n");
 	}
 	
 	private MyObject createTestObject() {
 		MyObject myObj = new MyObject();
 		myObj.setId(1000);
 		myObj.setEmailAddress("dp ebay com");
 		myObj.setNames(new String[] { "Dave", "David", "D" });
 		
 		MyObject middleObject = new MyObject();
 		myObj.setSingleObject(middleObject);
 		middleObject.setId(1001);
 		middleObject.setEmailAddress("middle@ebay.com");
 		middleObject.setNames(new String[] { "Mike", "Mia", "Meg" });
 
 		ArrayList<MyObject> middleObjects = new ArrayList<MyObject>(1);
 		myObj.setMultipleObjects(middleObjects);
 		MyObject middleObject1 = new MyObject();
 		middleObject1.setId(1007);
 		middleObject1.setEmailAddress("middle1@ebay.com");
 		middleObject1.setNames(new String[] { "Linda"});
 		middleObjects.add(middleObject1);
 
 		MyObject innerObject = new MyObject();
 		middleObject.setSingleObject(innerObject);
 		innerObject.setId(1002);
 		innerObject.setEmailAddress("inner@ebay.com");
 		innerObject.setNames(new String[] { "Simon", "Sam", "Selena" });
 		
 		ArrayList<MyObject> innerObjects = new ArrayList<MyObject>(3);
 		middleObject.setMultipleObjects(innerObjects);
 		MyObject innerObject1 = new MyObject();
 		innerObject1.setId(1004);
 		innerObject1.setEmailAddress("inner1@ebay.com");
 		innerObject1.setNames(new String[] { "Amy", "Anna", "Apple" });
 		innerObjects.add(innerObject1);
 
 		MyObject innerObject2 = new MyObject();
 		innerObject2.setId(1005);
 		innerObject2.setEmailAddress("inner2@ebay.com");
 		innerObject2.setNames(new String[] { "Bob", "Becky", "Bluce" });
 		innerObjects.add(innerObject2);
 
 		MyObject innerObject3 = new MyObject();
 		innerObject3.setId(1006);
 		innerObject3.setEmailAddress("inner3@ebay.com");
 		innerObject3.setNames(new String[] { });
 		innerObjects.add(innerObject3);
 		
 		return myObj;
 	}
 
 	private void doTest(MyObject msg, String dataFormat, String dataMIMEType,
 			ISerializerFactory serFactory, IDeserializerFactory deserFactory, String goodPayload)
 			throws Exception, ServiceException {
 		doTest(msg, msg.getClass(), createMyObjectElementSchema(), dataFormat, dataMIMEType,
 				serFactory, deserFactory, goodPayload);
 	}
 
 	private void doTest(Object msg, Class<?> msgClass, DataElementSchema schema, String dataFormat, String dataMIMEType,
 			ISerializerFactory serFactory, IDeserializerFactory deserFactory, String goodPayload)
 			throws Exception, ServiceException {
 		ISerializationContext serCtx = BindingFacade
 				.createSerializationContext(msg, serFactory,
 						TEST_NS, schema, msgClass);
 		IDeserializationContext deserCtx = BindingFacade
 				.createDeserializationContext(msg, deserFactory,
 						TEST_NS, schema, msgClass);
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		BindingFacade.serialize(serCtx, serFactory, out, msg);
 		String xml1 = out.toString();
 		System.out.println("payload='" + xml1 + "'");
 		if (goodPayload != null) {
 			Assert.assertEquals(goodPayload, xml1);
 		}
 		Object msg1 = BindingFacade.deserialize(deserCtx, deserFactory, xml1);
 		out = new ByteArrayOutputStream();
 		BindingFacade.serialize(serCtx, serFactory, out, msg1);
 		System.out.println("payload='" + out.toString() + "'");
 		msg1 = BindingFacade.deserialize(deserCtx, deserFactory, out.toString());
 		out = new ByteArrayOutputStream();
 		BindingFacade.serialize(serCtx, serFactory, out, msg1);
 		String xml2 = out.toString();
 		System.out.println("payload='" + xml2 + "'");
 		Assert.assertEquals(msg, msg1);
 	}
 		
 	private DataElementSchema createStringElementSchema() {
 		QName objectQN = new QName(TEST_NS, "String");
 		DataElementSchemaImpl object = new DataElementSchemaImpl(objectQN, 0);
 		return object;
 		
 	}
 	
 	// Creates the DataElementSchema for MyObject;
 	private DataElementSchema createSchemaInfoForStructureWithAttribute() {		
 		List<DataElementSchema> myObjectsChildren = new ArrayList<DataElementSchema>(6);
 
 		QName emailAddressQN = new QName(TEST_NS, "emailAddress");
 		DataElementSchemaImpl emailAddress = new DataElementSchemaImpl(emailAddressQN, 1);
 		myObjectsChildren.add(emailAddress);
 		
 		QName idQN = new QName(TEST_NS, "id");
 		DataElementSchemaImpl id = new DataElementSchemaImpl(idQN, 1);
 		myObjectsChildren.add(id);
 		
 		QName multipleObjectsQN = new QName(TEST_NS, "multipleObjects");
 		DataElementSchemaImpl multipleObjects = new DataElementSchemaImpl(multipleObjectsQN, -1);
 		myObjectsChildren.add(multipleObjects);
 		
 		QName namesQN = new QName(TEST_NS, "names");
 		DataElementSchemaImpl names = new DataElementSchemaImpl(namesQN, -1);
 		myObjectsChildren.add(names);
 		
 		QName singleObjectQN = new QName(TEST_NS, "singleObject");
 		DataElementSchemaImpl singleObject = new DataElementSchemaImpl(singleObjectQN, 1);
 		myObjectsChildren.add(singleObject);
 		
 		QName genericObjectQN = new QName(TEST_NS, "genericObject");
 		DataElementSchemaImpl genericObject = new DataElementSchemaImpl(genericObjectQN, 1);
 		myObjectsChildren.add(genericObject);
 
 		QName myObjectQN = new QName(TEST_NS, "MyObject");
 		@SuppressWarnings("unused")
 		DataElementSchemaImpl myObject = new DataElementSchemaImpl(myObjectQN, 0, myObjectsChildren);
 
 		List<DataElementSchema> structureObjectsChildren = new ArrayList<DataElementSchema>(1);
 
 		QName myObjectsQN = new QName(TEST_NS, "myObjects");
 		DataElementSchemaImpl myObjects = new DataElementSchemaImpl(myObjectsQN, -1, myObjectsChildren);
 		structureObjectsChildren.add(myObjects);
 
 		QName StructureWithAttributeQN = new QName(TEST_NS, "StructureWithAttribute");
 		DataElementSchemaImpl myStructureWithAttribute = new DataElementSchemaImpl(StructureWithAttributeQN, 0, structureObjectsChildren);
 		
 		multipleObjects.setChildren(myObjectsChildren);
 		singleObject.setChildren(myObjectsChildren);
 		
 		return myStructureWithAttribute;
 	}
 	
 	// Creates the DataElementSchema for MyObject;
 	private DataElementSchema createMyObjectElementSchema() {		
 		List<DataElementSchema> myObjectsChildren = new ArrayList<DataElementSchema>(6);
 
 		QName emailAddressQN = new QName(TEST_NS, "emailAddress");
 		DataElementSchemaImpl emailAddress = new DataElementSchemaImpl(emailAddressQN, 1);
 		myObjectsChildren.add(emailAddress);
 		
 		QName idQN = new QName(TEST_NS, "id");
 		DataElementSchemaImpl id = new DataElementSchemaImpl(idQN, 1);
 		myObjectsChildren.add(id);
 		
 		QName multipleObjectsQN = new QName(TEST_NS, "multipleObjects");
 		DataElementSchemaImpl multipleObjects = new DataElementSchemaImpl(multipleObjectsQN, -1);
 		myObjectsChildren.add(multipleObjects);
 		
 		QName namesQN = new QName(TEST_NS, "names");
 		DataElementSchemaImpl names = new DataElementSchemaImpl(namesQN, -1);
 		myObjectsChildren.add(names);
 		
 		QName singleObjectQN = new QName(TEST_NS, "singleObject");
 		DataElementSchemaImpl singleObject = new DataElementSchemaImpl(singleObjectQN, 1);
 		myObjectsChildren.add(singleObject);
 		
 		QName genericObjectQN = new QName(TEST_NS, "genericObject");
 		DataElementSchemaImpl genericObject = new DataElementSchemaImpl(genericObjectQN, 1);
 		myObjectsChildren.add(genericObject);
 
 		QName myObjectQN = new QName(TEST_NS, "MyObject");
 		DataElementSchemaImpl myObject = new DataElementSchemaImpl(myObjectQN, 0, myObjectsChildren);
 		
 		multipleObjects.setChildren(myObjectsChildren);
 		singleObject.setChildren(myObjectsChildren);
 		
 		return myObject;
 	}
 	
 	private static StructureWithAttribute createStructureWithAttribute(int n) {
 		StructureWithAttribute structure = new StructureWithAttribute();
 		structure.setCount(n);
 		List<MyObject> myObjs = structure.getItem();
 		for (int i=0; i<n;) {
 			MyObject msg = new MyObject();
 			msg.setId(++i);
 			myObjs.add(msg);
 		}
 		return structure;
 		
 	}
 }
