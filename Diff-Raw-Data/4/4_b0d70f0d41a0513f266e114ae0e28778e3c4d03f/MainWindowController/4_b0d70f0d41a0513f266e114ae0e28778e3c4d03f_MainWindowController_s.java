 /*******************************************************************************
  *  Copyright 2013 Jason Sipula                                                *
  *                                                                             *
  *  Licensed under the Apache License, Version 2.0 (the "License");            *
  *  you may not use this file except in compliance with the License.           *
  *  You may obtain a copy of the License at                                    *
  *                                                                             *
  *      http://www.apache.org/licenses/LICENSE-2.0                             *
  *                                                                             *
  *  Unless required by applicable law or agreed to in writing, software        *
  *  distributed under the License is distributed on an "AS IS" BASIS,          *
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
  *  See the License for the specific language governing permissions and        *
  *  limitations under the License.                                             *
  *******************************************************************************/
 
 package com.vanomaly.superd.controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.security.NoSuchAlgorithmException;
 import java.util.Comparator;
 import java.util.regex.Pattern;
 
 import net.snakedoc.jutils.ConfigException;
 
 import com.vanomaly.superd.Config;
 import com.vanomaly.superd.core.FileScanner;
 import com.vanomaly.superd.core.SimpleFileProperty;
 import com.vanomaly.superd.view.BaselineLeftStringCenterOverrunTableCell;
 import com.vanomaly.superd.view.BaselineRightStringTableCell;
 import com.vanomaly.superd.view.ThemedStageFactory;
 
 import javafx.application.Platform;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.concurrent.Task;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.Slider;
 import javafx.scene.control.TableCell;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableView;
 import javafx.scene.control.TextField;
 import javafx.scene.control.cell.PropertyValueFactory;
 import javafx.stage.DirectoryChooser;
 import javafx.stage.Stage;
 import javafx.util.Callback;
 
 /**
  * @author Jason Sipula
  *
  */
 public class MainWindowController {
     
     @FXML private TableView<SimpleFileProperty> table;
     @FXML private TableColumn<SimpleFileProperty, String> fileCol;
     @FXML private TableColumn<SimpleFileProperty, String> hashCol;
     @FXML private TableColumn<SimpleFileProperty, String> sizeCol;
     final private ObservableList<SimpleFileProperty> tableData = FXCollections.observableArrayList();
     
     @FXML private Label targetLabel;
     @FXML private TextField targetText;
     
     @FXML private Label delimiterLabel;
     @FXML private TextField delimiterText;
     
     @FXML private Label hashMethodLabel;
     @FXML private Slider hashSlider;
     @FXML private TextField hashMethodDescText;
     
     @FXML private Button addButton;
     @FXML private Button actionButton;
     
     private String CURRENT_HASHALGO;
     private String CURRENT_DELIMITER;
     private String LAST_DELIMITER;
     
     private MainWindowController() {
     }
     
     private static class MainWindowControllerator {
         private static final MainWindowController INSTANCE = new MainWindowController();
     }
     
     public static MainWindowController getInstance() {
         return MainWindowControllerator.INSTANCE;
     }
     
     /**
      * Initializes the controller class. This method is automatically called
      * after the fxml file has been loaded.
      */
     @FXML
     private void initialize() {
         hashSlider.valueProperty().addListener(new ChangeListener<Number>() {
             @Override
             public void changed(final ObservableValue<? extends Number> ov,
                     final Number old_val, final Number new_val) {
                 try {
                     setAndUpdateHashAlgo(new_val.intValue());
                 } catch (ConfigException e) {
                     // don't care
                 }
             }
         });
         hashSlider.setValue(initializeSlider());
         delimiterText.textProperty().addListener(new ChangeListener<String>() {
             @Override
             public void changed(final ObservableValue<? extends String> ov,
                                     final String old_val, final String new_Val) {
                 if (!"".equals(new_Val) && new_Val != null) {
                     refreshDelimiter();
                 }
             }
         });
         initializeDelimiterText();
         initializeColumns();
         table.setItems(tableData);
     }
     
     private void initializeColumns() {
         fileCol.setCellValueFactory(
                 new PropertyValueFactory<SimpleFileProperty, String>("path")
                 );
         fileCol.setCellFactory(
                 new Callback<TableColumn<SimpleFileProperty, String>, TableCell<SimpleFileProperty, String>>() {
                     @Override
                     public TableCell<SimpleFileProperty, String> call(TableColumn<SimpleFileProperty, String> t) {
                         return new BaselineLeftStringCenterOverrunTableCell();
                     }
                 });
         hashCol.setCellValueFactory(
                 new PropertyValueFactory<SimpleFileProperty, String>("hash")
                 );
         hashCol.setCellFactory(
                 new Callback<TableColumn<SimpleFileProperty, String>, TableCell<SimpleFileProperty, String>>() {
                     @Override
                     public TableCell<SimpleFileProperty, String> call(TableColumn<SimpleFileProperty, String> t) {
                         return new BaselineLeftStringCenterOverrunTableCell();
                     }
                 });
         sizeCol.setCellValueFactory(
                 new PropertyValueFactory<SimpleFileProperty, String>("size")
                 );
         sizeCol.setCellFactory(
                 new Callback<TableColumn<SimpleFileProperty,String>, TableCell<SimpleFileProperty, String>>() {
                     @Override
                     public TableCell<SimpleFileProperty, String> call(TableColumn<SimpleFileProperty, String> t) {
                         return new BaselineRightStringTableCell();
                     }
                 });
         sizeCol.setComparator(new Comparator<String>() {
 
             @Override
             public int compare(String arg0, String arg1) {
                 return getValue(arg0).compareTo(getValue(arg1));
             }
             
             private BigDecimal getValue(final String str) {
                 char[] origArr = str.toCharArray();
                 char[] destArr = new char[origArr.length];
                 int j = 0;
                 for (int i = 0; i < origArr.length; i++) {
                     char c = origArr[i];
                    if (c >= 48 && c <= 57) {
                         destArr[j++] = c;
                     }
                 }
                 return new BigDecimal(new String(destArr, 0, j));
             }
             
         });
     }
     
     public void addTableRow(final SimpleFileProperty data) {
         Platform.runLater(new Runnable() {
             @Override
             public void run() {
                 tableData.add(data);
             }
         });
         table.scrollTo(tableData.size() + 1);
     }
     
     private void refreshDelimiter() {
         if (!"".equals(getTargetText()) && getTargetText() != null) {
             setDelimiter();
             if (isDelimiterChanged() && LAST_DELIMITER != null) {
                 setTargetText(getTargetText().replaceAll(Pattern.quote(LAST_DELIMITER), CURRENT_DELIMITER));
             }
         }
     }
     
     private boolean isDelimiterChanged() {
         if (CURRENT_DELIMITER.equals(LAST_DELIMITER))
             return false;
         else
             return true;
     }
     
     public void setDelimiter(final String currentDelimiter) {
         LAST_DELIMITER = CURRENT_DELIMITER;
         CURRENT_DELIMITER = currentDelimiter;
         setDelimiterText(currentDelimiter);
     }
     
     public void setDelimiter() {
         LAST_DELIMITER = CURRENT_DELIMITER;
         CURRENT_DELIMITER = getDelimiterText();
         setDelimiterText(CURRENT_DELIMITER);
     }
     
     public String getDelimiter() {
         return CURRENT_DELIMITER;
     }
     
     private void initializeDelimiterText() {
         String prefDelimiter = "";
         prefDelimiter = Config.PREFS.getString("delimiter.pref");
         if (null == prefDelimiter || "".equals(prefDelimiter)) {
             this.setDelimiter(Config.SUPERD.getString("delimiter.default"));
         } else {
             this.setDelimiter(prefDelimiter);
         }
     }
     
     private int initializeSlider() {
         String prefAlgo = "";
         prefAlgo = Config.PREFS.getString("hashalgo.pref");
         if (null == prefAlgo || "".equals(prefAlgo)) {
             return hashAlgoString2Int(Config.SUPERD.getString(Config.SUPERD.getString("hashalgo.default")));
         }
         return hashAlgoString2Int(prefAlgo);
     }
     
     private int hashAlgoString2Int(String hashAlgo) {
         switch(hashAlgo) {
         case "MD5":
             return 0;
         case "SHA-1":
             return 50;
         case "SHA-256":
             return 100;
         case "SHA-512":
             return 150;
         default:
             return 100;
         }
     }
     
     /* targetLabel */
     public String getTargetLabel() {
         return this.targetLabel.getText();
     }
     
     public void setTargetLabel(final String targetLabel) {
         this.targetLabel.setText(targetLabel);
     }
     
     /* targetText */
     public String getTargetText() {
         return this.targetText.getText();
     }
     
     public void setTargetText(final String targetText) {
         this.targetText.setText(targetText);
     }
     
     public void appendTargetText(final String targetTextAddition) {
         this.targetText.appendText(targetTextAddition);
     }
     
     /* delimiterLabel */
     public String getDelimiterLabel() {
         return this.delimiterLabel.getText();
     }
     
     public void setDelimiterLabel(final String delimiterLabel) {
         this.delimiterLabel.setText(delimiterLabel);
     }
     
     /* delimiterText */
     private String getDelimiterText() {
         return this.delimiterText.getText();
     }
     
     private void setDelimiterText(final String delimiterText) {
         this.delimiterText.setText(delimiterText);
     }
     
     /* hashMethodLabel */
     public String getHashMethodLabel() {
         return this.hashMethodLabel.getText();
     }
     
     public void setHashMethodLabel(final String hashMethodLabel) {
         this.hashMethodLabel.setText(hashMethodLabel);
     }
     
     /* hashSlider */
     public int getHashSlider() {
         return (int) this.hashSlider.getValue();
     }
     
     public void setHashSlider(final int position) {
         this.hashSlider.setValue((double) position);
     }
     
     /* hashMethodDescText */
     public String getHashMethodDescText() {
         return this.hashMethodDescText.getText();
     }
     
     public void setHashMethodDescText(final String hashMethodDescText) {
         this.hashMethodDescText.setText(hashMethodDescText);
     }
     
     /* addButton */
     @FXML
     public void handleAddButtonAction(final ActionEvent event) {
         DirectoryChooser directoryChooser = new DirectoryChooser();
         directoryChooser.setTitle(Config.SUPERD.getString("addbutton.directorychooser.title"));
         Stage dialog = null;
         dialog = ThemedStageFactory.getNewThemedDialogStage();
         File file = null;
         if (dialog != null) {
             file = directoryChooser.showDialog(dialog);
         } else {
             throw new RuntimeException("Unable to display Window!");
         }
         
         if ("".equals(getTargetText()) || getTargetText() == null) {
             setTargetText(file.getAbsolutePath());
         } else {
             setDelimiter();
             if (isDelimiterChanged() && LAST_DELIMITER != null) {
                 setTargetText(getTargetText().replaceAll(Pattern.quote(LAST_DELIMITER), CURRENT_DELIMITER));
             }
             appendTargetText(getDelimiter() + file.getAbsolutePath());
         }
     }
     
     /* actionBUtton */
     @FXML
     public static void handleActionButtonAction(final ActionEvent event) {
         Task<Integer> task = new Task<Integer>() {
             @Override
             protected Integer call() throws Exception {
         // perform logical action (start, stop, etc)
         FileScanner fileScanner = null;
         try {
             fileScanner = new FileScanner(MainWindowController.getInstance().getHashAlgo());
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
         }
         String[] dirs = MainWindowController.getInstance().getTargetText()
                             .split(MainWindowController.getInstance().getDelimiterText());
         Path[] paths = new Path[dirs.length];
         for (int i = 0; i < dirs.length; i++) {
             paths[i] = Paths.get(dirs[i]);
         }
         for (Path path : paths) {
             try {
                 Files.walkFileTree(path, fileScanner);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return 0;
             }
         };
         new Thread(task).start();
     }
     
     public void setAndUpdateHashAlgo(final int hashAlgo) throws ConfigException {
         switch(hashAlgo) {
         case 0:
             setAndUpdateHashAlgo(Config.SUPERD.getString("hashalgo.md5"));
             break;
         case 50:
             setAndUpdateHashAlgo(Config.SUPERD.getString("hashalgo.sha1"));
             break;
         case 100:
             setAndUpdateHashAlgo(Config.SUPERD.getString("hashalgo.sha256"));
             break;
         case 150:
             setAndUpdateHashAlgo(Config.SUPERD.getString("hashalgo.sha512"));
             break;
         default:
             throw new ConfigException("Non supported algorithm!");
         }
     }
     
     public void setAndUpdateHashAlgo(final String hashAlgo) throws ConfigException {
         setHashAlgo(hashAlgo);
         updateViewHashAlgo();
     }
     
     private void setHashAlgo(final String hashAlgo) {
         MainWindowController.getInstance().CURRENT_HASHALGO = hashAlgo;
     }
     
     private String getHashAlgo() {
         return MainWindowController.getInstance().CURRENT_HASHALGO;
     }
     
     private void updateViewHashAlgo() throws ConfigException {
         this.setHashMethodLabel(getHashAlgo());
         this.setHashMethodDescText(
                 getHashAlgo().equals(Config.SUPERD.getString("hashalgo.md5")) ? Config.LANGUAGE.getString("hashalgo.description.md5") : 
                     getHashAlgo().equals(Config.SUPERD.getString("hashalgo.sha1")) ? Config.LANGUAGE.getString("hashalgo.description.sha1") : 
                         getHashAlgo().equals(Config.SUPERD.getString("hashalgo.sha256")) ? Config.LANGUAGE.getString("hashalgo.description.sha256") :
                             getHashAlgo().equals(Config.SUPERD.getString("hashalgo.sha512")) ? Config.LANGUAGE.getString("hashalgo.description.sha512") :
                                         ""
                 );
     }
 }
