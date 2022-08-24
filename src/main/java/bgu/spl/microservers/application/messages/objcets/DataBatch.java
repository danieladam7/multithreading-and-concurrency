package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {

    private final Data data;
    private final int start_index;
    private int insertedTick;

    public DataBatch(Data otherData,int index,int insertedTick){
        data=otherData;
        start_index=index;
        this.insertedTick=insertedTick;
    }

    @Override
    public String toString() {
        return "DataBatch {" +
                " start_index= " + start_index +
                '}';
    }

    public Data getData() {
        return data;
    }


    public int getStart_index() {
        return start_index/1000;
    }

    public int getInsertedTick() {
        return insertedTick;
    }

    public void setInsertedTick(int insertedTick) {
        this.insertedTick = insertedTick;
    }
}
