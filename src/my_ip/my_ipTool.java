/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my_ip;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JTextField;

/**
 *
 * @author Razorbreak
 */
public class my_ipTool extends Thread{
    private String publicIP="0.0.0.0";
    private JTextField salida;
    private JButton boton;
    private terrariaserver.GUI father;
    private boolean onlyCheck=false;
    
    public my_ipTool(terrariaserver.GUI gui){
        this.father = gui;
        this.onlyCheck = true;
    }
    
    public my_ipTool(JTextField jtf,JButton jb,terrariaserver.GUI gui){
        this.salida = jtf;
        this.boton = jb;
        this.father = gui;
    }
    
    public String get_public_ip(){
        try {
            URL tempURL = new URL("http://myip.xname.org/");
            HttpURLConnection tempConn = (HttpURLConnection)tempURL.openConnection();
            InputStream tempInStream = tempConn.getInputStream();
            InputStreamReader tempIsr = new InputStreamReader(tempInStream);
            BufferedReader tempBr = new BufferedReader(tempIsr);        
                        
            publicIP = tempBr.readLine();

            tempBr.close();
            tempInStream.close(); 
        } catch (Exception ex) {
                publicIP = "127.0.0.1";   
        }
        return publicIP;
    }
    
    @Override
    public void run(){
        if(this.onlyCheck){
            father.actualizarIP(this.get_public_ip());
        }else{
            salida.setText(this.get_public_ip());
            father.actualizarIP(publicIP);
            salida.setFocusable(true);
            boton.setEnabled(true);        
        }
    }
}
