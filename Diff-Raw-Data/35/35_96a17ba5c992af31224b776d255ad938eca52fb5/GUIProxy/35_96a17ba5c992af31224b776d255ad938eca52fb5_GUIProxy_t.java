 /*
  * This is a stub proxy.
  * It is only intented for use in the observer pattern,
  * where update() will be the only method called.
  */
 
 
 package command;
 
 import gui.Main.GUI;
 import hypeerweb.HyPeerWeb;
 
 public class GUIProxy
     extends GUI
 {
     private GlobalObjectId globalObjectId;
 
     public GUIProxy(GlobalObjectId globalObjectId)
     {
         this.globalObjectId = globalObjectId;
     }
 
     public void update(java.util.Observable p0, java.lang.Object p1){
         String[] parameterTypeNames = new String[2];
         parameterTypeNames[0] = "java.util.Observable";
         parameterTypeNames[1] = "java.lang.Object";
         Object[] actualParameters = new Object[2];
         actualParameters[0] = p0;
         actualParameters[1] = p1;
         Command command = new Command(globalObjectId.getLocalObjectId(), "gui.Main.GUI", "update", parameterTypeNames, actualParameters, false);
         PeerCommunicator.getSingleton().sendASynchronous(globalObjectId, command);
     }
 
     public boolean equals(java.lang.Object p0){
        /*String[] parameterTypeNames = new String[1];
         parameterTypeNames[0] = "java.lang.Object";
         Object[] actualParameters = new Object[1];
         actualParameters[0] = p0;
         Command command = new Command(globalObjectId.getLocalObjectId(), "java.lang.Object", "equals", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
        return (Boolean)result;*/
        GlobalObjectId otherId = ((GUIProxy) p0).globalObjectId;
        return globalObjectId.equals(otherId);
     }
 
     public int hashCode(){
         String[] parameterTypeNames = new String[0];
         Object[] actualParameters = new Object[0];
         Command command = new Command(globalObjectId.getLocalObjectId(), "java.lang.Object", "hashCode", parameterTypeNames, actualParameters, true);
         Object result = PeerCommunicator.getSingleton().sendSynchronous(globalObjectId, command);
         return (Integer)result;
     }
     
     public Object writeReplace()
     {
         return this;
     }
     
     public Object readResolve()
     {
         GlobalObjectId newId = new GlobalObjectId();
         if (globalObjectId.onSameMachineAs(newId))
         {
             return GUI.getSingleton(HyPeerWeb.getSingleton());
         }
         else
         {
             return this;
         }
     }
 
 }
