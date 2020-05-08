 package pl.jw.currencyexchange.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.InputVerifier;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.text.DefaultFormatter;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import pl.jw.currency.exchange.api.CurrencyData;
 import pl.jw.currencyexchange.Util;
 import pl.jw.currencyexchange.data.IDataSynchronizer;
 
 class PanelBoard extends JPanel {
 
 	private final class ListenerTextField extends InputVerifier {
 		@Override
 		public boolean verify(JComponent input) {
 			if (input instanceof JTextField) {
 
 				log.info("\n");
 				for (int i = 0; i < FIELDS_COUNT; i++) {
 
 					log.info(MessageFormat.format("{0}: {1} {2}", i, listBuyPrice.get(i).getText(), listSellPrice.get(i).getText()));
 				}
 			}
 			return true;
 		}
 	}
 
 	private final class ListenerSynchronization implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			try {
 				synchronize();
 			} catch (Exception ex) {
 				Util.exceptionSupport(PanelBoard.this, ex);
 			}
 		}
 	}
 
 	private static Logger log = Logger.getLogger(PanelBoard.class);
 
 	private static final int FIELD_OFFSET_SELL = 130;
 
 	private static final int FIELD_OFFSET_BUY = 515;
 
 	private static final int FIELDS_COUNT = 11;
 
 	private final Set<CurrencyData> setCurrencies = new LinkedHashSet<>();
 	{
 		setCurrencies.add(new CurrencyData("EURO", "EUR", BigDecimal.ZERO, BigDecimal.ZERO));
 		setCurrencies.add(new CurrencyData("USA", "USD", BigDecimal.ZERO, BigDecimal.ZERO));
 		setCurrencies.add(new CurrencyData("W. BRYTANIA", "GBP", BigDecimal.ZERO, BigDecimal.ZERO));
 		setCurrencies.add(new CurrencyData("KANADA", "CAD", BigDecimal.ZERO, BigDecimal.ZERO));
 		setCurrencies.add(new CurrencyData("AUSTRALIA", "AUD", BigDecimal.ZERO, BigDecimal.ZERO));
 		setCurrencies.add(new CurrencyData("NORWEGIA", "NOK", BigDecimal.ZERO, BigDecimal.ZERO));
 		setCurrencies.add(new CurrencyData("SZWAJCARIA", "CHF", BigDecimal.ZERO, BigDecimal.ZERO));
 		setCurrencies.add(new CurrencyData("CZECHY", "CZK", BigDecimal.ZERO, BigDecimal.ZERO));
 		setCurrencies.add(new CurrencyData("DANIA", "DKK", BigDecimal.ZERO, BigDecimal.ZERO));
 		setCurrencies.add(new CurrencyData("SZWECJA", "SEK", BigDecimal.ZERO, BigDecimal.ZERO));
 		setCurrencies.add(new CurrencyData("EURO - BILON", "EURb", BigDecimal.ZERO, BigDecimal.ZERO));
 	}
 
 	private final JButton jButtonSynchronize = new JButton();
 	private final List<JTextField> listSellPrice = new ArrayList<>();
 	private final List<JTextField> listBuyPrice = new ArrayList<>();
 
 	private IDataSynchronizer dataSynchronizer;
 
 	@Autowired
 	public void setDataSynchronizer(IDataSynchronizer dataSynchronizer) {
 		this.dataSynchronizer = dataSynchronizer;
 	}
 
 	void initialize() throws ParseException {
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		setOpaque(true);
 
 		int buttonHeight = 20;
		Util.setComponentSize(jButtonSynchronize, 180, buttonHeight);
 
 		jButtonSynchronize.setText("Synchronizacja danych");
 		jButtonSynchronize.setBorderPainted(false);
 		jButtonSynchronize.setFocusPainted(false);
 		jButtonSynchronize.setBackground(Color.BLACK);
 		jButtonSynchronize.setContentAreaFilled(false);
 
 		jButtonSynchronize.addActionListener(new ListenerSynchronization());
 
 		// delta from the top
 		Box toolBox = new Box(BoxLayout.X_AXIS);
 		toolBox.add(jButtonSynchronize);
		toolBox.add(Box.createHorizontalGlue());
 
 		add(toolBox);
 		add(Box.createRigidArea(new Dimension(0, 133 - buttonHeight)));
 
 		for (int i = 0; i < FIELDS_COUNT; i++) {
 			JFormattedTextField jTextFiledBuyPrice = createTextField();
 			listBuyPrice.add(jTextFiledBuyPrice);
 
 			JFormattedTextField jTextFiledSellPrice = createTextField();
 			listSellPrice.add(jTextFiledSellPrice);
 
 			Box rowBox = new Box(BoxLayout.X_AXIS);
 			rowBox.add(Box.createRigidArea(new Dimension(FIELD_OFFSET_BUY, 0)));
 			rowBox.add(jTextFiledBuyPrice);
 			rowBox.add(Box.createRigidArea(new Dimension(FIELD_OFFSET_SELL, 0)));
 			rowBox.add(jTextFiledSellPrice);
 
 			add(rowBox);
 			add(Box.createRigidArea(new Dimension(0, 2)));
 
 		}
 
 		refreshToolBoxVisibility(true);
 	}
 
 	private void synchronize() {
 
 		dataSynchronizer.synchronize(setCurrencies);
 
 		int fieldNumber = 0;
 		for (CurrencyData currencyData : setCurrencies) {
 
 			listSellPrice.get(fieldNumber).setText(getCourse(currencyData, currencyData.getSellPrice()));
 			listBuyPrice.get(fieldNumber).setText(getCourse(currencyData, currencyData.getBuyPrice()));
 
 			++fieldNumber;
 		}
 	}
 
 	private String getCourse(CurrencyData currencyData, BigDecimal price) {
 		BigDecimal curse = price.divide(BigDecimal.valueOf(currencyData.getCount()), RoundingMode.HALF_UP);
 
 		return curse.setScale(5).toPlainString().replace('.', ',');
 	}
 
 	private JFormattedTextField createTextField() throws ParseException {
 
 		DefaultFormatter formatter = new DefaultFormatter() {
 
 			private final Pattern pattern = Pattern.compile("[0-9]{0,3}[,]?[0-9]{0,5}");
 
 			@Override
 			public Object stringToValue(String string) throws ParseException {
 
 				if (!pattern.matcher(string).matches()) {
 					throw new ParseException(string, 0);
 				}
 
 				return super.stringToValue(string);
 			}
 
 		};
 		formatter.setAllowsInvalid(false);
 
 		JFormattedTextField jTextFiled = new JFormattedTextField(formatter);
 		Util.setComponentSize(jTextFiled, 170, 45);
 		jTextFiled.setForeground(Color.RED);
 		jTextFiled.setBorder(null);
 		jTextFiled.setOpaque(false);
 		jTextFiled.setFont(new Font("Curier New", Font.BOLD, 36));
 
 		if (log.isDebugEnabled()) {
 			// jTextFiled.setBorder(BorderFactory.createLineBorder(Color.WHITE));
 		}
 
 		jTextFiled.setText("0,0000");
 
 		jTextFiled.setInputVerifier(new ListenerTextField());
 
 		return jTextFiled;
 	}
 
 	void setState(boolean isVisible) {
 
 		refreshToolBoxVisibility(isVisible);
 
 	}
 
 	private void refreshToolBoxVisibility(boolean isActive) {
 
 		// jButtonSynchronize.setFont(jButtonSynchronize.getFont().)
 		// jButtonSynchronize.setEnabled(isActive);
 		jButtonSynchronize.setForeground(isActive ? Color.RED : Color.BLACK);
 
 	}
 
 }
