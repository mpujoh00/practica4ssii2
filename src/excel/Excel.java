/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import clases.Categorias;

/**
 *
 * @author Micaela Pujol Higueras
 * @author Silvia Matilla García
 */
public class Excel {
    
    private static String rutaArchivo = "./resources/SistemasInformacionII.xlsx";
    private static XSSFWorkbook excel;
    private static FileInputStream archivo;
    
    private Excel(){
        
        try{ // leo el archivo excel
            archivo = new FileInputStream(new File(rutaArchivo));
            excel = new XSSFWorkbook(archivo);               
        }
        catch(Exception e){
            e.printStackTrace();
        }              
    }
        
    public static boolean filaVacia(Row fila){
        
        if (fila == null) {
            return true;
        }
        if (fila.getLastCellNum() <= 0) {
            return true;
        }
        for (int cellNum = fila.getFirstCellNum(); cellNum < fila.getLastCellNum(); cellNum++) {
            Cell celda = fila.getCell(cellNum);
            if (celda != null && celda.getCellType() != CellType.BLANK && StringUtils.isNotBlank(celda.toString())) {
                return false;
            }
        }
        return true;
    } 
    
    // HASHMAP
    
     public static ArrayList<Categorias> getCategorias(){ //hoja 2
        
        XSSFSheet hoja = excel.getSheetAt(1);
        
        Row fila;
        
        ArrayList<Categorias> categorias = new ArrayList<>(hoja.getLastRowNum()-1);
        
        for(int i = 1; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);
            
            Categorias cat = new Categorias(i, fila.getCell(0).toString(), Double.parseDouble(fila.getCell(1).toString()),
            		Double.parseDouble(fila.getCell(2).toString()));
            
            categorias.add(cat);
        } 
        
        for(Categorias cat: categorias) {
        	
        	System.out.println(cat.toString());
        }
        
        return categorias;
    }
    
    public HashMap<Double, Double> getRetenciones(){ //hoja 3
        
        XSSFSheet hoja = excel.getSheetAt(2);
        
        Row fila;
        
        //definir un HashMap
        HashMap<Double, Double> retenciones = new HashMap<Double, Double>();
                
        for(int i = 1; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);
            
            //insertar valores "key"-"value" al HashMap
            retenciones.put(Double.parseDouble(fila.getCell(0).toString()), Double.parseDouble(fila.getCell(1).toString()));            
        }
                
        for(Double key: retenciones.keySet()) {
        	
        	System.out.println(key + ", " + retenciones.get(key));
        }
        
        return retenciones;
        
    }
    
    public HashMap<String, Double> getValores(){ //hoja 4
        
        XSSFSheet hoja = excel.getSheetAt(3);
        
        Row fila;
        
        //definir un HashMap
        HashMap<String, Double> valores = new HashMap<String, Double>();
                
        for(int i = 0; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);
            
            //insertar valores "key"-"value" al HashMap
            valores.put(fila.getCell(0).toString(), Double.parseDouble(fila.getCell(1).toString()));
        }
        
        for(String key: valores.keySet()) {
        	
        	System.out.println(key + ", " + valores.get(key));
        }
        
        return valores;
    }
    
    public HashMap<Double, Double> getTrienios(){ //hoja 5
        
        XSSFSheet hoja = excel.getSheetAt(4);
        
        Row fila;
        
        //definir un HashMap
        HashMap<Double, Double> trienios = new HashMap<Double, Double>();
                
        for(int i = 1; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);
            
            //insertar valores "key"-"value" al HashMap
            trienios.put(Double.parseDouble(fila.getCell(0).toString()), Double.parseDouble(fila.getCell(1).toString()));
        }
        
        for(Double key: trienios.keySet()) {
        	
        	System.out.println(key + ", " + trienios.get(key));
        }
        
        return trienios;
    }
    
    public static void close(){
        
        try{
            archivo.close();
            FileOutputStream output = new FileOutputStream(new File(rutaArchivo));        
            excel.write(output);
            output.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }        
    }
    
    public static XSSFWorkbook getExcel(){
        
        if(excel == null){
            new Excel();
        }
        return excel;
    }
}
