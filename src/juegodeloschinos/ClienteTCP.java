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
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jjramos
 */
public class ClienteTCP {
    
    
    /**
     * Método principal de la clase:
     * @param argumentos 
     */
    public static void main(String []argumentos){
        int puerto=8767;
        String direccionServidor="127.0.0.1";
        
        // Por si quisiéramos pasarle argumentos por la línea de comandos:
        if(argumentos.length==2){
            direccionServidor=argumentos[0];
            puerto=Integer.parseInt(argumentos[1]);
        }
        
        // Creamos un cliente:
        new ClienteTCP(direccionServidor,puerto);
        
    }

    /**
     * Constructor del cliente.
     * @param direccionServidor Dirección o nombre del servidor.
     * @param puerto  Puerto donde escucha el servidor.
     */
    private ClienteTCP(String direccionServidor, int puerto) {
        Socket socketConexion;
        
        try {
            // Abrimos la conexión.
            socketConexion=new Socket(direccionServidor, puerto);
            
                
                PrintWriter out = new PrintWriter(socketConexion.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socketConexion.getInputStream()));
                socketConexion.close();
                // Para leer de la línea de comandos:
                BufferedReader inConsola = new BufferedReader(new InputStreamReader(System.in));
                
                //ProtocoloCliente sesion = new ProtocoloCliente();
                
                String eleccion=inConsola.readLine();  
            
                // Envía la pregunta:
                out.println(eleccion);
                out.flush();
                
                // Recibe la respuesta:
                String respuesta=in.readLine();
                System.out.println("La respuesta ha sido: " + respuesta);
                
            /*// Interpretamos los campos del mensaje:
            String[] campos = respuesta.split(" ");
                
            // Si es un error, lo comentamos:
            if(campos[0].compareTo("error")==0){
                System.out.println("Error! Has enviado una selección incorrecta.");
            } else {
                String ganador=(campos[1].charAt(0)=='S')?"Ha ganado el servidor":"Has ganado tú";
                
                System.out.println(ganador);
            }*/
                
            in.close();
            out.close();
            
        } catch (IOException ex) {
            Logger.getLogger(ClienteTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
