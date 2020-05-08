 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package logica;
 
 import java.awt.Point;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.ArrayList;
 import java.util.Vector;
 
 /**
  *
  * @author roberacc
  */
 public class PoligonoFinal {
 
     int tamaño;
     int inicialY;
     int finalY;
     int k = 0;
     int[] indiceY;
     int[] out;
     boolean acep = false;
     ArrayList indi;
     Vector vIniciales = new Vector();
     Vector vAux = new Vector();
     Vector vPuntos = new Vector();
     Vector vPuntosFinales = new Vector();
     Vector v3 = new Vector();
     Vector v2 = new Vector();
 
     public PoligonoFinal() {
 //        Point[] puntosA = {new Point(-4, -5), new Point(4, -3), new Point(8, 8),
 //            new Point(5, 5), new Point(5, 10), new Point(-3, 8), new Point(0, 0)};
 //        Point[] puntosB = {new Point(-4, -5), new Point(0, -3), new Point(8, -8),
 //            new Point(5, 8), new Point(5, 12), new Point(0, 10), new Point(-5, 8)};
 //
 ////        arregloInicial(puntosB);
 ////        scanLine(puntosB);
 //
 //        arregloInicial(puntosA);
 //        scanLine(puntosA);
 //        vPuntosFinales();
     }
 
     public ArrayList<Point> algoritmoPoligono(Point[] puntos) {
         arregloInicial(puntos);
         scanLine(puntos);
         vPuntosFinales();
 
 
         for (int i = 0; i < vPuntosFinales.size(); i++) {
 
             Vector getVector2 = (Vector) vPuntosFinales.get(i);
             for (int w = 0; w < getVector2.size(); w++) {
                 Point ab = (Point) getVector2.get(w);
                 System.out.println("todos los puntos w= " + w + " = " + ab.x + " " + ab.y);
             }
             System.out.println();
         }
 
         ArrayList<Point> vectorTodosPuntos = vectorTodosPuntos(vPuntosFinales);
         return vectorTodosPuntos;
     }
 
     //Crea el Vector auxiliar
     void scanLine(Point[] puntos) {
 
         for (int i = 0; i < puntos.length; i++) {
 
             if (i == 0) {
                 vIniciales.add(ordenVector(puntos[puntos.length - 1], puntos[i], puntos[i + 1], puntos[i].y));
                 continue;
             }
 
             if (i == puntos.length - 1) {
                 vIniciales.add(ordenVector(puntos[i - 1], puntos[i], puntos[0], puntos[i].y));
                 break;
             }
 
             if (puntos[i].y > puntos[i - 1].y && puntos[i].y > puntos[i + 1].y) {
                 continue;
             }
 
             vIniciales.add(ordenVector(puntos[i - 1], puntos[i], puntos[i + 1], puntos[i].y));

         }
 
     }
 
     //Genera el vector con los vectores finales
     void vPuntosFinales() {
 
         int idx = indiceY[0];
         vPuntos.add(obtieneVectorFinal(vIniciales, idx));
         Vector text = (Vector) vPuntos.get(0);
         v3 = text;
         v2 = obtenerVectorPuntos(v3, idx);
         //vPuntosFinales.add(obtenerVectorPuntos(v3, idx));
 
 
         for (int i = 0; i < text.size(); i++) {
             double[] mostrar = new double[10];
             mostrar = (double[]) text.get(i);
            System.out.println("vectorMostrar [" + mostrar[0] + ", " + mostrar[1] + ", " + mostrar[2] + "] ");
         }
 
         for (int i = 1; i < tamaño + 1; i++) {
 
             idx = indiceY[i];
             Vector vec = (obtieneVectorFinal(vIniciales, idx));
 
             if (acep && i > 1) {
                 v3 = (incrementoAutomatico(v3));
                 v3 = unirVectores(v3, vec);
                 v3 = EliminaFinales(v3, idx);
 
                 v3 = ordenarVector(v3);
                 vPuntos.add(v3);
                 v2 = obtenerVectorPuntos(v3, idx);
                 // vPuntosFinales.add(obtenerVectorPuntos(v3, idx));
 
 
                 Vector vPuntosFinales = (Vector) vPuntos.get(i);
 
                 for (int r = 0; r < vPuntosFinales.size(); r++) {
                     double[] point = new double[3];
 
                     point = (double[]) vPuntosFinales.get(r);
                     System.out.println("punto: en " + r + " [" + point[0] + ", " + point[1] + ", " + point[2] + "]");
 
                 }
                 acep = false;
                 continue;
             }
 
 
 
             v3 = (incrementoAutomatico(v3));
             v3 = EliminaFinales(v3, idx);
             v3 = ordenarVector(v3);
 
             vPuntos.add(v3);
             v2 = obtenerVectorPuntos(v3, idx);
             //vPuntosFinales.add(obtenerVectorPuntos(v3, idx));
 
             Vector vPuntosFinales = (Vector) vPuntos.get(i);
 
             for (int r = 0; r < vPuntosFinales.size(); r++) {
                 double[] point = new double[3];
 
                 point = (double[]) vPuntosFinales.get(r);
                 System.out.println("punto: en " + r + " [" + point[0] + ", " + point[1] + ", " + point[2] + "]");
 
             }
         }
 
     }
 
     //Une un vector del vector auxiliar, con el vector incrementado
     Vector unirVectores(Vector v1, Vector v2) {
 
         for (int i = 0; i < v2.size(); i++) {
             double[] arr = new double[3];
             arr = (double[]) v2.get(i);
             v1.add(arr);
         }
 
         acep = false;
         return v1;
     }
 
     Vector EliminaFinales(Vector vec, int ind) {
 
         double[] arreglo = new double[3];
 
         for (int i = 0; i < vec.size(); i++) {
             arreglo = (double[]) vec.get(i);
 
             if (k == 15) {
                 k = 0;
             }
             if (arreglo[0] == ind) {
                System.out.println("elimina... ");
 
                 vec.remove(i);
                 i--;
                 continue;
             }
 
         }
 
         return vec;
     }
 
     //Ingresa los arreglos del Vector Auxiliar
     Vector obtieneVectorFinal(Vector vectorF, int indi) {
 
         Vector outt = new Vector();
         Vector out = new Vector();
 
         System.out.println("indice: " + indi);
         for (int i = 0; i < vIniciales.size(); i++) {
             out = (Vector) vIniciales.get(i);
 
             if (indi == out.get(0)) {
                 Vector outAux = new Vector();
 
                 for (int y = 1; y < out.size(); y++) {
                     outAux.add(out.get(y));
                 }
 
                 acep = true;
                 System.out.println(acep);
 
                 return outAux;
             }
 
         }
 
         return outt;
     }
 
     //Recibe el vector con los arreglos para ser calculados
     Vector incrementoAutomatico(Vector a) {
 
         Vector outFinal = new Vector();
         double[] dato = new double[3];
 
         for (int i = 0; i < a.size(); i++) {
             dato = (double[]) a.get(i);
             dato = puntoCalculado(dato);
             outFinal.add(dato);
         }
 
         return outFinal;
     }
 
     //Retorna arreglo calculado
     double[] puntoCalculado(double[] dato) {
         DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
         simbolos.setDecimalSeparator('.');
         DecimalFormat df = new DecimalFormat("0.00", simbolos);
 
         double[] out = new double[3];
         out[0] = dato[0];
         out[1] = Double.parseDouble(df.format(dato[1] + dato[2]));
         out[2] = dato[2];
 
         return out;
     }
 
     //genera el primer vector con su respectivo indice
     Vector ordenVector(Point puntoA, Point puntoB, Point puntoC, int y) {
 
         Vector a = new Vector();
         double[] punto1 = new double[3];
         double[] punto2 = new double[3];
 
         a.add(y);
         if (puntoB.y < puntoA.y) {
             punto1[0] = puntoA.y;
             punto1[1] = puntoB.x;
             punto1[2] = pendiente(puntoA.x, puntoA.y, puntoB.x, puntoB.y);
 
 
             a.add(punto1);
         }
         if (puntoB.y < puntoC.y) {
             punto2[0] = puntoC.y;
             punto2[1] = puntoB.x;
             punto2[2] = pendiente(puntoC.x, puntoC.y, puntoB.x, puntoB.y);
 
 
             a.add(punto2);
         }
 //        System.out.println("Punto 1: en:"+y+" ["+punto1[0]+", "+punto1[1]+", "+punto1[2]+" ]" );
 //        System.out.println("Punto 2: en:"+y+" ["+punto2[0]+", "+punto2[1]+", "+punto2[2]+" ]" );
 
         return a;
     }
 
     //Obtiene la pendiente entre dos puntos
     double pendiente(int x1, int y1, int x2, int y2) {
 
         DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
         simbolos.setDecimalSeparator('.');
         DecimalFormat df = new DecimalFormat("0.00", simbolos);
 
         double pend = (double) (y2 - y1) / (x2 - x1);
         double salida;
 
         if (x2 - x1 == 0 || y2 - y1 == 0) {
 
             return 0;
         }
 
         salida = 1 / pend;
 
         return Double.parseDouble(df.format(salida));
     }
 
     //crea un arreglo del menor al mayor punto en el eje Y
     void arregloInicial(Point[] puntos) {
 
         inicialY = puntos[0].y;
         finalY = puntos[0].y;
 
         for (int i = 0; i < puntos.length; i++) {
 
             if (puntos[i].y < inicialY) {
                 inicialY = puntos[i].y;
             }
 
             if (puntos[i].y > finalY) {
                 finalY = puntos[i].y;
             }
         }
         tamaño = finalY - inicialY;
         indiceY = new int[tamaño + 1];
         indiceY[0] = inicialY;
 
         for (int i = 1; i <= tamaño; i++) {
             indiceY[i] = indiceY[i - 1] + 1;
         }
 
     }
 
     private Vector ordenarVector(Vector v3) {
 
         double[][] valorX = ordenarX(v3);
         Vector out1 = new Vector();
         for (int i = 0; i < valorX.length; i++) {
             double xAux = valorX[i][0];
             double mAux = valorX[i][1];
             for (int j = 0; j < v3.size(); j++) {
                 double[] get = (double[]) v3.get(j);
                 if (get[1] == xAux && get[2] == mAux) {
                     out1.add(v3.get(j));
                     break;
                 }
 
             }
 
         }
         return out1;
     }
 
     double[][] ordenarX(Vector vecX) {
         double[][] valorX = new double[vecX.size()][2];
         double[] out = new double[3];
 
         for (int i = 0; i < vecX.size(); i++) {
             out = (double[]) vecX.get(i);
             valorX[i][0] = out[1];
             valorX[i][1] = out[2];
         }
         valorX = burbuja(valorX);
         return valorX;
     }
 
     public double[][] burbuja(double[][] a) {
         for (int i = a.length - 1; i > 0; i--) {
             for (int j = 0; j < i; j++) {
                 if (a[j][0] > a[j + 1][0]) {
                     double temp = a[j][0];
                     a[j][0] = a[j + 1][0];
                     a[j + 1][0] = temp;
                     double temp2 = a[j][1];
                     a[j][1] = a[j + 1][1];
                     a[j + 1][1] = temp2;
 
                 }
             }
         }
 
         for (int i = 0; i < a.length; i++) {
             for (int j = 0; j < a[0].length; j++) {
                 double ds = a[i][j];
                 System.out.println(ds);
             }
 
         }
 
         return a;
     }
 
     /// Este metodo recibe el vector  de vectores con los Points resultantes 
     public ArrayList<Point> vectorTodosPuntos(Vector v) {
         ArrayList<Point> a = new ArrayList<Point>();
 
         for (int i = 0; i < v.size(); i++) {
 
             Vector getVector = (Vector) v.get(i);
             for (int w = 0; w < getVector.size(); w++) {
                 Point ab = (Point) getVector.get(w);
                 //System.out.println("todos los puntos "+ab.toString());
             }
             //System.out.println();
 
 
             for (int j = 0; j < getVector.size(); j = j + 2) {
                 Point punto1 = (Point) getVector.elementAt(j);
                 Point punto2;
                 try {
                     punto2 = (Point) getVector.elementAt(j + 1);
                 } catch (Exception e) {
                     continue;
                 }
                 ArrayList<Point> hallarPuntosEntreDosPuntos = hallarPuntosEntreDosPuntos(punto1, punto2);
                 //System.out.println("hallar punto entre "+punto1.toString()+" hasta  "+punto2.toString());
                 for (int k = 0; k < hallarPuntosEntreDosPuntos.size(); k++) {
                     a.add(hallarPuntosEntreDosPuntos.get(k));
                     //System.out.println("hallado "+hallarPuntosEntreDosPuntos.get(k).toString());
                 }
             }
 
         }
 
         return a;
     }
 
     private ArrayList<Point> hallarPuntosEntreDosPuntos(Point p1, Point p2) {
         ArrayList<Point> retorno = new ArrayList<Point>();
         if (p1.x > p2.x) {
             Point pAux = p1;
             p1 = p2;
             p2 = pAux;
         }
         for (int i = 0; i < Math.abs(p1.x - p2.x); i++) {
             //System.out.println("punto intermediooooooo "+((p1.x)+i)+","+p1.y);
             retorno.add(new Point((p1.x) + i, p1.y));
         }
 
         return retorno;
     }
 
     Vector obtenerVectorPuntos(Vector vectorP, int y) {
 
         Vector vectorOut = new Vector();
         double[] out = new double[3];
 
 
         for (int i = 0; i < vectorP.size(); i++) {
             Point punto1 = new Point();
             out = (double[]) vectorP.get(i);
             punto1.x = (int) Math.rint(out[1]);
             punto1.y = y;
 
             vectorOut.add(punto1);
             System.out.println("añadido a vector final punto.x: " + punto1.x + "," + punto1.y);
         }
         vPuntosFinales.add(vectorOut);
         return vectorOut;
     }
 
     public static void main(String[] arg) {
 
         PoligonoFinal pol = new PoligonoFinal();
         Point[] puntosA = {new Point(-4, -5), new Point(4, -3), new Point(8, 8),
             new Point(5, 5), new Point(5, 10), new Point(-3, 8), new Point(0, 0)};
         Point[] puntosB = {new Point(-4, -5), new Point(0, -3), new Point(8, -8),
            new Point(5, 8), new Point(5, 12), new Point(0, 10), new Point(-5, 8)};
         pol.algoritmoPoligono(puntosB);
 
     }
 }
