 package org.ebag.uploadtool;
 
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 
import net.MinaClient;

 import org.ebag.net.obj.I;
 import org.ebag.parser.Parser;
 import org.ebag.parser.Problem;
 import org.ebag.parser.TeacherUploadExamRequest;
 
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

 public class UI extends JFrame implements ActionListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1744341509483465101L;
 	private JPanel contentPane;
 	private JTextField path;
 	private JTextField time;
 	JTextPane log;
 	JButton chooseFile;
 	JCheckBox checkBox;
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					UIManager.setLookAndFeel(UIManager
 							.getSystemLookAndFeelClassName());
 					UI frame = new UI();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public UI() {
 		setTitle("\u63D0\u4EA4\u8BD5\u9898");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		JLabel label = new JLabel("\u8BD5\u9898\u8DEF\u5F84");
 		label.setBounds(10, 10, 54, 15);
 		contentPane.add(label);
 
 		path = new JTextField();
 		path.setBounds(10, 33, 311, 21);
 		contentPane.add(path);
 		path.setColumns(10);
 		chooseFile = new JButton("\u9009\u62E9\u8DEF\u5F84");
 		chooseFile.setBounds(331, 32, 93, 23);
 		contentPane.add(chooseFile);
 
 		 checkBox = new JCheckBox(
 				"\u5BB6\u5EAD\u4F5C\u4E1A\uFF08\u4E0D\u52FE\u9009\u9ED8\u8BA4\u4E3A\u8003\u8BD5\uFF09");
 		checkBox.setBounds(6, 81, 269, 23);
 		contentPane.add(checkBox);
 
 		JLabel label_1 = new JLabel("\u8BD5\u5377\u7C7B\u578B");
 		label_1.setBounds(10, 64, 54, 15);
 		contentPane.add(label_1);
 
 		JLabel label_2 = new JLabel("\u8003\u8BD5\u65F6\u95F4");
 		label_2.setBounds(10, 111, 54, 15);
 		contentPane.add(label_2);
 
 		time = new JTextField();
 		time.setHorizontalAlignment(SwingConstants.RIGHT);
 		time.setBounds(10, 136, 143, 21);
 		contentPane.add(time);
 		time.setColumns(10);
 
 		JLabel label_3 = new JLabel("\u5C0F\u65F6");
 		label_3.setBounds(163, 139, 24, 15);
 		contentPane.add(label_3);
 
 		JButton submit = new JButton("\u63D0\u4EA4\u8003\u8BD5");
 		submit.setBounds(46, 181, 93, 55);
 		contentPane.add(submit);
 
 		log = new JTextPane();
 		log.setEditable(false);
 		log.setBounds(194, 105, 230, 147);
 		contentPane.add(log);
 
 		JLabel label_4 = new JLabel("\u5F53\u524D\u72B6\u6001");
 		label_4.setBounds(281, 85, 54, 15);
 		contentPane.add(label_4);
 		submit.addActionListener(this);
 		chooseFile.addActionListener(this);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if (arg0.getSource() == chooseFile) {
 			JFileChooser chooser = new JFileChooser();
 			chooser.showDialog(this, "ѡļ");
 			try {
 				String str = chooser.getSelectedFile().getAbsolutePath();
 				path.setText(str);
 			} finally {
 			}
 			
 		}else{
 			File f=new File(path.getText().trim());
 			double t=Double.parseDouble(time.getText().trim());
 			log.setText("");
 			Parser p=new Parser("teacher1");
 			log.setText("ڳʼת");
 			log.setText("ת");
 			ArrayList<Problem> plist = p.parse(f.getAbsolutePath());
 			TeacherUploadExamRequest request=new TeacherUploadExamRequest();
 			request.setDirName(p.dir);
 			request.setExamName("");//TODO
 			request.setpList(plist);
 			request.setType(checkBox.isSelected()?I.choice.examType_homework:I.choice.examType_exam);
 			request.setUid(1);//TODO
 			request.setTime((long) (1L*t*60*60*1000));
 			log.setText("ϴĿͼƬ");
 			FtpDemo.sendExam(request);
 			FtpDemo.sendFile(f);
 			ExamUploadHandler.handle(request);
 			log.setText("ϴɹ");
 		}
 	}
 }
