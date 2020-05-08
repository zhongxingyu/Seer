 /*
  * jCrypt - Programmierumgebung für das Kryptologie-Praktikum
  * Studienarbeit am Institut für Theoretische Informatik der
  * Technischen Universität Braunschweig
  * 
  * Datei:        Vigenere.java
  * Beschreibung: Dummy-Implementierung der Vigenère-Chiffre
  * Erstellt:     30. März 2010
  * Autor:        Martin Klußmann
  */
 
 package task1;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import de.tubs.cs.iti.jcrypt.chiffre.CharacterMapping;
 import de.tubs.cs.iti.jcrypt.chiffre.Cipher;
 import de.tubs.cs.iti.jcrypt.chiffre.FrequencyTables;
 import de.tubs.cs.iti.jcrypt.chiffre.NGram;
 
 /**
  * Dummy-Klasse für die Vigenère-Chiffre.
  * 
  * @author Martin Klußmann
  * @version 1.0 - Tue Mar 30 15:53:38 CEST 2010
  */
 public class Vigenere extends Cipher {
 
   /**
    * keyword
    */
   private int[] keyword;
 
   /**
    * Analysiert den durch den Reader <code>ciphertext</code> gegebenen Chiffretext, bricht die
    * Chiffre bzw. unterstützt das Brechen der Chiffre (ggf. interaktiv) und schreibt den Klartext
    * mit dem Writer <code>cleartext</code>.
    * 
    * @param ciphertext
    *          Der Reader, der den Chiffretext liefert.
    * @param cleartext
    *          Der Writer, der den Klartext schreiben soll.
    */
   public void breakCipher(BufferedReader ciphertext, BufferedWriter cleartext) {
     // ciphertext als list mit integer representations der characters bauen
     LinkedList<Integer> ciphertextList = new LinkedList<Integer>();
     try {
       int character;
       while ((character = ciphertext.read()) != -1) {
         if (character != -1) {
           // map ints zu internem alphabet
           character = charMap.mapChar(character);
           ciphertextList.add(character);
         } else {
           // Ein überlesenes Zeichen sollte bei korrekter Chiffretext-Datei
           // eigentlich nicht auftreten können.
         }
       }
       cleartext.close();
       ciphertext.close();
     } catch (IOException e) {
       System.err.println("Abbruch: Fehler beim Zugriff auf Klar- oder " + "Chiffretextdatei.");
       e.printStackTrace();
       System.exit(1);
     }
 
     Logger("ciphertextList= " + ciphertextList);
 
     // ngramme finden
     HashMap<Integer, Integer> intervalFrequencies = getIntervalFrequencies(ciphertextList, 3);
 
     Logger("intervalFrequencies= " + intervalFrequencies);
 
     // intervalFrequencies
 
     // gcd -> periode d
 
     // in teiltexte zerlegen
     // for (int i = 0; i < d; i++) {
     // LinkedList<Integer> sublist = getSublist(ciphertextList, i, d);
     // Logger("" + sublist);
     // // HashMap<Integer, Integer> quantityHashMap = getQuantities(sublist);
     // // Logger("" + quantityHashMap);
     //
     // // mit friedman auf periode 1 testen
     // int d_friedman = friedmanTest(sublist);
     //
     // }
 
     // auf teiltexte Caesar anwenden
     // ----------------------------
     HashMap<Integer, Integer> quantityHashMap = getQuantities(ciphertextList); // TODO
     Logger("quantity" + quantityHashMap);
     int[] caesar = breakCaesar(quantityHashMap); // mögliche shifts für diesen caesar teiltext
     // ----------------------------
 
     //
     //
     // // OLD:
     // // int choice = getUserInput("Moechten Sie den Buchstaben " + mostFrequented + " ("+ (char)
     // // charMap.remapChar(mostFrequented) + "), der am oeftesten vorkommt,\nauf " +
     // // nGramMostFrequentedMapped + " ("+ (char) charMap.remapChar(nGramMostFrequentedMapped) +
     // // ") oder auf " + nGramMostFrequentedMapped2 + " ("+ (char)
     // // charMap.remapChar(nGramMostFrequentedMapped2) + ") mappen? ");
     //
     //
     //
     // String keyOutput = "";
     // String keyOutputRemaped = "";
     // for (int j = 0; j < key.length; j++) {
     // // int:
     // keyOutput += key[j] + " ";
     // // ascii:
     // int remapedChar = charMap.remapChar(key[j]);
     // keyOutputRemaped += remapedChar + " ";
     // }
     //
     // Logger("Key as Integers: " + keyOutput);
     // Logger("Key as ASCII: " + keyOutputRemaped);
     //
     // // save as keyword
     // keyword = key;
     //
     // decipher(ciphertext, cleartext);
     Logger("ende");
   }
 
   private HashMap<Integer, Integer> getIntervalFrequencies(LinkedList<Integer> list, int n) {
     HashMap<Integer, Integer> frequenciesHashMap = new HashMap<Integer, Integer>();
     HashMap<String, Integer> stringPositionMap = new HashMap<String, Integer>();
 
     for (int listPosition = 0; listPosition < list.size(); listPosition++) {
       String currentString = new String();
 
       for (int j = listPosition; j < n + listPosition; j++) {
         if (j == list.size()) {
           break;
         }
         currentString += (list.get(j) + "");
       }
 
       if (stringPositionMap.containsKey(currentString)) {
         int stringPosition = stringPositionMap.get(currentString);
         int difference = listPosition - stringPosition;
 
         if (frequenciesHashMap.containsKey(difference)) {
           int newValue = frequenciesHashMap.get(difference) + 1;
           frequenciesHashMap.put(difference, newValue);
         } else {
           frequenciesHashMap.put(difference, 1);
         }
 
         stringPositionMap.put(currentString, listPosition);
 
       } else {
         stringPositionMap.put(currentString, listPosition);
       }
 
     }
 
     return frequenciesHashMap;
   }
 
   private HashMap<Integer, Integer> removeUnnecessaryInformation(HashMap<Integer, Integer> map) {
 
     Iterator<Integer> valueIterator = map.values().iterator();
     int max = 0;
 
     while (valueIterator.hasNext()) {
       int currentValue = valueIterator.next();
       if (max < currentValue) {
         max = currentValue;
       }
     }
 
     Iterator<Integer> keyIterator = map.keySet().iterator();
 
     while (keyIterator.hasNext()) {
       int key = keyIterator.next();
       int value = map.get(key);
 
      if (value < max / 2) {
        map.remove(key);
       }
     }
 
     return map;
   }
 
   /**
    * Generiere HashMap mit allen im Chiffretext vorkommenden Buchstaben und der jeweiligen Anzahl
    * 
    * @param ciphertextList
    * @return quantities
    */
   private HashMap<Integer, Integer> getQuantities(LinkedList<Integer> list) {
     HashMap<Integer, Integer> quantityHashMap = new HashMap<Integer, Integer>();
     Iterator<Integer> it = list.iterator();
 
     while (it.hasNext()) {
       int character = it.next();
 
       if (quantityHashMap.containsKey(character)) {
         int value = quantityHashMap.get(character) + 1;
         quantityHashMap.put(character, value);
       } else {
         quantityHashMap.put(character, 1);
       }
     }
     return quantityHashMap;
   }
 
   /**
    * Return the greatest common divisor
    */
   private static int gcd(int a, int b) {
     if (b == 0)
       return a;
     else
       return gcd(b, a % b);
   }
 
   /**
    * gcd over list
    */
   private int gcdOverList(LinkedList<Integer> list) {
     Iterator<Integer> iter = list.iterator();
     int gcd = list.get(0);
     int b;
     while (iter.hasNext()) {
       b = (int) iter.next();
       gcd = gcd(b, gcd);
     }
 
     return gcd;
   }
 
   /**
    * Chiffretext in Sublisten teilen
    * 
    * @param list
    * @param start
    * @param period
    * @return
    */
   private LinkedList<Integer> getSublist(LinkedList<Integer> list, int start, int period) {
     LinkedList<Integer> subList = new LinkedList<Integer>();
 
     for (int i = start; i < list.size(); i = i + period) {
       subList.add(list.get(i));
     }
 
     return subList;
   }
 
   /**
    * Caesar brechen Shift berechnen
    * 
    * 
    * @param quantityHashMap
    * @return
    */
   int[] breakCaesar(HashMap<Integer, Integer> quantityHashMap) {
     // eine linkedlist bauen, die die character representationen enthält, von vorne nach hinten
     // sortiert nach der auftrittshäufigkeit
     LinkedList<Integer> charactersByQuantity = new LinkedList<Integer>();
     while (!quantityHashMap.isEmpty()) {
       // größten wert aus quantityHashMap bekommen
       int currKey = -1, currValue = -1, greatest = -1, mostFrequented = -1;
       Iterator<Integer> it = quantityHashMap.keySet().iterator();
       while (it.hasNext()) {
         currKey = it.next();
         currValue = quantityHashMap.get(currKey);
 
         if (currValue > greatest) {
           greatest = currValue;
           mostFrequented = currKey;
         }
       }
       // den mostfrequented als ersten eintrag hinzufügen
       charactersByQuantity.addLast(mostFrequented);
       // diesen eintrag aus der hashmap entfernen
       quantityHashMap.remove(mostFrequented);
     }
 
     Logger("breakCaesar charactersByQuantity=" + charactersByQuantity);
 
     // die beiden häufigsten buchstaben im gewählten alphabet, normalerweise e und n
     ArrayList<NGram> nGrams = FrequencyTables.getNGramsAsList(1, charMap);
     int[] nGramMostFrequentedMapped = new int[2];
     nGramMostFrequentedMapped[0] = charMap.mapChar(Integer.parseInt(nGrams.get(0).getIntegers())); // e
     nGramMostFrequentedMapped[1] = charMap.mapChar(Integer.parseInt(nGrams.get(1).getIntegers())); // n
 
     // mögliche shifts
     int[] computedShift = new int[4];
     // häufigster auf e
     computedShift[0] = charactersByQuantity.get(0) - nGramMostFrequentedMapped[0];
     // 2. häufigster auf e
     computedShift[1] = charactersByQuantity.get(1) - nGramMostFrequentedMapped[0];
     // häufigster auf n
     computedShift[2] = charactersByQuantity.get(0) - nGramMostFrequentedMapped[1];
     // 2. häufigster auf n
     computedShift[3] = charactersByQuantity.get(1) - nGramMostFrequentedMapped[1];
 
     // alle computedShifts vom negativen ins positive
     for (int i = 0; i < computedShift.length; i++) {
       if (computedShift[i] < 0) {
         computedShift[i] += modulus;
       }
     }
 
     // debug logging
     Logger("breakCaesar computedShift= ");
     for (int i = 0; i < computedShift.length; i++) {
       Logger("" + computedShift[i] + ", ");
     }
 
     return computedShift;
   }
 
   /**
    * Helper Methode für User Input
    * 
    * @param question
    * @return data
    */
   private int getUserInput(String question) {
     BufferedReader standardInput = launcher.openStandardInput();
     int data = 0;
 
     try {
       Logger(question);
       data = Integer.parseInt(standardInput.readLine());
     } catch (NumberFormatException e) {
       e.printStackTrace();
     } catch (IOException e) {
       Logger("Problem beim Einlesen");
       e.printStackTrace();
     }
 
     return data;
   }
 
   /**
    * Berechne Periode d mittels Friedman-Test
    * 
    * @param ciphertextList
    * @return d
    */
   int friedmanTest(LinkedList<Integer> ciphertextList) {
     HashMap<Integer, Integer> quantities = getQuantities(ciphertextList);
 
     Logger("friedmanTest: quantities= " + quantities.toString());
 
     int N = ciphertextList.size();
 
     double IC = IC(N, quantities);
     Logger("friedmanTest IC= " + IC);
 
     double d = d(N, IC);
 
     int d_round = (int) Math.round(d);
 
     Logger("friedmanTest d= " + d);
     Logger("friedmanTest d gerundet= " + d_round);
 
     return d_round;
   }
 
   /**
    * Friedman-Test: Berechne IC
    * 
    * @param N
    * @param quantities
    * @return IC
    */
   private double IC(int N, HashMap<Integer, Integer> quantities) {
     int currentCharacter, F, sum = 0;
     double IC;
 
     Iterator<Integer> iter = quantities.keySet().iterator();
     while (iter.hasNext()) {
       currentCharacter = (int) iter.next();
       F = quantities.get(currentCharacter);
 
       sum += F * (F - 1);
     }
 
     IC = (double) sum / (N * (N - 1));
 
     return IC;
   }
 
   /**
    * Friedman-Test: Berechne Periode d
    * 
    * @param N
    * @param IC
    * @return d
    */
   private double d(int N, double IC) {
     // Summe der relativen Häufigkeiten eines beliebigen zufälligen chiffretextes:
     double sum = sumP();
 
     double enumerator = ((sum - (1 / (double) modulus)) * (double) N);
     double denominator = (((double) N - 1) * IC - (1 / (double) modulus) * (double) N + sum);
 
     double d = (enumerator / denominator);
 
     return d;
   }
 
   /**
    * Friedman-Test: Berechne summe mit pi's für standard nGram frequency tabellen
    * 
    * @param modulus
    * @return
    */
   private double sumP() {
     // unigramm frequency tabelle
     ArrayList<NGram> nGrams = FrequencyTables.getNGramsAsList(1, charMap);
 
     NGram currentNGram;
     double p, sum = 0;
     Iterator<NGram> iter = nGrams.iterator();
     while (iter.hasNext()) {
       currentNGram = iter.next();
 
       // get frequency as percentage
       p = currentNGram.getFrequency() / 100;
       sum += p * p;
     }
 
     return sum;
   }
 
   /**
    * Entschlüsselt den durch den Reader <code>ciphertext</code> gegebenen Chiffretext und schreibt
    * den Klartext mit dem Writer <code>cleartext</code>.
    * 
    * @param ciphertext
    *          Der Reader, der den Chiffretext liefert.
    * @param cleartext
    *          Der Writer, der den Klartext schreiben soll.
    */
   public void decipher(BufferedReader ciphertext, BufferedWriter cleartext) {
     // Kommentierung analog 'encipher(cleartext, ciphertext)'.
     try {
 
       int character;
       int d = 0;
 
       while ((character = ciphertext.read()) != -1) {
         character = charMap.mapChar(character);
         if (character != -1) {
 
           int index = d++ % keyword.length;
 
           int val = character - keyword[index];
           if (val < 0) {
             val += modulus;
           }
 
           character = charMap.remapChar(val);
           cleartext.write(character);
         } else {
           // Ein überlesenes Zeichen sollte bei korrekter Chiffretext-Datei
           // eigentlich nicht auftreten können.
         }
       }
       cleartext.close();
       ciphertext.close();
     } catch (IOException e) {
       System.err.println("Abbruch: Fehler beim Zugriff auf Klar- oder " + "Chiffretextdatei.");
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   /**
    * Verschlüsselt den durch den Reader <code>cleartext</code> gegebenen Klartext und schreibt den
    * Chiffretext mit dem Writer <code>ciphertext</code>.
    * 
    * @param cleartext
    *          Der Reader, der den Klartext liefert.
    * @param ciphertext
    *          Der Writer, der den Chiffretext schreiben soll.
    */
   public void encipher(BufferedReader cleartext, BufferedWriter ciphertext) {
     // An dieser Stelle könnte man alle Zeichen, die aus der Klartextdatei
     // gelesen werden, in Klein- bzw. Großbuchstaben umwandeln lassen:
     // charMap.setConvertToLowerCase();
     // charMap.setConvertToUpperCase();
 
     int c = 0;
 
     try {
       int character;
       boolean characterSkipped = false;
       while ((character = cleartext.read()) != -1) {
         character = charMap.mapChar(character);
         if (character != -1) {
 
           // character = (character + shift) % modulus;
           int index = c % keyword.length;
           // int keywordMapped = charMap.mapChar();
           character = (character + keyword[index]) % modulus;
 
           character = charMap.remapChar(character);
           ciphertext.write(character);
           c++;
         } else {
           // Das gelesene Zeichen ist im benutzten Alphabet nicht enthalten.
           characterSkipped = true;
           c++;
         }
       }
       if (characterSkipped) {
         System.out.println("Warnung: Mindestens ein Zeichen aus der " + "Klartextdatei ist im Alphabet nicht\nenthalten und wurde " + "überlesen.");
       }
       cleartext.close();
       ciphertext.close();
     } catch (IOException e) {
       System.err.println("Abbruch: Fehler beim Zugriff auf Klar- oder " + "Chiffretextdatei.");
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   /**
    * Erzeugt einen neuen Schlüssel.
    * 
    * @see #readKey readKey
    * @see #writeKey writeKey
    */
   public void makeKey() {
     BufferedReader standardInput = launcher.openStandardInput();
     boolean accepted = false;
 
     // Frage jeweils solange die Eingabe ab, bis diese akzeptiert werden kann.
     do {
       Logger("Geben Sie den Modulus ein: ");
       try {
         modulus = Integer.parseInt(standardInput.readLine());
 
         String defaultAlphabet = CharacterMapping.getDefaultAlphabet(modulus);
         if (!defaultAlphabet.equals("")) {
           Logger("Vordefiniertes Alphabet: '" + defaultAlphabet + "'\nDieses vordefinierte Alphabet kann durch Angabe einer " + "geeigneten Alphabet-Datei\nersetzt werden. Weitere " + "Informationen finden Sie im Javadoc der Klasse\n'Character" + "Mapping'.");
           accepted = true;
         } else {
           Logger("Warnung: Dem eingegebenen Modulus kann kein Default-" + "Alphabet zugeordnet werden.\nErstellen Sie zusätzlich zu " + "dieser Schlüssel- eine passende Alphabet-Datei.\nWeitere " + "Informationen finden Sie im Javadoc der Klasse 'Character" + "Mapping'.");
           accepted = true;
         }
       } catch (NumberFormatException e) {
         System.out.println("Fehler beim Parsen des Modulus. Bitte korrigieren" + " Sie Ihre Eingabe.");
       } catch (IOException e) {
         System.err.println("Abbruch: Fehler beim Lesen von der Standardeingabe.");
         e.printStackTrace();
         System.exit(1);
       }
     } while (!accepted);
     // define mapping for testing later
     CharacterMapping mapping = new CharacterMapping(modulus);
 
     accepted = false;
     do {
       try {
         Logger("Schlüssel eingeben:");
         // von string nach character array konvertieren
         String keywordString = standardInput.readLine().toString();
         char keywordChar[] = keywordString.toCharArray();
 
         // länge vom keyword array initialiseren
         keyword = new int[keywordChar.length];
 
         // konvertieren in int array
         int character, mappedCharacter;
         for (int i = 0; i < keywordChar.length; i++) {
           // konvertiere in int
           character = (int) keywordChar[i];
           // map to our alphabet
           mappedCharacter = mapping.mapChar(character);
 
           if (mappedCharacter >= 0 && mappedCharacter < modulus) {
             accepted = true;
           } else {
             System.out.println("Ein Buchstabe im Schlüssel passt nicht zum Alphabet, das durch den Modulus definiert wurde. " + "korrigieren Sie Ihre Eingabe.");
             System.exit(1);
           }
 
           // character in dem array speichern
           keyword[i] = mappedCharacter;
         }
 
       } catch (IOException e) {
         System.err.println("Abbruch: Fehler beim Lesen von der Standardeingabe.");
         e.printStackTrace();
         System.exit(1);
       }
     } while (!accepted);
   }
 
   /**
    * Liest den Schlüssel mit dem Reader <code>key</code>.
    * 
    * @param key
    *          Der Reader, der aus der Schlüsseldatei liest.
    * @see #makeKey makeKey
    * @see #writeKey writeKey
    */
   public void readKey(BufferedReader key) {
     try {
       StringTokenizer st = new StringTokenizer(key.readLine(), " ");
 
       modulus = Integer.parseInt(st.nextToken());
       Logger("Modulus: " + modulus);
 
       keyword = new int[st.countTokens()];
 
       int c = 0;
       while (st.hasMoreTokens()) {
         keyword[c++] = Integer.parseInt(st.nextToken());
       }
 
       key.close();
     } catch (IOException e) {
       System.err.println("Abbruch: Fehler beim Lesen oder Schließen der " + "Schlüsseldatei.");
       e.printStackTrace();
       System.exit(1);
     } catch (NumberFormatException e) {
       System.err.println("Abbruch: Fehler beim Parsen eines Wertes aus der " + "Schlüsseldatei.");
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   /**
    * Schreibt den Schlüssel mit dem Writer <code>key</code>.
    * 
    * @param key
    *          Der Writer, der in die Schlüsseldatei schreibt.
    * @see #makeKey makeKey
    * @see #readKey readKey
    */
   public void writeKey(BufferedWriter key) {
     try {
 
       String keyString = "";
 
       for (int i = 0; i < keyword.length; i++) {
         keyString = keyString + " " + keyword[i];
       }
 
       key.write(modulus + keyString);
 
       key.newLine();
       key.close();
     } catch (IOException e) {
       System.out.println("Abbruch: Fehler beim Schreiben oder Schließen der " + "Schlüsseldatei.");
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   private static void Logger(String event) {
     System.out.println(event);
   }
 }
