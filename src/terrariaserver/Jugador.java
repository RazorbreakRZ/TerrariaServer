/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terrariaserver;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 *
 * @author Razorbreak
 */
public class Jugador {
    private String nombre="";
    private String ip="";
    private boolean special=false;
    private boolean isBanned=false;
    
    public Jugador(String pj,String ip){
        this.nombre = pj;
        this.ip = ip;
        this.special = this.ip.equals("127.0.0.1")||this.ip.equals("localhost");
    }
    
    public String getName(){
        return this.nombre;
    }
    
    public String getIP(){
        return this.ip;
    }
    
    public void setSuperUser(){
        this.special = true;
    }
    
    public String genCode(){
        String s="";
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        String str = this.nombre+c.get(Calendar.DATE)+c.get(Calendar.MONTH)+c.get(Calendar.YEAR)+c.get(Calendar.HOUR)+c.get(Calendar.MINUTE);
        byte bytes[] = str.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes,0,bytes.length);
        long lngChecksum = checksum.getValue();
        s+=lngChecksum;
        return s;
    }
    
    public boolean isSpecial(){
        return this.special;
    }
    
    public void ban(){
        this.isBanned = true;
    }
    
    public void unban(){
        this.isBanned = false;
    }
    
    public boolean isBanned(){
        return this.isBanned;
    }
    
    @Override
    public String toString(){
        String s="";
        s+=this.nombre+" ("+this.ip+")";
        return s;        
    }
}
