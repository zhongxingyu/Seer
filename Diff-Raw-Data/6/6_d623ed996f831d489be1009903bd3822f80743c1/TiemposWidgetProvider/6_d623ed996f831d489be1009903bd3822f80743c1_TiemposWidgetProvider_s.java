 /**
  *  TiempoBus - Informacion sobre tiempos de paso de autobuses en Alicante
  *  Copyright (C) 2012 Alberto Montiel
  *
  *  based on the Copyright (C) 2011 The Android Open Source Project
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package alberapps.android.tiempobuswidgets;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import alberapps.android.tiempobuswidgets.tasks.LoadTiemposLineaParadaAsyncTask;
 import alberapps.android.tiempobuswidgets.tasks.LoadTiemposLineaParadaAsyncTask.LoadTiemposLineaParadaAsyncTaskResponder;
 import alberapps.java.datos.Datos;
 import alberapps.java.datos.GestionarDatos;
 import alberapps.java.tam.BusLlegada;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.ContentObserver;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 /**
  * Our data observer just notifies an update for all widgets when it detects a
  * change.
  */
 class TiemposDataProviderObserver extends ContentObserver {
 	private AppWidgetManager mAppWidgetManager;
 	private ComponentName mComponentName;
 
 	TiemposDataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
 		super(h);
 		mAppWidgetManager = mgr;
 		mComponentName = cn;
 	}
 
 	@Override
 	public void onChange(boolean selfChange) {
 		// The data has changed, so notify the widget that the collection view
 		// needs to be updated.
 		// In response, the factory's onDataSetChanged() will be called which
 		// will requery the
 		// cursor for the new data.
 		mAppWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.tiempos_list);
 	}
 }
 
 /**
  * The widget's AppWidgetProvider.
  */
 public class TiemposWidgetProvider extends AppWidgetProvider {
 	public static String CLICK_ACTION = "alberapps.android.tiempobuswidgets.CLICK";
 	public static String REFRESH_ACTION = "alberapps.android.tiempobuswidgets.REFRESH";
 	public static String DATO_ID = "alberapps.android.tiempobuswidgets.dato";
 
 	private static HandlerThread sWorkerThread;
 	private static Handler sWorkerQueue;
 	private static TiemposDataProviderObserver sDataObserver;
 
 	private boolean mIsLargeLayout = true;
 
 	private List<BusLlegada> listaTiempos;
 
 	SharedPreferences preferencias = null;
 
 	public TiemposWidgetProvider() {
 		// Start the worker thread
 		sWorkerThread = new HandlerThread("WeatherWidgetProvider-worker");
 		sWorkerThread.start();
 		sWorkerQueue = new Handler(sWorkerThread.getLooper());
 	}
 
 	// XXX: clear the worker queue if we are destroyed?
 
 	@Override
 	public void onEnabled(Context context) {
 		// Register for external updates to the data to trigger an update of the
 		// widget. When using
 		// content providers, the data is often updated via a background
 		// service, or in response to
 		// user interaction in the main app. To ensure that the widget always
 		// reflects the current
 		// state of the data, we must listen for changes and update ourselves
 		// accordingly.
 		final ContentResolver r = context.getContentResolver();
 		if (sDataObserver == null) {
 			final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
 			final ComponentName cn = new ComponentName(context, TiemposWidgetProvider.class);
 			sDataObserver = new TiemposDataProviderObserver(mgr, cn, sWorkerQueue);
 			r.registerContentObserver(TiemposDataProvider.CONTENT_URI, true, sDataObserver);
 		}
 
 		onReceive(context, new Intent().setAction(REFRESH_ACTION));
 
 	}
 
 	/**
 	 * Lanza la subactivididad de preferencias
 	 */
 	private void showPreferencias(Context ctx) {
 
 		final Context context = ctx;
 
 		Intent i = new Intent(context, PreferencesFromXml.class);
 
 		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
 		context.startActivity(i);
 
 	}
 
 	@Override
 	public void onReceive(Context ctx, final Intent intent) {
 		final String action = intent.getAction();
 
 		final Context context = ctx;
 
 		if (action.equals(REFRESH_ACTION)) {
 
 			// BroadcastReceivers have a limited amount of time to do work, so
 			// for this sample, we
 			// are triggering an update of the data on another thread. In
 			// practice, this update
 			// can be triggered from a background service, or perhaps as a
 			// result of user actions
 			// inside the main application.
 
 			actualizar(context, intent);
 
 		} else if (action.equals(CLICK_ACTION)) {
 			// Show a toast
 			final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
 			final int dato = intent.getIntExtra(DATO_ID, -1);
 
 			Log.d("tag", " eliminar: " + dato);
 
 			Intent i = new Intent(context, EliminarDatoActivity.class);
 			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			i.putExtra("DATO", dato);
 			context.startActivity(i);
 
 			// actualizar(context, intent);
 
 		}
 
 		super.onReceive(ctx, intent);
 	}
 
 	/**
 	 * Actualizar el contenido
 	 * 
 	 * @param context
 	 * @param intent
 	 */
 	public void actualizar(final Context context, Intent intent) {
 
 		preferencias = context.getSharedPreferences("datoswidget", Context.MODE_MULTI_PROCESS);
 
 		listaTiempos = new ArrayList<BusLlegada>();
 
 		LoadTiemposLineaParadaAsyncTaskResponder loadTiemposLineaParadaAsyncTaskResponder = new LoadTiemposLineaParadaAsyncTaskResponder() {
 			public void tiemposLoaded(List<BusLlegada> tiempos) {
 
 				if (tiempos != null) {
 
 					listaTiempos = tiempos;
 
 					sWorkerQueue.removeMessages(0);
 					sWorkerQueue.post(new Runnable() {
 						@Override
 						public void run() {
 							final ContentResolver r = context.getContentResolver();
 							// final Cursor c =
 							// r.query(TiemposDataProvider.CONTENT_URI, null,
 							// null,
 							// null, null);
 							// final int count = c.getCount();
 
 							// We disable the data changed observer temporarily
 							// since
 							// each of the updates
 							// will trigger an onChange() in our data observer.
 
 							try {
 								r.unregisterContentObserver(sDataObserver);
 							} catch (Exception e) {
 
 							}
 
 							r.delete(TiemposDataProvider.CONTENT_URI, null, null);
 
 							for (int i = 0; (listaTiempos != null && i < listaTiempos.size()); ++i) {
 
 								final Uri uri = ContentUris.withAppendedId(TiemposDataProvider.CONTENT_URI, i);
 
 								final ContentValues values = new ContentValues();
 
 								// Linea
 								values.put(TiemposDataProvider.Columns.LINEA, listaTiempos.get(i).getLinea());
 
 								// Tiempo
 								values.put(TiemposDataProvider.Columns.TIEMPO, listaTiempos.get(i).getProximo());
 
 								// Destino
 								values.put(TiemposDataProvider.Columns.DESTINO, listaTiempos.get(i).getDestino());
 
 								// Parada
 								values.put(TiemposDataProvider.Columns.PARADA, listaTiempos.get(i).getParada());
 
 								r.insert(uri, values);
 							}
 							r.registerContentObserver(TiemposDataProvider.CONTENT_URI, true, sDataObserver);
 
 							final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
 							final ComponentName cn = new ComponentName(context, TiemposWidgetProvider.class);
 							mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.tiempos_list);
 						}
 					});
 
 					Toast.makeText(context, context.getString(R.string.aviso_recarga_completa), Toast.LENGTH_SHORT).show();
 
 				} else {
 
					Toast.makeText(context, context.getString(R.string.error_tiempos), Toast.LENGTH_LONG).show();
 
 				}
 			}
 
 		};
 
 		// Control de disponibilidad de conexion
 		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 
 			List<Datos> lineasParada = GestionarDatos.listaDatos(preferencias.getString("lineas_parada", ""));
 
 			new LoadTiemposLineaParadaAsyncTask(loadTiemposLineaParadaAsyncTaskResponder).execute(lineasParada);
 
 		} else {
 			Toast.makeText(context.getApplicationContext(), context.getString(R.string.error_red), Toast.LENGTH_LONG).show();
 		}
 
 		final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
 
 		// Cambiar hora actualizacion
 		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
 		final Calendar c = Calendar.getInstance();
 		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 		String updated = df.format(c.getTime()).toString();
 		rv.setTextViewText(R.id.hora_act, updated);
 
 		final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
 		final ComponentName cn = new ComponentName(context, TiemposWidgetProvider.class);
 		mgr.updateAppWidget(cn, rv);
 
 		Toast.makeText(context, context.getString(R.string.aviso_recarga), Toast.LENGTH_SHORT).show();
 
 	}
 
 	private RemoteViews buildLayout(Context context, int appWidgetId, boolean largeLayout) {
 		RemoteViews rv;
 		if (largeLayout) {
 			// Specify the service to provide data for the collection widget.
 			// Note that we need to
 			// embed the appWidgetId via the data otherwise it will be ignored.
 			final Intent intent = new Intent(context, TiemposWidgetService.class);
 			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
 			rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
 			rv.setRemoteAdapter(appWidgetId, R.id.tiempos_list, intent);
 
 			// Set the empty view to be displayed if the collection is empty. It
 			// must be a sibling
 			// view of the collection view.
 			rv.setEmptyView(R.id.tiempos_list, R.id.empty_view);
 
 			// Bind a click listener template for the contents of the weather
 			// list. Note that we
 			// need to update the intent's data if we set an extra, since the
 			// extras will be
 			// ignored otherwise.
 			final Intent onClickIntent = new Intent(context, TiemposWidgetProvider.class);
 			onClickIntent.setAction(TiemposWidgetProvider.CLICK_ACTION);
 			onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 			onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
 			final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 			rv.setPendingIntentTemplate(R.id.tiempos_list, onClickPendingIntent);
 
 			// Bind the click intent for the refresh button on the widget
 			final Intent refreshIntent = new Intent(context, TiemposWidgetProvider.class);
 			refreshIntent.setAction(TiemposWidgetProvider.REFRESH_ACTION);
 			final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 			rv.setOnClickPendingIntent(R.id.refresh, refreshPendingIntent);
 
 			// Restore the minimal header
 			rv.setTextViewText(R.id.titulo, context.getString(R.string.app_name));
 
 		} else {
 			rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small);
 			/*
 			 * // Update the header to reflect the weather for "today" Cursor c
 			 * =
 			 * context.getContentResolver().query(TiemposDataProvider.CONTENT_URI
 			 * , null, null, null, null); if (c.moveToPosition(0)) { int
 			 * tempColIndex =
 			 * c.getColumnIndex(TiemposDataProvider.Columns.TIEMPO); int temp =
 			 * c.getInt(tempColIndex); String formatStr =
 			 * context.getResources().getString(R.string.header_format_string);
 			 * String header = String.format(formatStr, temp, "reduce");
 			 * rv.setTextViewText(R.id.titulo, header); } c.close();
 			 */
 		}
 		return rv;
 	}
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
 		// Update each of the widgets with the remote adapter
 		for (int i = 0; i < appWidgetIds.length; ++i) {
 			RemoteViews layout = buildLayout(context, appWidgetIds[i], mIsLargeLayout);
 			appWidgetManager.updateAppWidget(appWidgetIds[i], layout);
 		}
 
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 	}
 
 	@Override
 	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
 
 		/*
 		 * int minWidth =
 		 * newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH); int
 		 * maxWidth =
 		 * newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH); int
 		 * minHeight =
 		 * newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT); int
 		 * maxHeight =
 		 * newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
 		 * 
 		 * RemoteViews layout; if (minHeight < 200) { mIsLargeLayout = false; }
 		 * else { mIsLargeLayout = true; } layout = buildLayout(context,
 		 * appWidgetId, mIsLargeLayout);
 		 * appWidgetManager.updateAppWidget(appWidgetId, layout);
 		 */
 	}
 }
