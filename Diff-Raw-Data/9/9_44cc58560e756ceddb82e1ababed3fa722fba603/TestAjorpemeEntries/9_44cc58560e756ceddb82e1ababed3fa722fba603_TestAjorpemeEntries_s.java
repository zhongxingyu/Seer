 /**
  * 
  */
 package com.wp3x.reader;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.wp3x.SourcePattern;
 import com.wp3x.SourcePatternFactory;
 import com.wp3x.reader.LinkEntry;
 import com.wp3x.reader.LinkException;
 import com.wp3x.reader.LinkReader;
 import com.wp3x.reader.SourceEntry;
 import com.wp3x.reader.SourceReader;
 
 /**
  * @author marcos.ferreira
  * 
  */
 public class TestAjorpemeEntries {
 
     final URL resource = this.getClass().getClassLoader().getResource("ajorpeme-list.htm");
     final URL link1 = this.getClass().getClassLoader().getResource("ajorpeme-n1.htm");
     final URL link2 = this.getClass().getClassLoader().getResource("ajorpeme-n2.htm");
     SourcePattern entryPattern;
     
     @Before
     public void before() throws MalformedURLException{
     	entryPattern = SourcePatternFactory.getAjorpemePattern();
     }
 
     @Test
     public void testGetLinks() throws Exception {
     	entryPattern.setSourceURL(resource);
     	SourceReader newsReader = new SourceReader(entryPattern);
         final Collection<SourceEntry> entries = newsReader.getEntries();
         System.out.println(entries);
         final SourceEntry firstEntry = entries.iterator().next();
         Assert.assertEquals("\n   18/04/13 - Esta semana tem Café & Negócios na Ajorpeme", firstEntry.getTitle());
         Assert.assertEquals("18/04/13", firstEntry.getDate());
         Assert.assertEquals("/site/noticias/1803-180413-esta-semana-tem-cafe-a-negocios-na-ajorpeme", firstEntry.getUrl());
        Assert.assertEquals("Esta Semana Tem Café & Negócios Na Ajorpeme", firstEntry.getFormattedTitle());
         Assert.assertEquals("http://ajorpeme.com.br/site/noticias/1803-180413-esta-semana-tem-cafe-a-negocios-na-ajorpeme", firstEntry.getFormattedURL());
         
         Assert.assertEquals(10, entries.size());
     }
 
     @Test
     public void testReadLink1() throws MalformedURLException, LinkException{
     	LinkReader linkReader = new LinkReader(entryPattern, link1);
     	LinkEntry linkEntry = linkReader.getLinkEntry();
     	Assert.assertEquals(getExpectedLinkText1(), linkEntry.getText());
     	String formatedText = linkEntry.getFormattedText();
     	Assert.assertEquals(getExpectedFormattedLinkText1(), formatedText);
     }
     
     private String getExpectedFormattedLinkText1() {
 		return "A Ajorpeme recebeu na noite desta quinta-feira, 09, a presença do prefeito Udo para apresentar um diagnostico da prefeitura e as ações que estão sendo realizadas nos primeiros meses da sua gestão.  Participaram da reunião membros da diretoria, conselheiros, ex-presidentes e presidentes dos núcleos setoriais. Informações sobre a saúde, educação, infraestrutura e segurança foram debatidas entre os participantes.\nA presidente da Ajorpeme, Christiane Schramm Guisso; a Conselheira do Conselho Fiscal, Robina Saito Sonnesen; o prefeito Udo Dohler e o presidente do Conselho Deliberativo da Ajorpeme, Gean Marcos Dombroski Corrêa.\nAssessoria de Imprensa da Ajorpeme";
 	}
 
 	private String getExpectedLinkText1() {
 		return "\n\r\n\r\r\r\nA Ajorpeme recebeu na noite desta quinta-feira, 09, a presença do prefeito Udo para apresentar um diagnostico da prefeitura e as ações que estão sendo realizadas nos primeiros meses da sua gestão.  Participaram da reunião membros da diretoria, conselheiros, ex-presidentes e presidentes dos núcleos setoriais. Informações sobre a saúde, educação, infraestrutura e segurança foram debatidas entre os participantes.\r\n \r\n\r\nA presidente da Ajorpeme, Christiane Schramm Guisso; a Conselheira do Conselho Fiscal, Robina Saito Sonnesen; o prefeito Udo Dohler e o presidente do Conselho Deliberativo da Ajorpeme, Gean Marcos Dombroski Corrêa.\r\n \r\n \r\n \r\nAssessoria de Imprensa da Ajorpeme";
 	}
 
 	@Test
     public void testReadLink2() throws MalformedURLException, LinkException{
     	LinkReader linkReader = new LinkReader(entryPattern, link2);
     	LinkEntry linkEntry = linkReader.getLinkEntry();
     	Assert.assertEquals(getExpectedLinkText2(), linkEntry.getText());
     	String formatedText = linkEntry.getFormattedText();
     	Assert.assertEquals(getExpectedFormattedLinkText2(), formatedText);
     }
 
 	private String getExpectedFormattedLinkText2() {
 		return "Uma comitiva da Ajorpeme e da Fampesc esteve participando nesta terça-feira, 07, da oficina para construção da Agenda Nacional de Desenvolvimento e Competitividade das Micro e Pequenas empresas 2013-2022, em Curitiba.  As atividades práticas do encontro incluíram diálogos colaborativos e plenárias abordando temas como: Compras Governamentais, Desoneração e Desburocratização, Informação e Capacitação, Investimento e Financiamento e Tecnologia e Inovação.\nO objetivo da oficina foi de contribuir para a construção da Agenda Nacional de Desenvolvimento e Competitividade de MEI, ME e EPP, através de debates e indicação de ações estratégicas nas seis áreas temáticas.\nAgenda Nacional\nCoordenada pelo Ministério do Desenvolvimento, Indústria e Comércio Exterior (MDIC), a Agenda será elaborada com sugestões, ideias e propostas reunidas em encontros que envolvem o poder público dos estados e municípios, além de entidades de classe, academia e sociedade civil.\nA preparação da Agenda Nacional é decorrente da Política Nacional de Empreendedorismo (PNE), uma iniciativa do Governo Federal, conduzida também pelo MDIC, com o apoio de instituições e órgãos públicos, entidades privadas e do terceiro setor.\n.\n.\n.\nAssessoria de Imprensa da Ajorpeme";
 	}
 
 	private String getExpectedLinkText2() {
 		return "\n \r\n\r\r\r\nUma comitiva da Ajorpeme e da Fampesc esteve participando nesta terça-feira, 07, da oficina para construção da Agenda Nacional de Desenvolvimento e Competitividade das Micro e Pequenas empresas 2013-2022, em Curitiba.  As atividades práticas do encontro incluíram diálogos colaborativos e plenárias abordando temas como: Compras Governamentais, Desoneração e Desburocratização, Informação e Capacitação, Investimento e Financiamento e Tecnologia e Inovação.\r\n \r\nO objetivo da oficina foi de contribuir para a construção da Agenda Nacional de Desenvolvimento e Competitividade de MEI, ME e EPP, através de debates e indicação de ações estratégicas nas seis áreas temáticas.\r\n \r\nAgenda Nacional\r\n \r\nCoordenada pelo Ministério do Desenvolvimento, Indústria e Comércio Exterior (MDIC), a Agenda será elaborada com sugestões, ideias e propostas reunidas em encontros que envolvem o poder público dos estados e municípios, além de entidades de classe, academia e sociedade civil.\r\n \r\nA preparação da Agenda Nacional é decorrente da Política Nacional de Empreendedorismo (PNE), uma iniciativa do Governo Federal, conduzida também pelo MDIC, com o apoio de instituições e órgãos públicos, entidades privadas e do terceiro setor.\r\n \r\n.\r\n\r\n\r\n.\r\n\r\n.\r\nAssessoria de Imprensa da Ajorpeme";
 	}
 }
