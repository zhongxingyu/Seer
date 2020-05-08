 package de.htwg.seapal.aview.tui.states.mark;
 
 import java.util.UUID;
 
 import de.htwg.seapal.aview.tui.StateContext;
 import de.htwg.seapal.aview.tui.TuiState;
 import de.htwg.seapal.aview.tui.activity.MarkActivity;
 
 public class NewState implements TuiState {
 
 	@Override
 	public String buildString(StateContext context) {
 		return "q - quit \n\n Enter MarkName";
 	}
 
 	@Override
 	public boolean process(StateContext context, String input) {
 		if (input.equals("q")) {
 			context.setState(new StartState());
 			return false;
 		}
		UUID mark = ((MarkActivity) context).getController().newMark();
		((MarkActivity) context).getController().setName(mark, input);
 		context.setState(new ShowState(mark));
 		return true;
 	}
 
 }
