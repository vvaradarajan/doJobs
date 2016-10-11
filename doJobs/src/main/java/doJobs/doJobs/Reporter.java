package doJobs.doJobs;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import doJobs.doJobs.App.jobLine;

public class Reporter implements Runnable {
	MsgCtr mc;
	Map<String,Job> jobs;
	Reporter(MsgCtr mc, Map<String,Job> jobs) {
		this.mc=mc;
		this.jobs=jobs;
	}
	void processMsgs() {
		System.out.println("Reporter Process Messages");
		for (String s:mc.msgs) {
			
		}
	}
	public void run() {
		// TODO Auto-generated method stub
	  while (true) {
		synchronized(mc){
            try{
                System.out.println("Waiting for trigger from MsgCtr...");
                mc.wait();
            }catch(InterruptedException e){
                System.out.println("Reporter Interrupted => stopped");
                System.exit(0);
            }
            processMsgs();
        }
	  }
	}

}

