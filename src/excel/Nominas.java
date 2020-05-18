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
    Nomina nomina;
    int contNominas;
    
    public Nominas(){
        
        excel = Excel.getExcel();
        categorias = Excel.getCategorias();
    }
    
    public void generaNominas(int mes, int anyo){

        nominas = new ArrayList<>();
        XSSFSheet hoja = excel.getSheetAt(0); // hoja de trabajadores
        Row fila;
        contNominas = 1;
                
        for(int i = 1; i <= hoja.getLastRowNum(); i++){ // genera la nómina de cada trabajador
            
            fila = hoja.getRow(i);
            Cell celda = fila.getCell(7);
                                
            if(celda != null && celda.getCellType() != CellType.BLANK && StringUtils.isNotBlank(celda.toString()) && !filaVacia(fila)){
                try {
                    nomina = new Nomina(contNominas);
                    if(generaNominaTrabajador(mes, anyo, fila)){
                        nominas.add(nomina);
                        contNominas++;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            
            System.out.println("--------------------------------------------------------------------------------");
        }
    }

    
    private boolean generaNominaTrabajador(int mes, int anyo, Row trabajador) throws ParseException {
        
        // comprueba si se ha de generar la nómina
        Date fechaAltaTrabajador = trabajador.getCell(3).getDateCellValue();
        Calendar fechaAltaCalendar = Calendar.getInstance();
        fechaAltaCalendar.setTime(fechaAltaTrabajador);

        Calendar fechaNominaCalendar = new GregorianCalendar(anyo, mes-1, 2);
        
        if(fechaAltaCalendar.after(fechaNominaCalendar)){ // aún no estaba en la empresa
            System.out.println("El trabajador aun no estaba en la empresa: Alta en " + fechaAltaCalendar.get(Calendar.MONTH) + "/" + fechaAltaCalendar.get(Calendar.YEAR));
            return false;
        }
        
        nomina.setMes(mes);
        nomina.setAnio(anyo);

        // imprime datos
        //System.out.println("EMPRESA\n" + trabajador.getCell(1).getStringCellValue() + "  CIF: " + trabajador.getCell(0).getStringCellValue());
        String apellidos = "";
        if(trabajador.getCell(6) != null){
            apellidos = trabajador.getCell(5).getStringCellValue() + " " + trabajador.getCell(6).getStringCellValue();
        } else{
            apellidos = trabajador.getCell(5).getStringCellValue();
        }
        System.out.println("\n" + trabajador.getCell(4).getStringCellValue() + " " + apellidos);
        
        if(trabajador.getCell(4).getStringCellValue().equals("Rocío")){
            System.out.println("jeje it's me bitch");
        }
        
        // calcula la antigüedad del trabajador (trienios)
        boolean cambioTrienio = hayCambioDeTrienio(fechaAltaCalendar, fechaNominaCalendar);
        int trienios = getTrienios(fechaAltaCalendar, fechaNominaCalendar, cambioTrienio);
        
        // obtengo la categoría del trabajador
        String nombreCat = trabajador.getCell(2).getStringCellValue();
        Categorias categoria = Excel.getCategoriaPorNombre(nombreCat);
        System.out.println("-Categoría: " + categoria.getNombreCategoria() + "\n");
        
        // comprueba el prorrateo
        String prorrateoString = trabajador.getCell(12).getStringCellValue();
        boolean prorrateo = false;
        if(prorrateoString.equals("SI")){
            prorrateo = true;
        } else if(prorrateoString.equals("NO")){
            prorrateo = false;
        }
        
        // calcula la nómina
        // System.out.println("NÓMINA " + mes + "/" + anyo);
        nomina.setEsExtra(false);
        calcularNomina(prorrateo, categoria, trienios, false, cambioTrienio, fechaAltaCalendar, fechaNominaCalendar);
        
        // calcula la extra, si corresponde
        boolean esPagaExtra = !prorrateo && (fechaNominaCalendar.get(Calendar.MONTH) == Calendar.JUNE || fechaNominaCalendar.get(Calendar.MONTH) == Calendar.DECEMBER);
        if(esPagaExtra){
            //System.out.println("EXTRA " + mes + "/" + anyo);
            System.out.println(nomina.toString()); // imprime la nomina normal
            nominas.add(nomina);
            nomina = new Nomina(++contNominas);
            nomina.setMes(mes);
            nomina.setAnio(anyo);
            nomina.setEsExtra(true);
            calcularNomina(prorrateo, categoria, trienios, true, cambioTrienio, fechaAltaCalendar, fechaNominaCalendar);
        }
        
        System.out.println(nomina.toString());
        
        return true;
    }

    private double calcularNomina(boolean prorrateo, Categorias categoria, int trienios, boolean esExtra, boolean cambioTrienio, Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        // importes
        double dineroMensualPorTrieniosAnt = Excel.getTrienios().getOrDefault(trienios-1, 0);
        double dineroMensualPorTrieniosNuevo = Excel.getTrienios().getOrDefault(trienios, 0);
        double totalTrienios = 0.0;
        if(cambioTrienio){
            int mesesTrienioAnt = fechaAltaCalendar.get(Calendar.MONTH) + 1;
            int extraNuevo = 2;
            if(fechaAltaCalendar.get(Calendar.MONTH) > Calendar.JUNE){
                extraNuevo = 1;
            }
            totalTrienios = dineroMensualPorTrieniosAnt * (mesesTrienioAnt + 2 - extraNuevo) + dineroMensualPorTrieniosNuevo * (12 - mesesTrienioAnt + extraNuevo);                        
        }else if(hayCambioDeTrienioAnyoSig(fechaAltaCalendar, fechaNominaCalendar)){ // influye por la extra de diciembre (prorrateo y bruto anual)
            double dineroMensualPorTrieniosAnyoSig = Excel.getTrienios().getOrDefault(trienios+1, 0);
            totalTrienios = redondea(dineroMensualPorTrieniosNuevo * 14 + (dineroMensualPorTrieniosAnyoSig - dineroMensualPorTrieniosNuevo) / 6);            
        }else{
            totalTrienios = dineroMensualPorTrieniosNuevo*14;
        }
        int trieniosMes = getTrieniosMes(fechaAltaCalendar, fechaNominaCalendar);
        double salarioBase = categoria.getSalarioBaseCategoria();
        double complemento = categoria.getComplementoCategoria();
        double brutoAnual = redondea(salarioBase + complemento + totalTrienios);
        double brutoMensual = redondea(salarioBase/14 + complemento/14 + dineroMensualPorTrieniosNuevo);
        double prorrateoExtra = redondea(brutoMensual/6);
        double calculoBase = redondea(brutoMensual + prorrateoExtra);
        if(prorrateo){
            brutoMensual = calculoBase; 
        }else{
            prorrateoExtra = 0.0;
        }
        
        // descuentos
        double retenciones = getRetenciones(calculoBase, brutoMensual, brutoAnual, esExtra);
        double liquidoMensual = redondea(brutoMensual - retenciones);
                       
        // coste empresario
        double costeEmpresario = getRetencionesEmpresario(calculoBase, esExtra);
        
        nomina.setNumeroTrienios(trieniosMes);
        nomina.setImporteTrienios(Excel.getTrienios().getOrDefault(trieniosMes, 0));
        nomina.setImporteSalarioMes(redondea(salarioBase/14));
        nomina.setImporteComplementoMes(redondea(complemento/14));
        nomina.setValorProrrateo(prorrateoExtra);
        nomina.setBrutoAnual(brutoAnual);
        nomina.setBaseEmpresario(calculoBase);
        nomina.setBrutoNomina(brutoMensual);
        nomina.setLiquidoNomina(liquidoMensual);
        nomina.setCosteTotalEmpresario(costeEmpresario);
        
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

        nomina.setSeguridadSocialTrabajador(porcentajeSSocial);
        nomina.setImporteSeguridadSocialTrabajador(retencionSSocial);
        nomina.setDesempleoTrabajador(porcentajeDesempleo);
        nomina.setImporteDesempleoTrabajador(desempleo);
        nomina.setFormacionTrabajador(porcentajeFormacion);
        nomina.setImporteFormacionTrabajador(formacion);
        nomina.setIrpf(porcentajeIrpf);
        nomina.setImporteIrpf(irpf);        
        
        return redondea(retencionSSocial + desempleo + formacion + irpf);
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
        
        nomina.setSeguridadSocialEmpresario(porcentajeSSocial);
        nomina.setImporteSeguridadSocialEmpresario(sSocialEmpresario);
        nomina.setDesempleoEmpresario(porcentajeDesempleo);
        nomina.setImporteDesempleoEmpresario(desempleo);
        nomina.setFogasaempresario(porcentajeFogasa);
        nomina.setImporteFogasaempresario(fogasa);
        nomina.setFormacionEmpresario(porcentajeFormacion);
        nomina.setImporteFormacionEmpresario(formacion);
        nomina.setAccidentesTrabajoEmpresario(porcentajeAccidentes);
        nomina.setImporteAccidentesTrabajoEmpresario(accidentesTrabajo);
        
        return redondea(sSocialEmpresario + desempleo + fogasa + formacion + accidentesTrabajo);
    }
    
    private boolean esEmpleadoNuevo(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        
        return false;
    }
    
    private boolean hayCambioDeTrienio(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        
        int cambioTrienio = (fechaNominaCalendar.get(Calendar.YEAR) - fechaAltaCalendar.get(Calendar.YEAR)) % 3;
        if(cambioTrienio == 0){
            return true;
        }else{
            return false;
        }
    }

    private boolean hayCambioDeTrienioAnyoSig(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        
        int cambioTrienio = (fechaNominaCalendar.get(Calendar.YEAR) - fechaAltaCalendar.get(Calendar.YEAR)) % 3;
        if(cambioTrienio == 2 && fechaNominaCalendar.get(Calendar.MONTH) == Calendar.DECEMBER){
            return true;
        }else{
            return false;
        }
    }
    
    private int getTrienios(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar, boolean cambioTrienio){ // revisar // diciembre año anterior
        
        int diffInYears = fechaNominaCalendar.get(Calendar.YEAR) - fechaAltaCalendar.get(Calendar.YEAR); 
        /*if (fechaAltaCalendar.get(Calendar.MONTH) > fechaNominaCalendar.get(Calendar.MONTH) && !cambioTrienio) {
            diffInYears--;
        }*/
        return diffInYears/3;
    }
    
    private int getTrieniosMes(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        
        int diffInYears = fechaNominaCalendar.get(Calendar.YEAR) - fechaAltaCalendar.get(Calendar.YEAR); 
        if (fechaAltaCalendar.get(Calendar.MONTH) > fechaNominaCalendar.get(Calendar.MONTH)) {
            diffInYears--;
        }
        return diffInYears/3;
    }
    
    private double redondea(double valor){
        
        return (double) Math.round(valor*100)/100;
    }
	
}
