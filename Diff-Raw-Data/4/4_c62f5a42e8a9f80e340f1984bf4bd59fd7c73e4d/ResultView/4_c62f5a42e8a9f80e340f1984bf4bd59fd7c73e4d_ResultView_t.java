 package com.tung;
 
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.List;
 
 // TODO add totals per team
 // TODO separate view for home and visitor team, switch by single click instead of scrolling?
 public class ResultView extends List implements CommandListener {
 	
 	private static final String SCORE_SEPARATOR = "-";
 
 	private static final String HOME_DESC = "Home";
 
 	private static final String VISITOR_DESC = "Visit";
 
 	private Result result;
 	
 	private Command gotoSend = new Command("Send result", Command.SCREEN, 1);
 	
 	private Display display;
 
 	private Main main;
 	
 	public ResultView(Result result, Display display, Main main) {
 		super("Results", List.EXCLUSIVE);
 		
 		this.main = main;
 		
 		this.result = result;
 		this.display = display;
 
 		addCommand(gotoSend);
 		addCommand(main.editResult);
 		setCommandListener(this);
 	}
 	
 	public void refresh() {
 		
 		deleteAll();
 		
 		addPlayerResult(Result.HOME, 1);
 		addPlayerResult(Result.HOME, 2);
 		addPlayerResult(Result.HOME, 3);
 		addDoublesResult(Result.HOME);
 		addTotals(Result.HOME);
 		
 		addPlayerResult(Result.VISITOR, 1);
 		addPlayerResult(Result.VISITOR, 2);
 		addPlayerResult(Result.VISITOR, 3);
 		addDoublesResult(Result.VISITOR);
 		addTotals(Result.VISITOR);
 	}
 	
 	private void addPlayerResult(int team, int player) {
 		int[] entry = result.getSummary(team, player);
 		String prefix = Integer.toString(player) + "-- " + getDescription(team);
 		StringBuffer summary = new StringBuffer(prefix);
 		summary = addFormattedSummary(summary, entry);
 		
 		append(summary.toString(), null);
 	}
 
 	private StringBuffer addFormattedSummary(StringBuffer summary, int[] entry) {
 		summary.append(": ");
 		summary.append(entry[2]).append(SCORE_SEPARATOR).append(entry[3]);
 		summary.append(" | ");
 		summary.append(entry[0]).append(SCORE_SEPARATOR).append(entry[1]);
 		return summary;
 	}
 	
 	private void addDoublesResult(int team) {
 		int[] entry = result.getDoubleSets();
 		if (team == Result.VISITOR) {
 			entry = Result.invert(entry);
 		}
 		String prefix = "DBL " + getDescription(team); 
 		StringBuffer summary = new StringBuffer(prefix);
 		summary = addFormattedSummary(summary, entry);
 		
 		append(summary.toString(), null);
 	}
 	
 	private void addTotals(int team) {
 		int[] totals = result.getTotals();
 		if (team == Result.VISITOR) {
 			totals = Result.invert(totals);
 		}
 		String prefix = "== " + getDescription(team);
 		StringBuffer summary = new StringBuffer(prefix);
 		summary = addFormattedSummary(summary, totals);
 		
 		append(summary.toString(), null);
 		
 	}
 
 	private String getDescription(int team) {
 		return Result.HOME == team ? HOME_DESC : VISITOR_DESC;
 	}
 
 	public void commandAction(Command cmd, Displayable displayable) {
 		if (cmd == gotoSend) {
			display.setCurrent(main.resultSender);
 		} else if (cmd == main.editResult) {
 			display.setCurrent(main.enterResultForm);
 		}
 	}
 
 
 }
