 package org.javabits.yar.guice;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.util.concurrent.Futures.addCallback;
 import static com.google.common.util.concurrent.Futures.getUnchecked;
 
 import java.lang.InterruptedException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicReference;
 import javax.annotation.Nullable;
 
 import org.javabits.yar.*;
 import com.google.common.util.concurrent.FutureCallback;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.SettableFuture;
 
 class BlockingSupplierImpl<T> implements BlockingSupplier<T>, SupplierListener {
     private final AtomicReference<SettableFuture<Supplier<T>>> supplierRef;
     private final Id<T> id;
     @SuppressWarnings("unused")
     private Registration<T> selfRegistration;
 
     BlockingSupplierImpl(Id<T> id, Supplier<T> supplier) {
         this.id = checkNotNull(id, "id");
         SettableFuture<Supplier<T>> settableFuture = SettableFuture.create();
         if (supplier != null) {
             settableFuture.set(supplier);
         }
         this.supplierRef = new AtomicReference<>(settableFuture);
     }
 
 
     @Override
     public Id<T> id() {
         return id;
     }
 
     @Nullable
     @Override
     public T get() {
         Future<Supplier<T>> future = supplierRef.get();
         // Do not block on Future.get() here. Just check if the future is done.
         if (future.isDone())
             return future.isCancelled() ? null : getUnchecked(future).get();
         return null;
     }
 
     @Override
     public T getSync() throws InterruptedException {
         try {
             return getAsync().get();
         } catch (ExecutionException e) {
             // getAsync() future can't fail (by design). If it did then there is something
             // horribly wrong with this code.
             throw new AssertionError(e);
         }
     }
 
     @Nullable
     @Override
     public T getSync(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
         try {
             return getAsync().get(timeout, unit);
         } catch (ExecutionException e) {
             // getAsync() future can't fail (by design). If it did then there is something
             // horribly wrong with this code.
             throw new AssertionError(e);
         }
     }
 
     @Override
     public ListenableFuture<T> getAsync() {
         final SettableFuture<T> future = SettableFuture.create();
 
         // The future callback will be executed either on the current thread (if the future is
         // already completed) or on the registry's action handler thread.
         addCallback(supplierRef.get(), new FutureCallback<Supplier<T>>() {
             @Override
             public void onSuccess(Supplier<T> supplier) {
                 future.set(supplier.get());
             }
 
             @Override
             public void onFailure(Throwable t) {
             }
         });
 
         return future;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void supplierChanged(SupplierEvent supplierEvent) {
         SupplierEvent.Type type = supplierEvent.type();
         Supplier<T> supplier = (Supplier<T>) supplierEvent.supplier();
         switch (type) {
         case ADD:
             supplierRef.get().set(supplier);
             break;
         case REMOVE:
             supplierRef.set(SettableFuture.<Supplier<T>> create());
             break;
         default:
             throw new IllegalStateException("Unknown supplier event: " + supplierEvent);
         }
     }
     //preserve a strong reference on the registration listener registration
     void setSelfRegistration(Registration<T> selfRegistration) {
         this.selfRegistration = selfRegistration;
     }
 
     @Override
     public String toString() {
        SettableFuture<Supplier<T>> delegateSupplier = supplierRef.get();
         return "BlockingSupplierImpl{" +
                "id=" + id +
                ", delegate=" + (delegateSupplier != null? delegateSupplier.getClass().getName():"null") +
                 '}';
     }
 }
