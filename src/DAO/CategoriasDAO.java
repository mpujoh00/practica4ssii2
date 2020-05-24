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
import java.util.Iterator;
import org.hibernate.Transaction;


/**
 * @author Micaela Pujol Higueras
 * @author Silvia Matilla García
 */
public class CategoriasDAO {
    
    SessionFactory sf = null;
    Session sesion = null;
    Transaction tx = null;
    
    static List<Categorias> categorias;
    
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
    
    public void insertarCategoria(Categorias categoria){
        
        int indice = 0;
        boolean existe = false;
        
        Iterator<Categorias> iterador = categorias.iterator();
        
        tx = sesion.beginTransaction();
        
        while(iterador.hasNext()){
            
            Categorias cat = iterador.next();
            indice = cat.getIdCategoria();
            
            if(cat.getNombreCategoria().equals(categoria.getNombreCategoria())){
                
                existe = true;
                categoria.setIdCategoria(cat.getIdCategoria());
                sesion.update(categoria);
                tx.commit();
                sesion.close();
                return;
            }
        }
        
        indice++;
        
        if(!existe){
            
            categoria.setIdCategoria(indice);
            sesion.save(categoria);
            categorias.add(categoria);
            tx.commit();
            sesion.close();
        }
    }
    
    /*public int comprobarIdCategoria(String nombre){
        
        int id  = 0;
        Iterator<Categorias> iterador = categorias.iterator();
         
        tx = sesion.beginTransaction();
         
         while(iterador.hasNext()){
              
            Categorias cat = iterador.next();
           
            if(cat.getNombreCategoria().equals(nombre)){
                
                id = cat.getIdCategoria();
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
