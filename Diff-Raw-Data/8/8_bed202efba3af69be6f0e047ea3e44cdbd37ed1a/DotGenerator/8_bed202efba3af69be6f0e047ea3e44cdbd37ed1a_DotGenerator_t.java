 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package de.weltraumschaf.neuron;
 
 import com.google.common.collect.Sets;
 import de.weltraumschaf.neuron.node.Node;
 import de.weltraumschaf.neuron.shell.Environment;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Simple implementation to generate unidirected graphs in <a href="http://en.wikipedia.org/wiki/DOT_language">Dot
  * Language</a> from the nodes living in an {@link Environment}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class DotGenerator {
 
     /**
      * Format for graph with its neighbor.
      */
     private static final String ONE_NODES_FORMAT = "  %d;%n";
 
     /**
      * Format for a single node w/o any neighbor.
      */
     private static final String TWO_NODES_FORMAT = "  %d -> %d;%n";
 
     /**
      * Name of graph.
      */
     private static final String NAME = "neuron";
 
     /**
      * Default file extension for dot files.
      */
     private static final String FILE_EXTENSION = ".dot";
 
     /**
      * Graph name.
      */
     private String name = NAME;;
 
     /**
      * Environment to obtain graphed nodes from.
      */
     private final Environment env;
 
     /**
      * Dedicated constructor.
      *
      * @param env used to get the graphed nodes
      */
     public DotGenerator(final Environment env) {
         super();
         this.env = env;
     }
 
     /**
      * Iterates over the nodes from {@link #env} and generates the vertexes.
      *
      * @return returns empty string if {@link #env} has no nodes
      */
     private String generateUnidirectedGraph() {
         final StringBuilder buffer = new StringBuilder();
         final List<Node> nodes = env.getNodes();
         final Set<Node> singleNodes = Sets.newTreeSet(); // sorted by id
         final Set<Node> alreadySeen = Sets.newHashSet();
 
         for (final Node n : nodes) {
             if (n.hasNeighbors()) {
                 for (final Node neighbor : n.getNeighbors()) {
                     buffer.append(String.format(TWO_NODES_FORMAT, n.getId(), neighbor.getId()));
                     alreadySeen.add(neighbor);
 
                     if (singleNodes.contains(neighbor)) {
                         singleNodes.remove(neighbor);
                     }
                 }
             } else {
                 if (alreadySeen.contains(n)) {
                     continue;
                 }
 
                 singleNodes.add(n);
             }
         }
 
         for (final Node n : singleNodes) {
             buffer.append(String.format(ONE_NODES_FORMAT, n.getId()));
         }
 
         return buffer.toString();
     }
 
     /**
      * Returns the Dot Language string.
      *
      * @return string recalculated on every invocation
      */
     @Override
     public String toString() {
         return String.format("digraph %s {%n%s}", getName(), generateUnidirectedGraph());
     }
 
     /**
      * Get graph name.
      *
      * @return string used in dot output.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Set graph name.
      *
      * @param name name to be used in dot output
      */
     public void setName(final String name) {
         this.name = name;
     }
 
     /**
      * Filename is {@link #getName()} with {@link #FILE_EXTENSION}.
      *
      * @return the file name string
      */
     public String getFileName() {
         return getName() + FILE_EXTENSION;
     }
 
 }
