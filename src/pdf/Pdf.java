/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdf;

import clases.Nomina;
import clases.Trabajadorbbdd;
import com.itextpdf.io.font.constants.StandardFonts;
import static com.itextpdf.kernel.colors.ColorConstants.RED;
import static com.itextpdf.kernel.colors.ColorConstants.YELLOW;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Micaela Pujol Higueras
 * @author Silvia Matilla García
 */
public class Pdf{
        
    public static void generarPDF(Nomina nomina, Trabajadorbbdd trabajadorbbdd) throws FileNotFoundException, IOException{
        String nombrePdf = trabajadorbbdd.getNifnie() + trabajadorbbdd.getNombreCompleto() + nomina.getMesNombre() + nomina.getAnio();
        if(nomina.getEsExtra()){
            nombrePdf = trabajadorbbdd.getNifnie() + trabajadorbbdd.getNombreCompleto() + nomina.getMesNombre() + nomina.getAnio() + "EXTRA";
        }
        PdfWriter writer = new PdfWriter("./resources/nominas/" + nombrePdf + ".pdf");
        PdfDocument pdf = new PdfDocument(writer);
        Document documento = new Document(pdf);
        
        PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
          
        double importe, retenciones, totalEmpresario, costeTrabajador;
        importe = (double) Math.round((nomina.getImporteSalarioMes()+nomina.getValorProrrateo()+nomina.getImporteComplementoMes()+nomina.getImporteTrienios())*100)/100;
        retenciones = (double) Math.round((nomina.getImporteSeguridadSocialTrabajador()+nomina.getImporteDesempleoTrabajador()+nomina.getImporteFormacionTrabajador()+nomina.getImporteIrpf())*100)/100;
        totalEmpresario = (double) Math.round((nomina.getImporteSeguridadSocialEmpresario()+nomina.getImporteDesempleoEmpresario()+nomina.getImporteFormacionEmpresario()+nomina.getImporteAccidentesTrabajoEmpresario()+nomina.getImporteFogasaempresario())*100)/100;
        costeTrabajador = (double) Math.round((importe+totalEmpresario)*100)/100;
        
        Table tabla = new Table(1);
        documento.add(tabla.addCell(trabajadorbbdd.getEmpresas().getNombre() + "\nCIF: " + trabajadorbbdd.getEmpresas().getCif()).setFont(font).setFont(bold).setFontSize(14));
        documento.add(new Paragraph("\n"));
        
        documento.add(new Paragraph("IBAN: " + trabajadorbbdd.getIban() + "\nBruto anual: " + nomina.getBrutoAnual() + "\nCategoría: " + trabajadorbbdd.getCategorias().getNombreCategoria() + "\nFecha de alta: " + trabajadorbbdd.getFechaAlta() + "\n").setFont(font).setFontSize(11));
        documento.add(new Paragraph("\n"));
        Table tabla2 = new Table(1);
        documento.add(tabla2.addCell(trabajadorbbdd.getNombreCompleto() + "\nDNI: " + trabajadorbbdd.getNifnie()).setFont(font).setFont(bold).setFontSize(14));

        if(nomina.getEsExtra()){
            documento.add(new Paragraph("\nNómina: Extra de " + nomina.getMesNombre().toLowerCase() + " de " + nomina.getAnio() + "\n").setFont(font).setFont(bold).setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        }
        else{
            documento.add(new Paragraph("\nNómina: " + nomina.getMesNombre() + " de " + nomina.getAnio() + "\n").setFont(font).setFont(bold).setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        }
        documento.add(new Paragraph("____________________________________________________________________________\n"));
        documento.add(new Paragraph("Salario base\t\t" + nomina.getImporteSalarioMes() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("Prorrateo\t\t" + nomina.getValorProrrateo() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("Complemento\t\t" + nomina.getImporteComplementoMes() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("Antigüedad\t\t" + nomina.getNumeroTrienios() + " trienios\t\t" + nomina.getImporteTrienios() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("Contingencias generales\t" + nomina.getSeguridadSocialTrabajador() + "% de " + nomina.getBaseEmpresario() + "\t\t" + nomina.getImporteSeguridadSocialTrabajador() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("Desempleo\t\t" + nomina.getDesempleoTrabajador() + "% de " + nomina.getBaseEmpresario() + "\t\t" + nomina.getImporteDesempleoTrabajador() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("Cuota formación\t\t" + nomina.getFormacionTrabajador() + "% de " + nomina.getBaseEmpresario() + "\t\t" + nomina.getImporteFormacionTrabajador() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("IRPF\t\t" + nomina.getIrpf() + "% de " + nomina.getBaseEmpresario() + "\t\t" + nomina.getImporteIrpf() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("______________________________________________________________________________\n"));
        documento.add(new Paragraph("Total deducciones\t\t" + retenciones + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("Total devengos\t\t" + importe + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("______________________________________________________________________________\n"));

        documento.add(new Paragraph("Líquido a percibir\t\t" + nomina.getLiquidoNomina()).setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
        documento.add(new Paragraph("______________________________________________________________________________\n"));
        if(nomina.getEsExtra()){
            documento.add(new Paragraph("Cálculo empresario: BASE\t\t" + 0.0).setFont(font).setFontSize(10));
        }
        else{
            documento.add(new Paragraph ("Cálculo empresario: BASE\t\t" + nomina.getBaseEmpresario()).setFont(font).setFontSize(10));
        }
        documento.add(new Paragraph("______________________________________________________________________________\n"));
        documento.add(new Paragraph("Contingencias comunes empresario " + nomina.getSeguridadSocialEmpresario() + "%\t\t" + nomina.getImporteSeguridadSocialEmpresario() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("Desempleo " + nomina.getDesempleoEmpresario() + "%\t\t" + nomina.getImporteDesempleoEmpresario() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("Formación " + nomina.getFormacionEmpresario() + "%\t\t" + nomina.getImporteFormacionEmpresario() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("Accidentes de trabajo " + nomina.getAccidentesTrabajoEmpresario() + "%\t\t" + nomina.getImporteAccidentesTrabajoEmpresario() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("FOGASA " + nomina.getFogasaempresario() + "%\t\t" + nomina.getImporteFogasaempresario() + "\n").setFont(font).setFontSize(10));
        documento.add(new Paragraph("______________________________________________________________________________\n"));
        documento.add(new Paragraph("Total empresario\t\t" + totalEmpresario + "\n").setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
        
        Table tabla3 = new Table(1);
        documento.add(tabla3.addCell("COSTE TOTAL TRABAJADOR:\t\t" + costeTrabajador + "\n").setFont(font).setFontSize(10).setFontColor(RED));
        
        documento.close();
    }
}