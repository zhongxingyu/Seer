 package org.gwtapp.io.client;
 
 import org.gwtapp.io.rpc.api.IOService;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.SerializationException;
 import com.google.gwt.user.client.rpc.SerializationStreamFactory;
 import com.google.gwt.user.client.rpc.SerializationStreamWriter;
 import com.google.gwt.user.client.ui.HTML;
 
 public class IOClient {
 
 	private final static SerializationStreamFactory ssf = GWT
 			.create(IOService.class);
 
 	public static String getSerializedString(Object object)
 			throws SerializationException {
 		SerializationStreamWriter ssw = ssf.createStreamWriter();
 		ssw.writeObject(object);
 		return ssw.toString();
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> T getDeserializedObject(String serialized)
 			throws SerializationException {
 		return (T) ssf.createStreamReader(serialized).readObject();
 	}
 
 	public static String decode(String t) {
		return new HTML(t).getHTML();
 	}

 }
