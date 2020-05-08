 package org.modica.parser.lazy;
 
 import java.nio.channels.FileChannel;
 
 import org.modica.afp.modca.Context;
 import org.modica.afp.modca.Context.MODCAContext;
 import org.modica.afp.modca.ContextImpl;
 import org.modica.afp.modca.StructuredFieldFactory;
 import org.modica.afp.modca.StructuredFieldFactoryImpl;
 import org.modica.afp.modca.structuredfields.StructuredField;
 import org.modica.afp.modca.structuredfields.StructuredFieldIntroducer;
 
 public class LazyStructuredFieldFactory implements StructuredFieldFactory {
 
     private final StructuredFieldFactory delegate;
 
     private final ContextStack contextStack;
 
     private Context previousContext;
 
     public LazyStructuredFieldFactory(FileChannel fileChannel) {
         this.contextStack = new ContextStack(new ContextImpl(), null);
         previousContext = contextStack;
         this.delegate = new StructuredFieldFactoryImpl(fileChannel, contextStack);
     }
 
     public Context getPreviousContext() {
         return previousContext;
     }
 
     private void beforeCreation(StructuredFieldIntroducer introducer) {
         previousContext = contextStack;
         pushContext();
     }
 
     private void pushContext() {
         Context next = new ContextImpl();
         next.put(MODCAContext.GCSGID, null);
         contextStack.push(next);
     }
 
     @Override
     public StructuredField createBegin(StructuredFieldIntroducer introducer) {
         beforeCreation(introducer);
         return delegate.createBegin(introducer);
     }
 
     @Override
     public StructuredField createMap(StructuredFieldIntroducer introducer) {
         beforeCreation(introducer);
         return delegate.createMap(introducer);
     }
 
     @Override
     public StructuredField createDescriptor(StructuredFieldIntroducer introducer) {
         beforeCreation(introducer);
         return delegate.createDescriptor(introducer);
     }
 
     @Override
     public StructuredField createMigration(StructuredFieldIntroducer introducer) {
         beforeCreation(introducer);
         return delegate.createMigration(introducer);
     }
 
     @Override
     public StructuredField createEnd(StructuredFieldIntroducer introducer) {
         beforeCreation(introducer);
         return delegate.createEnd(introducer);
     }
 
     @Override
     public StructuredField createData(StructuredFieldIntroducer introducer) {
         beforeCreation(introducer);
         return delegate.createData(introducer);
     }
 
     @Override
     public StructuredField createPosition(StructuredFieldIntroducer introducer) {
         beforeCreation(introducer);
         return delegate.createPosition(introducer);
     }
 
     @Override
     public StructuredField createInclude(StructuredFieldIntroducer introducer) {
         beforeCreation(introducer);
         return delegate.createInclude(introducer);
     }
 
     @Override
     public StructuredField createControl(StructuredFieldIntroducer introducer) {
         beforeCreation(introducer);
         return delegate.createControl(introducer);
     }
 
     @Override
     public StructuredField createIndex(StructuredFieldIntroducer introducer) {
         beforeCreation(introducer);
         return delegate.createIndex(introducer);
     }
 
     private static class ContextStack implements Context {
 
         private final Context current;
 
         private final ContextStack previous;
 
         public ContextStack(Context first, ContextStack previous) {
             this.current = first;
             this.previous = previous;
         }
 
         public ContextStack push(Context context) {
             return new ContextStack(context, this);
         }
 
         @Override
         public Object get(FOCAContext focaContext) {
             Object ob = current.get(focaContext);
             if (ob == null && previous != null) {
                 ob = previous.get(focaContext);
                 if (ob != null) {
                     current.put(focaContext, ob);
                 }
             }
             return ob;
         }
 
         @Override
         public Object get(MODCAContext modcaContext) {
             Object ob = current.get(modcaContext);
            if (ob == null && previous != null) {
                 ob = previous.get(modcaContext);
                if (ob != null) {
                     current.put(modcaContext, ob);
                 }
             }
             return ob;
         }
 
         @Override
         public void put(FOCAContext focaContext, Object obj) {
             current.put(focaContext, obj);
         }
 
         @Override
         public void put(MODCAContext modcaContext, Object obj) {
             current.put(modcaContext, obj);
         }
     }
 
 }
