 package biz.c24.batchdemo;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.batch.core.Job;
 import org.springframework.batch.core.JobParameter;
 import org.springframework.batch.core.JobParameters;
 import org.springframework.batch.core.JobParametersInvalidException;
 import org.springframework.batch.core.launch.JobLauncher;
 import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
 import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
 import org.springframework.batch.core.repository.JobRestartException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.core.io.ClassPathResource;
 
 public class RunJob {
 	
 	public static void runTransform(ApplicationContext context) throws IOException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
 		
 		// Get the job
 		Job job = context.getBean("fileTransformer", Job.class);
 		
 		// .. and a single JobLauncher bean
 		JobLauncher jobLauncher = context.getBean(JobLauncher.class);
 		
 		// Create the job parameters and supply the name of the file to parse
 		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
 		
 		// The C24 Spring Batch sources expect to get the filename from a job property called input.file
 		// In this case, the file is in the classpath and is called employees-3-valid.csv
		params.put("input.file", new JobParameter(new ClassPathResource("employees-3-valid.csv").getFile().getAbsolutePath()));
 		
 		File outputFile = File.createTempFile("RunJob-", ".csv.zip");
 		System.out.println("Transforming to " + outputFile.getAbsolutePath());		
 		params.put("output.file", new JobParameter(outputFile.getAbsolutePath()));
 		
 		JobParameters jobParameters = new JobParameters(params);
 
 		// Launch the job!
 		jobLauncher.run(job, jobParameters);
 
 	}
 	
 	public static void runParse(ApplicationContext context) throws IOException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
 		// Get the file loader job from the context
 		Job job = context.getBean("fileLoader", Job.class);
 		
 		// .. and a single JobLauncher bean
 		JobLauncher jobLauncher = context.getBean(JobLauncher.class);
 		
 		// Create the job parameters and supply the name of the file to parse
 		Map<String, JobParameter> params = new HashMap<String, JobParameter>();
 		
 		// The C24 Spring Batch sources expect to get the filename from a job property called input.file
 		// In this case, the file is in the classpath and is called employees-3-valid.csv
		params.put("input.file", new JobParameter(new ClassPathResource("employees-3-valid.csv").getFile().getAbsolutePath()));
 		
 		JobParameters jobParameters = new JobParameters(params);
 
 		// Launch the job!
 		jobLauncher.run(job, jobParameters);
 
 	}
 	
 	public static void main(String[] args) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, IOException {
 		
 		// Create our application context - assumes the Spring configuration is in the classpath in a file called spring-config.xml
 		ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
 		
 		runParse(context);
 		runTransform(context);
 
 	}
 
 }
