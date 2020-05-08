 /*******************************************************************************
  * Copyright (c) 2011, 2013 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Said Salem - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.rmf.reqif10.pror.editor.agilegrid;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.lang.management.ManagementFactory;
 import java.lang.management.ThreadMXBean;
 import java.math.BigInteger;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import org.eclipse.rmf.reqif10.AttributeDefinitionString;
 import org.eclipse.rmf.reqif10.AttributeValueString;
 import org.eclipse.rmf.reqif10.DatatypeDefinitionString;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.ReqIF10Factory;
 import org.eclipse.rmf.reqif10.ReqIFContent;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.SpecObjectType;
 import org.eclipse.rmf.reqif10.SpecRelation;
 import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.rmf.reqif10.pror.configuration.Column;
 import org.eclipse.rmf.reqif10.pror.configuration.ConfigurationFactory;
 import org.eclipse.rmf.reqif10.pror.configuration.ProrSpecViewConfiguration;
 import org.eclipse.rmf.reqif10.pror.util.ConfigurationUtil;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Tests {@link ProrRow}
  * 
  * @author salem
  */
 public class CachingTests extends AbstractContentProviderTests {
 
 	@BeforeClass
 	public static void setup() {
 		System.setProperty("http.proxyHost", "proxy.eclipse.org");
 		System.setProperty("http.proxyPort", "9898");
 	}
 	
 	private long getCpuTime() {
 		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
 		return bean.isCurrentThreadCpuTimeSupported() ? bean
 				.getCurrentThreadCpuTime() : 0L;
 	}
 
 	private String getGitRepositoryState() {
 		try {
 			InputStream s = CachingTests.class
 					.getResourceAsStream("/commit-id");
 			if (s != null) {
 				BufferedReader in = new BufferedReader(new InputStreamReader(s));
 				return in.readLine();
 			}
 		} catch (IOException e) {
 			System.out
 					.println("## PERFORMANCE TEST ### Could not get commit-id");
 		}
 		return null;
 	}
 
 	private void sendData(String commitId, String benchmark, long value) {
 
 		if (commitId == null)
 			return;
 
 		String httpURL = "http://cobra.cs.uni-duesseldorf.de:8000/result/add/";
 
 		StringBuilder query = new StringBuilder();
 
 		HttpURLConnection con = null;
 		OutputStreamWriter wr = null;
 		BufferedReader rr = null;
 
 		try {
 
 			URL myurl = new URL(httpURL);
 
 			query.append("commitid=").append(
 					URLEncoder.encode(commitId, "UTF-8"));
 			query.append("&branch=").append(
 					URLEncoder.encode("default", "UTF-8"));
 			query.append("&project=")
 					.append(URLEncoder.encode("ProR", "UTF-8"));
 			query.append("&executable=").append(
 					URLEncoder.encode("ProR", "UTF-8"));
 			query.append("&benchmark=").append(
 					URLEncoder.encode(benchmark, "UTF-8"));
 			query.append("&environment=").append(
 					URLEncoder.encode("Eclipse", "UTF-8"));
 			query.append("&result_value=").append(
 					URLEncoder.encode("" + value, "UTF-8"));
 			
 			System.out
 					.println("## PERFORMANCE TEST ### TRY TO OPEN CONNECTION");
 			con = (HttpURLConnection) myurl.openConnection();
 			System.out
 					.println("## PERFORMANCE TEST ### CONNECTION ESTABLISHED");
 			con.setDoOutput(true);
 			con.setRequestMethod("POST");
 			con.setRequestProperty("Content-Type",
 					"application/x-www-form-urlencoded");
 			con.setRequestProperty("Content-Length",
 					String.valueOf(query.toString().length()));
 			con.connect();
 
 			wr = new OutputStreamWriter(con.getOutputStream());
 			wr.write(query.toString());
 			wr.flush();
 
 			System.out.println("## PERFORMANCE TEST ### Query String: "
 					+ query.toString());
 
 			rr = new BufferedReader(new InputStreamReader(con.getInputStream()));
 
 			for (String line; (line = rr.readLine()) != null;) {
 				System.out.println("## PERFORMANCE TEST ### " + line);
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (wr != null)
 					wr.close();
 				if (rr != null)
 					rr.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			if (con != null)
 				con.disconnect();
 		}
 
 	}
 
 	@Test
 	public void testPerfCreateReqIF() {
 		int milliNano = 100000;
 		int row = 100000;
 		String benchmark = "Create_ReqIF";
 		String commitId = getGitRepositoryState();
 
 		long startTime = getCpuTime();
 		createReqIF(row);
 		long endTime = getCpuTime();
 
 		long cpuDuration = (endTime - startTime) / milliNano;
 		sendData(commitId, benchmark, cpuDuration);
 	}
 
 	@Test
 	public void testPerfCall() {
 		int milliNano = 100000;
 		int row = 100000;
 		String benchmark = "First_call";
 		ReqIF reqif = createReqIF(row);
 		String commitId = getGitRepositoryState();
 
 		Specification spec = reqif.getCoreContent().getSpecifications().get(0);
 		ProrAgileGridContentProvider cp = new ProrAgileGridContentProvider(
 				spec, ConfigurationUtil.createSpecViewConfiguration(spec,
 						editingDomain));
 
 		long starTime = getCpuTime();
 		cp.getContentAt((row - 1), 0);
 		long endTime = getCpuTime();
 		long duration = (endTime - starTime) / milliNano;
 		sendData(commitId, benchmark, duration);
 
 		// Second call
 		benchmark = "Second_call";
 		starTime = getCpuTime();
 		cp.getContentAt((row - 1), 0);
 		endTime = getCpuTime();
 		duration = (endTime - starTime) / milliNano;
 		sendData(commitId, benchmark, duration);
 	}
 
 	@Test
 	public void testAssignSpecHierarchy() {
 		SpecHierarchy specH = ReqIF10Factory.eINSTANCE.createSpecHierarchy();
 		SpecObject specObj = ReqIF10Factory.eINSTANCE.createSpecObject();
 		specH.setObject(specObj);
 
 		contentProvider.getContentAt(0, 0);
 		specification.getChildren().set(0, specH);
 
 		assertEquals(specObj, contentProvider.getContentAt(0, 0));
 	}
 
 	@Test
 	public void testAssignSpecObject() {
 		SpecObject specObj = ReqIF10Factory.eINSTANCE.createSpecObject();
 
 		contentProvider.getContentAt(0, 0);
 		specHierarchy.setObject(specObj);
 		assertEquals(specObj, contentProvider.getContentAt(0, 0));
 	}
 
 	@Test
 	public void testSetShowSpecRelations() {
 		SpecObject specObj = ReqIF10Factory.eINSTANCE.createSpecObject();
 		SpecHierarchy specH1 = ReqIF10Factory.eINSTANCE.createSpecHierarchy();
 		SpecHierarchy specH2 = ReqIF10Factory.eINSTANCE.createSpecHierarchy();
 		SpecHierarchy specH3 = ReqIF10Factory.eINSTANCE.createSpecHierarchy();
 
 		specH1.setObject(specObject);
 		specH2.setObject(specObj);
 		specH3.setObject(specObject);
 
 		SpecRelation specRelation = ReqIF10Factory.eINSTANCE
 				.createSpecRelation();
 
 		reqif.getCoreContent().getSpecObjects().add(specObj);
 
 		specification.getChildren().add(specH1);
 		specification.getChildren().add(specH2);
 		specification.getChildren().add(specH3);
 
 		specRelation.setSource(specObj);
 		specRelation.setTarget(specObject);
 		reqif.getCoreContent().getSpecRelations().add(specRelation);
 
 		assertEquals(specH3.getObject(), contentProvider.getContentAt(3, 0));
 		contentProvider.setShowSpecRelations(true);
 		assertEquals(specRelation, contentProvider.getContentAt(3, 0));
 	}
 
 	private ReqIF createReqIF(int numRows) {
 
 		ReqIF root = ReqIF10Factory.eINSTANCE.createReqIF();
 
 		ReqIFContent content = ReqIF10Factory.eINSTANCE.createReqIFContent();
 		root.setCoreContent(content);
 
 		// Add a DatatypeDefinition
 		DatatypeDefinitionString ddString = ReqIF10Factory.eINSTANCE
 				.createDatatypeDefinitionString();
 		ddString.setLongName("T_String32k");
 		ddString.setMaxLength(new BigInteger("32000"));
 		content.getDatatypes().add(ddString);
 
 		// Add a SpecObjectType
 		SpecObjectType specObjectType = ReqIF10Factory.eINSTANCE
 				.createSpecObjectType();
 		specObjectType.setLongName("Requirement Type");
 		content.getSpecTypes().add(specObjectType);
 
 		// Add an AttributeDefinition
 		AttributeDefinitionString ad1 = ReqIF10Factory.eINSTANCE
 				.createAttributeDefinitionString();
 		ad1.setType(ddString);
 		ad1.setLongName("Description");
 		specObjectType.getSpecAttributes().add(ad1);
 
 		// Add a Specification
 		Specification spec = ReqIF10Factory.eINSTANCE.createSpecification();
 		spec.setLongName("Specification Document");
 		content.getSpecifications().add(spec);
 		ProrSpecViewConfiguration config = ConfigurationUtil
 				.createSpecViewConfiguration(spec, editingDomain);
 		Column column = ConfigurationFactory.eINSTANCE.createColumn();
 		column.setLabel("Description");
 		config.getColumns().add(column);
 
 		for (int i = 0; i < numRows; i++) {
 			SpecHierarchy specH = ReqIF10Factory.eINSTANCE
 					.createSpecHierarchy();
 			SpecObject specObj = ReqIF10Factory.eINSTANCE.createSpecObject();
 			specObj.setType(specObjectType);
 			content.getSpecObjects().add(specObj);
 
 			AttributeValueString value2 = ReqIF10Factory.eINSTANCE
 					.createAttributeValueString();
 			value2.setTheValue("Value-" + i);
 			value2.setDefinition(ad1);
 			specObj.getValues().add(value2);
 
 			specH.setObject(specObj);
 			spec.getChildren().add(specH);
 
 			// ProrUtil.createAddTypedElementCommand(parent, childFeature,
 			// newSpecElement, typeFeature, specType, index, resultIndex,
 			// domain, adapterFactory);
 		}
 		return root;
 	}
 
 }
