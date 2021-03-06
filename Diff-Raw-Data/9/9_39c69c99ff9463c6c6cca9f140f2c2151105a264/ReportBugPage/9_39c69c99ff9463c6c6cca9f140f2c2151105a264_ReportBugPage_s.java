 /*
  * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free
  * software: you can redistribute it and/or modify it under the terms of the GNU
  * Affero General Public License as published by the Free Software Foundation,
  * either version 3 of the License, or (at your option) any later version.
  * BloatIt is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details. You should have received a copy of the GNU Affero General Public
  * License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.bloatit.web.pages;
 
 import static com.bloatit.framework.webserver.Context.tr;
 
 import java.util.EnumSet;
 
 import com.bloatit.framework.webserver.Context;
 import com.bloatit.framework.webserver.annotations.ParamContainer;
 import com.bloatit.framework.webserver.annotations.RequestParam;
 import com.bloatit.framework.webserver.annotations.RequestParam.Role;
 import com.bloatit.framework.webserver.components.HtmlDiv;
 import com.bloatit.framework.webserver.components.HtmlTitleBlock;
 import com.bloatit.framework.webserver.components.form.FormFieldData;
 import com.bloatit.framework.webserver.components.form.HtmlDropDown;
 import com.bloatit.framework.webserver.components.form.HtmlFileInput;
 import com.bloatit.framework.webserver.components.form.HtmlForm;
 import com.bloatit.framework.webserver.components.form.HtmlFormBlock;
 import com.bloatit.framework.webserver.components.form.HtmlSubmit;
 import com.bloatit.framework.webserver.components.form.HtmlTextArea;
 import com.bloatit.framework.webserver.components.form.HtmlTextField;
 import com.bloatit.framework.webserver.components.meta.HtmlElement;
 import com.bloatit.model.Offer;
 import com.bloatit.model.demand.DemandManager;
 import com.bloatit.web.actions.ReportBugAction;
 import com.bloatit.web.actions.ReportBugAction.BindedLevel;
 import com.bloatit.web.components.LanguageSelector;
 import com.bloatit.web.url.ReportBugActionUrl;
 import com.bloatit.web.url.ReportBugPageUrl;
 
 /**
  * Page that hosts the form to create a new Idea
  */
 @ParamContainer("demand/bug/report")
 public final class ReportBugPage extends LoggedPage {
 
     private static final int BUG_DESCRIPTION_INPUT_NB_LINES = 10;
     private static final int BUG_DESCRIPTION_INPUT_NB_COLUMNS = 80;
 
     public static final String BUG_BATCH = "bug_offer";
 
     @RequestParam(name = BUG_BATCH, role = Role.GET)
     private final Offer offer;
 
     public ReportBugPage(final ReportBugPageUrl reportBugPageUrl) {
         super(reportBugPageUrl);
         offer = reportBugPageUrl.getOffer();
     }
 
     @Override
     protected String getPageTitle() {
         return "Report a bug";
     }
 
     @Override
     public boolean isStable() {
         return false;
     }
 
     @Override
     public HtmlElement createRestrictedContent() {
         if (DemandManager.canCreate(session.getAuthToken())) {
             return new HtmlDiv("padding_box").add(generateReportBugForm());
         }
         return generateBadRightError();
     }
 
     private HtmlElement generateReportBugForm() {
         final HtmlTitleBlock formTitle = new HtmlTitleBlock(Context.tr("Report a bug"), 1);
         final ReportBugActionUrl doReportUrl = new ReportBugActionUrl();
         doReportUrl.setBatch(offer.getCurrentBatch());
 
         // Create the form stub
         final HtmlForm reportBugForm = new HtmlForm(doReportUrl.urlString());
         reportBugForm.enableFileUpload();
 
         formTitle.add(reportBugForm);
 
         // Create the field for the title of the bug
         final FormFieldData<String> createFormFieldData = doReportUrl.getTitleParameter().formFieldData();
         final HtmlTextField bugTitleInput = new HtmlTextField(createFormFieldData, Context.tr("Bug title"));
         bugTitleInput.setComment(Context.tr("A short title of the bug."));
         reportBugForm.add(bugTitleInput);
 
         // Create the fields that will describe the descriptions of the project
 
         final FormFieldData<String> descriptionFormFieldData = doReportUrl.getDescriptionParameter().formFieldData();
         final HtmlTextArea descriptionInput = new HtmlTextArea(descriptionFormFieldData,
                                                                Context.tr("Describe the bug"),
                                                                BUG_DESCRIPTION_INPUT_NB_LINES,
                                                                BUG_DESCRIPTION_INPUT_NB_COLUMNS);
         descriptionInput.setComment(Context.tr("Mininum 10 character. You can enter a long description of the bug."));
         reportBugForm.add(descriptionInput);
 
         // Language
         final FormFieldData<String> languageFormFieldData = doReportUrl.getLangParameter().formFieldData();
         final LanguageSelector languageInput = new LanguageSelector(languageFormFieldData, Context.tr("Language"));
         languageInput.setComment(Context.tr("Language of the description."));
         reportBugForm.add(languageInput);
 
         // Level
         final FormFieldData<BindedLevel> levelFormFieldData = doReportUrl.getLevelParameter().formFieldData();
         final LevelSelector levelInput = new LevelSelector(levelFormFieldData, Context.tr("Level"));
         levelInput.setComment(Context.tr("Level of the bug."));
         reportBugForm.add(levelInput);
 
         //File
         final HtmlFormBlock attachementBlock = new HtmlFormBlock(tr("Attachement"));
         reportBugForm.add(attachementBlock);
 
         final HtmlFileInput attachementInput = new HtmlFileInput(ReportBugAction.ATTACHEMENT_CODE, Context.tr("Attachement file"));
         attachementInput.setComment("Optional. If attach a file, you must add an attachement description. Max 2go.");
         attachementBlock.add(attachementInput);
 
         final FormFieldData<String> attachementDescriptionFormFieldData = doReportUrl.getAttachementDescriptionParameter().formFieldData();
         final HtmlTextField attachementDescriptionInput = new HtmlTextField(attachementDescriptionFormFieldData, Context.tr("Attachment description"));
         attachementDescriptionInput.setComment(Context.tr("Need only if you add an attachement."));
         attachementBlock.add(attachementDescriptionInput);
 
 
         reportBugForm.add(new HtmlSubmit(Context.tr("Report the bug")));
 
         final HtmlDiv group = new HtmlDiv();
         group.add(formTitle);
         return group;
     }
 
     private HtmlElement generateBadRightError() {
         final HtmlDiv group = new HtmlDiv();
 
         return group;
     }
 
     @Override
     public String getRefusalReason() {
         return Context.tr("You must be logged to report a new bug.");
     }
 
     static public class LevelSelector extends HtmlDropDown {
 
         public LevelSelector(FormFieldData<BindedLevel> levelFormFieldData, String label) {
             super(levelFormFieldData, label);
 
             addDropDownElements(EnumSet.allOf(BindedLevel.class));
 
             // That doesn't works. Make it work
            doSetDefaultValue(levelFormFieldData.getFieldDefaultValueAsString());
 
         }
 
     }
 
 }
