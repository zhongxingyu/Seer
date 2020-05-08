 package com.hceris.graphs;
 
 import java.util.LinkedList;
 
 public class Graphs {
 
     enum State {
         White,
         Grey,
         Black
     }
     
     private Graphs() {}
 
     public static int[] bfs(Graph g, int source) {
         int[] d = new int[g.vertices()];
         int[] p = new int[g.vertices()];
         State[] state = new State[g.vertices()];
 
         for(int i = 0; i < g.vertices(); i++) {
             d[i] = Integer.MAX_VALUE;
             p[i] = -1;
             state[i] = State.White;
         }
 
         Node s = g.get(source);
         d[s.index()] = 0;
 
         LinkedList<Node> queue = new LinkedList<Node>();
         queue.addLast(s);
 
         while(!queue.isEmpty()) {
         	Node u = queue.removeFirst();
 
            for(Edge e : u) {
                 Node v = e.destination();
                 if(state[v.index()] == State.White) {
                     state[v.index()] = State.Grey;
                     d[v.index()] = d[u.index()] + 1;
                     p[v.index()] = u.index();
                     queue.addLast(v);
                 }
             }
 
             state[u.index()] = State.Black;
         }
         return d;
     }
     
     public static int[] dfs(Graph g, int source) {
         int[] d = new int[g.vertices()];
         int[] p = new int[g.vertices()];
         State[] state = new State[g.vertices()];
 
         for(int i = 0; i < g.vertices(); i++) {
             d[i] = Integer.MAX_VALUE;
             p[i] = -1;
             state[i] = State.White;
         }
 
         Node s = g.get(source);
         d[s.index()] = 0;
 
         LinkedList<Node> stack = new LinkedList<Node>();
         stack.addLast(s);
 
         while(!stack.isEmpty()) {
             Node u = stack.removeLast();
 
             for(Edge e : u) {
                 Node v = e.destination();
                 if(state[v.index()] == State.White) {
                     state[v.index()] = State.Grey;
                     d[v.index()] = d[u.index()] + 1;
                     p[v.index()] = u.index();
                     stack.addLast(v);
                 }
             }
 
             state[u.index()] = State.Black;
         }
 
         return d;
     }
 }
