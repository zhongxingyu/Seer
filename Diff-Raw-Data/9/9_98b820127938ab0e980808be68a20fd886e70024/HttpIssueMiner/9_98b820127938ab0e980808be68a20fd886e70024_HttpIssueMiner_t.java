 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package geronimominer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import pojo.ArquivoModificado;
 import pojo.Comentario;
 import pojo.Commits;
 import pojo.Issue;
 import pojo.Projeto;
 import util.Conn;
 import util.Util;
 
 /**
  *
  * @author Douglas
  */
 public class HttpIssueMiner {
 
     private int numeroProximaPagina;
     private Projeto projeto;
     private String logFile;
     private int inexistentes;
     public boolean minerarComentarios;
     public boolean minerarCommits;
 
     public HttpIssueMiner() {
         this.inexistentes = 0;
     }
 
     public HttpIssueMiner(Projeto projeto) {
         this();
         this.logFile = "src/" + projeto.getxKey();
         this.projeto = projeto;
     }
 
     public HttpIssueMiner(Projeto projeto, int numeroProximaPagina, boolean minerarComentarios, boolean minerarCommits) {
         this(projeto);
         this.numeroProximaPagina = numeroProximaPagina;
         this.minerarComentarios = minerarComentarios;
         this.minerarCommits = minerarCommits;
     }
 
     public void atualizarCommits() throws Exception {
         this.logFile += ".commit";
 
         System.out.println("");
         System.out.println("----------------------------------------------");
         System.out.println("Iniciando a mineração dos Commits das Issues");
         System.out.println("----------------------------------------------\n");
         writeToFile(logFile, "Início da mineração: " + new Date() + "\n");
 
         int contadorGC = 0;
 
         for (Issue issue : projeto.getIssues()) {
             if (contadorGC >= 50) {
                 contadorGC = 0;
                 System.gc();
             }
 
             this.numeroProximaPagina = issue.getNumeroIssue();
 
             System.err.println("--------- Iniciando a mineração dos Commits da Issues ---------");
             System.err.println("Issue: " + getUrl());
             System.err.println("---------------------------------------------------------------\n");
 
             System.out.println("---- Conectando a URL : " + getUrl());
             URL urlCommmits = new URL(getUrl() + "?page=com.atlassian.jira.plugin.ext.subversion:subversion-commits-tabpanel#issue-tabs");
             BufferedReader disCommits = Util.abrirStream(urlCommmits);
             System.out.println("---- Conectado a URL : " + getUrl());
            try {
                lerCommits(issue, capturarCodigoHtml(disCommits));
                writeToFile(logFile, "Commits minerados com sucesso da issue: " + issue.getNumeroIssue() + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
                writeToFile(logFile, "*Erro ao minerar commits da issue: " + issue.getNumeroIssue() + "*\n");
            }
             disCommits.close();
 
             System.err.println("--------- Concluido a mineração dos Commits da Issues ---------");
             System.err.println("Issue: " + getUrl());
             System.err.println("---------------------------------------------------------------\n");
 
             disCommits = null;
             urlCommmits = null;
             issue = null;
 
             contadorGC++;
         }
 
         writeToFile(logFile, "Fim da mineração: " + new Date() + "\n");
         System.out.println("----------------------------------------------");
         System.out.println("Terminado a mineração dos Commits das Issues");
         System.out.println("----------------------------------------------\n");
 
         projeto = null;
     }
 
     public void minerarIssues() throws Exception {
 
         System.out.println("");
         System.out.println("-----------------------------------------");
         System.out.println("Iniciando a mineração das Issues");
         System.out.println("-----------------------------------------\n");
         writeToFile(logFile, "Início da mineração: " + new Date() + "\n");
 
         int contadorGC = 0;
 
         while (inexistentes <= 40) {
             if (contadorGC >= 50) {
                 System.gc();
                 contadorGC = 0;
             }
             System.out.println("---- Conectando a URL : " + getUrl());
             URL urlComentarios = new URL(getUrl() + "?page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#issue-tabs");
             URL urlCommmits = new URL(getUrl() + "?page=com.atlassian.jira.plugin.ext.subversion:subversion-commits-tabpanel#issue-tabs");
             BufferedReader disComentarios = Util.abrirStream(urlComentarios);
             BufferedReader disCommits = Util.abrirStream(urlCommmits);
             System.out.println("---- Conectado a URL : " + getUrl());
             lerPaginasHtmls(capturarCodigoHtml(disComentarios), capturarCodigoHtml(disCommits));
             disComentarios.close();
             disCommits.close();
             urlComentarios = null;
             urlCommmits = null;
             disComentarios = null;
             disCommits = null;
             contadorGC++;
         }
 
         writeToFile(logFile, "Fim da mineração: " + new Date() + "\n");
         System.out.println("-----------------------------------------");
         System.out.println("Terminado a mineração das Issues");
         System.out.println("-----------------------------------------\n");
 
         projeto = null;
     }
 
     private void lerPaginasHtmls(List<String> linhasComentarios, List<String> linhasCommits) throws Exception {
         Issue issue = lerIssue(linhasComentarios);
         if (issue != null && issue.getId() != 0) {
             if (minerarComentarios) {
                 lerComentarios(issue, linhasComentarios);
             }
             if (minerarCommits) {
                 lerCommits(issue, linhasCommits);
             }
         }
         issue = null;
         linhasComentarios = null;
         linhasCommits = null;
         numeroProximaPagina++;
     }
 
     private Issue lerIssue(List<String> linhas) {
         Issue issue = new Issue();
         issue.setNumeroIssue(numeroProximaPagina);
         for (int i = 0; i < linhas.size(); i++) {
             if (!pegarDadosIssue(issue, linhas, i)) {
                 return null;
             }
         }
         if (issue.getNome() != null) {
             inexistentes = 0;
             boolean inseriu = false;
             projeto.addIssue(issue);
             if (Conn.daoProjeto.insere(issue)) {
                 if (Conn.daoProjeto.atualiza(projeto)) {
                     System.err.println("-------- Issue cadastrado e adicionado ao Projeto --------");
                     System.err.println("Nome: " + issue.getNome());
                     System.err.println("Numero: " + issue.getNumeroIssue());
                     System.err.println("----------------------------------------------------------\n");
                     writeToFile(logFile, "- Issue " + issue.getNumeroIssue() + " cadastrada e adicionado ao projeto.");
                 } else {
                     projeto.removeIssue(issue);
                     System.err.println("----- Issue cadastrado e *NÃO* adicionado ao Projeto -----");
                     System.err.println("Nome: " + issue.getNome());
                     System.err.println("Numero: " + issue.getNumeroIssue());
                     writeToFile(logFile, "- Issue " + issue.getNumeroIssue() + " cadastrada.");
                     System.err.println("----------------------------------------------------------\n");
                 }
                 return issue;
             }
             if (!inseriu) {
                 System.err.println("---------------- Erro ao cadastrar Issue ----------------");
                 System.err.println("Link: " + getUrl());
                 System.err.println("Numero: " + numeroProximaPagina);
                 System.err.println("----------------------------------------------------------\n");
                 writeToFile(logFile, "- Erro: Issue " + numeroProximaPagina + " não foi cadastrada.");
             }
         }
         return null;
     }
 
     private boolean pegarDadosIssue(Issue issue, List<String> linhas, int i) {
         if (linhas.get(i).contains("<title>Issue Does Not Exist - ASF JIRA </title>")) {
             inexistentes++;
             System.err.println("---------------------------------------------\n");
             System.err.println("A página de Issue não existe");
             System.err.println("---------------------------------------------\n");
             writeToFile(logFile, "- A Issue " + (numeroProximaPagina - 1) + " não existe, por isso não pode ser cadastrada.");
             return false;
         } else if (linhas.get(i).contains("environment-val")) { // pega ENVIRONMENT
             issue.setAmbiente(pegaAmbiente(linhas, i));
         } else if (linhas.get(i).contains("issue_header_summary")) { // pega NAME
             issue.setNome(pegaNome(linhas.get(i)));
         } else if (linhas.get(i).contains("type-val")
                 && linhas.get(i).contains("class=\"value\"")
                 && linhas.get(i).contains("<span")) { // pega TIPO
             issue.setTipo(pegaTipo(linhas.get(i + 2)));
         } else if (linhas.get(i).contains("versions-val")) { // pega VERSAO AFETADA
             issue.setVersoesAfetadas(pegaVersoes(linhas, i));
         } else if (linhas.get(i).contains("status-val")) { // pega STATUS
             issue.setStatus(pegaStatus(linhas.get(i + 2)));
         } else if (linhas.get(i).contains("resolution-val")) { // pega RESOLUCAO
             issue.setResolucao(pegaResolucao(linhas.get(i + 1)));
         } else if (linhas.get(i).contains("fixfor-val")) { // pega VERSAO FIXADA
             issue.setVersoesFixadas(pegaVersoes(linhas, i));
         } else if (linhas.get(i).contains("assignee-val")) { // pega ASSIGNEE
             issue.setAssignee(pegaLogin(linhas, i));
         } else if (linhas.get(i).contains("reporter-val")) { // pega REPORTER
             issue.setReporter(pegaLogin(linhas, i));
         } else if (linhas.get(i).contains("priority-val")) { // pega PRIORIDADE
             issue.setPrioridade(pegaPrioridade(linhas.get(i + 2)));
         } else if (linhas.get(i).contains("components-val")) { // pega COMPONENTES
             issue.setComponentes(pegaComponentes(linhas, i));
         } else if (linhas.get(i).contains("create-date")) { // pega DATA CRIADA
             issue.setDataCriada(pegaData(linhas.get(i + 1)));
         } else if (linhas.get(i).contains("resolved-date")) { // pega DATA RESOLVIDA
             issue.setDataResolvida(pegaData(linhas.get(i + 1)));
         }
 
         return true;
     }
 
     private Date pegaData(String linha) {
 //                        <time datetime="2008-11-18T10:31+0000">18/Nov/08 10:31</time></dd>                     </dd>
 // added a comment  - <span class='commentdate_12648727_verbose subText'><span class='date user-tz' title='18/Nov/08 19:52'><time datetime='2008-11-18T19:52+0000'>18/Nov/08 19:52</time></span></span>  </div>
         Date data = null;
         try {
             String[] partes = linha.split("datetime=");
             partes = partes[1].split("T");
             data = Util.stringToDate(partes[0].replaceAll("\"", "").replaceAll("'", ""));
             System.out.println("----------- Capturado Data da Issue ------------");
             System.out.println("Componente: " + data.toString());
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("-------- Erro ao capturar Data da Issue --------");
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return data;
     }
 
     private String pegaComponentes(List<String> linhas, int i) {
 //    <span id="components-val" class="value">
 //        <span class="shorten" id="components-field">
 //              <a href="/jira/browse/LUCENE/component/12311546" title="general/build issues with building Lucene using the ANT build scripts">general/build</a> 
         String componentes = "";
         try {
             if (linhas.get(i + 1).contains("None")) {
                 componentes = "None";
             } else {
                 String linha = "";
                 int j = i + 2;
                 while (!linhas.get(j).contains("</div>")) {
                     linha += linhas.get(j);
                     j++;
                 }
                 if (linha.contains(">,")) {
                     String[] comps = linha.split(">,");
                     for (String comp : comps) {
                         componentes += pegaComponente(comp) + ";";
                     }
                 } else {
                     componentes = pegaComponente(linha);
                 }
             }
             System.out.println("-------- Capturado Componente da Issue ---------");
             System.out.println("Componente: " + componentes);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("----- Erro ao capturar Componente da Issue -----");
             System.err.println(linhas.get(i + 2));
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return componentes;
     }
 
     private String pegaComponente(String comp) throws Exception {
 //<a href="/jira/browse/LUCENE/component/12311546" title="general/build issues with building Lucene using the ANT build scripts">general/build</a>                             
         String nome = "";
         String link = "";
         String[] partes = comp.split(">");
         partes = partes[1].split("<");
         nome = partes[0];
         partes = comp.split("\"");
         link = partes[1];
         return "nome=" + nome + " / link=" + link;
     }
 
     private String pegaPrioridade(String linha) {
         String prioridade = null;
         try {
             prioridade = linha.trim();
             System.out.println("-------- Capturado Prioridade da Issue ---------");
             System.out.println("Prioridade: " + prioridade);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("----- Erro ao capturar Prioridade da Issue -----");
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return prioridade;
     }
 
     private String pegaLogin(List<String> linhas, int i) {
 //        <a class="user-hover" rel="jdillon" id="issue_summary_assignee_jdillon" href="/jira/secure/ViewProfile.jspa?name=jdillon">Jason Dillon</a>
         String login = "";
         try {
             if (!linhas.get(i + 1).contains("<a")) {
                 login = "unassigned";
             } else {
                 String[] partes = linhas.get(i + 1).split("rel=\"");
                 partes = partes[1].split("\"");
                 login = partes[0];
             }
             System.out.println("--------- Capturado Assignee da Issue ----------");
             System.out.println("Assignee: " + login);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------ Erro ao capturar Assignee da Issue ------");
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return login;
     }
 
     private String pegaResolucao(String linha) {
         String resol = null;
         try {
             resol = linha.trim().replaceAll("&#39;", "'");
             System.out.println("--------- Capturado Resolucao da Issue ---------");
             System.out.println("Resolucao: " + resol);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------ Erro ao capturar Resolucao da Issue -----");
             System.err.println(linha);
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return resol;
     }
 
     private String pegaStatus(String linha) {
         String status = null;
         try {
             status = linha.trim();
             System.out.println("---------- Capturado Status da Issue -----------");
             System.out.println("Status: " + status);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------- Erro ao capturar Status da Issue -------");
             System.err.println("");
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return status;
     }
 
     private String pegaVersoes(List<String> linhas, int i) {
         //    <span title="2.4 ">2.4</span>,                                                            <span title="2.9 Last release (barring major bugs) before migrating to 3.0 and JDK 1.5">2.9</span>                                                    </span>
         String versoes = "";
         try {
             if (linhas.get(i + 1).contains("None")) {
                 versoes = "None";
             } else {
                 String linha = "";
                 int j = i + 2;
                 while (!linhas.get(j).contains("</div>")) {
                     linha += linhas.get(j);
                     j++;
                 }
                 if (linha.contains(">,")) {
                     String[] comps = linha.split(">,");
                     for (String comp : comps) {
                         versoes += pegaVersao(comp) + ";";
                     }
                 } else {
                     versoes = pegaVersao(linha);
                 }
             }
             System.out.println("---------- Capturado Versoes da Issue ----------");
             System.out.println("Versoes: " + versoes);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------ Erro ao capturar Versoes da Issue -------");
             System.err.println(linhas.get(i));
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return versoes;
     }
 
     private String pegaVersao(String ver) {
 //<a href="/jira/browse/LUCENE/component/12311546" title="general/build issues with building Lucene using the ANT build scripts">general/build</a>                             
         String versao = "";
         String[] partes = ver.split(">");
         partes = partes[1].split("<");
         versao = partes[0];
         return "versao=" + versao;
     }
 
     private String pegaTipo(String linha) {
 //                    <span id="type-val" class="value">
 //                                                        <img alt="Bug" height="16" src="/jira/images/icons/bug.gif" title="Bug - A problem which impairs or prevents the functions of the product." width="16" />
 //   *          Bug
 //            </span>
         String tipo = "";
         try {
             tipo = linha.trim();
             System.out.println("----------- Capturado Tipo da Issue ------------");
             System.out.println("Tipo: " + tipo);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------- Erro ao capturar Tipo da Issue ---------");
             System.err.println(linha);
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return tipo;
     }
 
     private String pegaNome(String linha) {
         // <h2 id="issue_header_summary" class="item-summary"><a href="/jira/browse/LUCENE-1400">Add Apache RAT (Release Audit Tool) target to build.xml</a></h2>
         String nome = "";
         try {
             String[] partes = linha.split("</a>");
             partes = partes[0].split(">");
             nome = partes[partes.length - 1];
             System.out.println("----------- Capturado Nome da Issue ------------");
             System.out.println("Nome: " + nome);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------- Erro ao capturar Nome da Issue ---------");
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
 
         }
         return nome;
     }
 
     private String pegaAmbiente(List<String> linhas, int i) {
 //          <div id="environment-val" class="value">
 //             <p>jdk 1.5.0_13<br/>
 //               ant 1.7.1<br/>
 //               osx 10.5.5 </p>
         String ambiente = "";
         i++;
         try {
             while (!linhas.get(i).contains("</div>")) {
                 String linha = linhas.get(i).trim();
                 linha = linha.replaceAll("<p>", "");
                 linha = linha.replaceAll("</p>", "");
                 linha = linha.replaceAll("<br/>", "\n");
                 ambiente += linha;
                 i++;
             }
             System.out.println("--------- Capturado Ambiente da Issue ---------");
             System.out.println("Ambiente: " + ambiente);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("----- Erro ao capturar Ambiente da Issue -------");
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return ambiente;
     }
 
     private void lerComentarios(Issue issue, List<String> linhas) {
         for (int i = 0; i < linhas.size(); i++) {
             if (linhas.get(i).contains("action-body flooded")) {
                 Comentario comentario = pegarComentario(linhas, i);
                 if (comentario != null && Conn.daoProjeto.insere(comentario)) {
                     issue.addComentario(comentario);
                     if (Conn.daoProjeto.atualiza(issue)) {
                         System.err.println("\n--------- Comentário Cadastrado e adicioado a Issue ---------");
                         System.err.println("Autor: " + comentario.getAutor());
                         System.err.println("Data: " + comentario.getDataComentario() + " / " + comentario.getHoraComentario());
                         System.err.println("Comentario: " + comentario.getComentario());
                         System.err.println("-----------------------------------------------------------------\n");
                     } else {
                         System.err.println("\n--------- Comentário Cadastrado e *NÃO* adicioado a Issue ---------");
                         System.err.println("Autor: " + comentario.getAutor());
                         System.err.println("Data: " + comentario.getDataComentario() + " / " + comentario.getHoraComentario());
                         System.err.println("Comentario: " + comentario.getComentario());
                         System.err.println("-----------------------------------------------------------------\n");
                     }
                 } else {
                     System.err.println("\n-------- Erro ao Cadastrar Comentario ----------");
                     System.err.println("Autor: " + comentario.getAutor());
                     System.err.println("Data: " + comentario.getDataComentario() + " / " + comentario.getHoraComentario());
                     System.err.println("Comentario: " + comentario.getComentario());
                     System.err.println("-----------------------------------------------------------------\n");
                 }
                 comentario = null;
             }
         }
         issue = null;
         linhas = null;
     }
 
     private Comentario pegarComentario(List<String> linhas, int i) {
         Comentario comentario = new Comentario();
         comentario.setAutor(pegaLogin(linhas, i - 3));
         comentario.setDataComentario(pegaData(linhas.get(i - 1)));
         comentario.setHoraComentario(pegaHora(linhas.get(i - 1)));
         String linha = linhas.get(i);
         String coment = "";
         while (!linha.contains("twixi-wrap concise actionContainer")) {
             if (!linha.trim().isEmpty()) {
                 coment += linha.replaceAll("<div class=\"action-body flooded\">", "") + "\n";
             }
             i++;
             linha = linhas.get(i);
         }
         try {
             comentario.setComentario(URLEncoder.encode(coment, "UTF-8"));
         } catch (UnsupportedEncodingException ex) {
             ex.printStackTrace();
         }
         return comentario;
     }
 
     private String pegaHora(String linha) {
         // added a comment  - <span class='commentdate_12648727_verbose subText'><span class='date user-tz' title='18/Nov/08 19:52'><time datetime='2008-11-18T19:52+0000'>18/Nov/08 19:52</time></span></span>  </div>
         String hora = "";
         try {
             String[] partes = linha.split("</time>");
             partes = partes[0].split(" ");
             hora = partes[partes.length - 1].trim();
             System.out.println("--------- Capturado Hora do Comentario ---------");
             System.out.println("Hora: " + hora);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("----- Erro ao capturar Hora do Comentario ------");
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return hora;
     }
 
     private void lerCommits(Issue issue, List<String> linhas) {
         for (int i = 0; i < linhas.size(); i++) {
             if (linhas.get(i).contains("<td bgcolor=\"#f0f0f0\" width=\"10%\"><b>User</b></td>")) {
                 Commits commit = pegarCommit(linhas, i);
                 if (commit != null && Conn.daoProjeto.insere(commit)) {
                     leiArquivosModificados(commit, linhas, i + 13);
                     issue.addCommit(commit);
                     if (Conn.daoProjeto.atualiza(issue)) {
                         System.err.println("\n--------- Commit Cadastrado e adicioado a Issue ---------");
                         System.err.println("Autor: " + commit.getAutor());
                         System.err.println("Data: " + commit.getDataHora());
                         System.err.println("Mesangem: " + commit.getMensagem());
                         System.err.println("-----------------------------------------------------------------\n");
                     } else {
                         System.err.println("\n--------- Commit Cadastrado e *NÃO* adicioado a Issue ---------");
                         System.err.println("Autor: " + commit.getAutor());
                         System.err.println("Data: " + commit.getDataHora());
                         System.err.println("Mesangem: " + commit.getMensagem());
                         System.err.println("-----------------------------------------------------------------\n");
                     }
                 } else {
                     System.err.println("\n-------- Erro ao Cadastrar Commit ----------");
                     System.err.println("Autor: " + commit.getAutor());
                     System.err.println("Data: " + commit.getDataHora());
                     System.err.println("Mesangem: " + commit.getMensagem());
                     System.err.println("-----------------------------------------------------------------\n");
                 }
                 commit = null;
             }
         }
         issue = null;
         linhas = null;
     }
 
     private Commits pegarCommit(List<String> linhas, int i) {
         Commits commit = new Commits();
         commit.setRepositorio(pegaRespositorio(linhas.get(i + 4)));
         commit.setnRevisao(pegaRevisao(linhas.get(i + 5)));
         commit.setDataHora(pegaDataHoraCommit(linhas.get(i + 6)));
         commit.setAutor(pegaLoginCommit(linhas.get(i + 7)));
         commit.setMensagem(pegaMensagemCommit(linhas, i + 8)); // pega mensagem com tags html por nao seguir um padrao
         return commit;
     }
 
     private String pegaMensagemCommit(List<String> linhas, int i) {
 //       <td bgcolor="#ffffff">    Add missing license for <a href="/jira/browse/IVY-1" title="use MBeans do find projects"><strike>IVY-1</strike></a>.4.1<br/>
 //   Issue: <a href="/jira/browse/FOR-855" title="verify the license situation prior to each release">FOR-855</a>
 //   </td>
         String mensagem = "";
         String linha = "";
         try {
             linha = linhas.get(i);
             while (!linha.contains("</tr>")) {
                 mensagem += linha;
                 i++;
                 linha = linhas.get(i);
             }
             System.out.println("--------- Capturado Mensagem do Commit ---------");
             System.out.println("Mensagem: " + mensagem);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------ Erro ao capturar Mensagem do Commit -----");
             System.err.println(linha);
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return mensagem;
     }
 
     private void leiArquivosModificados(Commits commit, List<String> linhas, int i) {
         while (!linhas.get(i).contains("<td bgcolor=\"#f0f0f0\" width=\"10%\"><b>Repository</b></td>")
                 && !linhas.get(i).contains("</table>")) {
             if (linhas.get(i).contains("<font ") && linhas.get(i).contains("<b ")) {
                 ArquivoModificado arquivo = new ArquivoModificado();
                 arquivo.setAcao(pegaAcaoArquivoModificado(linhas.get(i)));
                 arquivo.setUrl(pegaURLArquivoModificado(linhas.get(i + 1)));
                 arquivo.setNome(pegaNomeArquivoModificado(linhas.get(i + 1)));
                 if (Conn.daoProjeto.insere(arquivo)) {
                     commit.addArquivoModificado(arquivo);
                 }
                 arquivo = null;
             }
             i++;
         }
     }
 
     private String pegaAcaoArquivoModificado(String linha) {
 //                                    <font color="#999933" size="-2"><b title="Modify">MODIFY</b></font>
         String acao = "";
         try {
             String[] partes = linha.split("</b>");
             partes = partes[0].split(">");
             acao = partes[partes.length - 1];
             System.out.println("---------- Capturado Acao do Arquivo -----------");
             System.out.println("Acao: " + acao);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------- Erro ao capturar Acao do Arquivo -------");
             System.err.println(linha);
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return acao;
     }
 
     private String pegaURLArquivoModificado(String linha) {
 //                        <a href="http://svn.apache.org/viewvc/lucene/java/branches/flex_1458/src/java/org/apache/lucene/index/FreqProxFieldMergeState.java/?rev=885265&view=diff&r1=885265&r2=885264&p1=/lucene/java/branches/flex_1458/src/java/org/apache/lucene/index/FreqProxFieldMergeState.java&p2=/lucene/java/branches/flex_1458/src/java/org/apache/lucene/index/FreqProxFieldMergeState.java">/lucene/java/branches/flex_1458/src/java/org/apache/lucene/index/FreqProxFieldMergeState.java</a>
         String url = "";
         try {
             String[] partes = linha.split("href=\"");
             partes = partes[1].split("\"");
             url = partes[0];
             System.out.println("----------- Capturado URL do Arquivo -----------");
             System.out.println("URL: " + url);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------- Erro ao capturar URL do Arquivo --------");
             System.err.println(linha);
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return url;
     }
 
     private String pegaNomeArquivoModificado(String linha) {
         String nome = "";
         try {
             nome = pegaSomenteConteudo(linha);
             System.out.println("---------- Capturado Nome do Arquivo -----------");
             System.out.println("URL: " + nome);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------- Erro ao capturar Nome do Arquivo -------");
             System.err.println(linha);
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return nome;
     }
 
     private Date pegaDataHoraCommit(String linha) {
 //     <td bgcolor="#ffffff" width="10%" valign="top" rowspan="3">Wed Sep 03 21:58:13 UTC 2008</td>
         Date data = null;
         try {
             linha = pegaSomenteConteudo(linha);
             DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
             String[][] meses = new String[][]{{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"}, {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"}};
             String[] partes = linha.split(" ");
             for (int i = 0; i < meses[0].length; i++) {
                 if (partes[1].equals(meses[0][i])) {
                     partes[1] = meses[1][i];
                 }
             }
             String dt = partes[2] + "/" + partes[1] + "/" + partes[5] + " " + partes[3];
             data = df.parse(dt);
             System.out.println("--------- Capturado Revisao do Commit ----------");
             System.out.println("Revisao: " + data);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------ Erro ao capturar Revisao do Commit ------");
             System.err.println(linha);
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return data;
     }
 
     private String pegaSomenteConteudo(String linha) throws Exception {
         String[] partes = linha.split("<");
         partes = partes[partes.length - 2].split(">");
         return partes[partes.length - 1];
     }
 
     private String pegaRevisao(String linha) {
 //     <td bgcolor="#ffffff" width="10%" valign="top" rowspan="3"><a href="http://svn.apache.org/viewvc?view=rev&rev=691802">#691802</a></td>
         String revisao = "";
         try {
             String link = "";
             String num = "";
             String[] partes = linha.split("</a>");
             partes = partes[0].split(">");
             num = partes[partes.length - 1];
             partes = partes[partes.length - 2].split("\"");
             link = partes[partes.length - 1];
             revisao = "numero=" + num + " / link=" + link;
             System.out.println("--------- Capturado Revisao do Commit ----------");
             System.out.println("Revisao: " + revisao);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------ Erro ao capturar Revisao do Commit ------");
             System.err.println(linha);
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return revisao;
     }
 
     private String pegaRespositorio(String linha) {
 //    <td bgcolor="#ffffff" width="10%" valign="top" rowspan="3">ASF</td>        
         String repositorio = "";
         try {
             repositorio = pegaSomenteConteudo(linha);
             System.out.println("------- Capturado Respositorio do Commit -------");
             System.out.println("Login: " + repositorio);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("--- Erro ao capturar Respositorio do Commit ----");
             System.err.println(linha);
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return repositorio;
     }
 
     private String pegaLoginCommit(String linha) {
 //     <td bgcolor="#ffffff" width="10%" valign="top" rowspan="3">maartenc</td>
         String login = "";
         try {
             login = pegaSomenteConteudo(linha);
             System.out.println("---------- Capturado Login do Commit -----------");
             System.out.println("Login: " + login);
             System.out.println("------------------------------------------------");
         } catch (Exception ex) {
             System.err.println("------- Erro ao capturar Login do Commit -------");
             System.err.println(linha);
             ex.printStackTrace();
             System.err.println("------------------------------------------------");
         }
         return login;
     }
 
     private String getUrl() {
         return projeto.getLinkIssue().replaceAll("-1", "-" + numeroProximaPagina);
     }
 
     private boolean writeToFile(String caminho, String msg) {
         File f = new File(caminho);
         try {
             if (!f.exists()) {
                 f.createNewFile();
             }
             FileWriter fw = new FileWriter(f, true);
             PrintWriter pw = new PrintWriter(fw);
             pw.println(msg);
             pw.close();
             fw.close();
         } catch (IOException ex) {
             ex.printStackTrace();
             return false;
         }
         return true;
     }
 
     private List<String> capturarCodigoHtml(BufferedReader dis) throws Exception {
         List<String> linhas = new ArrayList<String>();
         String linha = dis.readLine();
         while (!linha.trim().equals("</html>")) {
             linhas.add(linha);
             linha = dis.readLine();
         }
         dis = null;
         return linhas;
     }
 }
