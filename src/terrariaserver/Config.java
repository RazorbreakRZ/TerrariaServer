/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terrariaserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 *
 * @author Razorbreak
 */
public class Config {
    String home = System.getProperty("user.home");// C:\Users\[username]
    String filename = "\\TerrariaServerConfig.ini";
    File folder;// C:\Users\[username]\
    File worldsFolder;// \worlds
    File worldsBackup;// C:\Users\[username]\worlds
    File bannedFile;
    String onHost="";
    String onIP="";
    String onPort="";
    //CONFIG
    boolean chkmsg=false;
    String msg="";
    int tmsg=0;
    boolean chkjs=false;
    File javascript = new File("statusServer.js");
    String tjs="5";
    //
    private Server ser;
    
    public Config(){
        folder = new File(home);
        worldsFolder = new File("worlds");
        if(!worldsFolder.exists()){
            worldsFolder.mkdir();
        }
        worldsBackup = new File(home+"\\worldsBackup");
        if(!worldsBackup.exists()){
            worldsBackup.mkdir();
        }
        bannedFile = new File("banned.txt");
    }
    
    public void loadConfiguration(Server ser){
        this.ser = ser;
        ser.worldBackuppath=this.worldsBackup.getPath();
        ser.motd=(new motd.motdTool()).generarMOTD();
        try {
            BufferedReader br = new BufferedReader(new FileReader(folder+filename));
            String line;
            while((line = br.readLine())!=null){
                if(line.contains("TServerPath=")){
                    ser.TServerPath=line.substring("TServerPath=".length());
                }else if(line.contains("world=")){
                    ser.world=line.substring("world=".length());
                }else if(line.contains("worldpath=")){
                    ser.worldpath=line.substring("worldpath=".length());
                }else if(line.contains("banlist=")){
                    ser.banlist=line.substring("banlist=".length());
                }else if(line.contains("language=")){
                    ser.language=Integer.parseInt(line.substring("language=".length()));
                }else if(line.contains("maxplayers=")){
                    ser.maxplayers=Integer.parseInt(line.substring("maxplayers=".length()));
                }else if(line.contains("secure=")){
                    ser.secure=Integer.parseInt(line.substring("secure=".length()));
                }else if(line.contains("priority=")){
                    ser.priority=Integer.parseInt(line.substring("priority=".length()));
                }else if(line.contains("port=")){
                    ser.port=Integer.parseInt(line.substring("port=".length()));
                }else if(line.contains("password=")){
                    ser.password=line.substring("password=".length());
                }else if(line.contains("worldsize=")){
                    ser.worldsize=Integer.parseInt(line.substring("worldsize=".length()));
                }else if(line.contains("chkmsg=")){
                    this.chkmsg = line.substring("chkmsg=".length()).equals("true");
                }else if(line.contains("chkjs=")){
                    this.chkjs = line.substring("chkjs=".length()).equals("true");
                }else if(line.contains("tmsg=")){
                    this.tmsg = Integer.parseInt(line.substring("tmsg=".length()));
                }else if(line.contains("msg=")){
                    this.msg = line.substring("msg=".length());
                }else if(line.contains("tjs=")){
                    this.tjs = line.substring("tjs=".length());
                }else if(line.contains("jsPath=")){
                    this.javascript = new File(line.substring("jsPath=".length()));
                }
            }
            br.close();
        }catch(Exception e){
            File f = new File(this.folder+filename);
            try {
                f.createNewFile();
                this.saveConfiguration(ser);
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }
    
    public void loadBanned(ArrayList<Jugador> bannedList){
        if(bannedFile.exists()){
            try{
                BufferedReader br = new BufferedReader(new FileReader(bannedFile));
                String line,pj,ip;
                while((line = br.readLine())!=null){
                    if(line.length()>7){
                        ip = line.substring(0,line.indexOf(" "));
                        pj = line.substring(ip.length()+1,line.length());
                        bannedList.add(new Jugador(pj,ip));
                    }
                }
                br.close();
            }catch(Exception e){
                System.err.println("Excepcion al cargar lista baneados:\n"+e);
            }
        }
    }
    
    public void saveBannedList(ArrayList<Jugador> bannedList){
        if(!bannedFile.exists()){
            try {
                bannedFile.createNewFile();
            } catch (IOException ex) {
                System.err.println("Excepcion al crear fichero baneados:\n"+ex);
            }
        }
        if(bannedList.size()>0){
            try{
                String baneados="";
                for(int i=0;i<bannedList.size();i++){
                    Jugador n = bannedList.get(i);
                    baneados+=n.getIP()+" "+n.getName()+"\n";
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(bannedFile));
                bw.write(baneados);
                bw.close();
            }catch(Exception e){
                System.err.println("Excepcion al guardar lista baneados:\n"+e);
            }
        }else{
            bannedFile.delete();
        }
    }
    
    public void setConfig(boolean chkmsg,String msg,int tmsg,boolean chkjs,String js,String tjs){
        this.chkmsg = chkmsg;
        this.msg = msg;
        this.tmsg = tmsg;
        this.chkjs = chkjs;
        this.javascript = new File(js);
        this.tjs = tjs;
    }
    
    public void saveConfiguration(Server ser){
        String configFile="[CONFIG]\n"
                +"chkmsg="+this.chkmsg
                +"\nmsg="+this.msg
                +"\ntmsg="+this.tmsg
                +"\nchkjs="+this.chkjs
                +"\njsPath="+this.javascript.getPath().toString()
                +"\ntjs="+this.tjs;
        configFile+="\n[SERVER]\n"+ser.toString();
        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(folder+filename));
            bw.write(configFile);
            bw.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    
    public void updateWorld(File origen, File destino,boolean deleteOrigen){
        File[] wfiles = origen.listFiles();
        for(int f=0;f<wfiles.length;f++){
            if(wfiles[f].getName().matches(ser.world+"\\.wld.*")){
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = new FileInputStream(origen+"\\"+wfiles[f].getName());
                    out = new FileOutputStream(destino+"\\"+wfiles[f].getName());
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len=in.read(buf))>0){
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                    if(deleteOrigen){
                        wfiles[f].delete();
                    }
                }catch (FileNotFoundException ex) {
                    System.err.println(ex);
                }catch (IOException ex) {
                    System.err.println(ex);
                }
            }
        }
        //if(deleteOrigen){origen.delete();}
    }
    
