 package info.guardianproject.justpayphone.app.adapters;
 
 import info.guardianproject.justpayphone.R;
 import info.guardianproject.justpayphone.models.JPPWorkSummary;
 import info.guardianproject.justpayphone.utils.Constants.App;
 import info.guardianproject.justpayphone.utils.Constants.Forms;
 import info.guardianproject.justpayphone.utils.Constants.HomeActivityListener;
 import info.guardianproject.odkparser.utils.QD;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import org.witness.informacam.InformaCam;
 import org.witness.informacam.models.forms.IForm;
 import org.witness.informacam.models.media.ILog;
 import org.witness.informacam.models.media.IMedia;
 import org.witness.informacam.models.media.IRegion;
 import org.witness.informacam.storage.FormUtility;
 import org.witness.informacam.utils.Constants.App.Storage.Type;
 import org.witness.informacam.utils.TimeUtility;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 public class ILogGallery extends BaseAdapter {
 	InformaCam informaCam;
 	List<ILog> iLogs;
 	Activity a;
 	private RadioGroup lunchTakenProxy;
 	private RadioButton lunchTakenProxyYes;
 	private RadioButton lunchTakenProxyNo;
 	private EditText lunchMinutesProxy;
 	
 	private final static String LOG = App.Home.LOG;
 	
 	public ILogGallery(List<ILog> iLogs, Activity a) {
 		this.iLogs = iLogs;
 		this.a = a;
 		informaCam = InformaCam.getInstance();
 		
 		lunchTakenProxy = new RadioGroup(a);
 		lunchTakenProxyYes = new RadioButton(a);
 		lunchTakenProxyNo = new RadioButton(a);
 		lunchTakenProxy.addView(lunchTakenProxyYes);
 		lunchTakenProxy.addView(lunchTakenProxyNo);
 		lunchMinutesProxy = new EditText(a);
 		lunchMinutesProxy.setText(a.getString(R.string.x_minutes, 0));
 	}
 
 	@Override
 	public int getCount() {
 		return iLogs.size();
 	}
 
 
 
 	@Override
 	public Object getItem(int position) {
 		return iLogs.get(position);
 	}
 
 
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 //		convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_ilog_gallery_parent, null);
 //		ILog iLog = iLogs.get(position);
 //		
 //		TextView date = (TextView) convertView.findViewById(R.id.ilog_date);
 //		date.setText(TimeUtility.millisecondsToDayOnly(iLog.startTime));
 //		
 //		LinearLayout imagesAndVideo = (LinearLayout) convertView.findViewById(R.id.ilog_images_and_video);
 //		for(String l : iLog.attachedMedia) {
 //			ImageView iv = new ImageView(parent.getContext());
 //			IMedia m = informaCam.mediaManifest.getById(l);
 //			byte[] bBytes = informaCam.ioService.getBytes(m.bitmapThumb, Type.IOCIPHER);
 //			Bitmap b = BitmapFactory.decodeByteArray(bBytes, 0, bBytes.length);
 //			
 //			LinearLayout.LayoutParams lp = new LayoutParams(90, 90);
 //			lp.setMargins(0, 0, 10, 0);
 //			
 //			iv.setLayoutParams(lp);
 //			iv.setImageBitmap(b);
 //			
 //			imagesAndVideo.addView(iv);
 //		}
 		
 		if (convertView == null)
 			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_day_item, parent, false);
 		
 		ILog iLog = iLogs.get(position);
 	
 		TextView tv = (TextView) convertView.findViewById(R.id.tvTimeDate);
 		
 		Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(iLog.startTime);
 		
 		tv.setText(String.valueOf(cal.get(Calendar.DATE)));
 
 		tv = (TextView) convertView.findViewById(R.id.tvTimeWorked);
 		tv.setText(getWorkDisplayString(iLog));
 
 		tv = (TextView) convertView.findViewById(R.id.tvTimeLunch);
 		tv.setText(getLunchDisplayString(iLog));
 		
 		return convertView;
 	}
 
 	public String getWorkDisplayString(ILog iLog)
 	{
		long msWorked = iLog.endTime - iLog.startTime;
 		float hWorked = (msWorked / 3600000);
 	    if(hWorked == (int) hWorked)
 	        return String.format(Locale.getDefault(), "%d", (int)hWorked);
 	    else
	        return String.format(Locale.getDefault(), "%.1fs", hWorked);
 	}
 	
 	private String getLunchDisplayString(ILog iLog)
 	{
 		String lunchTaken = a.getString(R.string.time_lunch_no_lunch);
 		List<IForm> forms = iLog.getForms(a);
 	
 		for(IForm form : forms) {
 			if(form.namespace.equals(Forms.LUNCH_QUESTIONNAIRE)) {
 				form.associate(lunchTakenProxy, Forms.LunchQuestionnaire.LUNCH_TAKEN);
 				form.associate(lunchMinutesProxy, Forms.LunchQuestionnaire.LUNCH_MINUTES);
 				
 				if (this.lunchTakenProxyYes.isChecked())
 				{
 					Integer lunchMinutes = Integer.valueOf(lunchMinutesProxy.getText().toString());
 					lunchTaken = a.getString(R.string.time_lunch_minutes, lunchMinutes);
 				}
 				break;
 			}
 		}
 		return lunchTaken;
 	}
 	
 }
