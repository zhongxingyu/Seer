 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter;
 
 import java.lang.reflect.Field;
 import java.util.Iterator;
 import java.util.List;
 
 import se.chalmers.dat255.sleepfighter.audio.AudioDriver;
 import se.chalmers.dat255.sleepfighter.audio.AudioDriverFactory;
 import se.chalmers.dat255.sleepfighter.factory.FromPresetAlarmFactory;
 import se.chalmers.dat255.sleepfighter.factory.PresetAlarmFactory;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.AlarmList;
 import se.chalmers.dat255.sleepfighter.model.gps.GPSFilterAreaSet;
 import se.chalmers.dat255.sleepfighter.persist.PersistenceManager;
 import se.chalmers.dat255.sleepfighter.preference.GlobalPreferencesManager;
 import se.chalmers.dat255.sleepfighter.service.AlarmPlannerService;
 import se.chalmers.dat255.sleepfighter.speech.TextToSpeechUtil;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import se.chalmers.dat255.sleepfighter.utils.message.Message;
 import se.chalmers.dat255.sleepfighter.utils.message.MessageBus;
 import android.app.Application;
 import android.speech.tts.TextToSpeech;
 import android.view.ViewConfiguration;
 
 /**
  * A custom implementation of Application for SleepFighter.
  */
 public class SFApplication extends Application implements TextToSpeech.OnInitListener {
 	private static final boolean CLEAN_START = false;
 
 	private static SFApplication app;
 
 	private GlobalPreferencesManager prefs;
 
 	private AlarmList alarmList;
 	private Alarm ringingAlarm;
 	private MessageBus<Message> bus;
 
 	private PersistenceManager persistenceManager;
 
 	private AlarmPlannerService.ChangeHandler alarmPlanner;
 
 	private AudioDriver audioDriver;
 	private AudioDriverFactory audioDriverFactory;
 
 	private FromPresetAlarmFactory fromPresetFactory;
 
 	private GPSFilterAreaSet gpsAreaManaged;
 	
 	//private String weather;
 	
 	private TextToSpeech tts;
 
 	// called when the text to speech engine is initialized. 
 	@Override
 	public void onInit(int status) {
 		Debug.d("on init tts");
 		tts.setLanguage(TextToSpeechUtil.getBestLanguage(tts, this));
 		TextToSpeechUtil.config(tts);
 	}
 	
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		app = this;
 
 		tts = new TextToSpeech(this, this);
 		
 		this.prefs = new GlobalPreferencesManager( this );
 
 		this.persistenceManager = new PersistenceManager( this );
 		this.getBus().subscribe( this.persistenceManager );
 
 		forceActionBarOverflow();
 	}
 
 	/**
 	 * Returns the one and only SFApplication in town.
 	 *
 	 * @return the application.
 	 */
 	public static final SFApplication get() {
 		return app;
 	}
 
 	/**
 	 * Show the triple-dot action bar overflow icon even on devices with a
 	 * dedicated menu button.
 	 * 
 	 * <p>
 	 * Solution from stackoverflow post found 
 	 * <a href="http://stackoverflow.com/questions/9286822/
 	 * how-to-force-use-of-overflow-menu-on-devices-with-menu-button/
 	 * 11438245#11438245">here</a>.
 	 * </p>
 	 */
 	private void forceActionBarOverflow() {
 	    try {
 	        ViewConfiguration config = ViewConfiguration.get(this);
 	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
 	        if(menuKeyField != null) {
 	            menuKeyField.setAccessible(true);
 	            menuKeyField.setBoolean(config, false);
 	        }
 	    } catch (Exception ex) {
 	        // Ignore
 	    }
 	}
 
 	/**
 	 * Returns the application global GlobalPreferencesReader object.
 	 *
 	 * @return the GlobalPreferencesReader.
 	 */
 	public synchronized GlobalPreferencesManager getPrefs() {
 		return this.prefs;
 	}
 
 	/**
 	 * Returns the factory that creates alarms from preset.<br/>
 	 * If factory does not exist, neither does preset,<br/>
 	 * so one is made via {@link PresetAlarmFactory}.
 	 *
 	 * @return the factory that creates alarms from preset.
 	 */
 	public synchronized FromPresetAlarmFactory getFromPresetFactory() {
 		// Make sure we have pulled a list from database or this won't work.
 		if ( this.alarmList == null ) {
 			this.getAlarms();
 		}
 
 		if ( this.fromPresetFactory == null ) {
 			// Make preset alarm, add to database and finally store in factory.
 			Alarm preset = new PresetAlarmFactory().createAlarm();
 			preset.setMessageBus( this.getBus() );
 
 			this.getPersister().addAlarm( preset );
 
 			this.fromPresetFactory = new FromPresetAlarmFactory( preset );
 		}
 
 		return this.fromPresetFactory;
 	}
 
 	/**
 	 * Filters out preset alarm and stores it in SFApplication for later use.<br/>
 	 * If no preset was found, preset is not stored.
 	 *
 	 * @param alarms the list of alarms to filter.
 	 */
 	private void filterPresetAlarm( List<Alarm> alarms ) {
 		Iterator<Alarm> iter = alarms.iterator();
 		while ( iter.hasNext() ) {
 			Alarm alarm = iter.next();
 			if ( alarm.isPresetAlarm() ) {
 				// Take the opportunity to store preset in a factory.
 				this.fromPresetFactory = new FromPresetAlarmFactory( alarm );
 				iter.remove();
				alarm.setMessageBus(this.getBus());
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Returns the AlarmList for the application.<br/>
 	 * It is lazy loaded.
 	 * 
 	 * @return the AlarmList for the application
 	 */
 	public synchronized AlarmList getAlarms() {
 		if ( this.alarmList == null ) {
 			if ( CLEAN_START ) {
 				this.persistenceManager.cleanStart();
 			}
 
 			List<Alarm> alarms = this.getPersister().fetchAlarms();
 			this.filterPresetAlarm( alarms );
 
 			this.alarmList = new AlarmList( alarms );
 			this.alarmList.setMessageBus(this.getBus());
 
 			this.registerAlarmPlanner();
 		}
 
 		return alarmList;
 	}
 
 	private void registerAlarmPlanner() {
 		this.alarmPlanner = new AlarmPlannerService.ChangeHandler( this, this.alarmList );
 		this.bus.subscribe( this.alarmPlanner );
 	}
 
 	/**
 	 * Returns the default MessageBus for the application.
 	 * 
 	 * @return the default MessageBus for the application
 	 */
 	public synchronized MessageBus<Message> getBus() {
 		if ( this.bus == null ) {
 			this.bus = new MessageBus<Message>();
 		}
 		return bus;
 	}
 
 	/**
 	 * Returns the PersistenceManager for the application.
 	 *
 	 * @return the persistence manager.
 	 */
 	public synchronized PersistenceManager getPersister() {
 		return this.persistenceManager;
 	}
 
 	/**
 	 * Returns the application global AudioDriver if any.
 	 *
 	 * @return the audio driver.
 	 */
 	public synchronized AudioDriver getAudioDriver() {
 		return this.audioDriver;
 	}
 
 	/**
 	 * Sets an application global AudioDriver, null is allowed.<br/>
 	 * If the previous audio driver was playing, it is stopped.
 	 *
 	 * @param driver the audio driver to set.
 	 */
 	public synchronized void setAudioDriver( AudioDriver driver ) {
 		if ( this.audioDriver != null && this.audioDriver.isPlaying() ) {
 			this.audioDriver.stop();
 		}
 
 		this.audioDriver = driver;
 	}
 
 	/**
 	 * Returns the application global AudioDriverFactory object.<br/>
 	 * This object is lazy loaded.
 	 *
 	 * @return the factory.
 	 */
 	public synchronized AudioDriverFactory getAudioDriverFactory() {
 		if ( this.audioDriverFactory == null ) {
 			this.audioDriverFactory = new AudioDriverFactory();
 		}
 
 		return this.audioDriverFactory;
 	}
 	
 	/**
 	 * Fetches the set of areas and manages it in application.<br/>
 	 * Call {@link #releaseGPSSet()} to release reference to it in application.
 	 */
 	public synchronized GPSFilterAreaSet getGPSSet() {
 		if ( this.gpsAreaManaged == null ) {
 			this.gpsAreaManaged = this.getPersister().fetchGPSFilterAreas();
 			this.gpsAreaManaged.setMessageBus( this.getBus() );
 		}
 
 		return this.gpsAreaManaged;
 	}
 
 	/**
 	 * Clears the reference the application holds to any {@link GPSFilterAreaSet}
 	 * 
 	 * @return the set, or null if no set is managed.
 	 */
 	public synchronized void releaseGPSSet() {
 		this.gpsAreaManaged = null;
 	}
 	
 	
 	public TextToSpeech getTts() {
 		return this.tts;
 	}
 
 	/**
 	 * Gets the currently ringing alarm.
 	 * 
 	 * @return the ringing alarm, null if no alarm is ringing
 	 */
 	public Alarm getRingingAlarm() {
 		return ringingAlarm;
 	}
 
 	/**
 	 * Sets the currently ringing alarm.
 	 * 
 	 * <p>
 	 * Added for being able to check from activities if an alarm is currently
 	 * ringing, to then redirect the user to AlarmActivity.
 	 * </p>
 	 * 
 	 * @param alarm
 	 *            the alarm to set as the ringing alarm
 	 */
 	public void setRingingAlarm(Alarm alarm) {
 		this.ringingAlarm = alarm;
 	}
 }
