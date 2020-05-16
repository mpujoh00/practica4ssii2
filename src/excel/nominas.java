package excel;

import clases.Categorias;
import clases.Nomina;
import static excel.Excel.filaVacia;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class nominas {

    XSSFWorkbook excel;
    ArrayList<Nomina> nominas;
    ArrayList<Categorias> categorias;
    
    public nominas(){
        
        excel = Excel.getExcel();
        categorias = Excel.getCategorias();
    }
    
    public void generaNominas(int mes, int anyo) {

        XSSFSheet hoja = excel.getSheetAt(0); // hoja de trabajadores
        Row fila;
                
        for(int i = 1; i <= hoja.getLastRowNum(); i++){ // genera la nómina de cada trabajador
            
            fila = hoja.getRow(i);
                                
            if(!filaVacia(fila)){
                
                generaNominaTrabajador(mes, anyo, fila);
            }
        }
    }
    
    private void generaNominaTrabajador(int mes, int anyo, Row trabajador){
        
        // comprueba si se ha de generar la nómina
        
        String fecha = trabajador.getCell(3).getStringCellValue();
        String f[] = fecha.split("/");
        int d = Integer.parseInt(f[0]);
    	int m = Integer.parseInt(f[1]);
    	int a = Integer.parseInt(f[2]);
        
        Calendar fechaAlta = new GregorianCalendar(a, m, d);
        Calendar fechaNomina = new GregorianCalendar(anyo, mes, 2);
        
        if(fechaAlta.after(fechaNomina)){ // aún no estaba en la empresa
            return;
        }
        
        // calcula la antigüedad del trabajador (trienios)
        
        int anyosTrabajados = fechaNomina.get(Calendar.YEAR) - fechaAlta.get(Calendar.YEAR);
        int trienios = anyosTrabajados/3;
        
        if(fechaNomina.get(Calendar.MONTH) - fechaAlta.get(Calendar.MONTH) <= 0){
            
            trienios--;
        }
        
        // obtengo la categoría del trabajador
        
        String nombreCat = trabajador.getCell(2).getStringCellValue();
        Categorias categoria;
        
        for(Categorias cat: categorias){
            
            if(cat.getNombreCategoria().equals(nombreCat)){
                
                categoria = cat;
            }
        }
        
    }
	
}
