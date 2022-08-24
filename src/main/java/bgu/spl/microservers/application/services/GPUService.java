package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.*;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
 //loop waiting for tick broadcast
    private int currTick;
    private final int Duration;
    private final Cluster cluster=Cluster.getInstance();
    private final PriorityQueue<Event<?>> eventsQueue;
    //private final ConcurrentHashMap<Model,TrainModelEvent> ModelTraining;
    private volatile TrainModelEvent workingTrainModel;
    private final GPU gpu;
    public GPUService(String name,int duration,GPU gpu) {
        super(name);
        this.Duration=duration;
        currTick=0;
        this.gpu=gpu;
        eventsQueue=new PriorityQueue<>((message, t1) -> {
            if(message instanceof TestModelEvent){
                return 1;
            }else if(t1 instanceof TestModelEvent){
                return -1;
            }else
                return 0;
        });
        gpu.setService(this);
        //this.gpus.addAll(gpus);
        //ModelTraining=new ConcurrentHashMap<>();
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class,(T)->{
            currTick++;
            if (currTick<T.getDuration()) {
                //if (workingTrainModel != null && workingTrainModel.getModel().getName().equals("YOLO9000"))
                //   System.out.println(Thread.currentThread().getName() + "is updating Tick while Training Bert at Tick" + currTick);
                if (!gpu.isTraining()) {
                    if (gpu.isCompleted()) {
                        /*
                        if (workingTrainModel != null && workingTrainModel.getModel().getName().equals("Bert"))
                            System.out.println(Thread.currentThread().getName() + " is completeing Bert " + workingTrainModel.toString());

                         */
                        complete(workingTrainModel, gpu.getModel());
                        gpu.setCompleted(false);
                    }
                    nextJob();
                }
                gpu.updateTick();
            }
        });
        subscribeEvent(TrainModelEvent.class,(T)->{
            if(T.getStudent().getName().equals("Nala"))
                System.out.println("*********\n"+"got nala training job"+"\n ************");

            if(gpu.isTraining())
                eventsQueue.add(T);
            else {
                workingTrainModel=T;
                gpu.trainModel(T.getModel(),currTick);
                //System.out.println("GPU "+ getName()+" Training "+T.getModel());
            }
        });
        subscribeEvent(TestModelEvent.class,(T)->{
            if(T.getStudent().getName().equals("Nala"))
                System.out.println("*********\n"+"got nala testing job"+"\n ************");

            if(gpu.isTraining()){
                eventsQueue.add(T);
            }
            else{
                /*
                if(T.getTestModel().getName().equals("Bert"))
                    System.out.println("*********\n"+gpu.getName()+" started working on "+T.getTestModel().getName()+"at tick"+currTick+"\n ************");

                 */
                complete(T,gpu.testModel(T.getTestModel(),T.getStudent()));
            }
        });
        subscribeBroadcast(TerminateBroadcast.class,(T)->{
            System.out.println("*********\n"+Thread.currentThread().getName()+" knows it is time to kill the process"+"\n ************");
            //gpu.Terminate();
            for(Event<?> event: eventsQueue){
                if(event instanceof TrainModelEvent) {
                    ((TrainModelEvent)event).terminate();
                    complete((TrainModelEvent) event, ((TrainModelEvent) event).getModel());
                }
                else{
                    ((TestModelEvent)event).timeOUT();
                    complete((TestModelEvent) event,gpu.testModel(((TestModelEvent) event).getTestModel(), ((TestModelEvent) event).getStudent()));
                }
            }
            //cluster.terminateGPU(gpu);
            terminate();
        });
    }

    public void nextJob() {
        while (!eventsQueue.isEmpty()) {
            Event<?> event = eventsQueue.poll();
            if (event instanceof TestModelEvent) {
                //testing
                /*
                if(gpu.getModel()!=null){
                    System.out.println("*********\n"+gpu.getName()+"has a model and it is in nextJob"+"\n ************");
                }

                 */
                Boolean result = gpu.testModel(((TestModelEvent) event).getTestModel(), ((TestModelEvent) event).getStudent());
                complete((TestModelEvent) event, result);
                break;
            } else {
                workingTrainModel=(TrainModelEvent)event;
                gpu.trainModel(((TrainModelEvent)event).getModel(),currTick);
                //System.out.println("GPU " + gpu.getType() + " Training " + ((TrainModelEvent)event).getModel());
                break;
            }
        }
    }
}
