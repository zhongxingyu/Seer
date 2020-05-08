 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package db;
 
 /**
  * Contains stored procedure names, parameters and columns names
  * @author rdinarte
  */
 public class Constants {
 
    public static final String ConfigurationFilePath = "/etc/hpca/hpca.conf";
    public static final String ConfigNodes = "Nodes";
    public static final String ConfigDefaultNodes = "DefaultNodes";
    public static final String ConfigDataPath = "UsersDataPath";

     // Data info messages
     // -------------------------------------------------------------------------
     public static final String ExperimentLoadError = "Problemas de conexión en el sistema para"
             + "obtener los experimentos.";
     public static final String SaveExecutionFailed = "No se pudo guardar el resultado de ejecución"
             + "del experimento.";
 
     // Login
     // -------------------------------------------------------------------------
     public static final String LoginSp = "Login";
     public static final int LoginParamUserName = 1;
     public static final int LoginParamPassword = 2;
     public static final int LoginColId = 1;
     public static final int LoginColUserName = 2;
     public static final int LoginColName = 3;
     public static final int LoginColLastName1 = 4;
     public static final int LoginColLastName2 = 5;
     public static final int LoginColRole = 6;
 
     // Get all users
     // -------------------------------------------------------------------------
     public static final String AllUsersSp = "GetAllUsers";
     public static final int AllUsersColId = 1;
     public static final int AllUsersColUserName = 2;
     public static final int AllUsersColName = 3;
     public static final int AllUsersColLastName1 = 4;
     public static final int AllUsersColLastName2 = 5;
     public static final int AllUsersColRole = 6;
     public static final int AllUsersColCreationDate = 7;
     public static final int AllUsersColEnabled = 8;
 
     // Create new user
     // -------------------------------------------------------------------------
     public static final String NewUserSp = "CreateUser";
     public static final int NewUserParamUserName = 1;
     public static final int NewUserParamPassword = 2;
     public static final int NewUserParamName = 3;
     public static final int NewUserParamLastName1 = 4;
     public static final int NewUserParamLastName2 = 5;
     public static final int NewUserParamRole = 6;
     public static final int NewUserColResult = 1;
 
     // Get user applications
     // -------------------------------------------------------------------------
     public static final String UserAppsSp = "GetApplications";
     public static final int UserAppsParamUserId = 1;
     public static final String UserAppsColId = "ApplicationId";
     public static final String UserAppsColDescription = "Description";
     public static final String UserAppsColUpdateDate = "UpdateDate";
     public static final String UserAppsColRelativePath = "RelativePath";
 
     // Create new program
     // -------------------------------------------------------------------------
     public static final String NewProgramSp = "CreateProgram";
     public static final int NewProgramParamDescription = 1;
     public static final int NewProgramParamRelativePath = 2;
     public static final int NewProgramParamOwnerId = 3;
     public static final int NewProgramColId = 1;
 
     // Get parameter types
     // -------------------------------------------------------------------------
     public static final String ParamTypesSp = "GetParameterTypes";
     public static final String ParamTypesColName = "Type";
 
     // Get parameter types
     // -------------------------------------------------------------------------
     public static final String UserRolesSp = "GetUserTypes";
     public static final String UserRolesColName = "Role";
 
     // Create new experiment
     // -------------------------------------------------------------------------
     public static final String NewExperimentSp = "CreateExperiment";
     public static final int NewExperimentParamName = 1;
     public static final int NewExperimentParamDescription = 2;
     public static final int NewExperimentParamApplication = 3;
     public static final int NewExperimentParamParallelExecution = 4;
     public static final int NewExperimentParamInputLine = 5;
     public static final int NewExperimentParamInputPath = 6;
     public static final int NewExperimentParamOwnerId = 7;
     public static final int NewExperimentColId = 1;
 
     // Add parallel configuration
     // -------------------------------------------------------------------------
     public static final String ParallelSp = "AddParallelConfiguration";
     public static final int ParallelParamExperimentId = 1;
     public static final int ParallelParamProcessors = 2;
     public static final int ParallelParamSaveNodeLog = 3;
     public static final int ParallelParamSharedWorkingDir = 4;
     public static final int ParallelParamMiddleware= 5;
 
     // Add experiment parameters
     // -------------------------------------------------------------------------
     public static final String AddParamsSp = "AddExperimentParameter";
     public static final int AddParamsParamExperimentId = 1;
     public static final int AddParamsParamName = 2;
     public static final int AddParamsParamType = 3;
     public static final int AddParamsParamValue = 4;
 
     // Get user experiments
     // -------------------------------------------------------------------------
     public static final String UserExpSp = "GetExperimentGeneralInfo";
     public static final int UserExpParamUserId = 1;
     public static final String UserExpColId = "ExperimentId";
     public static final String UserExpColName = "Name";
     public static final String UserExpColDescription = "Description";
     public static final String UserExpColExecPath = "ExecutablePath";
     public static final String UserExpColParallelExec = "ParallelExecution";
     public static final String UserExpColInputLine = "InputParametersLine";
     public static final String UserExpColInFilePath = "InputFilePath";
     public static final String UserExpColCreationDate = "CreationDate";
 
     // Get experiment parameters
     // -------------------------------------------------------------------------
     public static final String ExpParamSp = "GetExperimentParameters";
     public static final int ExpParamParamExpId = 1;
     public static final String ExpParamColName = "ParameterName";
     public static final String ExpParamColType = "Type";
     public static final String ExpParamColValue = "Value";
 
     // Get experiment statistics
     // -------------------------------------------------------------------------
     public static final String ExpStatsSp = "GetExperimentStatistics";
     public static final int ExpStatsParamExpId = 1;
     public static final String ExpStatsColOutputPath = "OutputPath";
     public static final String ExpStatsColStartDate = "StartDateTime";
     public static final String ExpStatsColFinishDate = "FinishDateTime";
     public static final String ExpStatsColUsedMemory = "UsedMemory";
     public static final String ExpStatsColWallClockTime = "WallClockTime";
     public static final String ExpStatsColCPUUsage = "CPUUsage";
 
     // Get parallel configuration
     // -------------------------------------------------------------------------
     public static final String ExpParConfSp = "GetParallelConfiguration";
     public static final int ExpParConfExpId = 1;
     public static final String ExpParConfColProcNumb = "NumberOfProcessors";
     public static final String ExpParConfSaveNodeLog = "SaveNodeLog";
     public static final String ExpParConfShrWorkDir = "SharedWorkingDirectory";
     public static final String ExpParConfMiddleware = "Middleware";
 
     // Save experiment execution
     // -------------------------------------------------------------------------
     public static final String SaveExecSp = "SaveExecution";
     public static final int SaveExecParamStartDate = 1;
     public static final int SaveExecParamFinishDate = 2;
     public static final int SaveExecParamExpId = 3;
     public static final int SaveExecParamUsedMemory = 4;
     public static final int SaveExecParamWallCLockTime = 5;
     public static final int SaveExecParamOutputFilePath = 6;
     public static final int SaveExecParamCPUUsage = 7;
 
 }
