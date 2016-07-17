/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Razorbreak
 */
public class motdTool {
        
    
    public motdTool(){
        
    }
    
    public String generarMOTD(){
        String motd="Powered by "+System.getProperty("user.name");
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        
        try {
            archivo = new File ("motds.txt");
            fr = new FileReader (archivo);
            br = new BufferedReader(fr);
            String linea;
            ArrayList<String> motds = new ArrayList<>();
            //motds.add(motd);
            while((linea=br.readLine())!=null){
                motds.add(linea);
            }
            if(!motds.isEmpty()){
                if(motds.size()>1){
                    Random rand = new Random();
                    int x = rand.nextInt(motds.size());
                    motd = motds.get(x);
                }else{
                    motd = motds.get(0);
                }
            }
            
        } catch (IOException ex) {
        }finally{
            try{                    
               if( null != fr ){   
                  fr.close();     
               }                  
            }catch (Exception e2){ 
                //NOTHING
            }
        }
        return motd;
    }
    
}
