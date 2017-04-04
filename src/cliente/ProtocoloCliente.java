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
 * Visit https://github.com/FranciscoQuero/JuegoDeLosChinos for updates and more info
 */
package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import mensajesjuegochinos.MensajeProtocoloJuegoChinos;

/**
 * Clase ProtocoloCliente implementa los estados posibles ne los que puede entrar
 * el protocolo del cliente del juego de los chinos, así como los métodos 
 * necesarios para enviar los mensajes al servidor. También recibe e interpreta los
 * mensajes enviados del servidor al cliente.
 * @author Francisco J. Quero
 */
public class ProtocoloCliente extends MensajeProtocoloJuegoChinos {
    
        PrintWriter out;
        BufferedReader in;
        int estado;
  
        // Estados del protocolo (ver el disenio de la ma'quina de estados)
        static final int estadoInicial=0;
        static final int estadoEsperandoAutenticacion=1;
        static final int estadoAutenticado=2;
        static final int estadoEsperandoChinosRival=3;
        static final int estadoEsperandoGanador=4;
        static final int estadoEsperandoNuevaRonda = 5;
        static final int estadoError = 6;
        static final int estadoFinalizar = 7;

        
        // Co'digos de las notificaciones cliente:
        public static final int notificacionAutenticado = 1;
        public static final int   notificacionAliasIncorrecto=2;
        public static final int   notificacionRondasRestantes=3;         
        public static final int   notificacionChinos=4; 
        public static final int   notificacionGanador=5; 
        public static final int   notificacionFinalizar=6;  
        public static final int  notificacionError=7;
        public static final int notificacionTurno = 8;
        

        
        // Variables temporales que contienen los datos de la notificacion en curso:
        public String alias;
        public String aliasRival;
        public int numRondas;
        public int numChinos;
        public int numChinosTotal;
        public boolean hablasElPrimero;
        public int ganador;
        public String ganadorAlias;
        
        public MensajeProtocoloJuegoChinos fabricaDeMensajes;
                   
    /**
     * Crea un objeto de la clase Protocolo
     * @param in_ buffer de entrada
     * @param out_ buffer de salida
     */
    public ProtocoloCliente(BufferedReader in_, PrintWriter out_) {
        in=in_;
        out=out_;        
        estado=estadoInicial;
        
        fabricaDeMensajes=new MensajeProtocoloJuegoChinos();
    }
    
    /**
     * Método que implementa la interpretación de los mensajes recibidos por la 
     * conexión, las transiciones y los estados del protocolo desde el punto de 
     * vista del cliente. 
     * Devuelve un código de mensaje recibido, que la clase "ClienteTCP" utiliza para 
     * realizar la operación que requiera.
     * @return un código único perteneciente al tipo de mensaje recibido
     */
    
