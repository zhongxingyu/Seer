 package edacc.model;
 
 import java.sql.*;
 import java.util.Observable;
 
 /**
  * singleton class handling the database connection.
  * It is possible to get a notification of a change of the connection state by adding an Observer to this class.
  * @author daniel
  */
 public class DatabaseConnector extends Observable {
 
     private static DatabaseConnector instance = null;
     private Connection conn;
 
     private String hostname;
     private int port;
     private String database;
     private String username;
     private String password;
 
     private DatabaseConnector() {
     }
 
     public static DatabaseConnector getInstance() {
         if (instance == null) {
             instance = new DatabaseConnector();
         }
         return instance;
     }
 
     /**
      * Creates a connection to a specified DB.
      * @param hostname the hostname of the DB server.
      * @param port the port of the DB server.
      * @param username the username of the DB user.
      * @param database the name of the database containing the EDACC tables.
      * @param password the password of the DB user.
      * @throws ClassNotFoundException if the driver couldn't be found.
      * @throws SQLException if an error occurs while trying to establish the connection.
      */
     public void connect(String hostname, int port, String username, String database, String password) throws ClassNotFoundException, SQLException {
         if (conn != null) {
             conn.close();
         }
         try {
             this.hostname = hostname;
             this.port = port;
             this.username = username;
             this.password = password;
             this.database = database;
             Class.forName("com.mysql.jdbc.Driver");
             conn = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?user=" + username + "&password=" + password);
         } catch (ClassNotFoundException e) {
             throw e;
         } catch (SQLException e) {
             throw e;
         } finally {
             // inform Observers of changed connection state
             this.setChanged();
             this.notifyObservers();
         }
     }
 
     /**
      * Closes an existing connection. If no connection exists, this method does nothing.
      * @throws SQLException if an error occurs while trying to close the connection.
      */
     public void disconnect() throws SQLException {
         if (conn != null) {
             conn.close();
             this.setChanged();
             this.notifyObservers();
         }
     }
 
     public Connection getConn() throws NoConnectionToDBException {
         try {
             if (!isConnected()) {
                 // inform Obeservers of lost connection
                 this.setChanged();
                 this.notifyObservers();
                 throw new NoConnectionToDBException();
             }
             return conn;
         } catch (SQLException e) {
             conn = null;
             throw new NoConnectionToDBException();
         }
     }
 
     /**
      *
      * @return if a valid connection exists.
      */
     public boolean isConnected() {
         try {
             return conn != null && conn.isValid(10);
         } catch (SQLException ex) {
             return false;
         }
     }
 
     /**
      * Creates the correct DB schema for EDACC using an already established connection.
      */
     public void createDBSchema() throws NoConnectionToDBException, SQLException {
 
         Statement st = getConn().createStatement();
 
         st.addBatch("SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS,"
                 + " UNIQUE_CHECKS=0;");
         st.addBatch("SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS,"
                 + "  FOREIGN_KEY_CHECKS=0;");
         st.addBatch("SET @OLD_SQL_MODE=@@SQL_MODE,"
                 + " SQL_MODE='TRADITIONAL';");
         st.addBatch("CREATE  TABLE IF NOT EXISTS `Solver` (  `idSolver` INT NOT NULL AUTO_INCREMENT ,"
                 + "  `name` VARCHAR(60) NOT NULL COMMENT 'The solvername has to be unique!' ,"
                 + "  `binaryName` VARCHAR(100) NOT NULL ,"
                 + "  `binary` MEDIUMBLOB NOT NULL COMMENT 'Contains the file of the solver' ,"
                 + "  `description` TEXT NULL ,"
                 + "  `md5` VARCHAR(60) NOT NULL ,"
                 + "  `code` MEDIUMBLOB NULL ,"
                 + "  PRIMARY KEY (`idSolver`) ,"
                 + "  UNIQUE INDEX `name` (`name` ASC) ,"
                 + "  UNIQUE INDEX `md5` (`md5` ASC) )ENGINE = InnoDBDEFAULT CHARACTER SET = latin1"
                 + "  COLLATE = latin1_german1_ci;");
         st.addBatch("DROP TABLE IF EXISTS `Parameters` ;");
         st.addBatch("CREATE  TABLE IF NOT EXISTS `Parameters` (  `idParameter` INT NOT NULL AUTO_INCREMENT ,"
                 + "  `name` VARCHAR(60) NULL ,"
                 + "  `prefix` VARCHAR(60) NULL ,"
                 + "  `value` VARCHAR(60) NULL ,"
                 + "  `order` INT NULL ,"
                 + "  `Solver_idSolver` INT NOT NULL ,"
                 + "  PRIMARY KEY (`idParameter`) ,"
                 + "  INDEX `fk_Parameters_Solver` (`Solver_idSolver` ASC) ,"
                 + "  CONSTRAINT `fk_Parameters_Solver`    FOREIGN KEY (`Solver_idSolver` )    REFERENCES `Solver` (`idSolver` )    "
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE)"
                 + "  ENGINE = InnoDB;");
         st.addBatch("DROP TABLE IF EXISTS `instanceClass` ;");
         st.addBatch("CREATE  TABLE IF NOT EXISTS `instanceClass` (  `idinstanceClass` INT NOT NULL AUTO_INCREMENT,"
                 + "  `name` VARCHAR(60) NULL ,"
                 + "  `description` TEXT NULL COMMENT 'the description should contain the source-url of the instances' ,"
                 + "  `source` TINYINT(1) NOT NULL COMMENT 'tells if the class is a source class. ' ,"
                 + "  PRIMARY KEY (`idinstanceClass`) ,"
                 + "  UNIQUE INDEX `name` (`name` ASC) )ENGINE = InnoDB"
                 + "  COMMENT = 'Enables to manage instances trough classes';");
         st.addBatch("DROP TABLE IF EXISTS `Instances` ;");
         st.addBatch("CREATE  TABLE IF NOT EXISTS `Instances` (  `idInstance` INT NOT NULL AUTO_INCREMENT ,"
                 + "  `name` VARCHAR(255) NOT NULL ,"
                 + "  `instance` MEDIUMBLOB NOT NULL COMMENT 'contains ' ,"
                 + "  `md5` VARCHAR(60) NOT NULL ,"
                 + "  `numAtoms` INT NULL ,"
                 + "  `numClauses` INT NULL ,"
                 + "  `ratio` FLOAT NULL ,"
                 + "  `maxClauseLength` INT NULL ,"
                 + "  `instanceClass_idinstanceClass` INT NOT NULL ,"
                 + "  PRIMARY KEY (`idInstance`, `instanceClass_idinstanceClass`) ,"
                 + "  UNIQUE INDEX `name` (`name` ASC) ,"
                 + "  INDEX `fk_Instances_instanceClass1` (`instanceClass_idinstanceClass` ASC) ,"
                 + "  CONSTRAINT `fk_Instances_instanceClass1`    FOREIGN KEY (`instanceClass_idinstanceClass` )    REFERENCES `instanceClass` (`idinstanceClass` )    "
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE)"
                 + "  ENGINE = InnoDB;");
         st.addBatch("DROP TABLE IF EXISTS `gridSettings` ;");
         st.addBatch("CREATE  TABLE IF NOT EXISTS `gridSettings` (  `numNodes` INT NOT NULL ,"
                 + "  `maxRuntime` INT NULL ,"
                 + "  `maxJobsInQueue` INT NULL ,"
                 + "  PRIMARY KEY (`numNodes`) )ENGINE = InnoDB;");
         st.addBatch("DROP TABLE IF EXISTS `Experiment` ;");
         st.addBatch("CREATE  TABLE IF NOT EXISTS `Experiment` (  `idExperiment` INT NOT NULL AUTO_INCREMENT ,"
                 + "  `description` TEXT NULL ,"
                 + "  `date` DATE NOT NULL ,"
                 + "  `numRuns` INT NOT NULL ,"
                 + "  `timeOut` INT NOT NULL COMMENT 'Seconds!\n\n' ,"
                 + "  `autoGeneratedSeeds` TINYINT(1) NOT NULL ,"
                 + "  `name` VARCHAR(255) NULL ,"
                 + "  `memOut` INT NULL COMMENT 'maximum amount of memeory a solver is allowed to use' ,"
                 + "  `maxSeed` BIGINT NULL ,"
                 + "  PRIMARY KEY (`idExperiment`) )ENGINE = InnoDB;");
         st.addBatch("DROP TABLE IF EXISTS `Experiment_has_Instances` ;");
         st.addBatch("CREATE  TABLE IF NOT EXISTS `Experiment_has_Instances` (  `idEI` INT NOT NULL AUTO_INCREMENT ,"
                 + "  `Experiment_idExperiment` INT NOT NULL ,"
                 + "  `Instances_idInstance` INT NOT NULL ,"
                 + "  PRIMARY KEY (`idEI`) ,"
                 + "  INDEX `fk_Experiment_has_Instances_Experiment1` (`Experiment_idExperiment` ASC) ,"
                 + "  INDEX `fk_Experiment_has_Instances_Instances1` (`Instances_idInstance` ASC) ,"
                 + "  CONSTRAINT `fk_Experiment_has_Instances_Experiment1`    FOREIGN KEY (`Experiment_idExperiment` )    "
                 + "  REFERENCES `Experiment` (`idExperiment` )    "
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE,"
                 + "  CONSTRAINT `fk_Experiment_has_Instances_Instances1`    "
                 + "  FOREIGN KEY (`Instances_idInstance` )    "
                 + "  REFERENCES `Instances` (`idInstance` )    "
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE)"
                 + "  ENGINE = InnoDB;");
         st.addBatch("DROP TABLE IF EXISTS `SolverConfig` ;");
         st.addBatch("CREATE  TABLE IF NOT EXISTS `SolverConfig` (  `idSolverConfig` INT NOT NULL AUTO_INCREMENT ,"
                 + "  `Solver_idSolver` INT NOT NULL ,"
                 + "  `Experiment_idExperiment` INT NOT NULL ,"
                 + "  `seed_group` INT NULL DEFAULT 0 ,"
                 + "  PRIMARY KEY (`idSolverConfig`) ,"
                 + "  INDEX `fk_SolverConfig_Solver1` (`Solver_idSolver` ASC) ,"
                 + "  INDEX `fk_SolverConfig_Experiment1` (`Experiment_idExperiment` ASC) ,"
                 + "  CONSTRAINT `fk_SolverConfig_Solver1`    FOREIGN KEY (`Solver_idSolver` )    REFERENCES `Solver` (`idSolver` )    "
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE,"
                 + "  CONSTRAINT `fk_SolverConfig_Experiment1`    FOREIGN KEY (`Experiment_idExperiment` )    REFERENCES `Experiment` (`idExperiment` )    "
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE)"
                 + "  ENGINE = InnoDB;");
         st.addBatch("DROP TABLE IF EXISTS `ExperimentResults` ;");
         st.addBatch("CREATE  TABLE IF NOT EXISTS `ExperimentResults` (  `idJob` INT NOT NULL AUTO_INCREMENT ,"
                 + "  `run` INT NOT NULL ,"
                 + "  `status` INT NOT NULL ,"
                 + "  `seed` INT NOT NULL ,"
                 + "  `resultFileName` VARCHAR(255) NULL ,"
                 + "  `time` FLOAT NULL ,"
                 + "  `statusCode` INT NULL ,"
                 + "  `SolverConfig_idSolverConfig` INT NOT NULL ,"
                 + "  `Experiment_idExperiment` INT NOT NULL ,"
                 + "  `Instances_idInstance` INT NOT NULL ,"
                 + "  `resultFile` MEDIUMBLOB NULL ,"
                 + "  `clientOutput` MEDIUMBLOB NULL ,"
                 + "  PRIMARY KEY (`idJob`) ,"
                 + "  INDEX `fk_ExperimentResults_SolverConfig1` (`SolverConfig_idSolverConfig` ASC) ,"
                 + "  INDEX `fk_ExperimentResults_Experiment1` (`Experiment_idExperiment` ASC) ,"
                 + "  INDEX `fk_ExperimentResults_Instances1` (`Instances_idInstance` ASC) ,"
                 + "  CONSTRAINT `fk_ExperimentResults_SolverConfig1`    FOREIGN KEY (`SolverConfig_idSolverConfig` )    REFERENCES `SolverConfig` (`idSolverConfig` )    "
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE,"
                 + "  CONSTRAINT `fk_ExperimentResults_Experiment1`    "
                 + "  FOREIGN KEY (`Experiment_idExperiment` )    "
                 + "  REFERENCES `Experiment` (`idExperiment` )    "
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE,"
                 + "  CONSTRAINT `fk_ExperimentResults_Instances1`    "
                 + "  FOREIGN KEY (`Instances_idInstance` )    "
                 + "  REFERENCES `Instances` (`idInstance` )    "
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE)"
                 + "  ENGINE = InnoDB;");
         st.addBatch("DROP TABLE IF EXISTS `SolverConfig_has_Parameters` ;");
         st.addBatch("CREATE  TABLE IF NOT EXISTS `SolverConfig_has_Parameters` (  `SolverConfig_idSolverConfig` INT NOT NULL ,"
                 + "  `Parameters_idParameter` INT NOT NULL ,"
                 + "  `value` VARCHAR(45) NULL ,"
                 + "  PRIMARY KEY (`SolverConfig_idSolverConfig`,"
                 + " `Parameters_idParameter`) ,"
                 + "  INDEX `fk_SolverConfig_has_Parameters_SolverConfig1` (`SolverConfig_idSolverConfig` ASC) ,"
                 + "  INDEX `fk_SolverConfig_has_Parameters_Parameters1` (`Parameters_idParameter` ASC) ,"
                 + "  CONSTRAINT `fk_SolverConfig_has_Parameters_SolverConfig1`    "
                 + "  FOREIGN KEY (`SolverConfig_idSolverConfig` )    "
                 + "  REFERENCES `SolverConfig` (`idSolverConfig` )"
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE,"
                 + "CONSTRAINT `fk_SolverConfig_has_Parameters_Parameters1`"
                 + "  FOREIGN KEY (`Parameters_idParameter` )    REFERENCES `Parameters` (`idParameter` )"
                 + "  ON DELETE CASCADE"
                 + "  ON UPDATE CASCADE)"
                 + "  ENGINE = InnoDB;");
         st.addBatch("DROP TABLE IF EXISTS `gridQueue` ;");
        st.addBatch("CREATE  TABLE IF NOT EXISTS `gridQueue` (  `idgridQueue` INT NOT NULL ,"
                 + "  `name` VARCHAR(60) NOT NULL ,"
                 + "  `location` VARCHAR(60) NOT NULL ,"
                 + "  `numNodes` INT NULL COMMENT 'how many nodes are in the queue' ,"
                 + "  `numCPUs` INT NOT NULL COMMENT 'number of cpus per node\n' ,"
                 + "  `walltime` INT NOT NULL COMMENT 'in hours\n' ,"
                 + "  `availNodes` INT NOT NULL COMMENT 'maxmum number of nodes that can be assigned to a single user' ,"
                 + "  `maxJobsQueue` INT NOT NULL COMMENT 'maximum number of nodes in the queue' ,"
                 + "  `genericPBSScript` MEDIUMBLOB NOT NULL ,"
                 + "  `description` TEXT NULL ,"
                 + "  PRIMARY KEY (`idgridQueue`) )"
                 + "  ENGINE = InnoDB ");
         st.addBatch("DROP TABLE IF EXISTS `Experiment_has_gridQueue` ;");
         st.addBatch("CREATE  TABLE IF NOT EXISTS `Experiment_has_gridQueue` (  `Experiment_idExperiment` INT NOT NULL ,"
                 + "  `gridQueue_idgridQueue` INT NOT NULL ,"
                 + "  PRIMARY KEY (`Experiment_idExperiment`,"
                 + " `gridQueue_idgridQueue`) ,"
                 + "  INDEX `fk_Experiment_has_gridQueue_Experiment1` (`Experiment_idExperiment` ASC) ,"
                 + "  INDEX `fk_Experiment_has_gridQueue_gridQueue1` (`gridQueue_idgridQueue` ASC) ,"
                 + "  CONSTRAINT `fk_Experiment_has_gridQueue_Experiment1`    FOREIGN KEY (`Experiment_idExperiment` )    REFERENCES `Experiment` (`idExperiment` )    "
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE,"
                 + "  CONSTRAINT `fk_Experiment_has_gridQueue_gridQueue1`    "
                 + "  FOREIGN KEY (`gridQueue_idgridQueue` )    "
                 + "  REFERENCES `gridQueue` (`idgridQueue` )    "
                 + "  ON DELETE CASCADE    "
                 + "  ON UPDATE CASCADE)"
                 + "  ENGINE = InnoDB;");
 
         st.addBatch("DROP TABLE IF EXISTS `Instances_has_instanceClass`");
        st.addBatch("CREATE  TABLE IF NOT EXISTS `EDACC`.`Instances_has_instanceClass` ("
                 + "  `Instances_idInstance` INT NOT NULL , "
                 + "  `instanceClass_idinstanceClass` INT NOT NULL , "
                 + "  PRIMARY KEY (`Instances_idInstance`, `instanceClass_idinstanceClass`) , "
                 + "  INDEX `fk_Instances_has_instanceClass_Instances1` (`Instances_idInstance` ASC) ,"
                 + "  INDEX `fk_Instances_has_instanceClass_instanceClass1` (`instanceClass_idinstanceClass` ASC) ,"
                 + "  CONSTRAINT `fk_Instances_has_instanceClass_Instances1`"
                 + "  FOREIGN KEY (`Instances_idInstance` )"
                 + "  REFERENCES `EDACC`.`Instances` (`idInstance` )"
                 + "  ON DELETE CASCADE"
                 + "  ON UPDATE CASCADE,"
                 + "  CONSTRAINT `fk_Instances_has_instanceClass_instanceClass1`"
                 + "  FOREIGN KEY (`instanceClass_idinstanceClass` )"
                 + "  REFERENCES `EDACC`.`instanceClass` (`idinstanceClass` )"
                 + "  ON DELETE CASCADE"
                 + "  ON UPDATE CASCADE)"
                 + "  ENGINE = InnoDB;");
 
         st.addBatch("SET SQL_MODE=@OLD_SQL_MODE;");
         st.addBatch("SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;");
         st.addBatch("SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;");
 
 
 
         st.executeBatch();
     }
 
     public String getDatabase() {
         return database;
     }
 
     public String getHostname() {
         return hostname;
     }
 
     public String getPassword() {
         return password;
     }
 
     public int getPort() {
         return port;
     }
 
     public String getUsername() {
         return username;
     }
 }
