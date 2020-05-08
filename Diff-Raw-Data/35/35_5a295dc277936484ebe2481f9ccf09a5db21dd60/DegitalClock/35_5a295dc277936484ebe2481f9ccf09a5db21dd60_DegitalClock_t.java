 package sec01.ex04;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Calendar;
 
 public class DegitalClock extends Frame implements Runnable{
 	private static final long serialVersionUID = 1L;
 	private static int h;           //時を入れる変数を宣言
 	private static int m;           //分を入れる変数を宣言
 	private static int s;           //秒を入れる変数を宣言
 	private static int height = 200;
 	private static int width = 400;
 	private static int x;
 	private static int y;
	private static final int BLANK_SPACE_SIZE = 110;
 	
 	private Graphics buffer;        //オフスクリーンバッファのグラフィックコンテキスト
 	private Image back = null;
     
 	private Thread timerThread;
 	
 	private MenuItem propertyItem;
 	
 	private PropertyDialog pDialog = new PropertyDialog();
 	
     /*
      * コンストラクタ
      */
     public DegitalClock() {   	
     	timerThread = new Thread(this);
     	timerThread.start();
     	
     	setTitle("Degital clock");
         setLayout(new FlowLayout());
         
                
         //メニューバーの生成
         MenuBar menuBar = new MenuBar();
     	
     	//メニューアイテムの生成
         Menu menuProperty = new Menu("Property");
         propertyItem = new MenuItem("Open");
         menuProperty.add(propertyItem);
         menuBar.add(menuProperty);
         
         //プロパティを押された時の処理
         propertyItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             	pDialog.load();
             }
         });
         
         //×を押されたときの処理
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e)  {
             	PropertyData.setData(e.getWindow().getX(), e.getWindow().getY());
             	System.exit(0);
             }
         });
 
         //メニューバーをフレームへ追加
         setMenuBar(menuBar);
         setBounds(PropertyData.x, PropertyData.y, width, height);
     }
     
 	public void run() {
 	    while(true) {
 	          h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //時を代入
 	          m = Calendar.getInstance().get(Calendar.MINUTE);      //分を代入
 	          s = Calendar.getInstance().get(Calendar.SECOND);      //秒を代入
 	          repaint();
 	          pause(100);
 	    }
 	}
 	
 	public void update(Graphics g) {
 		paint(g);
 	}
 	
 	public void paint(Graphics g) {    
 		back = this.createImage(width, height);  
 		buffer = back.getGraphics();
 		
 		buffer.setFont(new Font(PropertyData.font, PropertyData.fontStyle, PropertyData.fontSize));
 		buffer.setColor(PropertyData.parseColor(PropertyData.color));
 		setBackground(PropertyData.parseColor(PropertyData.backgroundColor));
 	    
 		drawTimeCenter();
 		setSize(width, height);
 		g.drawImage(back, 0, 0, this);      //バッファを画面に描画
 	}
 	
     private void drawTimeCenter() {
     	FontMetrics fo = buffer.getFontMetrics();
         int strWidth = fo.stringWidth(h+":"+m+":"+s);
         int strHeight = fo.getAscent() + fo.getDescent();
         
         width = strWidth + BLANK_SPACE_SIZE;
         height = strHeight + BLANK_SPACE_SIZE;
         
         buffer.drawString(h+":"+m+":"+s, (width - strWidth) / 2, (height - strHeight) / 2 + fo.getMaxAscent());
     }
 
 	private void pause(int time) {
         try {
         	Thread.sleep(time);
         } catch (InterruptedException e) {}
     }
     
 	/**
 	 * メイン関数
 	 */
 	public static void main(String args[]) {	
 		DegitalClock clock = new DegitalClock();
 		clock.setResizable(false);
 	    clock.setVisible(true);
 	}
 }
