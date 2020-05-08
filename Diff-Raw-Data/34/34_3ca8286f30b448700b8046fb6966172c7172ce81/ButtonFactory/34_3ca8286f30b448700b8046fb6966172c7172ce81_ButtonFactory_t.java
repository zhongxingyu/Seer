 package com.km2team.syriush;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.content.Context;
 
 import com.km2team.syriush.database.DataManipulator;
 import com.km2team.syriush.database.DataSerializer;
import com.km2team.syriush.database.DatabaseException;
 
  class ButtonFactory {
 	public static ArrayList<SyriushButton>L=null;
 	static int id=0;
 	static int folderid=0;
 	static Context context;
 	static int load = 0;
 	
 	
 	static void load(){
		try{L=new DataSerializer(new DataManipulator(context)).loadSyriushButtonArrayList("asd1");}
		catch(Exception e){}
 		if (L!=null) return;
 		load=1;
 		L=new ArrayList<SyriushButton>();
 		L.add(new SyriushButton(++id,"Kompas", id));//1
 		L.add(new SyriushButton(++id,"Najbliszy Punkt", id));
 		L.add(new SyriushButton(++id,"Kolejny Punkt Kontrolny", id));
 		L.add(new SyriushButton(++id,"Obecna Pozycja", id));//4
 		L.add(new SyriushButton(++id,"Trasa", id));
 		L.add(new SyriushButton(++id,"Cel", id));
 		L.add(new SyriushButton(++id,"Dodaj punkt", id));//7
 		SyriushButton tempFolder=new SyriushButton(++folderid,"Tworzenie trasy", folderid);
 		tempFolder.itIsFolder=true;
 		tempFolder.setItIsFolder(true);
 		L.add(tempFolder);
 		SyriushButton[]tempTab={new SyriushButton(++id,"Rozpocznij zapis trasy", id),new SyriushButton(++id,"Dodaj putnkt kontrolny", id),new SyriushButton(++id,"Zakocz zapis trasy", id)};
 		for (SyriushButton b : tempTab){
 			L.add(b);
 			moveToFolder(b, tempFolder);
 		}
 	}
 	
 	static void setContext(Context con){
 		context = con;
 	}
 	
	static void save() throws DatabaseException{
 		new DataSerializer(new DataManipulator(context)).saveSyriushButtonArrayList("asd1", L);
 	}
 	
 	static void moveToFolder(int itemIdx, int folderIdx){
 		SyriushButton tmp = new SyriushButton(L.get(itemIdx));
 		L.get(folderIdx).list.add(tmp);
 		L.get(itemIdx).setNum(-L.get(itemIdx).getNum());
 	}
 	
 	static void moveToFolder(SyriushButton button, SyriushButton folder){
 		SyriushButton tmp = new SyriushButton(button);
 		folder.list.add(tmp);
 		button.setNum(-button.getNum());
 	}
 	
 	@SuppressWarnings("unchecked")
 	static ArrayList<SyriushButton> getButtonList(){
 		ArrayList<SyriushButton> Ret = new ArrayList<SyriushButton>();
 		for (SyriushButton b : L) if (b.getNum()>=1) Ret.add(b);
 		Collections.sort(Ret);
 		return Ret;
 
 	}
 	
 	static void remove(int idx){
 		L.remove(idx);
 	}
 	
 	static void addFolder(String name){
 		SyriushButton temp=new SyriushButton(++folderid,name, folderid);
 		temp.setItIsFolder(true);
 		L.add(temp);
 	}
 }
