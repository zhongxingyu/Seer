 import java.util.ArrayList;
 import java.util.Scanner;
 
 import CuevanaApi.Captcha;
 import CuevanaApi.Content;
 import CuevanaApi.Content.ContentType;
 import CuevanaApi.CuevanaApi;
 import CuevanaApi.NotValidCaptcha;
 import CuevanaApi.Source;
 import CuevanaApi.SourceSearcher;
 
 
 public class Prueba {
 	
 	static CuevanaApi api = new CuevanaApi();
 	static Scanner input = new Scanner(System.in);
 	static boolean DEBUG = true;
 	
 	public static void main(String[] args) throws Exception {
 		
 		String url = null;
 		if(args.length > 0)
 		{
 			url = args[0];
 		}else
 		{
 			if(DEBUG)
 			{
 				url = "http://www.cuevana.tv/#!/series/3497/how-i-met-your-mother/come-on";
 			}else
 			{
 				System.out.println("Usage: java " + Prueba.class.getName() + " <URL>");
 				return;
 			}
 		}
 		
 		
 		System.out.println("[+] Test Cuevana API: ");
 		System.out.println("[!] URL: " + url);
 		Content contenido = null;
 		
 		
 		try
 		{
 			if (api.get_type(url) == ContentType.EPISODE) {
 				contenido = api.get_episode(url);
 			} else {
 				contenido = api.get_movie(url);
 			}
 		}catch(Exception NotValidURL)
 		{
 			System.out.println("[!] Lo siento, URL invalida");
 			return;
 		}
 		System.out.println(contenido.toString());
 		ArrayList<Source> sources = contenido.getSources();
 		System.out.println("[+] Existen " + sources.size() + " sources:");
 		for (int i = 0; i < sources.size(); i++) {
 			System.out.println("\t[" + (i + 1) + "] " + sources.get(i).toString());
 		}
 		int opcion = 0;
 		do {
 			System.out.print("[+] Source a buscar : ");
 			try {
				opcion = Integer.valueOf(input.nextLine());
 			} catch (Exception e) {
 				continue;
 			}
		} while (opcion <= 0 || opcion > sources.size());
 		
 		SourceSearcher ss = new SourceSearcher(sources.get(1));
 		
 		while(true)
 		{
 			Captcha cacha = ss.getCaptcha();
 			System.out.println("[+] Captcha image url: " + cacha.getUrl());
 			System.out.print("[?] Solucion?: ");
 			if(input.hasNext())
 			{
 				try{
 					String sol = input.nextLine().trim();
 					System.out.println("[+] Link: " + ss.getLink(sol));
 					break;
 				}catch(Exception e)
 				{
 					System.out.println("[!] Error: " + e.getMessage() );
 				}
 			}
 		}
 		System.out.println("[+] Subtitulos: " + sources.get(1).getSubUrl());
 		
 	}
 
 }
