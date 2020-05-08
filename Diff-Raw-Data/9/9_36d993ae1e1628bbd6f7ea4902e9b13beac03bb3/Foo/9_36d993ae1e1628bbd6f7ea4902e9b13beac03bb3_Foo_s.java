package $package;
 
 import javax.annotation.Nonnull;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 import org.springframework.data.jpa.domain.AbstractPersistable;
 
 @Nonnull
 @Entity
 public class Foo extends AbstractPersistable<Long> {
 	private static final long serialVersionUID = -5858125293405585370L;
 
 	@ManyToOne(cascade = { CascadeType.PERSIST }, optional = false)
 	private Bar bar;
 
 	public Bar getBar() {
 		return bar;
 	}
 
 	public void setBar(Bar bar) {
 		this.bar = bar;
 	}
 
 	@Override
 	public String toString() {
 		return "Foo [bar=" + bar + ", toString()=" + super.toString() + "]";
 	}
 }
