 package uk.co.brotherlogic.mdb.record;
 
 /**
  * Class to represent a record
  * @author Simon Tucker
  */
 
 import java.io.Serializable;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 import java.util.Map.Entry;
 
 import uk.co.brotherlogic.mdb.artist.Artist;
 import uk.co.brotherlogic.mdb.categories.Category;
 import uk.co.brotherlogic.mdb.format.Format;
 import uk.co.brotherlogic.mdb.groop.Groop;
 import uk.co.brotherlogic.mdb.groop.LineUp;
 import uk.co.brotherlogic.mdb.label.Label;
 
 public class Record implements Comparable<Record>, Serializable
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5625039435654063418L;
 
 	private static final double GROOP_RATIO = 0.8;
 
 	String author;
 
 	private int discogsNum = -1;
 
 	Calendar boughtDate;
 
 	Category category;
 
 	Collection<String> catnos;
 
 	Format format;
 
 	Collection<Label> labels;
 
 	String notes;
 
 	boolean updated = false;
 
 	int number = -1;
 
 	int owner;
 
 	String title;
 
 	Collection<Track> tracks;
 
 	Integer year;
 
 	int releaseMonth;
 
 	int releaseType;
 
 	double price;
 
 	private RecordScore score;
 
 	Collection<Artist> compilers;
 
 	/** The location of the record on it's respective shelf */
 	private int shelfpos;
 
 	/** The state of the record */
 	private int state;
 	private boolean stateUpdated = false;
 
 	public static final int RANKED = 4;
 
 	public Record()
 	{
 		title = "";
 		notes = " ";
 		year = -1;
 		boughtDate = Calendar.getInstance();
 		labels = new LinkedList<Label>();
 		tracks = new LinkedList<Track>();
 		catnos = new LinkedList<String>();
 
 		price = 0.0;
 	}
 
 	public Record(String title, Format format, Calendar boughtDate, int shelfpos)
 	{
 		this();
 		this.title = title;
 		this.format = format;
 		this.boughtDate = boughtDate;
 		this.catnos = catnos;
 		this.labels = labels;
 		this.tracks = tracks;
 		this.shelfpos = shelfpos;
 	}
 
 	public void addCatNo(String catNo)
 	{
 		catnos.add(catNo);
 	}
 
 	public void addLabel(Label label)
 	{
 		labels.add(label);
 	}
 
 	public void addPersonnel(int trackNumber, Collection<Artist> pers)
 	{
 		Track intTrack = getTrack(trackNumber);
 		intTrack.addPersonnel(pers);
 	}
 
 	public void addTrack(Track trk)
 	{
 		updated = true;
 		tracks.add(trk);
 	}
 
 	public void addTracks(int addPoint, int noToAdd)
 	{
 		// Work through the tracks
 		Iterator<Track> tIt = tracks.iterator();
 		Collection<LineUp> groops = new Vector<LineUp>();
 		Collection<Artist> pers = new Vector<Artist>();
 
 		while (tIt.hasNext())
 		{
 			// Get the current track
 			Track currTrack = tIt.next();
 
 			// If the track is beyond the addition point - move it along
 			if (currTrack.getTrackNumber() > addPoint)
 				// Update the trackNumber
 				currTrack.setTrackNumber(currTrack.getTrackNumber() + noToAdd);
 			else if (currTrack.getTrackNumber() == addPoint)
 			{
 				// Collect the information from the previous track
 				groops = currTrack.getLineUps();
 				pers = currTrack.getPersonnel();
 				// currTrack.setTrackNumber(currTrack.getTrackNumber() +
 				// noToAdd);
 			}
 		}
 
 		// Now add the new tracks using the new information collected above
 		for (int i = addPoint + 1; i < addPoint + noToAdd + 1; i++)
 			tracks.add(new Track("", 0, groops, pers, i, -1));
 	}
 
 	public int compareTo(Record o)
 	{
 		return (title.toLowerCase() + number).compareTo(o.getTitle().toLowerCase() + (o.getNumber()));
 	}
 
 	public void createTracks(int noTracks)
 	{
 		for (int i = 0; i < noTracks; i++)
 		{
 			Track t = new Track(i + 1);
 			tracks.add(t);
 		}
 	}
 
 	@Override
 	public boolean equals(Object o)
 	{
 		if (o instanceof Record)
 			return this.compareTo((Record) o) == 0;
 		else
 			return false;
 	}
 
 	public Collection<LineUp> getAllLineUps()
 	{
 		Collection<LineUp> allGroops = new Vector<LineUp>();
 
 		Iterator<Track> tIt = tracks.iterator();
 		while (tIt.hasNext())
 			allGroops.addAll((tIt.next()).getLineUps());
 
 		return allGroops;
 	}
 
 	public String getAuthor()
 	{
 		return author;
 	}
 
 	public Category getCategory()
 	{
 		return category;
 	}
 
 	public Collection<String> getCatNos() throws SQLException
 	{
 		if (catnos == null || catnos.size() == 0)
 			catnos = GetRecords.create().getCatNos(number);
 
 		return catnos;
 	}
 
 	public String getCatNoString() throws SQLException
 	{
 		String ret = "";
 		for (String catNo : getCatNos())
 			ret += catNo;
 
 		return ret;
 	}
 
 	public Collection<Artist> getCompilers() throws SQLException
 	{
 		if (compilers == null)
 			compilers = GetRecords.create().getCompilers(this);
 
 		return compilers;
 	}
 
 	public Calendar getDate()
 	{
 		return boughtDate;
 	}
 
 	public int getDiscogsURI()
 	{
 		return discogsNum;
 	}
 
 	public String getDisplayTitle()
 	{
 		return author + " - " + title;
 	}
 
 	public Format getFormat()
 	{
 		return format;
 	}
 
 	public int getGenre()
 	{
 		return category.getMp3Number();
 	}
 
 	public String getGroopString()
 	{
 		// Construct the groop string
 		Collection<String> main = getMainGroops();
 		Iterator<String> gIt = main.iterator();
 		String groop = "";
 		while (gIt.hasNext())
 			groop += gIt.next() + " & ";
 
 		// Remove the trailing & or replace with various
 		if (groop.length() > 0)
 			groop = groop.substring(0, groop.length() - 3);
 		else
 			groop = "Various";
 
 		return groop;
 
 	}
 
 	public Collection<Label> getLabels() throws SQLException
 	{
 		if (labels == null || labels.size() == 0)
 			labels = GetRecords.create().getLabels(number);
 
 		return labels;
 	}
 
 	public Collection<String> getMainGroops()
 	{
 		// A Map of groopName --> Count
 		Map<String, Integer> mainGroopMap = new TreeMap<String, Integer>();
 		Collection<String> mainGroops = new Vector<String>();
 
 		Iterator<Track> tIt = tracks.iterator();
 		while (tIt.hasNext())
 		{
 			// Increment the count for each groop
 			Collection<LineUp> groops = (tIt.next()).getLineUps();
 			Iterator<LineUp> gIt = groops.iterator();
 			while (gIt.hasNext())
 			{
 				Groop grp = gIt.next().getGroop();
 				String groopName = grp.getSortName();
 
 				Integer intVal;
 				if (mainGroopMap.containsKey(groopName))
 				{
 					intVal = mainGroopMap.get(groopName);
 					intVal = intVal.intValue() + 1;
 				}
 				else
 					intVal = 1;
 
 				mainGroopMap.put(groopName, intVal);
 			}
 		}
 
 		// Select only groops who appear on the right number of tracks
 		for (Entry<String, Integer> ent : mainGroopMap.entrySet())
 			if (((ent.getValue()).doubleValue() / tracks.size()) > GROOP_RATIO)
 				mainGroops.add(ent.getKey());
 
 		return mainGroops;
 
 	}
 
 	public String getNotes()
 	{
 		return notes;
 	}
 
 	public int getNumber()
 	{
 		return number;
 	}
 
 	public int getOwner()
 	{
 		return owner;
 	}
 
 	public double getPrice()
 	{
 		return price;
 	}
 
 	/**
 	 * @return Returns the releaseMonth.
 	 */
 	public int getReleaseMonth()
 	{
 		return releaseMonth;
 	}
 
 	/**
 	 * @return Returns the releaseType.
 	 */
 	public int getReleaseType()
 	{
 		return releaseType;
 	}
 
 	public Integer getReleaseYear()
 	{
 		return year;
 	}
 
 	public RecordScore getScore() throws SQLException
 	{
 		if (score == null)
 			score = RecordScore.get(this);
 		return score;
 	}
 
 	public Integer getShelfPos()
 	{
 		return shelfpos;
 	}
 
 	public String getTitle()
 	{
 		return title;
 	}
 
 	public String getTitleWithCat() throws SQLException
 	{
 		return getTitle() + getCatNoString();
 	}
 
 	public Track getTrack(int trackNumber)
 	{
 		Track ret = new Track();
 
 		// Search all the tracks
 		boolean found = false;
 		Iterator<Track> tIt = tracks.iterator();
 		while (tIt.hasNext() && !found)
 		{
 			Track currTrack = tIt.next();
 			if (currTrack.getTrackNumber() == trackNumber)
 			{
 				ret = currTrack;
 				found = true;
 			}
 		}
 		return ret;
 	}
 
 	public Collection<Track> getTracks() throws SQLException
 	{
 		if (tracks == null || tracks.size() == 0)
 			tracks = GetRecords.create().getTracks(number);
 
 		return tracks;
 	}
 
 	public Collection<String> getTrackTitles()
 	{
 		Collection<String> retSet = new Vector<String>();
 		Iterator<Track> tIt = tracks.iterator();
 		while (tIt.hasNext())
 			retSet.add((tIt.next()).getTitle());
 
 		return retSet;
 	}
 
 	public Integer getYear()
 	{
 		return year;
 	}
 
 	@Override
 	public int hashCode()
 	{
 		return number;
 	}
 
 	public void save() throws SQLException
 	{
		System.err.println("Saving: " + number + " and " + updated);
 		if (number == -1)
 			number = GetRecords.create().addRecord(this);
 		else if (updated)
 		{
 			GetRecords.create().updateRecord(this);
 			updated = false;
 		}
 	}
 
 	/**
 	 * Fixes the author of the record
 	 * 
 	 * @param in
 	 *            The {@link String} to set the author to
 	 */
 	public final void setAuthor(final String in)
 	{
 		author = in;
 		updated = true;
 	}
 
 	public void setCategory(Category cat)
 	{
 		category = cat;
 	}
 
 	public void setCatNo(String cat)
 	{
 		// Remove and add
 		catnos.clear();
 		catnos.add(cat);
 	}
 
 	public void setCatNos(Collection<String> cats)
 	{
 		// Remove and add
 		catnos.clear();
 		catnos.addAll(cats);
 	}
 
 	public void setCompilers(Collection<Artist> compilers)
 	{
 		this.compilers = new LinkedList<Artist>(compilers);
 	}
 
 	public void setDate(Date dat)
 	{
 		boughtDate.setTime(dat);
 		updated = true;
 	}
 
 	public void setDiscogsNum(int dNum)
 	{
 		discogsNum = dNum;
 	}
 
 	public void setFormat(Format form)
 	{
 		// Reset the shelfpos
 		resetShelfPos();
 		format = form;
 	}
 
 	public void setGroops(int trackNumber, Collection<LineUp> lineups)
 	{
 		Track intTrack = getTrack(trackNumber);
 		for (LineUp lineUp : lineups)
 			intTrack.addLineUp(lineUp);
 	}
 
 	public void setLabel(Label lab)
 	{
 		labels.clear();
 		labels.add(lab);
 	}
 
 	public void setLabels(Collection<Label> labs)
 	{
 		// Remove and add
 		labels.clear();
 		labels.addAll(labs);
 	}
 
 	public void setNotes(String in)
 	{
 		notes = in;
 	}
 
 	public void setNumber(int num)
 	{
 		number = num;
 	}
 
 	public void setOwner(int in)
 	{
 		owner = in;
 	}
 
 	public void setPersonnel(int trackNumber, Collection<Artist> pers)
 	{
 		Track intTrack = getTrack(trackNumber);
 		intTrack.addPersonnel(pers);
 	}
 
 	public void setPrice(double price)
 	{
 		this.price = price;
 	}
 
 	/**
 	 * @param releaseMonth
 	 *            The releaseMonth to set.
 	 */
 	public void setReleaseMonth(int releaseMonth)
 	{
 		this.releaseMonth = releaseMonth;
 	}
 
 	/**
 	 * @param releaseType
 	 *            The releaseType to set.
 	 */
 	public void setReleaseType(int releaseType)
 	{
 		this.releaseType = releaseType;
 	}
 
 	public void setState(int value)
 	{
 		state = value;
 		stateUpdated = true;
 	}
 
 	public void setTitle(String tit)
 	{
 		title = tit;
 	}
 
 	public void setTracks(Collection<Track> tracksIn)
 	{
 		tracks.clear();
 		tracks.addAll(tracksIn);
 	}
 
 	public void setTracks(int maxNumber)
 	{
 		// Only include relevant tracks
 		Collection<Track> newTracks = new LinkedList<Track>();
 		Iterator<Track> trIt = tracks.iterator();
 		while (trIt.hasNext())
 		{
 			Track currTrack = trIt.next();
 			if (currTrack.getTrackNumber() <= maxNumber)
 				newTracks.add(currTrack);
 		}
 
 		// Replace the tracks
 		tracks = newTracks;
 
 	}
 
 	public void setYear(int in)
 	{
 		year = in;
 	}
 
 	@Override
 	public String toString()
 	{
 		String ret = "TITLE: " + getTitle() + "\n";
 		try
 		{
 			ret += "LABEL: " + getLabels() + "\n";
 			ret += "FORMAT: " + getFormat() + "\n";
 			ret += "TYPE: " + getReleaseType() + "\n";
 			ret += "CATNO: " + getCatNos() + "\n";
 			DateFormat df = DateFormat.getDateInstance();
 			ret += "DATE: " + df.format(getDate().getTime()) + "\n";
 			ret += "YEAR: " + getReleaseYear() + "\n";
 			ret += "MONTH: " + getReleaseMonth() + "\n";
 			ret += "CATEGORY: " + getCategory() + "\n";
 			ret += "NOTE: " + getNotes() + "\n";
 			ret += "OWNER: " + getOwner() + "\n";
 			ret += "COMPILER: " + getCompilers() + "\n";
 			ret += "PRICE: " + getPrice() + "\n";
 			ret += "AUTHOR: " + getAuthor() + "\n";
 			for (Track tr : tracks)
 			{
 				ret += "TRACK: " + tr.getTrackNumber() + "\n";
 				ret += "ARTIST: " + tr.getLineUps() + "\n";
 				ret += "TITLE: " + tr.getTitle() + "\n";
 				ret += "PERSONNEL: " + tr.getPersonnel() + "\n";
 				ret += "LENGTH: " + tr.getLengthInSeconds() + "\n";
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	private void resetShelfPos()
 	{
 		if (shelfpos > 0)
 			shelfpos = 0;
 	}
 }
