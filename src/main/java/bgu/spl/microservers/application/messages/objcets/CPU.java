package bgu.spl.mics.application.objects;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class CPU {
    //private Fields
    private final int cores;
    private final Cluster cluster=Cluster.getInstance();
    //private final PriorityBlockingQueue<DataBatch> dataBatchQueue;
    private final LinkedBlockingQueue<DataBatch> dataBatchQueue;
    private DataBatch processingData;
    private int ReleaseTime;
    private int time;
    private boolean finished;
    private final ReentrantReadWriteLock readWriteLock=new ReentrantReadWriteLock();
    private final AtomicInteger integer;
    private boolean isTerminated;
    //public methods
     /**
     * @param cores number of cores
      * @pre : cluster !=null
     */
    public CPU(int cores){
        this.cores=cores;
        time=0;
        /*
        dataBatchQueue =new PriorityBlockingQueue<>(100, (dataBatch, t1) -> {
            if(dataBatch.getWaitingTime()!=0 && dataBatch.getWaitingTime()>=dataBatch.getOptimalTime()){
                if(t1.getWaitingTime()!=0 && t1.getWaitingTime()>=t1.getOptimalTime())
                    return Integer.compare(t1.getWaitingTime(),dataBatch.getWaitingTime());
                else
                    return Integer.compare(dataBatch.getWaitingTime(),dataBatch.getWaitingTime()+5);
            }else{
                return Integer.compare(dataBatch.getOptimalTime(),t1.getOptimalTime());
            }
        });

         */
        dataBatchQueue=new LinkedBlockingQueue<>();
        finished=false;
        integer=new AtomicInteger(0);
        isTerminated=false;
    }
    /**
     * @post : @post(getTime)=@pre(getTime)+1
     */
    public void increaseTick(){
        time++;
        //System.out.println(Thread.currentThread().getName()+" has "+dataBatchQueue.size());
        if(!isTerminated){
            /*
            if(dataBatchQueue.isEmpty()){
                cluster.giveCPUJobs(this);
            }*/
            if(processingData==null||finished) {
                synchronized (this) {
                    while (dataBatchQueue.isEmpty()&&!isTerminated) {
                        try {
                            wait();
                        } catch (InterruptedException ignore) {
                        }
                    }
                    DataBatch dataBatch = dataBatchQueue.poll();
                    if (dataBatch.getStart_index() == -1) {
                        isTerminated = true;
                        notifyAll();
                    } else {
                                    /*
                if (dataBatch.getData().getModel().getName().equals("YOLO9000"))
                    System.out.println(Thread.currentThread().getName() + " is Processing YOLO9000's databatch at " + dataBatch.getStart_index() + " in time " + time +
                            "\n expected time " + time + calculateTime(Data.Type.Text));*/
                        notifyAll();
                        ProcessData(dataBatch);
                    }
                }
            }
            else{
                updateProcess();
            }
        }
    }

    private void updateProcess() {
        if(time==ReleaseTime){
            //System.out.println(Thread.currentThread().getName()+" is in updateProcess");

            /*if(processingData.getData().getModel().getName().equals("YOLO9000"))
                System.out.println(Thread.currentThread().getName() + " finished Processing Bart's databatch at "+processingData.getStart_index()+" in time "+time);
            */
            integer.decrementAndGet();
            ReleaseTime=0;
            processingData.getData().incrementProcessed();
            cluster.sendBacktoGPU(processingData);
            Statistics.getInstance().incrementProcessed();
            finished=true;
        }
    }
    public void getUnprocessedData(Queue<DataBatch> queue){
        //System.out.println(Thread.currentThread().getName()+" is in getUnprocessedData");
        synchronized (this){
            dataBatchQueue.addAll(queue);
            notifyAll();
        }
        integer.addAndGet(queue.size());
    }
    /**
     *
     * @param dataBatch the dataBatch needed to be processed
     * @pre dataBatchQueue!=null
     * @pre : @pre(getDataBatchQueue)==null
     * @post @post(getDataBatchQueue)!=null
     */
    private void ProcessData(DataBatch dataBatch){
        //System.out.println(Thread.currentThread().getName()+" is in ProcessData");
        processingData=dataBatch;
        ReleaseTime=time+calculateTime(dataBatch.getData().getType());
        finished=false;
    }
    public HashMap<Data.Type,Integer> TimeTable(){
        HashMap<Data.Type,Integer> arrayList=new HashMap<>(3);
        arrayList.put(Data.Type.Images,calculateTime(Data.Type.Images));
        arrayList.put(Data.Type.Text,calculateTime(Data.Type.Text));
        arrayList.put(Data.Type.Tabular,calculateTime(Data.Type.Tabular));
        return arrayList;
    }
    public int calculateTime(Data.Type type){
        if(type== Data.Type.Images)
            return (32*4)/cores;
        else if(type== Data.Type.Text)
            return (32*2)/cores;
        else
            return 32/cores;
    }
    /**
     * @pre @pre(getDataBatchQueue)!=null
     * @post : @post(getDataBatchQueue())==null
     */
    public void sendProcessedData(){}

    public int getTime(){
        return time;
    }
    public int getDataBatchesSize(){
        return integer.get();
    }
    public int getCores() {
        return cores;
    }
    public void Terminate(){
        isTerminated=true;
        this.notifyAll();
    }

    public boolean isTerminated() {
        return isTerminated;
    }
    /*
    public void updateQueue(int Tick) {
        synchronized (dataBatchQueue){
            cluster.getScheduler().reScheduleQueue(Tick,dataBatchQueue);
            dataBatchQueue.notifyAll();
        }
    }

 */
}
