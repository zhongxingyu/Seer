 package org.atlasapi.query.v2;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.atlasapi.application.query.ApplicationConfigurationFetcher;
 import org.atlasapi.beans.AtlasErrorSummary;
 import org.atlasapi.beans.AtlasModelType;
 import org.atlasapi.beans.AtlasModelWriter;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.media.entity.Topic;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.topic.TopicContentLister;
 import org.atlasapi.persistence.topic.TopicQueryResolver;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.http.HttpStatusCode;
 
 @Controller
 public class TopicController extends BaseController {
 
     private final TopicQueryResolver topicResolver;
     private static final AtlasErrorSummary NOT_FOUND = new AtlasErrorSummary(new NullPointerException()).withErrorCode("TOPIC_NOT_FOUND").withStatusCode(HttpStatusCode.NOT_FOUND);
     private static final AtlasErrorSummary FORBIDDEN = new AtlasErrorSummary(new NullPointerException()).withErrorCode("TOPIC_NOT_FOUND").withStatusCode(HttpStatusCode.FORBIDDEN);
     private final TopicContentLister contentLister;
 
     public TopicController(TopicQueryResolver topicResolver, TopicContentLister contentLister, ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter atlasModelOutputter) {
         super(configFetcher, log, atlasModelOutputter);
         this.topicResolver = topicResolver;
         this.contentLister = contentLister;
     }
 
     @RequestMapping(value={"3.0/topics.*","/topics.*"})
     public void topics(HttpServletRequest req, HttpServletResponse resp) throws IOException  {
         try {
             modelAndViewFor(req, resp, ImmutableList.copyOf(topicResolver.topicsFor(builder.build(req))), AtlasModelType.TOPIC);
         } catch (Exception e) {
             errorViewFor(req, resp, AtlasErrorSummary.forException(e));
         }
     }
     
     @RequestMapping(value={"3.0/topics/{id}.*","/topics/{id}.*"})
     public void topic(HttpServletRequest req, HttpServletResponse resp, @PathVariable("id") String id) throws IOException {
         
         ContentQuery query = builder.build(req);
         
         String topicUri = Topic.topicUriForId(id);
         Maybe<Topic> topicForUri = topicResolver.topicForUri(topicUri);
         
         if(topicForUri.isNothing()) {
             outputter.writeError(req, resp, NOT_FOUND.withMessage("Topic " + topicUri + " not found"));
             return;
         }
         
         Topic topic = topicForUri.requireValue();
         
         //TODO: train wreck: query.allowsPublisher(publisher)?;
        if(Sets.intersection(query.getConfiguration().getIncludedPublishers(),topic.getPublishers()).isEmpty()) {
             outputter.writeError(req, resp, FORBIDDEN.withMessage("Topic " + topicUri + " unavailable"));
             return;
         }
         
         
         outputter.writeTo(req, resp, ImmutableSet.<Object>of(topicForUri.requireValue()), AtlasModelType.TOPIC);
     }
     
     @RequestMapping(value={"3.0/topics/{id}/content.*", "/topics/{id}/content"})
     public void topicContents(HttpServletRequest req, HttpServletResponse resp, @PathVariable("id") String id) throws IOException {
         ContentQuery query = builder.build(req);
         
         String topicUri = Topic.topicUriForId(id);
         Maybe<Topic> topicForUri = topicResolver.topicForUri(topicUri);
         
         if(topicForUri.isNothing()) {
             outputter.writeError(req, resp, NOT_FOUND.withMessage("Topic " + topicUri + " not found"));
             return;
         }
         
         Topic topic = topicForUri.requireValue();
         
         //TODO: train wreck: query.allowsPublisher(publisher)?;
        if(Sets.intersection(query.getConfiguration().getIncludedPublishers(),topic.getPublishers()).isEmpty()) {
             outputter.writeError(req, resp, FORBIDDEN.withMessage("Topic " + topicUri + " unavailable"));
             return;
         }
         
         try {
             modelAndViewFor(req, resp, ImmutableList.copyOf(contentLister.contentForTopic(topicUri, query)), AtlasModelType.CONTENT);
         } catch (Exception e) {
             errorViewFor(req, resp, AtlasErrorSummary.forException(e));
         }
     }
      
 }
