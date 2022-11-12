package p2p_chat_auth_server;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class AuthServer extends Thread{
    private Socket socket;
    public static int AUTH_SERVER_PORT = 8585;
    
    private AuthServer(Socket socket) {
        this.socket=socket;
    }
    
    public static void main(String[] args) throws IOException {
        ServerSocket ssocket = new ServerSocket(AUTH_SERVER_PORT);
        System.out.println("Authentication Server is running on port " + AUTH_SERVER_PORT);
        while(true) {
            Socket socket = ssocket.accept();
            new AuthServer(socket).start();
        }
    }

    @Override
    public void run() {
        String ip =  ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().toString().substring(1);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String type = br.readLine().trim();
            switch (type) {
                case "Add Friend" -> addFriend(br, bw);
                case "Login" -> checkLogin(br, bw, true, ip);
                case "Register" -> doRegister(br, bw, ip);
                case "Friends" -> getFriends(br, bw, ip);
            }
            bw.flush();
            socket.close();
        } catch (IOException ignored) {

        }
        
    }
    
    private void addFriend(BufferedReader br, BufferedWriter bw)  throws IOException {
        String name = br.readLine().trim();
        String pwd = br.readLine();
        String friend = br.readLine().trim();
        bw.write(AuthData.getInstance().addFriend(name,pwd,friend));
        bw.flush();
    }

    private boolean checkLogin(BufferedReader br, BufferedWriter bw, boolean b, String ip) throws IOException {
        String name=br.readLine().trim();
        String pwd = br.readLine();
        
        if(AuthData.getInstance().isUser(name)) {
            
            if(AuthData.getInstance().checkPassword(name, pwd)) {
                AuthData.getInstance().updateIP(name,ip);
                AuthData.getInstance().dataUpdated();
                System.out.println(name + " logged in from " + ip);
                if(!b)
                    return true;
                else
                    bw.write("DONE");
                bw.flush();
                return true;
            }
            else {
                bw.write("Wrong Password");
                System.out.println(name + " from " + ip + " failed to log in. Reason: Wrong password");
            }
        }
        else {
            bw.write("Username does not exist");
            System.out.println(name + " from " + ip + " failed to log in. Reason: Username does not exist");
        }
        bw.flush();
        return false;
    }

    private void doRegister(BufferedReader br, BufferedWriter bw, String ip)  throws IOException {
        String name=br.readLine().trim();
        String pwd = br.readLine();
        bw.write(AuthData.getInstance().register(name, pwd, ip));
        bw.flush();
    }

    private void getFriends(BufferedReader br, BufferedWriter bw, String ip)  throws IOException {
        String name = br.readLine().trim();
        String pwd = br.readLine();
        bw.write(AuthData.getInstance().getFriends(name,pwd, ip));
        bw.flush();
    }
    
}
