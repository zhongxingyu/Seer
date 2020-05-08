 /* 
  * Copyright (C) 2007 Aram Julhakyan (Buscador.java)
  * Copyright (C) 2011 A. Sofyan Wahyudin
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package bin.logic;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.Vector;
 
 public class Pencari {
     private final int MAX_RECORDS = 25;   // 30
     String index[][];
     String encoding = "UTF-8";
     String idx_file ="";
 
     private int[] idx = new int[58];    // index posisi sesuai alpabet
     
     public Pencari() {
     }
     
     public void setIndexFile(String index_file) throws IOException{
         Vector v = new Vector(50, 10);
         StringBuffer reading = new StringBuffer(7);
         int ch = 0;
         idx_file = index_file; // <# awal nama
         
         InputStream is = this.getClass().getResourceAsStream("/" + index_file);
         InputStreamReader ir;
         
         try {
             ir = new InputStreamReader(is, encoding);
         } catch (UnsupportedEncodingException ex) {
             ir = new InputStreamReader(is, "UTF-8");
         }
          
          while ((ch = ir.read()) > -1){
             if (ch == '\n'){ 
                 v.addElement(reading.toString());
                 reading.setLength(0);
             }else{
                 if (ch == '#'){
                     v.addElement(reading.toString());
                     reading.setLength(0);
                 }else{
                     reading.append((char)ch);
                 }
             }
          }
          
         ir.close();
         
         if (reading.length() > 0){
              v.addElement(reading.toString());
         }
          
         index = new String[v.size()/3][3];
         for(int i=0; i<v.size(); i++){
             index[i/3][(i+3)%3] = (String) v.elementAt(i);
             //System.out.println((i/3) + " - " + ((i+3)%3) + " = " + index[i/3][(i+3)%3] );
         }
         
         v = null;
         
         setArrayIdx();
     }
     
     
     private void setArrayIdx(){
         
         char old_c= ' ', c;
         int ic, mulai;
         
         for (int i=0; i<idx.length; i++){   // hapus isi
             idx[i]=0;
         }
         
         for (int i=0; i<index.length; i++){ // catet diamana mulai satiap karakter
             c = index[i][0].charAt(0);
             ic = (int)c; 
                         
             if ((ic>=65 & ic<=122) && c!=old_c){
                     mulai = i-1;
                     if (mulai<0) mulai = 0;
 
                     idx[c-65] = mulai;
 
                     old_c = c;
             }
         }    
     }
     
     private int getStartIdx(String s){
 
         try {
             int ic = (int)s.charAt(0); 
             
             if (ic>=65 & ic<=122){
                 //System.out.println("start "+ idx[ic-65]);
                 return  idx[ic-65];
             }
         } catch (Exception ex) {
             //return 0;
         }
         
         return 0;
     }
     
     public String[] startSearch(String s){  // memulai search
         
         String ini="";
         String[] r1 = null;
         String[] r2 = null;
         
         int mulai = getStartIdx(s);
         
         if (s!=null | s.equals("")==false) {
               
             boolean second = false;  // kedua
             for (int i = mulai; i < index.length; i++){
                 //System.out.println(index[i][0] + " - " + index[i][1] + " - " + index[i][2]);
                 ini = index[i][0];
                 if (ini.length()>s.length()){
                     ini = ini.substring(0, s.length());
                 }
                 if ( (s.compareTo(ini)>=0 ) && ( s.compareTo(index[i][1])<=0) ){
 
                     try {
                         if (second == false){  // kedua
                             r1 = search(s, index[i][2], MAX_RECORDS);
                             second = true;
                             if (r1.length == MAX_RECORDS)
                                 return r1;
                         }else{
                             if (r1 !=null){
                                 r2 = search(s, index[i][2], MAX_RECORDS - r1.length);
                             return join(r1, r2); 
                             }
                         }
                         //return search(s, index[i][2]);
                     } catch (IOException ex) {
                         //ex.printStackTrace();
                     }
                 }
             } // for
            System.out.println(r1.length);
             // jika result nihil, chek apakan input lebih dari dua kata lalu cari masing2 kata
            if (r1.length==0 && s.indexOf(" ")!=-1){
                 String[] sPart = split(s, " ");
                 Vector words = new Vector();
                 
                 for (int i=0; i<sPart.length; i++){
                     mulai = getStartIdx(sPart[i]);
                     for (int j = mulai; j < index.length; j++)
                         if ( (sPart[i].compareTo(index[j][0])>=0 ) && (sPart[i].compareTo(index[j][1]) <= 0 ) ){
                             try {
                                 String ret = "";
                                 ret = singleSearch(sPart[i], index[j][2]);
                                 
                                 if (ret!=null & ret.trim().length()>0)
                                     words.addElement(ret);
                                 
                             } catch (IOException ex) {
                                 //ex.printStackTrace();
                             }
                             
                         }
                 }
                 
                 r1 = new String[words.size()];
                 for(int i=0; i<words.size(); i++)
                     r1[i] = (String) words.elementAt(i);
                 
             }
                
         }
         
         return r1;
     }
     
     // <# paranti maluruh teks
     public String [] search(String word, String file, int records_to_return) throws  IOException{
         
         String dic_file = idx_file + "x" + file;
         InputStream is = this.getClass().getResourceAsStream("/dic/" + dic_file); // <# lokasi kamus
         StringBuffer reading = new StringBuffer();
         boolean wordOrder = false;
         boolean wordList = false;
         short found = 0; //indicara el numero de palabras encontradas, lo limitamos a 30
         Vector words = new Vector(20, 10);
         int ch=0;
 
         InputStreamReader ir;
         try {
             ir = new InputStreamReader(is, encoding);
         } catch (UnsupportedEncodingException ex) {
             ir = new InputStreamReader(is, "UTF-8");
         }
         
         while ((ch = ir.read()) > -1) {
             if (ch=='\n'){  // akhir baris
                 reading.setLength(0);
                 wordOrder = false;
                 wordList = false;
             }else{
                 if (ch != '#' && wordOrder == false){
                     reading.append((char)ch);
                 }else{
                     if (wordList == false){
                         wordOrder = true;
                         if (reading.length() >= word.length())
                             if (word.compareTo(reading.toString().substring(0, word.length())) == 0){
                                 words.addElement(reading.toString());
                                 found ++;
                                 
                                 if (found==records_to_return) break;
                             }
                         wordList = true;
                     }
                 }
             }
         }         
         
         is.close();
         
         String wrds[] = new String[words.size()];
         for(int i=0; i<words.size(); i++)
             wrds[i] = (String) words.elementAt(i);
         
         return wrds;
     }
     
     public String singleSearch(String word, String file) throws  IOException{
         
         String dic_file = idx_file + "x" + file;
         InputStream is = this.getClass().getResourceAsStream("/dic/" + dic_file); // <# lokasi kamus
         StringBuffer reading = new StringBuffer();
         boolean wordOrder = false;
         int ch=0;
         String ret="";
         
         InputStreamReader ir;
         try {
             ir = new InputStreamReader(is, encoding);
         } catch (UnsupportedEncodingException ex) {
             ir = new InputStreamReader(is, "UTF-8");
         }
         
         while ((ch = ir.read()) > -1) {
             if (ch=='\n'){  // akhir baris
                 reading.setLength(0);
                 wordOrder = false;
             }else{
                 if (ch != '#' && wordOrder == false){
                     reading.append((char)ch);
                 }else{
                     wordOrder = true;
                     if (reading.length() >= word.length())
                         if (word.compareTo(reading.toString().substring(0, word.length())) == 0){
                             ret = reading.toString();
                             break;
                         }
                 }
             }
         }         
         
         is.close();
         
         return ret;
     }
     
     // <# maluruh hartina
     public String searchExactWord(String word) throws IOException{
         String file=null;
         
         int mulai = getStartIdx(word);
         
         for (int i = mulai; i < index.length; i++){
             if ( (word.compareTo(index[i][0])>=0 ) && (word.compareTo(index[i][1]) <= 0 ) ){
                    file= index[i][2];
                    break;
             }
         }
         
         if (file == null) return null;
         
         StringBuffer reading = new StringBuffer(13);
         int ch = 0;
         boolean b = false;
         
         String dic_file = idx_file + "x" + file;
         
         InputStream is = this.getClass().getResourceAsStream("/dic/" + dic_file);
         InputStreamReader ir;
         
         try {
             ir = new InputStreamReader(is, encoding);
         } catch (UnsupportedEncodingException ex) {
             ir = new InputStreamReader(is, "UTF-8");
         }
         
         while ((ch = ir.read()) > -1){
             if (ch=='\n'){
                 if(b == true)
                     break;
                 reading.setLength(0);
                 b = false;
             }else{
                 if (ch=='#'){
                     if(reading.toString().compareTo(word)==0){
                         b = true;
                         reading.setLength(0);
                     }
                 }else{
                      reading.append((char)ch);
                 }
             }
         }
         
         is.close();
         
         return reading.toString();
     }
 
     private String[] join(String[] r1, String[] r2) {
         if (r1 !=null && r2 != null){
             String [] r3 = new String[r1.length + r2.length];
             int j = r1.length;
             for (int i=0; i < r1.length; i++){
                 r3[i] = r1[i];
             }
             for (int i = 0; i < r2.length; i++){
                 r3[j+i] = r2[i];
             }
             return r3;
         }
         return null;
     }
     
     private String[] split(String s, String separator) {
         Vector nodes = new Vector();
         // Parse nodes into vector
         int index = s.indexOf(separator);
         while(index >= 0) {
             nodes.addElement( s.substring(0, index) );
             s = s.substring(index+separator.length());
             index = s.indexOf(separator);
         }
         // Get the last node
         nodes.addElement( s );
 
         // Create split string array
         String[] result = new String[ nodes.size() ];
         if( nodes.size() > 0 ) {
             for(int loop = 0; loop < nodes.size(); loop++)
             {
                 result[loop] = (String)nodes.elementAt(loop);
                 System.out.println(result[loop]);
             }
 
         }
         return result;
     }
 
     
 }
