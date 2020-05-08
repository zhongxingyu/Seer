 package com.hifiremote.jp1;
 
 public class TranslatorFromDev
   extends Translator
 {
   public TranslatorFromDev( String[] textParms )
   {
     super( textParms );
   }
 
   public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
   {
     int[] hex = hexData.getData();
     if ( index >= devParms.length )
     {
       System.err.println("TranslatorFromDev.in() index="+ index +" exceeds "+ devParms.length +" item buffer");
       return;
     }
     int w=0;
 
     Integer i = ( Integer )devParms[index].getValueOrDefault();
     if ( i == null )
       System.err.println("TranslatorFromDev.in() index="+ index +" missing parameter value");
     else
      w = i.intValue() >> lsbOffset;
 
     if ( comp )
       w = 0xFFFFFFFF - w;
 
     if ( lsb )
       w = reverse(w, bits );
 
     insert( hexData, bitOffset, bits, w );
   }
 
   public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
   {
   }
 }
 
 
