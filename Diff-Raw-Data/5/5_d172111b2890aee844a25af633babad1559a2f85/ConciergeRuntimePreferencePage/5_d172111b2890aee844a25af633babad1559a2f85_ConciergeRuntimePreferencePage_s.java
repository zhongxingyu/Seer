 /* Copyright (c) 2007 Bug Labs, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.buglabs.osgi.concierge.ui.preferences;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.ListEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 import com.buglabs.osgi.concierge.runtime.ConciergeRuntime;
 import com.buglabs.osgi.concierge.ui.Activator;
 
 /**
  * A peference page for modifying the Concierge launch configuration.
  * @author kgilmer
  *
  */
 public class ConciergeRuntimePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
 
 	public ConciergeRuntimePreferencePage() {
 		super(GRID);
 		setPreferenceStore(Activator.getDefault().getPreferenceStore());
 	}
 
 	public void createFieldEditors() {
 		addField(new RuntimeJarListEditor(PreferenceConstants.P_JARS, "&Concierge Bundles", getFieldEditorParent(), ConciergeRuntime
 				.getDefault().getConciergeJars()));
 		
 		new Label(getFieldEditorParent(), SWT.NONE).setText("Note: Jar order has no effect on launch.\n\nWarning, changes may result in a non-working launch configuration.");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
 	 */
 	public void init(IWorkbench workbench) {
 	}
 
 	private class RuntimeJarListEditor extends ListEditor {
 
 		private final List existingJars;
 
 		public RuntimeJarListEditor(String name, String label, Composite parent, List existingJars) {
 			super(name, label, parent);
 			this.existingJars = existingJars;
 		}
 
 		protected String createList(String[] items) {
 			List newJarList = Arrays.asList(items);
 
 			for (Iterator i = existingJars.iterator(); i.hasNext();) {
 				File jar = (File) i.next();
 
 				if (!newJarList.contains(jar.getName())) {
 					if (jar.exists() && jar.isFile()) {
 						jar.delete();
 					}
 				}
 			}
 
 			return "";
 		}
 
 		protected String getNewInputObject() {
 			try {
 				FileDialog fd = new FileDialog(getShell());
 
 				fd.setFilterExtensions(new String[] { "*.jar" });
 				fd.setText("Add Library");
 
 				String path = fd.open();
 
 				if (path != null && path.length() > 0) {
 					File inFile = new File(path);
 
 					if (inFile.exists() && inFile.isFile()) {
 						File outFile = new File(ConciergeRuntime.getDefault().getJarsLocation() + File.separator + inFile.getName());
 
 						if (!outFile.exists()) {
 							copyFile(inFile, outFile);
 
 							return outFile.getName();
 						} else {
 							MessageDialog.openError(getShell(), "Error", "Library " + outFile.getAbsolutePath() + " already exists.");
 						}
 
 					} else {
 						MessageDialog.openError(getShell(), "Error", "Library " + inFile.getAbsolutePath() + " is not valid.");
 					}
 				}
 			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Error", "An error occured while adding library: " + e.getMessage());
 			} catch (URISyntaxException e) {
				MessageDialog.openError(getShell(), "Error", "An error occured while adding library: " + e.getMessage());
 			}
 
 			return null;
 		}
 
 		protected String[] parseString(String stringList) {
 			List fileNames = new ArrayList();
 
 			for (Iterator i = existingJars.iterator(); i.hasNext();) {
 				File jar = (File) i.next();
 
 				fileNames.add(jar.getName());
 			}
 
 			return (String[]) fileNames.toArray(new String[fileNames.size()]);
 
 		}
 
 		public void copyFile(File in, File out) throws IOException {
 			FileInputStream fis = new FileInputStream(in);
 			FileOutputStream fos = new FileOutputStream(out);
 			byte[] buf = new byte[1024];
 			int i = 0;
 			while ((i = fis.read(buf)) != -1) {
 				fos.write(buf, 0, i);
 			}
 			fis.close();
 			fos.close();
 		}
 	}
 }
