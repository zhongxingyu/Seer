 package models;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.validation.constraints.NotNull;
 
 import org.apache.commons.lang.time.DurationFormatUtils;
 
 import play.data.format.Formats;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 import play.i18n.Messages;
 
 import com.google.gson.annotations.Expose;
 
 /**
  * task entity managed by Ebean
  */
 @Entity
 public class ValueOccurrence extends Model {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -889123393559267773L;
 
 	@Id
 	@Expose
 	public Long id;
 
 	/** The type of this occurrence (result or duration) **/
 	@Required
 	@Expose
 	public OccurrenceType type;
 
 	/** The result of this occurrence (if result type) **/
 	@Required
 	@Expose
 	public Integer result;
 
 	/** Is this occurence known as correct (used in average calculation...) **/
 	@Required
 	@Expose
 	public Boolean isCorrect = Boolean.TRUE;
 
 	/** The date of this occurrence (the start if it's a duration) **/
 	@Formats.DateTime(pattern = "dd/MM/yyyy hh:mm:ss")
 	@Expose
 	public Date date;
 
 	/** The end of this occurrence (if it's a duration) **/
 	@Formats.DateTime(pattern = "dd/MM/yyyy hh:mm:ss")
 	@Expose
 	public Date endDate;
 
 	/** The value corresponding to this occurrence **/
 	@ManyToOne
 	@Required
 	@NotNull
 	public Run run;
 
 	/** The value corresponding to this occurrence **/
 	@ManyToOne
 	@Required
 	@NotNull
 	public Value value;
 
 	/**
 	 * finder
 	 */
 	public static Finder<Long, ValueOccurrence> find = new Finder<Long, ValueOccurrence>(Long.class, ValueOccurrence.class);
 
 	
 	@Override
 	public void save() {
 		ValueOccurrence occurence = this;
 		Set<Run> changedRun = new HashSet<Run>();
 		Set<Task> changedTask = new HashSet<Task>();
 		
 		// Get run concerned
 		Set<Run> runs = new HashSet<Run>();
 		Value value = occurence.value;
 		for (ValueOccurrence o1 : value.occurrences) {
 			runs.add(o1.run);
 		}
 		// Status and State
 		Map<Run, Status> runStatus = new HashMap<Run, Status>();
 		Map<Run, State> runState = new HashMap<Run, State>();
 		Map<Run, Map<Task, Status>> runTaskStatus = new HashMap<Run, Map<Task, Status>>();
 		Map<Run, Map<Task, State>> runTaskState = new HashMap<Run, Map<Task, State>>();
 		
 		
 		// Calculate status before the save
 		for (Run run : runs) {
 			runStatus.put(run, run.getStatus());
 			runState.put(run, run.getState());
 			Task parent = occurence.value.task;
 			while (parent != null) {
 				if (!runTaskStatus.containsKey(run)) {
 					runTaskStatus.put(run, new HashMap<Task, Status>());
 					runTaskState.put(run, new HashMap<Task, State>());
 				}
 				runTaskStatus.get(run).put(parent, run.getTaskStatus(parent.getId()));
 				runTaskState.get(run).put(parent, run.getTaskState(parent.getId()));
 				parent = parent.parent;
 			}
 		}
 		
 		//save and reload
 		super.save();
 		occurence = ValueOccurrence.find.byId(this.id);
 		
 		
 		// Get run concerned
 		runs = new HashSet<Run>();
 		value = occurence.value;
 		for (ValueOccurrence o1 : value.occurrences) {
 			runs.add(o1.run);
 		}
 		// Calculate changes
 		for (Run run : runs) {
 			Task parent = occurence.value.task;
 			if (runStatus.get(run) != run.getStatus()) {
 				changedRun.add(run);
 			}
 			if (runState.get(run) != run.getState()) {
 				changedRun.add(run);
 			}
 			while (parent != null) {
 				if ((runTaskStatus.get(run) == null) || (runTaskStatus.get(run).get(parent) != run.getTaskStatus(parent.getId()))) {
 					changedTask.add(parent);
 				}
 				if ((runTaskState.get(run) == null) || (runTaskState.get(run).get(parent) != run.getTaskState(parent.getId()))) {
 					changedTask.add(parent);
 				}
 				parent = parent.parent;
 			}
 		}
 
 		Change.createChange(this, null, changedRun, changedTask);
 	}
 	
 	/**
 	 * Update or create a value Occurrence
 	 * 
 	 * @param value
 	 *          the Value
 	 * @param date
 	 *          the Date of the hit
 	 * @param type
 	 *          the Message type (Start, End or Value)
 	 * @param result
 	 *          the value
 	 * @param theRun
 	 * @return
 	 */
 	public static ValueOccurrence updateOrCreateValueOccurrence(Value value, Date date, MessageType type, Integer result, Run theRun) {
 
 		ValueOccurrence occurrence = null;
 		switch (type) {
 		case Result:
 			// Check if already exit
 			occurrence = find.where().eq("type", OccurrenceType.Result).eq("value", value).eq("date", date).findUnique();
 			// else create
 			if (occurrence == null) {
 				occurrence = new ValueOccurrence();
 			}
 			occurrence.value = value;
 			occurrence.type = OccurrenceType.Result;
 			occurrence.date = date;
 			occurrence.result = result;
 			occurrence.run = theRun;
 			occurrence.save();
 
 			if (theRun.startDate.after(date)) {
 				theRun.startDate = date;
 				theRun.save();
 			}
 
 			break;
 
 		case Start:
 			// Check if already exit for this run
 			occurrence = find.where().eq("type", OccurrenceType.Duration).eq("value", value).eq("run.id", theRun.getId()).findUnique();
 			// else create
 			if (occurrence == null) {
 				occurrence = new ValueOccurrence();
 				occurrence.value = value;
 				occurrence.type = OccurrenceType.Duration;
 				occurrence.date = date;
 				occurrence.run = theRun;
 				occurrence.save();
 
 				if (theRun.startDate.after(date)) {
 					theRun.startDate = date;
 					theRun.save();
 				}
 			}
 
 			break;
 
 		default:
 		case End:
 			// Check if already exit
 			occurrence = find.where().eq("type", OccurrenceType.Duration).eq("value", value).eq("run.id", theRun.getId()).eq("endDate", date).findUnique();
 
 			if (occurrence == null) {
 
 				// else find the corresponding duration in the run
 				occurrence = find.where().eq("type", OccurrenceType.Duration).eq("value", value).eq("run.id", theRun.getId()).findUnique();
 				
 				if (occurrence == null) {
 					// Not found, create it
 					occurrence = new ValueOccurrence();
 					occurrence.value = value;
 					occurrence.type = OccurrenceType.Duration;
 					occurrence.date = date;
 				}
 				
 				occurrence.endDate = date;
 				occurrence.run = theRun;
 				occurrence.result = (int) (occurrence.endDate.getTime() - occurrence.date.getTime());
 				occurrence.save();
 				
 				theRun = Run.find.byId(theRun.id);
 				if (theRun.startDate.after(date)) {
 					theRun.startDate = date;
 				}
 				// Check if all duration are finished (and get the last ended)
 				Date theEnd = null;
 				for (ValueOccurrence occ : theRun.occurrences) {
 					if (occ.type == OccurrenceType.Duration) {
 						if (occ.endDate == null) {
 							theEnd = null;
 							break;
 						} else if ((theEnd == null) || (theEnd.before(occ.endDate))) {
 							theEnd = occ.endDate;
 						}
 					}
 				}
 				theRun.endDate = theEnd;
 				theRun.save();
 			}
 
 			break;
 		}
 
 		return occurrence;
 
 	}
 
 	/**
 	 * Get the occurrence status
 	 * 
 	 * @return
 	 */
 	public Status getStatus() {
 		Status ret = value.getStatus(getValue());
 
 		return ret;
 	}
 
 	/**
 	 * Get the occurrence state
 	 * 
 	 * @return
 	 */
 	public State getState() {
 		if (!isRunning()) {
 			return State.FINISHED;
 		} else if (!isCorrect) {
 			return State.CANCELED;
 		} else {
 			return State.RUNNING;
 		}
 	}
 
 	/**
 	 * Get the occurrence value
 	 * 
 	 * @return
 	 */
 	public Integer getValue() {
 		Integer ret = result;
 		if (isRunning()) {
 			ret = (int) (System.currentTimeMillis() - date.getTime());
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Get the occurrence value (formated)
 	 * 
 	 * @return
 	 */
 	public String getValueStr() {
 		return format(getValue());
 	}
 
 	/**
 	 * Get the occurrence value (formated)
 	 * 
 	 * @return
 	 */
 	public String format(Integer i) {
 		if (i == null) {
 			i = 0;
 		}
 		return format(Float.valueOf(i));
 	}
 
 	public String format(Float f) {
 
 		if (f == null) {
 			return null;
 		}
 
 		switch (type) {
 		case Result:
 			if (Math.floor(f) == f) {
 				return ("" + Math.round(f));
 			} else {
 				return ("" + f);
 			}
 
 		default:
 			return (DurationFormatUtils.formatDuration(f.longValue(), "HH:mm:ss"));
 		}
 	}
 
 	/**
 	 * Is this occurrence (duration) is still running
 	 */
 	public Boolean isRunning() {
 		return ((type == OccurrenceType.Duration) && (endDate == null));
 	}
 
 	/**
 	 * Get the String from the date
 	 * 
 	 * @param dateS
 	 * @return
 	 * @throws ParseException
 	 */
 	public static String getDateS(Date date) throws ParseException {
 		SimpleDateFormat df = new SimpleDateFormat(Messages.get("date.format"));
 		return df.format(date);
 	}
 
 	/**
 	 * Get the date from the String
 	 * 
 	 * @param dateS
 	 * @return
 	 * @throws ParseException
 	 */
 	public static Date getDate(String dateS) throws ParseException {
		System.out.println(dateS+" "+Messages.get("date.format"));
 		SimpleDateFormat df1 = new SimpleDateFormat(Messages.get("date.format"));
 		SimpleDateFormat df2 = new SimpleDateFormat("yy/MM/dd hh:mm:ss");
 
 		// Read the date
 		Date date = null;
 
 		if (dateS.length() == 19) {
 				date = df1.parse(dateS);
 			} else {
 				date = df2.parse(dateS);
 		}
 			
	  System.out.println(dateS+ " "+df1.format(date));
		

 		return date;
 	}
 
 	public enum OccurrenceType {
 		Result, Duration
 	};
 
 	public enum MessageType {
 		Result, Start, End
 	};
 
 }
