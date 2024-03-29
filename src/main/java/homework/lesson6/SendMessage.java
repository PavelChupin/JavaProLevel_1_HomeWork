package homework.lesson6;

import homework.lesson6.Runnable.InputMessage;
import homework.lesson6.Runnable.OutputMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SendMessage {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread threadIn;
    private Thread threadOut;
    private boolean server;

    public SendMessage(boolean server) {
        this.socket = new Socket();
        this.server = server;
    }

    public SendMessage(String addresServer, int portServer,boolean server) throws IOException {
        this.socket = new Socket(addresServer, portServer);
        this.server = server;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void start() throws InterruptedException, IOException {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        startThread();
    }

    public void start(String message) throws InterruptedException, IOException {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        out.writeUTF(message);

        startThread();
    }

    private void startThread() throws InterruptedException, IOException {
        threadIn = new Thread(new InputMessage(this.in));
        threadIn.setDaemon(true);
        threadOut = new Thread(new OutputMessage(this.out));
        threadOut.setDaemon(true);
        threadIn.start();
        threadOut.start();

        do {
            Thread.sleep(1);
        } while (threadIn.isAlive() && threadOut.isAlive()/*(server && threadIn.isAlive()) || (!server && threadOut.isAlive())*/);

        close();
    }

    private void close() throws IOException {
        //if(!threadIn.isInterrupted()) {threadIn.interrupt();}
        //if(!threadOut.isInterrupted()) {threadOut.interrupt();}
        if(threadIn.isAlive()) {threadIn.interrupt();}
        if(threadOut.isAlive()) {threadOut.interrupt();}
        socket.close();
    }

}
