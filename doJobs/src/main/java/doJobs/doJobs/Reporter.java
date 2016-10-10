package doJobs.doJobs;

public class Reporter implements Runnable {
	MsgCtr mc;

	Reporter(MsgCtr mc) {
		this.mc=mc;
	}
	void processMsgs() {
		System.out.println("Reporter Process Messages");
	}
	public void run() {
		// TODO Auto-generated method stub
		synchronized(mc){
            try{
                System.out.println("Waiting for trigger from MsgCtr...");
                mc.wait();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            processMsgs();
        }
	}

}

