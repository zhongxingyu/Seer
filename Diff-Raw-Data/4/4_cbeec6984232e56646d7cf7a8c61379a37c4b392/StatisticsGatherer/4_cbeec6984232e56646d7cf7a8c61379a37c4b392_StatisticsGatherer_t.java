 package com.clustrino.csv;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 
 import javax.annotation.Nullable;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class StatisticsGatherer implements LineReadListener {
     private final List<ColumnStatistics> stats;
     private final List<CSVError> errors;
     private final long limit;
     private long linesRead;
 
     public StatisticsGatherer() {
         this(-1L);
     }
 
     public StatisticsGatherer(long limit) {
         stats = new ArrayList<>();
         errors = new ArrayList<>();
         this.limit=limit;
         linesRead = 0L;
     }
 
     public List<ColumnStatistics> getStats() {
         return stats;
     }
 
     public List<CSVError> getErrors() {
         return errors;
     }
 
     @Override
     public Object lineRead(long lineNumber, String[] line, String raw, List<DataCategory> categories) {
         initStats(categories);
         List<Comparable<?>> parsedValues = parsedLine(lineNumber, line, raw, categories);
         if (parsedValues != null) {
             linesRead++;
             for (int idx = 0; idx < stats.size(); idx++) {
                 stats.get(idx).add(parsedValues.get(idx));
             }
         }
         return parsedValues;
     }
 
     private void initStats(List<DataCategory> categories) {
         if(stats.isEmpty()) {
             for (DataCategory cat : categories) {
                 stats.add(new ColumnStatistics(cat));
             }
         }
     }
 
     @Override
     public boolean finished() {
         if (limit < 0) return false;
         System.out.println("Stats" + limit + " " +linesRead);
         return linesRead >= limit;
     }
 
     private List<Comparable<?>> parsedLine(long lineNumber, String[] line, String raw, List<DataCategory> categories){
         if (line == null || categories.size() != line.length) {
             System.out.println("Error "+ categories +" " +line);
            errors.clear();
             errors.add(new CSVError(lineNumber, raw));
             return null;
         } else {
             try {
                 List<Comparable<?>> parsedValues = new ArrayList<>(categories.size());
 
                 for (int idx = 0; idx < stats.size(); idx++) {
                     String stringValue = line[idx];
                     Comparable<?> parsedValue = categories.get(idx).parsedValue(stringValue);
                     parsedValues.add(parsedValue);
                 }
                 return parsedValues;
             } catch (Exception e) {
                 System.out.println(e);
                 System.out.println(e.getStackTrace());
                errors.clear();
                 errors.add(new CSVError(lineNumber, raw));
                 return null;
             }
         }
 
     }
 
     public List<Float> getPercentagePopulated() {
         return Lists.transform(getStats(), new Function<ColumnStatistics, Float>() {
             @Nullable
             @Override
             public Float apply(@Nullable ColumnStatistics columnStatistics) {
                 return columnStatistics.getPopulatedPercentage();
             }
         });
 
     }
 }
