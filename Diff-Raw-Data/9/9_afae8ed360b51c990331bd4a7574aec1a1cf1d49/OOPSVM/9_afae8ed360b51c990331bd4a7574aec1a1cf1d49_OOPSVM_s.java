 /**
  * Diese Klasse stellt die Hauptmethode der virtuellen Maschine 
  * für OOPS bereit. Sie wertet die Kommandozeilen-Optionen aus
  * und bietet eine Hilfe an, falls diese falsch sind.
  */
 class OOPSVM {
     /**
      * Die Hauptmethode der virtuellen Maschine. 
      * Sie wertet die Kommandozeilen-Optionen aus und bietet eine Hilfe an, falls diese falsch sind.
      * Sind sie gültig, wird der Assembler benutzt, um den übergebenen Quelltext in ein
      * Maschinenprogramm zu übersetzen. Dieses wird dann von der virtuellen Maschine ausgeführt.
      * @param args Die Kommandozeilenargumente. Diese sind im Quelltext der Methode 
      * {@link #usage usage} nachzulesen.
      */
     public static void main(String[] args) {
         String fileName = null;
         boolean showInstructions = false;
         boolean showMemory = false;
         boolean showRegisters = false;
         boolean showFirst = false;
         boolean showSecond = false;
         boolean execution = true;
         boolean showR2f = false;
         boolean showR2b = false;
         boolean showR4f = false;
         boolean showR4b = false;
         Inspector insp = null;
 
         for (String arg : args) {
             if (arg.equals("-i")) {
                 showInstructions = true;
             } else if (arg.equals("-m")) {
                 showMemory = true;
             } else if (arg.equals("-r")) {
                 showRegisters = true;
             } else if (arg.equals("-1")) {
                 showFirst = true;
             } else if (arg.equals("-2")) {
                 showSecond = true;
             } else if (arg.equals("-c")) {
                 execution = false;
             } else if (arg.equals("-f2")) {
                 showR2f = true;
             } else if (arg.equals("-b2")) {
                 showR2b = true;
             } else if (arg.equals("-f4")) {
                 showR4f = true;
             } else if (arg.equals("-b4")) {
                 showR4b = true;
             } else if (arg.equals("-g")) {
                 if(insp == null) insp = new SwingInspector(false);
             } else if (arg.equals("-gg")) {
                 if(insp == null) insp = new SwingInspector(true);
             } else if (arg.equals("-h")) {
                 usage();
                 return;
             } else if (arg.length() > 0 && arg.charAt(0) == '-') {
                 System.out.println("Unbekannte Option " + arg);
                 usage();
                 return;
             } else if (fileName != null) {
                 System.out.println("Nur ein Dateiname erlaubt: " + fileName + " vs. " + arg);
                 usage();
                 return;
             } else {
                 fileName = arg;
             }
         }
             
         if (fileName == null) {
             System.out.println("Kein Dateiname angegeben");
             usage();
             return;
         }
         
         try {
             VirtualMachine vm = new VirtualMachine(
                     new Assembler(showFirst, showSecond).assemble(fileName), new int[8],
                     showInstructions, showMemory, showRegisters, showR2f, showR2b, showR4f, showR4b, insp);
             if(insp!=null)insp.readCode(fileName);
             if (execution) {
                 vm.run();
             }
         } catch (Exception e) {
             System.out.println(e.getMessage());
	    e.printStackTrace();
             if(insp!=null)insp.sendException(e);
         }
     }
     
     /**
      * Die Methode gibt eine Hilfe auf der Konsole aus.
      */
     private static void usage() {
         System.out.println("java -jar OOPSVM.jar [-1] [-2] [-c] [-h] [-i] [-m] [-r] [-f2] [-b2] [-f4] [-b4] <dateiname>");
         System.out.println("    -1  Ausgabe beim ersten Assemblierungslauf");
         System.out.println("    -2  Ausgabe beim zweiten Assemblierungslauf");
         System.out.println("    -c  Programm wird nur uebersetzt, aber nicht ausgefuehrt");
         System.out.println("    -h  Zeige diese Hilfe");
         System.out.println("    -i  Zeige Instruktionen bei der Ausfuehrung");
         System.out.println("    -m  Zeige Speicher bei der Ausfuehrung");
         System.out.println("    -r  Zeige Registersatz bei der Ausfuehrung");
         System.out.println("    -f2 Zeige Stapelauszug für Register R2");
         System.out.println("    -b2 Zeige Stapelauszug für Register R2 rückwärts");
         System.out.println("    -f4 Zeige Stapelauszug für Register R4");
         System.out.println("    -b4 Zeige Stapelauszug für Register R4 rückwärts");
         System.out.println("    -g  Zeige graphisch den Ablauf des Programms");
         System.out.println("    -gg Zeige graphisch in Schritten den Ablauf des Programms");
     }
 }
