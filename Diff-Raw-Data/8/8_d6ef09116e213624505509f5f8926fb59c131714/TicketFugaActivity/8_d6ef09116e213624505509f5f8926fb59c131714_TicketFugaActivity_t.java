 package cmdf2011.weff.interfaz.activities;
 
 import java.util.List;
 
 import cmdf2011.weff.rest.LugarFisicoRest;
 import cmdf2011.weff.rest.PrioridadRest;
 import cmdf2011.weff.rest.SentidoRest;
 import cmdf2011.weff.rest.TramoRest;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
 import android.widget.Spinner;
 
 public class TicketFugaActivity extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ticket_fuga_agua);		
 		
 		Spinner s = (Spinner) findViewById(R.id.prioridadSpinner);
 		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, PrioridadRest.findPrioridadAll(10).toArray());
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		s.setAdapter(adapter);
 
 		s = (Spinner) findViewById(R.id.lugarFisicoSppiner);
 		adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, LugarFisicoRest.findLugarFisicoAll(10).toArray());
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		s.setAdapter(adapter);
 
 		s = (Spinner) findViewById(R.id.sentidoSpinner);
 		adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, SentidoRest.findSentidoAll(10).toArray());
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		s.setAdapter(adapter);
 
		AutoCompleteTextView a = (AutoCompleteTextView) findViewById(R.id.tramoAutoComplete);
 		adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, TramoRest.findTramosAll(10).toArray());
		a.setAdapter(adapter);
 		
 	}
 
 	public void cancelar(View v) {
 		finish();
 	}
 }
