/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdf;

import clases.Empresas;
import clases.Nomina;
import clases.Trabajadorbbdd;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Micaela Pujol Higueras
 * @author Silvia Matilla García
 */
public class Pdf{
        
    public static void generarPDF(Nomina nomina) throws FileNotFoundException, IOException{
        
        Trabajadorbbdd trabajador = nomina.getTrabajadorbbdd();
        File carpetaNominas = new File("./resources/nominas"); // crea la carpeta si no existe
        carpetaNominas.mkdirs();
        
        String nombrePdf = trabajador.getNifnie() + trabajador.getNombre() + trabajador.getApellido1() + trabajador.getApellido2() + nomina.getMesNombre() + nomina.getAnio();
        if(nomina.getEsExtra()){
            nombrePdf = trabajador.getNifnie() + trabajador.getNombre() + trabajador.getApellido1() + trabajador.getApellido2() + nomina.getMesNombre() + nomina.getAnio() + "EXTRA";
        }
        String ruta = "./resources/nominas/" + nombrePdf + ".pdf";        
        PdfWriter writer = new PdfWriter(ruta);
        PdfDocument pdf = new PdfDocument(writer);
        Document documento = new Document(pdf);
                
        PdfFont negritaCursiva = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLDOBLIQUE);
        
        Table fila1 = new Table(2);
        fila1.setWidth(520);
        
        // cuadro empresa               
        Cell celdaEmpresa = new Cell();
        celdaEmpresa.setBorder(new SolidBorder(1));
        celdaEmpresa.setWidth(200);
        celdaEmpresa.setTextAlignment(TextAlignment.CENTER);
        celdaEmpresa.setVerticalAlignment(VerticalAlignment.MIDDLE);

        Empresas empresa = trabajador.getEmpresas(); 
        celdaEmpresa.add(new Paragraph(empresa.getNombre()));
        celdaEmpresa.add(new Paragraph("CIF: " + empresa.getCif()));
        celdaEmpresa.add(new Paragraph("Avenida de la facultad - 6"));
        celdaEmpresa.add(new Paragraph("24001 León"));
        fila1.addCell(celdaEmpresa);

        // datos trabajador
        Cell celdaDatosTrabajador = new Cell();
        celdaDatosTrabajador.setBorder(Border.NO_BORDER);
        celdaDatosTrabajador.setPadding(10);
        celdaDatosTrabajador.setTextAlignment(TextAlignment.RIGHT);
        
        celdaDatosTrabajador.add(new Paragraph("IBAN: " + trabajador.getIban()));
        celdaDatosTrabajador.add(new Paragraph("Bruto anual: " + nomina.getBrutoAnual()));
        celdaDatosTrabajador.add(new Paragraph("Categoría: " + trabajador.getCategorias().getNombreCategoria()));
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        celdaDatosTrabajador.add(new Paragraph("Fecha de alta: " + df.format(trabajador.getFechaAlta())));
        fila1.addCell(celdaDatosTrabajador);
        
        documento.add(fila1);
        
        Table fila2 = new Table(2);
        fila2.setWidth(520);
        
        // logo empresa        
        File file = new File("./resources/" + empresa.getNombre() + ".jpg");
        String imagen;
        if(file.exists())
            imagen = "./resources/" + empresa.getNombre() + ".jpg";
        else
            imagen = "./resources/TecnoLeonSL.jpg";
        Image img = new Image(ImageDataFactory.create(imagen));
        img.setBorder(Border.NO_BORDER);
        img.setPadding(5);
        img.scaleAbsolute(200,80);
        
        Cell logo = new Cell();
        logo.add(img);
        logo.setBorder(Border.NO_BORDER);
        logo.setPaddingTop(5);
        logo.setWidth(230);
        logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
        fila2.addCell(logo);             
        
        // cuadro trabajador
        Cell celdaTrabajador = new Cell();
        celdaTrabajador.setBorder(new SolidBorder(1));
        celdaTrabajador.setWidth(130);
        celdaTrabajador.setPadding(10);
        celdaTrabajador.setTextAlignment(TextAlignment.RIGHT);
        celdaTrabajador.setVerticalAlignment(VerticalAlignment.MIDDLE);
        
        celdaTrabajador.add(new Paragraph(trabajador.getNombreCompleto()));
        celdaTrabajador.add(new Paragraph("DNI: " + trabajador.getNifnie()));
        celdaTrabajador.add(new Paragraph("Avenida de la facultad - 6"));
        celdaTrabajador.add(new Paragraph("24001 León"));
        fila2.addCell(celdaTrabajador);
        
