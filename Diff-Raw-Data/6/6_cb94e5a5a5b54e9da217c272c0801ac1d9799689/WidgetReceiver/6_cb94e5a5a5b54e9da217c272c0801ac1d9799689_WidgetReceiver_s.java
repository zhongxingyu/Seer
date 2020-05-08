 package com.obriand.android_basicwidget;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 //Tuto : http://sberfini.developpez.com/tutoriaux/android/appwidget/
 
 public class WidgetReceiver extends AppWidgetProvider 
 {
 	
 	public static final String ACTION_LANCER_APPLICATION = "com.obriand.android_basicwidget.LANCER_APPLICATION"; 
 	
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
 	{
 		final int N = appWidgetIds.length;
 	
 		// Pour chaque AppWidget MonWidgetDeveloppez (n'oubliez pas qu'on peut en ajouter tant qu'on veut), on les met a jour :
 		for (int i = 0; i < N; i++) 
 		{
 			int appWidgetId = appWidgetIds[i];
 			updateAppWidget(context, appWidgetManager, appWidgetId);
 		}
 	}
 
 	// Cette methode est entierement libre, a vous de la modifier comme bon vous semble. Voici toutefois une base minimaliste
 	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) 
 	{
 		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main); // On recupere les Views de notre layout
 		views.setTextViewText(R.id.content, "Hello Developpez !"); // On peut agir sur ces vues
 		appWidgetManager.updateAppWidget(appWidgetId, views); // On met ensuite a jour l'affichage du widget
 		
 		// On prpare un intent a lancer lors d'un clic
 		Intent intent = new Intent(context, WidgetReceiver.class);
 		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 		intent.setAction(ACTION_LANCER_APPLICATION); // Je cree ici ma propre action
 
		// On lie l'intent  l'action
 		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
 		views.setOnClickPendingIntent(R.id.text_layout, pendingIntent); // L'id de la view qui reagira au clic sur le widget.
 		appWidgetManager.updateAppWidget(appWidgetId, views);
 	}
 	
 	@Override
 	public void onReceive(Context context, Intent intent) 
 	{
 	    super.onReceive(context, intent);
 	    Toast.makeText(context, "onReceive", Toast.LENGTH_LONG).show(); 
 	    if (intent.getAction().equals(ACTION_LANCER_APPLICATION))
 	    {
 	        lancerActivityPrincipale(context);
 	    }
 	}
 
 	protected void lancerActivityPrincipale(Context context) 
 	{
 	    Intent i = new Intent(context, MainActivity.class);
 	    context.startActivity(i);
 	}
 
 }
 
