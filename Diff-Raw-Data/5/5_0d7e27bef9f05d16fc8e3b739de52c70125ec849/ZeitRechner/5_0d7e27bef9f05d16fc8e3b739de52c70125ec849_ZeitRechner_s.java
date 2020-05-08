 package gui;
 import java.awt.GridLayout;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import data.Config;
 import data.ZeitListener;
 
 
 import zeitgeber.AZeit;
 import zeitgeber.Zeit;
 
 
 public class ZeitRechner extends JPanel implements ZeitListener
 {
 	private static final long serialVersionUID = -4921294151324453428L;
 	AZeit uhr;
 	AZeit startZeit;
 	
 	JLabel jUhrzeit = new JLabel();
 	JLabel jGesamtZeit = new JLabel();
 	JLabel jProduktivZeit = new JLabel();
 	JLabel jPausenZeit = new JLabel();
 	JLabel jKontoVeraenderung = new JLabel();
 	JLabel jRestZeit = new JLabel();
 	
 	AZeit gesamtZeit;
 	AZeit produktivZeit;
 	char kontoVeraenderungPrefix;
 	AZeit kontoveraenderung;
 	AZeit pausenZeit;
 	AZeit restZeit; 
 	
 	public ZeitRechner(AZeit startZeit,AZeit uhr)
 	{
 		this.uhr = uhr;
 		this.uhr.addZeitListener(this);
 		this.startZeit = startZeit;
 		this.startZeit.addZeitListener(this);
 		this.setLayout(new GridLayout(0,2));
 		
 		this.add(new JLabel("von - bis"));
 		this.add(jUhrzeit);
 		
 		this.add(new JLabel("Gesamtzeit:"));
 		this.add(jGesamtZeit);
 		
 		this.add(new JLabel("Produktivzeit:"));
 		this.add(jProduktivZeit);
 		
 		this.add(new JLabel("Pausenzeit:"));
 		this.add(jPausenZeit);
 		
 		this.add(new JLabel("Konto Veraenderung:"));
 		this.add(jKontoVeraenderung);
 		
 		this.add(new JLabel("Zeit bis Plus:"));
 		this.add(jRestZeit);
 	}
 	@Override
 	public void timeHasChanged()
 	{
 		this.recalculate(uhr.getZeit());
 		this.display();
 		
 	}
 	private void display()
 	{
 		jUhrzeit.setText(startZeit.toString()+" - "+uhr.toString());
 		jGesamtZeit.setText(gesamtZeit.getNormalAnd100());
 		jProduktivZeit.setText(produktivZeit.getNormalAnd100());
 		jPausenZeit.setText(pausenZeit.getNormalAnd100());
 		jKontoVeraenderung.setText(kontoveraenderung.getNormalAnd100(""+kontoVeraenderungPrefix));
 		jRestZeit.setText(restZeit.getNormalAnd100());
 	}
 	private void recalculate(Zeit zeit)
 	{
 		Config.ZeitAbschnitt[] a = Config.getAbschnitte();
 		gesamtZeit=startZeit.vonNach(zeit);
 		produktivZeit=new Zeit(0);
 		pausenZeit=new Zeit(0);
 		Zeit abschnittEnde=new Zeit(0);
 		Zeit abschnittStart=new Zeit(0);
 		for(int i=0;i<a.length;i++)
 		{
 			abschnittEnde=abschnittStart.add(a[i].dauer);
 			if(gesamtZeit.compareTo(abschnittEnde)<=0)
 			{
 				//Der aktuelle Abschnitt ist bereits vollendet
 				if(a[i].arbeit)
 					produktivZeit=produktivZeit.add(a[i].dauer);
 				else
 					pausenZeit=pausenZeit.add(a[i].dauer);
 			}
 			else 
 			{
 				//Der aktuelle Abschnitt ist nicht vollendet
 				//Wir fuegen die zeit, die wir bereits in diesem abschnitt sind hinzu.
 				if(a[i].arbeit)
 					produktivZeit=produktivZeit.add(gesamtZeit.sub(abschnittStart));
 				else
 					pausenZeit=pausenZeit.add(gesamtZeit.sub(abschnittStart));
 				//und beenden die Schleife
 				break;
 			}
 			abschnittStart=abschnittEnde;
 		}
 		
 		//Unlogisch aber funktioniert...
 		if(produktivZeit.compareTo(Config.minProduktivForPlus)>0)
 		{
 			kontoVeraenderungPrefix='-';
 			kontoveraenderung=Config.minProduktivForPlus.sub(produktivZeit);
 		}
 		else
 		{
 			kontoVeraenderungPrefix='+';
 			kontoveraenderung=produktivZeit.sub(Config.minProduktivForPlus);
 		}
		
		if(startZeit.add(Config.minGesamtZeitForPlus).nach(uhr)) { 
			restZeit = startZeit.add(Config.minGesamtZeitForPlus).sub(uhr);
 		} else {
 			restZeit = new Zeit(0);
 		}
 	}
 }
