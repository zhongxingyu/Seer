 package net.sf.okapi.tm.trados;
 
 import java.util.ArrayList;
 import java.util.List;
 import com.jacob.activeX.ActiveXComponent;
 import com.jacob.com.Variant;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.lib.translation.ITMQuery;
 import net.sf.okapi.lib.translation.QueryResult;
 
 public class TradosTMConnector implements ITMQuery {
 	
 	private int threshold = 100;
 	private int maxHits = 5;
 	private List<QueryResult> results;
 	private int current = -1;
 	private String srcLang;
 	private String trgLang;
 	
 	ActiveXComponent tradosInstance, tmInstance;
 
 	public void open(String connectionString) {
 
 		tradosInstance = new ActiveXComponent("TW4Win.Application");
 		tmInstance = tradosInstance.getPropertyAsComponent("TranslationMemory");
 		tmInstance.invoke("Open",new Variant(connectionString),new Variant("BATCH_ANALYZER"));
 
 	}
 		
 	public void close() {
 		tradosInstance.invoke("quit", new Variant[] {});
 	}
 

 	public void export(String outputPath) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public int query(String plainText) {
 		TextFragment tf = new TextFragment(plainText);
 		return query(tf);
 	}
 
 	public int query(TextFragment text) {
 		
 		String s = text.getCodedText();
 		current = -1;
 		
 		try {
 	    	//ActiveXComponent xl = ActiveXComponent.connectToActiveInstance("TW4Win.Application");
 	    	//if(xl==null){
 	    	//	xl = new ActiveXComponent("TW4Win.Application");    		
 	    	//}
 			//ActiveXComponent tm = xl.getPropertyAsComponent("TranslationMemory");
 			//tm.invoke("Open",new Variant("D:\\Rainbow_ID_Aligner\\tm\\testMultiple.tmw"),new Variant("BATCH_ANALYZER"));
 			ActiveXComponent tu = tmInstance.getPropertyAsComponent("TranslationUnit");
 
 			//--execute the search--
 			tmInstance.invoke("Search",new Variant(s));
 			
 			current = -1;
 			
 			int hits = tmInstance.invoke("HitCount").getInt();
 			int counter = 0;
 			
 			if (hits ==0){
 				return 0;
 			}else{
 				
 				results = new ArrayList<QueryResult>();
 				do {
 					counter++;
 					QueryResult qr = new QueryResult();
 					qr.score = tu.invoke("Score").getInt();
 					qr.source = new TextFragment(tu.invoke("Source").getString());
 					qr.target = new TextFragment(tu.invoke("Target").getString());
 					results.add(qr);
 					
 				} while (tu.invoke("Next").getBoolean() && (counter < maxHits));
 			}
 			
 			current = 0;
 	
         } catch (Exception e) {
     		System.out.println("Error occured");
         } 			
 		
 		return results.size();
 	}
 
 	public void removeAttribute(String anme) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setAttribute(String name, String value) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public boolean hasOption(int option) {
 		switch ( option ) {
 		case HAS_FILEPATH:
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	public QueryResult next() {
 		if ( results == null ) return null;
 		if (( current > -1 ) && ( current < results.size() )) {
 			current++;
 			return results.get(current-1);
 		}
 		current = -1;
 		return null;
 	}
 	
 	public boolean hasNext() {
 		if ( results == null ) return false;
 		if ( current >= results.size() ) {
 			current = -1;
 		}
 		return (current > -1);
 	}
 
 	public String getSourceLanguage () {
 		return srcLang;
 	}
 	
 	public String getTargetLanguage () {
 		return trgLang;
 	}	
 
 	public void setLanguages(String sourceLang, String targetLang) {
 		srcLang = sourceLang;
 		trgLang = targetLang;
 	}
 
 	public void setMaximumHits(int max) {
 		if ( max < 1 ) maxHits = 1;
 		else maxHits = max;
 	}
 	
 	public int getMaximunHits() {
 		return maxHits;
 	}
 
 	public void setThreshold(int threshold) {
 		this.threshold = threshold;
 	}
 
 	public int getThreshold() {
 		return threshold;	}
 	}
