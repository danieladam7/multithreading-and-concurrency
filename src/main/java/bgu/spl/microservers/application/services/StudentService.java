package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
    final private Student student;
    final private int Duration;
    private int currTime;
    private HashMap<TrainModelEvent,Future<Model>> TrainFutures;
    private HashMap<TestModelEvent,Future<Boolean>> TestFutures;
    /*TODO:
     * 1. Decide how to get all the data from the MessageBus.
     * 2. Implement CallBacks
     * 3. Implement Student
     * 4. Read about the Service further
     */
    public StudentService(String name, Student student, int duration) {
        super(name);
        this.student = student;
        Duration = duration;
        this.currTime = 0;
        TrainFutures=new HashMap<>();
        TestFutures=new HashMap<>();
    }
    @Override
    protected void initialize() {
        subscribeBroadcast(TerminateBroadcast.class, (b) -> {
            terminate();
        });
        subscribeBroadcast(PublishConferenceBroadcast.class, (b) -> {
            Collection<Model> collection = b.getPublished();
            Model[] models = student.getModels();
            int publications = 0;
            for (Model model : models) {
                if (collection.contains(model)) {
                    publications++;
                }
            }
            if (publications != 0) {
                student.setPublications(student.getPublications() + publications);
                student.setPapersRead(student.getPapersRead() + 1);
            }

        });
        boolean notTerminated=true;
        Model[] models=student.getModels();
        for (int i=0;i< models.length&&notTerminated;i++) {
            TrainModelEvent trainModelEvent = new TrainModelEvent(models[i], student);
            if (student.getName().equals("Nala"))
                System.out.println("************\n" + "Nala is sending model to train " + models[i].getName() + "\n ************");
            models[i].getData().setModel(models[i]);
            Future<Model> future = sendEvent(trainModelEvent);
            Model model1 =future.get();
            if(trainModelEvent.isShouldTerminate()||model1==null) {
                System.out.println(student.getName() + " is exiting");
                terminate();
                notTerminated=false;
            }else {
                if (student.getName().equals("Nala"))
                    System.out.println("************\n" + "Nala is sending model to get tested " + models[i].getName() + "\n ************");
                //System.out.println("*********\n"+student.getName()+" got trained model "+future.get().getName()+"\n ************");
                TestModelEvent testModelEvent=new TestModelEvent(models[i], student);
                Future<Boolean> future1= sendEvent(testModelEvent);
                future1.get();
                if(testModelEvent.isTimeOut()){
                    terminate();
                    notTerminated=false;
                }else {
                    boolean isGood=future1.get();
                    //System.out.println("*********\n"+student.getName()+" got tested model "+model.getName()+"\n ************");
                    //System.out.println("Student "+student.getName()+"got Tested model"+model.getName());
                    if (student.getName().equals("Nala"))
                        System.out.println("************\n" + "Nala's model " + models[i].getName() + "got tested and " + isGood + "\n ************");

                    models[i].setCurrStatus(Model.status.Tested);
                    if(isGood) {
                        models[i].setResult(Model.results.Good);
                        if (student.getName().equals("Nala"))
                            System.out.println("************\n" + "Nala is sending model to get published " + models[i].getName() + "\n ************");
                        Integer result = sendEvent(new PublishResultsEvent(models[i], student)).get();
                        if (result == null) {
                            terminate();
                            notTerminated=false;
                        } else
                            student.setPapersRead(student.getPapersRead() + result);
                    }
                }
            }
        }
    }
}
