
package mensajesjuegochinos;
/*
 * ProtocoloDirectorio.java
 *
 * Created on 18 de marzo de 2006, 3:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author jjramos
 */

//
// Clase padre del protocolo de directorio implementado.
// Especifica los formatos de mensajes del protocolo y otras
// operaciones comunes. 
// 
public class MensajeProtocoloJuegoChinos {
    
    static final int errorEnRegistro=1;
    
    /** Creates a new instance of ProtocoloDirectorio */
    public void Protocolo() {
    }
    
public     String mensajeBienvenidaCliente(String texto){
        
        return "Cliente Juego de los Chinos 1.0 fjqr "+texto;
    }
public     String mAlias(String login){
        String mensaje;
        mensaje="1110"+" "+login+" "+"*";
        return mensaje;
    }
public     String mAliasCorrecto(String texto){
        String mensaje;
        mensaje="0001"+" "+texto+" "+"*";
        return mensaje;
    }
    
public     String mAliasIncorrecto(String texto){
        String mensaje;
        mensaje="0000"+" "+texto+" "+"*";
        return mensaje;
    }

public     String mVsHumano(String texto){
        String mensaje;
        mensaje="0101"+" "+texto+" "+"*";
        return mensaje;
    }

public     String mVsMaquina(){
        String mensaje;
        
        mensaje="0100"+" "+"*";
       
        return mensaje;
    }

public     String mReiniciar(){
        String mensaje;
        
        mensaje="0011"+" "+"*";
       
        return mensaje;
    }

public     String mDesconectar(){
        String mensaje;
        
        mensaje="0010"+" "+"*";
       
        return mensaje;
    }

 public    String mJugadorEncontrado(String texto){
        String mensaje;        
        mensaje="0110"+" "+texto+" "+"*";
        return mensaje;
    }
    
public     String mNumeroRondas(int nRondas){
        String mensaje;
        mensaje="0111"+" "+nRondas+" "+"*";
        return mensaje;
    }
public     String mNumeroChinos(int nChinos){
        String mensaje;
        mensaje="0111"+" "+nChinos+" "+"*";
        return mensaje;
    }    

public     String mHablaJugador(int empiezaJugador, int numChinosMano, int numChinosTotal ){
        String mensaje;
        mensaje="1001" +" "+ empiezaJugador + " " + numChinosMano + " " + numChinosTotal + " "+"*";
        return mensaje;
    }
    
public     String mChinos(int numChinosMano, int numChinosTotal){
        String mensaje;
        
        mensaje = "1010" +" "+ numChinosMano + " " + numChinosTotal +" "+ "*";
        
            return mensaje;
    }
    
public     String mGanador(int ganador, String texto, String alias, int numChinosMano, int numChinosTotal){
        String mensaje;
        
        mensaje = "1011" +" "+ ganador +" "+ texto +" "+ alias +" "+ numChinosMano +" "+ numChinosTotal +" "+ "*";
        
            return mensaje;
    }
    
public     String mFinalizar(String texto){
        String mensaje;
        
        mensaje = "1100"+" "+texto+" "+"*";
        
        return mensaje;
    }
    
public     String mError(String texto){
        String mensaje;
        
        mensaje = "1101"+" "+texto+" "+"*";
        
        return mensaje;
    }

    public String mensajeSaludoCliente(String hola) {
      return hola;
    }
}