        documento.add(new Paragraph(""));
        documento.add(fila2);
        
        // titulo
        Paragraph titulo;
        if(nomina.getEsExtra())
            titulo = new Paragraph("\nNómina: Extra de " + nomina.getMesNombre().toLowerCase() + " de " + nomina.getAnio() + "\n");
        else
            titulo = new Paragraph("\nNómina: " + nomina.getMesNombre() + " de " + nomina.getAnio() + "\n");
        titulo.setTextAlignment(TextAlignment.CENTER);
        titulo.setFont(negritaCursiva);
        titulo.setFontSize(15);
        documento.add(titulo);
        documento.add(new LineSeparator(new SolidLine(1f)));
        
        // trabajador
        double importe, retenciones, totalEmpresario, costeTrabajador;
        importe = (double) Math.round((nomina.getImporteSalarioMes()+nomina.getValorProrrateo()+nomina.getImporteComplementoMes()+nomina.getImporteTrienios())*100)/100;
        retenciones = (double) Math.round((nomina.getImporteSeguridadSocialTrabajador()+nomina.getImporteDesempleoTrabajador()+nomina.getImporteFormacionTrabajador()+nomina.getImporteIrpf())*100)/100;
        totalEmpresario = (double) Math.round((nomina.getImporteSeguridadSocialEmpresario()+nomina.getImporteDesempleoEmpresario()+nomina.getImporteFormacionEmpresario()+nomina.getImporteAccidentesTrabajoEmpresario()+nomina.getImporteFogasaempresario())*100)/100;
        costeTrabajador = (double) Math.round((importe+totalEmpresario)*100)/100;
        
        Table tablaTrabajadorTitulos = new Table(UnitValue.createPercentArray(new float[]{2,1,1,1}));
        tablaTrabajadorTitulos.setBorder(Border.NO_BORDER);
        tablaTrabajadorTitulos.setWidth(520);
        Cell conceptos = new Cell();
        conceptos.add(new Paragraph("Conceptos").setFontSize(14));
        conceptos.setBorder(Border.NO_BORDER);
        conceptos.setTextAlignment(TextAlignment.LEFT);
        Cell cantidad = new Cell();
        cantidad.add(new Paragraph("Cantidad").setFontSize(14));
        cantidad.setBorder(Border.NO_BORDER);
        cantidad.setTextAlignment(TextAlignment.CENTER);
        Cell devengo = new Cell();
        devengo.add(new Paragraph("Devengo").setFontSize(14));
        devengo.setBorder(Border.NO_BORDER);
        devengo.setTextAlignment(TextAlignment.CENTER);
        Cell deduccion = new Cell();
        deduccion.add(new Paragraph("Deducción").setFontSize(14));
        deduccion.setBorder(Border.NO_BORDER);
        deduccion.setTextAlignment(TextAlignment.RIGHT);
        tablaTrabajadorTitulos.addCell(conceptos);
        tablaTrabajadorTitulos.addCell(cantidad);
        tablaTrabajadorTitulos.addCell(devengo);
        tablaTrabajadorTitulos.addCell(deduccion);
        documento.add(tablaTrabajadorTitulos);
        documento.add(new LineSeparator(new SolidLine(1f)));
        
        Table tablaTrabajador = new Table(UnitValue.createPercentArray(new float[]{2,1,1,1}));
        tablaTrabajador.setBorder(Border.NO_BORDER);
        tablaTrabajador.setWidth(520);
        
        // salario base
        Cell salarioBaseTitulo = new Cell();
        salarioBaseTitulo.add(new Paragraph("Salario base"));
        salarioBaseTitulo.setBorder(Border.NO_BORDER);
        salarioBaseTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell impDias = new Cell();
        impDias.add(new Paragraph("30 días"));
        impDias.setBorder(Border.NO_BORDER);
        impDias.setTextAlignment(TextAlignment.CENTER);
        Cell salarioBase = new Cell();
        salarioBase.add(new Paragraph("" + nomina.getImporteSalarioMes()));
        salarioBase.setBorder(Border.NO_BORDER);
        salarioBase.setTextAlignment(TextAlignment.CENTER);
        Cell celdaEnBlanco = new Cell();
        celdaEnBlanco.add(new Paragraph(""));
        celdaEnBlanco.setBorder(Border.NO_BORDER);
        celdaEnBlanco.setTextAlignment(TextAlignment.RIGHT);
        
