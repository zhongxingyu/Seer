 /*
  * Copyright 2011 - 2012 by the CloudRAID Team
  * see AUTHORS for more details
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
 
  * http://www.apache.org/licenses/LICENSE-2.0
 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package de.dhbw_mannheim.cloudraid.amazons3.impl.net.connector;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 
 import javax.activation.MimetypesFileTypeMap;
 
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Verb;
 import org.scribe.oauth.OAuthService;
 
 import de.dhbw_mannheim.cloudraid.amazons3.impl.net.oauth.AmazonS3Api;
 import de.dhbw_mannheim.cloudraid.amazons3.impl.net.oauth.AmazonS3Service;
 import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
 import de.dhbw_mannheim.cloudraid.config.exceptions.MissingConfigValueException;
 import de.dhbw_mannheim.cloudraid.core.net.connector.IStorageConnector;
 
 /**
  * @author Markus Holtermann
  */
 public class AmazonS3Connector implements IStorageConnector {
 
 	/**
 	 * The users public key
 	 */
 	private String accessKeyId = null;
 
 	/**
 	 * The users secret key
 	 */
 	private String secretAccessKey = null;
 
 	/**
 	 * The bucket used by this {@link AmazonS3Connector}
 	 */
 	private String bucketname = null;
 
 	/**
 	 * The regarding {@link OAuthService}
 	 */
 	private AmazonS3Service service;
 
 	private String splitOutputDir = null;
 
 	/**
 	 * A reference to the current {@link ICloudRAIDConfig}
 	 */
 	private ICloudRAIDConfig config = null;
 
 	private final static MimetypesFileTypeMap MIME_MAP = new MimetypesFileTypeMap();
 
 	private int id = -1;
 
 	private boolean bucketExists(String name) {
 		Response response = sendRequest(Verb.HEAD,
 				this.service.getBucketEndpoint(name));
 		System.out.println(response.getCode());
 		System.err.print(response.getBody() == null ? "" : response.getBody());
 		switch (response.getCode()) {
 		case 200:
 			System.out.println("You have access to bucket " + name);
 			return true;
 		case 403:
 			System.out.println("Bucket " + name
 					+ " exists, but without required priviledges to you");
 			return false;
 		case 404:
 			System.out.println("Bucket " + name + " does not exist");
 			return false;
 		default:
 			return false;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean connect() {
 		this.service = (AmazonS3Service) new ServiceBuilder()
 				.provider(AmazonS3Api.class).apiKey(this.accessKeyId)
 				.apiSecret(this.secretAccessKey).build();
 		if (!bucketExists(this.bucketname)) {
 			if (!createVolume(this.bucketname)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * This function initializes the {@link AmazonS3Connector} with the customer
 	 * and application tokens. During the {@link #connect()} process various
 	 * tokens are used. If {@link #connect()} returns <code>false</code>, this
 	 * class has to be re-instantiated and initialized with proper credentials.
 	 * </br>
 	 * 
 	 * The {@link ICloudRAIDConfig} must contain following keys:
 	 * <ul>
 	 * <li><code>connector.ID.accessKeyId</code></li>
 	 * <li><code>connector.ID.secretAccessKey</code></li>
 	 * </ul>
 	 * 
 	 * @param connectorid
 	 *            The internal id of this connector.
 	 * @param config
 	 *            The reference to a running {@link ICloudRAIDConfig} service.
 	 * 
 	 * @throws InstantiationException
 	 *             Thrown if not all required parameters are passed.
 	 */
 	@Override
 	public IStorageConnector create(int connectorid, ICloudRAIDConfig config)
 			throws InstantiationException {
 		this.id = connectorid;
 		this.config = config;
 		String kAccessKeyId = String
 				.format("connector.%d.accessKeyId", this.id);
 		String ksecretAccessKey = String.format("connector.%d.secretAccessKey",
 				this.id);
 		String kBucketName = String.format("connector.%d.bucket", this.id);
 		try {
 			this.splitOutputDir = this.config.getString("split.output.dir");
 			if (this.config.keyExists(kAccessKeyId)
 					&& this.config.keyExists(ksecretAccessKey)
 					&& this.config.keyExists(kBucketName)) {
 				this.accessKeyId = this.config.getString(kAccessKeyId);
 				this.secretAccessKey = this.config.getString(ksecretAccessKey);
 				this.bucketname = this.config.getString(kBucketName);
 			} else {
				throw new InstantiationException(kAccessKeyId + ", "
						+ ksecretAccessKey + " and " + kBucketName
						+ " have to be set in the config!");
 			}
 		} catch (MissingConfigValueException e) {
 			e.printStackTrace();
 			throw new InstantiationException(e.getMessage());
 		}
 
 		return this;
 	}
 
 	private boolean createVolume(String name) {
 		OAuthRequest request = new OAuthRequest(Verb.PUT,
 				this.service.getBucketEndpoint(name));
 		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
 		System.err.println(request);
 		this.service.signRequest(request);
 		Response response = request.send();
 		System.out.println(response.getCode());
 		if (response.getCode() == 200) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean delete(String resource) {
 		boolean ret = performDelete(resource, String.valueOf(this.id));
 		if (ret) {
 			if (!performDelete(resource, "m")) {
 				System.err
 						.println("The data file has been removed. But unfortunately the meta data file has not been removed!");
 			}
 		}
 		return ret;
 	}
 
 	@Override
 	public void disconnect() {
 
 	}
 
 	@Override
 	public InputStream get(String resource) {
 		Response response = performGet(resource, String.valueOf(this.id));
 		if (response == null) {
 			return null;
 		}
 		return response.getStream();
 	}
 
 	@Override
 	public byte[] getMetadata(String resource, int size) {
 		Response response = performGet(resource, "m");
 		if (response == null) {
 			return null;
 		}
 		BufferedInputStream bis = new BufferedInputStream(response.getStream());
 		byte meta[] = new byte[size];
 		Arrays.fill(meta, (byte) 0);
 		try {
 			bis.read(meta, 0, size);
 		} catch (IOException ignore) {
 			meta = null;
 		} finally {
 			try {
 				bis.close();
 			} catch (Exception ignore) {
 			}
 		}
 		return meta;
 	}
 
 	private boolean objectExists(String path) {
 		Response response = sendRequest(Verb.HEAD,
 				this.service.getBucketEndpoint(this.bucketname) + path);
 		return (response.getCode() == 204);
 	}
 
 	private boolean performDelete(String resource, String extension) {
 		System.out.println("DELETE " + resource + "." + extension);
 		Response response = sendRequest(Verb.DELETE,
 				this.service.getBucketEndpoint(this.bucketname) + resource
 						+ "." + extension);
 		System.out.println(response.getCode());
 		if (response.getCode() != 204) {
 			System.err.println("An error occured during deletion.");
 			System.err.print(response.getBody() == null ? "" : response
 					.getBody());
 			return false;
 		}
 		return true;
 	}
 
 	private Response performGet(String resource, String extension) {
 		System.out.println("GET " + resource + "." + extension);
 		Response response = sendRequest(Verb.GET,
 				this.service.getBucketEndpoint(this.bucketname) + resource
 						+ "." + extension);
 		System.out.println(response.getCode());
 		if (response.getCode() != 200) {
 			return null;
 		}
 		return response;
 	}
 
 	private boolean performUpload(String resource, String extension) {
 		File f = new File(this.splitOutputDir + "/" + resource + "."
 				+ extension);
 		if (!f.exists()) {
 			System.err.println("File does not exist.");
 			return false;
 		} else {
 			int maxFilesize;
 			try {
 				maxFilesize = this.config.getInt("filesize.max", null);
 				if (f.length() > maxFilesize) {
 					System.err.println("File too big");
 					return false;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 
 		byte[] fileBytes = new byte[(int) f.length()];
 		InputStream fis;
 		try {
 			fis = new FileInputStream(f);
 			fis.read(fileBytes);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		OAuthRequest request = new OAuthRequest(Verb.PUT,
 				this.service.getBucketEndpoint(this.bucketname) + resource
 						+ "." + extension);
 		request.addHeader("Content-Type",
 				AmazonS3Connector.MIME_MAP.getContentType(f));
 		this.service.signRequest(request);
 		// request.addHeader("Expect", "100-continue"); // TODO
 		request.addPayload(fileBytes);
 		Response response = request.send();
 		System.out.println(response.getCode());
 		System.err.print(response.getBody() == null ? "" : response.getBody());
 		if (response.getCode() == 411 || response.getCode() == 400) {
 			System.err.println("Could not PUT file to AmazonS3.");
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Creates a {@link org.scribe.model.OAuthRequest} to <code>endpoint</code>
 	 * as a HTTP <code>verb</code> Request Method. The request is signed with
 	 * the secret customer and application keys.
 	 * 
 	 * HTTP Request Methods: http://tools.ietf.org/html/rfc2616#section-5.1.1
 	 * 
 	 * @param verb
 	 *            The HTTP Request Method
 	 * @param endpoint
 	 *            The endpoint URL
 	 * @return Returns the corresponding response object to the request
 	 */
 	private Response sendRequest(Verb verb, String endpoint) {
 		OAuthRequest request = new OAuthRequest(verb, endpoint);
 		System.err.println(request);
 		this.service.signRequest(request);
 		Response response = request.send();
 		System.err.println(String.format("@Response(%d, %s, %s)",
 				response.getCode(), verb, endpoint));
 		return response;
 	}
 
 	@Override
 	public boolean update(String resource) {
 		System.out.println("Update " + resource + "." + this.id);
 		if (!objectExists(resource + "." + this.id)) {
 			return false;
 		}
 		boolean ret = performUpload(resource, String.valueOf(this.id));
 		if (ret) {
 			System.out.println("Upload (and overwrite) " + resource + ".m");
 			// If the upload of the data file succeeded, the meta data file must
 			// be uploaded
 			ret = performUpload(resource, "m");
 			if (!ret) {
 				// If the meta data cannot be uploaded we will remove the data
 				// file
 				delete(resource);
 			}
 		}
 		return ret;
 	}
 
 	@Override
 	public boolean upload(String resource) {
 		System.out.println("Upload " + resource + "." + this.id);
 		boolean ret = false;
 		if (!objectExists(resource + "." + this.id)) {
 			ret = performUpload(resource, String.valueOf(this.id));
 			if (ret) {
 				System.out.println("Upload (and overwrite) " + resource + ".m");
 				// If the upload of the data file succeeded, the meta data file
 				// must be uploaded
 				ret = performUpload(resource, "m");
 				if (!ret) {
 					// If the meta data cannot be uploaded we will remove the
 					// data file
 					delete(resource);
 				}
 			}
 		}
 		return ret;
 	}
 }
