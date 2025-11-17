package com.tienda.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.tienda.model.dtos.DefaulterClientDTO;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DefaulterReportGenerator {

    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public static File generateReport(List<DefaulterClientDTO> defaulters) throws Exception {
        
        String userHome = System.getProperty("user.home");
        String downloadsPath = userHome + File.separator + "Downloads";
        String fileName = "Reporte_Morosos_" + System.currentTimeMillis() + ".pdf";
        String filePath = downloadsPath + File.separator + fileName;
        
        File pdfFile = new File(filePath);
        PdfWriter writer = new PdfWriter(pdfFile);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Título
        Paragraph title = new Paragraph("REPORTE DE CLIENTES MOROSOS")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Resumen
        Paragraph summary = new Paragraph("Total clientes morosos: " + defaulters.size())
                .setFontSize(14)
                .setBold()
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(summary);

        // Tabla
        float[] columnWidths = {3, 2, 2, 3, 2, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Headers
        addTableHeader(table, "Cliente");
        addTableHeader(table, "Documento");
        addTableHeader(table, "Teléfono");
        addTableHeader(table, "Email");
        addTableHeader(table, "Deuda Total");
        addTableHeader(table, "Cuotas Vencidas");
        addTableHeader(table, "Último Pago");
        addTableHeader(table, "Días Mora");

        // Datos
        for (DefaulterClientDTO client : defaulters) {
            table.addCell(new Cell().add(new Paragraph(client.getClientName())));
            table.addCell(new Cell().add(new Paragraph(client.getDocumentNumber())));
            table.addCell(new Cell().add(new Paragraph(client.getPhoneNumber())));
            table.addCell(new Cell().add(new Paragraph(client.getEmail() != null ? client.getEmail() : "N/A")));
            table.addCell(new Cell().add(new Paragraph(currencyFormat.format(client.getTotalDebt()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(client.getOverdueQuotas()))));
            table.addCell(new Cell().add(new Paragraph(
                    client.getLastPaymentDate() != null ? dateFormat.format(client.getLastPaymentDate()) : "N/A"
            )));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(client.getDaysPastDue()))));
        }

        document.add(table);

        // Footer
        Paragraph footer = new Paragraph("Generado el: " + dateFormat.format(new Date()))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20);
        document.add(footer);

        document.close();
        
        return pdfFile;
    }

    private static void addTableHeader(Table table, String headerText) {
        Cell header = new Cell()
                .add(new Paragraph(headerText).setBold().setFontSize(10))
                .setBackgroundColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(header);
    }
}
