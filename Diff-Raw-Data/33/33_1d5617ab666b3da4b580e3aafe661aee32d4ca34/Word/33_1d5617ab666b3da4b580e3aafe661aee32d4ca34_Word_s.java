 package dom;
 
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.HashMap;
 import java.util.List;
 
 public class Word {
 	private final List<Splitting> splittings = new ArrayList<Splitting>();
 
	private String name;
 
 	public int count = 1;
 
 	public Word(String name) {
 		this.name = name;
 	}
 
 	public List<Splitting> getSplittings() {
 		return Collections.unmodifiableList(splittings);
 	}
 
 	public void generateAllPossibleSplittings(Text text) {
 		// i and j iterate over the gaps between the characters
		// string:   a b c d e
 		// indices: 0 1 2 3 4 5
		for (int i=0; i<=4; i++){
			for (int j=name.length();j>i;j--){
				WordPart prefix=text.getOrAddPrefix(name.substring(0,i));
				WordPart stem=text.getOrAddStem(name.substring(i,j));
				WordPart suffix=text.getOrAddSuffix(name.substring(j,name.length()));
				
 				addSplitting(new Splitting(prefix, stem, suffix));
 			}
 		}
 	}
 
 	public void addSplitting(Splitting s) {
 		if (s.getWord() != null)
 			throw new Error("splitting already added to an other word");
 		splittings.add(s);
 		s.setWordImp(this);
 	}
 
 	public void removeSplitting(Splitting s) {
 		splittings.remove(s);
 		s.setWordImp(null);
 	}
 
 	void addSplittingImp(Splitting splitting) {
 		splittings.add(splitting);
 	}
 
 	void removeSplittingImp(Splitting splitting) {
 		splittings.remove(splitting);
 	}
 
 	public String getName() {
 		return name;
 	}
	
 	@Override
 	public boolean equals(Object obj) {
		if (!(obj instanceof Word)) return false;
		Word other=(Word) obj;
 		return name.equals(other.name);
 	}
	
 	@Override
 	public int hashCode() {
 		return name.hashCode();
 	}
 }
