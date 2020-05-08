 /////////////////////////////////////////////////////////////////////////
 //
 // © University of Southampton IT Innovation Centre, 2011
 //
 // Copyright in this library belongs to the University of Southampton
 // University Road, Highfield, Southampton, UK, SO17 1BJ
 //
 // This software may not be used, sold, licensed, transferred, copied
 // or reproduced in whole or in part in any manner or form or in or
 // on any media by any person other than in accordance with the terms
 // of the Licence Agreement supplied with the software, or otherwise
 // without the prior written consent of the copyright owners.
 //
 // This software is distributed WITHOUT ANY WARRANTY, without even the
 // implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 // PURPOSE, except where stated in the Licence Agreement supplied with
 // the software.
 //
 //	Created By :			Thomas Leonard
 //	Created Date :			2011-11-03
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis.sam.gui;
 
 import eu.serscis.sam.AnyTerm;
 import org.deri.iris.api.terms.ITerm;
 import org.deri.iris.api.IKnowledgeBase;
 import org.deri.iris.api.basics.IAtom;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import java.util.HashMap;
 import java.util.Map;
 import org.deri.iris.api.basics.IRule;
 import eu.serscis.sam.Reporter;
 import eu.serscis.sam.Model;
 import eu.serscis.sam.Constants;
 import org.deri.iris.api.basics.IQuery;
 import org.deri.iris.api.basics.ILiteral;
 import org.deri.iris.storage.IRelation;
 import org.deri.iris.api.basics.ITuple;
 import org.deri.iris.api.basics.IPredicate;
 import java.util.Arrays;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.layout.RowLayout;
 import static org.deri.iris.factory.Factory.*;
 import eu.serscis.sam.Debugger;
 
 public class DebugViewer implements Updatable {
 	private final Shell myShell;
 	private final Tree myTree;
 	private final Text myText;
 	private final LiveResults myResults;
 	private final IQuery myProblem;
 	private final Map<TreeItem,Details> extraData = new HashMap<TreeItem,Details>();
 	private final Color PRETTY_COLOUR = new Color(Display.getCurrent(), 0, 50, 150);
 	private final Color GREY = new Color(Display.getCurrent(), 100, 100, 100);
 	private boolean accessControlOn;
 
 	public DebugViewer(final Shell parent, final LiveResults results, ILiteral problem) throws Exception {
 		this(parent, results, BASIC.createQuery(problem));
 	}
 
 	public DebugViewer(final Shell parent, final LiveResults results, IQuery problem) throws Exception {
 		myShell = new Shell(parent, SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.TITLE);
 		myShell.setText("Debug: " + problem);
 		myResults = results;
 		myProblem = problem;
 
 		myTree = new Tree(myShell, 0);
 		myText = new Text(myShell, SWT.MULTI | SWT.READ_ONLY);
 
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 1;
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.verticalSpacing = 0;
 
 		GridData tableLayoutData = new GridData();
 		tableLayoutData.horizontalAlignment = GridData.FILL;
 		tableLayoutData.verticalAlignment = GridData.FILL;
 		tableLayoutData.grabExcessHorizontalSpace = true;
 		tableLayoutData.grabExcessVerticalSpace = true;
 		myTree.setLayoutData(tableLayoutData);
 
 		GridData textLayoutData = new GridData();
 		textLayoutData.horizontalAlignment = GridData.FILL;
 		textLayoutData.verticalAlignment = GridData.FILL;
 		textLayoutData.grabExcessHorizontalSpace = true;
 		textLayoutData.grabExcessVerticalSpace = false;
 		myText.setLayoutData(textLayoutData);
 
 		myTree.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				TreeItem item = (TreeItem) e.item;
 				Details details = extraData.get(item);
 				String msg = details != null ? details.notes : "";
 				if (details.negativeQuery != null) {
 					msg += details.negativeQuery;
 				}
 				myText.setText(msg);
 				myShell.layout();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				TreeItem item = (TreeItem) e.item;
 				Details details = extraData.get(item);
 				if (details != null) {
 					IQuery query = null;
 					if (details.lit != null) {
 						query = BASIC.createQuery(details.lit);
 					} else if (details.negativeQuery != null) {
 						query = details.negativeQuery;
 					}
 					if (query != null) {
 						try {
 							new DebugViewer(parent, results, query);
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 					}
 				}
 			}
 		});
 
 		myShell.setLayout(gridLayout);
 
 		myShell.open();
 
 		update();
 	}
 
 	public void update() throws Exception {
 		if (myShell.isDisposed()) {
 			return;
 		}
 
 		//System.out.println("refresh " + myShell.getText());
 
 		myTree.removeAll();
 
 		Model model = myResults.getResults().model;
 
 		accessControlOn = checkAccessControl(model);
 
 		GUIReporter reporter = new GUIReporter();
 		Debugger debugger = new Debugger(model);
 		debugger.debug(myProblem, reporter);
 
 		Display.getCurrent().timerExec(0, new Runnable() {
 			public void run() {
 				for (TreeItem item : myTree.getItems()) {
 					item.setExpanded(true);
 					for (TreeItem child : item.getItems()) {
 						child.setExpanded(true);
 					}
 				}
 			}
 		});
 
 		myResults.whenUpdated(this);
 	}
 
 	private boolean checkAccessControl(Model model) throws Exception {
 		IKnowledgeBase finalKnowledgeBase = myResults.getResults().finalKnowledgeBase;
 		if (finalKnowledgeBase == null) {
 			return false;
 		}
 		ILiteral lit = BASIC.createLiteral(true, Constants.accessControlOnP, BASIC.createTuple());
 		IQuery query = BASIC.createQuery(lit);
 		IRelation rel = finalKnowledgeBase.execute(query);
 		return rel.size() > 0;
 	}
 
 	private static String getInvocation(ITuple tuple, int i) {
 		String object = tuple.get(i).getValue().toString();
 		if ("_testDriver".equals(object)) {
 			return "config";
 		}
 		return object;
 	}
 
 	/* Produce a friendly description of literal */
 	private String getPrettyText(ILiteral literal) {
 		if (!literal.isPositive()) {
 			return null;
 		}
 
 		IAtom atom = literal.getAtom();
 		ITuple tuple = atom.getTuple();
 		IPredicate p = atom.getPredicate();
 
 		if (p.getPredicateSymbol().equals("didReceive")) {
 			String target = tuple.get(0).getValue().toString();
 			String invocation = tuple.get(1).getValue().toString();
 			String method = tuple.get(2).getValue().toString().split("\\.", 2)[1];
 			ITerm arg;
 			if (tuple.size() == 4) {
 				arg = tuple.get(3);
 			} else {
 				arg = tuple.get(4);
 			}
 			String msg = "<" + target + ">." + method + "() received argument " + arg;
 			if (!invocation.equals("")) {
 				msg += " [" + invocation + "]";
 			}
 			return msg;
 		} else if (p.equals(Constants.mayStoreP)) {
 			String callSite = tuple.get(0).getValue().toString();
 			String target = tuple.get(1).getValue().toString();
 			return "(" + callSite + " may store result in " + target + ")";
 		} else if (p.equals(Constants.mayReturnP)) {
 			String object = tuple.get(0).getValue().toString();
 			String invocation = tuple.get(1).getValue().toString();
 			String[] method = tuple.get(2).getValue().toString().split("\\.");
 			ITerm value = tuple.get(3);
 			String msg = "<" + object + ">." + method[1] + " returned " + value;
 			if (!invocation.equals("")) {
 				msg += " [" + invocation + "]";
 			}
 			return msg;
 		} else if (p.equals(Constants.didCall3P)) {
 			String caller = tuple.get(0).getValue().toString();
 			String target = tuple.get(1).getValue().toString();
 			String method = tuple.get(2).getValue().toString();
 			int i = method.indexOf('.');
 			method = method.substring(i + 1);
 			return "<" + caller + "> called <" + target + ">." + method + "()";
 		} else if (p.equals(Constants.didCallP)) {
 			String caller = getInvocation(tuple, 0);
 			String[] callSite = tuple.get(2).getValue().toString().split("\\.", 2);
 			String target = getInvocation(tuple, 3);
 			String method = tuple.get(5).getValue().toString();
 
 			//String method = tuple.get(4).getValue().toString();
 			//String arg = tuple.get(5).getValue().toString();
 			//String result = tuple.get(6).getValue().toString();
 			//msg = caller + "@" + callSite + " calls " + target + "." + method;
 			int i = method.indexOf('.');
 			method = method.substring(i + 1);
 			String msg = "<" + caller + ">." + callSite[1] + " called <" + target + ">." + method + "()";
 
 			String callerInvocation = tuple.get(1).getValue().toString();
 			String targetInvocation = tuple.get(4).getValue().toString();
 
 			if (callerInvocation.equals("") && targetInvocation.equals("")) {
 				return msg;
 			} else if (callerInvocation.equals(targetInvocation)) {
 				return msg + " [" + callerInvocation + "]";
 			} else {
 				return msg + " [" + callerInvocation + " -> " + targetInvocation + "]";
 			}
 		} else if (p.equals(Constants.maySend5P)) {
 			String caller = getInvocation(tuple, 0);
 			String invocation = tuple.get(1).getValue().toString();
 			String callSite = tuple.get(2).getValue().toString();
 			ITerm arg = tuple.get(4);
 			String msg = "<" + caller + "> passed " + arg;
 			ITerm pos = tuple.get(3);
 			if (!(pos instanceof AnyTerm)) {
 				msg += " as argument " + pos;
 			} else {
 				msg += " as an argument";
 			}
 			if (!invocation.equals("")) {
 				msg += " [" + invocation + "]";
 			}
 			return msg;
 		} else if (p.equals(Constants.didGetP)) {
 			String caller = getInvocation(tuple, 0);
 			String callSite = tuple.get(2).getValue().toString();
 			ITerm result = tuple.get(3);
 			return "<" + caller + "> got " + result;
 		} else if (p.equals(Constants.didGetExceptionP)) {
 			String caller = getInvocation(tuple, 0);
 			String callSite = tuple.get(2).getValue().toString();
 			ITerm result = tuple.get(3);
 			return "<" + caller + "> got exception " + result;
 		} else if (p.equals(Constants.liveMethodP)) {
 			String object = tuple.get(0).getValue().toString();
 			String invocation = tuple.get(1).getValue().toString();
 			String[] method = tuple.get(2).getValue().toString().split("\\.");
 			String msg = "<" + object + ">." + method[1] + "() ran";
 			if (!invocation.equals("")) {
 				msg += " [" + invocation + "]";
 			}
 			return msg;
 		} else if (p.equals(Constants.isAP)) {
 			String name = tuple.get(0).getValue().toString();
 			String type = tuple.get(1).getValue().toString();
 			return type + " <" + name + "> exists";
 		} else if (p.equals(Constants.accessAllowedP)) {
 			if (!accessControlOn) {
 				return null;		// Not interesting then
 			}
 			String caller = tuple.get(0).getValue().toString();
 			String target = tuple.get(1).getValue().toString();
 			String[] method = tuple.get(2).getValue().toString().split("\\.");
 			return "Access control: <" + caller + "> may call <" + target + ">." + method[1];
 		} else if (p.equals(Constants.fieldP)) {
 			String actor = tuple.get(0).getValue().toString();
 			String name = tuple.get(1).getValue().toString();
 			ITerm value = tuple.get(2);
 			return "<" + actor + ">." + name + " = " + value;
 		} else if (p.equals(Constants.localP)) {
 			String actor = getInvocation(tuple, 0);
 			String invocation = tuple.get(1).getValue().toString();
 			String name = tuple.get(2).getValue().toString();
 			if (name.equals("this")) {
 				return null;
 			}
 			String[] nameParts = name.split("\\.");
 			ITerm value = tuple.get(3);
 			String msg = "<" + actor + ">." + nameParts[1] + "()'s " + nameParts[2] + " = " + value;
 			if (!invocation.equals("")) {
 				msg += " [" + invocation + "]";
 			}
 			return msg;
 		} else if (p.equals(Constants.mayCallObjectP)) {
 			String actor = getInvocation(tuple, 0);
 			String invocation = tuple.get(1).getValue().toString();
			String[] callSite = tuple.get(2).getValue().toString().split("\\.", 2);
 			String target = tuple.get(3).getValue().toString();
			String msg = "<" + actor + ">." + callSite[1] + " tries to call <" + target + ">";
 			if (!invocation.equals("")) {
 				msg += " [" + invocation + "]";
 			}
 			return msg;
 		} else if (p.equals(Constants.didCreateP)) {
 			String actor = getInvocation(tuple, 0);
 			//String resultVar = tuple.get(2).getValue().toString();
 			String child = tuple.get(3).getValue().toString();
 			return "<" + actor + "> created <" + child + ">";
 		}
 
 		return null;
 	}
 
 	private class GUIReporter implements Reporter {
 		private ILiteral optNegativeNeedHeader;
 		private TreeItem currentItem;
 
 		public void enter(ILiteral literal) {
 			//System.out.println("enter " + literal + ", " + currentItem);
 			TreeItem item;
 			if (currentItem == null) {
 				item = new TreeItem(myTree, 0);
 			} else {
 				item = new TreeItem(currentItem, 0);
 			}
 
 			Details details = new Details(literal);
 			String prettyText = getPrettyText(literal);
 			if (prettyText == null) {
 				item.setText("" + literal);
 				item.setForeground(GREY);
 			} else {
 				item.setText(prettyText);
 				item.setForeground(PRETTY_COLOUR);
 				FontData fontData = item.getFont().getFontData()[0];
 				item.setFont(new Font(Display.getCurrent(), fontData.getName(), fontData.getHeight(), SWT.BOLD));
 				details.notes += literal + "\n";
 			}
 
 			extraData.put(item, details);
 
 			currentItem = item;
 		}
 
 		public void leave(ILiteral literal) {
 			currentItem = currentItem.getParentItem();
 		}
 
 		public void noteNewProblem(ILiteral problem) {
 		}
 
 		public void noteQuery(IQuery ruleQ) {
 			if (currentItem != null) {
 				String msg = "Rule body:\n";
 				for (ILiteral literal : ruleQ.getLiterals()) {
 					msg += literal + "\n";
 				}
 				extraData.get(currentItem).notes += msg;
 			}
 		}
 
 
 		/* We are about to explain why literal is false. */
 		public void enterNegative(ILiteral literal) {
 			optNegativeNeedHeader = literal;
 			if (currentItem == null) {
 				currentItem = new TreeItem(myTree, 0);
 			} else {
 				currentItem = new TreeItem(currentItem, 0);
 			}
 			currentItem.setForeground(GREY);
 			currentItem.setText("" + literal + "; none of these was true:");
 			extraData.put(currentItem, new Details(literal));
 		}
 
 
 		public void leaveNegative() {
 			if (optNegativeNeedHeader != null) {
 				currentItem.setText("" + optNegativeNeedHeader + "; no rules for this predicate");
 			}
 			currentItem = currentItem.getParentItem();
 		}
 
 
 		/* This rule might have been intended to fire, but there was no match. */
 		public void noteNegative(IRule rule, IQuery unified) {
 			optNegativeNeedHeader = null;
 
 			TreeItem ruleItem = new TreeItem(currentItem, 0);
 			ruleItem.setText("" + rule);
 			ruleItem.setForeground(GREY);
 
 			Details details = new Details(null);
 			details.negativeQuery = unified;
 
 			extraData.put(ruleItem, details);
 		}
 	}
 
 	private class Details {
 		private IQuery negativeQuery;
 		private ILiteral lit;
 		private String notes = "";
 
 		private Details(ILiteral lit) {
 			this.lit = lit;
 		}
 	}
 }
