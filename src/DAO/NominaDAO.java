/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import clases.Trabajadorbbdd;
import hibernate.HibernateUtil;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 *
 * @author maybeitsmica
 */
public class NominaDAO {
    
    SessionFactory sf = null;
    Session sesion = null;
    Transaction tx = null;
    
    public NominaDAO(){
        
        sf = HibernateUtil.getSessionFactory();
        sesion = sf.openSession();        
    }
    
    public void borraNomina(int id){
        
        tx = sesion.beginTransaction();
        
        String consulta = "DELETE Nomina n WHERE n.idNomina = :param";
        
        Query query = sesion.createQuery(consulta);
        query.setParameter("param", id);
        
        query.executeUpdate();
        
        tx.commit();
        
    }
    
}
