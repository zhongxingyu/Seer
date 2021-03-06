 package com.bretth.osm.conduit.sort.impl;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.NoSuchElementException;
 
 import com.bretth.osm.conduit.ConduitRuntimeException;
 
 
 /**
  * This class reads objects from an ObjectInputStream until the end of stream is
  * reached.
  * 
  * @param <DataType>
  *            The type of data to be returned by the iterator.
  * @author Brett Henderson
  */
 public class ObjectStreamIterator<DataType> implements ReleasableIterator<DataType> {
 	
 	private ObjectInputStream inStream;
 	private DataType nextElement;
 	
 	
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param inStream
 	 *            The stream to read objects from.
 	 */
 	public ObjectStreamIterator(ObjectInputStream inStream) {
 		this.inStream = inStream;
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean hasNext() {
 		try {
 			if (nextElement != null) {
 				return true;
 			}
 			
			nextElement = (DataType) inStream.readObject();
 			
 			return true;
 			
 		} catch (ClassNotFoundException e) {
 			throw new ConduitRuntimeException("Unable to read object from object stream.", e);
 		} catch (IOException e) {
 			throw new ConduitRuntimeException("Unable to read from object stream.", e);
 		}
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public DataType next() {
 		if (hasNext()) {
 			DataType result;
 			
 			result = nextElement;
 			nextElement = null;
 			
 			return result;
 			
 		} else {
 			throw new NoSuchElementException();
 		}
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void remove() {
 		throw new UnsupportedOperationException();
 	}
 
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public void release() {
 		if (inStream != null) {
 			try {
 				inStream.close();
 			} catch (Exception e) {
 				// Do nothing.
 			}
 			
 			inStream = null;
 		}
 	}
 }
