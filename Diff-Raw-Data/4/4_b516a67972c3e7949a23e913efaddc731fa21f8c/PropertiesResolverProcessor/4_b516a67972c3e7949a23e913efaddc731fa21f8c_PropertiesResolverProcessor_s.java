 package com.google.code.qualitas.internal.installation.resolution;
 
 import org.apache.camel.Exchange;
 import org.springframework.stereotype.Component;
 
 import com.google.code.qualitas.engines.api.core.Bundle;
import com.google.code.qualitas.engines.api.resolver.Properties;
import com.google.code.qualitas.engines.api.resolver.PropertiesResolver;
 import com.google.code.qualitas.internal.installation.core.AbstractProcessor;
 
 /**
  * The Class PropertiesResolverProcessor.
  */
 @Component
 public class PropertiesResolverProcessor extends AbstractProcessor {
 
     /* (non-Javadoc)
      * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
      */
     @Override
     public void process(Exchange exchange) throws Exception {
 
         Bundle bundle = exchange.getIn().getBody(Bundle.class);
 
         PropertiesResolver resolver = findQualitasComponent(PropertiesResolver.class,
                 bundle.getProcessType());
 
         Properties properties = resolver.resolve(bundle);
         
     }
 
 }
