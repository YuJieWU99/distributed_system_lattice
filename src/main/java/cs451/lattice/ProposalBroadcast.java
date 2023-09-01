package cs451.lattice;

import cs451.CreateFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class ProposalBroadcast extends Thread{
    private int hostId;
    private int proNum;
    private int proEleMaxNum;
    private int eleMaxNum;
    private int proSeq;
    private Boolean active;
    private int ackCount;
    private int nackCount;
    private int activeProNum;
    // ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

    // Create the set by newKeySet() method of ConcurrentHashMap
    Set<String> set = ConcurrentHashMap.newKeySet();
    private BufferedReader br;
    Set<Short> receiverCheckList = new TreeSet<>();
    Map<Integer, CopyOnWriteArraySet<Short>> broList = new ConcurrentHashMap<>();
    ArrayList<Integer> processPorts = new ArrayList<>();
    Map<Short, CopyOnWriteArraySet<Short>> ack = new ConcurrentHashMap<>();
    Map<Integer, CopyOnWriteArraySet<Short>> nack = new ConcurrentHashMap<>();
    Map<Integer, CopyOnWriteArraySet<Integer>> proValue = new ConcurrentHashMap<>();

    public ProposalBroadcast(int hostId, int proNum, int proEleMaxNum, int eleMaxNum, ArrayList<Integer> processPorts, BufferedReader br, Map<Integer, CopyOnWriteArraySet<Short>> broList,
                    Map<Short, CopyOnWriteArraySet<Short>> ack, Map<Integer, CopyOnWriteArraySet<Short>> nack, Set<Short> receiverCheckList, Map<Integer, CopyOnWriteArraySet<Integer>> proValue){
        this.hostId = hostId;
        this.proNum = proNum;
        this.proEleMaxNum = proEleMaxNum;
        this.eleMaxNum = eleMaxNum;
        this.processPorts = processPorts;
        this.br = br;
        this.broList = broList;
        this.ack = ack;
        this.nack = nack;
        this.receiverCheckList = receiverCheckList;
        this.proValue = proValue;
    }

    public void broadcast(int hostId, ArrayList<Integer> processPorts, Map<Integer, CopyOnWriteArraySet<Short>> broList, Set<Short> receiverCheckList, Map<Integer, CopyOnWriteArraySet<Integer>> proValue) throws IOException {
        //检测一下此时发的propose 序号是不是正在decide的

        int max = 0;
        for (Integer number : broList.keySet()){
            if (max < number){
                max = number;
            }
        }


        for (Integer proposalNumber : broList.keySet()){
            Set<Short> receiveList = broList.get(proposalNumber);
            String messageInfo = null;
            Set<Short> toSendSet;
            //String apn = proposalNumber.split(",")[1];
            if (receiveList != null) {
                toSendSet = receiverCheckList.stream().filter(a -> !receiveList.contains(a)).collect(Collectors.toSet());
            } else {
                toSendSet = Collections.emptySet();
            }

            if (max > proposalNumber){
                for (Short process : toSendSet){
                    List<Integer> collect = new ArrayList<>(proValue.get(proposalNumber));
                    messageInfo = collect.stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(","));

                    String messageHost = "p" + " " + hostId + " " + proposalNumber + " " + messageInfo + " " + max; // propose the sender id.active nu

//                if (ack.containsKey(messageSeqAndFrom)){
//                    List<Short> collect = new ArrayList<>(ack.get(messageSeqAndFrom));
//                    messageInfo = collect.stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(","));
////                    messageInfo = ack.get(messageSeqAndFrom).stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(","));
//                    messageHost = "b" + " " + String.valueOf(hostId) + " " + messageSeqAndFrom + " " + messageInfo;
//                }
                    byte[] messagesSend = messageHost.getBytes();
                    DatagramSocket hostBroadcast = new DatagramSocket();
                    InetAddress address = InetAddress.getByName("localhost");
                    DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length, address, processPorts.get(process - 1));
                    hostBroadcast.send(packet); //System.out.println("b" + " " + messagesToSend);
                    //System.out.println(messageHost);
                    hostBroadcast.close();
                }
            }
            if (max == proposalNumber){
                for (Short process : toSendSet){

                    List<Integer> collect = new ArrayList<>(proValue.get(proposalNumber));
                    messageInfo = collect.stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(","));
                    String messageHost = "p" + " " + hostId + " " + proposalNumber + " " + messageInfo; // propose the sender id.active nu

//                if (ack.containsKey(messageSeqAndFrom)){
//                    List<Short> collect = new ArrayList<>(ack.get(messageSeqAndFrom));
//                    messageInfo = collect.stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(","));
////                    messageInfo = ack.get(messageSeqAndFrom).stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(","));
//                    messageHost = "b" + " " + String.valueOf(hostId) + " " + messageSeqAndFrom + " " + messageInfo;
//                }
                    byte[] messagesSend = messageHost.getBytes();
                    DatagramSocket hostBroadcast = new DatagramSocket();
                    InetAddress address = InetAddress.getByName("localhost");
                    DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length, address, processPorts.get(process - 1));
                    hostBroadcast.send(packet); //System.out.println("b" + " " + messagesToSend);
                    //System.out.println(messageHost);
                    hostBroadcast.close();
                }
            }
        }
    }

    public void acknow(int hostId, ArrayList<Integer> processPorts, Map<Short, CopyOnWriteArraySet<Short>> ack) throws IOException {
        for (Short process : ack.keySet()){
            Integer processId = Integer.valueOf(process);
            //String messageInfo = null;
            for (Short proposeSequence: ack.get(process)){
                //messageInfo = collect.stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(","));
                String messageHost = "ack" + " " + hostId + " " + proposeSequence;
                byte[] messagesSend = messageHost.getBytes();
                DatagramSocket hostBroadcast = new DatagramSocket();
                InetAddress address = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length, address, processPorts.get(processId - 1));
                hostBroadcast.send(packet); //System.out.println("b" + " " + messagesToSend);
                //System.out.println(messageHost);
                hostBroadcast.close();
            }

        }
    }

    public void run() {
        while(true) {
            try {
                broadcast(hostId, processPorts, broList, receiverCheckList, proValue);
                //acknow(hostId, processPorts, ack);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
