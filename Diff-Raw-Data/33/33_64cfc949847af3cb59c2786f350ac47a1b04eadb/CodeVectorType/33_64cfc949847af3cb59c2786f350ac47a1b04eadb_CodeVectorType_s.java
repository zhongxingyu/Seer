 package machines.types;
 
 public class CodeVectorType {
 	
   AbstractInstruction ins;
   Integer i;
   Boolean b;
 	
   public CodeVectorType(AbstractInstruction i)
   {
 	  
 	ins = i;
 		
   }
 	
   public CodeVectorType(int n)
   {
 	  
 	i = n;
 		
   }
 	
   public CodeVectorType(boolean bool)
   {
 	  
 	b = bool;
 		
   }
 	
   public String toString()
   {
 	  
     if (ins != null)
       return ins.getClass().getSimpleName();
   
     else if (i != null)
       return i.toString();
   
     else
       return b.toString();
   
   }
 	
   public AbstractInstruction getIns()
   {
 	  
 	return ins;
 		
   }
 	
   public int getInt()
   {
 	  
 	return i;
 		
   }
 	
   public boolean getBool()
   {
 	  
 	return b;
 		
   }
 
 }
