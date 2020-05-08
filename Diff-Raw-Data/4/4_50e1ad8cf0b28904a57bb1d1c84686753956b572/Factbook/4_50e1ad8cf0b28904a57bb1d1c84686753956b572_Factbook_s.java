 package org.lenition.domain;
 
 import java.util.List;
 
 public class Factbook {
     List<Country> countries;
 }
 
 class Country {
     String name;
     String id;
     String history;
     String stuff;
     Area area;
     DatedValue gdp;
     DatedValue gdpPerCapita;
     DatedValue gini;
     DatedValue population;
     DatedValue populationGrowthRate;
     DatedValue deathRate;
     DatedValue healthExpenditure;
 }
 
 class Area {
     long value;
     long rank;
     String comparison;
 }
 
 class DatedValue {
    long value;
     long rank;
     String dateText;
 }
 
