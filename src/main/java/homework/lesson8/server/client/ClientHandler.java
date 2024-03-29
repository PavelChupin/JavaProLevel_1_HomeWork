package homework.lesson8.server.client;

import homework.lesson8.messageconvert.Command;
import homework.lesson8.messageconvert.Message;
import homework.lesson8.messageconvert.message.AuthMessage;
import homework.lesson8.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {
    private Server server;

    private String clientName;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(Socket socket, Server server, int timeout) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            Thread thread = new Thread(() -> {
                try {
                    authentication(timeout);
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create client handler", e);
        }

    }

    /*
    private void waitTimeOut(int timeout, Thread chat) throws InterruptedException {
        Thread waittimeout = new Thread(() -> {
            try {
                long a = System.currentTimeMillis();
                do {
                    Thread.sleep(1);
                } while (System.currentTimeMillis() - a <= timeout * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        waittimeout.setDaemon(true);
        waittimeout.start();

        //Если поток ожидания закончил работу, а пользователь не был авторизирован, то закроем сокет и отправим клиенту сообщение
        do {
            Thread.sleep(1);
        } while (waittimeout.isAlive());

        if (this.clientName == null || this.clientName.isEmpty()) {
            System.out.println("Client disconnect of timeout");
            sendMessage("TimeOut for connection end");
            if (chat.isAlive()) {
                chat.interrupted();
            }
            closeConnection();
        }
    }
    */
    // "/auth login password"
    private void authentication(int timeout) throws IOException {
        while (true) {
            Timer timeoutTimer = new Timer(true);
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            if (clientName == null) {
                                System.out.println("authentication is terminated caused by timeout expired");
                                sendMessage(Message.createAuthError("Истекло время ожидания подключения!"));
                                Thread.sleep(100);
                                socket.close();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, timeout*1000);
            String clientMessage = in.readUTF();
            synchronized (this) {
                Message message = Message.fromJson(clientMessage);
                if (message.command == Command.AUTH_MESSAGE) {
                    AuthMessage authMessage = message.authMessage;
                    String login = authMessage.login;
                    String password = authMessage.pass;
                    String nick = server.getAuthService().getNickByLoginPass(login, password);
                    if (nick == null) {
                        sendMessage(Message.createAuthError("Неверные логин/пароль"));
                        continue;
                    }

                    if (server.isNickBusy(nick)) {
                        sendMessage(Message.createAuthError("Учетная запись уже используется"));
                        continue;
                    }
                    clientName = nick;
                    sendMessage(Message.createAuthOk(clientName));
                    server.broadcastMessage(Message.createPublic(null, clientName + " is online"), this);
                    server.subscribe(this);
                    break;
                }
            }
        }
    }


    //Основной метод чата по пересылке сообщений
    private void readMessages() throws IOException {
        while (true) {
            String clientMessage = in.readUTF();
            System.out.printf("PrivateMessage '%s' from client %s%n", clientMessage, clientName);
            Message m = Message.fromJson(clientMessage);
            switch (m.command) {
                case PUBLIC_MESSAGE:
                    //PublicMessage publicMessage = m.publicMessage;
                    //server.broadcastMessage(publicMessage.from + " : " + publicMessage.message, this);
                    server.broadcastMessage(m, this);
                    break;
                case PRIVATE_MESSAGE:
                    //PrivateMessage privateMessage = m.privateMessage;
                    //server.messageToPrivateLogin(privateMessage.to, privateMessage.message);
                    server.messageToPrivateLogin(m);
                    break;
                case END:
                    return;
            }
        }
    }

    private void closeConnection() {
        server.unSubscribe(this);
        server.broadcastMessage(clientName + " is offline", this);
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket!");
            e.printStackTrace();
        }
    }

    private void sendMessage(Message message) {
        sendMessage(message.toJson());
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            System.err.println("Failed to send message to user " + clientName + " : " + message);
            e.printStackTrace();
        }
    }


    public String getClientName() {
        return clientName;
    }
}
