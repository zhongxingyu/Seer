 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012 John Lawson
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at  
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package org.nof1trial.nof1;
 
 /**
  * Container class holding various keys for loading SharedPreferences or extras from Bundles, Intents etc
  * 
  * @author John Lawson
  * 
  */
 public class Keys {
 	
 	/** Name of default shared preferences */
	public static final String DEFAULT_PREFS = "org.nof1trial.nof1_preferences";
 	
 	/** Shared preference key for Patient name */
 	public static final String DEFAULT_PATIENT_NAME = "prefs_patient_name";
 	
 	/** Shared preference key for reminder time */
 	public static final String DEFAULT_TIME = "prefs_reminder_time";
 	
 	/** Shared preference key for first time bool */
 	public static final String DEFAULT_FIRST = "prefs_first";
 	
 	/** Shared preference key for back up bool */
 	public static final String DEFAULT_BACKUP = "prefs_backup";
 	
 	/** Shared preference key for whether to vibrate on notification */
 	public static final String DEFAULT_VIBE = "prefs_vibrate";
 	
 	/** Shared preference key for whether to make noise on notification */
 	public static final String DEFAULT_LOUD = "prefs_loud";
 	
 	/** Shared preference key for whether to flash lights on notification */
 	public static final String DEFAULT_FLASH = "prefs_flash";
 	
 	/** Name of shared preferences holding the questions */
 	public static final String QUES_NAME = "questions";
 	
 	/** Shared preference key with question text. Append with required question number. */
 	public static final String QUES_TEXT = "questionText";
 	
 	/** Shared preference key with question type. Append with required question number. */
 	public static final String QUES_TYPE = "inputType";
 	
 	/** Shared preference key with minimum hint. Append with required question number. */
 	public static final String QUES_MIN = "questionMin";
 	
 	/** Shared preference key with maximum hint. Append with required question number. */
 	public static final String QUES_MAX = "questionMax";
 	
 	/** Shared preferences key for the number of questions in questionnaire */
 	public static final String QUES_NUMBER_QUESTIONS = "number_questions";
 	
 	/** Shared preference key for comment bool. True = show comment */
 	public static final String COMMENT = "comment";
 	
 	/** Intent key for preview mode */
 	public static final String INTENT_PREVIEW = "org.nof1trial.nof1.preview";
 	
 	/** Intent key for email string */
 	public static final String INTENT_EMAIL = "org.nof1trial.nof1.email";
 	
 	/** Intent key for started on boot */
 	public static final String INTENT_BOOT = "org.nof1trial.nof1.boot";
 	
 	/** Intent key for alarm bool */
 	public static final String INTENT_ALARM = "org.nof1trial.nof1.alarm";
 	
 	/** Intent key for first run */
 	public static final String INTENT_FIRST = "org.nof1trial.nof1.first";
 	
 	/** Intent key for question id */
 	public static final String INTENT_ID = "org.nof1trial.nof1.id";
 	
 	/** Intent key for medicine notification bool */
 	public static final String INTENT_MEDICINE = "org.nof1trial.nof1.medicine";
 	
 	/** Intent key for mins to reschedule */
 	public static final String INTENT_RESCHEDULE = "org.nof1trial.nof1.reschedule";
 	
 	/** Intent key for whether the questionnaire is loaded from scheduled alert */
 	public static final String INTENT_SCHEDULED = "org.nof1trial.nof1.scheduled";
 	
 	/** SharedPrefernces holding config data */
 	public static final String CONFIG_NAME = "config";
 	
 	/** Shared preference key for UUID */
 	public static final String CONFIG_UUID = "uuid";
 	
 	/** Shared preference key for whether the questionnaire is built */
 	public static final String CONFIG_BUILT = "config_built";
 	
 	/** Shared preferences key for hashed email */
 	public static final String CONFIG_EMAIL = "email_hash";
 	
 	/** Shared preference key for doctor's email */
 	public static final String CONFIG_DOC = "doc";
 	
 	/** Shared preference key for whether this is the first time doctor has logged in */
 	public static final String CONFIG_FIRST = "first_run";
 	
 	/** Shared preferences key for hashed password */
 	public static final String CONFIG_PASS = "pass_hash";
 	
 	/** Shared preferences key for doctor inputed patient name */
 	public static final String CONFIG_PATIENT_NAME = "patient_name";
 	
 	/** Shared preference key for doctor name */
 	public static final String CONFIG_DOCTOR_NAME = "doctor_name";
 	
 	/** Shared preferences key for number of days per treatment period */
 	public static final String CONFIG_PERIOD_LENGTH = "period_length";
 	
 	/** Shared preferences key for number of *pairs of* treatment periods in trial */
 	public static final String CONFIG_NUMBER_PERIODS = "number_periods";
 	
 	/** Shared preferences key prefix for whether to fill in questionnaire on this day */
 	public static final String CONFIG_DAY = "day";
 	
 	/** Shared preference key for start date */
 	public static final String CONFIG_START = "start";
 	
 	/** Shared preference key prefix for time to take medicine */
 	public static final String CONFIG_TIME = "time";
 	
 	/** Shared preference key for the treatment A */
 	public static final String CONFIG_TREATMENT_A = "treatment_a";
 	
 	/** Shared preference key for the treatment B */
 	public static final String CONFIG_TREATMENT_B = "treatment_b";
 	
 	/** Shared preference key for any treatment notes */
 	public static final String CONFIG_TREATMENT_NOTES = "treatment_notes";
 	
 	/** Shared preference key for database version */
 	public static final String CONFIG_DB_VERSION = "db_version";
 	
 	/** Shared preferences name for scheduler preferences */
 	public static final String SCHED_NAME = "config";
 	
 	/** Shared preferences key for next date to set notification */
 	public static final String SCHED_NEXT_DATE = "next_date";
 	
 	/** Shared preference key for the number of days into treatment period the next notification is */
 	public static final String SCHED_NEXT_DAY = "next_day";
 	
 	/** Shared preference key for the current treatment period in, starts at 1 */
 	public static final String SCHED_CUR_PERIOD = "cur_period";
 	
 	/** Shared preference key for the last day notification was set */
 	public static final String SCHED_LAST_DAY = "last_day";
 	
 	/** Shared preference key for the last date notification was set */
 	public static final String SCHED_LAST_DATE = "last_date";
 	
 	/** Shared preference key for the period in when last notification was set */
 	public static final String SCHED_LAST_PERIOD = "last_period";
 	
 	/** Shared preference key for the cumulative number of days into the trial */
 	public static final String SCHED_CUMULATIVE_DAY = "cum_day";
 	
 	/** Shared preference key for the next cumulative number of days into the trial */
 	public static final String SCHED_NEXT_CUMULATIVE_DAY = "next_cum_day";
 	
 	/** Shared preference key for whether the trial is finished */
 	public static final String SCHED_FINISHED = "finished";
 	
 }
