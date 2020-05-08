 /*
  * Copyright (c) 2010 by Guido Steinacker
  */
 
 package de.steinacker.jcg.transform.type;
 
 import de.steinacker.jcg.Context;
 import de.steinacker.jcg.model.Field;
 import de.steinacker.jcg.model.FieldModifier;
 import de.steinacker.jcg.model.Method;
 import org.apache.log4j.Logger;
 
 /**
  * Abstract implementation of a ModelTransformer used to add Methods for every Field of a Type.
  *
  * @author Guido Steinacker
  * @version %version: 28 %
  */
 public abstract class AbstractFieldToMethodTransformer extends AbstractTypeTransformer implements TypeTransformer {
 
     private final static Logger LOG = Logger.getLogger(AbstractFieldToMethodTransformer.class);
 
     @Override
     public final void visit(final Field field, final Context context) {
        getTypeBuilder(context).addMethod(transformFieldToMethod(field, context));
     }
 
     protected abstract Method transformFieldToMethod(final Field field, final Context context);
 }
