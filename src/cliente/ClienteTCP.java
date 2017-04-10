/*
 * Copyright (C) 2017 Francisco J. Quero
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Visit https://github.com/FranciscoQuero/JuegoDeLosChinos for updates and more info.
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
 * Clase Cliente TCP, que incluye el cliente necesario para conectarse al servidor
 * de juego de los chinos. Este cliente pide datos al usuario y lo guia a traves del
 * proceso de la partida. Cuando se han acabado las rondas, se cierra y cierra la conexion.
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
    ProtocoloCliente protocolo;
    public int numRondasGanadas, numRondasPerdidas;
        
    /**
     * Constructor sin parámetros que inicializa las variables esenciales
     */
    public ClienteTCP(){
        fabricaDeMensajes=new MensajeProtocoloJuegoChinos();
        servidorDir = "localhost";
        alias = new String();
        numRondasGanadas = 0;
        numRondasPerdidas = 0;
    }
    /**
     * Establece la dirección del servidor a la deseada.
     * @param texto direccion del servidor
     */
    public void setServidor(String texto){
        this.servidorDir = texto;
    }
    /**
     * Establece el alias deseado
     * @param texto alias
     */
    public void setAlias(String texto){
        this.alias = texto;
    }
    /**
     * Establece el puerto al que se conectará
     * @param port puerto, numero entero
     */
    public void setPuerto(int port){
        this.puerto = port;
    }
    
    /**
     * Método principal del cliente. Ejecuta la conexión y llama al protocolo para
     * crear e interpretar los mensajes. Guía al usuario interactivamente.
     * @param args argumentos opcionales.
     */
    public static void main(String []args){
        ClienteTCP conexion = new ClienteTCP();
        Scanner conin;
        conin = new Scanner(System.in);
        int peticion;
        
        System.out.println("Cliente JuegoDeLosChinos v0.2");
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
                    case ProtocoloCliente.notificacionAutenticado: // notificacion de autenticado

                        System.out.println("Elige contra quien quieres jugar (Maquina/Jugador): Jugando contra maquina." );

                        // Aqui se comprobaria si quieres jugar contra maquina o humano
                        // Pero solo esta implementado contra maquina
                        conexion.protocolo.enviarVsMaquina();
                        
                        System.out.print("Elige numero de rondas: " );
                        conexion.numRondas = Integer.parseInt(conin.nextLine());
                        conexion.protocolo.enviarNumeroRondas(conexion.numRondas);
                        
                        break;
                    case ProtocoloCliente.notificacionTurno: // Si se recibe la notificacion del turno
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
                    case ProtocoloCliente.notificacionGanador: // Si se recibe el ganador
                        suma = conexion.protocolo.numChinos + conexion.numChinos;
                        System.out.println("El rival ha sacado "+conexion.protocolo.numChinos+". Ha habido un total de "+suma+" chinos.");
                        
                        // Notificacion y determinacion del ganador de la ronda
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
                    case ProtocoloCliente.notificacionRondasRestantes: // Si se reciben las rondas restantes
                        conexion.numRondas = conexion.protocolo.numRondas;
                        if (conexion.numRondas == 0){ // Si ya no quedan rondas
                            
                            // Se compara y elige el ganador final
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
                            // Comenzamos nueva ronda si no se ha llegado al final
                            conexion.protocolo.enviarNumeroRondas(conexion.numRondas);
                            
                        }
                        break;
                        
                    case ProtocoloCliente.notificacionFinalizar: // Si se recibe el fin del juego
                        conexion.protocolo.enviarDespedida();

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

    
    

