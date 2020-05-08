 package org.nof1trial.nof1.containers;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import org.nof1trial.nof1.Keys;
 import org.nof1trial.nof1.app.Util;
 import org.nof1trial.nof1.shared.ConfigProxy;
 import org.nof1trial.nof1.shared.ConfigRequest;
 import org.nof1trial.nof1.shared.MyRequestFactory;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 import com.google.web.bindery.requestfactory.shared.Receiver;
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 
 public class ConfigData {
 
 	private String patientName;
 	private String doctorName;
 	private String doctorEmail;
 	private String pharmEmail;
 	private int numberPeriods;
 	private int periodLength;
 	private String startDate;
 	private String treatmentA;
 	private String treatmentB;
 	private String treatmentNotes;
 	private ArrayList<String> quesList;
 	private ArrayList<String> timeList;
 	private ArrayList<Boolean> dayList;
 	private boolean formBuilt;
 
 	private OnConfigRequestListener listener;
 
 	private ConfigData() {
 		quesList = new ArrayList<String>();
 		timeList = new ArrayList<String>();
 		dayList = new ArrayList<Boolean>();
 	}
 
 	public interface OnConfigRequestListener {
 		/**
 		 * Called when config uploaded successfully
 		 * 
 		 * @param conf Copy of the configProxy saved in the datastore
 		 */
 		public void onConfigUploadSuccess(ConfigProxy conf);
 
 		/**
 		 * Called when config fails to upload. Could be auth failure, or other problem with server.
 		 * 
 		 * @param failure Wrapper containing information about the failure
 		 */
 		public void onConfigUploadFailure(ServerFailure failure);
 	}
 
 	public void saveToPrefs(SharedPreferences prefs) {
 		// Save to file
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putString(Keys.CONFIG_PATIENT_NAME, patientName);
 		editor.putString(Keys.CONFIG_DOCTOR_NAME, doctorName);
 		editor.putString(Keys.CONFIG_DOC, doctorEmail);
 		editor.putInt(Keys.CONFIG_NUMBER_PERIODS, numberPeriods);
 		editor.putInt(Keys.CONFIG_PERIOD_LENGTH, periodLength);
 		editor.putBoolean(Keys.CONFIG_BUILT, formBuilt);
 		editor.putString(Keys.CONFIG_START, startDate);
 		editor.putString(Keys.CONFIG_PHARM, pharmEmail);
 
 		for (int i = 0; i < timeList.size(); i++) {
 			editor.putString(Keys.CONFIG_TIME + i, timeList.get(i));
 		}
 
 		editor.putString(Keys.CONFIG_TREATMENT_A, treatmentA);
 		editor.putString(Keys.CONFIG_TREATMENT_B, treatmentB);
 		editor.putString(Keys.CONFIG_TREATMENT_NOTES, treatmentNotes);
 
		for (int i = 0; i < dayList.size(); i++) {
			editor.putBoolean(Keys.CONFIG_DAY + (i + 1), dayList.get(i));
 		}
 		editor.commit();
 
 	}
 
 	public void upload(Context context) {
 		// Get request factory
 		MyRequestFactory factory = Util.getRequestFactory(context, MyRequestFactory.class);
 		ConfigRequest request = factory.configRequest();
 
 		// Build config
 		ConfigProxy conf = request.create(ConfigProxy.class);
 		conf.setDocEmail(doctorEmail);
 		conf.setDoctorName(doctorName);
 		conf.setPatientName(patientName);
 		conf.setPharmEmail(pharmEmail);
 		conf.setStartDate(startDate);
 		conf.setLengthPeriods((long) periodLength);
 		conf.setNumberPeriods((long) numberPeriods);
 		conf.setTreatmentA(treatmentA);
 		conf.setTreatmentB(treatmentB);
 		conf.setTreatmentNotes(treatmentNotes);
 		conf.setQuestionList(quesList);
 		conf.setEndDate(getEndDate());
 
 		request.update(conf).fire(new Receiver<ConfigProxy>() {
 
 			@Override
 			public void onSuccess(ConfigProxy response) {
 				listener.onConfigUploadSuccess(response);
 			}
 
 			@Override
 			public void onFailure(ServerFailure error) {
 				listener.onConfigUploadFailure(error);
 			}
 		});
 	}
 
 	private Calendar getEndDateCalendar() {
 		String[] arr = startDate.split(":");
 		int[] date = new int[] { Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]) };
 		Calendar cal = Calendar.getInstance();
 		cal.set(date[2], date[1], date[0], 23, 59);
 		cal.add(Calendar.DAY_OF_MONTH, (int) (2 * periodLength * numberPeriods));
 		return cal;
 	}
 
 	private long getEndDate() {
 		Calendar cal = getEndDateCalendar();
 		return cal.getTimeInMillis();
 	}
 
 	public static class Factory {
 
 		private ConfigData conf;
 
 		public Factory(OnConfigRequestListener listener) {
 			conf = new ConfigData();
 			conf.listener = listener;
 		}
 
 		public ConfigData generateFromPrefs(SharedPreferences prefs, SharedPreferences ques) {
 
 			conf.patientName = prefs.getString(Keys.CONFIG_PATIENT_NAME, "");
 			conf.doctorName = prefs.getString(Keys.CONFIG_DOCTOR_NAME, "");
 			conf.doctorEmail = prefs.getString(Keys.CONFIG_DOC, "");
 			conf.pharmEmail = prefs.getString(Keys.CONFIG_PHARM, "");
 			conf.numberPeriods = prefs.getInt(Keys.CONFIG_NUMBER_PERIODS, 0);
 			conf.periodLength = prefs.getInt(Keys.CONFIG_PERIOD_LENGTH, 0);
 			conf.startDate = prefs.getString(Keys.CONFIG_START, "");
 			conf.treatmentA = prefs.getString(Keys.CONFIG_TREATMENT_A, "");
 			conf.treatmentB = prefs.getString(Keys.CONFIG_TREATMENT_B, "");
 			conf.treatmentNotes = prefs.getString(Keys.CONFIG_TREATMENT_NOTES, "");
 
 			conf.quesList = new ArrayList<String>();
 			for (int i = 0; ques.contains(Keys.QUES_TEXT + i); i++) {
 				conf.quesList.add(ques.getString(Keys.QUES_TEXT + i, ""));
 			}
 
 			return conf;
 		}
 
 		public ConfigData generateFromIntent(Intent intent) {
 
 			conf.patientName = intent.getStringExtra(Keys.CONFIG_PATIENT_NAME);
 			conf.doctorName = intent.getStringExtra(Keys.CONFIG_DOCTOR_NAME);
 			conf.doctorEmail = intent.getStringExtra(Keys.CONFIG_DOC);
 			conf.pharmEmail = intent.getStringExtra(Keys.CONFIG_PHARM);
 			conf.numberPeriods = intent.getIntExtra(Keys.CONFIG_NUMBER_PERIODS, 0);
 			conf.periodLength = intent.getIntExtra(Keys.CONFIG_PERIOD_LENGTH, 0);
 			conf.startDate = intent.getStringExtra(Keys.CONFIG_START);
 			conf.treatmentA = intent.getStringExtra(Keys.CONFIG_TREATMENT_A);
 			conf.treatmentB = intent.getStringExtra(Keys.CONFIG_TREATMENT_B);
 			conf.treatmentNotes = intent.getStringExtra(Keys.CONFIG_TREATMENT_NOTES);
 			conf.formBuilt = intent.getBooleanExtra(Keys.CONFIG_BUILT, false);
 			conf.quesList = intent.getStringArrayListExtra(Keys.CONFIG_QUESTION_LIST);
 
 			for (int i = 0; intent.hasExtra(Keys.CONFIG_TIME + i); i++) {
 				conf.timeList.add(intent.getStringExtra(Keys.CONFIG_TIME + i));
 			}
 			for (int i = 1; intent.hasExtra(Keys.CONFIG_DAY + i); i++) {
 				conf.dayList.add(intent.getBooleanExtra(Keys.CONFIG_DAY + i, false));
 			}
 
 			return conf;
 		}
 
 	}
 
 }
