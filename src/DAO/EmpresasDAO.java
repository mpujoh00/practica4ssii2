/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import clases.Empresas;
import hibernate.HibernateUtil;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import java.util.Iterator;

/**
 * @author Micaela Pujol Higueras
 * @author Silvia Matilla García
 */
public class EmpresasDAO {
    
    SessionFactory sf = null;
    Session sesion = null;
    Transaction tx = null;

    static List<Empresas> empresas;
    
    public EmpresasDAO(){ // conexión con la base de datos
        
        sf = HibernateUtil.getSessionFactory();
        sesion = sf.openSession();
    }
    
    public void insertEmpresa(Empresas empresa){
        
        int indice = 0;
        boolean existe = false;
        
        Iterator<Empresas> iterador = empresas.iterator();
        
        tx = sesion.beginTransaction();
        
        while(iterador.hasNext()){
            
            Empresas emp = iterador.next();
            indice = emp.getIdEmpresa();
            
            if(emp.getCif().equals(empresa.getCif())){
                
                existe=true;
                empresa.setIdEmpresa(emp.getIdEmpresa());
                sesion.update(empresa);
                tx.commit();
                sesion.close();
                return;  
            } 
        }
        
       indice++;
       
        if(!existe){
            
            empresa.setIdEmpresa(indice);
            sesion.save(empresa);
            empresas.add(empresa);
            tx.commit();
            sesion.close();
        }
     }
    
    /*public int comprobarIdEmpresa(String cif){
        
        int id = 0;
        Iterator<Empresas> iterador = empresas.iterator();
         
        tx = sesion.beginTransaction();
         
        while(iterador.hasNext()){
            
            Empresas empresa = iterador.next();
          
            if(empresa.getCif().equals(cif)){
                
                id = empresa.getIdEmpresa();
                tx.commit();
                sesion.close();
                return id;
            }
        }
        
        tx.commit();
        sesion.close();
        return id; 
    }*/

}