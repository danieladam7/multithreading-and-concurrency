package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class GPUTest {

    GPU testObject;
    Cluster cluster;
    @Before
    public void setUp() throws Exception {
        cluster=Cluster.getInstance();
         testObject = new GPU("GTX1080");
    }

    @After
    public void tearDown() throws Exception {
        testObject = null;
        cluster=null;
    }


    @Test
    public void TestprepareUnprocessedBatches() {
        Data testData = new Data(Data.Type.Images, 10000);
        //testObject.setDataBatches(testData);
        //testObject.prepareUnprocessedBatches();
        //ArrayList<DataBatch> testArray = testObject.getBatches();
        //assertEquals(testObject.getDataBatchQueue().getSize(), testArray.size());
    }


    @Test
    public void TestSetData() {
        Data testData = new Data(Data.Type.Images, 10000);
        //testObject.setDataBatches(testData);
        //assertNotNull(testObject.getDataBatchQueue());
    }

    @Test
    public void TestcheckStatus(){
        Data testData = new Data(Data.Type.Images, 10000);
        //testObject.setDataBatches(testData);
        //assertNotNull(testObject.checkStatus());
    }


    @Test
    public void TestgetData() {
        GPU testObject = new GPU("RTX3090");
        Data testData = new Data(Data.Type.Text, 5000);
        //testObject.setDataBatches(testData);
        //assertEquals(testData, testObject.getDataBatchQueue());
    }



    @Test
    public void TestsendUnprocessedBatches(){
        Data testData = new Data(Data.Type.Images, 10000);
        //testObject.setDataBatches(testData);
        //testObject.prepareUnprocessedBatches();
        //ArrayList<DataBatch> testArray = testObject.getBatches();
        //assertTrue(testArray.isEmpty());
    }

    @Test
    public void TestgetBackprocessedBatches(){
        Data testData = new Data(Data.Type.Images, 10000);
        //testObject.setDataBatches(testData);
        assertFalse(testObject.checkStatus());
        //testObject.getBatches();
        //assertEquals(testObject.getBatches().size(), testData.getProcessed());
        //assertTrue(testObject.checkStatus());
    }


}