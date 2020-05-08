 package analysis;
 
 import faceclicker.SalientPoint;
 import faceclicker.SalientPointCollection;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.Scanner;
 
 /*
  0 left eyebrow outer
  1 left eyebrow inner
  2 right eyebrow inner
  3 right eyebrow outer
  4 left eye outer
  5 left eye center
  6 left eye inner
  7 right eye inner
  8 right eye center
  9 right eye outer
  10 left nostril
  11 right nostril
  12 left ear
  13 right ear
  14 mouth left
  15 mouth right
  16 mouth top
  17 mouth bottom
  18 chin left
  19 chin right
  20 chin center
  21 forehead top    
  */
 public class Calculator {
 
     //Gunes #
     double topToChin;       //1
     double eyesToChin;      //2
     double topToNostrils;    //3
     double topToPupils;     //4
     double nostrilsToChin;  //5
     double eyesToLips;      //6
     double lipsToChin;      //7/14
     double eyesToNostrils;  //8
     double nostrilsToLips;  //9
     double topToEyebrows;   //10
     double eyebrowsToNose;  //11
     double noseToChin;      //12
     double noseToLips;      //13
     double earToEar;
 
     public Calculator(String filePath) throws FileNotFoundException {
         SalientPointCollection s = loadPoints(filePath);
         performCalculations(s);
     }
 
     private void performCalculations(SalientPointCollection s) {
 
         SalientPoint fore = s.get(21);
         SalientPoint eyebrowL = s.get(1);
         SalientPoint eyebrowR = s.get(2);
         SalientPoint eyeL = s.get(6);
         SalientPoint eyeR = s.get(7);
         SalientPoint noseL = s.get(10);
         SalientPoint noseR = s.get(11);
         SalientPoint mouthL = s.get(14);
         SalientPoint mouthR = s.get(15);
         SalientPoint mouthT = s.get(16);
         SalientPoint mouthB = s.get(17);
         SalientPoint earL = s.get(12);
         SalientPoint earR = s.get(13);
         SalientPoint chin = s.get(20);
 
         SalientPoint eyeMid = new SalientPoint("eyeMid",
                 ((eyeL.getX() + eyeR.getX()) / 2),
                 ((eyeL.getY() + eyeR.getY()) / 2));
 
         SalientPoint browMid = new SalientPoint("eyebrowMid",
                 ((eyebrowL.getX() + eyebrowR.getX()) / 2),
                 ((eyebrowL.getY() + eyebrowR.getY()) / 2));
 
         SalientPoint noseMid = new SalientPoint("noseMid",
                 ((noseL.getX() + noseR.getX()) / 2),
                 ((noseL.getY() + noseR.getY()) / 2));
 
         SalientPoint lipsMid = new SalientPoint("lipsMid",
                ((mouthT.getX() + mouthB.getX()) / 2),
                ((mouthB.getY() + mouthT.getY()) / 2));
 
         topToChin = lengthBetween(chin, fore);
         eyesToChin = lengthBetween(eyeMid, chin);
         topToNostrils = lengthBetween(fore, noseMid);
         topToPupils = lengthBetween(fore, eyeMid);
         nostrilsToChin = lengthBetween(noseMid, chin);
         eyesToLips = lengthBetween(eyeMid, lipsMid);
         eyesToNostrils = lengthBetween(eyeMid, noseMid);
        nostrilsToLips = lengthBetween(noseMid, lipsMid);
         topToEyebrows = lengthBetween(fore, browMid);
         eyebrowsToNose = lengthBetween(browMid, noseMid);
         noseToChin = lengthBetween(noseMid, chin);
         noseToLips = lengthBetween(noseMid, mouthT);
         lipsToChin = lengthBetween(mouthB, chin);
         earToEar = lengthBetween(earL, earR);
 
     }
 
     private double lengthBetween(SalientPoint a, SalientPoint b) {
         double i = (a.getX() - b.getX());
         double j = (a.getY() - b.getY());
         double k = (i * i) + (j * j);
         return Math.sqrt(k);
     }
 
     private double ratio(double a, double b) {
         return a / b;
     }
 
     public SalientPointCollection loadPoints(String pointsFile) throws FileNotFoundException {
         SalientPointCollection points = new SalientPointCollection();
         File file = new File(pointsFile);
         String temp;
         Scanner fileScanner = new Scanner(file);
 
         while (fileScanner.hasNextLine()) {
             SalientPoint sp = points.getCurrent();
             int x, y;
             String line = fileScanner.nextLine();
             String pointName = line.substring(0, line.lastIndexOf(':'));
             String values = line.substring(line.lastIndexOf(':') + 2);
 
             Scanner lineScanner = new Scanner(values);
             temp = lineScanner.next(); //x
             temp = lineScanner.next(); //=
             x = lineScanner.nextInt();
             temp = lineScanner.next(); //y
             temp = lineScanner.next(); //=
             y = lineScanner.nextInt();
 
             if ((x == 0 && y == 0)) {
                 break;
             }
             sp.setX(x);
             sp.setY(y);
             points.iterate();
 
             lineScanner.close();
         }
         fileScanner.close();
         if (points.getCurrent() != null) {
             //nextclickmessage.setText(points.getCurrent().getName());
         }
 
         return points;
     }
 
     public double ratioGoldenOne() {
         return ratio(eyesToChin, topToPupils);
     }
 
     public double ratioGoldenTwo() {
         return ratio(topToNostrils, nostrilsToChin);
     }
 
     public double ratioGoldenThree() {
         return ratio(eyesToLips, lipsToChin);
     }
 
     public double ratioGoldenFour() {
         return ratio(nostrilsToChin, eyesToNostrils);
     }
 
     public double ratioGoldenFive() {
         return ratio(eyesToNostrils, nostrilsToLips);
     }
 
     public double ratioGoldenSix() {
         return ratio(lipsToChin, nostrilsToLips);
     }
     
     public double ratioGoldenMean() {
         return (ratioGoldenOne()+ratioGoldenTwo()+ratioGoldenThree()+ratioGoldenFour()+ratioGoldenFive()+ratioGoldenSix())/6;
     }
     
     public double ratioThirdsOne() {
         return ratio(topToEyebrows, topToChin);
     }
     
     public double ratioThirdsTwo() {
         return ratio(eyebrowsToNose, topToChin);
     }
     
     public double ratioThirdsThree() {
         return ratio(noseToChin, topToChin);
     }
     
     public double ratioThirdsFour() {
         return ratio(noseToLips, lipsToChin);
     }
 
     public double ratioWidthToHeight() {
         return ratio(earToEar, topToChin);
     }
     
     
 
     public void writeData(String filename) throws FileNotFoundException, UnsupportedEncodingException {
         PrintWriter writer = new PrintWriter(filename, "UTF-8");
         writer.println("Measurements");
         writer.printf(
                  "1: %f\n"
                 +"2: %f\n"
                 +"3: %f\n"
                 +"4: %f\n"
                 +"5: %f\n"
                 +"6: %f\n"
                 +"7: %f\n"
                 +"8: %f\n"
                 +"9: %f\n"
                 +"10: %f\n"
                 +"11: %f\n"
                 +"12: %f\n"
                 +"13: %f\n", 
                 topToChin, eyesToChin, topToNostrils, 
                 topToPupils, nostrilsToChin, eyesToLips, 
                 lipsToChin, eyesToNostrils, nostrilsToLips,
                 topToEyebrows, eyebrowsToNose, noseToChin, noseToLips);
         writer.println("\nGolden Proportions");
         writer.println(String.format("2:4 = %f", ratioGoldenOne()));
         writer.println(String.format("3:5 = %f", ratioGoldenTwo()));
         writer.println(String.format("6:7 = %f", ratioGoldenThree()));
         writer.println(String.format("5:8 = %f", ratioGoldenFour()));
         writer.println(String.format("8:9 = %f", ratioGoldenFive()));
         writer.println(String.format("7:9 = %f", ratioGoldenSix()));
         writer.println(String.format("mean = %f", ratioGoldenMean()));
         writer.println("\nFacial Thirds");
         writer.println(String.format("10:1 = %f", ratioThirdsOne()));
         writer.println(String.format("11:1 = %f", ratioThirdsTwo()));
         writer.println(String.format("12:1 = %f", ratioThirdsThree()));
         writer.println(String.format("13:14 = %f", ratioThirdsFour()));
         writer.close();
     }
 }
