 
 import java.util.ArrayList;
 import java.util.ResourceBundle;
 
 import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;
 import com.trolltech.qt.gui.QCheckBox;
 import com.trolltech.qt.gui.QGridLayout;
 import com.trolltech.qt.gui.QMessageBox;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QTableWidget;
 import com.trolltech.qt.gui.QWidget;
 import com.trolltech.qt.gui.QAbstractItemView.EditTrigger;
 
 
 public class PlaceShipScreen extends QWidget {
 	
 	QWidget parent;
 	ResourceBundle bundle;
 	GameBoard gb;
 	int shipNum;
 	int[] lengths;
 	boolean horiz;
 	ArrayList<Ship> ships;
 	
 	public PlaceShipScreen(QWidget parent, ResourceBundle bundle, GameBoard gb, int[] lengths) {
 		super(parent);
		this.shipNum = 1;
 		this.bundle = bundle;
 		this.lengths = lengths;
 		this.parent = parent;
 		this.horiz = false;
 		this.parent.setWindowTitle(bundle.getString("placeShipScreen"));
 		ships = new ArrayList<Ship>();
 		
 		this.gb = gb;
 		
 		QTableWidget table = createTable();
 		
 		QGridLayout widgetLayout = new QGridLayout();
 		widgetLayout.addWidget(table, 1, 2);
 		
 		QPushButton back = new QPushButton(bundle.getString("back"));
 		QPushButton play = new QPushButton(bundle.getString("play"));
 		
 		QCheckBox horizontal = new QCheckBox(bundle.getString("horizontal"));
 		
 		widgetLayout.addWidget(back, 2, 1);
 		widgetLayout.addWidget(play, 2, 3);
 		widgetLayout.addWidget(horizontal, 1,3);
 		
 		back.clicked.connect(this, "showSetupScreen2()");
 		play.clicked.connect(this, "play()");
 		horizontal.clicked.connect(this, "setHorizontal()");
 		
 		this.setLayout(widgetLayout);
 		table.setFixedSize(500,500);
 		
 		this.show();
 	}
 	
 	public QTableWidget createTable() {
 		QTableWidget table = new QTableWidget(this.gb.getHeight(), gb.getWidth());
 		for (int i = 0; i < table.columnCount(); i++) {
 			table.setColumnWidth(i, (int)(475.0/((double)this.gb.getWidth())));
 		}
 		for (int i = 0; i < table.rowCount(); i++) {
 			table.setRowHeight(i, (int)(475.0/((double)this.gb.getHeight())));
 		}
 		table.setEditTriggers(EditTrigger.NoEditTriggers);
 		table.setSelectionMode(SelectionMode.SingleSelection);
 		table.cellClicked.connect(this, "place1(int,int)");
 		
 		return table;
 	}
 	
 	
 	public void setHorizontal(){
 		horiz = !horiz;
 	}
 	
 	public void place1(int r, int c){
 		ships.add(new Ship(r,c,lengths[shipNum],horiz));
 		System.out.printf("row %d and column %d \n",r, c);
 	}
 	
 	public void play(){
 		if(gb.checkAndPlaceShips(ships, "bottom")){
 			System.out.println(gb.toString());
 			((GameStarter) parent).showBoardScreen(gb, lengths);
 		}
 		else{
 			QMessageBox mess = new QMessageBox();
 			mess.setWindowTitle("Error");
 			mess.setText("Improper Placement\n Try again");
 			mess.exec();
 			((GameStarter) this.parent).showPlaceShipScreen(gb,lengths);
 		}
 	}
 	
 	public void showSetupScreen2(){
 		((GameStarter) parent).showSetupScreen2(gb);
 	}
 }
