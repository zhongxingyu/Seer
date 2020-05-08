 package org.fuzzydb.samples;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.validation.Valid;
 
 import org.fuzzydb.samples.repositories.ItemRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageImpl;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.thoughtworks.xstream.XStream;
 import com.wwm.db.query.Result;
 import com.wwm.db.spring.repository.AttributeMatchQuery;
 import com.wwm.db.spring.repository.PageUtils;
 import com.wwm.db.spring.repository.SubjectMatchQuery;
 
 /**
  * Example controller for creating fuzzy items and querying them
  */
 @Controller
 public class SearchController {
 
 
 	@Autowired
 	private DataGenerator dataGenerator;
 	
 	
 	/**
 	 * A repository provided the line
 	 * <code>&lt;fuzzy:repository id="itemRepository" class="org.fuzzydb.samples.FuzzyItem" useDefaultNamespace="true" /></code>
 	 * in the application context.
 	 */
 	@Autowired
 	private ItemRepository itemRepo;
 
 
 	
 	@Transactional
 	@RequestMapping(value="/createPeople", method=RequestMethod.GET) 
 	public String createPeople(@RequestParam(defaultValue="5") int numPeople) {
 		
 		for (int i = 0; i < numPeople; i++) {
 			itemRepo.save(dataGenerator.createRandomPerson());
 		}
 		return "redirect:/search";
 	}
 
 	@Transactional(readOnly=true)
 	@RequestMapping(value="/search", method=RequestMethod.POST) 
 	public String search(
 			@RequestParam(defaultValue="similarPeople") String style,
 			@RequestParam(defaultValue="0") int start,
 			@RequestParam(defaultValue="10") int pageSize,
 			Model model, 
 			@ModelAttribute("command") @Valid FuzzyItem form, 
 			Errors result) {
 		
 		Pageable pageable = new PageRequest(start, pageSize);
 		doSearch(model, style, null, pageable, form);
 		
 		return "results";
 	}
 
 	/**
 	 * 
 	 * @param style The name of the matching configuration or 'style'
 	 * @param ref A database reference - something we probably don't want in a real app
 	 * @param maxResults This lets the database know the maximum number of results you're going to iterate over
      *	 		you could potentially feed results out as you get them, so long as you've got the 
 	 *			transaction held open... hmmm. I feel some Ajax coming on ;)
 	 */
 	@Transactional(readOnly=true)
 	@RequestMapping(value="/search", method=RequestMethod.GET)
 	public String findMatches(
 			Model model, 
 			@RequestParam(defaultValue="similarPeople") String style,
 			@RequestParam(required=false) String ref,
 			@RequestParam(defaultValue="0") int start,
 			@RequestParam(defaultValue="10") int pageSize) {
 	
 		// We need some attributes to search against.  This doesn't have to be something already in the 
 		// database.  For the default, we'll just grab a named sample from our dataGenerator.
 		// If we have a key (ref), then we'll use that to grab an item.
 		FuzzyItem idealMatch = StringUtils.hasText(ref) ? itemRepo.findOne(ref) : dataGenerator.createPerson("Matt");
 
 		Pageable pageable = new PageRequest(start/pageSize, pageSize);
 		doSearch(model, style, ref, pageable, idealMatch);
 		model.addAttribute("command", new FuzzyItem("Entered search"));
 		return "results";
 	}
 
 	protected void doSearch(Model model, String style, String ref,
 			Pageable pageable, FuzzyItem idealMatch) {
 		// A SubjectMatchQuery looks for the best matches for a provided subject, according to the
 		// requested match style
 		int maxResults = pageable.getOffset() + pageable.getPageSize(); 
 		AttributeMatchQuery<FuzzyItem> query = new SubjectMatchQuery<FuzzyItem>(idealMatch, style, maxResults);
 		
 		// Do the actual query
		Iterator<Result<FuzzyItem>> resultIterator = itemRepo.findMatchesFor(query);
		
		// Extract the results
		Page<Result<FuzzyItem>> results = PageUtils.getPage(resultIterator, pageable);
		
 		// Stick 'em in our model for our view to render
 		model.addAttribute("subject", idealMatch);
 		model.addAttribute("ref", ref);
 		model.addAttribute("results", results.getContent());
 		model.addAttribute("style", style);
 		model.addAttribute("startNextPage", results.hasNextPage() ? maxResults : -1);
 		model.addAttribute("pageSize", results.getSize());
 	}
 
 
 	@Transactional(readOnly=true)
 	@RequestMapping(value="/dump", method=RequestMethod.GET)
 	public void dump(OutputStream response) throws IOException {
 
 		Iterable<FuzzyItem> people = itemRepo.findAll();
 		
 		XStream xs = new XStream();
 		
 		for (FuzzyItem item : people) {
 			xs.toXML(item, response);
 		}
 	}
 	
 	@Transactional(readOnly=true)
 	@RequestMapping(value="/dumpMatches", method=RequestMethod.GET)
 	public void dumpMatches(
 			Model model, 
 			@RequestParam(defaultValue="Matt") String name,
 			@RequestParam(defaultValue="similarPeople") String style,
 			OutputStream response) {
 	
 		FuzzyItem subject = dataGenerator.createPerson(name);
 		int maxResults = 10; 
 		AttributeMatchQuery<FuzzyItem> query = new SubjectMatchQuery<FuzzyItem>(subject, style, maxResults + 1); // + 1 so we can check if there are more results
 		Iterator<Result<FuzzyItem>> resultIterator = itemRepo.findMatchesFor(query);
 		
 		XStream xs = new XStream();
 		
 		for (Iterator<Result<FuzzyItem>> iterator = resultIterator; iterator.hasNext();) {
 			Result<FuzzyItem> item = iterator.next();
 			
 			xs.toXML(item.getItem(), response);
 		}
 	}
 }
 
