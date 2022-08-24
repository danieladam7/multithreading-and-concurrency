package bgu.spl.mics.JSONOBJECTS;


import java.util.List;

import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Student;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class MainJsonObject {

    @SerializedName("Students")
    @Expose
    private List<Student> students = null;
    @SerializedName("GPUS")
    @Expose
    private List<String> gpus = null;
    @SerializedName("CPUS")
    @Expose
    private List<Integer> cpus = null;
    @SerializedName("Conferences")
    @Expose
    private List<ConfrenceInformation> conferences = null;
    @SerializedName("TickTime")
    @Expose
    private int tickTime;
    @SerializedName("Duration")
    @Expose
    private int duration;

    public MainJsonObject(List<Student> students, List<String> gpus, List<Integer> cpus, List<ConfrenceInformation> conferences, int tickTime, int duration) {
        this.students = students;
        this.gpus = gpus;
        this.cpus = cpus;
        this.conferences = conferences;
        this.tickTime = tickTime;
        this.duration = duration;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<String> getGpus() {
        return gpus;
    }

    public void setGpus(List<String> gpus) {
        this.gpus = gpus;
    }

    public List<Integer> getCpus() {
        return cpus;
    }

    public void setCpus(List<Integer> cpus) {
        this.cpus = cpus;
    }

    public List<ConfrenceInformation> getConferences() {
        return conferences;
    }

    public void setConferences(List<ConfrenceInformation> conferences) {
        this.conferences = conferences;
    }

    public int getTickTime() {
        return tickTime;
    }

    public void setTickTime(int tickTime) {
        this.tickTime = tickTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
