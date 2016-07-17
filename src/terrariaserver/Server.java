/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terrariaserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.swing.JTextArea;

/**
 *
 * @author Razorbreak
 */
public class Server extends Thread{
    private Process p;//Proceso del servidor
    private InputStream is;
    private InputStream es;
    private OutputStream os;
    boolean ready=true;
    String TServerPath="TerrariaServer.exe";
    String world="world1";//Nombre mundo
    String worldpath="worlds";//Carpeta mundos /worlds
    String worldBackuppath;//Carpeta mundos [HOME]/worlds
    int worldsize=1;//1:pequeño, 2:mediano, 3:grande
    String ip="127.0.0.1";
    int port=7777;// 1024 <= port <= 65535
    String motd = "Hello!";
    int maxplayers=8;
    int secure=0;//0:inseguro, 1:seguro
    int priority=0;//0:Tiempo real, 1:Alto, 2:Medio-Alto, 3:Medio, 4:Medio-Bajo, 5:Bajo
    int language=5;//1:Inglés, 2:Alemán, 3:Italiano, 4:Francés, 5:Español
    String password="";
    private boolean status=false;//F:offline, T:online
    String banlist="banned.txt";
    private JTextArea con;
    private MsgAnalyzer msg;
    private Config cfg;
    private GUI gui;
    private String version="1.x.x.x.x";
    
    public Server(JTextArea console,GUI win,Config cfg){
        this.con = console;
        this.msg = new MsgAnalyzer(win);
        this.cfg = cfg;
        this.gui = win;
    }
    
    public boolean isOnline(){
        return this.status;
    }
    
    public boolean isSecured(){
        return this.secure==1;
    }
    
    public String getVersion(){
        return this.version;
    }
    
    public synchronized int startServer(){
        String vars=  " -autocreate "+this.worldsize
                    + " -worldname \""+this.world+"\""
                    + " -world \""+this.worldBackuppath+"\\"+this.world+".wld\""
                    //+ " -world \""+this.worldpath+"\\"+this.world+".wld\""
                    + " -port "+this.port
                    + " -motd \""+this.motd+"\""
                    + " -players "+this.maxplayers
                    + " -pass \""+this.password+"\""
                    + " -priority "+this.priority
                    + " -banlist \"banlist.txt\""
                    + " -secure "+this.secure
                    + " -lang "+this.language;
        
        try {
            System.out.println("Executing ==>\""+this.TServerPath+"\""+vars+"<==");
            p = Runtime.getRuntime().exec("\""+this.TServerPath+"\""+vars);
            is = p.getInputStream();
            es = p.getErrorStream();
            os = p.getOutputStream();
            this.status = true;
            return 0;
        } catch (Exception ex) {
            System.err.println("ERROR proceso: "+ex);
            return 1;
        }
    }
    
    public int execServer(String command){
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write(command+"\n");
            writer.flush();
            this.con.append(command+"\n");
            this.con.setCaretPosition(this.con.getDocument().getLength());
            return 0;
        } catch (Exception ex) {
            System.err.println("ERROR comando: "+ex);
            return 1;
        }
    }
    
    @Override
    public void run(){
        boolean ini = true;
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        String line;
        this.con.setText("");
        try {
            while((line=br.readLine())!=null){
                if(ini){ini=false;this.version=line.substring(17);}
                this.con.append(line+"\n");
                this.con.setCaretPosition(this.con.getDocument().getLength());
                try{
                    this.msg.analizarMensaje(line,this.language-1);
                }catch(Exception e){
                    System.err.println("ERROR analizador: "+e);
                }
            }
        } catch (IOException ex) {
            System.err.println("ERROR servidor: "+ex);
        }
        this.status=false;
        this.con.append("\nSERVIDOR FINALIZADO!");
        this.con.setCaretPosition(this.con.getDocument().getLength());
        this.gui.updateJS();
        this.ready=false;
        this.con.append("\n\nCopiando mundos...");
        this.con.setCaretPosition(this.con.getDocument().getLength());
        this.cfg.updateWorld(this.cfg.worldsBackup,this.cfg.worldsFolder,true);
        this.con.append("[Terminado]");
        this.ready=true;
    }
    
    @Override
    public String toString(){
        String s="";
        s+=      "TServerPath="+this.TServerPath+"\n"
                +"banlist="+this.banlist+"\n"
                +"password="+this.password+"\n"
                +"world="+this.world+"\n"
                +"worldpath="+this.worldpath+"\n"
                +"worldsize="+this.worldsize+"\n"
                +"language="+this.language+"\n"
                +"maxplayers="+this.maxplayers+"\n"
                +"port="+this.port+"\n"
                +"priority="+this.priority+"\n"
                +"secure="+this.secure+"\n"
                ;
        return s;
    }
}
