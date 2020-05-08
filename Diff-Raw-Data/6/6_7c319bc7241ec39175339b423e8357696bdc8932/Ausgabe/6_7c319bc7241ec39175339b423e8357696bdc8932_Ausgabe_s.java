 import com.pi4j.io.gpio.GpioController;
 import com.pi4j.io.gpio.GpioFactory;
 import com.pi4j.io.gpio.GpioPinDigitalOutput;
 import com.pi4j.io.gpio.RaspiPin;
 
 /**
 * Created with IntelliJ IDEA.
 * User: Daniel
 * Date: 17.08.13
 * Time: 13:14
 * To change this template use File | Settings | File Templates.
  */
 public class Ausgabe {
     public static GpioPinDigitalOutput[] init() {
         GpioPinDigitalOutput out[] = new GpioPinDigitalOutput[10];
         // create gpio controller
         final GpioController gpio = GpioFactory.getInstance();
 
         // provision gpio pin #01
         out[1] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01);
         out[4] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04);
         out[5] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05);
         out[6] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06);
         out[1].setState(true);
         out[4].setState(true);
         out[5].setState(true);
         out[6].setState(true);
 
 
         return out;
 
     }
 
     public static int[][] Adressen() {
 
         int LED_Positon[][] =
                 {{11, 26, 31, 90, 43, 56, 91, 86, 74, 69, 106, 115, 128, 153, 143, 130, 165, 175},
                         {10, 23, 30, 25, 41, 52, 94, 96, 67, 65, 105, 113, 123, 152, 151, 129, 166, 178},
                         {2, 24, 29, 36, 33, 62, 63, 84, 79, 71, 110, 118, 124, 157, 150, 135, 167, 171},
                         {3, 17, 28, 34, 46, 53, 64, 83, 80, 101, 98, 119, 154, 156, 139, 141, 161, 179},
                         {4, 9, 27, 32, 47, 50, 57, 89, 78, 100, 99, 120, 122, 155, 140, 136, 168, 170},
                         {5, 16, 18, 35, 48, 51, 95, 85, 73, 72, 111, 112, 125, 145, 138, 134, 172, 173},
                         {6, 15, 19, 38, 42, 55, 93, 88, 76, 104, 102, 121, 158, 159, 144, 137, 163, 174},
                         {7, 14, 20, 39, 49, 58, 61, 81, 77, 68, 107, 117, 114, 148, 142, 131, 169, 176},
                         {8, 13, 21, 40, 44, 59, 92, 87, 66, 97, 108, 116, 127, 160, 146, 132, 164, 180},
                         {1, 12, 22, 37, 45, 54, 60, 82, 75, 70, 103, 109, 126, 149, 147, 133, 162, 181}};
 
 
         return LED_Positon;
 
 
     }
 
     public static void Ausgabe(int Bild[][], GpioPinDigitalOutput SI, GpioPinDigitalOutput SCK, GpioPinDigitalOutput RCK) {
         int Ausgabe[] = new int[190];
         for (int x = 1; x < 19; x++) {
             for (int y = 1; y < 11; y++) {
 
                 if (Bild[x][y] == 1) {
                     if ((y > 1) & (y < 7)) {
                         Ausgabe[Adressen()[y - 1][x - 1]] = 1;
                     } else {
                         Ausgabe[Adressen()[y - 1][x - 1]] = 0;
                     }
                 } else {
                     if ((y > 1) & (y < 7)) {
                         Ausgabe[Adressen()[y - 1][x - 1]] = 0;
                     } else {
                         Ausgabe[Adressen()[y - 1][x - 1]] = 1;
                     }
                 }
 
 
             }
 
         }
 
         for (int i = 1; i < 182; i++) {
             //System.out.println(Ausgabe[182 - i]);
             if (Ausgabe[182 - i] == 1) {
                 SI.setState(false);
             } else {
                 SI.setState(true);
             }
             SCK.setState(false);
             SCK.setState(true);
 
         }
         RCK.setState(false);
         RCK.setState(true);
 
     }
 
 }
