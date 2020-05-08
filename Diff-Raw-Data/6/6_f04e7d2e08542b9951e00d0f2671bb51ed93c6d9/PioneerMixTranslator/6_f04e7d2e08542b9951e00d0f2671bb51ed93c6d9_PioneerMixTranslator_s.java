 package com.hifiremote.jp1;
 
 public class PioneerMixTranslator
   extends Translate
 {
   public PioneerMixTranslator( String[] textParms )
   {
     super( textParms );
   }
 
   // called to store parms into hex data
   public void in( Value[] parms, Hex hex, DeviceParameter[] devParms, int onlyIndex )
   {
     int flag = extract( hex, 14, 2 );
     boolean doInsert = true;
     for ( int i = 0; i < parms.length; i++ )
     {
       if (( parms[ i ] != null ) && ( parms[ i ].getValue() != null ))
       {
         int val = (( Integer )parms[ i ].getValue()).intValue();
         switch( i )
         {
           case 0: // Prefix device
             if ( val == 0 )  // none
               flag = 0;
             else // dev1
               flag |= 1;
             break;
           case 1: // Prefix cmd
             if ( val == 0 ) // none
               flag = 0;
             else if ( val == 1 ) // cmd1
               flag = 1;
             else // cmd2
               flag = 3;
             break;
           case 2:  // Device
             if ( val == 0 ) // dev1
               flag = 0;
             else //dev2
               flag |= 1;
             break;
           case 3:  // OBC
             doInsert = false;
             break;
         }
       }
     }
     if ( doInsert )
       insert( hex, 14, 2, flag );
   }
 
   // called to extract parms from hex data
   public void out( Hex hex, Value[] parms, DeviceParameter[] devParms )
   {
     int val = extract( hex, 14, 2 );
     Integer zero = new Integer( 0 );
     Integer one = new Integer( 1 );
     switch ( val )
     {
       case 0:   // single
         parms[ 0 ] = new Value( zero, null );  // PrefixDevice = none
         parms[ 1 ] = new Value( zero, null );  // PrefixCmd = none
         parms[ 2 ] = new Value( zero, null );  // Device = dev1
         break;
       case 1:  // dev1/cmd1
         parms[ 0 ] = new Value( one, null );   // PrefixDevice = dev1
         parms[ 1 ] = new Value( one, null );   // PrefixCmd = cmd1
         parms[ 2 ] = new Value( one, null );   // Device = dev2
         break;
       case 3: // dev1/cmd2
         parms[ 0 ] = new Value( one, null );   // PrefixDevice = dev1
         parms[ 1 ] = new Value( new Integer( 2 ), null );  // PrefixCmd = cmd2
         parms[ 2 ] = new Value( one, null );   // Device = dev 2
         break;
     }
   }

  private int styleIndex = 0;
  private int cmdIndex = 1;
 }
