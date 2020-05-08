 /**
  * 
  */
 package openscriptures.ref;
 
 /**
  * Represents a selection of Bible verses. This may be a single verse, a . 
  * @author Neal Audenaert
  */
 public abstract class Passage implements Comparable<Passage> {
 	protected static final String LOGGER = Passage.class.getName();
 	
 	protected BookOrder order = BookOrder.KJV;
 	
 	protected Passage(BookOrder order) {
 		this.order = order;
 	}
 	
 	public abstract VerseRef getFirst();
 	
 	public abstract VerseRef getLast();
 	
 //	public abstract boolean contains(VerseRef ref);
 	
 
 	public BookOrder getBookOrder() {
 		return this.order;
 	}
 	
 	private int compareParts(Integer a, Integer b) {
 		if ((a != null) && (b != null)) {
 			int ch = a - b;
 			return ch;
 		} else {
 			return (a == b) ? 0 
 					: (a == null) ? -1 : 1;
 		}
 	}
 	
 	public int compareTo(Passage passage) {
 		// TODO need to make sure that we're using the same book order.
 		//      figure out how to order them if we aren't.
 		// TODO possible null pointer errors
 		
 		VerseRef a = this.getFirst();
 		VerseRef b = passage.getFirst();
 		
 		int result = compareParts(a.getBookIndex(), b.getBookIndex());
 		if (result != 0) return result;
 			
 		result = compareParts(a.getChapter(), b.getChapter());
 		if (result != 0) return result;
 		
 		result = compareParts(a.getVerse(), b.getVerse());
 		if (result != 0) return result;
 		
 		
		String extA = a.getExtension();
		String extB = b.getExtension();
		
		extA = (extA == null) ? "" : extA.toLowerCase();
	    extB = (extB == null) ? "" : extB.toLowerCase();
 		
 		return extA.compareTo(extB);
 	}
 }
