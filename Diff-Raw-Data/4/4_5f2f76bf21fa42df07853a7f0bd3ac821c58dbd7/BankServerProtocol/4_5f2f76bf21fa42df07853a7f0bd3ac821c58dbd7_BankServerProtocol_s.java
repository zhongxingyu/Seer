 import java.io.*;
 import java.net.*;
 
 public class BankServerProtocol {
 	private int PROTOCOL_VERSION = 1;
 
 	private static final int CREATE = 0x10;
 	private static final int DEPOSIT = 0x20;
 	private static final int WITHDRAW = 0x30;
 	private static final int GETBALANCE = 0x40;
 	private static final int CLOSE = 0x50;
 
 	private static final int MAX_ACCOUNTS = 1024;
 
 	private int[] balance;
 	private boolean[] accountState;
 
 	// number of accounts EVER created; does not decrement when account is closed
 	// we do not reclaim account numbers
 	private static int numAccounts;
 
 	public BankServerProtocol() {
 		balance = new int[MAX_ACCOUNTS];
 		accountState = new boolean[MAX_ACCOUNTS];
 	}
 
 	public BankMessage processInput(BankMessage inputMessage) {
 		BankMessage output = null;
 
 		int opcode = inputMessage.opcode;
 
 		int firstParam = inputMessage.parameters[0];
 		int secondParam = inputMessage.parameters[1];
 
 		switch (opcode) {
 		case CREATE:
 
 			// INVALID INITIAL DEPOSIT
 			if(firstParam < 0) { 
 				output = buildMessage(0x11);
 			}
 
 			// ACCOUNT NUMBERS EXHAUSTED
 			else if(numAccounts >= MAX_ACCOUNTS) {
 				output = buildMessage(0x12);
 			}
 
 			// SUCCESSFUL CREATION
 			else {
 				accountState[numAccounts] = true;
 				balance[numAccounts] = firstParam;
 
 				output = buildMessage(0x10, numAccounts);
 				numAccounts++;
 			}
 			break;
 
 		case DEPOSIT:
 			// INVALID ACCOUNT NUMBER
 			if(firstParam < 0 || firstParam >= MAX_ACCOUNTS || !accountState[firstParam]) {
 				output = buildMessage(0x21);
 			}
 
 			// INVALID DEPOSIT AMOUNT
 			else if(secondParam < 0) {
 				output = buildMessage(0x22);
 			}
 
 			// SUCCESSFUL DEPOSIT
 			else {
 				balance[firstParam] += secondParam;
 
 				output = buildMessage(0x20, balance[firstParam]);
 			}
 			break;
 
 		case WITHDRAW:
 			// INVALID ACCOUNT NUMBER
 			if(firstParam < 0 || firstParam >= MAX_ACCOUNTS || !accountState[firstParam]) {
 				output = buildMessage(0x31);
 			}
 
			// INVALID DEPOSIT AMOUNT
			else if(secondParam < 0) {
 				output = buildMessage(0x32);
 			}
 
 			// SUCCESSFUL DEPOSIT
 			else {
 				balance[firstParam] -= secondParam;
 				output = buildMessage(0x30, balance[firstParam]);
 			}
 			break;
 
 		case GETBALANCE:
 			// INVALID ACCOUNT NUMBER
 			if(firstParam < 0 || firstParam >= MAX_ACCOUNTS || !accountState[firstParam]) {
 				output = buildMessage(0x41);
 			}
 
 			// SUCCESSFUL BALANCE CHECK
 			else {
 				output = buildMessage(0x40, balance[firstParam]);
 			}
 			break;
 
 		case CLOSE:
 			// INVALID ACCOUNT NUMBER
 			if(firstParam < 0 || firstParam >= MAX_ACCOUNTS || !accountState[firstParam]) {
 				output = buildMessage(0x51);
 			}
 
 			// SUCCESSFUL ACCOUNT CLOSURE
 			else {
 				accountState[firstParam] = false;
 				output = buildMessage(0x50, firstParam);
 			}
 
 			break;
 
 		default:
 		}
 
 		return output;
 	}
 
 	private BankMessage buildMessage(int opcode) {
 		int[] parameters = new int[1];
 		parameters[0] = 0;
 
 		return new BankMessage(PROTOCOL_VERSION, 
 				opcode, 
 				404, 
 				parameters);
 	}
 
 
 	private BankMessage buildMessage(int opcode, int parameter) {
 		int[] parameters = new int[1];
 		parameters[0] = parameter;
 
 		return new BankMessage(PROTOCOL_VERSION, 
 				opcode, 
 				404, 
 				parameters);
 	}
 }
