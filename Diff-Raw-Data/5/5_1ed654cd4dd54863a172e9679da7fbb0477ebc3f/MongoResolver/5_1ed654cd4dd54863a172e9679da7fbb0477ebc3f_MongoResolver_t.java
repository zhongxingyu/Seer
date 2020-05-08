 package org.qrone.mongo;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.WeakHashMap;
 
 import org.qrone.r7.resolver.AbstractURIResolver;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 public class MongoResolver extends AbstractURIResolver{
 	private static final String ID = "id";
 	private static final String DATA = "data";
 	
 	private Map<String, byte[]> weakmap = new WeakHashMap<String, byte[]>();
 	
 	private DBCollection col;
 
 	public MongoResolver(DB db, String collection){
 		this.col = db.getCollection(collection);
 	}
 
 	@Override
 	public boolean exist(String path) {
 		if(weakmap.containsKey(path)){
 			return false;
 		}else{
 			InputStream i = null;
 			try {
 				i = getInputStream(new URI(path));
 			} catch (IOException e) {
 			} catch (URISyntaxException e) {
 			}
 			if(i != null) return true;
 			return false;
 		}
 	}
 
 	@Override
 	public boolean remove(URI uri) {
 		BasicDBObject obj = new BasicDBObject();
 		obj.put(ID, uri.toString());
 		col.remove(obj);
 		fireUpdate(uri);
 		return false;
 	}
 
 	@Override
 	public InputStream getInputStream(URI uri) throws IOException {
		byte[] cache = weakmap.get(uri.toString());
		if(cache != null){
			return new ByteArrayInputStream(cache);
 		}
 		try{
 			BasicDBObject obj = new BasicDBObject();
 			obj.put(ID, uri.toString());
 			DBCursor c = col.find(obj);
 			DBObject o = c.next();
 			byte[] v = (byte[])o.get(DATA);
 			weakmap.put(uri.toString(), v);
 			return new ByteArrayInputStream(v);
 		}catch(NoSuchElementException e){
 			return null;
 		}
 	}
 
 	@Override
 	public OutputStream getOutputStream(URI uri) throws IOException {
 		return new MongoOutputStream(uri);
 	}
 	
 
 	private class MongoOutputStream extends ByteArrayOutputStream{
 		private URI uri;
 		public MongoOutputStream(URI uri) {
 			this.uri = uri;
 		}
 		
 		@Override
 		public void close() throws IOException {
 			super.close();
 			
 			BasicDBObject obj = new BasicDBObject();
 			obj.put(ID, uri.toString());
 			obj.put(DATA, toByteArray());
 			col.save(obj);
 			fireUpdate(uri);
 		}
 	}
 	
 }
