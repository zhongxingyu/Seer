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
 //	Created Date :			2011-08-23
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis.sam.gui;
 
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
 
 public class RelationViewer implements Updatable {
 	private final Shell myShell;
 	private final ResultsTable myTable;
 	private final LiveResults myResults;
 	private final IPredicate myPred;
 
 	public RelationViewer(Shell parent, final LiveResults results, final IPredicate pred) throws Exception {
 		ITuple args = results.getResults().model.declared.get(pred);
 
		myShell = new Shell(parent, SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.TITLE);
 		myShell.setText(pred.toString() + args);
 		myResults = results;
 		myPred = pred;
 
 		String[] headings = new String[args.size()];
 		for (int i = 0; i < headings.length; i++) {
 			headings[i] = args.get(i).toString();
 		}
 		myTable = new ResultsTable(myShell, headings, new RowViewer() {
 			public void openRow(ITuple row) throws Exception {
 				ILiteral lit = BASIC.createLiteral(true, pred, row);
 				new DebugViewer(myShell, results, lit);
 			}
 		});
 
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
 		myTable.getTable().setLayoutData(tableLayoutData);
 
 		update();
 
 		myShell.setLayout(gridLayout);
 
 		myShell.open();
 	}
 
 	public void update() throws Exception {
 		if (myShell.isDisposed()) {
 			return;
 		}
 
 		//System.out.println("refresh " + myShell.getText());
 
 		Results results = myResults.getResults();
 
 		ITuple args = results.model.declared.get(myPred);
 		ILiteral lit = BASIC.createLiteral(true, myPred, args);
 		IQuery query = BASIC.createQuery(lit);
 		IRelation rel = results.finalKnowledgeBase.execute(query);
 		myTable.fillTable(rel);
 
 		myResults.whenUpdated(this);
 	}
 }
