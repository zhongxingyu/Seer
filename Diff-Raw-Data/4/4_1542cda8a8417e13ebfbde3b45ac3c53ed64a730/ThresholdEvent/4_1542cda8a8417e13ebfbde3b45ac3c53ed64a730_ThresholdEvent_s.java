ackage org.h2o.autonomic.numonic;
 
 import uk.ac.standrews.cs.numonic.appinterface.ResourceType;
 
 /**
  * Value class containing the details of a particular threshold that has been breached. Sent from NUMONIC to any registered
  * classes implementing {@link IThresholdSubscriber}.
  *
  * @author Angus Macdonald (angus AT cs.st-andrews.ac.uk)
  */
 public class ThresholdEvent {
 
     /**
      * The resource this threshold applies to (e.g. CPU Utilization).
      */
     ResourceType resourceName;
 
     /**
      * The current level of utilization of this resource.
      */
     double value;
 
 }
