 package org.iplantc.de.client;
 
 import com.google.gwt.i18n.client.Messages;
 
 /**
  * Defines a set of common application display strings.
  * 
  * Intended for strings like "Save", "Save As...", and "Apply".
  * 
  * @author lenards
  * 
  */
 public interface CommonDisplayStrings extends Messages {
     /**
      * Display text for NSF project statement.
      * 
      * @return a String representing the text.
      */
     String nsfProjectText();
 
     /**
      * Localized text for display as the copyright statement for the project.
      * 
      * Currently, this statement appears in the footer of the Discovery Environment and TITo.
      * 
      * @return a string representing the localized text.
      */
     String projectCopyrightStatement();
 
     /**
      * Localized display text for when File Name needs to be displayed.
      * 
      * @return a string representing the localized text.
      */
     String fileName();
 
     String affirmativeResponse();
 
     String negativeResponse();
 
     String confirmAction();
 
     String applyOperation();
 
     String manageData();
 
     String viewResults();
 
     String start();
 
     String alert();
 
     /**
      * Localized display text for name.
      * 
      * @return a string representing the localized text.
      */
     String name();
 
     String createdDate();
 
     String status();
 
     /**
      * Localized display text for delete option.
      * 
      * @return a string representing the localized text.
      */
     String delete();
 
     String refresh();
 
     String downloadResult();
 
     String information();
 
     /**
      * Localized display text used for displaying 'Save'.
      * 
      * @return a string representing the localized text.
      */
     String save();
 
     /**
      * Localized display text used for displaying 'Save As'.
      * 
      * @return a string representing the localized text.
      */
     String saveAs();
 
     String progress();
 
     /**
      * Localized display text used for displaying 'Error'.
      * 
      * @return a string representing the localized text.
      */
     String error();
 
     /**
      * Localized display text for text of details button when displaying errors.
      * 
      * @return a string representing the localized text.
      */
     String details();
 
     String gwtVersion();
 
     String gxtVersion();
 
     String majorVersion();
 
     String minorVersion();
 
     String userAgent();
 
     String date();
 
     /**
      * Localized text for about
      * 
      * @return string representing the text
      */
     String about();
 
     /**
      * Localized text to display a cancel option.
      * 
      * @return a string representing the localized text.
      */
     String cancel();
 
     /**
      * Localized display text for copy
      * 
      * @return a string representing the localized text.
      */
     String copy();
 
     /**
      * Localized text for display as a field label for a user's email address.
      * 
      * @return a string representing the localized text.
      */
     String email();
 
     /**
      * Display text for help
      * 
      * @return a String representing the text.
      */
     String help();
 
     /**
      * Localized display text for Label.
      * 
      * @return a string representing the localized text.
      */
     String label();
 
     /**
      * Localized display text for Logout.
      * 
      * @return a string representing the localized text.
      */
     String logout();
 
     /**
      * Localized text for display as a message for when there are no items to display in a grid.
      * 
      * @return a string representing the localized text.
      */
     String noItemsToDisplay();
 
     /**
      * Localized text for display as a field label for a user's phone number.
      * 
      * @return a string representing the localized text.
      */
     String phone();
 
     /**
      * Localized display text for a warning title.
      * 
      * @return a string representing the localized text.
      */
     String warning();
 
     /**
      * Display text for welcome
      * 
      * @return a string representing the text.
      */
     String welcome();
 
     /**
      * Localized display text for edit.
      * 
      * @return a string representing the localized text.
      */
     String edit();
 
     /**
      * Localized text for display as a label for a user's identifier.
      * 
      * This identifier is the username assigned to the user.
      * 
      * @return a string representing the localized text.
      */
     String userId();
 
     /**
      * Localized text for display as text in the browse button for a file selector.
      * 
      * @return a string representing the localized text.
      */
     String browse();
 
     /**
      * Localized display text for description field labels and column headers.
      * 
      * @return a string representing the localized text.
      */
     String description();
 
     /**
      * Localized text for async service call loading masks.
      * 
      * @return string representing the text
      */
     String loadingMask();
 
     /**
      * Localized text for async service call saving masks.
      * 
      * @return string representing the text
      */
     String savingMask();
 
     /**
      * Localized display text for preview.
      * 
      * @return a string representing the localized text.
      */
     String preview();
 
     /**
      * Localized display text for displaying 'Upload'.
      * 
      * @return a string representing the localized text.
      */
     String upload();
 
     /**
      * Localized text for documentation
      * 
      * @return string representing the text
      */
     String documentation();
 
     /**
      * Localized text for success
      *
      * @return string representing the text
      */
     String success();
 
     /**
      * Localized text for submit
      *
      * @return string representing the text
      */
     String submit();
 
     /**
      * Localized display text for a "submit for public use" label.
      *
      * @return a string representing the localized text.
      */
     String makePublic();
 
     /**
      * Title of a dialog that tells the user the app has been made public.
      * 
      * @return
      */
     String makePublicSuccessTitle();
 
     /**
      * A message telling the user that "make public" was successful
      * 
      * @param url The URL of the documentation page
      * @return
      */
     String makePublicSuccessMessage(String url);
 
     /**
      * A message telling the user that "make public" was unsuccessful
      *
      * @return
      */
     String makePublicFail();
 
     /**
      * Title of the "public submission" dialog
      *
      * @return
      */
     String publicSubmissionForm();
 
     /**
      * Localized label for a tool integrator's name.
      *
      * @return string representing the text
      */
     String integratorName();
 
     /**
      * Localized label for a tool integrator's email.
      *
      * @return string representing the text
      */
     String integratorEmail();
 
     /**
      * Localized text for a analysis description field.
      *
      * @return string representing the text
      */
     String analysisDesc();
 
     /**
      * Localized text for a category selection label.
      *
      * @return string representing the text
      */
     String categorySelect();
 
     /**
      * Localized text for a Wiki URL label.
      * 
      * @param url the actual URL to the wiki page this label will use in a link
      * @return string representing the text
      */
     String wikiUrlLabel(String url);
 
     /**
      * Localized text for an "attach sample input files" label.
      * 
      * @return string representing the text
      */
     String attachSampleInput();
 
     /**
      * Localized text for an "attach sample output files" label.
      *
      * @return string representing the text
      */
     String attachSampleOutput();
 
     /**
      * Localized text for a references field label.
      *
      * @return string representing the text
      */
     String referencesLabel();
 
     /**
      * Localized text for add.
      *
      * @return string representing the text
      */
     String add();
 
     /**
      * Localized text for pop up warning msg
      * 
      * @return string representing the text
      */
     String popWarningMsg();
 
     /**
      * Localized text for pop up warning msg title
      * 
      * @return string representing the text
      */
     String popUpWarning();
     
     /**
      * Localized text for logout tool tip
      * 
      * @return string representing the text
      */
     String logoutToolTipText();
     
     /** Localized display text for the DE category
      * @return a string representing the localized text.
      */
      String category();
      
      /**
       * Localized display text for "Value(s)"
       * @return a string representing the localized text.
       */
      String valueParenS();
 }
