/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terrariaserver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.Timer;

/**
 *
 * @author Razorbreak
 */
public class Reloj extends Thread{
    private Timer timer;
    private int s=0,m=0,h=0,d=0;
    private String ss,mm,hh,dd;
    private JLabel tempo;
    
    public Reloj(int duration,JLabel temp){
        this.tempo = temp;
        this.setReloj(duration);
        this.resetReloj();
    }
    
    public void setReloj(int duration){
        timer = new Timer(duration,new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                //Cálculos reloj
                s++;
                if(s==60){
                    s=0;
                    m++;
                    if(m==60){
                        m=0;
                        h++;
                        if(h==24){
                            h=0;
                            d++;
                        }
                    }
                }
                //Representación gráfica reloj
                if(s<10){ss="0"+s;}else{ss=""+s;}
                if(m<10){mm="0"+m;}else{mm=""+m;}
                if(h<10){hh="0"+h;}else{hh=""+h;}
                if(d>0){dd=d+"d";}else{dd="";}
                tempo.setText(dd+" "+hh+":"+mm+":"+ss);
            }//Fin evento reloj
        });
    }
    
    public void resetReloj(){
        this.s=0;
        this.m=0;
        this.h=0;
        this.d=0;
        tempo.setText("00:00:00");
    }
    
    public void startReloj(){
        this.resetReloj();
        this.timer.start();
    }
    
    public void stopReloj(){
        this.timer.stop();
    }
}
