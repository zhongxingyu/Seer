 package de.uni_hamburg.informatik.sep.zuul.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 
 public class ClientConsole extends Client
 {
 
 	public ClientConsole(String serverName, String serverIP, int clientport,
 			String clientName) throws MalformedURLException, RemoteException,
 			NotBoundException
 	{
 		super(serverName, serverIP, clientport, clientName);
 
 		if(!serverIP.equals("127.0.0.1"))
 		{
 			warteFenster();
 		}
 		else
 		{
 			login();
 			_server.empfangeStartEingabe(_clientName);
 		}
 
 	}
 
 	private void warteFenster() throws RemoteException
 	{
 		login();
 		schreibeText("Warten auf Spieler Bereit.");
 		schreibeText("Geben sie OK ein, wenn sie auch Bereit sind: ");
 		String bereit = leseZeileEin();
 		if(bereit.equals("OK"))
 		{
 			_server.empfangeStartEingabe(_clientName);
 		}
 	}
 
 	@Override
 	public void schreibeText(String text)
 	{
 		System.out.println(text);
 	}
 
 	@Override
 	public void run() throws RemoteException
 	{
 		while(!_isSpielzuEnde)
 		{
 			String nachricht = leseZeileEin();
 
 			verarbeiteEingabe(nachricht);
 
 			//TODO warten einbauen
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	protected String leseZeileEin()
 	{
 		System.out.print("> ");
 
 		String eingabezeile = "";
 		BufferedReader eingabe = new BufferedReader(new InputStreamReader(
 				System.in));
 		try
 		{
 			eingabezeile = eingabe.readLine();
 		}
 		catch(IOException exc)
 		{
 			System.out.println("There was an error during reading: "
 					+ exc.getMessage());
 		}
 		return eingabezeile;
 	}
 
 	@Override
 	public boolean zeigeAn(ClientPaket paket) throws RemoteException
 	{
		for(String zeile : paket.getNachricht().split("\n"))
			System.out.println(zeile);
		run();
 		return true;
 	}
 
 	@Override
 	public boolean zeigeVorschau(ClientPaket paket) throws RemoteException
 	{
 		return zeigeAn(paket);
 	}
 
 	@Override
 	public void starteClientUI(ClientPaket paket) throws RemoteException
 	{
 
 	}
 
 	@Override
 	public void beendeSpiel(boolean duHastGewonnen) throws RemoteException
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void serverBeendet()
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 }
