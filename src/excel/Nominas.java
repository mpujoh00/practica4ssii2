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
        }
    }

    
    private void generaNominaTrabajador(int mes, int anyo, Row trabajador) throws ParseException {
        
        // comprueba si se ha de generar la nómina
        Date fechaAltaTrabajador = trabajador.getCell(3).getDateCellValue();
        Calendar fechaAltaCalendar = Calendar.getInstance();
        fechaAltaCalendar.setTime(fechaAltaTrabajador);

        Calendar fechaNominaCalendar = new GregorianCalendar(anyo, mes-1, 2);
        
        if(fechaAltaCalendar.after(fechaNominaCalendar)){ // aún no estaba en la empresa
            System.out.println("El trabajador aun no estaba en la empresa: " + fechaAltaTrabajador.toString());
            return;
        }

        // calcula la antigüedad del trabajador (trienios)
        int trienios = getTrienios(fechaAltaCalendar, fechaNominaCalendar);
        System.out.println("Trienios: " + trienios + " Alta: " + fechaAltaTrabajador.toString() + " nomina " + mes + "/" + anyo);

        // obtengo la categoría del trabajador
        String nombreCat = trabajador.getCell(2).getStringCellValue();
        Categorias categoria = Excel.getCategoriaPorNombre(nombreCat);
        System.out.println("EL trabajador es: " + categoria.getNombreCategoria());
        String prorrateoString = trabajador.getCell(12).getStringCellValue();
        boolean prorrateo = false;
        if(prorrateoString.equals("SI")){
            prorrateo = true;
        } else if(prorrateoString.equals("NO")){
            prorrateo = false;
        }
        boolean esPagaExtra = !prorrateo && (fechaNominaCalendar.get(Calendar.MONTH) == Calendar.JUNE || fechaNominaCalendar.get(Calendar.MONTH) == Calendar.DECEMBER);
        calcularNomina(prorrateo, fechaAltaCalendar, fechaNominaCalendar, categoria, trienios, false);
        System.out.println(esPagaExtra + " " + fechaNominaCalendar.get(Calendar.MONTH));
        if(esPagaExtra){
            System.out.println("Generando Extra:");
            calcularNomina(prorrateo, fechaAltaCalendar, fechaNominaCalendar, categoria, trienios, true);
        }
    }

    private double calcularNomina(boolean prorrateo, Calendar fechaAltaCalendar, Calendar fechaNominaCalendar, Categorias categoria, int trienios, boolean esExtra){
        double dineroPorTrienios = Excel.getTrienios().getOrDefault(trienios, 0);
        System.out.println("Dinero por trienios: " + dineroPorTrienios);
        double brutoAnual = categoria.getSalarioBaseCategoria() + categoria.getComplementoCategoria() + dineroPorTrienios*14;
        double brutoMensual = brutoAnual / 14;
        if(prorrateo){
            double parteExtra = calcularNomina(false, fechaAltaCalendar, fechaNominaCalendar, categoria, trienios, true) / 6;
            brutoMensual = brutoMensual + parteExtra;
        }
        System.out.println("Salario base: " + brutoAnual/12);
        double retenciones = getRetenciones(brutoMensual, brutoAnual, esExtra);
        System.out.println("Bruto Anual: " + brutoAnual + ", Bruto Mensual: " + brutoMensual + " retencion:" + retenciones);
        double liquidoMensual = brutoMensual - retenciones;
        System.out.println("Liquido Mensual: " + liquidoMensual);
        double costeEmpresario = brutoMensual + getRetencionesEmpresario(brutoMensual);
        System.out.println("Coste Empresario: " + costeEmpresario);

        return liquidoMensual;
    }

    private double getRetenciones(double brutoMensual, double brutoAnual, boolean pagaExtra){
        Map<String, Double> valorRetenciones = Excel.getValores();
        double retencionSSocial = brutoAnual/12 * valorRetenciones.get("Cuota obrera general TRABAJADOR") / 100;
        double desempleo = brutoAnual/12 * valorRetenciones.get("Cuota desempleo TRABAJADOR") / 100;
        double formacion = brutoAnual/12 * valorRetenciones.get("Cuota formación TRABAJADOR") / 100;
        double porcentajeIrpf = Excel.getRetencion(brutoAnual);
        double irpf = brutoMensual * porcentajeIrpf / 100;

        if(pagaExtra){
            return irpf;
        }

        return retencionSSocial + desempleo + formacion + irpf;
    }

    private double getRetencionesEmpresario(double brutoMensual){
        Map<String, Double> valorRetenciones = Excel.getValores();
        double sSocialEmpresario = brutoMensual * valorRetenciones.get("Contingencias comunes EMPRESARIO") / 100;
        double desempleo = brutoMensual * valorRetenciones.get("Desempleo EMPRESARIO") / 100;
        double fogasa = brutoMensual * valorRetenciones.get("Fogasa EMPRESARIO") / 100;
        double formacion = brutoMensual * valorRetenciones.get("Formacion EMPRESARIO") / 100;
        double accidentesTrabajo = brutoMensual * valorRetenciones.get("Accidentes trabajo EMPRESARIO") / 100;

        return sSocialEmpresario + desempleo + fogasa + formacion + accidentesTrabajo;
    }

    private int getTrienios(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        int diffInYears = fechaNominaCalendar.get(Calendar.YEAR) - fechaAltaCalendar.get(Calendar.YEAR); //Numero de años de diferencia
        if (fechaAltaCalendar.get(Calendar.MONTH) > fechaNominaCalendar.get(Calendar.MONTH)) {
            diffInYears--;
        }
        return diffInYears/3;
    }
	
}
