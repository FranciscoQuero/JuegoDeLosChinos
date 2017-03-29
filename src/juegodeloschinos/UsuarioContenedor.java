package juegodeloschinos;
/*
 * UsuarioContenedor.java
 *
 * Created on March 14, 2006, 8:03 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import java.util.*;

/**
 *
 * @author jjramos
 * 
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
    
    boolean anadir(Usuario u){
        boolean error=false;
        
        if(usuarios.get(u.usuario)==null)
            usuarios.put(u.usuario,u);
        else    
            error=true;

        return error;
    }
    
    Usuario obtener(String login){
        Usuario u=null;
        
        u=(Usuario)usuarios.get(login);
        
        return u;
    }
}
