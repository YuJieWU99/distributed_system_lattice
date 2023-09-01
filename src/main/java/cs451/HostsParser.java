package cs451;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HostsParser {

    private static final String HOSTS_KEY = "--hosts";
    private static final String SPACES_REGEX = "\\s+";

    private String filename;
    private List<Host> hosts = new ArrayList<>();

    public boolean populate(String key, String filename) {
        if (!key.equals(HOSTS_KEY)) {
            return false;
        }

        this.filename = filename; //The most common use of the this keyword is to eliminate the confusion between class attributes and parameters with the same name
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {//使用buffer的办法读入file的数据
            int lineNum = 1;
            for(String line; (line = br.readLine()) != null; lineNum++) {
                if (line.isBlank()) {
                    continue;
                }//除非是\\s+不然继续buffer入一行

                String[] splits = line.split(SPACES_REGEX);
                if (splits.length != 3) {
                    System.err.println("Problem with the line " + lineNum + " in the hosts file!");
                    return false;
                }
//一个host有三个变量，id，ip，port
                Host newHost = new Host();
                if (!newHost.populate(splits[0], splits[1], splits[2])) {//here the boolean value is used and return the value of the host
                    return false;
                }

                hosts.add(newHost);
            }
        } catch (IOException e) {
            System.err.println("Problem with the hosts file!");
            return false;
        }

        if (!checkIdRange()) {
            System.err.println("Hosts ids are not within the range!");
            return false;
        }

        // sort by id
        Collections.sort(hosts, new HostsComparator());
        return true;
    }

    private boolean checkIdRange() {
        int num = hosts.size();
        for (Host host : hosts) {
            if (host.getId() < 1 || host.getId() > num) {
                System.err.println("Id of a host is not in the right range!");
                return false;
            }
        }

        return true;
    }

    public boolean inRange(int id) {
        return id <= hosts.size();
    }

    public List<Host> getHosts() {
        return hosts;
    }

    class HostsComparator implements Comparator<Host> {

        public int compare(Host a, Host b) {
            return a.getId() - b.getId();
        }//so that it can sort the hosts by id.

    }

}
