package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.services.GPUService;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    public void setService(GPUService service) {
        this.service = service;
    }

    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}
    private final Type type;
    private Model model;
    private final Cluster cluster=Cluster.getInstance();
    private volatile DataBatch TrainingBatch;
    private int currTick,TrainingTime,numUnprocessed,numTrained;
    private final LinkedBlockingQueue<DataBatch> TrainingBatches;
    private final Queue<DataBatch> UnprocessedData;
    private final int vramSize,waitTime;
    private final String name;
    private volatile boolean finishedTraining,isTraining,isCompleted;
    private boolean  isTerminated;
    private final AtomicInteger integer;
    private GPUService service;
    // constructor
    public GPU (String type){
        integer=new AtomicInteger(0);
        if(type.equals("RTX3090")) {
            this.type=Type.RTX3090;
            vramSize = 32;
            waitTime=1;
        }
        else if(type.equals("RTX2080")) {
            vramSize = 16;
            this.type=Type.RTX2080;
            waitTime=2;
        }
        else {
            vramSize = 8;
            this.type=Type.GTX1080;
            waitTime=4;
        }

        TrainingBatches =new LinkedBlockingQueue<>(vramSize);
        model=null;
        isCompleted = false;
        isTraining=false;
        TrainingTime=0;
        currTick=0;
        UnprocessedData=new LinkedBlockingQueue<>();
        name=type;
        finishedTraining=false;
        //finalBatch=false;
        numTrained=0;
        isTerminated=false;
    }

    /**
     *   get Batches from Data in Model
     *   @pre  testObject.getDataBatchQueue().getSize() = size of Data
     *         testArray.size() = 0
     *   @post  testObject.getDataBatchQueue().getSize() == testArray.size());
     */
    public void updateTick() {
        currTick++;
        if(isTraining){
            if (numTrained==numUnprocessed/*model.getData().getTrained() == numUnprocessed*/) {

                /*if(model.getName().equals("YOLO9000")){
                    System.out.println("entered finished at "+currTick+"while "+model.getData().getTrained()+" and "+model.getData().getProcessed());
                }*/

                //System.out.println("*********" + name + " finished " + model.getName());
                System.out.println("*********\n"+Thread.currentThread().getName()+" finished training a model"+"\n ************");
                cluster.FinishedTraining(model.getData());
                //finalBatch=false;
                //model=null;
                isCompleted = true;
                isTraining=false;
                TrainingTime=0;
                currTick=0;
                finishedTraining=false;
                numTrained=0;
                integer.set(0);
            } else if (TrainingBatch==null ||  finishedTraining) {
                synchronized (TrainingBatches) {
                    while (TrainingBatches.isEmpty()) {
                        try {
                            TrainingBatches.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    DataBatch dataBatch = TrainingBatches.poll();
                    /*if (dataBatch.getData().getModel().getName().equals("YOLO9000")) {
                        System.out.println("Training Bert's dataBatch at" + dataBatch.getStart_index() + "expected finish time " + (currTick + waitTime));
                    }*/
                    TrainBatch(dataBatch);
                    TrainingBatches.notifyAll();
                }
            } else
                UpdateTraining();
        }
        else
            service.nextJob();
    }

    private void UpdateTraining() {
        if(currTick==TrainingTime){

            /*if(model.getName().equals("YOLO9000")) {
                System.out.println("Finished Training Bert's dataBatch at" + TrainingBatch.getStart_index() + "finished at finish time " + currTick);
            }*/
            integer.decrementAndGet();
            TrainingTime=0;
            finishedTraining=true;
            numTrained++;
            model.getData().incrementTrained();
            if(numTrained==numUnprocessed/*model.getData().getTrained() == numUnprocessed*/){

                /*if(model.getName().equals("YOLO9000")){
                    System.out.println("entered finished at "+currTick+"while "+model.getData().getTrained()+" and "+model.getData().getProcessed());
                }*/
                cluster.FinishedTraining(model.getData());
                System.out.println("*********\n"+Thread.currentThread().getName()+" finished training a batch"+"\n ************");
                isCompleted = true;
                isTraining=false;
                TrainingTime=0;
                currTick=0;
                finishedTraining=false;
                //finalBatch=false;
                //isTerminated=false;
                numTrained=0;
                //model=null;
                integer.set(0);
            }
        }
    }

    private void TrainBatch(DataBatch dataBatch) {
        TrainingBatch=dataBatch;
        TrainingTime=currTick+waitTime;
        finishedTraining=false;
    }
    public void trainModel(Model model,int insertedTick) {

        System.out.println("*********\n"+Thread.currentThread().getName()+" started working on"+model.getName()+" at Tick "+currTick+"\n ************");
        isTraining=true;
        isCompleted=false;
        this.model=model;
        model.setCurrStatus(Model.status.Training);
        Data data=model.getData();
        numUnprocessed = (int) data.getSize() / 1000;
        for(int i=0;i<numUnprocessed;i++){
            UnprocessedData.add(new DataBatch(data,i*1000,insertedTick));
            /*if (model.getName().equals("Bert"))
                System.out.println(i*1000);*/
        }
        //System.out.println(name+"is Sending "+numUnprocessed+" Unprocessed Data");
        System.out.println("*********\n"+Thread.currentThread().getName()+" is registering the data of"+model.getName()+" at Tick "+currTick+"\n ************");
        cluster.InitialRegister(data,this);
        sendUnprocessedBatches();
    }

    /**
     * @param model
     * @pre: model == null
     * @post: model != null
     */
    public void setModel(Model model){
        this.model=model;
    }

    /**
     *
     * @return data of the field
     * @pre data = data
     * @post data = data
     */
    public Model getModel() {
        return model;
    }

/**
 * @pre:  isEmpty(ArrayList<DataBatch> testArray) = false
 * @post: isEmpty(ArrayList<DataBatch> testArray) = true
*/
    public void sendUnprocessedBatches(){
        Queue<DataBatch> dataBatches=new LinkedBlockingQueue<>();
        for(int i=0;i<vramSize && !UnprocessedData.isEmpty();i++){
            dataBatches.add(UnprocessedData.poll());
        }

        /*if(model.getName().equals("YOLO9000")) {
            System.out.println("sending " + dataBatches.size()+ "batches to the cluster at" + currTick);
        }*/
        cluster.sendUnprocessedData(model.getData(),dataBatches);
    }

    /**
     * @pre: checkStatus(testObject) = false
     * @post: checkStatus(testObject) = true
     *
     */
    public void getProcessedBatches(DataBatch dataBatch){
        /*
        if(model.getName().equals("YOLO9000")) {
            System.out.println("got Processed data of" + dataBatch.getData().getModel().getName() +"at index"+dataBatch.getStart_index()+"at time" + currTick);
            System.out.println("TrainingBatches before:\n"+TrainingBatches.toString());
        }*/
        /*
        if(dataBatch.getStart_index()==numUnprocessed-1){
            lastBatch=dataBatch;
            finalBatch=true;
        }else{

         */
        synchronized (TrainingBatches) {
            TrainingBatches.add(dataBatch);
            TrainingBatches.notifyAll();
        }
        integer.incrementAndGet();
        /*if(model.getName().equals("YOLO9000")) {
            System.out.println("TrainingBatches after:\n"+TrainingBatches.toString());
        }*/
        if (!UnprocessedData.isEmpty()) {
            DataBatch dataBatch1=UnprocessedData.poll();
           /*if(model.getName().equals("YOLO9000")) {
                System.out.println("sending to the cluster databatch "+dataBatch1.getStart_index());
            }*/
            cluster.sendToCPU(model.getData(),dataBatch1);
        }
    }
    /**
     *
     * @return field isCompleted
     * @pre isCompleted = isCompleted
     * @post isCompleted = isCompleted
     */
    public boolean checkStatus (){
        return isCompleted;
    }

    public Boolean testModel(Model testModel,Student student) {
        boolean result=false;
        Random random=new Random();
        int probability= random.nextInt(10);
        if(probability<=6 && student.getStatus()== Student.Degree.MSc)
            result=true;
        else if(probability<=8 && student.getStatus() == Student.Degree.PhD)
            result=true;

        System.out.println("*********\n"+name+" finished Testing"+testModel.getName()+"\n ************");

        return result;
    }
    /**
     *
     * @return ArrayList batches of field
     * @pre banches = banches
     * @post banches = banches
     */
    public Type getType() {
        return type;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isTraining() {
        return isTraining;
    }

    public void setTraining(boolean training) {
        isTraining = training;
    }

    public DataBatch getTrainingBatch() {
        return TrainingBatch;
    }

    public void setTrainingBatch(DataBatch trainingBatch) {
        TrainingBatch = trainingBatch;
    }

    public int getTrainingTime() {
        return TrainingTime;
    }

    public void setTrainingTime(int trainingTime) {
        TrainingTime = trainingTime;
    }

    public int getCurrTick() {
        return currTick;
    }

    public void setCurrTick(int currTick) {
        this.currTick = currTick;
    }

    public Queue<DataBatch> getTrainingBatches() {
        return TrainingBatches;
    }
    public int numTrainingBatch(){
        return integer.get();
    }

    public Queue<DataBatch> getUnprocessedData() {
        return UnprocessedData;
    }

    public void setUnprocessedData(Queue<DataBatch> unprocessedData) {
        UnprocessedData.addAll(unprocessedData);
    }

    public int getVramSize() {
        return vramSize;
    }
    public String getName() {
        return name;
    }
    public void Terminate(){
        isTerminated=true;
    }
}
