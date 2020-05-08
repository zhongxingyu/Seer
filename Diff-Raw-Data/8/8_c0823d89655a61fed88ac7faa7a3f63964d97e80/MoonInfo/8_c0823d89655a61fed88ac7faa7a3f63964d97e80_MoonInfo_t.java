 package com.typeiisoft.lct.utils;
 
 import com.typeiisoft.lct.features.LunarFeature;
 
 import com.mhuss.AstroLib.Astro;
 import com.mhuss.AstroLib.AstroDate;
 import com.mhuss.AstroLib.DateOps;
 import com.mhuss.AstroLib.Lunar;
 import com.mhuss.AstroLib.LunarCalc;
 import com.mhuss.AstroLib.NoInitException;
 import com.mhuss.AstroLib.ObsInfo;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
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
 	/** Enum containing the lunar phases for integer comparison. */
 	private enum Phase {
 		NM, WAXING_CRESENT, FQ, WAXING_GIBBOUS, FM, WANING_GIBBOUS, TQ,
 		WANING_CRESENT;
 	}
 	/** Array of lunar phase names. */
 	private String[] phaseNames = {"New Moon", "Waxing Cresent", 
 			"First Quarter", "Waxing Gibbous", "Full Moon", "Waning Gibbous",
 			"Third Quarter", "Waning Cresent"};
 	/** Enum containing the time of lunar day for integer comparison. */
 	private enum TimeOfDay {
 		MORNING, EVENING;
 	}
 	/** Selenographic longitude where relief makes it hard to see feature. */
 	private static final double FEATURE_CUTOFF = 15D;
 	/** Lunar features to which no selenographic longitude cutoff is applied. */
 	private String[] noCutoffType = {"Mare", "Oceanus"};
 	
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
 		
 		return isVisible;
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
 		this.getColongitude();
 		if (270.0 == this.colongitude) {
 			return Phase.NM;
 		}
 		else if (this.colongitude > 270.0 && this.colongitude < 360.0) {
 			return Phase.WAXING_CRESENT;
 		}
 		else if (0.0 == this.colongitude || 360.0 == this.colongitude) {
 			return Phase.FQ;
 		}
 		else if (this.colongitude > 0.0 && this.colongitude < 90.0) {
 			return Phase.WAXING_GIBBOUS;
 		}
 		else if (90.0 == this.colongitude) {
 			return Phase.FM;
 		}
 		else if (this.colongitude > 90.0 && this.colongitude < 180.0) {
 			return Phase.WANING_GIBBOUS;
 		}
 		else if (180.0 == this.colongitude) {
 			return Phase.TQ;
 		}
 		else if (this.colongitude > 180.0 && this.colongitude < 270.0) {
 			return Phase.WANING_CRESENT;
 		}
 		else {
 			return Phase.NM;
 		}
 	}
 	
 	/**
 	 * This function determines the current time of day on the moon. In 
      * otherwords, if the sun is rising on the moon it is morning or if the 
      * sun is setting on the moon it is evening.
 	 * @return : The current time of day.
 	 */
 	private TimeOfDay getTimeOfDay() {
 		int phase = this.getPhase().ordinal();
 		if (phase >= Phase.NM.ordinal() && 
 				phase <= Phase.WAXING_GIBBOUS.ordinal()) {
 			return TimeOfDay.MORNING;
 		}
 		else {
 			return TimeOfDay.EVENING;
 		}
 	}
 	
 	/**
 	 * This function calculates the conversion between the selenographic  
      * colongitude and actual lunar longitude.
 	 * @return : The lunar longitude for the current selenographic colongitude.
 	 */
 	private double colongToLong() {
 		this.getColongitude();
 		int phase = this.getPhase().ordinal();
 		if (Phase.NM.ordinal() == phase || Phase.WAXING_CRESENT.ordinal() == phase) {
 			return 360.0 - this.colongitude;
 		}
 		else if (Phase.FQ.ordinal() == phase || Phase.WAXING_GIBBOUS.ordinal() == phase) {
 			return -1.0 * this.colongitude;
 		}
 		else if (Phase.FM.ordinal() == phase || Phase.WANING_GIBBOUS.ordinal() == phase) {
 			return 180.0 - this.colongitude;
 		}
 		else {
 			return -1.0 * (this.colongitude - 180.0);
 		}
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
