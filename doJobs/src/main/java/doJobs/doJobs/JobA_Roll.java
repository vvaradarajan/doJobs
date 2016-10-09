package doJobs.doJobs;

import java.util.ArrayList;

public class JobA_Roll extends Thread implements Job {
	 private Thread t;
	 public MsgCtr mc;
	 public String jobId;
	 public ArrayList<String> StConds;
	 JobA_Roll(MsgCtr mc,String jobId) {
			StConds=new ArrayList<String>();
			StConds.add("Gazebo");
			this.mc = mc;
			this.jobId=jobId;
		}
		
	public String execute() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void run(){
       System.out.println("JobA_Roll in Mix");
       while (true) {
       boolean startExec = mc.msgPosted("Gazebo");
       if (startExec) 
         {execute(); 
          mc.post(jobId + " done");
          return;
         } else {
        System.out.println("Waiting for msg: "+ jobId);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       }
      }
   }
}