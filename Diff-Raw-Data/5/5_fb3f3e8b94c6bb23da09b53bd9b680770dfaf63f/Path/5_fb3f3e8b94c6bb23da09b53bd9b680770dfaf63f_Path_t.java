 package net.cheney.cocktail.application;
 
 import static org.apache.commons.lang.StringUtils.join;
 import static org.apache.commons.lang.StringUtils.split;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 
 public class Path implements Iterable<String> {
 
 	private String[] parts;
 
 	private Path(String[] parts) {
 		this.parts = parts;
 	}
 	
 	public static Path emptyPath() {
 		return new Path(new String[0]);
 	}
 	
 	public static Path fromURI(URI uri) {
 		String path = uri.getPath();
 		return path.equals("/") ? emptyPath() : new Path(split(path, '/')); 
 	}
 
 	@Override
 	public String toString() {
 		return parts.length == 0 ? "/" : "/" + join(parts, '/'); 
 	}
 
 	@Override
 	public Iterator<String> iterator() {
 		return Iterators.forArray(parts);
 	}
 	
 	@Override
 	public boolean equals(Object that) {
 		if(that instanceof Path) {
 			return Arrays.equals(((Path)that).parts, this.parts);
 		} else {
 			return false;
 		}
 	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.parts);
	}
 
 	public static Path create(String... string) {
 		return new Path(string);
 	}
 
 	public Path append(String name) {
 		ArrayList<String> l = Lists.newArrayList(parts);
 		l.add(name);
 		return new Path(l.toArray(new String[l.size()]));
 	}
 
 	public boolean isEmpty() {
 		return parts.length == 0;
 	}
 
 }
