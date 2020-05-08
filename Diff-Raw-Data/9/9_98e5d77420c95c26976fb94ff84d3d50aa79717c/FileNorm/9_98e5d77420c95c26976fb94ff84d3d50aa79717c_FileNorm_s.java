 package slim.javaseis.io;
 
 import java.util.Arrays;
 import beta.javaseis.io.Seisio;
 import beta.javaseis.util.SeisException;
 
 public class FileNorm {
 
   public static double Norm(String path,int norm) throws SeisException {
 	  
 	  Seisio sio = new Seisio(path);
 	  sio.open("r");
 	   
       double finallala = 0.0;
       double normComp = 0.0;
       
       double exp = 1/(double)norm ;
       
       long[] Axis = sio.getGridDefinition().getAxisLengths() ;
     
       
       for(int j=0;j<Axis[3];j++) {
     	   for(int i=0;i<Axis[2];i++) {
     	   
               pos[]
                
    	       int[] pos = {0,0,i,j} ;
 	           System.out.println(Arrays.toString(pos));
 	           
 	           if (norm == 1 || norm == 0 || norm ==2) {
 	           
 	               normComp += slim.javaseis.io.PartNorm.sliceNormComponent(sio,pos,norm) ;
 	           
 	           } else if (norm == 999 || norm == -999) {
 	        	   
 	        	   normComp = slim.javaseis.io.PartNorm.sliceNormComponent(sio,pos,norm) ;
 	           }
     	   }
 	    }     
 	    
     sio.close() ;
 	
     if (norm == 999) {
     	
     	finallala = normComp ;
     	
     } else if (norm == -999) {
     	
     	finallala = normComp ;
     	
     } else if (norm == 0 || norm == 1 || norm == 2) {
     
       finallala = Math.pow(normComp,exp);
     
     }
     
     return finallala ;
 }
   
 }
   
 
