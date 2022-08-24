package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TestModelEvent implements Event<Boolean>{
    final private Model TestModel;
    private int TestFuturesIndex;
    private boolean isGood;
    private volatile boolean timeOut;
    public Student getStudent() {
        return student;
    }

    private final Student student;
    public Model getTestModel() {
        return TestModel;
    }

    public int getTestFuturesIndex() {
        return TestFuturesIndex;
    }

    public boolean isGood() {
        return isGood;
    }

    public void setGood(boolean good) {
        isGood = good;
    }

    public void setTestFuturesIndex(int testFuturesIndex) {
        TestFuturesIndex = testFuturesIndex;
    }
    public void timeOUT(){
        timeOut=true;
    }

    public boolean isTimeOut() {
        return timeOut;
    }
    public TestModelEvent(Model testModel,Student student1) {
        TestModel = testModel;
        TestFuturesIndex = -1;
        isGood=false;
        student=student1;
        timeOut=false;
    }
}
