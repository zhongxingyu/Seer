 /*
  * Copyright (c) 2010 by Guido Steinacker
  */
 
 package de.steinacker.jcg.transform.type;
 
 import de.steinacker.jcg.model.Type;
 import de.steinacker.jcg.model.TypeBuilder;
 import org.apache.log4j.Logger;
 
 import static de.steinacker.jcg.model.TypeModifier.*;
 
 /**
 * A ModelTransformer which makes a type 'public final'.
  * <p/>
 * This Transformer optionally supports predicates to decide, whether a type
 * will be transformed or not.
  *
  * @author Guido Steinacker
  * @version %version: 28 %
  */
 public final class PublicFinalClass implements TypeTransformer {
 
     private final static Logger LOG = Logger.getLogger(PublicFinalClass.class);
 
     @Override
     public String getName() {
         return "PublicFinalClass";
     }
 
     @Override
     public TypeMessage transform(final TypeMessage message) {
         final Type type = message.getPayload();
         if (type.is(ABSTRACT) || (type.is(FINAL) && type.is(PUBLIC))) {
             return message;
         } else {
             final Type finalizedType = new TypeBuilder(type)
                     .addModifier(FINAL)
                     .addModifier(PUBLIC)
                     .toType();
             return new TypeMessage(finalizedType, message.getContext());
         }
     }
 
     @Override
     public String toString() {
         return getName();
     }
 
 }
