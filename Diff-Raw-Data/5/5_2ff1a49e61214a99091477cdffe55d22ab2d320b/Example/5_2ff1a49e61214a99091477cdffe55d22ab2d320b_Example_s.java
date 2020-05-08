 package com.techtangents.eq.examples.pairs.example4;
 
 import static com.techtangents.eq.examples.pairs.example4.Pair.pair;
 import static com.techtangents.eq.examples.pairs.example4.Person.person;
 import static java.lang.System.out;
 
 public class Example {
   public static void main(final String[] _) {
 
    pair("a", "b");
     pair(person("mary"), person("bob"));
 
    final Pair<Person, Person> p1 = ;
     final Pair<Person, Person> p2 = pair(person("mary"), person("fred"));
     final boolean eq = p1.eq(p2);
     out.println(eq);
   }
 }
