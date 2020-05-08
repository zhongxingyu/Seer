 /**
  * Implements a linked-list array, to be used as runtime-efficient RAM.
  *
  * @author Ory Band
  * @Version 1.0
  */
 public class RAM {
     private Page[] ram;
    private int    first, last;
 
     public RAM(int hdSize, int ramSize) {
         if (ramSize <= 1) {
             throw new RuntimeException("RAM size too small.");
         } else if (hdSize <= 1) {
             throw new RuntimeException("HD size too small.");
         } else if (hdSize < ramSize) {
             throw new RuntimeException("HD size smaller than RAM size.");
         }
 
         this.ram = new Page[hdSize];
 
         // Init data.
         for (int i=0; i<hdSize; i++) {
            this.ram[i] = new Page("", -1, null, null);
         }
 
         for (int i=0; i<ramSize -1; i++) {
             // Set previous order.
             if (i >= 1) {
                 this.ram[i].prev = this.ram[i-1];
             }
 
             // Set advancing order.
             if (i <= ramSize -2) {
                 this.ram[i].next = this.ram[i+1];
             }
         }
 
         // Set first and last pages.
         this.first = this.ram[0];
        this.last  = this.ram[ramSize]
     }
 }
 
