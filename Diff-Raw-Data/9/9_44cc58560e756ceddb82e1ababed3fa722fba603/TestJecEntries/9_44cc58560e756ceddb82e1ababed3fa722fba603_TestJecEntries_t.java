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
 public class TestJecEntries {
 
     final URL resource = this.getClass().getClassLoader().getResource("jec-list.htm");
     final URL link1 = this.getClass().getClassLoader().getResource("jec-n1.htm");
     final URL link2 = this.getClass().getClassLoader().getResource("jec-n2.htm");
     SourcePattern entryPattern;
     
     @Before
     public void before() throws MalformedURLException{
     	entryPattern = SourcePatternFactory.getJecPattern();
     }
 
     @Test
     public void testGetLinks() throws Exception {
     	entryPattern.setSourceURL(resource);
     	SourceReader newsReader = new SourceReader(entryPattern);
         final Collection<SourceEntry> entries = newsReader.getEntries();
         System.out.println(entries);
         final SourceEntry firstEntry = entries.iterator().next();
        Assert.assertEquals("Citadino: JEC vence o Santos", firstEntry.getFormattedTitle());
         Assert.assertEquals(null, firstEntry.getDate());
         Assert.assertEquals("http://jec.com.br/citadino-jec-vence-o-santos/", firstEntry.getUrl());
         Assert.assertEquals("http://jec.com.br/citadino-jec-vence-o-santos/", firstEntry.getFormattedURL());
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
 		return "Empresário Rainor da Silva (esquerda) arcará com salários do meia Welligton Bruno\nO Joinville Esporte Clube acertou a contratação de um sonho antigo da diretoria. Mirando o acesso ao Brasileiro da Série A, Welligton Bruno, meia, de 27 anos, foi apresentando nesta sexta-feira na Arena Joinville. O atleta estava na Ponte Preta e marcou passagens no Flamengo e Ipatinga. Na equipe mineira, inclusive, enfrentou o JEC na segunda fase da Série C 2011 e foi o grande destaque da partida.\nWelligton Bruno assinou com o Joinville até dezembro de 2014. Além disso, parte dos direitos do jogador pertencerá ao clube até o término do seu contrato.\n“A torcida aqui é fantástica e minha vontade em defender esse clube sempre foi muito grande. Depois de algumas tentativas, finalmente deu certo. Agora quero trabalhar para estar à disposição para o segundo jogo contra o Santos”, declarou.\nPatrocinadora oficial do JEC, a Red Horse se encarregou de arcar com os salários do novo meia tricolor. “É uma promessa que eu fiz para todos nós joinvilenses. É um jogador que eu sempre gostei e o presidente trouxe. Esse ano vamos subir com certeza”, aposta o empresário Rainor da Silva.\nFicha do atleta\nNome: Welligton Bruno da Silva\nData de Nascimento: 25/04/1986\nNaturalidade: Guarulhos-SP\nPosição: Meia\nAltura: 1,74m\nPeso: 79 Kg\nEquipes: Noroeste-SP, América-TO, Ipatinga-MG, Flamengo-RJ e Ponte Preta-SP.";
 	}
 
 	private String getExpectedLinkText1() {
 		return "Empresário Rainor da Silva (esquerda) arcará com salários do meia Welligton Bruno\nO Joinville Esporte Clube acertou a contratação de um sonho antigo da diretoria. Mirando o acesso ao Brasileiro da Série A, Welligton Bruno, meia, de 27 anos, foi apresentando nesta sexta-feira na Arena Joinville. O atleta estava na Ponte Preta e marcou passagens no Flamengo e Ipatinga. Na equipe mineira, inclusive, enfrentou o JEC na segunda fase da Série C 2011 e foi o grande destaque da partida.\nWelligton Bruno assinou com o Joinville até dezembro de 2014. Além disso, parte dos direitos do jogador pertencerá ao clube até o término do seu contrato.\n“A torcida aqui é fantástica e minha vontade em defender esse clube sempre foi muito grande. Depois de algumas tentativas, finalmente deu certo. Agora quero trabalhar para estar à disposição para o segundo jogo contra o Santos”, declarou.\nPatrocinadora oficial do JEC, a Red Horse se encarregou de arcar com os salários do novo meia tricolor. “É uma promessa que eu fiz para todos nós joinvilenses. É um jogador que eu sempre gostei e o presidente trouxe. Esse ano vamos subir com certeza”, aposta o empresário Rainor da Silva.\nFicha do atleta\nNome: Welligton Bruno da Silva\nData de Nascimento: 25/04/1986\nNaturalidade: Guarulhos-SP\nPosição: Meia\nAltura: 1,74m\nPeso: 79 Kg\nEquipes: Noroeste-SP, América-TO, Ipatinga-MG, Flamengo-RJ e Ponte Preta-SP.\n";
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
 		return "Em função da estreia do JEC no Campeonato Brasileiro da Série B, no dia 25, diante do Bragantino, o departamento das categorias de base do clube, em comum acordo com a Chapecoense, antecipou a estreia das equipes no Catarinense da categoria para o próximo dia 24, sexta-feira.\nO jogo do time sub-17 está marcada para as 13h30min. Em seguida, às 15h30min, o sub-20 entra em campo. As partidas acontecem no campo da Tupy, em Joinville.";
 	}
 
 	private String getExpectedLinkText2() {
 		return "Em função da estreia do JEC no Campeonato Brasileiro da Série B, no dia 25, diante do Bragantino, o departamento das categorias de base do clube, em comum acordo com a Chapecoense, antecipou a estreia das equipes no Catarinense da categoria para o próximo dia 24, sexta-feira.\nO jogo do time sub-17 está marcada para as 13h30min. Em seguida, às 15h30min, o sub-20 entra em campo. As partidas acontecem no campo da Tupy, em Joinville.\n";
 	}
 }
