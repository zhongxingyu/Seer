 package com.tiemenschut.javacollectionhelper.client;
 
 public abstract class ModelBuilder {
     private static final String SET_WHICH_ORDERING               = "Should the items be sorted by insertion-order or value-order?";
     private static final String SET_WHICH_ORDERING_INSERTION     = "Insertion";
     private static final String SET_WHICH_ORDERING_VALUE         = "Value";
 
     private static final String MAP_WHICH_ORDERING               = "Should the items be sorted by insertion-order or key-order?";
     private static final String MAP_WHICH_ORDERING_INSERTION     = "Insertion";
     private static final String MAP_WHICH_ORDERING_KEY           = "Key";
 
     private static final String SET_NEEDS_ORDER                  = "Is the order of the values in de collection important?";
     private static final String SET_NEEDS_ORDER_YES              = "Yes";
     private static final String SET_NEEDS_ORDER_NOPE             = "Nope";
 
     private static final String MAP_NEEDS_ORDER                  = "Is the order of the key/values pairs in de collection important?";
     private static final String MAP_NEEDS_ORDER_YES              = "Yes";
     private static final String MAP_NEEDS_ORDER_NOPE             = "Nope";
 
     private static final String LIST_ARRAY_OR_LINKED             = "Which is more important, fast random access or fast insertion and removal?";
     private static final String LIST_ARRAY_OR_LINKED_ACCESS      = "Fast access";
     private static final String LIST_ARRAY_OR_LINKED_MODIFACTION = "Fast modification";
 
     private static final String LIST_OR_SET                      = "Do you want to store duplicates or will each entry be unique?";
     private static final String LIST_OR_SET_UNIQUE               = "Unique";
     private static final String LIST_OR_SET_DUPES                = "Duplicates";
 
     private static final String VALUES_OR_MAP                    = "Do you want to store values or key/value pairs?";
     private static final String VALUES_OR_MAP_VALUES             = "Values";
     private static final String VALUES_OR_MAP_MAP                = "Pairs";
 
     public static QuestionModel build() {
         FinalAnswerModel arrayList = new FinalAnswerModel(3, "ArrayList",
                 "http://docs.oracle.com/javase/7/docs/api/java/util/ArrayList.html");
         FinalAnswerModel linkedList = new FinalAnswerModel(3, "LinkedList",
                 "http://docs.oracle.com/javase/7/docs/api/java/util/LinkedList.html");
         FinalAnswerModel hashSet = new FinalAnswerModel(3, "HashSet", "http://docs.oracle.com/javase/7/docs/api/java/util/HashSet.html");
         FinalAnswerModel linkedHashSet = new FinalAnswerModel(4, "LinkedHashSet",
                 "http://docs.oracle.com/javase/7/docs/api/java/util/LinkedHashSet.html");
         FinalAnswerModel treeSet = new FinalAnswerModel(4, "TreeSet", "http://docs.oracle.com/javase/7/docs/api/java/util/TreeSet.html");
         FinalAnswerModel hashMap = new FinalAnswerModel(2, "HashMap", "http://docs.oracle.com/javase/7/docs/api/java/util/HashMap.html");
         FinalAnswerModel linkedHashMap = new FinalAnswerModel(3, "LinkedHashMap",
                 "http://docs.oracle.com/javase/7/docs/api/java/util/LinkedHashMap.html");
         FinalAnswerModel treeMap = new FinalAnswerModel(3, "TreeMap", "http://docs.oracle.com/javase/7/docs/api/java/util/TreeMap.html");
 
         QuestionModel setTreeOrLinked = new QuestionModel(SET_WHICH_ORDERING, 3, new AnswerModel(SET_WHICH_ORDERING_INSERTION,
                 linkedHashSet), new AnswerModel(SET_WHICH_ORDERING_VALUE, treeSet));
 
         QuestionModel setOrderedOrNot = new QuestionModel(SET_NEEDS_ORDER, 2, new AnswerModel(SET_NEEDS_ORDER_NOPE, hashSet),
                 new AnswerModel(SET_NEEDS_ORDER_YES, setTreeOrLinked));
         QuestionModel listArrayOrLinked = new QuestionModel(LIST_ARRAY_OR_LINKED, 2,
                 new AnswerModel(LIST_ARRAY_OR_LINKED_ACCESS, arrayList), new AnswerModel(LIST_ARRAY_OR_LINKED_MODIFACTION, linkedList));
         QuestionModel mapTreeOrLinked = new QuestionModel(MAP_WHICH_ORDERING, 2, new AnswerModel(MAP_WHICH_ORDERING_INSERTION,
                 linkedHashMap), new AnswerModel(MAP_WHICH_ORDERING_KEY, treeMap));
 
        QuestionModel listOrSet = new QuestionModel(LIST_OR_SET, 1, new AnswerModel(LIST_OR_SET_UNIQUE, listArrayOrLinked),
                new AnswerModel(LIST_OR_SET_DUPES, setOrderedOrNot));
         QuestionModel mapOrderedOrNot = new QuestionModel(MAP_NEEDS_ORDER, 1, new AnswerModel(MAP_NEEDS_ORDER_NOPE, hashMap),
                 new AnswerModel(MAP_NEEDS_ORDER_YES, mapTreeOrLinked));
 
         QuestionModel valuesOrMap = new QuestionModel(VALUES_OR_MAP, 0, new AnswerModel(VALUES_OR_MAP_VALUES, listOrSet), new AnswerModel(
                 VALUES_OR_MAP_MAP, mapOrderedOrNot));
 
         return valuesOrMap;
     }
 }
