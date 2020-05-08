 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package projetcodagecrypto;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author Benjamin
  */
 public class MoveToFront {
     
     private int position;
     private Ascii ascii;
     private ArrayList<Integer> tableau;
     
     public MoveToFront(int pos, Ascii as)
     {
         ascii = as;
         initTableau();
         position = pos;
     }
     
     
     public void initTableau()
     {
         tableau = new ArrayList<>();
         for(int i=0 ; i<256 ; i++)
             tableau.add(i);
     }
     public ArrayList<Integer> compressionMTF(ArrayList<Integer> mot)
     {
         ArrayList<Integer> code = new ArrayList<Integer>();
        for(int lettre : mot)
         {
             code.add(tableau.indexOf(lettre));
             //si c'est la première lettre du tableau pas besoin de bouger sinon oui
             if(lettre != tableau.get(0))
             {
                tableau.remove(tableau.get(lettre));
                 tableau.add(0, lettre);
             }
         }
         /*System.out.println("Tableau : " + tableau);
         System.out.println("Code obtenu avec MVT :" + code);
         */
         return code;
     }
     
     public ArrayList<Integer> decompressionMTF(ArrayList<Integer> code)
     {
         ArrayList<Integer> decode = new ArrayList<Integer>();
         initTableau();
         for(int chiffre : code)
         {
             Integer lettre = tableau.get(chiffre);
             decode.add(tableau.get(chiffre));
             
             //si c'est la première lettre du tableau pas besoin de bouger sinon oui
             if(chiffre != 0)
             {                
                 tableau.remove(lettre);
                 tableau.add(0, lettre);
             }
             
         }
         return decode;
     }
 }
