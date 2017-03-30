/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package juegodeloschinos;

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
        static final int estadoFinal=7;
        static final int estadoError=8;
        
        // Co'digos de las solicitudes:
        public static final int solicitudLoguear = 1;
        public static final int   solicitudVsHumano=2;
        public static final int   solicitudVsMaquina=3;         
        public static final int   solicitudDesconectar=4; 
        public static final int   solicitudVsJugador=5; 
        public static final int   solicitudEsperarJ2=6;  
        public static final int  solicitudFinalizar=7;
        
        // Variables temporales que contienen los datos de la solicitud en curso:
        public String solicitudLogin;
        public String solicitudRival;
        public int solicitudNumRondas;
        public int solicitudChinos;
        public int[] solicitudChinosElegidos;
        
        MensajeProtocoloJuegoChinos fabricaDeMensajes;
                   
    /** Creates a new instance of Protocolo */
    public Protocolo( String n, BufferedReader in_, PrintWriter out_) {
        in=in_;
        out=out_;
        nombre=n;
        
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
                    error = solicitudLoguear;
                    // Si se trata del registro:
                    if(palabras[0].compareTo("1110")==0){
                        System.out.println("Logueando como "+palabras[1]+"...");
                        // Ojo, no se comprueba el n?mero de campos, as? que es
                    // "Explotable"
                    solicitudLogin=new String(palabras[1]);
                    
                    
                    //solicitudPuerto=Integer.valueOf(palabras[5]);
                    estado=estadoAutenticado; // Tal vez esto debería estar fuera del if
                    
                    
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
                            estado = estadoVsMaquina;
                        } else if (palabras[0].compareTo("0010")==0){
                            error = solicitudDesconectar;
                            estado = estadoInicial; // Tal vez haga falta hacer algo mas
                        }
                    break;
                    
                case estadoVsHumano:
                    
                    error = solicitudVsJugador;
                    estado = estadoEsperandoJ2;
                    solicitudRival = new String(palabras[1]);
                    
                    break;
                case estadoVsMaquina:
                    error = solicitudVsMaquina;
                    estado = estadoEsperandoNumeroRondas;
                    
                    break;
                case estadoEsperandoJ2:
                    error = solicitudEsperarJ2;
                    estado = estadoEsperandoNumeroRondas;
                    
                    break;
                case estadoEsperandoNumeroRondas:
                    solicitudNumRondas = parseInt(palabras[1]);
                    error = estadoEsperandoNumeroRondas;
                    estado = estadoEsperandoNumeroChinos;
                    
                    break;
                case estadoEsperandoNumeroChinos:
                    solicitudNumRondas = parseInt(palabras[1]);
                    error = estadoEsperandoNumeroChinos;
  //Como elijo entre estados?                  estado = estado;
                    
                    break;
                case estadoFinal:
                    //¿?
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
    public int confirmarAlias(String alias){
        int error=0;
        
        out.println(fabricaDeMensajes.mAliasCorrecto("Te has identificado correctamente."));
        estado=estadoAutenticado;
        return error;
    }
    
    // Envi'a mensaje de confirmacio'n de contacto aniadido:
    public int confirmarAliasIncorrecto(){
        int error=0;
        out.println(fabricaDeMensajes.mAliasIncorrecto("Error de identificacion. Prueba con otro alias."));
        return error;
    }
    
     // Envi'a mensaje de denegacio'n de contacto aniadido:
    public int notificarJugadorEncontrado(String texto){
        int error=0;
        out.println(fabricaDeMensajes.mJugadorEncontrado("Se ha encontrado el usuario."));
        estado=estadoEsperandoNumeroRondas;
        return error;
    }

    public void notificarTurno(int empiezaJugador, int numChinosMano, int numChinosTotal) {
       out.println(fabricaDeMensajes.mHablaJugador(empiezaJugador, numChinosMano, numChinosTotal));   
    }

    public void notificarFinal(String despedida) {
      out.println(fabricaDeMensajes.mFinalizar(despedida));
    }

    public void notificarError(String textoError) {
     out.println(fabricaDeMensajes.mError(textoError));  
    }
    
    public void responderSolicitudCerrar(String texto){
        out.println(fabricaDeMensajes.mJugadorEncontrado(texto));
    }
}

    
    // Tarea pendiente: separar los cases de modo que una vez logueado, no puedas
    // recibir nuevos logins por ejemplo. Es opcional porque un cliente bien diseñado
    // no debería dar problemas.
    /*public void main(Socket socketConexion, ArrayList<String> listadoAlias, String []argumentos) {
        String respuesta = "";
        String linea, mensaje;
        
        try{
            PrintWriter out = new PrintWriter(socketConexion.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socketConexion.getInputStream()));

            
            // Leemos una petición:
            linea = in.readLine();

            // Interpretamos la petición. Para que no haya problema con las letras de la palabra
            // al compararlas con los comandos, las pasamos todas a minúsculas:
            mensaje = linea.toLowerCase();
            mensaje = mensaje.substring(5, mensaje.length()-1);

            int codigo = Integer.parseInt(mensaje.substring(0, 4));

            switch(codigo){
                case 2:
                    respuesta = this.desconexion(mensaje);
                    out.print(respuesta);
                break;
                case 4:
                    // Mensaje de peticion vs maquina
                break;
                case 5:
                    // Mensaje de peticion vs humano
                break;
                case 7:
                    // Mensaje de numero de rondas
                break;
                case 8:
                    // Mensaje de numero de chinos mandados
                break;
                case 14:
                    this.login(mensaje);
                    
                    if ("".equals(this.alias)) {
                        respuesta = "No logueado. Intenta nuevo alias";
                    }
                    else {
                        respuesta = "Logueado correctamente con el alias" + this.alias;
                    }
                    out.print(respuesta);
                break;
                default:
                    respuesta = "Mensaje erroneo";
                    out.print(respuesta);
                break;
            }
        }catch (IOException ex) {
            Logger.getLogger(JuegoDeLosChinos.ServidorTCP.class.getName()).log(Level.SEVERE, null, ex);     
        }  
    }*/
