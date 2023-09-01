package cs451;

import cs451.lattice.Proposal;
import cs451.lattice.ProposalAck;
import cs451.lattice.ProposalBroadcast;
import cs451.urb.FIFOBroadcast;
import cs451.urb.FIFODeliver;
import cs451.urb.FIFOListen;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static void handleSignal() throws IOException {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
        CreateFile.writeLogFile();

    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    handleSignal();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Parser parser = new Parser(args);
        parser.parse();
        String outputPath = parser.output();
        CreateFile.getInstance().init(outputPath);
        initSignalHandlers();

        ArrayList<Integer> hostId = new ArrayList<>();
        ArrayList<String> hostIp = new ArrayList<>();
        ArrayList<Integer> hostPort = new ArrayList<>();
        ArrayList<Integer> senderPort = new ArrayList<>();
        //boolean isEnd = false;

        // example
        long pid = ProcessHandle.current().pid();
        int id = parser.myId();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            hostId.add(host.getId());

            System.out.println("Human-readable IP: " + host.getIp());
            hostIp.add(host.getIp());

            System.out.println("Human-readable Port: " + host.getPort());
            hostPort.add(host.getPort());
            senderPort.add(host.getPort());
        }

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");
        System.out.println("please Broadcasting and delivering messages...\n");

        //create file for keeping the output
//        String outputPath = parser.output();
//        CreateFile.getInstance().init(outputPath);

        //get the number of message and the id of the receiver
        String configurePath = parser.config();
        //System.out.println(configurePath);
        File file = new File(configurePath);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line;
        line = br.readLine();
        String [] arr = line.split("\\s+");
        int proNum = Integer.parseInt(arr[0]);
        int proEleMaxNum = Integer.parseInt(arr[1]);
        int eleMaxNum = Integer.parseInt(arr[2]);

        Map<Integer, CopyOnWriteArraySet<Short>> broList = new ConcurrentHashMap<>();
        Map<Short, CopyOnWriteArraySet<Short>> ack = new ConcurrentHashMap<>();
        Map<Integer, CopyOnWriteArraySet<Short>> nack = new ConcurrentHashMap<>();
        Map<Integer, CopyOnWriteArraySet<Integer>> proValue = new ConcurrentHashMap<>();
        Set<Short> receiverCheckList = Stream.iterate(1, item -> item+1).map(Integer::shortValue).limit(hostPort.size()).collect(Collectors.toSet());
        Proposal proposal = new Proposal(id, proNum, proEleMaxNum, eleMaxNum, hostPort, br, broList, ack, nack, proValue);
        ProposalBroadcast proposalBroadcast = new ProposalBroadcast(id, proNum, proEleMaxNum, eleMaxNum, hostPort, br, broList, ack, nack,receiverCheckList, proValue);
        //ProposalAck proposeAck = new ProposalAck(id, proNum, proEleMaxNum, eleMaxNum, hostPort, br, broList, ack, nack, proValue);
        proposal.start();
        proposalBroadcast.start();
        //proposeAck.start();



        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
