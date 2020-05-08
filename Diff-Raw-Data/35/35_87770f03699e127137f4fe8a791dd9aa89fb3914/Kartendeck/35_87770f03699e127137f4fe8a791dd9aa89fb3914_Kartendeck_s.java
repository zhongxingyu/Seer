 package benchmark;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Kartendeck<T> {
 	
 	public enum KartendeckType {
 		DEBUG, DEBUG2, Q1_Q6_TEMP, Q1_Q6_WITH, Q1_Q6_BEST, Q7
 	};
 	
 	public static Kartendeck<Karte> createKartendeckByType(KartendeckType type) {
 		Kartendeck<Karte> deck = new Kartendeck<Karte>();
 		if (type == KartendeckType.DEBUG) {
 			deck.addKarte(new Karte("Q3"), 1);
 		}
 		else if (type == KartendeckType.DEBUG2) {
 			deck.addKarte(new Karte("Q3"), 1);
 			deck.addKarte(new Karte("Q4"), 1);
 		}
 		else if (type == KartendeckType.Q1_Q6_TEMP) {
 			deck.addKarte(new Karte("Q1"), 5);
 			deck.addKarte(new Karte("Q2"), 2);
 			deck.addKarte(new Karte("Q3"), 5);
 			deck.addKarte(new Karte("Q4"), 2);
 			deck.addKarte(new Karte("Q5"), 2);
 			deck.addKarte(new Karte("Q6"), 4);
 		}
 		else if (type == KartendeckType.Q1_Q6_WITH) {
 			deck.addKarte(new Karte("Q1.WITH"), 5);
 			deck.addKarte(new Karte("Q2.WITH"), 2);
 			deck.addKarte(new Karte("Q3.WITH"), 5);
 			deck.addKarte(new Karte("Q4.WITH"), 2);
 			deck.addKarte(new Karte("Q5.WITH"), 2);
 			deck.addKarte(new Karte("Q6"), 4);
 		}
 		else if (type == KartendeckType.Q7) {
 			deck.addKarte(new Karte("Q7"), 5);
 			deck.addKarte(new Karte("Q7.WITH"), 5);
 		} 
     else if (type == KartendeckType.Q1_Q6_BEST) {
       deck.addKarte(new Karte("Q1.WITH"), 5);
      deck.addKarte(new Karte("Q2"), 2);
       deck.addKarte(new Karte("Q3.WITH"), 5);
       deck.addKarte(new Karte("Q4.WITH"), 2);
       deck.addKarte(new Karte("Q5.WITH"), 2);
       deck.addKarte(new Karte("Q6"), 4);
     }
 		
 		return deck;
 	}
 	
   Map<T, Integer> deck;
 
   public Kartendeck() {
     deck = new HashMap<T, Integer>();
   }
 
   public int numKarten() {
     int result = 0;
     for (int c : deck.values()) {
       result += c;
     }
     return result;
   }
 
   public void addKarte(T k, int count) {
     if (deck.containsKey(k)) {
       final int oldCount = deck.get(k);
       final int newCount = oldCount + count;
       deck.put(k, newCount);
     } else {
       deck.put(k, count);
     }
   }
 
   public boolean isEmpty() {
     return deck.isEmpty();
   }
   
   public T removeRandomKarte() {
     final int numKarten = numKarten();
     int restKarten = (int) (numKarten * Math.random());
     for (T k : deck.keySet()) {
       final int countK = deck.get(k);
       restKarten -= countK;
       if (restKarten < 0) {
         if (countK > 1) {
           deck.put(k, countK - 1);
         } else {
           deck.remove(k);
         }
         return k;
       }
     }
     // Only happens, if deck is empty.
     assert(isEmpty());
     return null;
   }
 }
