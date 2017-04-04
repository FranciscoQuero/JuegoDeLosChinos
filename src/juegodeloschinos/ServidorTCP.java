/*
 * Copyright (C) 2015 jjramos
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
 */
package juegodeloschinos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Servidor TCP del juego de los chinos, desarrollado para la práctica 1 de la
 * asignatura Desarrollo de Aplicaciones en Red. Implementa un servidor multihebra
 * con comprobación de duplicidad de nombres de usuario, con puerto por defecto 9090.
 * También incorpora la lógica del juego en cuestión.
 * @author Francisco J. Quero
 */
public class ServidorTCP {
    // Base de datos de usuarios registrados.
    static Map usuarios;

    
    // Socket y puerto de acceso al servicio
    static ServerSocket socketServicio;    
    static int puerto;
    
    /** Creates a new instance of Servidor */
    // Inicializacio'n simple
    public ServidorTCP() {
        puerto=0;
    }
    
    /**
     * Método principal. Lee el puerto en el que abrirá el servidor. Por defecto, 9090.
     * Además, acepta conexiones y crea una hebra para cada una.
     * @param args posible puerto que el usuario quiera introducir.
     */
    public static void main(String args[]){
        
        puerto=9090;
        
        if(args.length<1&&false){
            System.err.println("Sintaxis: ServidorTCP puerto");
            System.exit(-1);
        } 
        
        if(args.length>=1)
                puerto=Integer.parseInt(args[1]);
        
        // Atendemos al puerto especificado (9090 por defecto)
        iniciarServicio(puerto);
        
        // Aceptamos conexiones y las servimos a cada cliente con una hebra
        do {
              try{
                Socket s=socketServicio.accept();
                System.out.println("Creando hebra de servicio ...");
                new Servicio(s,usuarios).start();
                
               
                
            }catch(IOException e){
                System.err.print("Error");
            } 
            
        } while(true);
        
    }
    /**
     * Método que inicia el servicio de escucha en el puerto seleccionado.
     * @param p puerto en el que el servidor escuchará.
     * @return un posible error producido si es 1.
     */
    static int iniciarServicio(int p){
        int error=0;
      
        // Creamos la base de datos de usuarios (deberi'a ser persistente, mantenerse en una 
        // base de datos en disco)
        //
        usuarios=new HashMap();
        
        
        // Abrimos el puerto especificado para acceso al servicio.
        try {
            
            System.out.print("Abriendo socket de escucha en puerto "+puerto+"...");
            socketServicio=new ServerSocket(puerto);
            System.out.println(" ok.");
            
        } catch (IOException ex) {
            ex.printStackTrace();
            error=1;
        }
        
        return error;
    }
    
    //
    // La subclase "Servicio" implementa las operaciones del servicio.
    // Cada vez que se registra una solicitud del servicio al puerto de acceso,
    // se lanza una hebra nueva para ofrecerlo.
/**
 * La subclase "Servicio" implementa las operaciones del servicio.
 * Cada vez que se registra una solicitud del servicio al puerto de acceso,
 * se lanza una hebra nueva para ofrecerlo.
 */
    static public class Servicio extends Thread {
        
        // Socket utilizado para ofrecer el servicio al cliente efectu'a la solicitud
        // de conexio'n recibida en el socket de la clase "Servidor"
        Socket socket;
        
        // Manejadores de los flujos de recepcio'n y envi'o. No admiten todos los caracteres,
        // pero en este ejemplo nos ofrecen toda la funcionalidad requerida.
        PrintWriter out;
        BufferedReader in;
        
         // objeto de la clase "Protocolo". Rige y mantiene los pasos de estado en el protocolo.
        Protocolo protocolo;
        
         // referencia de la base de datos de usuarios comu'n.'
         Map usuarios;
          
          
        // Constructor de la clase:
        Servicio(Socket s, Map u){
           
            usuarios=u;
            socket=s;
            
            // Obtenemos los flujos de escritura y lectura:
            try {
            
                out= new PrintWriter(socket.getOutputStream(), true);
                in= new BufferedReader(new InputStreamReader(socket.getInputStream()));
                protocolo=new Protocolo(Servicio.currentThread().getName(),in,out);
                System.out.println(Servicio.currentThread().getName());
            } catch(IOException e){
                // Se acabo'
                System.err.println("Error en la hebra "+Servicio.currentThread().getName());
            }
        }

