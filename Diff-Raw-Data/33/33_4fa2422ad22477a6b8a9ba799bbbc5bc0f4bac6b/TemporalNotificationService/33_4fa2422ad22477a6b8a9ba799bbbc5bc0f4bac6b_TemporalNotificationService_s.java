 package com.eusa.service;
 import com.eusa.dao.ServicioDao;
 import com.eusa.domain.Servicio;
 import com.eusa.domain.Equipo;
 import com.eusa.domain.ServicioTemporal;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Properties;
 
 
 public class TemporalNotificationService {
 	
 	
	private ServicioDao servicioDao;
 	
 
 
	
 	final String MAIL_TO = "laura_luc@hotmail.com";
 	//final String MAIL_TO = "eusconstrucciones@gmail.com";
 	final String MAIL_FROM="eusaweb@gmail.com";
 	final String password="1_eusaweb";
 	
 	public void calculaServicio(Servicio serv){
 		
 		serv = (ServicioTemporal) serv;
 		Long ini1=((ServicioTemporal) serv).getFechaIni().getTime();
 		Long fin1=((ServicioTemporal) serv).getFechafin().getTime();
 		Long hoy=obtenFechaHoy();
 		if(hoy>ini1 && hoy<fin1){
 			System.out.println("Estamos en el rango inicial");
 			String carro=obtenerEquipo(serv);
 			mandarCorreo(serv, carro);
 		}else{
 			System.out.println("No estamos en el rango inicial, vamos a calcular");
 			calculaProxServicio(serv);
 		}
 	
 	}
 	
    	public void calculaProxServicio(Servicio serv){
 		   System.out.println("Soy temporal");
 		   GregorianCalendar dummy = new GregorianCalendar();
 		   
 		   /*
 		    * Primero obtengo las fechas de inicio y de fin para el servicio
 		    * 
 		    * Todas las fechas están al año 1970
 		   */	   
 		   GregorianCalendar ini1=new GregorianCalendar();
 		   ini1.setTime(((ServicioTemporal) serv).getFechaIni());
 		   
 		   GregorianCalendar fin1=new GregorianCalendar();
 		   fin1.setTime(((ServicioTemporal) serv).getFechafin());
 		   /*
 		    * Despues obtengo la frecuencia del servicio  y se la sumo al 
 		    * rango inicial
 		    * 
 		    */
 		   
 		   Calendar ini2=ini1;
 		   ini2.add(Calendar.MONTH,((ServicioTemporal) serv).getFrecuencia()); 
 		   System.out.println("ini2 "+ini2.get(Calendar.MONTH)+" " +ini2.get(Calendar.DAY_OF_MONTH));
 		   
 		   Calendar fin2=fin1;
 		   fin2.add(Calendar.MONTH,((ServicioTemporal) serv).getFrecuencia());
 		   System.out.println("fin2 "+fin2.get(Calendar.MONTH)+" " +fin2.get(Calendar.DAY_OF_MONTH));
 		   
 		   if(ini2.get(Calendar.YEAR)>1970){
 			   ini2.add(Calendar.YEAR,-1);
 		   }
 		   if(fin2.get(Calendar.YEAR)>1970){
 			   fin2.add(Calendar.YEAR,-1);
 		   }
 
 		   Long hoy=obtenFechaHoy();
 		   System.out.println("hoy: "+hoy);
 		   if(hoy>ini2.getTime().getTime() && hoy<fin2.getTime().getTime()){
 			   String carro=obtenerEquipo(serv);
 			   mandarCorreo(serv, carro);
 		   }else{
 		   	System.out.println("No mandare correos");
 		   }
 	}
 	   
 	public Long obtenFechaHoy(){
 		Calendar c=Calendar.getInstance();
 		GregorianCalendar dummy = new GregorianCalendar();
 		Date miFecha;
 		System.out.println("Fechas-- mes: "+c.get(Calendar.MONTH)+" dia: "+c.get(Calendar.DAY_OF_MONTH));
 		dummy.set(1970, c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)); // Meses contando desde 0; Enero = 0
 		//return dummy.getTime();
 		miFecha = dummy.getTime();
 		//System.out.println("miFecha: "+miFecha);
 		return miFecha.getTime();
 	}
 	
 	public void mandarCorreo(Servicio serv, String carro){
 		System.out.println("Nombre "+serv.getNombre());
 		Properties props = new Properties();
 		props.put("mail.smtp.host", "smtp.gmail.com");
 		props.put("mail.smtp.socketFactory.port", "465");
 		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
 		props.put("mail.smtp.auth", "true");
 		props.put("mail.smtp.port", "465");
 
 		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
 					protected PasswordAuthentication getPasswordAuthentication() {
 						return new PasswordAuthentication(MAIL_FROM,password);
 					}
 				});
 		try {
 
 			Message message = new MimeMessage(session);
 			message.setFrom(new InternetAddress(MAIL_FROM));
 			message.setRecipients(Message.RecipientType.TO,
 					InternetAddress.parse(MAIL_TO));
 			message.setSubject("Recordatorio Proximo Vencimiento");
 			message.setText("Estimado," +
 					"\n\n Le recordamos que el servicio "+serv.getNombre()+" para el vehiculo: \n\n"+carro+" esta proximo a vencer"+
 					"\n\n Le sugerimos realizar el servicio a la brevedad"+
 					"\n\n Ingresa a la liga para capturarla \n\n eusa.notification.mx:8080/captura/"+serv.getId()+
 					"\n\n\n Atentamente Enlaces Urbanos y Suburbanos");
 
 			//Transport.send(message);
 
 			System.out.println("Done");
 
 		} catch (MessagingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public String obtenerEquipo(Servicio serv){
 		
		System.out.println(serv.getEquipos());
 		//Equipo equipo=Equipo.findById(serv.getId());
 		return "equipo";
 
 	}
 
 }
