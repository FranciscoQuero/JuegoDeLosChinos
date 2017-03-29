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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Francis
 */
public class ProtocoloCliente {
    public String alias;
    public int numeroRondasRestantes;
    public int numeroRondasGanadas;
    public int numeroChinosActuales;
    public int numeroChinosRival;
    
    public ProtocoloCliente(){
        this.alias = "";
        this.numeroChinosActuales = 0;
        this.numeroChinosRival = 0;
        this.numeroRondasGanadas = 0;
        this.numeroRondasRestantes = 0;
    }
    
    public ProtocoloCliente(String nuevoAlias){
        this.alias = nuevoAlias;
        this.numeroChinosActuales = 0;
        this.numeroChinosRival = 0;
        this.numeroRondasGanadas = 0;
        this.numeroRondasRestantes = 0;
    }
    /*private String login(String aliasNuevo) {
        // Crea mensaje de conexion
    }
    
    private String desconexion() {
        // Crea mensaje de desconexion
    }*/
    
    // Tarea pendiente: separar los cases de modo que una vez logueado, no puedas
    // recibir nuevos logins por ejemplo. Es opcional porque un cliente bien diseñado
    // no debería dar problemas.
    public void main(Socket socketConexion, String []argumentos) {
        String respuesta = "";
        String linea, mensaje;
        
        /*try{
            
            PrintWriter out = new PrintWriter(socketConexion.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socketConexion.getInputStream()));

            
            // Leemos una petición:
            linea = in.readLine();

            // Interpretamos la petición. Para que no haya problema con las letras de la palabra
            // al compararlas con los comandos, las pasamos todas a minúsculas:
            mensaje = linea.toLowerCase();
            mensaje = mensaje.substring(5, mensaje.length()-1);

            int codigo = Integer.parseInt(mensaje.substring(0, 4));

            /*switch(codigo){
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
                    
                    if (this.alias == "") {
                        respuesta = "No logueado. Intenta nuevo alias";
                    }
                    else {
                        respuesta = "Todo ok";
                    }
                    out.print(respuesta);
                break;
                default:
                    respuesta = "Mensaje erroneo";
                break;*/
           /* }
        }catch (IOException ex) {
            Logger.getLogger(JuegoDeLosChinos.ServidorTCP.class.getName()).log(Level.SEVERE, null, ex);     
        }  */
    }

}
