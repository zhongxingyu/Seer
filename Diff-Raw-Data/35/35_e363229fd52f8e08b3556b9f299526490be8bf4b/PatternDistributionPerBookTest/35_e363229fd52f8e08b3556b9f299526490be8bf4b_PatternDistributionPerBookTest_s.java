 package com.hidden.data.web.service.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.hidden.data.nosql.model.discovery.BookDiscovery;
 import com.hidden.data.nosql.model.discovery.Line;
 
 public class PatternDistributionPerBookTest {
 
 	private static final String BOOK_TITLE = "I, robot";
 	private static final int BOOK_LINES = 100;
 	private PatternDistributionPerBook victim;
 	private List<Integer> patternsPerPercentageThreshold;
 	private List<BookDiscovery> bookDiscoveries = new ArrayList<BookDiscovery>();
 
 	@Test
 	public void getPatternsPerPercentageThreshold_1In9_Returns1Within10() {
 		givenEmptyPatternDistributionsPerBook();
 		givenNumberOfPatternsInDistribution(1, 9);
 		whenSetPatternThreshold();
 		whenGetPatternsPerPercentageThreshold();
 		thenShouldPatternsPerPercentageShouldBe(1);
 	}
 
 	private void givenEmptyPatternDistributionsPerBook() {
 		victim = new PatternDistributionPerBook(BOOK_LINES);
 	}
 
 	private void givenNumberOfPatternsInDistribution(int patterns, int location) {
 		Line line = new Line(location, "");
 		BookDiscovery bookDiscovery = new BookDiscovery(BOOK_TITLE,
 				StringUtils.EMPTY, Collections.singletonList(line), null,
 				BOOK_LINES);
 		bookDiscoveries.addAll(Collections.nCopies(patterns, bookDiscovery));
 	}
 
 	private void whenSetPatternThreshold() {
 		victim.setPatternThreshold(bookDiscoveries);
 	}
 
 	private void whenGetPatternsPerPercentageThreshold() {
 		patternsPerPercentageThreshold = victim
 				.getPatternsPerPercentageThreshold();
 	}
 
 	private void thenShouldPatternsPerPercentageShouldBe(int... expectedValues) {
 		for (int i = 0; i < expectedValues.length; i++) {
 			Assert.assertEquals(expectedValues[i],
 					patternsPerPercentageThreshold.get(i).intValue());
 
 		}
 	}
 
 	@Test
 	public void getPatternsPerPercentageThreshold_1In10_Returns1Within10() {
 		givenEmptyPatternDistributionsPerBook();
 		givenNumberOfPatternsInDistribution(1, 10);
 		whenSetPatternThreshold();
 		whenGetPatternsPerPercentageThreshold();
 		thenShouldPatternsPerPercentageShouldBe(1);
 	}
 
 	@Test
 	public void getPatternsPerPercentageThreshold_10In9_Returns10Within10() {
 		givenEmptyPatternDistributionsPerBook();
 		givenNumberOfPatternsInDistribution(10, 9);
 		whenSetPatternThreshold();
 		whenGetPatternsPerPercentageThreshold();
 		thenShouldPatternsPerPercentageShouldBe(10);
 	}
 
 	@Test
 	public void getPatternsPerPercentageThreshold_1In20_Returns0Within10And1Within20() {
 		givenEmptyPatternDistributionsPerBook();
 		givenNumberOfPatternsInDistribution(1, 20);
 		whenSetPatternThreshold();
 		whenGetPatternsPerPercentageThreshold();
 		thenShouldPatternsPerPercentageShouldBe(0, 1);
 	}
 
 	@Test
 	public void getPatternsPerPercentageThreshold_3In6And2In14_Returns3Within10And2Within20() {
 		givenEmptyPatternDistributionsPerBook();
 		givenNumberOfPatternsInDistribution(3, 6);
 		givenNumberOfPatternsInDistribution(2, 14);
 		whenSetPatternThreshold();
 		whenGetPatternsPerPercentageThreshold();
 		thenShouldPatternsPerPercentageShouldBe(3, 2);
 	}
 
 	@Test
 	public void getPatternsPerPercentageThreshold_3In6And2In7_Returns5Within10() {
 		givenEmptyPatternDistributionsPerBook();
 		givenNumberOfPatternsInDistribution(3, 6);
 		givenNumberOfPatternsInDistribution(2, 7);
 		whenSetPatternThreshold();
 		whenGetPatternsPerPercentageThreshold();
 		thenShouldPatternsPerPercentageShouldBe(5);
 	}
 
 }
