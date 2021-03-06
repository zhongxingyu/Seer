 package com.droidplanner.dialogs;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.droidplanner.R;
 import com.droidplanner.drone.variables.waypoint;
 import com.droidplanner.polygon.GridBuilder;
 import com.droidplanner.polygon.Polygon;
 import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
 import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;
 import com.google.android.gms.maps.model.LatLng;
 
 public abstract class SurveyDialog implements DialogInterface.OnClickListener,
 		OnTextSeekBarChangedListner {
 	public abstract void onPolygonGenerated(List<waypoint> list);
 
 	private Context context;
 	private SeekBarWithText overlapView;
 	private SeekBarWithText angleView;
 	private SeekBarWithText altitudeView;
 	private SeekBarWithText sidelapView;
 	private TextView lenghtTextView;
 	private TextView areaTextView;
 	private TextView distanceTextView;
 	private TextView footprintTextView;
 	private TextView groundResolutionTextView;
 
 	private Polygon polygon;
 	private LatLng originPoint;
 
 	private SurveyData surveyData;
 
 	public void generateSurveyDialog(Polygon polygon, double defaultHatchAngle,
 			LatLng lastPoint, double defaultAltitude, Context context) {
 		this.polygon = polygon;
 		this.originPoint = lastPoint;
 		this.context = context;
 
 		if (checkIfPolygonIsValid(polygon)) {
 			Toast.makeText(context, "Invalid Polygon", Toast.LENGTH_SHORT)
 					.show();
 			return;
 		}
 
 		AlertDialog dialog = buildDialog(context);
 
 		surveyData = new SurveyData(defaultHatchAngle, defaultAltitude);
 		updateViews();
 		dialog.show();
 	}
 
 	@Override
 	public void onSeekBarChanged() {
 		surveyData.update(angleView.getValue(), altitudeView.getValue(),
 				overlapView.getValue(), sidelapView.getValue());
 		updateViews();
 	}
 
 	private void updateViews() {
 		angleView.setValue(surveyData.getAngle());
 		altitudeView.setValue(surveyData.getAltitude());
 		sidelapView.setValue(surveyData.getSidelap());
 		overlapView.setValue(surveyData.getOverlap());
 		lenghtTextView.setText(context.getString(R.string.lenght) + ": "
 				+ surveyData.getMissionLength() + " m");
 		areaTextView.setText(context.getString(R.string.area) + ": "
 				+ surveyData.getArea() + " deg");
 		distanceTextView.setText(context
 				.getString(R.string.distance_between_pictures)
 				+ ": "
 				+ surveyData.getDistanceBetweenPictures() + " m");
 		footprintTextView.setText(context.getString(R.string.footprint) + ": "
 				+ ((Double)surveyData.getLateralFootPrint()).intValue() + "x"
 				+ ((Double)surveyData.getLongitudinalFootPrint()).intValue() + " m");
 		groundResolutionTextView.setText(context
 				.getString(R.string.ground_resolution)
 				+ ": "
 				+ surveyData.getGroundResolution() + " cm²/pixel");
 	}
 
 	private boolean checkIfPolygonIsValid(Polygon polygon) {
 		return !polygon.isValid();
 	}
 
 	private AlertDialog buildDialog(Context context) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setTitle("Survey");
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.dialog_survey, null);
 		builder.setView(layout);
 		builder.setNegativeButton("Cancel", this).setPositiveButton("Ok", this);
 		AlertDialog dialog = builder.create();
 
 		angleView = (SeekBarWithText) layout.findViewById(R.id.angleView);
 		overlapView = (SeekBarWithText) layout.findViewById(R.id.overlapView);
 		sidelapView = (SeekBarWithText) layout.findViewById(R.id.sidelapView);
 		altitudeView = (SeekBarWithText) layout.findViewById(R.id.altitudeView);
 
 		areaTextView = (TextView) layout.findViewById(R.id.areaTextView);
 		lenghtTextView = (TextView) layout.findViewById(R.id.lenghtTextView);
 		footprintTextView = (TextView) layout
 				.findViewById(R.id.footprintTextView);
 		groundResolutionTextView = (TextView) layout
 				.findViewById(R.id.groundResolutionTextView);
 		distanceTextView = (TextView) layout
 				.findViewById(R.id.distanceTextView);
 
 		angleView.setOnChangedListner(this);
 		altitudeView.setOnChangedListner(this);
 		overlapView.setOnChangedListner(this);
 		sidelapView.setOnChangedListner(this);
 		return dialog;
 	}
 
 	@Override
 	public void onClick(DialogInterface arg0, int which) {
 		if (which == Dialog.BUTTON_POSITIVE) {
 			GridBuilder grid = new GridBuilder(polygon, surveyData.getAngle(),
 					surveyData.getLineDistance(), originPoint,
 					surveyData.getAltitude());
 			onPolygonGenerated(grid.hatchfill());
 		}
 	}
 
 }
