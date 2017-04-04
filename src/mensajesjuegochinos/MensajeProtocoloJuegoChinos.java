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
package mensajesjuegochinos;

//
/**
 * Clase padre del protocolo de directorio implementado.
 * Especifica los formatos de mensajes del protocolo y otras
 * operaciones comunes. 
 * @author Francisco J. Quero
*/
public class MensajeProtocoloJuegoChinos {
    
    static final int errorEnRegistro=1;
    
    /** Constructor vacío, no contiene campos. */
    public void Protocolo() {
    }
    /**
     * Elabora el mensaje de petición de Alias
     * @param login alias para loguearte
     * @return mensaje elaborado
     */
public     String mAlias(String login){
        String mensaje;
        mensaje="1110"+" "+login+" "+"*";
        return mensaje;
    }
    /**
     * Elabora el mensaje de confirmación de alias
     * @param texto confirmación
     * @return mensaje elaborado
     */
public     String mAliasCorrecto(String texto){
        String mensaje;
        mensaje="0001"+" "+texto+" "+"*";
        return mensaje;
    }
    /**
     * Elabora el mensaje de notificación de login incorrecto
     * @param texto mensaje de error
     * @return mensaje elaborado
     */
    public     String mAliasIncorrecto(String texto){
            String mensaje;
            mensaje="0000"+" "+texto+" "+"*";
            return mensaje;
        }
    /**
     * Elabora el mensaje de petición de juego contra un humano
     * @param texto alias del rival
     * @return mensaje elaborado
     */
    public     String mVsHumano(String texto){
            String mensaje;
            mensaje="0101"+" "+texto+" "+"*";
            return mensaje;
        }
    /**
     * Elabora el mensaje de petición de juego contra la máquina
     * @return mensaje elaborado
     */
    public     String mVsMaquina(){
            String mensaje;

            mensaje="0100"+" "+"*";

            return mensaje;
        }
    /**
     * Elabora el mensaje de petición de reinicio del juego
     * @return mensaje elaborado
     */
    public     String mReiniciar(){
            String mensaje;

            mensaje="0011"+" "+"*";

            return mensaje;
        }
    /**
     * Elabora el mensaje de desconexión
     * @return mensaje elaborado
     */
    public     String mDesconectar(){
            String mensaje;

            mensaje="0010"+" "+"*";

            return mensaje;
        }
    /**
     * Elabora el mensaje de notificación de que el jugador 2 ha sido encontrado
     * @param texto mensaje de confirmación
     * @return mensaje elaborado
     */
     public    String mJugadorEncontrado(String texto){
            String mensaje;        
            mensaje="0110"+" "+texto+" "+"*";
            return mensaje;
        }
     /**
      * Elabora el mensaje de notificaión de número de rondas restantes
      * @param nRondas entero con las rondas restantes actualmente
      * @return mensaje elaborado
      */
    public     String mNumeroRondas(int nRondas){
            String mensaje;
            mensaje="0111"+" "+nRondas+" "+"*";
            return mensaje;
        }
    /**
     * Elabora el mensaje de notificación de elección de chinos predichos
     * @param nChinos numero de chinos predichos
     * @return mensaje elaborado
     */
    public     String mNumeroChinos(int nChinos){
            String mensaje;
            mensaje="1000"+" "+nChinos+" "+"*";
            return mensaje;
        }    
    /**
     * Elabora el mensaje de notificación de turno, adjuntando, si fuera necesario,
     * la elección anterior del rival, en caso de que el usuario notificado sea el
     * segundo en hablar
     * @param empiezaJugador 0 si empieza, 1 si ya ha hablado el rival
     * @param numChinosMano numero de chinos elegidos por el rival en caso de que ya haya hablado
     * @param numChinosTotal numero de chinos predichos por el rival en caso de que ya haya hablado
     * @return mensaje elaborado
     */
    public     String mHablaJugador(int empiezaJugador, int numChinosMano, int numChinosTotal ){
            String mensaje;
            mensaje="1001" +" "+ empiezaJugador + " " + numChinosMano + " " + numChinosTotal + " "+"*";
            return mensaje;
        }
    /**
     * Elabora el mensaje de notificación del número de chinos predichos
     * @param numChinosMano número de chinos predichos
     * @return mensaje elaborado
     */
    public     String mChinos(int numChinosMano){
            String mensaje;

            mensaje = "1010" +" "+ numChinosMano +" "+ "*";

                return mensaje;
        }
    /**
     * Elabora el mensaje de notificación del ganador de la ronda, junto con los
     * parámetros del rival por si no se conocían previamente
     * @param ganador 0 si ha ganado el servidor, 1 si ha ganado el jugador, 2 si empate
     * @param alias nombre del ganador
     * @param numChinosMano chinos del servidor/rival en la mano
     * @param numChinosTotal chinos predichos por el servidor/rival
     * @return mensaje elaborado
     */
    public     String mGanador(int ganador, String alias, int numChinosMano, int numChinosTotal){
            String mensaje;

            mensaje = "1011" +" "+ ganador +" "+ alias +" "+ numChinosMano +" "+ numChinosTotal +" "+ "*";

                return mensaje;
        }
    /**
     * Elabora el mensaje de petición de finalización de partida
     * @param texto mensaje de petición, texto no procesado
     * @return mensaje elaborado
     */
    public     String mFinalizar(String texto){
            String mensaje;

            mensaje = "1100"+" "+texto+" "+"*";

            return mensaje;
        }
    /**
     * Elabora el mensaje de error
     * @param texto mensaje de error
     * @return mensaje elaborado
     */
    public     String mError(String texto){
            String mensaje;

            mensaje = "1101"+" "+texto+" "+"*";

            return mensaje;
        }
}
