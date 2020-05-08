 /**
  * 
  */
 package com.github.podd.impl.purl;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddProcessorStage;
 import com.github.podd.api.purl.PoddPurlProcessor;
 import com.github.podd.api.purl.PoddPurlProcessorFactory;
 
 /**
  * A Simple Processor Factory that creates <code>SimpleUUIDPurlProcessor</code> instances.
  * 
  * 
  * @author kutila
  * 
  */
 public class SimpleUUIDPurlProcessorFactory implements PoddPurlProcessorFactory
 {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
     private String prefix;
     
     private String[] temporaryUriArray;
     
     /* The fixed set of stages supported by this Factory */
     private static final Set<PoddProcessorStage> stages = new HashSet<PoddProcessorStage>(
             Arrays.asList(new PoddProcessorStage[] { PoddProcessorStage.RDF_PARSING }));
     
     @Override
     public String getSPARQLConstructBGP()
     {
         return "?subject ?predicate ?object";
     }
     
     @Override
     public String getSPARQLConstructWhere()
     {
         final StringBuilder builder = new StringBuilder();
        
         // match all triples
         builder.append(" ?subject ?predicate ?object ");
         
         // get a focused SPARQL by checking for the temporary URI patterns
         if(this.temporaryUriArray != null && this.temporaryUriArray.length > 0)
         {
             builder.append("FILTER ( ");
            final int startLength = builder.length();
             
            for(final String tempUri : this.temporaryUriArray)
             {
                if(builder.length() > startLength)
                 {
                     builder.append(" || ");
                 }
                builder.append(" strstarts(STR(?subject), ");
                builder.append("\"");
                 builder.append(tempUri);
                builder.append("\")");
                 builder.append(" || ");
                builder.append(" strstarts(STR(?object), ");
                builder.append("\"");
                 builder.append(tempUri);
                builder.append("\")");
             }
             builder.append(" ) ");
             
         }
         return builder.toString();
     }
     
     @Override
     public String getSPARQLConstructWhere(final URI subject)
     {
         // TODO
         if(subject == null)
         {
             return "?s ?p ?o";
         }
         final StringBuilder builder = new StringBuilder();
         builder.append("<");
         builder.append(subject.stringValue());
         builder.append("> ?p ?o");
         return builder.toString();
     }
     
     @Override
     public String getSPARQLGroupBy()
     {
         // an empty GROUP BY clause
         return "";
     }
     
     @Override
     public boolean canHandleStage(final PoddProcessorStage stage)
     {
         if(stage == null)
         {
             throw new NullPointerException("Cannot handle NULL stage");
         }
         return SimpleUUIDPurlProcessorFactory.stages.contains(stage);
     }
     
     @Override
     public String getKey()
     {
         return this.getClass().getName();
     }
     
     @Override
     public PoddPurlProcessor getProcessor()
     {
         SimpleUUIDPurlProcessor processor = new SimpleUUIDPurlProcessor();
         if(this.prefix != null)
         {
             processor = new SimpleUUIDPurlProcessor(this.prefix);
         }
         
         if(this.temporaryUriArray == null || this.temporaryUriArray.length == 0)
         {
             // NOTE: Could throw a custom exception, possibly extending a PoddRuntimeException ?
             throw new RuntimeException("Not enough data to create SimplePoddPurlProcessor (temporary URIs)");
         }
         for(final String tempUri : this.temporaryUriArray)
         {
             processor.addTemporaryUriHandler(tempUri);
         }
         
         return processor;
     }
     
     @Override
     public Set<PoddProcessorStage> getStages()
     {
         return SimpleUUIDPurlProcessorFactory.stages;
     }
     
     /**
      * 
      * @param prefix
      *            The Prefix used by the PurlProcessor's created by this factory
      */
     public void setPrefix(final String prefix)
     {
         this.prefix = prefix;
     }
     
     /**
      * 
      * @param temporaryUri
      *            The temporary URIs that the PurlProcessor created by this factory can handle.
      */
     public void setTemporaryUriArray(final String[] temporaryUri)
     {
         this.temporaryUriArray = temporaryUri;
     }
     
 }
