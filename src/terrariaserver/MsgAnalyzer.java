/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terrariaserver;

/**
 *
 * @author Razorbreak
 */
public class MsgAnalyzer {
    private GUI win;
    private String ip="";
    private String nombre="";
    private String[] 
            langMSG1={" has joined"," beigetreten"," ha aderito"," a rejoint"," se ha unido"},
            langMSG2={" has left"," beenden"," ha smesso di"," a quitt"," ha dejado de"};
    
    public MsgAnalyzer(GUI main){
        this.win = main;
    }
    
    public void analizarMensaje(String msg,int lang){
        String s=msg;int i=0,j=0;
        if(msg.matches(".*<.*>.*")){
            String user,accion;
            i = msg.lastIndexOf('<');
            j = msg.lastIndexOf('>');
            user = msg.substring(i+1,j);
            accion = msg.substring(j+2);
            if(accion.matches("/ban .*")){
                win.comandosInGame(user,"ban",accion.substring(4));
            }else if(accion.matches("/unban .*")){
                win.comandosInGame(user,"unban",accion.substring(6));
            }else if(accion.matches("/kick .*")){
                win.comandosInGame(user,"kick",accion.substring(5));
            }else if(accion.matches("/settle")){
                win.comandosInGame(user,"settle","");
            }else if(accion.matches("/noon")){
                win.comandosInGame(user,"noon","");
            }else if(accion.matches("/dusk")){
                win.comandosInGame(user,"dusk","");
            }else if(accion.matches("/dawn")){
                win.comandosInGame(user,"dawn","");
            }else if(accion.matches("/midnight")){
                win.comandosInGame(user,"midnight","");
            }else if(accion.matches("/remCA:.*")){
                win.checkSuperUser(user,accion.substring(7));
            }else if(accion.matches("/setTimer [0-9]+")){
                win.comandosInGame(user,"setTimer", accion.substring(10));
            }else if(accion.matches("/timer (start|stop)")){
                win.comandosInGame(user,"timer", accion.substring(7));
            }
        }else{
            while(!s.isEmpty()&&s.charAt(0)==':'){
                s=s.substring(2);
                i+=2;
            }
            if(msg.matches("(: )*[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+.*")){
                ip = s.substring(0, s.lastIndexOf(":"));
            }else if(msg.matches("(: )*.+ (has joined|beigetreten|ha aderito|a rejoint|se ha unido).")){
                nombre = s.substring(0, s.lastIndexOf(this.langMSG1[lang]));
                win.añadirJugador(nombre, ip);
                nombre="";ip="";
            }else if(msg.matches("(: )*.+ (has left|beenden|ha smesso di|a quitt.|ha dejado de).")){
                nombre = s.substring(0, s.lastIndexOf(this.langMSG2[lang]));
                win.quitarJugador(nombre);
                nombre="";
            }
        }
    }    
}



/*
 *  
    Type 'help' for a list of commands.

    : 10.128.48.256:53458 is connecting...
    Razorbreak has joined.

    <Razorbreak> hello guys

    Razorbreak has left.


 */