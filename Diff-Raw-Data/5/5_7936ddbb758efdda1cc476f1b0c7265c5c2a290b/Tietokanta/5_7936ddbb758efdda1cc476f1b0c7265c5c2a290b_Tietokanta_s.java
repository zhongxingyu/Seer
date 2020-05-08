 Connection yhteys = null;
 PreparedStatement kysely = null;
 ResultSet tulokset = null;
 
 try {
   //Etsitään mysql-ajuri ja otetaan yhteys tietokantaan
   Class.forName("com.mysql.jdbc.Driver");
  yhteys = DriverManager.getConnection("jdbc:mysql://localhost/tietokannan_nimi?user=kayttaja&password=salasana");
   
   //Suoritetaan sql-kysely. Haetaan täysi-ikäiset Lehtoset tietokannasta
   String sql = "SELECT etunimi, sukunimi, ika FROM kayttajat WHERE ika >= ? and sukunimi = ?";
   kysely = yhteys.prepareStatement(sql);
   kysely.setInteger(1, 18);
   kysely.setString(2, "Lehtonen");
  tulokset = preparedStatement.executeQuery();
 
   //Tulostetaan tietoja löydetyistä käyttäjistä
   while(tulokset.next()) {
     String nimi = tulokset.getString("etunimi") + " " +tulokset.getString("sukunimi");
     int ika = tulokset.getString("ika");
     System.out.println("Käyttäjän "+nimi+" ikä on "+ika);
   }
 
 } catch (Exception e) {
   throw e;
 } finally {
   //Suljetaan lopulta kaikki avatut resurssit
   try { tulokset.close(); } catch (Exception e) {  }
   try { kysely.close(); } catch (Exception e) {  }
   try { yhteys.close(); } catch (Exception e) {  }
 }
