 package edu.illinois.ncsa.versus.extract.impl;
 
 import java.security.MessageDigest;
 import java.util.HashSet;
 import java.util.Set;
 
 import edu.illinois.ncsa.versus.UnsupportedTypeException;
 import edu.illinois.ncsa.versus.adapter.Adapter;
 import edu.illinois.ncsa.versus.adapter.HasBytes;
 import edu.illinois.ncsa.versus.descriptor.Descriptor;
 import edu.illinois.ncsa.versus.descriptor.impl.MD5Digest;
 import edu.illinois.ncsa.versus.extract.Extractor;
 
 /**
  * TODO move to DigestInputStream instead of loading all bytes in memory.
  * 
  * @author Luigi Marini <lmarini@ncsa.illinois.edu>
  *
  */
 public class MD5Extractor implements Extractor {
 
 	@Override
 	public Descriptor extract(Adapter adapter) throws Exception {
 		if (adapter instanceof HasBytes) {
 			HasBytes hasBytes = (HasBytes) adapter;
 			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
 			byte[] digest = messageDigest.digest(hasBytes.getBytes());
 			return new MD5Digest(digest);
 		} else {
 			throw new UnsupportedTypeException();
 		}
 	}
 
 	@Override
 	public Adapter newAdapter() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getName() {
 		return "MD5 Digest";
 	}
 
 	@Override
 	public Set<Class<? extends Adapter>> supportedAdapters() {
 		Set<Class<? extends Adapter>> adapters = new HashSet<Class<? extends Adapter>>();
 		adapters.add(HasBytes.class);
 		return adapters;
 	}
 
 	@Override
 	public Class<? extends Descriptor> getFeatureType() {
 		return MD5Digest.class;
 	}
 
 	@Override
 	public boolean hasPreview() {
 		return false;
 	}
 
 }
