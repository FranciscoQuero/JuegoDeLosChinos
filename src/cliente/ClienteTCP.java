/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import mensajesjuegochinos.MensajeProtocoloJuegoChinos;

/**
 *
 * @author Francis
 */
public class ClienteTCP {
    public Socket socket;
    public BufferedReader in;
    public PrintWriter out;
    public MensajeProtocoloJuegoChinos fabricaDeMensajes;
    public String servidorDir;
    public String alias;
    public int puerto;
    public int numChinos;
    public int numRondas;
    public boolean cerrarConexion;
    ProtocoloCliente protocolo;
        
    public ClienteTCP(){
        fabricaDeMensajes=new MensajeProtocoloJuegoChinos();
        servidorDir = "Hasta aqui bien";
        alias = new String();
        cerrarConexion = false;
    }
    public void setServidor(String texto){
        this.servidorDir = texto;
    }
    public void setAlias(String texto){
        this.alias = texto;
    }
    public void setPuerto(int port){
        this.puerto = port;
    }
    public void setCerrar(boolean bool){
        this.cerrarConexion = bool;
    }
    
    public static void main(String []args){
        ClienteTCP conexion = new ClienteTCP();
        BufferedReader inConsola = new BufferedReader(new InputStreamReader(System.in));
        Scanner conin;
        conin = new Scanner(System.in);
        int peticion;
        
        System.out.println("Cliente JuegoDeLosChinos v0.1");
        System.out.print("Por favor, introduce servidor: ");
        conexion.servidorDir = conin.nextLine();
        System.out.print("Por favor, introduce puerto: ");
        conexion.puerto = Integer.parseInt(conin.nextLine());
        
        //establecemos conexion
        try {
            conexion.socket = new Socket(conexion.servidorDir,conexion.puerto);
            conexion.in = new BufferedReader(new InputStreamReader(conexion.socket.getInputStream()));
            conexion.out = new PrintWriter(conexion.socket.getOutputStream());          
        } catch (IOException ex) {
            Logger.getLogger(ClienteTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        conexion.protocolo = new ProtocoloCliente(conexion.in, conexion.out);
        // Ejecucion de protocolo
        System.out.print("Por favor, introduce alias: ");
        
        conexion.alias = conin.nextLine();
        conexion.protocolo.enviarLogin(conexion.alias);
        
        do {
               
                // Segu'n el tipo de evento (mensaje recibido), interpretada por el objeto "protocolo", 
                // la aplicacio'n debe realizar operaciones sobre la base de datos de 
                // usuarios:
                switch(peticion=conexion.protocolo.recibirPeticion()){
                    
                    // Solicitud de darse de alta en la base de datos:
                    // Mensaje:
                    //  1110 <nombre de usuario>
                    case ProtocoloCliente.notificacionAliasIncorrecto:
                        System.out.print("Por favor, introduce tu alias: ");
                        conexion.alias = conin.nextLine();
                        conexion.protocolo.enviarLogin(conexion.alias);
                        break;
                    case ProtocoloCliente.notificacionAutenticado:

                        System.out.print("Elige contra quien quieres jugar (Maquina/Jugador): " );

                        // Aqui se comprobaria si quieres jugar contra maquina o humano
                        // Pero solo esta implementado conta maquina
                        conexion.protocolo.enviarVsMaquina();
                        
                        System.out.print("Jugando contra maquina. Elige numero de rondas: " );
                        conexion.numRondas = conin.nextInt();
                        conexion.protocolo.enviarNumeroRondas(conexion.numRondas);
                        
                        break;

                       
                    case ProtocoloCliente.notificacionFinalizar:
                        conexion.protocolo.mDesconectar();

                        
                        try { 
                            conexion.in.close();
                            conexion.out.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
               
                        break;
                                            
                    default:
                        break;
                }
        
       // conexion.fabricaDeMensajes.mAlias(conexion.alias);
        } while(peticion!=ProtocoloCliente.notificacionFinalizar);
    }
}

    
    

