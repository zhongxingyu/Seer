 package com.typeiisoft.lct.utils;
 
 import com.typeiisoft.lct.features.LunarFeature;
 
 import com.mhuss.AstroLib.Astro;
 import com.mhuss.AstroLib.AstroDate;
 import com.mhuss.AstroLib.DateOps;
 import com.mhuss.AstroLib.LocationElements;
 import com.mhuss.AstroLib.Lunar;
 import com.mhuss.AstroLib.LunarCalc;
 import com.mhuss.AstroLib.NoInitException;
 import com.mhuss.AstroLib.ObsInfo;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import android.annotation.SuppressLint;
 import android.util.Log;
 
 /**
  * This class handles calling calculations and returning various bits of 
  * information about the moon. It will also handle the logic for determining 
  * if a lunar feature is visible.
  * 
  * @author Michael Reuter
  */
 public class MoonInfo {
 	/** Logging identifier. */
 	private static final String TAG = MoonInfo.class.getName();
 	/** The current date and time in UTC for all observation information. */
 	private AstroDate obsDate;
 	/** The current date and time in the local timezone. */
 	private GregorianCalendar obsLocal;
 	/** Object that does most of the calculations. */
 	private Lunar lunar;
 	/** Object that holds the observing site information. */
 	private ObsInfo obsInfo;
 	/** Holder for the Time Zone offset. */
 	private int tzOffset;
 	/** Holder for the selenographic colongitude. */
 	private double colongitude;
 	/** Holder for the libration in latitude */
 	private double liblatitude;
 	/** Holder for the libration in longitude */
 	private double liblongitude;
 	/** Enum containing the lunar phases for integer comparison. */
 	private enum Phase {
 		NM, WAXING_CRESENT, FQ, WAXING_GIBBOUS, FM, WANING_GIBBOUS, TQ,
 		WANING_CRESENT;
 	}
 	/** Array of lunar phase names. */
 	private String[] phaseNames = {"New Moon", "Waxing Cresent", 
 			"First Quarter", "Waxing Gibbous", "Full Moon", "Waning Gibbous",
 			"Third Quarter", "Waning Cresent"};
 	/** Array of lunar phase values. */
 	private int[] phaseValues = {Lunar.NEW, Lunar.Q1, Lunar.FULL, Lunar.Q3};
 	/** Array of phase boundaries based on illuminated fraction. */
 	private double[] phaseBounds = {0.005, 0.49, 0.51, 0.99};
 	/** Enum containing the time of lunar day for integer comparison. */
 	private enum TimeOfDay {
 		MORNING, EVENING;
 	}
 	/** Selenographic longitude where relief makes it hard to see feature. */
 	private static final double FEATURE_CUTOFF = 15D;
 	/** Lunar features to which no selenographic longitude cutoff is applied. */
 	private String[] noCutoffType = {"Mare", "Oceanus"};
 	/** Latitude and/or longitude region where librations makes big effect. */
 	private static final double LIBRATION_ZONE = 80D;
 	/** Theoretical visibility limit for features. */
 	private static final double LUNAR_EDGE = 90D;
 	
 	/**
 	 * This function is the class constructor.
 	 */
 	public MoonInfo() {
 		Calendar now = Calendar.getInstance();
 		this.obsLocal = (GregorianCalendar)now.clone();
 		this.tzOffset = now.getTimeZone().getOffset(now.getTimeInMillis()) / Astro.MILLISECONDS_PER_HOUR;
 		now.add(Calendar.HOUR_OF_DAY, this.tzOffset);
 		this.obsDate = new AstroDate(now.get(Calendar.DATE), 
 				now.get(Calendar.MONTH)+1, now.get(Calendar.YEAR), 
 				now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), 
 				now.get(Calendar.SECOND));
 		this.initialize();
 	}
 	
 	/**
 	 * This function is the class constructor with parameters.
 	 * @param datetime : Array of seven values of the current date and time.
 	 */
 	public MoonInfo(int[] datetime) {
 		this.obsDate = new AstroDate(datetime[0], datetime[1], datetime[2],
 				datetime[3], datetime[4], datetime[5]);
 		this.obsLocal = this.obsDate.toGCalendar();
 		this.tzOffset = datetime[6];
 		this.obsLocal.add(Calendar.HOUR_OF_DAY, datetime[6]);
 		this.initialize();
 	}
 	
 	/**
 	 * This function consolidates some of the common setup.
 	 */
 	private void initialize() {
 		this.lunar = new Lunar(this.getJulianCenturies());
 		this.obsInfo = new ObsInfo();
 		this.colongitude = Double.MAX_VALUE;
 		this.liblatitude = Double.MAX_VALUE;
 		this.liblongitude = Double.MAX_VALUE;
 	}
 	
 	/**
 	 * This function calculates the Julian centuries based on the current 
 	 * observation date/time. The empty AstroDate constructor defaults to 
 	 * epoch J2000.0
 	 * @return : The Julian centuries for the current observation date/time.
 	 */
 	private double getJulianCenturies() {
 		double daysSinceEpoch = this.obsDate.jd() - new AstroDate().jd();
 		return daysSinceEpoch / Astro.TO_CENTURIES;
 	}
 	
 	/**
 	 * This function sets the latitude and longitude for an observing site. 
 	 * This is used for some of the calculations.
 	 * @param lat : The observing site's latitude in decimal degrees.
 	 * @param lon : The observing site's longitude in decimal degrees.
 	 */
 	public void setObservationInfo(double lat, double lon) {
 		this.obsInfo.setLatitudeDeg(lat);
 		this.obsInfo.setLongitudeDeg(lon);
 	}
 	
 	/**
 	 * This function gets the age of the Moon in days.
 	 * @return : The Moon's age.
 	 */
 	public double age() {
 		return LunarCalc.ageOfMoonInDays(this.obsLocal);
 	}
 	
 	/**
 	 * This function gets the illuminated fraction of the Moon's surface.
 	 * @return : The fraction of the illuminated Moon surface.
 	 */
 	public double illumation() {
 		double illum = 0.0;
 		try {
 			illum = this.lunar.illuminatedFraction();
 		}
 		catch (NoInitException nie) {
 			Log.e(TAG, "Lunar object is not initialized for calculating illumination.");
 		}
 		return illum;
 	}
 	
 	/**
 	 * This function gets the current local observation time.
 	 * @return : The object holding the time.
 	 */
 	public Calendar getObsLocal() {
 		return this.obsLocal;
 	}
 
 	/**
 	 * This function gets the current UTC observation time.
 	 * @return : The object holding the time.
 	 */
 	public Calendar getObsUtc() {
 		return this.obsDate.toGCalendar();
 	}
 
 	/**
 	 * This function returns the selenographic colongitude for the current 
 	 * date and time.
 	 * @return : The current selenographic colongitude.
 	 */
 	public double colong() {
 		this.getColongitude();
 		Log.i(TAG, "Colongitude calculated = " + Double.toString(this.colongitude));
 		return this.colongitude;
 	}
 	
 	/**
 	 * This function returns the phase angle for the moon in units of 
 	 * radians.
 	 * @return : The current lunar phase angle.
 	 */
 	public double phaseAngle() {
 		double phaseAngle = 0.0;
 		try {
 			phaseAngle = this.lunar.phaseAngle();
 		}
 		catch (NoInitException nie) {
 			Log.e(TAG, "Lunar object is not initialized for calculating phase angle.");
 		}
 		return phaseAngle;
 	}
 
 	/**
 	 * This function returns the lunar librations in both latitude and longitude.
 	 * @return : The lunar librations array.
 	 */
 	public double[] librations() {
 		this.getLibrations();
 		double[] temp = {this.liblatitude, this.liblongitude};
 		return temp;
 	}
 	
 	/**
 	 * This function returns the phase of the Moon as a string.
 	 * @return : The Moon phase as a string.
 	 */
 	public String phase() {
 		return this.phaseNames[this.getPhase().ordinal()];
 	}
 	
 	/**
 	 * This function determines if the given lunar feature is visible based 
      * on the current selenographic colongitude (SELCO). For most features 
      * near the equator, from NM to FM once the SELCO recedes about 15 
      * degrees, the shadow relief makes it tough to observe. Conversely, the 
      * SELCO needs to be within 15 degrees of the feature from FM to NM. 
      * Features closer to the poles are visible much longer after the 15 
      * degree cutoff. A 1/cos(latitude) will be applied to the cutoff. 
      * Mare and Oceanus are special exceptions and once FULLY visible they are 
      * always visible.
 	 * @param feature : The lunar feature to check for visibility.
 	 * @return : True is the feature is visible.
 	 */
 	public boolean isVisible(LunarFeature feature) {
 		double selcoLong = this.colongToLong();
 		Log.v(TAG, "SelcoLong = " + Double.toString(selcoLong));
 		int curTod = this.getTimeOfDay().ordinal();
 		Log.v(TAG, "CurTod = " + Integer.toString(curTod));
 		
 		double minLon = feature.getLongitude() - feature.getDeltaLongitude() / 2.0;
 		double maxLon = feature.getLongitude() + feature.getDeltaLongitude() / 2.0;
 		
 		// Switch things around if necessary
 		if (minLon > maxLon) {
 			double temp = minLon;
 			minLon = maxLon;
 			maxLon = temp;
 		}
 		
 		boolean isVisible = false;
 		double latitudeScaling = Math.cos(Math.toRadians(Math.abs(feature.getLatitude())));
 		double cutoff = MoonInfo.FEATURE_CUTOFF / latitudeScaling;
 		
 		double lonCutoff = 0.0;
 		if (TimeOfDay.MORNING.ordinal() == curTod) {
 			// Minimum longitude for morning visibility
 			lonCutoff = minLon - cutoff;
 			if (this.noCutoffFeature(feature.getFeatureType())) {
 				
 				isVisible = selcoLong <= minLon;
 			}
 			else {
 				isVisible = (selcoLong >= lonCutoff && selcoLong <= minLon);
 			}
 		}
 		if (TimeOfDay.EVENING.ordinal() == curTod) {
 			// Maximum longitude for evening visibility
 			lonCutoff = maxLon + cutoff;
 			if (this.noCutoffFeature(feature.getFeatureType())) {
 				isVisible = maxLon <= selcoLong;
 			}
 			else {
 				isVisible = (selcoLong >= maxLon && selcoLong <= lonCutoff);
 			}
 		}
 		
 		return (isVisible && this.isLibrationOk(feature));
 	}
 
 	/**
 	 * This function determines if feature is effected and possibly obscured 
 	 * by libration.
 	 * @param feature : The lunar feature to check for the libration effect.
 	 * @return : False if libration obscures feature.
 	 */
 	private boolean isLibrationOk(LunarFeature feature) {
 		boolean isLongitudeInZone = Math.abs(feature.getLongitude()) > LIBRATION_ZONE;
 		boolean isLatitudeInZone = Math.abs(feature.getLatitude()) > LIBRATION_ZONE;
 		if (isLongitudeInZone || isLatitudeInZone) {
 			this.getLibrations();
 			if (isLongitudeInZone) {
 				double longitude = feature.getLongitude();
 				double[] longRange = feature.getLongitudeRange();
 				
 				longitude -= this.liblongitude;
 				longRange[0] -= this.liblongitude;
 				longRange[1] -= this.liblongitude;
 				
 				Log.d(TAG, "Adjusted longitude: " + longitude + " ["
 						+ longRange[0] + ", " + longRange[1] + "]");
 				
 				if (longitude < 0) {
 					if (longRange[1] < -LUNAR_EDGE) {
 						return false;
 					}
 				}
 				else {
 					if (longRange[0] > LUNAR_EDGE) {
 						return false;
 					}
 				}
 			}
 			if (isLatitudeInZone) {
 				double latitude = feature.getLatitude();
 				double[] latRange = feature.getLatitudeRange();
 				
 				latitude -= this.liblatitude;
 				latRange[0] -= this.liblatitude;
 				latRange[1] -= this.liblatitude;
 				
 				Log.d(TAG, "Adjusted latitude: " + latitude + " ["
 						+ latRange[0] + ", " + latRange[1] + "]");
 				
 				if (latitude < 0) {
 					if (latRange[1] < -LUNAR_EDGE) {
 						return false;
 					}
 				}
 				else {
 					if (latRange[0] > LUNAR_EDGE) {
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 		return true;
 	}
 	
 	/**
 	 * This function is to set the selenographic colongitude once for a given 
 	 * instance. This will cut down on the number of calculations done by the 
 	 * library call.
 	 */
 	private void getColongitude() {
 		if (Double.MAX_VALUE == this.colongitude) {
 			this.colongitude = LunarCalc.colongitude(this.getJulianCenturies());
 		}
 	}
 
 	/**
 	 * This function obtains the lunar librations in both latitude and longitude.
 	 */
 	private void getLibrations() {
 		if (Double.MAX_VALUE == this.liblatitude && 
 				Double.MAX_VALUE == this.liblongitude) {
 			try {
 				LocationElements le = this.lunar.getTotalLibrations();
 				this.liblatitude = Math.toDegrees(le.getLatitude());
 				this.liblongitude = Math.toDegrees(le.getLongitude());
 				Log.i(TAG, "Libration in Latitude = " + this.liblatitude);
 				Log.i(TAG, "Libration in Longitude = " + this.liblongitude);
 			}
 			catch (NoInitException nie) {
 				Log.e(TAG, "Lunar object is not initialized for calculating librations.");
 			}
 		}
 	}
 	
 	/**
 	 * This function checks a given lunar feature type against the list of lunar 
 	 * feature types that have no cutoff.
 	 * @param type : The string containing the lunar feature type.
 	 * @return : True is the incoming feature type is in no cutoff list.
 	 */
 	private boolean noCutoffFeature(String type) {
 		for (String s : this.noCutoffType) {
 			if (s.equalsIgnoreCase(type)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * This function returns the moon phase according to standard nomenclature.
 	 * @return : The moon phase as an enum value.
 	 */
 	private Phase getPhase() {
 		double illum = this.illumation();
 		double phaseAngle = this.phaseAngle();
 		double sinPhaseAngle = Math.sin(phaseAngle);
 		if (illum < phaseBounds[0]) {
 			return Phase.NM;
 		}
 		if (phaseBounds[0] <= illum && illum <= phaseBounds[1]) {
 			if (sinPhaseAngle > 0.0) {
 				return Phase.WAXING_CRESENT;
 			}
 			else {
 				return Phase.WANING_CRESENT;
 			}
 		}
 		if (phaseBounds[1] < illum && illum < phaseBounds[2]) {
 			if (sinPhaseAngle > 0.0) {
 				return Phase.FQ;
 			}
 			else {
 				return Phase.TQ;
 			}
 		}
 		if (phaseBounds[2] <= illum && illum <= phaseBounds[3]) {
 			if (sinPhaseAngle > 0.0) {
 				return Phase.WAXING_GIBBOUS;
 			}
 			else {
 				return Phase.WANING_GIBBOUS;
 			}
 		}
 		if (illum > phaseBounds[3]) {
 			return Phase.FM;
 		}
 		// Shouldn't make it here
 		return Phase.NM;
 	}
 	
 	/**
 	 * This function determines the current time of day on the moon. In 
      * otherwords, if the sun is rising on the moon it is morning or if the 
      * sun is setting on the moon it is evening.
 	 * @return : The current time of day.
 	 */
 	private TimeOfDay getTimeOfDay() {
 		double phaseAngle = this.phaseAngle();
 		double sinPhaseAngle = Math.sin(phaseAngle);
 		if (sinPhaseAngle > 0.0) {
 			return TimeOfDay.MORNING;
 		}
 		else if (sinPhaseAngle < 0.0) {
 			return TimeOfDay.EVENING;
 		}
 		else {
 			if (phaseAngle == 0.0 || phaseAngle == Astro.TWO_PI) {
 				return TimeOfDay.EVENING;
 			}
 		}
 		return TimeOfDay.MORNING;
 	}
 	
 	/**
 	 * This function calculates the conversion between the selenographic  
      * colongitude and actual lunar longitude.
 	 * @return : The lunar longitude for the current selenographic colongitude.
 	 */
 	private double colongToLong() {
 		this.getColongitude();
 		double phaseAngle = this.phaseAngle();
 		double sinPhaseAngle = Math.sin(phaseAngle);
		if (phaseAngle <= Math.PI && phaseAngle > Astro.PI_OVER_TWO) {
 			return 360.0 - this.colongitude;
 		}
		if (phaseAngle <= Astro.PI_OVER_TWO && phaseAngle > 0.0) {
 			return -1.0 * this.colongitude;
 		}
 		if (sinPhaseAngle < 0.0 || phaseAngle == 0.0 || phaseAngle == Astro.TWO_PI) {
 			return 180.0 - this.colongitude;
 		}
 		Log.d(TAG, "Oops, shouldn't have gotten here!");
 		return this.colongitude;
 	}
 	
 	/**
 	 * This function creates the string representation of the object.
 	 * @return : The current string representation.
 	 */
 	public String toString() {
 		StringBuffer buf = new StringBuffer();
 		String[] tmp = StrFormat.dateFormat(this.obsLocal).split(" ", 2);
 		buf.append("Date: ").append(tmp[0]).append(System.getProperty("line.separator"));
 		buf.append("Time: ").append(tmp[1]).append(System.getProperty("line.separator"));
 		buf.append("Julian Date: ").append(Double.toString(this.obsDate.jd()));
 		buf.append(System.getProperty("line.separator"));
 		return buf.toString();
 	}
 	
 	/**
 	 * This function returns the local date of the previous new Moon.
 	 * @return : The date of the previous new Moon.
 	 */
 	public Calendar previousNewMoon() {
 		AstroDate nmDate = this.findPreviousPhase(Lunar.NEW);
 		return this.fixTime(nmDate);
 	}
 
 	/**
 	 * This function returns the local date of the previous full Moon.
 	 * @return : The date of the previous full Moon.
 	 */
 	public Calendar previousFullMoon() {
 		AstroDate fmDate = this.findPreviousPhase(Lunar.FULL);
 		return this.fixTime(fmDate);
 	}
 
 	/**
 	 * This function returns the local date of the previous first quarter Moon.
 	 * @return : The date of the previous first quarter Moon.
 	 */
 	public Calendar previousFirstQuarterMoon() {
 		AstroDate fqmDate = this.findPreviousPhase(Lunar.Q1);
 		return this.fixTime(fqmDate);
 	}
 	
 	/**
 	 * This function returns the local date of the previous third quarter Moon.
 	 * @return : The date of the previous third quarter Moon.
 	 */
 	public Calendar previousThirdQuarterMoon() {
 		AstroDate tqmDate = this.findPreviousPhase(Lunar.Q3);
 		return this.fixTime(tqmDate);
 	}
 	
 	/**
 	 * This function returns the local date of the next new Moon.
 	 * @return : The date of the next new Moon.
 	 */
 	public Calendar nextNewMoon() {
 		AstroDate nmDate = this.findNextPhase(Lunar.NEW);
 		return this.fixTime(nmDate);
 	}
 	
 	/**
 	 * This function returns the local date of the next full Moon.
 	 * @return : The date of the next full Moon.
 	 */
 	public Calendar nextFullMoon() {
 		AstroDate fmDate = this.findNextPhase(Lunar.FULL);
 		return this.fixTime(fmDate);
 	}
 	
 	/**
 	 * This function returns the local date of the next first quarter Moon.
 	 * @return : The date of the next first quarter Moon.
 	 */
 	public Calendar nextFirstQuarterMoon() {
 		AstroDate fqmDate = this.findNextPhase(Lunar.Q1);
 		return this.fixTime(fqmDate);
 	}
 
 	/**
 	 * This function returns the local date of the next third quarter Moon.
 	 * @return : The date of the next third quarter Moon.
 	 */
 	public Calendar nextThirdQuarterMoon() {
 		AstroDate tqmDate = this.findNextPhase(Lunar.Q3);
 		return this.fixTime(tqmDate);
 	}
 	
 	/**
 	 * This function determines the previous UTC date of the requested lunar 
 	 * phase. In this context, previous means before the current UTC date.
 	 * @param phase : The requested phase to find.
 	 * @return : The UTC date of the requested phase.
 	 */
 	private AstroDate findPreviousPhase(int phase) {
 		double date = Lunar.getPhase(DateOps.calendarToDay(this.obsDate.toGCalendar()), 
 				phase);
 		AstroDate phaseDate = new AstroDate(date);
 		if (this.obsDate.toGCalendar().before(phaseDate.toGCalendar())) {
 			// Roll date back by Lunar month
 			Calendar cal = this.obsDate.toGCalendar();
 			cal.add(Calendar.DAY_OF_MONTH, 
 					-1*(int)(LunarCalc.SYNODIC_MONTH+0.5));
 			Log.d(TAG, "Previous phase date to use: " + StrFormat.dateFormat(cal));
 			date = Lunar.getPhase(DateOps.calendarToDay(cal), phase);
 			phaseDate = new AstroDate(date);
 		}
 		return phaseDate;
 	}
 	
 	/**
 	 * This function determines the next UTC date of the requested lunar 
 	 * phase. In this context, next means after the current UTC date.
 	 * @param phase : The requested phase to find.
 	 * @return : The UTC date of the requested phase.
 	 */
 	private AstroDate findNextPhase(int phase) {
 		double date = Lunar.getPhase(DateOps.calendarToDay(this.obsDate.toGCalendar()), 
 				phase);
 		AstroDate phaseDate = new AstroDate(date);
 		if (this.obsDate.toGCalendar().after(phaseDate.toGCalendar())) {
 			// Push date out by Lunar month
 			Calendar cal = this.obsDate.toGCalendar();
 			cal.add(Calendar.DAY_OF_MONTH, 
 					(int)(LunarCalc.SYNODIC_MONTH+0.5));
 			Log.d(TAG, "Next phase date to use: " + StrFormat.dateFormat(cal));
 			date = Lunar.getPhase(DateOps.calendarToDay(cal), phase);
 			phaseDate = new AstroDate(date);
 		}
 		return phaseDate;
 	}
 	
 	/**
 	 * This function finds the calendar date of the requested lunar phase.
 	 * @param phase : The value of the lunar phase.
 	 * @return : The calendar date of the lunar phase.
 	 */
 	private Calendar findPhase(int phase) {
 		double date = Lunar.getPhase(DateOps.calendarToDay(this.obsDate.toGCalendar()), 
 				phase);
 		AstroDate phaseDate = new AstroDate(date);
 		GregorianCalendar c = phaseDate.toGCalendar();
 		c.add(Calendar.HOUR_OF_DAY, this.tzOffset);
 		return c;
 	}
 	
 	/**
 	 * This function finds the dates for the next four lunar phases. A map is created that 
 	 * contains the calendar date of the phase as the map key and an integer values for the 
 	 * phases as the map value.
 	 * @return : A map containing the information.
 	 */
 	@SuppressLint("UseSparseArrays")
 	public Map<Calendar, Integer> findNextFourPhases() {
 		Map<Calendar, Integer> phases = new HashMap<Calendar, Integer>();
 		List<Integer> indices = new ArrayList<Integer>();
 		for (int i = 0; i < 4; i++) {
 			Calendar cal = this.findPhase(phaseValues[i]);
 			if (cal.before(this.obsLocal)) {
 				indices.add(Integer.valueOf(i));
 				continue;
 			}
 			else {
 				phases.put(cal, Integer.valueOf(i));
 			}
 		}
 		// If the indices list is not empty, need to fill with next phases.
 		Iterator<Integer> lit = indices.iterator();
 		while (lit.hasNext()) {
 			Integer phase = lit.next();
 			AstroDate ad = this.findNextPhase(phase.intValue());
 			Calendar cal = this.fixTime(ad);
 			phases.put(cal, phase);
 		}
 		return phases;
 	}
 	
 	/**
 	 * This function takes an AstroDate which is in UTC and changes it to a 
 	 * GregorianCalendar and then adjusts it for local time.
 	 * @param ad : The date to adjust.
 	 * @return : The adjusted local date.
 	 */
 	private GregorianCalendar fixTime(AstroDate ad) {
 		GregorianCalendar c = ad.toGCalendar();
 		c.add(Calendar.HOUR_OF_DAY, this.tzOffset);
 		return c;
 	}
 }
