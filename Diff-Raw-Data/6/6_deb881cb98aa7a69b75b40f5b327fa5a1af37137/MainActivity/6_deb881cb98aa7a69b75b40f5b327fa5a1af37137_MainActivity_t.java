 package com.example.grid;
 
 import java.util.ArrayList;
 
 import android.R.bool;
 import android.app.Activity;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	
 	ArrayList<ArrayList<ImageView>> square; 
 	boolean navio = false;
 	int type_navio = 0;
 	int[][] cmp = new int[10][10];
 	int portaavioes=0, destroier=0, cruzador=0, submarino=0;
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         square = new ArrayList<ArrayList<ImageView>>(10);
         
         ArrayList<ImageView> aux = new ArrayList<ImageView>(10);
         
         aux.add((ImageView) findViewById(R.id.square1_1));
         aux.add((ImageView) findViewById(R.id.square1_2));
         aux.add((ImageView) findViewById(R.id.square1_3));
         aux.add((ImageView) findViewById(R.id.square1_4));
         aux.add((ImageView) findViewById(R.id.square1_5));
         aux.add((ImageView) findViewById(R.id.square1_6));
         aux.add((ImageView) findViewById(R.id.square1_7));
         aux.add((ImageView) findViewById(R.id.square1_8));
         aux.add((ImageView) findViewById(R.id.square1_9));
         aux.add((ImageView) findViewById(R.id.square1_10));
         square.add(aux);
         
         aux = new ArrayList<ImageView>(10);
         aux.add((ImageView) findViewById(R.id.square2_1));
         aux.add((ImageView) findViewById(R.id.square2_2));
         aux.add((ImageView) findViewById(R.id.square2_3));
         aux.add((ImageView) findViewById(R.id.square2_4));
         aux.add((ImageView) findViewById(R.id.square2_5));
         aux.add((ImageView) findViewById(R.id.square2_6));
         aux.add((ImageView) findViewById(R.id.square2_7));
         aux.add((ImageView) findViewById(R.id.square2_8));
         aux.add((ImageView) findViewById(R.id.square2_9));
         aux.add((ImageView) findViewById(R.id.square2_10));
         square.add(aux);
         
         aux = new ArrayList<ImageView>(10);
         aux.add((ImageView) findViewById(R.id.square3_1));
         aux.add((ImageView) findViewById(R.id.square3_2));
         aux.add((ImageView) findViewById(R.id.square3_3));
         aux.add((ImageView) findViewById(R.id.square3_4));
         aux.add((ImageView) findViewById(R.id.square3_5));
         aux.add((ImageView) findViewById(R.id.square3_6));
         aux.add((ImageView) findViewById(R.id.square3_7));
         aux.add((ImageView) findViewById(R.id.square3_8));
         aux.add((ImageView) findViewById(R.id.square3_9));
         aux.add((ImageView) findViewById(R.id.square3_10));
         square.add(aux);
         
         
         aux = new ArrayList<ImageView>(10);
         aux.add((ImageView) findViewById(R.id.square4_1));
         aux.add((ImageView) findViewById(R.id.square4_2));
         aux.add((ImageView) findViewById(R.id.square4_3));
         aux.add((ImageView) findViewById(R.id.square4_4));
         aux.add((ImageView) findViewById(R.id.square4_5));
         aux.add((ImageView) findViewById(R.id.square4_6));
         aux.add((ImageView) findViewById(R.id.square4_7));
         aux.add((ImageView) findViewById(R.id.square4_8));
         aux.add((ImageView) findViewById(R.id.square4_9));
         aux.add((ImageView) findViewById(R.id.square4_10));
         square.add(aux);
         
         aux = new ArrayList<ImageView>(10);
         aux.add((ImageView) findViewById(R.id.square5_1));
         aux.add((ImageView) findViewById(R.id.square5_2));
         aux.add((ImageView) findViewById(R.id.square5_3));
         aux.add((ImageView) findViewById(R.id.square5_4));
         aux.add((ImageView) findViewById(R.id.square5_5));
         aux.add((ImageView) findViewById(R.id.square5_6));
         aux.add((ImageView) findViewById(R.id.square5_7));
         aux.add((ImageView) findViewById(R.id.square5_8));
         aux.add((ImageView) findViewById(R.id.square5_9));
         aux.add((ImageView) findViewById(R.id.square5_10));
         square.add(aux);
         
         aux = new ArrayList<ImageView>(10);
         aux.add((ImageView) findViewById(R.id.square6_1));
         aux.add((ImageView) findViewById(R.id.square6_2));
         aux.add((ImageView) findViewById(R.id.square6_3));
         aux.add((ImageView) findViewById(R.id.square6_4));
         aux.add((ImageView) findViewById(R.id.square6_5));
         aux.add((ImageView) findViewById(R.id.square6_6));
         aux.add((ImageView) findViewById(R.id.square6_7));
         aux.add((ImageView) findViewById(R.id.square6_8));
         aux.add((ImageView) findViewById(R.id.square6_9));
         aux.add((ImageView) findViewById(R.id.square6_10));
         square.add(aux);
         
         aux = new ArrayList<ImageView>(10);
         aux.add((ImageView) findViewById(R.id.square7_1));
         aux.add((ImageView) findViewById(R.id.square7_2));
         aux.add((ImageView) findViewById(R.id.square7_3));
         aux.add((ImageView) findViewById(R.id.square7_4));
         aux.add((ImageView) findViewById(R.id.square7_5));
         aux.add((ImageView) findViewById(R.id.square7_6));
         aux.add((ImageView) findViewById(R.id.square7_7));
         aux.add((ImageView) findViewById(R.id.square7_8));
         aux.add((ImageView) findViewById(R.id.square7_9));
         aux.add((ImageView) findViewById(R.id.square7_10));
         square.add(aux);
         
         aux = new ArrayList<ImageView>(10);
         aux.add((ImageView) findViewById(R.id.square8_1));
         aux.add((ImageView) findViewById(R.id.square8_2));
         aux.add((ImageView) findViewById(R.id.square8_3));
         aux.add((ImageView) findViewById(R.id.square8_4));
         aux.add((ImageView) findViewById(R.id.square8_5));
         aux.add((ImageView) findViewById(R.id.square8_6));
         aux.add((ImageView) findViewById(R.id.square8_7));
         aux.add((ImageView) findViewById(R.id.square8_8));
         aux.add((ImageView) findViewById(R.id.square8_9));
         aux.add((ImageView) findViewById(R.id.square8_10));
         square.add(aux);
         
         aux = new ArrayList<ImageView>(10);
         aux.add((ImageView) findViewById(R.id.square9_1));
         aux.add((ImageView) findViewById(R.id.square9_2));
         aux.add((ImageView) findViewById(R.id.square9_3));
         aux.add((ImageView) findViewById(R.id.square9_4));
         aux.add((ImageView) findViewById(R.id.square9_5));
         aux.add((ImageView) findViewById(R.id.square9_6));
         aux.add((ImageView) findViewById(R.id.square9_7));
         aux.add((ImageView) findViewById(R.id.square9_8));
         aux.add((ImageView) findViewById(R.id.square9_9));
         aux.add((ImageView) findViewById(R.id.square9_10));
         square.add(aux);
         
         aux = new ArrayList<ImageView>(10);
         aux.add((ImageView) findViewById(R.id.square10_1));
         aux.add((ImageView) findViewById(R.id.square10_2));
         aux.add((ImageView) findViewById(R.id.square10_3));
         aux.add((ImageView) findViewById(R.id.square10_4));
         aux.add((ImageView) findViewById(R.id.square10_5));
         aux.add((ImageView) findViewById(R.id.square10_6));
         aux.add((ImageView) findViewById(R.id.square10_7));
         aux.add((ImageView) findViewById(R.id.square10_8));
         aux.add((ImageView) findViewById(R.id.square10_9));
         aux.add((ImageView) findViewById(R.id.square10_10));
         square.add(aux);
         for (int i=0; i < 10; i++)
         	for(int j=0; j<10;j++)
         		cmp[i][j]=0;
     }
         
     public void onclickchip(View v){
     	switch(v.getId()){
     	
     	case R.id.portaavioes:	
     		this.type_navio = 5;
     		this.navio = true;
     	break;
     	
     	case R.id.destroier:	
     		this.type_navio = 4;
     		this.navio = true;
     	break;
     	
     	case R.id.cruzador:	
     		this.type_navio = 3;
     		this.navio = true;
     	break;
     	
     	case R.id.submarino:	
     		this.type_navio = 1;
     		this.navio = true;
     	break;
     	
     	}
     }    
     public void onclick(View v){
     	
     	if( type_navio != 0 && navio ){
     		switch(type_navio){
     			case 5:
     				insert_pa(v);
     			break;
     			case 4:
     				insert_destroier(v);
     			break;
     			case 3:
     				insert_cruzador(v);
 				break;
     			case 1:
     				insert_submarino(v);
     			break;
     		}
     	}
     } 
     
     private void insert_pa(View v){
     	int[] coordenada = coordenadas(v);
     	TextView txt2 = (TextView) findViewById(R.id.txterror);
     	txt2.setText("x"+ coordenada[1] + "Y" + this.type_navio +"test "+(coordenada[1]+this.type_navio));
 
     	if(coordenada[1]+5 <= 11){
     		ArrayList<ImageView> line = square.get(coordenada[0]-1);
     		boolean valid = true;
     		int j = coordenada[1]-1;
     		while (j < coordenada[1]+5-1){
     			if(cmp[coordenada[0]-1][j] == 1){
     				valid = false;
     		
     			}
     			j++;
     		}
     		
     		
     		ImageView img2;
     		img2 = (ImageView) v;
     		if(valid == true && portaavioes < 2){
     		
     			j=coordenada[1]-1;
 	    		while (j < coordenada[1]+this.type_navio-1){
 	    			cmp[coordenada[0]-1][j]=1;
 	    			img2 = line.get(j);
 	    			img2.setImageResource(R.drawable.pa);
 		    		j++;
 	    		}
 	    		portaavioes++;
     		}
 		}
     }
     
     private void insert_destroier(View v){
     	int[] coordenada = coordenadas(v);
     	TextView txt2 = (TextView) findViewById(R.id.txterror);
     	txt2.setText("x"+ coordenada[1] + "Y" + this.type_navio +"test "+(coordenada[1]+this.type_navio));
 
     	if(coordenada[1]+4 <= 11){
     		ArrayList<ImageView> line = square.get(coordenada[0]-1);
     		boolean valid = true;
     		int j = coordenada[1]-1;
     		while (j < coordenada[1]+3){
     			if(cmp[coordenada[0]-1][j] == 1){
     				valid = false;
     		
     			}
     			j++;
     		}
     		
     		
     		ImageView img2;
     		img2 = (ImageView) v;
     		if(valid == true && destroier < 2){
     			j=coordenada[1]-1;
 	    		while (j < coordenada[1]+3){
 	    			cmp[coordenada[0]-1][j]=1;
 	    			img2 = line.get(j);
 	    			img2.setImageResource(R.drawable.destroier);
 		    		j++;
 	    		}
 	    		destroier++;
     		}
 		}
     }
     
     private void insert_cruzador(View v){
     	int[] coordenada = coordenadas(v);
     	TextView txt2 = (TextView) findViewById(R.id.txterror);
     	txt2.setText("x"+ coordenada[1] + "Y" + this.type_navio +"test "+(coordenada[1]+this.type_navio));
 
    	if(coordenada[1]+3 <= 11){
     		ArrayList<ImageView> line = square.get(coordenada[0]-1);
     		boolean valid = true;
     		int j = coordenada[1]-1;
     		while (j < coordenada[1]+2){
     			if(cmp[coordenada[0]-1][j] == 1){
     				valid = false;
     			}
     			j++;
     		}
     		
     		ImageView img2;
     		img2 = (ImageView) v;
     		if(valid == true && cruzador < 3){
     			j=coordenada[1]-1;
 	    		while (j < coordenada[1]+2){
 	    			cmp[coordenada[0]-1][j]=1;
 	    			img2 = line.get(j);
 	    			img2.setImageResource(R.drawable.cruzador);
 		    		j++;
 	    		}
 	    		cruzador++;
     		}
 		}
     }
     
     private void insert_submarino(View v){
     	int[] coordenada = coordenadas(v);
     	TextView txt2 = (TextView) findViewById(R.id.txterror);
     	txt2.setText("x"+ coordenada[1] + "Y" + this.type_navio +"test "+(coordenada[1]+this.type_navio));
 
     	if(coordenada[1] <= 11){
     		ArrayList<ImageView> line = square.get(coordenada[0]-1);
     		boolean valid = true;
     		int j = coordenada[1]-1;
     		while (j < coordenada[1]){
     			if(cmp[coordenada[0]-1][j] == 1){
     				valid = false;
     		
     			}
     			j++;
     		}
     		
     		
     		ImageView img2;
     		img2 = (ImageView) v;
     		if(valid == true && submarino < 5){
     			j=coordenada[1]-1;
 	    		while (j < coordenada[1]){
 	    			cmp[coordenada[0]-1][j]=1;
 	    			img2 = line.get(j);
 	    			img2.setImageResource(R.drawable.submarino);
 		    		j++;
 	    		}
 	    		submarino++;
     		}
 		}
     }
     
     
     private int[] coordenadas(View v){
     	int[] aux = new int[2]; 
     	switch (v.getId()){
     	case R.id.square1_1:
     		aux[0] = 1;
 			aux[1] = 1;
     	break;
     	
     	case R.id.square1_2:
     		aux[0] = 1;
 			aux[1] = 2;
     	break;
     	
     	case R.id.square1_3:
     		aux[0] = 1;
 			aux[1] = 3;
     	break;
     	
     	case R.id.square1_4:
     		aux[0] = 1;
 			aux[1] = 4;
     	break;
     	
     	case R.id.square1_5:
     		aux[0] = 1;
 			aux[1] = 5;
     	break;
     	
     	case R.id.square1_6:
     		aux[0] = 1;
 			aux[1] = 6;
     	break;
     	
     	case R.id.square1_7:
     		aux[0] = 1;
 			aux[1] = 7;
     	break;
     	
     	case R.id.square1_8:
     		aux[0] = 1;
 			aux[1] = 8;
     	break;
     	
     	case R.id.square1_9:
     		aux[0] = 1;
 			aux[1] = 9;
     	break;
     	
     	case R.id.square1_10:
     		aux[0] = 1;
 			aux[1] = 10;
     	break;
     	
     	case R.id.square2_1:
     		aux[0] = 2;
 			aux[1] = 1;
     	break;
     	
     	case R.id.square2_2:
     		aux[0] = 2;
 			aux[1] = 2;
     	break;
     	
     	case R.id.square2_3:
     		aux[0] = 2;
 			aux[1] = 3;
     	break;
     	
     	case R.id.square2_4:
     		aux[0] = 2;
 			aux[1] = 4;
     	break;
     	
     	case R.id.square2_5:
     		aux[0] = 2;
 			aux[1] = 5;
     	break;
     	
     	case R.id.square2_6:
     		aux[0] = 2;
 			aux[1] = 6;
     	break;
     	
     	case R.id.square2_7:
     		aux[0] = 2;
 			aux[1] = 7;
     	break;
     	
     	case R.id.square2_8:
     		aux[0] = 2;
 			aux[1] = 8;
     	break;
     	
     	case R.id.square2_9:
     		aux[0] = 2;
 			aux[1] = 9;
     	break;
     	
     	case R.id.square2_10:
     		aux[0] = 2;
 			aux[1] = 10;
     	break;
     	
     	case R.id.square3_1:
     		aux[0] = 3;
 			aux[1] = 1;
     	break;
     	
     	case R.id.square3_2:
     		aux[0] = 3;
 			aux[1] = 2;
     	break;
     	
     	case R.id.square3_3:
     		aux[0] = 3;
 			aux[1] = 3;
     	break;
     	
     	case R.id.square3_4:
     		aux[0] = 3;
 			aux[1] = 4;
     	break;
     	
     	case R.id.square3_5:
     		aux[0] = 3;
 			aux[1] = 5;
     	break;
     	
     	case R.id.square3_6:
     		aux[0] = 3;
 			aux[1] = 6;
     	break;
     	
     	case R.id.square3_7:
     		aux[0] = 3;
 			aux[1] = 7;
     	break;
     	
     	case R.id.square3_8:
     		aux[0] = 3;
 			aux[1] = 8;
     	break;
     	
     	case R.id.square3_9:
     		aux[0] = 3;
 			aux[1] = 9;
     	break;
     	
     	case R.id.square3_10:
     		aux[0] = 3;
 			aux[1] = 10;
     	break;
     	
     	case R.id.square4_1:
     		aux[0] = 4;
 			aux[1] = 1;
     	break;
     	
     	case R.id.square4_2:
     		aux[0] = 4;
 			aux[1] = 2;
     	break;
     	
     	case R.id.square4_3:
     		aux[0] = 4;
 			aux[1] = 3;
     	break;
     	
     	case R.id.square4_4:
     		aux[0] = 4;
 			aux[1] = 4;
     	break;
     	
     	case R.id.square4_5:
     		aux[0] = 4;
 			aux[1] = 5;
     	break;
     	
     	case R.id.square4_6:
     		aux[0] = 4;
 			aux[1] = 6;
     	break;
     	
     	case R.id.square4_7:
     		aux[0] = 4;
 			aux[1] = 7;
     	break;
     	
     	case R.id.square4_8:
     		aux[0] = 4;
 			aux[1] = 8;
     	break;
     	
     	case R.id.square4_9:
     		aux[0] = 4;
 			aux[1] = 9;
     	break;
     	
     	case R.id.square4_10:
     		aux[0] = 4;
 			aux[1] = 10;
     	break;
     	
     	case R.id.square5_1:
     		aux[0] = 5;
 			aux[1] = 1;
     	break;
     	
     	case R.id.square5_2:
     		aux[0] = 5;
 			aux[1] = 2;
     	break;
     	
     	case R.id.square5_3:
     		aux[0] = 5;
 			aux[1] = 3;
     	break;
     	
     	case R.id.square5_4:
     		aux[0] = 5;
 			aux[1] = 4;
     	break;
     	
     	case R.id.square5_5:
     		aux[0] = 5;
 			aux[1] = 5;
     	break;
     	
     	case R.id.square5_6:
     		aux[0] = 5;
 			aux[1] = 6;
     	break;
     	
     	case R.id.square5_7:
     		aux[0] = 5;
 			aux[1] = 7;
     	break;
     	
     	case R.id.square5_8:
     		aux[0] = 5;
 			aux[1] = 8;
     	break;
     	
     	case R.id.square5_9:
     		aux[0] = 5;
 			aux[1] = 9;
     	break;
     	
     	case R.id.square5_10:
     		aux[0] = 5;
 			aux[1] = 10;
     	break;
         
     	case R.id.square6_1:
     		aux[0] = 6;
 			aux[1] = 1;
     	break;
     	
     	case R.id.square6_2:
     		aux[0] = 6;
 			aux[1] = 2;
     	break;
     	
     	case R.id.square6_3:
     		aux[0] = 6;
 			aux[1] = 3;
     	break;
     	
     	case R.id.square6_4:
     		aux[0] = 6;
 			aux[1] = 4;
     	break;
     	
     	case R.id.square6_5:
     		aux[0] = 6;
 			aux[1] = 5;
     	break;
     	
     	case R.id.square6_6:
     		aux[0] = 6;
 			aux[1] = 6;
     	break;
     	
     	case R.id.square6_7:
     		aux[0] = 6;
 			aux[1] = 7;
     	break;
     	
     	case R.id.square6_8:
     		aux[0] = 6;
 			aux[1] = 8;
     	break;
     	
     	case R.id.square6_9:
     		aux[0] = 6;
 			aux[1] = 9;
     	break;
     	
     	case R.id.square6_10:
     		aux[0] = 6;
 			aux[1] = 10;
     	break;
     	
     	case R.id.square7_1:
     		aux[0] = 7;
 			aux[1] = 1;
     	break;
     	
     	case R.id.square7_2:
     		aux[0] = 7;
 			aux[1] = 2;
     	break;
     	
     	case R.id.square7_3:
     		aux[0] = 7;
 			aux[1] = 3;
     	break;
     	
     	case R.id.square7_4:
     		aux[0] = 7;
 			aux[1] = 4;
     	break;
     	
     	case R.id.square7_5:
     		aux[0] = 7;
 			aux[1] = 5;
     	break;
     	
     	case R.id.square7_6:
     		aux[0] = 7;
 			aux[1] = 6;
     	break;
     	
     	case R.id.square7_7:
     		aux[0] = 7;
 			aux[1] = 7;
     	break;
     	
     	case R.id.square7_8:
     		aux[0] = 7;
 			aux[1] = 8;
     	break;
     	
     	case R.id.square7_9:
     		aux[0] = 7;
 			aux[1] = 9;
     	break;
     	
     	case R.id.square7_10:
     		aux[0] = 7;
 			aux[1] = 10;
     	break;
     	
     	case R.id.square8_1:
     		aux[0] = 8;
 			aux[1] = 1;
     	break;
     	
     	case R.id.square8_2:
     		aux[0] = 8;
 			aux[1] = 2;
     	break;
     	
     	case R.id.square8_3:
     		aux[0] = 8;
 			aux[1] = 3;
     	break;
     	
     	case R.id.square8_4:
     		aux[0] = 8;
 			aux[1] = 4;
     	break;
     	
     	case R.id.square8_5:
     		aux[0] = 8;
 			aux[1] = 5;
     	break;
     	
     	case R.id.square8_6:
     		aux[0] = 8;
 			aux[1] = 6;
     	break;
     	
     	case R.id.square8_7:
     		aux[0] = 8;
 			aux[1] = 7;
     	break;
     	
     	case R.id.square8_8:
     		aux[0] = 8;
 			aux[1] = 8;
     	break;
     	
     	case R.id.square8_9:
     		aux[0] = 8;
 			aux[1] = 9;
     	break;
     	
     	case R.id.square8_10:
     		aux[0] = 8;
 			aux[1] = 10;
     	break;
     	
     	case R.id.square9_1:
     		aux[0] = 9;
 			aux[1] = 1;
     	break;
     	
     	case R.id.square9_2:
     		aux[0] = 9;
 			aux[1] = 2;
     	break;
     	
     	case R.id.square9_3:
     		aux[0] = 9;
 			aux[1] = 3;
     	break;
     	
     	case R.id.square9_4:
     		aux[0] = 9;
 			aux[1] = 4;
     	break;
     	
     	case R.id.square9_5:
     		aux[0] = 9;
 			aux[1] = 5;
     	break;
     	
     	case R.id.square9_6:
     		aux[0] = 9;
 			aux[1] = 6;
     	break;
     	
     	case R.id.square9_7:
     		aux[0] = 9;
 			aux[1] = 7;
     	break;
     	
     	case R.id.square9_8:
     		aux[0] = 9;
 			aux[1] = 8;
     	break;
     	
     	case R.id.square9_9:
     		aux[0] = 9;
 			aux[1] = 9;
     	break;
     	
     	case R.id.square9_10:
     		aux[0] = 9;
 			aux[1] = 10;
     	break;
     	
     	case R.id.square10_1:
     		aux[0] = 10;
 			aux[1] = 1;
     	break;
     	
     	case R.id.square10_2:
     		aux[0] = 10;
 			aux[1] = 2;
     	break;
     	
     	case R.id.square10_3:
     		aux[0] = 10;
 			aux[1] = 3;
     	break;
     	
     	case R.id.square10_4:
     		aux[0] = 10;
 			aux[1] = 4;
     	break;
     	
     	case R.id.square10_5:
     		aux[0] = 10;
 			aux[1] = 5;
     	break;
     	
     	case R.id.square10_6:
     		aux[0] = 10;
 			aux[1] = 6;
     	break;
     	
     	case R.id.square10_7:
     		aux[0] = 10;
 			aux[1] = 7;
     	break;
     	
     	case R.id.square10_8:
     		aux[0] = 10;
 			aux[1] = 8;
     	break;
     	
     	case R.id.square10_9:
     		aux[0] = 10;
 			aux[1] = 9;
     	break;
     	
     	case R.id.square10_10:
     		aux[0] = 10;
 			aux[1] = 10;
     	break;
     	
     	default:
     		aux = null;
     	break;
     	
     	}
     	
     	return aux;
     }
     
 }
