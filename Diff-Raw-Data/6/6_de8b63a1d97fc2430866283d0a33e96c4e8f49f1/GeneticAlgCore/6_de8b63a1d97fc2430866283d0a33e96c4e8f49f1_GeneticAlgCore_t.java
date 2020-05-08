 package ru.study.core;
 
 import ru.study.utils.MathExpressionParser;
 import ru.study.utils.Pair;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: markdev
  * Date: 10/7/13
  * Time: 9:53 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GeneticAlgCore {
     private MathExpressionParser MEP;
 
     public GeneticAlgCore(MathExpressionParser MEP) {
         this.MEP = MEP;
     }
 
     public ArrayList<Person> initialPersons(int from, int to) {
         int bitSize = Integer.toBinaryString(to).length() - 1;
        int generationSize = (int) Math.sqrt(to - from);   //размер одного поколения
        if (generationSize < 2) {
            generationSize = 2;
        }
        System.out.println("generationSize = "+generationSize);
         //Берем рандомные generationSize особи
         ArrayList<Person> randomp = new ArrayList<Person>();
         int rouletteSize = 0;  //подсчитываем общий размер рулетки
         for (int i = 0; i < generationSize; i++) {
             Person p = new Person(randomNum(from, to), bitSize, MEP);
             rouletteSize = rouletteSize + p.getAdaptation();
             randomp.add(p);
         }
         //формируем список интервалов
         double[][] intervals = new double[generationSize][2];
         double prevInterval = 0;
         for (int i = 0; i < generationSize; i++) {
             intervals[i][0] = prevInterval;
             double newInterval = prevInterval + (double) (randomp.get(i).getAdaptation()) / (double) rouletteSize;
             intervals[i][1] = newInterval;
             prevInterval = newInterval;
         }
         //Отбираем особей которые попадут в начальный пул - метод рулетки
         ArrayList<Person> initialPool = new ArrayList<Person>();
         for (int i = 0; i < generationSize; i++) {
             initialPool.add(randomp.get(intervalId(intervals)));
         }
         return initialPool;
     }
 
     private int randomNum(int from, int to) {
         Random r = new Random();
         return r.nextInt(to) + from;
     }
 
     public int intervalId(double[][] intervals) {
         double r = Math.random();
         for (int i = 0; i < intervals.length; i++) {
             double from = intervals[i][0];
             double to = intervals[i][1];
             if (r >= from && r <= to) {
                 return i;
             }
         }
         return intervals.length - 1;
     }
 
     //HashSet не допускает дублирования, так что если его длина = 1 то
     //значит что все элементы списка - одинаковые - а значит можно заканчивать
     public boolean isBestPersonFound(ArrayList<Person> personsPool) {
         return new HashSet<Person>(personsPool).size() == 1;
     }
 
     //Попарно скрещивает
     public ArrayList<Person> crossing(ArrayList<Person> personsPool) {
         ArrayList<Person> crossed = new ArrayList<Person>();
         for (int i = 1; i < personsPool.size(); i++) {
             Person p1 = personsPool.get(i - 1);
             Person p2 = personsPool.get(i);
             Pair<Person, Person> crossing = Person.crossing(new Pair<Person, Person>(p1, p2));
             crossed.add(crossing.getFirst());
             crossed.add(crossing.getSecond());
         }
         personsPool.addAll(crossed);
         return personsPool;
     }
 
     public ArrayList<Person> getBestPersons(ArrayList<Person> personsPool) {
         //Сортируем и берем первую половину
         Collections.sort(personsPool, new Comparator<Person>() {
             @Override
             public int compare(Person o1, Person o2) {
                 if (o1.getAdaptation() < o2.getAdaptation()) {
                     return 1;
                 } else if (o1.getAdaptation() == o2.getAdaptation()) {
                     return 0;
                 } else if (o1.getAdaptation() > o2.getAdaptation()) {
                     return -1;
                 }
                 return 0;
             }
         });
         return new ArrayList<Person>(personsPool.subList(0, personsPool.size() / 2));
     }
 }
