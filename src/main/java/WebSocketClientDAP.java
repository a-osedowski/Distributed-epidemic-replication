import jakarta.websocket.*;

import java.net.URI;

@ClientEndpoint
public class WebSocketClientDAP {

    private Session clientSession;

    @OnOpen
    public void onOpen(Session clientSession) {
        this.clientSession = clientSession;
    }

    @OnMessage
    public void onMessage(String messg) {
        System.out.println("Got message" + messg);
    }

    public void sendStatus(String message) {
        this.clientSession.getAsyncRemote().sendText(message);
        System.out.println("Sent message");
    }

    public WebSocketClientDAP(int nodenum) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String URI = "ws://localhost:" + 8080 + "/nodes/" + nodenum;
            container.connectToServer(this, new URI(URI));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        WebSocketClientDAP web = new WebSocketClientDAP(1);
        web.sendStatus("Kurwa");
    }
}
