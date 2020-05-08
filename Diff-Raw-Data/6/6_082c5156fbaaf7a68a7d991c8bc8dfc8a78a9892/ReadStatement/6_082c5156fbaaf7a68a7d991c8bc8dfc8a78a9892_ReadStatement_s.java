 import java.util.LinkedList;
 
 /**
  * Die Klasse repräsentiert die Anweisung READ im Syntaxbaum.
  */
 class ReadStatement extends Statement {
     /** Die Variable, in der das eingelesene Zeichen gespeichert wird. */
     Expression operand;
     
     /** Ein Ausdruck, der ein neues Objekt vom Typ Integer erzeugen kann. */
     Expression newInt = new NewExpression(new ResolvableIdentifier("Integer", null), null);
     
     /**
      * Konstruktor.
      * @param operand Die Variable, in der das eingelesene Zeichen gespeichert wird.
      */
     ReadStatement(Expression operand) {
         this.operand = operand;
     }
 
     /**
      * Die Methode führt die Kontextanalyse für diese Anweisung durch.
      * @param declarations Die an dieser Stelle gültigen Deklarationen.
      * @throws CompileException Während der Kontextanylyse wurde ein Fehler
      *         gefunden.
      */
     void contextAnalysis(Declarations declarations) throws CompileException {
         operand = operand.contextAnalysis(declarations);
         if (!operand.lValue) {
             throw new CompileException("L-Wert erwartet", operand.position);
         }
         operand.type.check(ClassDeclaration.intClass, operand.position);
         newInt = newInt.contextAnalysis(declarations);
     }
 
 
     /**
      * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
      * @param tree Der Strom, in den die Ausgabe erfolgt.
      */
     void print(TreeStream tree) {
         tree.println("READ");
         tree.indent();
         operand.print(tree);
         tree.unindent();
     }
 
     /**
      * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
      * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
      * @param code Der Strom, in den die Ausgabe erfolgt.
      */
     void generateCode(CodeStream code) {
     	code.println("; READ lvalue ablegen");
         operand.generateCode(code);
     	code.println("; READ Speicher allokieren");
         newInt.generateCode(code);
         code.println("; READ");
         code.println("MRM R5, (R2)"); // R2 zeigt auf ein boxed Integer
         /** BEGIN Aufgabe (i): Vererbung*/
        code.println("MRI R6, " + ClassDeclaration.HEADERSIZE); 
         code.println("ADD R5, R6");
         /** END Aufgabe (i)*/
         code.println("SYS 0, 6 ; Gelesenen Wert in R6 ablegen");
         code.println("MMR (R5), R6 ; Zeichen in neuen Integer schreiben");
         code.println("MRM R5, (R2) ; Neuen Integer vom Stapel entnehmen");
         code.println("SUB R2, R1");
         code.println("MRM R6, (R2) ; Ziel vom Stapel entnehmen");
         code.println("SUB R2, R1");
         code.println("MMR (R6), R5 ; Zuweisen");
     }
 }
