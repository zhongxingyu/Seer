 
 import brutes.client.ScenesContext;
 import brutes.client.net.ErrorResponseException;
 import brutes.client.net.InvalidResponseException;
 import brutes.client.net.NetworkClient;
 import brutes.client.user.Session;
 import brutes.net.Protocol;
 import brutes.server.db.DatasManager;
 import brutes.server.db.entity.BonusEntity;
 import brutes.server.game.Bonus;
 import brutes.server.net.NetworkServer;
import brutes.server.ui;
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 /**
  *
  * @author Karl
  */
 public class BrutesTestSequence {
 
     private static Thread SERVER = new Thread() {
         @Override
         public void run() {
             try (ServerSocket sockserv = new ServerSocket(42666)) {
 
                 // DEBUG
                 (new File("~$bdd.db")).delete();
                 DatasManager.getInstance("sqlite", "~$bdd.db");
                 DatasManager.populate();
 
                 while (true) {
                     Socket sockcli = sockserv.accept();
                     try (NetworkServer n = new NetworkServer(sockcli)) {
                         n.read();
                     } catch (Exception ex) {
                         Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             } catch (IOException ex) {
                 Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     };
 
     public static void main(String[] args) {
         BrutesTestSequence.SERVER.start();
 
         String token = "";
         int me = -1;
         int ch = -1;
 
         System.out.println("Login");
         try (NetworkClient sut = new NetworkClient(new Socket("localhost", Protocol.CONNECTION_PORT))) {
             token = sut.sendLogin("Kirauks", "root2");
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
         ScenesContext.getInstance().setSession(new Session("localhost", token));
 
         System.out.println("Delete Brute");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             sut.sendDeleteBrute(token);
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println("Create Brute");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             sut.sendCreateBrute(token, "Rauks");
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println("Update Brute");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             sut.sendUpdateBrute(token, "Rauks le Brave");
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println("Get Me");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             me = sut.sendGetMyBruteId(token);
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println("Datas Me");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             sut.getDataBrute(me);
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println("Get Ch");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             ch = sut.sendGetChallengerBruteId(token);
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println("Datas Ch");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             sut.getDataBrute(ch);
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println("Fight Regular");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             sut.sendDoFight(token);
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println("Fight Loose");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             sut.sendCheatFightLoose(token);
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println("Fight Win");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             sut.sendCheatFightWin(token);
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.out.println("Fight Random");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             sut.sendCheatFightRandom(token);
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         double i[] = new double[]{0, 0, 0, 0};
         int n = 100000000;
 
         for (int k = 0; k < n; k++) {
            int l = (int) ui.random(0, 3);
             i[l]++;
         }
         System.out.println("0 = " + i[0] + "x -> " + (i[0] / n * 100) + " %");
         System.out.println("1 = " + i[1] + "x -> " + (i[1] / n * 100) + " %");
         System.out.println("2 = " + i[2] + "x -> " + (i[2] / n * 100) + " %");
         System.out.println("3 = " + i[3] + "x -> " + (i[3] / n * 100) + " %");
 
         System.out.println("+ Select Bonus");
         try {
             double[] levels = new double[11];
             int nb = 5;
 
             for (int j = 0; j < nb; j++) {
                 Bonus bonus = BonusEntity.findMathematicalRandom();
                 levels[(int) bonus.getLevel()]++;
             }
             int l = 0;
             for (double p : levels) {
                 System.out.println("\t$> level=" + (l++) + " : x" + p + " (" + (p / nb * 100) + "%)");
             }
         } catch (IOException | SQLException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         System.out.println("Logout");
         try (NetworkClient sut = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
             sut.sendLogout(token);
         } catch (IOException | ErrorResponseException | InvalidResponseException ex) {
             Logger.getLogger(BrutesTestSequence.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