        /**
         * Hebra principal del servicio. Desarrolla la lógica así como la 
         * generación de mensajes hacia el cliente.
         */
        @SuppressWarnings("null")
        public void run(){
            int peticion=0;
            int suma;
            String mensaje;
            String [] palabras;
            Usuario u=null,u0;
            int numRondas = 0, numChinos = 0, numChinosTotales = 0, numChinosMaquina = 0;
            SecureRandom r = new SecureRandom();
            int quienEmpieza;
            int numChinosTotalesMaquina = 0;
            // Mientras... siempre
            do {
               
                // Segu'n el tipo de evento (mensaje recibido), interpretada por el objeto "protocolo", 
                // la aplicacio'n debe realizar operaciones sobre la base de datos de 
                // usuarios:
                switch(peticion=protocolo.recibirPeticion()){
                    
                    // Solicitud de darse de alta en la base de datos:
                    
                    case Protocolo.solicitudLoguear:

                        // se comprueba si exsite ya el nombre de usuario solicitado:
                        u=(Usuario)usuarios.get(protocolo.solicitudLogin);
                    
                        // Si existe en la base de datos, se deniega el acceso
                        if(u!=null){
                            //Denegamos
                            protocolo.confirmarAliasIncorrecto();
                            
                        } else {
                            // Si no existe, se a?ade a la base de datos:
                            u=new Usuario(protocolo.solicitudLogin);
                            
                            usuarios.put(u.usuario,u);
                            System.out.println("EL usuario "+u.usuario+" ha sido dado de alta.");
                            protocolo.confirmarAlias(u.usuario);                            
                        }
                        
                        break;

                        
                     case Protocolo.solicitudVsMaquina:
                        System.out.println("El usuario "+u.usuario+" ha decidido jugar contra la maquina");
                        break;
                        
                     case Protocolo.solicitudRondas:
                        numRondas = protocolo.solicitudNumRondas;
                        System.out.println("El usuario "+u.usuario+" ha elegido "+protocolo.solicitudNumRondas+" rondas.");
                        quienEmpieza = r.nextInt(2); // 0 si empieza el jugador, 1 si empieza la maquina
                        
                        if (quienEmpieza == 0){
                            protocolo.notificarTurno(quienEmpieza, 0, 0);
                            System.out.println("Empieza el usuario "+u.usuario);
                        } else {
                            //Empezamos nosotros
                            System.out.println("Empieza la maquina contra el usuario "+u.usuario);
                            numChinosMaquina = r.nextInt(6);
                            do {
                                numChinosTotalesMaquina = r.nextInt(10);
                            } while (numChinosTotalesMaquina < numChinosMaquina);
                            System.out.println("La maquina ha elegido sacar "+numChinosMaquina+" chinos y en total: "+numChinosTotalesMaquina);
                            protocolo.notificarTurno(quienEmpieza, numChinosMaquina, numChinosTotalesMaquina);
                        }
                         break;
                         
                     case Protocolo.solicitudNumChinos:
                            numChinos = protocolo.solicitudChinosElegidos;
                            System.out.println("Elegidos "+numChinos+" chinos por el usuario "+u.usuario);
                         break;
                     case Protocolo.solicitudNumChinosTotales:
                             numChinosTotales = protocolo.solicitudChinos;
                             System.out.println("Predichos "+numChinosTotales+" chinos por el usuario "+u.usuario);
                             if(numChinosTotalesMaquina == 0){
                                numChinosMaquina = r.nextInt(6);
                                do {
                                    numChinosTotalesMaquina = r.nextInt(10);
                                } while (numChinosTotalesMaquina < numChinosMaquina);
                                System.out.println("La maquina ha elegido sacar "+numChinosMaquina+" chinos y en total: "+numChinosTotalesMaquina);
                            }
                             
                            suma = numChinos + numChinosMaquina;
                            if (suma == numChinosTotalesMaquina){
                                protocolo.notificarGanador(0, "maquina", numChinos, numChinosTotalesMaquina);
                                System.out.println("La maquina ha ganado esta ronda contra "+u.usuario);
                                numRondas--;
                            } else if(suma == numChinosTotales) {
                                protocolo.notificarGanador(1, u.usuario, numChinos, numChinosTotalesMaquina);
                                System.out.println("Esta ronda la ha ganado el usuario "+u.usuario);
                                numRondas--;
                            } else {
                                protocolo.notificarGanador(2, "Empate", numChinos, numChinosTotalesMaquina);
                                System.out.println("Empate en esta ronda contra "+u.usuario);
                            }
                            
                            // Reseteamos parametros generados
                            
                            numChinosMaquina = 0;
                            numChinosTotalesMaquina = 0;
                         
                            // Notificamos numero de rondas restantes
                            protocolo.notificarRondasRestantes(numRondas);

                         break;
                        //
                        // Solicitud de cieere de la sesio'n:
                        // CERRAR
                        //
                    case Protocolo.solicitudFinalizar:
                        protocolo.responderSolicitudCerrar("Un placer jugar contigo. Saludos.");

                        try { 
                            in.close();
                            out.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
               
                        break;
                                            
                    default:
                        break;
                }
               
                
            } while (peticion!=Protocolo.solicitudFinalizar);
        }
        
    }
}
