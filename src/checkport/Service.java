/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package checkport;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Razorbreak
 */
public class Service extends Thread {
    
    private int port;
    private boolean running = true;
    private boolean stopped = false;
    
    public Service(String port){
        this.port = Integer.parseInt(port);
    }
    
    public void killService(){
        running = false;
    }
    
    @Override
    public void run(){
        int serverPort = this.port;
        ServerSocket listenSocket;
        Socket clientSocket;
        Connection c;
        try{
            listenSocket = new ServerSocket(serverPort);
            //while (this.running){
            clientSocket = listenSocket.accept();
            c = new Connection(clientSocket);
                //System.out.println("0");
            //}
            clientSocket.close();
        }
        catch(Exception e){
            //System.out.println("Error en socket: " + e.getMessage());
        }
        this.stopped = true;
    }
}
