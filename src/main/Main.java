/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import clases.Nomina;
import excel.Excel;
import excel.Cuentas;
import excel.Emails;
import excel.Nifs;
import excel.Nominas;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import xml.Xml;

/**
 * @author Micaela Pujol Higueras
 * @author Silvia Matilla García
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {    
        
        Nifs nif = new Nifs();
        nif.corrigeNifs();
        Cuentas cuenta = new Cuentas();
        ArrayList<ArrayList<String>> cuentasErrores = cuenta.corrigeDigitosDeControl();
        cuenta.iban();
        Emails email = new Emails();
        email.email();
        
        Scanner sc = new Scanner(System.in);       
    	System.out.println("Introduzca el mes y el año del que desea generar las nóminas:");
    	String fecha = sc.nextLine();    	
    	String f[] = fecha.split("/");
    	int mes = Integer.parseInt(f[0]);
    	int anyo = Integer.parseInt(f[1]);
    	
        Nominas noms = new Nominas();
        List<Nomina> nominas = noms.generaNominas(mes, anyo);
        /*for(Nomina n: nominas){
            System.out.println(n.toString());
        }*/
                   
        Xml xml = new Xml();
        xml.creaFicheroErrores();
        xml.creaFicheroErroresCCC(cuentasErrores);    
        
        Excel.close();
    }
    
}