 package exams.insurance;
 
 import exams.insurance.models.Quote;
 import exams.insurance.views.ConsoleOutputView;
 import exams.insurance.views.FormOutputView;
 import exams.insurance.views.InputView;
 import exams.insurance.views.OutputView;
 import exams.insurance.views.PromptInputView;
 
 public class Insurance {
 	
 	private static InputView INPUT_VIEW = new PromptInputView();
 	private static OutputView OUTPUT_VIEW = new FormOutputView();
 	
 	public static void main(String[] args) {
 		Quote quote = Insurance.INPUT_VIEW.getQuote();
 		Insurance.OUTPUT_VIEW.displayOutputViewForQuote(quote);
 	}
 	
 }
