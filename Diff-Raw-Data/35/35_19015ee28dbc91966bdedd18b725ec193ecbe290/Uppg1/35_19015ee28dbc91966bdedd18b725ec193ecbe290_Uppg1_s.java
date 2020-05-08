 package lab1;
 import java.lang.System;
 
 
 public class Uppg1 {
 	//ArrayList<String> listStrings;
 	String[] listStrings;
 	private int position;
 	private int elemAmount;
 	private int limit;
 
 	public Uppg1(){
 		listStrings=new String[10];
 		elemAmount=0;
 		limit=10;
 		position=0;
 	}
 
 	public Uppg1(int i){
 		listStrings=new String[i];
 		elemAmount=0;
 		limit=i;
 		position=0;
 	}
 
 	// Vi har valt att ka listans storlek vid behov d vi ser detta 
 	// som en mer generell lsning. Givetvis kan det finnas tillfllen
 	// d det kan vara nskvrt att ha en begrnsad liststorlek, men
 	// vi uppfattar det som ett mer ovanligt scenario.
 	public void addFirst(String element){
 		elemAmount++;
 		if (elemAmount>limit){
 			enlargeArray();
 			//			limit=limit*2;
 			//			String[] newArray= new String[limit];
 			//			System.arraycopy(listStrings, 0, newArray, 0, elemAmount-1);//Do something to move all the elements one step to the right
 			//			listStrings=newArray;
 			listStrings[elemAmount-1]=element;
 		} else {
 			listStrings[elemAmount-1]=element;
 		}
 	}
 
 	//Check if string array is empty
 
 	public boolean empty(){
 		for (int i=0; i<listStrings.length;i++){
 			if (listStrings[i]!=null)
 				return false;
 		}
 		return true;
 	}
 
 	public String getFirst(){
 		return listStrings[elemAmount-1];
 	}
 
 	public void removeFirst(){
 		listStrings[elemAmount-1]=null;
 		elemAmount--;
 	}
 
 	public boolean existP(String elem){
 		if (elem==null){
 			for (int i=elemAmount; i>=0;i--){
 				if (listStrings[i]==null)
 					return true;
 			}
 		}
 		else for (int i=elemAmount; i>=0;i--){
 			if (listStrings[i]!=null && listStrings[i].equals(elem))
 				return true;
 		}
 		return false;
 	}
 
 	public String toString(){ 
 		StringBuilder s= new StringBuilder();
 		for(int i=(elemAmount-1); i>=0;i--){ 
 			s.append(listStrings[i]);
 			if (i!=0){
 				s.append(", ");
 			}
 		}
 		return "["+s+"]";
 	}
 
 	public void setP (int p){//Behver vi kolla om nya positionen r giltig?
 		int tillf= position;
		position=p;
		if(!hasNext())
 			position=tillf;
	}


	
	public String get(int p){
		return listStrings[(elemAmount-p)];//elementet p eller det 'efter'??
 	}
 	
	void moveP (int val) {
		position = position+(-val);	
	}

 	private boolean hasNext(){
 		return 0<=position&& position<=elemAmount;
 	}


 	private void addAfterP(int index, String element){//Menas att stta in p platsen P eller platsen efter?? SKa man ndra platsen p p?
 		if(index>=0){
 			if(elemAmount+1>limit){
 				enlargeArray();
 			}
 			for(int i=elemAmount; i>=elemAmount-index;i--){
 				listStrings[i+1]=listStrings[i];
 			}
 			listStrings[elemAmount-index]=element;	
 			elemAmount++;
 		}
 	}
 
 	private void enlargeArray(){
 		limit=limit*2;
 		String[] newArray= new String[limit];
 		System.arraycopy(listStrings, 0, newArray, 0, elemAmount-1);//Do something to move all the elements one step to the right
 		listStrings=newArray;
 	}

 }
