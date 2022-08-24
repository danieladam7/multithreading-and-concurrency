package bgu.spl.mics.application.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }
    @SerializedName("name")
    @Expose
    private final String name;
    @SerializedName("department")
    @Expose
    private final String department;
    @SerializedName("status")
    @Expose
    private final Degree status;
    private int publications;
    private int papersRead;
    @SerializedName("models")
    @Expose
    private final Model[] Models;
    /**
     *
     * @param name - The name of the Student
     * @param department - The student's department
     * @param status - The student's Status
     * @param models - Student's models.
     */
    public Student(String name, String department, Degree status,Model[] models) {
        this.name = name;
        this.department = department;
        this.status = status;
        this.publications = 0;
        this.papersRead = 0;
        this.Models=models;
        Arrays.sort(models, Comparator.comparingInt(Model::getSize));
    }

    public String getName() {
        return name;
    }


    public String getDepartment() {
        return department;
    }

    public Degree getStatus() {
        return status;
    }

    public int getPublications() {
        return publications;
    }

    public void setPublications(int publications) {
        this.publications = publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public void setPapersRead(int papersRead) {
        this.papersRead = papersRead;
    }

    public Model[] getModels() {
        return Models;
    }
}
