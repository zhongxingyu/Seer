 package fr.gcastel.freeboxV6GeekIncDownloader;
 
 import java.util.List;
 
 import fr.gcastel.freeboxV6GeekIncDownloader.services.FreeboxDownloaderService;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.os.AsyncTask;
 
 public class ListPodcastAdapter extends BaseAdapter {
 
 	private final ProgressDialog dialog;
 	private final List<PodcastElement> elements;
 	private final LayoutInflater layoutInflater;
 	private FreeboxDownloaderService fbxService;
 	private final Context context;
 
 	public ListPodcastAdapter(Context inContext, List<PodcastElement> elements) {
 		super();
 		this.elements = elements;
 		context = inContext;
 		layoutInflater = LayoutInflater.from(context);
 		dialog = new ProgressDialog(context);
 		dialog.setCancelable(true);
 		dialog.setMessage("Connexion  la freebox");
 		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		fbxService = new FreeboxDownloaderService(
 				context.getString(R.string.freeboxURL), context, dialog);
 	}
 
 	@Override
 	public int getCount() {
 		if (elements == null) {
 			return 0;
 		}
 		return elements.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return elements.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	
 	private void askForPassword(final String url) {
 		LayoutInflater infalter = LayoutInflater.from(context);
 		final View textEntryView = infalter.inflate(R.layout.password_dialog,
 				null);
 		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
 		dialogBuilder.setTitle("Saisissez le mot de passe freebox");
 		dialogBuilder.setView(textEntryView);
 		dialogBuilder.setPositiveButton("Ok",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
						EditText passField = (EditText) textEntryView.findViewById(R.id.passwordField); 
						if (fbxService.getStatus() == AsyncTask.Status.PENDING) {
                                                  fbxService.execute(url, passField.getText().toString());
						}
 					}
 				});
 		dialogBuilder.setNegativeButton("Annuler", null);
 	  dialogBuilder.show();		
 	}
 
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent) {
 		TextView title;
 		if (convertView == null) {
 			convertView = layoutInflater.inflate(R.layout.list_item, parent,
 					false);
 			convertView.setTag(convertView.findViewById(R.id.titleItem));
 		}
 		title = (TextView) convertView.getTag();
 
 		title.setText(elements.get(position).getTitre());
 		title.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Une tche ne peut tre excute qu'une fois
 				if (fbxService.getStatus() == AsyncTask.Status.FINISHED) {
 					fbxService = new FreeboxDownloaderService(context
 							.getString(R.string.freeboxURL), context, dialog);
 				}
 				if (fbxService.getStatus() == AsyncTask.Status.PENDING) {
 					if (fbxService.isConnectedViaWifi()) {
 					  askForPassword(elements.get(position).getUrl());
 					} else {
 			      Toast.makeText(context, "Vous devez tre connect en Wifi pour accder  la freebox", Toast.LENGTH_SHORT).show();
 					}
 				}
 			}
 		});
 		
 		return convertView;
 	}
 }
