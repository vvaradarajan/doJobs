package doJobs.doJobs;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import doJobs.doJobs.App.jobLine;
import doJobs.doJobs.Job.jobStatusCode;

public class MsgCtr {
public Set<String> msgs;
Map<String,Job> jobs;
public long startTime; //to track when messages are posted.
boolean test = false;
public synchronized boolean IsMsgPosted(String s) {
	if (msgs.contains(s)) return true;
	return false;
}

public synchronized boolean IsJobStatus(String jobId,jobStatusCode js) {
	Job j = jobs.get(jobId);
	if (j == null) return false; //job may not have been added to jobs yet
	try {
	if (j.jobStatus.equals(js)) return true;
	return false;
	} catch(Exception e) {
		e.printStackTrace();
		System.out.println("job not found: " + jobId);
		dumpJobs();
	};
	return false;
}
public void dumpMsgs() {
	System.out.println("Messages Rcvd: count="+msgs.size());
	for (String s:msgs) {
		System.out.println(s);
	}
}
public void dumpJobs() {
	System.out.println("Jobs : count="+jobs.size());
	for (String jid:jobs.keySet()) {
		Job j = jobs.get(jid);
		System.out.println(j.jobId+"; status="+j.jobStatus);
	}
}
public synchronized void post(String jobId,jobStatusCode js) {
	msgs.add(jobId + " "+js.name());
	Job jl=jobs.get(jobId);
	jl.jobStatus=js;
	notify();
}
MsgCtr(long startTime,Map<String,Job> jobs) {
	msgs = new HashSet<String>();
	this.startTime=startTime;
	this.jobs=jobs;
}
}
