package doJobs.doJobs;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        //initiate MsgCtr
    	MsgCtr mc = new MsgCtr();
    	System.out.println( "Allowing all jobs to go free!" );
        Thread t = new JobA_Roll(mc,"GazeboExecute");
        t.start();
    }
}