    // 
    public int recibirPeticion(){
        int error=0;
        int aux;
        String mensaje;
        String [] palabras;
        
        try {   
            // Segun el estado actual, se interpreta el mensaje;
            mensaje=in.readLine();
            // Se divide el mensaje en palabras:
            palabras=mensaje.split(" ");
            
            // Descomentar para que aparezca en la linea de comandos el mensaje recibido:
            // System.out.println(mensaje); 
            switch(estado){
            // Estado Inicial
                case estadoInicial:
                    
                    // Si se trata de confirmacion de login 0001:
                    if(palabras[0].compareTo("0001")==0){
                        System.out.println("Logueado correctamente...");

                        estado=estadoAutenticado; 
                        error = notificacionAutenticado;
                    // Si se trata de login incorrecto 0000:
                    } else if(palabras[0].compareTo("0000")==0){
                        System.out.println("Login incorrecto, intenta de nuevo.");
                        error = notificacionAliasIncorrecto;
                    } else {
                        System.err.println("Mensaje incorrecto:"+mensaje);
                    }
                    
                    break;
                
            // Estado autenticado
                case estadoAutenticado:
                        // Si se trata de notificacion de turno:
                        if (palabras[0].compareTo("1001")==0){
                            error = notificacionTurno;
                            aux = parseInt(palabras[1]);
                            if(aux == 0) {
                                hablasElPrimero = true;
                                System.out.println("Te toca hablar el primero.");
                            }
                            else {
                                hablasElPrimero = false;
                                numChinos = parseInt(palabras[2]);
                                numChinosTotal = parseInt(palabras[3]);
                                System.out.println("Tu rival ya ha hablado.");
                                
                            }
                            estado = estadoEsperandoGanador;
                            
                        // Si se trata de desconexion:
                        } else if (palabras[0].compareTo("0010")==0){
                            error = notificacionFinalizar;
                            estado = estadoInicial;
                        }
                    break;
            // Estado Esperando Chinos Rival
                case estadoEsperandoChinosRival:
                    error = estadoEsperandoChinosRival;
                    estado = estadoEsperandoGanador;
                    break;
            // Estado Esperando Ganador
                case estadoEsperandoGanador:
                    error = notificacionGanador;
                    // Comprueba que sea una notificacion de ganador
                    if (palabras[0].compareTo("1011")==0){
                        ganador = Integer.parseInt(palabras[1]);
                        ganadorAlias = palabras[2];
                        // Se añaden los chinos elegidos por el rival por si no se habian mandado antes:
                        numChinos = Integer.parseInt(palabras[3]); 
                        numChinosTotal = Integer.parseInt(palabras[4]);
                    }
                    estado = estadoEsperandoNuevaRonda;
                    break;
            // Estado Esperando Nueva Ronda
                case estadoEsperandoNuevaRonda:
                    error = notificacionRondasRestantes;
                    numRondas = Integer.parseInt(palabras[1]);
                    // Puede comenzar nueva ronda o finalizar el juego
                    if(numRondas == 0){
                        estado = estadoFinalizar;
                    } else {
                        estado = estadoAutenticado;
                    }
                    break;
            // Estado Final
                case estadoFinalizar:
                    System.out.println("El juego ha finalizado.");
                    System.exit(0);
                    break;
            // Estado de Error
                case estadoError:
                    error = estadoError;
                    estado = estadoInicial; // Devuelve al estado inicial en cualquier caso
                    break;
                default:
                    error=-1;
                    break;
            };
        } catch (IOException ex) {
            error=-2;
        }
        
        
        return error;
    }

       

    /**
     * Envia solicitud de login de un alias enviado
     * @param alias un alias con el que se registrará el usuario
     */
    
    public void enviarLogin(String alias) {
        out.println(fabricaDeMensajes.mAlias(alias));    
        out.flush();
    }
    /**
     * Envía el mensaje de peticion de juego contra la máquina
     */
    public void enviarVsMaquina(){
        out.println(fabricaDeMensajes.mVsMaquina());
        out.flush();
    }
    /**
     * Envía el mensaje de petición de juego contra un segundo jugador
     * @param rival nombre del rival con el que quieres jugar
     */
    public void enviarVsHumano(String rival){
        out.println(fabricaDeMensajes.mVsHumano(rival));
        out.flush();
    }
    /**
     * Envía el número de rondas restantes o introducidas por el usuario
     * @param numero número de rondas restantes/introducidas
     */
    public void enviarNumeroRondas(int numero){
        out.println(fabricaDeMensajes.mNumeroRondas(numero));
        out.flush();
    }
    /**
     * Envía el número de chinos totales predichos por el jugador
     * @param numero chinos predichos
     */
    public void enviarNumeroChinos(int numero){
        out.println(fabricaDeMensajes.mNumeroChinos(numero));
        out.flush();
    }
    /**
     * Envía el número de chinos elegidos en la mano
     * @param numero chinos en mano
     */
    public void enviarNumeroChinosElegidos(int numero){
        out.println(fabricaDeMensajes.mChinos(numero));
        out.flush();
    }
    /**
     * Envía un mensaje simple de petición de cierre de sesión
     */
    public void enviarDespedida(){
        out.println(fabricaDeMensajes.mFinalizar("Adios."));
        out.flush();
    }
}
