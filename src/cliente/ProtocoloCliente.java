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
package cliente;

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
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.spec.SecretKeySpec;
import juegodeloschinos.Protocolo;
import mensajesjuegochinos.MensajeProtocoloJuegoChinos;

/**
 * Clase ProtocoloCliente implementa los estados posibles ne los que puede entrar
 * el protocolo del cliente del juego de los chinos, así como los métodos 
 * necesarios para enviar los mensajes al servidor. También recibe e interpreta los
 * mensajes enviados del servidor al cliente.
 * @author Francisco J. Quero
 */
public class ProtocoloCliente extends MensajeProtocoloJuegoChinos {
        // Buffers de entrada y salida
        PrintWriter out;
        BufferedReader in;
        int estado; // estado actual
        
        // Claves utilizadas para el cifrado
        PublicKey publicKeyClient;
        PublicKey publicKeyServer;
        PrivateKey privateKeyClient;
        
        // Estados del protocolo (ver el disenio de la ma'quina de estados)
        static final int estadoInicial=0;
        static final int estadoEsperandoAutenticacion=1;
        static final int estadoAutenticado=2;
        static final int estadoEsperandoChinosRival=3;
        static final int estadoEsperandoGanador=4;
        static final int estadoEsperandoNuevaRonda = 5;
        static final int estadoError = 6;
        static final int estadoFinalizar = 7;

        
        // Co'digos de las notificaciones cliente:
        public static final int notificacionAutenticado = 1;
        public static final int   notificacionAliasIncorrecto=2;
        public static final int   notificacionRondasRestantes=3;         
        public static final int   notificacionChinos=4; 
        public static final int   notificacionGanador=5; 
        public static final int   notificacionFinalizar=6;  
        public static final int  notificacionError=7;
        public static final int notificacionTurno = 8;
        
        // Variables temporales que contienen los datos de la notificacion en curso:
        public String alias; // alias enviado
        public String aliasRival; // alias del rival
        public int numRondas; // numero de rondas restantes/pedidas
        public int numChinos; // numero de chinos del jugador
        public int numChinosTotal; // numero de chinos predichos por el jugador
        public boolean hablasElPrimero; // boolean que inidica quien habla primero
        public int ganador; // int que indica quien ha ganado o si hay empate
        public String ganadorAlias; // alias del ganador
        
        // Objeto para fabricar los mensajes
        public MensajeProtocoloJuegoChinos fabricaDeMensajes;
               
