package bgu.spl.mics.application.objects;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {

    private static class StatisticsHolder{
        private static final Statistics instance=new Statistics();
    }
    public static Statistics getInstance(){
        return StatisticsHolder.instance;
    }

    private AtomicInteger batchesProcessed,cpuTime,gpuTime;
    private LinkedBlockingQueue<Student> students;
    private LinkedBlockingQueue<Model> trainedModels;

    public Statistics() {
        batchesProcessed=new AtomicInteger(0);
        cpuTime=new AtomicInteger(0);
        gpuTime=new AtomicInteger(0);
        students=new LinkedBlockingQueue<>();
        trainedModels=new LinkedBlockingQueue<>();
    }
    public void incrementProcessed(){
        batchesProcessed.incrementAndGet();
    }
    public void addcpuTime(int time){
        cpuTime.addAndGet(time-cpuTime.get());
    }
    public void addgpuTime(int time){
        gpuTime.addAndGet(time-gpuTime.get());
    }
    public void addStudents(Student student){
        students.add(student);
    }
    public void addTrainedModels(Model model){
        trainedModels.add(model);
    }
}
