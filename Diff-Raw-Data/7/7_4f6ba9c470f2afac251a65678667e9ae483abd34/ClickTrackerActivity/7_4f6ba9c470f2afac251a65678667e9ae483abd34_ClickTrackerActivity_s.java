 /**
  * 
  */
 package de.mbentwicklung.android.clickTracker;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import de.mbentwicklung.android.clickTracker.mail.MailMessageBuilder;
 import de.mbentwicklung.android.clickTracker.mail.MailService;
 import de.mbentwicklung.android.clickTracker.positioning.Position;
 import de.mbentwicklung.android.clickTracker.positioning.PositionLoader;
 
 /**
  * 
  * @author Marc Bellmann <marc.bellmann@mb-entwicklung.de>
  */
 public class ClickTrackerActivity extends Activity {
 
 	/** Senden Button */
 	private Button clickButton;
 
 	/** Maileingabefeld */
 	private EditText mailEditText;
 
 	/** Auswahlbox */
 	private RadioGroup selectBox;
 
 	/** Aktuelle Position */
 	private Position position;
 
 	/** Positionlader */
 	private PositionLoader positionLoader;
 
 	/**
 	 * Erstelle Activity mit alle Komponenten
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		position = new Position() {
 			/** serialVersionUID */
 			private static final long serialVersionUID = 814576456000699370L;
 
 			@Override
 			public void positionLoaded() {
 				sendMailWithService();
 				clickButton.setEnabled(true);
 			}
 		};
 		positionLoader = new PositionLoader(getApplicationContext(), position);
 
 		setupMailEditText();
 		setupSelectBox();
 		setupClickButton();
 	}
 
 	/**
 	 * Konfiguriere die Auswahlfelder
 	 */
 	private void setupSelectBox() {
 		selectBox = (RadioGroup) findViewById(R.id.SelectPositionType);
 
 		findViewById(R.id.gps).setEnabled(positionLoader.isGpsProviderEnabled());
 		findViewById(R.id.network).setEnabled(positionLoader.isNetworkProviderEnabled());
 		findViewById(R.id.last).setEnabled(positionLoader.isLastKnownPositionProviderEnabled());
 	}
 
 	/**
 	 * Konfiguriert das Maileingabefeld
 	 */
 	private void setupMailEditText() {
 		mailEditText = (EditText) findViewById(R.id.mail_editText);
 		mailEditText.setText(PreferencesManager.readMailAddress(getApplicationContext()));
 	}
 
 	/**
 	 * Konfiguriert den Senden Button 
 	 */
 	private void setupClickButton() {
 
 		clickButton = (Button) findViewById(R.id.button_click);
 		clickButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View view) {
 				clickButton.setEnabled(false);
 				loadLocation();
				PreferencesManager.writeMailAddress(getApplicationContext(), mailEditText.getText()
						.toString());
 			}
 		});
 	}
 
 	/**
 	 * Lädt mit Hilfe des {@link PositionLoader}s die aktuelle Position.
 	 */
 	private void loadLocation() {
 		switch (selectBox.getCheckedRadioButtonId()) {
 		case R.id.gps:
 			positionLoader.loadGPSPosition();
 			break;
 		case R.id.network:
 			positionLoader.loadNetworkPosition();
 			break;
 		case R.id.last:
 			positionLoader.loadLastKnownPosition();
 			break;
 		}
 	}
 
 	/**
 	 * Starte {@link MailService} zum Versenden der Mail
 	 */
 	private void sendMailWithService() {
 		Intent intent = new Intent(this, MailService.class);
 		intent.putExtra(MailService.KEY_POSITION_LINK, MailMessageBuilder.buildLinkWith(position));
 		intent.putExtra(MailService.KEY_MAIL_TO_ADDR, mailEditText.getText().toString());
 		startService(intent);
 	}
 }
