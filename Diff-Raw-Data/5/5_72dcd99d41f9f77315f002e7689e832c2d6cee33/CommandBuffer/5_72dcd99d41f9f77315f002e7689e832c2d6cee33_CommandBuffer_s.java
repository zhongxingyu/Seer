 /**
  * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
  */
 package us.stangl.crostex.command;
 
 import us.stangl.crostex.util.Stack;
 
 /**
  * Object that holds commands that are applied to a target object, and can be undone.
  *
  * Commands are basically stored in a zipper comprised of a stack of
  * done commands and one of undone commands. Whenever any new command
  * gets added to done stack (other than via a redo), undone stack is cleared. 
  * 
  * @author Alex Stangl
  */
 public class CommandBuffer<T> {
 	private final T target;
 	
 	// stack of "done" commands, most recent on top
 	private Stack<UndoableCommand<T>> doneCommands = new Stack<UndoableCommand<T>>();
 	
 	// stack of "undone" commands, most recently undone on top
 	private Stack<UndoableCommand<T>> undoneCommands = new Stack<UndoableCommand<T>>();
 	
 	public CommandBuffer(T target) {
 		this.target = target;
 	}
 	
 	public void applyCommand(UndoableCommand<T> command) {
 		command.apply(target);
 		if (! undoneCommands.empty()) {
 			// allocating new stack rather than resetting fill pointer to avoid leaking memory
 			// by continuing to hold stale references in the unused portion of the stack
 			undoneCommands = new Stack<UndoableCommand<T>>();
 		}
 		doneCommands.push(command);
 	}
 	
 	public void undo() {
 		if (doneCommands.empty())
			throw new IllegalStateException("Attempt too undo empty CommandBuffer");
 		UndoableCommand<T> command = doneCommands.pop();
 		command.unApply(target);
 		undoneCommands.push(command);
 	}
 	
 	public void redo() {
 		if (undoneCommands.empty())
			throw new IllegalStateException("Attempt too redo CommandBuffer with nothing to redo");
 		UndoableCommand<T> command = undoneCommands.pop();
 		command.apply(target);
 		doneCommands.push(command);
 	}
 	
 	public boolean haveCommandsToUndo() {
 		return ! doneCommands.empty();
 	}
 	
 	public boolean haveCommandsToRedo() {
 		return ! undoneCommands.empty();
 	}
 }
