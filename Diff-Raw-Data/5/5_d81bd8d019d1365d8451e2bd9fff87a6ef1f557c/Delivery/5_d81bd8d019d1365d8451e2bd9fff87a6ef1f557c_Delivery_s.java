 package PizzaPck;
 
 import java.util.ArrayList;
 
 import com.trolltech.qt.core.QUrl;
 import com.trolltech.qt.gui.QGridLayout;
 import com.trolltech.qt.gui.QListWidget;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QWidget;
 
 /**
  * This class creates the GUI for displaying the 
  * chef's user interface. And inherit from QWidget. 
  * @author Everyone
  */
 public class Delivery extends QWidget{
 
 	private DB db;
 	private QListWidget deliveryList;
 	private QGridLayout layout;
 	private QPushButton btnDelivered, update;
 	private QPushButton receipt;
 	private Map map;
 	private QVBoxLayout map_lay;
 	private ArrayList<String[]> mirrorDeliveryList; 
 	private int row;
 	private PrintReceipt print;
 
 
 
 	/**
 	 * The constructor receives a reference through
 	 * db for accessing the database methods it need to
 	 * get the information about the orders.
 	 * @param db
 	 */
 	public Delivery(DB db){
 		this.db = db;
 		map = new Map();
 		setup();
 	}
 
 
 	/**
 	 * This method setup the interface for the delivery.
 	 * 
 	 */
 	private void setup(){
 
 		deliveryList = new QListWidget();
 
 
 		mirrorDeliveryList = new ArrayList<String[]>();
 		layout = new QGridLayout(this);
 
 		receipt = new QPushButton("Skriv ut");
 		btnDelivered = new QPushButton("Levering");
 		update = new QPushButton("Oppdater");
 		map_lay = new QVBoxLayout();
 		map_lay.addWidget(map);
 
 
 		layout.addLayout(map_lay, 1, 1);
 		layout.addWidget(update, 2, 0);
 		layout.addWidget(btnDelivered,2,1);
 		layout.addWidget(receipt,2,2);
 
 		getDeliveries();
 		update.clicked.connect(this,"getDeliveries()");
 		btnDelivered.clicked.connect(this, "setOrderDelivered()");
 		receipt.clicked.connect(this,"print()");
 	}
 
 	/**
 	 * This method takes the selected element in deliveryList
 	 * and sets the order status to delivered. And makes the map in
 	 * the QWebView change to the default address.
 	 */
 	private void setOrderDelivered(){
 		String[] tmp = mirrorDeliveryList.get(row);
 		String orderID = tmp[0];
 
 		db.updateDeliveredStatus(orderID);
 		getDeliveries();
		map.loadMap(new QUrl(map.getMap("Bispegata 5  7032 Trondheim","")));
 	}
 
 
 	/**
 	 * This method creates a print receipt and displays it. 
 	 * And to do that it gets the information from the database.
 	 *  
 	 */
 	private void print(){
 		String[] tmp = mirrorDeliveryList.get(row);
 
 		ArrayList<String[]> receiptProducts = db.getReceipt(tmp[0]);
 
 		try {
 			print = new PrintReceipt(receiptProducts, tmp);
 		} catch (NullPointerException e) {
 			// TODO: handle exception
 		}		
 		print.show();
 	}
 
 	/**
 	 * This method get all orders from the database
 	 * where the finish status is set to true. And
 	 * sends a signal to the method showDeliveries()
 	 * which shows the orders that are ready to be delivered/ 
 	 * picked up by costumer.
 	 */
 	public void getDeliveries() {
 
 		deliveryList = new QListWidget();
 		layout.addWidget(deliveryList, 1, 0);
 		mirrorDeliveryList = new ArrayList<String[]>();
 		ArrayList<String[]> list = db.getAllDeliveries();
 
 		if(list == null) {
 			return;
 		}
 
 		for(int i = 0; i <= list.size()-1; i++) {
 
 			deliveryList.addItem(format(list.get(i)));
 			mirrorDeliveryList.add(list.get(i));
 
 		}
 		deliveryList.clicked.connect(this, "showDeliveries()");
 	}
 
 	/**
 	 * This method displays the map for the current picked 
 	 * delivery form the delivery list. If the costumer wants 
 	 * to pick up the order by them self, a default map is shown.
 	 * Else a map with start address and costumer address is shown.  
 	 */
 	private void showDeliveries(){
 		row = deliveryList.currentIndex().row();
 
 		StringBuilder toAdress = new StringBuilder();
 		String[] tmp = mirrorDeliveryList.get(row);
 
 		toAdress.append(tmp[10]+" ");
 		toAdress.append(tmp[11]+" ");
 		toAdress.append(tmp[12]+" ");
 
 		if(tmp[2].equals("1")){
			map.loadMap(new QUrl(map.getMap("Bispegata 5  7032 Trondheim",
 					toAdress.toString())));
 		}
 		else{
 			map.loadMap(map.getDefaultMap());
 		}
 	}
 
 	/**
 	 * This method formats the string displayed in delivery list.
 	 * A really easy formatting on the order string, just showing
 	 * the ordernumber, if the order should be delivered or picked up,
 	 * the name of the costumer and phonenumber.
 	 *  
 	 * @param data
 	 * @return String
 	 */
 	private String format(String[] data) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(data[0]+":  "); 					//Ordre nummer
 		sb.append((data[2].equals("1") ? 
 				"Skal leveres " : "Skal IKKE leveres "));
 		sb.append(data[6]+"\n         ");			//leverings dato+ time
 
 		sb.append(data[8]+" ");						//Fornavn
 		sb.append(data[9]+" ");						//Etternavn
 
 		sb.append(data[13]+"\n");					//telefonnummer
 
 		return sb.toString();
 
 	}
 }