        /**
         * Este metodo sirve para simular el intercambio de claves. Damos por hecho
         * que el cliente ya tiene su par de claves generadas y ya ha recibido de
         * alguna forma la clave publica del servidor.
         */
        private void prepararClaves(){
            try {
                String clavePublicaCliente = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDHMjWfzQ7EB1oF7qlh7l0rm4Tf26RK9tk1/HO7Z90SRXKr2nNLmEtXfJvPM+fQbcoqrqxTdVlYJq6o71E3tQZnJ6yfX/nSX2tav5Y8BSV1eCNcoNU2iFArLTK+R09PlFRuRV7fyFacd3oVL7iSOGdpOqp+AZMwz0UUJ6dhrLMpZQIDAQAB";
                String clavePrivadaCliente = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMcyNZ/NDsQHWgXuqWHuXSubhN/bpEr22TX8c7tn3RJFcqvac0uYS1d8m88z59BtyiqurFN1WVgmrqjvUTe1BmcnrJ9f+dJfa1q/ljwFJXV4I1yg1TaIUCstMr5HT0+UVG5FXt/IVpx3ehUvuJI4Z2k6qn4BkzDPRRQnp2GssyllAgMBAAECgYBQB1IYA7B8/V8jpwESQUvZaz/1GC9nlskpsWVl05Kz9obdmIRdyK/sVDndA0ONL67bexXs6eadTd06Lfv5X5dUQNBEKSANwhpGZH9Fv/Y68tZjI6Ng2wls1c3XUtFQuhPDT9vMwu/NiAH8W77uzux68sMm/AgRCyEzBhljFOTIgQJBAOeB6s0HwxjHv3bWdVpNEVLQGbSsqSzWpGBz8qv49nMWQ1xam0N6HaHNJ+c64nQybYX8QV3x66K5NlIQy1omsxECQQDcRS5AGwrNTIFgEUoHduHyoyTNGVJRFoza1jJerjoP1Zh9QiMfuEORDpkbMJlnzxITUiU8F4b8QsT4aT+NzOkVAkEAsQXNxmO2Ej/DLxrD933QzlMkJNyWLBwg60Qd/tRLlysh7P+3k7xP5kZaydxkBtf8maSPU0fGl9IqMEx5QoEvEQJAPckN+x9avVF7bMYMvOFE6bmHZhx3MZWgtvWkNViroqtoVaJKlegq07KDkdPlA/Bagp7lIOD8lR/pfkCPeigDLQJADND4d9waSIEBVJf1ATr2vENy2DcJxQo0tdVd08I5vwKcUKniH/rnqHm7v4MW0Ydb8gLZaR1qYMX77L+JSTl/CQ==";
                
                KeyFactory kf = KeyFactory.getInstance("RSA");
                
                X509EncodedKeySpec X509Key = new X509EncodedKeySpec(Base64.getDecoder().decode(clavePublicaCliente));
                this.publicKeyClient = kf.generatePublic(X509Key);
                
                String clavePublicaServidor = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC25hBeDVAhaHZIt+1Snu1NFOz2f9z3C9i8Q2J3URJ0xx1kdoXu6ffpN4BrLsFmuh/giWAGdfrVHevtZIX7TI+FJQgEJvSkFIWcrbNC3gwT7drdKjobN/MsZZlx98H3NSbBzhCrAQRILCCEpsQYE5sf20HQ8q7V21ECUwyxO0FXpQIDAQAB";
                
                X509Key = new X509EncodedKeySpec(Base64.getDecoder().decode(clavePublicaServidor));
                this.publicKeyServer = kf.generatePublic(X509Key);
                
                PKCS8EncodedKeySpec PKCS8Key = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(clavePrivadaCliente));
                this.privateKeyClient = kf.generatePrivate(PKCS8Key);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Protocolo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeySpecException ex) {
                Logger.getLogger(Protocolo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    /**
     * Crea un objeto de la clase Protocolo
     * @param in_ buffer de entrada
     * @param out_ buffer de salida
     */
    public ProtocoloCliente(BufferedReader in_, PrintWriter out_) {
        in=in_;
        out=out_;        
        estado=estadoInicial;
        
        fabricaDeMensajes=new MensajeProtocoloJuegoChinos();
    }
    
    /**
     * Método que implementa la interpretación de los mensajes recibidos por la 
     * conexión, las transiciones y los estados del protocolo desde el punto de 
     * vista del cliente. 
     * Devuelve un código de mensaje recibido, que la clase "ClienteTCP" utiliza para 
     * realizar la operación que requiera.
     * @return un código único perteneciente al tipo de mensaje recibido
     */
    
    // 
    public int recibirPeticion(){
        int error=0;
        int aux;
        String mensaje; // mensaje recibido
        String [] palabras;
        
        try {   
            // Segun el estado actual, se interpreta el mensaje;
            mensaje=in.readLine();
            // Se divide el mensaje en palabras:
            palabras=mensaje.split(" ");
            
            // Descomentar para que aparezca en la linea de comandos el mensaje recibido:
            // System.out.println(mensaje); 
            switch(estado){
            // Estado Inicial
                case estadoInicial:
                    
                    // Si se trata de confirmacion de login 0001:
                    if(palabras[0].compareTo("0001")==0){
                        System.out.println("Logueado correctamente...");

                        estado=estadoAutenticado; 
                        error = notificacionAutenticado;
                    // Si se trata de login incorrecto 0000:
                    } else if(palabras[0].compareTo("0000")==0){
                        System.out.println("Login incorrecto, intenta de nuevo.");
                        error = notificacionAliasIncorrecto;
                    } else {
                        System.err.println("Mensaje incorrecto:"+mensaje);
                    }
                    
                    break;
                
            // Estado autenticado
                case estadoAutenticado:
                        // Si se trata de notificacion de turno:
                        if (palabras[0].compareTo("1001")==0){
                            error = notificacionTurno;
                            
                            int[] mensajeDescifrado = descifrarMensaje(mensaje);
                            
                            aux = parseInt(palabras[1]);
                            if(aux == 0) {
                                hablasElPrimero = true;
                                System.out.println("Te toca hablar el primero.");
                            }
                            else {
                                hablasElPrimero = false;
                                numChinos = mensajeDescifrado[0];
                                numChinosTotal = mensajeDescifrado[1];
                                System.out.println("Tu rival ya ha hablado.");
                            }
                            estado = estadoEsperandoGanador;
                            
                        // Si se trata de desconexion:
                        } else if (palabras[0].compareTo("0010")==0){
                            error = notificacionFinalizar;
                            estado = estadoInicial;
                        } else {
                        System.err.println("Mensaje incorrecto:"+mensaje);
                    }
                    break;
            // Estado Esperando Chinos Rival
                case estadoEsperandoChinosRival:
                    error = estadoEsperandoChinosRival;
                    estado = estadoEsperandoGanador;
                    break;
            // Estado Esperando Ganador
                case estadoEsperandoGanador:
                    error = notificacionGanador;
                    // Comprueba que sea una notificacion de ganador
                    if (palabras[0].compareTo("1011")==0){
                        ganador = Integer.parseInt(palabras[1]);
                        ganadorAlias = palabras[2];
                        // Se añaden los chinos elegidos por el rival por si no se habian mandado antes:
                        numChinos = Integer.parseInt(palabras[3]); 
                        numChinosTotal = Integer.parseInt(palabras[4]);
                    }
                    estado = estadoEsperandoNuevaRonda;
                    break;
            // Estado Esperando Nueva Ronda
                case estadoEsperandoNuevaRonda:
                    error = notificacionRondasRestantes;
                    numRondas = Integer.parseInt(palabras[1]);
                    // Puede comenzar nueva ronda o finalizar el juego
                    if(numRondas == 0){
                        estado = estadoFinalizar;
                    } else {
                        estado = estadoAutenticado;
                    }
                    break;
            // Estado Final
                case estadoFinalizar:
                    System.out.println("El juego ha finalizado.");
                    System.exit(0);
                    break;
            // Estado de Error
                case estadoError:
                    error = estadoError;
                    estado = estadoInicial; // Devuelve al estado inicial en cualquier caso
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
     * Metodo que descifra el mensaje recibido en caso de que sea un mensaje de
     * notificacion de turno. Dicho mensaje se encuentra cifrado con clave simetrica y asimetrica,
     * y contiene ademas un hash para verificacion. Todo ello codificado en Base64.
     * Por ello, descifra y decodifica ademas de comprobar el hash.
     * @param mensajeCifrado mensaje generado por el servidor y cifrado
     * @return un vector de dimension 2 que contiene el numero de chinos elegidos por el rival
     * en la posicion [0] y el numero de chinos predichos en la posicion [1]
     */
    private int[] descifrarMensaje(String mensajeCifrado){
        int[] numChinos = new int[2];
        // Primero establecemos las claves
        this.prepararClaves();
        
        // Segundo separamos el mensaje recibido en partes
        String[] palabras = mensajeCifrado.split(" ");
        
        String infoCifrada = palabras[2]; // Primera parte informacion cifrada con DES
        String claveCifrada = palabras[3]; // Segunda parte clave DES cifrada con RSA
        
        // Tercero desciframos la clave simetrica con RSA
        byte[] claveSimetrica = coding.Criptografia.descifradoAsimetrico(Base64.getDecoder().decode(claveCifrada), this.privateKeyClient);
       
        byte[] encodedKey = Base64.getDecoder().decode(claveSimetrica); // lo leemos en base64
        Key keySimetrica = new SecretKeySpec(encodedKey,0,encodedKey.length, "DES");
        
        byte[] infoCifradaByte = Base64.getDecoder().decode(infoCifrada); // obtenemos la info leyendola en base64
          
        // Cuarto desciframos la informacion con la clave simetrica descifrada
        String info = coding.Criptografia.descifradoSimetrico(infoCifradaByte, keySimetrica);
        
        String[] campo = info.split(" ");
            String campoInfo = campo[0]+" "+campo[1]; // Leemos el numero de chinos elegidos y predichos del emisor
            String hash = campo[2]; // Leemos el hash del emisor
            
            // Quinto calcular hash y compararlo con el del emisor
            if (coding.Resumen.getHashMD5(campoInfo).compareTo(hash) == 0){
                String[] numeros = campoInfo.split(" ");
                // Si el hash coincide, obtenemos los datos
                numChinos[0] = Integer.parseInt(numeros[0]);
                numChinos[1] = Integer.parseInt(numeros[1]);
                
            } else {
                System.out.println("Error en la transmision del mensaje. Prueba de nuevo.");
            }

        return numChinos;
    }
       

    /**
     * Envia solicitud de login de un alias enviado
     * @param alias un alias con el que se registrará el usuario
     */
    
    public void enviarLogin(String alias) {
        out.println(fabricaDeMensajes.mAlias(alias));    
        out.flush();
    }
    /**
     * Envía el mensaje de peticion de juego contra la máquina
     */
    public void enviarVsMaquina(){
        out.println(fabricaDeMensajes.mVsMaquina());
        out.flush();
    }
    /**
     * Envía el mensaje de petición de juego contra un segundo jugador
     * @param rival nombre del rival con el que quieres jugar
     */
    public void enviarVsHumano(String rival){
        out.println(fabricaDeMensajes.mVsHumano(rival));
        out.flush();
    }
    /**
     * Envía el número de rondas restantes o introducidas por el usuario
     * @param numero número de rondas restantes/introducidas
     */
    public void enviarNumeroRondas(int numero){
        out.println(fabricaDeMensajes.mNumeroRondas(numero));
        out.flush();
    }
    /**
     * Envía el número de chinos totales predichos por el jugador
     * @param numero chinos predichos
     */
    public void enviarNumeroChinos(int numero){
        out.println(fabricaDeMensajes.mNumeroChinos(numero));
        out.flush();
    }
    /**
     * Envía el número de chinos elegidos en la mano
     * @param numero chinos en mano
     */
    public void enviarNumeroChinosElegidos(int numero){
        out.println(fabricaDeMensajes.mChinos(numero));
        out.flush();
    }
    /**
     * Envía un mensaje simple de petición de cierre de sesión
     */
    public void enviarDespedida(){
        out.println(fabricaDeMensajes.mFinalizar("Adios."));
        out.flush();
    }
}
