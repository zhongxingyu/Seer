 package net.trajano.gasprices;
 
 import java.io.IOException;
 
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class UpdateTask extends AsyncTask<Void, Void, Exception> {
 	private final Context context;
 
 	public UpdateTask(final Context context) {
 		this.context = context;
 	}
 
 	@Override
 	protected Exception doInBackground(final Void... params) {
 		final PreferenceAdaptor preferences = new PreferenceAdaptor(context);
 		final PreferenceAdaptorEditor editor = preferences.edit();
 
 		try {
 			final JSONObject data = GetDataUtil.getGasPricesDataFromInternet();
 			editor.setJsonData(data);
 			editor.setLastUpdatedToNow();
 			return null;
 		} catch (final IOException e) {
 			// TODO fix this is yucky
 			Log.e("GasPrices", e.getMessage() + " and cry");
 			try {
 				editor.setLastError(e.getMessage(),
 						GetDataUtil.getRawGasPricesDataFromInternet());
 			} catch (final IOException e2) {
 				editor.setLastError(e.getMessage(), "");
 				return e2;
 			}
 			return e;
 		} finally {
 			editor.apply();
 		}
 	}
 
 	@Override
 	protected void onPostExecute(final Exception result) {
 		if (result == null) {
 			final AppWidgetManager widgetManager = AppWidgetManager
 					.getInstance(context);
 			final ComponentName widgetComponent = new ComponentName(context,
 					GasPricesWidgetProvider.class);
 			final int[] widgetIds = widgetManager
 					.getAppWidgetIds(widgetComponent);
 			if (widgetIds.length > 0) {
 				final Intent update = new Intent();
 				update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
 				update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
 				context.sendBroadcast(update);
 			}
 		} else {
 			new AlertDialog.Builder(context)
 					.setTitle(R.string.error)
 					.setMessage(
 							context.getString(R.string.problem_loading_format,
 									result.getLocalizedMessage())).show();
 		}
 	}
 
 	@Override
 	protected void onPreExecute() {
 		final PreferenceAdaptor preferences = new PreferenceAdaptor(context);
 		final PreferenceAdaptorEditor editor = preferences.edit();
 		editor.removeLastError();
 		editor.apply();
 	}
 }
