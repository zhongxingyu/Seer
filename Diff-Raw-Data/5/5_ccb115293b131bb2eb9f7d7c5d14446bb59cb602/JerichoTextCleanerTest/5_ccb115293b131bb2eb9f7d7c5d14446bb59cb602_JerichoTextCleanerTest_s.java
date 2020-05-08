 package de.spektrumprojekt.informationextraction.extractors;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Date;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import de.spektrumprojekt.datamodel.common.MimeType;
 import de.spektrumprojekt.datamodel.message.Message;
 import de.spektrumprojekt.datamodel.message.MessagePart;
 import de.spektrumprojekt.datamodel.message.MessageType;
 import de.spektrumprojekt.datamodel.subscription.status.StatusType;
 import de.spektrumprojekt.informationextraction.InformationExtractionContext;
 import de.spektrumprojekt.persistence.simple.PersistenceMock;
 
 public class JerichoTextCleanerTest {
 
     @Test
     public void testTextCleaner() {
         JerichoTextCleanerCommand textCleaner = new JerichoTextCleanerCommand(false);
 
         Message message = new Message(MessageType.CONTENT, StatusType.OK, new Date());
         String text = "<!-- comment --><p>the <i>quick</i> brown fox jumps over the lazy dog.</p>";
         MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, text);
         message.addMessagePart(messagePart);
         InformationExtractionContext context = new InformationExtractionContext(
                 new PersistenceMock(), message,
                 messagePart);
         textCleaner.process(context);
        assertEquals("the quick brown fox jumps over the lazy dog.", context.getCleanText());
 
         message = new Message(MessageType.CONTENT, StatusType.OK, new Date());
         text = "<p>In <a href=\"http://arstechnica.com/apple/2012/07/the-server-simplified-a-power-users-guide-to-os-x-server/\">our review of OS X Server</a>, we found that Mountain Lion has a lot to offer home users or Mac-centric small businesses. Enterprise-level features, however, have fallen by the wayside. Luckily, some great first- and third-party tools exist to help close the gap between Apple's server product and more robust enterprise management systems from the likes of Microsoft and Dell.</p><p>Some of the products are free open-source programs, and some are strong, for-pay products intended for use with hundreds if not thousands of Macs. Whatever your needs are, this list of applications should point you in the right direction if you're looking to extend OS X Server's capabilities.</p><h2>Apple Remote Desktop</h2><div class=\"image center full-width\" style=\"width:631px\"><img src=\"http://cdn.arstechnica.net/wp-content/uploads/2012/08/ARD-UNIX-command.png\" width=\"631\" height=\"547\"><div class=\"caption\"><div class=\"caption-text\">Sending a Software Update UNIX command with Apple Remote Desktop.</div> </div></div><p>One of OS X Server's most glaring blind spots relative to Windows Server and Active Directory is software management. There's no way to install third-party applications on Macs that are already out in the field. And if you use a program like DeployStudio to install applications when you set up your Mac, it isn't much help to you once the Mac is off your desk and out in the field.</p><p><a href=\"http://arstechnica.com/information-technology/2012/08/filling-in-the-gaps-great-add-ons-for-os-x-server/\">Read 17 remaining paragraphs</a> | <a href=\"http://arstechnica.com/information-technology/2012/08/filling-in-the-gaps-great-add-ons-for-os-x-server/?comments=1#comments-bar\">Comments</a></p><p><a href=\"http://feedads.g.doubleclick.net/~at/kFKNQf0ovGoFHtbBLQkks4z89Q8/0/da\"><img src=\"http://feedads.g.doubleclick.net/~at/kFKNQf0ovGoFHtbBLQkks4z89Q8/0/di\" border=\"0\" ismap=\"true\"></img></a><br/><a href=\"http://feedads.g.doubleclick.net/~at/kFKNQf0ovGoFHtbBLQkks4z89Q8/1/da\"><img src=\"http://feedads.g.doubleclick.net/~at/kFKNQf0ovGoFHtbBLQkks4z89Q8/1/di\" border=\"0\" ismap=\"true\"></img></a></p><div class=\"feedflare\"><a href=\"http://feeds.arstechnica.com/~ff/arstechnica/index?a=gqo4KE45VGA:EF43jNJcvAM:V_sGLiPBpWU\"><img src=\"http://feeds.feedburner.com/~ff/arstechnica/index?i=gqo4KE45VGA:EF43jNJcvAM:V_sGLiPBpWU\" border=\"0\"></img></a> <a href=\"http://feeds.arstechnica.com/~ff/arstechnica/index?a=gqo4KE45VGA:EF43jNJcvAM:F7zBnMyn0Lo\"><img src=\"http://feeds.feedburner.com/~ff/arstechnica/index?i=gqo4KE45VGA:EF43jNJcvAM:F7zBnMyn0Lo\" border=\"0\"></img></a> <a href=\"http://feeds.arstechnica.com/~ff/arstechnica/index?a=gqo4KE45VGA:EF43jNJcvAM:qj6IDK7rITs\"><img src=\"http://feeds.feedburner.com/~ff/arstechnica/index?d=qj6IDK7rITs\" border=\"0\"></img></a> <a href=\"http://feeds.arstechnica.com/~ff/arstechnica/index?a=gqo4KE45VGA:EF43jNJcvAM:yIl2AUoC8zA\"><img src=\"http://feeds.feedburner.com/~ff/arstechnica/index?d=yIl2AUoC8zA\" border=\"0\"></img></a></div><img src=\"http://feeds.feedburner.com/~r/arstechnica/index/~4/gqo4KE45VGA\" height=\"1\" width=\"1\"/>";
         messagePart = new MessagePart(MimeType.TEXT_PLAIN, text);
         message.addMessagePart(messagePart);
         context = new InformationExtractionContext(new PersistenceMock(), message, messagePart);
         textCleaner.process(context);
        assertEquals("Extracted text is: " + context.getCleanText(), 1180, context.getCleanText()
                 .length());
 
         message = new Message(MessageType.CONTENT, StatusType.OK, new Date());
         text = "<p>Test1</p><p>Test2</p><ul><li>Test3</li><li>Test4</li><li>Test5 Test6&#160; Test7</li><ul><li>test17</li></ul><li>Test8</li></ul><ol><li>Test9</li><ol><li>Test10</li><li>Test11</li></ol><li><u>Test12</u></li><li><b>Test13</b></li><li>Test14</li><ol><li><i>Test15</i></li><li><a href=\"http://www.communote.com\" target=\"_blank\">Test16</a></li><ol><li><u><i><b>Test18</b></i></u>esdfzisdfzsdf</li></ol><li>#test19</li><li>#test20</li></ol></ol><p>&#8203;</p><p>&#8203;</p><p><br/><br/>test21 test22   test23</p><p>test24 test25 test26 test27 test28<br/>test29 test30</p><p>&#8203;</p>";
         messagePart = new MessagePart(MimeType.TEXT_PLAIN, text);
         message.addMessagePart(messagePart);
         context = new InformationExtractionContext(new PersistenceMock(), message, messagePart);
         textCleaner.process(context);
 
         String lowerText = context.getCleanText().toLowerCase();
         for (int i = 0; i < lowerText.length(); i++) {
             System.out.println("'" + lowerText.charAt(i) + "' = " + (double) lowerText.charAt(i)
                     + " isWhiteSpace=" + Character.isWhitespace(lowerText.charAt(i)));
         }
         char ch = '\0';
         System.out.println("'" + ch + "' = " + (int) ch);
         for (int i = 1; i <= 30; i++) {
             Assert.assertTrue("Text should contain test" + i, lowerText.contains("test" + i));
         }
     }
 }
