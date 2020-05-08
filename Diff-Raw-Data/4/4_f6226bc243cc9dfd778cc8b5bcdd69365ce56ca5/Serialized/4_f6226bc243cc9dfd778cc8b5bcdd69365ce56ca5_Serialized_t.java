 /**
  *
  */
 package org.selfip.bkimmel.rmi;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import org.selfip.bkimmel.io.AlternateClassLoaderObjectInputStream;
 import org.selfip.bkimmel.util.UnexpectedException;
 
 /**
  * @author brad
  *
  */
 public final class Serialized<T> implements Serializable {
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 222718136239175900L;
 
 	private final byte[] data;
 	private transient T object;
 
 	public Serialized(T obj) {
 		this.object = obj;
 		try {
 			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
			objectStream.writeObject(obj);
 			objectStream.flush();
 			data = byteStream.toByteArray();
 			objectStream.close();
 		} catch (IOException e) {
 			throw new UnexpectedException(e);
 		}
 	}
 
 	public boolean isDeserialized() {
 		return (object != null);
 	}
 
 	public T get() {
 		if (!isDeserialized()) {
 			throw new IllegalStateException("Object not deserialized.");
 		}
 		return object;
 	}
 
 	public T get(boolean deserialize) throws ClassNotFoundException {
 		if (!isDeserialized()) {
 			if (deserialize) {
 				deserialize();
 			} else {
 				throw new IllegalStateException("Object not deserialized.");
 			}
 		}
 		return object;
 	}
 
 	@SuppressWarnings("unchecked")
 	public T deserialize() throws ClassNotFoundException {
 		try {
 			ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
 			ObjectInputStream objectStream = new ObjectInputStream(byteStream);
 			object = (T) objectStream.readObject();
 			return object;
 		} catch (IOException e) {
 			throw new UnexpectedException(e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public T deserialize(ClassLoader loader) throws ClassNotFoundException {
 		try {
 			ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
 			ObjectInputStream objectStream = new AlternateClassLoaderObjectInputStream(
 					byteStream, loader);
 			object = (T) objectStream.readObject();
 			return object;
 		} catch (IOException e) {
 			throw new UnexpectedException(e);
 		}
 	}
 
 }
