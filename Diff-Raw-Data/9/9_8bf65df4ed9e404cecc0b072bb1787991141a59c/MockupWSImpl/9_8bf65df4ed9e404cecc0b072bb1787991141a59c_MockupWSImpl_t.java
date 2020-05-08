 package com.sergio.ws.impl;
 
 import javax.jws.WebService;
 
 import com.sergio.ws.MockupWS;
 
@WebService(serviceName = "MockupWS", endpointInterface ="com.sergio.ws.MockupWS")
 public class MockupWSImpl implements MockupWS{
 
 	public String ejecutarMetodoWS(String sessionGuid, String parametro01,
 			String parametro02)  {
 		
 		String resultado = "OK";
 		
 		
 		
 		return resultado;
 	}
 	
 	/*
 	 * PARA FICHEROS
 	public void fileDownload(String nombre,
 			@XmlMimeType("application/octet-stream") DataHandler data, String codRutaRelativa)
 			throws IOException {
 		String message = MessageFormat.format((String) lp
 				.getValor(ConstantesUtils.MESSAGE_FICHERO_COPIA_INICIO),
 				nombre);
 		helper.showMessageInfo(message);
 		
 		Fichero fichero = new Fichero();
 		fichero.setRutaAbsoluta(codRutaRelativa, nombre);
 		String rutaAbsoluta = fichero.getRutaAbsoluta();
 		StreamingDataHandler dh = (StreamingDataHandler) data;
 		File file = new File(rutaAbsoluta);
 		dh.moveTo(file);
 		dh.close();
 		
 		message = MessageFormat.format((String) lp
 				.getValor(ConstantesUtils.MESSAGE_FICHERO_COPIA_FIN),
 				nombre);
 		helper.showMessageInfo(message);
 	}*/
 	
 
 }
