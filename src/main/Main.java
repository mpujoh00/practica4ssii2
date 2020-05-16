/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import excel.Excel;
import excel.Cuentas;
import excel.Emails;
import excel.Nifs;
import excel.Nominas;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
    public static void main(String[] args) {    
        
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
        noms.generaNominas(mes, anyo);
        
        Excel.close();
    
        /*Xml xml = new Xml();
        xml.creaFicheroErrores();
        xml.creaFicheroErroresCCC(cuentasErrores);*/
    	
    }
    
}