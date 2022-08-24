package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;

/**
 * CPU service is responsible for handling the
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private int currTick;
    private final int Duration;
    private final Cluster cluster= Cluster.getInstance();
    private final CPU cpu;
    public CPUService(String name, int duration,CPU cpu) {
        super(name);
        Duration = duration;
        currTick=0;
        this.cpu=cpu;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class,(T)->{
            currTick+=1;//testing
            if(!cpu.isTerminated()){
                if (currTick == Duration) {
                    System.out.println("*********\n" + Thread.currentThread().getName() + " knows  it is time to kill the process" + "\n ************");
                    cpu.Terminate();
                    terminate();
                } else
                    cpu.increaseTick();
            }else{
                System.out.println("*********\n"+Thread.currentThread().getName()+" knows  it is time to kill the process"+"\n ************");
                terminate();
            }
        });
        subscribeBroadcast(TerminateBroadcast.class,(T)->{
            System.out.println("*********\n"+Thread.currentThread().getName()+" knows  it is time to kill the process"+"\n ************");
            cpu.Terminate();
            terminate();
        });
    }
}
