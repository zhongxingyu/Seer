 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller.terminal.controller;
 
 /**
  *
  * @author Valentin SEITZ
  */
 class TerminalVueStateMachine {
 
 	//The vues, first dimensions of possibilites array
 	private static final int IMPOSSIBLE = -2;
 	private static final int VUE_WELCOME = 0;
 	private static final int VUE_END = 0;
 	private static final int VUE_RENT = 1;
 	private static final int VUE_PAY = 2;
 	private static final int VUE_RETURN = 3;
 	private static final int VUE_RETURN_SUMMARY = 4;
 	//The actions which can change the vue, second dimension of possibilites
 	public static final int ACTION_DO_CANCEL = 0;
 	public static final int ACTION_DO_CONFIRM_RETURN = ACTION_DO_CANCEL;
 	public static final int ACTION_ASK_RENT = 1;
 	public static final int ACTION_ASK_PAY = 2;
 	public static final int ACTION_DO_RENT = ACTION_ASK_PAY;
 	public static final int ACTION_DO_PAY = 3;
 	public static final int ACTION_ASK_RETURN = 4;
 	public static final int ACTION_DO_RETURN = 5;
 	//The possibilites array
 	private static int[][] possibilities = {
 		{VUE_END, VUE_RENT, IMPOSSIBLE, IMPOSSIBLE, VUE_RETURN, IMPOSSIBLE}, //Welcome && End
 		{VUE_END, IMPOSSIBLE, VUE_PAY, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE}, //Rent
 		{IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, VUE_END, IMPOSSIBLE, IMPOSSIBLE}, //Pay
 		{VUE_END, IMPOSSIBLE, VUE_PAY, IMPOSSIBLE, IMPOSSIBLE, VUE_RETURN_SUMMARY}, //Return
 		{VUE_END, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE, IMPOSSIBLE} //Return summary
 	};
 	//The state of machine
 	private static int state = VUE_WELCOME;
 
 	public static int getState() {
 		return state;
 	}
 
 	public static boolean possibleAction(int action) {
 		return change(action, false);
 	}
 
 	public static boolean doAction(int action) {
 		return change(action, true);
 	}
 
 	private static boolean change(int action, boolean validate) {
 		boolean possible;
 		int nextState;
 		//State index ok?
 		possible = (0 <= state && state < possibilities.length);
 		if (possible) {
 			//Action index ok?
 			possible = (0 <= action && action < possibilities[state].length);
 		}
 		if (possible) {
 			//Reading next state, if not impossible
 			nextState = possibilities[state][action];
 			possible = (nextState != IMPOSSIBLE);
 			if (possible) {
 				if (validate) {
 				state = nextState;
 					displayVue(state);
 				}
 			} else {
 				TerminalController.getMainVue().showError("Impossible d'effectuer cette action!");
 			}
 		}
 		return possible;
 	}
 
 	private static void displayVue(int state) {
 		switch (state) {
 			case VUE_WELCOME:
 				//case VUE_END: (Same as welcome)
 				//TODO : Reinit all processed data
 				TerminalController.getMainVue().displayTerminalWelcome();
 				break;
 			case VUE_RENT:
 				TerminalController.getMainVue().displayTerminalRent();
				TerminalController.getMainVue().getTerminalRent().init();
 				break;
 			case VUE_PAY:
 				TerminalController.getMainVue().displayTerminalPay();
				TerminalController.getMainVue().getTerminalPay().init();
 				break;
 			case VUE_RETURN:
 				TerminalController.getMainVue().displayTerminalReturn();
 				break;
 			case VUE_RETURN_SUMMARY:
 				TerminalController.getMainVue().displayTerminalReturnSummary();
 				break;
 			default:
 				TerminalController.getMainVue().displayTerminalWelcome();
 		}
 	}
 }
