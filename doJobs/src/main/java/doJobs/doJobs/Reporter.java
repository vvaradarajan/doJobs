package doJobs.doJobs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import doJobs.doJobs.App.jobLine;
import doJobs.doJobs.Job.jobStatusCode;

public class Reporter implements Runnable {
	MsgCtr mc;
	Map<String,Job> jobs;
	class StatusTime {
		public StatusTime(jobStatusCode status, long msTime) {
			this.status = status;
			this.msTime = msTime;
		}
		jobStatusCode status;
		long msTime;
		
	}
	Map<String,ArrayList<StatusTime>> jobStatusChanges;
	static Charset charset = Charset.forName("US-ASCII");
	Path reportFile;
	Path lockFile;
	public boolean stopFlag;
	ArrayList<StatusTime> getJobStatusArrayList(String jid) {
		if (jobStatusChanges.containsKey(jid)) return
				jobStatusChanges.get(jid);
		ArrayList<StatusTime> st=new ArrayList<StatusTime>();
		st.add(new StatusTime(jobStatusCode.wait,mc.getTimeFromStart()));
		jobStatusChanges.put(jid,(ArrayList<StatusTime>) st);
		return st;
	}
	Reporter(MsgCtr mc, Map<String,Job> jobs,Path file) {
		this.mc=mc;
		this.jobs=jobs;
		this.reportFile=file;
		this.stopFlag=false;
		this.lockFile=Paths.get("C:/junk/junk.lock");
		this.jobStatusChanges=new HashMap<String,ArrayList<StatusTime>> ();
	}
	boolean getInterProcessLock() {
		try {
		Files.createFile(lockFile);  //nio guarantees atomicty of operation
		} catch (Exception e) {
			System.out.println("getInterProcessLock: " + e.getMessage());
			return false;
		}
		return true;
	}
	public boolean releaseInterProcessLock() {
		try {
			Files.delete(lockFile);  
		} catch (Exception e) {
			System.out.println("releaseInterProcessLock: " + e.getMessage());
		}
		return true;		
	}
	void dumpJobStatusTime() {
		for (String jid:jobStatusChanges.keySet()) {
			System.out.println("Job: "+jid );
			for (StatusTime st:jobStatusChanges.get(jid)) {
				System.out.println(st.status.name() + ":" + st.msTime);
			}
		}
	}
	void processJobs() {
		//write job status one line per job line
		Job j;
		BufferedWriter writer = null;
		if (getInterProcessLock()) {
			StringBuffer s = new StringBuffer();
			ArrayList<StatusTime> sta;
			s.append("{jobs:[");
			for (String jid : jobs.keySet()) {
				// create a json array [[jobId,status], ...]
				j = jobs.get(jid);
				sta = getJobStatusArrayList(jid);
				jobStatusCode cjs = j.jobStatus;
				StatusTime lastSte = sta.get(sta.size() - 1);
				if (lastSte.status.equals(cjs)) {
					// update nothing (The same status continues..)
					// lastSte.msTime = mc.getTimeFromStart();
				} else {
					lastSte = new StatusTime(cjs, mc.getTimeFromStart());
					sta.add(lastSte);
				}
				//output the Json
				s.append("{jobId:'"+jid+"',");
				for (StatusTime st:sta) s.append(st.status.name()+":"+st.msTime+",");
				s.setCharAt(s.length() - 1, '}');
				s.append(",");

			}
			s.setCharAt(s.length() - 1, ']');s.append("}");
			try {
				writer = Files.newBufferedWriter(reportFile, charset);
				writer.write(s.toString(), 0, s.length());
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			releaseInterProcessLock();
		};
		
		if (stopFlag) {
			System.out.println("Reporter Stopped");
			dumpJobStatusTime();
			System.exit(0);
		}
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
                //System.out.println("Waiting for trigger from MsgCtr...");
                mc.wait();
                processJobs();
            }catch(InterruptedException e){
                System.out.println("Reporter Interrupted and aborted!");
            }            
        }
	  }
	}

}

