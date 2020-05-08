 package poo.rasp;
 
 import java.io.*;
 import java.util.*;
 
 public class Assembler {
 	private TabellaSimboli tab = new TabellaSimboli();
 	public static Map<Integer, String> opCodeString = new HashMap<Integer, String>();
 	private Map<String, Integer> opCode = new HashMap<String, Integer>();
 	private Map<Character, Integer> modi = new HashMap<Character, Integer>();
 	private Lexer lex = null;
 	private String RES = "RES";
 	private String OPCODE = "(ADD|SUB|MUL|DIV|HALT|JZ|JNZ|JGZ|JLZ|JLEZ|JGEZ|JUMP|LOAD|STORE|READ|WRITE)";
 	private String JUMP = "(JZ|JNZ|JGZ|JLZ|JLEZ|JGEZ|JUMP)";
 	private boolean declaration = true;
 	private Lexer.Sim simboloCorrente;
 	private String etichetta, opc;
 	private char modo;
 	private int numero;
 	private int plc, dlc; // Program Location Counter, Data Location Counter
 	private ObjectModule codice = null;
 	private boolean esisteHALT = false;
 	public Assembler(String nomeFileSorgente, String nomeFileListing) throws IOException {
 		lex = new Lexer(nomeFileSorgente, nomeFileListing);
 		opCode.put("LOAD", 10); opCodeString.put(10, "LOAD");
 		opCode.put("STORE", 11); opCodeString.put(11, "STORE");
 		opCode.put("READ", 12); opCodeString.put(12, "READ");
 		opCode.put("WRITE", 13); opCodeString.put(13, "WRITE");
 		opCode.put("ADD", 14); opCodeString.put(14, "ADD");
 		opCode.put("SUB", 15); opCodeString.put(15, "SUB");
 		opCode.put("MUL", 16); opCodeString.put(16, "MUL");
 		opCode.put("DIV", 17); opCodeString.put(17, "DIV");
 		opCode.put("JZ", 18); opCodeString.put(18, "JZ");
 		opCode.put("JNZ", 19); opCodeString.put(19, "JNZ");
		opCode.put("JLZ", 20); opCodeString.put(20, "JLZ");
 		opCode.put("JLEZ", 21); opCodeString.put(21, "JLEZ");
 		opCode.put("JGZ", 22); opCodeString.put(22, "JGZ");
 		opCode.put("JGEZ", 23); opCodeString.put(23, "JGEZ");
 		opCode.put("JUMP", 24); opCodeString.put(24, "JUMP");
 		opCode.put("HALT", 25); opCodeString.put(25, "HALT");
 		modi.put(' ', 0); // diretto
 		modi.put('#', 1); // immediato
 		modi.put('@', 2); // indiretto
 	} // Costruttore
 	private void avanza() throws IOException {
 		do {
 			simboloCorrente = lex.prossimoSimbolo();
 		} while (simboloCorrente == Lexer.Sim.SP);
 	} // avanza
 	public void compile() throws IOException {
 		System.out.println("Prima passata ...");
 		simboloCorrente = lex.prossimoSimbolo();
 		while (simboloCorrente != Lexer.Sim.EOF) {
 			while (simboloCorrente == Lexer.Sim.SP) simboloCorrente = lex.prossimoSimbolo();
 			if (simboloCorrente == Lexer.Sim.IDENT && !lex.getStr().matches(OPCODE)) {
 				etichetta = lex.getStr();
 				tab.add(new Simbolo(etichetta));
 				avanza();
 				if (simboloCorrente != Lexer.Sim.END_LABEL) lex.error("Atteso :");
 				avanza();
 			}
 			if (simboloCorrente != Lexer.Sim.IDENT) lex.error("Atteso codice operativo");
 			opc = lex.getStr();
 			if (opc.equals(RES)) {
 				if (!declaration) lex.error("RES inattesa");
 				avanza();
 				if (simboloCorrente != Lexer.Sim.NUMBER) lex.error("Atteso numero");
 				numero = lex.getNum();
 				if (numero < 0) lex.error("Numero negativo");
 				Simbolo s = tab.find(etichetta);
 				s.setSize(numero);
 				s.setTipo(Simbolo.Tipo.DATO);
 				avanza();
			} else if (!opc.matches(OPCODE)) lex.error("Codice operativo illegale");
 			else {
 				declaration = false;
 				if (lex.getStr().equals("HALT")) {
 					esisteHALT = true;
 					avanza(); continue;
 				}
 				avanza();
 				modo = ' ';
 				if (simboloCorrente == Lexer.Sim.MODE) {
 					modo = lex.getStr().charAt(0);
 					if (modo == '#')
 						if (opc.equals("READ") || opc.equals("STORE") || opc.matches(JUMP))
 							lex.error("Modo incompatibile col codice operativo");
 					avanza();
 				}
 				if (simboloCorrente != Lexer.Sim.IDENT && simboloCorrente != Lexer.Sim.NUMBER)
 					lex.error("Atteso operando");
 				if (simboloCorrente == Lexer.Sim.IDENT) {
 					if (!opc.matches(JUMP)) {
 						String opnd = lex.getStr();
 						if (tab.find(opnd) == null) {
 							Simbolo so = new Simbolo(opnd);
 							so.setTipo(Simbolo.Tipo.DATO);
 							tab.add(so);
 						}
 					}
 					avanza();
 				} else avanza();
 			}
 		}
 		if (!esisteHALT) lex.error("Manca istruzione HALT");
 		System.out.println("Seconda passata ...");
 		lex.setEnabledEcho(false);
 		lex.rewind();
 		simboloCorrente = lex.prossimoSimbolo();
 		plc = 0;
 		while (simboloCorrente != Lexer.Sim.EOF) {
 			while (simboloCorrente == Lexer.Sim.SP) simboloCorrente = lex.prossimoSimbolo();
 			if (simboloCorrente == Lexer.Sim.IDENT && !lex.getStr().matches(OPCODE)) {
 				etichetta = lex.getStr();
 				Simbolo s = tab.find(etichetta);
 				if (s.getTipo() == Simbolo.Tipo.ISTR) s.setIndirizzo(plc);
 				avanza(); avanza(); // Salta ':'
 			}
 			opc = lex.getStr();
 			if (opc.equals("HALT")) {
 				plc++; avanza(); continue;
 			}
 			if (!opc.equals("RES")) plc = plc + 3;
 			do {
 				simboloCorrente = lex.prossimoSimbolo();
 			} while (simboloCorrente != Lexer.Sim.IDENT && simboloCorrente != Lexer.Sim.NUMBER);
 			if (simboloCorrente == Lexer.Sim.IDENT)
 				if (opc.matches(JUMP) && (tab.find(lex.getStr()) == null))
 					lex.error("Etichetta istruzione non definita");
 			avanza();
 		}
 		dlc = plc;
 		for (Simbolo s: tab)
 			if (s.getTipo() == Simbolo.Tipo.DATO) {
 				s.setIndirizzo(dlc);
 				dlc += s.getSize();
 			}
 		lex.toListing(tab.toString());
 		System.out.println("Generazione di codice ...");
 		codice = new ObjectModule();
 		lex.rewind();
 		simboloCorrente = lex.prossimoSimbolo();
 		while (true) {
 			while (simboloCorrente != Lexer.Sim.EOF && !lex.getStr().matches(OPCODE))
 				simboloCorrente = lex.prossimoSimbolo();
 			if (simboloCorrente == Lexer.Sim.EOF) break;
 			opc = lex.getStr();
 			avanza();
 			if (opc.equals("HALT")) {
 				codice.addInstruction(opCode.get("HALT")); continue;
 			}
 			modo = ' ';
 			if (simboloCorrente == Lexer.Sim.MODE) {
 				modo = lex.getStr().charAt(0);
 				avanza();
 			}
 			int indirizzoOperando = 0;
 			if (simboloCorrente == Lexer.Sim.IDENT)
 				indirizzoOperando = tab.find(lex.getStr()).getIndirizzo();
 			else
 				indirizzoOperando = lex.getNum();
 			codice.addInstruction(opCode.get(opc), modi.get(modo), indirizzoOperando);
 			avanza();
 		}
 		for (Simbolo s: tab)
 			if (s.getTipo() == Simbolo.Tipo.DATO)
 				codice.addData(s.getSize());
 		
 		lex.toListing("Tabella codici operativi");
 		lex.toListing(opCode.toString());
 
 		lex.toListing("Tabella dei modi");
 		lex.toListing(modi.toString());
 
 		lex.toListing("Codice macchina generato");
 		lex.toListing(codice.toString());
 	} // compile
 	public ObjectModule getObjectModule() { return codice; }
 } // Assembler
