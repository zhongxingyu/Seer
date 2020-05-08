 package sk.gista.medobs.view;
 
 import java.util.List;
 
 import sk.gista.medobs.R;
 import sk.gista.medobs.Reservation;
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 
 public class ReservationAdapter extends ArrayAdapter<Reservation>{
 
 	private static final int[] colors = {
 		0, // empty
 		Color.parseColor("#939DAC"), //disabled
 		//Color.parseColor("#80B3FF"), //enabled
		Color.parseColor("#AACCFF"), //enabled
 		Color.parseColor("#71C837"), //booked
 		Color.parseColor("#C83737")  //in held
 	};
 	private LayoutInflater inflater;
 	
 	public ReservationAdapter(Context context, List<Reservation> reservations) {
 		super(context, R.layout.reservation_item, reservations);
 		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		if (view == null) {
 			view = inflater.inflate(R.layout.reservation_item, parent, false);
 		}
 		TextView timeText = (TextView) view.findViewById(R.id.time_text);
 		TextView patientText = (TextView) view.findViewById(R.id.patient);
 		Reservation reservation = getItem(position);
 		timeText.setText(reservation.getTime());
 		String phoneNum = reservation.getPatientPhoneNumber();
 		if (phoneNum.length() > 0) {
 			patientText.setText(reservation.getpatient()+" ("+reservation.getPatientPhoneNumber()+")");
 		} else {
 			patientText.setText(reservation.getpatient());
 		}
 		
		System.out.println(reservation.getStatus().numCode);
 		view.setBackgroundColor(colors[reservation.getStatus().numCode]);
 		return view;
 	}
 }
