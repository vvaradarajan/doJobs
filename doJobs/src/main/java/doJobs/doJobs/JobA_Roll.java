package doJobs.doJobs;

import java.util.ArrayList;

public class JobA_Roll extends Job {
	public JobA_Roll(MsgCtr mc, String jobID, String jobClass, String jobParams) {
		super(mc, jobID, jobClass, jobParams);
		// TODO Auto-generated constructor stub
	}
	public String execute() {
		// TODO Auto-generated method stub
		System.out.println("Job " + jobId+ " started at: " + mc.getTimeFromStart());
		try {
			Thread.sleep(jobDuration*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Job " + jobId+ " finished  at: " + mc.getTimeFromStart());
		return jobId+" done";
	}
	@Override
	public void run(){
       while (true) {
       TestIfReady();
       if (startExec) 
         {mc.post(jobId ,jobStatusCode.running);
    	  execute(); 
          mc.post(jobId ,jobStatusCode.complete);
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
	@Override
	public void TestIfReady() {
		// TODO Auto-generated method stub
		startExec = true;
        for (conds s: StConds) {
        	startExec = mc.IsJobStatus(s.jobId, s.js);
        	if (startExec) {
        		System.out.println(jobId + " got msg: "+ s.js.name());
        	}
        	else break;
        }
	}
}