 package group1.project.synthlab.module.vco;
 
 import group1.project.synthlab.factory.Factory;
 import group1.project.synthlab.module.Module;
 import group1.project.synthlab.port.IPort;
 import group1.project.synthlab.port.IPortObserver;
 import group1.project.synthlab.port.in.IInPort;
 import group1.project.synthlab.port.out.IOutPort;
 import javax.swing.JFrame;
 import com.jsyn.JSyn;
 import com.jsyn.Synthesizer;
 import com.jsyn.scope.AudioScope;
 import com.jsyn.unitgen.LineOut;
 import com.jsyn.unitgen.Multiply;
 import com.jsyn.unitgen.PassThrough;
 import com.jsyn.unitgen.PowerOfTwo;
 import com.jsyn.unitgen.SineOscillator;
 import com.jsyn.unitgen.SquareOscillator;
 import com.jsyn.unitgen.TriangleOscillator;
 
 /**
  * Module VCO
  * 
  * @author Groupe 1
  * 
  */
 public class VCOModule extends Module implements IPortObserver, IVCOModule {
 
 	/** Modulation de frequence connectee ou pas */
 	protected boolean fmConnected;
 	
 	/** Amplitude a l'arret */
 	public static final double amin = 0;
 	/** Amplitude par defaut */
 	public static final double a0 = 0.5;
 
 	
 	/** Frequence min */
 	public static final double fmin = 0;
 	/** Frequence max */
 	public static final double fmax = 6000;
 	/** Frequence de base */
 	protected double f0 = 300;
 	
 	/** Oscillateur generant le signal sinusoidale */
 	protected SineOscillator sineOsc;
 	/** Oscillateur generant le signal carre */
 	protected SquareOscillator squareOsc;
 	/** Oscillateur generant le signal triangulaire */
 	protected TriangleOscillator triangleOsc;
 
 	/** Port d'entree : modulation de frequence */
 	protected IInPort fm;
 	
 	/** Un PassThrough pour envoyer la modulation de frequence vers l'entree des 3 oscillateurs */
 	private PassThrough passThrough;
 	
 	/** Pour de sortie pour le signal sinusoidale */
 	protected IOutPort outSine;
 	/** Pour de sortie pour le signal carre */
 	protected IOutPort outSquare;
 	/** Pour de sortie pour le signal triangulaire */
 	protected IOutPort outTriangle;
 	
 	/** Reglage grossier de la frequence de base : entier de 0 a 9 */
 	protected int coarseAdjustment; // entre 0 et 9 
 	/** Reglage fin de la frequence de base : double entre 0 et 1 */
 	protected double fineAdjustment; // entre 0 et 1
 	
 	/** Etat du module (allume ou eteint) */
 	protected boolean isOn;
 	
 	/**
 	 * Constructeur : initialise le VCO (, port, ...)
 	 */
 	public VCOModule(Factory factory) {
 		super("VCO-" + moduleCount, factory);
 		
 		// Creation des oscillateurs
 		sineOsc = new SineOscillator();
 		squareOsc = new SquareOscillator();
 		triangleOsc = new TriangleOscillator();
 
 		circuit.add(sineOsc);
 		circuit.add(squareOsc);
 		circuit.add(triangleOsc);
 		
 		// Creation du PassThrough : on applique la formule f0 * 2 ^ (5Vfm) au signal en entree fm et on envoie la sortie dans un passThrough
 		// Cette formule garantit egalement que si un signal nul est connecte en entree, la frequence vaudra f0
 		// On doit multiplier Vfm par 5 car JSyn considere des amplitudes entre -1 et 1, et nous considerons des tensions entre -5V et +5V)
 		passThrough = new PassThrough();
 		Multiply multiply5 = new Multiply();
 		multiply5.inputB.set(5); 
 		PowerOfTwo poweroftwo = new PowerOfTwo();
 		multiply5.output.connect(poweroftwo.input);
 		Multiply multiplyf0 = new Multiply();
 		multiplyf0.inputB.set(f0);
 		multiplyf0.inputA.connect(poweroftwo.output);
 		passThrough.input.connect(multiplyf0.output);
 		
 		// Port d'entree : 
 		fm = factory.createInPort("fm", multiply5.inputA, this);
 
 		// Ports de sortie
 		outSine = factory.createOutPort("outsine", sineOsc.output, this);
 		outSquare = factory.createOutPort("outsquare", squareOsc.output, this);
 		outTriangle = factory.createOutPort("outtriangle", triangleOsc.output, this);
 		
 		// Quand on cree notre VCO, il n'a pas de signal en entree, donc la frequence vaut f0
 		fmConnected = false;
 		
 		// On regle les frequences des oscillateurs aux valeurs par defaut
 		sineOsc.frequency.set(f0);
 		squareOsc.frequency.set(f0);
 		triangleOsc.frequency.set(f0);
 		
 		// On regle les amplitudes des oscillateurs au minimum, puisque le VCO n'est pas encore demarre
 		sineOsc.amplitude.set(amin);
 		squareOsc.amplitude.set(amin);
 		triangleOsc.amplitude.set(amin);
 				
 		// Lorsqu'il est cree le VCO est eteint
 		isOn = false;
 	}
 	
 	// Fonction appelee lorsque les reglages sont modifiees sur l'IHM
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#changeFrequency()
 	 */
 	public void changeFrequency(){
 		if(!fmConnected){
 			double newFrequency = (coarseAdjustment + fineAdjustment) * ((fmax - fmin) / 10);
 			f0 = newFrequency;
 			sineOsc.frequency.set(f0);
 			squareOsc.frequency.set(f0);
 			triangleOsc.frequency.set(f0);
 		}
 	}
 	
 	// Fonction qui gere la connexion a l'entree FM, et donc le passage a la modulation de frequence par un signal en entree
 	// La fonction sera donc appelee lorsqu'on connectera un cable au port fm
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#cableConnected()
 	 */
 	public void cableConnected(IPort port) {
 		if(port == fm){ // Si un cable vient d'etre connecte dans l'entree fm, on "active" la modulation de frequence
 			System.out.println("\nConnexion d'un cable dans l'entree fm !");
 			fmConnected = true;
 			passThrough.output.connect(sineOsc.frequency);
 			passThrough.output.connect(squareOsc.frequency);
 			passThrough.output.connect(triangleOsc.frequency);
 		}
 	}
 
 	// Fonction qui gere la deconnexion a l'entree FM
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#cableDisconnected()
 	 */
 	public void cableDisconnected(IPort port) {
 		if(port == fm){ // Si un cable vient d'etre deconnecte de l'entree fm, on remet la frequence des oscillateurs a f0
 			fmConnected = false;
 			passThrough.output.disconnectAll();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IModule#start()
 	 */
 	public void start() {
 		//circuit.start();
 		sineOsc.amplitude.set(a0);
 		squareOsc.amplitude.set(a0);
 		triangleOsc.amplitude.set(a0);
 		isOn = true;
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IModule#stop()
 	 */
 	public void stop() {
 		circuit.stop();
 		sineOsc.amplitude.set(amin);
 		squareOsc.amplitude.set(amin);
 		triangleOsc.amplitude.set(amin);
 		isOn = false;
 	}
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#getf0()
 	 */
 	public double getf0() {
 		return f0;
 	}
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#getSineOsc()
 	 */
 	public SineOscillator getSineOsc() {
 		return sineOsc;
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#getSquareOsc()
 	 */
 	public SquareOscillator getSquareOsc() {
 		return squareOsc;
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#getTriangleOsc()
 	 */
 	public TriangleOscillator getTriangleOsc() {
 		return triangleOsc;
 	}
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#getFmConnected()
 	 */
 	public boolean getFmConnected(){
 		return fmConnected;
 	}
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#getFm()
 	 */
 	public IInPort getFm() {
 		return fm;
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#getOutSine()
 	 */
 	public IOutPort getOutSine() {
 		return outSine;
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#getOutSquare()
 	 */
 
 	public IOutPort getOutSquare() {
 		return outSquare;
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#getOutTriangle()
 	 */
 	public IOutPort getOutTriangle() {
 		return outTriangle;
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#getCoarseAdjustment()
 	 */
 
 	public int getCoarseAdjustment() {
 		return coarseAdjustment;
 
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#setCoarseAdjustment()
 	 */
 	public void setCoarseAdjustment(int coarseadjustment) {
 		this.coarseAdjustment = coarseadjustment;
 		changeFrequency();
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#getFineAdjustment()
 	 */
 	public double getFineAdjustment() {
 		return fineAdjustment;
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IVCOModule#setFineAdjustment()
 	 */
 	public void setFineAdjustment(double fineadjustment) {
 		this.fineAdjustment = fineadjustment;
 		changeFrequency();
 	}
 
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IModule#isStarted()
 	 */
 	public boolean isStarted() {
 		return isOn;
 	}
 	
 	// Tests fonctionnels
 	@SuppressWarnings("deprecation")
 	public static void main(String[] args) throws Throwable{
 		//Factory
 		Factory factory = new Factory();
 		
 		// On cree et demarre le Synthesizer
 		Synthesizer synth = JSyn.createSynthesizer();
 		synth.start();
 		
 		// On cree notre VCO et on ajoute le circuit cree au Synthesizer
 		VCOModule vco = (VCOModule) factory.createVCOModule();
 		synth.add(vco.getCircuit());
 		vco.start();
 		
 		// LineOut remplace ici OutModule
 		LineOut out = new LineOut();
 
 		synth.add(out);
 		out.start();
 		
 		// On connecte la sortie sinusoidale de notre VCO  la sortie
 
 		out.input.connect(vco.getOutSine().getJSynPort());
 		
 		// On cree un VCO dont on va utiliser la sortie carree pour moduler la frequence de notre premier vco
 		VCOModule fm = new VCOModule(factory);
 		synth.add(fm.getCircuit());
 		// la frequence du signal modulant doit etre faible pour que le changement de frequence soit audible
 		fm.squareOsc.frequency.set(0.5); 
 		// Les amplitudes en JSyn varient entre -1 et 1, ce qui correspond dans notre modele a -5V +5V
 		// Une amplitude de 0.2 correspond donc a une amplitude crete  crete de 1V
 
 		// Ainsi, en theorie, quand on passe d'un sommet a un creux, la frequence du signal doit etre divisee par 2 et lorsqu'on passe d'un creux a un sommet la frequence doit etre multipliee par 2.
 		fm.squareOsc.amplitude.set(0.2);
	
		fm.start();
 		
 		// Pour l'affichage des courbes
 		AudioScope scope= new AudioScope( synth );
 		scope.addProbe(vco.sineOsc.output);
 
 		scope.setTriggerMode( AudioScope.TriggerMode.AUTO );
 		scope.getModel().getTriggerModel().getLevelModel().setDoubleValue( 0.0001 );
 		scope.getView().setShowControls( true );
 		scope.start();
 		JFrame frame = new JFrame();
 		frame.add(scope.getView());
 		frame.pack();
 		frame.setVisible(true);
 		
 		// Sans modulation de frequence pendant 6s
 		// On verifie que notre VCO genere bien 3 signaux (carre, sinusoidale et triangulaire)
 		// 2s par signal, le changement de signal doit etre audible
 		
 		// Sinusoidale
 		try
 		{
 			double time = synth.getCurrentTime();
 			synth.sleepUntil( time + 2.0 );
 		} catch( InterruptedException e )
 		{
 			e.printStackTrace();
 		}
 		
 		// Carree
 		out.input.disconnectAll();
 		out.input.connect(vco.getOutSquare().getJSynPort());
 		
 		try
 		{
 			double time = synth.getCurrentTime();
 			synth.sleepUntil( time + 2.0 );
 		} catch( InterruptedException e )
 		{
 			e.printStackTrace();
 		}
 		
 		// Triangulaire
 		out.input.disconnectAll();
 		out.input.connect(vco.getOutTriangle().getJSynPort());
 		
 		try
 		{
 			double time = synth.getCurrentTime();
 
 			synth.sleepUntil( time + 2.0 );
 		} catch( InterruptedException e )
 		{
 			e.printStackTrace();
 		}
 				
 		// Retour en sinusoidale
 		out.input.disconnectAll();
 		out.input.connect(vco.getOutSine().getJSynPort());
 		
 		// Avec modulation de frequence pendant quelques secondes
 		// On connecte la sortie carree de notre VCO fm vers le port d'entree fm de notre VCO vco
 		fm.getOutSquare().getJSynPort().connect(vco.getFm().getJSynPort());
 
 		vco.cableConnected(vco.getFm());
 
 		// On verifie que la frequence est bien divisee par 2 ou multipliee par 2 quand on passe d'une crete a la suivante
 		// Avec un signal modulant carre, la valeur de la frequence du signal modulee va alternee entre 2 valeurs
 		// Il suffit donc d'afficher ces valeurs pour verifier le rapport de 1 a 4.
 		int i = 0;
 		while(i<30) {
 			System.out.println("Frequence = " + vco.sineOsc.frequency.getValue());
 			i++;
 			try {
 				synth.sleepUntil( synth.getCurrentTime() + 0.3 );
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		// On verifie que lorsque le signal modulant est nul, la frequence est bien f0
 		// On peut voir cela dans l'affichage ou en verifiant que la hauteur de la note jouee est la meme qu'avant modulation.
 		fm.squareOsc.amplitude.set(0);
 		
 		try
 		{
 			double time = synth.getCurrentTime();
 			synth.sleepUntil( time + 2.0 );
 		} catch( InterruptedException e )
 		{
 			e.printStackTrace();
 		}
 		
 		// Sans modulation de frequence le reste du temps, avec une frequence reglee un peu plus haut
 		fm.squareOsc.output.disconnectAll();
 		vco.cableDisconnected(vco.getFm());
 		vco.setCoarseAdjustment(1);
 
 		vco.setFineAdjustment(0.2);
 		vco.changeFrequency();
 	}
 
 
 	
 }
