 import akka.actor.ActorRef;
 import akka.actor.UntypedActor;
 
 /**
  * The body scanner as part of a line in the airport security system. A passenger
  * is passed in and is sent away with either a pass or fail flag depending on the
  * results of the body scan.
  * 
  * @author Anthony Barone
  * @author Ryan Clough
  * @author Rebecca Dudley
  * @author Ryan Mentley
  */
 public class BodyScan extends UntypedActor {
 
     // Instance variables
     private final int lineNumber;
     private final ActorRef securityStation;
     private final String INDENT = "    ";
 
     /**
      * Constructor for a body scanner.
      * 
      * @param lineNumber - Which line this scanner is in
      * @param scanQueue - The scan queue before this (for sending NextMsgs to)
      * @param securityStation - The security station after this (for sending Reports to)
      */
     public BodyScan( int lineNumber, ActorRef securityStation ) {
         this.lineNumber = lineNumber;
         this.securityStation = securityStation;
     }
 
     @Override
     public void onReceive( final Object msg ) throws Exception {
         //TODO printouts
 
     	
         if ( msg instanceof Passenger ) {
         	System.out.println(INDENT + "Body Scan " + lineNumber + ": Passenger " + 
         			((Passenger) msg).getName() + " enters");
             // If msg is a Passenger, perform the body scan.
            if ( ( Math.random()*100 ) <= TestBedConstants.BODY_SCAN_FAIL_PERCENTAGE ) {
             	System.out.println(INDENT + "Body Scan " + lineNumber + ": Passenger " + 
             			((Passenger) msg).getName() + " fails");
                 securityStation.tell( new Report( (Passenger)msg, ScanResult.FAIL ) );
             } else {
                 // Body scan approved, send a pass Report to security station
             	System.out.println(INDENT + "Body Scan " + lineNumber + ": Passenger " + 
             			((Passenger) msg).getName() + " passes");
                 securityStation.tell( new Report( (Passenger)msg, ScanResult.PASS ) );
             }
             // Tell scan queue to send the next passenger
             getContext().channel().tell(new NextMsg(), getContext());
         } else if ( msg instanceof CloseMsg ) {
             // If msg is a CloseMsg, relay to the security station and terminate self.
         	System.out.println(INDENT + "Body Scan " + lineNumber + "close received");
             securityStation.tell( msg );
            System.out.println(INDENT + "Body Scan " + lineNumber + "close sent to security");
             this.getContext().stop();
             System.out.println(INDENT + "Body Scan " + lineNumber + "closed");
         }
     }
 }
