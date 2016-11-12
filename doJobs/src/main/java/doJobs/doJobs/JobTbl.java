package doJobs.doJobs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Persistence;
import javax.persistence.Table;

import com.google.gson.Gson;

import doJobs.doJobs.Job.jobStatusCode;
import doJobs.doJobs.JobExecRecords.jobs;


@Entity
@Table (name="jobs")
public class JobTbl implements Serializable  {
	public JobTbl(String jobId, String jobClass, String jobParams) {
		super();
		this.jobId = jobId;
		this.jobClass = jobClass;
		this.jobParams = jobParams;
    	Gson gson = new Gson();
    	this.params = gson.fromJson(jobParams, jobParams.class);
	}
    static class jobParams {
    	//{"Dependents":["passA","passK"],"jobTime":2}
    	Set<String> Dependents;
    	int jobTime;
    }
	@Id String jobId;
	String jobClass;
	String jobParams;
	transient jobParams params;
	transient jobStatusCode jobStatus;
	static ArrayList<JobTbl> jobLines = new ArrayList<JobTbl>();
	@Override
	public String toString() {
		return "jobId/jobClass/jobParams: " + jobId+"/"+jobClass+"/"+jobParams;
	}
    public static List<JobTbl> loadJobLinesDB(EntityManager em,String jc) {
    	//put some jobs
    	jobLines.clear();
    	String sql="select j.jobId,j.jobClass,j.jobParams from jobs j inner join jobCategory jc "
    			+ " on j.jobid=jc.jobid and jc.category='"+jc+"'";
    	for (Object[] objA:((List<Object[]>)em.createNativeQuery(sql).getResultList())) {
    		jobLines.add(new JobTbl(String.valueOf(objA[0]),String.valueOf(objA[1]),String.valueOf(objA[2])));
    	}
    	return jobLines ;
    }
    public static List<JobTbl> loadJobLinesTest() {
    	//put some jobs
    	jobLines.clear();
    	jobLines.add(new JobTbl("a","JobA_Roll","{\"Dependents\":['d','e'],\"jobTime\":1}"));
    	jobLines.add(new JobTbl("b","JobA_Roll","{\"Dependents\":['d','e'],\"jobTime\":5}"));
    	jobLines.add(new JobTbl("c","JobA_Roll","{\"Dependents\":['d','e'],\"jobTime\":6}"));
    	jobLines.add(new JobTbl("d","JobA_Roll","{\"Dependents\":[],\"jobTime\":1}"));
    	jobLines.add(new JobTbl("e","JobA_Roll","{\"Dependents\":[],\"jobTime\":1}"));
    	return jobLines;
    }
    public static List<JobTbl> loadJobLines(String fileNM) throws IOException {
    	jobLines.clear();
    	BufferedReader b = new BufferedReader(new FileReader(fileNM));
    	Pattern jobLinePattern= Pattern.compile("\"([^\"]+?)\",?\"([^\"]+?)\",?\"([^\"]+?)\"$");
    	String ln;
    	while ((ln=b.readLine())!=null) {
    		Matcher m=jobLinePattern.matcher(ln);
    		if (m.matches()) {
		    	if (m.groupCount() == 3) {
		    		jobLines.add(new JobTbl(m.group(1),m.group(2),m.group(3)));
	    		}
	    		else System.out.println("No Groups Ignoring line: " + ln);
    		} else System.out.println("No Match Ignoring line: " + ln);
    	}
    	return jobLines;
    }
    public static ArrayList<JobTbl> getReadySet(List<JobTbl> jobLines) {
    	//Intelligently choose the next set of jobs to execute (Note: jobLines should contain only remaining jobs)
    	ArrayList<JobTbl> jrs = new ArrayList<JobTbl>();
    	for (JobTbl j:jobLines) {
    		if (j.params.Dependents.isEmpty()) {
    			//Add job to readyList
    			jrs.add(j);
    		}
    	}
    	//delete those that ready from jobLines
    	for (JobTbl jr:jrs) {
    		jobLines.remove(jr);
    		for (JobTbl j:jobLines) {
        		j.params.Dependents.remove(jr.jobId);
        	}
    	}
    	//delete those dependencies from the params
    	
    	return jrs;
    }

    public static void dumpJobLines(List<JobTbl> jobLines) {
    	for (JobTbl j:jobLines) System.out.println(j.toString());
    }
}