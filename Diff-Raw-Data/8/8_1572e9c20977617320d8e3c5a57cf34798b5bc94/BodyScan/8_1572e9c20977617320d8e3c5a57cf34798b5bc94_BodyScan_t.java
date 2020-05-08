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
 
     // Constants
     private static final String INDENT = "    ";
 
     // Instance variables
     private final int lineNumber;
     private final ActorRef securityStation;
 
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
         if ( msg instanceof Passenger ) {
             Passenger p = (Passenger)msg;
             printMsg("Passenger " + p.getName() + " enters");
             // If msg is a Passenger, perform the body scan.
             if ( ( Math.random()*100 ) < TestBedConstants.BODY_SCAN_FAIL_PERCENTAGE ) {
                 printMsg("Passenger " + p.getName() + " fails");
                 securityStation.tell( new Report( p, false ) );
             } else {
                 // Body scan approved, send a pass Report to security station
                 printMsg("Passenger " + p.getName() + " passes");
                 securityStation.tell( new Report( p, true ) );
             }
             // Tell scan queue to send the next passenger
             getContext().channel().tell(new NextMsg(), getContext());
         } else if ( msg instanceof CloseMsg ) {
             // If msg is a CloseMsg, relay to the security station and terminate self.
             printMsg("Close received");
             securityStation.tell( msg );
             printMsg("Close sent to security");
             getContext().stop();
             printMsg("Closed");
         }
     }

     /**
      * Prints a properly indented and labeled message
      * 
      * @param msg the message to print
      */
     private void printMsg(String msg) {
         System.out.println(INDENT + "Body Scan " + lineNumber + ": " + msg);
     }
 }
