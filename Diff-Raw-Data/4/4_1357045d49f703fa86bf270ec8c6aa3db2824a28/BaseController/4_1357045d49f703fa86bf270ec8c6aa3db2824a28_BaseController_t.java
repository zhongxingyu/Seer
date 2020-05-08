 package org.atlasapi.query.v2;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.application.query.ApplicationConfigurationFetcher;
 import org.atlasapi.beans.AtlasErrorSummary;
 import org.atlasapi.beans.AtlasModelType;
 import org.atlasapi.beans.AtlasModelWriter;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.query.content.parser.ApplicationConfigurationIncludingQueryBuilder;
 import org.atlasapi.query.content.parser.QueryStringBackedQueryBuilder;
 import org.joda.time.DateTime;
 
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public abstract class BaseController {
 
     protected static final Splitter URI_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
     
     protected final ApplicationConfigurationIncludingQueryBuilder builder;
     
     protected final AdapterLog log;
     protected final AtlasModelWriter outputter;
 
     private final ApplicationConfigurationFetcher configFetcher;
     
     protected BaseController(ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter outputter) {
         this.configFetcher = configFetcher;
         this.log = log;
         this.outputter = outputter;
         this.builder = new ApplicationConfigurationIncludingQueryBuilder(new QueryStringBackedQueryBuilder(), configFetcher) ;
     }
     
     protected void errorViewFor(HttpServletRequest request, HttpServletResponse response, AtlasErrorSummary ae) throws IOException {
         log.record(new AdapterLogEntry(ae.id(), Severity.ERROR, new DateTime(DateTimeZones.UTC)).withCause(ae.exception()).withSource(this.getClass()));
         outputter.writeError(request, response, ae);
     }
     
     @SuppressWarnings("unchecked")
     protected void modelAndViewFor(HttpServletRequest request, HttpServletResponse response, Collection<?> queryResults, AtlasModelType type) throws IOException {
         if (queryResults == null) {
             errorViewFor(request, response, AtlasErrorSummary.forException(new Exception("Query result was null")));
        } else {
            outputter.writeTo(request, response, (Collection<Object>) queryResults, type);
         }
     }
     
     protected ApplicationConfiguration appConfig(HttpServletRequest request) {
         Maybe<ApplicationConfiguration> config = configFetcher.configurationFor(request);
         return config.hasValue() ? config.requireValue() : ApplicationConfiguration.DEFAULT_CONFIGURATION;
     }
     
     protected Set<Publisher> publishers(String publisherString, ApplicationConfiguration config) {
         Set<Publisher> appPublishers = ImmutableSet.copyOf(config.publishersInOrder());
         if (Strings.isNullOrEmpty(publisherString)) {
             return appPublishers;
         }
         
         ImmutableSet.Builder<Publisher> publishers = ImmutableSet.builder();
         for (String publisherKey: URI_SPLITTER.split(publisherString)) {
             Maybe<Publisher> publisher = Publisher.fromKey(publisherKey);
             if (publisher.hasValue()) {
                 publishers.add(publisher.requireValue());
             }
         }
         
         return Sets.intersection(publishers.build(), appPublishers);
     }
 }
