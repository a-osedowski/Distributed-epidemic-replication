public class LayerTwo {
    public static void main(String[] args) throws InterruptedException {
        String stringID = args[0];
        String logFile = "";
        int listenPort = 0;
        int listenCustomer = 0;
        int myId = Integer.parseInt(stringID);
        System.out.print(myId);
        if (myId == 1) {logFile = "C1.txt"; listenPort = 8012; listenCustomer = 8015;}
        else if (myId == 2) {logFile = "C2.txt"; listenPort = 8013; listenCustomer = 8016;}
        SecondaryNode secondaryNode = new SecondaryNode(logFile, listenPort);
        String finalLogFile = logFile;
        int finalListenPort = listenPort;
        int finalListenCustomer = listenCustomer;
        Thread customerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SecondaryNode secondaryNodeCustomer = new SecondaryNode(finalLogFile, finalListenPort);
                secondaryNodeCustomer.listenCustomers(finalListenCustomer);
            }
        });
        secondaryNode.start();
        customerThread.start();
    }
}
