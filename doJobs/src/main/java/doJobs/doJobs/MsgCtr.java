package doJobs.doJobs;

import java.util.HashSet;
import java.util.Set;

public class MsgCtr {
public Set<String> msgs;
public long startTime; //to track when messages are posted.
boolean test = false;
public synchronized boolean IsMsgPosted(String s) {
	if (msgs.contains(s)) return true;
	return false;
}

public void dumpMsgs() {
	System.out.println("Messages Rcvd: count="+msgs.size());
	for (String s:msgs) {
		System.out.println(s);
	}
}
public synchronized void post(String msg) {
	msgs.add(msg);
	notify();
}
MsgCtr(long startTime) {
	msgs = new HashSet<String>();
	this.startTime=startTime;
}
}
