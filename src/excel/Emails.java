/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excel;

import excel.Excel;
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
public class emails {
    
    XSSFWorkbook excel;
    
    public emails(){
        
        excel = Excel.getExcel();
    }
    
     public void email(){
        
        XSSFSheet hoja = excel.getSheetAt(0);
        
        Row fila;
        
        ArrayList<String> usuarios = new ArrayList<String>();
        ArrayList<String> empresas = new ArrayList<String>();
        
        for(int i = 1; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);
            
            Cell celdaA1 = fila.getCell(5); //selecciona la casilla correspondiente al apellido 1
            Cell celdaA2 = fila.getCell(6); //selecciona la casilla correspondiente al apellido 2
            Cell celdaN = fila.getCell(4); //selecciona la casilla correspondiente al nombre
            Cell celdaE = fila.getCell(1); //selecciona la casilla correspondiente al nombre de la empresa
            Cell celdaEmail = fila.getCell(8); //selecciona la casilla correspondiente al correo electrónico
                        
            if(celdaA1 != null && celdaA1.getCellType() != CellType.BLANK && StringUtils.isNotBlank(celdaA1.toString()) && !filaVacia(fila)){
                if(celdaN != null && celdaN.getCellType() != CellType.BLANK && StringUtils.isNotBlank(celdaN.toString()) && !filaVacia(fila)){
                    if(celdaE != null && celdaE.getCellType() != CellType.BLANK && StringUtils.isNotBlank(celdaE.toString()) && !filaVacia(fila)){
                        
                    	String email;
                    	
                    	if(celdaA2 != null && celdaA2.getCellType() != CellType.BLANK && StringUtils.isNotBlank(celdaA2.toString()) && !filaVacia(fila)){
                            
                            email = generaEmail(celdaA1.getStringCellValue(), celdaA2.getStringCellValue(), celdaN.getStringCellValue(), celdaE.getStringCellValue(),usuarios,empresas);
                        }
                        else{
                            email = generaEmail(celdaA1.getStringCellValue(),celdaN.getStringCellValue(),celdaE.getStringCellValue(),usuarios,empresas);
                            
                        }
                        
                        if(celdaEmail == null) {
                        	
                        	celdaEmail = fila.createCell(8);
                        	celdaEmail.setCellValue(email);
                        }
                    }
                }  
            }
        }
    }
    
    public String generaEmail(String apellido1, String apellido2, String nombre, String empresa, ArrayList<String> usuarios, ArrayList<String> empresas){
        
        String[] surname1 = apellido1.split("");
        String[] name = nombre.split("");
        
        String usuario = null;
        
        if(apellido2 != null){   
            
            String[] surname2 = apellido2.split("");
            
            usuario = surname1[0].concat(surname2[0]).concat(name[0]);
        }
        else{
            
            usuario = surname1[0].concat(name[0]);
        }
                
        String num = repeticion(usuarios, empresas, usuario, empresa);
        
        usuarios.add(usuario);
        empresas.add(empresa);
        
        String email = usuario.concat(num).concat("@").concat(empresa).concat(".es");
        
        return email;
    }
    
    public String generaEmail(String apellido1, String nombre, String empresa, ArrayList<String> usuarios, ArrayList<String> empresas){
        
        String[] surname = apellido1.split("");
        String[] name = nombre.split("");
        
        String usuario = surname[0].concat(name[0]);
                
        String num = repeticion(usuarios, empresas, usuario, empresa);
        
        usuarios.add(usuario);
        empresas.add(empresa);
        
        String email = usuario.concat(num).concat("@").concat(empresa).concat(".es");
        
        return email;
    }
    
     public String repeticion(ArrayList<String> usuarios, ArrayList<String> empresas, String usuario, String empresa){
        
        int contador = 0;
        
        for(int i = 0; i < usuarios.size(); i++){
            
            if(usuario.equals(usuarios.get(i)) && empresa.equals(empresas.get(i))){
                
                contador++;
            }
        }
        
        String num = Integer.toString(contador);
        
        if(contador < 10){
            
            num = "0".concat(num);  
        }
        
        return num;
    }
    
}
