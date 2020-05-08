 package com.epam.api.movies.service.impl;
 
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.epam.api.movies.service.ShowtimeService;
 import com.epam.api.movies.service.model.Movie;
 import com.epam.api.movies.service.model.Theater;
 import com.google.common.base.CharMatcher;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
 import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
 
 public class GoogleShowtimeService implements ShowtimeService {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleShowtimeService.class);
 
	private static final Predicate<String> NOT_EMPTY = new Predicate<String>() {
		@Override
		public boolean apply(final String info) {
			return !info.isEmpty();
		}
	};

 	private static final Predicate<String> NOT_DOT = new Predicate<String>() {
 		@Override
 		public boolean apply(final String info) {
			return CharMatcher.isNot(':').matchesAllOf(info);
		}
	};

	private static final Function<String, String> EMPTY_OR_TRIM = new Function<String, String>() {
		@Override
		public String apply(String info) {
			return Strings.nullToEmpty(info).trim();
 		}
 	};
 
 	public GoogleShowtimeService() {
 		super();
 	}
 
 	@Override
 	public Collection<Theater> getShowtimes(String city, String local, int date) throws Exception {
 
 		String url = String.format("http://www.google.com/movies?near=%s&hl=%s&date=%d", URLEncoder.encode(city, "UTF-8"), local, date);
 
 		GoogleShowtimeService.LOGGER.info("Google movie url: {}", url);
 
 		Document doc = Jsoup.connect(url).get();
 		doc.outputSettings().charset("UTF-8");
 
 		return Collections2.transform(doc.getElementsByClass("theater"), new Function<Element, Theater>() {
 			@Override
 			public Theater apply(Element theaterElement) {
 				return GoogleShowtimeService.this.processTheaterElement(theaterElement);
 			}
 		});
 
 	}
 
 	private Theater processTheaterElement(Element element) {
 
 		Elements nameElement = element.select(".theater>.desc>.name>a");
 		Elements infoElement = element.select(".theater>.desc>.info");
 
 		Elements moviesElement = element.select(".theater>.showtimes>*>.movie");
 
 		return new Theater(nameElement.text(), infoElement.text(), this.processMoviesElement(moviesElement));
 	}
 
 	private Collection<Movie> processMoviesElement(Elements moviesElement) {
 
 		return Collections2.transform(moviesElement, new Function<Element, Movie>() {
 
 			@Override
 			public Movie apply(Element movieElement) {
 				String name = GoogleShowtimeService.this.removeSpecialChars(movieElement.select(".name").text());
 
 				Elements links = movieElement.select(".info>a").remove();
 				String info = GoogleShowtimeService.this.clearInfoText(GoogleShowtimeService.this.removeSpecialChars(movieElement.select(".info").text()));
 
 				String rating = movieElement.select(".info>nobr>img[alt]").attr("alt");
 				List<String> times = GoogleShowtimeService.this.processTimesElement(movieElement.select(".times>span"));
 
 				return new Movie(name, info, rating, times);
 			}
 
 		});
 
 	}
 
 	private String clearInfoText(String infoText) {

		Collection<String> transformedList = Collections2.transform(Lists.newArrayList(infoText.split("-")), GoogleShowtimeService.EMPTY_OR_TRIM);
		Collection<String> infoList = Collections2.filter(transformedList, Predicates.and(GoogleShowtimeService.NOT_EMPTY, GoogleShowtimeService.NOT_DOT));

		return StringUtils.join(infoList, " - ");
 	}
 
 	private List<String> processTimesElement(Elements timesElement) {
 
 		return new LinkedList<String>(Collections2.transform(timesElement, new Function<Element, String>() {
 
 			@Override
 			public String apply(Element timeElement) {
 				// Times should be date, there is a possibly solution for
 				// formatting
 				// http://magicmonster.com/kb/prg/java/spring/webmvc/jackson_custom.html
 				return GoogleShowtimeService.this.removeSpecialChars(timeElement.text());
 
 			}
 
 		}));
 
 	}
 
 	private String removeSpecialChars(String text) {
 		return CharMatcher.INVISIBLE.and(CharMatcher.isNot(' ')).removeFrom(text);
 	}
 
 }
