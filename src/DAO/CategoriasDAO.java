/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.util.List;
import org.hibernate.Query;
import clases.Categorias;
import org.hibernate.Transaction;


/**
 *
 * @author maybeitsmica
 */
public class CategoriasDAO {
    
    SessionFactory sf = null;
    Session sesion = null;
    Transaction tx = null;
    
    public CategoriasDAO(){
        
        sf = HibernateUtil.getSessionFactory();
        sesion = sf.openSession();        
    }
        
    public List obtenerCategorias(){
        
        String consulta = "FROM Categorias";
        
        Query query = sesion.createQuery(consulta);
                
        return query.list();
    }
    
    public void aumentaSalario(Categorias categoria){
        
        categoria.setSalarioBaseCategoria(categoria.getSalarioBaseCategoria() + 200);
        
        tx = sesion.beginTransaction();
        
        sesion.saveOrUpdate(categoria);
        
        tx.commit();        
    }
    
}
