 package de.hypoport.twitterwall;
 
 import com.google.common.base.Optional;
 import de.hypoport.twitterwall.mapper.ResultMapper;
 import de.hypoport.twitterwall.model.SearchResult;
 import de.hypoport.twitterwall.twitter.TweetSearchService;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import twitter4j.Query;
 import twitter4j.QueryResult;
 import twitter4j.TwitterException;
 
 import javax.inject.Inject;
 
 import static com.google.common.base.Optional.fromNullable;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 
 @Controller
 public class TweetSearchController {
 
   @Inject
   TweetSearchService searchService;
 
   @Inject
   ResultMapper resultMapper;
 
  @RequestMapping(value = "/search", method = GET, produces = "application/json;charset=utf-8")
   @ResponseBody
   public SearchResult search(@RequestParam(required = true, value = "q") String search,
                              @RequestParam(required = false, value = "since") String since,
                              @RequestParam(required = false, value = "since_id") Long sinceId) throws TwitterException {
     Query query = createQuery(search, fromNullable(since), fromNullable(sinceId));
 
     QueryResult queryResult = searchService.searchTweets(query);
     SearchResult searchResult = resultMapper.map(queryResult);
     return searchResult;
   }
 
   private Query createQuery(String searchText, Optional<String> since, Optional<Long> sinceId) {
     Query query = new Query(searchText);
     if (since.isPresent()) query.setSince(since.get());
     if (sinceId.isPresent()) query.setSinceId(sinceId.get());
     return query;
   }
 
   @ExceptionHandler(Exception.class)
   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
   String handleException(Exception e) {
     return e.getMessage();
   }
 }
