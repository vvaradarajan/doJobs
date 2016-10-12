package doJobs.doJobs;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    static ArrayList<jobLine> jobLines = new ArrayList<jobLine>();
	static Map<String,Job> jobs = new HashMap<String,Job>();
	static final int timeFactor=1000000; //convert nano seconds to ms
    static {
    	//put some jobs
    	jobLines.add(new jobLine("a","JobA_Roll","{Dependents:['d','e'],jobTime:1}"));
    	jobLines.add(new jobLine("b","JobA_Roll","{Dependents:['d','e'],jobTime:5}"));
    	jobLines.add(new jobLine("c","JobA_Roll","{Dependents:['d','e'],jobTime:6}"));
    	jobLines.add(new jobLine("d","JobA_Roll","{Dependents:[],jobTime:1}"));
    	jobLines.add(new jobLine("e","JobA_Roll","{Dependents:[],jobTime:1}"));
    }
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
	public static void main( String[] args ) throws InterruptedException
    {
        //initiate MsgCtr
		//mainTest();
		System.out.println("Starting..");
		long startTime = System.nanoTime();
    	MsgCtr mc = new MsgCtr(startTime,jobs);
    	Reporter rptr = new Reporter(mc,jobs,Paths.get("c:\\junk\\report.txt"));
    	rptr.releaseInterProcessLock();
    	System.out.println( "Allowing all jobs to go free!" );
    	int corePoolSize=5; int maximumPoolSize=10; long keepAliveTime=60;
    	TimeUnit unit=TimeUnit.SECONDS;
    	BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100);
    	ThreadPoolExecutor tpi = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit, workQueue);
    	//run reporter as a separate thread
    	Thread RepThrd= new Thread(rptr);
    	RepThrd.start();
    	Job jl;
    	mc.dumpMsgs();
    	 for (jobLine j :jobLines) {  
			try {
				Constructor<Job> c = (Constructor<Job>) Class.forName("doJobs.doJobs." + j.jobClass)
						.getConstructor(MsgCtr.class, String.class, String.class, String.class);
				jl = (Job) c.newInstance(mc, j.jobId, j.jobClass, j.jobParams);
				jobs.put(j.jobId,jl);
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
    	System.out.println("Waiting for all threads to Complete..");
    	tpi.shutdown();
    	tpi.awaitTermination(1000, TimeUnit.SECONDS);
    	System.out.println("All threads to Completed..");
    	rptr.stopFlag=true; // stop reporter thread after all jobs are done
    	mc.dumpMsgs();  
    	long endTime = System.nanoTime();
    	System.out.println("Total Elapsed Time: " + mc.getTimeFromStart() + " ms");
    }
}
