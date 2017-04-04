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
 * @author Francisco J. Quero
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
    public int numChinosTotales;
    public int numRondas;
    public boolean cerrarConexion;
    ProtocoloCliente protocolo;
    public int numRondasGanadas, numRondasPerdidas;
        
    public ClienteTCP(){
        fabricaDeMensajes=new MensajeProtocoloJuegoChinos();
        servidorDir = "localhost";
        alias = new String();
        cerrarConexion = false;
        numRondasGanadas = 0;
        numRondasPerdidas = 0;
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
        int suma;
        do {
               
                // Segu'n el tipo de evento (mensaje recibido), interpretada por el objeto "protocolo", 
                // la aplicacio'n debe realizar operaciones sobre la base de datos de 
                // usuarios:
                switch(peticion=conexion.protocolo.recibirPeticion()){
                    
                    // Solicitud de darse de alta en la base de datos:
                    case ProtocoloCliente.notificacionAliasIncorrecto:
                        System.out.print("Por favor, introduce tu alias: ");
                        conexion.alias = conin.nextLine();
                        conexion.protocolo.enviarLogin(conexion.alias);
                        break;
                    case ProtocoloCliente.notificacionAutenticado:

                        System.out.println("Elige contra quien quieres jugar (Maquina/Jugador): Jugando contra maquina." );

                        // Aqui se comprobaria si quieres jugar contra maquina o humano
                        // Pero solo esta implementado contra maquina
                        conexion.protocolo.enviarVsMaquina();
                        
                        System.out.print("Elige numero de rondas: " );
                        conexion.numRondas = Integer.parseInt(conin.nextLine());
                        conexion.protocolo.enviarNumeroRondas(conexion.numRondas);
                        
                        break;
                    case ProtocoloCliente.notificacionTurno:
                            System.out.print("¿Cuantos chinos eliges?: ");
                            conexion.numChinos = Integer.parseInt(conin.nextLine());
                            conexion.protocolo.enviarNumeroChinosElegidos(conexion.numChinos);
                            
                            if(conexion.protocolo.hablasElPrimero == false)
                                System.out.println("Tu rival predice "+ conexion.protocolo.numChinosTotal+" chinos.");
                            
                            System.out.print("¿Cuantos chinos predices?: ");
                            conexion.numChinosTotales = Integer.parseInt(conin.nextLine());
                            conexion.protocolo.enviarNumeroChinos(conexion.numChinosTotales);
                            
                            if(conexion.protocolo.hablasElPrimero == true)
                                System.out.println("Tu rival predice "+ conexion.protocolo.numChinosTotal+" chinos.");
                        break;
                    case ProtocoloCliente.notificacionGanador:
                        suma = conexion.protocolo.numChinos + conexion.numChinos;
                        System.out.println("El rival ha sacado "+conexion.protocolo.numChinos+". Ha habido un total de "+suma+" chinos.");
                        
                        switch (conexion.protocolo.ganador) {
                            case 0:
                                conexion.numRondasPerdidas++;
                                
                                System.out.println(conexion.protocolo.ganadorAlias+ " ha ganado esta ronda.");
                                break;
                            case 1:
                                conexion.numRondasGanadas++;
                                System.out.println("Has ganado esta ronda.");
                                break;
                            case 2:
                                System.out.println(conexion.protocolo.ganadorAlias+". Ronda extra.");
                                break;
                            default:
                                break;
                            
                        }
                        break;
                    case ProtocoloCliente.notificacionRondasRestantes:
                        conexion.numRondas = conexion.protocolo.numRondas;
                        if (conexion.numRondas == 0){
                            if (conexion.numRondasGanadas > conexion.numRondasPerdidas) {
                                System.out.println("Has ganado mas rondas que el rival. Enhorabuena, eres el ganador.");
                            } else if (conexion.numRondasGanadas < conexion.numRondasPerdidas) {
                                System.out.println("Has ganado menos rondas que el rival. El rival es el ganador.");
                            } else {
                                System.out.println("El rival y tu habeis ganado las mismas rondas. Habeis empatado!");
                            }
                            conexion.protocolo.enviarDespedida();
                        } else {
                            System.out.println("Quedan "+conexion.numRondas+" rondas restantes. Comenzando siguiente ronda...\n");
                            // Comenzamos nueva ronda
                            conexion.protocolo.enviarNumeroRondas(conexion.numRondas);
                            
                        }
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
        
        } while(peticion!=ProtocoloCliente.notificacionFinalizar);
    }
}

    
    

