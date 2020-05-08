 package nl.q42.hue2.dialogs;
 
 import nl.q42.hue2.PHUtilitiesImpl;
 import nl.q42.hue2.R;
 import nl.q42.hue2.activities.LightsActivity;
 import nl.q42.hue2.models.Preset;
 import nl.q42.javahueapi.models.Light;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 
 public class PresetRemoveDialog extends DialogFragment {
 	public static PresetRemoveDialog newInstance(Preset preset, Light light) {
 		PresetRemoveDialog dialog = new PresetRemoveDialog();
 		
 		Bundle args = new Bundle();
 		args.putSerializable("preset", preset);
 		args.putSerializable("light", light);
 		dialog.setArguments(args);
 		
 		return dialog;
 	}
 	
 	public static PresetRemoveDialog newInstance(Preset preset) {
 		PresetRemoveDialog dialog = new PresetRemoveDialog();
 		
 		Bundle args = new Bundle();
 		args.putSerializable("preset", preset);
 		dialog.setArguments(args);
 		
 		return dialog;
 	}
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		final Preset preset = (Preset) getArguments().getSerializable("preset");
 		final Light light = (Light) getArguments().getSerializable("light");
 		
 		int size = (int) (getResources().getDisplayMetrics().density * 64.0f);
 		Bitmap bm = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		bm.eraseColor(PHUtilitiesImpl.colorFromXY(preset.xy, light != null ? light.modelid : null));
 		
 		return new AlertDialog.Builder(getActivity())
 			.setTitle(R.string.dialog_remove_preset_title)
 			.setMessage(R.string.dialog_remove_preset)
 			.setPositiveButton(R.string.dialog_yes, new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					((LightsActivity) getActivity()).removeColorPreset(preset);
 				}
 			})
 			.setIcon(new BitmapDrawable(bm))
 			.setNegativeButton(R.string.dialog_no, new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 				}
 			})
 			.create();
 	}
 }
