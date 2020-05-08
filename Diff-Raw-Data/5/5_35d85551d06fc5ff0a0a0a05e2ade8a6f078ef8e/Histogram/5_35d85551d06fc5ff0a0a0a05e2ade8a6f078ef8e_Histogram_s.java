 package histogram;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 
 public class Histogram {
     private Integer[] bins;
     private Float h;
     private Float min, max;
     private Integer count;
     private String name = "";
     
     public Histogram(String name, Integer n, Float min, Float max) throws HistogramException {
         if (n <= 0)
             throw new InvalidCreationArgumentsHistogramException();
         if (min > max)
             throw new InvalidCreationArgumentsHistogramException();
         this.min = min;
         this.max = max;
         this.count = n;
         this.bins = new Integer[this.count];
         Arrays.fill(this.bins, 0);
         this.h = 1.0f/n;
         this.name = name;
     }
     
     public Histogram(String filename, Integer n, Float min, Float max, Integer[] bins) throws HistogramException {
         if (n <= 0)
             throw new InvalidCreationArgumentsHistogramException();
         if (min > max)
             throw new InvalidCreationArgumentsHistogramException();
         if (bins.length != n)
             throw new InvalidCreationArgumentsHistogramException();
         this.min = min;
         this.max = max;
         this.count = n;
         this.h = 1.0f/n;
         this.bins = Arrays.copyOf(bins, this.count);
         this.name = name;
     }
 
     public void addPoint(Float p) {
         if (p < this.min || p > this.max) return;
         if (p == this.max) {
             this.bins[count-1]++;
             return;
         }
         Float temp = (p-this.min)/(this.max-this.min);
         for (int i = 0; i < this.count; i++)
             if (temp >= this.h*i && temp < this.h*(i+1)) {
                 this.bins[i]++;
                 return;
             }
     }
     
     public void addAll(Collection<Float> coll) {
         for (Float f: coll)
             this.addPoint(f);
     }
     
     public void printHistogram() {
         String output = "";
         output += "Number of bins: "+this.count+"\n";
         output += "Min/max: "+this.min+"/"+this.max+"\n";
         output += "==================================\n";
         int i = 0;
         for (Integer b: bins) 
             output += "Bin #"+(i++)+": "+b+"\n";
         System.out.print(output);
     }
     
     public void saveToFile(String filename) throws IOException {
         try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
             out.println(this.count);
             out.println(this.min);
             out.println(this.max);
             for (Integer b: bins)
                 out.println(b);
         }
     }
 
     public void saveToFile() throws IOException {
         try (PrintWriter out = new PrintWriter(new FileWriter(this.name+".hist"))) {
             out.println(this.count);
             out.println(this.min);
             out.println(this.max);
             for (Integer b: bins)
                 out.println(b);
         }
     }
 
     static public Histogram readFromFile(String filename) throws IOException, HistogramException {
         Integer c;
         Float m;
         Float x;
         Integer[] bins;
         try (BufferedReader input = new BufferedReader(new FileReader(filename))) {
             c = Integer.parseInt(input.readLine());
             m = Float.parseFloat(input.readLine());
             x = Float.parseFloat(input.readLine());
             if (c <= 0) throw new InvalidFileSyntaxHistogramException();
             if (m > x) throw new InvalidFileSyntaxHistogramException();
             bins = new Integer[c];
             String nextBin;
             int i = 0;
             while ((nextBin = input.readLine()) != null) {
                 bins[i] = Integer.parseInt(nextBin);
                 if (bins[i++] < 0) throw new InvalidFileSyntaxHistogramException();
             }
            if (i != c - 1) throw new InvalidFileSyntaxHistogramException();
         }
         String[] tokens = filename.split(".");
         String temp = "";
         for (int i = 0; i < tokens.length; i++) temp += tokens[i];
         return new Histogram(temp, c, m, x, bins);
     }
 
     static public Histogram createFromCollection(String name, Integer n, Collection<Float> coll) throws HistogramException {
         if (n < 1) throw new InvalidCreationArgumentsHistogramException();
         if (coll == null) throw new NullPointerException();
         if (coll.isEmpty()) throw new InvalidCreationArgumentsHistogramException();
         Histogram result;
         result = new Histogram(name, n, Collections.min(coll), Collections.max(coll));
         result.addAll(coll);
         return result;
     }
 
     public Float getDistanceFromHistogram(final Histogram target) {
        if (this.count != target.count)
             throw new IllegalArgumentException("Histograms must have equal count of bins");
         ArrayList<Float> left = new ArrayList<>();
         ArrayList<Float> right = new ArrayList<>();
         Float sum = 0.0f;
         for (Integer i: this.bins) sum += this.h*new Float(i);
         for (Integer i: this.bins) left.add(new Float(i)/sum);
         sum = 0.0f;
         for (Integer i: target.bins) sum += target.h*new Float(i);
         for (Integer i: target.bins) right.add(new Float(i)/sum);
         Float result = 0.0f;
         for (int i = 0; i < this.count; i++)
             result += Math.abs(this.h*left.get(i)-target.h*right.get(i));
         return result;
     }
 }
