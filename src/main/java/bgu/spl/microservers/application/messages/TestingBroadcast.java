package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.services.StudentService;

public class TestingBroadcast implements Broadcast {
    private final TrainModelEvent event;
    private final String studentName;

    public TestingBroadcast(TrainModelEvent event, String instance) {
        this.event=event;
        this.studentName =instance;

    }

    public TrainModelEvent getEvent() {
        return event;
    }

    public String getStudentName() {
        return studentName;
    }
}
