 package cz.romario.opensudoku.game.command;
 
 import java.util.Stack;
 
 import android.os.Bundle;
 
 import cz.romario.opensudoku.game.SudokuGame;
 
 
 public class CommandInvoker {
 	
 	private Stack<Command> mUndoStack = new Stack<Command>();
 	
 	private SudokuGame mGame;
 	
 	public CommandInvoker(SudokuGame game) {
 		mGame = game;
 	}
 	
 	public void execute(Command c) {
 		c.execute();
 		mUndoStack.push(c);
 	}	
 	
 	/** 
 	 * Undo last command.
 	 */
 	public void undo() {
 		if (!mUndoStack.empty()) {
 			Command c = mUndoStack.pop();
 			c.undo();
 		}
 	}
 	
 	public boolean hasSomethingToUndo() {
 		return mUndoStack.size() != 0;
 	}
 	
 	private static final int COMMAND_SET_CELL_VALUE = 1;
 	private static final int COMMAND_EDIT_CELL_NOTE = 2;
 	private static final int COMMAND_FILL_IN_NOTES = 3;
 	private static final int COMMAND_CLEAR_ALL_NOTES = 4;
 	
 	public void saveState(Bundle outState) {
 		
 		outState.putInt("commands.size", mUndoStack.size());
 		
		for (int i = 0; i < mUndoStack.size(); i++) {
			Command command = mUndoStack.get(i);
 			int commandClass = 0;
			if (command instanceof SetCellValueCommand)
 				commandClass = COMMAND_SET_CELL_VALUE;
			else if (command instanceof EditCellNoteCommand)
 				commandClass = COMMAND_EDIT_CELL_NOTE;
			else if (command instanceof FillInNotesCommand)
 				commandClass = COMMAND_FILL_IN_NOTES;
			else if (command instanceof ClearAllNotesCommand)
 				commandClass = COMMAND_CLEAR_ALL_NOTES;
 			else
				throw new IllegalStateException(String.format("Unknown command class '%s'.", command.getClass().getName()));
 			
			outState.putInt("commands." + i + ".class", commandClass);
 			Bundle commandState = new Bundle();
			command.saveState(commandState);
			outState.putBundle("commands." + i + ".state", commandState);
 			
 		}
 		
 	}
 	
 	public void restoreState(Bundle state) {
 		int commandsSize = state.getInt("commands.size");
 		
 		for (int i = 0; i < commandsSize; i++) {
 			Command command;
 			
 			int commandClass = state.getInt("commands." + i + ".class");
 			switch (commandClass) {
 			case COMMAND_SET_CELL_VALUE:
 				command = new SetCellValueCommand();
 				break;
 			case COMMAND_EDIT_CELL_NOTE:
 				command = new EditCellNoteCommand();
 				break;
 			case COMMAND_FILL_IN_NOTES:
 				command = new FillInNotesCommand();
 				break;
 			case COMMAND_CLEAR_ALL_NOTES:
 				command = new ClearAllNotesCommand();
 				break;
 			default:
 				throw new IllegalStateException(String.format("Unknown command class '%s'.", commandClass));
 			}
 			
 			Bundle commandState = state.getBundle("commands." + i + ".state");
 			command.restoreState(commandState, mGame);
			
			mUndoStack.add(command);
 		}
 	}
 }
