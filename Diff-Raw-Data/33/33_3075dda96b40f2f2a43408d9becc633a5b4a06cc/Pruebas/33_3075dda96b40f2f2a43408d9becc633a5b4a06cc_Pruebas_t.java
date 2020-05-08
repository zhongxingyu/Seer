 
 package main;
 
 import java.math.BigDecimal;
import metodosnumericos.Matriz;
 import metodosnumericos.SistemaEcuacionesLineales;
 
 /**
  *
  * @author jberdugo10
  */
 public class Pruebas {
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws Exception {
        
        System.out.println(Matriz.pascal(10));
        
         BigDecimal zero = BigDecimal.ZERO;
         BigDecimal uno = BigDecimal.ONE;
         BigDecimal cuatro = new BigDecimal(4);
         BigDecimal mat[][] = {
             {cuatro,uno.negate(),zero,zero,uno},
             {uno.negate(),cuatro,uno.negate(),zero,uno},
             {zero,uno.negate(),cuatro,uno.negate(),uno},
             {zero,zero,uno.negate(),cuatro,uno}};
         BigDecimal mat1[][] = new BigDecimal[mat.length][mat[0].length];
         for (int i = 0; i < mat.length; i++) {
             System.arraycopy(mat[i], 0, mat1[i], 0, mat[0].length);
         }
         SistemaEcuacionesLineales m = new SistemaEcuacionesLineales(mat1);
         m.metodoGauss().imprimirMatriz("primera gauss");
         SistemaEcuacionesLineales B = new SistemaEcuacionesLineales(mat);
         B.metodoJordan().imprimirMatriz("La misma jordan");
         
        
        
         /*
         double mat3[][] = {
             {1,-0.25,0,0,0.25},
             {-0.25,1,-0.25,0,0.25},
             {0,-0.25,1,-0.25,0.25},
             {0,0,-0.25,1,0.25}};
         SistemaEcuacionesLineales sel = new SistemaEcuacionesLineales(mat);
         sel.metodoJacobi(15, 0.0001d);
         sel.metodoSeidel(15, 0.0001d);
         
         Matriz b = sel.getVectorB();
         Matriz coef = sel.getMatrizCoef();
         Matriz inv = coef.inversa();
         Matriz d = coef.diagonal();
         Matriz u = coef.trianguloSuperior();
         Matriz us = coef.trianguloSuperiorEstricto();
         Matriz l = coef.trianguloInferior();
         Matriz ls = coef.trianguloInferiorEstricto();
         Matriz r = us.sumar(ls);
         
         sel.getMatrizAmpliada().imprimirMatriz("Matriz");
         coef.imprimirMatriz("Matriz de Coeficientes");
         inv.imprimirMatriz("Matriz inversa de Coeficientes");
         b.imprimirMatriz("Vector de constantes");
         inv.multipicar(b).imprimirMatriz("Hope this works");
         d.imprimirMatriz("D");
         coef.restar(d).imprimirMatriz("Coef - D");
         u.imprimirMatriz("U");
         us.imprimirMatriz("U'");
         l.imprimirMatriz("L");
         ls.imprimirMatriz("L'");
         
         
         Matriz dInv = d.inversa();
         Matriz X = b;
         //METODO DE JACOBI -- HERMOSAMENTE ANALITICO-NUMERICO
         for (int i = 0; i < 3; i++) {
             Matriz rX = r.multipicar(X);
             Matriz b_rX = b.restar(rX);
             Matriz X1 = dInv.multipicar(b_rX);
             X1.imprimirMatriz("Jacobi Iteración "+i);
             X = X1;
         }
         
         Matriz lInv = l.inversa();
         //METODO GAUSS-SEIDEL -- NO SE
         for (int i = 0; i < 3; i++) {
             Matriz usX = us.multipicar(X);
             Matriz b_usX = b.restar(usX);
             Matriz X1 = lInv.multipicar(b_usX);
             X1.imprimirMatriz("Seidel Iteración "+i);
             X = X1;
         }
         */
     }
 }
