 public class DriverExam {
    
    //Instance fields
    private char[] correctAnswers = 
    {'B','D','A','A','C','A','B','A','C','D','B','C','D','A','D','C','C','B','D','A'};
    
    private char[] studentAnswers;
    
    //Constructor
    public DriverExam(char[] c) {
       
       studentAnswers = new char[c.length];
       
       //move passed array values to instance field
       for(int i = 0; i < c.length; i++) {
          studentAnswers[i] = c[i];
       }
    }
    
    /**
       the passed method compares studentAnswers to
       correctAnswers and returns true if the
       answers match, false if they do not.
    */
    public boolean passed() {
       
       boolean pass;
       int index = 0;
       
       //compare answers
      do{
			
          if(studentAnswers[index] == correctAnswers[index]) {
             pass = true;
             index++;
          } else {
             pass = false;
          }
      }while(pass == true && index < studentAnswers.length);
       
       return pass;   
    }
    
    public int[] questionsMissed() {
       // todo: implement
       return null;
    }
    
    public int totalCorrect() {
       // todo: implement
       return 0;
    }
    
    public int totalIncorrect() {
       // todo: implement
       return 0;
    }
 
    public static void main(String[] args) {
       char[] s = {'A', 'B', 'C', 'D', 'A', 'B', 'C', 'D', 'A', 'B', 'C', 'D', 'A', 'B', 'C', 'D', 'A', 'B', 'C', 'D'};
       DriverExam e = new DriverExam(s);
       System.out.println(e.passed());
    }
 
 }
 
