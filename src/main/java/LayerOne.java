import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class LayerOne {
    public static void main(String[] args) throws InterruptedException {
        String stringID = args[0];
        String logFile = "";
        int listenPort = 0;
        int listenCustomer = 0;
        int myId = Integer.parseInt(stringID);
        System.out.print(myId);
        int backupPort = 0;
        if (myId == 1) {logFile = "B1.txt"; listenPort = 8031; listenCustomer = 8035;}
        else if (myId == 2) {logFile = "B2.txt"; listenPort = 8032; listenCustomer = 8036;}
        SecondaryNode secondaryNodeListen = new SecondaryNode(logFile, listenPort);

        String finalLogFile1 = logFile;
        int finalListenPort = listenPort;
        int finalListenCustomer = listenCustomer;
        Thread customerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SecondaryNode secondaryNodeCustomer = new SecondaryNode(finalLogFile1, finalListenPort);
                secondaryNodeCustomer.listenCustomers(finalListenCustomer);
            }
        });

        if (myId == 2) {

            final String[] dataFromLog = new String[1];
            System.out.printf("Started thread backup");
            String finalLogFile = logFile;
            Thread backup = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        SecondaryNode secondaryNodeC1 = new SecondaryNode(finalLogFile, 8012);
                        SecondaryNode secondaryNodeC2 = new SecondaryNode(finalLogFile, 8013);

                        dataFromLog[0] = secondaryNodeC1.getDataFromLog();
                        System.out.printf("Data from log file(last line): " + dataFromLog[0]);
                        try {
                            Socket sock = new Socket("localhost", 8012);
                            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
                            out.println(dataFromLog[0]);
                            System.out.print("Backup C1 sent");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        dataFromLog[0] = secondaryNodeC2.getDataFromLog();
                        System.out.printf("Data from log file(last line): " + dataFromLog[0]);
                        try {
                            Socket sock = new Socket("localhost", 8013);
                            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
                            out.println(dataFromLog[0]);
                            System.out.print("Backup C2 sent");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            backup.start();
        }
        secondaryNodeListen.start();
        customerThread.start();
    }
}
