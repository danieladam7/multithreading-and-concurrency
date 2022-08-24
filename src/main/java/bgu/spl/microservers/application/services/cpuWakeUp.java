package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class cpuWakeUp extends MicroService {
    /**
     * @param name the micro-service name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    Cluster cluster=Cluster.getInstance();

    public void setT(Thread[] t) {
        this.t = t;
    }

    private Thread[] t;
    public cpuWakeUp(String name) {
        super(name);
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class,(T)->{
            if(T.getTickTime()<T.getDuration()){
                for(CPU cpu:cluster.getCpus()){
                    cluster.unlockCPU(cpu);
                }
            }else {
                for(CPU cpu:cluster.getCpus())
                    cpu.Terminate();
                terminate();
            }
        });
    }
}
