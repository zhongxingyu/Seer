 /**
  *
  * Copyright 2010 Laurent Desgrange
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package net.desgrange.pwad.functional;
 
 import static org.junit.Assert.assertEquals;
 import static org.uispec4j.assertion.UISpecAssert.assertFalse;
 import static org.uispec4j.assertion.UISpecAssert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
import java.util.Collections;
import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.uispec4j.Button;
 import org.uispec4j.MenuItem;
 import org.uispec4j.TextBox;
 import org.uispec4j.Trigger;
 import org.uispec4j.Window;
 import org.uispec4j.interception.FileChooserHandler;
 import org.uispec4j.interception.WindowHandler;
 import org.uispec4j.interception.WindowInterceptor;
 
 public class DownloadPublicAlbumTest extends PwadTestCase {
     private Window window;
     private MenuItem openAlbumFromLinkMenu;
     private TextBox albumNameField;
     private TextBox picturesCountField;
     private Button downloadButton;
 
     @Before
     public void setUp() {
         window = getMainWindow();
         openAlbumFromLinkMenu = window.getMenuBar().getMenu("File").getSubMenu("Open album from invitation link…");
         albumNameField = window.getTextBox("albumNameField");
         picturesCountField = window.getTextBox("picturesCountField");
         downloadButton = window.getButton("Download all pictures");
     }
 
     @Test
     public void testUserCanDownloadPublicAlbumFromInvitationLink() throws Exception {
         assertTrue(window.titleEquals("pwad - Picasa Web Albums Downloader"));
         assertFalse(downloadButton.isEnabled());
         WindowInterceptor.init(openAlbumFromLinkMenu.triggerClick()).process(new OpenAlbumDialog(createInvitationLink())).run();
         assertTrue(albumNameField.textEquals("Holiday in Cambodia"));
         assertTrue(picturesCountField.textEquals("2"));
         assertTrue(downloadButton.isEnabled());
         final File outputFolder = createTempDirectory();
         WindowInterceptor.init(downloadButton.triggerClick())
                 .process(FileChooserHandler.init().titleEquals("Select output folder").assertAcceptsDirectoriesOnly().select(outputFolder))
                 .process(new WindowHandler() {
                     @Override
                     public Trigger process(final Window dialog) throws Exception {
                         assertTrue(dialog.titleEquals("Downloading…"));
                         assertTrue(dialog.getTextBox().textContains("Downloading picture", "out of", "…"));
                         assertTrue(dialog.getProgressBar().isVisible());
                         assertTrue(dialog.getProgressBar().completionEquals(0));
                         assertTrue(dialog.getProgressBar().isCompleted());
                         return Trigger.DO_NOTHING;
                     }
                 }).run();
        final List<String> actualFiles = Arrays.asList(outputFolder.list());
        Collections.sort(actualFiles);
        assertEquals("[100_0001.JPG, 100_0002.JPG]", actualFiles.toString());
     }
 
     private String createInvitationLink() {
         final String userId = "dead_kennedys";
         final String albumId = "holiday_in_cambodia";
         final String authenticationKey = "the_authentication_key";
         final String invitationId = "invitation_id";
         return "http://picasaweb.google.fr/lh/sredir?uname=" + userId + "&target=ALBUM&id=" + albumId + "&authkey=" + authenticationKey + "&invite=" + invitationId + "&feat=email";
     }
 
     private File createTempDirectory() throws IOException {
         final File file = File.createTempFile(getClass().getSimpleName(), "");
         file.delete();
         file.mkdir();
         file.deleteOnExit();
         return file;
     }
 
     private static class OpenAlbumDialog extends WindowHandler {
         private final String link;
 
         public OpenAlbumDialog(final String link) {
             this.link = link;
         }
 
         @Override
         public Trigger process(final Window dialog) throws Exception {
             assertTrue(dialog.titleEquals("Open album from invitation link…"));
             assertTrue(dialog.getTextBox("message").textEquals("Paste, in the following field, the link you received in the invitation email."));
             dialog.getInputTextBox("Invitation link").setText(link);
             return dialog.getButton("OK").triggerClick();
         }
     }
 }
