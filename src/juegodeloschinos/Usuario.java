package juegodeloschinos;
/*
 * Usuario.java
 *
 * Created on March 14, 2006, 7:54 PM
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
 * InformaciÃ³n bÃ¡sica de cada usuario.
 * 
 */
public class Usuario {

    public String usuario;
    
    UsuarioContenedor usuarios;

    
    /** Creates a new instance of Usuario */
    public Usuario(String login_) {
        usuario=login_;
    }
        
    
    public Usuario(UsuarioContenedor uc_) {
        usuarios=uc_;
    }
    
}
