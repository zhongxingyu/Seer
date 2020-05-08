 /*******************************************************************************
  * Copyright (c) Feb 9, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.webapi.test.connection.services;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.restlet.engine.io.BioUtils;
 import org.restlet.representation.Representation;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.ServerConfig;
 import org.zend.webapi.core.connection.data.ServersList;
 import org.zend.webapi.core.connection.request.IRequest;
 import org.zend.webapi.core.connection.request.IRequestInitializer;
 import org.zend.webapi.core.connection.request.RequestParameter;
 import org.zend.webapi.core.connection.response.IResponse;
 import org.zend.webapi.core.connection.response.ResponseCode;
 import org.zend.webapi.core.service.WebApiMethodType;
 import org.zend.webapi.internal.core.connection.exception.InternalWebApiException;
 import org.zend.webapi.internal.core.connection.request.ConfigurationImportRequest;
 import org.zend.webapi.internal.core.connection.request.MultipartRepresentation;
 import org.zend.webapi.test.AbstractTestServer;
 import org.zend.webapi.test.Configuration;
 import org.zend.webapi.test.DataUtils;
 import org.zend.webapi.test.server.utils.ServerUtils;
 
 public class TestServerConfiguration extends AbstractTestServer {
 
 	public static final String CONFIG_FOLDER = "configuration/";
 	public static final String EXAMLE_CONFIG = "myConfig.zcfg";
 
 	@Test
 	public void testConfigurationExport() throws WebApiException,
 			FileNotFoundException, IOException {
 		initConfigMock(handler.configurationExport(), "configurationExport",
 				ResponseCode.OK);
 		final ServerConfig config = Configuration.getClient()
 				.configuratioExport();
 		Assert.assertTrue(config.getFileSize() > 0);
 		Assert.assertNotNull(config.getFilename());
 		Assert.assertNotNull(config.getFileContent());
 		Assert.assertEquals(config.getFileSize(),
 				config.getFileContent().length);
 	}
 
 	@Test
 	public void testConfigurationImport() throws WebApiException,
 			FileNotFoundException, IOException {
 		initMock(handler.configurationImport(), "configurationImport",
 				ResponseCode.OK);
 		final File tFile = prepareFile();
 		final ServersList list = Configuration.getClient().configuratioImport(
 				tFile);
 		DataUtils.checkValidServersList(list);
 	}
 
 	@Test
 	public void testConfigurationImportIgnoreMismatch() throws WebApiException,
 			FileNotFoundException, IOException {
 		initMock(handler.configurationImport(), "configurationImport",
 				ResponseCode.OK);
 		final File tFile = prepareFile();
 		final ServersList list = Configuration.getClient().configuratioImport(
 				tFile, true);
 		DataUtils.checkValidServersList(list);
 	}
 
 	@Test
 	public void testConfigurationImportParams() throws WebApiException,
 			MalformedURLException, FileNotFoundException {
 		initMock(handler.configurationImport(), "configurationImport",
 				ResponseCode.OK);
 		final File tFile = prepareFile();
 		IResponse response = Configuration.getClient().handle(
 				WebApiMethodType.CONFIGURATION_IMPORT,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						ConfigurationImportRequest r = (ConfigurationImportRequest) request;
 						r.setFile(tFile);
 						r.setIgnoreSystemMismatch(true);
 					}
 				});
 
 		final ConfigurationImportRequest request = (ConfigurationImportRequest) response
 				.getRequest();
 
 		String parameters = "configFile=" + tFile.toString()
 				+ "&ignoreSystemMismatch=TRUE";
 		Assert.assertEquals(parameters, request.getParametersAsString());
 	}
 
 	@Test
 	public void testMultipart() throws IOException {
 		final ArrayList<RequestParameter<?>> arrayList = new ArrayList<RequestParameter<?>>();
 		arrayList.add(new RequestParameter<Boolean>("ignoreSystemMismatch",
 				true));
 		File file = getTempFile("mySavedConfig");
 		file.deleteOnExit();
 		arrayList.add(new RequestParameter<File>("configFile", file));
 		Representation representation = new MultipartRepresentation(arrayList,
 				"--bla-bla-bla--",
 				ConfigurationImportRequest.APPLICATION_SERVER_CONFIG);
 		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 		representation.write(outputStream);
 		final String actual = outputStream.toString();
 		final InputStream resourceAsStream = new FileInputStream(new File(
 				ServerUtils.createFileName(CONFIG_FOLDER + "multipart.txt")));
 		System.out.println(actual);
 		String expected = BioUtils.toString(resourceAsStream);
 		expected = expected.replace("%filename%", file.getName());
 		Assert.assertEquals("Error comparing expected/actual", expected, actual);
 	}
 
 	@Test
 	public void testMultipartHashMap() throws IOException {
 		final ArrayList<RequestParameter<?>> arrayList = new ArrayList<RequestParameter<?>>();
 		Map<String, String> hashMap = new HashMap<String, String>();
 		hashMap.put("test1", "value1");
 		hashMap.put("test2", "value2");
 		arrayList
 				.add(new RequestParameter<Map<String, String>>("map", hashMap));
 		Representation representation = new MultipartRepresentation(arrayList,
 				"--bla-bla-bla--",
 				ConfigurationImportRequest.APPLICATION_SERVER_CONFIG);
 		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 		representation.write(outputStream);
 		final String actual = outputStream.toString();
 		final InputStream resourceAsStream = new FileInputStream(new File(
 				ServerUtils.createFileName(CONFIG_FOLDER
 						+ "multipartHashMap.txt")));
 		String expected = BioUtils.toString(resourceAsStream);
 		Assert.assertEquals(expected, actual);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testMultipartNullBoundary() {
 		final ArrayList<RequestParameter<?>> arrayList = new ArrayList<RequestParameter<?>>();
 		new MultipartRepresentation(arrayList, (String) null);
 	}
 
 	private File getTempFile(String prefix) throws IOException {
 		File file = File.createTempFile(prefix, ".zcfg");
 		FileWriter writer = new FileWriter(file);
 		writer.write("[...binary data follows...]");
 		writer.flush();
 		writer.close();
 		return file;
 	}
 
 	private File prepareFile() throws InternalWebApiException,
 			FileNotFoundException {
 		File tFile = null;
 		try {
 			tFile = File.createTempFile("test", "zcfg");
 		} catch (IOException e) {
 		}
 		tFile.deleteOnExit();
 
 		final InputStream isSource = new FileInputStream(new File(
 				ServerUtils.createFileName(CONFIG_FOLDER + EXAMLE_CONFIG)));
 		try {
 			BioUtils.copy(isSource, new FileOutputStream(tFile));
 		} catch (Exception e) {
 			throw new InternalWebApiException(e);
 		}
 		return tFile;
 	}
 }
