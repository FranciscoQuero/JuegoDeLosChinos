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
import static java.lang.Integer.parseInt;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mensajesjuegochinos.MensajeProtocoloJuegoChinos;

/**
 *
 * @author Francis
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

        
        // Co'digos de las notificaciones cliente:
        public static final int notificacionAutenticado = 1;
        public static final int   notificacionAliasIncorrecto=2;
        public static final int   notificacionRondasRestantes=3;         
        public static final int   notificacionChinos=4; 
        public static final int   notificacionGanador=5; 
        public static final int   notificacionFinalizar=6;  
        public static final int  notificacionError=7;
        

        
        // Variables temporales que contienen los datos de la notificacion en curso:
        public String alias;
        public String aliasRival;
        public int numRondas;
        public int numChinos;
        public int[] numChinosTotal;
        
        public MensajeProtocoloJuegoChinos fabricaDeMensajes;
                   
    /** Creates a new instance of Protocolo */
    public ProtocoloCliente(BufferedReader in_, PrintWriter out_) {
        in=in_;
        out=out_;        
        estado=estadoInicial;
        
        fabricaDeMensajes=new MensajeProtocoloJuegoChinos();
    }
    
    //
    // Me'todo que implementa la interpretacio'n de los mensajes recibidos por la 
    // conexio'n, las transiciones y los estados del protocolo desde el punto de 
    // vista del servidor. 
    //  Devuelve un co'digo de mensaje recibido, que la clase "servidor" utiliza para 
    // realizar la operacio'n que requiera.
    // 
    public int recibirPeticion(){
        int error=0;
        String mensaje;
        String [] palabras;
        
        try {   
            // Seg?n el estado actual, se interpreta el mensaje;
            mensaje=in.readLine();
            palabras=mensaje.split(" ");
            
            switch(estado){
                case estadoInicial:
                    
                    // Si se trata del registro:
                    System.out.println("Has entrado correctamente al estado inicial.");
                    if(palabras[0].compareTo("0001")==0){
                        System.out.println("Logueando correctamente...");

                        estado=estadoAutenticado; // Tal vez esto debería estar fuera del if
                        error = notificacionAutenticado;
                    } else if(palabras[0].compareTo("0000")==0){
                        System.out.println("Login incorrecto, intenta de nuevo.");
                        error = notificacionAliasIncorrecto;
                    } else {
                        System.err.println("Mensaje incorrecto:"+mensaje);
                    }
                    
                    break;
                    
                case estadoAutenticado:
                        if(palabras[0].compareTo("0101")==0){
                            error = notificacionAutenticado;
                            estado = estadoEsperandoChinosRival;
                        } else if (palabras[0].compareTo("0100")==0){
                            error = notificacionRondasRestantes;
                            estado = estadoEsperandoChinosRival;
                        } else if (palabras[0].compareTo("0010")==0){
                            error = notificacionFinalizar;
                            estado = estadoInicial; // Tal vez haga falta hacer algo mas
                        }
                    break;
                    
                case estadoEsperandoChinosRival:
                    
                    error = estadoEsperandoChinosRival;
                    estado = estadoEsperandoGanador;
                    //solicitudRival = new String(palabras[1]);
                    
                    break;
                case estadoEsperandoGanador:
                    error = estadoEsperandoGanador;
                    estado = estadoEsperandoNuevaRonda;
                    
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
            //ex.printStackTrace();
            error=-2;
        }
        
        
        return error;
    }

        
    // Envi'a mensaje de confirmacio'n del registro

    /**
     *
     * @param alias
     * @return
     */
    
    public void enviarLogin(String alias) {
        out.println(fabricaDeMensajes.mAlias(alias));    
        out.flush();
    }
    public void enviarVsMaquina(){
        out.println(fabricaDeMensajes.mVsMaquina());
        out.flush();
    }
    public void enviarVsHumano(String rival){
        out.println(fabricaDeMensajes.mVsHumano(rival));
        out.flush();
    }
    public void enviarNumeroRondas(int numero){
        out.println(fabricaDeMensajes.mNumeroRondas(numero));
        out.flush();
    }
    public void enviarNumeroChinos(int numero){
        out.println(fabricaDeMensajes.mNumeroChinos(numero));
        out.flush();
    }
    public void enviarNumeroChinosElegidos(int numero){
        out.println(fabricaDeMensajes.mChinos(numero));
        out.flush();
    }
    public void enviarDespedida(){
        out.println(fabricaDeMensajes.mDesconectar());
        out.flush();
    }
}
