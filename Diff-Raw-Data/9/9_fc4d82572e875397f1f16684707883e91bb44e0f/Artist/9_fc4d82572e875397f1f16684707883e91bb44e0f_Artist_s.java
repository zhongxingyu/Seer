 package uk.co.brotherlogic.mdb.artist;
 
 import java.io.Serializable;
 import java.sql.SQLException;
 import java.util.Collection;
 
 import uk.co.brotherlogic.mdb.Utils;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
 
 /**
  * Application level artist object
  * 
  * @author Simon Tucker
  */
 public class Artist implements Comparable<Artist>, Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3058167016487618030L;
 
 	/** The id number of the artist */
 	private int id;
 
	/** The sort name */
	private final String sortName;

 	/** The show name */
 	private final String showName;
 
 	/**
 	 * Empty constructor
 	 */
 	public Artist(final String sort) {
 		sortName = sort;
 		showName = Utils.flipString(sort);
 		id = -1;
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param sort
 	 *            The sort name
 	 * @param show
 	 *            The show name
 	 * @param idNumber
 	 *            The id number
 	 */
 	public Artist(final String sort, final String show, final int idNumber) {
 		this.sortName = sort;
 		this.id = idNumber;
 		this.showName = show;
 	}
 
 	public final int compareTo(final Artist o) {
 		return -sortName.toLowerCase().compareTo(o.sortName.toLowerCase());
 	}
 
 	/**
 	 * String method for representing the object in a list
 	 * 
 	 * @return A {@link String} of how the object should be represented in a
 	 *         list
 	 */
 	public final String displayInList() {
 		return sortName;
 	}
 
 	@Override
 	public final boolean equals(final Object o) {
 		if (o instanceof Artist) {
 			if (((Artist) o).getId() > 0 && id > 0)
 				return ((Artist) o).getId() == id;
 			else
 				return this.compareTo((Artist) o) == 0;
 		} else
 			return false;
 	}
 
 	public Collection<Record> getGroopMemberRecords() throws SQLException {
 		return GetRecords.create().getRecordsWithGrpMember(id);
 	}
 
 	/**
 	 * Get the id number for this artist
 	 * 
 	 * @return The ID number
 	 */
 	public final int getId() {
 		return id;
 	}
 
 	public Collection<Record> getRecords() throws SQLException {
 		return GetRecords.create().getRecordsWithPers(id);
 	}
 
 	/**
 	 * Get the show name
 	 * 
 	 * @return {@link String} of the Show name
 	 */
 	public final String getShowName() {
 		return showName;
 	}
 
 	/**
 	 * Get the sorting name
 	 * 
 	 * @return {@link String} of the name used for sorting
 	 */
 	public final String getSortName() {
 		return sortName;
 	}
 
 	@Override
 	public final int hashCode() {
 		return sortName.hashCode();
 	}
 
 	/**
 	 * Saves this artist
 	 * 
 	 * @throws SQLException
 	 *             if the DB connection fails
 	 */
 	public final void save() throws SQLException {
 		if (id == -1)
 			id = GetArtists.create().saveArtist(this);
 	}
 
 	@Override
 	public final String toString() {
 		return sortName;
 	}
 
 }
