import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Customer {

    private Socket socket = null;
    private BufferedReader input = null;
    private PrintWriter out = null;
    private String address;
    private int corePort;

    public synchronized boolean writeReadCore(int corePort, List<String> operation, List<Integer> index, List<Integer> value){
        boolean ret = false;
        String resp = null;
        String beginRequest = "";
        while (!beginRequest.equals("okayBG")) {
            try {
                System.out.printf("Send to core\n" + "Operation: " + operation + "\nLine: " + index + "\nValue:" +  value);
                socket = new Socket(address, corePort);
                System.out.println("Connected");
                out = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out.println("begin");
                System.out.print("Begin sent");
                beginRequest = input.readLine();
                System.out.printf("Message received: " + beginRequest);
                if (operation.get(0).equals("w")) {
                    out.println(operation.get(0) + "\n" + index.get(0) + "\n" + value.get(0));
                } else if ((operation.get(0).equals("r"))) {
                    out.println(operation.get(0) + "\n" + index.get(0));
                }
                if (!beginRequest.equals("refuseBG")){
                    if (operation.get(0).equals("w")) {
                        input.readLine();
                        System.out.printf("\nWritten");
                    } else if ((operation.get(0).equals("r"))) {
                        resp = input.readLine();
                        System.out.printf("\nResponse: " + resp);
                    }
                    for (int i = 1; i<operation.size(); i++) {
                        if (operation.get(i).equals("w")) {
                            out.println(operation.get(i) + "\n" + index.get(i) + "\n" + value.get(i));
                            input.readLine();
                            System.out.printf("\nWritten");
                        } else if ((operation.get(i).equals("r"))) {
                            out.println(operation.get(i) + "\n" + index.get(i));
                            resp = input.readLine();
                            System.out.printf("\nResponse: " + resp);
                        }
                    }
                    out.println("close");
                }
                Thread.sleep(200);

            } catch(UnknownHostException u){
                System.out.println(u);
            } catch(IOException i){
                System.out.println(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                System.out.println("Closing client");
                input.close();
                out.close();
                socket.close();
            } catch (IOException i) {
                System.out.println("Exception close");
                System.out.println(i);
            }
        }
        return true;
    }



    public static void main(String[] args) throws InterruptedException {
        Customer customer = new Customer();
//        customer.writeToCore(8006,1, 11);
//        customer.writeToCore(8006,1, 12);
//        customer.writeToCore(8006,2, 13);
//        operation.add("w"); line.add(1); value.add(18);
//        operation.add("r"); line.add(1); value.add(null);
//        operation.add("w"); line.add(3); value.add(12);
//        operation.add("w"); line.add(4); value.add(13);
        int corePort = 0;

        try {

            FileReader fileReader = new FileReader("LogFiles/Customer.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                List<String> operation = new ArrayList<>();
                List<Integer> index = new ArrayList<>();
                List<Integer> value = new ArrayList<>();
                List<String> array = new ArrayList<>();
                for (String s : line.split(";")) {
                    System.out.printf("Split:" + s);
                    array.add(s);
                }
                if (array.get(0).equals("b")){
                    corePort = 8006;
                } else {
                    String[] parts = array.get(0).split("");
                    String b = parts[0];
                    String number = parts[1];
                    int number_int = Integer.parseInt(number);
                    if (number_int == 0){
                        corePort = 8007;
                    } else if (number_int == 1){
                        corePort = 8035;
                    } else if (number_int == 2){
                        corePort = 8015;
                    }
                }


                for(int i = 1; i<array.size()-1; i++) {
                    String[] parts = array.get(i).split("\\("); // split on the "(" character
                    String oper= parts[0];
                    System.out.printf("Oper: " + oper);
                    operation.add(oper);
                    if (oper.equals("w")) {
                        System.out.printf("parts[1]: " +parts[1]);
                        String [] numbers_str = parts[1].split("\\)"); // split on the ")" character
                        System.out.printf("number_str: " + numbers_str[0]);
                        String[] numbers = numbers_str[0].split(",");
                        String number1 = numbers[0];
                        String number2 = numbers[1];
                        System.out.printf("Index: " + number1);
                        index.add(Integer.valueOf(number1));
                        System.out.printf("Value: " + number2);
                        value.add(Integer.valueOf(number2));
                    }else if (oper.equals("r")){
                        String number = parts[1].split("\\)")[0];
                        index.add(Integer.valueOf(number));
                        System.out.printf("Index: " + number);
                        value.add(null);
                    }
                }
                customer.writeReadCore(corePort, operation, index, value);
                Thread.sleep(1500);
            }
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
