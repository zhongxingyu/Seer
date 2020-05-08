 package sos;
 
 import java.util.*;
 
 /**
  * This class sets up the SOS simulation
  *
  * @see RAM
  * @see CPU
  * @see SOS
  * @see Program
  * @see Device
  */
 public class Sim
 {
     /**
      * the constructor does nothing
      *
      */
     public Sim()
     {
     }
 
     /**
      * runSimple
      *
      * runs a single counting program that prints to the console
      *
      *
      */
     public static void runSimple()
     {
         //Create the simulated hardware and OS
         RAM ram = new RAM(1000, 10);
         ConsoleDevice cd = new ConsoleDevice();
         CPU cpu = new CPU(ram);
         SOS os  = new SOS(cpu, ram);
 
         //Register the device drivers with the OS
         os.registerDevice(cd, 1);
 
         //Load the program into RAM
         Program prog = new Program();
         if (prog.load("print10.asm", false) != 0)
         {
             //Error loading program so exit
             return;
         }
         os.createProcess(prog,  500);
         cpu.run();
     }//runSimple
 
     /**
      * runIO
      *
      * runs a program that reads from the keyboard and prints to the console
      *
      *
      */
     public static void runIO()
     {
         //Create the simulated hardware and OS
         RAM ram = new RAM(1000, 10);
         KeyboardDevice kd = new KeyboardDevice();
         ConsoleDevice cd = new ConsoleDevice();
         CPU cpu = new CPU(ram);
         SOS os  = new SOS(cpu, ram);
 
         //Register the device drivers with the OS
         os.registerDevice(kd, 0);
         os.registerDevice(cd, 1);
 
         //Load the program into RAM
         Program prog = new Program();
        if (prog.load("begert13_freire14.asm", false) != 0)
         {
             //Error loading program so exit
             return;
         }
         os.createProcess(prog,  500);
         cpu.run();
         
     }//runIO
 
     /**
      * main
      *
      * This function makes the simulation go.
      *
      */
     public static void main(String[] args)
     {
         runIO();
         
         System.out.println("END OF SIMULATION");
 
         System.exit(0);         // Success
     }//main
     
 };//class Sim
