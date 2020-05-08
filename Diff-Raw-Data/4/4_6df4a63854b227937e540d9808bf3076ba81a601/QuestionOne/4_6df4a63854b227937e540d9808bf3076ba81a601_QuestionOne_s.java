 public class QuestionOne{
   
 	static <T extends Printable> void print(T [] t){
 		if(t.length != 0){
 			for(int i = 0; i < t.length; i++){
 				if(t[i] != null)
 					t[i].put();
 			}
    	}
 	}//Close print Method
 }
 
 interface Printable{
 	void put();
 }
 
 class Date implements Printable{
 		private int day, month, year;
 		Date(){
 			//Default no args constructor
 		}
 		void get(){
 			//Do not code
 		}
 		boolean lte(Date d){
 			return true;
 			//Do not code
 		}
 		public void put() {
 			System.out.println(day + "/"+ month + "/" + year);
 		}
 }
