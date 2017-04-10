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
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import mensajesjuegochinos.MensajeProtocoloJuegoChinos;

/**
 * Clase Protocolo implementa los estados posibles ne los que puede entrar
 * el protocolo del servidor del juego de los chinos, así como los métodos 
 * necesarios para enviar los mensajes al cliente. También recibe e interpreta los
 * mensajes enviados del cliente al servidor.
 * @author Francisco J. Quero
 */
public class Protocolo extends MensajeProtocoloJuegoChinos {
    
        String nombre; // nombre (alias) del jugador
        PrintWriter out; // Buffers de entrada y salida
        BufferedReader in;
        int estado; // estado actual
        
        // Claves utilizadas para el cifrado
        PublicKey publicKeyClient;
        PublicKey publicKeyServer;
        PrivateKey privateKeyServer;
        
        static final String clavePublicaCliente = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDHMjWfzQ7EB1oF7qlh7l0rm4Tf26RK9tk1/HO7Z90SRXKr2nNLmEtXfJvPM+fQbcoqrqxTdVlYJq6o71E3tQZnJ6yfX/nSX2tav5Y8BSV1eCNcoNU2iFArLTK+R09PlFRuRV7fyFacd3oVL7iSOGdpOqp+AZMwz0UUJ6dhrLMpZQIDAQAB";
        
        // Estados del protocolo (ver el disenio de la maquina de estados)
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
        
        // Objeto de la clase MensajeProtocoloJuegoChinos para generar mensajes:
        MensajeProtocoloJuegoChinos fabricaDeMensajes;
        
