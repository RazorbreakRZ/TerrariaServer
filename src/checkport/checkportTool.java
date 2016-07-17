/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package checkport;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JTextArea;

/**
 *
 * @author Razorbreak
 */
public class checkportTool extends Thread{
    private JTextArea resultados;
    private String[] listaPuertos;
    private JButton boton;
    
    public checkportTool(JTextArea listado,String listaPuertos,JButton boton){
        this.resultados = listado;
        this.boton = boton;
        this.listaPuertos = listaPuertos.replaceAll(" ", "").split(",");
    }
    
    public void comprobarPuertos(String[] args){
        if(args.length>0){
            String status;
            String varValue;
            String url = "http://www.canyouseeme.org/";
            Map<String,String> data;
            URLConection con = new URLConection();
            Analyzer ana = new Analyzer();
            for(int i=0;i<args.length;i++){
                varValue = args[i];
                if(varValue.matches("^[0-9]+$") && varValue.length()<6 && Integer.parseInt(varValue)<=65535){
                    Service ser = new Service(varValue);
                    ser.start();
                    status = String.format("%1$5s",varValue);
                    data = new HashMap<>();
                    data.put("port",varValue);
                    data.put("submit","OK");
                    if(con.doSubmit(url,data)==0){
                        int portStatus = ana.analyzeResponse(con.getResponse());//1=opened 0=closed -1=error
                        if(portStatus==1){
                            status += "...OK";
                        }else if(portStatus==0){
                            status += "...Error: not available";
                        }
                        this.resultados.append(status+"\n");
                    }
                    ser.killService();
                }else{
                    this.resultados.append(String.format("%1$5s",varValue)+"...Error: invalid port\n");
                }
                this.resultados.setCaretPosition(this.resultados.getDocument().getLength());
            }
        }
    }
    
    @Override
    public void run(){
        this.boton.setEnabled(false);
        this.comprobarPuertos(this.listaPuertos);
        this.boton.setEnabled(true);
    }
}
