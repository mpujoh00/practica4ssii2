/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import clases.Nomina;
import hibernate.HibernateUtil;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import clases.Trabajadorbbdd;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author maybeitsmica
 */
public class TrabajadorDAO {
    
    SessionFactory sf = null;
    Session sesion = null;
    Transaction tx = null;
    
    public TrabajadorDAO(){ // conexión con la base de datos
        
        sf = HibernateUtil.getSessionFactory();
        sesion = sf.openSession();
    }
    
    public Trabajadorbbdd obtenerTrabajador(String nif){
        
        Trabajadorbbdd seleccionado = null;
        
        try{
            
            String consulta = "FROM Trabajadorbbdd t WHERE t.nifnie = :param";
            
            Query query = sesion.createQuery(consulta);
            query.setParameter("param", nif);
            
            List listaRes = query.list();
            
            if(!listaRes.isEmpty()){
                
                seleccionado = (Trabajadorbbdd) listaRes.get(0);
            }
                        
        }
        catch(Exception e){ e.printStackTrace();}
        
        return seleccionado;
    }
    
    public void borraTrabajador(String nifTrabajador){
        
        NominaDAO nDAO = new NominaDAO();
        
        // obtengo al trabajador
        
        Trabajadorbbdd trabajador = obtenerTrabajador(nifTrabajador);
        
        // borro la nómina del trabajador
                
        Set noms = trabajador.getNominas();
        List nominas = new ArrayList();
        nominas.addAll(noms);
        
        for(int i=0; i<nominas.size(); i++){
            
            Nomina nomina = (Nomina)nominas.get(i);
            
            nDAO.borraNomina(nomina.getIdNomina());
        }
               
        // borro al trabajador
        
        tx = sesion.beginTransaction();
        
        String consulta = "DELETE Trabajadorbbdd t WHERE t.nifnie = :param";
        
        Query query = sesion.createQuery(consulta);
        query.setParameter("param", nifTrabajador);
        
        query.executeUpdate();
        
        tx.commit();
    }
    
}
