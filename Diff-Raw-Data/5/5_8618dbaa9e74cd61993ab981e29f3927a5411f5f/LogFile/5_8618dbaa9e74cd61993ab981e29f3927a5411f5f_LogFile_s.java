 package models;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 import play.db.jpa.Model;
 import play.modules.search.Field;
 import play.modules.search.Indexed;
 
 /**
  * The class LogFile stores every change a registered user makes on an entry in
  * the database. Logged in users can then access the log and see what changes
  * have been done.
  */
 @Indexed
 @Entity
 public class LogFile extends Model {
 
 	@Field
 	@ManyToOne
 	@JoinColumn(referencedColumnName = "Id")
 	public Act act;
 
 	@Field
 	@ManyToOne
 	@JoinColumn(referencedColumnName = "Id")
 	public Revision rev;
 
 	@Field
 	@ManyToOne
 	@JoinColumn(referencedColumnName = "Id")
 	public Term term;
 
 	@Field
 	@ManyToOne
 	@JoinColumn(referencedColumnName = "Id")
 	public Competence competence;
 
 	@Field
 	public String original;
 
 	@Field
 	public String newObject;
 
 	@Field
 	public String author;
 
 	@Field
 	public Date changeDate;
 
 	public LogFile(Act act, Revision r, Competence competence, Term term,
 			String original, String newObject, String author, Date timestamp) {
 
 		try {
 			this.act = act;
 			this.rev = r;
 			this.competence = competence;
 			this.term = term;
 		} catch (NullPointerException e) {
 		}
 		this.original = original;
 		this.newObject = newObject;
 		this.author = author;
 		this.changeDate = timestamp;
 	}
 
 	/**
 	 * Creates a log file entry for a change on an act.
 	 * 
 	 * @param a
 	 *            - the act that is changed.
 	 * @param old
 	 *            - the old string representation (containing all information)
 	 *            of the act.
 	 * @param newObject
 	 *            - the new string representation of the act.
 	 * @param author
 	 *            - the author who contributed the change.
 	 * @param timestamp
 	 *            - time and date when the change was stored.
 	 */
 	public static void logActChange(Act a, String old, String newObject,
 			String author, Date timestamp) {
 		new LogFile(a, null, null, null, old, newObject, author, timestamp)
 				.save();
 	}
 
 	/**
 	 * Creates a log file entry for a change on a term.
 	 * 
 	 * @param a
 	 *            - the act the term belongs to.
 	 * @param t
 	 *            - the term that's changed.
 	 * @param old
 	 *            - the old string representation (containing all information)
 	 *            of the term.
 	 * @param newObject
 	 *            - the new string representation of the act.
 	 * @param author
 	 *            - the author who contributed the change.
 	 * @param timestamp
 	 *            - time and date when the change was stored.
 	 */
 	public static void logTermChange(Act a, Term t, String old,
 			String newObject, String author, Date timestamp) {
 		new LogFile(a, null, null, t, old, newObject, author, timestamp).save();
 	}
 
 	/**
 	 * Creates a log file entry for a change on a competence.
 	 * 
 	 * @param c
 	 *            - the competence that is changed.
 	 * @param old
 	 *            - the old string representation (containing all information)
 	 *            of the competence.
 	 * @param newObject
 	 *            - the new string representation of the competence.
 	 * @param author
 	 *            - the author who contributed the change.
 	 * @param timestamp
 	 *            - time and date when the change was stored.
 	 */
 	public static void logCompetenceChange(Competence c, Term t, Revision r,
 			Act a, String old, String newObject, String author, Date timestamp) {
 		new LogFile(a, r, c, t, old, newObject, author, timestamp).save();
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		if (this.competence != null)
 			sb.append("Kompetenz: " + this.competence + "aus ");
 		if (this.term != null)
 			sb.append("Artikel: " + this.term + " aus ");
 		if (this.act != null)
 			sb.append("Gesetz: " + this.act.id);
 		if (this.rev != null)
 			sb.append("(" + rev + ")");
 		sb.append("| \n");
 		sb.append(this.original + ">>>>>\n");
 		sb.append(this.newObject);
 		return sb.toString();
 	}
 }
