 /*
  *   BellmanFord.java
  *
  *   Copyright 2012 Vitor Morato Almeida.
  *
  *   This file is part of Bellman-Ford.
  *
  *   Bellman-Ford is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   MySeries is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with Bellman-Ford.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package br.edu.ufcg.atal.miniteste06;
 
 import java.util.List;
 import java.util.LinkedList;
 
 import br.edu.ufcg.atal.miniteste06.exemplos.Exemplo01;
 import br.edu.ufcg.atal.miniteste06.exemplos.Exemplo02;
 
 public class BellmanFord {
     
     public static void main(String[] args) {
         List<Exemplo> exemplos = new LinkedList<Exemplo>();
         exemplos.add(new Exemplo01());
         exemplos.add(new Exemplo02());
         for (Exemplo exemplo : exemplos) {
             System.out.println(BellmanFord(exemplo.grafo(), exemplo.origem(), exemplo.destino()));
         }
     }
 
     public static String BellmanFord(Grafo grafo, No origem, No destino) {
         inicializarGrafo(grafo, origem);
 
        for(Aresta a : grafo.E){
            relaxar(a);
         }
 
         if(!temCicloNegativo(grafo)){
             return caminho(origem, destino);
         } else {
             return "O grafo contém ciclo negativo!";
         }
     }
 
     private static void inicializarGrafo(Grafo grafo, No origem){
         for(No vertice : grafo.V){
             if(vertice.equals(origem)){
                 vertice.setDistancia(0);
             } else {
                 vertice.setDistancia(Double.POSITIVE_INFINITY);
             }
             vertice.setPredecessor(null);
         }
     }
 
     private static void relaxar(Aresta aresta){
         No origem = aresta.getOrigem();
         No destino = aresta.getDestino();
         double custoAresta = getCusto(aresta);
         if(destino.getDistancia() > (origem.getDistancia() + custoAresta)){
             destino.setDistancia(origem.getDistancia() + custoAresta);
             destino.setPredecessor(origem);
         }
     }
 
     private static double getCusto(Aresta aresta) {
         double custoAresta = (aresta.getDelay()/aresta.getBandwidth());
         return custoAresta;
     }
     private static boolean temCicloNegativo(Grafo grafo) {
         for(Aresta a : grafo.E){
             No origem = a.getOrigem();
             No destino = a.getDestino();
             if(origem.getDistancia() + getCusto(a) < destino.getDistancia()){
                 return true;
             }
         }
         return false;
     }
 
     private static String caminho(No origem, No destino) {
         String caminho = "";
         String separador =  "->";
         No atual = destino;
         while(atual.getPredecessor() != null) {
             caminho = atual.getNome() + separador + caminho;
             atual = atual.getPredecessor();
         }
         caminho = origem.getNome() + separador + caminho.substring(0, caminho.length() - separador.length());
         return "Caminho: " + caminho +";" + " Custo: " + destino.getDistancia();
     }
 
 }
