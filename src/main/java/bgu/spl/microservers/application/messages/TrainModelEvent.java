package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TrainModelEvent implements Event<Model> {
    private final Model model;
    private final Student student;
    private volatile boolean shouldTerminate;
    public TrainModelEvent(Model model, Student student) {
        this.model = model;
        this.student = student;
        shouldTerminate=false;

    }
    public Model getModel() {
        return model;
    }
    public Student getStudent() {
        return student;
    }
    public void terminate(){
        shouldTerminate=true;
    }

    public boolean isShouldTerminate() {
        return shouldTerminate;
    }
}
