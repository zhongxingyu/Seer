 package sisop;
 
 import sisop.FairSemaphore;
 import sisop.logging.Log;
 import java.util.Vector;
 
 
 public class SynchPort<T> {
     Vector<Message<T>> buffer;
     int head, tail, count, maxDim;
     FairSemaphore synchAdd, synchRemove, synchReceive;
 
     public SynchPort(int nMitt) {
         buffer = new Vector<Message<T>>(nMitt);
         synchAdd = new FairSemaphore(0);
         synchRemove = new FairSemaphore(0);
         synchReceive = new FairSemaphore(0);
         head = 0;
         tail = 0;
         count = 0;
         maxDim = nMitt;
     }
 
     public void sendTo( T message, String name ) {
         Message<T> app = new Message<T>(message, name);
         boolean go;
         synchronized(buffer){
            go=(count==(maxDim-1))?false:true;
         }    
         if (!go) {
             Log.info(Thread.currentThread().getName() + ": Wait empty element in port");
             //System.out.println(Thread.currentThread().getName() + ": Wait empty element in port");
             synchAdd.P();
         }
         synchronized(buffer){
            try {
                buffer.setElementAt(app, tail);    
            }
            catch (ArrayIndexOutOfBoundsException e) {
                buffer.add(tail, app);
            }
             tail = (tail+1)%maxDim;
             count++;
             Log.info(Thread.currentThread().getName() + ": Insert message in port");
             //System.out.println(Thread.currentThread().getName() + ": Insert message in port");
             
             if (!synchReceive.isEmpty()) synchReceive.V();
             Log.info(Thread.currentThread().getName() + ": Wait until message received");
             //System.out.println(Thread.currentThread().getName() + ": Wait until message received");
        }
        //FIXME: fare in modo che dopo il blocco synchronized venga effettuata subito la P() altrimenti ci sta inversione di priorità
         synchRemove.P();
     }
 
     public Message<T> receiveFrom() {
         Log.info(Thread.currentThread().getName() + ": Start receive");
         //System.out.println(Thread.currentThread().getName() + ": Start receive");
         Message<T> app;
             boolean go;
             synchronized(buffer){
                 go=(count==0)?false:true;
             }
             if (!go) {
                 Log.info(Thread.currentThread().getName() + ": Wait a element in port");
                 //System.out.println(Thread.currentThread().getName() + ": Wait a element in port");
                 
                 synchReceive.P();
             }
             synchronized(buffer){
                 app = buffer.get(head);
                 head = (head+1)%maxDim;
                 count--;
             }
             Log.info(Thread.currentThread().getName() + ": Extract message");
             //System.out.println(Thread.currentThread().getName() + ": Extract message");
             synchRemove.V();            
            if (!synchAdd.isEmpty()){
                Log.severe("Devo risvegliare per inserimento buffer");
                synchAdd.V();
            }
             return app;
         }
 }
 
 
 
 
 
 
 class Message<T> {
     T message;
     String threadName;
 
     Message( T data, String name) {
         this.message = data;
         this.threadName = name;
     }
 }
