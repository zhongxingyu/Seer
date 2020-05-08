 package uk.org.lidalia.lang;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.base.Supplier;
 
import javax.annotation.Nullable;
 import java.util.Set;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public class RichOptional<T> {
 
     public static <T> RichOptional<T> absent() {
         return new RichOptional<T>(Optional.<T>absent());
     }
 
     public static <T> RichOptional<T> of(T reference) {
         return new RichOptional<T>(Optional.of(reference));
     }
 
    public static <T> RichOptional<T> fromNullable(@Nullable T nullableReference) {
         return new RichOptional<T>(Optional.fromNullable(nullableReference));
     }
 
     public static <T> RichOptional<T> from(Optional<T> optional) {
         return new RichOptional<T>(optional);
     }
 
     private final Optional<T> decorated;
 
     private RichOptional(Optional<T> decorated) {
         this.decorated = checkNotNull(decorated);
     }
 
     public boolean isPresent() {
         return decorated.isPresent();
     }
 
     public boolean isAbsent() {
         return !isPresent();
     }
 
     public T get() {
         return decorated.get();
     }
 
     public T or(T defaultValue) {
         return decorated.or(defaultValue);
     }
 
     public RichOptional<T> or(Optional<? extends T> secondChoice) {
         return new RichOptional<T>(decorated.or(secondChoice));
     }
 
     public RichOptional<T> or(RichOptional<? extends T> secondChoice) {
         return new RichOptional<T>(decorated.or(secondChoice.decorated));
     }
 
     public T or(Supplier<? extends T> supplier) {
         return decorated.or(supplier);
     }
 
    @Nullable
     public T orNull() {
         return decorated.orNull();
     }
 
     public Set<T> asSet() {
         return decorated.asSet();
     }
 
     public <R> RichOptional<R> map(Function<T, R> mapper) {
         if (isPresent()) {
             return of(mapper.apply(get()));
         } else {
             return absent();
         }
     }
 
     public Optional<T> toOptional() {
         return decorated;
     }
 
     @Override
     public String toString() {
         if (isPresent()) {
             return get().toString();
         } else {
             return "absent";
         }
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof RichOptional)) return false;
         RichOptional<?> that = (RichOptional<?>) o;
         return decorated.equals(that.decorated);
     }
 
     @Override
     public int hashCode() {
         return decorated.hashCode();
     }
 }
