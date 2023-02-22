import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class CoreNode extends Process{

    private Socket socket = null;
    private BufferedReader inputCore = null;
    private PrintWriter outCore = null;
    private BufferedReader inputCustomer = null;
    private PrintWriter outCustomer = null;
    private BufferedReader inputToken = null;
    private PrintWriter outToken = null;
    private ServerSocket serverCustomer = null;
    private ServerSocket serverCores = null;
    private String address;
    private int ownPort;
    private int secondLayerPort;
    private int nextCorePort;
    private int customerPort;

    public boolean haveToken;
    public boolean socketBlocked;
    public int counter;
    public String logFile;

    ArrayList<Integer> actualArray;

    public CoreNode(String address, int ownPort, int secondLayerPort, int nextCorePort, int customerPort, String logFile){
        this.address = address;
        this.ownPort = ownPort;
        this.secondLayerPort = secondLayerPort;
        this.nextCorePort = nextCorePort;
        this.customerPort = customerPort;
        this.socketBlocked = false;
        this.logFile = "LogFiles/" + logFile;
        this.actualArray = new ArrayList<Integer>();

        try {
            this.serverCustomer = new ServerSocket(customerPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.serverCores = new ServerSocket(ownPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenCores() {
            try {

                System.out.print("\nListen cores, port server: " + ownPort);
                socket = serverCores.accept();
                outCore = new PrintWriter(socket.getOutputStream(), true);
                inputCore = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = inputCore.readLine();
                System.out.print("Message received: ");
                System.out.print(message);
                String array2_str = inputCore.readLine();
                String array1_str = inputCore.readLine();
                List<Integer> array1 = new ArrayList<>();
                List<Integer> array2 = new ArrayList<>();
                System.out.printf("Array2: " + array2_str);
                System.out.printf("Array1: " + array1_str);
                for (String s : array1_str.split(",")) {
                    array1.add(Integer.parseInt(s));
                }
                for (String s : array2_str.split(",")) {
                    array2.add(Integer.parseInt(s));
                }

                if (message.equals("release")) {
                    outCore.println("okayRL");
                    System.out.print("Okay sent");
                    haveToken = true;
//                    System.out.printf("Element 2: " + readFromFile(2))[2]);

                    if (array1.equals(readFromFile(1)) && array2.equals(readFromFile(2))){

                    }else if (array2.equals(readFromFile(1))){
                        writeToFile(array1);
                        counter ++;
                        System.out.printf("Counter: " + counter);
                    }else{
                        writeToFile(array2);
                        writeToFile(array1);
                        counter += 2;
                        System.out.printf("Counter: " + counter);
                    }

                }
            } catch (IOException i) {
                System.out.println(i);
            }

            try {
                System.out.println("Closing listen");
                inputCore.close();
                outCore.close();
                socket.close();
            } catch (IOException i) {
                System.out.println("Exception close");
                System.out.println(i);
            }
    }

    public void listenCustomers() {
        while (true) {
            try {

                System.out.printf("\nListen customer, port server: " + customerPort);
                socket = serverCustomer.accept();
                outCustomer = new PrintWriter(socket.getOutputStream(), true);
                inputCustomer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = inputCustomer.readLine();
                if (message.equals("begin")) {
                    if (haveToken && socketBlocked == false) {
                        outCustomer.println("okayBG");
                        System.out.print("Okay sent");
                        socketBlocked = true;
                        actualArray = (ArrayList<Integer>) readFromFile(1);
                    } else {
                        outCustomer.println("refuseBG");
                        System.out.print("Okay sent");
                    }
                }
                while (socketBlocked) {
                    String operation = inputCustomer.readLine();
                    System.out.print("Operation received: ");
                    System.out.print(operation);


                    if (operation.equals("close")) {
                        outCustomer.println("okayCL");
                        System.out.print("Okay sent");
                        if (!actualArray.equals(readFromFile(1))){
                            writeToFile(actualArray);
                            counter++;
                            System.out.printf("Counter: " + counter);
                        }
                        socketBlocked = false;
                        break;
                    } else {
                        String line_number_str = inputCustomer.readLine();
                        System.out.printf("Line: " + line_number_str);
                        int value;
                        Boolean ret;
                        int index = Integer.parseInt(line_number_str);
                        if (operation.equals("w")) {
                            System.out.print("Operation write");
                            String value_str = inputCustomer.readLine();
                            System.out.printf("Value: " + value_str);
                            value = Integer.parseInt(value_str);
                            System.out.printf("Value of index:" + actualArray.get(index));
                            actualArray.set(index, value);
                            System.out.printf("\nValue of index:" + actualArray.get(index));
                            outCustomer.println("Saved");

                        } else if (operation.equals("r")) {
                            value = actualArray.get(index);
                            outCustomer.println(value);
                            }
                        }
                    if (haveToken) {
                        sendToken();
                    }
                    }
            } catch (IOException i) {
                System.out.println(i);
                socketBlocked = false;
            }

            try {
                System.out.println("Closing listen");
                inputCustomer.close();
                outCustomer.close();
                socket.close();
            } catch (IOException i) {
                System.out.println("Exception close");
                System.out.println(i);
            }
        }
    }

    private List<Integer> readFromFile(int line_number) throws IOException {
        FileReader fileReader = new FileReader(logFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<List<Integer>> lines = new ArrayList<>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            List<Integer> array = new ArrayList<>();
            for (String s : line.split(",")) {
                array.add(Integer.parseInt(s));
            }
            lines.add(array);
        }
        bufferedReader.close();

        return lines.get(lines.size() - line_number);
    }


    private Boolean writeToFile(List<Integer> array) {
        FileWriter logWriter = null;
        try {
            logWriter = new FileWriter(logFile, true);
            System.out.printf("LogFile" + logFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            logWriter.write(System.lineSeparator());
            for (Integer value : array) {
                System.out.printf(value + ", ");
                logWriter.write(value + ",");
            }
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendToken() {
        boolean ret = false;
        if (!socketBlocked) {
            try {
                System.out.println("Send token to another core");
                socket = new Socket(address, nextCorePort);
                System.out.println("Connected");
                String message = "release";
                System.out.print("Send release");
                outToken = new PrintWriter(socket.getOutputStream(), true);
                inputToken = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ArrayList<Integer> array1 = (ArrayList<Integer>) readFromFile(1);
                ArrayList<Integer> array2 = (ArrayList<Integer>) readFromFile(2);
                String array1_str = "";
                String array2_str = "";
                for (Integer value : array1) {
                    array1_str = array1_str + value + ",";
                }
                for (Integer value : array2) {
                    array2_str = array2_str + value + ",";
                }
                outToken.println(message);
                outToken.println(array2_str);
                outToken.println(array1_str);
                String resp = inputToken.readLine();
                if (resp.equals("okayRL")) {
                    ret = true;
                    haveToken = false;
                    System.out.print("Received ok");
                    System.out.print("do not have token");
                }
            } catch (UnknownHostException u) {
                System.out.println(u);
            } catch (IOException i) {
                System.out.println(i);
            }
            try {
                System.out.println("Closing client");
                inputToken.close();
                outToken.close();
                socket.close();
            } catch (IOException i) {
                System.out.println("Exception close");
                System.out.println(i);
            }
            return ret;
        }
        return ret;
    }


    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public InputStream getErrorStream() {
        return null;
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    @Override
    public int exitValue() {
        return 0;
    }

    @Override
    public void destroy() {

    }



    public static void main(String[] args) throws InterruptedException {
        int ownPort = 0;
        int secondLayerPort = 0;
        int nextCorePort = 0;
        int customerPort = 0;
        String stringID = args[0];
        int myId = Integer.parseInt(stringID);
        boolean haveToken = false;
        int backupPort = 0;
        String logFile = "";

        if (myId == 1) {ownPort = 8001; secondLayerPort = 0; nextCorePort = 8002; customerPort = 8006; logFile = "A1.txt"; haveToken = true;}
        else if (myId == 2) {ownPort = 8002; secondLayerPort = 8021; nextCorePort = 8003; customerPort = 8007; backupPort = 8031; logFile = "A2.txt";}
        else if (myId == 3) {ownPort = 8003; secondLayerPort = 8022; nextCorePort = 8001; customerPort = 8008; backupPort = 8032; logFile = "A3.txt";}

        CoreNode coreNode = new CoreNode("localhost", ownPort, secondLayerPort, nextCorePort, customerPort, logFile);
        coreNode.haveToken = haveToken;

        Thread listen = new Thread() {
            @Override
            public void run() {
                coreNode.listenCustomers();
            }
        };

        Thread token = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (!coreNode.haveToken){
                        coreNode.listenCores();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (coreNode.haveToken) {
                        coreNode.sendToken();
                    }
                }
            }
        };

        if (backupPort != 0) {
            final String[] dataFromLog = new String[1];
            SecondaryNode secondaryNode = new SecondaryNode(logFile, backupPort);
            int finalBackupPort = backupPort;
            System.out.printf("Backup port: " + backupPort);
            coreNode.counter = 0;
            System.out.printf("Started thread backup");

            Thread backup = new Thread() {
                @Override
                public void run() {
                    while (true) {
//                        System.out.printf("Counter: " + coreNode.counter);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (coreNode.counter >= 10) {
                            coreNode.counter = 0;
                            dataFromLog[0] = secondaryNode.getDataFromLog();
                            System.out.printf("Data from log file(last line): " + dataFromLog[0]);
                            try {
                                Socket sock = new Socket("localhost", finalBackupPort);
                                PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
                                out.println(dataFromLog[0]);
                                System.out.print("Backup sent");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    }
            };
            backup.start();
        }
        listen.start();
        token.start();
    }
}
