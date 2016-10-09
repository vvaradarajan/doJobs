package doJobs.doJobs;

import java.util.HashSet;
import java.util.Set;

public class MsgCtr {
public Set<String> msgs;
boolean test = false;
public synchronized boolean msgPosted(String s) {
	if (!test) {
		//test block
		test=true;
		return false;
	}
	try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return true;
}

public synchronized void post(String msg) {
	msgs.add(msg);
	System.out.println("Got: " + msg);
}
MsgCtr() {
	msgs = new HashSet<String>();
}
}
