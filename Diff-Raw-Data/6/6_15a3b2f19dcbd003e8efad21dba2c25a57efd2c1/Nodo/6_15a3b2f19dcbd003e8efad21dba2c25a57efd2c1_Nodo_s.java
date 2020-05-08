 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package practica2fia;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author victor
  */
 public class Nodo implements Comparable {
 
     // selector de la heurística, 0 = Trivial, 1 = Manhattan, 2 = Euclídea^2 , 3 = Euclídea aproximada 
     static final int heuristica = 0;
     long f;
     int g;
     long h;
     int x, y;
     int tamano, destino; // variables auxiliares para la función esMeta
     Nodo padre;
     ArrayList<Nodo> hijos;
 
     /**
      * Constructor de Nodo
      * @param _x fila en el mapa
      * @param _y columna en el mapa
      * @param _padre nodo que lo ha creado
      * @param _tamano tamaño del mapa. Variable auxiliar
      * @param _destino fila en la que se encuentra la columna de destino. Variable auxiliar
      */
     public Nodo(int _x, int _y, Nodo _padre, int _tamano, int _destino) {
         padre = _padre;
         tamano = _tamano;
         destino = _destino;
         x = _x;
         y = _y;
         if (padre == null) {
             g = 0;
         } else {
             g = padre.g + 1;
         }
 
         if (heuristica == 0) { // Heurística trivial
             h = 0;
         }else if(heuristica == 1){ // Distancia Manhattan
             h = Math.abs(destino - x) + Math.abs(tamano - 1 - y);
         } else if (heuristica == 2) {// Distancia euclídea al cuadrado
             h = (long) (Math.pow(destino - x, 2) + Math.pow(tamano - 1 - y, 2));
         } else { // Distancia euclídea aproximada
             int distX = Math.abs(destino - x);
             int distY = Math.abs(tamano - 1 - y);
             if (distX > distY) {
                 h = 14 * distY + 10 * (distX - distY);
             } else {
                 h = 14 * distX + 10 * (distY - distX);
             }
         }
         actualizarF();
 
         hijos = new ArrayList<Nodo>();
     }
 
     /**
      * Comprueba si la casilla es la casilla destino.
      * @return booleano que dice si es la casilla destino.
      */
     public boolean esMeta() {
         return x == destino && y == tamano - 1;
     }
 
     /**
      * crea los hijos del nodo y los añade a su lista de hijos. El orden es:
      * Derecha, Arriba, Abajo, Izquierda
      * @param mundo mapa
      */
     public void generarHijos(int[][] mundo) {
         if (y < tamano) {
             Nodo aux = new Nodo(x, y + 1, this, tamano, destino);
            if (mundo[x][y + 1] == 0 || aux.esMeta()) { // Derecha
                 hijos.add(aux);
             }
         }
         if (x < tamano) {
             if (mundo[x + 1][y] == 0) { // Arriba
                 hijos.add(new Nodo(x + 1, y, this, tamano, destino));
             }
         }
         if (x > 0) {
             if (mundo[x - 1][y] == 0) { // Abajo
                 hijos.add(new Nodo(x - 1, y, this, tamano, destino));
             }
         }
         if (y > 0) {
             if (mundo[x][y - 1] == 0) { // Izquierda
                 hijos.add(new Nodo(x, y - 1, this, tamano, destino));
             }
         }
 
     }
 
     /**
      * Actualiza G y recalcula F
      * @param nuevoG
      */
     public void actualizarG(int nuevoG) {
         g = nuevoG;
         actualizarF();
     }
 
     /**
      * Actualiza H y recalcula F
      * @param nuevoH
      */
     public void actualizarH(int nuevoH) {
         h = nuevoH;
         actualizarF();
     }
 
     private void actualizarF() {
             f = g + h;
     }
 
     /**
      * Comparador con x e y
      * @param n Nodo con el que se compara
      * @return
      */
     public boolean equals(Nodo n) {
         return x == n.x && y == n.y;
     }
 
     /**
      * Comparador con x e y
      * @param n Nodo con el que se compara
      * @return
      */
     @Override
     public boolean equals(Object n) {
         Nodo aux = (Nodo) n;
         return x == aux.x && y == aux.y;
     }
 
     /**
      * [Autogenerado]
      * @return
      */
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 17 * hash + this.x;
         hash = 17 * hash + this.y;
         return hash;
     }
 
     /**
      * Nodo -> (x,y)
      * @return
      */
     @Override
     public String toString() {
         return "(" + x + ", " + y + ")";
     }
 
     /**
      * Compara según la f y, en caso de igualdad, da prioridad al que más y tenga.
      * @param o Nodo con el que comparar
      * @return
      */
     public int compareTo(Object o) {
         Nodo n = (Nodo) o;
         if (this.f < n.f) {
             return -2;
         } else if (this.f == n.f) {
             if (this.y > n.y) {
                 return -1;
             } else if (this.y < n.y) {
                 return 1;
             } else {
                 return 0;
             }
 
         } else {
             return 2;
         }
     }
 }
