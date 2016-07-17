/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terrariaserver;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author Razorbreak
 */
public class GUI extends javax.swing.JFrame {
    String currentIP="";
    private Server ser;
    private Reloj reloj;
    private Timer tempOff,tempMsg,tempJs;
    private int countTempOff,countTempMsg,countTempJs;
    private int duration=1000;
    private Config cfg;
    private ArrayList<Jugador> players = new ArrayList();
    private ArrayList<Jugador> banned = new ArrayList();
    private DefaultListModel lmP=new DefaultListModel();
    private DefaultListModel lmB=new DefaultListModel();
    private Thread reader;
    private int indiceSeleccion=0;
    private boolean wasOnline=false;
    /**
     * Creates new form GUI
     */
    public GUI() {
        initComponents();
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/icon.png")));
        this.setLocationRelativeTo(null);
        this.jTextArea_SalidaConsola.setText("");
        reloj = new Reloj(this.duration,this.jLabel_TimeOnline);
        this.cfg = new Config();
        this.ser = new Server(this.jTextArea_SalidaConsola,this,this.cfg);
        this.cfg.loadConfiguration(ser);
        this.cfg.loadBanned(banned);
        this.listadoJugadores();
        this.cargarVariablesConfig();
        this.comprobarIP();
        this.checkUpdates();
    }
    
    public void checkUpdates(){
        Thread upd = new Thread(new Updates(this));
        upd.start();
    }
    
    public void updateJS(){
        if(this.jCheckBox_autoJavascript.isSelected()){
            this.cfg.updateJavascript(players,this.jLabel_TimeOnline.getText());
        }
    }
    
    public void comprobarIP(){
        Thread p = new Thread(new my_ip.my_ipTool(this));
        p.start();
    }
    
