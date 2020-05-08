 package dolda.jsvc.util;
 
 import java.util.*;
 import java.io.*;
 
 public class AcceptMap implements Iterable<AcceptMap.Entry> {
     private static final Entry[] typehint = new Entry[0];
     private final Entry[] list;
     private static final Comparator<Entry> lcmp = new Comparator<Entry>() {
 	public int compare(Entry a, Entry b) {
 	    if(a.q > b.q)
 		return(-1);
 	    else if(a.q < b.q)
 		return(1);
 	    return(0);
 	}
     };
     
     public static class Entry {
 	public String type;
 	public double q = 1.0;
 	public Map<String, String> pars = new HashMap<String, String>();
     }
     
     private AcceptMap(Entry[] list) {
 	this.list = list;
     }
     
     private static String token(PushbackReader in) throws IOException {
 	Misc.eatws(in);
 	StringBuilder buf = new StringBuilder();
 	while(true) {
 	    int c = in.read();
 	    if((c < 0) || Character.isWhitespace((char)c) || (",;=:\\\"?".indexOf((char)c) >= 0)) {
 		if(c >= 0)
 		    in.unread(c);
 		if(buf.length() == 0)
 		    return(null);
 		return(buf.toString());
 	    } else {
 		buf.append((char)c);
 	    }
 	}
     }
 
     public static AcceptMap parse(Reader in) throws IOException {
 	PushbackReader pin = new PushbackReader(in);
 	List<Entry> lbuf = new LinkedList<Entry>();
 	List<Entry> ebuf = new LinkedList<Entry>();
 	while(true) {
 	    Entry e = new Entry();
 	    if((e.type = token(pin)) == null)
 		throw(new Http.EncodingException("Illegal format for Accept* header (expected type)"));
 	    ebuf.add(e);
 	    Misc.eatws(pin);
 	    int sep = pin.read();
 	    if(sep < 0) {
 		lbuf.addAll(ebuf);
 		break;
 	    } else if(sep == ';') {
 		String st = "tp";
 		boolean flush = false;
 		boolean end = false;
 		while(true) {
 		    String key = Http.tokenunquote(pin);
 		    Misc.eatws(pin);
 		    if(pin.read() != '=')
 			throw(new Http.EncodingException("Illegal format for Accept* header (expected `=' in parameter)"));
 		    String val = Http.tokenunquote(pin);
 		    Misc.eatws(pin);
 		    int psep = pin.read();
 		    if(st == "tp") {
 			if(key.equals("q")) {
 			    double q;
 			    try {
 				q = Double.parseDouble(val);
 			    } catch(NumberFormatException exc) {
 				throw(new Http.EncodingException(exc.getMessage()));
 			    }
 			    for(Entry e2 : ebuf)
 				e2.q = q;
 			    flush = true;
 			    st = "ap";
			} else {
 			    e.pars.put(key, val);
 			}
		    } else if(st == "ap") {
 			/* No known accept-params */
 		    }
 		    if(psep < 0) {
 			end = true;
 			flush = true;
 			break;
 		    } else if(psep == ';') {
 		    } else if(psep == ',') {
 			break;
 		    } else {
 			throw(new Http.EncodingException("Illegal format for Accept* header (expected `;', `,' or end of parameters)"));
 		    }
 		}
 		if(flush) {
 		    lbuf.addAll(ebuf);
 		    ebuf = new LinkedList<Entry>();
 		}
 		if(end)
 		    break;
 	    } else if(sep == ',') {
 	    } else {
 		throw(new Http.EncodingException("Illegal format for Accept* header (expected `;', `,' or end of list)"));
 	    }
 	}
 	Entry[] list = lbuf.toArray(typehint);
 	Arrays.sort(list, lcmp);
 	return(new AcceptMap(list));
     }
     
     public static AcceptMap parse(String input) {
 	try {
 	    return(parse(new StringReader(input)));
 	} catch(IOException e) {
 	    throw(new Error(e));
 	}
     }
 
     public Iterator<Entry> iterator() {
 	return(new Iterator<Entry>() {
 		private int i = 0;
 		
 		public Entry next() {
 		    return(list[i++]);
 		}
 		
 		public boolean hasNext() {
 		    return(i < list.length);
 		}
 		
 		public void remove() {
 		    throw(new UnsupportedOperationException());
 		}
 	    });
     }
     
     public Entry accepts(String type) {
 	for(Entry e : list) {
 	    if(e.type.equals(type))
 		return(e);
 	}
 	return(null);
     }
     
     public String toString() {
 	StringBuilder buf = new StringBuilder();
 	for(Entry e : list)
 	    buf.append(String.format("%s %f %s\n", e.type, e.q, e.pars));
 	return(buf.toString());
     }
 }
