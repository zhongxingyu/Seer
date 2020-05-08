 package poo.util;
 
 import java.util.*;
 
 public class AlberoDiEspressione {
 	private class Nodo {
 		Nodo figlioS, figlioD;
 	} // Nodo
 	private class NodoOperando extends Nodo {
 		int val;
 	} // NodoOperando
 	private class NodoOperatore extends Nodo {
 		char op;
 	} // NodoOperatore
 	private Nodo radice = null;
 	public void inFix() { inFix(radice); }
 	private void inFix(Nodo radice) {
 		if (radice == null) return;
 		if (radice instanceof NodoOperando) System.out.print(((NodoOperando)radice).val);
 		else {
 			System.out.print('(');
 			inFix(radice.figlioS);
 			System.out.print(((NodoOperatore)radice).op);
 			inFix(radice.figlioD);
 			System.out.print(')');
 		}
 	} // inFix
 	public void preFix() { preFix(radice); }
 	private void preFix(Nodo radice) {
 		if (radice == null) return;
 		if (radice instanceof NodoOperando) System.out.print(((NodoOperando)radice).val + " ");
 		else {
 			System.out.print(((NodoOperatore)radice).op + " ");
 			preFix(radice.figlioS);
 			preFix(radice.figlioD);
 		}
 	} // preFix
 	public void postFix() { postFix(radice); }
 	private void postFix(Nodo radice) {
 		if (radice == null) return;
 		if (radice instanceof NodoOperando) System.out.print(((NodoOperando)radice).val + " ");
 		else {
 			postFix(radice.figlioS);
 			postFix(radice.figlioD);
 			System.out.print(((NodoOperatore)radice).op + " ");
 		}
 	} // postFix
 	public int value() { return value(radice); }
 	private int value(Nodo radice) {
 		if (radice == null) throw new RuntimeException("Albero vuoto!");
 		if (radice instanceof NodoOperando) return ((NodoOperando)radice).val;
 		int v1 = value(radice.figlioS);
 		int v2 = value(radice.figlioD);
 		char op = ((NodoOperatore)radice).op;
 		switch (op) {
 			case '+': return v1 + v2;
 			case '-': return v1 - v2;
 			case '*': return v1 * v2;
 			case '/': return v1 / v2;
 			case '%': return v1 % v2;
 			default: throw new RuntimeException(op + ": operatore sconosciuto.");
 		}
 	} // value
 	public void build(String expr) {
 		StringTokenizer st = new StringTokenizer(expr, "+-*/%()", true);
 		radice = buildEspressione(st);
 	} // build
 	private Nodo buildEspressione(StringTokenizer st) {
 		Nodo radice = buildOperando(st);
 		while (st.hasMoreTokens()) {
 			char op = st.nextToken().charAt(0);
 			if (op == ')') return radice;
 			NodoOperatore nop = new NodoOperatore();
 			nop.op = op; nop.figlioS = radice; nop.figlioD = buildOperando(st);
 			radice = nop;
 		}
 		return radice;
 	} // buildEspressione
 	private Nodo buildOperando(StringTokenizer st) {
 		String tk = st.nextToken();
 		if (tk.charAt(0) == '(') return buildEspressione(st);
 		NodoOperando opnd = new NodoOperando();
 		opnd.val = Integer.parseInt(tk);
 		return opnd;
 	} // buildOperando
 
 	public void buildPre(String expr) {
 		StringTokenizer st = new StringTokenizer(expr, " ");
 		radice = buildPre(st);
 	} // buildPre
 	private Nodo buildPre(StringTokenizer st) {
 		Stack<Nodo> pila = new StackConcatenato<Nodo>();
 		String OPERANDO = "[0-9]+";
		String OPERATORE = "[\\+\\-\\*/%]";
 		while (st.hasMoreTokens()) {
 			String tk = st.nextToken();
 			if (tk.matches(OPERATORE)) {
 				NodoOperatore nop = new NodoOperatore();
 				nop.op = tk.charAt(0);
 				pila.push(nop);
 			} else if (tk.matches(OPERANDO)) {
 				NodoOperando opnd = new NodoOperando();
 				opnd.val = Integer.parseInt(tk);
 				if (pila.isEmpty()) pila.push(opnd);
 				else if (pila.top() instanceof NodoOperando || pila.top().figlioS != null) {
 					Nodo cor = opnd;
 					do {
 						Nodo prec = pila.pop();
 						pila.top().figlioS = prec; pila.top().figlioD = cor;
 						cor = pila.pop();
 					} while (!pila.isEmpty() && pila.top().figlioS != null);
 					pila.push(cor);
 				} else pila.push(opnd);
 			}
 		}
 		if (pila.size() != 1)
 			throw new RuntimeException("Espressione malformata!");
 		return pila.pop();
 	} // buildPre
 
 	public void buildPost(String expr) {
 		StringTokenizer st = new StringTokenizer(expr, " ");
 		radice = buildPost(st);
 	} // buildPost
 	private Nodo buildPost(StringTokenizer st) {
 		String OPERANDO = "[0-9]+";
 		String OPERATORE = "[\\+\\-\\*/%]";
 		Stack<Nodo> pila = new StackConcatenato<Nodo>();
 		while (st.hasMoreTokens()) {
 			String tk = st.nextToken();
 			if (tk.matches(OPERANDO)) {
 				NodoOperando opnd = new NodoOperando();
 				opnd.val = Integer.parseInt(tk);
 				pila.push(opnd);
 			} else if (tk.matches(OPERATORE)) {
 				NodoOperatore nop = new NodoOperatore();
 				nop.op = tk.charAt(0);
				nop.figlioD = pila.pop(); nop.figlioS = pila.pop();
 				pila.push(nop);
 			}
 		}
 		if (pila.size() != 1)
 			throw new RuntimeException("Espressione malformata!");
 		return pila.pop();
 	} // buildPost
 
 	public static void main(String[]args) {
 		String EXPR = "[\\+\\-/%\\*\\d\\(\\)\\s]+";
 		Scanner sc = new Scanner(System.in);
 		AlberoDiEspressione abe = new AlberoDiEspressione();
 		System.out.println("Test AlberoDiEspressione.\n" +
 				"Inserire espressione aritmetica intera. Operatori supportati: +, -, *, /, % (equiprioritari).\n" +
 				"E' possibile inserire espressioni in forma infissa, prefissa o postfissa.\n" +
 				"Per la forma infissa: non inserire spazi, e utilizzare parentesi tonde '(' e ')' per recuperare la priorità.\n\n" +
 				"Comandi disponibili sull'albero di espressione:\n" +
 				"in : visualizza espressione inFix\n" +
 				"pre : visualizza espressione preFix\n" +
 				"post : visualizza espressione postFix\n" +
 				"val : calcola valore dell'espressione\n" +
 				". (punto) : esci");
 		do {
 			System.out.print("> ");
 			String linea = sc.nextLine().toLowerCase();
 			if (linea.equals("")) continue;
 			else if (linea.equals("in")) abe.inFix();
 			else if (linea.equals("pre")) abe.preFix();
 			else if (linea.equals("post")) abe.postFix();
 			else if (linea.equals("val")) { abe.inFix(); System.out.print(" = " + abe.value()); }
 			else if (linea.equals(".")) break;
 			else if (!linea.matches(EXPR)) System.out.print("Input non valido!");
 			else {
 				try {
 					System.out.println("L'espressione inserita è in forma: IN(fissa), PRE(fissa) o POST(fissa)?");
 					System.out.print(">>> ");
 					String forma = sc.nextLine().toLowerCase();
 					if (forma.equals("in")) abe.build(linea);
 					else if (forma.equals("pre")) abe.buildPre(linea);
 					else if (forma.equals("post")) abe.buildPost(linea);
 					else if (forma.equals(".")) break;
 					else { System.out.println("Input non valido!"); continue; }
 					System.out.print("Espressione inserita correttamente nell'albero.");
 				} catch (RuntimeException e) {
 					System.out.print("Espressione malformata!");
 				}
 			}
 			System.out.println();
 		} while (true);
 		System.out.println("Bye!");
 	} // main
 } // AlberoDiEspressione
