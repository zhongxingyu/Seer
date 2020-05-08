 package Utils.Monopoly;
 
import com.sun.javaws.exceptions.InvalidArgumentException;

 /**
  * Created by IntelliJ IDEA.
  * User: bsankar
  * Date: 8/20/12
  */
 public enum MonopolySquares {
     GO("00"),
     A1("01"),
     CC1("02"),
     A2("03"),
     T1("04"),
     R1("05"),
     B1("06"),
     CH1("07"),
     B2("08"),
     B3("09"),
     JAIL("10"),
     C1("11"),
     U1("12"),
     C2("13"),
     C3("14"),
     R2("15"),
     D1("16"),
     CC2("17"),
     D2("18"),
     D3("19"),
     FP("20"),
     E1("21"),
     CH2("22"),
     E2("23"),
     E3("24"),
     R3("25"),
     F1("26"),
     F2("27"),
     U2("28"),
     F3("29"),
     G2J("30"),
     G1("31"),
     G2("32"),
     CC3("33"),
     G3("34"),
     R4("35"),
     CH3("36"),
     H1("37"),
     T2("38"),
     H2("39");
 
     String modalValue;
     int value;
     public static final int size = 40;
 
     MonopolySquares(String value) {
         this.modalValue = value;
         this.value = Integer.parseInt(value);
     }
 
     public String getModalValue() {
         return this.modalValue;
     }
 
     public int getValue() {
         return this.value;
     }
 
     public static MonopolySquares getByValue(int value) throws Exception {
 
         switch (value) {
             case 0:
                 return GO;
             case 1:
                 return A1;
             case 2:
                 return CC1;
             case 3:
                 return A2;
             case 4:
                 return T1;
             case 5:
                 return R1;
             case 6:
                 return B1;
             case 7:
                 return CH1;
             case 8:
                 return B2;
             case 9:
                 return B3;
             case 10:
                 return JAIL;
             case 11:
                 return C1;
             case 12:
                 return U1;
             case 13:
                 return C2;
             case 14:
                 return C3;
             case 15:
                 return R2;
             case 16:
                 return D1;
             case 17:
                 return CC2;
             case 18:
                 return D2;
             case 19:
                 return D3;
             case 20:
                 return FP;
             case 21:
                 return E1;
             case 22:
                 return CH2;
             case 23:
                 return E2;
             case 24:
                 return E3;
             case 25:
                 return R3;
             case 26:
                 return F1;
             case 27:
                 return F2;
             case 28:
                 return U2;
             case 29:
                 return F3;
             case 30:
                 return G2J;
             case 31:
                 return G1;
             case 32:
                 return G2;
             case 33:
                 return CC3;
             case 34:
                 return G3;
             case 35:
                 return R4;
             case 36:
                 return CH3;
             case 37:
                 return H1;
             case 38:
                 return T2;
             case 39:
                 return H2;
             default:
                throw new InvalidArgumentException(new String[]{"Invalid square"});
         }
     }
 }
