 package model;
 
 import com.google.common.base.Objects;
 
 public class ObjectCounter<T> implements Comparable<ObjectCounter<T>> {
 	private final T card;
 	private Integer count;
 
 	public ObjectCounter(T card) {
 		this.card = card;
 		this.count = 1;
 	}
 
 	@Override
 	public int compareTo(ObjectCounter<T> other) {
 		return this.count.compareTo(other.getCount());
 	}
 
 	@Override
	public int hashCode() {
		return Objects.hashCode(count, card);
	}

	@Override
 	public boolean equals(Object obj) {
 		if (obj != null && this.getClass().equals(obj.getClass())) {
 			final ObjectCounter<?> other = (ObjectCounter<?>) obj;
 			return Objects.equal(count, other.count) && Objects.equal(card, other.card);
 		}
 
 		return false;
 	}
 
 	public T getCard() {
 		return card;
 	}
 
 	public Integer getCount() {
 		return count;
 	}
 
 	public void increment() {
 		count++;
 	}
 }
