 package de.htwg.seapal.aview.tui.states.mark;
 
 import java.util.UUID;
 
 import de.htwg.seapal.aview.tui.StateContext;
 import de.htwg.seapal.aview.tui.TuiState;
 import de.htwg.seapal.aview.tui.activity.MarkActivity;
import de.htwg.seapal.controller.IMarkController;
 
 public class NewState implements TuiState {
 
 	@Override
 	public String buildString(StateContext context) {
 		return "q - quit \n\n Enter MarkName";
 	}
 
 	@Override
 	public boolean process(StateContext context, String input) {
		IMarkController controller = ((MarkActivity) context).getController();
 		if (input.equals("q")) {
 			context.setState(new StartState());
 			return false;
 		}
		UUID mark = controller.newMark();
		controller.setName(mark, input);
		controller.setIsRouteMark(mark, false);
 		context.setState(new ShowState(mark));
 		return true;
 	}
 
 }
