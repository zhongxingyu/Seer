 package process_analysis_core;
 
import analyze.impl.AnalysisInversionMethod;
import analyze.impl.AnalysisSeriesMethod;
import interfaces.IArrayGenerator;
 import least_squares.ParabolicApproximation;
import regression_generator.help.DistributionCalcHelper;
import regression_generator.impl.RegressionArrayGeneratorModel2;
 
 import java.io.File;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mark
  * Date: 15.03.13
  * Time: 19:45
  * To change this template use File | Settings | File Templates.
  */
 public class Main {
     public static void main(String[] args) {
         int processArrayLength = 5000; //N1
         int parts = 100; //  N2
         double alpha1 = 0.9;
         double alpha2 = -0.6;
         //первые два члена регрессионной модели
         double z1 = -0.2;
         double z2 = 0.5;
 
        processLab(processArrayLength, parts, alpha1, alpha2, z1, z2);
     }
 
     private static void processLab(int processArrayLength, int parts, double alpha1, double alpha2, double z1, double z2){
         /*
         Генерируем processArrayLength(N1) значений, разбиваем на parts(N2) частей
         и считаем оценки для массивов мат ожиданий и отклонения методом инверсий и серий
         */
         IArrayGenerator generator2 = new RegressionArrayGeneratorModel2(alpha1,alpha2,z1,z2){
             //Функция из модели 2
             @Override
             protected double additionalFunc(int i) {
                 return (i - Math.pow(i,2) / 2000.0)*0.001;
             }
         };
         processAnalysis(generator2,processArrayLength,parts);
     }
     //Выполняет задание 3, используя заданный генератор массива
     //колво элементов которое необходимо сгенерить (processArrayLength)
     //Разбивает на  parts частей
     private static void processAnalysis(IArrayGenerator generator,
                                         int processArrayLength,
                                         int parts){
 
         //Генерим  processArrayLength значений
         double[] experementalData = generator.generateSeries(processArrayLength);
         File f = new File("out/"+"tranded.txt");
         DistributionCalcHelper.saveToFile(experementalData, f);
          splitAndAnalyse(experementalData,parts);
         //Удаление тренда
         double[] fixed = calcFixedArray(experementalData);
         splitAndAnalyse(fixed,parts);
         File f1 = new File("out/"+"fixed.txt");
         DistributionCalcHelper.saveToFile(fixed, f1);
     }
     private static void splitAndAnalyse(double[] experementalData,int parts){
         //Разбиваем на части
         ArrayList<double[]> splitted = DistributionCalcHelper.splitByParts(experementalData,parts);
 
         //Считаем  мат ожидания и отклонения для каждой части,
         // сохраняем в массив
         double[] mxArray = new double[splitted.size()];
         double[] sigmaArray = new double[splitted.size()];
         DistributionCalcHelper.fillMxAndSigmaArrays(splitted,mxArray,sigmaArray);
 
         //Расчитываем по две оценки для каждого массива
         printEstimates("MX",mxArray);
         printEstimates("Sigma",sigmaArray);
     }
     //Печатает оценки методом серий и инверсий
     private static void printEstimates(String header,double[] array){
         AnalysisInversionMethod inversion  = new AnalysisInversionMethod();
        AnalysisSeriesMethod  series = new AnalysisSeriesMethod();
         System.out.println(header+"\n\tInversion: "
                 + inversion.calcEstimate(array)
                 + "\n\tSeries: "+series.calcEstimate(array));
     }
     //Удаляет тренд из массива
     private static double[] calcFixedArray(double[] experementalData) {
         ParabolicApproximation parabola = new ParabolicApproximation(experementalData);
         double[] fixed = new double[experementalData.length];
         for(int i=0;i<fixed.length;i++){
             fixed[i] =experementalData[i]-parabola.getApproximation(i);
         }
         return fixed;
     }
 
 }
