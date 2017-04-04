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
package juegodeloschinos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import mensajesjuegochinos.MensajeProtocoloJuegoChinos;

/**
 * Clase Protocolo implementa los estados posibles ne los que puede entrar
 * el protocolo del servidor del juego de los chinos, así como los métodos 
 * necesarios para enviar los mensajes al cliente. También recibe e interpreta los
 * mensajes enviados del cliente al servidor.
 * @author Francisco J. Quero
 */
public class Protocolo extends MensajeProtocoloJuegoChinos {
    
        String nombre;
        PrintWriter out;
        BufferedReader in;
        int estado;
  
        // Estados del protocolo (ver el disenio de la ma'quina de estados)
        static final int estadoInicial=0;
        static final int estadoAutenticado=1;
        static final int estadoVsMaquina=2;
        static final int estadoVsHumano=3;
        static final int estadoEsperandoJ2=4;
        static final int estadoEsperandoNumeroRondas=5;
        static final int estadoEsperandoNumeroChinos=6;
        static final int estadoFinal=8;
        static final int estadoError=9;
        
        // Co'digos de las solicitudes servidor:
        public static final int solicitudLoguear = 1;
        public static final int solicitudVsHumano=2;
        public static final int solicitudVsMaquina=3;         
        public static final int solicitudDesconectar=4; 
        public static final int solicitudVsJugador=5; 
        public static final int solicitudEsperarJ2=6;  
        public static final int solicitudFinalizar=7;
        public static final int solicitudRondas = 8;
        public static final int solicitudNumChinos = 9;
        public static final int solicitudNumChinosTotales = 10;
        public static final int solicitudGanador = 11;
        

        
        // Variables temporales que contienen los datos de la solicitud en curso:
        public String solicitudLogin;
        public String solicitudRival;
        public int solicitudNumRondas;
        public int solicitudChinos;
        public int solicitudChinosElegidos;
        
        MensajeProtocoloJuegoChinos fabricaDeMensajes;
                   
    /** Creates a new instance of Protocolo */
    public Protocolo( String n, BufferedReader in_, PrintWriter out_) {
        in=in_;
        out=out_;
        nombre=n;
        
        estado=estadoInicial;
        fabricaDeMensajes=new MensajeProtocoloJuegoChinos();
    }

