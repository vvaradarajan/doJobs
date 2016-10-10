package doJobs.doJobs;

import java.util.ArrayList;

public class JobA_Roll extends Job {
	public JobA_Roll(MsgCtr mc, String jobID, String jobClass, String jobParams) {
		super(mc, jobID, jobClass, jobParams);
		// TODO Auto-generated constructor stub
	}
	public String execute() {
		// TODO Auto-generated method stub
		System.out.println("Job " + jobId+ " started at: " + (System.nanoTime()-mc.startTime)/1000000);
		try {
			Thread.sleep(jobDuration*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Job " + jobId+ " finished  at: " + (System.nanoTime()-mc.startTime)/1000000);
		return jobId+" done";
	}
	@Override
	public void run(){
       while (true) {
    	boolean startExec = true;
        for (String s: StConds) {
        	startExec = mc.msgPosted(s);
        	if (startExec) {
        		System.out.println(jobId + " got msg: "+ s);
        	}
        	else break;
        }
       if (startExec) 
         {execute(); 
          mc.post(jobId + " done");
          return;
         } else {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       }
      }
   }
}