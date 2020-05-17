package excel;

import clases.Categorias;
import clases.Nomina;
import static excel.Excel.filaVacia;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Nominas {

    XSSFWorkbook excel;
    List<Nomina> nominas;
    List<Categorias> categorias;
    
    public Nominas(){
        
        excel = Excel.getExcel();
        categorias = Excel.getCategorias();
    }
    
    public void generaNominas(int mes, int anyo){

        XSSFSheet hoja = excel.getSheetAt(0); // hoja de trabajadores
        Row fila;
                
        for(int i = 1; i <= hoja.getLastRowNum(); i++){ // genera la nómina de cada trabajador
            
            fila = hoja.getRow(i);
            Cell celda = fila.getCell(7);
                                
            if(celda != null && celda.getCellType() != CellType.BLANK && StringUtils.isNotBlank(celda.toString()) && !filaVacia(fila)){
                try {
                    generaNominaTrabajador(mes, anyo, fila);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            
            System.out.println("-------------------------------");
        }
    }

    
    private void generaNominaTrabajador(int mes, int anyo, Row trabajador) throws ParseException {
        
        // comprueba si se ha de generar la nómina
        Date fechaAltaTrabajador = trabajador.getCell(3).getDateCellValue();
        Calendar fechaAltaCalendar = Calendar.getInstance();
        fechaAltaCalendar.setTime(fechaAltaTrabajador);

        Calendar fechaNominaCalendar = new GregorianCalendar(anyo, mes-1, 2);
        
        if(fechaAltaCalendar.after(fechaNominaCalendar)){ // aún no estaba en la empresa
            System.out.println("El trabajador aun no estaba en la empresa: " + fechaAltaCalendar.get(Calendar.MONTH) + "/" + fechaAltaCalendar.get(Calendar.YEAR));
            return;
        }

        // imprime datos
        //System.out.println("EMPRESA\n" + trabajador.getCell(1).getStringCellValue() + "  CIF: " + trabajador.getCell(0).getStringCellValue());
        String apellidos = "";
        if(trabajador.getCell(6) != null){
            apellidos = trabajador.getCell(5).getStringCellValue() + " " + trabajador.getCell(6).getStringCellValue();
        } else{
            apellidos = trabajador.getCell(5).getStringCellValue();
        }
        System.out.println("TRABAJADOR\n" + trabajador.getCell(4).getStringCellValue() + " " + apellidos);
        
        // calcula la antigüedad del trabajador (trienios)
        int trienios = getTrienios(fechaAltaCalendar, fechaNominaCalendar);

        // obtengo la categoría del trabajador
        String nombreCat = trabajador.getCell(2).getStringCellValue();
        Categorias categoria = Excel.getCategoriaPorNombre(nombreCat);
        System.out.println("Categoría: " + categoria.getNombreCategoria());
        
        // comprueba el prorrateo
        String prorrateoString = trabajador.getCell(12).getStringCellValue();
        boolean prorrateo = false;
        if(prorrateoString.equals("SI")){
            prorrateo = true;
        } else if(prorrateoString.equals("NO")){
            prorrateo = false;
        }
        
        // calcula la nómina
        System.out.println("NÓMINA " + mes + "/" + anyo);
        calcularNomina(prorrateo, fechaAltaCalendar, fechaNominaCalendar, categoria, trienios, false);
        
        // calcula la extra, si corresponde
        boolean esPagaExtra = !prorrateo && (fechaNominaCalendar.get(Calendar.MONTH) == Calendar.JUNE || fechaNominaCalendar.get(Calendar.MONTH) == Calendar.DECEMBER);
        if(esPagaExtra){
            System.out.println("EXTRA " + mes + "/" + anyo);
            calcularNomina(prorrateo, fechaAltaCalendar, fechaNominaCalendar, categoria, trienios, true);
        }
    }

    private double calcularNomina(boolean prorrateo, Calendar fechaAltaCalendar, Calendar fechaNominaCalendar, Categorias categoria, int trienios, boolean esExtra){
        // importes
        double dineroMensualPorTrienios = Excel.getTrienios().getOrDefault(trienios, 0);
        double brutoAnual = redondea(categoria.getSalarioBaseCategoria() + categoria.getComplementoCategoria() + dineroMensualPorTrienios*14);
        double brutoMensual = redondea(brutoAnual / 14);
        double prorrateoExtra = redondea(brutoMensual/6);
        double calculoBase = brutoMensual + prorrateoExtra;
        if(prorrateo){
            brutoMensual = calculoBase; 
        }else{
            prorrateoExtra = 0.0;
        }
        
        // imprime datos importes
        System.out.println("-Importes\nSalario base mes: " + redondea(categoria.getSalarioBaseCategoria()/14));
        System.out.println("Prorrateo: " + prorrateoExtra);
        System.out.println("Complemento mes: " + redondea(categoria.getComplementoCategoria()/14));
        System.out.println("Antigüedad: " + trienios + " trienios  Dinero mensual por trienios: " + dineroMensualPorTrienios);
        
        // descuentos
        double retenciones = getRetenciones(calculoBase, brutoMensual, brutoAnual, esExtra);
        double liquidoMensual = redondea(brutoMensual - retenciones);
               
        // imprime datos trabajador
        System.out.println("TOTAL ingresos: " + brutoMensual + "  TOTAL deducciones: " + retenciones);
        System.out.println("LÍQUIDO a percibir: " + liquidoMensual);
        
        // coste empresario
        double costeEmpresario = getRetencionesEmpresario(calculoBase, esExtra);
        System.out.println("TOTAL empresario: " + costeEmpresario);
        System.out.println("COSTE TOTAL TRABAJADOR: " + (costeEmpresario+brutoMensual));
        
        return liquidoMensual;
    }

    private double getRetenciones(double calculoBase, double brutoMensual, double brutoAnual, boolean pagaExtra){
        Map<String, Double> valorRetenciones = Excel.getValores();
        double porcentajeSSocial = valorRetenciones.get("Cuota obrera general TRABAJADOR");
        double porcentajeDesempleo = valorRetenciones.get("Cuota desempleo TRABAJADOR");
        double porcentajeFormacion = valorRetenciones.get("Cuota formación TRABAJADOR");
        double porcentajeIrpf = Excel.getRetencion(brutoAnual);
        
        double retencionSSocial = 0.0, desempleo = 0.0, formacion = 0.0;
        if(!pagaExtra){
            retencionSSocial = redondea(calculoBase * porcentajeSSocial / 100);
            desempleo = redondea(calculoBase * porcentajeDesempleo / 100);
            formacion = redondea(calculoBase * porcentajeFormacion / 100);
        }
        double irpf = redondea(brutoMensual * porcentajeIrpf / 100);

        // imprime datos
        System.out.println("-Retenciones\nContingencias generales, " + porcentajeSSocial + "% de " + calculoBase + ": " + retencionSSocial);
        System.out.println("Desempleo, " + porcentajeDesempleo + "% de " + calculoBase + ": " + desempleo);
        System.out.println("Cuota formación, " + porcentajeFormacion + "% de " + calculoBase + ": " + formacion);
        System.out.println("IRPF, " + porcentajeIrpf + "% de " + brutoMensual + ": " + irpf);
        
        return retencionSSocial + desempleo + formacion + irpf;
    }

    private double getRetencionesEmpresario(double calculoBase, boolean pagaExtra){
        Map<String, Double> valorRetenciones = Excel.getValores();
        double porcentajeSSocial = valorRetenciones.get("Contingencias comunes EMPRESARIO");
        double porcentajeDesempleo = valorRetenciones.get("Desempleo EMPRESARIO");
        double porcentajeFogasa = valorRetenciones.get("Fogasa EMPRESARIO");
        double porcentajeFormacion = valorRetenciones.get("Formacion EMPRESARIO");
        double porcentajeAccidentes = valorRetenciones.get("Accidentes trabajo EMPRESARIO");
        
        double sSocialEmpresario = 0.0, desempleo = 0.0, fogasa = 0.0, formacion = 0.0, accidentesTrabajo = 0.0;
        if(!pagaExtra){
            sSocialEmpresario = redondea(calculoBase * porcentajeSSocial / 100);
            desempleo = redondea(calculoBase * porcentajeDesempleo / 100);
            fogasa = redondea(calculoBase * porcentajeFogasa / 100);
            formacion = redondea(calculoBase * porcentajeFormacion / 100);
            accidentesTrabajo = redondea(calculoBase * porcentajeAccidentes / 100);
        }
        
        // imprime datos
        System.out.println("-Pagos empresario  BASE: " + calculoBase);
        System.out.println("Contingencias comunes empresario, " + porcentajeSSocial + "% : " + sSocialEmpresario);
        System.out.println("Desempleo, " + porcentajeDesempleo + "% : " + desempleo);
        System.out.println("Formación, " + porcentajeFormacion + "% : " + formacion);
        System.out.println("Accidentes de trabajo,  " + porcentajeAccidentes + "% : " + accidentesTrabajo);
        System.out.println("FOGASA, " + porcentajeFogasa + "% : " + fogasa);

        return sSocialEmpresario + desempleo + fogasa + formacion + accidentesTrabajo;
    }

    private int getTrienios(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        int diffInYears = fechaNominaCalendar.get(Calendar.YEAR) - fechaAltaCalendar.get(Calendar.YEAR); //Numero de años de diferencia
        if (fechaAltaCalendar.get(Calendar.MONTH) > fechaNominaCalendar.get(Calendar.MONTH)) {
            diffInYears--;
        }
        return diffInYears/3;
    }
    
    private double redondea(double valor){
        
        return (double) Math.round(valor*100)/100;
    }
	
}
