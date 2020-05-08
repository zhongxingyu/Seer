 package com.github.podd.impl.file;
 
 import java.util.Collections;
 import java.util.Set;
 
 import org.kohsuke.MetaInfServices;
 import org.openrdf.model.vocabulary.RDF;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddProcessorStage;
 import com.github.podd.api.file.FileReferenceProcessor;
import com.github.podd.api.file.FileReferenceProcessorFactory;
 import com.github.podd.api.file.SSHFileReferenceProcessorFactory;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * An SSH File Reference Processor Factory that creates <code>SSHFileReferenceProcessorImpl</code> instances.
  * 
  * @author kutila
  */
@MetaInfServices(FileReferenceProcessorFactory.class)
 public class SSHFileReferenceProcessorFactoryImpl implements SSHFileReferenceProcessorFactory
 {
 
     protected final Logger log = LoggerFactory.getLogger(this.getClass());
     
     /* The fixed set of stages supported by this Factory */
     private static final Set<PoddProcessorStage> STAGES = Collections.singleton(PoddProcessorStage.RDF_PARSING);
     
     @Override
     public boolean canHandleStage(final PoddProcessorStage stage)
     {
         if(stage == null)
         {
             throw new NullPointerException("Cannot handle NULL stage");
         }
         return SSHFileReferenceProcessorFactoryImpl.STAGES.contains(stage);
     }
     
     @Override
     public String getKey()
     {
         return this.getClass().getName();
     }
     
     @Override
     public FileReferenceProcessor getProcessor()
     {
         return new SSHFileReferenceProcessorImpl();
     }
 
     @Override
     public String getSPARQLConstructBGP()
     {
         return "?subject ?predicate ?object";
     }
 
     @Override
     public String getSPARQLConstructWhere()
     {
         final StringBuilder builder = new StringBuilder();
         
         // match all triples about a subject whose TYPE is poddBase:FileReference
         builder.append(" ?subject <" + RDF.TYPE.stringValue() + "> <"
                 + PoddRdfConstants.PODD_BASE_FILE_REFERENCE_TYPE.stringValue() + "> . ");
         builder.append(" ?subject ?predicate ?object . ");
         
         return builder.toString();
     }
 
     @Override
     public String getSPARQLGroupBy()
     {
         // an empty GROUP BY clause
         return "";
     }
 
     @Override
     public String getSPARQLVariable()
     {
         // to find ALL file references, subject should not be bound 
         return "subject";
     }
 
 
     @Override
     public Set<PoddProcessorStage> getStages()
     {
         return SSHFileReferenceProcessorFactoryImpl.STAGES;
     }
     
 }
