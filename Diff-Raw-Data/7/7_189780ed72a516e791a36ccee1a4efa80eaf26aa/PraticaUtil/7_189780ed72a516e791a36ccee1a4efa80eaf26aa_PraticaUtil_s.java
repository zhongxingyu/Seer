 /*
  * Copyright (C) 2012 AXIA Studio (http://www.axiastudio.com)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.axiastudio.suite.pratiche;
 
 import com.axiastudio.mapformat.MessageMapFormat;
 import com.axiastudio.menjazo.AlfrescoHelper;
 import com.axiastudio.pypapi.Register;
 import com.axiastudio.pypapi.db.Controller;
 import com.axiastudio.pypapi.db.Database;
 import com.axiastudio.pypapi.db.IDatabase;
 import com.axiastudio.pypapi.db.Validation;
 import com.axiastudio.suite.SuiteUtil;
 import com.axiastudio.suite.anagrafiche.entities.Soggetto;
 import com.axiastudio.suite.base.entities.Giunta;
 import com.axiastudio.suite.base.entities.IUtente;
 import com.axiastudio.suite.base.entities.Ufficio;
 import com.axiastudio.suite.base.entities.Utente;
 import com.axiastudio.suite.deliberedetermine.entities.Determina;
 import com.axiastudio.suite.deliberedetermine.entities.ServizioDetermina;
 import com.axiastudio.suite.deliberedetermine.entities.UfficioDetermina;
 import com.axiastudio.suite.plugins.cmis.CmisPlugin;
 import com.axiastudio.suite.pratiche.entities.Pratica;
 import com.axiastudio.suite.pratiche.entities.TipoPratica;
 import com.axiastudio.suite.procedimenti.entities.Procedimento;
 import com.axiastudio.suite.protocollo.entities.*;
 import com.sun.org.apache.xpath.internal.operations.Bool;
 import com.trolltech.qt.core.QProcess;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import java.util.*;
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 public class PraticaUtil {
 
     public static String creaCodificaInterna(TipoPratica tipoPratica){
 
         Map<String, Object> map = new HashMap();
 
         Database db = (Database) Register.queryUtility(IDatabase.class);
         EntityManager em = db.getEntityManagerFactory().createEntityManager();
 
         // anno
         Calendar calendar = Calendar.getInstance();
         Integer year = calendar.get(Calendar.YEAR);
         map.put("anno", year.toString());
 
         // giunta
         Giunta giuntaCorrente = SuiteUtil.trovaGiuntaCorrente();
         map.put("giunta", giuntaCorrente.getNumero());
 
         // codici sezione e numerici
         String formulacodifica = tipoPratica.getFormulacodifica();
         List<TipoPratica> tipi = new ArrayList();
         TipoPratica iter = tipoPratica;
         while( iter != null ){
             tipi.add(0, iter);
             iter = iter.getTipopadre();
         }
 
         Integer n = tipoPratica.getLunghezzaprogressivo();
 
         // calcolo progressivi per nodo
         Integer i = 0;
         for( TipoPratica tipo: tipi ){
             i++;
             if (n>0) {
             String sql = "select max(substring(p.codiceinterno, length(p.codiceinterno) - " + tipoPratica.getLunghezzaprogressivo().toString() + " + 1)) " +
                                 "from Pratica p join p.tipo t";
             sql += " where p.codiceinterno like '"+tipo.getCodice()+"%'";
             if( tipoPratica.getProgressivoanno() ){
                 sql += " and t.progressivoanno = TRUE";
                 sql += " and p.anno = " + year.toString();
             }
             if( tipoPratica.getProgressivogiunta() ){
                 sql += " and t.progressivogiunta = TRUE";
                 sql += " and p.datapratica >= '" + SuiteUtil.DATE_FORMAT.format(giuntaCorrente.getDatanascita()) +"'";
             }
             Query q = em.createQuery(sql);
             String maxString = (String) q.getSingleResult();
             Integer max=1;
             if( maxString != null ){
                 max = Integer.parseInt(maxString.substring(maxString.length() - n));
                 max += 1;
             }
             map.put("n"+i, max);
             }
             map.put("s"+i, tipo.getCodice());
         }
 
         // composizione codifica
         MessageMapFormat mmp = new MessageMapFormat(formulacodifica);
         String codifica = mmp.format(map);
         return codifica;
     }
 
     // Verifica se esiste già una pratica con il codice specificato, x tipologie senza progressivo
     public static Boolean codificaInternaUnivoca(TipoPratica tipoPratica){
         if (tipoPratica.getLunghezzaprogressivo() > 0) {
             return Boolean.TRUE;
         }
 
         String codifica=creaCodificaInterna(tipoPratica);
         Database db = (Database) Register.queryUtility(IDatabase.class);
         EntityManager em = db.getEntityManagerFactory().createEntityManager();
 //        String sql = "select count(p.codiceinterno) from Pratica p where p.codiceinterno = '" + codifica + "'";
         Query q = em.createQuery("select count(p.codiceinterno) from Pratica p where p.codiceinterno = '" + codifica + "'");
         Long i = (Long) q.getSingleResult();
         return (i == 0);
     }
 
     public static Validation protocollaDetermina(Determina determina){
         Pratica pratica = determina.getPratica();
         Database db = (Database) Register.queryUtility(IDatabase.class);
 
         Long idOggettoDeterminazione = Long.parseLong(SuiteUtil.trovaCostante("OGGETTO_DETERMINAZIONE").getValore());
         Controller controllerOggetto = db.createController(Oggetto.class);
         Oggetto oggetto = (Oggetto) controllerOggetto.get(idOggettoDeterminazione);
         Long idUfficioRagioneria = Long.parseLong(SuiteUtil.trovaCostante("UFFICIO_RAGIONERIA_E_CONTABILITA").getValore());
         Controller controllerUfficio = db.createController(Ufficio.class);
         Ufficio ragioneria = (Ufficio) controllerUfficio.get(idUfficioRagioneria);
 
         // come sportello l'ufficio del servizio principale
         Ufficio sportello = null;
         for( ServizioDetermina servizioDetermina: determina.getServizioDeterminaCollection() ){
             if( servizioDetermina.getPrincipale() ){
                 sportello = servizioDetermina.getServizio().getUfficio();
                 break;
             }
         }
 
         // di spesa: come attribuzione principale l'ufficio ragioneria, e poi la principale della attribuzioni della determina
         // non di spesa: come attribuzioni le attribuzioni della determina (per prima la principale)
         List<Ufficio> attribuzioni = new ArrayList<Ufficio>();
         if( determina.getDispesa() ){
             attribuzioni.add(ragioneria);
         }
         for(UfficioDetermina ufficioDetermina: determina.getUfficioDeterminaCollection() ){
             if( ufficioDetermina.getPrincipale() ){
                 attribuzioni.add(ufficioDetermina.getUfficio());
             }
         }
         if( !determina.getDispesa() ){
             for(UfficioDetermina ufficioDetermina: determina.getUfficioDeterminaCollection() ){
                 if( !ufficioDetermina.getPrincipale() ){
                     attribuzioni.add(ufficioDetermina.getUfficio());
                 }
             }
 
         }
 
         List<Ufficio> uffici = new ArrayList<Ufficio>();
         uffici.add(sportello);
 
         return protocollaPratica(pratica, sportello, determina.getOggetto(), attribuzioni, oggetto, null, uffici, TipoProtocollo.INTERNO);
     }
 
     public static Validation inserisciAttribuzioniProtocolloDetermina(Determina determina) {
 
         Protocollo protocollo = determina.getProtocollo();
 
         // la lista degli uffici della determina (il primo è il principale)
         Collection<UfficioDetermina> ufficioDeterminaCollection = determina.getUfficioDeterminaCollection();
         List<Ufficio> ufficiDetermina = new ArrayList<Ufficio>();
         for( UfficioDetermina ufficioDetermina: ufficioDeterminaCollection ){
             if( ufficioDetermina.getPrincipale() ){
                 ufficiDetermina.add(0, ufficioDetermina.getUfficio());
             } else {
                 ufficiDetermina.add(ufficioDetermina.getUfficio());
             }
         }
 
         // la lista degli uffici attribuzioni già presenti sul protocollo (non mi interessa il principale)
         Collection<Attribuzione> attribuzioneCollection = protocollo.getAttribuzioneCollection();
         List<Ufficio> ufficiProtocollo = new ArrayList<Ufficio>();
         for( Attribuzione attribuzione: attribuzioneCollection ){
             ufficiProtocollo.add(attribuzione.getUfficio());
         }
 
         // la lista degli uffici attribuzione nuovi
         List<Attribuzione> attribuzioni = new ArrayList<Attribuzione>();
         Boolean principale = Boolean.TRUE;
         for( Ufficio ufficio: ufficiDetermina ){
             Attribuzione attribuzione = new Attribuzione();
             attribuzione.setUfficio(ufficio);
             if( principale ){
                 attribuzione.setPrincipale(Boolean.TRUE);
                 principale = Boolean.FALSE;
             }
             attribuzioni.add(attribuzione);
         }
         for( Ufficio ufficio: ufficiProtocollo ){
             if( !ufficiDetermina.contains(ufficio) ){
                 Attribuzione attribuzione = new Attribuzione();
                 attribuzione.setUfficio(ufficio);
                 attribuzione.setPrincipale(Boolean.FALSE);
                 attribuzioni.add(attribuzione);
             }
         }
 
         protocollo.setAttribuzioneCollection(attribuzioni);
 
         Database db = (Database) Register.queryUtility(IDatabase.class);
         Controller controller = db.createController(Protocollo.class);
         Validation validation = controller.commit(protocollo);
 
         return validation;
     }
 
     // protocollazione della pratica
     public static Validation protocollaPratica(Pratica pratica, Ufficio sportello, String oggettoProtocollo, List<Ufficio> attribuzioni){
         return protocollaPratica(pratica, sportello, oggettoProtocollo, attribuzioni, null, null, null, TipoProtocollo.INTERNO);
     }
     public static Validation protocollaPratica(Pratica pratica, Ufficio sportello, String oggettoProtocollo, List<Ufficio> attribuzioni,
                                                Oggetto oggetto){
         return protocollaPratica(pratica, sportello, oggettoProtocollo, attribuzioni, oggetto, null, null, TipoProtocollo.INTERNO);
     }
     public static Validation protocollaPratica(Pratica pratica, Ufficio sportello, String oggettoProtocollo, List<Ufficio> attribuzioni,
                                                Oggetto oggetto, List<Soggetto> soggetti){
         return protocollaPratica(pratica, sportello, oggettoProtocollo, attribuzioni, oggetto, soggetti, null, TipoProtocollo.INTERNO);
     }
     public static Validation protocollaPratica(Pratica pratica, Ufficio sportello, String oggettoProtocollo, List<Ufficio> attribuzioni,
                                                Oggetto oggetto, List<Soggetto> soggetti, List<Ufficio> uffici){
         return protocollaPratica(pratica, sportello, oggettoProtocollo, attribuzioni, oggetto, soggetti, uffici, TipoProtocollo.INTERNO);
     }
     public static Validation protocollaPratica(Pratica pratica, Ufficio sportello, String oggettoProtocollo, List<Ufficio> attribuzioni,
                                                Oggetto oggetto, List<Soggetto> soggetti, List<Ufficio> uffici, TipoProtocollo tipo){
         Protocollo protocollo = new Protocollo();
         protocollo.setOggetto(oggettoProtocollo);
         protocollo.setSportello(sportello);
         protocollo.setTipo(tipo);
         // attribuzioni
         List<Attribuzione> attribuzioniList = new ArrayList<Attribuzione>();
         Boolean isFirst=true;
         for( Ufficio ufficio: attribuzioni ){
             Attribuzione attribuzione = new Attribuzione();
             attribuzione.setUfficio(ufficio);
             attribuzione.setPrincipale(isFirst);
             if( isFirst){
                 isFirst = false;
             }
             attribuzioniList.add(attribuzione);
         }
         protocollo.setAttribuzioneCollection(attribuzioniList);
         // uffici mittenti o destinatari
         if( uffici != null ){
             List<UfficioProtocollo> ufficioProtocolloList = new ArrayList<UfficioProtocollo>();
             for( Ufficio attribuzione: uffici ){
                 UfficioProtocollo ufficioProtocollo = new UfficioProtocollo();
                 ufficioProtocollo.setUfficio(attribuzione);
                 ufficioProtocolloList.add(ufficioProtocollo);
             }
             protocollo.setUfficioProtocolloCollection(ufficioProtocolloList);
         }
         // soggetti
         if( soggetti != null ){
             List<SoggettoProtocollo> soggettiProtocollo = new ArrayList<SoggettoProtocollo>();
             for( Soggetto soggetto: soggetti ){
                 SoggettoProtocollo soggettoProtocollo = new SoggettoProtocollo();
                 soggettoProtocollo.setSoggetto(soggetto);
             }
             protocollo.setSoggettoProtocolloCollection(soggettiProtocollo);
         }
         // inserimento protocollo nella pratica
         Collection<PraticaProtocollo> pratiche = new ArrayList<PraticaProtocollo>();
         PraticaProtocollo pp = new PraticaProtocollo();
         pp.setPratica(pratica);
         pp.setOggetto(oggetto);
         pp.setOriginale(true);
         pratiche.add(pp);
         protocollo.setPraticaProtocolloCollection(pratiche);
         // commit
         Database db = (Database) Register.queryUtility(IDatabase.class);
         Controller controller = db.createController(Protocollo.class);
         Validation validation = controller.commit(protocollo);
         if( validation.getResponse() ){
             // creo la cartella
             CmisPlugin plugin = (CmisPlugin) Register.queryPlugin(protocollo.getClass(), "CMIS");
             AlfrescoHelper helper = plugin.createAlfrescoHelper(protocollo);
             helper.createFolder();
         }
        // convalida
        protocollo.setConvalidaattribuzioni(Boolean.TRUE);
        protocollo.setConvalidaprotocollo(Boolean.TRUE);
         return validation;
     }
 
     public static IDettaglio trovaDettaglioDaPratica(Pratica pratica) {
         Procedimento procedimento = pratica.getTipo().getProcedimento();
         if( procedimento == null ){
             return null;
         }
         String className = procedimento.getTipodettaglio();
         if( className == null || className.length()==0 ){
             return null;
         }
         try {
             Class<?> klass = Class.forName(className);
             Database db = (Database) Register.queryUtility(IDatabase.class);
             EntityManager em = db.getEntityManagerFactory().createEntityManager();
             CriteriaBuilder cb = em.getCriteriaBuilder();
             CriteriaQuery<Object> cq = cb.createQuery();
             Class<?> returnType = klass;
             Root from = cq.from(returnType);
             Predicate predicate = cb.equal(from.get("pratica"), pratica);
             cq.select(from);
             cq.where(predicate);
             TypedQuery<Object> tq = em.createQuery(cq);
             IDettaglio dettaglio = (IDettaglio) tq.getSingleResult();
             return dettaglio;
         } catch (ClassNotFoundException e) {
             return null;
         }
     }
 
     public static Boolean eseguiDettaglioEsterno(Pratica pratica){
         Procedimento procedimento = pratica.getTipo().getProcedimento();
         if( procedimento == null ){
             return false;
         }
         String dettaglio = procedimento.getTipodettaglio();
         if( dettaglio.startsWith("exec ") ){
             dettaglio = dettaglio.substring(5);
         }
         Map<String, String> map = new HashMap<String, String>();
         Utente autenticato = (Utente) Register.queryUtility(IUtente.class);
         map.put("idpratica", pratica.getIdpratica());
         map.put("idutente", autenticato.getId().toString());
         MessageMapFormat mmp = new MessageMapFormat(dettaglio);
         String comando = mmp.format(map);
         int execute = QProcess.execute(comando);
         return execute == 0;
     }
 
 }
