package homework.lesson7.server;

import homework.lesson7.server.auth.IAuthService;
import homework.lesson7.server.auth.BaseAuthService;
import homework.lesson7.server.client.ClientHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Server {
    //private static final int PORT = 8189;
    private static final String HOST_PORT_PROP = "server.port";
    private final IAuthService authService = new BaseAuthService();

    private List<ClientHandler> clients = new ArrayList<>();

    public Server() {
        System.out.println("Server is running");
        try (ServerSocket serverSocket = new ServerSocket(getHostPort())) {

            authService.start();

            //Бесконечный цикл ожидания подключения пользователей
            while (true) {
                System.out.println("Awaiting client connection...");
                //Ждем подключений
                Socket socket = serverSocket.accept();
                System.out.println("Client has connected");
                //Подключения получено, запускаем сервис авторизации
                new ClientHandler(socket, this);
            }

        } catch (IOException e) {
            System.err.println("Ошибка работы сервера. Причина: " + e.getMessage());
            e.printStackTrace();
        } finally {
            authService.stop();
        }
    }

    //Метод подписки клиентов на работу с сервером
    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    //Метод отписки пользователя от сервера
    public synchronized void unSubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public IAuthService getAuthService() {
        return authService;
    }

    //Метод проверки на занятость/ранее авторизированного пользователя
    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (client.getClientName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    //Метод отправки сообщения всем пользователям чата
        public synchronized void broadcastMessage(String s, ClientHandler unfilteredClients) {
            List<ClientHandler> unfiltered = Arrays.asList(unfilteredClients);
            for (ClientHandler client : clients) {
                if (!unfiltered.contains(client)) {
                    client.sendMessage(s);
                }
            }
        }

    public synchronized void messageToPrivateLogin(String nickName, String s) {
        for (ClientHandler client : clients) {
            if (client.getClientName().equals(nickName)) {
                client.sendMessage(s);
                break;
            }
        }
    }


    private int getHostPort() {
        Properties serverProperties = new Properties();
        int hostPort;
        try (InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
            serverProperties.load(inputStream);
            hostPort = Integer.parseInt(serverProperties.getProperty(HOST_PORT_PROP));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read application.properties file", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port value", e);
        }
        return hostPort;
    }
}
