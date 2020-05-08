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
 import java.util.List;
 
 import org.mule.galaxy.web.client.AbstractErrorShowingComposite;
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.client.util.WorkspaceOracle;
 import org.mule.galaxy.web.client.validation.StringNotEmptyValidator;
 import org.mule.galaxy.web.client.validation.ui.ValidatableSuggestBox;
 
 public class ArtifactForm extends AbstractErrorShowingComposite {
     private TextBox nameBox;
     private FlexTable table;
     private FormPanel form;
     private FileUpload artifactUpload;
     private TextBox versionBox;
     private final Galaxy galaxy;
     private String artifactId;
     private CheckBox disablePrevious;
     private boolean add;
     private Button addButton;
     private RegistryMenuPanel menuPanel;
     private ValidatableSuggestBox workspaceSB;
 
     public ArtifactForm(final Galaxy galaxy) {
         this.galaxy = galaxy;
 
         menuPanel = new RegistryMenuPanel(galaxy);
         form = new FormPanel();
         menuPanel.setMain(form);
 
         initWidget(menuPanel);
     }
 
     public void onHide() {
         form.clear();
     }
 
     public void onShow(List<String> params) {
         if (params.size() > 0) {
             artifactId = params.get(0);
            add = false;
         } else {
             add = true;
         }
 
         form.setAction(GWT.getModuleBaseURL() + "../artifactUpload.form");
         form.setEncoding(FormPanel.ENCODING_MULTIPART);
         form.setMethod(FormPanel.METHOD_POST);
 
         FlowPanel panel = new FlowPanel();
         form.add(panel);
 
         panel.add(createPrimaryTitle("Add Artifact"));
 
         table = createColumnTable();
         panel.add(table);
 
        if (add) {
             setupAddForm();
         } else {
             setupAddVersionForm(panel);
         }
         menuPanel.onShow();
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
 
         Button cancel = new Button("Cancel");
         cancel.addClickListener(new ClickListener() {
             public void onClick(final Widget widget) {
                 History.back();
             }
         });
 
         InlineFlowPanel buttons = new InlineFlowPanel();
         buttons.add(addButton);
         buttons.add(cancel);
 
         table.setWidget(row + 1, 1, buttons);
 
         form.addFormHandler(new FormHandler() {
             public void onSubmit(FormSubmitEvent event) {
 
                 // whitespace will throw an invalid path exception
                 // on the server -- so trim this optional value
                 if (nameBox != null) {
                     String name = nameBox.getText().trim();
                     if (name != null || !"".equals(name)) {
                         nameBox.setText(name);
                     }
                 }
 
                 if (artifactUpload.getFilename().length() == 0) {
                     Window.alert("You did not specify a filename!");
                     event.setCancelled(true);
                 }
 
                 // trim version to prevent path error
                 String version = versionBox.getText().trim();
                 versionBox.setText(version);
                 if (version == null || "".equals(version)) {
                     setMessage("You must specify a version label.");
                     event.setCancelled(true);
                 }
 
                 addButton.setText("Add");
                 addButton.setEnabled(true);
             }
 
             public void onSubmitComplete(FormSubmitCompleteEvent event) {
                 String msg = event.getResults();
 
                 // some platforms insert css info into the pre-tag -- just remove it all
                 msg = msg.replaceAll("\\<.*?\\>", "");
 
                 // This is our 200 OK response
                 // eg:  OK 9c495a52-4a07-4697-ba73-f94f95cd3020
                 if (msg.startsWith("OK ")) {
                     String artifactId2 = artifactId;
                     if (add) {
                         // remove the "OK " string to get the artifactId
                         artifactId2 = msg.substring(3);
                     }
                     // send them to the view artifact info page on success.
                     History.newItem("artifact/" + artifactId2);
                 } else
 
                     // something bad happened...
                     if (msg.startsWith("ArtifactPolicyException")) {
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
 
         List<String> warnings = new ArrayList<String>();
         List<String> failures = new ArrayList<String>();
         String lines = null;
         boolean warning = true;
         for (int i = 1; i < split.length; i++) {
             String s = split[i];
 
             if (s.startsWith("WARNING: ")) {
                 addWarningOrFailure(warnings, failures, lines, warning);
 
                 warning = true;
                 lines = getMessage(s);
             } else if (s.startsWith("FAILURE: ")) {
                 addWarningOrFailure(warnings, failures, lines, warning);
 
                 warning = false;
                 lines = getMessage(s);
             } else {
                 lines += s;
             }
         }
 
         addWarningOrFailure(warnings, failures, lines, warning);
 
         String token = "policy-failures";
         if (artifactId != null) {
             token += "-" + artifactId;
         }
         PolicyResultsPanel failurePanel = new PolicyResultsPanel(galaxy, warnings, failures);
         failurePanel.setMessage("The artifact did not meet all the necessary policies!");
         galaxy.createPageInfo(token, failurePanel, 0);
         History.newItem(token);
     }
 
     private void addWarningOrFailure(List<String> warnings, List<String> failures, String lines, boolean warning) {
         if (lines == null) return;
 
         if (warning) {
             warnings.add(lines);
         } else {
             failures.add(lines);
         }
     }
 
     private String getMessage(String s) {
         s = s.substring(9);
         return s;
     }
 
     private void setupAddForm() {
         table.setWidget(0, 0, new Label("Workspace"));
 
         workspaceSB = new ValidatableSuggestBox(new StringNotEmptyValidator(),
                                                 new WorkspaceOracle(galaxy, this));
         table.setWidget(0, 1, workspaceSB);
 
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
         disablePrevious.setChecked(true);
         disablePrevious.setName("disablePrevious");
         table.setWidget(1, 1, disablePrevious);
 
         Label artifactLabel = new Label("Artifact");
         table.setWidget(2, 0, artifactLabel);
 
         panel.add(new Hidden("artifactId", artifactId));
 
         setupRemainingTable(2);
     }
 }
