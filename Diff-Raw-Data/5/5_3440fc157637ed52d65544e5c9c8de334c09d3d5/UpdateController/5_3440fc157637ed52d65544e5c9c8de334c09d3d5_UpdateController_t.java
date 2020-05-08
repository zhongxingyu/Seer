 /*
     Copyright (C) 2012  Filippe Costa Spolti
 
 	This file is part of Hrstatus.
 
     Hrstatus is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package br.com.hrstatus.controller;
 
 /*
  * @author spolti
  */
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.Validator;
 import br.com.caelum.vraptor.validator.ValidationMessage;
 import br.com.caelum.vraptor.view.Results;
 import br.com.hrstatus.dao.BancoDadosInterface;
 import br.com.hrstatus.dao.Iteracoes;
 import br.com.hrstatus.dao.UsersInterface;
 import br.com.hrstatus.model.BancoDados;
 import br.com.hrstatus.model.Servidores;
 import br.com.hrstatus.model.Users;
 import br.com.hrstatus.security.Crypto;
 import br.com.hrstatus.security.SpringEncoder;
 import br.com.hrstatus.utils.UserInfo;
 
 @Resource
 public class UpdateController {
 	
 	private Result result;
 	private Iteracoes iteracoesDAO;
 	private Validator validator;
 	private UsersInterface usersDAO;
 	private BancoDadosInterface BancoDadosDAO;
 	private HttpServletRequest request;
 	UserInfo userInfo = new UserInfo();
 
 	public UpdateController(Result result, Iteracoes iteracoesDAO,
 			Validator validator, UsersInterface usersDAO,HttpServletRequest request, BancoDadosInterface BancoDadosDAO) {
 		this.result = result;
 		this.iteracoesDAO = iteracoesDAO;
 		this.validator = validator;
 		this.usersDAO = usersDAO;
 		this.request = request;
 		this.BancoDadosDAO = BancoDadosDAO;
 	}
 
 	@SuppressWarnings("static-access")
 	@Get("/findForUpdateServer/{serverID}")
 	@Post("/findForUpdateServer/{serverID}")
 	public void findForUpdateServer(Servidores s, String serverID) {
 		//inserindo html title no result
 		result.include("title","Atualizar Servidor");
 		
 		result.include("loggedUser", userInfo.getLoggedUsername());
 
 		Logger.getLogger(getClass()).info("[ " + userInfo.getLoggedUsername() + " ] URI Called: /findForUpdateServer");
 		Crypto decodePass = new Crypto();
 		int id = Integer.parseInt(serverID);
 		Logger.getLogger(getClass()).info("[ " + userInfo.getLoggedUsername() + " ] Server id selected for update: " + id);
 
 		Servidores server = this.iteracoesDAO.getServerByID(id);
 
 		// Setando ID
 		server.setId(id);
 
 		// Descriptografando senha e a jogando no formulário
 		try {
 
 			String textPass = String
 					.valueOf(decodePass.decode(server.getPass()));
 			server.setPass(textPass);
 
 		} catch (Exception e) {
 			Logger.getLogger(getClass()).error("[ " + userInfo.getLoggedUsername() + " ] Erro ao descriptografar senha: ", e);
 		}
 
 		// populating SO combobox
 		List<Servidores> so = this.iteracoesDAO.getListOfSO();
 		int size = so.size();
 		if ((size < 1) || (size < 4)) {
 			so.removeAll(so);
 
 			Servidores linux = new Servidores();
 			Servidores windows = new Servidores();
 			Servidores unix = new Servidores();
 			Servidores outro = new Servidores();
 			linux.setSO("LINUX");
 			windows.setSO("WINDOWS");
 			unix.setSO("UNIX");
 			outro.setSO("OUTRO");
 			so.add(linux);
 			so.add(windows);
 			so.add(unix);
 			so.add(outro);
 			result.include("SO", so);
 		} else {
 			result.include("SO", so);
 		}
 
 		result.include("server", server);
 
 		if (s != null) {
 			Logger.getLogger(getClass())
 					.info("[ " + userInfo.getLoggedUsername() + " ] Objeto do tipo Servidores não está vazio, atribuindo valores.");
 			server = s;
 		}
 	}
 
 	@SuppressWarnings("static-access")
 	@Get("/updateServer")
 	@Post("/updateServer")
 	public void updateServer(Servidores server) {
 		//inserindo html title no result
 		result.include("title","Atualizar Servidor");
 		
 		result.include("loggedUser", userInfo.getLoggedUsername());
 
 		Crypto encodePass = new Crypto();
 		Logger.getLogger(getClass()).info("[ " + userInfo.getLoggedUsername() + " ] URI Called: /updateServer");
 		Pattern pattern = Pattern
 				.compile("\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z");
 		Matcher matcher = pattern.matcher(server.getIp());
 
 		if (server.getIp().isEmpty()) {
 			validator.add(new ValidationMessage(
 					"O campo Ip deve ser informado", "Erro"));
 		} else if (!matcher.matches()) {
 			validator.add(new ValidationMessage("O ip " + server.getIp()
 					+ " não é válido.", "Erro"));
 		} else if (server.getHostname().isEmpty()) {
 			validator.add(new ValidationMessage(
 					"O campo Hostname deve ser informado", "Erro"));
 		} else if (server.getUser().isEmpty()) {
 			validator.add(new ValidationMessage(
 					"O campo Usuário deve ser informado", "Erro"));
 		} else if (server.getPass().isEmpty()) {
 			validator.add(new ValidationMessage(
 					"O campo Senha deve ser informado", "Erro"));
 		} else if (server.getPort() <= 0 || server.getPort() >= 65536) {
 			validator.add(new ValidationMessage(
 					"O campo porta está incorreto ou vazio", "Erro"));
 		} else if (server.getSO().isEmpty()) {
 			validator.add(new ValidationMessage(
 					"O campo SO deve ser informado", "Erro"));
 		}
 		validator.onErrorUsePageOf(UpdateController.class).findForUpdateServer(
 				server, "");
 
 		server.setSO(server.getSO().toUpperCase());
 
 		// verificar se os campos estão vazios, se não estiverem não faz nada se
 		// estiverem seta os campos com valores default
 
 		try {
 
 			// Critpografando a senha
 			server.setPass(encodePass.encode(server.getPass()));
 
 		} catch (Exception e) {
 			Logger.getLogger(getClass()).error("[ " + userInfo.getLoggedUsername() + " ] Error: ", e);
 		}
 
 		this.iteracoesDAO.updateServer(server);
 
 		// redirecto to /configClients
 		result.redirectTo(ConfigController.class).configClients();
 	}
 
 	@SuppressWarnings("static-access")
 	@Get("/findForUpdateDataBase/{dataBaseID}")
 	public void findForUpdateDataBase(BancoDados db, String dataBaseID) {
 		//inserindo html title no result
 		result.include("title","Atualizar Banco de Dados");
 		
 		result.include("loggedUser", userInfo.getLoggedUsername());
 
 		Logger.getLogger(getClass()).info("[ " + userInfo.getLoggedUsername() + " ] URI Called: /findForUpdateDataBase");
 		Crypto decodePass = new Crypto();
 		int id = Integer.parseInt(dataBaseID);
 		Logger.getLogger(getClass()).info("[ " + userInfo.getLoggedUsername() + " ] DataBase id selected for update: " + id);
 		
 		BancoDados dataBase = this.BancoDadosDAO.getDataBaseByID(id);
 
 		// Setando ID
 		dataBase.setId(id);
 
 		// Descriptografando senha e a jogando no formulário
 		try {
 
 			String textPass = String
 					.valueOf(decodePass.decode(dataBase.getPass()));
 			dataBase.setPass(textPass);
 
 		} catch (Exception e) {
 			Logger.getLogger(getClass()).error("[ " + userInfo.getLoggedUsername() + " ] Erro ao descriptografar senha: ", e);
 		}
 		
 		// populating SO combobox
 		ArrayList<String> VENDOR = new ArrayList<String>();
 		VENDOR.add("MySQL");
 		VENDOR.add("ORACLE");
 		VENDOR.add("PostgreSQL");
 		VENDOR.add("SqlServer");
 		result.include("VENDOR", VENDOR);
 		
 		result.include("dataBase", dataBase);
 		
 		if (db != null) {
 			Logger.getLogger(getClass())
 					.info("[ " + userInfo.getLoggedUsername() + " ] Objeto do tipo BancoDados não está vazio, atribuindo valores.");
 			dataBase = db;
 		}
 	}
 	
 	@SuppressWarnings("static-access")
 	@Post("/updateDataBase")
 	public void updateDataBase(BancoDados dataBase) {
 		
 		result.include("title","Atualizar Banco de Dados");
 		
 		result.include("loggedUser", userInfo.getLoggedUsername());
 
 		Crypto encodePass = new Crypto();
 		Logger.getLogger(getClass()).info("[ " + userInfo.getLoggedUsername() + " ] URI Called: /updateServer");
 		Pattern pattern = Pattern
 				.compile("\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z");
 		Matcher matcher = pattern.matcher(dataBase.getIp());
 		
 		if (dataBase.getIp().isEmpty()) {
 			validator.add(new ValidationMessage(
 					"O campo Ip deve ser informado", "Erro"));
 		} else if (!matcher.matches()) {
 			validator.add(new ValidationMessage("O ip " + dataBase.getIp()
 					+ " não é válido.", "Erro"));
 		} else if (dataBase.getHostname().isEmpty()) {
 			validator.add(new ValidationMessage(
 					"O campo Hostname deve ser informado", "Erro"));
 		} else if (dataBase.getUser().isEmpty()) {
 			validator.add(new ValidationMessage(
 					"O campo Usuário deve ser informado", "Erro"));
 		} else if (dataBase.getPass().isEmpty()) {
 			validator.add(new ValidationMessage(
 					"O campo Senha deve ser informado", "Erro"));
 		} else if (dataBase.getPort() <= 0 || dataBase.getPort() >= 65536) {
 			validator.add(new ValidationMessage(
 					"O campo porta está incorreto ou vazio", "Erro"));
 		} else if (dataBase.getVendor().isEmpty()) {
 			validator.add(new ValidationMessage(
 					"O campo SO deve ser informado", "Erro"));
 		}
 		if (dataBase.getQueryDate().isEmpty()) {
 			if (dataBase.getVendor().toUpperCase().equals("MYSQL")){
 				dataBase.setQueryDate("SELECT NOW() AS date;");
 			}
 			if (dataBase.getVendor().toUpperCase().equals("ORACLE")){
 				dataBase.setQueryDate("select sysdate from dual");
 			}
 			if (dataBase.getVendor().toUpperCase().equals("SQLSERVER")){
 				dataBase.setQueryDate("sqlserver query default");
 			}
 			if (dataBase.getVendor().toUpperCase().equals("POSTGRESQL")){
 				dataBase.setQueryDate("SELECT now();");
 			}
 		
 		}
 		validator.onErrorUsePageOf(UpdateController.class).findForUpdateDataBase(dataBase,"");
 		
 		try {
 
 			// Critpografando a senha
 			dataBase.setPass(encodePass.encode(dataBase.getPass()));
 
 		} catch (Exception e) {
 			Logger.getLogger(getClass()).error("[ " + userInfo.getLoggedUsername() + " ] Error: ", e);
 		}
 		
 		this.BancoDadosDAO.updateDataBase(dataBase);
 		
 		result.redirectTo(ConfigController.class).configDataBases();
 	}
 	
 	@Get("/findForUpdateUser/{username}/{action}")
 	public void findForUpdateUser(Users u, String username, String action) {
 		Logger.getLogger(getClass()).info("[ " + userInfo.getLoggedUsername() + " ] URI Called: /findForUpdateUser");
 
 		result.include("title","Atualizar Usuário");
 
 		String LoggedUsername = userInfo.getLoggedUsername();
 		
 		//obtendo roles do usuário:
 		boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
 		boolean isUser = request.isUserInRole("ROLE_USER");
 		
		if (!username.equals(LoggedUsername.toString()) && (isUser)) {
 			result.use(Results.http()).sendError(403);
 		} else if (action.equals("changePass")) {
 			Logger.getLogger(getClass()).info("[ " + userInfo.getLoggedUsername() + " ] validação de usuário OK");
 			result.include("loggedUser", LoggedUsername);
 
 			Logger.getLogger(getClass()).info("[ " + userInfo.getLoggedUsername() + " ] URI Called: /changePass");
 
 			Users user = this.usersDAO.getUserByID(username);
 			// setando username
 			user.setUsername(username);
 			user.setFirstLogin(false);
 			result.include("isDisabled","disabled");
 				
 			result.include("user", user);
 
 			if (user != null) {
 				Logger.getLogger(getClass())
 						.info("[ " + userInfo.getLoggedUsername() + " ] Objeto do tipo Users não está vazio, atribuindo valores.");
 				u = user;
 			}
 		}else {
 			if (isAdmin){
 				result.include("loggedUser", LoggedUsername);
 	
 				Logger.getLogger(getClass()).info("[ " + userInfo.getLoggedUsername() + " ] URI Called: /findForUpdateUser");
 	
 				Users user = this.usersDAO.getUserByID(username);
 				// setando username
 				user.setUsername(username);
 					
 				result.include("user", user);
 	
 				if (user != null) {
 					Logger.getLogger(getClass())
 							.info("[ " + userInfo.getLoggedUsername() + " ] Objeto do tipo Users não está vazio, atribuindo valores.");
 					u = user;
 				}
 			}else {
 				result.use(Results.http()).sendError(403);
 			}
 		}
 	}
 
 	@SuppressWarnings("static-access")
 	@Post("/updateUser")
 	public void updateUser(Users user) {
 		//inserindo html title no result
 		result.include("title","Atualizar Usuário");
 
 		String LoggedUsername = userInfo.getLoggedUsername();
 		
 		//obtendo roles do usuário:
 		boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
 		boolean isUser = request.isUserInRole("ROLE_USER");
 		
		if (!user.getUsername().equals(LoggedUsername.toString()) && (isUser)) {
 			result.use(Results.http()).sendError(403);
 		} else {
 			result.include("loggedUser", LoggedUsername);
 
 			Logger.getLogger(getClass()).info("[ " + userInfo.getLoggedUsername() + " ] URI Called: /updateUser");
 			SpringEncoder encode = new SpringEncoder();
 
 			if (user.getNome().isEmpty()) {
 				validator.add(new ValidationMessage(
 						"O campo Nome deve ser informado", "Erro"));
 			} else if (user.getUsername().isEmpty()) {
 				validator.add(new ValidationMessage(
 						"O campo Username deve ser informado", "Erro"));
 			} else if (!user.getPassword().isEmpty()
 					&& !user.getConfirmPass().isEmpty()) {
 				if (!user.getPassword().equals(user.getConfirmPass())) {
 					validator.add(new ValidationMessage(
 							"As senhas informadas não são iguais.", "Erro"));
 				}
 			} else if (user.getMail().isEmpty()) {
 				validator.add(new ValidationMessage(
 						"O campo E-mail deve ser informado", "Erro"));
 			}
 			
 			if (user.getAuthority() == null) {
 				//Obtendo role do usuário logado do banco Se a mesma vier do jsp em branco.
 				String role = this.usersDAO.getRole(LoggedUsername);
 				user.setAuthority(role);
 			}
 
 			if (user.getPassword().isEmpty()) {
 				// pegar senha do banco e setar objeto
 				user.setPassword(this.usersDAO.getPass(user.getUsername()));
 			} else {
 				user.setPassword(encode.encodePassUser(user.getPassword()));
 			}
 			validator.onErrorUsePageOf(UpdateController.class)
 					.findForUpdateUser(user, "","");
 		
 			if (!user.getUsername().equals(LoggedUsername.toString()) && !(isAdmin || isUser)) {
 				result.use(Results.http()).sendError(403);
 			} else {
 				this.usersDAO.updateUser(user);
 				if (this.usersDAO.searchUserChangePass(LoggedUsername) == 1){
 					Logger.getLogger(getClass()).info("Usuário " + LoggedUsername + " solicitou recuperação de sennha a pouco tempo, apagando registro da tabela temporária");
 					this.usersDAO.delUserHasChangedPass(LoggedUsername);
 				}
 			}
 
 			result.redirectTo(HomeController.class).home("");
 		}
 	}
 }
