 
 public class AnimationY {
 	
 	public static void AnimationY() {
 		
		//if(Strings.Zusammensto.equalsIgnoreCase("false")) {
		//	if(608 - gui.ySpace - Strings.ChaY == 508) {
			//	Strings.AnimationY = true;
			//	Strings.berhrung = false;
				
			//}
	//	}
		
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
