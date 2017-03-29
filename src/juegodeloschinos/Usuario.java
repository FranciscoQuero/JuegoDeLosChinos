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
    
    String apodo;
    public String usuario;
    String contrasenia;
    
    String localizacion;
    
    UsuarioContenedor usuarios;
    
    List contactos;
    
    /** Creates a new instance of Usuario */
    public Usuario(String login_,String nombre_) {
        apodo=nombre_;
        usuario=login_;
        contactos=new ArrayList();
    }
        
    boolean anadirContacto(String c){
        contactos.add(c);
        return false;
    }
    
    public Usuario(UsuarioContenedor uc_) {
        usuarios=uc_;
    }
    
    String asignarContrasenia(String c){
        
        contrasenia=c;
        
        return contrasenia;
    }
    
    void listaContactos(){
        System.out.println("Listado:");
        for (ListIterator i = contactos.listIterator(contactos.size()); i.hasPrevious(); ) {
            System.out.println("\t"+(String)i.previous());
        }
    }
    
    String asignarLocalizacion(String anfitrion,int puerto){
        localizacion=usuario+"@"+anfitrion;
        return localizacion;
    }
    
    String obtenerLocalizacion(){
        return localizacion;
    }
    
}