        tablaTrabajador.addCell(salarioBaseTitulo);
        tablaTrabajador.addCell(impDias.clone(true));
        tablaTrabajador.addCell(salarioBase);
        tablaTrabajador.addCell(celdaEnBlanco.clone(true));
        
        // prorrateo
        Cell prorrateoTitulo = new Cell();
        prorrateoTitulo.add(new Paragraph("Prorrateo"));
        prorrateoTitulo.setBorder(Border.NO_BORDER);
        prorrateoTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell prorrateo = new Cell();
        prorrateo.add(new Paragraph("" + nomina.getValorProrrateo()));
        prorrateo.setBorder(Border.NO_BORDER);
        prorrateo.setTextAlignment(TextAlignment.CENTER);
        
        tablaTrabajador.addCell(prorrateoTitulo);
        tablaTrabajador.addCell(impDias.clone(true));
        tablaTrabajador.addCell(prorrateo);
        tablaTrabajador.addCell(celdaEnBlanco.clone(true));
        
        // complemento
        Cell complementoTitulo = new Cell();
        complementoTitulo.add(new Paragraph("Complemento"));
        complementoTitulo.setBorder(Border.NO_BORDER);
        complementoTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell complemento = new Cell();
        complemento.add(new Paragraph("" + nomina.getImporteComplementoMes()));
        complemento.setBorder(Border.NO_BORDER);
        complemento.setTextAlignment(TextAlignment.CENTER);
        
        tablaTrabajador.addCell(complementoTitulo);
        tablaTrabajador.addCell(impDias.clone(true));
        tablaTrabajador.addCell(complemento);
        tablaTrabajador.addCell(celdaEnBlanco.clone(true));
        
        // antigüedad
        Cell antiguedadTitulo = new Cell();
        antiguedadTitulo.add(new Paragraph("Antigüedad"));
        antiguedadTitulo.setBorder(Border.NO_BORDER);
        antiguedadTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell antiguedadCantidad = new Cell();
        antiguedadCantidad.add(new Paragraph(nomina.getNumeroTrienios() + " trienios"));
        antiguedadCantidad.setBorder(Border.NO_BORDER);
        antiguedadCantidad.setTextAlignment(TextAlignment.CENTER);
        Cell antiguedad = new Cell();
        antiguedad.add(new Paragraph("" + nomina.getImporteTrienios()));
        antiguedad.setBorder(Border.NO_BORDER);
        antiguedad.setTextAlignment(TextAlignment.CENTER);
        
        tablaTrabajador.addCell(antiguedadTitulo);
        tablaTrabajador.addCell(antiguedadCantidad);
        tablaTrabajador.addCell(antiguedad);
        tablaTrabajador.addCell(celdaEnBlanco.clone(true));
        
        // contingencias generales
        Cell contingenciasTitulo = new Cell();
        contingenciasTitulo.add(new Paragraph("Contingencias generales"));
        contingenciasTitulo.setBorder(Border.NO_BORDER);
        contingenciasTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell contingenciasCantidad = new Cell();
        contingenciasCantidad.add(new Paragraph(nomina.getSeguridadSocialTrabajador() + "% de " + nomina.getBaseEmpresario()));
        contingenciasCantidad.setBorder(Border.NO_BORDER);
        contingenciasCantidad.setTextAlignment(TextAlignment.CENTER);
        Cell contingencias = new Cell();
        contingencias.add(new Paragraph("" + nomina.getImporteSeguridadSocialTrabajador()));
        contingencias.setBorder(Border.NO_BORDER);
        contingencias.setTextAlignment(TextAlignment.RIGHT);
        
        tablaTrabajador.addCell(contingenciasTitulo);
        tablaTrabajador.addCell(contingenciasCantidad);
        tablaTrabajador.addCell(celdaEnBlanco.clone(true));
        tablaTrabajador.addCell(contingencias);
        
        // desempleo
        Cell desempleoTitulo = new Cell();
        desempleoTitulo.add(new Paragraph("Desempleo"));
        desempleoTitulo.setBorder(Border.NO_BORDER);
        desempleoTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell desempleoCantidad = new Cell();
        desempleoCantidad.add(new Paragraph(nomina.getDesempleoTrabajador() + "% de " + nomina.getBaseEmpresario()));
        desempleoCantidad.setBorder(Border.NO_BORDER);
        desempleoCantidad.setTextAlignment(TextAlignment.CENTER);
        Cell desempleo = new Cell();
        desempleo.add(new Paragraph("" + nomina.getImporteDesempleoTrabajador()));
        desempleo.setBorder(Border.NO_BORDER);
        desempleo.setTextAlignment(TextAlignment.RIGHT);
        
