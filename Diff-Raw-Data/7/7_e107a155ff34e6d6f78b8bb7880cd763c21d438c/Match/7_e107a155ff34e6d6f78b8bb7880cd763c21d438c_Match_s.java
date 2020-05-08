 package br.eti.rslemos.nlp.corpora.chave.parser;
 
 import gate.AnnotationSet;
 import gate.FeatureMap;
 import gate.util.InvalidOffsetException;
 import gate.util.SimpleFeatureMapImpl;
 
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 public final class Match {
 	private final int from;
 	private final int to;
 	
 	private final Set<Span> spans;
 	
 	public class Span extends br.eti.rslemos.nlp.corpora.chave.parser.Span {
 		public Span(int from, int to, int entry) {
 			super(from, to, entry);
 		}
 
 		public Match getMatch() {
 			return Match.this;
 		}
 
 //		@Override
 //		public boolean equals(Object o) {
 //			if (!super.equals(o))
 //				return false;
 //			
 //			if (!(o instanceof Span))
 //				return false;
 //			
 //			Span m = (Span) o;
 //			return Match.this == m.getMatch(); 
 //		}
 
 //		@Override
 //		public int hashCode() {
 //			return 11 * System.identityHashCode(Match.this) + super.hashCode();
 //		}
 	}
 	
 	private Match(int from, int to, Set<Span> spans) {
 		this.from = from;
 		this.to = to;
 		this.spans = spans;
 	}
 	
 	public void apply(AnnotationSet annotationSet, List<CGEntry> cg) throws InvalidOffsetException {
 		for (Span span : getSpans()) {
 			if (span.from >= 0 && span.to >= 0) {
 				FeatureMap features = new SimpleFeatureMapImpl();
 				features.put("match", cg.get(span.entry).getKey());
 				features.put("cg", cg.get(span.entry).getValue());
 				annotationSet.add((long)span.from, (long)span.to, "token", features);
 			}
 		}
 	}
 
 	public Match adjust(int k, int i) {
 		Set<Span> spans = new LinkedHashSet<Span>(this.spans.size());
 		
 		for (Span span : this.spans) {
			span = new Span(span.from + k, span.to + k, span.entry + i);
 			spans.add(span);
 		}
 		
		return new Match(from + k, to + k, spans);
 	}
 
 	public int getMatchLength() {
 		return to - from;
 	}
 
 	public int getSkipLength() {
 		return from;
 	}
 
 	public int getFrom() {
 		return from;
 	}
 	
 	public int getTo() {
 		return to;
 	}
 	
 	public Set<Span> getSpans() {
 		return spans;
 	}
 
 	public int getConsume() {
 		return spans.size();
 	}
 	
 	public boolean equals(Object o) {
 		if (o == null)
 			return false;
 		
 		if (o == this)
 			return true;
 		
 		if (!(o instanceof Match))
 			return false;
 		
 		Match m = (Match) o;
 		
 		return this.from == m.from &&
 			this.to == m.to &&
 			(this.spans != null ? this.spans.equals(m.spans) : m.spans == null);
 	}
 	
 	public int hashCode() {
 		return this.to * 3 + this.from * 5 + (this.spans != null ? this.spans.hashCode() * 7 : 1); 
 	}
 	
 	public String toString() {
 		return "{" + from + ", " + to + "}/" + spans;
 	}
 	
 	private Match(int from, int to, br.eti.rslemos.nlp.corpora.chave.parser.Span... nakedSpans) {
 		this.from = from;
 		this.to = to;
 		this.spans = new LinkedHashSet<Span>(nakedSpans.length);
 		
 		for (br.eti.rslemos.nlp.corpora.chave.parser.Span nakedSpan : nakedSpans) {
 			Span span = new Span(nakedSpan.from, nakedSpan.to, nakedSpan.entry);
 			spans.add(span);
 		}
 	}
 	
 	public static Match match(int from, int to, br.eti.rslemos.nlp.corpora.chave.parser.Span... spans) {
 		return new Match(from, to, spans);
 	}
 }
