 /*******************************************************************************
  * Copyright (c) Feb 20, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.webapi.internal.core.connection.request;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 
 import org.restlet.data.Disposition;
 import org.restlet.data.MediaType;
 import org.restlet.engine.http.header.DispositionWriter;
 import org.restlet.engine.http.header.HeaderConstants;
 import org.restlet.engine.http.header.HeaderUtils;
 import org.restlet.engine.io.BioUtils;
 import org.restlet.representation.OutputRepresentation;
 import org.zend.webapi.core.connection.request.RequestParameter;
 
 /**
  * Defines the multipart/form- data, which can be used by a wide variety of
  * applications and transported by a wide variety of protocols as a way of
  * returning a set of values as the result of a user filling out a form.
  * 
  * @see http://www.ietf.org/rfc/rfc2388.txt
  * @author Roy, 2011
  * 
  */
 public class MultipartRepresentation extends OutputRepresentation {
 
 	/**
 	 * Boundary prefix constant
 	 */
 	protected static final byte[] BOUNDRY_PREFIX = "--".getBytes();
 
 	/**
 	 * Parameters to list in this presentation
 	 */
 	protected final List<RequestParameter<?>> parameters;
 
 	/**
 	 * a random boundary string
 	 */
 	protected String boundary;
 
 	/**
 	 * Content type for binary content. Default is
 	 * {@link MediaType.MULTIPART_FORM_DATA}.
 	 */
 	protected MediaType mediaType;
 
 	/**
 	 * The content to be represented
 	 */
 	final protected byte[] contents;
 
 	public MultipartRepresentation(List<RequestParameter<?>> parameters,
 			String boundary, MediaType mediaType) {
 		super(createMediaType(boundary));
 		if (boundary == null) {
 			throw new IllegalArgumentException(
 					"Error initializing MultipartRepresentation, null boundary");
 		}
 		this.mediaType = mediaType;
 		this.parameters = parameters;
 		this.boundary = boundary;
 		this.contents = resolveContent();
 		this.setSize(this.contents.length);
 	}
 
 	/**
 	 * @param byteArrayOutputStream
 	 * @return
 	 */
 	private byte[] resolveContent() {
 		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		try {
 			internalWrite(byteArrayOutputStream);
 		} catch (IOException e) {
 			// very unlikely to happen (in byte array case)
 			return null;
 		}
 		return byteArrayOutputStream.toByteArray();
 	}
 
 	/**
 	 * @param boundary
 	 * @return
 	 */
 	private static MediaType createMediaType(String boundary) {
 		return new MediaType("multipart/form-data; boundary=" + boundary, null,
 				"Multipart form data");
 	}
 
 	public MultipartRepresentation(String boundary) {
 		this(null, boundary, MediaType.MULTIPART_FORM_DATA);
 	}
 
 	public MultipartRepresentation(List<RequestParameter<?>> parameters) {
 		this(parameters, getRandomBoundary(), MediaType.MULTIPART_FORM_DATA);
 	}
 
 	public MultipartRepresentation(List<RequestParameter<?>> parameters,
 			String boundary) {
 		this(parameters, boundary, MediaType.MULTIPART_FORM_DATA);
 	}
 
 	public MultipartRepresentation(List<RequestParameter<?>> parameters,
 			MediaType mediaType) {
 		this(parameters, getRandomBoundary(), mediaType);
 	}
 
 	public MultipartRepresentation(String boundary, MediaType mediaType) {
 		this(null, boundary, mediaType);
 	}
 
 	public MultipartRepresentation(MediaType mediaType) {
 		this(null, getRandomBoundary(), mediaType);
 	}
 
 	/**
 	 * Generates a random boundary
 	 */
 	private static String getRandomBoundary() {
 		Random r = new Random();
 		return Long.toString(Math.abs(r.nextLong()), 36);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.restlet.representation.Representation#write(java.io.OutputStream)
 	 */
 	@Override
 	public void write(OutputStream outputStream) throws IOException {
 		BioUtils.copy(new ByteArrayInputStream(this.contents), outputStream);
 	}
 
 	/**
 	 * internal writer method to prepare the content to write
 	 * 
 	 * @param outputStream
 	 * @throws IOException
 	 */
 	protected void internalWrite(OutputStream outputStream) throws IOException {
 		header(outputStream);
 
 		for (RequestParameter<?> parameter : parameters) {
 			writeParameter(outputStream, parameter);
 			writeSeparator(outputStream);
 		}
 
 		footer(outputStream);
 	}
 
 	private void header(OutputStream outputStream) throws IOException {
 		outputStream.write(HeaderConstants.HEADER_CONTENT_TYPE.getBytes());
 		outputStream.write(": ".getBytes());
 		outputStream.write(this.getMediaType().getName().getBytes());
 		writeNewLine(outputStream, 2);
 		writeSeparator(outputStream);
 	}
 
 	private void writeParameter(OutputStream outputStream,
 			RequestParameter<?> parameter) throws IOException {
 		final Object value = parameter.getValue();
 		if (value instanceof HashMap<?, ?>) {
 			writeHashMapParameter(outputStream, parameter, value);
 		} else {
			writeNewLine(outputStream, 1);
 			Disposition d = createContentDisposition(outputStream);
 			d.getParameters().add("name", parameter.getKey());
 			if (value instanceof File) {
 				d.setFilename(((File) value).getName());
 			}
 
 			final String write = DispositionWriter.write(d);
 			outputStream.write(write.getBytes());
 
 			// content type parameter
 			if (parameter.getValue() instanceof File) {
 				writeNewLine(outputStream, 1);
 				outputStream.write(HeaderConstants.HEADER_CONTENT_TYPE
 						.getBytes());
 				outputStream.write(": ".getBytes());
 				outputStream.write(mediaType.getName().getBytes());
 			}
 
 			// writing parameter's value
 			writeNewLine(outputStream, 2);
 			BioUtils.copy(parameter.getValueAsStream(), outputStream);
 			writeNewLine(outputStream, 1);
 		}
 	}
 
 	private Disposition createContentDisposition(OutputStream outputStream)
 			throws IOException {
 		outputStream.write(HeaderConstants.HEADER_CONTENT_DISPOSITION
 				.getBytes());
 		outputStream.write(": ".getBytes());
 		// disposition
 		Disposition d = new Disposition("form-data");
 		return d;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void writeHashMapParameter(OutputStream outputStream,
 			RequestParameter<?> parameter, final Object value)
 			throws IOException {
 		HashMap<String, String> map = (HashMap<String, String>) value;
 		String mapName = parameter.getKey();
 		Set<Entry<String, String>> entries = map.entrySet();
 		writeNewLine(outputStream, 1);
 		for (Entry<String, String> entry : entries) {
 			Disposition d = createContentDisposition(outputStream);
 			d.getParameters().add("name",
 					mapName + "%5B" + entry.getKey() + "%5D");
 			final String write = DispositionWriter.write(d);
 			outputStream.write(write.getBytes());
 			writeNewLine(outputStream, 2);
 			outputStream.write(entry.getValue().getBytes());
 			writeNewLine(outputStream, 1);
 		}
 	}
 
 	private void footer(OutputStream outputStream) throws IOException {
 		outputStream.write(BOUNDRY_PREFIX);
 	}
 
 	/**
 	 * @param outputStream
 	 * @throws IOException
 	 */
 	private void writeSeparator(OutputStream outputStream) throws IOException {
 		outputStream.write(BOUNDRY_PREFIX);
 		outputStream.write(this.boundary.getBytes());
 	}
 
 	/**
 	 * @param outputStream
 	 * @param times
 	 * @throws IOException
 	 */
 	private void writeNewLine(OutputStream outputStream, int times)
 			throws IOException {
 		for (int j = 0; j < times; j++) {
 			HeaderUtils.writeCRLF(outputStream);
 		}
 	}
 
 	/**
 	 * updates the size of the representation by a given size
 	 * 
 	 * @param i
 	 */
 	protected void incrementSize(int i) {
 		if (getSize() == UNKNOWN_SIZE) {
 			return;
 		}
 		setSize(getSize() + i);
 	}
 }