        tablaTrabajador.addCell(desempleoTitulo);
        tablaTrabajador.addCell(desempleoCantidad);
        tablaTrabajador.addCell(celdaEnBlanco.clone(true));
        tablaTrabajador.addCell(desempleo);
        
        // cuota formación
        Cell formacionTitulo = new Cell();
        formacionTitulo.add(new Paragraph("Cuota formación"));
        formacionTitulo.setBorder(Border.NO_BORDER);
        formacionTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell formacionCantidad = new Cell();
        formacionCantidad.add(new Paragraph(nomina.getFormacionTrabajador() + "% de " + nomina.getBaseEmpresario()));
        formacionCantidad.setBorder(Border.NO_BORDER);
        formacionCantidad.setTextAlignment(TextAlignment.CENTER);
        Cell formacion = new Cell();
        formacion.add(new Paragraph("" + nomina.getImporteFormacionTrabajador()));
        formacion.setBorder(Border.NO_BORDER);
        formacion.setTextAlignment(TextAlignment.RIGHT);
        
        tablaTrabajador.addCell(formacionTitulo);
        tablaTrabajador.addCell(formacionCantidad);
        tablaTrabajador.addCell(celdaEnBlanco.clone(true));
        tablaTrabajador.addCell(formacion);
        
        // irpf
        Cell irpfTitulo = new Cell();
        irpfTitulo.add(new Paragraph("IRPF"));
        irpfTitulo.setBorder(Border.NO_BORDER);
        irpfTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell irpfCantidad = new Cell();
        irpfCantidad.add(new Paragraph(nomina.getIrpf() + "% de " + nomina.getBaseEmpresario()));
        irpfCantidad.setBorder(Border.NO_BORDER);
        irpfCantidad.setTextAlignment(TextAlignment.CENTER);
        Cell irpf = new Cell();
        irpf.add(new Paragraph("" + nomina.getImporteIrpf()));
        irpf.setBorder(Border.NO_BORDER);
        irpf.setTextAlignment(TextAlignment.RIGHT);
        
        tablaTrabajador.addCell(irpfTitulo);
        tablaTrabajador.addCell(irpfCantidad);
        tablaTrabajador.addCell(celdaEnBlanco.clone(true));
        tablaTrabajador.addCell(irpf);
        
        documento.add(tablaTrabajador);
        documento.add(new LineSeparator(new SolidLine(1f)));
        
        // totales
        Table tablaTotales = new Table(UnitValue.createPercentArray(new float[]{2,1,1,1}));
        tablaTotales.setBorder(Border.NO_BORDER);
        tablaTotales.setWidth(520);
        
        Cell totalDeduccionesTitulo = new Cell();
        totalDeduccionesTitulo.add(new Paragraph("Total deducciones"));
        totalDeduccionesTitulo.setBorder(Border.NO_BORDER);
        totalDeduccionesTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell totalDeducciones = new Cell();
        totalDeducciones.add(new Paragraph("" + retenciones));
        totalDeducciones.setBorder(Border.NO_BORDER);
        totalDeducciones.setTextAlignment(TextAlignment.RIGHT);
        
        tablaTotales.addCell(totalDeduccionesTitulo);
        tablaTotales.addCell(celdaEnBlanco.clone(true));
        tablaTotales.addCell(celdaEnBlanco.clone(true));
        tablaTotales.addCell(totalDeducciones);
        
        Cell totalDevengosTitulo = new Cell();
        totalDevengosTitulo.add(new Paragraph("Total devengos"));
        totalDevengosTitulo.setBorder(Border.NO_BORDER);
        totalDevengosTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell totalDevengos = new Cell();
        totalDevengos.add(new Paragraph("" + importe));
        totalDevengos.setBorder(Border.NO_BORDER);
        totalDevengos.setTextAlignment(TextAlignment.CENTER);
        
        tablaTotales.addCell(totalDeduccionesTitulo);
        tablaTotales.addCell(celdaEnBlanco.clone(true));
        tablaTotales.addCell(celdaEnBlanco.clone(true));
        tablaTotales.addCell(totalDeducciones);
        
        documento.add(tablaTotales);
        documento.add(new LineSeparator(new SolidLine(1f)));
        
