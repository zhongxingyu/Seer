 package se;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.io.IOException;
 import java.io.PrintStream;
 
 public class V00 {
     public static final ObjectMapper MAPPER = new ObjectMapper();
     static {
         //  конфігуруємо серіалізатор: вмикаємо вирівнювання
         MAPPER.getSerializationConfig().set(
                 SerializationConfig.Feature.INDENT_OUTPUT,
                 true
         );
     }
     /**
      * Деякий об’єкт, який можна зважувати (абстрактний інтерфейс)
      */
     public static interface Weighted {
         /**
          * @return значення атрибуту "вага"
          */
         double getWeight();
 
         /**
          * @return обчислюване значення ваги з урахуванням вкладених об’єктів
          */
         double computeWeight();
     }
 
     /**
      * Базова реалізація інтерфейсу
      */
     public static class WeightedImpl implements Weighted {
         double weight;
         Weighted[] nested;
 
         /**
          * Створення екземляра класу без вкладених об’єктів
          *
          * @param weight вага об’єкту
          */
         public WeightedImpl(double weight) {
             this(weight, new WeightedImpl[0]);
         }
 
         /**
          * Створення екземляра класу зі вкладеними об’єктами
          *
          * @param weight вага об’єкту
          * @param nested масив вкладених об’єктів
          */
         public WeightedImpl(double weight, Weighted... nested) {
             this.weight = weight;
             this.nested = nested;
         }
 
         /**
          * Базова реалізація метода інтерфейсу
          *
          * @return значення атрибуту "вага"
          */
         public double getWeight() {
             return weight;
         }
         
         public Weighted[] getNested() {
             return nested;
         }
 
         /**
          * Базова реалізація метода інтерфейсу
          *
          * @return обчислюване значення ваги з урахуванням вкладених об’єктів
          */
         public double computeWeight() {
             double total = getWeight();
 
             for (Weighted aWeighted : nested) {
                 total += aWeighted.computeWeight();
             }
 
             return total;
         }
     }
 
     /**
      * Реалізація, що відслідковує кількість викликів методу computeCount()
      */
     public static class WeightedAudit implements Weighted {
         protected Weighted audited;
         protected int computeCallCount;
 
         /**
          * Створення екземляра
          *
          * @param audited інший екземпляр, якому делегуються виклики інтерфейсу
          *               Weighted
          */
         public WeightedAudit(Weighted audited) {
             this.audited = audited;
         }
         
         public Weighted getAudited() {
             return audited;
         }
 
         public double computeWeight() {
             computeCallCount++;
             return audited.computeWeight();
         }
 
         public double getWeight() {
             return audited.getWeight();
         }
 
         /**
          * @return кількість викликів @{link WeightedAudit#computeWeight()}
          */
         public int getComputeCallCount() {
             return computeCallCount;
         }
     }
 
     public static void main(String[] args) throws IOException {
         WeightedAudit audit = null;
         Weighted wTree = null;
 
         //  якщо програма виконується без аргументів, створюємо
         //      дерево об’єктів безпосередньо у коді
         if (args.length == 0 || args[0].length() == 0) {
             //  створення дерева вкладених об’єктів, зовнішній
             //  має вагу 1 та три вкладені об’єкти
             wTree = new WeightedImpl(
                     1,
                     //  один простий об’єкт
                     new WeightedImpl(2),
                     //  обгортаємо один з об’єктів у реалізацію об’єкта аудитора
                     audit = new WeightedAudit(new WeightedImpl(2)),
                     //  об’єкт котрий має ще один вкладений
                     new WeightedImpl(3, new WeightedImpl(4))
             );
         } else {    //  інакше спробуємо знайти відповідний контекст
             ClassPathXmlApplicationContext cpCtx =
                     //  використовуємо парсер контекстів бібліотеки Spring
                     new ClassPathXmlApplicationContext(
                             //  генеруємо повний шлях до ресурсу
                             //    що описує контекст з компонентами
                            "se/var00-" + args[0] + ".xml"
                     );
 
             //  компонента з id=root є кореневим елементом дерева об’єктів
             wTree = (Weighted) cpCtx.getBean(
                     "root",
                     Weighted.class
             );
             //  компонента з id=audit є деяким об’єктом, що виконує аудит
             audit = (WeightedAudit) cpCtx.getBean(
                     "audit",
                     WeightedAudit.class
             );
         }
 
         final PrintStream sout = System.out;
 
         ObjectMapper om = new ObjectMapper();
         om.getSerializationConfig().set(
                 SerializationConfig.Feature.INDENT_OUTPUT,
                 true
         );
         System.out.println(
                 "JSON: " +
                         om.writeValueAsString(
                                 wTree
                         )
         );
 
         //  --> 12
         sout.println("total weight: " + wTree.computeWeight());
         //  --> 1
         sout.println("audit object calls: " + audit.getComputeCallCount());
 
         //  --> 12
         sout.println("total weight: " + wTree.computeWeight());
         //  --> 2
         sout.println("audit object calls: " + audit.getComputeCallCount());
 
         //  --> 2
         sout.println("audit's weight: " + audit.computeWeight());
         //  --> 3
         sout.println("audit object calls: " + audit.getComputeCallCount());
     }
}
