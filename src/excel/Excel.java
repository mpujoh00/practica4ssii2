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
import java.util.List;
import java.util.Map;

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
    
     public static List<Categorias> getCategorias(){ //hoja 2
        
        XSSFSheet hoja = excel.getSheetAt(1);
        
        Row fila;
        
        List<Categorias> categorias = new ArrayList<>(hoja.getLastRowNum()-1);
        
        for(int i = 1; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);
            
            Categorias cat = new Categorias(i, fila.getCell(0).toString(), Double.parseDouble(fila.getCell(1).toString()),
            		Double.parseDouble(fila.getCell(2).toString()));
            
            categorias.add(cat);
        } 
        
        return categorias;
    }

    public static Categorias getCategoriaPorNombre(String nombreCategoria){
        List<Categorias> categorias = getCategorias();
        for(Categorias cat : categorias){
            if(cat.getNombreCategoria().equals(nombreCategoria)){
                return cat;
            }
        }
        return null;
    }
    
    public static Map<Integer, Double> getRetenciones(){ //hoja 3
        
        XSSFSheet hoja = excel.getSheetAt(2);
        
        Row fila;
        
        //definir un HashMap
        Map<Integer, Double> retenciones = new HashMap<>();
                
        for(int i = 1; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);
            
            //insertar valores "key"-"value" al HashMap
            retenciones.put((int) fila.getCell(0).getNumericCellValue(), fila.getCell(1).getNumericCellValue());
        }
        
        return retenciones;
        
    }

    public static double getRetencion(double salario){
        Map<Integer, Double> retenciones = getRetenciones();
        int salarioRedondeado = (int) Math.ceil(salario / 1000) * 1000;
        double valorObtenido = retenciones.getOrDefault(salarioRedondeado, 0.0);
        if(valorObtenido == 0){
            if(salario > 60000){
                valorObtenido = retenciones.getOrDefault(60000, -1.0);
            }else if(salario < 12000){
                valorObtenido = retenciones.getOrDefault(12000, -1.0);
            }
        }
        
        return valorObtenido;
    }
    
    public static Map<String, Double> getValores(){ //hoja 4
        
        XSSFSheet hoja = excel.getSheetAt(3);
        
        Row fila;
        
        //definir un HashMap
        Map<String, Double> valores = new HashMap<>();
                
        for(int i = 0; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);
            
            //insertar valores "key"-"value" al HashMap
            valores.put(fila.getCell(0).toString(), Double.parseDouble(fila.getCell(1).toString()));
        }
        
        return valores;
    }
    
    public static Map<Integer, Integer> getTrienios(){ //hoja 5
        
        XSSFSheet hoja = excel.getSheetAt(4);
        
        Row fila;
        
        //definir un HashMap
        Map<Integer, Integer> trienios = new HashMap<>();
        for(int i = 1; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);
            
            //insertar valores "key"-"value" al HashMap
            trienios.put((int) fila.getCell(0).getNumericCellValue(), (int) fila.getCell(1).getNumericCellValue());
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
