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
import com.tienda.model.dtos.SaleReportDTO;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IVAReportGenerator {

    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public static File generateReport(List<SaleReportDTO> sales, double totalIVA, 
                                     Date startDate, Date endDate) throws Exception {
        
        // Crear archivo en el directorio de descargas del usuario
        String userHome = System.getProperty("user.home");
        String downloadsPath = userHome + File.separator + "Downloads";
        String fileName = "Reporte_IVA_" + System.currentTimeMillis() + ".pdf";
        String filePath = downloadsPath + File.separator + fileName;
        
        File pdfFile = new File(filePath);
        PdfWriter writer = new PdfWriter(pdfFile);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Título
        Paragraph title = new Paragraph("IVA REPORT - DIAN")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        // Periodo
        Paragraph period = new Paragraph("Period from: " + dateFormat.format(startDate) + 
                                        " to " + dateFormat.format(endDate))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(period);

        // Resumen
        Paragraph summary = new Paragraph("Total IVA Tax: " + currencyFormat.format(totalIVA))
                .setFontSize(14)
                .setBold()
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(summary);

        // Tabla de ventas
        float[] columnWidths = {1, 3, 1, 2, 2, 2, 2, 2, 3};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Headers
        addTableHeader(table, "Id");
        addTableHeader(table, "Product");
        addTableHeader(table, "Quant.");
        addTableHeader(table, "Date");
        addTableHeader(table, "Type");
        addTableHeader(table, "Subtotal");
        addTableHeader(table, "IVA");
        addTableHeader(table, "Total");
        addTableHeader(table, "Client");

        // Datos
        for (SaleReportDTO sale : sales) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(sale.getSaleId()))));
            table.addCell(new Cell().add(new Paragraph(sale.getProductName())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(sale.getQuantity()))));
            table.addCell(new Cell().add(new Paragraph(dateFormat.format(sale.getDate()))));
            table.addCell(new Cell().add(new Paragraph(sale.getSaleType())));
            table.addCell(new Cell().add(new Paragraph(currencyFormat.format(sale.getSubtotal()))));
            table.addCell(new Cell().add(new Paragraph(currencyFormat.format(sale.getIvaTotal()))));
            table.addCell(new Cell().add(new Paragraph(currencyFormat.format(sale.getTotal()))));
            table.addCell(new Cell().add(new Paragraph(sale.getClientName())));
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
                .add(new Paragraph(headerText).setBold())
                .setBackgroundColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(header);
    }
}
