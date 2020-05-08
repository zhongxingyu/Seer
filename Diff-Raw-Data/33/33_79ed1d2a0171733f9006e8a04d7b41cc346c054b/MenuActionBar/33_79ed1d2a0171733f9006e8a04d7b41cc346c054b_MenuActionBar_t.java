 package net.mabako.zwickau.mensa;
 
 import java.util.Calendar;
 
 import android.app.ActionBar;
 import android.text.format.DateUtils;
 import android.view.View;
 import android.widget.TextView;
 
 /**
  * IceCream Sandwich-ActionBar-Menü.
  * 
  * Gibt nur das Datum, Wochentag und Mensa in der Actionbar aus.
  * 
  * @author Marcus Bauer (mabako@gmail.com)
  */
 // TODO DropDown-Menü für Tage statt normalem Menü mittels Taste.
 public class MenuActionBar extends MenuHelper {
 	private MensaActivity activity;
 
 	/**
 	 * Aktualisiert den View für die ActionBar und passt diese an den heutigen
 	 * Tag an.
 	 */
 	public MenuActionBar() {
 		activity = MensaActivity.getInstance();
 
 		// Eigenen View erzeugen
 		ActionBar actionBar = activity.getActionBar();
 		View spinner = activity.getLayoutInflater().inflate(R.layout.spinner, null);
 		actionBar.setCustomView(spinner);
 		actionBar.setDisplayShowCustomEnabled(true);
 
 		// ActionBar für "Heute" aktualisieren.
		updateTitle(activity.getCurrentDay());
 	}
 
 	/**
 	 * Aktualisiert die Anzeige/Text in der ActionBar.
 	 */
 	public void updateTitle(int day) {
 		TextView text = (TextView) activity.findViewById(R.id.actionbar_text);
 		text.setText(activity.getNameOfDay(day));
 
 		// Zeige Datum und Mensa auf zweiter Zeile
 		TextView subtext = (TextView) activity.findViewById(R.id.actionbar_subtext);
 		Calendar c = Calendar.getInstance();
 		c.add(Calendar.DAY_OF_MONTH, day - activity.getToday());
 		subtext.setText(DateUtils.formatDateTime(activity, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_YEAR) + ", " + activity.getMensa().getName());
 	}
 }
