 package columbia.plt.tt;
 
 public class CheckType {
 	
 	
 	
	/*
 	
 	
 	type returns[String type]
 					  : 'String' {type = "String";}
 					  | 'Number' {type = "Number";}
 					  | 'Date' {type = "Date";}
 					  | 'Task' {type = "Task";}
 					  | 'TimeFrame' {type = "TimeFrame";}
 					  | 'Calendar' {type = "Calendar";}
 					  | 'Time' {type = "Time";}
 					  ;		
 			
 	expr returns[String result]:  
 		 ^(t=('||'|'&&') e1=expr e2=expr) { 
 		 checkType(bool, e1.type, $text); 
 		 checkType(bool, e2.type, $text); 
 		 $result = bool;} 
 	
 	
 		 | ^(t=('=='|'!='|'>'|'<'|'<='|'>=') e1=expr e2=expr) { 
 		 checkType(number, e1.type, $text); 
 		 checkType(number, e2.type, $text); 
 		 $result = bool;} 
 		 
 		 | ^(t=('+'|'-'|'*'|'/') e1=expr e2=expr) { 
 		 checkType(number, e1.type, $text); 
 		 checkType(number, e2.type, $text); 
 		 $type = number;} 
*/
 }