        // líquido
        Table tablaLiquido = new Table(UnitValue.createPercentArray(new float[]{2,1,1,1}));
        tablaLiquido.setBorder(Border.NO_BORDER);
        tablaLiquido.setWidth(520);
        
        Cell liquidoTitulo = new Cell();
        liquidoTitulo.add(new Paragraph("Líquido a percibir"));
        liquidoTitulo.setBorder(Border.NO_BORDER);
        liquidoTitulo.setTextAlignment(TextAlignment.RIGHT);
        Cell liquido = new Cell();
        liquido.add(new Paragraph("" + retenciones));
        liquido.setBorder(Border.NO_BORDER);
        liquido.setTextAlignment(TextAlignment.RIGHT);
        
        tablaLiquido.addCell(celdaEnBlanco);
        tablaLiquido.addCell(liquidoTitulo);
        tablaLiquido.addCell(celdaEnBlanco.clone(true));
        tablaLiquido.addCell(liquido);
        
        documento.add(tablaLiquido);
        documento.add(new Paragraph("\n"));
        
        // empresario
        SolidLine separador = new SolidLine(1f);
        separador.setColor(ColorConstants.GRAY);
        documento.add(new LineSeparator(separador));
        
        Table tablaTituloEmp = new Table(UnitValue.createPercentArray(new float[]{4,1}));
        tablaTituloEmp.setBorder(Border.NO_BORDER);
        tablaTituloEmp.setWidth(520);
        
        double baseEmpresario = 0.0;
        if(!nomina.getEsExtra())
            baseEmpresario = nomina.getBaseEmpresario();
        
        Cell baseTitulo = new Cell();
        baseTitulo.add(new Paragraph("Cálculo empresario: BASE").setFontColor(ColorConstants.GRAY));
        baseTitulo.setBorder(Border.NO_BORDER);
        baseTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell base = new Cell();
        base.add(new Paragraph("" + baseEmpresario).setFontColor(ColorConstants.GRAY));
        base.setBorder(Border.NO_BORDER);
        base.setTextAlignment(TextAlignment.RIGHT);
        
        tablaTituloEmp.addCell(baseTitulo);
        tablaTituloEmp.addCell(base);
        
        documento.add(tablaTituloEmp);
        documento.add(new LineSeparator(separador));
        documento.add(new Paragraph("\n"));
        
        Table tablaEmpresario = new Table(UnitValue.createPercentArray(new float[]{4,1}));
        tablaEmpresario.setBorder(Border.NO_BORDER);
        tablaEmpresario.setWidth(520);
        
        Cell contingenciasEmpTitulo = new Cell();
        contingenciasEmpTitulo.add(new Paragraph("Contingencias comunes empresario " + nomina.getSeguridadSocialEmpresario() + "%").setFontColor(ColorConstants.GRAY));
        contingenciasEmpTitulo.setBorder(Border.NO_BORDER);
        contingenciasEmpTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell contingenciasEmp = new Cell();
        contingenciasEmp.add(new Paragraph("" + nomina.getImporteSeguridadSocialEmpresario()).setFontColor(ColorConstants.GRAY));
        contingenciasEmp.setBorder(Border.NO_BORDER);
        contingenciasEmp.setTextAlignment(TextAlignment.RIGHT);
        
        tablaEmpresario.addCell(contingenciasEmpTitulo);
        tablaEmpresario.addCell(contingenciasEmp);
        
        Cell desempleoEmpTitulo = new Cell();
        desempleoEmpTitulo.add(new Paragraph("Desempleo " + nomina.getDesempleoEmpresario() + "%").setFontColor(ColorConstants.GRAY));
        desempleoEmpTitulo.setBorder(Border.NO_BORDER);
        desempleoEmpTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell desempleoEmp = new Cell();
        desempleoEmp.add(new Paragraph("" + nomina.getImporteDesempleoEmpresario()).setFontColor(ColorConstants.GRAY));
        desempleoEmp.setBorder(Border.NO_BORDER);
        desempleoEmp.setTextAlignment(TextAlignment.RIGHT);
        
        tablaEmpresario.addCell(desempleoEmpTitulo);
        tablaEmpresario.addCell(desempleoEmp);
                
