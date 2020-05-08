 package edu.klemen.rekreAsist.android;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.app.Application;
 import android.database.Cursor;
 import android.util.Log;
 import edu.klemen.rekreAsist.android.database.DBAdapterBaza;
 import edu.klemen.rekreasist.database.poti.DBAdapterBazaPoti;
 
 public class ApplicationExample extends Application {
 	//Step 4.1
 	//Step 4.2 popravi AndroidManifest.xml
 	
 	/*
 	 * widget
 	 */
 	private List<String> news;
 	private int stevec;
 	public List<String> getNews() {
 		return news;
 	}
 	public void setNews(List<String> news) {
 		this.news = news;
 	}
 	public int getStevec() {
 		return stevec;
 	}
 	public void setStevec(int stevec) {
 		this.stevec = stevec;
 	}
 	//konec widget
 	/*
 	 * baze
 	 * */
     public  PodatkiArrayAdapter podatkiList; //seznam
     
 	DBAdapterBaza db;
 	public ArrayList<podatkiZaBazo> lista;
 	
 	DBAdapterBazaPoti dbPoti;
 	public ArrayList<PodatkiZaPoti> listaPoti;
 //konec baze
 
 	public void onCreate() {
         super.onCreate(); //ne pozabi
         db = new DBAdapterBaza(this);
         dbPoti= new DBAdapterBazaPoti(this);
         
       
         lista= new ArrayList<podatkiZaBazo>();
         listaPoti= new ArrayList<PodatkiZaPoti>();
  
        initPoti();
         
         fillFromDB();
         podatkiList = new PodatkiArrayAdapter(this, R.layout.seznam_podatkov,lista);//podatkiList-seznam, R-layout ki bo izpiso, s keriga seznama
 
 	}
 	
 
 	
 	public void init()
 	{
 		//testni podatki
 		lista.add(new podatkiZaBazo(5202,9,5,14,"35:55","5.6.2011"));
 		lista.add(new podatkiZaBazo(6020,16,2,25,"43:45","7.6.2011"));
 		lista.add(new podatkiZaBazo(370,13,2,18,"00:55","7.6.2011"));
 		lista.add(new podatkiZaBazo(650,7,2,10,"02:45","9.6.2011"));
 
 
 		addDB(lista.get(0));
 		addDB(lista.get(1));
 		addDB(lista.get(2));
 		addDB(lista.get(3));
 
 	}
 	public void initPoti()
 	{
 		//testni podatki
 		Random ran= new Random(System.currentTimeMillis());
 		PodatkiZaPoti tmp = new PodatkiZaPoti();
 		for(int i=0;i<200;i++){
 			Log.e("test", "v zanki "+i);
 			if(i<50) tmp.id=1;
 			else if(i<100) tmp.id=2;
 			else if(i<150) tmp.id=3;
 			else if(i<200) tmp.id=4; //extDouble() * (5.12 - (-5.12)) + (-5.12);
 			tmp.Xkord=ran.nextDouble()* (90 -(-90)) + (-90);
 			tmp.Ykord=ran.nextDouble()* (90 -(-90)) + (-90);
 			
 			addDBPot(tmp);
 		}
 
 	}
 
 	public void dodajPodatke(podatkiZaBazo tmp)//za rekreacije
 	{
 		lista.add(tmp);
 		addDB(tmp);
 	}
 	public void dodajPodatkePot(PodatkiZaPoti tmp)//za poti
 	{
 		listaPoti.add(tmp);
 		addDBPot(tmp);
 	}
 
 	//DB dodano
 	public void fillFromDB() {
 		db.open();
 		Cursor c = db.getAll();
 		podatkiZaBazo tmp;
 		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 			tmp = new podatkiZaBazo();
 			
 			tmp.pot= c.getDouble(DBAdapterBaza.POS__POT);
 			tmp.povPrecnaHitrost= c.getDouble(DBAdapterBaza.POS__POVPRECNA_HITROST);
 			tmp.stKrogov=c.getInt(DBAdapterBaza.POS__ST_KROGOV);
 			tmp.maxSpeed=c.getDouble(DBAdapterBaza.POS__MAX_SPEED);
 			tmp.cas=c.getString(DBAdapterBaza.POS__CAS);
 			tmp.datum= c.getString(DBAdapterBaza.POS__DATUM);
 			
 			
 			lista.add(tmp); 
 			
 			
 		}
 		c.close();
 		db.close();
 	}
 	public void addDB(podatkiZaBazo s) {
 		db.open();
 		s.setID(db.insertPodatki(s));
 		db.close();	
 	}
 	public void addDBPot(PodatkiZaPoti s) {
 		dbPoti.open();
 		s.setID(dbPoti.insertPodatki(s));
 		dbPoti.close();	
 	}
 
 	public void getPotIzDB(int index) {//vrnem točke določene poti
 		dbPoti.open();
 		PodatkiZaPoti tmp;
 		Cursor c= dbPoti.getRoute(index);
 
 		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 			tmp = new PodatkiZaPoti();
 			
 			
 			tmp.Xkord= c.getDouble(DBAdapterBazaPoti.POS__XKORD);
 			tmp.Ykord= c.getDouble(DBAdapterBazaPoti.POS__YKORD);
 			
 			listaPoti.add(tmp); 
 
 		}
 		c.close(); 
 		dbPoti.close();
 	}
 
 }
