 
 public class AnimationY {
 	
 	public static void AnimationY() {
 		
 	if(Strings.AnimationY == true) {		
 				if(Strings.ChaY <= Strings.BlockHhe) {			
 					Strings.ChaY = Strings.BlockHhe;
 					Strings.StepGrass = "false";
 					Strings.AnimationY = false;				
 				}
 				if(!(Strings.ChaY <= Strings.BlockHhe)) {
 					if(Strings.AnimationY == true) {
 						Strings.ChaY =  Strings.ChaY - 4;					
 					}
 				
 			}			
 		}		
 	}
 }
