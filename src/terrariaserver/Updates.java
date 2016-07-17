/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package terrariaserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * @author Razorbreak
 */
public class Updates extends Thread{
    private String currentAppVersion="1.0.1.1";
    private int currentAppBuild=13103002;
    private String msg = java.util.ResourceBundle.getBundle("lang/language").getString("Updates_msg");
    private String url = "https://dl.dropboxusercontent.com/u/62477074/Proyectos/TerrariaServer/currentVersion.txt";
    private String newVer[] = null;
    java.awt.Frame mainW;
    
    public Updates(java.awt.Frame parent){
        mainW = parent;
    }
    
    private boolean comprobarAct(){
        try {
            URL webpage = new URL(this.url);
            BufferedReader in = new BufferedReader(new InputStreamReader(webpage.openStream()));
            newVer = in.readLine().split(" ");
            System.out.println("Last version: "+newVer[0]+" ("+newVer[1]+")");
            if(Integer.parseInt(newVer[1])>this.currentAppBuild){
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            System.out.println("ERROR: "+e);
            return false;
        }
    }
    
    @Override
    public void run(){
        if(comprobarAct()){
            UpdatesDialog a = new UpdatesDialog(mainW,true);
            a.setLabel(msg+newVer[0]+" ("+newVer[1]+").");
            a.setVisible(true);
        }
    }
}
