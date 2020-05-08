 /*
  * Project: xdccBee
  * Copyright (C) 2009 snert@snert-lab.de,
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.snertlab.xdccBee.ui;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
 
import de.snertlab.xdccBee.irc.DccPacket;
 import de.snertlab.xdccBee.ui.actions.ActionDownloadFile;
 
 
 public class FileViewerMenu {
 	public FileViewerMenu (final FileViewer fileViewer) {
 		MenuManager contextMenu = new MenuManager();
 		final Action downloadAction = new ActionDownloadFile();
 		downloadAction.setEnabled(false);
 		contextMenu.add(downloadAction);
 		fileViewer.getControl().setMenu(contextMenu.createContextMenu(fileViewer.getControl()));
		fileViewer.getTable().addListener(SWT.Show, new Listener() {	
 			@Override
			public void handleEvent(Event event) {
				if(getSelectedFileItem(fileViewer) == null) {
					downloadAction.setEnabled(false);
				} else {
					downloadAction.setEnabled(true);
 				}
			}			
 		});
	}
	public DccPacket getSelectedFileItem(FileViewer packetViewer) {
		IStructuredSelection selectedItem = (IStructuredSelection) packetViewer.getSelection();
		return (DccPacket)selectedItem.getFirstElement();
	}
 }
