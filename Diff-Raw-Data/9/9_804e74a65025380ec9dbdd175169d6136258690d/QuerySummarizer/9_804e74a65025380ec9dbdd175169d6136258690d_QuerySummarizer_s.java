 package net.shrine.webclient.client.util;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.shrine.webclient.client.domain.Expression;
 import net.shrine.webclient.client.domain.Or;
 import net.shrine.webclient.client.domain.QueryGroup;
 import net.shrine.webclient.client.domain.Term;
 
 import com.allen_sauer.gwt.log.client.Log;
 
 /**
  * 
  * @author clint
  * @date May 3, 2012
  */
 public final class QuerySummarizer {
 
 	private QuerySummarizer() {
 		super();
 	}
 
 	public static String summarize(final Iterable<QueryGroup> queryGroups) {
 		Log.info("Summarizing queries: " + queryGroups);
 		
 		Util.requireNotNull(queryGroups);
 		Util.require(queryGroups.iterator().hasNext());
 
 		final StringBuilder result = new StringBuilder();
 
 		result.append("Find the number of patients per medical center ");
 
 		final List<String> querySummaries = Util.makeArrayList();
 
 		{
 			int i = 0;
 		
 			for (final QueryGroup query : queryGroups) {
 				final String queryGroupCssClass = "querySummaryLabel" + (i % Util.numRowColors);
 				
 				querySummaries.add(summarize(query, queryGroupCssClass));
 				
 				++i;
 			}
 		}
 
 		result.append(Util.join("<span class=\"joiner\">&nbsp;AND&nbsp;</span>", querySummaries));
 
 		return result.toString();
 	}
 
 	static final String summarize(final QueryGroup queryGroup, final String queryGroupCssClass) {
 		final StringBuilder result = new StringBuilder();
 		
 		final Expression expr = queryGroup.getExpression();
 		
 		Util.require(expr instanceof Term || expr instanceof Or);
 		
 		if(expr instanceof Term) {
 			final Term term = (Term)expr;
 			
 			final String category = Labels.forCategory.get(term.getCategory());
 			
 			Util.requireNotNull(category);
 			
 			result.append(category).append(" ").append(color(term.getSimpleName(), queryGroupCssClass));
 		} else if(expr instanceof Or) {
 			final Or or = (Or)expr;
 			
 			final List<Term> terms = Util.toList(or.getTerms());
 			
 			requireAllSameCategory(terms);
 			
 			final String category = Labels.forCategory.get(terms.get(0).getCategory());
 			
 			Util.requireNotNull(category);
 			
 			final String categoryName = Labels.singularCategories.get(category);
 			
 			Util.requireNotNull(categoryName);
 			
 			result.append(category).append(" at least one of the " + categoryName + " concepts in group " + color(queryGroup.getId().name, queryGroupCssClass));
 		}
 
 		return result.toString();
 	}
 	
 	static String color(final String text, final String queryGroupCssClass) {
		return "<span class=\"" + queryGroupCssClass + " querySummaryLabel\">" + text + "</span>";
 	}
 	
 	static void requireAllSameCategory(final Collection<Term> terms) {
 		if(terms.size() == 0) {
 			return;
 		}
 		
 		if(terms.size() == 1) {
 			return;
 		}
 		
 		final String firstCategory = terms.iterator().next().getCategory();
 		
 		for(final Term term : terms) {
 			Util.require(firstCategory.equals(term.getCategory()));
 		}
 	}
 	
 	public static final class Labels {
 		private Labels() {
 			super();
 		}
 
 		@SuppressWarnings("serial")
 		static final Map<String, String> forCategory = new HashMap<String, String>() {{
 			this.put("Demographics", "who were");
 			this.put("Diagnoses", "diagnosed with");
 			this.put("medications", "prescribed or administered");
 			this.put("Labs", "tested for levels of");
 		}};
 		
 		@SuppressWarnings("serial")
 		static final Map<String, String> singularCategories = new HashMap<String, String>() {{
 			this.put("Demographics", "demographic");
 			this.put("Diagnoses", "diagnosis");
 			this.put("medications", "medication");
 			this.put("Labs", "lab test");
 		}};
 
 		static final String defaultCategoryLabel = "with a record of";
 	}
 }
