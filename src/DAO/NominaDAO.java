/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import clases.Nomina;
import hibernate.HibernateUtil;
import java.util.Iterator;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * @author Micaela Pujol Higueras
 * @author Silvia Matilla García
 */
public class NominaDAO {
    
    SessionFactory sf = null;
    Session sesion = null;
    Transaction tx = null;
    
    static List<Nomina> nominas;
    
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
    
    public void insertarNomina(Nomina nomina){
        
        int indice = 0;
        boolean existe = false;
        
        Iterator<Nomina> iterador = nominas.iterator();
        
        tx = sesion.beginTransaction();
        
        while(iterador.hasNext()){
            
            Nomina nom = iterador.next();
            indice = nom.getIdNomina();
            
            if(nom.getMes() == nomina.getMes() && nom.getAnio() == nomina.getAnio() && nom.getTrabajadorbbdd().equals(nomina.getTrabajadorbbdd()) && nom.getBrutoNomina() == nomina.getBrutoNomina() && nom.getLiquidoNomina() == nomina.getLiquidoNomina()){
                
                existe = true;
                nomina.setIdNomina(nom.getIdNomina());
                sesion.update(nomina);
                tx.commit();
                sesion.close();
                return;
            }
        }
        
        indice++;
        
        if(!existe){
            nomina.setIdNomina(indice);
            sesion.save(nomina);
            nominas.add(nomina);
            tx.commit();
            sesion.close();
        }
    }
    
}
