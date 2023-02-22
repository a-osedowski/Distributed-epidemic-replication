import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SecondaryNode {

    private static int NUM_LIGHTWEIGHTS = 2;
    private FileWriter logWriter;
    private Socket listenSocket;
    private Socket replicationSocket;

    private String logFile;
    private int port;

    ServerSocket serverSocket;


    private Socket socket = null;
    private BufferedReader inputCustomer = null;
    private PrintWriter outCustomer = null;
    private ServerSocket serverCustomer = null;



    ArrayList<Integer> actualArray;

    public SecondaryNode(String logFile, int port) {
        this.logFile = "LogFiles/" + logFile;
        this.port = port;
    }


    public void start() {
        // Start listening for updates in a separate thread
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true) {
                    // wait for data updates
                    try {
                        getData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        updateThread.start();
    }

    private boolean getData() throws IOException {
        System.out.printf("Port server: " + port);
        System.out.print("\nListening");


        String message = "";
        try {
            listenSocket = serverSocket.accept();
            System.out.print("Connected");
            try {
                logWriter = new FileWriter(this.logFile, false);
                logWriter.write("");
                logWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                logWriter = new FileWriter(this.logFile, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(listenSocket.getInputStream()));
            while (!(message = input.readLine()).equals("")){
                System.out.printf("Message received: " + message);
                if(message != null){ logData(message); }
            }
            System.out.print("Writting ended");
            logWriter.close();
            } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void logData(String data) {
        try {
            System.out.printf("\nData to save: " + data);
            logWriter.write(data + "\n");
            System.out.print("Saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDataFromLog() {
        // read the last line from the log file
        // return the data

        String fileString = "";
        try {

            FileReader fileReader = new FileReader(logFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fileString = fileString + line + "\n";
            }
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileString;
    }

    public void listenCustomers(int customerPort) {
        try {
            serverCustomer = new ServerSocket(customerPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {

                System.out.printf("\nListen customer, port server: " + customerPort);
                socket = serverCustomer.accept();
                outCustomer = new PrintWriter(socket.getOutputStream(), true);
                inputCustomer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = inputCustomer.readLine();
                if (message.equals("begin")) {
                    outCustomer.println("okayBG");
                    System.out.print("Okay sent");
                    actualArray = (ArrayList<Integer>) readFromFile(1);
                }
                while (true) {
                    String operation = inputCustomer.readLine();
                    System.out.print("Operation received: ");
                    System.out.print(operation);

                    if (operation.equals("close")) {
                        outCustomer.println("okayCL");
                        System.out.print("Okay sent");
                        break;
                    } else {
                        String line_number_str = inputCustomer.readLine();
                        System.out.printf("Line: " + line_number_str);
                        int value;
                        int index = Integer.parseInt(line_number_str);
                        value = actualArray.get(index);
                        outCustomer.println(value);
                    }
                }

            } catch(IOException i){
                System.out.println(i);
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

}