    public boolean servidorOnline(){
        File on = new File("online.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(on));
            this.onHost = br.readLine();
            this.onIP = br.readLine();
            this.onPort = br.readLine();
            br.close();
        }catch (IOException ex) {
            System.err.println("El servidor no esta online. Puede ser lanzado.");
        }
        return on.exists();
    }
    
    public void crearFicheroStatus(boolean isOnline){
        File on = new File("online.txt");
        if(isOnline){
            try {
                on.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(on));
                bw.write(System.getProperty("user.name")+"\n"+this.ser.ip+"\n"+this.ser.port);
                bw.close();
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }else{
            on.delete();
        }
    }
    
    public void updateJavascript(ArrayList<Jugador> players,String time){
        try {
            this.javascript.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.javascript));
            String codigoJS="";
            //
            if(this.ser.isOnline()){
                codigoJS+="function getHostName(){return \""+System.getProperty("user.name")+"\";}\n";
                codigoJS+="function getServerVersion(){return \""+this.ser.getVersion()+"\";}\n";
                codigoJS+="function getServerIP(){return \""+this.ser.ip+"\";}\n";
                codigoJS+="function getServerPort(){return "+this.ser.port+";}\n";
                codigoJS+="function getTimeOnline(){return \""+time.substring(0,time.length()-3)+"\";}\n";
                codigoJS+="function getWorldName(){return \""+this.ser.world+"\";}\n";
                codigoJS+="function getMaxPlayers(){return "+this.ser.maxplayers+";}\n";
                codigoJS+="function getNumberOfPlayers(){return "+players.size()+";}\n";
                codigoJS+="function hasPassword(){return "+!this.ser.password.equals("")+";}\n";
                codigoJS+="function isOnline(){return "+this.ser.isOnline()+";}\n";
                codigoJS+="function isSecured(){return "+this.ser.isSecured()+";}\n";
                codigoJS+="function getPlayersList(){var list; list=new Object();";
                for(int i=0;i<players.size();i++){
                    codigoJS+="list["+i+"]=\""+players.get(i).getName()+"\";";
                }
                codigoJS+=" return list;}\n";
            }else{
                codigoJS+="function getHostName(){return \"---\";}\n";
                codigoJS+="function getServerVersion(){return \""+this.ser.getVersion()+"\";}\n";
                codigoJS+="function getServerIP(){return \"---.---.---.---\";}\n";
                codigoJS+="function getServerPort(){return \"---\";}\n";
                codigoJS+="function getTimeOnline(){return \"00:00\";}\n";
                codigoJS+="function getWorldName(){return \"---\";}\n";
                codigoJS+="function getMaxPlayers(){return "+this.ser.maxplayers+";}\n";
                codigoJS+="function getNumberOfPlayers(){return "+players.size()+";}\n";
                codigoJS+="function hasPassword(){return "+!this.ser.password.equals("")+";}\n";
                codigoJS+="function isOnline(){return "+this.ser.isOnline()+";}\n";
                codigoJS+="function isSecured(){return "+this.ser.isSecured()+";}\n";
                codigoJS+="function getPlayersList(){var list; list=new Object(); return list;}\n";
            }
            //
            bw.write(codigoJS);        
            bw.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    
    public void deleteJavascript(){
        if(this.javascript.exists()){
            this.javascript.delete();
        }
    }
    
    public boolean worldsBackupExist(){
        return this.worldsBackup.exists();
    }
}
