 package com.zenika.granite.interceptor;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.zip.DataFormatException;
 import java.util.zip.Inflater;
 
 import org.granite.messaging.amf.io.AMF3Deserializer;
 import org.granite.messaging.amf.process.AMF3MessageInterceptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import flex.messaging.messages.Message;
 
 public class InflateAMF3MessageInterceptor implements AMF3MessageInterceptor {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(InflateAMF3MessageInterceptor.class);
 
 	public static final String INFLATED_BYTES_LENGTH = "INFLATED_BYTES_LENGTH";
 
 	public static final String ZIP_HEADER = "DEFLATE";
 
 	@Override
 	public void before(final Message request) {
 		final Object header = request.getHeader(ZIP_HEADER);
 		final Object inflatedBytesHeader = request.getHeader(INFLATED_BYTES_LENGTH);
		if ((header != null) && (inflatedBytesHeader != null)) {
 			LOGGER.debug("AMF header found {}:{}", ZIP_HEADER, header);
 			final int inflatedBytesLength = (Integer) inflatedBytesHeader;
			LOGGER.debug("AMF header found {}:{}", INFLATED_BYTES_LENGTH, inflatedBytesLength);
 
			final Object requestBody = request.getBody();
 			if (requestBody instanceof byte[]) {
 				final byte[] deflatedBodyBytes = (byte[]) requestBody;
 				byte[] inflatedBodyBytes = null;
 				try {
 					inflatedBodyBytes = this.inflateBytes(deflatedBodyBytes, inflatedBytesLength);
 
 				} catch (final DataFormatException dataFormatException) {
 					LOGGER.warn("Cannot read deflated bytes! {} Skipping inflating ByteArray", deflatedBodyBytes);
 					inflatedBodyBytes = deflatedBodyBytes;
 				}
 
 				try {
 					final Object inflatedBodyObject = this.getJavaObjectFromAMF3Bytes(inflatedBodyBytes);
 					LOGGER.debug("Deflated bytes length: {} Inflated bytes length: {} Java object: {}", new Object[]{deflatedBodyBytes.length, inflatedBodyBytes.length, inflatedBodyObject});
 					request.setBody(inflatedBodyObject);
 
 				} catch (IOException ioException) {
 					LOGGER.error("AMF3 deserialization failed! {} Skipping deserialization of ByteArray", ioException);
 				}
 
 			} else {
 				LOGGER.warn("AMF request body is not ByteArray but deflate header was found! {} Skipping inflating request body", requestBody);
 			}
 		}
 	}
 
 	protected byte[] inflateBytes(final byte[] deflatedBytes, int inflatedBytesLength) throws DataFormatException {
 		final Inflater inflater = new Inflater(true);
 		inflater.setInput(deflatedBytes);
 
 		final byte[] inflatedBytes = new byte[inflatedBytesLength];
 		final int bytesRead = inflater.inflate(inflatedBytes);
 		if (bytesRead != inflatedBytesLength) {
 			LOGGER.warn("Bytes length is not correct! Expected: {} was: {}", inflatedBytesLength, bytesRead);
 		}
 		return inflatedBytes;
 	}
 
 	protected Object getJavaObjectFromAMF3Bytes(final byte[] amfBytes) throws IOException {
 		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(amfBytes);
 		AMF3Deserializer amf3Deserializer = null;
 		try {
 			amf3Deserializer = new AMF3Deserializer(byteArrayInputStream);
 			final Object javaObject = amf3Deserializer.readObject();
 			return javaObject;
 
 		} finally {
 			if (amf3Deserializer != null) {
 				try {
 					amf3Deserializer.close();
 				} catch (final IOException ioException) {
 					LOGGER.warn("AMF3Deserializer failed to close: {} Exception: {}", amf3Deserializer, ioException);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void after(final Message request, final Message response) {
 	}
 
 }
