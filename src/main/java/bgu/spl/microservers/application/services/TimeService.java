package bgu.spl.mics.application.services;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Cluster;


/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{
	final private int TickTime;
	final private int Duration;
	public TimeService(int TickTime, int Duration) {
		super("TimeService");
		this.TickTime=TickTime;
		this.Duration=Duration;
	}
	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class,(b)->{
			if(b.getCurrTime()==Duration)
				System.out.println("*********\n"+Thread.currentThread().getName()+" knows  it is time to kill the process"+"\n ************");
				terminate();

		});
		for(int currTime=0;currTime<=Duration;currTime+=TickTime){
			try {
				Thread.sleep(TickTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//System.out.println("Tick "+currTime);
				//System.out.println(Cluster.getInstance().toString());
			TickBroadcast tick=new TickBroadcast(Duration,currTime,TickTime);
			sendBroadcast(tick);
		}
		sendBroadcast(new TerminateBroadcast());
	}
	public int getTickTime() {
		return TickTime;
	}
	public int getDuration() {
		return Duration;
	}
}

