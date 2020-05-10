/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import excel.Excel;
import excel.cuentas;
import excel.emails;
import excel.nifs;
import excel.nominas;
import java.util.ArrayList;
import java.util.Scanner;

import xml.xml;

/**
 * @author Micaela Pujol Higueras
 * @author Silvia Matilla García
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {    
    	
    	Scanner sc = new Scanner(System.in);
       
    	System.out.println("Introduzca el mes y el año del que se desea generar las nóminas:");
    	String fecha = sc.nextLine();
    	
    	String f[] = fecha.split("/");
    	int mes = Integer.parseInt(f[0]);
    	int anyo = Integer.parseInt(f[1]);
    	
        nominas noms = new nominas();
        noms.generaNominas(mes, anyo);
        
        /*nifs nif = new nifs();
        nif.corrigeNifs();
        cuentas cuenta = new cuentas();
        ArrayList<ArrayList<String>> cuentasErrores = cuenta.corrigeDigitosDeControl();
        cuenta.iban();
        emails email = new emails();
        email.email();
        Excel.close();
    
        xml xml = new xml();
        xml.creaFicheroErrores();
        xml.creaFicheroErroresCCC(cuentasErrores);*/
    	
    }
    
}