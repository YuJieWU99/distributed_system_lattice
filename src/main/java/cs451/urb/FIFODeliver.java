package cs451.urb;

import cs451.CreateFile;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FIFODeliver extends Thread {
    ArrayList<Integer> processPorts = new ArrayList<>();
    ArrayList<String> deliver = new ArrayList<>();

    public FIFODeliver(ArrayList<Integer> processPorts, ArrayList<String> deliver) {
        this.processPorts = processPorts;
        this.deliver = deliver;
    }

    public void deliverMessage(ArrayList<Integer> processPorts, ArrayList<String> deliver, Map<Integer, Integer> deliverLog, ArrayList<String> delivered) {
        //System.out.println(deliver);
        for (int i = 1; i < processPorts.size() + 1; i++) {
            int seq = deliverLog.get(i);
            String de = seq + "." + i;
            if (deliver.contains(de) && !delivered.contains(de)) {
                CreateFile.getInstance().addLogBuffer("d " + i + " " + seq + "\n");
                deliverLog.put(i, seq + 1);
                delivered.add(de);
            }
        }
    }

    public void run(){
        //"deliverlog" records the current delivered message sequence
        Map<Integer, Integer> deliverLog = new ConcurrentHashMap<>();
//        LocalDateTime time1 = LocalDateTime.now();
        //"delivered" records the delivered messageSequence.originalSender
        ArrayList<String> delivered = new ArrayList<>();
        for(int j = 1; j < processPorts.size()+1; j++){
            deliverLog.put(j, 1);
        }
//        int timer = 1;
        while(true) {
            //System.out.println("system is listening");
            deliverMessage(processPorts, deliver, deliverLog, delivered);
//            int timeCounter = 1;
//            if (delivered.size() == 9000 && timer==timeCounter){
//                timer = timer +1;
//                LocalDateTime time2 = LocalDateTime.now();
//                System.out.println(Duration.between(time1, time2).toMillis());
//            }
        }
    }
}
