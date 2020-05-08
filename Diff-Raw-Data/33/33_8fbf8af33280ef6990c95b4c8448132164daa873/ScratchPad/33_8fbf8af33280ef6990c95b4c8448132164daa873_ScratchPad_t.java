 package no.uio.duo.bagit;
 
 import gov.loc.repository.bagit.Bag;
 import gov.loc.repository.bagit.BagItTxt;
 import gov.loc.repository.bagit.writer.impl.ZipWriter;
 import nu.xom.Builder;
 import nu.xom.Document;
 import org.junit.Test;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 public class ScratchPad
 {
     @Test
    public void arr() throws Exception
    {
        File bagfile = new File("/home/richard/Dropbox/Documents/DUO/deposit/deposit.zip");
        BagIt bi = new BagIt(bagfile);
    }

    @Test
     public void construct() throws Exception
     {
         String fileBase = System.getProperty("user.dir") + "/src/test/resources/testbags/testfiles/";
 
         String firstFinal = fileBase + "MainArticle.pdf";
         String secondFinal = fileBase + "AppendixA.pdf";
         String thirdFinal = fileBase + "AppendixB.pdf";
         String firstOSecondary = fileBase + "MainArticle.odt";
         String secondOSecondary = fileBase + "AppendixA.odt";
         String thirdOSecondary = fileBase + "AppendixB.odt";
         String firstCSecondary = fileBase + "UserData1.odt";
         String secondCSecondary = fileBase + "UserData2.odt";
         String thirdCSecondary = fileBase + "UserData3.odt";
         String metadata = fileBase + "metadata.xml";
         String licence = fileBase + "licence.txt";
 
         String out = System.getProperty("user.dir") + "/src/test/resources/testbags/fullbag.zip";
 
         BagIt bi = new BagIt(new File(out));
 
         bi.addFinalFile(new File(firstFinal), 1);
         bi.addFinalFile(new File(secondFinal), 2);
         bi.addFinalFile(new File(thirdFinal), 3);
 
         bi.addSupportingFile(new File(firstOSecondary), 1, "open");
         bi.addSupportingFile(new File(secondOSecondary), 2, "open");
         bi.addSupportingFile(new File(thirdOSecondary), 3, "open");
 
         bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
         bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
         bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");
 
         bi.addMetadataFile(new File(metadata));
         bi.addLicenceFile(new File(licence));
 
         bi.writeToFile();
     }
 
     @Test
     public void constructWithMetadata() throws Exception
     {
         String fileBase = System.getProperty("user.dir") + "/src/test/resources/testbags/testfiles/";
 
         String firstFinal = fileBase + "MainArticle.pdf";
         String secondFinal = fileBase + "AppendixA.pdf";
         String thirdFinal = fileBase + "AppendixB.pdf";
         String firstOSecondary = fileBase + "MainArticle.odt";
         String secondOSecondary = fileBase + "AppendixA.odt";
         String thirdOSecondary = fileBase + "AppendixB.odt";
         String firstCSecondary = fileBase + "UserData1.odt";
         String secondCSecondary = fileBase + "UserData2.odt";
         String thirdCSecondary = fileBase + "UserData3.odt";
         // String metadata = fileBase + "metadata.xml";
         String licence = fileBase + "licence.txt";
 
         String out = System.getProperty("user.dir") + "/src/test/resources/testbags/fullbag.zip";
 
         Metadata metadata = new Metadata();
 
         metadata.addField(Metadata.NAME, "Thor Heyerdahl");
         metadata.addField(Metadata.FAMILY_NAME, "Heyerdahl");
         metadata.addField(Metadata.GIVEN_NAME, "Thor");
         metadata.addField(Metadata.STUDENT_NUMBER, "123456789");
         metadata.addField(Metadata.UID, "theyerdahl");
         metadata.addField(Metadata.FOEDSELSNUMMER, "987654321");
         metadata.addField(Metadata.POSTAL_ADDRESS, "Colla Micheri, Italy");
         metadata.addField(Metadata.EMAIL, "t.heyerdahl@kontiki.com");
         metadata.addField(Metadata.TELEPHONE_NUMBER, "0047 123456");
         metadata.addSubject("AST3220", "Kosmologi I");
         metadata.addField(Metadata.UNITCODE, "123");
         metadata.addField(Metadata.UNIT_NAME, "Arkeologi, konservering og historie");
         metadata.addField(Metadata.TITLE, "101 days around some of the world");
         metadata.addField(Metadata.TITLE, "101 days in the Pacific", "nob");
         metadata.addField(Metadata.LANGUAGE, "nob");
         metadata.addField(Metadata.ABSTRACT, "Thor Heyerdahl og fem andre dro fra Peru til Raroia i en selvkonstruert balsafl�te ved navn\n" +
                 "        Kon-Tiki.", "nob");
         metadata.addField(Metadata.ABSTRACT, "In the Kon-Tiki Expedition, Heyerdahl and five fellow adventurers went to Peru, where\n" +
                 "        they constructed a pae-pae raft from balsa wood and other native materials, a raft that\n" +
                 "        they called the Kon-Tiki.");
         metadata.addField(Metadata.TYPE, "Master's thesis");
         metadata.addField(Metadata.EMBARGO_TYPE, "5 years");
         metadata.addField(Metadata.EMBARGO_END_DATE, "01-01-2015");
         metadata.addField(Metadata.GRADE, "pass");
 
         BagIt bi = new BagIt(new File(out));
 
         bi.addFinalFile(new File(firstFinal), 1);
         bi.addFinalFile(new File(secondFinal), 2);
         bi.addFinalFile(new File(thirdFinal), 3);
 
         bi.addSupportingFile(new File(firstOSecondary), 1, "open");
         bi.addSupportingFile(new File(secondOSecondary), 2, "open");
         bi.addSupportingFile(new File(thirdOSecondary), 3, "open");
 
         bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
         bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
         bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");
 
         bi.addMetadata(metadata);
         bi.addLicenceFile(new File(licence));
 
         bi.writeToFile();
     }
 
 }
