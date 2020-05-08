 package com.github.a2g.core;
 
 
 import java.util.Arrays;
 import java.util.List;
 
 
 public class VerbCollection {
     private static final List<Verb> VERBS = Arrays.asList(
             new Verb("Walk", "Walk to AAA"),
             new Verb("Talk", "Talk to AAA"),
             new Verb("Examine", "Examine AAA"),
             new Verb("Grab", "Grab AAA"),
            new Verb("CUT",
            "CUT AAA|CUT AAA with BBB"),
             new Verb("Swing", "Swing AAA"),
             new Verb("Give",
             "Give AAA|Give AAA to BBB"),
             new Verb("Use",
             "Use AAA|Use AAA with BBB"),
             new Verb("Push", "Push AAA"),
             new Verb("Pull", "Pull AAA"),
             new Verb("Throw",
             "Throw AAA|Throw AAA at BBB"),
             new Verb("Eat", "Eat AAA"));
     VerbCollection() {}
 	 
     public Verb get(int i) {
         return VERBS.get(i);
     }
 }
