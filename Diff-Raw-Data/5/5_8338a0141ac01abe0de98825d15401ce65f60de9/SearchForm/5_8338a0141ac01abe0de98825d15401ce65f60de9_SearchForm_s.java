 package uk.ac.cam.sup.form;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.QueryParam;
 
 import uk.ac.cam.sup.exceptions.FormValidationException;
 import uk.ac.cam.sup.exceptions.ModelNotFoundException;
 import uk.ac.cam.sup.exceptions.QueryAlreadyOrderedException;
 import uk.ac.cam.sup.models.Tag;
 import uk.ac.cam.sup.models.User;
 import uk.ac.cam.sup.queries.Query;
 import uk.ac.cam.sup.queries.TagQuery;
 import uk.ac.cam.sup.queries.UserQuery;
 import uk.ac.cam.sup.util.Mappable;
 import uk.ac.cam.sup.util.TripleChoice;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 
 public abstract class SearchForm<T extends Mappable> {
 	@QueryParam("tags")
 	protected String tags;
 	protected List<Tag> tagList = new ArrayList<Tag>();
 	
 	@QueryParam("authors")
 	protected String authors;
 	protected List<User> authorList = new ArrayList<User>();
 	
 	@QueryParam("minduration")
 	protected String minDuration;
 	protected Integer minDurationInt;
 	
 	@QueryParam("maxduration")
 	protected String maxDuration;
 	protected Integer maxDurationInt;
 	
 	@QueryParam("after")
 	protected String after;
 	protected Date afterDate = new Date();
 	
 	@QueryParam("before")
 	protected String before;
 	protected Date beforeDate = new Date();
 	
 	@QueryParam("supervisor")
 	protected String supervisor;
 	protected TripleChoice supervisorChoice = TripleChoice.DONT_CARE;
 	
 	@QueryParam("starred")
 	protected String starred;
 	protected TripleChoice starredChoice = TripleChoice.DONT_CARE;
 	
 	@QueryParam("page")
 	protected String page;
 	protected Integer pageInt;
 	
 	@QueryParam("amount")
 	protected String amount;
 	protected Integer amountInt;
 	
 	protected Integer totalAmount = null;
 	protected boolean emptySearch = true;
 	
 	protected boolean validated = false;
 	
 	protected final void checkValidity() throws FormValidationException {
 		if ( ! this.validated) {
 			throw new FormValidationException("Form was nod validated");
 		}
 	}
 	
 	public SearchForm<T> validate() throws FormValidationException {
 		
 		if (tags == null) {
 			tags = "";
 		}
 
 		if (authors == null) {
 			authors = "";
 		}
 		
 		if (minDuration == null) {
 			minDuration = "";
 		}
 		
 		if (maxDuration == null) {
 			maxDuration = "";
 		}
 		
 		if (after == null) {
 			after = "";
 		}
 		
 		if (before == null) {
 			before = "";
 		}
 		
 		if (supervisor == null || supervisor.equals("")) {
 			supervisor = "DONT_CARE";
 		}
 		
 		try {
 			TripleChoice.valueOf(supervisor);
 		} catch (IllegalArgumentException | NullPointerException e) {
 			throw new FormValidationException
 				("Illegal value for supervisor filter: "+supervisor);
 		}
 		
 		if (starred == null || starred.equals("")) {
 			starred = "DONT_CARE";
 		}
 		
 		try {
 			TripleChoice.valueOf(starred);
 		} catch (IllegalArgumentException | NullPointerException e) {
 			throw new FormValidationException
 				("Illegal value for starred filter: "+starred);
 		}
 		
 		if(page == null || page == ""){
 			page = "1";
 			pageInt = 1;
 		}
 		
 		if(amount == null || amount == ""){
 			amount = "25";
 			amountInt = 25;
 		}
 			
 		this.validated = true;
 		return this;
 	}
 	
 	public SearchForm<T> parse() throws FormValidationException {
 		checkValidity();
 		
 		try{
 			pageInt = Integer.parseInt(page);
 		} catch (NumberFormatException e) {
 			page = "1";
 			pageInt = 1;
 		}
 		try{
 			amountInt = Integer.parseInt(amount);
 		} catch (NumberFormatException e) {
 			amount = "25";
 			amountInt = 25;
 		}
 		
 		String[] split = tags.split(",");
 		if(tags.trim().length() > 0) emptySearch = false;
 		for (String s: split) {
 			if ( ! s.equals("")) {
 				tagList.add(TagQuery.get(s.trim()));
 			}
 		}
 		
 		split = authors.split(",");
 		if(authors.trim().length() > 0) emptySearch = false;
 		for (String s: split) {
 			if ( ! s.equals("")) {
 				try {
 					authorList.add(UserQuery.get(s.trim()));
 				} catch (ModelNotFoundException e) {
 					continue;
 				}
 			}
 		}
 		
 		try {
 			minDurationInt = Integer.parseInt(minDuration);
 			emptySearch = false;
 		} catch (Exception e) {
 			minDurationInt = null;
 		}
 		
 		try {
 			maxDurationInt = Integer.parseInt(maxDuration);
 			emptySearch = false;
 		} catch (Exception e) {
 			maxDurationInt = null;
 		}
 		
 		Calendar c = Calendar.getInstance();
 		try {
 			split = before.split("/");
 			int day = Integer.parseInt(split[0]);
 			int month = Integer.parseInt(split[1]);
 			int year = Integer.parseInt(split[2]);
			c.set(year, month, day);
 			beforeDate = c.getTime();
 			emptySearch = false;
 		} catch(Exception e) {
 			beforeDate = null;
 		}
 		
 		try {
 			split = after.split("/");
 			int day = Integer.parseInt(split[0]);
 			int month = Integer.parseInt(split[1]);
 			int year = Integer.parseInt(split[2]);
			c.set(year, month, day);
 			afterDate = c.getTime();
 			emptySearch = false;
 		} catch(Exception e) {
 			afterDate = null;
 		}
 		
 		supervisorChoice = TripleChoice.valueOf(supervisor);
 		starredChoice = TripleChoice.valueOf(starred);
 		
 		if(supervisorChoice != TripleChoice.DONT_CARE || starredChoice != TripleChoice.DONT_CARE){
 			emptySearch = false;
 		}
 		
 		return this;
 	}
 	
 	public final List<T> getSearchResults() {
 		Query<T> query = getQueryObject();
 		
 		if (query == null) {
 			totalAmount = 0;
 			return new ArrayList<T>();
 		}
 		
 		if (tagList.size() > 0) {
 			query.withTags(tagList);
 		}
 		
 		if (authorList.size() > 0) {
 			query.withOwners(authorList);
 		}
 		
 		if (supervisorChoice == TripleChoice.YES) {
 			query.bySupervisor();
 		} else if (supervisorChoice == TripleChoice.NO) {
 			query.byStudent();
 		}
 		
 		if (starredChoice == TripleChoice.YES){
 			query.withStar();
 		} else if (starredChoice == TripleChoice.NO) {
 			query.withoutStar();
 		}
 		
 		if(maxDurationInt != null) query.maxDuration(maxDurationInt);
 		if(minDurationInt != null) query.minDuration(minDurationInt);
 		if(afterDate != null) query.after(afterDate);
 		if(beforeDate != null) query.before(beforeDate);
 		
 		if(emptySearch || !query.isModified()){
 			totalAmount = 0;
 		} else{
 			try {
 				totalAmount = query.size();
 			} catch (QueryAlreadyOrderedException e) {
 				totalAmount = -1;
 				e.printStackTrace();
 			}
 		}
 		
 		if(query.isModified()){
 			return query
 					.maxResults(amountInt)
 					.offset(amountInt * (pageInt - 1))
 					.list();
 		} else {
 			return new ArrayList<T>();
 		}
 	}
 	
 	public final Map<String, ?> toMap() {
 		Builder<String,Object> b = new ImmutableMap.Builder<String,Object>();
 		b.put("tags", tags);
 		b.put("authors", authors);
 		b.put("minDuration", minDuration);
 		b.put("maxDuration", maxDuration);
 		b.put("after", after);
 		b.put("before", before);
 		b.put("supervisor", supervisor);
 		b.put("starred", starred);
 		if(totalAmount == null) getSearchResults();
 		b.put("page", pageInt);
 		b.put("amount", amountInt);
 		b.put("totalAmount", totalAmount);
 		b.put("emptySearch", emptySearch);
 		
 		return b.build();
 	}
 	
 	protected abstract Query<T> getQueryObject();
 }
