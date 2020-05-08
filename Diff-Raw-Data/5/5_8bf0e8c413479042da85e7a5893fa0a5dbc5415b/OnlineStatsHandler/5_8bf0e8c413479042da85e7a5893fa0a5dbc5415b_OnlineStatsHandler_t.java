 package database;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 /**
  *
  * @author Team 12
  * @Description Esta clase permite realizr la conexion a una base de datos
  * albergada en un servidor externo
  */
 public class OnlineStatsHandler extends JFrame implements Connectible {
 
     private static final long serialVersionUID = -9029903859955635542L;
     private Connection conn;
     private Statement statement;
     private ResultSet rs;
     private PreparedStatement ps;
     private static OnlineStatsHandler instance = null;
     private JPanel topPanel;
     private JTable table;
     private JScrollPane scrollPane;
 
     @Override
     public void conectar() {
 
         try {
             // Cargar por refletividad el driver de JDBC MySQL
             Class.forName("com.mysql.jdbc.Driver");
             // Ahora indicamos la URL para conectarse a la BD de MySQL
             String url = "jdbc:mysql://sql2.freesqldatabase.com/sql25266";
             String userid = "sql25266";
             String password = "mX5%gY7!";
 
             conn = DriverManager.getConnection(url, userid, password);
         } catch (ClassNotFoundException e1) {
             JOptionPane.showMessageDialog(this, "No se ha podido establecer la conexi�n con el servidor de Powders", "Error de conexi�n", 0);
         } catch (SQLException e2) {
             JOptionPane.showMessageDialog(this, "No se ha podido establecer la conexi�n con el servidor de Powders", "Error de conexi�n", 0);
         }
     }
 
     @Override
     public void desconectar() {
         try {
             // cerramos todo
             conn.close();
         } catch (SQLException e) {
             JOptionPane.showMessageDialog(this, "Error de comunicaci�n con el servidor.", "Error de conexi�n", 2);
         }
     }
 
     public static OnlineStatsHandler getInstance() {
         if (instance == null) {
             instance = new OnlineStatsHandler();
         }
         return instance;
     }
 
     public void borrarPartidas() throws SQLException {
         conectar();
         String query = "DELETE * FROM JUGADOR;";
 
     }
 
     public void listarEstadisticasJugares() {
         conectar();
         // Add the table to a scrolling pane
 
         try {
             // Ahora utilizamos las sentencias de BD
             String query = "SELECT * FROM JUGADOR;";
             statement = conn.createStatement();
             rs = statement.executeQuery(query);
             // y recorremos lo obtenido
 
             while (rs.next()) {
 
                 String nick = "" + rs.getString("NICK");
                 System.out.println("NICK: " + nick);
 
                 String name = "" + rs.getString("NOMBRE");
                 System.out.println("NOMBRE: " + name);
 
                 String pass = "" + rs.getString("PASSWORD");
                 System.out.println("PASSWORD: " + pass);
 
                 System.out.println("--");
             }
 
             rs.close();
             statement.close();
         } catch (SQLException e) {
             e.printStackTrace();
 
         }
         desconectar();
     }
 
     //
     public void agregarPerfil(Player jugador) {
 
         conectar();
         String pass = jugador.getPassword();
         String nick = jugador.getNick();
         String nombre = jugador.getNombre();
         try {
             ps = conn.prepareStatement("INSERT INTO JUGADOR VALUES (?, ?, ?)");
             ps.setString(1, nick);
             ps.setString(2, pass);
             ps.setString(3, nombre);
             ps.executeUpdate();
             ps.close();
             desconectar();
         } catch (SQLException e) {
             e.printStackTrace();
 
         }
     }
 
     public void agregarPartida(Game partida) {
         conectar();
         String nick = partida.getNick();
         Timestamp fecha_hora = partida.getFecha_hora();
         String fecha_horaS = fecha_hora.toString();
         String puntuacion = partida.getPuntuacion();
         String nivel = partida.getNivel();
 
         String disparos_ac = partida.getDisparos_ac();
         String disparos_tot = partida.getDisparos_tot();
         String muertes = partida.getMuertes();
         double tiempo = partida.getTiempo();
 
         try {
             ps = conn.prepareStatement("INSERT INTO PARTIDA VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
             ps.setString(1, nick);
             ps.setString(2, fecha_horaS);
             ps.setString(3, puntuacion);
             ps.setString(4, nivel);
 
             ps.setString(6, disparos_ac);
             ps.setString(7, disparos_tot);
             ps.setString(8, muertes);
             ps.setDouble(9, tiempo);
             ps.executeUpdate();
             ps.close();
             desconectar();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public void listarEstadisticasPartidas() {
         conectar();
 
         // Set the frame characteristics
         setTitle("Partidas en el servidor Powders");
         setSize(1300, 700);
         setResizable(false);
         setLocationRelativeTo(null);
         setBackground(Color.blue);
 
         // Create a panel to hold all other components
         topPanel = new JPanel();
         topPanel.setLayout(new BorderLayout());
         getContentPane().add(topPanel);
 
         // Create columns names
         String columnNames[] = {"RANKING", "COD_U", "FECHA_HORA", "PUNTUACION", "NIVEL", "DISPAROS ACERTADOS", "DISPAROS TOTALES", "MUERTOS", "TIEMPO"};
         ArrayList a = new ArrayList();
 
         try {
             // Ahora utilizamos la sentencias de BD
             String query = "SELECT * FROM PARTIDA ORDER BY PUNTUACION DESC,MUERTES DESC;";
             statement = conn.createStatement();
             rs = statement.executeQuery(query);
 
 
             // y recorremos lo obtenido
             while (rs.next()) {
                 String nick = "" + rs.getString("NICK");
 
                 String fecha_hora = "" + rs.getString("FECHA_HORA");
 
                 String punt = "" + rs.getString("PUNTUACION");
 
                 String nivel = "" + rs.getString("NIVEL");
 
                 String disparos_ac = "" + rs.getString("DISPAROS_AC");
 
                 String disparos_tot = "" + rs.getString("DISPAROS_TOT");
 
                 String muertos = "" + rs.getString("MUERTES");
 
                 String tiempod = "" + rs.getString("TIEMPO");
 
                 double numero = Double.valueOf(tiempod).doubleValue();
 
 
             //   Game p = new Game(nick, punt, nivel, disparos_ac, disparos_tot, muertos, numero, fecha_hora);
 
              //  a.add(p);
 
 
             }
 
             for (int s = 1; s - 1 < a.size(); s++) {
 
                 String pos = "" + s;
 
 
             }
 
             String dataValues[][] = new String[a.size()][10];
 
             for (int x = 0; x < a.size(); x++) {
                 Game parTempx = (Game) a.get(x);
                 String nick = parTempx.getNick();
                 String b = parTempx.getFecha_h();
                 String c = parTempx.getPuntuacion();
                 String d = parTempx.getNivel();
 
                 String h = parTempx.getDisparos_ac();
                 String tot = parTempx.getDisparos_tot();
                 String dm = parTempx.getMuertes();
 
 
 
 
                 dataValues[x][0] = nick;
 
                 dataValues[x][1] = b;
 
                 dataValues[x][2] = c;
 
                 dataValues[x][3] = d;
 
                 dataValues[x][4] = h;
 
                 dataValues[x][5] = dm;
 
                 dataValues[x][6] = tot;
 
                 String tiempo = "" + parTempx.getTiempo();
 
                 dataValues[x][7] = tiempo;
 
 
             }
 
             table = new JTable(dataValues, columnNames);
             scrollPane = new JScrollPane(table);
             topPanel.add(scrollPane, BorderLayout.CENTER);
             this.setVisible(true);
             rs.close();
             statement.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         desconectar();
     }
 
     public static void main(String[] args) throws SQLException {
 
         //Partida p = new Game ("S","ss","dd","22","344","23","232",23);
         //getInstance().agregarPartida(p);
         getInstance().listarEstadisticasJugares();
         getInstance().listarEstadisticasPartidas();
         //getInstance().borrarPartidas();
 
     }
 }
