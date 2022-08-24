package bgu.spl.mics.application.objects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Scheduler {
    private ConcurrentSkipListMap<Data,Integer> dataQueue;
    private final PriorityBlockingQueue<CPU> CPUs; //mapping all the cpus by number of cores
    //private final ConcurrentHashMap<CPU>
    private final ConcurrentHashMap<CPU,HashMap<Data,Integer>> CPUPriority;
    private final int numBestCPUs;
    private final int bestCPUCores;
    private int currTick;
    private final ReentrantReadWriteLock readWriteLock=new ReentrantReadWriteLock(true);
    private final Object cpuLock=new Object();
    /**
     * The Scheduler algorithm:
     * Insert all the jobs, which are the data that need to get processed, to an hashtable which is implemented with RedBlackTree.
     * Defenitions:
     * @param bestCPUCores the maximum number of cores a cpu can have.
     * @param numBestCPUs number of the cpus with the bestCPUCores.
     * OptimalTime: the time which would take to process all the data's DataBatches if only the best CPUs would handle the processing
     * OptimalBatchTime: the time that would take to process a single Batch in the bestCPU.
     *
     * when a cpu finished handling a dataBatch, the Scheduler will give it the next data batch in its queue.
     * if the queue is empty, the Scheduler will fill the queue with DataBatches in fair-manner.
     */


    public Scheduler(int numBestCPUs, int bestCPUCores, Collection<CPU> cpuCollection){
        this.numBestCPUs=numBestCPUs;
        this.bestCPUCores = bestCPUCores;
        CPUs=new PriorityBlockingQueue<>(cpuCollection.size(), (cpu, t1) -> Integer.compare(t1.getCores(),cpu.getCores()));
        CPUs.addAll(cpuCollection);
        CPUPriority =new ConcurrentHashMap<>(cpuCollection.size());
        currTick=0;
        for (CPU cpu: CPUs){
            CPUPriority.put(cpu,new HashMap<>());
        }
        dataQueue=new ConcurrentSkipListMap<>((data, t1) -> {
            if(data.getWaitingTime()!=0) {
                if (t1.getWaitingTime() != 0) {
                    return Integer.compare(data.getWaitingTime(),t1.getWaitingTime());
                }
                return data.getWaitingTime();
            }
            else if(t1.getWaitingTime()!=0)
                return t1.getWaitingTime();
            else
                return Integer.compare(data.getOptimalProcessingTime(),t1.getOptimalProcessingTime());
        });
    }
    /*
        //reschedule every data
        public void schedule(int currentTick) {
            currTick = currentTick;
            readWriteLock.writeLock().lock();
            try {
                System.out.println("scheduler is scheduling right now");
                //System.out.println("Scheduler is in schedule");
                for (Data data : dataQueue.keySet()) {
                    if(data!=null){
                        if(dataQueue.containsKey(data)){
                            if(dataQueue.get(data)==0){
                                dataQueue.remove(data);
                            }
                            else {
                                data.setWaitingTime(currentTick);
                                dataQueue.put(data, dataQueue.remove(data));
                            }
                        }
                    }
                }
            }finally {
                readWriteLock.writeLock().unlock();
            }
        }

        public void RegisterData(Data data,int num){
            readWriteLock.writeLock().lock();
            try {
                data.setOptimalProcessingTime(calcOptimalData(data,num));
                dataQueue.put(data,num);
            }finally {
                readWriteLock.writeLock().unlock();
            }
        }

            //inserting the updated list.
            //no more data to Process
            public void RemoveData(Data data){
                readWriteLock.writeLock().lock();
                try {
                    //System.out.println(Thread.currentThread().getName()+" is in RemoveData");
                    //System.out.println("Leftovers: "+Cluster.getInstance().getProcessingData().get(data));
                    dataQueue.remove(data);
                }finally {
                    readWriteLock.writeLock().unlock();
                }
            }
            public ConcurrentHashMap<Data,Integer> sendPriority(CPU cpu) {
                readWriteLock.readLock().lock();
                readWriteLock.readLock().unlock();
                readWriteLock.writeLock().lock();
                ConcurrentHashMap<Data, Integer> dataIntegerHashMap=new ConcurrentHashMap<>();
                if (cpu.getCores() == bestCPUCores) {
                    int numberToSend = 0;
                    int dataBatchNumber = dataQueue.get(dataQueue.firstKey());
                    if (dataBatchNumber >= 20) {
                        numberToSend = dataBatchNumber;
                    } else {
                        numberToSend = dataBatchNumber;
                    }
                    if (dataBatchNumber == 0) {
                        dataQueue.remove(dataQueue.firstKey());
                    }
                    dataIntegerHashMap.put(dataQueue.firstKey(), numberToSend);
                } else {
                    for (Data data : dataQueue.keySet()) {
                        if (dataQueue.get(data) != 0) {
                            int n = 1;
                            if (dataQueue.get(data) > 1)
                                n = 2;
                            dataQueue.put(data,dataQueue.get(data)-n);
                            dataIntegerHashMap.put(data, n);
                        }
                        else{
                            dataQueue.remove(data);
                        }
                    }
                }
                readWriteLock.writeLock().unlock();
                return dataIntegerHashMap;
            }
            */
    public ConcurrentHashMap<CPU,Integer> setPriority(PriorityBlockingQueue<CPU> cpus,int numUnprocessed) {
        ConcurrentHashMap<CPU, Integer> cpuIntegerHashMap=new ConcurrentHashMap<>();
        int n=cpus.size();
        for(int i=0;i<n;i++){
            CPU cpu=cpus.poll();
            if(cpu.getCores()==bestCPUCores){
                numUnprocessed-=numUnprocessed/numBestCPUs*numBestCPUs;
                cpuIntegerHashMap.put(cpu,numUnprocessed);
            }
            else{
                numUnprocessed-=(numUnprocessed*(numBestCPUs*numBestCPUs-1))/(numBestCPUs*(n-numBestCPUs));
                cpuIntegerHashMap.put(cpu,numUnprocessed);
            }
            System.out.println(cpuIntegerHashMap.toString());
            cpus.add(cpu);
        }
        return cpuIntegerHashMap;
    }


    public int calcOptimalBatchTime(DataBatch dataBatch){
        int output=-1;
        switch (dataBatch.getData().getType()){
            case Images:
                output=128/ bestCPUCores;
                break;
            case Text:
                output=64/ bestCPUCores;
                break;
            case Tabular:
                output=32/ bestCPUCores;
                break;
        }
        return output;
    }
    public int calcOptimalData(Data data,int num){
        int optimalTime=-1;
        //the optimal time is the time of the best scenario: only the best cpus works on the data.
        switch (data.getType()){
            case Images:
                optimalTime=(128* num*numBestCPUs)/ bestCPUCores;
                break;
            case Text:
                optimalTime=(64* num*numBestCPUs)/ bestCPUCores;
                break;
            case Tabular:
                optimalTime=(32* num*numBestCPUs)/ bestCPUCores;
                break;
        }
        return optimalTime;
    }
    public ConcurrentSkipListMap<Data,Integer> getData() {
        return dataQueue;
    }

    public void terminate() {
        dataQueue.clear();
        CPUs.clear();
    }

    public int getBestCPUCores() {
        return bestCPUCores;
    }
/*
    public void reScheduleQueue(int TickTime,PriorityBlockingQueue<DataBatch> dataBatchQueue) {
        int n=dataBatchQueue.size();
        while(n!=0){
            DataBatch dataBatch=dataBatchQueue.poll();
            dataBatch.incrementWaitingTime(TickTime-dataBatch.getInsertedTick());
            dataBatchQueue.add(dataBatch);
            n--;
        }
    }

 */
}
