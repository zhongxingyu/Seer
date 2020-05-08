 package com.haakenstad.potwater.scheduler;
 
 import com.pi4j.device.piface.PiFace;
 import com.pi4j.device.piface.impl.PiFaceDevice;
 import com.pi4j.io.gpio.GpioPinDigitalOutput;
 import com.pi4j.wiringpi.Spi;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bjornhaa
  * Date: 11.07.13
  * Time: 21:12
  * To change this template use File | Settings | File Templates.
  */
 class PiFaceCommand extends Command {
 
 
     private static PiFaceDevice pifaceDevice;
     private final GpioPinDigitalOutput pumpPin;
     private final GpioPinDigitalOutput valvePin;
 
     public PiFaceCommand(Integer valve, Integer pump) throws Exception {
         super(valve, pump);
         pifaceDevice = new PiFaceDevice(PiFace.DEFAULT_ADDRESS, Spi.CHANNEL_0);
         valvePin = pifaceDevice.getOutputPin(valve);
         pumpPin = pifaceDevice.getOutputPin(pump);
 
     }
 
     public void enable() {
         System.out.println("\tStarting pump and valve " + valve);
        pifaceDevice.getOutputPin(3).high();
         valvePin.high();
        //pumpPin.high();
     }
 
     public void disable() throws Exception {
         System.out.println("\tStopping pump and valve " + valve + ". Has been running for " + formatDuration());
        //pumpPin.low();
        pifaceDevice.getOutputPin(3).low();
         valvePin.low();
     }
 
 }
