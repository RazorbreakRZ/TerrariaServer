/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package checkport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 *
 * @author Razorbreak
 */
class Connection extends Thread{
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    public Connection(Socket aSocket){
        try{
            clientSocket = aSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        }
        catch(Exception e){
            //System.out.println("Error en conexion: " + e.getMessage());
        }
    }
        
    public void run(){
        String data;
        try{
            data = in.readUTF();
            out.writeUTF("OK" + data);
        }catch(Exception e){

        }
    }
}