package doJobs.doJobs;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    	public jobLine(String jobId, String jobClass, String jobParams) {
			this.jobId=jobId;
			this.jobClass=jobClass;
			this.jobParams=jobParams;
		}
    }
    static ArrayList<jobLine> jobs = new ArrayList<jobLine>();
    static {
    	//put some jobs
    	jobs.add(new jobLine("a","JobA_Roll","{Dependents:['d','e'],jobTime:1}"));
    	jobs.add(new jobLine("b","JobA_Roll","{Dependents:['d','e'],jobTime:5}"));
    	jobs.add(new jobLine("c","JobA_Roll","{Dependents:['d','e'],jobTime:6}"));
    	jobs.add(new jobLine("d","JobA_Roll","{Dependents:[],jobTime:1}"));
    	jobs.add(new jobLine("e","JobA_Roll","{Dependents:[],jobTime:1}"));
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
		long startTime = System.nanoTime();
    	MsgCtr mc = new MsgCtr(startTime);
    	System.out.println( "Allowing all jobs to go free!" );
    	int corePoolSize=5; int maximumPoolSize=10; long keepAliveTime=60;
    	TimeUnit unit=TimeUnit.SECONDS;
    	BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100);
    	ThreadPoolExecutor tpi = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit, workQueue);
    	Job jl;
    	mc.dumpMsgs();
    	for (jobLine j:jobs) {
			try {
				Constructor<Job> c = (Constructor<Job>) Class.forName("doJobs.doJobs." + j.jobClass)
						.getConstructor(MsgCtr.class, String.class, String.class, String.class);
				jl = (Job) c.newInstance(mc, j.jobId, j.jobClass, j.jobParams);
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
    	mc.dumpMsgs();  
    	long endTime = System.nanoTime();
    	System.out.println("Total Elapsed Time: " + (endTime-startTime)/1000000 + " seconds");
    }
}
