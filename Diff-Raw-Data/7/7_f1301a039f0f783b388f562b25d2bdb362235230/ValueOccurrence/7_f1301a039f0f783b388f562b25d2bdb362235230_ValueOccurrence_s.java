 package models;
 
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.NotNull;
 
 import play.Logger;
 import play.data.format.Formats;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 import play.i18n.Messages;
 import utils.PerfLogger;
 
 import com.google.gson.annotations.Expose;
 
 /**
  * task entity managed by Ebean
  */
 @Entity
 @Table(uniqueConstraints = @UniqueConstraint(columnNames = { "run_id", "value_id" }))
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
 	public OccurrenceType type = OccurrenceType.Result;
 
 	/** The result of this occurrence (if result type) **/
 	@Required
 	@Expose
 	public Long result;
 
 	/** Is this occurence known as correct (used in average calculation...) **/
 	@Required
 	@Expose
 	public Boolean isCorrect = Boolean.TRUE;
 
 	/** State of this occurence **/
 	@Required
 	@Expose
 	public State state = State.FINISHED;
 
 	/** Status of this occurence **/
 	@Required
 	@Expose
 	public Status status = Status.OK;
 
 	/** The date of this occurrence (the start if it's a duration) **/
 	@Formats.DateTime(pattern = "dd/MM/yyyy hh:mm:ss")
 	@Expose
 	public Date creationDate;
 
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
 	@ManyToOne(fetch = FetchType.EAGER)
 	@Required
 	@NotNull
 	public Value value;
 
 	/**
 	 * finder
 	 */
 	public static Finder<Long, ValueOccurrence> find = new Finder<Long, ValueOccurrence>(Long.class, ValueOccurrence.class);
 
 	/**
 	 * Constructor
 	 * 
 	 * @param run
 	 * @param value
 	 */
 	public ValueOccurrence(Run run, Value value) {
 		this.run = run;
 		this.value = value;
 	}
 
 	public void updateAndSave(Event event) {
 		PerfLogger.log("ValueOccurrence.updateAndSave", 1);
 
 		switch (event.type) {
 		case Start:
 			this.creationDate = event.date;
 			this.type = OccurrenceType.Duration;
 			break;
 
 		case End:
 			this.endDate = event.date;
 			if (this.creationDate == null) {
 				this.creationDate = event.date;
 			}
 			this.result = (this.endDate.getTime() - this.creationDate.getTime());
 			this.type = OccurrenceType.Duration;
 			break;
 
 		default:
 			this.creationDate = event.date;
 			this.result = (long) event.result;
 			this.type = OccurrenceType.Result;
 			break;
 		}
 
 		PerfLogger.log("ValueOccurrence.updateAndSave", 2);
 		this.save();
 		PerfLogger.log("ValueOccurrence.updateAndSave", 3);
 	}
 
 	@Override
 	public void save() {
 
 		PerfLogger.log("ValueOccurrence.save", 1);
 
 		//		Change change = new Change();
 //		save(change);
 //		change.save();
 //	}
 //
 //	public void save(Change change) {
 
 		// before calculating, check if we force the state
 		boolean isChanged = ((_ebean_intercept.getChangedProps() != null) && (_ebean_intercept.getChangedProps().size() != 0));
 		boolean isStateForced = isChanged && _ebean_intercept.getChangedProps().contains("state");
 
 		PerfLogger.log("ValueOccurrence.save", 2);
 
 		// Set state (if not forced to CANCEL)
 		if (!isStateForced) {
 			state = calcState();
 		}
 
 		PerfLogger.log("ValueOccurrence.save", 3);
 		// Set status
 		status = value.calcStatus(result);
 		PerfLogger.log("ValueOccurrence.save", 4);
 		
 		// calc the result
 		if ((type == OccurrenceType.Duration) && (endDate != null)) {
 			result = endDate.getTime() - creationDate.getTime();
 		}
 
 		PerfLogger.log("ValueOccurrence.save", 5);
 		// Check if is is changed
 		boolean isNew = (id == null);
 		isChanged = ((_ebean_intercept.getChangedProps() != null) && (_ebean_intercept.getChangedProps().size() != 0));
 		boolean isChangeResult = (isChanged && _ebean_intercept.getChangedProps().contains("result"));
 		boolean isStateChanged = (isChanged && _ebean_intercept.getChangedProps().contains("state"));
 		boolean isChangeStatus = (isChanged && _ebean_intercept.getChangedProps().contains("status"));
 		boolean isChangeType = (isChanged && _ebean_intercept.getChangedProps().contains("type"));
 		boolean isChangeIsCorrect = (isChanged && _ebean_intercept.getChangedProps().contains("isCorrect"));
 
 		PerfLogger.log("ValueOccurrence.save", 6);
 		super.save();
 		
 		PerfLogger.log("ValueOccurrence.save", 7);
 		// add to change if needed
 //		if (isNew || isChanged) {
 //			change.occurrencesChanged.add(this);
 //		}
 
 		PerfLogger.log("ValueOccurrence.save", 8);
 		// if result changed, recalculate value
 		if (!isStateForced && (isNew || isChangeResult || isChangeType || isChangeIsCorrect)) {
 			ObjectToUpdate.saveNewObjectToUpdate(value);
 
 			//value.type = type;
 			//value.save();
 		}
 
 		PerfLogger.log("ValueOccurrence.save", 9);
 		// if state changed, recalculate parent taskoccurence
 		if (!isStateForced && (isNew || isStateChanged || isChangeStatus)) {
 			TaskOccurrence to = TaskOccurrence.find.where().eq("run", run).eq("task", value.task).findUnique();
 			if (to == null) {
 				to = new TaskOccurrence(run, value.task);
 			}
 			to.save();
 		}
 		PerfLogger.log("ValueOccurrence.save", 10);
 
 	}
 
 	private State calcState() {
 		if (!isRunning()) {
 			return State.FINISHED;
 		} else if (!isCorrect) {
 			return State.CANCELED;
 		} else {
 			return State.RUNNING;
 		}
 	}
 
 	/**
 	 * Is this occurrence (duration) is still running
 	 */
 	private Boolean isRunning() {
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
 	 * Get the String from the result
 	 * 
 	 * @param result
 	 * @return
 	 * @throws ParseException
 	 */
 	public String getResultStr() throws ParseException {
		return format(result, type);
 	}
 
 	/**
 	 * Get the occurrence value (formated)
 	 * 
 	 * @return
 	 */
 	public static String format(Long i, OccurrenceType type) {
 		if (i == null) {
 			i = 0L;
 		}
 		return format(Double.valueOf(i), type);
 	}
 
 	public static String format(Double f, OccurrenceType type) {
 
 		if (f == null) {
 			return null;
 		}
 
 		switch (type) {
 		case Result:
 			if (Math.floor(f) == f) {
 				return ("" + Math.round(f));
 			} else {
 				return (new DecimalFormat("##########.##").format(Math.round(f * 100D) / 100D));
 			}
 
 		default:
 			long s = f.longValue() / 1000;
 			return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
 		}
 	}
 
 	public enum OccurrenceType {
 		Result, Duration
 	};
 
 	public enum MessageType {
 		Start, Result, End
 	};
 
 	// NOTE: play framework was bugging out without this method even though it's
 	// supposed to be automatic
 	public Long getId() {
 		return id;
 	}
 
 }
