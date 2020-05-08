 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package modelo;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  *
  * @author Ruben
  */
 public class Poblacion {
 
    public static final int MAX_POBLACION = 104;
     private ArrayList<Individuo> poblado = new ArrayList(MAX_POBLACION);
     private Random random = new Random();
 
     public ArrayList<Individuo> getPoblado() {
         return poblado;
     }
 
     public void setPoblado(ArrayList<Individuo> poblado) {
         this.poblado = poblado;
     }
 
     public Individuo getIndividuo(int posicion) {
         return poblado.get(posicion);
     }
 
     protected void crearIndividuo(byte c200, byte c100, byte c50, byte c25, byte c10, byte c5) {
         Individuo individuo = new Individuo(c200, c100, c50, c25, c10, c5);
         this.poblado.add(individuo);
     }
 
     protected void crearIndividuo(Individuo individuo) {
         this.poblado.add(individuo);
     }
 
     public void ordenarPobladoPorAptitud() {
 
         ArrayList<Individuo> auxiliar = new ArrayList(MAX_POBLACION);
         ArrayList<Individuo> borrador = poblado;
         float maxF;
         Individuo elegido;
 
         for (int i = 0; i < MAX_POBLACION; i++) {
             elegido = null;
             maxF = -20000000;
             for (Individuo individuo : borrador) {
 
                 if (individuo.getAptitud() >= maxF) {
                     maxF = individuo.getAptitud();
                     elegido = individuo;
                 }
             }
             auxiliar.add(elegido);
             borrador.remove(elegido);
         }
         poblado = auxiliar;
     }
 
     public Poblacion genPoblacionInicial(long semilla) {
         Poblacion nuevaPoblacion = new Poblacion();
         random.setSeed(semilla);
 
         for (int i = 0; i < Poblacion.MAX_POBLACION; i++) {
             nuevaPoblacion.crearIndividuo((byte) random.nextInt(15), (byte) random.nextInt(15), (byte) random.nextInt(15), (byte) random.nextInt(15),
                     (byte) random.nextInt(15), (byte) random.nextInt(15));
         }
 
         return nuevaPoblacion;
     }
 
     public float evaluarAptitud(int cambioIngresada) {
 
         float aptitudPoblacion = 0;
 
         for (int i = 0; i < Poblacion.MAX_POBLACION; i++) {
             aptitudPoblacion = +poblado.get(i).evaluarAptitud(cambioIngresada);
         }
 
         //calculo el promedio de al aptitud de la poblacion
         aptitudPoblacion = aptitudPoblacion / Poblacion.MAX_POBLACION;
 
         return aptitudPoblacion;
     }
 
     public Poblacion seleccionarSgteGeneracion() {
 
         this.ordenarPobladoPorAptitud();
 
         Poblacion nueva = new Poblacion();
 
         for (int i = 0; i < (Poblacion.MAX_POBLACION / 2); i++) {
             nueva.crearIndividuo(this.getIndividuo(i));
         }
 
         return nueva;
     }
 
     public void cruzarPoblacion() {
 
        for (int i = 0; i < Poblacion.MAX_POBLACION/2; i = i +2) {
             this.crearIndividuo(this.getIndividuo(i).cruzarse(this.getIndividuo(i + 1)));
             this.crearIndividuo(this.getIndividuo(i + 1).cruzarse(this.getIndividuo(i)));
         }
     }
 
     public void mutarPoblacion(long semilla) {
         random.setSeed(semilla);
 
         for (Individuo individuo : poblado) {
             if (random.nextGaussian() < 0.10) {
                 individuo.mutar();
             }
         }
     }
 
     public Individuo getIndMayorApt() {
         ordenarPobladoPorAptitud();
         return getIndividuo(0);
     }
 }
