 package com.enpasos.navi;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class App {
     
    
 
     public static void main(String[] args) {
         Landschaft landschaft = getLandschaft();
         Stadt startStadt = landschaft.getStadt("O");
         startStadt.kuerzesterWegLaenge = 0;
        Stadt endeStadt = landschaft.getStadt("H");
         
         Weg weg = AmeisenGott.getKuerzesterWeg(startStadt, endeStadt);
         System.out.println(weg.toString());
     }
     
 
     public static Landschaft getLandschaft() {
         Landschaft land = new Landschaft();
         Stadt z = new Stadt("Z");
         Stadt o = new Stadt("O");
         Stadt f = new Stadt("F");
         Stadt p = new Stadt("P");
         Stadt h = new Stadt("H");
         Stadt y = new Stadt("Y");    
         Stadt g = new Stadt("G");
         Stadt l = new Stadt("L");
         Stadt d = new Stadt("D");
         Stadt a = new Stadt("A");
         Stadt m = new Stadt("M");
         Stadt i = new Stadt("I");
         Stadt c = new Stadt("C");
         Stadt x = new Stadt("X");
         Stadt n = new Stadt("N");
         Stadt b = new Stadt("B");
         Stadt e = new Stadt("E");
         Stadt k = new Stadt("K");
         
         Strasse ke = new Strasse(k, 31, e);
         Strasse oe = new Strasse(o, 102, e);
         Strasse of = new Strasse(o, 14, f);
         Strasse op = new Strasse(o, 91, p);
         Strasse fp = new Strasse(f, 57, p);
         Strasse fk = new Strasse(f, 29, k);
         Strasse fe = new Strasse(f, 79, e);
         Strasse kg = new Strasse(k, 58, g);
         Strasse ky = new Strasse(k, 20, y);
         Strasse kh = new Strasse(k, 31, h);
         Strasse kp = new Strasse(k, 25, p);
         Strasse eg = new Strasse(e, 60, g);
         Strasse gy = new Strasse(g, 30, y);
         Strasse gl = new Strasse(g, 58, l);
         Strasse hp = new Strasse(h, 34, p);
         Strasse hi = new Strasse(h, 65, i);
         Strasse bh = new Strasse(b, 30, h);
         Strasse hl = new Strasse(h, 106, l);
         Strasse yz = new Strasse(y, 23, z);
         Strasse bz = new Strasse(b, 32, z);
         Strasse bd = new Strasse(b, 64, d);
         Strasse ab = new Strasse(a, 69, b);
         Strasse bx = new Strasse(b, 26, x);
         Strasse bl = new Strasse(b, 34, l);
         Strasse nz = new Strasse(n, 30, z);
         Strasse ad = new Strasse(a, 36, d);
         Strasse an = new Strasse(a, 22, n);
         Strasse am = new Strasse(a, 36, m);
         Strasse mx = new Strasse(m, 12, x);
         Strasse ml = new Strasse(m, 43, l);
         Strasse cm = new Strasse(c, 31, m);
         Strasse cl = new Strasse(c, 40, l);
         Strasse cx = new Strasse(c, 23, x);
         Strasse dl = new Strasse(d, 95, l);
         
         land.staedte.add(z);
         land.staedte.add(o);
         land.staedte.add(f);
         land.staedte.add(p);
         land.staedte.add(h);
         land.staedte.add(y);
         land.staedte.add(g);
         land.staedte.add(h);
         land.staedte.add(d);
         land.staedte.add(a);
         land.staedte.add(e);
         land.staedte.add(i);
         land.staedte.add(c);
         land.staedte.add(x);
         land.staedte.add(n);
         land.staedte.add(b);
         land.staedte.add(e);
         land.staedte.add(k);
         return land;
        
     }
      
 
        
        
    
  
     
 }
