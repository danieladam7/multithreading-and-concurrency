package bgu.spl.mics.application.objects;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {
    public enum status {PreTrained , Training , Trained , Tested}
    public enum results {None , Good , Bad}

    @SerializedName("name")
    @Expose
    private final String name;
    @SerializedName("type")
    @Expose
    private final String type;
    @SerializedName("size")
    @Expose
    private final int size;


    private Data data;
    private Student student;
    private status currStatus;
    private results result;

    public Model(String name,String type, int size){
        this.name=name;
        Data.Type type1=null;
        this.type=type;
        this.size=size;
        if(type.equals("Images"))
            type1= Data.Type.Images;
        else if (type.equals("Text"))
            type1= Data.Type.Text;
        else
            type1= Data.Type.Tabular;
        this.data=new Data(type1,size);
        this.currStatus= Model.status.PreTrained;
        result=results.None;
    }
    public String getName() {
        return name;
    }

    public Data getData() {
        return data;
    }

    public status getCurrStatus() {
        return currStatus;
    }

    public void setCurrStatus(status currStatus) {
        this.currStatus = currStatus;
    }

    public results getResult() {
        return result;
    }

    public boolean isGood(){return result==results.Good;}

    public void setResult(results result) {
        this.result = result;
    }

    public int getSize() {
        return size;
    }
    public void setData(){
        Data.Type type1;
        if(type.equals("Images"))
            type1= Data.Type.Images;
        else if (type.equals("Text"))
            type1= Data.Type.Text;
        else
            type1= Data.Type.Tabular;
        data=new Data(type1,size);
    }

    @Override
    public String toString() {
        return "Model{" +
                "name='" + name + '\'' +
                '}';
    }

}
