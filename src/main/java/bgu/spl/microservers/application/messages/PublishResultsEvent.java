package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.ArrayList;

public class PublishResultsEvent implements Event<Integer> {
    private final Model model;
    private final Student student;

    public PublishResultsEvent(Model model, Student student) {
        this.model = model;
        this.student = student;
    }

    public Model getModel() {
        return model;
    }

    public Student getStudent() {
        return student;
    }
}
