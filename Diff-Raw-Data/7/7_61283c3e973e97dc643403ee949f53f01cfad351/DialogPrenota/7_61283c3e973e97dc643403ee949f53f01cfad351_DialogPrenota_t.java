 package interfaccia;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import modelloTreni.ClassePosto;
 import modelloTreni.IstanzaTreno;
 import modelloTreni.TrainException;
 import modelloTreni.TrainManager;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TableItem;
 
 public class DialogPrenota {
 	TrainManager manager = TrainManager.getInstance();
 	MainShell mainWindow = MainShell.getMainShell();
 
 	private Shell popup;
 	private Label prezzototale;
 	private Label[] labelPrezziIstanze;
 	private Double[] oldprezzoistanza;
 	private Double[][] prezziIstanze;
 	private Double counterprezzofinale;	
 	private ClassePosto[] classiscelte;
 	private Button ok, bottoniIstanze[][];
 	private String outputmessage;
 	private String[] disponibilita;
 	private int counter, numeroPosti;
 	private TableItem[] itemsIstanze;
 	private ArrayList<IstanzaTreno> istanze;
	private boolean abilitaOk=true;
 
 	public DialogPrenota(TableItem[] itemsIstanze, HashMap<String, ArrayList<Double>> prezzi,
 			ArrayList<IstanzaTreno> istanze, int numeroPosti) {
 		this.itemsIstanze = itemsIstanze;
 		this.numeroPosti = numeroPosti;
 		this.istanze = istanze;
 
 		popup = new Shell(mainWindow.getShell(), 65616);
 		prezzototale = new Label(popup, SWT.NONE);
 		prezzototale.setText("Prezzo Totale: -                ");
 		String message = "";
 		boolean thereisistancenoprenot = false;
 		boolean noistanceprenot = true;
 		Integer count = 0;
 		counterprezzofinale = new Double(0.0);
 		classiscelte = new ClassePosto[itemsIstanze.length];
 		oldprezzoistanza = new Double[itemsIstanze.length];
         bottoniIstanze = new Button[itemsIstanze.length][2];
         prezziIstanze = new Double[itemsIstanze.length][2];
         labelPrezziIstanze = new Label[itemsIstanze.length];
         disponibilita = new String[itemsIstanze.length];
 		for (int i = 0; i < itemsIstanze.length; i++) {
 			Label label = new Label(popup, SWT.NONE);
 			label.setText("Scegli la classe che desideri per il treno " + itemsIstanze[i].getText(1) + " "
 					+ itemsIstanze[i].getText(0));
 			bottoniIstanze[i] = new Button[2];
 			bottoniIstanze[i][0] = new Button(popup, SWT.RADIO);
 			bottoniIstanze[i][0].setText("I classe");
 			bottoniIstanze[i][1] = new Button(popup, SWT.RADIO);
 			bottoniIstanze[i][1].setText("II classe");
 			disponibilita[i] = itemsIstanze[i].getText(6);
 			if (itemsIstanze[i].getText(6).equals("NON DISPONIBILE")){
 				bottoniIstanze[i][0].setEnabled(false);
 				bottoniIstanze[i][1].setEnabled(false);
				abilitaOk = false;
 			}else{
 				if (itemsIstanze[i].getText(6).equals("SOLO I CLASSE")){
 					bottoniIstanze[i][1].setEnabled(false);
 				}else{
 					if (itemsIstanze[i].getText(6).equals("SOLO II CLASSE")){
 						bottoniIstanze[i][0].setEnabled(false);
 					}
 				}
 			}
 				
 			
 			labelPrezziIstanze[i] = new Label(popup, SWT.NONE);
 			prezziIstanze[i][0] = (prezzi.get(itemsIstanze[i].getText(0)).get(0)) * numeroPosti;
 			prezziIstanze[i][1] = (prezzi.get(itemsIstanze[i].getText(0)).get(1)) * numeroPosti;
 			labelPrezziIstanze[i].setText("Prezzo: -                ");
 			counter = i;
 			oldprezzoistanza[i] = 0.0;
 
 			createPrimaClasseRadioButtonListener(counter);
 			createSecondaClasseRadioButtonListener(counter);
 
 			if (itemsIstanze[i].getText(6).equals("NON PRENOTABILE")) {
 				thereisistancenoprenot = true;
 				count++;
 
 			} else {
 				if (!thereisistancenoprenot) {
 					message = "Prenotazione/i effettuata/e con successo. Ricevuta stampata" + "\nBuon viaggio.";
 				}
 				noistanceprenot = false;
 			}
 		}
 
 		count = count * numeroPosti;
 
 		if ((!noistanceprenot) && thereisistancenoprenot) {
 			message = "Prenotazione/i effettuata/e con successo. Hai acquistato " + count
 					+ "biglietto/i per corsa regionale" + " Ricevuta stampata" + "\nBuon viaggio.";
 		} else if (noistanceprenot) {
 			message = "Hai acquistato " + count + " biglietto/i per corsa regionale" + " Ricevuta stampata"
 					+ "\nBuon viaggio.";
 		}
 
 		outputmessage = message;
 
 		ok = new Button(popup, SWT.PUSH);
 		ok.setText("Ok");
 		createOkButtonListener();
		if(abilitaOk==false){
			ok.setEnabled(false);
		}
 
 		popup.setLayout(new RowLayout(SWT.VERTICAL));
 		popup.pack();
 		popup.open();
 	}
 
 	public Shell getDialog() {
 		return popup;
 	}
 
 	public void showDialog() {
 		popup.open();
 	}
 	
 	private void createOkButtonListener() {
 		ok.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				for (int i = 0; i < itemsIstanze.length; i++) {
 					if ((!itemsIstanze[i].getText(6).equals("NON DISPONIBILE")))
 					{
 						try {
 							if(itemsIstanze[i].getText(6).equals("NON PRENOTABILE")){
 								istanze.get(i).compraBiglietto(numeroPosti, classiscelte[i]);
 							}else{
 								manager.compraBiglietto(istanze.get(i), numeroPosti, classiscelte[i]);
 							}
 						} catch (TrainException e) {
 							MessageBox alert = new MessageBox(MainShell.getMainShell().getShell(), SWT.ICON_ERROR
 									| SWT.OK);
 							alert.setMessage(e.getMessage());
 							alert.open();
 							return;
 						} catch (NullPointerException e) {
 							MessageBox alert = new MessageBox(MainShell.getMainShell().getShell(), SWT.ICON_ERROR
 									| SWT.OK);
 							alert.setMessage("Devi selezionare una classe per il/i treno/i desiderato/i prima "
 									+ "\nprocedere all'acquisto");
 							alert.open();
 							return;
 						}
 					}
 				}
 				MessageBox alert = new MessageBox(MainShell.getMainShell().getShell(), SWT.ICON_INFORMATION | SWT.OK);
 				alert.setMessage(outputmessage);
 				alert.open();
 				popup.close();
 
 			}
 		});
 
 	}
 
 	private void createSecondaClasseRadioButtonListener(int c) {
 		final int count = c;
 		bottoniIstanze[c][1].addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				bottoniIstanze[count][0].setSelection(false);
 				bottoniIstanze[count][1].setSelection(true);
 				if (disponibilita[count].equals("NON DISPONIBILE") || disponibilita[count].equals("SOLO I CLASSE")) {
 					labelPrezziIstanze[count].setText("Non sono disponibili posti di II classe");
 				} else {
 					String text = Double.toString(prezziIstanze[count][1]);
 					int integerPlaces = text.indexOf('.');
 					int decimalPlaces = text.length() - integerPlaces - 1;
 					if (decimalPlaces > 1) {
 						labelPrezziIstanze[count].setText("Prezzo: " + prezziIstanze[count][1].toString() + " euro");
 					} else {
 						labelPrezziIstanze[count].setText("Prezzo: " + prezziIstanze[count][1].toString() + "0 euro");
 					}
 				}
 				
 				classiscelte[count] = ClassePosto.SECONDACLASSE;
 				counterprezzofinale = counterprezzofinale + prezziIstanze[count][1] - oldprezzoistanza[count];
 				String text = Double.toString(counterprezzofinale);
 				int integerPlaces = text.indexOf('.');
 				int decimalPlaces = text.length() - integerPlaces - 1;
 				if (decimalPlaces > 1) {
 					prezzototale.setText("Prezzo: " + counterprezzofinale.toString() + " euro");
 				} else {
 					prezzototale.setText("Prezzo: " + counterprezzofinale.toString() + "0 euro");
 				}
 				oldprezzoistanza[count] = prezziIstanze[count][1];
 			}
 		});
 	}
 
 	private void createPrimaClasseRadioButtonListener(int c) {
 		final int count = c;
         bottoniIstanze[c][0].addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				bottoniIstanze[count][1].setSelection(false);
 				bottoniIstanze[count][0].setSelection(true);
 				if (disponibilita[count].equals("NON DISPONIBILE") || disponibilita[count].equals("SOLO II CLASSE")) {
 					labelPrezziIstanze[count].setText("Non sono disponibili posti di I classe");
 				} else {
 					String text = Double.toString(prezziIstanze[count][0]);
 					int integerPlaces = text.indexOf('.');
 					int decimalPlaces = text.length() - integerPlaces - 1;
 					if (decimalPlaces > 1) {
 						labelPrezziIstanze[count].setText("Prezzo: " + prezziIstanze[count][0].toString() + " euro");
 					} else {
 						labelPrezziIstanze[count].setText("Prezzo: " + prezziIstanze[count][0].toString() + "0 euro");
 					}
 
 				}
 				classiscelte[count] = ClassePosto.PRIMACLASSE;
 				counterprezzofinale = counterprezzofinale + prezziIstanze[count][0] - oldprezzoistanza[count];
 				String text = Double.toString(counterprezzofinale);
 				int integerPlaces = text.indexOf('.');
 				int decimalPlaces = text.length() - integerPlaces - 1;
 				if (decimalPlaces > 1) {
 					prezzototale.setText("Prezzo: " + counterprezzofinale.toString() + " euro");
 				} else {
 					prezzototale.setText("Prezzo: " + counterprezzofinale.toString() + "0 euro");
 				}
 				oldprezzoistanza[count] = prezziIstanze[count][0];
 			}
 		});
 
 	}
 }
