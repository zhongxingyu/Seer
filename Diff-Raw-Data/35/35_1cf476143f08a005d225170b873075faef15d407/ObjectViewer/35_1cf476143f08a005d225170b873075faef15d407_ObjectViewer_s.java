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
 
 import java.util.LinkedList;
 import java.util.List;
 import org.deri.iris.api.terms.IVariable;
 import org.deri.iris.api.terms.ITerm;
 import eu.serscis.sam.Constants;
 import org.deri.iris.api.basics.IQuery;
 import org.deri.iris.api.basics.ILiteral;
 import org.deri.iris.storage.IRelation;
 import org.deri.iris.api.basics.ITuple;
 import org.deri.iris.api.basics.IPredicate;
 import java.util.Arrays;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import eu.serscis.sam.Graph;
 import eu.serscis.sam.Eval;
 import eu.serscis.sam.Results;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.File;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.layout.RowLayout;
 import static org.deri.iris.factory.Factory.*;
 
 public class ObjectViewer implements Updatable {
 	private final String myName;
 	private final LiveResults myResults;
 	private final Shell myShell;
 
 	private ResultsTable myLocals;
 	private ResultsTable myFields;
 	private ResultsTable myCalled;
 	private ResultsTable myWasCalled;
 
 	public ObjectViewer(Shell parent, final LiveResults results, final String name) throws Exception {
		myShell = new Shell(parent, SWT.RESIZE);
 		myShell.setText(name);
 
 		myName = name;
 		myResults = results;
 
 		TabFolder folder = new TabFolder(myShell, 0);
 
 		TabItem fieldsTab = new TabItem(folder, 0);
 		fieldsTab.setText("Fields");
 		myFields = new ResultsTable(folder, new String[] {"Field", "Value"}, new RowViewer() {
 			public void openRow(ITuple row) throws Exception {
 				ILiteral lit = BASIC.createLiteral(true, Constants.fieldP,
 					BASIC.createTuple(TERM.createString(name), row.get(0), row.get(1)));
 				new DebugViewer(myShell, results, lit);
 			}
 		});
 		fieldsTab.setControl(myFields.getTable());
 
 		TabItem localsTab = new TabItem(folder, 0);
 		localsTab.setText("Local variables");
 		myLocals = new ResultsTable(folder, new String[] {"Invocation", "Var name", "Value"}, new RowViewer() {
 			public void openRow(ITuple row) throws Exception {
 				ILiteral lit = BASIC.createLiteral(true, Constants.localP,
 					BASIC.createTuple(TERM.createString(name), row.get(0), row.get(1), row.get(2)));
 				new DebugViewer(myShell, results, lit);
 			}
 		});
 		localsTab.setControl(myLocals.getTable());
 
 		TabItem wasCalledTab = new TabItem(folder, 0);
 		wasCalledTab.setText("Was called");
 		myWasCalled = new ResultsTable(folder, new String[] {"Caller", "Caller invocation", "Call-site", "Target invocation", "Method"}, new RowViewer() {
 			public void openRow(ITuple row) throws Exception {
 				ILiteral lit = BASIC.createLiteral(true, Constants.didCallP,
 					BASIC.createTuple(row.get(0), row.get(1), row.get(2), TERM.createString(name), row.get(3), row.get(4)));
 				new DebugViewer(myShell, results, lit);
 			}
 		});
 		wasCalledTab.setControl(myWasCalled.getTable());
 
 		TabItem calledTab = new TabItem(folder, 0);
 		calledTab.setText("Called");
 		myCalled = new ResultsTable(folder, new String[] {"Caller invocation", "Call-site", "Target", "Target invocation", "Method"}, new RowViewer() {
 			public void openRow(ITuple row) throws Exception {
 				ILiteral lit = BASIC.createLiteral(true, Constants.didCallP,
 					BASIC.createTuple(TERM.createString(name), row.get(0), row.get(1), row.get(2), row.get(3), row.get(4)));
 				new DebugViewer(myShell, results, lit);
 			}
 		});
 		calledTab.setControl(myCalled.getTable());
 
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 1;
 		gridLayout.marginHeight = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.verticalSpacing = 0;
 
 		GridData layoutData = new GridData();
 		layoutData.horizontalAlignment = GridData.FILL;
 		layoutData.verticalAlignment = GridData.FILL;
 		layoutData.grabExcessHorizontalSpace = true;
 		layoutData.grabExcessVerticalSpace = true;
 		folder.setLayoutData(layoutData);
 
 		update();
 
 		myShell.setLayout(gridLayout);
 
 		myShell.open();
 	}
 
 	public void update() throws Exception {
 		if (myShell.isDisposed()) {
 			return;
 		}
 
 		//System.out.println("refresh " + myName);
 
 		populateFields();
 		populateLocals();
 		populateWasCalled();
 		populateCalled();
 
 		myResults.whenUpdated(this);
 	}
 
 	private void populateLocals() throws Exception {
 		ITuple args = BASIC.createTuple(
 				TERM.createString(myName),
 				TERM.createVariable("Invocation"),
 				TERM.createVariable("VarName"),
 				TERM.createVariable("Value"));
 
 		ILiteral lit = BASIC.createLiteral(true, Constants.localP, args);
 		IQuery query = BASIC.createQuery(lit);
 		List<IVariable> bindings = new LinkedList<IVariable>();
 		IRelation rel = myResults.getResults().finalKnowledgeBase.execute(query, bindings);
 
 		myLocals.fillTable(rel);
 	}
 
 	private void populateFields() throws Exception {
 		ITuple args = BASIC.createTuple(
 				TERM.createString(myName),
 				TERM.createVariable("FieldName"),
 				TERM.createVariable("Value"));
 
 		ILiteral lit = BASIC.createLiteral(true, Constants.fieldP, args);
 		IQuery query = BASIC.createQuery(lit);
 		List<IVariable> bindings = new LinkedList<IVariable>();
 		IRelation rel = myResults.getResults().finalKnowledgeBase.execute(query, bindings);
 
 		myFields.fillTable(rel);
 	}
 
 	private void populateWasCalled() throws Exception {
 		ITuple args = BASIC.createTuple(
 				TERM.createVariable("Caller"),
 				TERM.createVariable("CallerInvocation"),
 				TERM.createVariable("CallSite"),
 				TERM.createString(myName),
 				TERM.createVariable("TargetInvocation"),
 				TERM.createVariable("Method"));
 
 		ILiteral lit = BASIC.createLiteral(true, Constants.didCallP, args);
 		IQuery query = BASIC.createQuery(lit);
 		List<IVariable> bindings = new LinkedList<IVariable>();
 		IRelation rel = myResults.getResults().finalKnowledgeBase.execute(query, bindings);
 
 		myWasCalled.fillTable(rel);
 	}
 
 	private void populateCalled() throws Exception {
 		ITuple args = BASIC.createTuple(
 				TERM.createString(myName),
 				TERM.createVariable("CallerInvocation"),
 				TERM.createVariable("CallSite"),
 				TERM.createVariable("Target"),
 				TERM.createVariable("TargetInvocation"),
 				TERM.createVariable("Method"));
 
 		ILiteral lit = BASIC.createLiteral(true, Constants.didCallP, args);
 		IQuery query = BASIC.createQuery(lit);
 		List<IVariable> bindings = new LinkedList<IVariable>();
 		IRelation rel = myResults.getResults().finalKnowledgeBase.execute(query, bindings);
 
 		myCalled.fillTable(rel);
 	}
 }
