package doJobs.doJobs;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

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
    	jobs.add(new jobLine("a","JobA_Roll","{Dependents:['c','d','e']}"));
    	jobs.add(new jobLine("b","JobA_Roll","{Dependents:['c','d','e']}"));
    	jobs.add(new jobLine("c","JobA_Roll","{Dependents:['d','e']}"));
    	jobs.add(new jobLine("d","JobA_Roll","{Dependents:[]}"));
    	jobs.add(new jobLine("e","JobA_Roll","{Dependents:[]}"));
    }
	public static void main( String[] args )
    {
        //initiate MsgCtr
    	MsgCtr mc = new MsgCtr();
    	System.out.println( "Allowing all jobs to go free!" );
    	Job jl;
		for (jobLine j:jobs) {
		try {
			Constructor<Job> c = (Constructor<Job>) Class.forName("doJobs.doJobs."+j.jobClass).getConstructor(MsgCtr.class,String.class, String.class, String.class);
			jl = (Job) c.newInstance(mc,j.jobId,j.jobClass,j.jobParams);
			jl.start();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
        
    }
}
