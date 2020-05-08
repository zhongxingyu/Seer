 package org.paxle.se.provider.paxleinfrastructure.impl;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.paxle.core.doc.IIndexerDocument;
 import org.paxle.core.doc.IndexerDocument;
 import org.paxle.se.query.ITokenFactory;
 import org.paxle.se.search.ISearchProvider;
 
 public class PaxleInfrastructureSearchProvider implements ISearchProvider {
 
 	public PaxleInfrastructureSearchProvider(){
 	}
 	public ITokenFactory getTokenFactory() {
 		return new PaxleInfrastructureTokenFactor();
 	}
 	
 	
 	public void search(String request, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException, InterruptedException {
 		try {
 			IIndexerDocument indexerDoc = new IndexerDocument();
 			System.out.println(request);
 			if(request.toLowerCase().equals("paxle wiki")){
 				indexerDoc.set(IIndexerDocument.LOCATION, "http://wiki.paxle.net/");
 				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Wiki");
 			}else if(request.toLowerCase().equals("paxle homepage")){
 				indexerDoc.set(IIndexerDocument.LOCATION, "http://www2.paxle.net/");
 				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Homepage");
 			}else if(request.toLowerCase().equals("paxle forum")){
 				indexerDoc.set(IIndexerDocument.LOCATION, "http://forum.paxle.info/");
 				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Forum");
 			}else if(request.toLowerCase().equals("paxle bts")){
 				indexerDoc.set(IIndexerDocument.LOCATION, "http://bugs.paxle.net/");
 				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Bugtracker");
			}else if(request.toLowerCase().startsWith("paxle bug #")){
				String bugNum=request.substring(11);
				indexerDoc.set(IIndexerDocument.LOCATION, "https://bugs.pxl.li/view.php?id="+bugNum);
				indexerDoc.set(IIndexerDocument.TITLE, "Paxle Bug #"+bugNum);
 			}else{
 				indexerDoc=null;
 			}
 			if(indexerDoc!=null)
 				results.add(indexerDoc);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
