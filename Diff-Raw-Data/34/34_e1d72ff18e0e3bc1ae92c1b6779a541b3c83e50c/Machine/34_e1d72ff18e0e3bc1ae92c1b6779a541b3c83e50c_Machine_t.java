 package tacc;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Machine
 {
 
 	/**
 	 * Costrutture, inizializza la lista di istruzioni e le mappe di variabili
 	 */
	public Machine(boolean debugMode)
 	{
 		this._istructions = new ArrayList<DecodedInstruction>();
 		this._X = new HashMap<String, Integer>();
 		this._Z = new HashMap<String, Integer>();
 		this._Y = 0;

		this.debug = debugMode;
 	}
 
 	/**
 	 * Lista di istruzioni
 	 */
 	protected ArrayList<DecodedInstruction> _istructions;
 
 	/**
 	 * Variabili di input
 	 */
 	protected HashMap<String, Integer> _X;
 
 	/**
 	 * Variabili di lavoro
 	 */
 	protected HashMap<String, Integer> _Z;
 
 	/**
 	 * Variabile di output
 	 */
 	protected Integer _Y;
 
 	/**
 	 * True per effettuare il debugging
 	 */
 	protected boolean debug = false;
 
 	/**
 	 * Aggiunge un'istruzione alla lista delle istruzioni.
 	 *
 	 * @param di Istruzione decodificata
 	 */
 	protected void addInstruction(DecodedInstruction di)
 	{
 		this._istructions.add(di);
 	}
 
 	/**
 	 * Carica un file contenente istruzioni TACC, una per riga.
 	 * Se le istruzioni sono valide vengono aggiunte alla memoria,
 	 * altrimenti viene stampata l'istruzione non valida e il metodo
 	 * restituisce false.
 	 *
 	 * @param fileName Nome del file da caricare
 	 * @return boolean
 	 */
 	public boolean loadScript(String fileName)
 	{
 		try {
             BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
             String istruction;
 
             while ((istruction = fileReader.readLine()) != null)
 			{
 				if (!istruction.trim().equals("")) {
 					Istruction istr = new Istruction(istruction);
 					if (istr.isValid()) {
 						this.addInstruction(istr.decode());
 					} else {
 						System.out.println("Istruzione non valida: " + istruction);
 						return false;
 					}
 				}
             }
         }
 		catch (java.io.IOException e) {
 			System.out.println("Impossibile leggere lo script: " + e.getMessage());
 			return false;
         }
 		return true;
 	}
 
 	/**
 	 * Fa partire la macchina virtuale TACC.
	 * Si inizia dalla prima istruzione (l'istruzione 0) e si evolve secondo
	 * la funzione "successore".
 	 */
 	public void run()
 	{
 		int istructionPointer = 0;
 		boolean runnable = true;
 
 		while (runnable) {
 			if (this.debug) System.out.println("Istruction pointer: " + istructionPointer);
 			istructionPointer = this.goAhead(istructionPointer);
 			runnable = (istructionPointer!=-1);
 		}
 	}
 
 	/**
 	 * Prende l'istruzione index-esmina dalla lista delle istruzioni.
 	 *
 	 * @param index Numero dell'istruzione
 	 * @return DecodedInstruction|null
 	 */
 	public DecodedInstruction getIstruction(int index)
 	{
 		try {
 			return this._istructions.get(index);
 		}
 		catch (IndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Evoluzione dello stato della macchina.
 	 * Prende l'istruzione all'indice dato e ne esegue il contenuto.
 	 *
 	 * @param index Indice della istruzione da eseguire
 	 * @return Indice della prossima istruzione, -1 se lo script termina
 	 */
 	public int goAhead(int index)
 	{
 		DecodedInstruction istruction = this.getIstruction(index);
 		if (istruction == null) {
 			if (this.debug) System.out.println("  STOP: reached end of TACC script!");
 			return -1;
 		}
 		
 		if (this.debug) System.out.println("  EXECUTE: " + istruction);
 
 		switch (istruction.opcode)
 		{
 			case DecodedInstruction.ADD:
 				this.incrementVariable(istruction.variable);
 				return index+1;
 
 			case DecodedInstruction.SUB:
 				this.decrementVariable(istruction.variable);
 				return index+1;
 
 			case DecodedInstruction.VOID:
 				return index+1;
 
 			case DecodedInstruction.GOTO:
 				if (this.readVariable(istruction.variable) == 0) {
 					return index+1;
 				} else {
 					if (istruction.isTerminator()) {
						if (this.debug) System.out.println("  STOP: jump to exit point!");
 						return -1;
 					} else {
 						return this.getIndexOfIstructionLabeled(istruction.jump);
 					}
 				}
 		}
 
 		return -1;
 	}
 
 	/**
 	 * Legge una variabile dalla memoria
 	 *
 	 * @param var Nome della variabile (x1, z3, ..)
 	 * @return int
 	 */
 	protected int readVariable(String var)
 	{
 		if (var.equals("Y")) {
 			return this._Y;
 		} else {
 			HashMap<String, Integer> map = (var.charAt(0) == 'x') ? this._X : this._Z;
 
 			if (map.containsKey(var)) {
 				return map.get(var);
 			} else {
 				return 0;
 			}
 		}
 	}
 
 	/**
 	 * Incrementa la variabile passata come argomento.
 	 *
 	 * Se la variabile è già presente in memoria, il valore viene incrementato.
 	 * Se la variabile non è mai stata richiamata prima, viene creata con valore 1.
 	 *
 	 * @param var Nome della variabile (x1, z3, ..)
 	 */
 	protected void incrementVariable(String var)
 	{
 		if (var.equals("Y")) {
 			this._Y++;
 		} else {
 			HashMap<String, Integer> map = (var.charAt(0) == 'x') ? this._X : this._Z;
 
 			if (map.containsKey(var)) {
 				map.put(var, map.get(var)+1);
 			} else {
 				map.put(var, 1);
 			}
 		}
 	}
 
 	/**
 	 * Decrementa la variabile passata come argomento.
 	 *
 	 * Se la variabile è già presente in memoria, viene decrementata solo se il
 	 * suo valore è strettamente positivo.
 	 * Se la variabile non è mai stata richiama prima, viene creata con valore 0.
 	 *
 	 * @param var  Nome della variabile (x1, z3, ..)
 	 */
 	protected void decrementVariable(String var)
 	{
 		if (var.equals("Y")) {
 			this._Y--;
 		} else {
 			HashMap<String, Integer> map = (var.charAt(0) == 'x') ? this._X : this._Z;
 
 			if (map.containsKey(var)) {
 				if (map.get(var) > 0) {
 					map.put(var, map.get(var)-1);
 				}
 			} else {
 				map.put(var, 0);
 			}
 		}
 	}
 
 	/**
 	 * Stampa le variabili di input e quelle di lavoro.
 	 */
 	public void dumpMemory()
 	{
 		System.out.println(" X: " + this._X);
 		System.out.println(" Z: " + this._Z);
 	}
 
 	/**
 	 * Risultato dell'eleborazione della macchina TACC
 	 *
 	 * @return int Contenuto della variabile Y.
 	 */
 	public int getResponse()
 	{
 		return this._Y;
 	}
 
 	/**
 	 * Trova l'indice della prima istruzione etichettata con "label".
 	 * Se nessuna istruzione ha tale etichetta, restituisce -1.
 	 *
 	 * @param label Etichetta da cercare
 	 * @return int
 	 */
 	public int getIndexOfIstructionLabeled(String label)
 	{
 		for (int i=0; i<this._istructions.size(); i++)
 		{
 			if (this._istructions.get(i).label != null && this._istructions.get(i).label.equals(label)) {
 				return i;
 			}
 		}
 
 		return -1;
 	}
 
 }
