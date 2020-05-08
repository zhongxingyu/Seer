 package eu.ist.fears.client;
 
 import java.util.Date;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import eu.ist.fears.client.admin.Admin;
 import eu.ist.fears.client.interfaceweb.Header;
 import eu.ist.fears.client.interfaceweb.Path;
 import eu.ist.fears.common.FearsAsyncCallback;
 import eu.ist.fears.common.FearsConfigClient;
 import eu.ist.fears.common.communication.Communication;
 import eu.ist.fears.common.views.ViewVoterResume;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Fears extends Widget implements EntryPoint, ValueChangeHandler<String> {
 
     protected static Communication com;
     protected VerticalPanel frameBox;
     protected VerticalPanel frame;
     protected static VerticalPanel content;
     protected static Header header;
     protected static HorizontalPanel footer;
     protected static DialogBox popup;
     protected static Path path;
     protected static ViewVoterResume curretUser;
     public static boolean validCookie;
     protected static String lastURL;
     protected static String currentProject;
 
     /**
      * This is the entry point method.
      */
     public void onModuleLoad() {
 
 	if (RootPanel.get("Admin") != null) {
 	    return;
 	}
 
 	init();
 
 	History.addValueChangeHandler(this);
 	History.fireCurrentHistoryState();
     }
 
     @SuppressWarnings("deprecation")
     public void init() {
 	com = new Communication("service");
 	verifyAuthentication();
 	
 	frameBox = new VerticalPanel();
 	frame = new VerticalPanel();
 	content = new VerticalPanel();
 	footer = new HorizontalPanel();
 	content.setStyleName("width100");
 	path = new Path();
 	curretUser = new ViewVoterResume("guest", "guest", null, false);
 	currentProject = null;
 	header = new Header();
 	RootPanel.get().setStyleName("centered");
 	Window.setMargin("0px");
 	RootPanel.get().add(header);
 	frameBox.setStyleName("frameBox");
 	frame.setStyleName("frame");
 	RootPanel.get().add(frameBox);
 	frameBox.add(frame);
 	frame.add(path);
 	frame.add(content);
 	footer.setStyleName("footer");
 	footer.add(new HTML("© " + (new Date().getYear() + 1900)
 		+ ", Instituto Superior Técnico. Todos os direitos reservados.  |  &nbsp;"));
 	footer.add(new Hyperlink(" Sobre o FeaRS", "help"));
 	RootPanel.get().add(footer);
 
 	
 	Fears.getHeader().update(false, isAdminPage());
 
     }
 
     private void verifyAuthentication() {
 	String ticket = getTicket();
 	if (ticket != null && !(ticket.length() == 0)) {
 	    if (this instanceof Admin)
 		com.CASlogin(ticket, true, null, new WaitForLogin());
 	    else
 		com.CASlogin(ticket, false, null, new WaitForLogin());
 
 	    return;
 	}
 	verifyLogin(false);
     }
 
     public static void setContet(Widget w) {
 	content.clear();
 	content.add(w);
     }
 
     public static void setError(Widget w) {
 	w.setStyleName("error");
 	setContet(w);
     }
 
     public static boolean isAdminPage() {
 	return RootPanel.get("Admin") != null;
     }
 
     public static Path getPath() {
 	return path;
     }
 
     public static Header getHeader() {
 	return header;
     }
 
     public static void setCurrentUser(ViewVoterResume v) {
 	curretUser = v;
 	header.update((getCurrentProject() != null ? true : false), isAdminUser());
     }
 
     public static boolean isLogedIn() {
 	return validCookie;
     }
 
     public static boolean isAdminUser() {
 	return curretUser.isAdmin();
     }
 
     public static String getUsername() {
 	return curretUser.getName();
     }
 
     public static String getNickname() {
 	return curretUser.getNick();
     }
 
     public static int getVotesLeft() {
 	return curretUser.getVotesLeft();
     }
 
     public static String getUserOID() {
 	return curretUser.getOID();
     }
 
     public static String getCurrentProject() {
 	return currentProject;
     }
 
     public void listFeatures(String projectName, String filter) {
 	content.clear();
 
 	verifyLogin(false);
 
 	ListFeatures features = new ListFeatures(projectName, filter);
 
 	features.update();
 	content.add(features);
     }
 
     public void addFeature(String projectName) {
 	content.clear();
 
 	if (!verifyLogin(true)) {
 	    content.add(new HTML("Por favor fa&ccedil;a login para continuar"));
 	    return;
 	}
 
 	content.add(new CreateFeature(projectName));
 
     }
 
     public void viewFeature(String projectName, String featureID) {
 	content.clear();
 
 	verifyLogin(false);
 	DisplayFeatureDetailed d = new DisplayFeatureDetailed(projectName, featureID);
 	content.add(d);
     }
 
     public void viewVoter(String projectID, String voterName) {
 	content.clear();
 	verifyLogin(false);
 	content.add(new DisplayVoter(projectID, voterName));
     }
 
     public void viewListProjects() {
 	content.clear();
 
 	verifyLogin(false);
 
 	Fears.getPath().setFears();
 
 	ListProjects projects = new ListProjects();
 
 	projects.update();
 	HorizontalPanel intro = new HorizontalPanel();
 	if (this instanceof Admin)
 	    intro.add(new HTML(getFearsIntro() + " <a href=\"" + GWT.getHostPageBaseURL() + "Admin.html#help\"> Ler Mais</a>"));
 	else
 	    intro.add(new HTML(getFearsIntro() + " <a href=\"" + GWT.getHostPageBaseURL() + "#help\"> Ler Mais</a>"));
 
 	content.add(intro);
 	content.add(projects);
     }
 
     public void viewHelp() {
 	content.clear();
 
 	verifyLogin(false);
 	Fears.getPath().setHelp();
 	showHelp();
     }
 
     public void showHelp() {
 	VerticalPanel help = new VerticalPanel();
 
 	HTML first = new HTML(getFearsIntro(), true);
 	help.add(first);
 
 	Label titleproj = new Label("Projectos");
 	titleproj.setStyleName("helpTitles");
 	Label proj = new Label(
 		"Cada projecto tem um conjunto de sugestões próprio. Ao entrar no FeaRS o utilizador deve escolher o projecto no qual pretende criar ou consultar sugestões.",
 		true);
 	help.add(titleproj);
 	help.add(proj);
 
 	Label titleList = new Label("Lista de Sugestões");
 	titleList.setStyleName("helpTitles");
 	Label list = new Label(
 		"Depois de escolher o projecto, é visualizada a lista de sugestões existentes. Esta lista aparece ordenada por \"data de modificação\", aparecendo no topo as sugestões que foram alvo de algum tipo de modificação (um comentário, mudança de estado, etc.). No entanto é possível ordenar e filtrar a lista por outros critérios. Para isso basta utilizar a barra de opções por baixo do campo de pesquisa.",
 		true);
 	help.add(titleList);
 	help.add(list);
 
 	Label titleState = new Label("Estados");
 	titleState.setStyleName("helpTitles");
 	Label state = new HTML(
 		"Uma sugestão pode ter cinco estados diferentes. São os responsáveis pelo projecto que controlam o estado das sugestão.<p>"
 			+ "<ul><li> <b>Novo</b> - Estado inicial.</li>"
 			+ "<li> <b>Planeado</b> - A sugestão foi aprovada e a sua implementação está prevista.</li>"
 			+ "<li> <b>Implementação</b> - A sugestão está a ser implementada no sistema.</li>"
 			+ "<li> <b>Completo</b> - A sugestão já está implementada no sistema.</li>"
 			+ "<li> <b>Rejeitado</b> - A sugestão não foi aprovada.</li></ul>");
 	help.add(titleState);
 	help.add(state);
 
 	Label titleVote = new Label("Votos");
 	titleVote.setStyleName("helpTitles");
 	Label vote = new Label(
 		"Os votos permitem medir a popularidade de uma sugestão. Cada utilizador tem um número limitado de votos para usar nas sugestões que achar mais importantes. Uma vez esgotados todos os seus votos, o utilizador não pode votar em mais nenhuma sugestão até ter de novo mais votos disponíveis. Um utilizador recupera votos quando as sugestões em que tenha votado deixam de estar no estado Novo, ou quando remove votos de sugestões em que tenha votado anteriormente. Só é possível votar e remover votos se a sugestão estiver no estado Novo.",
 		true);
 	help.add(titleVote);
 	help.add(vote);
 
 	Label titleCreate = new Label("Criar uma Sugestão");
 	titleCreate.setStyleName("helpTitles");
 	Label create = new Label(
 		"Qualquer utilizador pode criar uma sugestão desde que tenha acesso ao projecto. Para criar uma sugestão basta carregar no botão ao lado do botão de pesquisa e preencher o formulário.",
 		true);
 	help.add(titleCreate);
 	help.add(create);
 
 	Label titleGoodPractices = new Label("Boas práticas para a criação de sugestões");
 	titleGoodPractices.setStyleName("helpTitles");
 	Label goodPractices = new HTML(getgoodPracticesHead() + ":<p>" + getgoodPracticesLong());
 	help.add(titleGoodPractices);
 	help.add(goodPractices);
 
 	content.add(help);
     }
 
     public static String getgoodPracticesHead() {
 	return "A utilidade e eficácia do sistema FeaRS depende em grande parte das sugestões criadas. Por isso, sugere-se a adopção das seguintes boas práticas na criação de novas sugestões";
 
     }
 
     public static String getFearsIntro() {
 	return "O FeaRS (Feature Request System) é um sistema que permite gerir sugestões. Os utilizadores podem criar novas sugestões e votar em sugestões já existentes. As sugestões podem ser pedidos de novas funcionalidades ou de alterações a funcionalidades existentes, com o objectivo de melhorar o serviço.  Através do número de votos das sugestões, os responsáveis pelos projectos percebem quais as sugestões mais populares, ajudando-os a definir a prioridade de implementação."
 		+ "<br>Não utilize o sistema FeaRS para a indicação de erros ou \"bugs\" do sistema. Estes devem ser enviados para <a href=\"mailto:ci@ist.utl.pt\">ci@ist.utl.pt</a>, como habitualmente.";
     }
 
     public static String getgoodPracticesLong() {
 	return "<ul><li> <b>Pesquisar primeiro</b> - Antes de criar uma nova sugestão utilize a opção de pesquisa para verificar se não existe já uma sugestão idêntica, na qual pode votar e/ou adicionar um comentário.</li>"
 		+ "<li> <b>Uma ideia por sugestão</b> - Exponha apenas uma ideia por cada sugestão feita, em vez de propor várias em simultâneo na mesma sugestão.</li>"
 		+ "<li> <b>Rever antes de criar</b> - Releia a sua sugestão antes de a criar, reescrevendo-a se necessário para a tornar mais clara.</li>"
 		+ "<li> <b>Ser sucinto</b> - Seja sucinto na forma como expõe a sua sugestão.</li>"
 		+ "<li> <b>Ser construtivo</b> - Se tiver ideias de como resolver um determinado problema, apresente as suas ideias, para além de indicar o que está mal.</li></ul>";
     }
 
     public void viewLogin() {
 	// content.clear();
 
 	verifyLogin(false);
 
 	if (this instanceof Admin)
	    Window.open("../redirectLogin?service=" + GWT.getHostPageBaseURL() + "Admin.html", "_self", "");
 	else
	    Window.open("../redirectLogin?service=" + GWT.getHostPageBaseURL() + "Fears.html", "_self", "");
 
     }
 
     public void CASLogout() {
 
 	validCookie = false;
 	header.update(false, isAdminPage());
 	com.logoff(Cookies.getCookie("fears"), new FearsAsyncCallback<Object>() {
 	    public void onSuccess(Object result) {
 	    }
 	});
 	curretUser.setName("guest");
 	Cookies.removeCookie("fears");
 	Cookies.removeCookie("JSESSIONID");
 
     }
 
     public void setCookie(String value, ViewVoterResume user) {
 	final long DURATION = 1000 * 60 * 60 * 1; // duration remembering login,
 	// 1 hour
 	Date expires = new Date(System.currentTimeMillis() + DURATION);
 	Cookies.setCookie("fears", value, expires);
 	setCurrentUser(user);
 	validCookie = true;
     }
 
     protected boolean verifyLogin(boolean tryToLogin) {
 
 	if (validCookie) {
 	    return true;
 	}
 
 	String sessionID = Cookies.getCookie("fears");
 	com.validateSessionID(sessionID, new ValidateSession(this, tryToLogin));
 	return false;
 
     }
 
     public void onHistoryChanged(String historyToken) {
     }
 
     public static void parseURL(String url, Fears f) {
 	// This method is called whenever the application's history changes. Set
 	// the label to reflect the current history token.
 
 	if (url.length() == 0) {
 	    f.viewListProjects();
 	}
 
 	if (url.startsWith("projectos")) {
 	    f.viewListProjects();
 	} else if (url.startsWith("login")) {
 	    f.viewLogin();
 	} else if (url.startsWith("Project")) {
 	    projectParse(url.substring("Project".length()), f);
 	}
 	if (url.startsWith("admins")) {
 	    if (f instanceof Admin)
 		((Admin) f).viewEditAdmins();
 	} else if (url.startsWith("logout")) {
 	    f.CASLogout();
 	} else if (url.startsWith("createProject")) {
 	    if (f instanceof Admin)
 		((Admin) f).viewCreateProject();
 	} else if (url.startsWith("help")) {
 	    f.viewHelp();
 	} else if (url.startsWith("viewUser")) {
 	    f.viewVoter(null, url.substring("viewUser".length()));
 	}
 
     }
 
     private static void projectParse(String string, Fears f) {
 	int parseAt = string.indexOf('&');
 	int parseB = string.indexOf("%26");
 	String projectID;
 	String parse;
 
 	// Estamos no Caso: #ProjectXPTO
 	if (parseAt == -1 && parseB == -1) {
 	    projectID = string;
 	    currentProject = projectID;
 	    /* getCurrentUser, to update Votes */
 	    header.update(projectID);
 	    f.listFeatures(projectID, "");
 	    return;
 	}
 
 	if (parseAt != -1) {
 	    projectID = string.substring(0, parseAt);
 	    parse = string.substring(parseAt + 1);
 
 	} else {
 	    projectID = string.substring(0, parseB);
 	    parse = string.substring(parseB + 3);
 	}
 
 	currentProject = projectID;
 	/* getCurrentUser, to update Votes */
 	header.update(projectID);
 
 	if ("listFeatures".equals(parse)) {
 	    f.listFeatures(projectID, "");
 	} else if ("addFeature".equals(parse)) {
 	    f.addFeature(projectID);
 	} else if (parse.startsWith("viewFeature")) {
 	    f.viewFeature(projectID, parse.substring("viewFeature".length()));
 	} else if (parse.startsWith("viewUser")) {
 	    f.viewVoter(projectID, parse.substring("viewUser".length()));
 	} else if (parse.startsWith("filter")) {
 	    f.listFeatures(projectID, parse.substring("filter".length()));
 	} else if (parse.startsWith("edit")) {
 	    if (f instanceof Admin)
 		((Admin) f).viewEditProject(projectID);
 	} else if (parse.startsWith("adminEdit")) {
 	    if (f instanceof Admin)
 		((Admin) f).viewEditAdmins(projectID);
 	}
 
     }
 
     public void loggedIn() {
 	// popup.hide();
 	if (History.getToken().equals("login")) {
 	    History.newItem(lastURL);
 	    if (History.getToken().equals("loggedIn")) {
 		History.newItem(lastURL);
 	    }
 	} else {
 	    History.fireCurrentHistoryState();
 	}
     }
 
     /*
      * public native void saveFears(Fears f)-{ $wnd.myfears=f; }-;
      */
 
     /*
      * public static native void callLoggedIn()-{ var temp=$wnd.parent.myfears;
      * temp.@eu.ist.fears.client.Fears::loggedIn()(); }-;
      */
 
     public static native String getParamString() /*-{
         return $wnd.location.search;
     }-*/;
 
     public static String getTicket() {
 	String string = getParamString();
 	
 	if (!string.startsWith("?ticket="))
 	    return null;
 
 	int index = string.indexOf('=');
 	if (string.length() > index + 1) {
 	    String ticket = string.substring(index + 1);
 	    return ticket;
 	}
 	return null;
 
     }
 
     protected class ValidateSession extends FearsAsyncCallback<Object> {
 	Fears fears;
 	boolean trytoLogin;
 
 	public ValidateSession(Fears f, boolean trytoLogin) {
 	    this.fears = f;
 	    this.trytoLogin = trytoLogin;
 	}
 
 	public void onSuccess(Object result) {
 	    ViewVoterResume voter = (ViewVoterResume) result;
 	    if (voter != null) {
 		validCookie = true;
 		setCurrentUser(voter);
 		fears.onHistoryChanged(History.getToken());
 	    } else {
 		if (trytoLogin)
 		    viewLogin();
 	    }
 	}
 
     };
 
     protected class WaitForLogin extends FearsAsyncCallback<Object> {
 
 	public WaitForLogin() {
 	}
 
 	public void onSuccess(Object result) {
 	    ViewVoterResume voter = (ViewVoterResume) result;
 	    if (voter != null) {
 		Log.log("yes");
 		Fears.setCurrentUser(voter);
 		loggedIn();
 		return;
 	    }
 	}
 
     }
 
     public void onValueChange(ValueChangeEvent<String> event) {
 	String historyToken = event.getValue();
 	if (RootPanel.get("Admin") != null) {
 	    return;
 	}
 
 	if (!"login".equals(historyToken) && !"logoff".equals(historyToken))
 	    lastURL = historyToken;
 
 	header.update(false, false);
 	parseURL(historyToken, this);
 
     }
 
 }
