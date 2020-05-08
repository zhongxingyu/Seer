 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package apacheJiraMiner.miner;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.Date;
 import apacheJiraMiner.pojo.Projeto;
 import apacheJiraMiner.util.Conn;
 import apacheJiraMiner.util.Util;
 
 /**
  *
  * @author Douglas
  */
 public class HttpProjetosMiner {
 
     private String stringUrl;
     private String logFile;
 
     public HttpProjetosMiner() {
         stringUrl = "https://issues.apache.org/jira/secure/BrowseProjects.jspa#all";
         logFile = "log/" + "log-projetos" + ".txt";
     }
 
     /**
      * Faz a mineração de todos os projetos do JIRA
      * @throws Exception 
      */
     public void minerarProjetos() throws Exception {
 
         URL url = new URL(stringUrl);
 
         System.out.println("---- Conectando a URL : " + stringUrl);
         BufferedReader dis = Util.abrirStream(url);
         System.out.println("---- Conectado a URL : " + stringUrl);
         System.out.println("");
         System.out.println("-----------------------------------------");
         System.out.println("Iniciando a mineração dos Projetos");
         System.out.println("-----------------------------------------\n");
         Util.writeToFile(logFile, "Início da mineração: " + new Date() + "\n");
 
         lerPaginaHtml(dis);
 
         Util.writeToFile(logFile, "Fim da mineração: " + new Date() + "\n");
         System.out.println("-----------------------------------------");
         System.out.println("Terminado a mineração dos Projetos");
         System.out.println("-----------------------------------------\n");
     }
 
     private void lerPaginaHtml(BufferedReader dis) throws Exception {
         String linha = dis.readLine();
         System.out.println("---- Iniciado leitura de página HTML: \n");
         while (linha != null && !linha.contains("</html>")) {
             if (linha.contains("<h3>")) {
                 lerGrupoDeProjetos(dis, linha);
             }
             linha = dis.readLine();
         }
         System.out.println("\n---- Finalizado leitura de página HTML: \n");
     }
 
     private void lerGrupoDeProjetos(BufferedReader dis, String linha) throws Exception {
         String grupoProjto = getNomeGrupoProjeto(linha);
         System.err.println("\n-------- Inicio dos Projetos do Grupo: " + grupoProjto + " ------\n");
         linha = dis.readLine();
         while (dis.ready() && !linha.contains("class=\"mod-header plain\"")) {
             if (linha.contains("<tbody") && !grupoProjto.contains("Recent Projects")) {
                 lerProjetosDoGrupo(dis, linha, grupoProjto);
             }
             linha = dis.readLine();
         }
         System.err.println("\n-------- Término dos Projetos do Grupo: " + grupoProjto + " ------\n");
     }
 
     private void lerProjetosDoGrupo(BufferedReader dis, String linha, String grupoProjto) throws Exception {
         while (dis.ready() && !linha.contains("</table>")) {

            if (linha.contains("<a") && linha.contains("/jira/browse/")) {
                lerProjeto(dis, linha, grupoProjto);
             }

             linha = dis.readLine();
         }
     }
 
     private void lerProjeto(BufferedReader dis, String linha, String grupoProjto) throws Exception {
         Projeto projeto = new Projeto();
         System.out.println("\n------------- Cadastrando Novo Projeto ---------------");
         while (dis.ready() && !linha.contains("<img") && !linha.contains("</tbody>")) {
             projeto.setGrupoProjeto(grupoProjto);
             if (linha.contains("<a") && linha.contains("href=\"/jira/browse/")) {
                 projeto.setNome(getNomeProjeto(linha));
                 projeto.setLinkIssue(getLinkIssueProjeto(linha));
                 projeto.setxKey(getKeyProjeto(linha));
             } else if (linha.contains("class=\"user-hover\"")) {
                 projeto.setProjectLead(getLeadProjeto(linha));
             } else if (linha.contains("<a") && linha.contains("href=\"http")) {
                 projeto.setUrl(getUrlProjeto(linha));
             }
             linha = dis.readLine();
         }
         if (Conn.daoProjeto.insere(projeto)) {
             Util.writeToFile(logFile, "Registrado projeto: " + projeto.getNome());
         } else {
             Util.writeToFile(logFile, "Erro ao gravar projeto: " + projeto.getNome());
         }
         System.out.println("--- Cadastrado Projeto: " + projeto.getNome() + " ---");
         System.out.println("------------------------------------------------------\n");
     }
 
     private String getNomeProjeto(String linha) throws Exception {
         //<a href="/jira/browse/IVY">Ivy</a>
         String[] partes = linha.split(">");
         partes = partes[1].split("<"); // Ivy</a>
         return partes[0]; // Ivy
     }
 
     private String getKeyProjeto(String linha) throws Exception {
         //<a href="/jira/browse/IVY">Ivy</a>
         String[] partes = linha.split("/jira/browse/");
         partes = partes[1].split("\">"); // IVY">Ivy</a>
         return partes[0]; // IVY
     }
 
     private String getLeadProjeto(String linha) throws Exception {
         // <a class="user-hover" rel="xavier" id="project_IVY_table_xavier" href="/jira/secure/ViewProfile.jspa?name=xavier">Xavier Hanin</a>
         String[] partes = linha.split(">");
         partes = partes[1].split("<"); // Xavier Hanin</a>
         return partes[0];
     }
 
     private String getLinkIssueProjeto(String linha) throws Exception {
         return "https://issues.apache.org/jira/browse/" + getKeyProjeto(linha) + "-1";
     }
 
     private String getNomeGrupoProjeto(String linha) throws Exception {
         //                          <h3>Ant</h3>
         String[] partes = linha.split("<h3>"); // Ant</h3>
         partes = partes[1].split("</h3>"); // Ant
         return partes[0]; // Ant
     }
 
     private String getUrlProjeto(String linha) throws Exception {
         // <a href="http://ant.apache.org/ivy/">http://ant.apache.org/ivy/</a>
         String[] partes = linha.split(">");
         partes = partes[1].split("<"); // http://ant.apache.org/ivy/</a>
         return partes[0]; // http://ant.apache.org/ivy/
     }
 }
