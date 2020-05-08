 /**
  * 
  */
 package ea.ga.gui;
 
 import static ea.gui.GUIKonstante.*;
 import static ea.ga.gui.GAGUIKonstante.*;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SwingWorker;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYShapeRenderer;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 import de.congrace.exp4j.ExpressionBuilder;
 import de.congrace.exp4j.UnknownFunctionException;
 import de.congrace.exp4j.UnparsableExpressionException;
 
 import ea.GASimulator;
 import ea.ga.Jedinka;
 import ea.gui.DijeljenaPloca;
 import ea.gui.GUI;
 import ea.gui.RadioGumbi;
 import ea.gui.TekstualnaVrijednost;
 import ea.util.RealniKrajolik;
 
 /**
  * @author Zlikavac32
  *
  */
 public class GAGUI extends GUI {
 
 	private TekstualnaVrijednost funkcija;
 
 	private TekstualnaVrijednost donjaGranica;
 
 	private RadioGumbi trazi;
 
 	private static TekstualnaVrijednost brojTocaka;
 	
 	private TekstualnaVrijednost gornjaGranica;
 
 	private RadioGumbi reprezentacija;
 
 	private RadioGumbi vrstaMutacijeRealna;
 
 	private TekstualnaVrijednost velicinaPopulacije;
 
 	private TekstualnaVrijednost delta;
 
 	private RadioGumbi vrstaMutacijeBinarna;
 
 	private RadioGumbi vrstaRekombinacijeBinarna;
 
 	private RadioGumbi vrstaPopulacije;
 
 	private RadioGumbi selekcija;
 
 	private TekstualnaVrijednost brojGeneracija;
 
 	private TekstualnaVrijednost sjeme;
 
 	private TekstualnaVrijednost brojDjece;
 
 	private TekstualnaVrijednost brojBitova;
 
 	private TekstualnaVrijednost vjerojatnostMutacije;
 
 	private TekstualnaVrijednost brojJedinkiSkracivanje;
 
 	private RadioGumbi vrstaRekombinacijeRealna;
 	
 	
 	//Ploce
 
 	private Ploca funkcijaPloca = new Ploca();
 
 	private Ploca donjaGranicaPloca = new Ploca();
 
 	private Ploca traziPloca = new Ploca();
 
 	private Ploca brojTocakaPloca = new Ploca();
 
 	private Ploca gornjaGranicaPloca = new Ploca();
 
 	private Ploca reprezentacijaPloca = new Ploca();
 
 	private Ploca vrstaMutacijeRealnaPloca = new Ploca();
 
 	private Ploca velicinaPopulacijePloca = new Ploca();
 
 	private Ploca deltaPloca = new Ploca();
 
 	private Ploca vrstaMutacijeBinarnaPloca = new Ploca();
 
 	private Ploca vrstaRekombinacijeBinarnaPloca = new Ploca();
 
 	private Ploca vrstaPopulacijePloca = new Ploca();
 
 	private Ploca selekcijaPloca = new Ploca();
 
 	private Ploca brojGeneracijaPloca = new Ploca();
 
 	private Ploca sjemePloca = new Ploca();
 
 	private Ploca brojDjecePloca = new Ploca();
 
 	private Ploca brojBitovaPloca = new Ploca();
 
 	private Ploca vjerojatnostMutacijePloca = new Ploca();
 
 	private Ploca brojJedinkiSkracivanjePloca = new Ploca();
 
 	private Ploca vrstaRekombinacijeRealnaPloca = new Ploca();
 
 
 	protected XYSeriesCollection kolekcija;
 
 	protected JFreeChart graf;
 
 	protected XYPlot nacrt;
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8465945028324200176L;
 	
 	private class NacrtajFunkciju extends SwingWorker<Void, Void> {
 		
 		RealniKrajolik krajolik;
 		private XYSeries podatci;
 		
 		NacrtajFunkciju(RealniKrajolik krajolik) {
 			this.krajolik = krajolik;
 		}
 
 		@Override
 		protected Void doInBackground() 
 			throws Exception {
 			podatci = new XYSeries("Funckija");
 
 			int brojElemenata = Integer.parseInt(brojTocaka.vratiVrijednost());
 			if (brojElemenata < 1) { throw new IllegalArgumentException("Broj tocaka mora biti veci od 0"); }
 			double dolje = krajolik.vratiDonjuGranicu()[0];
 			double gore = krajolik.vratiGornjuGranicu()[0];
 			double korak = (gore - dolje) / brojElemenata;
 			for (int i = 0; i <= brojElemenata; i++) {
 				podatci.add(dolje, krajolik.racunajVrijednost(new double[] { dolje }));
				//System.out.println("(" + dolje + ", " + krajolik.racunajVrijednost(new double[] { dolje }) + ")");
 				dolje += korak;			
 			}
 			return null;
 		}
 		
 		@Override
 		protected void done() {
 			kolekcija.removeSeries(0);
 			kolekcija.addSeries(podatci);
 		}
 	}
 	
 	private class NacrtajJedinke extends SwingWorker<Void, Void> {
 		
 		RealniKrajolik krajolik;
 		
 		List<Jedinka<RealniKrajolik>> jedinke;
 
 		private XYSeries podatci;
 		
 		NacrtajJedinke(List<Jedinka<RealniKrajolik>> jedinke, RealniKrajolik krajolik) {
 			this.jedinke = jedinke;
 			this.krajolik = krajolik;
 		}
 
 		@Override
 		public Void doInBackground()
 			throws Exception {
 			podatci = new XYSeries("podatci");
 			int limit = jedinke.size();
 			for (int i = 0; i < limit; i++) {
 				double x = (Double) jedinke.get(i).vratiVrijednost();
 				podatci.add(x, krajolik.racunajVrijednost(new double[] { x }));
 			}
 			return null;
 		}
 		
 		@Override
 		protected void done() {
 			XYSeriesCollection kolekcijaJedinki = new XYSeriesCollection(podatci);
 			nacrt.setDataset(1, kolekcijaJedinki);
 		}
 		
 	}
 
 	public GAGUI(String title) {
 		super(title);
 		// TODO Auto-generated constructor stub
 	}
 	
 	
 	protected JPanel stvoriKontroleKontejner() {
 		
 		JPanel kontroleKontejner = new JPanel();
 	
 		kontroleKontejner.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
 		kontroleKontejner.setLayout(new BoxLayout(kontroleKontejner, BoxLayout.Y_AXIS));
 		
 		inicijalizirajElementeKontrola();
 		
 		DijeljenaPloca[] elementi = new DijeljenaPloca[] {
 			funkcija, sjeme, donjaGranica, gornjaGranica, brojTocaka,
 			trazi, velicinaPopulacije, reprezentacija, brojBitova, vrstaMutacijeRealna,
 			delta, vrstaRekombinacijeRealna, vrstaMutacijeBinarna, vjerojatnostMutacije, 
 			vrstaRekombinacijeBinarna,
 			vrstaPopulacije, brojDjece, selekcija, brojJedinkiSkracivanje, brojGeneracija
 		};
 		
 		Ploca[] ploce = new Ploca[] {
 			funkcijaPloca, sjemePloca, donjaGranicaPloca, gornjaGranicaPloca, brojTocakaPloca,
 			traziPloca, velicinaPopulacijePloca, reprezentacijaPloca, brojBitovaPloca, vrstaMutacijeRealnaPloca,
 			deltaPloca, vrstaRekombinacijeRealnaPloca, vrstaMutacijeBinarnaPloca, vjerojatnostMutacijePloca, 
 			vrstaRekombinacijeBinarnaPloca,
 			vrstaPopulacijePloca, brojDjecePloca, selekcijaPloca, brojJedinkiSkracivanjePloca, brojGeneracijaPloca
 		};
 		
 		for (int i = 0; i < elementi.length; i++) {
 			JPanel ploca = new JPanel();
 			ploca.setLayout(new BoxLayout(ploca, BoxLayout.Y_AXIS));
 			ploca.add(elementi[i]);
 			ploca.add(Box.createRigidArea(new Dimension(0, 10)));
 			ploce[i].ploca = ploca;
 			kontroleKontejner.add(ploca);
 		}
 		return kontroleKontejner;
 	}
 	
 	protected void inicijalizirajElementeKontrola() {
 		
 		ActionListener sakrijOtkrij = new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				
 				
 				boolean zastavica = false;
 				if (reprezentacija.vratiOdabrani().getActionCommand().equals(GENOTIP)) { zastavica = true; }
 				vrstaMutacijeRealna.setEnabled(!zastavica && true);
 				vrstaRekombinacijeRealna.setEnabled(!zastavica && true);
 				delta.setEnabled(!zastavica && true);
 				vrstaMutacijeBinarna.setEnabled(zastavica && true);
 				vrstaRekombinacijeBinarna.setEnabled(zastavica && true);
 				brojBitova.setEnabled(zastavica && true);
 
 				boolean pomoc = reprezentacija.vratiOdabrani().getActionCommand().equals(FENOTIP);
 				zastavica = (!vrstaMutacijeRealna.vratiOdabrani().getActionCommand().equals(NISTA) && pomoc)
 					|| (!vrstaMutacijeBinarna.vratiOdabrani().getActionCommand().equals(NISTA) && reprezentacija.vratiOdabrani().getActionCommand().equals(GENOTIP));
 				delta.setEnabled(zastavica && pomoc);
 				vjerojatnostMutacije.setEnabled(zastavica);
 				
 //				zastavica = vrstaPopulacije.vratiOdabrani().getActionCommand().equals(PREKLAPAJUCA);
 //				selekcija.setEnabled(zastavica);
 //				brojDjece.setEnabled(zastavica);
 				
 				zastavica = selekcija.vratiOdabrani().getActionCommand().equals(SKRACIVANJE);
 				brojJedinkiSkracivanje.setEnabled(zastavica);
 				
 			}
 		};
 		
 		brojJedinkiSkracivanje = new TekstualnaVrijednost("Broj jedinki za skraćivanje", "5");
 		brojJedinkiSkracivanje.setEnabled(false);
 		vjerojatnostMutacije = new TekstualnaVrijednost("Vjerojatnost mutacije", "0.05");
 		vjerojatnostMutacije.setEnabled(false);
 		brojBitova = new TekstualnaVrijednost("Broj bitova", "8");
 		brojBitova.setEnabled(false);
 		brojDjece = new TekstualnaVrijednost("Broj djece", "20");
 		brojGeneracija = new TekstualnaVrijednost("Broj generacija", "15");
 		selekcija = new RadioGumbi("Selekcija", new Object[] {
 			UNIFORMNA, PROPORCIONALNA, DVO_TURNIRSKA, SKRACIVANJE
 		}, 0, new ActionListener[] {
 			sakrijOtkrij, sakrijOtkrij, sakrijOtkrij, sakrijOtkrij
 		});
 		vrstaPopulacije = new RadioGumbi("Vrsta populacije", new Object[] {
 			NEPREKLAPAJUCA, PREKLAPAJUCA	
 		}, 0, new ActionListener[] {
 			sakrijOtkrij, sakrijOtkrij
 		});
 		vrstaRekombinacijeRealna = new RadioGumbi("Vrsta rekombinacije fenotip", new Object[] {
 			NISTA, ARITMETICKA_SREDINA, TEZINSKA_SREDINA, ALFTA_INTERVAL
 		}, 0);
 		vrstaRekombinacijeBinarna = new RadioGumbi("Vrsta rekombinacije genotip", new Object[] {
 			NISTA, JEDNA_TOCKA, DVIJE_TOCKE
 		}, 0);
 		vrstaRekombinacijeBinarna.setEnabled(false);
 		vrstaMutacijeBinarna = new RadioGumbi("Vrsta mutacije genotip", new Object[] {
 			NISTA, OKRET_BITA
 		}, 0, new ActionListener[] {
 			sakrijOtkrij, sakrijOtkrij
 		});
 		vrstaMutacijeBinarna.setEnabled(false);
 		delta = new TekstualnaVrijednost("Delta", "0.1");
 		delta.setEnabled(false);
 		vrstaMutacijeRealna = new RadioGumbi("Vrsta mutacije fenotip", new Object[] {
 			NISTA, LINEARNA, GAUSS
 		}, 0, new ActionListener[] {
 			sakrijOtkrij, sakrijOtkrij, sakrijOtkrij
 		});
 		reprezentacija = new RadioGumbi("Reprezentacija", new Object[] {
 			GENOTIP, FENOTIP	
 		}, 1, new ActionListener[] {
 			sakrijOtkrij, sakrijOtkrij
 		});
 		velicinaPopulacije = new TekstualnaVrijednost("Veličina populacije", "20");
 		trazi = new RadioGumbi("Traži", new Object[] {
 			MINIMUM, MAKSIMUM	
 		}, 0);
 		brojTocaka = new TekstualnaVrijednost("Broj točaka", "1000");
 		gornjaGranica = new TekstualnaVrijednost("Do", "5");
 		donjaGranica = new TekstualnaVrijednost("Od", "-5");
 		sjeme = new TekstualnaVrijednost("Sjeme", "123456");
 		funkcija = new TekstualnaVrijednost("Funkcija", GAGUIKonstante.FUNKCIJA);
 	}
 
 	protected ChartPanel stvoriGraf() {
 		XYSeries podatci = new XYSeries("");
 		kolekcija = new XYSeriesCollection();
 		kolekcija.addSeries(podatci);
 		graf = ChartFactory.createXYLineChart(GAGUIKonstante.FUNKCIJA, "X", "Y", kolekcija, PlotOrientation.VERTICAL, false, false, false);
 		nacrt = graf.getXYPlot();
 		nacrt.setRenderer(1, new XYShapeRenderer());
 		
 		ChartPanel grafPloca = new ChartPanel(graf);
 		
 		return grafPloca;
 	}
 
 	public void nacrtajFunkciju(RealniKrajolik krajolik) {
 		new NacrtajFunkciju(krajolik).execute();
 	}
 	
 	protected void pokreniSimulaciju(JButton gumb) 
 		throws UnknownFunctionException, UnparsableExpressionException {
 		
 		boolean genotip = reprezentacija.vratiOdabrani().getActionCommand().equals(GENOTIP);
 		
 		GASimulator simulator = new GASimulator();
 		try {
 			simulator.koristeciSjeme(Long.parseLong(sjeme.vratiVrijednost()));
 		} catch (NumberFormatException e) { 
 			zapisiUZapisnik("Sjeme mora biti cijeli broj");
 			return ;
 		} 
 		
 		try {
 			simulator.unutarGranica(
 				Double.parseDouble(donjaGranica.vratiVrijednost()),
 				Double.parseDouble(gornjaGranica.vratiVrijednost())
 			);
 		} catch (NumberFormatException e) { 
 			zapisiUZapisnik("Granice moraju biti realan broj");
 			return ;
 		}
 		
 		simulator.traziEkstrem(
 			trazi.vratiOdabrani().getActionCommand().equals(MINIMUM) ? GASimulator.MINIMUM
 				: GASimulator.MAKSIMUM
 		);
 		try {
 			simulator.saVelicinomPopulacije(Integer.parseInt(velicinaPopulacije.vratiVrijednost()));
 		} catch (NumberFormatException e) { 
 			zapisiUZapisnik("Velicina populacije mora biti cijeli broj");
 			return ;
 		}
 		simulator.koristeciReprezentaciju(
 			genotip ? GASimulator.GENOTIP : GASimulator.FENTOTIP
 		);
 		
 		if (genotip) {
 			try {
 				simulator.koristeciBrojBitova(Integer.parseInt(brojBitova.vratiVrijednost()));
 			} catch (NumberFormatException e) { 
 				zapisiUZapisnik("Broj bitova mora biti cijeli broj");
 				return ;
 			}
 			if (vrstaMutacijeBinarna.vratiOdabrani().equals(OKRET_BITA)) {
 				simulator.koristeciMutaciju(GASimulator.OKRET_BITA_MUTACIJA);
 				try {
 					simulator.uzVjerojatnostMutacije(
 						Double.parseDouble(vjerojatnostMutacije.vratiVrijednost())
 					);
 				} catch (NumberFormatException e) { 
 					zapisiUZapisnik("Vjerojatnost mutacije mora biti realan broj");
 					return ;
 				}
 			}
 			
 			String vrstaRekombinacijeBinarnaString = vrstaRekombinacijeBinarna.vratiOdabrani().getActionCommand();
 			if (vrstaRekombinacijeBinarnaString.equals(JEDNA_TOCKA)) {
 				simulator.koristeciRekombinaciju(GASimulator.JEDAN_CVOR_REKOMBINACIJA);
 			} else if (vrstaRekombinacijeBinarnaString.equals(DVIJE_TOCKE)) {
 				simulator.koristeciRekombinaciju(GASimulator.DVA_CVORA_REKOMBINACIJA);
 			}
 			
 		} else {
 			String vrstaMutacijeRealnaString = vrstaMutacijeRealna.vratiOdabrani().getActionCommand();
 			if (vrstaMutacijeRealnaString.equals(GAUSS)) {
 				simulator.koristeciMutaciju(GASimulator.GAUSS_MUTACIJA);
 			} else if (vrstaMutacijeRealnaString.equals(LINEARNA)) {
 				simulator.koristeciMutaciju(GASimulator.DELTA_MUTACIJA);
 			}
 			if (!vrstaMutacijeRealnaString.equals(NISTA)) {
 				try {
 					simulator.uzMutacijskuDeltu(
 						Double.parseDouble(delta.vratiVrijednost())
 					);
 				} catch (NumberFormatException e) { 
 					zapisiUZapisnik("Mutacijska delta mora biti realan broj");
 					return ;
 				}
 				try {
 					simulator.uzVjerojatnostMutacije(
 						Double.parseDouble(vjerojatnostMutacije.vratiVrijednost())
 					);
 				} catch (NumberFormatException e) { 
 					zapisiUZapisnik("Vjerojatnost mutacije mora biti realan broj");
 					return ;
 				}
 			}
 			
 			String vrstaRekombinacijeRealnaString = vrstaRekombinacijeRealna.vratiOdabrani().getActionCommand();
 			if (vrstaRekombinacijeRealnaString.equals(ARITMETICKA_SREDINA)) {
 				simulator.koristeciRekombinaciju(GASimulator.ARITMETICKA_SREDINA_REKOMBINACIJA);
 			} else if (vrstaRekombinacijeRealnaString.equals(TEZINSKA_SREDINA)) {
 				simulator.koristeciRekombinaciju(GASimulator.TEZINSKA_SREDINA_REKOMBINACIJA);
 			} else if (vrstaRekombinacijeRealnaString.equals(ALFTA_INTERVAL)) {
 				simulator.koristeciRekombinaciju(GASimulator.ALFTA_INTERVAL_REKOMBINACIJA);
 			}
 		}
 		
 		if (vrstaPopulacije.vratiOdabrani().getActionCommand().equals(NEPREKLAPAJUCA)) {
 			simulator.koristeciPopulaciju(GASimulator.NEPREKLAPAJUCA);
 		} else if (vrstaPopulacije.vratiOdabrani().getActionCommand().equals(PREKLAPAJUCA)) {
 			simulator.koristeciPopulaciju(GASimulator.PREKLAPAJUCA);
 		}
 		try {
 			simulator.saBrojemDjece(Integer.parseInt(brojDjece.vratiVrijednost()));
 		} catch (NumberFormatException e) { 
 			zapisiUZapisnik("Broj djece mora biti cijeli broj");
 			return ;
 		}
 		String selekcija = this.selekcija.vratiOdabrani().getActionCommand();
 		if (selekcija.equals(UNIFORMNA)) {
 			simulator.koristeciSelektor(GASimulator.UNIFORMNA);
 		} else if (selekcija.equals(PROPORCIONALNA)) {
 			simulator.koristeciSelektor(GASimulator.PROPORCIONALNA);
 		} else if (selekcija.equals(DVO_TURNIRSKA)) {
 			simulator.koristeciSelektor(GASimulator.DVO_TURNIRSKA);
 		} else if (selekcija.equals(SKRACIVANJE)) {
 			simulator.koristeciSelektor(GASimulator.SKRACIVANJE);
 			try {
 				simulator.uzBrojJedinkiZaSkracivanje(Integer.parseInt(brojJedinkiSkracivanje.vratiVrijednost()));
 			} catch (NumberFormatException e) { 
 				zapisiUZapisnik("Broj jedinki za skracivanje mora biti cijeli broj");
 				return ;
 			}
 		}
 		
 		String funkcijaString = funkcija.vratiVrijednost().toLowerCase();
 		graf.setTitle(funkcijaString);
 		
 		simulator.koristeciFunkciju(new ExpressionBuilder(funkcijaString).withVariableNames("x").build());
 		
 		try {
 			simulator.uzBrojGeneracija(Integer.parseInt(brojGeneracija.vratiVrijednost()));
 		} catch (NumberFormatException e) { 
 			zapisiUZapisnik("Broj generacija mora biti cijeli broj");
 			return ;
 		}
 		
 		simulator.postaviGUI(this);
 		
 		simulator.addPropertyChangeListener(new ZaustaviSimulaciju(gumb));
 		simulator.execute();
 		
 		this.simulator = simulator;
 		
 		
 		gumb.setText(ZAUSTAVI);
 				
 	}
 
 	public void iscrtajPopulaciju(List<Jedinka<RealniKrajolik>> jedinke, RealniKrajolik krajolik) {
 		new NacrtajJedinke(jedinke, krajolik).execute();
 	}
 }
