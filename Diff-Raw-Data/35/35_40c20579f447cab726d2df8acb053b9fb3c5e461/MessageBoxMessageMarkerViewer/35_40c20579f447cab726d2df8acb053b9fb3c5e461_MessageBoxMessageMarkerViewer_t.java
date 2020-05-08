 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.marker;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import org.eclipse.riena.ui.core.marker.IMessageMarker;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IMessageBoxRidget;
 
 /**
  * Visualizes certain types of message markers by displaying the message in a
  * message box.
  */
 public class MessageBoxMessageMarkerViewer extends AbstractMessageMarkerViewer {
 
 	private IMessageBoxRidget messageBoxRidget;
 
 	public MessageBoxMessageMarkerViewer(IMessageBoxRidget messageBoxRidget) {
 		this.messageBoxRidget = messageBoxRidget;
 		setVisible(false);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.marker.AbstractMessageMarkerViewer#hideMessages(org.eclipse.riena.ui.ridgets.IMarkableRidget)
 	 */
 	@Override
 	protected void hideMessages(IMarkableRidget ridget) {
 		// automatically hidden when the message box is closed
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.marker.AbstractMessageMarkerViewer#showMessages(org.eclipse.riena.ui.ridgets.IMarkableRidget)
 	 */
 	@Override
 	protected void showMessages(IMarkableRidget ridget) {
 		if (isVisible()) {
 			String message = getMessage();
 			if (message.length() > 0) {
 				messageBoxRidget.setText(message);
 				messageBoxRidget.show();
 			}
 			setVisible(false);
 		}
 	}
 
 	/**
 	 * Construct a Message of all Adapter for display, remove the marker!
 	 * 
 	 * @return a complete Message
 	 */
 	private String getMessage() {
 		Collection<IMessageMarker> allMessageMarker = new LinkedHashSet<IMessageMarker>();
 		IMarkableRidget nextMarkableAdapter = null;
 		Collection<IMarkableRidget> localMarkableAdapter = new HashSet<IMarkableRidget>();
 		localMarkableAdapter.addAll(ridgets);
		for (Iterator<IMarkableRidget> i = localMarkableAdapter.iterator(); i.hasNext();) {
 			nextMarkableAdapter = (IMarkableRidget) i.next();
 			allMessageMarker.addAll(getMessageMarker(nextMarkableAdapter, false));
 		}
 		return constructMessage(allMessageMarker);
 	}
 
 	private String constructMessage(Collection<IMessageMarker> pMessageMarker) {
 		StringWriter sw = new StringWriter();
 		if (pMessageMarker != null) {
 			List<IMessageMarker> sortedMarkers = new ArrayList<IMessageMarker>(pMessageMarker);
 			Collections.sort(sortedMarkers, new MessageMarkerComparator());
 			for (IMessageMarker nextMarker : sortedMarkers) {
 				if (sw.toString().trim().length() > 0) {
 					sw.write("\n"); //$NON-NLS-1$
 				}
 				if (nextMarker.getMessage() != null) {
 					sw.write(nextMarker.getMessage());
 				}
 			}
 		}
 		return sw.toString().trim();
 	}
 
 }
