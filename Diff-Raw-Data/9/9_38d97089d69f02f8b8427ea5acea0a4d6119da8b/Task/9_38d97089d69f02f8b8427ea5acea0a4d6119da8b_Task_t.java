 package pl.kaczanowski.model;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 /**
  * Represents task (module) that will be execute on processor. Task have information about time of execution (ticks).
  * 
  * @author pawel
  * 
  */
 public class Task {
 
 	private final int ticks;
 
 	private final int id;
 
 	/**
 	 * @param id
 	 *            - task id
 	 * @param ticks
 	 *            - number of ticks to execute task (time of task execution)
 	 */
 	public Task(final int id, final int ticks) {
 		checkArgument(ticks > 0, "ticks must be positive value");
 		this.ticks = ticks;
 		this.id = id;
 	}
 
 	/**
 	 * 
 	 * @return task id
 	 */
 	public int getId() {
 		return id;
 	}
 
	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + id;
 		return result;
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(obj instanceof Task)) {
 			return false;
 		}
 		Task other = (Task) obj;
 		if (id != other.id) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Task [ticks=");
 		builder.append(ticks);
 		builder.append(", id=");
 		builder.append(id);
 		builder.append("]");
 		return builder.toString();
 	}
 
 	/**
 	 * 
 	 * @return number of ticks
 	 */
 	public int getTicks() {
 		return ticks;
 	}
 
 }
