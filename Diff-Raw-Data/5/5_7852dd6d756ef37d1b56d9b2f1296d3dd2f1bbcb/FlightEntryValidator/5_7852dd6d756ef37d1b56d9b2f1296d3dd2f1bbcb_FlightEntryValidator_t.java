 package ch.ubx.startlist.client.ui;
 
 import java.util.Date;
 
 
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class FlightEntryValidator {
 
 	private FlightEntryListGUI gui = null;
 	private DialogBox dialogBox;
 
 	public FlightEntryValidator(FlightEntryListGUI gui) {
 		this.gui = gui;
 
 		dialogBox = new DialogBox();
 		dialogBox.setAutoHideEnabled(true);
 		dialogBox.setModal(false);
 		Button closeButton = new Button("Close");
 		closeButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				dialogBox.hide();
 			}
 		});
 		dialogBox.setWidget(closeButton);
 		dialogBox.hide();
 	}
 
 	public boolean isValid() {
 		Date startTime, landingTime;
 
 		// Date
 		{
 			Date date = gui.dateBox.getValue();
 			if (date.compareTo(new Date()) > 0) {
 				showMessage(gui.dateBox, "Datum in der Zukunft!");
 				return false;
 			}
 		}
 
 		// Start Time
 		{
 			TextBox tb = gui.startDateBox.getTextBox();
 			if (tb != null) {
 				if (!checkTime(tb.getValue())) {
 					showMessage(gui.startDateBox, "Startzeit format mm:ss sein!");
 					return false;
 				}
 			}
 
 		}
 
 		// Landing Time
 		{
 			TextBox tb = gui.endDateBox.getTextBox();
 			if (tb != null) {
 				if (!checkTime(tb.getValue())) {
 					showMessage(gui.endDateBox, "Landezeit format mm:ss sein!");
 					return false;
 				}
 
 			}
 		}
 
 		// Duration
 		{
 			startTime = gui.startDateBox.getValue();
 			landingTime = gui.endDateBox.getValue();
 			if (startTime != null && landingTime != null) {
 				if (!landingTime.after(startTime)) {
 					showMessage(gui.startDateBox, "Startzeit vor oder gleich Landezeit!");
 					return false;
 				}
 			}
 		}
 
 		// Pilot Name
 		{
			String pilot = gui.pilotNameBox.getValue();
 			if (pilot.length() > 32) {
				showMessage(gui.pilotNameBox, "Piloten Name zu lang, maximum 32 Zeichen!");
 				return false;
 			}
 		}
 
 		// Landing Place
 		String lp = gui.allPlacesSuggestBox.getValue();
 		if (lp.length() > 20) {
 			showMessage(gui.registrationBox, "Landeort zu lang, maximum 20 Zeichen!");
 			return false;
 		}
 
 		// 
 
 		// Registration
 		{
 			String reg = gui.registrationBox.getValue();
 			if (reg.length() > 7) {
 				showMessage(gui.registrationBox, "Immatrikulation zu lang, maximum 7 Zeichen!");
 				return false;
 			}
 		}
 
 		// Remarks
 		{
 			String rmk = gui.remarksTextBox.getValue();
 			if (rmk.length() > 80) {
 				showMessage(gui.remarksTextBox, "Text zu lang, maximum 80 Zeichen!");
 				return false;
 			}
 		}
 
 		// All OK!
 		return true;
 	}
 
 	private boolean checkTime(String value) {
 		if (value.length() == 0) {
 			return true;
 		}
 		String[] mmss = value.split(":");
 		if (mmss.length == 2) {
 			int val;
 			for (String ms : mmss) {
 				try {
 					val = Integer.parseInt(ms);
 				} catch (Exception e) {
 					return false;
 				}
 				if (val < 0 || val > 59) {
 					return false;
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	private void showMessage(Widget root, String msg) {
 		dialogBox.setPopupPosition(root.getAbsoluteLeft(), root.getAbsoluteTop() + root.getOffsetHeight());
 		dialogBox.setText(msg);
 		dialogBox.show();
 	}
 
 }
