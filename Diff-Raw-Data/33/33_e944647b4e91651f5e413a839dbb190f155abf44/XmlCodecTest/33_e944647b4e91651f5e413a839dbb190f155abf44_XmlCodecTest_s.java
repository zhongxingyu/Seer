 package net.codjo.tools.pyp.xml;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import net.codjo.test.common.XmlUtil;
 import net.codjo.tools.pyp.model.Brin;
 import net.codjo.tools.pyp.model.Team;
 import org.junit.Test;
 /**
  *
  */
 public class XmlCodecTest {
     private XmlCodec xmlCodec = new XmlCodec();
 
 
     @Test
     public void test_toXml() throws Exception {
         List<Brin> brins = new ArrayList<Brin>();
 
         Brin brin = new Brin("titre1");
         brin.setUuid("MonUUID");
         brin.setCreationDate(new SimpleDateFormat("yyyy-MM-dd").parse("2011-06-01"));
         brin.setDescription("description1");
         brins.add(brin);
 
         Brin titre2 = new Brin("titre2");
         titre2.setCreationDate(new SimpleDateFormat("yyyy-MM-dd").parse("2011-06-01"));
         titre2.setDescription("description2");
         List<Team> affectedTeams = new ArrayList<Team>();
         affectedTeams.add(Team.gacpa);
        affectedTeams.add(Team.rdm_codaf);
         titre2.setAffectedTeams(affectedTeams);
         brins.add(titre2);
 
         String actual = xmlCodec.toXml(brins);
 
         XmlUtil.assertEquivalent("<brinList xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                                  + "          xsi:noNamespaceSchemaLocation=\"/pyp/pypRepository.xsd\">"
                                  + "  <repository>\n"
                                  + "    <brin>\n"
                                  + "      <uuid>MonUUID</uuid>\n"
                                  + "      <title>titre1</title>\n"
                                  + "      <description>description1</description>\n"
                                  + "      <creationDate>2011-06-01 00:00:00.0 CEST</creationDate>\n"
                                  + "      <affectedTeams/>\n"
                                  + "      <status>current</status>\n"
                                  + "    </brin>\n"
                                  + "    <brin>\n"
                                  + "      <title>titre2</title>\n"
                                  + "      <description>description2</description>\n"
                                  + "      <creationDate>2011-06-01 00:00:00.0 CEST</creationDate>\n"
                                  + "      <affectedTeams>\n"
                                  + "           <team>gacpa</team>\n"
                                 + "           <team>rdm_codaf</team>\n"
                                  + "      </affectedTeams>\n"
                                  + "      <status>current</status>\n"
                                  + "    </brin>"
                                  + "  </repository>\n"
                                  + "</brinList>", actual);
     }
 
 
     @Test
     public void test_fromXml() throws Exception {
         String expected = "<brinList xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                           + "          xsi:noNamespaceSchemaLocation=\"/pyp/pypRepository.xsd\">"
                           + "  <repository>\n"
                           + "    <brin>\n"
                           + "      <uuid>uniqueUuid</uuid>\n"
                           + "      <title>titre1</title>\n"
                           + "      <description>description1</description>\n"
                           + "      <creationDate>2011-06-01 00:00:00.0 CEST</creationDate>\n"
                           + "      <unblockingDate>2011-06-01 00:00:00.0 CEST</unblockingDate>\n"
                           + "      <unblockingType>medium</unblockingType>\n"
                           + "      <unblockingDescription>Bon ben c'est bon</unblockingDescription>\n"
                           + "      <affectedTeams/>\n"
                           + "      <status>current</status>\n"
                           + "    </brin>\n"
                           + "    <brin>\n"
                           + "      <title>titre2</title>\n"
                           + "      <description>description2</description>\n"
                           + "      <creationDate>2011-06-01 00:00:00.0 CEST</creationDate>\n"
                           + "      <affectedTeams>\n"
                          + "      <team>rdm_codaf</team>\n"
                           + "      <team>transverse</team>\n"
                           + "      </affectedTeams>\n"
                           + "      <rootCause>Pas d'lectricit</rootCause>\n"
                           + "      <status>current</status>\n"
                           + "    </brin>"
                           + "  </repository>\n"
                           + "</brinList>";
 
         String actual = xmlCodec.toXml(xmlCodec.fromXml(expected));
 
         XmlUtil.assertEquivalent(expected, actual);
     }
 }
