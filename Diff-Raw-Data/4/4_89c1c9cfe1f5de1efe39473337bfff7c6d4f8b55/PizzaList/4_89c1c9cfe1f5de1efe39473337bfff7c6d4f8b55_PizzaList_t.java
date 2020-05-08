 package PizzaPck;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.trolltech.qt.core.Qt.ScrollBarPolicy;
 import com.trolltech.qt.gui.*;
 import com.trolltech.qt.gui.QSizePolicy.Policy;
 
 
 
 public class PizzaList extends QWidget implements Iterable<Pizza>{
 	
 	protected QLayout lay;
 	protected QVBoxLayout v_box;
 	protected List<Pizza> pizza_list;
 	protected QWidget main;
 	protected QScrollArea scrollarea;
 	private DB db;
 	
 	/**
 	 * 
 	 * @param db
 	 * @see fillList
 	 */
 	public PizzaList(DB db){
 		this.db = db;
 		//Init 
 		fillList();
 		
 	
 		}
 	
 	
 	public Signal1<String[]> signalPizza = new Signal1<String[]>();
 	public Signal1<String[]> signalBridge = new Signal1<String[]>();
 	
 	
 	@Override
 	public Iterator<Pizza> iterator() {
 		// TODO Auto-generated method stub
 		return pizza_list.iterator();
 	}
 	
 	/**
 	 * This method fills the list with pizzas from the database
 	 */
 	public void fillList() {
 		pizza_list = new ArrayList<Pizza>();
 		LinkedList<String[]> llProdukter;
 		llProdukter = db.getMenu();
 		Iterator<String[]> iter = llProdukter.iterator();
 		System.out.println(llProdukter);
 		while(iter.hasNext()){
 			String[] a = iter.next();
 			Pizza p = new Pizza(a);
			p.setFixedWidth(500);
			//p.setContentsMargins(0,1,1,1);
 			p.signalPizza.connect(this, "signalBridge(String[])");
 			pizza_list.add(p);
 		}
 		v_box = new QVBoxLayout(); 
 		lay = new QGridLayout();
 		v_box.setContentsMargins(1,1,1,1);
 		
 		
 		
 		main = new QWidget();
 		main.setLayout(v_box);
 		this.setContentsMargins(1, 1, 1, 2);
 		//main.setBaseSize(pizza_list.get(0).width()+10, pizza_list.get(0).height()*6);
 		scrollarea = new QScrollArea(this);
 		scrollarea.setWidgetResizable(true);
 		
 		scrollarea.setVerticalScrollBarPolicy(ScrollBarPolicy.ScrollBarAlwaysOn);
 		
 		scrollarea.setWidget(main);
 		
 		//scrollarea.setSizePolicy(Policy.Fixed, Policy.Fixed);
 		//Update gui
 		for (Pizza p: pizza_list) {
 			v_box.addWidget(p);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param data
 	 */
 	public void signalBridge(String [] data) {
 		System.out.println("Signal forwared from pizza list");
 		signalBridge.emit(data);
 	}
 
 }
