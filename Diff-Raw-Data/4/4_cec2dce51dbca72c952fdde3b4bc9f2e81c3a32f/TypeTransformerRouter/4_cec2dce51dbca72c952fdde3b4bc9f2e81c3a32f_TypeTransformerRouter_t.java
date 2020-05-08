 /*
  * Copyright (c) 2010 by Guido Steinacker
  */
 
 package de.steinacker.jcg.transform.type;
 
 import de.steinacker.jcg.transform.rule.TypeTransformerSelector;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 
 import java.util.List;
 
 /**
 * Routes TypeMessages to one of several possible TypeTransformers.
 * The TypeTransformer is selected by a {@link TypeTransformerSelector}
  *
  * @author Guido Steinacker
  * @version %version: 28 %
  */
 public final class TypeTransformerRouter implements TypeTransformer {
     private final static Logger LOG = Logger.getLogger(TypeTransformerRouter.class);
 
     private String name;
     private TypeTransformerSelector selector;
     private TypeTransformerProvider provider;
 
     /**
      * @param selector the Rule used to select a transformer from the provider.
      */
     @Required
     public void setSelector(final TypeTransformerSelector selector) {
         this.selector = selector;
     }
 
     /**
      * @param provider the possible provider.
      */
     @Required
     public void setTransformerProvider(final TypeTransformerProvider provider) {
         this.provider = provider;
     }
 
     @Required
     public void setName(final String name) {
         this.name = name;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     /**
      * Transforms the ModelMessage into another ModelMessage,
      *
      * @param message the source ModelMessage
      * @return the target ModelMessage
      * @throws de.steinacker.jcg.exception.NoTransformerException
      *          If no defaultTransformer is setParameters or no
      *          transform matches to the rule.
      */
     @Override
     public TypeMessage transform(final TypeMessage message) {
         TypeMessage tempMessage = message;
         final List<String> keys = this.selector.apply(message);
         if (keys.isEmpty()) {
             LOG.warn("No transformers returned from selector " + selector);
         }
         for (final String key : keys) {
             final TypeTransformer typeTransformer = provider.getTransformer(key);
             LOG.info("Selecting " + typeTransformer);
             tempMessage = typeTransformer.transform(tempMessage);
         }
         return tempMessage;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("TypeTransformerRouter");
         sb.append("{name='").append(getName()).append('\'');
         sb.append(", selector=").append(selector);
         sb.append('}');
         return sb.toString();
     }
 }
