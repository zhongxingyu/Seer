 package com.dajudge.serinstream;
 
 /**
  * A singleton class to configure serialization used by
  * {@link SerializableInputStream}. The default configuration deserializes into
  * memory.
  * 
  * @author Alex Stockinger
  */
 public class SerializableInputStreamConfiguration {
 	private static SerializableInputStreamConfiguration INSTANCE = createDefaultConfiguration();
 
 	private SerializationTempStore tempStore;
 
 	/**
 	 * Creates the default configuration.
 	 * 
 	 * @return the default configuration.
 	 */
 	private static SerializableInputStreamConfiguration createDefaultConfiguration() {
 		final SerializableInputStreamConfiguration ret = new SerializableInputStreamConfiguration();
 		ret.setSerializationTempStore(new MemorySerializationTempStore());
 		return ret;
 	}
 
 	/**
 	 * Sets the serialization temp store. The temp store is used for persisting
 	 * serialization data while the stream is retrieved. By default the main
 	 * memory is used to cache the deserialized data.
 	 * 
 	 * @param tempStore
 	 *            the temp store the be used for deserialization.
 	 */
 	public void setSerializationTempStore(final SerializationTempStore tempStore) {
 		this.tempStore = tempStore;
 	}
 
	/**
	 * Returns the serialization temp store. The temp store is used for
	 * persisting serialization data while the stream is retrieved. By default
	 * the main memory is used to cache the deserialized data.
	 * 
	 * @return the serialization temp store.
	 */
 	public SerializationTempStore getSerializationTempStore() {
 		return tempStore;
 	}
 
 	/**
 	 * Retrieves the singleton instance.
 	 * 
 	 * @return the singleton instance.
 	 */
 	public static SerializableInputStreamConfiguration getInstance() {
 		return INSTANCE;
 	}

	/**
	 * Sets the default chunk size that is used when piping data from the
	 * sending-side {@link InputStream} to the serializing
	 * {@link ObjectOutputStream}. The default write chunk size defaults to 2048
	 * bytes.
	 * 
	 * @param defaultWriteChunkSize
	 *            the default chunk size in bytes.
	 */
	public void setDefaultWriteChunkSize(int defaultWriteChunkSize) {
		this.defaultWriteChunkSize = defaultWriteChunkSize;
	}

	/**
	 * Returns the default chunk size that is used when piping data from the
	 * sending-side {@link InputStream} to the serializing
	 * {@link ObjectOutputStream}. The default write chunk size defaults to 2048
	 * bytes.
	 * 
	 * @return the write chunk size.
	 */
	public int getDefaultWriteChunkSize() {
		return defaultWriteChunkSize;
	}

 }
