/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excel;

import clases.Categorias;
import clases.Empresas;
import clases.Trabajadorbbdd;
import static excel.Excel.filaVacia;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
public class Trabajadores {
    
    XSSFWorkbook excel;
    
    public Trabajadores (){        
        excel = Excel.getExcel();
    }
    
    public List<Trabajadorbbdd> getTrabajadores(){
        
        List<Trabajadorbbdd> trabajadores = new ArrayList<>();  
        List<List<String>> nifs = new ArrayList<>();
        XSSFSheet hoja = excel.getSheetAt(0);
        
        // guarda los nifs 
        for(int i = 1; i <= hoja.getLastRowNum(); i++){

            Row fila = hoja.getRow(i);
            Cell celdaNif = fila.getCell(7); // selecciona la casilla correspondiente al NIF/NIE

            if(!(celdaNif == null || celdaNif.getCellType() == CellType.BLANK || StringUtils.isBlank(celdaNif.toString())) && !filaVacia(fila)){

                nifs.add(new ArrayList<String>());
                nifs.get(nifs.size()-1).add(celdaNif.getStringCellValue());
                nifs.get(nifs.size()-1).add(Integer.toString(i+1));
                nifs.get(nifs.size()-1).add("");
            }
        }
        
        // guarda los duplicados
        for(int i = 0; i <= nifs.size()-1; i++){
            for(int j = 0; j <= nifs.size()-1; j++){
                
                String nombre1 = hoja.getRow(Integer.parseInt(nifs.get(i).get(1))-1).getCell(4).getStringCellValue();
                String nombre2 = hoja.getRow(Integer.parseInt(nifs.get(j).get(1))-1).getCell(4).getStringCellValue(); 
                
                if(i != j && nifs.get(i).get(0).equals(nifs.get(j).get(0)) && nombre1.equals(nombre2) && nifs.get(i).get(2).equals("") && nifs.get(j).get(2).equals("")){

                    nifs.get(i).set(2, "duplicado");
                }          
            } 
        }
        
        int cont = 1;
        
        // crea los trabajadores / empresas
        for(int i = 0; i <= nifs.size()-1; i++){
          
            if(nifs.get(i).get(2).equals("")){ // no está duplicado
                
                int indiceFila = Integer.parseInt(nifs.get(i).get(1))-1;
                Row fila = hoja.getRow(indiceFila);
                
                Empresas empresa = Excel.getEmpresaPorCIF(fila.getCell(0).getStringCellValue());
                Categorias categoria = Excel.getCategoriaPorNombre(fila.getCell(2).getStringCellValue());
                Date fechaAlta = fila.getCell(3).getDateCellValue();
                String nombre = fila.getCell(4).getStringCellValue();
                String apellido1 = fila.getCell(5).getStringCellValue();
                String apellido2;
                if(fila.getCell(6) == null)
                    apellido2 = "";
                else
                    apellido2 = fila.getCell(6).getStringCellValue();
                String nif = fila.getCell(7).getStringCellValue();
                String email = fila.getCell(8).getStringCellValue();
                String codigoCuenta = fila.getCell(9).getStringCellValue();
                String iban = fila.getCell(11).getStringCellValue();
                boolean prorrateo = false;
                if(fila.getCell(12).getStringCellValue().equals("SI"))
                    prorrateo = true;
                
                Trabajadorbbdd trabajador = new Trabajadorbbdd(cont,categoria,empresa,nombre,apellido1,apellido2,nif,email,fechaAlta,codigoCuenta,iban,prorrateo);
                trabajadores.add(trabajador);
            }
        }        
        
        return trabajadores;
    }
    
}
