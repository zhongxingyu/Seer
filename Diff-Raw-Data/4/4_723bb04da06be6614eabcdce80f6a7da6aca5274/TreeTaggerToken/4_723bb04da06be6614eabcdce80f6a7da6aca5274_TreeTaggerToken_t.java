 package keywordExctraction;
 
 public class TreeTaggerToken {
 	private String pos;
 	private String lemma;
 	
 	public TreeTaggerToken(String pos, String lemma) {
 		super();
 		this.pos = pos;
 		this.lemma = lemma;
 	}
 
 	public String getPos() {
 		return pos;
 	}
 
 	public String getLemma() {
 		return lemma;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if(obj instanceof TreeTaggerToken){
			if(((TreeTaggerToken)obj).lemma.compareTo(this.lemma) == 0
					&& ((TreeTaggerToken)obj).pos.compareTo(this.pos) == 0){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public String toString() {
 		return pos + "\t" + lemma + "\n";
 	}
 }
