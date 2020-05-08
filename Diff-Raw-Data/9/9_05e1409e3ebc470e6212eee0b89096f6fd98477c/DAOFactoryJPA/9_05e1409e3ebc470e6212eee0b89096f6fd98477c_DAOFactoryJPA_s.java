 package amu.licence.edt.model.dao.jpa;
 
 import java.awt.Color;
 import java.util.HashSet;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 
 import amu.licence.edt.model.beans.Admin;
 import amu.licence.edt.model.beans.CRoom;
 import amu.licence.edt.model.beans.CRoomType;
 import amu.licence.edt.model.beans.Group;
 import amu.licence.edt.model.beans.Level;
 import amu.licence.edt.model.beans.Promo;
 import amu.licence.edt.model.beans.Rank;
 import amu.licence.edt.model.beans.SessionType;
 import amu.licence.edt.model.beans.TU;
 import amu.licence.edt.model.beans.Teacher;
 import amu.licence.edt.model.dao.DAOAdmin;
 import amu.licence.edt.model.dao.DAOCRoom;
 import amu.licence.edt.model.dao.DAOCRoomType;
 import amu.licence.edt.model.dao.DAOFactory;
 import amu.licence.edt.model.dao.DAOGroup;
 import amu.licence.edt.model.dao.DAOLevel;
 import amu.licence.edt.model.dao.DAOPromo;
 import amu.licence.edt.model.dao.DAORank;
 import amu.licence.edt.model.dao.DAOSession;
 import amu.licence.edt.model.dao.DAOSessionType;
 import amu.licence.edt.model.dao.DAOTU;
 import amu.licence.edt.model.dao.DAOTeacher;
 import amu.licence.edt.model.dao.DAOUnavailability;
 
 public class DAOFactoryJPA implements DAOFactory {
 
     private static DAOFactoryJPA instance = null;
 
     public static DAOFactoryJPA getInstance() {
         if (instance == null)
             instance = new DAOFactoryJPA();
         return instance;
     }
 
     private final EntityManagerFactory entityManagerFactory =
                         Persistence.createEntityManagerFactory("schedulePU");
     private EntityManager entityManager;
 
     private DAOAdmin          daoAdmin;
     private DAOCRoom          daoCRoom;
     private DAOCRoomType      daoCRoomType;
     private DAOGroup          daoGroup;
     private DAOLevel          daoLevel;
     private DAOPromo          daoPromo;
     private DAORank           daoRank;
     private DAOSession        daoSession;
     private DAOSessionType    daoSessionType;
     private DAOTeacher        daoTeacher;
     private DAOTU             daoTU;
     private DAOUnavailability daoUnavailability;
 
     private DAOFactoryJPA() {
         entityManager = entityManagerFactory.createEntityManager();
     }
 
     @Override
     public void finish() {
         entityManager.close();
         entityManagerFactory.close();
     }
 
     @Override
     public DAOAdmin getDAOAdmin() {
         if (daoAdmin == null)
             daoAdmin = new DAOAdminJPA(entityManager);
         return daoAdmin;
     }
 
     @Override
     public DAOCRoom getDAOCRoom() {
         if (daoCRoom == null)
             daoCRoom = new DAOCRoomJPA(entityManager);
         return daoCRoom;
     }
 
     @Override
     public DAOCRoomType getDAOCRoomType() {
         if (daoCRoomType == null)
             daoCRoomType = new DAOCRoomTypeJPA(entityManager);
         return daoCRoomType;
     }
 
     @Override
     public DAOGroup getDAOGroup() {
         if (daoGroup == null)
             daoGroup = new DAOGroupJPA(entityManager);
         return daoGroup;
     }
 
     @Override
     public DAOLevel getDAOLevel() {
         if (daoLevel == null)
             daoLevel = new DAOLevelJPA(entityManager);
         return daoLevel;
     }
 
     @Override
     public DAOPromo getDAOPromo() {
         if (daoPromo == null)
             daoPromo = new DAOPromoJPA(entityManager);
         return daoPromo;
     }
 
     @Override
     public DAORank getDAORank() {
         if (daoRank == null)
             daoRank = new DAORankJPA(entityManager);
         return daoRank;
     }
 
     @Override
     public DAOSession getDAOSession() {
         if (daoSession == null)
             daoSession = new DAOSessionJPA(entityManager);
         return daoSession;
     }
 
     @Override
     public DAOSessionType getDAOSessionType() {
         if (daoSessionType == null)
             daoSessionType = new DAOSessionTypeJPA(entityManager);
         return daoSessionType;
     }
 
     @Override
     public DAOTeacher getDAOTeacher() {
         if (daoTeacher == null)
             daoTeacher = new DAOTeacherJPA(entityManager);
         return daoTeacher;
     }
 
     @Override
     public DAOTU getDAOTU() {
         if (daoTU == null)
             daoTU = new DAOTUJPA(entityManager);
         return daoTU;
     }
 
     @Override
     public DAOUnavailability getDAOUnavailability() {
         if (daoUnavailability == null)
             daoUnavailability = new DAOUnavailabilityJPA(entityManager);
         return daoUnavailability;
     }
 
     @Override
     public void generateExampleTuples() {
 
         EntityTransaction tr = entityManager.getTransaction();
         tr.begin();
         System.out.println("Generating objects and persisting them...");
 
         /* Ranks */
         System.out.println("Ranks");
         Rank r1 = new Rank("Maître de conférences");
         Rank r2 = new Rank("Professeur");
         Rank r3 = new Rank("PRAG");
         Rank r4 = new Rank("PAST");
         Rank r5 = new Rank("ATER");
         Rank r6 = new Rank("Moniteur");
         entityManager.persist(r1);
         entityManager.persist(r2);
         entityManager.persist(r3);
         entityManager.persist(r4);
         entityManager.persist(r5);
         entityManager.persist(r6);
         /* */
 
         /* Levels */
         System.out.println("Levels");
         Level l1 = new Level("L1", "Licence 1re année", null);
         Level l2 = new Level("L2", "Licence 2e année",  null);
         Level l3 = new Level("L3", "Licence 3e année",  null);
         Level m1 = new Level("M1", "Master 1re année",  null);
         Level m2 = new Level("M2", "Master 2e année",   null);
         entityManager.persist(l1);
         entityManager.persist(l2);
         entityManager.persist(l3);
         entityManager.persist(m1);
         entityManager.persist(m2);
         /* */
 
         /* Admins */
         System.out.println("Admins");
         Admin a1 = new Admin("ladmin", "ladmin", false, new HashSet<Level>());
         Admin a2 = new Admin("madmin", "madmin", false, new HashSet<Level>());
         Admin a3 = new Admin("tadmin", "tadmin", true , new HashSet<Level>());
         Admin a4 = new Admin("aadmin", "aadmin", true , new HashSet<Level>());
         a1.addLevel(l1);
         a1.addLevel(l2);
         a1.addLevel(l3);
         a2.addLevel(m1);
         a2.addLevel(m2);
         a4.addLevel(l1);
         a4.addLevel(l2);
         a4.addLevel(l3);
         a4.addLevel(m1);
         a4.addLevel(m2);
         entityManager.persist(a1);
         entityManager.persist(a2);
         entityManager.persist(a3);
         entityManager.persist(a4);
         /* */
 
         /* Teachers */
         System.out.println("Teachers");
         Teacher talbot   = new Teacher("talbot",   "talbot",   false, new HashSet<Level>(), "Jean-Marc TALBOT",        "Luminy",    null, null, 0, r3);
         Teacher massat   = new Teacher("massat",   "massat",   false, new HashSet<Level>(), "Jean-Luc MASSAT",         "Luminy",    null, null, 0, r3);
         Teacher jamet    = new Teacher("jamet",    "jamet",    false, new HashSet<Level>(), "Line JAKUBIEC-JAMET",     "Luminy",    null, null, 0, r2);
         Teacher sabatier = new Teacher("sabatier", "sabatier", false, new HashSet<Level>(), "Claude SABATIER",         "Luminy",    null, null, 0, r2);
         Teacher chepoi   = new Teacher("chepoi",   "chepoi",   false, new HashSet<Level>(), "Victor CHEPOI",           "Luminy",    null, null, 0, r2);
         Teacher nouioua  = new Teacher("nouioua",  "nouioua",  false, new HashSet<Level>(), "Karim NOUIOUA",           "Luminy",    null, null, 0, r3);
         Teacher nasr     = new Teacher("nasr",     "nasr",     false, new HashSet<Level>(), "Alexis NASR",             "Luminy",    null, null, 0, r2);
         Teacher bechet   = new Teacher("bechet",   "bechet",   false, new HashSet<Level>(), "Frederique BECHET",       "Luminy",    null, null, 0, r3);
         Teacher estellon = new Teacher("estellon", "estellon", false, new HashSet<Level>(), "Bertrand ESTELLON",       "Luminy",    null, null, 0, r2);
         Teacher ritz     = new Teacher("ritz",     "ritz",     false, new HashSet<Level>(), "Christophe RITZENTHALER", "Extérieur", null, null, 0, r2);
         Teacher avell    = new Teacher("avell",    "avell",    false, new HashSet<Level>(), "Florent AVELLANEDA",      "Extérieur", null, null, 0, r6);
         entityManager.persist(talbot);
         entityManager.persist(massat);
         entityManager.persist(jamet);
         entityManager.persist(sabatier);
         entityManager.persist(chepoi);
         entityManager.persist(nouioua);
         entityManager.persist(nasr);
         entityManager.persist(bechet);
         entityManager.persist(estellon);
         entityManager.persist(ritz);
         entityManager.persist(avell);
         /* */
 
         /* Promos */
         System.out.println("Promos");
         Promo p1 = new Promo(60, l3, new HashSet<Group>());
         Promo p2 = new Promo(60, l3, new HashSet<Group>());
         Promo p3 = new Promo(60, l3, new HashSet<Group>());
         Promo p4 = new Promo(60, l3, new HashSet<Group>());
         Promo p5 = new Promo(60, l3, new HashSet<Group>());
         p1.setLevel(l1);
         p2.setLevel(l2);
         p3.setLevel(l3);
         p4.setLevel(m1);
         p5.setLevel(m2);
         entityManager.persist(p1);
         entityManager.persist(p2);
         entityManager.persist(p3);
         entityManager.persist(p4);
         entityManager.persist(p5);
         /* */
 
         /* Groups */
         System.out.println("Groups");
         Group g1 = new Group(30, p1);
         Group g2 = new Group(30, p1);
         p1.addGroup(g1);
         p1.addGroup(g2);
         entityManager.persist(g1);
         entityManager.persist(g2);
         /* */
 
         /* TUs */
         System.out.println("TUs");
         TU compil  = new TU("ENSIN6U1", "Compilation",                            Color.getColor("#CC9933"), 20, 20, 20, 0  , l3, nasr,     new HashSet<Teacher>());
         TU logique = new TU("ENSIN6U2", "Logique et calculabilité",               Color.getColor("#FF9900"), 20, 30, 0 , 0  , l3, jamet,    new HashSet<Teacher>());
         TU syst    = new TU("ENSIN6U3", "Systèmes d'exploitation",                Color.getColor("#FFFF99"), 20, 20, 20, 0  , l3, massat,   new HashSet<Teacher>());
         TU geoAlgo = new TU("ENSIN6U4", "Géométrie algorithmique",                Color.getColor("#CC6666"), 56, 0 , 20, 0  , l3, chepoi,   new HashSet<Teacher>());
         TU devWeb  = new TU("ENSIN6U5", "Développement web 2",                    Color.getColor("#6699FF"), 28, 0 , 56, 0  , l3, estellon, new HashSet<Teacher>());
         TU crypto  = new TU("ENSMI6U6", "Cryptographie",                          Color.getColor("#33FFFF"), 28, 26, 24, 0  , l3, ritz,     new HashSet<Teacher>());
         TU tal     = new TU("ENSMI6U7", "Langage naturel",                        Color.getColor("#66FFFF"), 0 , 0 , 0 , 0  , l3, nasr,     new HashSet<Teacher>());
         TU pPOO    = new TU("ENSIN6U8", "Projet de Programmation Orientée Objet", Color.getColor("#00CC99"), 4 , 0 , 0 , 100, l3, avell,    new HashSet<Teacher>());
         TU pApp    = new TU("ENSIN6U9", "Projet informatique appliqué",           Color.getColor("#00CC99"), 6 , 0 , 0 , 80 , l3, sabatier, new HashSet<Teacher>());
         compil.addCompetentTeacher(nasr);
         compil.addCompetentTeacher(bechet);
         logique.addCompetentTeacher(jamet);
         logique.addCompetentTeacher(sabatier);
         syst.addCompetentTeacher(massat);
         syst.addCompetentTeacher(talbot);
         geoAlgo.addCompetentTeacher(chepoi);
         geoAlgo.addCompetentTeacher(nouioua);
         devWeb.addCompetentTeacher(estellon);
         crypto.addCompetentTeacher(ritz);
         tal.addCompetentTeacher(nasr);
         tal.addCompetentTeacher(bechet);
         pPOO.addCompetentTeacher(avell);
         pApp.addCompetentTeacher(jamet);
         pApp.addCompetentTeacher(sabatier);
         entityManager.persist(compil);
         entityManager.persist(logique);
         entityManager.persist(syst);
         entityManager.persist(geoAlgo);
         entityManager.persist(devWeb);
         entityManager.persist(crypto);
         entityManager.persist(tal);
         entityManager.persist(pPOO);
         entityManager.persist(pApp);
         /* */
 
        for (Teacher t : syst.getCompetentTeachers()) {
            System.out.println(t.getName());
        }

         /* SessionTypes */
         System.out.println("SessionTypes");
         SessionType sessionTD = new SessionType("TD", 1, new HashSet<CRoomType>());
        SessionType sessionTP = new SessionType("TP", 1, new HashSet<CRoomType>());
        SessionType sessionCours = new SessionType("Lecture", 1, new HashSet<CRoomType>());
         SessionType sessionProjet = new SessionType("Project", 1, new HashSet<CRoomType>());
         entityManager.persist(sessionTD);
         entityManager.persist(sessionTP);
         entityManager.persist(sessionCours);
         entityManager.persist(sessionProjet);
         /* */
 
         /* CRoomTypes */
         System.out.println("CRoomTypes");
         CRoomType salleTD = new CRoomType("TD", new HashSet<SessionType>());
         CRoomType salleTP = new CRoomType("TP", new HashSet<SessionType>());
         CRoomType amphi   = new CRoomType("Amphi", new HashSet<SessionType>());
         salleTD.addCompatibleSessionType(sessionTD);
         salleTD.addCompatibleSessionType(sessionCours);
         salleTD.addCompatibleSessionType(sessionProjet);
         salleTP.addCompatibleSessionType(sessionTP);
         salleTP.addCompatibleSessionType(sessionProjet);
         amphi.addCompatibleSessionType(sessionCours);
         amphi.addCompatibleSessionType(sessionTD);
         entityManager.persist(salleTD);
         entityManager.persist(salleTP);
         entityManager.persist(amphi);
         /* */
 
         /* CRooms */
         System.out.println("CRooms");
         CRoom cr1  = new CRoom("Amphi.1",  80, amphi);
         CRoom cr2  = new CRoom("Amphi.2",  80, amphi);
         CRoom cr3  = new CRoom("Amphi.3",  80, amphi);
         CRoom cr4  = new CRoom("Amphi.4",  80, amphi);
         CRoom cr5  = new CRoom("Amphi.5",  80, amphi);
         CRoom cr6  = new CRoom("Amphi.6",  80, amphi);
         CRoom cr7  = new CRoom("Amphi.7",  80, amphi);
         CRoom cr8  = new CRoom("Amphi.8",  80, amphi);
         CRoom cr9  = new CRoom("Amphi.9",  80, amphi);
         CRoom cr10 = new CRoom("Amphi.10", 80, amphi);
         CRoom cr11 = new CRoom("Amphi.11", 80, amphi);
         CRoom cr12 = new CRoom("1.05B",    30, salleTD);
         CRoom cr13 = new CRoom("1.06B",    30, salleTD);
         CRoom cr14 = new CRoom("1.07B",    30, salleTD);
         CRoom cr15 = new CRoom("1.08B",    30, salleTD);
         CRoom cr16 = new CRoom("103A",     30, salleTD);
         CRoom cr17 = new CRoom("104H",     30, salleTD);
         CRoom cr18 = new CRoom("105H",     30, salleTD);
         CRoom cr19 = new CRoom("107A",     30, salleTD);
         CRoom cr20 = new CRoom("109A",     30, salleTD);
         CRoom cr21 = new CRoom("110H",     30, salleTD);
         CRoom cr22 = new CRoom("111H",     30, salleTD);
         CRoom cr23 = new CRoom("116H",     30, salleTD);
         CRoom cr24 = new CRoom("117H",     30, salleTD);
         CRoom cr25 = new CRoom("119A",     30, salleTD);
         CRoom cr26 = new CRoom("120H",     30, salleTD);
         CRoom cr27 = new CRoom("121H",     30, salleTD);
         CRoom cr28 = new CRoom("123A",     30, salleTD);
         CRoom cr29 = new CRoom("127A",     30, salleTD);
         CRoom cr30 = new CRoom("3.01B",    30, salleTD);
         CRoom cr31 = new CRoom("3.02B",    30, salleTD);
         CRoom cr32 = new CRoom("3.03B",    30, salleTD);
         CRoom cr33 = new CRoom("3.04B",    30, salleTD);
         CRoom cr34 = new CRoom("3.05B",    30, salleTD);
         CRoom cr35 = new CRoom("3017",     30, salleTD);
         CRoom cr36 = new CRoom("BU3A ",    30, salleTD);
         CRoom cr37 = new CRoom("113A ",    10, salleTD);
         CRoom cr38 = new CRoom("115A ",    10, salleTD);
         CRoom cr39 = new CRoom("TP - A",   20, salleTP);
         CRoom cr40 = new CRoom("TP - B",   20, salleTP);
         CRoom cr41 = new CRoom("TP - C",   20, salleTP);
         CRoom cr42 = new CRoom("TP - D",   20, salleTP);
         CRoom cr43 = new CRoom("TP - E",   20, salleTP);
         CRoom cr44 = new CRoom("TP - F",   20, salleTP);
         CRoom cr45 = new CRoom("0.01B",    25, salleTP);
         CRoom cr46 = new CRoom("0.02B",    25, salleTP);
         CRoom cr47 = new CRoom("0.03B",    25, salleTP);
         CRoom cr48 = new CRoom("0.04B",    25, salleTP);
         CRoom cr49 = new CRoom("0.05B",    25, salleTP);
         entityManager.persist(cr1 );
         entityManager.persist(cr2 );
         entityManager.persist(cr3 );
         entityManager.persist(cr4 );
         entityManager.persist(cr5 );
         entityManager.persist(cr6 );
         entityManager.persist(cr7 );
         entityManager.persist(cr8 );
         entityManager.persist(cr9 );
         entityManager.persist(cr10);
         entityManager.persist(cr11);
         entityManager.persist(cr12);
         entityManager.persist(cr13);
         entityManager.persist(cr14);
         entityManager.persist(cr15);
         entityManager.persist(cr16);
         entityManager.persist(cr17);
         entityManager.persist(cr18);
         entityManager.persist(cr19);
         entityManager.persist(cr20);
         entityManager.persist(cr21);
         entityManager.persist(cr22);
         entityManager.persist(cr23);
         entityManager.persist(cr24);
         entityManager.persist(cr25);
         entityManager.persist(cr26);
         entityManager.persist(cr27);
         entityManager.persist(cr28);
         entityManager.persist(cr29);
         entityManager.persist(cr30);
         entityManager.persist(cr31);
         entityManager.persist(cr32);
         entityManager.persist(cr33);
         entityManager.persist(cr34);
         entityManager.persist(cr35);
         entityManager.persist(cr36);
         entityManager.persist(cr37);
         entityManager.persist(cr38);
         entityManager.persist(cr39);
         entityManager.persist(cr40);
         entityManager.persist(cr41);
         entityManager.persist(cr42);
         entityManager.persist(cr43);
         entityManager.persist(cr44);
         entityManager.persist(cr45);
         entityManager.persist(cr46);
         entityManager.persist(cr47);
         entityManager.persist(cr48);
         entityManager.persist(cr49);
         /* */
 
         /* Sessions */
         // TODO
         /* */
 
         System.out.println("Committing...");
         tr.commit();
 //        tr.rollback();
         System.out.println("Done");
 
     }
 
 }
