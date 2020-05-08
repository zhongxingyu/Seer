 ﻿package net.mabako.zwickau.mensa;
 
 import java.util.Calendar;
 import java.util.List;
 import android.app.ListActivity;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.SimpleAdapter;
 
 /**
  * Die Hauptklasse unserer MensaApp.
  * 
  * @author Marcus Bauer (mabako@gmail.com)
  */
 public class MensaActivity extends ListActivity {
 	/** Instanz dieser Aktivität. */
 	private static MensaActivity instance;
 
 	/** Heutiger Wochentag. */
 	private int today;
 
 	/** zu nutzende Mensa. */
 	private Mensa mensa = Mensa.RING;
 
 	/** Menü */
 	private MenuHelper menu;
 
 	/**
 	 * Berechnet den heutigen Tag, erstellt das Menü und lädt den Speiseplan.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		instance = this;
 		
 		// Falls irgendwas gespeichert
 		SharedPreferences pref = getPreferences(0);
 		String m = pref.getString("mensa", null);
 		if(m != null)
 		{
 			try
 			{
 				mensa = Mensa.valueOf(m);
 			}
 			catch( IllegalArgumentException e )
 			{
 			}
 		}
 
 		// Tag ausrechnen, der angezeigt werden soll.
 		today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
 		boolean naechsteWoche = false;
 		if (today == Calendar.SATURDAY || today == Calendar.SUNDAY) {
			today = 7 + Calendar.MONDAY;
 			naechsteWoche = true;
 		}
 
 		// Menü erstellen.
 		if (menu == null) {
 			menu = MenuHelper.getInstance();
 		}
 
 		loadMensa(naechsteWoche);
 	}
 	
 	/**
 	 * Lädt die Mensa bzw. den Plan.
 	 * 
 	 * @param naechsteWoche
 	 */
 	private void loadMensa(boolean naechsteWoche) {
 		if (!mensa.hasAnyFood(naechsteWoche)) {
 			// Mensa-Webseite laden.
 			loadPlan(naechsteWoche, false);
 		} else {
 			// Schon ein Plan da, nehmen wir doch den.
 			update(naechsteWoche);
 		}
 	}
 
 	/**
 	 * Liefert diese Activity zurück.
 	 * 
 	 * @return
 	 */
 	public static MensaActivity getInstance() {
 		return instance;
 	}
 
 	/**
 	 * Lädt einen Mensaplan
 	 * 
 	 * @param naechsteWoche
 	 * @param background
 	 */
 	private void loadPlan(boolean naechsteWoche, boolean background) {
 		new MensaTask(mensa, naechsteWoche, background).execute();
 	}
 
 	/**
 	 * Aktualisiert die Liste.
 	 * 
 	 * @param naechsteWoche
 	 */
 	public void update(boolean naechsteWoche) {
		showDay(today);
 
 		// Noch keine Einträge für nächste Woche geladen, also tun wir das.
 		if (!naechsteWoche && !mensa.hasAnyFood(true)) {
			loadPlan(true, true);
 		}
 	}
 
 	/**
 	 * Zeigt den Speiseplan für einen Tag an.
 	 * 
 	 * @param day
 	 */
 	public void showDay(int day) {
 		// Im Titel den Tag anzeigen
 		setTitle(getNameOfDay(day) + ", " + mensa.getName());
 		menu.updateTitle(day);
 
 		// Liste mit Essen anzeigen.
 		List<Essen> essen = mensa.getPlan().get(day);
 		SimpleAdapter adapter = new SimpleAdapter(this, essen, android.R.layout.simple_list_item_2, new String[] { Essen.TEXT, Essen.TEXT2 }, new int[] { android.R.id.text1, android.R.id.text2 });
 		setListAdapter(adapter);
 	}
 
 	/**
 	 * Liefert den heutigen (relativen) Tag zurück.
 	 * 
 	 * 2-6 entspricht Montag bis Freitag der aktuellen Woche, 9-13 entspricht
 	 * Montag bis Freitag der nächsten Woche.
 	 * 
 	 * @return heutiger Tag.
 	 */
 	public int getToday() {
 		return instance.today;
 	}
 
 	/**
 	 * Liefert die aktuelle Mensa zurück.
 	 * 
 	 * @return
 	 */
 	public Mensa getMensa() {
 		return instance.mensa;
 	}
 
 	/**
 	 * Stellt ein Standard-Android-Menü bereit, welches mittels Menütaste
 	 * erreichbar ist.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		for (int i = today; i <= 7 + Calendar.FRIDAY; ++i) {
 			if (i % 7 > Calendar.SUNDAY) {
 				menu.add(1, i, i, getNameOfDay(i));
 			}
 		}
 
 		for (Mensa mensa : Mensa.values()) {
 			menu.add(2, mensa.hashCode(), 50, mensa.getName());
 		}
 		return true;
 	}
 
 	/**
 	 * Handler für das Anzeigen des Standard-Android-Menüs.
 	 */
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// Die Mensa verstecken, welche aktuell angezeigt wird.
 		for (Mensa mensa : Mensa.values()) {
 			menu.findItem(mensa.hashCode()).setVisible(this.mensa != mensa);
 		}
 		return true;
 	}
 
 	/**
 	 * Handler für das Drücken des Standard-Android-Menüs.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getGroupId()) {
 		case 1:
 			showDay(item.getItemId());
 			return true;
 		case 2:
 			for (Mensa mensa : Mensa.values()) {
 				if (item.getItemId() == mensa.hashCode()) {
 					this.mensa = mensa;
 					loadMensa(today > Calendar.FRIDAY);
 					
 					// Speichern.
 					SharedPreferences pref = getPreferences(0);
 					Editor editor = pref.edit();
 					editor.putString("mensa", mensa.toString());
 					editor.commit();
 					break;
 				}
 			}
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * Gibt den Namen des Tages zurück.
 	 * 
 	 * Dies ist im Allgemeinen entweder Heute, (Wochentag) oder nächster
 	 * (Wochentag).
 	 * 
 	 * @param day
 	 * @return Name des Tages
 	 * @see MensaActivity#getToday()
 	 */
 	public String getNameOfDay(int day) {
 		if (day == today)
 			return MensaActivity.getInstance().getString(R.string.today);
 		else
 			return MensaActivity.getInstance().getResources().getStringArray(R.array.daysOfWeek)[day];
 	}
 }
