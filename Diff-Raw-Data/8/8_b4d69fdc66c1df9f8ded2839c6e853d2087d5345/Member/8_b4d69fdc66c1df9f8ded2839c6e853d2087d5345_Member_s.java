 
 package band;
 
 import helper.EventNotification;
 import helper.InvalidBandObjectException;
 import helper.Status;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import auth.Authenticatable;
 
 /**
  * Class that contains all the information belonging to a member.
  * 
  * @author OOP Gruppe 187
  */
 public class Member extends Person {
 
 	private final String firstName;
 	private final String lastName;
 	private final String instrument;
 	private final String telephoneNumber;
 	private ArrayList<ProposedDate> events;
 	private ArrayList<Track> repertoire;
 	private final boolean substituteMember;
 	private ArrayList<EventNotification> eventNot;
 	private ArrayList<Rehearsal> rehersals;
 	private ArrayList<Band> bands;
 
 	/**
 	 * @return the repertoire of the member
 	 */
 	public ArrayList<Track> getTracks() {
 
 		return this.repertoire;
 
 	}
 
 	/**
 	 * @param bnd
 	 */
 	public void addBand(Band bnd) {
 
 		if (this.bands.indexOf(bnd) == -1) this.bands.add(bnd);
 	}
 
 	/**
 	 * Adds a Rehearsal the Member attended;
 	 * 
 	 * @param re
 	 *            a Rehearsal
 	 */
 	public void addRehersal(Rehearsal re) {
 
 		this.rehersals.add(re);
 	}
 
 	/**
 	 * @return a list of rehearsals the Member attended
 	 */
 	public ArrayList<Rehearsal> getRehersal() {
 
 		return rehersals;
 
 	}
 
 	/**
 	 * Constructor which requires four arguments
 	 * 
 	 * @param firstName
 	 *            the first name of the member
 	 * @param lastName
 	 *            the last name of the member
 	 * @param instrument
 	 *            the instrument of the member
 	 * @param telephoneNumber
 	 *            the telephone number of the member
 	 */
 	@Deprecated
 	public Member(final String firstName, final String lastName, final String instrument, final String telephoneNumber) {
 
 		this(firstName, lastName, instrument, telephoneNumber, false);
 	}
 
 	/**
 	 * Constructor which requires four arguments
 	 * 
 	 * @param firstName
 	 *            the first name of the member
 	 * @param lastName
 	 *            the last name of the member
 	 * @param instrument
 	 *            the instrument of the member
 	 * @param telephoneNumber
 	 *            the telephone number of the member
 	 */
 	public Member(final String firstName, final String lastName, final String instrument, final String telephoneNumber,
 			final boolean substituteMember) {
 
 		super();

 		this.telephoneNumber = telephoneNumber;
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.instrument = instrument;
 		this.substituteMember = substituteMember;
 		this.eventNot = new ArrayList<EventNotification>();
 		// set the owner to THIS
 		this.setRole(this, Permission.OWNER);
 	}
 
 	/**
 	 * Adds a new proposed date for an event.
 	 * 
 	 * @param pd
 	 *            a time proposal for an event
 	 * @throws InvalidBandObjectException
 	 *             if the date was already proposed to this member
 	 */
 	public void addProposedDate(final ProposedDate pd) throws InvalidBandObjectException {
 
 		if (this.events.indexOf(pd) == -1) {
 			this.events.add(pd);
 		}
 		else throw new InvalidBandObjectException("proposed date aready exists");
 	}
 
 	/**
 	 * adds a new track to the repertoire of the member
 	 * 
 	 * @param tr
 	 *            track to be added
 	 */
 	public void addTrack(final Track tr) {
 
 		if (this.repertoire.indexOf(tr) == -1) {
 			this.repertoire.add(tr);
 		}
 	}
 
 	/**
 	 * This method is used to agree/disagree to a proposed Date.
 	 * 
 	 * @param e
 	 *            event you want to agree or disagree
 	 * @param agreed
 	 *            agree or disagree to the proposed date true - agree false -
 	 *            disagree
 	 */
 	public void agree(final Event e, final Date date, final boolean agreed) {
 
 		final int idx = this.events.indexOf(new ProposedDate(e, date));
 		this.events.get(idx).agree(agreed);
 	}
 
 	/**
 	 * This method is used to agree/disagree to a proposed Date.
 	 * 
 	 * @param e
 	 *            event you want to agree or disagree
 	 * @param reason
 	 *            the reason you have agreed or disagreed
 	 * @param agreed
 	 *            agree or disagree to the proposed date true - agree false -
 	 *            disagree
 	 */
 	public void agree(final Event e, final Date date, final String reason, final boolean agreed) {
 
 		final int idx = this.events.indexOf(new ProposedDate(e, date));
 		this.events.get(idx).agree(agreed, reason);
 	}
 
 	/**
 	 * compares two members
 	 * 
 	 * @return true if the members are equal false otherwise
 	 */
 	@Override
 	public boolean equals(final Object obj) {
 
 		if (this == obj) return true;
 		if (obj == null) return false;
 		if (!(obj instanceof Member)) return false;
 		final Member other = (Member) obj;
 		if (this.firstName == null) {
 			if (other.firstName != null) return false;
 		}
 		else if (!this.firstName.equals(other.firstName)) return false;
 		if (this.instrument == null) {
 			if (other.instrument != null) return false;
 		}
 		else if (!this.instrument.equals(other.instrument)) return false;
 		if (this.lastName == null) {
 			if (other.lastName != null) return false;
 		}
 		else if (!this.lastName.equals(other.lastName)) return false;
 		if (this.telephoneNumber == null) {
 			if (other.telephoneNumber != null) return false;
 		}
 		else if (!this.telephoneNumber.equals(other.telephoneNumber)) return false;
 		return true;
 	}
 
 	/**
 	 * 
 	 */
 	public ArrayList<EventNotification> getNotifications() {
 
 		return this.eventNot;
 	}
 
 	@Override
 	public HashMap<Method, ArrayList<Permission>> getPermissions() {
 
 		return this.permissions;
 	}
 
 	@Override
 	public HashMap<Authenticatable, Permission> getRoles() {
 
 		return this.roles;
 	}
 
 	/**
 	 * @return a hash value representing the member
 	 */
 	@Override
 	public int hashCode() {
 
 		final int prime = 31;
 		int result = 1;
 		result = (prime * result) + ((this.firstName == null) ? 0 : this.firstName.hashCode());
 		result = (prime * result) + ((this.instrument == null) ? 0 : this.instrument.hashCode());
 		result = (prime * result) + ((this.lastName == null) ? 0 : this.lastName.hashCode());
 		result = (prime * result) + ((this.telephoneNumber == null) ? 0 : this.telephoneNumber.hashCode());
 		return result;
 	}
 
 	/**
 	 * initializes the permissions for each method of the class; this method
 	 * should be called in the constructor
 	 */
 	@Override
 	public void initPermissions() {
 
 		this.permissions = new HashMap<Method, ArrayList<Permission>>();
 		this.roles = new HashMap<Authenticatable, Permission>();
 
 		// get all methods of the class; there is NO difference in the
 		// permissions of methods with the same name but different arguments
 		final ArrayList<Method> methods = new ArrayList<Method>();
 		methods.addAll(Arrays.asList(this.getClass().getMethods()));
 
 		final ArrayList<Permission> tPerm = new ArrayList<Permission>();
 		for (final Method m : methods) {
 			if ("isSubstituteMember".equals(m.getName())) {
 				tPerm.add(Permission.OWNER);
 				tPerm.add(Permission.GROUP);
 				tPerm.add(Permission.MANAGEMENT);
 			}
 			else if ("agree".equals(m.getName())) {
 				tPerm.add(Permission.OWNER);
 			}
 			else if ("addProposedDate".equals(m.getName())) {
 				tPerm.add(Permission.OWNER);
 				tPerm.add(Permission.GROUP);
 				tPerm.add(Permission.MANAGEMENT);
 			}
 			else if ("addTrack".equals(m.getName())) {
 				tPerm.add(Permission.OWNER);
 			}
 			else if ("removeTrack".equals(m.getName())) {
 				tPerm.add(Permission.OWNER);
 			}
 
 			// save the permissions and reset the temporary list
 			this.permissions.put(m, new ArrayList<Permission>(tPerm));
 			tPerm.clear();
 		}
 	}
 
 	/**
 	 * @return true if the member is a substitute member false if the member
 	 *         isn't a substitute member
 	 */
 	public boolean isSubstituteMember() {
 
 		return this.substituteMember;
 	}
 
 	/**
 	 * Notify a member about the event @e.
 	 * 
 	 * @param e
 	 */
 	public void notifyEvent(final Event e, final Status stat) {
 
 		this.eventNot.add(new EventNotification(e, stat));
 	}
 
 	/**
 	 * removes a new track from the repertoire of the member
 	 * 
 	 * @param tr
 	 *            track to be removed
 	 * @throws InvalidBandObjectException
 	 */
 	public void removeTrack(final Track tr) throws InvalidBandObjectException {
 
 		if (this.repertoire.indexOf(tr) == -1) {
 			this.repertoire.add(tr);
 		}
 		else throw new InvalidBandObjectException("track already in repatoire");
 	}
 
 	/**
 	 * @return a string representation of an member
 	 */
 	@Override
 	public String toString() {
 
 		final String ret = "First name: " + this.firstName + " Last name: " + this.lastName + " Instrument: "
 				+ this.instrument + " Telephone number: " + this.telephoneNumber;
 		return ret;
 	}
 }
