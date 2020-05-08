 package pat.game.MS;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.*;
 
 public class Game {
 	private ImageButton faceBu;
 	private boolean gameEnd;
 	private int remaining, flagCount;
 	private JTextField flagTextField;
 	private Thread thread;
 	private JTextField timeTextField;
 
 	private static final Color[] numColor = new Color[] { Color.LIGHT_GRAY,
 			Color.BLUE, new Color(0, 128, 0), Color.RED,
 			new Color(128, 0, 128), Color.BLACK, new Color(195, 33, 72),
 			new Color(64, 224, 208), Color.GRAY };
 
 	private static final int buWidth = 20, buHeight = 20;
 
 	private Field[][] fieldGrid;
 	private int numOfBomb, gameWidth, gameHeight;
 
 	public Game(int x, int y, int numOfBomb, JPanel panel) {
 		panel.setSize(x * buWidth + 5, y * buHeight + 85);
 		panel.setLayout(null);
 		fieldGrid = new Field[x][y];
 		this.gameWidth = x;
 		this.gameHeight = y;
 		this.remaining = x * y;
 		this.flagCount = 0;
 		this.numOfBomb = numOfBomb;
 
 		flagTextField = new JTextField();
 		flagTextField.setBounds(20, 5, buWidth * 3, buHeight);
 		flagTextField.setDisabledTextColor(Color.RED);
 		flagTextField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
 		flagTextField.setEnabled(false);
 		flagTextField.setHorizontalAlignment(JTextField.RIGHT);
 		flagTextField.setText(Integer.toString(numOfBomb - flagCount));
 
 		panel.add(flagTextField);
 
 		faceBu = new ImageButton();
 		faceBu.setMargin(new Insets(0, 0, 0, 0));
 		faceBu.setBounds(buWidth * gameWidth / 2 - 10, 5, buWidth, buHeight);
 		faceBu.setImg(ButtonImage.getImage(ImageEnum.HAHA_IMAGE));
 		faceBuAction(faceBu);
 		panel.add(faceBu);
 
 		timeTextField = new JTextField();
 		timeTextField.setBounds(buWidth * (gameWidth - 4), 5, buWidth * 3,
 				buHeight);
 		timeTextField.setDisabledTextColor(Color.RED);
 		timeTextField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
 		timeTextField.setEnabled(false);
 		timeTextField.setHorizontalAlignment(JTextField.RIGHT);
 		panel.add(timeTextField);
 
 		thread = new Thread(new StopWatch(timeTextField));
 		thread.start();
 
 		for (int j = 0; j < y; j++) { // add buttons
 			for (int i = 0; i < x; i++) {
 				JToggleButton bu = new ImageButton();
 				bu.setMargin(new Insets(0, 0, 0, 0));
 				bu.setBounds(i * buWidth, j * buHeight + 30, buWidth, buHeight);
 				bu.setFocusable(false);
 				fieldGrid[i][j] = new Field(bu, i, j);
 				setMouseAction(fieldGrid[i][j]);
 				panel.add(bu);
 			}
 		}
 		placeBomb();
 		gameEnd = false;
 
 	}
 
 	private void faceBuAction(final ImageButton faceBu) {
 
 		faceBu.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent arg0) {
 				retry();
 
 			}
 		});
 
 		faceBu.addMouseListener(new MouseListener() {
 
 			public void mouseReleased(MouseEvent arg0) {
 				faceBu.setImg(ButtonImage.getImage(ImageEnum.HAHA_IMAGE));
 				faceBu.repaint();
 
 			}
 
 			public void mousePressed(MouseEvent arg0) {
 				faceBu.setImg(ButtonImage.getImage(ImageEnum.OOO_IMAGE));
 				faceBu.repaint();
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			public void mouseClicked(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 	}
 
 	private void setMouseAction(final Field field) {
 		JToggleButton bu = field.getBu();
 
 		bu.addMouseListener(new MouseListener() {
 			boolean cancel;
 			boolean leftClicked, rightClicked;
 
 			public void mouseReleased(MouseEvent arg0) {
 
 				if (arg0.getButton() == MouseEvent.BUTTON1)
 					leftClicked = false;
 				if (arg0.getButton() == MouseEvent.BUTTON3)
 					rightClicked = false;
 
 				if (gameEnd) {
 					arg0.consume();
 					return;
 				} else {
 					faceBu.doMouseEvent(arg0);
 					if (arg0.getButton() == MouseEvent.BUTTON1 && !cancel) {
 						// if left click
 						if (!field.isFlaged() && !field.isOpened()) {
 							click(field, arg0);
 						} else if (field.isFlaged()) {
 							field.getBu().setSelected(false);
 						}
 
 					} else if (arg0.getButton() == MouseEvent.BUTTON3
 							&& !cancel) { // right
 						// click
 						ImageButton bu = (ImageButton) field.getBu();
 						if (!field.isOpened() && !leftClicked) {
 							if (field.isFlaged()) {
 								field.setFlaged(false);
 								bu.setImg(null);
 								--flagCount;
 								flagTextField.setText(Integer
 										.toString(numOfBomb - flagCount));
 							} else {
 								field.setFlaged(true);
 								bu.setImg(ButtonImage
 										.getImage(ImageEnum.FLAG_IMAGE));
 								++flagCount;
 								flagTextField.setText(Integer
 										.toString(numOfBomb - flagCount));
 								checkWin();
 							}
 
 							bu.repaint();
 						} else if (leftClicked) {
 							autoFlagClick(field, arg0);
 							bu.setSelected(!bu.isSelected() && cancel);
 						} else
 							bu.setSelected(true);
 
 					}
 					if (cancel) {
 						field.getBu().setSelected(field.isOpened());
 					}
 				}
 			}
 
 			public void mousePressed(MouseEvent arg0) {
 
 				if (arg0.getButton() == MouseEvent.BUTTON1)
 					leftClicked = true;
 				if (arg0.getButton() == MouseEvent.BUTTON3)
 					rightClicked = true;
 
 				if (!(leftClicked && rightClicked))
 					cancel = false;
 
 				if (gameEnd || field.isOpened()) {
 					arg0.consume();
 					field.getBu().setSelected(!field.getBu().isSelected());
 
 				} else {
 					if (arg0.getButton() != MouseEvent.BUTTON3)
 						mouseClicked(arg0);
 					faceBu.doMouseEvent(arg0);
 				}
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				cancel = true;
 				if (leftClicked || rightClicked)
 					field.getBu().setSelected(field.getBu().isSelected());
 				arg0.consume();
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				arg0.consume();
 			}
 
 			public void mouseClicked(MouseEvent arg0) {
 				arg0.consume();
 			}
 		});
 	}
 
 	private void placeBomb() {
 
 		for (int i = 0; i < numOfBomb; i++) {
 
 			int bombX = (int) (Math.random() * gameWidth);
 			int bombY = (int) (Math.random() * gameHeight);
 
 			while (fieldGrid[bombX][bombY].isBomb()) {
 				bombX = (int) (Math.random() * gameWidth);
 				bombY = (int) (Math.random() * gameHeight);
 			}
 
 			fieldGrid[bombX][bombY].setBomb(true);
 
 			for (int k = -1; k < 2; k++) {
 				for (int j = -1; j < 2; j++) {
 
 					if (!(k == 0 && j == 0)) {
 
 						if (bombX + k > -1 && bombX + k < gameWidth
 								&& bombY + j > -1 && bombY + j < gameHeight)
 							fieldGrid[bombX + k][bombY + j]
 									.setBombValue(fieldGrid[bombX + k][bombY
 											+ j].getBombValue() + 1);
 					}
 				}
 			}
 			// System.out.println("bomb placed at " + bombX + "," + bombY);
 
 		}
 	}
 
 	private int click(Field field, MouseEvent me) {
 		int buX = field.getX();
 		int buY = field.getY();
 		ImageButton bu = (ImageButton) field.getBu();
 		bu.setSelected(true);
 
 		if (fieldGrid[buX][buY].isBomb()) {
 
 			faceBu.setImg(ButtonImage.getImage(ImageEnum.DEAD_IMAGE));
 
 			for (Field[] fieldArray : fieldGrid) {
 				for (Field fieldA : fieldArray) {
 					ImageButton ibu = (ImageButton) fieldA.getBu();
 					if (fieldA.isBomb()) {
 						ibu.setSelected(true);
 						if (fieldA.isFlaged()) {
 							ibu.setImg(
 									ButtonImage.getImage(ImageEnum.FLAG_IMAGE),
 									ButtonImage.getImage(ImageEnum.MINE_IMAGE));
 						} else
 							ibu.setImg(ButtonImage
 									.getImage(ImageEnum.MINE_IMAGE));
 					}
 				}
 			}
 
 			bu.setImg(ButtonImage.getImage(ImageEnum.MINE_IMAGE));
 			bu.setBackground(Color.RED);
 			bu.setSelected(false);
 			thread.interrupt();
 			this.gameEnd = true;
 
 			return 0;
 		}
 
 		--remaining;
		// System.out.println("remaining: " + remaining);

 		checkWin();
 
 		JToggleButton targetBu = fieldGrid[buX][buY].getBu();
 
 		targetBu.setFont(new Font("Ariel", 1, 15));
 
 		targetBu.setForeground(numColor[fieldGrid[buX][buY].getBombValue()]);
 
 		if (fieldGrid[buX][buY].getBombValue() > 0) {
 
 			targetBu.setText(Integer.toString(fieldGrid[buX][buY]
 					.getBombValue()));
 		}
 
 		fieldGrid[buX][buY].setOpened(true);
 
 		if (fieldGrid[buX][buY].getBombValue() == 0) {
 
 			for (int i = -1; i < 2; i++) {
 				for (int j = -1; j < 2; j++) {
 
 					if (!(i == 0 && j == 0)) {
 
 						if (buX + i > -1 && buX + i < gameWidth && buY + j > -1
 								&& buY + j < gameHeight
 								&& !fieldGrid[buX + i][buY + j].isOpened()) {
 
 							click(fieldGrid[buX + i][buY + j], me);
 						}
 
 					}
 
 				}
 			}
 
 		}
 
 		return 0;
 	}
 
 	private void checkWin() {
 
 		if (remaining - flagCount == 0) {
 			// System.out.println("win");
 			faceBu.setImg(ButtonImage.getImage(ImageEnum.WIN_IMAGE));
 			faceBu.repaint();
 			this.gameEnd = true;
 			thread.interrupt();
 		}
 
 	}
 
 	public void retry() {
 
 		this.gameEnd = false;
 
 		for (Field[] fieldArray : fieldGrid) {
 
 			for (Field field : fieldArray) {
 
 				field.setBomb(false);
 				field.setFlaged(false);
 				field.setOpened(false);
 				field.setBombValue(0);
 				ImageButton bu = (ImageButton) field.getBu();
 				bu.setImg(null);
 				bu.setSelected(false);
 				bu.setText("");
 				bu.setBackground(null);
 				flagCount = 0;
 				flagTextField.setText(Integer.toString(numOfBomb - flagCount));
 				remaining = gameHeight * gameWidth;
 				thread.interrupt();
 				thread = new Thread(new StopWatch(timeTextField));
 				thread.start();
 			}
 		}
 		placeBomb();
 	}
 
 	private void autoFlagClick(Field field, MouseEvent me) {
 		if (field.isOpened()) {
 			int bombPoint = field.getBombValue();
 			int fCount = 0;
 			int x = field.getX();
 			int y = field.getY();
 
 			for (int i = -1; i < 2; i++) {
 				for (int j = -1; j < 2; j++) {
 					if (x + i > -1 && y + j > -1 && x + i < gameWidth
 							&& y + j < gameHeight) {
 						if (!(j == 0 && i == 0)) {
 							if (this.fieldGrid[x + i][y + j].isFlaged()) {
 								fCount++;
 							}
 						}
 					}
 				}
 			}
 
 			if (fCount == bombPoint) {
 				for (int i = -1; i < 2; i++) {
 					for (int j = -1; j < 2; j++) {
 						if (x + i > -1 && y + j > -1 && x + i < gameWidth
 								&& y + j < gameHeight) {
 							if (!(j == 0 && i == 0)
									&& !this.fieldGrid[x + i][y + j].isFlaged()) {
 								click(this.fieldGrid[x + i][y + j], me);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 }
