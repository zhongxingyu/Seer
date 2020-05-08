 package br.ueg.si.sige;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import java.util.*;
 
 public class AlunoForm extends HttpServlet {
 	private static final long serialVersionUID = -2674206520689109277L;
 
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws
             ServletException, IOException {
         if(request.getSession().getAttribute("usuarioAcademico")==null){
             response.sendRedirect("/login");
         }
         if (request.getParameter("action") == null) {
             //mostra listagem
             showListaAlunos(request, response);
         } else {
             String action = request.getParameter("action");
             if (action.equals("incluir")) {
                 IncluirAluno(request, response);
             } else if (action.equals("editar")) {
                 //edicao
                 editarUsuario(request, response);
             } else if (action.equals("excluir")) {
                 //exclusao
                 excluirAluno(request, response);
             } else if (action.equals("pesquisar")) {
                 //pesquisa
                 ArrayList<Aluno> alunos = AlunoDAO.buscaPorNome(request.getParameter(
                         "pesquisa"));
                 showFormListaAlunos(request, response, "Resultados da busca",
                                     alunos);
             }
         }
     }
 
     private void editarUsuario(HttpServletRequest request,
                                HttpServletResponse response) throws
             ServletException, IOException, NumberFormatException {
         request.setAttribute("formAction", "editar");
         if (request.getMethod().equalsIgnoreCase("get")) {
             //mostra formulario
             Aluno aluno = AlunoDAO.buscaPorCodigo(Integer.parseInt(
                     request.getParameter("codigo")));
             showFormAlunos(request, response, aluno, null);
         } else if (request.getParameter("confirma") == null) {
             //mostra confirmacao
             Aluno aluno = fillAluno(request);
             showFormConfirmaDados(request, response, aluno);
         } else {
             //salva alteracoes
             Aluno aluno = fillAluno(request);
             if(AlunoDAO.salvarAlteracoes(aluno)){
                 //volta a tela principal
                 request.setAttribute("erro", "Alterações Salvas.");
                 showListaAlunos(request, response);
             }else{
                 //mostra erro
                 showError(request, response, "Aluno não Alterado");
             }
         }
     }
 
     private void excluirAluno(HttpServletRequest request,
                               HttpServletResponse response) throws
             ServletException, IOException, NumberFormatException {
         request.setAttribute("formAction", "excluir");
         if (request.getMethod().equalsIgnoreCase("get")) {
             //exibe formulario de confirmacao
             Aluno aluno = AlunoDAO.buscaPorCodigo(Integer.parseInt(request.
                     getParameter("codigo")));
             showFormConfirmaDados(request, response, aluno);
         } else {
             //excluir o aluno
             Aluno aluno = fillAluno(request);
             if (AlunoDAO.excluir(aluno)) {
                 showListaAlunos(request, response);
             } else {
                 showError(request, response, "Aluno não excluído");
             }
         }
     }
 
     private void IncluirAluno(HttpServletRequest request,
                               HttpServletResponse response) throws
             NumberFormatException, ServletException, IOException {
         request.setAttribute("formAction", "incluir");
         //inclusão
         if (request.getMethod().equalsIgnoreCase("get")) {
             //mostra o formulario
             showFormAlunos(request, response, null, null);
         } else if (request.getParameter("confirma") == null) {
             //mostra formulario de confirmacao
             Aluno aluno = fillAluno(request);
             showFormConfirmaDados(request, response, aluno);
         } else {
             //inclui o aluno
             Aluno aluno = fillAluno(request);
             ArrayList<Aluno> existe = AlunoDAO.buscaPorNome(aluno.getNome());
             if (existe != null) {
                 //mostra erro
                 String message = "Aluno já existe";
                 showError(request, response, message);
             } else {
                 //inclui o aluno no banco
                 if (AlunoDAO.incluir(aluno)) {
                     //volta a tela principal
                     request.setAttribute("erro", "Aluno Incluido com Sucesso!");
                     showListaAlunos(request, response);
                 } else {
                     //mostra erro
                     showError(request, response, "Aluno não incluído");
                 }
             }
         }
     }
 
     private void showFormConfirmaDados(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Aluno aluno) throws IOException,
             ServletException {
         request.setAttribute("aluno", aluno);
         request.getRequestDispatcher("confirma_dados_aluno.jsp").forward(
                 request, response);
     }
 
     private void showError(HttpServletRequest request,
                            HttpServletResponse response, String message) throws
             IOException, ServletException {
         request.setAttribute("erro", message);
         request.getRequestDispatcher("erro.jsp").forward(request, response);
     }
 
     private Aluno fillAluno(HttpServletRequest request) throws
             NumberFormatException {
         Aluno aluno = new Aluno();
         int codigo;
         try {
             codigo = Integer.parseInt(request.getParameter("codigo"));
         } catch (NumberFormatException ex) {
             codigo = 0;
         }
         aluno.setCodigo(codigo);
         aluno.setNome(request.getParameter("nome"));
        aluno.setAtivo(parameterToBoolean(request, "ativo"));
        aluno.setDocumentos(parameterToBoolean(request, "documentos"));
        String data = String.format("%02d/%02d/%04d", 
        		Integer.parseInt(request.getParameter("dia")), 
        		Integer.parseInt(request.getParameter("mes")), 
        		Integer.parseInt(request.getParameter("ano")));
         aluno.setDataDeNascimentoFormatada(data);
         aluno.setEndereco(request.getParameter("endereco"));
         aluno.setEntidade(EntidadeDAO.buscaPorCodigo(Integer.parseInt(request.
                 getParameter("entidade"))));
         aluno.setEscolaridade(Integer.parseInt(request.getParameter(
                 "escolaridade")));
         aluno.setMae(request.getParameter("mae"));
         aluno.setNacionalidade(request.getParameter("nacionalidade"));
         aluno.setNaturalidade(request.getParameter("naturalidade"));
         aluno.setPai(request.getParameter("pai"));
         aluno.setResponsavel(request.getParameter("responsavel"));
         aluno.setTelefone(request.getParameter("telefone"));
         return aluno;
     }
    
    private boolean hasField(HttpServletRequest request, String key) {
    	@SuppressWarnings("unchecked")
		Enumeration<String> names = request.getParameterNames();
    	while(names.hasMoreElements()) {
    		if (names.nextElement().equals(key))
    			return true;
    	}
    	return false;
    }
    
    private boolean parameterToBoolean(HttpServletRequest request, String key) {
    	return hasField(request, key) ? request.getParameter(key).equalsIgnoreCase("true") : false;
    }
 
     private void showFormAlunos(HttpServletRequest request,
                                 HttpServletResponse response, Aluno aluno,
                                 String erro) throws IOException,
             ServletException {
         ArrayList<Entidade> entidades = EntidadeDAO.buscarTodos();
         request.setAttribute("entidades", entidades);
         request.setAttribute("aluno", aluno);
         request.setAttribute("erro", erro);
         request.getRequestDispatcher("form_alunos.jsp").forward(request,
                 response);
     }
 
     private void showListaAlunos(HttpServletRequest request,
                                  HttpServletResponse response) throws
             ServletException, IOException {
 //        Map campos = new HashMap();
         Entidade entidade = ((Usuario)request.getSession().getAttribute(
                                     "usuarioAcademico")).getEntidade();
         ArrayList<Aluno> alunos = AlunoDAO.buscaPorEntidade(entidade);
         showFormListaAlunos(request, response, null, alunos);
     }
 
     private void showFormListaAlunos(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String erro, ArrayList<Aluno> alunos) throws
             IOException, ServletException {
         request.setAttribute("erro", erro);
         request.setAttribute("alunos", alunos);
         request.getRequestDispatcher("alunos.jsp").forward(request, response);
     }
 
 
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws
             ServletException, IOException {
         doGet(request, response);
     }
 }
