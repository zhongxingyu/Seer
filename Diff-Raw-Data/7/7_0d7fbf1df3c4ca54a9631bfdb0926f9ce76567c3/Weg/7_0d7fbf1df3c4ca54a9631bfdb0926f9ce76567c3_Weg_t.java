 package com.enpasos.navi;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Weg {
     
     
     List<Strasse> strassen;
     List<Stadt> staedteAufWeg;
     
     public Weg() {
         strassen = new ArrayList<Strasse>();
         staedteAufWeg = new ArrayList<Stadt>();
     }
 
     public void add(Stadt startStadt, Strasse s, Stadt zielStadt) {
         strassen.add(s);
         if (staedteAufWeg.size() == 0)
             staedteAufWeg.add(startStadt);
         staedteAufWeg.add(zielStadt);
     }
     
     public int laenge() {
         int l = 0;
         for (Strasse s : strassen) {
             l += s.laenge;
         }
         return l;
     }
     
     @Override
     public Weg clone() {
         Weg clone = new Weg();
         clone.strassen.addAll(strassen);
        clone.staedteAufWeg.addAll(staedteAufWeg);
         return clone;
     }
     
     
     public String toString() {
         StringBuffer buf = new StringBuffer();
         buf.append("Weglänge: " + laenge());
         buf.append("\n");
 
        int count  = 1;
         for (Stadt s : staedteAufWeg) {
             buf.append(s.toString());
            if (count++ < staedteAufWeg.size())
                buf.append(" > ");
         }
         return buf.toString();
     }
 }
