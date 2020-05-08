 package bartender;
 
 import java.awt.*;
 import java.util.ArrayList;
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.plaf.basic.BasicBorders;
 
 /**
  * Main window containg all buttons, labels etc.
  *
  * @author Tomas Susanka
  */
 public class MainJFrame extends JFrame
 {
 
 	/**
 	 * @var String[] active languages that can be selected
 	 */
 	private static final String[] activeLanguages = {"en", "cs", "es"};
 	/**
 	 * @var int Window width
 	 */
 	private final static int WINDOW_W = 500;
 	/**
 	 * @var int Window width
 	 */
 	private final static int WINDOW_H = 300;
 	/**
 	 * @var JButton save button
 	 */
 	private JButton payButton;
 	/**
 	 * @var Language language for accesing words
 	 */
 	private Language lang;
 	/**
 	 * @var ArrayList<Product> products offered
 	 */
 	private ArrayList<Product> products;
 
 	/**
 	 * Constructor initialazing components.
 	 */
 	public MainJFrame(Language language)
 	{
 		lang = language;
 		initComponents();
 	}
 
 	/**
 	 * Inicialization of top screen.
 	 */
 	private void initTop()
 	{
 		JLabel productsLabel = new JLabel("<html><b><u>"
 				+ lang.getSentence("choose") + "</u></b></html>");
 		productsLabel.setBorder(new EmptyBorder(new Insets(20, 10, 20, 0)));
 		add(productsLabel, BorderLayout.NORTH);
 	}
 
 	/**
 	 * Inicialization of bottom screen.
 	 */
 	private void initBottom()
 	{
 		JPanel pan = new JPanel(new BorderLayout());
 
		ImageIcon coninsIcon = new ImageIcon(getClass().getResource("imgs/coins.png"));
		payButton = new JButton(lang.getSentence("pay"), coninsIcon);
 		payButton.setPreferredSize(new Dimension(150, 60));
 		payButton.setBorder(new BasicBorders.ButtonBorder(Color.lightGray, Color.lightGray, Color.lightGray, Color.lightGray));
 		/*
 		 * savebtn.addActionListener(new ActionListener() {
 		 *
 		 * @Override
 		 * public void actionPerformed(ActionEvent ae) {
 		 * //performed?
 		 * }
 		 * });
 		 */
 		pan.add(payButton, BorderLayout.EAST);
 
 		add(pan, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Inicialization of center screen.
 	 */
 	private void initCenter()
 	{
 		//center components
 		loadProducts();
 		initProductLabels();
 
 		initLanguages();
 	}
 
 	/**
 	 * Creates language flags.
 	 */
 	private void initLanguages()
 	{
 		JPanel pan = new JPanel(new BorderLayout());
 		for (String lang : activeLanguages) {
 			System.out.println(lang);
 			ImageIcon icon = new ImageIcon(getClass().getResource("imgs/" + lang + ".jpg"));
 			JButton btn = new JButton("", icon);
 			btn.setPreferredSize(new Dimension(60, 26));
 			btn.setBorder(BorderFactory.createEmptyBorder());
 			pan.add(btn, BorderLayout.EAST);
 		}
 
 		add(pan, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Loads products from file to products variable.
 	 */
 	private void loadProducts()
 	{
 		//TODO: loading products from file
 		products = new ArrayList<Product>();
 		products.add(new Product("Vodka", 20));
 		products.add(new Product("Captaing Morgane", 20));
 		products.add(new Product("Sissy mix", 20));
 		products.add(new Product("Whiskey", 20));
 		products.add(new Product("Becherovka", 20));
 	}
 
 	/**
 	 * Initialize product labels.
 	 */
 	private void initProductLabels()
 	{
 		GridLayout layout = new GridLayout(7, 1);
 		setLayout(layout);
 
 		for (Product product : products) {
 			JLabel label = new JLabel(product.getName());
 			label.setBorder(new EmptyBorder(new Insets(0, 30, 0, 0)));
 			add(label);
 		}
 	}
 
 	/**
 	 * Inicialization of components.
 	 */
 	private void initComponents()
 	{
 		initTop();
 
 		initCenter();
 
 		initBottom();
 
 		setSize(WINDOW_W, WINDOW_H);
 		setTitle("Automatic Bartender");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLocationRelativeTo(null);
 	}
 }
