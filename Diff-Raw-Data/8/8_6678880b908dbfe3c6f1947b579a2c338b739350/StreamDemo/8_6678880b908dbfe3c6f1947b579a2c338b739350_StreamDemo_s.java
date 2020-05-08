 package se.altran.java.lambda.streamdemo;
 
 import java.util.List;
 import java.util.stream.Collectors;
 import java.util.stream.Stream;
 
 public class StreamDemo {
     public static void main (String[] args) {
         int max = 24;
         System.out.format("Checking numbers [1..%d]...%n", max);
         long start = System.nanoTime();
         System.out.format("Odd numbers: %s%n", naturalNumbersUpTo(max).stream().filter(StreamDemo::heavyIsOdd).collect(Collectors.<Integer>toList()));
         long end = System.nanoTime();
        System.out.format("Time taken: %f%n", (end - start) /1.0e9);
         start = System.nanoTime();
         System.out.format("Odd numbers: %s%n", naturalNumbersUpTo(max).parallelStream().filter(StreamDemo::heavyIsOdd).collect(Collectors.<Integer>toList()));
         end = System.nanoTime();
         System.out.format("Time taken: %f%n", (end - start) /1.0e9);
     }
 
     public static List<Integer> naturalNumbersUpTo(int max) {
         return Stream.iterate(1, num -> num + 1).limit(max).collect(Collectors.<Integer>toList());
 
     }
 
     public static boolean heavyIsOdd(Integer number) {
         try {
             Thread.sleep(1000);
         } catch (InterruptedException e) {
         }
         return number % 2 == 1;
     }
 
 }
