package bgu.spl.mics.application;
import bgu.spl.mics.JSONOBJECTS.*;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {
        Gson gson=new Gson();
        BufferedReader bufferedReader=null;
        MainJsonObject mainJsonObject=null;
        String input=args[0],output=args[1];
        try{
            bufferedReader=new BufferedReader(new FileReader(input));
            mainJsonObject=gson.fromJson(bufferedReader,MainJsonObject.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int index=0;
        List<MicroService> microServices=new ArrayList<>();
        List<String> stringList=mainJsonObject.getGpus();
        List<GPU> gpus=new ArrayList<>(stringList.size());

        for(String gpu:stringList){
            GPU gpu1=new GPU(gpu);
            gpus.add(gpu1);
            microServices.add(new GPUService("GPU"+index, mainJsonObject.getDuration(), gpu1));
            index++;
        }

        /*
        GPU gpu=new GPU("RTX3090");
        GPU gpu1=new GPU("RTX3090");
        gpus.add(gpu);gpus.add(gpu1);
        microServices.add(new GPUService("GPU Test", mainJsonObject.getDuration(),gpu));
        microServices.add(new GPUService("GPU Test 1", mainJsonObject.getDuration(),gpu1));

         */
        PriorityQueue<CPU> cpuPriorityQueue=new PriorityQueue<>(new Comparator<CPU>() {
            @Override
            public int compare(CPU cpu, CPU t1) {
                return Integer.compare(t1.getCores(), cpu.getCores());
            }
        });

        for(Integer integer: mainJsonObject.getCpus()){
            cpuPriorityQueue.add(new CPU(integer));
        }

        CPU cpu=cpuPriorityQueue.poll();
        cpuPriorityQueue.add(cpu);
        int maxCores=cpu.getCores(),cpuCounter=0;
        index=0;
        List<CPU> cpus=new ArrayList<>(cpuPriorityQueue);

        while(!cpuPriorityQueue.isEmpty()){
            CPU cpu1=cpuPriorityQueue.poll();
            if(cpu1.getCores()==maxCores)
                cpuCounter++;
            microServices.add(new CPUService("CPU"+index, mainJsonObject.getDuration(), cpu1));
            index++;
        }


        Scheduler scheduler=new Scheduler(cpuCounter,maxCores,cpus);
        microServices.add(new SchedulerService("Scheduler",scheduler));

        for(Student student: mainJsonObject.getStudents()){
            for(Model model: student.getModels())
                model.setData();
            microServices.add(new StudentService(student.getName(),student, mainJsonObject.getDuration()));
        }

        for(ConfrenceInformation confrenceInformation: mainJsonObject.getConferences()){
            microServices.add(new ConferenceService(confrenceInformation.getName(),confrenceInformation));
        }

        microServices.add(new TimeService(mainJsonObject.getTickTime(), mainJsonObject.getDuration()));

        //System.out.println("hello!");
        ArrayList<Thread> threads=new ArrayList<>();


        Statistics.getInstance();
        MessageBusImpl.getInstance();
        Cluster.getInstance().SetCluster(gpus,cpus,scheduler);
        cpuWakeUp cpuWakeUp1=new cpuWakeUp("cpuWakeUp");
        microServices.add(cpuWakeUp1);
        ArrayList<Thread> threads1=new ArrayList<>();
        for (MicroService microService:microServices) {
            Thread thread1=new Thread(microService);
            if(microService instanceof CPUService){
                threads1.add(thread1);
            }
            thread1.setName(microService.getName());
            threads.add(thread1);
            thread1.start();
        }
        Thread[] help=new Thread[cpus.size()];
        threads1.toArray(help);
        cpuWakeUp1.setT(help);
        for(Thread thread:threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String json= gson.toJson(Statistics.getInstance());
        try {
            Files.write(Paths.get(output), Arrays.asList(json.split("\n")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
