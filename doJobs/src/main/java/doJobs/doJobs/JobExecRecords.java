package doJobs.doJobs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Persistence;
import javax.persistence.Table;

import com.google.gson.Gson;

import doJobs.doJobs.Job.jobStatusCode;


@Entity
@Table (name="jobExecRecords")
public class JobExecRecords implements Serializable  {
	public JobExecRecords(String jobCateg) {
		super();
		this.category=jobCateg;
	}
	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	String id;
	
	String category;
	String jobId;
	int idle;
	int executing;
	int complete;
	transient jobStatusCode jobStatus;
	static ArrayList<JobTbl> jobLines = new ArrayList<JobTbl>();
	@Override
	public String toString() {
		return "category/jobId/idle/executing/complete: " + category+"/"+jobId+"/"+idle+"/"+executing+"/"+complete;
	}
	public class job {
		String jobId;
		int idle;
		int executing;
		int complete;
	}
	public class jobs {
		ArrayList<job> jobs;
	}
    public static void saveJobExecutionInDB(EntityManager em, String categ, Reader is) {
    	/* stream sample
    	{"jobs":
    	[{"jobId":"a","idle":3016,"executing":1000,"complete":20}
    	,{"jobId":"b","idle":3011,"executing":5001,"complete":20}
    	,{"jobId":"c","idle":8041,"executing":2000,"complete":20}
    	,{"jobId":"d","executing":2998,"complete":20}
    	,{"jobId":"e","idle":5,"executing":1001,"complete":20}
    	,{"jobId":"f","idle":1010,"executing":1000,"complete":20}]
    	}
    	*/
    	
    	Gson gson = new Gson();
    	jobs j = gson.fromJson(is, jobs.class);
    	if (j.jobs.size()>0) {//new data exists so delete old data
	    	em.getTransaction().begin();
	    	em.createNativeQuery("delete from jobExecRecords where category='"+categ+"'").executeUpdate();
	    	em.getTransaction().commit();
	    }
    	em.getTransaction().begin();
    	for (job jb:j.jobs) {
    		JobExecRecords je=new JobExecRecords(categ);
    		je.jobId=jb.jobId; je.complete=jb.complete;je.executing=jb.executing;je.idle=jb.idle;
    		em.persist(je);
    		System.out.println(jb.executing);
    	}
    	em.getTransaction().commit();
    }
    public static void main( String[] args ) throws InterruptedException, IOException {
    	FileReader is = new FileReader("C:/junk/report.txt");
    	EntityManagerFactory emf = Persistence.createEntityManagerFactory("mysqlTables");
    	EntityManager em = emf.createEntityManager();
    	saveJobExecutionInDB(em,"ProjectG",is);
    	em.close();emf.close();is.close();
    	
    }
}