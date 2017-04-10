/*
 * Copyright (C) 2017 Francis
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
package coding;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

/**
 * Implementa los métodos necesarios para el uso de cifrado con clave simétrica
 * y asimétrica.
 * 
 * @author Francisco J. Quero
 */
public class Criptografia {
    
    /**
     * Generador de clave. Utiliza DES.
     * @return par de claves.
     */
    static public Key generadorClave(){
        Key clave = null;
        KeyGenerator generadorClaves;        
        
        
         try {
            
            generadorClaves=KeyGenerator.getInstance("DES");
            
            clave=generadorClaves.generateKey();
            
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Error: el algoritmo de generacion de claves seleccionado no esta disponible.");
        }
        
        return clave;
    }
    
    /**
     * Generador de claves privada/publica. Habitualmente estas claves ya están
     * generadas y publicadas en algún anillo de claves. Utiliza DSA.
     * @return par de claves.
     */
    static public KeyPair generadorParDeClavesFirma(){
        KeyPair parClaves = null;
        KeyPairGenerator generadorParesClaves;        
        
        
         try {
            
            // Al generador de claves le indicamos qué algoritmo utilizar, y generamos el par de
            // llaves pública/privada:
            generadorParesClaves=KeyPairGenerator.getInstance("DSA");
            
            parClaves=generadorParesClaves.generateKeyPair();
            
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Error: el algoritmo de generacion de claves seleccionado no esta disponible.");
        }
        
        return parClaves;
    }
    
    /**
     * Generador de claves privada/publica. Habitualmente estas claves ya están
     * generadas y publicadas en algún anillo de claves. Utiliza RSA.
     * @return par de claves.
     */
    static public KeyPair generadorParDeClaves(){
        KeyPair parClaves = null;
        KeyPairGenerator generadorParesClaves;        
        
        
         try {
            
            //Al generador de claves le indicamos qué algoritmo utilizar, y generamos el par de
            // llaves pública/privada:
            generadorParesClaves=KeyPairGenerator.getInstance("RSA");
            
            parClaves=generadorParesClaves.generateKeyPair();
            
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Error: el algoritmo de generacion de claves seleccionado no esta disponible.");
        }
        
        return parClaves;
    }
    
    /**
     * Firma un mensaje con una clave privada proporcionada de modo que se pueda
     * verificar proporcionando la clave publica. Utiliza algoritmo SHA1 con DSA
     * @param mensaje texto que se va a firmar
     * @param clavePrivada clave privada del que firma
     * @return mensaje firmado
     */
    static byte[] firmar(String mensaje, PrivateKey clavePrivada){
            byte []mensajeFirmado=null;
            Signature firma=null;

            try {

                // Preparamos el motor de la firma (resumen SHA1 y cifrado asimÃ©trico
                // con DSA), y le damos la clave privada:
                firma=Signature.getInstance("SHA1withDSA");      
                firma.initSign(clavePrivada);

                // Le pasamos el mensaje a firmar:
                firma.update(mensaje.getBytes());


                // Firmamos:
                mensajeFirmado=firma.sign();

            } catch (NoSuchAlgorithmException ex) {
                System.err.println("Error: el algoritmo de firma seleccionado no esta disponible.");
            } catch (InvalidKeyException ex) {
                System.err.println("Error: la clave utilizada es incorrecta.");
            } catch (SignatureException ex) {
                System.err.println("Error: no se pudo realizar la firma.");
            }

            return mensajeFirmado;
    }
    
    /**
     * Comprueba que un documento recibido ha sido firmado por el propietario de
     * la clave publica proporcionada.
     * @param documentoOriginal mensaje sin firmar.
     * @param documentoFirmado mensaje firmado que se quiere comprobar.
     * @param clavePublica clave publica del supuesto propietario.
     * @return true si la firma es correcta. False si no lo ha firmado el propietario.
     */
    public static boolean verificarFirma(String documentoOriginal, String documentoFirmado, PublicKey clavePublica) {
         boolean firmaGenuina=false;

        try {  
            // Obtenemos un objeto que compruebe firmas con el mismo algoritmo:
            Signature firma=Signature.getInstance("SHA1withDSA");
            
            // inicializamos proporcionando la clave publica:
            firma.initVerify(clavePublica);
            
            // Se le proporciona el documento original
            firma.update(documentoOriginal.getBytes());
            
            // Se realiza la verificacion:
            firmaGenuina=firma.verify(documentoFirmado.getBytes());
            
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("¡Error! ¡No encuentro el algoritmo de firma especificado!");
        } catch (InvalidKeyException ex) {
            System.err.println("¡Error! ¡La clave publica proporcionada no es valida!");
        } catch (SignatureException ex) {
            System.err.println("¡Error! ¡Ha habido un problema realizando la comprobacion de la firma!");
        }
        
        return firmaGenuina;   
    }
    
