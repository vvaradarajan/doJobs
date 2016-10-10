package doJobs.doJobs;

import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
abstract class  Job extends Thread {

	public abstract String execute();
	String jobId;
	String jobClass;
	String jobParams;
	MsgCtr mc;
	boolean startExec;
	public abstract void TestIfReady();
	
	public ArrayList<String> StConds;
	public int jobDuration;
	public Job(MsgCtr mc,String jobID, String jobClass, String jobParams) {
		this.jobId=jobID;
		this.jobClass=jobClass;
		this.jobParams=jobParams;
		this.mc=mc;
		this.StConds=new ArrayList<String>();
		Gson g= new Gson();
		JsonObject jo= (JsonObject) new JsonParser().parse(jobParams);
		JsonArray ja = jo.get("Dependents").getAsJsonArray();
		StringBuffer sTemp = new StringBuffer();
		for (JsonElement s: ja){
		  String waitForjId=s.getAsString();
		  StConds.add(waitForjId+" done");
		  sTemp.append(waitForjId+",");
		}
		if (sTemp.length() > 2) sTemp.deleteCharAt(sTemp.length()-1);
		System.out.println(jobId +" will wait for: " + sTemp.toString());
		jobDuration = jo.get("jobTime").getAsInt();
	}
}
