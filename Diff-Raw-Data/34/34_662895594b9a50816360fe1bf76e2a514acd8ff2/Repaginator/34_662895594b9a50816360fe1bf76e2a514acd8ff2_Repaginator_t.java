 package com.simplexrepaginator;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDPage;
 
 /**
  * Class to repaginate or unrepaginate a {@link PDDocument}.
  * @author robin
  *
  */
 public class Repaginator {
 	/**
 	 * Repaginate a document.  Converts a document whose logical
 	 * page ordering is 1,3,5,7,8,6,4,2 to the "proper" ordering
 	 * of 1,2,3,4,5,6,7,8
 	 * @param doc The document to repaginate
 	 * @return A new document, with the proper pagination
 	 */
	public PDDocument repaginated(PDDocument doc) throws RepaginatorException {
 		List<PDPage> pages = new ArrayList<PDPage>();
 		doc.getDocumentCatalog().getPages().getAllKids(pages);
 		
 		PDDocument ret;
 		try {
 			ret = new PDDocument();
 		} catch(IOException ioe) {
			throw new RepaginatorException("Unable to create PDDocument", ioe);
 		}
 
 		int front = 0;
 		int back = pages.size() - 1;
 		
 		// Grab alternating pages from the front and back of the list
		while(front <= back) {
 			ret.addPage(pages.get(front++));
			if(front <= back)
 				ret.addPage(pages.get(back--));
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * Unrepaginate a document.  Does the opposite of {@link #repaginated(PDDocument)}.
 	 * Call in case you accidentally repaginate a document that wasn't in
 	 * the expected input order, such as if it was already properly paginated.
 	 * @param doc
 	 * @return
 	 */
	public PDDocument unrepaginated(PDDocument doc) throws RepaginatorException {
 		List<PDPage> pages = new ArrayList<PDPage>();
 		doc.getDocumentCatalog().getPages().getAllKids(pages);
 		
 		List<PDPage> fronts = new ArrayList<PDPage>(); // front pages
 		List<PDPage> backs = new ArrayList<PDPage>(); // back pages
 		
 		int front = 0;
		int back = (pages.size() / 2) * 2 - 1;
 		
		while(front < pages.size()) {
			fronts.add(pages.get(front));
			front += 2;
		}
		
		while(back >= 0) {
			backs.add(pages.get(back));
			back -= 2;
 		}
 		
 		PDDocument ret;
 		try {
 			ret = new PDDocument();
 		} catch(IOException ioe) {
			throw new RepaginatorException("Unable to create PDDocument", ioe);
 		}
 		
 		// Add the front pages, e.g. 1,3,5,7
 		for(PDPage page : fronts) {
 			ret.addPage(page);
 		}
 		// Add the back pages, e.g. 8,6,4,2
 		for(PDPage page : backs) {
 			ret.addPage(page);
 		}
 		
 		return ret;
 	}
 }
