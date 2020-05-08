 package se;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 public class V01 {
     /** Деякий об’єкт, який можна зважувати */
     public static class Weighted {
         double weight;
         Weighted[] nested;
 
         /** Створення екземляра класу без вкладених об’єктів
          * @param weight вага об’єкту */
         public Weighted(double weight) {
             this(weight, new Weighted[0]);
         }
 
         /** Створення екземляра класу зі вкладеними об’єктами
          * @param weight вага об’єкту
          * @param nested масив вкладених об’єктів */
         public Weighted(double weight, Weighted... nested) {
             this.weight = weight;
             this.nested = nested;
         }
 
 
         /** @return вага об’єкту */
         public double getWeight() {
             return weight;
         }
 
         /** @return масив вкладених об’єктів */
         public Weighted[] getNested() {
             return nested;
         }
     }
 
     /** Цей класс реалізує алгоритм рекурсивного обходу дерева об’єктів */
     public static class WeightedNavigator {
         /**
          * current path in the weighted tree
          */
         Deque<Weighted> treePath = new ArrayDeque<Weighted>();
         /**
          * current positions in the weighted tree nested arrays
          */
         Deque<Integer> positions = new ArrayDeque<Integer>();
 
 
         /** Створення класу, який спочатку поверне вершину дерева об’єктів
         * @param from деякий об’єкт, що може вмішувати інші */
         public WeightedNavigator(Weighted from) {
             treePath.addLast(from);
             positions.addLast(-1);
         }
 
         /** @return true, якщо обхід дерева закінчено */
         public boolean hasNext() {
             return !treePath.isEmpty();
         }
 
         /**
          * Переводить алгоритм на наступний об’єкт
          * @return поточний об’єкт при обході */
         public Weighted next() {
             final Weighted nextToReturn;
 
             if (positions.peekLast() == -1) {
                 nextToReturn = treePath.peekLast();
             } else {
                 nextToReturn = treePath.peekLast().getNested()[positions.peekLast()];
             }
 
             //	retreat fully processed elements
             while (
                     !treePath.isEmpty() &&
                     positions.peekLast() + 1 == treePath.peekLast().getNested().length
             ) {
                 positions.removeLast();
                 treePath.removeLast();
             }
 
             if (!treePath.isEmpty()) {
                 final int newPos = positions.removeLast() + 1;
                 positions.addLast(newPos);
                 treePath.addLast(treePath.peekLast().getNested()[newPos]);
                 positions.addLast(-1);
             }
 
             return nextToReturn;
         }
     }
 
     public static void main(String[] args) {
         //  створюємо деяке деревце об’єктів
         final Weighted wTree = new Weighted(
                 1,
                 new Weighted(2),
                 new Weighted(3,
                         new Weighted(4)
                 )
         );
 
         //  за допомогою "навігатора" обчислюємо вагу
         //      деревця та кількість об’єктів
         double totalWeight = 0;
         int count = 0;
         WeightedNavigator nav = new WeightedNavigator(wTree);
         while (nav.hasNext()) {
             count++;
             totalWeight += nav.next().getWeight();
         }
 
         System.out.println("total weight: " + totalWeight);
         System.out.println("count of elements: " + count);
     }
}
