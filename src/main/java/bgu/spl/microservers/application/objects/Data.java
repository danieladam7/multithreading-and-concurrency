package bgu.spl.mics.application.objects;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {


    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }
    private final Type type;
    private AtomicInteger processed;
    private final int size;
    private AtomicInteger Trained;
    private AtomicInteger optimalProcessingTime;
    private AtomicInteger insertedTick;
    private AtomicInteger WaitingTime;

    private Model model;
    public Data(Type type1, int size){
        type=type1;
        this.size=size;
        processed=new AtomicInteger(0);
        Trained=new AtomicInteger(0);
        optimalProcessingTime=new AtomicInteger();
        insertedTick=new AtomicInteger();
        WaitingTime=new AtomicInteger(0);
    }

    public void setInsertedTick(int insertedTick) {
        this.insertedTick.set(insertedTick);
    }

    public void setOptimalProcessingTime(int optimalProcessingTime) {
        this.optimalProcessingTime.set(optimalProcessingTime);
    }

    public int getOptimalProcessingTime() {
        return optimalProcessingTime.get();
    }

    public int getProcessed() {
        return processed.get();
    }

    public int incrementProcessed(){
        return processed.incrementAndGet();
    }

    public int getTrained() {
        return Trained.get();
    }

    public int incrementTrained(){
        return Trained.incrementAndGet();
    }

    public void setTrained(AtomicInteger trained) {
        Trained = trained;
    }

    public int getWaitingTime() {
        return WaitingTime.get();
    }

    public int setWaitingTime(int waitingTime) {
        return WaitingTime.addAndGet(waitingTime-insertedTick.get());
    }

    public long getSize() {
        return size;
    }

    public Type getType() {
        return type;
    }
    public void setModel(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }
}