    /**
     * Metodo que implementa el cifrado asimetrico de un texto con algoritmo DSA
     * @param mensaje texto que se quiere cifrar
     * @param clavePublica clave publica del destinatario
     * @return texto cifrado en formato String
     */
    public static byte[] cifradoAsimetrico(String mensaje, PublicKey clavePublica){
        String mensajeCifrado = "";
        byte[] mensajeCifradoBytes = null;
        Cipher cifrador=null;
        
        try {
            
            cifrador=Cipher.getInstance("RSA");
            
            // inicializamos el cifrador en modo "cifrar"
            cifrador.init(Cipher.ENCRYPT_MODE, clavePublica);
            
            // Ciframos el texto plano:
            mensajeCifradoBytes = cifrador.doFinal(mensaje.getBytes());
            
            mensajeCifrado = byteToString(mensajeCifradoBytes);
           
        }catch (BadPaddingException ex) {
            System.err.println("Error: el algoritmo de relleno es incorrecto.");
        } catch (NoSuchPaddingException ex) {
            System.err.println("Error: no existe el par?metro de cifrado especificado.");
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Error: no existe el algoritmo de cifrado especificado.");
        } catch (InvalidKeyException e){
            System.err.println("Error: el tipo de la llave especificada es incorrecto.");
        } catch (IllegalBlockSizeException e){
            System.err.println("Error: el tamano del bloque de cifrado es incorrecto.");
        }
        
        return mensajeCifradoBytes;
    }
    
