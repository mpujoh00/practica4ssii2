/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excel;

import static excel.Excel.filaVacia;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author maybeitsmica
 */
public class Cuentas {
    
    XSSFWorkbook excel;
    
    public Cuentas(){        
        excel = Excel.getExcel();
    }
    
    // DÍGITOS DE CONTROL
    
    public ArrayList<ArrayList<String>> corrigeDigitosDeControl(){
        
        XSSFSheet hoja = excel.getSheetAt(0);
        Row fila;        
        ArrayList<ArrayList<String>> cccErroneos = new ArrayList<ArrayList<String>>();
        
        for(int i = 1; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);            
            Cell celda = fila.getCell(9); //selecciona la casilla correspondiente al CCC

            if(celda != null && celda.getCellType() != CellType.BLANK && StringUtils.isNotBlank(celda.toString()) && !filaVacia(fila)){
                
                String res = validaDigitosDeControl(celda.getStringCellValue());
                
                if(res != "inc" && res != null){ //modifica los dígitos de control erróneos
                                    	
                    cccErroneos.add(new ArrayList<>());
                    cccErroneos.get(cccErroneos.size()-1).add(Integer.toString(i));
                    cccErroneos.get(cccErroneos.size()-1).add(celda.getStringCellValue());                	
                    celda.setCellValue(res);
                }
            }
        }        
        return cccErroneos;
    }
    
    private static String validaDigitosDeControl(String ccc){
        
        String res = null;
        String primerDigito = null;
        String segundoDigito = null;
        
        if(ccc.length() != 20){
            res = "inc";
        }
        else{            
            String primerasPosiciones = ccc.substring(0,8);
            String cadena1 = "00".concat(primerasPosiciones);
            String cadena2 = ccc.substring(10,20);
            int[] factores = {1, 2, 4, 8, 5, 10, 9, 7, 3, 6};            
            int productos1 = Integer.parseInt(cadena1.substring(0,1)) * factores[0];
            int productos2 = Integer.parseInt(cadena2.substring(0,1)) * factores[0];

            for(int i = 1; i < 10; i++){
                    
                productos1 =  productos1 + Integer.parseInt(cadena1.substring(i,i+1)) * factores[i];
                productos2 =  productos2 + Integer.parseInt(cadena2.substring(i,i+1)) * factores[i];
            }
            
            int resto1 = productos1%11;
            int resto2 = productos2%11;            
            primerDigito = Integer.toString(11-resto1);
            segundoDigito = Integer.toString(11-resto2);
            
            if(primerDigito.equals("10")){
                primerDigito = "1";
            }
            else if(primerDigito.equals("11")){
                primerDigito = "0";
            }
            
            if(segundoDigito.equals("10")){
                segundoDigito = "1";
            }
            else if(segundoDigito.equals("11")){
                segundoDigito = "0";
            }
            
            //comparo los dígitos de control obtenidos con los originales
            if(!ccc.substring(8,9).equals(primerDigito) || !ccc.substring(9,10).equals(segundoDigito)){
                res = primerasPosiciones.concat(primerDigito).concat(segundoDigito).concat(cadena2);
            }
        }
        return res;
    }
    
    //IBAN
    
    public void iban(){
        
        XSSFSheet hoja = excel.getSheetAt(0);        
        Row fila;
        
        for(int i = 1; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);            
            Cell celdaCCC = fila.getCell(9); //selecciona la casilla correspondiente al CCC
            Cell celdaPais = fila.getCell(10); //selecciona la casilla correspondiente al pais
            Cell celdaIban = fila.getCell(11); //selecciona la casilla correspondiente a la columna L en la que se añadirá el IBAN
            
            if(celdaCCC != null && celdaCCC.getCellType() != CellType.BLANK && StringUtils.isNotBlank(celdaCCC.toString()) && !filaVacia(fila)){
                if(celdaPais != null && celdaPais.getCellType() != CellType.BLANK && StringUtils.isNoneBlank(celdaPais.toString()) && !filaVacia(fila)){
                    
                    String iban = calculaIban(celdaCCC.getStringCellValue(), celdaPais.getStringCellValue());

                    if(celdaIban == null) {
                    	celdaIban = fila.createCell(11);
                    }                    
                    celdaIban.setCellValue(iban);                    
                }
            }
        } 
    }
    
    public String calculaIban(String ccc, String pais){

        String codigo = pais.concat("00").concat(ccc);        
        codigo = ccc.concat(pais).concat("00");        
        String[] letras = pais.split("");        
        String codigoletras = transformarNumero(letras[0]).concat(transformarNumero(letras[1]));
        codigo = ccc.concat(codigoletras).concat("00");
                
        java.math.BigInteger codigoCompleto = new java.math.BigInteger(codigo);
        java.math.BigInteger num = new java.math.BigInteger("97");       
        java.math.BigInteger resto = codigoCompleto.mod(num);                
        java.math.BigInteger num1 = new java.math.BigInteger("98");
        java.math.BigInteger diferencia = num1.subtract(resto);        
        String digitos = diferencia.toString();
        
        if(digitos.length() != 2){
            digitos = "0".concat(digitos);
        }        
        String iban = pais.concat(digitos).concat(ccc);
        
        return iban;
    }
    
    public String transformarNumero(String letra){
        
        String num = null;
        
        switch(letra){
                case "A":
                    num = "10";
                    break;
                case "B":
                    num = "11";
                    break;
                case "C":
                    num = "12";
                    break;
                case "D":
                    num = "13";
                    break;
                case "E":
                    num = "14";
                    break;
                case "F":
                    num = "15";
                    break;
                case "G":
                    num = "16";
                    break;
                case "H":
                    num = "17";
                    break;
                case "I":
                    num = "18";
                    break;
                case "J":
                    num = "19";
                    break;
                case "K":
                    num = "20";
                    break;
                case "L":
                    num = "21";
                    break;
                case "M":
                    num = "22";
                    break;
                case "N":
                    num = "23";
                    break;
                case "O":
                    num = "24";
                    break;
                case "P":
                    num = "25";
                    break;
                case "Q":
                    num = "26";
                    break;
                case "R":
                    num = "27";
                    break;
                case "S":
                    num = "28";
                    break;
                case "T":
                    num = "29";
                    break;
                case "U":
                    num = "30";
                    break;
                case "V":
                    num = "31";
                    break;
                case "W":
                    num = "32";
                    break;
                case "X":
                    num = "33";
                    break;
                case "Y":
                    num = "34";
                    break;
                case "Z":
                    num = "35";
                    break;
        }    
        return num;
    }
    
}
