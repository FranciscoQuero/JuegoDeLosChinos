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
package JuegoDeLosChinos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jjramos
 */
public class ServidorTCP {
    //public static final ArrayList<String> listadoAlias = new ArrayList<>(); // Intento de variable global
    boolean salir = false;
    boolean finSesion = false;
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
     * Método principal.
     * @param argumentos 
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
    //
    //
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
                protocolo=new Protocolo(this.currentThread().getName(),in,out);
            
            } catch(IOException e){
                // Se acabo'
                System.err.println("Error en la hebra "+this.currentThread().getName());
            }
        }
        
        // Hebra principal del servicio
        public void run(){
            int peticion=0;
            String mensaje;
            String [] palabras;
            Usuario u=null,u0;
            
            // Mientras... siempre
            do {
               
                // Segu'n el tipo de evento (mensaje recibido), interpretada por el objeto "protocolo", 
                // la aplicacio'n debe realizar operaciones sobre la base de datos de 
                // usuarios:
                switch(peticion=protocolo.recibirPeticion()){
                    
                    // Solicitud de darse de alta en la base de datos:
                    // Mensaje:
                    //  REGISTRAR <nombre de usuario> <contasenia> <apodo> <direccio'n de la ma'quina donde se encuentra>'
                    case Protocolo.solicitudRegistrar:

                        System.out.println(protocolo.solicitudLogin);

                        // se comprueba si exsite ya el nombre de usuario solicitado:
                        u=(Usuario)usuarios.get(protocolo.solicitudLogin);
                   
                        // Si existe en la base de datos, se comprueba la contrase?a:
                        if(u!=null){
                            u.asignarContrasenia(protocolo.solicitudContrasenia);
                            u.asignarLocalizacion(protocolo.solicitudDireccion,protocolo.solicitudPuerto);
                        
                            // Si coinciden las contrase?as, se acepta el registro
                            if(u.contrasenia.compareTo(protocolo.solicitudContrasenia)==0){
                                System.out.println("Usuario "+protocolo.solicitudLogin+" registrado.");
                                protocolo.confirmarRegistro(u.usuario);
                            } else {
                                // En caso de contrasenia no va'lida, se devuelve el 
                                // mensaje de error correspondiente:
                                // ****** A completar ******
                                //protocolo.denegarRegistro();                                
                            }
                            
                        } else {
                            // Si no existe, se a?ade a la base de datos:
                            u=new Usuario(protocolo.solicitudLogin,protocolo.solicitudContrasenia);
                            u.asignarLocalizacion(protocolo.solicitudDireccion,0);
                            usuarios.put(u.usuario,u);
                            System.out.println("EL usuario "+u.usuario+" ha sido dado de alta.");
                            protocolo.confirmarRegistro(u.usuario);                            
                        }
                        
                        break;
                        
                        // Se solicita aniadir un contacto a la lista de contactos:
                        // Mensaje:
                        // ANADIRCONTACTO <nombre de usuario>
                        //
                    case Protocolo.solicitudAnadir:
                        
                        // Comprobamos si existe el usuario a a?adir:
                        u0=(Usuario)usuarios.get(protocolo.solicitudLogin);
                        
                        if(u0==null){
                            // No existe:
                            //protocolo.usuarioNoExiste();
                            protocolo.responderAdicionDenegada(protocolo.solicitudLogin);
                        } else {
                            // Existe y lo a?adimos a la lista del usuario.
                            //
                            u.anadirContacto(u0.usuario);
                            u.listaContactos();
                            protocolo.responderAdicionAceptada(u0.usuario);
                        }
                        
                        break;
                    
                        // Solicitud de la lista de contactos:
                        // LISTARCONTACTOS
                        //
                     case Protocolo.solicitudListado:
                     {
                        String []lista;
                        int j=0;
                        
                        // Creamos la lista de los contactos y la enviamos en el mensaje correspondiente:
                        lista=new String[u.contactos.size()];                        
                        for (ListIterator i = u.contactos.listIterator(u.contactos.size()); i.hasPrevious(); ) {
                            lista[j]=(String)i.previous();
                            j++;
                        }
                        
                        protocolo.responderListadoContactos(lista);
                     }
                        break;
                        
                        //
                        // Solicitud de cieere de la sesio'n:
                        // CERRAR
                        //
                    case Protocolo.solicitudCerrar:
                        protocolo.responderSolicitudCerrar("Que tengas un buen día");

                        // indicamos que no esta' en l?nea:
                        u.asignarLocalizacion("desconectado",-1);
                        usuarios.put(u.usuario,u);
                        
                        try { 
                            in.close();
                            out.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
               
                        break;
                        
                        // Solicitud para eliminar un contacto de la lista propia:
                        // Mensaje:
                        // ELIMINAR
                        //
                    case Protocolo.solicitudEliminar:
                        {
                            // Comprueba si existe el contacto:
                        String []lista;
                        String contacto;
                        int j=0;
                        boolean encontrado=false;
                        
                        lista=new String[u.contactos.size()];
                        
                        
                        // Buscamos el contacto para eliminarlo:
                        for (ListIterator i = u.contactos.listIterator(u.contactos.size());!encontrado&& i.hasPrevious(); ) {
                            String usuarioTMP=(String)i.previous();
                            if(protocolo.solicitudLogin.compareTo(usuarioTMP)==0){
                                encontrado=true;
                                u.contactos.remove(usuarioTMP);
                            }
                            j++;
                        }
                        u.listaContactos();
                        
                        if(encontrado){
                            // Enviar mensaje de operaci?n correcta
                            protocolo.responderEliminacionCorrecta(protocolo.solicitudLogin);
                            
                        } else {
                            // Enviar mensaje de operaci?n incorrecta
                           protocolo.responderEliminacionIncorrecta(protocolo.solicitudLogin);
                        }
                    }
                        
                    // Solicitud de direccio'n del usuario especificado:
                    // mensaje:
                    // LOCALIZAR <nombre de usuario>
                    //
                    case Protocolo.solicitudLocalizar:
                    {
                        // Comprobamos que exista el usuario:
                        u0=(Usuario)usuarios.get(protocolo.solicitudLogin);
                        
                        if(u0==null){
                            // Si no existe, se devuelve respuesta incorrecta:
            
                            protocolo.responderLocalizarContactoDesconocido(protocolo.solicitudLogin);
                        } else {
                            // Existe, as? que obtenemos su direcci?n:
                            protocolo.responderLocalizacionUsuario(protocolo.solicitudLogin,u0.obtenerLocalizacion());
                        }
                    }   
                        
                        break;
                    
                    default:
                        break;
                }
               
                
            } while (peticion!=Protocolo.solicitudCerrar);
        }
        
    }
    
    /*
     * Constructor de esta clase servidora, 
     * @param puerto 
     
    private ServidorTCP(int puerto) {
        final ArrayList<String> listadoAlias = new ArrayList<>(); // Intento de variable global
        ServerSocket socketEscucha;
        
        try { /* Duda: este try para que sirve? puedo anidar este try y luego 
            llamar a un metodo que implementa un try? 
            
            socketEscucha=new ServerSocket(puerto);
            
            // Mientras que no haya que apagar el servidor:
            while(!salir){
                
                // Esperamos una conexión:
                Socket socketConexion = socketEscucha.accept();
                
                Protocolo sesion = new Protocolo();
                sesion.main(listadoAlias, socketConexion);
                
                socketConexion.close();
            }
            
            socketEscucha.close();
            
            
        } catch (IOException ex) {
            Logger.getLogger(ServidorTCP.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }*/
}
