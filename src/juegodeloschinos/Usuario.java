package juegodeloschinos;
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


/**
 *
 * @author Francisco J. Quero
 * Basado en el ejemplo de jjramos
 * Información básica del usuario
 */
public class Usuario {

    public String usuario;
    
    UsuarioContenedor usuarios;

    
    /**
     * Instancia con un nombre de usuario
     * @param login nombre de usuario
     */
    public Usuario(String login_) {
        usuario=login_;
    }
        
    /**
     * Instancia con un usuario ya creado
     * @param uc_ usuario contentedor
     */
    public Usuario(UsuarioContenedor uc_) {
        usuarios=uc_;
    }
    
}
