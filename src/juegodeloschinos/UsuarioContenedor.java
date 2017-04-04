
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
import java.util.*;

/**
 *
 * @author Francisco J. Quero
 * basado en el ejemplo de jjramos
 * Simula una base de datos de usuarios.
 */
public class UsuarioContenedor {
    
    Map usuarios;
    
    /**
     * Creates a new instance of UsuarioContenedor 
     */
    public UsuarioContenedor() {
       usuarios = new HashMap();  
    }
    /**
     * Instancia con un usuario ya creado
     * @param u usuario anteriormente creado que será añadido
     * @return true si ha habido error
     */
    boolean anadir(Usuario u){
        boolean error=false;
        
        if(usuarios.get(u.usuario)==null)
            usuarios.put(u.usuario,u);
        else    
            error=true;

        return error;
    }
    /**
     * Devuelve el usuario cuyo nombre coincida con el login
     * @param login nombre de usuario que se busca
     * @return 
     */
    Usuario obtener(String login){
        Usuario u=null;
        
        u=(Usuario)usuarios.get(login);
        
        return u;
    }
}
