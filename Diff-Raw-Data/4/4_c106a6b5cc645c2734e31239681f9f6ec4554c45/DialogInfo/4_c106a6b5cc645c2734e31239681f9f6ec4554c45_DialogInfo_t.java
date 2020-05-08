 package com.voc4u.activity;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.TextView;
 
 import com.voc4u.R;
 import com.voc4u.setting.CommonSetting;
 
 public class DialogInfo
 {
 	public static final String TYPE_INIT = "init";
 	public static final String TYPE_DICTIONARY = "dictionary";
 	public static final String TYPE_TRAIN = "train";
 	
 	public static Dialog create(final Context context)
 	{
 	
 		final Dialog dialog = new Dialog(context);
 		dialog.setContentView(R.layout.dialog_info);
 		
 		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 		lp.copyFrom(dialog.getWindow().getAttributes());
 		lp.width = WindowManager.LayoutParams.FILL_PARENT;
 		// lp.height = WindowManager.LayoutParams.FILL_PARENT;
 		// dialog.show();
 		dialog.getWindow().setAttributes(lp);
 		
 		//setup(context, type, dialog);
 		
 		return dialog;
 	}
 
 	public static void setup(final Context context, final String type, final Dialog dialog)
 	{
 		Resources r = context.getResources();
		int titleid = r.getIdentifier("form_" + type, "string", context.getPackageName());
		int textid = r.getIdentifier("information_" + type, "string", context.getPackageName());
 		
 		dialog.setTitle(titleid);
 		dialog.setOnCancelListener(new Dialog.OnCancelListener()
 		{
 			@Override
 			public void onCancel(DialogInterface dialog)
 			{
 				CommonSetting.store(context);	
 			}
 		});
 		
 		final TextView tvText = (TextView) dialog.findViewById(R.id.text);
 		tvText.setText(textid);
 
 		Button btnAdd = (Button) dialog.findViewById(R.id.btnCancel);
 		btnAdd.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				dialog.dismiss();
 				CommonSetting.store(context);	
 			}
 		});
 		
 		final CheckBox chkNSM = (CheckBox) dialog.findViewById(R.id.chkNoShowMore);
 		chkNSM.setChecked( GetChecked(type));
 		chkNSM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
 		{
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
 				SetupCommonSetting(type, isChecked);
 			}
 		});
 	}
 
 	public static boolean GetChecked(final String type)
 	{
 		if(type.contentEquals(TYPE_INIT))
 		{
 			return CommonSetting.NSMInit;
 		}
 		else if(type.contentEquals(TYPE_DICTIONARY))
 		{
 			return CommonSetting.NSMDictionary;
 		}
 		else if(type.contentEquals(TYPE_TRAIN))
 		{
 			return CommonSetting.NSMTrain;
 		}
 		else
 			// no show dialog which haven't 
 			// common setting value
 			return true;
 	}
 	
 	private static void SetupCommonSetting(final String type, boolean isChecked)
 	{
 		if(type.contentEquals(TYPE_INIT))
 		{
 			CommonSetting.NSMInit = isChecked;
 		}
 		else if(type.contentEquals(TYPE_DICTIONARY))
 		{
 			CommonSetting.NSMDictionary = isChecked;
 		}
 		else if(type.contentEquals(TYPE_TRAIN))
 		{
 			CommonSetting.NSMTrain = isChecked;
 		}
 	}
 }
