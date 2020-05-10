/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excel;

import static excel.Excel.filaVacia;
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
public class nifs {
    
    XSSFWorkbook excel;
    
    public nifs(){
        
        excel = Excel.getExcel();        
    }
    
    public void corrigeNifs(){
        
        XSSFSheet hoja = excel.getSheetAt(0);
        
        Row fila;
                
        for(int i = 1; i <= hoja.getLastRowNum(); i++){
            
            fila = hoja.getRow(i);
            
            Cell celda = fila.getCell(7); // selecciona la casilla correspondiente al NIF/NIE
                                
            if(celda != null && celda.getCellType() != CellType.BLANK && StringUtils.isNotBlank(celda.toString()) && !filaVacia(fila)){
                
                String res = validaLetra(celda.getStringCellValue());
                
                if(res != "inc" && res != null){ // modifica los nif/nie erróneos
                    
                    celda.setCellValue(celda.getStringCellValue().substring(0,8).concat(res));
                    
                }
            }
        }
    }
    
    private static String validaLetra(String nif){
        
        String res = null;
        String letra = null;
        
        if(nif.length() != 9){
            res = "inc";
        }
        else{
            String firstChar = nif.substring(0,1);
            String n = null;
            
            if(firstChar.equals("X")){
                n = "0";
                nif = n.concat(nif.substring(1));
            }
            else if(firstChar.equals("Y")){
                n = "1";
                nif = n.concat(nif.substring(1));
            }
            else if(firstChar.equals("Z")){
                n = "2";
                nif = n.concat(nif.substring(1));
            }
            
            String numNif = nif.substring(0,8);
            
            int num = Integer.parseInt(numNif);
            int resto = num%23;
            
            switch(resto){
                case 0:
                    letra = "T";
                    break;
                case 1:
                    letra = "R";
                    break;
                case 2:
                    letra = "W";
                    break;
                case 3:
                    letra = "A";
                    break;
                case 4:
                    letra = "G";
                    break;
                case 5:
                    letra = "M";
                    break;
                case 6:
                    letra = "Y";
                    break;
                case 7:
                    letra = "F";
                    break;
                case 8:
                    letra = "P";
                    break;
                case 9:
                    letra = "D";
                    break;
                case 10:
                    letra = "X";
                    break;
                case 11:
                    letra = "B";
                    break;
                case 12:
                    letra = "N";
                    break;
                case 13:
                    letra = "J";
                    break;
                case 14:
                    letra = "Z";
                    break;
                case 15:
                    letra = "S";
                    break;
                case 16:
                    letra = "Q";
                    break;
                case 17:
                    letra = "V";
                    break;
                case 18:
                    letra = "H";
                    break;
                case 19:
                    letra = "L";
                    break;
                case 20:
                    letra = "C";
                    break;
                case 21:
                    letra = "K";
                    break;
                case 22:
                    letra = "E";
                    break;
            }
            
            if(!nif.substring(8).equals(letra)){
                res = letra;
            }
        }
        
        return res;
    }
}
