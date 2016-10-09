package doJobs.doJobs;

import java.util.ArrayList;

abstract class  Job extends Thread {

	public abstract String execute();
	String jobId;
	String jobClass;
	String jobParams;
	MsgCtr mc;
	public Job(MsgCtr mc,String jobID, String jobClass, String jobParams) {
		this.jobId=jobID;
		this.jobClass=jobClass;
		this.jobParams=jobParams;
		this.mc=mc;
	}
}
