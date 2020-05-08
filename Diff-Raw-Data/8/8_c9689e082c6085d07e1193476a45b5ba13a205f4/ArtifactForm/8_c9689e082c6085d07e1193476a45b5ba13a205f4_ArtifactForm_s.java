 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.web.client.registry;
 
 import org.mule.galaxy.web.client.AbstractErrorShowingComposite;
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.client.artifact.ArtifactPolicyResultsPanel;
 import org.mule.galaxy.web.client.util.WorkspacesListBox;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FileUpload;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.FormHandler;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
 import com.google.gwt.user.client.ui.FormSubmitEvent;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class ArtifactForm extends AbstractErrorShowingComposite {
     private TextBox nameBox;
     private FlexTable table;
     private FormPanel form;
     private FileUpload artifactUpload;
     private TextBox versionBox;
     private WorkspacesListBox workspacesLB;
     private final Galaxy galaxy;
     private String artifactId;
     private CheckBox disablePrevious;
     private boolean add;
     private Button addButton;
 
     public ArtifactForm(final Galaxy galaxy) {
         this.galaxy = galaxy;
 
         RegistryMenuPanel menuPanel = new RegistryMenuPanel();
         form = new FormPanel();
         menuPanel.setMain(form);
         
         initWidget(menuPanel);
     }
 
     public void onHide() {
         form.clear();
     }
 
     public void onShow(List params) {
         if (params.size() > 0) {
             artifactId = (String) params.get(0);
         } else {
             add = true;
         }
         
         form.setAction(GWT.getModuleBaseURL() + "../artifactUpload");
         form.setEncoding(FormPanel.ENCODING_MULTIPART);
         form.setMethod(FormPanel.METHOD_POST);
 
         FlowPanel panel = new FlowPanel();
         form.add(panel);
 
         panel.add(createTitle("Add Artifact"));
         
         table = createColumnTable();
         panel.add(table);
         
         if (add) {
             setupAddForm();
         } else {
             setupAddVersionForm(panel);
         }
     }
 
     private void setupRemainingTable(int row) {
         artifactUpload = new FileUpload();
         artifactUpload.setName("artifactFile");
         table.setWidget(row, 1, artifactUpload);
 
         addButton = new Button("Add");
         addButton.addClickListener(new ClickListener() {
             public void onClick(Widget sender) {
                 addButton.setText("Uploading...");
                 addButton.setEnabled(false);
                 form.submit();
             }
         });
         table.setWidget(row+1, 1, addButton);
 
         form.addFormHandler(new FormHandler() {
             public void onSubmit(FormSubmitEvent event) {
                 if (artifactUpload.getFilename().length() == 0) {
                     Window.alert("You did not specify a filename!");
                     event.setCancelled(true);
                 }
                 
                 String version = versionBox.getText();
                 if (version == null || "".equals(version)) {
                     setMessage("You must specify a version label.");
                     event.setCancelled(true);
                 }
                 
                 addButton.setText("Add");
                 addButton.setEnabled(true);
             }
 
             public void onSubmitComplete(FormSubmitCompleteEvent event) {
                 String msg = event.getResults();
                 if (msg.startsWith("<PRE>OK ") || msg.startsWith("<pre>OK ")) {
                     int last = msg.indexOf("</PRE>");
                     if (last == -1) last = msg.indexOf("</pre>");
                     if (last == -1) last = msg.length();
                     
                     String artifactId2 = artifactId;
                     if (add) {
                         artifactId2 = msg.substring(8, last);
                     }
                     
                     History.newItem("artifact/" + artifactId2);
                 } else if (msg.startsWith("<PRE>ArtifactPolicyException") || msg.startsWith("<pre>ArtifactPolicyException")) {
                     parseAndShowPolicyMessages(msg);
                 } else {
                     setMessage(msg);
                 }
             }
         });
         
         styleHeaderColumn(table);
 
         if (add) {
             setTitle("Add Artifact");
         } else {
             setTitle("Add New Artifact Version");
         }
     }
 
     protected void parseAndShowPolicyMessages(String msg) {
         String[] split = msg.split("\n");
         
         List warnings = new ArrayList();
         List failures = new ArrayList();
         for (int i = 1; i < split.length; i++) {
             String s = split[i];
             
             if (s.startsWith("WARNING: ")) {
                 warnings.add(getMessage(s));
             } else if (s.startsWith("FAILURE: ")) {
                 failures.add(getMessage(s));
             }
         }
         
         String token = "policy-failures-" + artifactId;
         ArtifactPolicyResultsPanel failurePanel = new ArtifactPolicyResultsPanel(warnings, failures);
         failurePanel.setMessage("The artifact did not meet all the necessary policies!");
         galaxy.createPageInfo(token, failurePanel, 0);
         History.newItem(token);
     }
 
     private String getMessage(String s) {
         s = s.substring(9);
         if (s.endsWith("</PRE>") || s.endsWith("</pre>")) {
             s = s.substring(0, s.length() - 6);
         }
         return s;
     }
 
     private void setupAddForm() {
         galaxy.getRegistryService().getWorkspaces(new AbstractCallback(this) {
 
             public void onSuccess(Object workspaces) {
                 setupAddForm((Collection) workspaces);
             }
             
         });
     }
     
     private void setupAddForm(Collection workspaces) {
         table.setWidget(0, 0, new Label("Workspace"));
 
         workspacesLB = new WorkspacesListBox(workspaces,
                                              null,
                                              BrowsePanel.getLastWorkspaceId(),
                                              false);
         workspacesLB.setName("workspaceId");
         table.setWidget(0, 1, workspacesLB);
         
         Label nameLabel = new Label("Artifact Name");
         table.setWidget(1, 0, nameLabel);
 
         nameBox = new TextBox();
         nameBox.setName("name");
         table.setWidget(1, 1, nameBox);
 
         Label versionLabel = new Label("Version Label");
         table.setWidget(2, 0, versionLabel);
 
         versionBox = new TextBox();
         table.setWidget(2, 1, versionBox);
         versionBox.setName("versionLabel");
         
         Label artifactLabel = new Label("Artifact");
         table.setWidget(3, 0, artifactLabel);
         
         setupRemainingTable(3);
     }
 
     private void setupAddVersionForm(FlowPanel panel) {
         table.setText(0, 0, "Version Label");
 
         versionBox = new TextBox();
         table.setWidget(0, 1, versionBox);
         versionBox.setName("versionLabel");
         
         table.setText(1, 0, "Disable Previous");
 
         disablePrevious = new CheckBox();
         disablePrevious.setName("disablePrevious");
         table.setWidget(1, 1, disablePrevious);
         
         Label artifactLabel = new Label("Artifact");
         table.setWidget(2, 0, artifactLabel);
         
         panel.add(new Hidden("artifactId", artifactId));
 
         setupRemainingTable(2);
     }
 }
