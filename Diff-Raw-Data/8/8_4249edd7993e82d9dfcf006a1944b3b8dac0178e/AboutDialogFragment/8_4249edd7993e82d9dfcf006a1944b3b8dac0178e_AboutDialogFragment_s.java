 package ws.vannen.fiver.ui;
 
 import ws.vannen.fiver.R;
 import ws.vannen.fiver.app.CoreApp;
 import android.app.Dialog;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
 
 public class AboutDialogFragment extends SherlockDialogFragment {
 	
 	private View view = null;
 	private Dialog dialog = null;
 	private TextView textViewAboutTitle = null;
 	private TextView textViewAboutDetails = null;
 	private TextView textViewAboutMoreDetails = null;
 	private TextView textViewAboutLinkGithub = null;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		view = inflater.inflate(R.layout.dialogfragment_about, container, false);
 		
 		if(dialog == null){
 			dialog = getDialog();
 			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		}
 		
 		return view;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle bundle) {
 		super.onActivityCreated(bundle);
 	
 		textViewAboutTitle = (TextView)view.findViewById(R.id.textViewAboutTitle);
 		textViewAboutDetails = (TextView)view.findViewById(R.id.textViewAboutDetails);
 		textViewAboutMoreDetails = (TextView)view.findViewById(R.id.textViewAboutMoreDetails);
 		textViewAboutLinkGithub = (TextView)view.findViewById(R.id.textViewLinkGithub);
 		
 		
 		if(MainFragmentActivity.robotoRegularFontTypeface != null){
 			textViewAboutTitle.setTypeface(MainFragmentActivity.robotoRegularFontTypeface);
 		}
 		
 		if(MainFragmentActivity.robotoFontTypeface != null){
 			textViewAboutDetails.setTypeface(MainFragmentActivity.robotoFontTypeface);
 			textViewAboutMoreDetails.setTypeface(MainFragmentActivity.robotoFontTypeface);
 			textViewAboutLinkGithub.setTypeface(MainFragmentActivity.robotoFontTypeface);
 		}
 		
 		
 		textViewAboutMoreDetails.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_VIEW);
 				intent.setData(Uri.parse(CoreApp.My_WEBSITE));
 				startActivity(intent);
 			}
 		});
 		
 		
 		textViewAboutLinkGithub.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_VIEW);
 				intent.setData(Uri.parse(CoreApp.GITHUB_LINK));
 				startActivity(intent);
 			}
 		});
 		
 	}
 
 }
