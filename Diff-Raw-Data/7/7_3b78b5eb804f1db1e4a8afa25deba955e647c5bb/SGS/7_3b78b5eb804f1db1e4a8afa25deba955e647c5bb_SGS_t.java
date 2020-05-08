 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.server.robots;
 
 import org.accesointeligente.model.Request;
 import org.accesointeligente.shared.RequestStatus;
 import org.apache.http.*;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.util.EntityUtils;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 
 import java.beans.ConstructorProperties;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SGS extends Robot {
 	private HttpClient client;
 	private HtmlCleaner cleaner;
 	private Boolean loggedIn = false;
 	private String characterEncoding = null;
 	private String baseUrl;
 
 	public SGS() {
 		client = new DefaultHttpClient();
 		HttpProtocolParams.setUserAgent(client.getParams(), "Mozilla/5.0 (X11; U; Linux x86_64; es-CL; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12");
 		HttpProtocolParams.setVersion(client.getParams(), HttpVersion.HTTP_1_0);
 		cleaner = new HtmlCleaner();
 	}
 
 	@ConstructorProperties({"idEntidad", "baseUrl"})
 	public SGS(String idEntidad, String baseUrl) {
 		this();
 		setIdEntidad(idEntidad);
 		setBaseUrl(baseUrl);
 	}
 
 	@Override
 	public void login() throws RobotException {
 		if (characterEncoding == null) {
 			detectCharacterEncoding();
 		}
 
 		List<NameValuePair> formParams;
 		HttpPost post;
 		HttpGet get;
 		HttpResponse response;
 		TagNode document;
 		Header location;
 
 		try {
 			formParams = new ArrayList<NameValuePair>();
 			formParams.add(new BasicNameValuePair("login", username));
 			formParams.add(new BasicNameValuePair("password", password));
 			formParams.add(new BasicNameValuePair("Ingresar", "Ingresar"));
 
 			post = new HttpPost(baseUrl + "?accion=login");
 			post.addHeader("Referer", baseUrl + "?accion=Home");
 			post.setEntity(new UrlEncodedFormEntity(formParams, characterEncoding));
 			response = client.execute(post);
 			location = response.getFirstHeader("Location");
 
 			if (location == null || !"index.php".equals(location.getValue())) {
 				throw new Exception();
 			}
 
 			EntityUtils.consume(response.getEntity());
 
 			get = new HttpGet(baseUrl + "");
 			get.addHeader("Referer", baseUrl + "?accion=Home");
 			response = client.execute(get);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 
 			if (document.getElementListByAttValue("href", "index.php?accion=Salir", true, false).isEmpty()) {
 				throw new Exception();
 			}
 
 			loggedIn = true;
 		} catch (Throwable ex) {
			ex.printStackTrace();
 			throw new RobotException();
 		}
 	}
 
 	@Override
 	public Request makeRequest(Request request) throws RobotException {
 		if (!loggedIn) {
 			login();
 		}
 
 		List<NameValuePair> formParams;
 		HttpPost post;
 		HttpResponse response;
 		TagNode document, hidden;
 		Integer folio;
 		Header location;
 		Pattern pattern;
 		Matcher matcher;
 		String remoteIdentifier;
 
 		try {
 			formParams = new ArrayList<NameValuePair>();
 			formParams.add(new BasicNameValuePair("id_entidad", idEntidad));
 			formParams.add(new BasicNameValuePair("identificacion_documentos", request.getInformation() + "\n\n" + request.getContext()));
 			formParams.add(new BasicNameValuePair("notificacion", "1"));
 			formParams.add(new BasicNameValuePair("id_forma_recepcion", "1")); // Email
 			formParams.add(new BasicNameValuePair("oficina", ""));
 			formParams.add(new BasicNameValuePair("id_formato_entrega", "2")); // Digital
 			formParams.add(new BasicNameValuePair("Registrarse", "Continuar"));
 
 			post = new HttpPost(baseUrl + "?accion=solicitud-de-informacion&act=4");
 			post.addHeader("Referer", baseUrl + "?accion=Solicitud-de-Informacion");
 			post.setEntity(new UrlEncodedFormEntity(formParams, characterEncoding));
 			response = client.execute(post);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 
 			hidden = document.findElementByAttValue("name", "folio_hidden", true, true);
 
 			if (hidden == null || !"input".equals(hidden.getName())) {
 				throw new Exception();
 			}
 
 			folio = Integer.parseInt(hidden.getAttributeByName("value"));
 
 			if (folio == null) {
 				throw new Exception();
 			}
 
 			formParams = new ArrayList<NameValuePair>();
 			formParams.add(new BasicNameValuePair("folio_hidden", folio.toString()));
 			formParams.add(new BasicNameValuePair("Aceptar", "Enviar Solicitud"));
 
 			post = new HttpPost(baseUrl + "?accion=solicitud-de-informacion&act=6");
 			post.addHeader("Referer", baseUrl + "?accion=solicitud-de-informacion&act=4");
 			post.setEntity(new UrlEncodedFormEntity(formParams, characterEncoding));
 			response = client.execute(post);
 			location = response.getFirstHeader("Location");
 
 			if (location == null) {
 				throw new Exception();
 			}
 
 			pattern = Pattern.compile("^index.php\\?accion=solicitud-de-informacion&act=5&folio=(.+)$");
 			matcher = pattern.matcher(location.getValue());
 
 			if (!matcher.matches()) {
 				throw new Exception();
 			}
 
 			remoteIdentifier = matcher.group(1);
 
 			if (remoteIdentifier == null || remoteIdentifier.length() == 0) {
 				throw new Exception();
 			}
 
 			EntityUtils.consume(response.getEntity());
 
 			request.setRemoteIdentifier(remoteIdentifier);
 			request.setStatus(RequestStatus.PENDING);
 
 			return request;
 		} catch (Throwable ex) {
			ex.printStackTrace();
 			throw new RobotException();
 		}
 	}
 
 	@Override
 	public RequestStatus checkRequestStatus(Request request) throws RobotException {
 		if (!loggedIn) {
 			login();
 		}
 
 		HttpGet get;
 		HttpResponse response;
 		TagNode document, statusCell;
 		String statusLabel;
 
 		try {
 			if (request.getRemoteIdentifier() == null || request.getRemoteIdentifier().length() == 0) {
 				throw new Exception();
 			}
 
 			get = new HttpGet(baseUrl + "?accion=mis-solicitudes&act=1&folio=" + request.getRemoteIdentifier());
 			get.addHeader("Referer", baseUrl + "?accion=Mis-Solicitudes");
 			response = client.execute(get);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 			statusCell = document.findElementByAttValue("width", "36%", true, true);
 
 			if (statusCell == null) {
 				throw new Exception();
 			}
 
 			statusLabel = statusCell.getText().toString().trim();
 
 			// TODO: check if request expired
 			if (statusLabel.equals("En Proceso")) {
 				return RequestStatus.PENDING;
 			} else if (statusLabel.equals("Respondida")) {
 				return RequestStatus.CLOSED;
 			} else if (statusLabel.equals("Derivada")) {
 				return RequestStatus.DERIVED;
 			} else {
 				return null;
 			}
 		} catch (Throwable ex) {
			ex.printStackTrace();
 			throw new RobotException();
 		}
 	}
 
 	@Override
 	public Boolean checkInstitutionId() throws RobotException {
 		if (!loggedIn) {
 			login();
 		}
 
 		HttpGet get;
 		HttpResponse response;
 		TagNode document, selector;
 
 		try {
 			get = new HttpGet(baseUrl + "?accion=Solicitud-de-Informacion");
 			response = client.execute(get);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 			selector = document.findElementByAttValue("name", "id_entidad", true, true);
 
 			if (selector == null) {
 				throw new Exception();
 			}
 
 			for (TagNode option : selector.getChildTags()) {
 				if (option.hasAttribute("value") && option.getAttributeByName("value").equals(idEntidad)) {
 					return true;
 				}
 			}
 
 			return false;
 		} catch (Throwable ex) {
			ex.printStackTrace();
 			throw new RobotException();
 		}
 	}
 
 	public void detectCharacterEncoding() {
 		HttpGet get;
 		HttpResponse response;
 		Header contentType;
 		Pattern pattern;
 		Matcher matcher;
 
 		try {
 			get = new HttpGet(baseUrl + "?accion=Home");
 			response = client.execute(get);
 			contentType = response.getFirstHeader("Content-Type");
 			EntityUtils.consume(response.getEntity());
 
 			if (contentType == null || contentType.getValue() == null) {
 				characterEncoding = "ISO-8859-1";
 			}
 
 			pattern = Pattern.compile(".*charset=(.+)");
 			matcher = pattern.matcher(contentType.getValue());
 
 			if (!matcher.matches()) {
 				characterEncoding = "ISO-8859-1";
 			}
 
 			characterEncoding = matcher.group(1);
 		} catch (Exception e) {
 			characterEncoding = "ISO-8859-1";
 		}
 	}
 
 	public String getCharacterEncoding() {
 		return characterEncoding;
 	}
 
 	public void setCharacterEncoding(String characterEncoding) {
 		this.characterEncoding = characterEncoding;
 	}
 
 	public String getBaseUrl() {
 		return baseUrl;
 	}
 
 	public void setBaseUrl(String baseUrl) {
 		this.baseUrl = baseUrl;
 	}
 
 }
