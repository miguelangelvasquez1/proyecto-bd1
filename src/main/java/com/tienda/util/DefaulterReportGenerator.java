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

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Título
        Paragraph title = new Paragraph("REPORTE DE CLIENTES MOROSOS")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
        document.add(title);

        // Resumen
        Paragraph summary = new Paragraph("Total clientes morosos: " + defaulters.size())
                .setFontSize(13)
                .setBold()
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(8)
                .setMarginBottom(20)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(summary);

        // Tabla
        float[] columns = {3, 2, 2, 3, 2, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columns));
        table.setWidth(UnitValue.createPercentValue(100));

        // Headers
        addHeader(table, "Cliente");
        addHeader(table, "Documento");
        addHeader(table, "Teléfono");
        addHeader(table, "Email");
        addHeader(table, "Deuda Total");
        addHeader(table, "Cuotas Vencidas");
        addHeader(table, "Último Pago");
        addHeader(table, "Días Mora");

        // Datos
        for (DefaulterClientDTO c : defaulters) {
            table.addCell(c.getClientName());
            table.addCell(c.getDocumentNumber());
            table.addCell(c.getPhoneNumber());
            table.addCell(c.getEmail() == null ? "N/A" : c.getEmail());
            table.addCell(currencyFormat.format(c.getTotalDebt()));
            table.addCell(String.valueOf(c.getOverdueQuotas()));
            table.addCell(c.getLastPaymentDate() != null ? dateFormat.format(c.getLastPaymentDate()) : "N/A");
            table.addCell(String.valueOf(c.getDaysPastDue()));
        }

        document.add(table);

        document.add(new Paragraph("Generado el: " + dateFormat.format(new Date()))
                .setFontSize(10)
                .setMarginTop(20)
                .setTextAlignment(TextAlignment.RIGHT));

        document.close();

        return new File(filePath);
    }

    private static void addHeader(Table table, String title) {
        table.addHeaderCell(
                new Cell()
                        .add(new Paragraph(title).setBold().setFontSize(10))
                        .setBackgroundColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
        );
    }
}
