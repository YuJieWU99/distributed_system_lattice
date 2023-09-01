package cs451.lattice;

import cs451.CreateFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class Proposal extends Thread{
    private final int hostId;
    private final int proNum;
    private int proEleMaxNum;
    private int eleMaxNum;
    private Boolean active;
    private int ackCount;
    private int nackCount;
    private int activeProNum;
    Map<Integer, CopyOnWriteArraySet<Integer>> proValue = new ConcurrentHashMap<>();
    private final BufferedReader br;
    Map<Integer, CopyOnWriteArraySet<Short>> broList = new ConcurrentHashMap<>();
    ArrayList<Integer> processPorts = new ArrayList<>();
    Map<Short, CopyOnWriteArraySet<Short>> ack = new ConcurrentHashMap<>(); // 第几个process的第几个propose sequence要被acknowledge
    Map<Integer, CopyOnWriteArraySet<Short>> nack = new ConcurrentHashMap<>();

    public Proposal(int hostId, int proNum, int proEleMaxNum, int eleMaxNum, ArrayList<Integer> processPorts, BufferedReader br, Map<Integer, CopyOnWriteArraySet<Short>> broList,
                    Map<Short, CopyOnWriteArraySet<Short>> ack, Map<Integer, CopyOnWriteArraySet<Short>> nack, Map<Integer, CopyOnWriteArraySet<Integer>> proValue){
        this.hostId = hostId;
        this.proNum = proNum;
        this.proEleMaxNum = proEleMaxNum;
        this.eleMaxNum = eleMaxNum;
        this.processPorts = processPorts;
        this.br = br;
        this.broList = broList;
        this.ack = ack;
        this.nack = nack;
        this.proValue = proValue;
    }
    public void propose(int hostId, int proSeq, BufferedReader br, Map<Integer, CopyOnWriteArraySet<Integer>> proValue,
                        Map<Integer, CopyOnWriteArraySet<Short>> broList) throws IOException {
        String proSet = br.readLine();
        Integer proSequence = proSeq;
        active = true;

        //把string变成set
        Set<String> items = new HashSet<String>(Arrays.asList(proSet.split("\\s+")));
        Set<Integer> proposeSet = new HashSet<Integer>();
        for (String i : items){
            proposeSet.add(Integer.valueOf(i));
        }
        proValue.putIfAbsent(proSequence, new CopyOnWriteArraySet<>());
        proValue.get(proSequence).addAll(proposeSet); //先把自己的propose放进去
        broList.putIfAbsent(proSequence, new CopyOnWriteArraySet<>());  //生成一个已传消息process的名单，也是先把自己加进去
        broList.get(proSequence).add((short)hostId);
        ackCount = 1;
        // the broList keep key which is the sequence of proposal, the below value are hosts' id who receive the proposal
    }

    public static boolean equals(Set<Integer> set1, Set<Integer> set2){
        if(set1 == null || set2 ==null){//null就直接不比了
            return false;
        }
        if(set1.size()!=set2.size()){//大小不同也不用比了
            return false;
        }
        return set1.containsAll(set2);//最后比containsAll
    }
//    public void acceptor(int hostId, String senderId, String curProSeqAndAPN, String proSeqAndAPN, String proDifSet, Map<String, CopyOnWriteArraySet<Integer>> proValue, ArrayList<Integer> processPorts) throws IOException {
//
//        Set<String> items = new HashSet<String>(Arrays.asList(proDifSet.split(",")));
//        Set<Integer> proSetM = new HashSet<Integer>();
//        for (String i : items){
//            proSetM.add(Integer.valueOf(i));
//        }// change the read one-line string into list
//        proValue.putIfAbsent(proSeqAndAPN, new CopyOnWriteArraySet<>());
//        Set<Integer> receivedPro = new HashSet<>(proSetM);
//        if (Objects.equals(proSeqAndAPN, curProSeqAndAPN)){
//             // the set others send us
//            Set<Integer> result = new HashSet<>(proValue.get(proSeqAndAPN));
//            result.retainAll(receivedPro);
//            boolean equalSet = equals(result, receivedPro);
//
//            if (equalSet){
//                acknowledge(hostId, senderId, proSeqAndAPN, processPorts);
//            }
//            if (!equalSet){
//                Set<Integer> unionPro = new HashSet<>();
//                unionPro.addAll(proValue.get(proSeqAndAPN));
//                unionPro.addAll(receivedPro);
//                nacknowledge(hostId, senderId, proSeqAndAPN, processPorts, unionPro);
//                proValue.get(proSeqAndAPN).addAll(unionPro);
//            }
//        }
//        proValue.get(proSeqAndAPN).addAll(receivedPro);
//    }
//
    public void acknowledge(int hostId, String senderId, Integer proposeSeq, ArrayList<Integer> processPorts) throws IOException {
        String messageHost = "ack" +" "+ hostId +" "+proposeSeq;
        byte[] messageToSend = messageHost.getBytes();
        DatagramSocket hostAck = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        DatagramPacket packet = new DatagramPacket(messageToSend, messageToSend.length, address, processPorts.get(Integer.parseInt(senderId)-1));
        hostAck.send(packet);
        hostAck.close();
    }
//    public void nacknowledge(int hostId, String senderId, String proSeqAndNum, ArrayList<Integer> processPorts, Set<Integer> unionPro) throws IOException {
//        List<Integer> collect = new ArrayList<>(unionPro);
//        String messageInfo = collect.stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(","));// set to String
//        String messageHost = "nack" +" "+ hostId +" "+proSeqAndNum + " " + messageInfo;
//        byte[] messageToSend = messageHost.getBytes();
//        DatagramSocket hostAck = new DatagramSocket();
//        InetAddress address = InetAddress.getByName("localhost");
//        DatagramPacket packet = new DatagramPacket(messageToSend, messageToSend.length, address, processPorts.get(Integer.parseInt(senderId)-1));
//        hostAck.send(packet);
//        hostAck.close();
//    }
    public void reception(int proSeq, int hostId, ArrayList<Integer> processPorts, Map<Integer, CopyOnWriteArraySet<Integer>> proValue, Map<Integer, CopyOnWriteArraySet<Short>> broList, Map<Short, CopyOnWriteArraySet<Short>> ack) throws IOException {
        // listen to the port

        DatagramSocket listenFromPort = new DatagramSocket((int) processPorts.get(hostId - 1));
        listenFromPort.setReuseAddress(true);
        byte[] messageReceive = new byte[1024];
        DatagramPacket packet = new DatagramPacket(messageReceive, messageReceive.length);
        listenFromPort.receive(packet);
        String message = new String(packet.getData(), StandardCharsets.UTF_8);
        listenFromPort.close();
        message = message.trim();

        String senderMesType = message.split("\\s+")[0];
        String senderId = message.split("\\s+")[1];
        String senderProSeq = message.split("\\s+")[2]; //都是发送过来的信息

        // if the listened message is the normal propose
        if (senderMesType.contains("p")){

            //把收到的string变成set
            String ProposeSet = message.split("\\s+")[3];
            Set<String> items = new HashSet<String>(Arrays.asList(ProposeSet.split(",")));
            Set<Integer> senderProposeSet = new HashSet<Integer>();
            for (String i : items){
                senderProposeSet.add(Integer.valueOf(i));
            }

            //检查是不是在同一个propose sequence里面
            //若在同一个sequence里
            if (Integer.parseInt(senderProSeq) == proSeq){
                //检查和自己的propose value 一不一样
                Set<Integer> intersection = new HashSet<Integer>(senderProposeSet);
                intersection.retainAll(proValue.get(Integer.valueOf(senderProSeq)));
                if (equals(intersection, senderProposeSet)){
                    //若一样，就ack回去
                    ack.putIfAbsent(Short.valueOf(senderId), new CopyOnWriteArraySet<>());
                    ack.get(Short.valueOf(senderId)).add(Short.valueOf(senderProSeq));
                    acknowledge(hostId, senderId, proSeq, processPorts);
                }else {
                    //若不一样，就更新proValue和broList，清零ackCount
                    proValue.putIfAbsent(proSeq, new CopyOnWriteArraySet<>());
                    proValue.get(proSeq).addAll(senderProposeSet); //对刚过来的set取一个并集
                    broList.remove(proSeq);
                    broList.putIfAbsent(proSeq, new CopyOnWriteArraySet<>());  //刷新一个已传消息process的名单，也是先把自己加进去
                    broList.get(proSeq).add((short)hostId);
                    ackCount = 1;
                }
            }
            //若不在同一个sequence里，他的sequence比我小（我比他超前），他的value不会影响我，但是我的value需要被他涵盖
            if (Integer.parseInt(senderProSeq) < proSeq) {
                //计算两个set的交集
                if (message.split("\\s+").length == 5){
                    ack.putIfAbsent(Short.valueOf(senderId), new CopyOnWriteArraySet<>());
                    ack.get(Short.valueOf(senderId)).add(Short.valueOf(senderProSeq));
                    acknowledge(hostId, senderId, Integer.valueOf(senderProSeq), processPorts);
                }
                if (message.split("\\s+").length == 4){
                    Set<Integer> intersection = new HashSet<Integer>(senderProposeSet);
                    intersection.retainAll(proValue.get(Integer.valueOf(senderProSeq)));
                //如果收到的set包含了自己已经decide了的set
                    if (equals(intersection, proValue.get(Integer.valueOf(senderProSeq)))) {
                    //回复ack
                        ack.putIfAbsent(Short.valueOf(senderId), new CopyOnWriteArraySet<>());
                        ack.get(Short.valueOf(senderId)).add(Short.valueOf(senderProSeq));
                        acknowledge(hostId, senderId, Integer.valueOf(senderProSeq), processPorts);
                    }
                }
            }
            //若不在一个sequence里，他的sequence比我大（他比我超前），就默默地记下他的数
            if (Integer.parseInt(senderProSeq) > proSeq) {
                proValue.putIfAbsent(Integer.parseInt(senderProSeq), new CopyOnWriteArraySet<>());
                proValue.get(Integer.parseInt(senderProSeq)).addAll(senderProposeSet);
            }
        }
        // if the received message is an ack of the proposal
        if (senderMesType.contains("ack")) {
            //检查是不是在同一个propose sequence里面
            //若在同一个sequence里
            if (Integer.parseInt(senderProSeq) == proSeq){
                //sequence相同又收到的是ack证明这个sender已经知道了我们的信息，因此将其记录在已传达的broList上
                broList.putIfAbsent(proSeq, new CopyOnWriteArraySet<>());
                broList.get(proSeq).add(Short.valueOf(senderId));
                //此时ackCount加一
                ackCount = ackCount + 1;
                if (ackCount > processPorts.size()/2 && active){
                    decide(proValue.get(proSeq));
                    active = false;
                }
            }
            //若不在同一个sequence里，他的sequence比我小（我比他超前），他的ack也不会影响到我的proValue只是不用再给他传了
            if (Integer.parseInt(senderProSeq) < proSeq) {
                broList.putIfAbsent(Integer.parseInt(senderProSeq), new CopyOnWriteArraySet<>());
                broList.get(Integer.parseInt(senderProSeq)).add(Short.valueOf(senderId));
            }
        }
        // If I receive the message contains 'nack'
        if (senderMesType.contains("nack")) {
            //把收到的string变成set
            String ProposeSet = message.split("\\s+")[3];
            Set<String> items = new HashSet<String>(Arrays.asList(ProposeSet.split(",")));
            Set<Integer> senderProposeSet = new HashSet<Integer>();
            for (String i : items){
                senderProposeSet.add(Integer.valueOf(i));
            }
            //检查是不是在同一个propose sequence里面
            //若在同一个sequence里
            if (Integer.parseInt(senderProSeq) == proSeq){
                //sequence相同又收到的是nack证明这个sender已经知道了我们的信息，且有更新，因此刷新proValue和broList两个map，且清零ackCount
                proValue.putIfAbsent(proSeq, new CopyOnWriteArraySet<>());
                proValue.get(proSeq).addAll(senderProposeSet); //对刚过来的set取一个并集
                broList.remove(proSeq);
                broList.putIfAbsent(proSeq, new CopyOnWriteArraySet<>());  //刷新一个已传消息process的名单，也是先把自己加进去
                broList.get(proSeq).add((short)hostId);
                ackCount = 1;
            }
            //若不在同一个sequence里，他的sequence比我小（我比他超前），他的nack不会影响我，因为我在一直给他传之前的且在等着他的ack
        }
    }

//    public void rePropose(int hostId, int proSeq, ArrayList<Integer> processPorts, Map<String, CopyOnWriteArraySet<Short>> broList, Map<String, CopyOnWriteArraySet<Integer>> proValue){
//        if (nackCount > 0 && nackCount+ackCount > processPorts.size()/2){
//            if(active){
//                String oldProSequence = proSeq + "," + activeProNum;
//                broList.remove(oldProSequence);
//                activeProNum = activeProNum + 1;
//                ackCount = 1;
//                nackCount = 0;
//                String newProSequence = proSeq + "," + activeProNum;
//                broList.putIfAbsent(newProSequence, new CopyOnWriteArraySet<>());
//                proValue.putIfAbsent(newProSequence, new CopyOnWriteArraySet<>());
//                broList.get(newProSequence).add((short) hostId);
//                proValue.get(newProSequence).addAll(proValue.get(oldProSequence));
//                proValue.remove(oldProSequence);
//            }
//        }
//        if (ackCount > processPorts.size()/2 && active){
//            String proSeqAndAPN = proSeq + "," + activeProNum;
//            decide(proValue.get(proSeqAndAPN));
//            active = false;
//        }
//
//    }
    public void decide(Set<Integer> proValue){
        String messageInfo = proValue.stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(" "));// set to String
        CreateFile.getInstance().addLogBuffer(messageInfo + "\n");
    }


    public void run(){

        active = false;
        activeProNum = 0;
        LocalDateTime time1 = LocalDateTime.now();
        for (int proSeq = 1; proSeq < proNum +1; proSeq++){
            try {
                propose(hostId, proSeq, br, proValue, broList);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while(active) {
                try {
                    reception(proSeq, hostId, processPorts, proValue, broList, ack);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        LocalDateTime time2 = LocalDateTime.now();
        System.out.println(Duration.between(time1, time2).toMillis());

        while(true){
            try {
                reception(proNum, hostId, processPorts, proValue, broList, ack);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