        Cell formacionEmpTitulo = new Cell();
        formacionEmpTitulo.add(new Paragraph("Formación " + nomina.getFormacionEmpresario()+ "%").setFontColor(ColorConstants.GRAY));
        formacionEmpTitulo.setBorder(Border.NO_BORDER);
        formacionEmpTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell formacionEmp = new Cell();
        formacionEmp.add(new Paragraph("" + nomina.getImporteFormacionEmpresario()).setFontColor(ColorConstants.GRAY));
        formacionEmp.setBorder(Border.NO_BORDER);
        formacionEmp.setTextAlignment(TextAlignment.RIGHT);
        
        tablaEmpresario.addCell(formacionEmpTitulo);
        tablaEmpresario.addCell(formacionEmp);
        
        Cell accidentesTitulo = new Cell();
        accidentesTitulo.add(new Paragraph("Accidentes de trabajo " + nomina.getAccidentesTrabajoEmpresario()+ "%").setFontColor(ColorConstants.GRAY));
        accidentesTitulo.setBorder(Border.NO_BORDER);
        accidentesTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell accidentes = new Cell();
        accidentes.add(new Paragraph("" + nomina.getImporteAccidentesTrabajoEmpresario()).setFontColor(ColorConstants.GRAY));
        accidentes.setBorder(Border.NO_BORDER);
        accidentes.setTextAlignment(TextAlignment.RIGHT);
        
        tablaEmpresario.addCell(accidentesTitulo);
        tablaEmpresario.addCell(accidentes);
        
        Cell fogasaTitulo = new Cell();
        fogasaTitulo.add(new Paragraph("FOGASA " + nomina.getFogasaempresario()+ "%").setFontColor(ColorConstants.GRAY));
        fogasaTitulo.setBorder(Border.NO_BORDER);
        fogasaTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell fogasa = new Cell();
        fogasa.add(new Paragraph("" + nomina.getImporteFogasaempresario()).setFontColor(ColorConstants.GRAY));
        fogasa.setBorder(Border.NO_BORDER);
        fogasa.setTextAlignment(TextAlignment.RIGHT);
        
        tablaEmpresario.addCell(fogasaTitulo);
        tablaEmpresario.addCell(fogasa);
        
        documento.add(tablaEmpresario);
        documento.add(new LineSeparator(separador));
        
        Table tablaTotalEmp = new Table(UnitValue.createPercentArray(new float[]{4,1}));
        tablaTotalEmp.setBorder(Border.NO_BORDER);
        tablaTotalEmp.setWidth(520);
        
        Cell totalEmpTitulo = new Cell();
        totalEmpTitulo.add(new Paragraph("Total empresario").setFontColor(ColorConstants.GRAY));
        totalEmpTitulo.setBorder(Border.NO_BORDER);
        totalEmpTitulo.setTextAlignment(TextAlignment.LEFT);
        Cell totalEmp = new Cell();
        totalEmp.add(new Paragraph("" + totalEmpresario).setFontColor(ColorConstants.GRAY));
        totalEmp.setBorder(Border.NO_BORDER);
        totalEmp.setTextAlignment(TextAlignment.RIGHT);
        
        tablaTotalEmp.addCell(totalEmpTitulo);
        tablaTotalEmp.addCell(totalEmp);
        
        documento.add(tablaTotalEmp);
        documento.add(new Paragraph("\n"));
        
        Table tablaCosteTrabajador = new Table(2);
        tablaCosteTrabajador.setWidth(520);
        tablaCosteTrabajador.setBorder(new SolidBorder(2));
        
        Cell costeTrabajadorTitulo = new Cell();
        costeTrabajadorTitulo.add(new Paragraph("COSTE TOTAL TRABAJADOR:").setFontColor(ColorConstants.RED));
        costeTrabajadorTitulo.setBorder(Border.NO_BORDER);
        costeTrabajadorTitulo.setTextAlignment(TextAlignment.LEFT);
        costeTrabajadorTitulo.setVerticalAlignment(VerticalAlignment.MIDDLE);
        Cell costeTotalTrabajador = new Cell();
        costeTotalTrabajador.add(new Paragraph("" + costeTrabajador).setFontColor(ColorConstants.RED));
        costeTotalTrabajador.setBorder(Border.NO_BORDER);
        costeTotalTrabajador.setTextAlignment(TextAlignment.RIGHT);
        costeTotalTrabajador.setVerticalAlignment(VerticalAlignment.MIDDLE);
        
        tablaCosteTrabajador.addCell((costeTrabajadorTitulo));
        tablaCosteTrabajador.addCell((costeTotalTrabajador));
        
        documento.add(tablaCosteTrabajador);
              
        documento.close();
    }
        
}