package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Scheduler;

public class SchedulerService extends MicroService {
    /**
     * @param name the micro-service name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    final private Cluster cluster=Cluster.getInstance();
    //final private Scheduler scheduler;
    public SchedulerService(String name, Scheduler scheduler) {
        super(name);
        //this.scheduler=scheduler;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class,(T)->{
            if(T.getCurrTime()<T.getDuration()){
                for(GPU gpu: cluster.getGpus()){
                    cluster.unlockGPU(gpu);
                }
            }else{
                terminate();
                cluster.terminate();
            }
        });
    }
}
