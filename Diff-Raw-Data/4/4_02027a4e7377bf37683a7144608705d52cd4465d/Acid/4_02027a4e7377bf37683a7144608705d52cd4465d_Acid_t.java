 package ru.spbau.bioinf.tagfinder;
 
 import java.util.HashMap;
 
 public enum Acid {
     G(57.02146),
     A(71.03711),
     S(87.03203),
     P(97.05276),
     V(99.06841),
     T(101.04768),
     C(103.00919),
     I(113.08406),
     //L(113.08406),
     N(114.04293),
     D(115.02694),
     R(156.10111),
     Q(128.05858),
     K(128.09496),
     E(129.04259),
     M(131.04049),
     H(137.05891),
     F(147.06841),
     U(150.953636),
     Y(163.06333),
     W(186.07931),
     O(237.147727);
 
     private double mass;
 
     Acid(double mass) {
         this.mass = mass;
     }
 
     public double getMass() {
         return mass;
     }
 
    public boolean match(double[] limits) {
       return limits[0] < mass && limits[1] > mass;
    }

     public static HashMap<Character, Acid> acids = new HashMap<Character, Acid>();
 
     static {
         for (Acid acid : Acid.values()) {
             acids.put(acid.name().charAt(0), acid);
         }
     }
 
     public static Acid getAcid(char ch) {
         return acids.get(ch);
     }
 }
