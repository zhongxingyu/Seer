 package model;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import model.data.exceptions.RecordNotFoundException;
 import model.exceptions.EmptyFieldException;
 import model.exceptions.FalseIDException;
 
 /**
  * VideoUnit.java
  * 
  * @author Christopher Bertels (chbertel@uos.de)
  * @date 15.09.2008
  * 
  * Klasse für alle VideoUnits (Video-Exemplare).
  */
 public class VideoUnit
 {
 
 	private int uID;
 	private int videoID;
 	private Video video;
 
 	private boolean deleted = false;
 	
 	private static Map<Integer, VideoUnit> videoUnitList;
 	private static Map<Integer, Collection<VideoUnit>> unitToVideoMap;
 	private static int minuID;
 
 	/**
 	 * Öffentlicher Konstruktor für VideoUnits (VideoExemplare).
 	 * 
 	 * @param video Das Video, zu dem das Exemplar gehört.
 	 */
 	public VideoUnit(Video video)
 	{
 		this(minuID, video.getID());
 		minuID++;
 
 		videoUnitList.put(this.uID, this);
 		addToUnitToVideoMap(this);
 	}
 
 	/**
 	 * Privater Konstruktor für VideoUnits.
 	 * 
 	 * @param uID Die UnitID.
 	 * @param videoID Die VideoID des Videos, zu dem das VideoUnit gehört.
 	 */
 	private VideoUnit(int uID, int videoID)
 	{
 		this.uID = uID;
 		this.videoID = videoID;
 
 	}
 
 	/**
 	 * Gibt die ID des VideoUnit zurück.
 	 * 
 	 * @return Die ID des VideoUnit.
 	 */
 	public int getID()
 	{
 		return this.uID;
 	}
 
 	/**
 	 * Gibt zurück, ob das VideoUnit ausgeliehen ist oder nicht.
 	 * 
 	 * @return True, falls ausgeliehen, False sonst.y
 	 */
 	public boolean isRented()
 	{
 		try
 		{
 			InRent ir = InRent.findByVideoUnit(this);
 
 			if (ir != null)
 			{
 				return true;
 			}
 		}
 		catch (RecordNotFoundException ex)
 		{
 			return false;
 		}
 
 		return false;
 	}
 	
 	/**
 	 * Gibt das InRent Objekt zu diesem VideoUnit zurück, falls ausgeliehen.
 	 * @return Das InRent Objekt (falls vorhanden/VideoUnit ausgeliehen) oder null (sonst) zurück.
 	 */
 	public InRent getInRent()
 	{
 		InRent ir;
 		try
 		{
 			ir = InRent.findByVideoUnit(this);
 		}
 		catch (RecordNotFoundException e)
 		{
 			return null;
 		}
 		
 		return ir;
 	}
 
 	/**
 	 * Gibt die VideoID des Videos des VideoUnits zurück.
 	 * 
 	 * @return Die VideoID des zugehörigen Videos.
 	 */
 	public int getVideoID()
 	{
 		return this.videoID;
 	}
 
 	/**
 	 * Gibt das zugehörige Video zurück.
 	 * 
 	 * @return Das Video des VideoUnits.
 	 */
 	public Video getVideo()
 	{
		return Video.findByID(this.videoID);
 //		return this.video;
 	}
 	
 	/**
 	 * Entfernt VideoUnit aus globaler VideoUnit-Liste.
 	 * Wird beim nächsten Speichern nicht mehr mitgespeichert und geht somit verloren.
 	 */
 	public void delete()
 	{
 		videoUnitList.remove(this.getID());
 		this.deleted = true;
 	}
 	
 	/**
 	 * Gibt an, ob das Objekt gelöscht wurde (via delete())
 	 * @return True, falls gelöscht, False sonst.
 	 */
 	public boolean isDeleted()
 	{
 		return this.deleted;
 	}
 	
 	/**
 	 * Gibt eine String-Repräsentation des VideoUnits zurück.
 	 */
 	public String toString()
 	{
 		return this.uID + " - VideoUnit";
 	}
 	
 	/**
 	 * Gibt das zur übergebenen VideoUnitID zugehörige VideoUnit zurück, falls
 	 * vorhanden.
 	 * 
 	 * @param videoUnitID Die ID des zu suchenden VideoUnit.
 	 * @return Das VideoUnit Objekt, das zur angegebenen ID gehört.
 	 * @throws RecordNotFoundException Wird geworfen, falls kein solches
 	 *             VideoUnit Objekt existiert.
 	 */
 	public static VideoUnit findByID(int videoUnitID)
 			throws RecordNotFoundException
 	{
 		if (videoUnitList.containsKey(videoUnitID))
 		{
 			return videoUnitList.get(videoUnitID);
 		}
 		else
 		{
 			throw new RecordNotFoundException("VideoExemplar",
 					"ExemplarNummer", videoUnitID);
 		}
 	}
 
 	/**
 	 * Gibt die Menge aller VideoUnits in der Datenbasis zurück.
 	 * 
 	 * @return Die Menge aller VideoUnits.
 	 */
 	public static Collection<VideoUnit> findAll()
 	{
 		return videoUnitList.values();
 	}
 
 	/**
 	 * Gibt die Menge der VideoUnits zurück, deren Rented-Wert dem übergebenen
 	 * entspricht.
 	 * 
 	 * @param isRented Die Angabe, ob nach VideoUnits gesucht werden soll, die
 	 *            entweder ausgeliehen (true) oder nicht (false) sind.
 	 * @return Die Menge der VideoUnits, die dem Kriterium entsprechen.
 	 */
 	public static Collection<VideoUnit> findByRented(boolean isRented)
 	{
 		Collection<VideoUnit> foundVideoUnits = new LinkedList<VideoUnit>();
 		for (VideoUnit unit : videoUnitList.values())
 		{
 			if (unit.isRented() == isRented)
 			{
 				foundVideoUnits.add(unit);
 			}
 		}
 		return foundVideoUnits;
 	}
 
 	/**
 	 * Gibt die Menge der VideoUnits zurück, die zu einem angegebenen Video
 	 * geören.
 	 * 
 	 * @param video Das Video, deren VideoUnits gesucht werden.
 	 * @return Die Menge der VideoUnits, die zu dem Video gehören.
 	 */
 	public static Collection<VideoUnit> findByVideo(Video video)
 	{
 		if (unitToVideoMap.containsKey(video.getID()))
 		{
 			return unitToVideoMap.get(video.getID());
 		}
 		else
 		{
 			// falls keine VideoUnits vorhanden, leere Liste zurückgeben
 			return new LinkedList<VideoUnit>();
 		}
 	}
 
 	/**
 	 * Setzt die MinID für VideoUnits.
 	 * 
 	 * @param newMinuID Die neue MinID für VideoUnits.
 	 * @throws FalseIDException 
 	 */
 	public static void setMinID(int newMinuID) throws FalseIDException
 	{
 		if (newMinuID > 0)
 		{
 			minuID = newMinuID;
 		}
 		else
 		{
 			throw new FalseIDException("Übergebene MinID für VideoUnit ist kleiner 0!!!");
 		}
 	}
 
 	public static VideoUnit reCreate(int uID, int videoID)
 	{
 		return new VideoUnit(uID, videoID);
 	}
 
 	/**
 	 * Setzt die VideoUnit Liste, sodass sie global verfügbar wird.
 	 * 
 	 * @param newVideoUnitList Die Liste der geladenen VideoUnits.
 	 * @throws EmptyFieldException wird geworfen, wenn Paramter null
 	 */
 	public static void setVideoUnitList(Map<Integer, VideoUnit> newVideoUnitList)
 			throws EmptyFieldException
 	{
 		if (newVideoUnitList != null)
 		{
 			videoUnitList = newVideoUnitList;
 
 			unitToVideoMap = new HashMap<Integer, Collection<VideoUnit>>();
 
 			for (VideoUnit unit : newVideoUnitList.values())
 			{
 				addToUnitToVideoMap(unit);
 			}
 		}
 		else
 			throw new EmptyFieldException("VideoUnitList ist null!!");
 	}
 
 	/**
 	 * Gibt die aktuelle MinID für VideoUnits zurück.
 	 * 
 	 * @return Die aktuelle MinID für VideoUnits.
 	 */
 	public static int getMinID()
 	{
 		return minuID;
 	}
 
 	/**
 	 * Fügt ein VideoUnit zur unitToVideoMap hinzu.
 	 * 
 	 * @param unit Die VideoUnit, die hinzugefügt werden soll.
 	 */
 	private static void addToUnitToVideoMap(VideoUnit unit)
 	{
 		if (unitToVideoMap == null)
 			unitToVideoMap = new HashMap<Integer, Collection<VideoUnit>>();
 		if (unitToVideoMap.containsKey(unit.videoID))
 		{
 			unitToVideoMap.get(unit.videoID).add(unit);
 		}
 		else
 		{
 			LinkedList<VideoUnit> newList = new LinkedList<VideoUnit>();
 			newList.add(unit);
 			unitToVideoMap.put(unit.videoID, newList);
 		}
 	}
 }