    public void gestionarTemporizador(int tempType,boolean start){
        if(tempType==0){
            if(start){
                this.tempOff = new Timer(duration,new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        countTempOff++;
                        int dif = Integer.parseInt(jFormattedTextField_tiempoAutoapagado.getText())*60 - countTempOff;
                        if(dif<=0){
                            pararServidor("exit");
                        }else{
                            int values[] = {1,2,3,4,5,10,15,20,30,45,60,120,180,240,300,600,900,1800,3600};
                            for(int i=0;i<values.length;i++){
                                if(dif==values[i]){
                                    String mensajeOff=java.util.ResourceBundle.getBundle("lang/language").getString("EL SERVIDOR SE CERRARÁ EN ");
                                    if(values[i]==3600){
                                        mensajeOff += (values[i]/3600)+java.util.ResourceBundle.getBundle("lang/language").getString("Timer_Hora");
                                    }else if(values[i]>60){
                                        mensajeOff += (values[i]/60)+java.util.ResourceBundle.getBundle("lang/language").getString("Timer_Minutos");
                                    }else{
                                        mensajeOff += values[i]+java.util.ResourceBundle.getBundle("lang/language").getString("Timer_Segundos");
                                    }
                                    ser.execServer("say "+mensajeOff);
                                }
                            }
                        }
                    }});
                this.tempOff.start();
            }else{
                this.tempOff.stop();
                this.countTempOff=0;
            }
        }else if(tempType==1){
            if(start){
                this.tempMsg = new Timer(duration,new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        countTempMsg++;
                        if(countTempMsg>=Integer.parseInt((String)jComboBox_tiempoAutomensaje.getSelectedItem())*60){
                            ser.execServer("say "+jTextField_automensaje.getText());
                            countTempMsg=0;
                        }
                    }});
                this.tempMsg.start();
            }else{
                this.tempMsg.stop();
                countTempMsg=0;
            }
        }else if(tempType==2){
            if(start){
                cfg.updateJavascript(players,this.jLabel_TimeOnline.getText());
                this.tempJs = new Timer(duration,new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //Accion cada "duration" ms.
                        countTempJs++;
                        if(countTempJs>=Integer.parseInt(jFormattedTextField_tiempoJavascript.getText())*60){
                            cfg.updateJavascript(players,jLabel_TimeOnline.getText());
                            countTempJs=0;
                        }
                    }});
                this.tempJs.start();
            }else{
                this.tempJs.stop();
                this.countTempJs=0;
            }
        }
    }
    
    public void actualizarIP(String newIP){
        this.currentIP = newIP;
        this.ser.ip = newIP;
    }
        
    public void cerrarAplicacion(){
        if(!this.ser.isOnline()){
            if(this.ser.ready){
                this.cfg.setConfig(this.jCheckBox_automensajes.isSelected(), this.jTextField_automensaje.getText()
                        , this.jComboBox_tiempoAutomensaje.getSelectedIndex(), this.jCheckBox_autoJavascript.isSelected()
                        , this.jTextField_rutaJavascript.getText(), this.jFormattedTextField_tiempoJavascript.getText());
                this.cfg.saveConfiguration(ser);
                this.cfg.saveBannedList(banned);
                System.exit(0);
            }else{
                JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("lang/language").getString("ESPERE A QUE TERMINEN DE ACTUALIZARSE LOS FICHEROS DEL MUNDO."), java.util.ResourceBundle.getBundle("lang/language").getString("AVISO!"), JOptionPane.WARNING_MESSAGE);
            }
        }else{
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("lang/language").getString("EL SERVIDOR AÚN ESTÁ EN FUNCIONAMIENTO,\\NCIERRALO ANTES DE PROSEGUIR."), java.util.ResourceBundle.getBundle("lang/language").getString("AVISO!"), JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void cargarVariablesConfig(){
        this.jTextField_DirBanlist.setText(this.ser.banlist);
        this.jTextField_DirTServer.setText(this.ser.TServerPath);
        this.jTextField_DirWorlds.setText(this.ser.worldpath);
        this.jTextField_DirWorldsBackup.setText(this.ser.worldBackuppath);
        this.jTextField_WorldName.setText(this.ser.world);
        this.jTextField_Motd.setText(this.ser.motd);
        this.jComboBox_Worldsize.setSelectedIndex(this.ser.worldsize-1);
        this.jComboBox_SerPriority.setSelectedIndex(this.ser.priority);
        this.jComboBox_SerLanguage.setSelectedIndex(this.ser.language-1);
        this.jTextField_SerPassword.setText(this.ser.password);
        this.jFormattedTextField_MaxNumPlayers.setText(""+this.ser.maxplayers);
        this.jFormattedTextField_SerPort.setValue(this.ser.port);
        this.jCheckBox_SerSecure.setSelected(this.ser.secure==1);
        this.jButton_GuardarConfig.setEnabled(false);
        //
        this.jCheckBox_automensajes.setSelected(this.cfg.chkmsg);
        this.jTextField_automensaje.setText(this.cfg.msg);
        this.jTextField_automensaje.setEnabled(this.cfg.chkmsg);
        this.jComboBox_tiempoAutomensaje.setSelectedIndex(this.cfg.tmsg);
        this.jComboBox_tiempoAutomensaje.setEnabled(this.cfg.chkmsg);
        this.jCheckBox_autoJavascript.setSelected(this.cfg.chkjs);
        this.jTextField_rutaJavascript.setText(this.cfg.javascript.getPath().toString());
        this.jButton_dirJavascript.setEnabled(this.cfg.chkjs);
        this.jFormattedTextField_tiempoJavascript.setText(this.cfg.tjs);
        this.jFormattedTextField_tiempoJavascript.setEnabled(this.cfg.chkjs);
    }
    
    public void ejecutarComando(){
        String command = this.jTextField_EntradaManual.getText();
        if(command.equals("exit")||command.equals("exit-nosave")){
            this.pararServidor(command);
        }else if(command.equals("clear")){
            this.jTextArea_SalidaConsola.setText("");
        }else{
            this.ser.execServer(this.jTextField_EntradaManual.getText());
        }
        this.jTextField_EntradaManual.setText("");
    }
    
    public void comandosInGame(String usuario,String comando,String parametro){
        Jugador player;
        for(int i=0;i<this.players.size();i++){
            player = this.players.get(i);
            if(player.getName().equals(usuario)&&player.isSpecial()){
                if(comando.equals("ban")){
                    Jugador pj;
                    for(int j=0;j<this.players.size();j++){
                        if((pj=this.players.get(j)).getName().equals(parametro.substring(1))){
                            pj.ban();
                            this.banned.add(pj);
                            this.players.remove(j);
                            this.jList_Jugadores.clearSelection();
                            this.jList_Baneados.clearSelection();
                            this.listadoJugadores();
                            this.jButton_kick.setEnabled(false);
                            this.jButton_ban.setEnabled(false);
                            this.ser.execServer("kick "+pj.getName());
                            break;
                        }
                    }
                }else if(comando.equals("unban")){
                    Jugador pj;
                    for(int j=0;j<this.banned.size();j++){
                        if((pj=this.banned.get(j)).getName().equals(parametro.substring(1))){
                            pj.unban();
                            this.banned.remove(j);
                            this.jList_Jugadores.clearSelection();
                            this.jList_Baneados.clearSelection();
                            this.listadoJugadores();
                            this.jButton_kick.setEnabled(false);
                            this.jButton_ban.setEnabled(false);
                            break;
                        }
                    }
                }else if(comando.equals("timer")){
                    if(this.jCheckBox_autoapagado.isSelected()){
                        if(parametro.equals("start")&&this.jCheckBox_autoapagado.isEnabled()){
                            this.jButton_IniciarTempoApagado.setText(java.util.ResourceBundle.getBundle("lang/language").getString("PARAR"));
                            this.jCheckBox_autoapagado.setEnabled(false);
                            this.jFormattedTextField_tiempoAutoapagado.setEnabled(false);
                            this.gestionarTemporizador(0, true);
                            this.ser.execServer("say Timer running.");
                        }else if(!this.jCheckBox_autoapagado.isEnabled()){
                            this.jButton_IniciarTempoApagado.setText(java.util.ResourceBundle.getBundle("lang/language").getString("INICIAR"));
                            this.jCheckBox_autoapagado.setEnabled(true);
                            this.jFormattedTextField_tiempoAutoapagado.setEnabled(true);
                            this.gestionarTemporizador(0, false);
                            this.ser.execServer("say Timer stopped.");
                        }
                    }else{
                        this.ser.execServer("You must enable the timer using /setTimer");
                    }
                }else if(comando.equals("setTimer")){
                    if(this.jCheckBox_autoapagado.isEnabled()){
                        if(parametro.equals("0")){
                            this.jCheckBox_autoapagado.setSelected(false);
                            this.jFormattedTextField_tiempoAutoapagado.setEnabled(false);
                            this.jButton_IniciarTempoApagado.setEnabled(false);
                            this.jFormattedTextField_tiempoAutoapagado.setText("30");
                            this.ser.execServer("say Timer deactivated.");
                        }else{
                            this.jCheckBox_autoapagado.setSelected(true);
                            this.jFormattedTextField_tiempoAutoapagado.setEnabled(true);
                            this.jButton_IniciarTempoApagado.setEnabled(true);
                            this.jFormattedTextField_tiempoAutoapagado.setText(parametro);
                            this.ser.execServer("say Timer ready.");
                        }
                    }else{
                        this.ser.execServer("say You must stop the current timer.");
                    }
                }else{
                    this.ser.execServer(comando+parametro);
                    break;
                }
                break;
            }
        }
    }
        
    public void checkSuperUser(String usuario, String code){
        Jugador player;
        for(int i=0;i<this.players.size();i++){
            player = this.players.get(i);
            if(player.getName().equals(usuario)){
                if(code.equals(player.genCode())){
                    player.setSuperUser();
                    this.ser.execServer("say *** Remote Control Access ***");
                    this.ser.execServer("say --- Username: "+usuario);
                    this.ser.execServer("say *** ACCESS GRANTED ***");
                }else{
                    this.ser.execServer("say *** Remote Control Access ***");
                    this.ser.execServer("say --- Username: "+usuario);
                    this.ser.execServer("say *** ACCESS DENIED ***");
                }
                break;
            }
        }
    }
    
    public void añadirJugador(String name,String ip){
        boolean exist=false;
        Jugador pj;
        for(int i=0;i<this.banned.size();i++){
            pj = this.banned.get(i);
            if(pj.getName().equals(name)||pj.getIP().equals(ip)){
                exist=true;
                this.ser.execServer("kick "+name);
                break;
            }
        }
        if(!exist){
            this.players.add(new Jugador(name,ip));
        }
        this.listadoJugadores();
    }
    
    public void quitarJugador(String name){
        Jugador player;
        for(int i=0;i<this.players.size();i++){
            player = this.players.get(i);
            if(player.getName().equals(name)){
                this.players.remove(i);
                break;
            }
        }
        this.listadoJugadores();
    }
    
    public void banearJugador(boolean ban){
        Jugador pj;
        int index;
        if(ban && this.jList_Jugadores.getSelectedIndex()>=0){
            index = this.jList_Jugadores.getSelectedIndex();
            pj = this.players.get(index);
            pj.ban();
            this.banned.add(pj);
            this.players.remove(index);
            this.listadoJugadores();
            this.jList_Jugadores.clearSelection();
            this.jButton_kick.setEnabled(false);
            this.jButton_ban.setEnabled(false);
            this.ser.execServer("kick "+pj.getName());
        }else if(this.jList_Baneados.getSelectedIndex()>=0){
            index = this.jList_Baneados.getSelectedIndex();
            pj = this.banned.get(index);
            pj.unban();
            this.banned.remove(index);
            this.listadoJugadores();
            this.jList_Baneados.clearSelection();
            this.jButton_kick.setEnabled(false);
            this.jButton_ban.setEnabled(false);
        }
    }
            
    public void listadoJugadores(){
        lmP.clear();
        lmB.clear();
        for(int i=0;i<this.players.size();i++){
            lmP.addElement(this.players.get(i).toString());
        }
        for(int i=0;i<this.banned.size();i++){
            lmB.addElement(this.banned.get(i).toString());
        }
        this.jList_Jugadores.setModel(lmP);
        this.jList_Baneados.setModel(lmB);
        String title=java.util.ResourceBundle.getBundle("lang/language").getString("JUGADORES ONLINE (")+this.players.size()+")";
        this.jList_Jugadores.setEnabled(this.players.size()>0);
        this.jList_Baneados.setEnabled(this.banned.size()>0);
        this.jPanel_Jugadores.setBorder(BorderFactory.createTitledBorder(title));
    }
    
    public void habilitarOpcionesAvanzadas(boolean habilitar){
        this.jButton_dawn.setEnabled(habilitar);
        this.jButton_dusk.setEnabled(habilitar);
        this.jButton_noon.setEnabled(habilitar);
        this.jButton_midnight.setEnabled(habilitar);
        //
        this.jButton_settle.setEnabled(habilitar);
        //
        this.jCheckBox_automensajes.setEnabled(!habilitar);        
        this.jTextField_automensaje.setEnabled(this.jCheckBox_automensajes.isSelected()&&!habilitar);
        this.jComboBox_tiempoAutomensaje.setEnabled(this.jCheckBox_automensajes.isSelected()&&!habilitar);
        //
        this.jCheckBox_autoapagado.setEnabled(habilitar);
        this.jFormattedTextField_tiempoAutoapagado.setEnabled(this.jCheckBox_autoapagado.isSelected()&&habilitar);
        this.jButton_IniciarTempoApagado.setEnabled(this.jCheckBox_autoapagado.isSelected()&&habilitar);
        //
        this.jCheckBox_autoJavascript.setEnabled(!habilitar);
        this.jButton_dirJavascript.setEnabled(this.jCheckBox_autoJavascript.isSelected()&&!habilitar);
        this.jFormattedTextField_tiempoJavascript.setEnabled(this.jCheckBox_autoJavascript.isSelected()&&!habilitar);
    }
    
    public void iniciarServidor(){
        if(!this.cfg.servidorOnline()){
            this.cfg.updateWorld(this.cfg.worldsFolder,this.cfg.worldsBackup,false);
            if(ser.startServer()==0){
                this.wasOnline=true;
                this.reader = new Thread(ser);
                this.reader.start();
                boolean enabled=true;
                this.cfg.crearFicheroStatus(enabled);
                this.jButton_Enviar.setEnabled(enabled);
                this.jTextField_EntradaManual.setEnabled(enabled);
                this.jButton_Save.setEnabled(enabled);
                this.jButton_exitnosave.setEnabled(enabled);
                this.jButton_exit.setEnabled(enabled);
                this.jButton_IniciarServidor.setEnabled(!enabled);
                this.reloj.startReloj();
                if(this.jCheckBox_automensajes.isSelected()){this.gestionarTemporizador(1, enabled);}
                if(this.jCheckBox_autoJavascript.isSelected()){this.gestionarTemporizador(2, enabled);}
                this.habilitarOpcionesAvanzadas(enabled);
            }else{
                this.jTextArea_SalidaConsola.setText(java.util.ResourceBundle.getBundle("lang/language").getString("NO SE HA ENCONTRADO TERRARIASERVER.EXE\\NREVISA LA CONFIGURACIÓN."));
                System.err.println("No se ha podido iniciar el servidor!");
            }
        }else{
            JOptionPane.showMessageDialog(this, this.cfg.onHost+java.util.ResourceBundle.getBundle("lang/language").getString(" HA LANZADO EL SERVIDOR. ÚNETE CON LOS SIGUIENTES DATOS:\\N")
                    + "   IP: "+this.cfg.onIP
                    + java.util.ResourceBundle.getBundle("lang/language").getString("\\N   PUERTO: ")+this.cfg.onPort
                    + "\n", java.util.ResourceBundle.getBundle("lang/language").getString("AVISO!"), JOptionPane.WARNING_MESSAGE);
            this.wasOnline=false;
        }
    }
    
    public void pararServidor(String command){
        if(ser.execServer(command)==0){
            boolean enabled=false;
            this.cfg.crearFicheroStatus(enabled);
            this.jButton_Enviar.setEnabled(enabled);
            this.jTextField_EntradaManual.setEnabled(enabled);
            this.jButton_Save.setEnabled(enabled);
            this.jButton_exitnosave.setEnabled(enabled);
            this.jButton_exit.setEnabled(enabled);
            this.jButton_ban.setEnabled(enabled);
            this.jButton_kick.setEnabled(enabled);
            this.jButton_IniciarServidor.setEnabled(!enabled);
            this.reloj.stopReloj();
            if(this.jButton_IniciarTempoApagado.getText().equals(java.util.ResourceBundle.getBundle("lang/language").getString("PARAR"))){
                this.gestionarTemporizador(0, enabled);
                this.jButton_IniciarTempoApagado.setText(java.util.ResourceBundle.getBundle("lang/language").getString("INICIAR"));
            }
            if(this.jCheckBox_automensajes.isSelected()){this.gestionarTemporizador(1, enabled);}
            if(this.jCheckBox_autoJavascript.isSelected()){this.gestionarTemporizador(2, enabled);}
            this.players.clear();
            this.listadoJugadores();
            this.habilitarOpcionesAvanzadas(enabled);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea_SalidaConsola = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField_EntradaManual = new javax.swing.JTextField();
        jButton_Enviar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel_TimeOnline = new javax.swing.JLabel();
        jPanel_Jugadores = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList_Jugadores = new javax.swing.JList();
        jButton_kick = new javax.swing.JButton();
        jButton_ban = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList_Baneados = new javax.swing.JList();
        jPanel4 = new javax.swing.JPanel();
        jButton_Save = new javax.swing.JButton();
        jButton_exit = new javax.swing.JButton();
        jButton_exitnosave = new javax.swing.JButton();
        jButton_IniciarServidor = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jButton_dawn = new javax.swing.JButton();
        jButton_noon = new javax.swing.JButton();
        jButton_dusk = new javax.swing.JButton();
        jButton_midnight = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jCheckBox_autoapagado = new javax.swing.JCheckBox();
        jLabel15 = new javax.swing.JLabel();
        NumberFormatter f4 = new NumberFormatter();
        f4.setMinimum(1);
        jFormattedTextField_tiempoAutoapagado = new javax.swing.JFormattedTextField(f4);
        jLabel16 = new javax.swing.JLabel();
        jButton_IniciarTempoApagado = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jButton_settle = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jCheckBox_automensajes = new javax.swing.JCheckBox();
        jTextField_automensaje = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jComboBox_tiempoAutomensaje = new javax.swing.JComboBox();
        jLabel19 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jCheckBox_autoJavascript = new javax.swing.JCheckBox();
        jLabel20 = new javax.swing.JLabel();
        jTextField_rutaJavascript = new javax.swing.JTextField();
        jButton_dirJavascript = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        NumberFormatter f3 = new NumberFormatter();
        f3.setMinimum(1);
        jFormattedTextField_tiempoJavascript = new javax.swing.JFormattedTextField(f3);
        jLabel22 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextField_DirTServer = new javax.swing.JTextField();
        jButton_BuscarDirTServer = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextField_DirWorlds = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextField_DirBanlist = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextField_DirWorldsBackup = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jTextField_Motd = new javax.swing.JTextField();
        jButton_GenMOTD = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jComboBox_Worldsize = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jCheckBox_SerSecure = new javax.swing.JCheckBox();
        NumberFormatter f = new NumberFormatter();
        f.setMaximum(255);
        f.setMinimum(2);
        jFormattedTextField_MaxNumPlayers = new javax.swing.JFormattedTextField(f);
        NumberFormatter f2 = new NumberFormatter();
        f2.setMaximum(65535);
        f2.setMinimum(1024);
        jFormattedTextField_SerPort = new javax.swing.JFormattedTextField(f2);
        jLabel9 = new javax.swing.JLabel();
        jComboBox_SerPriority = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        jTextField_SerPassword = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField_WorldName = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jComboBox_SerLanguage = new javax.swing.JComboBox();
        jButton_GuardarConfig = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem_salir = new javax.swing.JMenuItem();
        jMenu_verIPpublica = new javax.swing.JMenu();
        jMenuItem_TestPuertos = new javax.swing.JMenuItem();
        jMenuItem_ipPublica = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem_ManualyUpdates = new javax.swing.JMenuItem();
        jMenuItem_CheckUpdates = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem_AcercaDe = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("TerrariaServerGUI by Razorbreak");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("lang/language"); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Tab1_Consola"))); // NOI18N

        jTextArea_SalidaConsola.setEditable(false);
        jTextArea_SalidaConsola.setBackground(new java.awt.Color(0, 0, 0));
        jTextArea_SalidaConsola.setColumns(20);
        jTextArea_SalidaConsola.setForeground(new java.awt.Color(0, 255, 0));
        jTextArea_SalidaConsola.setLineWrap(true);
        jTextArea_SalidaConsola.setRows(5);
        jTextArea_SalidaConsola.setText("Terraria Server 1.1.2");
        jTextArea_SalidaConsola.setFocusable(false);
        jScrollPane1.setViewportView(jTextArea_SalidaConsola);

        jLabel1.setText(bundle.getString("OUTPUT:")); // NOI18N

        jLabel2.setText(bundle.getString("INPUT:")); // NOI18N

        jTextField_EntradaManual.setEnabled(false);
        jTextField_EntradaManual.setPreferredSize(new java.awt.Dimension(250, 20));
        jTextField_EntradaManual.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField_EntradaManualKeyPressed(evt);
            }
        });

        jButton_Enviar.setText(bundle.getString("ENVIAR")); // NOI18N
        jButton_Enviar.setToolTipText(bundle.getString("EJECUTA EL COMANDO ESCRITO.")); // NOI18N
        jButton_Enviar.setEnabled(false);
        jButton_Enviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_EnviarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField_EntradaManual, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_Enviar)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_EntradaManual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_Enviar))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Tab1_Tiempo Online"))); // NOI18N

        jLabel_TimeOnline.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel_TimeOnline.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_TimeOnline.setText("365d 88:88:88");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel_TimeOnline, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel_TimeOnline, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel_Jugadores.setBorder(javax.swing.BorderFactory.createTitledBorder("Jugadores Online (0)"));

        jList_Jugadores.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList_Jugadores.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList_Jugadores.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jList_JugadoresFocusGained(evt);
            }
        });
        jScrollPane2.setViewportView(jList_Jugadores);

        jButton_kick.setText("kick");
        jButton_kick.setToolTipText(bundle.getString("ECHA DEL SERVIDOR AL JUGADOR ESCOGIDO.")); // NOI18N
        jButton_kick.setEnabled(false);
        jButton_kick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_kickActionPerformed(evt);
            }
        });

        jButton_ban.setText("ban");
        jButton_ban.setToolTipText(bundle.getString("EXPULSA PERMANENTEMENTE AL JUGADOR SELECCIONADO.")); // NOI18N
        jButton_ban.setEnabled(false);
        jButton_ban.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_banActionPerformed(evt);
            }
        });

        jList_Baneados.setForeground(new java.awt.Color(255, 0, 0));
        jList_Baneados.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList_Baneados.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList_Baneados.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jList_BaneadosFocusGained(evt);
            }
        });
        jScrollPane3.setViewportView(jList_Baneados);

        javax.swing.GroupLayout jPanel_JugadoresLayout = new javax.swing.GroupLayout(jPanel_Jugadores);
        jPanel_Jugadores.setLayout(jPanel_JugadoresLayout);
        jPanel_JugadoresLayout.setHorizontalGroup(
            jPanel_JugadoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_JugadoresLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_JugadoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_JugadoresLayout.createSequentialGroup()
                        .addComponent(jButton_kick)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_ban, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel_JugadoresLayout.setVerticalGroup(
            jPanel_JugadoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_JugadoresLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel_JugadoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_kick)
                    .addComponent(jButton_ban)))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Tab1_Servidor"))); // NOI18N

        jButton_Save.setText("save");
        jButton_Save.setToolTipText(bundle.getString("GUARDA EL ESTADO ACTUAL DEL MUNDO.")); // NOI18N
        jButton_Save.setEnabled(false);
        jButton_Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SaveActionPerformed(evt);
            }
        });

        jButton_exit.setText("exit");
        jButton_exit.setToolTipText(bundle.getString("FINALIZA EL SERVIDOR Y GUARDA EL ESTADO DEL MUNDO.")); // NOI18N
        jButton_exit.setEnabled(false);
        jButton_exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_exitActionPerformed(evt);
            }
        });

        jButton_exitnosave.setText("exit-nosave");
        jButton_exitnosave.setToolTipText(bundle.getString("FINALIZA EL SERVIDOR SIN GUARDAR EL MUNDO.")); // NOI18N
        jButton_exitnosave.setEnabled(false);
        jButton_exitnosave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_exitnosaveActionPerformed(evt);
            }
        });

        jButton_IniciarServidor.setText(bundle.getString("INICIAR")); // NOI18N
        jButton_IniciarServidor.setToolTipText(bundle.getString("LANZA EL SERVIDOR.")); // NOI18N
        jButton_IniciarServidor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_IniciarServidorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton_Save)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_IniciarServidor, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton_exitnosave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_exit))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_Save)
                    .addComponent(jButton_IniciarServidor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_exitnosave)
                    .addComponent(jButton_exit)))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel_Jugadores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel_Jugadores, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab(bundle.getString("Tab1_Title Servidor"), jPanel5); // NOI18N

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Tab2_Cambiar hora"))); // NOI18N

        jButton_dawn.setText("dawn");
        jButton_dawn.setToolTipText(bundle.getString("AMANECER (4:30 AM)")); // NOI18N
        jButton_dawn.setEnabled(false);
        jButton_dawn.setPreferredSize(new java.awt.Dimension(73, 23));
        jButton_dawn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_dawnActionPerformed(evt);
            }
        });

        jButton_noon.setText("noon");
        jButton_noon.setToolTipText(bundle.getString("MEDIODÍA (12:00 AM)")); // NOI18N
        jButton_noon.setEnabled(false);
        jButton_noon.setPreferredSize(new java.awt.Dimension(73, 23));
        jButton_noon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_noonActionPerformed(evt);
            }
        });

        jButton_dusk.setText("dusk");
        jButton_dusk.setToolTipText(bundle.getString("ATARDECER (7:30 PM)")); // NOI18N
        jButton_dusk.setEnabled(false);
        jButton_dusk.setPreferredSize(new java.awt.Dimension(73, 23));
        jButton_dusk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_duskActionPerformed(evt);
            }
        });

        jButton_midnight.setText("midnight");
        jButton_midnight.setToolTipText(bundle.getString("MEDIANOCHE (12:00 PM)")); // NOI18N
        jButton_midnight.setEnabled(false);
        jButton_midnight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_midnightActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jButton_dawn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_noon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jButton_dusk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_midnight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_dawn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_noon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_dusk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_midnight))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Tab2_Programador"))); // NOI18N

        jCheckBox_autoapagado.setText(bundle.getString("AUTO-APAGADO")); // NOI18N
        jCheckBox_autoapagado.setToolTipText(bundle.getString("<HTML>PERMITE CONFIGURAR EL TIEMPO PARA QUE EL SERVIDOR FINALICE AUTOMÁTICAMENTE.<BR>SE ENVIARÁ UN MENSAJE CUANDO QUEDE POCO TIEMPO.</HTML>")); // NOI18N
        jCheckBox_autoapagado.setEnabled(false);
        jCheckBox_autoapagado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_autoapagadoActionPerformed(evt);
            }
        });

        jLabel15.setText(bundle.getString("TIEMPO:")); // NOI18N

        jFormattedTextField_tiempoAutoapagado.setEnabled(false);
        jFormattedTextField_tiempoAutoapagado.setValue(30);

        jLabel16.setText(bundle.getString("MINUTOS.")); // NOI18N

        jButton_IniciarTempoApagado.setText(bundle.getString("INICIAR")); // NOI18N
        jButton_IniciarTempoApagado.setEnabled(false);
        jButton_IniciarTempoApagado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_IniciarTempoApagadoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextField_tiempoAutoapagado, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel16))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(jButton_IniciarTempoApagado))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jCheckBox_autoapagado)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jCheckBox_autoapagado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jFormattedTextField_tiempoAutoapagado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(18, 18, 18)
                .addComponent(jButton_IniciarTempoApagado)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Tab2_Liquidos y fisicas"))); // NOI18N

        jButton_settle.setText("settle");
        jButton_settle.setToolTipText(bundle.getString("RESTABLECER FÍSICA DE LOS LÍQUIDOS.")); // NOI18N
        jButton_settle.setEnabled(false);
        jButton_settle.setPreferredSize(new java.awt.Dimension(73, 23));
        jButton_settle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_settleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addComponent(jButton_settle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_settle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Tab2_Mensajes del servidor"))); // NOI18N

        jLabel17.setText(bundle.getString("MENSAJE:")); // NOI18N

        jCheckBox_automensajes.setText(bundle.getString("HABILITAR AUTO-MENSAJES")); // NOI18N
        jCheckBox_automensajes.setToolTipText(bundle.getString("ENVÍA EL MENSAJE ESCRITO A TODOS LOS JUGADORES CADA X TIEMPO.")); // NOI18N
        jCheckBox_automensajes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_automensajesActionPerformed(evt);
            }
        });

        jTextField_automensaje.setEnabled(false);

        jLabel18.setText(bundle.getString("SE EMITIRÁ CADA:")); // NOI18N

        jComboBox_tiempoAutomensaje.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "15", "30", "45", "60", "90", "120" }));
        jComboBox_tiempoAutomensaje.setEnabled(false);

        jLabel19.setText(bundle.getString("MINUTOS.")); // NOI18N

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_automensaje))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox_automensajes)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox_tiempoAutomensaje, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel19)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jCheckBox_automensajes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jTextField_automensaje, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jComboBox_tiempoAutomensaje, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Javascript"));

        jCheckBox_autoJavascript.setText(bundle.getString("HABILITAR CREACIÓN SERVERSTATUS.JS")); // NOI18N
        jCheckBox_autoJavascript.setToolTipText(bundle.getString("CREA UN FICHERO QUE PERMITA PUBLICAR EL ESTADO DEL SERVIDOR EN UNA PÁGINA WEB.")); // NOI18N
        jCheckBox_autoJavascript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_autoJavascriptActionPerformed(evt);
            }
        });

        jLabel20.setText(bundle.getString("RUTA SERVERSTATUS.JS:")); // NOI18N

        jTextField_rutaJavascript.setEditable(false);
        jTextField_rutaJavascript.setText("serverStatus.js");
        jTextField_rutaJavascript.setFocusable(false);

        jButton_dirJavascript.setText(bundle.getString("RUTA...")); // NOI18N
        jButton_dirJavascript.setEnabled(false);
        jButton_dirJavascript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_dirJavascriptActionPerformed(evt);
            }
        });

        jLabel21.setText(bundle.getString("TASA DE REFRESCO CADA:")); // NOI18N

        jFormattedTextField_tiempoJavascript.setEnabled(false);
        jFormattedTextField_tiempoJavascript.setValue(5);

        jLabel22.setText(bundle.getString("MINUTOS.")); // NOI18N

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox_autoJavascript)
                            .addComponent(jLabel20)
                            .addComponent(jTextField_rutaJavascript, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_dirJavascript))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextField_tiempoJavascript, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel22)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(14, 14, 14))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox_autoJavascript)
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_rutaJavascript, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_dirJavascript))
                .addGap(18, 18, 18)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jFormattedTextField_tiempoJavascript, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("Tab2_Title Avanzado"), jPanel6); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel3.setText(bundle.getString("RUTA TERRARIASERVER.EXE:")); // NOI18N

        jTextField_DirTServer.setEditable(false);
        jTextField_DirTServer.setText("TerrariaServer.exe");
        jTextField_DirTServer.setFocusable(false);

        jButton_BuscarDirTServer.setText(bundle.getString("BUSCAR...")); // NOI18N
        jButton_BuscarDirTServer.setToolTipText(bundle.getString("SELECCIONA LA RUTA AL EJECUTABLE TERRARIASERVER.EXE")); // NOI18N
        jButton_BuscarDirTServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_BuscarDirTServerActionPerformed(evt);
            }
        });

        jLabel4.setText(bundle.getString("RUTA /WORLDS:")); // NOI18N

        jTextField_DirWorlds.setEditable(false);
        jTextField_DirWorlds.setText("worlds");
        jTextField_DirWorlds.setToolTipText(bundle.getString("CARPETA DONDE SE GUARDARÁN LOS MUNDOS.")); // NOI18N
        jTextField_DirWorlds.setFocusable(false);

        jLabel11.setText(bundle.getString("RUTA BANNED.TXT:")); // NOI18N

        jTextField_DirBanlist.setEditable(false);
        jTextField_DirBanlist.setText("banned.txt");
        jTextField_DirBanlist.setToolTipText(bundle.getString("<HTML>RUTA DEL FICHERO CON EL LISTADO DE BANEADOS.<BR>POR DEFECTO EN EL MISMO DIRECTORIO DE TERRARIASERVERGUI.</HTML>")); // NOI18N
        jTextField_DirBanlist.setFocusable(false);

        jLabel12.setText(bundle.getString("RUTA /WORLDSBACKUP:")); // NOI18N

        jTextField_DirWorldsBackup.setEditable(false);
        jTextField_DirWorldsBackup.setText("C:\\Users\\<username>\\worldsBackup");
        jTextField_DirWorldsBackup.setToolTipText(bundle.getString("<HTML>CARPETA USADA PARA ALMACENAR LOS MUNDOS TEMPORALMENTE.<BR>AL CERRAR EL SERVIDOR, SE BORRA ÉSTA Y SE ACTUALIZA LA CARPETA /WORLDS.</HTML>")); // NOI18N
        jTextField_DirWorldsBackup.setFocusable(false);

        jLabel14.setText(bundle.getString("M.O.T.D.:")); // NOI18N

        jTextField_Motd.setText("Powered by <username>");
        jTextField_Motd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField_MotdKeyPressed(evt);
            }
        });

        jButton_GenMOTD.setText(bundle.getString("GENERAR")); // NOI18N
        jButton_GenMOTD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_GenMOTDActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jTextField_DirWorlds, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextField_DirTServer, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(10, 10, 10)
                                .addComponent(jButton_BuscarDirTServer))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jTextField_Motd, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField_DirBanlist, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField_DirWorldsBackup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING))
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addGap(14, 14, 14)
                                .addComponent(jButton_GenMOTD)))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_DirTServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_BuscarDirTServer))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField_DirWorlds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField_DirWorldsBackup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField_DirBanlist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_Motd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_GenMOTD))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(320, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("Tab3_Title Configuracion"), jPanel7); // NOI18N

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Tab3_Opciones"))); // NOI18N

        jLabel5.setText(bundle.getString("TAMAÑO:")); // NOI18N
        jLabel5.setToolTipText(bundle.getString("SI EL MUNDO NO EXISTE, LO CREARÁ DEL TAMAÑO DADO.")); // NOI18N

        jComboBox_Worldsize.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1:pequeño", "2:mediano", "3:grande" }));
        jComboBox_Worldsize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_WorldsizeActionPerformed(evt);
            }
        });

        jLabel7.setText(bundle.getString("PUERTO:")); // NOI18N
        jLabel7.setToolTipText(bundle.getString("PUERTO DE CONEXIÓN AL SERVIDOR.")); // NOI18N

        jLabel8.setText(bundle.getString("MÁX. JUGADORES:")); // NOI18N
        jLabel8.setToolTipText(bundle.getString("CANTIDAD DE JUGADORES PERMITIDOS.")); // NOI18N

        jCheckBox_SerSecure.setText(bundle.getString("ANTI-TRAMPAS")); // NOI18N
        jCheckBox_SerSecure.setToolTipText(bundle.getString("ACTIVAR SEGURO ANTI-TRAMPAS.")); // NOI18N
        jCheckBox_SerSecure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_SerSecureActionPerformed(evt);
            }
        });

        jFormattedTextField_MaxNumPlayers.setValue(2);
        jFormattedTextField_MaxNumPlayers.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jFormattedTextField_MaxNumPlayersKeyPressed(evt);
            }
        });

        jFormattedTextField_SerPort.setValue(1024);
        jFormattedTextField_SerPort.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jFormattedTextField_SerPortKeyPressed(evt);
            }
        });

        jLabel9.setText(bundle.getString("PRIORIDAD:")); // NOI18N
        jLabel9.setToolTipText(bundle.getString("<HTML>VELOCIDAD DE ACTUALIZACIÓN DEL SERVIDOR.<BR>UN MAYOR VALOR REQUIERE UNA MEJOR CONEXIÓN DE RED.</HTML>")); // NOI18N

        jComboBox_SerPriority.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0:Tiempo real", "1:Alto", "2:Medio-Alto", "3:Medio", "4:Medio-Bajo", "5:Bajo" }));
        jComboBox_SerPriority.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_SerPriorityActionPerformed(evt);
            }
        });

        jLabel10.setText(bundle.getString("CONTRASEÑA:")); // NOI18N
        jLabel10.setToolTipText(bundle.getString("<HTML>CONTRASEÑA DE ACCESO AL SERVIDOR.<BR>DEJAR VACIO PARA SERVIDOR PÚBLICO.</HTML>")); // NOI18N

        jTextField_SerPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField_SerPasswordKeyPressed(evt);
            }
        });

        jLabel6.setText(bundle.getString("NOMBRE:")); // NOI18N

        jTextField_WorldName.setText(bundle.getString("WORLD1")); // NOI18N
        jTextField_WorldName.setToolTipText(bundle.getString("<HTML>NOMBRE DEL MUNDO A CARGAR.<BR>SI NO EXISTE SE CREARÁ CON ESTE NOMBRE.</HTML>")); // NOI18N
        jTextField_WorldName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField_WorldNameKeyPressed(evt);
            }
        });

        jLabel13.setText(bundle.getString("IDIOMA:")); // NOI18N
        jLabel13.setToolTipText(bundle.getString("<HTML>IDIOMA DEL SERVIDOR.</HTML>")); // NOI18N

        jComboBox_SerLanguage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1:English", "2:Deutsch", "3:Italiano", "4:Fraçais", "5:Español" }));
        jComboBox_SerLanguage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_SerLanguageActionPerformed(evt);
            }
        });

        jButton_GuardarConfig.setText(bundle.getString("GUARDAR")); // NOI18N
        jButton_GuardarConfig.setEnabled(false);
        jButton_GuardarConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_GuardarConfigActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_WorldName))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox_SerLanguage, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jCheckBox_SerSecure)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jFormattedTextField_SerPort, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jFormattedTextField_MaxNumPlayers))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox_Worldsize, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox_SerPriority, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField_SerPassword)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton_GuardarConfig))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField_WorldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jComboBox_Worldsize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox_SerPriority, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jComboBox_SerLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTextField_SerPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jFormattedTextField_MaxNumPlayers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jFormattedTextField_SerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jCheckBox_SerSecure)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton_GuardarConfig))
        );

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(585, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 334, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("Tab4_Title Opciones"), jPanel14); // NOI18N

        jMenu1.setText(bundle.getString("ARCHIVO")); // NOI18N

        jMenuItem_salir.setText(bundle.getString("SALIR")); // NOI18N
        jMenuItem_salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_salirActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem_salir);

        jMenuBar1.add(jMenu1);

        jMenu_verIPpublica.setText(bundle.getString("HERRAMIENTAS")); // NOI18N
        jMenu_verIPpublica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_verIPpublicaActionPerformed(evt);
            }
        });

        jMenuItem_TestPuertos.setText(bundle.getString("TEST DE PUERTOS")); // NOI18N
        jMenuItem_TestPuertos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_TestPuertosActionPerformed(evt);
            }
        });
        jMenu_verIPpublica.add(jMenuItem_TestPuertos);

        jMenuItem_ipPublica.setText(bundle.getString("COMPROBAR IP")); // NOI18N
        jMenuItem_ipPublica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ipPublicaActionPerformed(evt);
            }
        });
        jMenu_verIPpublica.add(jMenuItem_ipPublica);

        jMenuBar1.add(jMenu_verIPpublica);

        jMenu3.setText(bundle.getString("AYUDA")); // NOI18N

        jMenuItem_ManualyUpdates.setText(bundle.getString("MANUAL DE USO Y ACTUALIZACIONES")); // NOI18N
        jMenuItem_ManualyUpdates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_ManualyUpdatesActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem_ManualyUpdates);

        jMenuItem_CheckUpdates.setText(bundle.getString("JMenu_Help_ChkUpdates")); // NOI18N
        jMenuItem_CheckUpdates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_CheckUpdatesActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem_CheckUpdates);
        jMenu3.add(jSeparator1);

        jMenuItem_AcercaDe.setText(bundle.getString("ACERCA DE...")); // NOI18N
        jMenuItem_AcercaDe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_AcercaDeActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem_AcercaDe);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem_AcercaDeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_AcercaDeActionPerformed
        AboutBox a= new AboutBox(this,true);
        a.setVisible(true);
    }//GEN-LAST:event_jMenuItem_AcercaDeActionPerformed

    private void jMenuItem_salirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_salirActionPerformed
        this.cerrarAplicacion();
    }//GEN-LAST:event_jMenuItem_salirActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.cerrarAplicacion();
    }//GEN-LAST:event_formWindowClosing

    private void jButton_IniciarServidorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_IniciarServidorActionPerformed
        if(!this.jButton_GuardarConfig.isEnabled()){
            this.iniciarServidor();
        }else{
            this.jTextArea_SalidaConsola.setText(java.util.ResourceBundle.getBundle("lang/language").getString("DEBES CONFIRMAR LA CONFIGURACIÓN DEL \\N")
                    + java.util.ResourceBundle.getBundle("lang/language").getString("SERVIDOR PINCHANDO SOBRE EL BOTÓN \\NGUARDAR!"));
        }
    }//GEN-LAST:event_jButton_IniciarServidorActionPerformed

    private void jList_JugadoresFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jList_JugadoresFocusGained
        this.jList_Baneados.clearSelection();
        this.jButton_kick.setEnabled(true);
        this.jButton_ban.setEnabled(true);
        this.jButton_ban.setText("ban");
        this.jButton_ban.setToolTipText(java.util.ResourceBundle.getBundle("lang/language").getString("EXPULSA PERMANENTEMENTE AL JUGADOR SELECCIONADO."));
    }//GEN-LAST:event_jList_JugadoresFocusGained

    private void jButton_exitnosaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_exitnosaveActionPerformed
        this.pararServidor("exit-nosave");
    }//GEN-LAST:event_jButton_exitnosaveActionPerformed

    private void jButton_exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_exitActionPerformed
        this.pararServidor("exit");
    }//GEN-LAST:event_jButton_exitActionPerformed

    private void jButton_SaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SaveActionPerformed
        this.ser.execServer("save");
    }//GEN-LAST:event_jButton_SaveActionPerformed

    private void jButton_kickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_kickActionPerformed
        if(this.jList_Jugadores.getSelectedIndex()>=0){
            this.ser.execServer("kick "+this.players.get(this.jList_Jugadores.getSelectedIndex()).getName());
            this.jList_Jugadores.clearSelection();
            this.jButton_kick.setEnabled(false);
            this.jButton_ban.setEnabled(false);
        }
    }//GEN-LAST:event_jButton_kickActionPerformed

    private void jButton_banActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_banActionPerformed
        this.banearJugador(this.jButton_ban.getText().equals("ban"));
    }//GEN-LAST:event_jButton_banActionPerformed

    private void jList_BaneadosFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jList_BaneadosFocusGained
        this.jList_Jugadores.clearSelection();
        this.jButton_kick.setEnabled(false);
        this.jButton_ban.setEnabled(true);
        this.jButton_ban.setText("unban");
        this.jButton_ban.setToolTipText(java.util.ResourceBundle.getBundle("lang/language").getString("ELIMINA EL BANEO AL JUGADOR SELECCIONADO."));
    }//GEN-LAST:event_jList_BaneadosFocusGained

    private void jButton_EnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_EnviarActionPerformed
        this.ejecutarComando();
    }//GEN-LAST:event_jButton_EnviarActionPerformed

    private void jTextField_EntradaManualKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_EntradaManualKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            this.ejecutarComando();
        }
    }//GEN-LAST:event_jTextField_EntradaManualKeyPressed

    private void jButton_BuscarDirTServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_BuscarDirTServerActionPerformed
        JFileChooser f = new JFileChooser();
        f.setDialogTitle(java.util.ResourceBundle.getBundle("lang/language").getString("SELECCIONAR EJECUTABLE..."));
        int exitValue = f.showOpenDialog(this);
        if(exitValue==JFileChooser.APPROVE_OPTION){
            String path = f.getSelectedFile().getPath().toString();
            if(path.contains("TerrariaServer.exe")){
                this.jTextField_DirTServer.setText(path);
                this.ser.TServerPath=path;
                this.jButton_GuardarConfig.setEnabled(true);
            }else{
                JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("lang/language").getString("DEBES SELECCIONAR EL FICHERO TERRARIASERVER.EXE."), java.util.ResourceBundle.getBundle("lang/language").getString("AVISO!"), JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton_BuscarDirTServerActionPerformed

    private void jButton_GenMOTDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_GenMOTDActionPerformed
        this.jTextField_Motd.setText((new motd.motdTool()).generarMOTD());
        this.jButton_GuardarConfig.setEnabled(true);
    }//GEN-LAST:event_jButton_GenMOTDActionPerformed

    private void jButton_GuardarConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_GuardarConfigActionPerformed
        if(this.jTextField_DirTServer.getText().equals("")
                ||this.jTextField_WorldName.getText().equals("")
                ||this.jTextField_Motd.getText().equals("")){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("lang/language").getString("POR FAVOR, RELLENE LOS CAMPOS VACÍOS.\\NNOTA: EL CAMPO 'CONTRASEÑA' ES OPCIONAL."), java.util.ResourceBundle.getBundle("lang/language").getString("AVISO!"), JOptionPane.WARNING_MESSAGE);
        }else{
            this.ser.language=this.jComboBox_SerLanguage.getSelectedIndex()+1;
            this.ser.maxplayers=Integer.parseInt(this.jFormattedTextField_MaxNumPlayers.getText());
            this.ser.port=Integer.parseInt(this.jFormattedTextField_SerPort.getText().replaceAll("\\.", ""));
            this.ser.world=this.jTextField_WorldName.getText();
            if(this.jCheckBox_SerSecure.isSelected()){this.ser.secure=1;}else{this.ser.secure=0;}
            this.ser.TServerPath=this.jTextField_DirTServer.getText();
            this.ser.banlist=this.jTextField_DirBanlist.getText();
            this.ser.password=this.jTextField_SerPassword.getText();
            this.ser.priority=this.jComboBox_SerPriority.getSelectedIndex();
            this.ser.worldsize=this.jComboBox_Worldsize.getSelectedIndex()+1;
            this.ser.worldpath=this.jTextField_DirWorlds.getText();
            this.ser.motd=this.jTextField_Motd.getText();
            this.jButton_GuardarConfig.setEnabled(false);
            this.cfg.setConfig(this.jCheckBox_automensajes.isSelected(), this.jTextField_automensaje.getText()
                    , this.jComboBox_tiempoAutomensaje.getSelectedIndex(), this.jCheckBox_autoJavascript.isSelected()
                    , this.jTextField_rutaJavascript.getText(), this.jFormattedTextField_tiempoJavascript.getText());
            this.cfg.saveConfiguration(ser);
        }
    }//GEN-LAST:event_jButton_GuardarConfigActionPerformed

    private void jTextField_MotdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_MotdKeyPressed
        this.jButton_GuardarConfig.setEnabled(true);
    }//GEN-LAST:event_jTextField_MotdKeyPressed

    private void jTextField_WorldNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_WorldNameKeyPressed
        this.jButton_GuardarConfig.setEnabled(true);
    }//GEN-LAST:event_jTextField_WorldNameKeyPressed

    private void jComboBox_WorldsizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_WorldsizeActionPerformed
        this.jButton_GuardarConfig.setEnabled(true);
    }//GEN-LAST:event_jComboBox_WorldsizeActionPerformed

    private void jComboBox_SerPriorityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_SerPriorityActionPerformed
        this.jButton_GuardarConfig.setEnabled(true);
    }//GEN-LAST:event_jComboBox_SerPriorityActionPerformed

    private void jComboBox_SerLanguageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_SerLanguageActionPerformed
        this.jButton_GuardarConfig.setEnabled(true);
    }//GEN-LAST:event_jComboBox_SerLanguageActionPerformed

    private void jTextField_SerPasswordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_SerPasswordKeyPressed
        this.jButton_GuardarConfig.setEnabled(true);
    }//GEN-LAST:event_jTextField_SerPasswordKeyPressed

    private void jFormattedTextField_MaxNumPlayersKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_MaxNumPlayersKeyPressed
        this.jButton_GuardarConfig.setEnabled(true);
    }//GEN-LAST:event_jFormattedTextField_MaxNumPlayersKeyPressed

    private void jFormattedTextField_SerPortKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jFormattedTextField_SerPortKeyPressed
        this.jButton_GuardarConfig.setEnabled(true);
    }//GEN-LAST:event_jFormattedTextField_SerPortKeyPressed

    private void jCheckBox_SerSecureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_SerSecureActionPerformed
        this.jButton_GuardarConfig.setEnabled(true);
    }//GEN-LAST:event_jCheckBox_SerSecureActionPerformed

    private void jMenuItem_TestPuertosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_TestPuertosActionPerformed
        CheckPortDialog chk = new CheckPortDialog(this,true);
        chk.setVisible(true);
    }//GEN-LAST:event_jMenuItem_TestPuertosActionPerformed

    private void jMenu_verIPpublicaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_verIPpublicaActionPerformed
        VerIPDialog a = new VerIPDialog(this,true);
        a.giveIP(this);
        a.setVisible(true);
    }//GEN-LAST:event_jMenu_verIPpublicaActionPerformed

    private void jMenuItem_ipPublicaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_ipPublicaActionPerformed
        VerIPDialog a = new VerIPDialog(this,true);
        a.giveIP(this);
        a.setVisible(true);
    }//GEN-LAST:event_jMenuItem_ipPublicaActionPerformed

    private void jButton_settleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_settleActionPerformed
        this.ser.execServer("settle");
    }//GEN-LAST:event_jButton_settleActionPerformed

    private void jButton_dawnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_dawnActionPerformed
        this.ser.execServer("dawn");
    }//GEN-LAST:event_jButton_dawnActionPerformed

    private void jButton_noonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_noonActionPerformed
        this.ser.execServer("noon");
    }//GEN-LAST:event_jButton_noonActionPerformed

    private void jButton_duskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_duskActionPerformed
        this.ser.execServer("dusk");
    }//GEN-LAST:event_jButton_duskActionPerformed

    private void jButton_midnightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_midnightActionPerformed
        this.ser.execServer("midnight");
    }//GEN-LAST:event_jButton_midnightActionPerformed

    private void jCheckBox_automensajesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_automensajesActionPerformed
        this.jTextField_automensaje.setEnabled(this.jCheckBox_automensajes.isSelected());
        this.jComboBox_tiempoAutomensaje.setEnabled(this.jCheckBox_automensajes.isSelected());
    }//GEN-LAST:event_jCheckBox_automensajesActionPerformed

    private void jCheckBox_autoJavascriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_autoJavascriptActionPerformed
        this.jFormattedTextField_tiempoJavascript.setEnabled(this.jCheckBox_autoJavascript.isSelected());
        this.jButton_dirJavascript.setEnabled(this.jCheckBox_autoJavascript.isSelected());
        if(this.jCheckBox_autoJavascript.isSelected()){
            this.cfg.updateJavascript(players,this.jLabel_TimeOnline.getText());
        }else{
            this.cfg.deleteJavascript();
        }
    }//GEN-LAST:event_jCheckBox_autoJavascriptActionPerformed

    private void jCheckBox_autoapagadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_autoapagadoActionPerformed
        this.jFormattedTextField_tiempoAutoapagado.setEnabled(this.jCheckBox_autoapagado.isSelected());
        this.jButton_IniciarTempoApagado.setEnabled(this.jCheckBox_autoapagado.isSelected());
    }//GEN-LAST:event_jCheckBox_autoapagadoActionPerformed

    private void jButton_IniciarTempoApagadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_IniciarTempoApagadoActionPerformed
        if(this.jButton_IniciarTempoApagado.getText().equals(java.util.ResourceBundle.getBundle("lang/language").getString("INICIAR"))){
            this.jButton_IniciarTempoApagado.setText(java.util.ResourceBundle.getBundle("lang/language").getString("PARAR"));
            this.jCheckBox_autoapagado.setEnabled(false);
            this.jFormattedTextField_tiempoAutoapagado.setEnabled(false);
            this.gestionarTemporizador(0, true);
        }else{
            this.jButton_IniciarTempoApagado.setText(java.util.ResourceBundle.getBundle("lang/language").getString("INICIAR"));
            this.jCheckBox_autoapagado.setEnabled(true);
            this.jFormattedTextField_tiempoAutoapagado.setEnabled(true);
            this.gestionarTemporizador(0, false);
        }
    }//GEN-LAST:event_jButton_IniciarTempoApagadoActionPerformed

    private void jButton_dirJavascriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_dirJavascriptActionPerformed
        JFileChooser f = new JFileChooser();
        f.setDialogTitle(java.util.ResourceBundle.getBundle("lang/language").getString("SELECCIONAR CARPETA..."));
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int exitValue = f.showOpenDialog(this);
        if(exitValue==JFileChooser.APPROVE_OPTION){
            String path = f.getSelectedFile().getPath().toString()+"\\statusServer.js";
            this.jTextField_rutaJavascript.setText(path);
            this.cfg.javascript = new java.io.File(path);
        }
    }//GEN-LAST:event_jButton_dirJavascriptActionPerformed

    private void jMenuItem_ManualyUpdatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_ManualyUpdatesActionPerformed
        String url = "http://razorbreakrestringedzone.blogspot.com.es/2013/08/descarga-y-manual-de-uso-de.html";
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        } catch (Exception e) {
            System.err.println("Failed to start a browser to open the url " + url);
        }
    }//GEN-LAST:event_jMenuItem_ManualyUpdatesActionPerformed

    private void jMenuItem_CheckUpdatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_CheckUpdatesActionPerformed
        this.checkUpdates();
    }//GEN-LAST:event_jMenuItem_CheckUpdatesActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
            
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_BuscarDirTServer;
    private javax.swing.JButton jButton_Enviar;
    private javax.swing.JButton jButton_GenMOTD;
    private javax.swing.JButton jButton_GuardarConfig;
    private javax.swing.JButton jButton_IniciarServidor;
    private javax.swing.JButton jButton_IniciarTempoApagado;
    private javax.swing.JButton jButton_Save;
    private javax.swing.JButton jButton_ban;
    private javax.swing.JButton jButton_dawn;
    private javax.swing.JButton jButton_dirJavascript;
    private javax.swing.JButton jButton_dusk;
    private javax.swing.JButton jButton_exit;
    private javax.swing.JButton jButton_exitnosave;
    private javax.swing.JButton jButton_kick;
    private javax.swing.JButton jButton_midnight;
    private javax.swing.JButton jButton_noon;
    private javax.swing.JButton jButton_settle;
    private javax.swing.JCheckBox jCheckBox_SerSecure;
    private javax.swing.JCheckBox jCheckBox_autoJavascript;
    private javax.swing.JCheckBox jCheckBox_autoapagado;
    private javax.swing.JCheckBox jCheckBox_automensajes;
    private javax.swing.JComboBox jComboBox_SerLanguage;
    private javax.swing.JComboBox jComboBox_SerPriority;
    private javax.swing.JComboBox jComboBox_Worldsize;
    private javax.swing.JComboBox jComboBox_tiempoAutomensaje;
    private javax.swing.JFormattedTextField jFormattedTextField_MaxNumPlayers;
    private javax.swing.JFormattedTextField jFormattedTextField_SerPort;
    private javax.swing.JFormattedTextField jFormattedTextField_tiempoAutoapagado;
    private javax.swing.JFormattedTextField jFormattedTextField_tiempoJavascript;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_TimeOnline;
    private javax.swing.JList jList_Baneados;
    private javax.swing.JList jList_Jugadores;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem_AcercaDe;
    private javax.swing.JMenuItem jMenuItem_CheckUpdates;
    private javax.swing.JMenuItem jMenuItem_ManualyUpdates;
    private javax.swing.JMenuItem jMenuItem_TestPuertos;
    private javax.swing.JMenuItem jMenuItem_ipPublica;
    private javax.swing.JMenuItem jMenuItem_salir;
    private javax.swing.JMenu jMenu_verIPpublica;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanel_Jugadores;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea_SalidaConsola;
    private javax.swing.JTextField jTextField_DirBanlist;
    private javax.swing.JTextField jTextField_DirTServer;
    private javax.swing.JTextField jTextField_DirWorlds;
    private javax.swing.JTextField jTextField_DirWorldsBackup;
    private javax.swing.JTextField jTextField_EntradaManual;
    private javax.swing.JTextField jTextField_Motd;
    private javax.swing.JTextField jTextField_SerPassword;
    private javax.swing.JTextField jTextField_WorldName;
    private javax.swing.JTextField jTextField_automensaje;
    private javax.swing.JTextField jTextField_rutaJavascript;
    // End of variables declaration//GEN-END:variables
}