    /**
     * Metodo que implementa el cifrado asimetrico de un texto con algoritmo DSA
     * @param mensaje texto que se quiere cifrar
     * @param clavePublica clave publica del destinatario
     * @return texto cifrado en formato cadena de bytes
     */
    public static byte[] cifradoAsimetrico(byte[] mensaje, PublicKey clavePublica){
        String mensajeCifrado = "";
        byte[] mensajeCifradoBytes = null;
        Cipher cifrador=null;
        
        try {
            
            cifrador=Cipher.getInstance("RSA");
            
            // inicializamos el cifrador en modo "cifrar"
            cifrador.init(Cipher.ENCRYPT_MODE, clavePublica);
            
            try {
                // Ciframos el texto plano:
                mensajeCifradoBytes = cifrador.doFinal(mensaje);
            } catch (IllegalBlockSizeException ex) {
                Logger.getLogger(Criptografia.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BadPaddingException ex) {
                Logger.getLogger(Criptografia.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        } catch (NoSuchPaddingException ex) {
            System.err.println("Error: no existe el par?metro de cifrado especificado.");
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Error: no existe el algoritmo de cifrado especificado.");
        } catch (InvalidKeyException e){
            System.err.println("Error: el tipo de la llave especificada es incorrecto.");
        }
        
        return mensajeCifradoBytes;
    }
    
    /**
     * Metodo que implementa el cifrado simetrico de un texto con algoritmo DES
     * @param mensaje texto que se quiere cifrar
     * @param clave la clave para cifrar
     * @return texto cifrado en formato cadena de bytes
     */
    public static byte[] cifradoSimetrico(String mensaje, Key clave){
        byte[] mensajeCifradoBytes = null;
        Cipher cifrador=null;
        
        try {
            
            cifrador=Cipher.getInstance("DES");
            
            // inicializamos el cifrador en modo "cifrar"
            cifrador.init(Cipher.ENCRYPT_MODE, clave);
            
            // Ciframos el texto plano:
            mensajeCifradoBytes = cifrador.doFinal(mensaje.getBytes());
           
        }catch (BadPaddingException ex) {
            System.err.println("Error: el algoritmo de relleno es incorrecto.");
        } catch (NoSuchPaddingException ex) {
            System.err.println("Error: no existe el par?metro de cifrado especificado.");
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Error: no existe el algoritmo de cifrado especificado.");
        } catch (InvalidKeyException e){
            System.err.println("Error: el tipo de la llave especificada es incorrecto.");
        } catch (IllegalBlockSizeException e){
            System.err.println("Error: el tamano del bloque de cifrado es incorrecto.");
        }
        
        return mensajeCifradoBytes;
    }
    
    /**
     * Metodo que implementa el descifrado asimetrico mediante DSA. Utiliza un 
     * mensaje previamente cifrado con clave publica para descifrarlo con la clave
     * privada del destinatario
     * @param textoCifrado texto previamente cifrado con clave publica
     * @param clavePrivada la clave privada que se utilizara para descifrar
     * @return texto descifrado y legible
     */
    public static byte[] descifradoAsimetrico(byte[] textoCifrado, PrivateKey clavePrivada){
        String textoDescifrado = "";
        byte[] textoPlanoDescifrado = null;
        Cipher cifrador;
        
        try{
            cifrador=Cipher.getInstance("RSA");
            // Inicializamos el cifrador en modo "descifrar", con los parÃ¡metros del cifrado:
            cifrador.init(Cipher.DECRYPT_MODE, clavePrivada);
            // desciframos el texto cifrado:
            textoPlanoDescifrado=cifrador.doFinal(textoCifrado);
            
        } catch (InvalidKeyException e){
            System.err.println("Error: el tipo de la llave especificada es incorrecto.");
        }  catch (IllegalBlockSizeException e){
            System.err.println("Error: el tama?o del bloque de cifrado es incorrecto.");
        }  catch (BadPaddingException ex) {
            System.err.println("Error: el algoritmo de relleno es incorrecto.");
        } catch (NoSuchPaddingException ex) {
            System.err.println("Error: no existe el par?metro de cifrado especificado.");
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Error: no existe el algoritmo de cifrado especificado.");
        }
        
        return textoPlanoDescifrado;
    }
    
    /**
     * Metodo que implementa el descifrado simetrico mediante DES. Utiliza un 
     * mensaje previamente cifrado con clave para descifrarlo con la misma clave
     * @param textoCifrado texto previamente cifrado con clave
     * @param clave la clave que se utilizara para descifrar
     * @return texto descifrado y legible
     */
    public static String descifradoSimetrico(byte[] textoCifrado, Key clave){
        String textoDescifrado = "";
        byte[] textoPlanoDescifrado = null;
        Cipher cifrador=null;
        
        try{
            cifrador=Cipher.getInstance("DES");
            // Inicializamos el cifrador en modo "descifrar", con los parÃ¡metros del cifrado:
            cifrador.init(Cipher.DECRYPT_MODE, clave);
            // desciframos el texto cifrado:
            textoPlanoDescifrado=cifrador.doFinal(textoCifrado);
            
            textoDescifrado = byteToString(textoPlanoDescifrado);
            
        } catch (InvalidKeyException e){
            System.err.println("Error: el tipo de la llave especificada es incorrecto.");
        }  catch (IllegalBlockSizeException e){
            System.err.println("Error: el tama?o del bloque de cifrado es incorrecto.");
        }  catch (BadPaddingException ex) {
            System.err.println("Error: el algoritmo de relleno es incorrecto.");
        } catch (NoSuchPaddingException ex) {
            System.err.println("Error: no existe el par?metro de cifrado especificado.");
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Error: no existe el algoritmo de cifrado especificado.");
        }
        
        return textoDescifrado;
    }
    
    /**
     * Metodo que transforma una cadena de bytes en un String legible caracter a
     * caracter.
     * @param texto mensaje que quieres convertir
     * @return String convertido
     */
    public static String byteToString(byte[] texto){
        String mensaje = "";
        
        for(int i = 0; i < texto.length; i++){
            mensaje += (char)texto[i];
        }
        
        return mensaje;
    }
    
}
