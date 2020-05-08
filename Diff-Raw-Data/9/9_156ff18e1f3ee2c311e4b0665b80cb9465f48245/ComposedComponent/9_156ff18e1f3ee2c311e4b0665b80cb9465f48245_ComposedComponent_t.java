 package org.pillarone.riskanalytics.core.components;
 
 import org.pillarone.riskanalytics.core.wiring.ITransmitter;
 import org.pillarone.riskanalytics.core.wiring.SilentTransmitter;
 import org.pillarone.riskanalytics.core.wiring.WiringUtils;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 abstract public class ComposedComponent extends Component {
 
     private List<ITransmitter> allInputReplicationTransmitter = new ArrayList<ITransmitter>();
     private List<SilentTransmitter> allOutputReplicationTransmitter = new ArrayList<SilentTransmitter>();
 
 
     public List<ITransmitter> getAllInputReplicationTransmitter() {
         return allInputReplicationTransmitter;
     }
 
     public List<SilentTransmitter> getAllOutputReplicationTransmitter() {
         return allOutputReplicationTransmitter;
     }
 
     protected void doCalculation() {
         for (ITransmitter transmitter : allInputReplicationTransmitter) {
             transmitter.transmit();
         }
     }
 
     protected void reset() {
         super.reset();
         resetOutputTransmitters();
     }
 
 
     /**
      * Reset outputReplicationTransmitter because they are not used as inputTransmitter anywhere !
      */
     private void resetOutputTransmitters() {
         for (ITransmitter output : allOutputReplicationTransmitter) {
             output.setTransmitted(false);
         }
     }
 
     public void validateParameterization() {
         validateParameterization(this);
     }
 
     private void validateParameterization(ComposedComponent component) {
         for (Component subComponent : component.allSubComponents()) {
             subComponent.validateParameterization();
             if (subComponent instanceof ComposedComponent) {
                 validateParameterization((ComposedComponent) subComponent);
             }
         }
     }
 
     /**
      * Each ComposedComponent has to define its internal wiring. The implementation
      * must not be recursive. As the recursive descent is done in wireComposedComponents().
      */
     abstract public void wire();
 
     /**
      * Calls the internal wiring of a ComposedComponent and recursively
      * the internal wiring of ComposedComponent it may contain.
      */
     public void internalWiring() {
         wireComposedComponents(this);
     }
 
     /**
      * Wires the component itself and all its sub components recursively,
      * if they are composed components.
      *
      * @param component
      */
     private void wireComposedComponents(ComposedComponent component) {
         component.wire();
         List<Component> componentList = component.allSubComponents();
         for (Component subComponent : componentList) {
             if (subComponent instanceof ComposedComponent) {
                 wireComposedComponents((ComposedComponent) subComponent);
             }
         }
     }
 
     public void internalChannelAllocation() {
         allocateChannelsToPhases(this);
     }
 
     private void allocateChannelsToPhases(ComposedComponent component) {
         if (component instanceof MultiPhaseDynamicComposedComponent) {
             ((MultiPhaseDynamicComposedComponent) component).allocateChannelsToPhases();
         } else if (component instanceof MultipleCalculationPhaseComposedComponent) {
             ((MultipleCalculationPhaseComposedComponent) component).allocateChannelsToPhases();
         }
         List<Component> componentList = component.allSubComponents();
         for (Component subComponent : componentList) {
             if (subComponent instanceof ComposedComponent) {
                 allocateChannelsToPhases((ComposedComponent) subComponent);
             }
         }
     }
 
    protected LinkedList<Component> cachedComponentList = null;

     /**
      * Sub components are either properties on the component or in case
      * of dynamically composed components stored in its componentList.
      *
      * @return all sub components
      */
     public List<Component> allSubComponents() {
         if (cachedComponentList != null) return cachedComponentList;
        LinkedList<Component> result = new LinkedList<Component>();
         for (Object prop : this.allCachedComponentProperties().values()) {
             if (prop instanceof Component) {
                 result.add((Component) prop);
             }
         }
         cachedComponentList = result; // avoid premature leaking
        return (List<Component>) cachedComponentList.clone(); //clone in order to make sure no one can modify the cached list
     }
 
     public void optimizeWiring() {
         WiringUtils.optimizeWiring(this);
     }
 }
