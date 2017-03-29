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
        public static final int   solicitudRegistrar=2;
        public static final int   solicitudAnadir=3;         
        public static final int   solicitudListado=4; 
        public static final int   solicitudCerrar=5; 
        public static final int   solicitudEliminar=6;  
        public static final int  solicitudLocalizar=7;
        
        // Variables temporales que contienen los datos de la solicitud en curso:
        String solicitudLogin;
        String solicitudContrasenia;
        String solicitudApodo;
        String solicitudDireccion;
        int solicitudPuerto;
        
        MensajeProtocoloJuegoChinos fabricaDeMensajes;
                   
    /** Creates a new instance of ServidorProtocoloDirectorio */
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
    int recibirPeticion(){
        int error=0;
        String mensaje;
        String [] palabras;
        
        try {   
            // Seg?n el estado actual, se interpreta el mensaje;
            mensaje=in.readLine();
            palabras=mensaje.split(" ");
            
            switch(estado){
                case estadoInicial:
                    
                    // se obtiene el mensaje de bienvenida del cliente:
                    System.out.println(nombre+": se recibe saludo ("+mensaje+")");            
                    estado=estadoAutenticado;
                    
                    break;
                    
                case estadoAutenticado:

                    // Si se trata del registro:
                    if(palabras[0].compareTo("REGISTRAR")==0){
                        System.out.println("Registrando a "+palabras[1]+"...");
                        // Ojo, no se comprueba el n?mero de campos, as? que es
                    // "Explotable"
                    solicitudLogin=new String(palabras[1]);
                    solicitudContrasenia=new String (palabras[2]);
                    solicitudApodo=new String (palabras[3]);
                    solicitudDireccion=new String(palabras[4]);
                    //solicitudPuerto=Integer.valueOf(palabras[5]);
                    
                    } else {
                        System.err.println("Petici?n incorrecta: "+mensaje);
                    }
                    error=solicitudRegistrar;
                    
                    break;
                    
                case estadoVsHumano:
                    
                    // Seg?n el tipo de solicitud que se haga...
                    if(palabras[0].compareTo("ANADIRCONTACTO")==0){
                        
                        // Comprobamos que el otro usuario existe:
                        error=solicitudAnadir;
                        solicitudLogin=palabras[1];
                    } else if(palabras[0].compareTo("LISTARCONTACTOS")==0){
                        // enviar la lista de contactos:
                        error=solicitudListado;
                    } else if(palabras[0].compareTo("CERRAR")==0){
                        // Cerrar la conexi?n:
                        error=solicitudCerrar;
                    } else if(palabras[0].compareTo("ELIMINAR")==0){
                        // Eliminar contacto
                        error=solicitudEliminar;
                        solicitudLogin=palabras[1];
                    } else if(palabras[0].compareTo("LOCALIZAR")==0){
                        // Devolver la direccion del usuario especificado:
                        error=solicitudLocalizar;
                        solicitudLogin=palabras[1];
                    }
                    
                    break;
                case estadoVsMaquina:
                    break;
                case estadoEsperandoJ2:
                    break;
                case estadoEsperandoNumeroRondas:
                    break;
                case estadoEsperandoNumeroChinos:
                    break;
                case estadoFinal:
                    break;
                case estadoError:
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
    int confirmarAliasIncorrecto(){
        int error=0;
        out.println(fabricaDeMensajes.mAliasIncorrecto("Error de identificacion. Prueba con otro alias."));
        return error;
    }
    
     // Envi'a mensaje de denegacio'n de contacto aniadido:
     int notificarJugadorEncontrado(String texto){
        int error=0;
        out.println(fabricaDeMensajes.mJugadorEncontrado("Se ha encontrado el usuario."));
        estado=estadoEsperandoNumeroRondas;
        return error;
    }

    void notificarTurno(int empiezaJugador, int numChinosMano, int numChinosTotal) {
       out.println(fabricaDeMensajes.mHablaJugador(empiezaJugador, numChinosMano, numChinosTotal));   
    }

    void notificarFinal(String despedida) {
      out.println(fabricaDeMensajes.mFinalizar(despedida));
    }

    void notificarError(String textoError) {
     out.println(fabricaDeMensajes.mError(textoError));  
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
