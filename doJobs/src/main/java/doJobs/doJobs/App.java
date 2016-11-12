package doJobs.doJobs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import doJobs.doJobs.*;
import doJobs.doJobs.Job.jobStatusCode;
/**
 * Hello world!
 *
 */

public class App 
{
    static class jobLine {

		String jobId;
    	String jobClass;
    	String jobParams;
    	jobStatusCode jobStatus ;
    	public jobLine(String jobId, String jobClass, String jobParams) {
			this.jobId=jobId;
			this.jobClass=jobClass;
			this.jobParams=jobParams;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			jobLine other = (jobLine) obj;
			if (jobId == null) {
				if (other.jobId != null)
					return false;
			} else if (!jobId.equals(other.jobId))
				return false;
			return true;
		}
    }
    static List<JobTbl> jobLines = new ArrayList<JobTbl>();
	static Map<String,Job> jobs = new HashMap<String,Job>();
	static final int timeFactor=1000000; //convert nano seconds to ms
	
    public static void mainTest() {
    	String jobParams = "{Dependents:['c','d','e'],jobTime:1}";
    	Gson g= new Gson();
		JsonObject jo= (JsonObject) new JsonParser().parse(jobParams);
		JsonArray ja = jo.get("Dependents").getAsJsonArray();
		for (JsonElement s: ja){
		  String waitForJobId=s.getAsString();
		  System.out.println(" will wait for: " + waitForJobId);
		}
		int jobDuration = jo.get("jobTime").getAsInt();
		System.out.println(jobDuration);
		System.exit(0);
    }
    public static void printClassPath() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		 
        URL[] urls = ((URLClassLoader)cl).getURLs();
        System.out.println("ClassPath:");
        for(URL url: urls){
        	System.out.println(url.getFile());
        }
	};
	
	public static void main( String[] args ) throws InterruptedException, IOException
    {
        //initiate MsgCtr
		//mainTest();
		System.out.println("Starting..");
		Properties doJobsProperties = new Properties();
		String propertyFNM="doJobs.properties";
		printClassPath();
		try (final InputStream stream =
				ClassLoader.getSystemClassLoader().getResourceAsStream(propertyFNM)) {
			doJobsProperties.load(stream);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not load properties! from: "+propertyFNM );
			System.exit(1);
		}
		String propPrefix="u";
		if (System.getProperty("os.name").startsWith("Windows")) propPrefix="w";
		//jobLines=JobTbl.loadJobLines(doJobsProperties.getProperty(propPrefix+".jobConfigfile"));
    	EntityManagerFactory emf = Persistence.createEntityManagerFactory("mysqlTables");
    	EntityManager em = emf.createEntityManager();
		String categ=args[0];
		jobLines=JobTbl.loadJobLinesDB(em, categ);
		em.close();emf.close();
		if (jobLines.isEmpty()) {
			System.out.println("No jobs in category : "+ args[0]+"\n Exiting..");
			System.exit(1);
		}
		long startTime = System.nanoTime();
    	MsgCtr mc = new MsgCtr(startTime,jobs);
    	Reporter rptr = new Reporter(mc,jobs,Paths.get(doJobsProperties.getProperty(propPrefix+".jobRptfile"))
    			,Paths.get(doJobsProperties.getProperty(propPrefix+".jobLockfile")));
    	rptr.releaseInterProcessLock();
    	
    	int corePoolSize=10; int maximumPoolSize=10; long keepAliveTime=60;
    	TimeUnit unit=TimeUnit.SECONDS;
    	BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100);
    	ThreadPoolExecutor tpi = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit, workQueue);
    	//run reporter as a separate thread
    	Thread RepThrd= new Thread(rptr);
    	RepThrd.start();
    	Job jl;
    	mc.dumpMsgs();
		while (!jobLines.isEmpty()) {
			ArrayList<JobTbl> jrs = JobTbl.getReadySet(jobLines);
			System.out.println("Allowing following jobs to go free!");
			JobTbl.dumpJobLines(jrs);

			for (JobTbl j : jrs) {
				try {
					Constructor<Job> c = (Constructor<Job>) Class.forName("doJobs.doJobs." + j.jobClass)
							.getConstructor(MsgCtr.class, String.class, String.class, String.class);
					jl = (Job) c.newInstance(mc, j.jobId, j.jobClass, j.jobParams);
					jobs.put(j.jobId, jl);
					System.out.println("Started: " + j.jobId);
					tpi.execute(jl);
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
    	System.out.println("Waiting for all threads to Complete..");
    	tpi.shutdown();
    	tpi.awaitTermination(1000, TimeUnit.SECONDS);
    	System.out.println("All threads to Completed..");
    	rptr.stopFlag=true; // stop reporter thread after all jobs are done
    	mc.dumpMsgs();
    	mc.endingPost();
    	System.out.println("Total Elapsed Time: " + mc.getTimeFromStart() + " ms");
    	while (true) {
    		if (RepThrd.getState() != Thread.State.TERMINATED) {
    			Thread.sleep(1000);
    		}
    		else {
    			//record execution status in DB
    			BufferedReader is = Files.newBufferedReader(Paths.get(doJobsProperties.getProperty(propPrefix+".jobRptfile")));
    			JobExecRecords.saveJobExecutionInDB(em, categ, is);
    			System.exit(0);
    		}
    	}
    }
}
