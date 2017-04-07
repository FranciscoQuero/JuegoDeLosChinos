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

import org.apache.commons.codec.binary.Base64;

/**
 * Clase Codificaciones que implementa los métodos necesarios para codificar y
 * decodificar en Base64 y hexadecimal, con opción de implementar más codificaciones en un futuro.
 * @author Francisco J. Quero
 */
public class Codificaciones {
    /**
     * Codifica en Base64 la cadena introducida y la devuelve en formato String
     * @param mensajeOriginal cadena que se quiere codificar
     * @return cadena codificada
     */
    public static String codificarEnBase64(String mensajeOriginal) {
        byte[] bitstream=new byte[128];
        
        // ¿para que es este for?
        for(int i=0;i<bitstream.length;i++){
            bitstream[i]=(byte) (255-i);
        }  
        
        bitstream=mensajeOriginal.getBytes();
        
        // Codificamos en base 64:
        String codificadoBase64 = Base64.encodeBase64String(bitstream);
        return codificadoBase64;
    }

    public static String decodificarEnBase64(String mensajeCodificado){
        Base64 coder=new Base64();
        String decodificado = "";
        byte[] decodificadoByte = coder.decode(mensajeCodificado);
        
        for(int i = 0; i < decodificadoByte.length; i++)
    {
        decodificado += (char)decodificadoByte[i];
    }
        
        return decodificado;
    }
    /**
     * Convierte en formato hexadecimal una cadena de bytes
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
