 package models;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Expression;
 import com.avaje.ebean.ExpressionFactory;
 import com.google.gson.annotations.Expose;
 
 /**
  * task entity managed by Ebean
  */
 @Entity
 public class Run extends Model {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6061719644261465560L;
 
 	@Id
 	@Expose
 	public Long id;
 
 	@Constraints.Required
 	@Expose
 	public Date startDate;
 
 	@Expose
 	public Date endDate;
 
 	@Constraints.MaxLength(value = 3000)
 	@Expose
 	public String description;
 
 	/** Task running **/
 	@ManyToMany(cascade = CascadeType.ALL)
 	@Expose
 	public List<Task> tasks = new ArrayList<Task>();
 
 	/** Occurrence during this run **/
 	@OneToMany(mappedBy = "run", cascade = CascadeType.ALL)
 	@Expose
 	public List<ValueOccurrence> occurrences = new ArrayList<ValueOccurrence>();
 
 	@Override
 	public String toString() {
 		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
 
 		String ret = "Run " + id + " : " + df.format(startDate) + "->" + (endDate == null ? "?" : df.format(endDate));
 
 //    ret += " ("; 
 //		for (Task task : tasks) {
 //			ret += task.name + ", ";
 //		}
 //		ret += ") [";
 //		for (ValueOccurrence occu : occurrences) {
 //			ret += occu.value.name + " " + df.format(occu.date) + (occu.endDate == null ? "" : "("+(occu.endDate.getTime()-occu.date.getTime())/1000+")") + ", ";
 //		}
 		return ret;
 	}
 
 	/**
 	 * Get the status of a run
 	 * @param taskId
 	 * @return
 	 */
 	public Status getStatus() {
 		Status ret = Status.NOT_CONCERNED;
 		
 		for (Task task : tasks) {
 			ret = Status.getWorst(ret, getTaskStatus(task.getId()));
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Get the status of a task in this run
 	 * @param taskId
 	 * @return
 	 */
 	public Status getTaskStatus(long taskId) {
 		Status ret = Status.NOT_CONCERNED;
 		
 		for (Task task : tasks) {
 			if (task.getId().longValue() == taskId) {
 				task = Task.find.byId(taskId);
 				ret = Status.OK;
 				for (Value v : task.values) {
 					ret = Status.getWorst(ret, getValueStatus(v.getId()));
 				}
 				for (Task t : task.children) {
 					ret = Status.getWorst(ret, getTaskStatus(t.getId()));
 				}
 				break;
 			}
 		}
 
 		return ret;
 	}
 	
 	/**
 	 * Get the status of a value in this run
 	 * @param valueId
 	 * @return
 	 */
 	public Status getValueStatus(long valueId) {
 		Status ret = Status.NOT_CONCERNED;
 		
 		for (ValueOccurrence	occurrence : occurrences) {
 			if (occurrence.value.getId().longValue() == valueId) {
 				ret = occurrence.getStatus();
 				break;
 			}
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Get the status of a all parent task in this run
 	 * @param taskId
 	 * @return
 	 */
 	public List<Status> getTaskStatusList(long taskId, long rootId) {
 		List<Status> ret = new ArrayList<Status>();
 		
 		for (Task task : tasks) {
 			if (task.getId().longValue() == taskId) {
 				Task parent = task;
 				while ((parent != null) && (parent.getId() != rootId)) {
 					Status s = getTaskStatus(parent.getId());
 					if (s != Status.NOT_CONCERNED) {
 						ret.add(0, s);
 					}
 					parent = parent.parent;
 				}
 				break;
 			}
 		}
 
 		return ret;
 	}
 	
 
 	/**
 	 * Get the state of a run
 	 * @param taskId
 	 * @return
 	 */
 	public State getState() {
 		State ret = State.FINISHED;
 		
 		for (Task task : tasks) {
 			ret = State.getWorst(ret, getTaskState(task.getId()));
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Get the state of a task in this run
 	 * @param taskId
 	 * @return
 	 */
 	public State getTaskState(long taskId) {
 		State ret = State.FINISHED;
 		
 		for (Task task : tasks) {
 			if (task.getId().longValue() == taskId) {
 				for (Value v : task.values) {
 					ret = State.getWorst(ret, getValueState(v.getId()));
 				}
 				for (Task t : task.children) {
 					ret = State.getWorst(ret, getValueState(t.getId()));
 				}
 				break;
 			}
 		}
 
 		return ret;
 	}
 	
 	/**
 	 * Get the state of a value in this run
 	 * @param valueId
 	 * @return
 	 */
 	public State getValueState(long valueId) {
 		State ret = State.FINISHED;
 		
 		for (ValueOccurrence	occurrence : occurrences) {
 			if (occurrence.value.getId().longValue() == valueId) {
 				ret = occurrence.getState();
 				break;
 			}
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Is this value flag as correct or rejected during this run
 	 * @param valueId
 	 * @return
 	 */
 	public Boolean isValueCorrect(long valueId) {
 		
 		for (ValueOccurrence	occurrence : occurrences) {
 			if (occurrence.value.getId().longValue() == valueId) {
 				 return occurrence.isCorrect;
 			}
 		}
 
 		return Boolean.FALSE;
 	}
 	
 	/**
 	 * Toggle the flag "correct" or "rejected" for this run
 	 * @param valueId
 	 * @return
 	 */
 	public void toggleValueCorrect(long valueId) {
 		
 		for (ValueOccurrence	occurrence : occurrences) {
 			if (occurrence.value.getId().longValue() == valueId) {
 				
 				
 				 occurrence.isCorrect = !occurrence.isCorrect;
 				 occurrence.save();
 				 
 				 break;
 			}
 		}
 
 	}
 	
 	/**
 	 * Get the result of a value in this run
 	 * @param taskId
 	 * @return
 	 */
 	public Integer getValueOccurrence(long valueId) {
 		Integer ret = null;
 		
 		for (ValueOccurrence	occurrence : occurrences) {
 			if (occurrence.value.getId().longValue() == valueId) {
 				ret = occurrence.getValue();
 				break;
 			}
 		}
 
 		return ret;
 	}
 	
 	/**
 	 * Get the result of a value in this run (formated)
 	 * @param taskId
 	 * @return
 	 */
 	public String getValueOccurrenceStr(long valueId) {
 		String ret = null;
 		
 		for (ValueOccurrence	occurrence : occurrences) {
 			if (occurrence.value.getId().longValue() == valueId) {
 				ret = occurrence.getValueStr();
 				break;
 			}
 		}
 
 		return ret;
 	}
 	
 	/**
 	 * finder
 	 */
 	public static Finder<Long, Run> find = new Finder<Long, Run>(Long.class, Run.class);
 
 	/**
 	 * Get all the runnings run
 	 * @param date
 	 * @return
 	 */
 	public static List<Run> getRunningRun(Date date) {
 		ExpressionFactory fact = find.getExpressionFactory();
 		Expression expStart = fact.le("startDate", date);
 		Expression expEnd = fact.or(fact.isNull("endDate"), fact.ge("endDate", date));
 		List<Run> runs = find.where().and(expStart, expEnd).findList();
 
		return runs;
 	}
 
 	// NOTE: play framework was bugging out without this method even though it's
 	// supposed to be automatic
 	public Long getId() {
 		return id;
 	}
 
 }