    /**
     * Método que implementa la interpretación de los mensajes recibidos por la 
     * conexión, las transiciones y los estados del protocolo desde el punto de 
     * vista del servidor. 
     * @return Devuelve un código de mensaje recibido, que la clase "servidor" utiliza para
     * realizar la operación que requiera.
     */
    public int recibirPeticion(){
        int error=0;
        String mensaje;
        String [] palabras;
        
        try {   
            // Según el estado actual, se interpreta el mensaje;
            mensaje=in.readLine();
            palabras=mensaje.split(" ");
            // System.out.println(mensaje);
            
            switch(estado){
                case estadoInicial:
                    error = solicitudLoguear;
                    // Si se trata del login:
                    if(palabras[0].compareTo("1110")==0){
                        System.out.println("Logueando como "+palabras[1]+"...");
                        // Ojo, no se comprueba el n?mero de campos, as? que es
                    // "Explotable"
                    solicitudLogin=new String(palabras[1]);
                    
                    estado=estadoAutenticado;
                    
                    } else {
                        System.err.println("Peticion incorrecta: "+mensaje);
                    }
                    
                    break;
                    
                case estadoAutenticado:
                    
                        if(palabras[0].compareTo("0101")==0){
                            error = solicitudVsHumano;
                            estado = estadoVsHumano;
                        } else if (palabras[0].compareTo("0100")==0){
                            error = solicitudVsMaquina;
                        } else if (palabras[0].compareTo("0010")==0){
                            error = solicitudDesconectar;
                            estado = estadoInicial;
                        } else if (palabras[0].compareTo("0111")==0){ //Esta enviando numero de rondas
                            solicitudNumRondas = parseInt(palabras[1]);
                            error = solicitudRondas;
                            if (solicitudNumRondas == 0) {
                                estado = estadoFinal;
                            } else {
                                estado = estadoEsperandoNumeroChinos;
                            }
                            
                        }
                    break;
                    
                case estadoVsHumano:
                    
                    error = solicitudVsJugador;
                    estado = estadoEsperandoJ2;
                    solicitudRival = new String(palabras[1]);
                    
                    break;
                case estadoVsMaquina:
                    error = solicitudVsMaquina;
                    estado = estadoAutenticado;
                    
                    break;
                case estadoEsperandoJ2:
                    error = solicitudEsperarJ2;
                    estado = estadoEsperandoNumeroRondas;
                    solicitudNumRondas = Integer.parseInt(palabras[1]);
                    break;
                case estadoEsperandoNumeroRondas:
                    
               
                    error = solicitudRondas;

                    break;
                case estadoEsperandoNumeroChinos:
                    if (palabras[0].compareTo("1000")==0){ //Esta enviando numero de chinos totales
                            solicitudChinos = Integer.parseInt(palabras[1]);
                            error = solicitudNumChinosTotales;
                            
                            //solicitudNumRondas--;
                            if (solicitudNumRondas == 0) {
                                estado = estadoFinal;
                            } else {
                                estado = estadoAutenticado;
                            }
                        } else if (palabras[0].compareTo("1010")==0){ //Esta enviando numero de chinos en mano
                            solicitudChinosElegidos = Integer.parseInt(palabras[1]);
                            error = solicitudNumChinos;
                            
                            estado = estadoEsperandoNumeroChinos;
                        }
                    break;
                case estadoFinal:
                    
                    if (palabras[0].compareTo("1100")==0)
                        error = solicitudFinalizar;
                    
                    estado = estadoInicial;
                    break;
                case estadoError:
                    error = estadoError;
                    estado = estadoInicial;
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
     * Envía el mensaje de confirmación de alias correcto
     * @param alias nombre confirmado
     * @return posible error
     */
    public int confirmarAlias(String alias){
        int error=0;
        
        out.println(fabricaDeMensajes.mAliasCorrecto("Te has identificado correctamente."));
        estado=estadoAutenticado;
        return error;
    }
    /**
     * Envía el mensaje de alias incorrecto
     * @return 
     */
    public int confirmarAliasIncorrecto(){
        int error=0;
        out.println(fabricaDeMensajes.mAliasIncorrecto("Error de identificacion. Prueba con otro alias."));
        return error;
    }
    /**
     * Envía el mensaje de confirmación de segundo jugador encontrado
     * @param texto nombre del jugador encontrado
     * @return 
     */
    public int notificarJugadorEncontrado(String texto){
        int error=0;
        out.println(fabricaDeMensajes.mJugadorEncontrado("Se ha encontrado el usuario."));
        estado=estadoEsperandoNumeroRondas;
        return error;
    }
    /**
     * Envía el mensaje de notificación de turno
     * @param empiezaJugador 0 si empieza la máquina, 1 si empieza le jugador notificado
     * @param numChinosMano número de chinos en la mano del rival/servidor
     * @param numChinosTotal número de chinos predichos por el rival/servidor
     */
    public void notificarTurno(int empiezaJugador, int numChinosMano, int numChinosTotal) {
       out.println(fabricaDeMensajes.mHablaJugador(empiezaJugador, numChinosMano, numChinosTotal));   
    }
    /**
     * Envía el mensaje de notificación del ganador
     * @param ganador 0/1/2
     * @param aliasGanador Nombre del ganador
     * @param numChinosMano Número de chinos en la mano del rival o servidor
     * @param numChinosTotal Número de chinos predichos por el rival o servidor
     */
    public void notificarGanador(int ganador, String aliasGanador, int numChinosMano, int numChinosTotal) {
        out.println(fabricaDeMensajes.mGanador(ganador, aliasGanador, numChinosMano, numChinosTotal));
    }
    /**
     * Envía el mensaje de notificación de final de partida
     * @param despedida mensaje de despedida
     */
    public void notificarFinal(String despedida) {
      out.println(fabricaDeMensajes.mFinalizar(despedida));
    }
    /**
     * Envía el mensaje de notificación de error
     * @param textoError mensaje de error
     */
    public void notificarError(String textoError) {
     out.println(fabricaDeMensajes.mError(textoError));  
    }
    /**
     * Envía el mensaje de notificación de rondas restantes
     * @param rondas número de rondas restantes
     */
    public void notificarRondasRestantes(int rondas){
        out.println(fabricaDeMensajes.mNumeroRondas(rondas));
    }
}
