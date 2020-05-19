package excel;

import clases.Categorias;
import clases.Nomina;
import clases.Trabajadorbbdd;

import java.text.ParseException;
import java.util.*;

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
    
    public List<Nomina> generaNominas(int mes, int anyo){

        nominas = new ArrayList<>();
        Trabajadores t = new Trabajadores();
        List<Trabajadorbbdd> trabajadores = t.getTrabajadores();
        contNominas = 1;
                
        for(Trabajadorbbdd trabajador: trabajadores){
            try{
                nomina = new Nomina(contNominas);
                if(generaNominaTrabajador(mes, anyo, trabajador)){
                    nominas.add(nomina);
                    contNominas++;
                }
            }catch(ParseException e){
                e.printStackTrace();
            }
        }
        
        return nominas;
    }

    
    private boolean generaNominaTrabajador(int mes, int anyo, Trabajadorbbdd trabajador) throws ParseException {
        
        // comprueba si se ha de generar la nómina
        Date fechaAltaTrabajador = trabajador.getFechaAlta();
        Calendar fechaAltaCalendar = Calendar.getInstance();
        fechaAltaCalendar.setTime(fechaAltaTrabajador);
        Calendar fechaNominaCalendar = new GregorianCalendar(anyo, mes-1, 2);
        
        if(fechaAltaCalendar.after(fechaNominaCalendar)){ // aún no estaba en la empresa
            return false;
        }
        
        nomina.setMes(mes);
        nomina.setAnio(anyo);
        nomina.setTrabajadorbbdd(trabajador);
                
        // calcula la antigüedad del trabajador (trienios)
        boolean cambioTrienio = hayCambioDeTrienio(fechaAltaCalendar, fechaNominaCalendar);
        int trienios = getTrienios(fechaAltaCalendar, fechaNominaCalendar);
        
        // comprueba si ha entrado ese mismo año
        boolean nuevo = esEmpleadoNuevo(fechaAltaCalendar, fechaNominaCalendar);
        int meses = 0;
        if(nuevo){
            meses = getMesesTrabajando(fechaAltaCalendar, fechaNominaCalendar);
        }
        nomina.setEsNuevo(nuevo);
        
        // obtengo la categoría del trabajador
        Categorias categoria = trabajador.getCategorias();
        
        // comprueba el prorrateo
        boolean prorrateo = trabajador.getProrrateo();
                        
        // calcula la nómina
        nomina.setEsExtra(false);
        calcularNomina(prorrateo, categoria, trienios, false, cambioTrienio, fechaAltaCalendar, fechaNominaCalendar, nuevo, meses);
        
        // calcula la extra, si corresponde
        boolean esPagaExtra = !prorrateo && (fechaNominaCalendar.get(Calendar.MONTH) == Calendar.JUNE || fechaNominaCalendar.get(Calendar.MONTH) == Calendar.DECEMBER);
        if(esPagaExtra){
            nominas.add(nomina);
            nomina = new Nomina(++contNominas);
            nomina.setMes(mes);
            nomina.setAnio(anyo);
            nomina.setTrabajadorbbdd(trabajador);
            nomina.setEsExtra(true);
            if(fechaNominaCalendar.get(Calendar.MONTH) == Calendar.JUNE){
                if(fechaAltaCalendar.get(Calendar.MONTH) < Calendar.JUNE){
                    meses = Calendar.JUNE - fechaAltaCalendar.get(Calendar.MONTH) +1;
                }
            }
            calcularNomina(prorrateo, categoria, trienios, true, cambioTrienio, fechaAltaCalendar, fechaNominaCalendar, nuevo, meses);
        }
                
        return true;
    }

    private double calcularNomina(boolean prorrateo, Categorias categoria, int trienios, boolean esExtra, boolean cambioTrienio, Calendar fechaAltaCalendar, Calendar fechaNominaCalendar, boolean esNuevo, int meses){
        
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
        int trieniosMesActual = getTrieniosMes(fechaAltaCalendar, fechaNominaCalendar);
        double importeTrienioActual = Excel.getTrienios().getOrDefault(trieniosMesActual, 0);
        double salarioBase = categoria.getSalarioBaseCategoria();
        double complemento = categoria.getComplementoCategoria();
        double brutoAnual = brutoAnual = redondea(salarioBase + complemento + totalTrienios);
        double brutoMensual = redondea(salarioBase/14 + complemento/14 + importeTrienioActual);
        double prorrateoExtra = redondea(brutoMensual/6);
        double calculoBase = redondea(brutoMensual + prorrateoExtra);
        
        if(esNuevo){
            if(prorrateo){
                brutoAnual = brutoAnual/12 * meses;
            }else if(esExtra && meses < 6){
                salarioBase = salarioBase/2;
                complemento = complemento/2;
                brutoAnual = brutoAnual/14 * meses + brutoAnual/14/2;
                brutoMensual = redondea(salarioBase/14 + complemento/14);
            }else{
                brutoAnual = brutoAnual/14 * meses + brutoAnual/14/2;
            }
        }
        
        if(prorrateo){
            brutoMensual = calculoBase; 
        }else{
            prorrateoExtra = 0.0;
        }
        
        // descuentos
        double retenciones = getRetenciones(calculoBase, brutoMensual, brutoAnual, esExtra, esNuevo);
        double liquidoMensual = redondea(brutoMensual - retenciones);
                       
        // coste empresario
        double costeEmpresario = getRetencionesEmpresario(calculoBase, esExtra);
        
        nomina.setNumeroTrienios(trieniosMesActual);
        nomina.setImporteTrienios(Excel.getTrienios().getOrDefault(trieniosMesActual, 0));
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

    private double getRetenciones(double calculoBase, double brutoMensual, double brutoAnual, boolean pagaExtra, boolean esNuevo){
        
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
        
        if(fechaAltaCalendar.get(Calendar.YEAR) == fechaNominaCalendar.get(Calendar.YEAR) || fechaAltaCalendar.get(Calendar.YEAR) == fechaNominaCalendar.get(Calendar.YEAR -1) && fechaAltaCalendar.get(Calendar.MONTH) > fechaNominaCalendar.get(Calendar.MONTH)){
            return true;
        }else{
            return false;
        }
    }
    
    private int getMesesTrabajando(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        
        int meses = 0;
        if(fechaAltaCalendar.get(Calendar.YEAR) == fechaNominaCalendar.get(Calendar.YEAR)){
            meses = fechaNominaCalendar.get(Calendar.MONTH) - fechaAltaCalendar.get(Calendar.MONTH) +1;
        }else{
            meses = fechaNominaCalendar.get(Calendar.MONTH) + 12 - fechaAltaCalendar.get(Calendar.MONTH) +1;
        }
        return meses;
    }
    
    private boolean hayCambioDeTrienio(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        
        int cambioTrienio = (fechaNominaCalendar.get(Calendar.YEAR) - fechaAltaCalendar.get(Calendar.YEAR)) % 3;
        if(cambioTrienio == 0 && fechaAltaCalendar.get(Calendar.MONTH) != Calendar.DECEMBER){
            return true;
        }else{
            return false;
        }
    }

    private boolean hayCambioDeTrienioAnyoSig(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        
        int cambioTrienio = (fechaNominaCalendar.get(Calendar.YEAR) - fechaAltaCalendar.get(Calendar.YEAR)) % 3;
        if(cambioTrienio == 2 && fechaNominaCalendar.get(Calendar.MONTH) == Calendar.DECEMBER && fechaAltaCalendar.get(Calendar.MONTH) < Calendar.JUNE){
            return true;
        }else{
            return false;
        }
    }
    
    private int getTrienios(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        
        int diffInYears = fechaNominaCalendar.get(Calendar.YEAR) - fechaAltaCalendar.get(Calendar.YEAR);
        if(diffInYears%3 == 0 && fechaNominaCalendar.get(Calendar.MONTH) == Calendar.DECEMBER && fechaAltaCalendar.get(Calendar.MONTH) == Calendar.DECEMBER){
            diffInYears--;
        }        
        return diffInYears/3;
    }
    
    private int getTrieniosMes(Calendar fechaAltaCalendar, Calendar fechaNominaCalendar){
        
        int diffInYears = fechaNominaCalendar.get(Calendar.YEAR) - fechaAltaCalendar.get(Calendar.YEAR); 
        if ((fechaAltaCalendar.get(Calendar.MONTH) > fechaNominaCalendar.get(Calendar.MONTH)) || (diffInYears%3 == 0 && fechaNominaCalendar.get(Calendar.MONTH) == Calendar.DECEMBER && fechaAltaCalendar.get(Calendar.MONTH) == Calendar.DECEMBER)) {
            diffInYears--;
        }
        return diffInYears/3;
    }
    
    private double redondea(double valor){
        
        return (double) Math.round(valor*100)/100;
    }
	
}
