 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package apacheJiraMiner.main;
 
 import apacheJiraMiner.miner.HttpIssueMiner;
 import apacheJiraMiner.miner.HttpProjetosMiner;
 import java.io.File;
 import apacheJiraMiner.pojo.Projeto;
 import apacheJiraMiner.util.Conn;
 
 /**
  *
  * @author Douglas
  */
 public class AllProjectsFullMiner {
 
     public static void main(String[] args) {
 
 
         /*
          * este método irá coletar todos os projetos da página: "https://issues.apache.org/jira/secure/BrowseProjects.jspa#all"
          * não tem problema rodar este método várias vezes pois não cadastrará projetos repetidos
          */
         minerarProjetos();
 
 
         /*
          * este método percorrerá todos os projetos cadastrados acima e irá minerar suas Issues e comentários
          */
        minerarIssues();
 
     }
 
     private static void minerarProjetos() {
         Conn.conectarDao();
         HttpProjetosMiner httpProjetos = new HttpProjetosMiner();
         try {
             httpProjetos.minerarProjetos();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         Conn.fecharConexao();
     }
 
     private static boolean projetoJaIniciado(Projeto projeto) {
         File file = new File("src");
         File[] files = file.listFiles();
         for (File fl : files) {
             if (fl.getName().replaceAll(".txt", "").equals(projeto.getNome())
                     || fl.getName().replaceAll(".txt", "").equals(projeto.getxKey())) {
                 return true;
             }
         }
         return false;
     }
 
    private static void minerarIssues() {
        for (int i = 838; i <= 400; i++) {
             Conn.conectarDao();
             Projeto projeto = (Projeto) Conn.daoProjeto.buscaIDint(Projeto.class, i);
             if (projeto != null && !projetoJaIniciado(projeto)) {
                 HttpIssueMiner httpIssues = new HttpIssueMiner(projeto, 1, true, true);
                 try {
                     httpIssues.minerarIssues();
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
                 httpIssues = null;
             }
             projeto = null;
             Conn.fecharConexao();
             System.gc();
         }
     }
 }
