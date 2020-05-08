package $package;
 
 import javax.annotation.Nonnull;
 import javax.persistence.Entity;
 
 import org.hibernate.annotations.Immutable;
 import org.springframework.data.jpa.domain.AbstractPersistable;
 
 @Nonnull
 @Entity
 @Immutable
 public class Bar extends AbstractPersistable<Long> {
 	private static final long serialVersionUID = -3009308007921127448L;
 
 }
