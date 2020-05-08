 package net.metadata.openannotation.lorestore.views;
 
 import static net.metadata.openannotation.lorestore.common.LoreStoreConstants.DC_TITLE;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.bind.DatatypeConverter;
 
 import net.metadata.openannotation.lorestore.model.rdf2go.OACAnnotationImpl;
 
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.node.Node;
 import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;
 
 import com.sun.syndication.feed.atom.Entry;
 import com.sun.syndication.feed.atom.Feed;
 import com.sun.syndication.feed.atom.Link;
 import com.sun.syndication.feed.atom.Person;
 
 public class OACAtomFeedView extends AbstractAtomFeedView {
 
 	@Override
 	protected void buildFeedMetadata(Map<String, Object> model, Feed feed,
 			HttpServletRequest request) {
 		feed.setTitle("Annotations for " + (String) model.get("browseURL"));
 		
 	}
 
 	@Override
 	protected List<Entry> buildFeedEntries(Map<String, Object> model,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		List<Entry> entries = new ArrayList<Entry>();
 		
 		
 		@SuppressWarnings("unchecked")
 		List<Model> allAnnotations = (List<Model>) model.get("annotations");
 		if (allAnnotations != null){
 			Iterator<Model> itr = allAnnotations.iterator();
 		    while(itr.hasNext()){
 		     Model annoModel = itr.next();
 		     OACAnnotationImpl anno = new OACAnnotationImpl(annoModel);
 		     Entry entry = new Entry();
 		     
 		     entry.setId(anno.getURL());
 		     Link altLink = new Link();
 		     altLink.setHref(anno.getURL());
 		     entry.setAlternateLinks(Collections.singletonList(altLink));
 		     
 		     String creator = anno.getCreator();
 		     if (creator == null){
 		    	 creator = anno.getOwnerId();
 		     }
 		     if (creator != null){
 		    	 Person person = new Person();
 				 person.setUri(creator);
 				 entry.setAuthors(Collections.singletonList(person));
 		     }
 		    Node m = anno.getModifiedDate();
 		    if (m != null){
		    	Date modDate = DatatypeConverter.parseDateTime(m.toString()).getTime();
 		    	entry.setModified(modDate);
 		     }
 		    Node c = anno.getCreatedDate();
 		    if (c != null){
		    	Date cDate = DatatypeConverter.parseDateTime(m.toString()).getTime();
 		    	entry.setCreated(cDate);
 		    }
 		     
 		     Node t = anno.lookupNode(annoModel.createURI(DC_TITLE));
 		     if (t != null){
 		    	 entry.setTitle(t.toString());
 		     }
 		     annoModel.close();
 		     entries.add(entry);
 		    }
 		} 
 		
 		return entries;
 	}
 
 }