        /**
         * Este metodo sirve para simular el intercambio de claves. Damos por hecho
         * que el servidor ya tiene su par de claves generadas y ya ha recibido de
         * alguna forma la clave publica del cliente.
         */
    private void prepararClaves(){
        
            try {
                String clavePublicaCliente = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDHMjWfzQ7EB1oF7qlh7l0rm4Tf26RK9tk1/HO7Z90SRXKr2nNLmEtXfJvPM+fQbcoqrqxTdVlYJq6o71E3tQZnJ6yfX/nSX2tav5Y8BSV1eCNcoNU2iFArLTK+R09PlFRuRV7fyFacd3oVL7iSOGdpOqp+AZMwz0UUJ6dhrLMpZQIDAQAB";
                
                KeyFactory kf = KeyFactory.getInstance("RSA");
                
                X509EncodedKeySpec X509Key = new X509EncodedKeySpec(Base64.getDecoder().decode(clavePublicaCliente));
                this.publicKeyClient = kf.generatePublic(X509Key);
                
                String clavePublicaServidor = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC25hBeDVAhaHZIt+1Snu1NFOz2f9z3C9i8Q2J3URJ0xx1kdoXu6ffpN4BrLsFmuh/giWAGdfrVHevtZIX7TI+FJQgEJvSkFIWcrbNC3gwT7drdKjobN/MsZZlx98H3NSbBzhCrAQRILCCEpsQYE5sf20HQ8q7V21ECUwyxO0FXpQIDAQAB";
                String clavePrivadaServidor = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALbmEF4NUCFodki37VKe7U0U7PZ/3PcL2LxDYndREnTHHWR2he7p9+k3gGsuwWa6H+CJYAZ1+tUd6+1khftMj4UlCAQm9KQUhZyts0LeDBPt2t0qOhs38yxlmXH3wfc1JsHOEKsBBEgsIISmxBgTmx/bQdDyrtXbUQJTDLE7QVelAgMBAAECgYAIy+fdniZMdQm7VZ1EbABdxLPg5yVpfFhgNZ12v59znl/7hPfMFdSmktTKUuVaW3lNA05rASGfPmEqebs36ua8G5ib/4Qdi+qQIKXBbl80zgHAseIfwy5IEsBgmLMirskOzuwG62Wsusus58C7r2aCt6idC1YfNg8+UMw29gEtwQJBAPhjdLJe+WYreePrMybyhOCp16gD5TKSlkcd+hzNPjbX1iE9HAv/4j3E7tfoLsEdgFl+PyelwPgrDvJCHOJpkpMCQQC8gNt3LKV7A2Qm3fvjOxZs0kOWxlIt0gcXTGofhqQLpi6ZwOQe4KbeFMmCYXrawlL8S6JdnvdPIbJmbvwgTbfnAkEAylIIjfn6X5RuNo4wHjtQrMbrWHnDyUvJiKgMQEWVtYpdarmiDMwi9nlgqxD+dGKZV0wUGTFUW1CHXiEn8exYbQJAdJuNp7oxn9goMzbs49Mgey7S6slB+uBKzKnTmC70+dPSTgCZ3VQBRa48PXBJzFubdEDhbuKwf/4LWvxa8SzCjwJABqyXhY1wyCJ1GBbajffPp8pZDrXsvWJ1/q57VMVpcUYskic+7/hTx11DF6hcLZBc8ACAKUzpCK64U3zfLs4Jxw==";
                
                X509Key = new X509EncodedKeySpec(Base64.getDecoder().decode(clavePublicaServidor));
                this.publicKeyServer = kf.generatePublic(X509Key);
                
                PKCS8EncodedKeySpec PKCS8Key = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(clavePrivadaServidor));
                this.privateKeyServer = kf.generatePrivate(PKCS8Key);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Protocolo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeySpecException ex) {
                Logger.getLogger(Protocolo.class.getName()).log(Level.SEVERE, null, ex);
            }
            
    }         
    /** 
     * Crea un objeto de la clase Protocolo
     * @param n nombre de la hebra
     * @param in_ buffer de entrada
     * @param out_ buffer de salida
     */
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
            
            // Descomentar para que se muestren por pantalla los mensajes recibidos:
            // System.out.println(mensaje);
            
            // Segun el estado actual, interpretamos el mensaje recibido. Cabria
            // distinguir los casos en los cuales el mensaje recibido no corresponde
            // al estado actual.
            switch(estado){
                case estadoInicial:
                    error = solicitudLoguear;
                    // Si se trata del login:
                    if(palabras[0].compareTo("1110")==0){
                        System.out.println("Logueando como "+palabras[1]+"...");
                    // Ojo, no se comprueba el numero de campos, asi que es
                    // "Explotable"
                    
                    solicitudLogin = palabras[1]; // recogemos el alias
                    
                    estado = estadoAutenticado;
                    
                    } else {
                        System.err.println("Peticion incorrecta: "+mensaje);
                    }
                    
                    break;
                // Caso en el que ya has sido autenticado correctamente
                case estadoAutenticado:
                    
                        if(palabras[0].compareTo("0101")==0){ // Solicitud vs humano
                            error = solicitudVsHumano;
                            estado = estadoVsHumano;
                        } else if (palabras[0].compareTo("0100")==0){ // Solicitud vs maquina
                            error = solicitudVsMaquina;
                        } else if (palabras[0].compareTo("0010")==0){ // Solicitud de desconexion
                            error = solicitudDesconectar;
                            estado = estadoInicial;
                        } else if (palabras[0].compareTo("0111")==0){ // Esta enviando numero de rondas
                            solicitudNumRondas = parseInt(palabras[1]);
                            error = solicitudRondas;
                            if (solicitudNumRondas == 0) { // Depende del numero de rondas, elegimos siguiente estado
                                estado = estadoFinal;
                            } else {
                                estado = estadoEsperandoNumeroChinos;
                            }
                            
                        } else {
                            System.err.println("Peticion incorrecta: "+mensaje);
                        }
                    break;
                // Caso en el cual el jugador ha solicitado jugar contra otro humano
                case estadoVsHumano:
                    if ("0101".equals(palabras[0])) {
                        error = solicitudVsJugador;
                        estado = estadoEsperandoJ2;
                        solicitudRival = palabras[1];
                    }  else {
                        System.err.println("Peticion incorrecta: "+mensaje);
                    }
                    
                    break;
                    
                // Caso en el cual el jugador ha solicitado jugar contra la maquina
                case estadoVsMaquina:
                    error = solicitudVsMaquina;
                    if ("0100".equals(palabras[0])) {
                        estado = estadoAutenticado;
                    }  else {
                        System.err.println("Peticion incorrecta: "+mensaje);
                    }
                    
                    break;
                    
                // Caso en el que se esta esperando al jugador 2. No implementado.
                case estadoEsperandoJ2:
                    error = solicitudEsperarJ2;
                    estado = estadoEsperandoNumeroRondas;
                    solicitudNumRondas = Integer.parseInt(palabras[1]);
                    break;
                    
                // Caso en el que la maquina espera el numero de rondas
                case estadoEsperandoNumeroRondas:
                    error = solicitudRondas;
                    
                    if (palabras[0] == "0111") {
                        
                    }  else {
                        System.err.println("Peticion incorrecta: "+mensaje);
                    }

                    break;
                    
                // Caso en el que la maquina espera el numero de chinos elegidos.
                case estadoEsperandoNumeroChinos:
                    if (palabras[0].compareTo("1000")==0){ //Esta enviando numero de chinos totales
                            solicitudChinos = Integer.parseInt(palabras[1]);
                            error = solicitudNumChinosTotales;
                            
                            if (solicitudNumRondas == 0) {
                                estado = estadoFinal;
                            } else {
                                estado = estadoAutenticado;
                            }
                        } else if (palabras[0].compareTo("1010")==0){ //Esta enviando numero de chinos en mano
                            solicitudChinosElegidos = Integer.parseInt(palabras[1]);
                            error = solicitudNumChinos;
                            
                            estado = estadoEsperandoNumeroChinos;
                        } else {
                        System.err.println("Peticion incorrecta: "+mensaje);
                        }
                    break;
                    
                // Caso en el que se finaliza la conexion
                case estadoFinal:
                    
                    if (palabras[0].compareTo("1100")==0) {
                        error = solicitudFinalizar;
                        estado = estadoInicial;
                    } else {
                        System.err.println("Peticion incorrecta: "+mensaje);
                    }
                    
                    
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
     * @return devuelve un posible error
     */
    public int confirmarAliasIncorrecto(){
        int error=0;
        out.println(fabricaDeMensajes.mAliasIncorrecto("Error de identificacion. Prueba con otro alias."));
        return error;
    }
    /**
     * Envía el mensaje de confirmación de segundo jugador encontrado
     * @param texto nombre del jugador encontrado
     * @return devuelve un posible error
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
        String infoString = numChinosMano + " " + numChinosTotal;
       
       this.prepararClaves();
       
       ArrayList<String> chinosCifrados = prepararCifrado(infoString, this.publicKeyClient);
       out.println(fabricaDeMensajes.mHablaJugador(empiezaJugador, chinosCifrados.get(0), chinosCifrados.get(1)));   
    }
    /**
     * Prepara el mensaje mHablaJugador con el cifrado y la codificacion necesarios
     * para cumplir con los requisitos que la práctica 2 de DAR exige. Crea el hash del mensaje,
     * cifra el mismo mensaje con clave simetrica generada aleatoriamente, cifra la clave simetrica
     * con la clave publica del destinatario y lo codifica todo en Base64.
     * @param informacion mensaje transmitido
     * @param clavePublica clave RSA publica del destinatario
     * @return ArrayList con dos string, el primero contiene la informacion codificada y cifrada
     * y el segundo la clave simetrica cifrada
     */
    private static ArrayList<String> prepararCifrado(String informacion, PublicKey clavePublica){
        ArrayList<String> lista = new ArrayList<>(); // ArrayList con los dos mensajes
        
        // Se calcula el hash del string formado por los dos numeros de chinos
        String hash = coding.Resumen.getHashMD5(informacion);
        
        // Se crea una clave random para cifrado simetrico
        Key clave = coding.Criptografia.generadorClave();
        byte[] claveString = Base64.getEncoder().encode(clave.getEncoded());
        
        // Se cifra la info con DES
        byte[] infoCifradoSimetrico = coding.Criptografia.cifradoSimetrico(informacion+" "+hash, clave);
        // Se cifra la clave DES con la clave publica del cliente
        byte[] claveSimetricaCifrada = coding.Criptografia.cifradoAsimetrico(claveString, clavePublica);
        
        // Se preparan los mensajes cifrados y se anaden al arraylist
        String claveCifrada = Base64.getEncoder().encodeToString(claveSimetricaCifrada);
        String infoCifrada = Base64.getEncoder().encodeToString(infoCifradoSimetrico);
        
        lista.add(infoCifrada);
        lista.add(claveCifrada);
        
        return lista;
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
