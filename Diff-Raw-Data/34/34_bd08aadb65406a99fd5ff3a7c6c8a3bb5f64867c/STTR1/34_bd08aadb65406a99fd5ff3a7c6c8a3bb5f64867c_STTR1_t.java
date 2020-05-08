 package startrekj;
 
 import static startrekj.hpbasic.Line.*;
 import static startrekj.hpbasic.functions.ABS.*;
 import static startrekj.hpbasic.functions.ADD.*;
 import static startrekj.hpbasic.functions.DIV.*;
 import static startrekj.hpbasic.functions.INT.*;
 import static startrekj.hpbasic.functions.MULT.*;
 import static startrekj.hpbasic.functions.RND.*;
 import static startrekj.hpbasic.functions.SQR.*;
 import static startrekj.hpbasic.functions.SUBTRACT.*;
 import static startrekj.hpbasic.functions.TIM.*;
 import static startrekj.hpbasic.Arithmetic.*;
 import static startrekj.hpbasic.operators.EQ.*;
 import static startrekj.hpbasic.operators.EQ_String.*;
 import static startrekj.hpbasic.operators.GT.*;
 import static startrekj.hpbasic.operators.GTE.*;
 import static startrekj.hpbasic.operators.LT.*;
 import static startrekj.hpbasic.operators.LTE.*;
 import static startrekj.hpbasic.operators.NEQ.*;
 import static startrekj.hpbasic.operators.NEQ_String.*;
 import static startrekj.hpbasic.operators.OR.*;
 import static startrekj.hpbasic.operators.SUBSTRING.*;
 import static startrekj.hpbasic.statements.SET_ArrayPlace.*;
 import static startrekj.hpbasic.statements.SET_StringVariable.*;
 import static startrekj.hpbasic.statements.SET_NumericVariable.*;
 import static startrekj.hpbasic.statements.DEF.*;
 import static startrekj.hpbasic.statements.DIM.*;
 import static startrekj.hpbasic.statements.DIM$.*;
 import static startrekj.hpbasic.statements.EXIT.*;
 import static startrekj.hpbasic.statements.FOR.*;
 import static startrekj.hpbasic.statements.GOSUB.*;
 import static startrekj.hpbasic.statements.GOTO.*;
 import static startrekj.hpbasic.statements.IF.*;
 import static startrekj.hpbasic.statements.IMAGE.*;
 import static startrekj.hpbasic.statements.INPUT.*;
 import static startrekj.hpbasic.statements.INPUT_String.*;
 import static startrekj.hpbasic.statements.MAT.*;
 import static startrekj.hpbasic.statements.NEXT.*;
 import static startrekj.hpbasic.statements.PRINT.*;
 import static startrekj.hpbasic.statements.PRINT_USING.*;
 import static startrekj.hpbasic.statements.REM.*;
 import static startrekj.hpbasic.statements.REPLACE.*;
 import static startrekj.hpbasic.statements.RETURN.*;
 import static startrekj.hpbasic.statements.TRACE.*;
 import startrekj.hpbasic.ArrayVariable;
 import startrekj.hpbasic.FunctionVariable;
 import startrekj.hpbasic.HPBasicProgram;
 import startrekj.hpbasic.NumericExpression;
 import startrekj.hpbasic.NumericVariable;
 import startrekj.hpbasic.StringVariable;
 import startrekj.hpbasic.statements.TRACE;
 
 public class STTR1 extends HPBasicProgram {
 	
 	static NumericVariable A = new NumericVariable("A");
 	static NumericVariable B3 = new NumericVariable("B3");
 	static NumericVariable B9 = new NumericVariable("B9");
 	static NumericVariable C1 = new NumericVariable("C1");
 	static NumericVariable C2 = new NumericVariable("C2");
 	static NumericVariable D0 = new NumericVariable("D0");
 	static NumericVariable E = new NumericVariable("E");
 	static NumericVariable E0 = new NumericVariable("E0");
 	static NumericVariable H = new NumericVariable("H");
 	static NumericVariable H8 = new NumericVariable("H8");
 	static NumericVariable I = new NumericVariable("I");
 	static NumericVariable J = new NumericVariable("J");
 	static NumericVariable K3 = new NumericVariable("K3");
 	static NumericVariable K7 = new NumericVariable("K7");
 	static NumericVariable K9 = new NumericVariable("K9");
 	static NumericVariable NN = new NumericVariable("NN");
 	static NumericVariable P = new NumericVariable("P");
 	static NumericVariable P0 = new NumericVariable("P0");
 	static NumericVariable Q1 = new NumericVariable("Q1");
 	static NumericVariable Q2 = new NumericVariable("Q2");
 	static NumericVariable S = new NumericVariable("S");
 	static NumericVariable R1 = new NumericVariable("R1");
 	static NumericVariable R2 = new NumericVariable("R2");
 	static NumericVariable S1 = new NumericVariable("S1");
 	static NumericVariable S2 = new NumericVariable("S2");
 	static NumericVariable S3 = new NumericVariable("S3");
 	static NumericVariable S8 = new NumericVariable("S8");
 	static NumericVariable S9 = new NumericVariable("S9");
 	static NumericVariable T = new NumericVariable("T");
 	static NumericVariable T0 = new NumericVariable("T0");
 	static NumericVariable T1 = new NumericVariable("T1");
 	static NumericVariable T7 = new NumericVariable("T7");
 	static NumericVariable T9 = new NumericVariable("T9");
 	static NumericVariable W1 = new NumericVariable("W1");
 	static NumericVariable X = new NumericVariable("X");
 	static NumericVariable X1 = new NumericVariable("X1");
 	static NumericVariable X2 = new NumericVariable("X2");
 	static NumericVariable Y = new NumericVariable("Y");
 	static NumericVariable Z1 = new NumericVariable("Z1");
 	static NumericVariable Z2 = new NumericVariable("Z2");
 	static NumericVariable Z3 = new NumericVariable("Z3");
 
 	static StringVariable A$ = new StringVariable("A$");
 	static StringVariable C$ = new StringVariable("C$");
 	static StringVariable D$ = new StringVariable("D$");
 	static StringVariable E$ = new StringVariable("E$");
 	static StringVariable Q$ = new StringVariable("Q$");
 	static StringVariable R$ = new StringVariable("R$");
 	static StringVariable S$ = new StringVariable("S$");
 	static StringVariable Z$ = new StringVariable("Z$");
 	
 	static ArrayVariable C = new ArrayVariable("C");
 	static ArrayVariable D = new ArrayVariable("D");
 	static ArrayVariable G = new ArrayVariable("G");
 	static ArrayVariable K = new ArrayVariable("K");
 	static ArrayVariable N = new ArrayVariable("N");
 	static ArrayVariable Z = new ArrayVariable("Z");
 	
 	static FunctionVariable FND = new FunctionVariable("FND");
 	static NumericExpression FND_BODY = new NumericExpression() {
 
 		public Number evaluate() {
 			Number KI1 = K.get(I,1);
 			Number KI2 = K.get(I, 2);
 			Number D1 = subtract(KI1, S1.getValue());
 			Number D2 = subtract(KI2, S2.getValue());
 			return Math.sqrt(add(mult(D1,D1),mult(D2,D2)).doubleValue());
 		}
 		public String toString() {
 			return "SQR((K[I,1]-S1)^2+(K[I,2]-S2)^2)";
 		};
 	};
 	
 	
 	public static void main(String[] args) {
 	
 		STTR1 startrek = new STTR1();
 		
 		startrek.loadProgramLines();
 //		System.out.println(startrek);
 		startrek.run();
 	}
 
 	private void loadProgramLines() {
 		addLines(
 			line(1, REM("****  HP BASIC PROGRAM LIBRARY  ******************************")),
 			line(2, REM()),
 			line(3, REM("     STTR1: STAR TREK")),
 			line(4, REM("")),
 			line(5, REM("      36243  REV B  --  10/73")),
 			line(6, REM()),
 			line(7, REM("****  CONTRIBUTED PROGRAM  ***********************************"))
 		);
 		addLines(
 			line(100, REM("*****************************************************************")),
 			line(110, REM("***                                                           ***")),
 			line(120, REM("***     STAR TREK: BY MIKE MAYFIELD, CENTERLINE ENGINEERING   ***")),
 			line(130, REM("***                                                           ***")),
 			line(140, REM("***        TOTAL INTERACTION GAME - ORIG. 20 OCT 1972")),
 			line(150, REM("***                                                           ***")),
 			line(160, REM("*****************************************************************"))
 		);
 		addLines(
 			line(170, GOSUB(5460))
 		);
 		addLines(
 			line(180, PRINT("                          STAR TREK ")),
 			line(190, PRINT("DO YOU WANT INSTRUCTIONS (THEY'RE LONG!)")),
 			line(200, INPUT(A$)),
 			line(210, IF(NEQ(A$, "YES")).THEN(230)),
 			line(220, GOSUB(5820))
 		);
 		addLines(
 			line(230, REM("REM *****  PROGRAM STARTS HERE *****")),
 			line(240, SET(Z$, "                                                                     ")),
 			line(250, GOSUB(5460)),
 			line(260, DIM(G,8,8)),
 			line(261, DIM(C,9,2)),
 			line(262, DIM(K,3,3)),
 			line(263, DIM(N,3)),
 			line(264, DIM(Z,8,8)),
 			line(270, DIM(C$,6)),
 			line(271, DIM(D$,72)),
 			line(272, DIM(E$,24)),
 			line(273, DIM(A$,3)),
 			line(274, DIM(Q$,72)),
 			line(275, DIM(R$,72)),
 			line(276, DIM(S$,48)),
 			line(280, DIM(Z$,72)),
 			line(290, SET(T0, SET(T, MULT(INT(ADD(MULT(RND(),20),20)),100) ))),
 			line(300, SET(T9,20)),
 			line(310, SET(D0,0)),
 			line(320, SET(E0, SET(E, 3000))),
 			line(330, SET(P0, SET(P, 10))),
 			line(340, SET(S9, 200)),
 			line(350, SET(S, SET(H8,0))),
 			line(360, DEF(FND, FND_BODY)),
 			line(370, SET(Q1, INT(ADD(MULT(RND(),8),1)) )),
 			line(380, SET(Q2, INT(ADD(MULT(RND(),8),1)) )),
 			line(390, SET(S1, INT(ADD(MULT(RND(),8),1)) )),
 			line(400, SET(S2, INT(ADD(MULT(RND(),8),1)) )),
 			line(410, SET(T7, ADD(TIM(0), MULT(60,TIM(1))))),
 			line(420, SET(C.at(2,1), C.at(3,1), C.at(4,1), C.at(4,2), C.at(5,2), C.at(6,2)).TO(-1)),
 			line(430, SET(C.at(1,1), C.at(3,2), C.at(5,1), C.at(7,2), C.at(9,1)).TO(0)),
			line(440, SET(C.at(1,2), C.at(2,2), C.at(6,1), C.at(7,1), C.at(8,1), C.at(8,2), C.at(9,2)).TO(1)),
 			line(450, DIM(D,8)),
 			line(451, MAT(D,0)),
 			line(460, SET(D$, "WARP ENGINESS.R. SENSORSL.R. SENSORSPHASER CNTRL")),
 			line(470, SET(D$, 49, "PHOTON TUBESDAMAGE CNTRL")),
 			line(480, SET(E$, "SHIELD CNTRLCOMPUTER")),
 			line(490, SET(B9, SET(K9, 0))),
 			line(500, FOR(I).FROM(1).TO(8)),
 			line(510, FOR(J).FROM(1).TO(8)),
 			line(520, SET(R1, RND())),
 			line(530, IF(GT(R1,.98)).THEN(580)),
 			line(540, IF(GT(R1,.95)).THEN(610)),
 			line(550, IF(GT(R1,.8)).THEN(640)),
 			line(560, SET(K3,0)),
 			line(570, GOTO(660)),
 			line(580, SET(K3,3)),
 			line(590, SET(K9,ADD(K9,3))),
 			line(600, GOTO(660)),
 			line(610, SET(K3,2)),
 			line(620, SET(K9,ADD(K9,2))),
 			line(630, GOTO(660)),
 			line(640, SET(K3,1)),
 			line(650, SET(K9,ADD(K9,1))),
 			line(660, SET(R1,RND())),
 			line(670, IF(GT(R1,.96)).THEN(700)),
 			line(680, SET(B3,0)),
 			line(690, GOTO(720)),
 			line(700, SET(B3,1)),
 			line(710, SET(B9,ADD(B9,1))),
 			line(720, SET(S3,INT(ADD(MULT(RND(),8),1)))),
 			line(730, SET(G.at(I,J)).TO(ADD(ADD(MULT(K3,100),MULT(B3,10)),S3))),
 			line(740, SET(Z.at(I,J)).TO(0)),
 			line(750, NEXT(J)),
 			line(760, NEXT(I)),
 			line(770, SET(K7,K9)),
 			line(775, IF(OR(LTE(B9,0),LTE(K9,0))).THEN(490)),
 			line(780, PRINT("YOU MUST DESTROY",K9, " KLINGONS IN",T9, " STARDATES WITH",B9," STARBASES")),
 			line(810, SET(K3, SET(B3, SET(S3, 0)))),
 			line(820, IF(OR(LT(Q1,1),OR(GT(Q1,8),OR(LT(Q2,1),GT(Q2,8))))).THEN(920)),
 			line(830, SET(X,MULT(G.at(Q1,Q2),.01))),
line(831, PRINT("G(Q1,Q2)=",G.at(Q1,Q2))),
line(832, PRINT("X=",X)),
 			line(840, SET(K3,INT(X))),
line(841, PRINT("K3=",K3)),
 			line(850, SET(B3,INT(MULT(SUBTRACT(X,K3),10)))),
 			line(860, SET(S3,SUBTRACT(G.at(Q1,Q2),MULT(INT(MULT(G.at(Q1,Q2),.1)),10)))),
 			line(870, IF(EQ(K3,0)).THEN(910)),
 			line(880, IF(GT(S,200)).THEN(910)),
 			line(890, PRINT("COMBAT AREA      CONDITION RED")),
 			line(900, PRINT("   SHIELDS DANGEROUSLY LOW")),
 			line(910, MAT(K,0)),
 			line(920, FOR(I).FROM(1).TO(3)),
 			line(930, SET(K.at(I,3)).TO(0)),
 			line(940, NEXT(I)),
 			line(950, SET(Q$,Z$)),
 			line(960, SET(R$,Z$)),
 			line(970, SET(S$,SUBSTRING(Z$,1,48))),
 			line(980, SET(A$,"<*>")),
 			line(990, SET(Z1,S1)),
 			line(1000, SET(Z2,S2)),
 			line(1010, GOSUB(5510)),
 			line(1020, FOR(I).FROM(1).TO(K3)),
 			line(1030, GOSUB(5380)),
 			line(1040, SET(A$,"+++")),
 			line(1050, SET(Z1,R1)),
 			line(1060, SET(Z2,R2)),
 			line(1070, GOSUB(5510)),
 			line(1080, SET(K.at(I,1)).TO(R1)),
 			line(1090, SET(K.at(I,2)).TO(R2)),
 			line(1100, SET(K.at(I,3)).TO(S9)),
 			line(1110, NEXT(I)),
 			line(1120, FOR(I).FROM(1).TO(B3)),
 			line(1130, GOSUB(5380)),
 			line(1140, SET(A$,">!<")),
 			line(1150, SET(Z1,R1)),
 			line(1160, SET(Z2,R2)),
 			line(1170, GOSUB(5510)),
 			line(1180, NEXT(I)),
 			line(1190, FOR(I).FROM(1).TO(S3)),
 			line(1200, GOSUB(5380)),
 			line(1210, SET(A$," * ")),
 			line(1220, SET(Z1,R1)),
 			line(1230, SET(Z2,R2)),
 			line(1240, GOSUB(5510)),
 			line(1250, NEXT(I))
 		);
 		addLines(
 			line(1260, GOSUB(4120)),
 			line(1270, PRINT("COMMAND:")),
 			line(1280, INPUT(A)),
 			line(1290, GOTO(ADD(A,1)).OF(1410,1260,2330,2530,2800,3460,3560,4630)),
 			line(1300, PRINT()),
 			line(1310, PRINT("   0 = SET COURSE")),
 			line(1320, PRINT("   1 = SHORT RANGE SENSOR SCAN")),
 			line(1330, PRINT("   2 = LONG RANGE SENSOR SCAN")),
 			line(1340, PRINT("   3 = FIRE PHASERS")),
 			line(1350, PRINT("   4 = FIRE PHOTON TORPEDOE")),
 			line(1360, PRINT("   5 = SHIELD CONTROL")),
 			line(1370, PRINT("   6 = DAMAGE CONTROL REPORT")),
 			line(1380, PRINT("   7 = CALL ON LIBRARY COMPUTE")),
 			line(1400, GOTO(1270))
 		);
 		addLines(
 			line(1410, PRINT("COURSE (1-9):")),
 			line(1420, INPUT(C1)),
 			line(1430, IF(EQ(C1,0)).THEN(1270)),
 			line(1440, IF(OR(LT(C1,1),GTE(C1,9))).THEN(1410)),
 			line(1450, PRINT("WARP FACTOR (0-8):")),
 			line(1460, INPUT(W1)),
 			line(1470, IF(OR(LT(W1,0),GT(W1,8))).THEN(1410)),
 			line(1480, IF(OR(GTE(D.at(1),0),LTE(W1,.2))).THEN(1510)),
 			line(1490, PRINT("WARP ENGINES ARE DAMAGED, MAXIMUM SPEED = WARP .2")),
 			line(1500, GOTO(1410)),
 			line(1510, IF(LTE(K3,0)).THEN(1560)),
 			line(1520, GOSUB(3790)),
 			line(1530, IF(LTE(K3,0)).THEN(1560)),
 			line(1540, IF(LT(S,0)).THEN(4000)),
 			line(1550, GOTO(1610)),
 			line(1560, IF(GT(E,0)).THEN(1610)),
 			line(1570, IF(LT(S,1)).THEN(3920)),
 			line(1580, PRINT("YOU HAVE",E," UNITS OF ENERGY")),
 			line(1590, PRINT("SUGGEST YOU GET SOME FROM YOUR SHIELDS WHICH HAVE",S," UNITS LEFT")),
 			line(1600, GOTO(1270)),
 			line(1610, FOR(I).FROM(1).TO(8)),
 			line(1620, IF(GTE(D.at(I),0)).THEN(1640)),
 			line(1630, SET(D.at(I)).TO(ADD(D.at(I),1))),
 			line(1640, NEXT(I)),
 			line(1650, IF(GT(RND(),.2)).THEN(1810)),
 			line(1660, SET(R1,INT(ADD(MULT(RND(),8),1)))),
 			line(1670, IF(GTE(RND(),.5)).THEN(1750)),
 			line(1680, SET(D.at(R1)).TO(SUBTRACT(D.at(R1),ADD(MULT(RND(),5),1)))),
 			line(1690, PRINT()),
 			line(1700, PRINT("DAMAGE CONTROL REPORT:")),
 			line(1710, GOSUB(5610)),
 			line(1720, PRINT("DAMAGED")),
 			line(1730, PRINT()),
 			line(1740, GOTO(1810)),
 			line(1750, SET(D.at(R1)).TO(ADD(D.at(R1),ADD(MULT(RND(),5),1)))),
 			line(1760, PRINT()),
 			line(1770, PRINT("DAMAGE CONTROL REPORT:")),
 			line(1780, GOSUB(5610)),
 			line(1790, PRINT(" STATE OF REPAIR IMPROVED")),
 			line(1800, PRINT()),
 			line(1810, SET(NN, MULT(W1,8))),
 			line(1820, SET(A$,"   ")),
 			line(1830, SET(Z1,S1)),
 			line(1840, SET(Z2,S2)),
 			line(1850, GOSUB(5510)),
 			line(1870, SET(X,S1)),
 			line(1880, SET(Y,S2)),
 			line(1885, SET(C2,INT(C1))),
 			line(1890, SET(X1,ADD(C.at(C2,1),MULT(SUBTRACT(C.at(ADD(C2,1),1),C.at(C2,1)),SUBTRACT(C1,C2))))),
 			line(1900, SET(X2,ADD(C.at(C2,2),MULT(SUBTRACT(C.at(ADD(C2,1),2),C.at(C2,2)),SUBTRACT(C1,C2))))),
 			line(1910, FOR(I).FROM(1).TO(NN)),
 			line(1920, SET(S1, ADD(S1,X1))),
 			line(1930, SET(S2, ADD(S2,X2))),
 			line(1940, IF(OR(LT(S1,.5),OR(GTE(S1,8.5),OR(LT(S2,.5),GTE(S2,8.5))))).THEN(2170)),
 			line(1950, SET(A$,"   ")),
 			line(1960, SET(Z1,S1)),
 			line(1970, SET(Z2,S2)),
 			line(1980, GOSUB(5680)),
 			line(1990, IF(NEQ(Z3,0)).THEN(2070)),
 			line(2030, PRINT_USING(5370, S1,S2)),
 			line(2040, SET(S1,SUBTRACT(S1,X1))),
 			line(2050, SET(S2,SUBTRACT(S2,X2))),
 			line(2060, GOTO(2080)),
 			line(2070, NEXT(I)),
 			line(2080, SET(A$,"<*>")),
 			line(2083, SET(S1, INT(ADD(S1,.5)))),
 			line(2086, SET(S2, INT(ADD(S2,.5)))),
 			line(2090, SET(Z1,S1)),
 			line(2100, SET(Z2,S2)),
 			line(2110, GOSUB(5510)),
 			line(2120, SET(E,ADD(SUBTRACT(E,NN),5))),
 			line(2130, IF(LT(W1,1)).THEN(2150)),
 			line(2140, SET(T,ADD(T,1))),
 			line(2150, IF(GT(T,ADD(T0,T9))).THEN(3970)),
 			line(2160, GOTO(1260))
 		);
 		addLines(
 			line(2170, SET(X, ADD(MULT(Q1,8),ADD(X,MULT(X1,NN))))),
 			line(2180, SET(Y, ADD(MULT(Q2,8),ADD(Y,MULT(X2,NN))))),
 			line(2190, SET(Q1,INT(DIV(X,8)))),
 			line(2200, SET(Q2,INT(DIV(Y,8)))),
 			line(2210, SET(S1,INT(SUBTRACT(X,ADD(MULT(Q1,8),.5))))),
 			line(2220, SET(S2,INT(SUBTRACT(Y,ADD(MULT(Q2,8),.5))))),
 			line(2230, IF(NEQ(S1,0)).THEN(2260)),
 			line(2240, SET(Q1,SUBTRACT(Q1,1))),
 			line(2250, SET(S1,8)),
 			line(2260, IF(NEQ(S2,0)).THEN(2290)),
 			line(2270, SET(Q2,SUBTRACT(Q2,1))),
 			line(2280, SET(S2,8)),
 			line(2290, SET(T,ADD(T,1))),
 			line(2300, SET(E,SUBTRACT(E,ADD(NN,5)))),
 			line(2310, IF(GT(T,ADD(T0,T9))).THEN(3970)),
 			line(2320, GOTO(810))
 		);
 		addLines(
 			line(2330, IF(GTE(D.at(3), 0)).THEN(2370)),
 			line(2340, PRINT("LONG RANGE SENSORS ARE INOPERABE")),
			line(2350, IMAGE().STRING("LONG RANGE SENSOR SCAN FOR QUADRANT ").FORMAT_D().STRING(",").FORMAT_D()),
 			line(2360, GOTO(1270)),
 			line(2370, PRINT_USING(2350, Q1, Q2)),
 			line(2380, PRINT_USING(2520)),
 			line(2390, FOR(I).FROM(SUBTRACT(Q1,1)).TO(ADD(Q1,1))),
 			line(2400, MAT(N, 0)),
 			line(2410, FOR(J).FROM(SUBTRACT(Q2, 1)).TO(ADD(Q2,1))),
 			line(2420, IF(OR(LT(I,1),OR(GT(I,8),OR(LT(J,1),GT(J,8))))).THEN(2460)),
 			line(2430, SET(N.at(ADD(SUBTRACT(J,Q2),2))).TO(G.at(I,J))),
 			line(2440, IF(LT(D.at(7),0)).THEN(2460)),
 			line(2450, SET(Z.at(I,J)).TO(G.at(I,J))),
 			line(2460, NEXT(J)),
 			line(2470, PRINT_USING(2510, N.at(1), N.at(2), N.at(3))),
 			line(2480, PRINT_USING(2520)),
 			line(2490, NEXT(I)),
 			line(2500, GOTO(1270)),
			line(2510, IMAGE().STRING(": ").FORMAT_3_3D_STRING(" :")),
 			line(2520, IMAGE().STRING("-----------------"))
 		);
 		addLines(
		line(2530, PRINT("K3=",K3," K9=", K9)),
			line(2531, IF(LTE(K3, 0)).THEN(3670)),
 			line(2540, IF(GTE(D.at(4), 0)).THEN(2570)),
 			line(2550, PRINT("PHASER CONTROL IS DISABLED")),
 			line(2560, GOTO(1270)),
 			line(2570, IF(GTE(D.at(7), 0)).THEN(2590)),
 			line(2580, PRINT(" COMPUTER FAILURE HANPERS ACCURACY")),
 			line(2590, PRINT("PHASERS LOCKED ON TARGET.  ENERGY AVAILABLE=",E)),
 			line(2600, PRINT("NUMBER OF UNITS TO FIRE:")),
 			line(2610, INPUT(X)),
 			line(2620, IF(LTE(X, 0)).THEN(1270)),
 			line(2630, IF(LT(SUBTRACT(E, X),0)).THEN(2570)),
 			line(2640, SET(E,SUBTRACT(E, X))),
 			line(2650, GOSUB(3790)),
 			line(2660, IF(GTE(D.at(7), 0)).THEN(2680)),
 			line(2670, SET(X,MULT(X,RND()))),
 			line(2680, FOR(I).FROM(1).TO(3)),
 			line(2690, IF(LTE(K.at(I,3),0)).THEN(2770)),
 			line(2700, SET(H,MULT(DIV(DIV(X,K3),FND),MULT(2,RND())))),
 			line(2710, SET(K.at(I,3)).TO(SUBTRACT(K.at(I,3), H))),
 			line(2720, PRINT_USING(2730, H,K.at(I,1),K.at(I,2),K.at(I,3))),
 			line(2730, IMAGE().FORMAT_4D().STRING(" UNIT HIT ON KLINGON AT SECTOR ").FORMAT_D().STRING(",").FORMAT_D().STRING("   (").FORMAT_3D().STRING("LEFT")),
 			line(2740, IF(GT(K.at(I,3), 0)).THEN(2770)),
 			line(2750, GOSUB(3690)),
 			line(2770, NEXT(I)),
			line(2780, IF(LT(E,0)).THEN(4000)),
 			line(2790, GOTO(1270))
 		);
 		addLines(
 			line(2800, IF(GTE(D.at(5), 0)).THEN(2830)),
 			line(2810, PRINT(" PHOTON TUBES ARE NOT OPERATIONAL")),
 			line(2820, GOTO(1270)),
 			line(2830, IF(GT(P, 0)).THEN(2860)),
 			line(2840, PRINT("ALL PHOTON TORPEDOES ARE EXPENDED")),
 			line(2850, GOTO(1270)),
 			line(2860, PRINT("TORPEDO COURSE (1-9):")),
 			line(2870, INPUT(C1)),
 			line(2880, IF(EQ(C1, 0)).THEN(1270)),
 			line(2890, IF(OR(LT(C1, 1),GTE(C1, 9))).THEN(2860)),
 			line(2900, SET(X1, ADD(C.at(C2,1),MULT(SUBTRACT(C.at(ADD(C2,1),1),C.at(C2,1)),SUBTRACT(C1,C2))))),
 			line(2910, SET(X2, ADD(C.at(C2,2),MULT(SUBTRACT(C.at(ADD(C2,1),2),C.at(C2,2)),SUBTRACT(C1,C2))))),
 			line(2920, SET(X,S1)),
 			line(2930, SET(Y,S2)),
 			line(2940, SET(P,SUBTRACT(P, 1))),
 			line(2950, PRINT(" TORPEDO TRACK:")),
 			line(2960, SET(X,ADD(X,X1))),
 			line(2970, SET(Y,ADD(Y,X2))),
 			line(2980, IF(OR(LT(X, .5),OR(GTE(X, 8.5),OR(LT(Y, .5),GTE(Y, 8.5))))).THEN(3420)),
 			line(2990, PRINT_USING(3000, X,Y)),
 			line(3000, IMAGE().FORMAT_15X().FORMAT_D().STRING(",").FORMAT_D()),
 			line(3010, SET(A$, "   ")),
 			line(3020, SET(Z1,X)),
 			line(3030, SET(Z2,Y)),
 			line(3040, GOSUB(5680)),
 			line(3050, IF(EQ(Z3, 0)).THEN(3070)),
 			line(3060, GOTO(2960)),
 			line(3070, SET(A$,"+++")),
 			line(3080, SET(Z1,X)),
 			line(3090, SET(Z2,Y)),
 			line(3100, GOSUB(5680)),
 			line(3110, IF(EQ(Z3, 0)).THEN(3220)),
 			line(3120, PRINT("*** KLINGON DESTROYED ***")),
 			line(3130, SET(K3,SUBTRACT(K3, 1))),
 			line(3140, SET(K9,SUBTRACT(K9, 1))),
 			line(3150, IF(LTE(K9,0)).THEN(4040)),
 			line(3160, FOR(I).FROM(1).TO(3)),
 			line(3170, IF(NEQ(INT(ADD(X,.5)),K.at(I,1))).THEN(3190)),
 			line(3180, IF(EQ(INT(ADD(Y,.5)),K.at(I,2))).THEN(3200)),
 			line(3190, NEXT(I)),
 			line(3200, SET(K.at(I,3)).TO(0)),
 			line(3210, GOTO(3360)),
 			line(3220, SET(A$," * ")),
 			line(3230, SET(Z1,X)),
 			line(3240, SET(Z2,Y)),
 			line(3250, GOSUB(5680)),
 			line(3260, IF(EQ(Z3,0)).THEN(3290)),
 			line(3270, PRINT("YOU CANT'T DESTROY STARS SILLY")),
 			line(3280, GOTO(3420)),
 			line(3290, SET(A$,">!<")),
 			line(3300, SET(Z1,X)),
 			line(3310, SET(Z2,Y)),
 			line(3320, GOSUB(5680)),
 			line(3330, IF(EQ(Z3,0)).THEN(2960)),
 			line(3340, PRINT("*** STAR BASE DESTROYED ***  .......CONGRATULATIONS")),
 			line(3350, SET(B3,SUBTRACT(B3, 1))),
 			line(3360, SET(A$,"   ")),
 			line(3370, SET(Z1,INT(ADD(X,.5)))),
 			line(3380, SET(Z2,INT(ADD(Y,.5)))),
 			line(3390, GOSUB(5510)),
 			line(3400, SET(G.at(Q1,Q2)).TO(ADD(MULT(K3,100),ADD(MULT(B3,10),S3)))),
 			line(3410, GOTO(3430)),
 			line(3420, PRINT("TORPEDO MISSED")),
 			line(3430, GOSUB(3790)),
 			line(3440, IF(LT(E,0)).THEN(4000)),
 			line(3450, GOTO(1270))
 			);
 		addLines(
 			line(3460, IF(GTE(D.at(7),0)).THEN(3490)),
 			line(3470, PRINT("SHIELD CONTROL IS NON-OPERATIONAL")),
 			line(3480, GOTO(1270)),
 			line(3490, PRINT("ENERGY AVAILABLE =",ADD(E,S),"   NUMBER OF UNITS TO SHIELDS:")),
 			line(3500, INPUT(X)),
 			line(3510, IF(LTE(X,0)).THEN(1270)),
 			line(3520, IF(LT(ADD(E,SUBTRACT(S,X)),0)).THEN(3490)),
 			line(3530, SET(E,ADD(E,SUBTRACT(S,X)))),
 			line(3540, SET(S,X)),
 			line(3550, GOTO(1270))
 		);
 		addLines(
 			line(3560, IF(GTE(D.at(6),0)).THEN(3590)),
 			line(3570, PRINT("DAMAGE CONTROL REPORT IS NOT AVAILABLE")),
 			line(3580, GOTO(1270)),
 			line(3590, PRINT()),
 			line(3600, PRINT("DEVICE        STATE OF REPAIR")),
 			line(3610, FOR(R1).FROM(1).TO(8)),
 			line(3620, GOSUB(5610)),
 			line(3630, PRINT("",D.at(R1))),
 			line(3640,NEXT(R1)),
 			line(3650, PRINT()),
 			line(3660, GOTO(1270))
 		);
 		addLines(
 			line(3670, PRINT("SHORT RANGE SENSORS REPORT NO KLINGONS IN THIS QUADRANT")),
 			line(3680, GOTO(1270))
 		);
 		addLines(
 			line(3690, PRINT_USING(3700, K.at(I,1),K.at(I,2))),
 			line(3700, IMAGE().STRING(" KLINGON AT SECTOR ").FORMAT_D().STRING(",").FORMAT_D().STRING(" DESTROYED ****")),
 			line(3710, SET(K3,SUBTRACT(K3, 1))),
 			line(3720, SET(K9,SUBTRACT(K9, 1))),
 			line(3730, SET(A$,"   ")),
 			line(3740, SET(Z1,K.at(I,1))),
 			line(3750, SET(Z2,K.at(I,2))),
 			line(3760, GOSUB(5510)),
 			line(3770, SET(G.at(Q1,Q2)).TO(ADD(MULT(K3,100),ADD(MULT(B3,10),S3)))),
 			line(3780, RETURN())
 		);
 		addLines(
 			line(3790, IF(NEQ(C$,"DOCKED")).THEN(3820)),
 			line(3800, PRINT("STAR BASE SHIELDS PROTECT THE ENTERPRISE")),
 			line(3810, RETURN()),
 			line(3820, IF(LTE(K3,0)).THEN(3910)),
 			line(3830, FOR(I).FROM(1).TO(3)),
 			line(3840, IF(LTE(K.at(I,3),0)).THEN(3900)),
 			line(3850, SET(H,MULT(DIV(K.at(I,3),FND),MULT(2,RND())))),
 			line(3860, SET(S,SUBTRACT(S,H))),
 			line(3870, PRINT_USING(3880, H,K.at(I,1),K.at(I,2),S)),
 			line(3880, IMAGE().FORMAT_4D().STRING(" UNIT HIT ON ENTERPRISE AT SECTOR ").FORMAT_D().STRING(",").FORMAT_D().STRING("   (").FORMAT_4D().STRING(" LEFT")),
 			line(3890, IF(LT(S,0)).THEN(4000)),
 			line(3900, NEXT(I)),
 			line(3910, RETURN())
 		);
 		addLines(
 			line(3920, PRINT("THE ENTERPRISE IS DEAD IN SPACE.  IF YOU SURVIVE ALL IMPENDING")),
 			line(3930, PRINT("ATTACK YOU WILL BE DEMOTED TO THE RANK OF PRIVATE")),
 			line(3940, IF(LTE(K3,0)).THEN(4020)),
 			line(3950, GOSUB(3790)),
 			line(3960, GOTO(3940))
 		);
 		addLines(
 			line(3970, PRINT()),
 			line(3980, PRINT("IT IS STARDATE", T)),
 			line(3990, GOTO(4020))
 		);
 		addLines(
 			line(4000, PRINT()),
 			line(4010, PRINT("THE ENTERPRISE HAS BEEN DESTROYED.  THE FEDERATION WILL BE CONQUERED"))
 		);
 		addLines(
 			line(4020, PRINT("THERE ARE STILL",K9," KLINGON BATTLE CRUISERS")),
 			line(4030, GOTO(230))
 		);
 		addLines(
 			line(4040, PRINT()),
 			line(4050, PRINT("THE LAST KLINGON BATTLE CRUISER IN THE GALAXY HAS BEEN DESTROYED")),
 			line(4060, PRINT("THE FEDERATION HAS BEEN SAVED !!!")),
 			line(4070, PRINT()),
 			line(4080, PRINT("YOUR EFFICIENCY RATING =",MULT(DIV(K7,(SUBTRACT(T,T0))),1000))),
 			line(4090, SET(T1,ADD(TIM(0),MULT(TIM(1),60)))),
 			line(4100, PRINT("YOUR ACTUAL TIME OF MISSION = ",INT(MULT(SUBTRACT(MULT(SUBTRACT(T1,T7),.4),T7),100))," MINUTES")),
 			line(4110, GOTO(230))
 		);
 		addLines(
 			line(4120, FOR(I).FROM(SUBTRACT(S1,1)).TO(ADD(S1,1))),
 			line(4130, FOR(J).FROM(SUBTRACT(S2,1)).TO(ADD(S2,1))),
 			line(4140, IF(OR(LT(I,1),OR(GT(I,8),OR(LT(J,1),GT(J,8))))).THEN(4200)),
 			line(4150, SET(A$,">!<")),
 			line(4160, SET(Z1,I)),
 			line(4170, SET(Z2,J)),
 			line(4180, GOSUB(5680)),
 			line(4190, IF(EQ(Z3,1)).THEN(4240)),
 			line(4200, NEXT(J)),
 			line(4210, NEXT(I)),
 			line(4220, SET(D0,0)),
 			line(4230, GOTO(4310)),
 			line(4240, SET(D0,1)),
 			line(4250, SET(D$, "DOCKED")),
 			line(4260, SET(E,3000)),
 			line(4270, SET(P,10)),
 			line(4280, PRINT("SHIELDS DROPPED FOR DOCKING PURPOSES")),
 			line(4290, SET(S,0)),
 			line(4300, GOTO(4380)),
 			line(4310, IF(GT(K3,0)).THEN(4350)),
 			line(4320, IF(LT(E,MULT(E0,.1))).THEN(4370)),
 			line(4330, SET(C$,"GREEN")),
 			line(4340, GOTO(4380)),
 			line(4350, SET(C$,"RED")),
 			line(4360, GOTO(4380)),
 			line(4370, SET(C$,"YELLOW")),
 			line(4380, IF(GTE(D.at(2),0)).THEN(4430)),
 			line(4390, PRINT()),
 			line(4400, PRINT("*** SHORT RANGE SENSORS ARE OUT ***")),
 			line(4410, PRINT()),
 			line(4420, GOTO(4530)),
 			line(4430, PRINT_USING(4540)),
 			line(4440, PRINT_USING(4550,Q$.chunk(1,3),Q$.chunk(4,6),Q$.chunk(7,9),Q$.chunk(10,12),Q$.chunk(13,15),Q$.chunk(16,18),Q$.chunk(19,21),Q$.chunk(22,24))),
 			line(4450, PRINT_USING(4560,Q$.chunk(25,27),Q$.chunk(28,30),Q$.chunk(31,33),Q$.chunk(34,36),Q$.chunk(37,39),Q$.chunk(40,42),Q$.chunk(43,45),Q$.chunk(46,48),T)),
 			line(4460, PRINT_USING(4570,Q$.chunk(49,51),Q$.chunk(52,54),Q$.chunk(55,57),Q$.chunk(58,60),Q$.chunk(61,63),Q$.chunk(64,66),Q$.chunk(67,69),Q$.chunk(70,72),C$)),
 			line(4470, PRINT_USING(4580,R$.chunk(1,3),R$.chunk(4,6),R$.chunk(7,9),R$.chunk(10,12),R$.chunk(13,15),R$.chunk(16,18),R$.chunk(19,21),R$.chunk(22,24),Q1,Q2)),
 			line(4480, PRINT_USING(4590,R$.chunk(25,27),R$.chunk(28,30),R$.chunk(31,33),R$.chunk(34,36),R$.chunk(37,39),R$.chunk(40,42),R$.chunk(43,45),R$.chunk(46,48),S1,S2)),
 			line(4490, PRINT_USING(4600,R$.chunk(49,51),R$.chunk(52,54),R$.chunk(55,57),R$.chunk(58,60),R$.chunk(61,63),R$.chunk(64,66),R$.chunk(67,69),R$.chunk(70,72),E)),
 			line(4500, PRINT_USING(4610,S$.chunk(1,3),S$.chunk(4,6),S$.chunk(7,9),S$.chunk(10,12),S$.chunk(13,15),S$.chunk(16,18),S$.chunk(19,21),S$.chunk(22,24),P)),
 			line(4510, PRINT_USING(4620,S$.chunk(25,27),S$.chunk(28,30),S$.chunk(31,33),S$.chunk(34,36),S$.chunk(37,39),S$.chunk(40,42),S$.chunk(43,45),S$.chunk(46,48),S)),
 			line(4520, PRINT_USING(4540)),
 			line(4530, RETURN())
 		);
 		addLines(
 			line(4540, IMAGE().STRING("---------------------------------")),
 			line(4550, IMAGE().FORMAT_8_X_3A()),
 			line(4560, IMAGE().FORMAT_8_X_3A().FORMAT_8X().STRING("STARDATE").FORMAT_8X().FORMAT_5D()),
 			line(4570, IMAGE().FORMAT_8_X_3A().FORMAT_8X().STRING("CONDITION").FORMAT_8X().FORMAT_6A()),
 			line(4580, IMAGE().FORMAT_8_X_3A().FORMAT_8X().STRING("QUADRANT").FORMAT_9X().FORMAT_D().STRING(",").FORMAT_D()),
 			line(4590, IMAGE().FORMAT_8_X_3A().FORMAT_8X().STRING("SECTOR").FORMAT_11X().FORMAT_D().STRING(",").FORMAT_D()),
 			line(4600, IMAGE().FORMAT_8_X_3A().FORMAT_8X().STRING("ENERGY").FORMAT_9X().FORMAT_6D()),
 			line(4610, IMAGE().FORMAT_8_X_3A().FORMAT_8X().STRING("PHOTON TORPEDOES").FORMAT_3D()),
 			line(4620, IMAGE().FORMAT_8_X_3A().FORMAT_8X().STRING("SHIELDS").FORMAT_8X().FORMAT_6D())
 		);
 		addLines(
 			line(4630, IF(GTE(D.at(8),0)).THEN(4660)),
 			line(4640, PRINT("COMPUTER DISABLED")),
 			line(4650, GOTO(1270)),
 			line(4660, PRINT("COMPUTER ACTIVE AND AWAITING COMMAND")),
 			line(4670, INPUT(A)),
 			line(4680, GOTO(ADD(A,1)).OF(4740, 4830, 4880)),
 			line(4690, PRINT("FUNCTIONS AVAILABLE FROM COMPUTER")),
 			line(4700, PRINT("   0 = CUMULATIVE GALACTIC RECORD")),
 			line(4710, PRINT("   1 = STATUS REPORT")),
 			line(4720, PRINT("   2 = PHOTON TORPEDO DATA")),
 			line(4730, GOTO(4660))
 		);
 		addLines(
 			line(4740, PRINT_USING(4750, Q1,Q2)),
 			line(4750, IMAGE().STRING("COMPUTER RECORD OF GALAXY FOR QUADRANT ").FORMAT_D().STRING(",").FORMAT_D()),
 			line(4760, PRINT_USING(5330)),
 			line(4770, PRINT_USING(5360)),
 			line(4780, FOR(I).FROM(1).TO(8)),
 			line(4790, PRINT_USING(5350, I,Z.at(I,1),Z.at(I,2),Z.at(I,3),Z.at(I,4),Z.at(I,5),Z.at(I,6),Z.at(I,7),Z.at(I,8))),
 			line(4800, PRINT_USING(5360)),
 			line(4810, NEXT(I)),
 			line(4820, GOTO(1270))
 		);
 		addLines(
 			line(4830, PRINT("\012   STATUS REPORT\012")),
 			line(4830, PRINT("NUMBER OF KLINGONS LEFT =",K9)),
 			line(4830, PRINT("NUMBER OF STARDATES LEFT =",SUBTRACT(ADD(T0,T9), T))),
 			line(4830, PRINT("NUMBER OF STARBASES LEFT =",B9)),
 			line(4870, GOTO(3560))
 		);
 		addLines(
 			line(4880, PRINT()),
 			line(4890, SET(H8,0)),
 			line(4900, FOR(I).FROM(1).TO(3)),
 			line(4910, IF(LTE(K.at(I,3),0)).THEN(5260)),
 			line(4920, SET(C1,S1)),
 			line(4930, SET(A,S2)),
 			line(4940, SET(W1,K.at(I,1))),
 			line(4950, SET(X,K.at(I,2))),
 			line(4960, GOTO(5010)),
 			line(4970, PRINT_USING(4980, Q1,Q2,S1,S2)),
 			line(4980, IMAGE().STRING("YOU ARE AT QUADRANT (").FORMAT_D().STRING(",").FORMAT_D().STRING(" )  SECTOR ( ").FORMAT_D().STRING(",").FORMAT_D().STRING(" )")),
 			line(4990, PRINT("SHIP'S & TARGET'S COORDINATES ARE")),
 			line(5000, INPUT(C1)),
 			line(5001, INPUT(A)),
 			line(5002, INPUT(W1)),
 			line(5003, INPUT(X)),
 			line(5010, SET(X,SUBTRACT(X, A))),
 			line(5020, SET(A,SUBTRACT(C1, W1))),
 			line(5030, IF(LT(X,0)).THEN(5130)),
 			line(5040, IF(LT(A,0)).THEN(5190)),
 			line(5050, IF(GT(X,0)).THEN(5070)),
 			line(5060, IF(EQ(A,0)).THEN(5150)),
 			line(5070, SET(C1,1)),
 			line(5080, IF(LTE(ABS(A),ABS(X))).THEN(5110)),
 			line(5090, PRINT("DIRECTION=",ADD(C1,DIV(ADD(SUBTRACT(ABS(A),ABS(X)),ABS(A)),ABS(A))))),
 			line(5100, GOTO(5240)),
 			line(5110, PRINT("DIRECTION =",ADD(C1,DIV(ABS(A),ABS(X))))),
 			line(5120, GOTO(5240)),
 			line(5130, IF(GT(A,0)).THEN(5170)),
 			line(5140, IF(EQ(X,0)).THEN(5190)),
 			line(5150, SET(C1,5)),
 			line(5160, GOTO(5080)),
 			line(5170, SET(C1,3)),
 			line(5180, GOTO(5200)),
 			line(5190, SET(C1,7)),
 			line(5200, IF(GTE(ABS(A),ABS(X))).THEN(5230)),
 			line(5210, PRINT("DIRECTION=",ADD(C1,DIV(ADD(SUBTRACT(ABS(X),ABS(A)),ABS(X)),ABS(X))))),
 			line(5220, GOTO(5240)),
 			line(5230, PRINT("DIRECTION =",ADD(C1,DIV(ABS(X),ABS(A))))),
 			line(5240, PRINT("DISTANCE =",SQR(ADD(MULT(X,X),MULT(A,A))))),
 			line(5250, IF(EQ(H8,1)).THEN(5320)),
 			line(5260, NEXT(I)),
 			line(5270, SET(H8,0)),
 			line(5280, PRINT("DO YOU WANT TO USE THE CALCULATOR")),
 			line(5290, INPUT(A$)),
 			line(5300, IF(EQ(A$,"YES")).THEN(4970)),
 			line(5310, IF(NEQ(A$,"NO")).THEN(5280)),
 			line(5320, GOTO(1270))
 		);
 		addLines(
 			line(5330, IMAGE().STRING("     1     2     3     4     5     6     7     8")),
 			line(5340, IMAGE().STRING("--------------------------------------------------")),
 			line(5350, IMAGE().FORMAT_D().FORMAT_8_3X_3D()),
 			line(5360, IMAGE().STRING("   ----- ----- ----- ----- ----- ----- ----- -----")),
 			line(5370, IMAGE().STRING(" WARP ENGINES SHUTDOWN AT SECTOR ").FORMAT_D().STRING(",").FORMAT_D().STRING( "DUE TO BAD NAVIGATION"))
 		);
 		addLines(
 			line(5380, SET(R1, INT(ADD(MULT(RND(),8),1)))),
 			line(5390, SET(R2, INT(ADD(MULT(RND(),8),1)))),
 			line(5400, SET(A$,"   ")),
 			line(5410, SET(Z1,R1)),
 			line(5410, SET(Z2,R2)),
 			line(5430, GOSUB(5680)),
 			line(5440, IF(EQ(Z3,0)).THEN(5380)),
 			line(5450, RETURN())
 		);
 		addLines(
 			line(5460, REM()),
 			line(5460, FOR(I).FROM(1).TO(11)),
 			line(5470, PRINT("")),
 			line(5480, NEXT(I)),
 			line(5490, PRINT()),
 			line(5500, RETURN())
 		);
 		addLines(
 			line(5510, REM("REM ******  INSERTION IN STRING ARRAY FOR QUADRANT ******")),
 			line(5520, SET(S8,SUBTRACT(ADD(MULT(Z1,24),MULT(Z2,3)),26) )),
 			line(5530, IF(GT(S8,72)).THEN(5560)),
 			line(5540, REPLACE(Q$,S8,ADD(S8,2)).WITH(A$)),
 			line(5550, GOTO(5600)),
 			line(5560, IF(GT(S8,144)).THEN(5590)),
 			line(5570, REPLACE(R$,SUBTRACT(S8,72),SUBTRACT(S8,70)).WITH(A$)),
 			line(5580, GOTO(5600)),
 			line(5590, REPLACE(S$,SUBTRACT(S8,144),SUBTRACT(S8,142)).WITH(A$)),
 			line(5600, RETURN())
 			);
 		addLines(
 			line(5610, REM("****  PRINTS DEVICE NAME FROM ARRAY ****")),
 			line(5620, SET(S8,SUBTRACT(MULT(R1,12),11))),
 			line(5630, IF(GT(S8,72)).THEN(5660)),
 			line(5640, PRINT(D$.chunk(S8, ADD(S8,11)))),
 			line(5650, GOTO(5670)),
 			line(5660, PRINT(E$.chunk(SUBTRACT(S8,72), SUBTRACT(S8,61)))),
 			line(5670, RETURN())
 		);
 		addLines(
 			line(5680, REM("*******  STRING COMPARISON IN QUADRANT ARRAY **********")),
 			line(5683, SET(Z1,INT(ADD(Z1,.5)))),
 			line(5686, SET(Z2,INT(ADD(Z2,.5)))),
 			line(5690, SET(S8,SUBTRACT(ADD(MULT(Z1,24),MULT(Z2,3)),26))),
 			line(5700, SET(Z3,0)),
 			line(5710, IF(GT(S8,72)).THEN(5750)),
 			line(5720, IF(NEQ(SUBSTRING(Q$,S8,ADD(S8,2)),A$)).THEN(5810)),
 			line(5730, SET(Z3,1)),
 			line(5740, GOTO(5810)),
 			line(5750, IF(GT(S8,144)).THEN(5790)),
 			line(5760, IF(NEQ(SUBSTRING(R$,SUBTRACT(S8,72),SUBTRACT(S8,70)),A$)).THEN(5810)),
 			line(5770, SET(Z3,1)),
 			line(5780, GOTO(5810)),
 			line(5790, IF(NEQ(SUBSTRING(S$,SUBTRACT(S8,144),SUBTRACT(S8,142)),A$)).THEN(5810)),
 			line(5800, SET(Z3,1)),
 			line(5810, RETURN())
 		);
 		addLines(
 			line(5820, PRINT("     INSTRUCTIONS:")),
 			line(5830, PRINT("<*> = ENTERPRISE")),
 			line(5840, PRINT("+++ = KLINGON")),
 			line(5850, PRINT(">!< = STARBASE")),
 			line(5860, PRINT(" *  = STAR")),
 			line(5870, PRINT("COMMAND 0 = WARP ENGINE CONTROL")),
 			line(5880, PRINT("  'COURSE' IS IN A CIRCULAR NUMERICAL          4  3  2")),
 			line(5890, PRINT("  VECTOR ARRANGEMENT AS SHOWN.                  \\ ^ /")),
 			line(5900, PRINT("  INTERGER AND REAL VALUES MAY BE                \\^/")),
 			line(5910, PRINT("  USED.  THEREFORE COURSE 1.5 IS              5 ----- 1")),
 			line(5920, PRINT("  HALF WAY BETWEEN 1 AND 2.                      /^\\")),
 			line(5930, PRINT("                                                / ^ \\")),
 			line(5940, PRINT("  A VECTOR OF 9 IS UNDEFINED, BUT              6  7  8")),
 			line(5950, PRINT("  VALUES MAY APPROACH 9.")),
 			line(5960, PRINT("                                               COURSE")),
 			line(5970, PRINT("  ONE 'WARP FACTOR' IS THE SIZE OF")),
 			line(5980, PRINT("  ONE QUADRANT.  THEREFORE TO GET")),
 			line(5990, PRINT("  FROM QUADRANT 6,5 TO 5,5 YOU WOULD")),
 			line(6000, PRINT("  USE COURSE 3, WARP FACTOR 1")),
 			line(6010, PRINT("COMMAND 1 = SHORT RANGE SENSOR SCAN")),
 			line(6020, PRINT("  PRINTS THE QUADRANT YOU ARE CURRENTLY IN, INCLUDING")),
 			line(6030, PRINT("  STARS, KLINGONS, STARBASES, AND THE ENTERPRISE; ALONG")),
 			line(6040, PRINT("  WITH OTHER PERTINATE INFORMATION.")),
 			line(6050, PRINT("COMMAND 2 = LONG RANGE SENSOR SCAN")),
 			line(6060, PRINT("  SHOWS CONDITIONS IN SPACE FOR ONE QUADRANT ON EACH SIDE")),
 			line(6070, PRINT("  OF THE ENTERPRISE IN THE MIDDLE OF THE SCAN.  THE SCAN")),
 			line(6080, PRINT("  IS CODED IN THE FORM XXX, WHERE THE UNITS DIGIT IS THE")),
 			line(6090, PRINT("  NUMBER OF STARS, THE TENS DIGIT IS THE NUMBER OF STAR-")),
 			line(6100, PRINT("  BASES, THE HUNDREDS DIGIT IS THE NUMBER OF KLINGONS.")),
 			line(6110, PRINT("COMMAND 3 = PHASER CONTROL")),
 			line(6120, PRINT("  ALLOWS YOU TO DESTROY THE KLINGONS BY HITTING HIM WITH")),
 			line(6130, PRINT("  SUITABLY LARGE NUMBERS OF ENERGY UNITS TO DEPLETE HIS ")),
 			line(6140, PRINT("  SHIELD POWER.  KEEP IN MIND THAT WHEN YOU SHOOT AT")),
 			line(6150, PRINT("  HIM, HE GONNA DO IT TO YOU TOO.")),
 			line(6160, PRINT("COMMAND 4 = PHOTON TORPEDO CONTROL")),
 			line(6170, PRINT("  COURSE IS THE SAME AS USED IN WARP ENGINE CONTROL")),
 			line(6180, PRINT("  IF YOU HIT THE KLINGON, HE IS DESTROYED AND CANNOT FIRE")),
 			line(6190, PRINT("  BACK AT YOU.  IF YOU MISS, HE WILL SHOOT HIS PHASERS AT")),
 			line(6200, PRINT("  YOU.")),
 			line(6210, PRINT("   NOTE: THE LIBRARY COMPUTER (COMMAND 7) HAS AN OPTION")),
 			line(6220, PRINT("   TO COMPUTE TORPEDO TRAJECTORY FOR YOU (OPTION 2)")),
 			line(6230, PRINT("COMMAND 5 = SHIELD CONTROL")),
 			line(6240, PRINT("  DEFINES NUMBER OF ENERGY UNITS TO BE ASSIGNED TO SHIELDS")),
 			line(6250, PRINT("  ENERGY IS TAKEN FROM TOTAL SHIP'S ENERGY.")),
 			line(6260, PRINT("COMMAND 6 = DAMAGE CONTROL REPORT")),
 			line(6270, PRINT("  GIVES STATE OF REPAIRS OF ALL DEVICES.  A STATE OF REPAIR")),
 			line(6280, PRINT("  LESS THAN ZERO SHOWS THAT THAT DEVICE IS TEMPORARALY")),
 			line(6290, PRINT("  DAMAGED.")),
 			line(6300, PRINT("COMMAND 7 = LIBRARY COMPUTER")),
 			line(6310, PRINT("  THE LIBRARY COMPUTER CONTAINS THREE OPTIONS:")),
 			line(6320, PRINT("    OPTION 0 = CUMULATIVE GALACTIC RECORD")),
 			line(6330, PRINT("     SHOWS COMPUTER MEMORY OF THE RESULTS OF ALL PREVIOUS")),
 			line(6340, PRINT("     LONG RANGE SENSOR SCANS")),
 			line(6350, PRINT("    OPTION 1 = STATUS REPORT")),
 			line(6360, PRINT("     SHOWS NUMBER OF KLINGONS, STARDATES AND STARBASES")),
 			line(6370, PRINT("     LEFT.")),
 			line(6380, PRINT("    OPTION 2 = PHOTON TORPEDO DATA")),
 			line(6390, PRINT("     GIVES TRAJECTORY AND DISTANCE BETWEEN THE ENTERPRISE")),
 			line(6400, PRINT("     AND ALL KLINGONS IN YOUR QUADRANT")),
 			line(6410, RETURN())
 		);
 	}
 }
