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
 */
package coding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * Clase Resumen contiene los métodos necesarios para generar resúmenes en MD5 con
 * posibilidad de añadir futuros algoritmos de resumen
 * @author Francisco J. Quero
 */
public class Resumen {
    // No convierte bien. Planteate pasar en hexadecimal directamente.
    private static String hexToString(byte[] textoHexadecimal) {
        String convertido ="";
        for(int i = 0; i < textoHexadecimal.length; i++){
            convertido += (char)textoHexadecimal[i];
        }
        
        return convertido;
    }
    
    /** Creates a new instance of Resumen*/
    public Resumen() {
    }    
    
    // Main:
    public static String getHashMD5(String mensaje){
        String hash = null;
        MessageDigest resumen;
        byte[] bytesMensaje = null;     
        try {
            // Obtenemos un motor de cálculo de funcionas Hash (resumen), 
            // especificando el algoritmo a utilizar (p.e. "SHA" o "MD5"):
            resumen = MessageDigest.getInstance("MD5");
            
            // introducimos el vector de octetos del mensaje que queremos "resumir":
            bytesMensaje=mensaje.getBytes();
            hash = byteToHex(resumen.digest(bytesMensaje));
            
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Error: el algoritmo de resumen especificado no existe!");
        }   
        
        return hash;
           
   }
    /**
     * Obtiene en formato hexadecimal una cadena de bytes
     * @param bitstream cadena de bytes que quieres obtener
     * @return linea cadena en hexadecimal
     */
    static public String byteToHex(byte[] bitstream) {
        String linea="";
        
        for(int i=0;i<bitstream.length;i++){
            linea=linea+String.format("%02x", bitstream[i]);
        }
        
        linea = linea.toLowerCase();
        
        return linea;
    }
}
