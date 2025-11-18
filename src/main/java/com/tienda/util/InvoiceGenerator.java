package com.tienda.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.tienda.model.Purchase;

import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class InvoiceGenerator {

    private static final NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    public static File generateInvoice(Purchase p) throws Exception {

        String home = System.getProperty("user.home");
        String path = home + File.separator + "Downloads" + File.separator +
                "Factura_" + p.getId() + ".pdf";

        File pdfFile = new File(path);

        PdfWriter writer = new PdfWriter(pdfFile);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        // Título
        Paragraph title = new Paragraph("FACTURA DE VENTA")
                .setBold().setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER);
        doc.add(title);

        doc.add(new Paragraph("Número de factura: " + p.getId()).setBold());
        doc.add(new Paragraph("Fecha: " + LocalDate.now()));
        doc.add(new Paragraph("\n"));

        // Info cliente
        doc.add(new Paragraph("Cliente: " + p.getClient().getName()));
        doc.add(new Paragraph("Vendedor: " + p.getUser().getName()));
        doc.add(new Paragraph("Tipo de venta: " + p.getSaleType()));
        doc.add(new Paragraph("\n"));

        // Tabla
        Table table = new Table(2);
        table.setWidth(UnitValue.createPercentValue(60));

        table.addCell(makeHeader("Subtotal"));
        table.addCell(format.format(p.getSubtotal()));

        table.addCell(makeHeader("IVA"));
        table.addCell(format.format(p.getIvaTotal()));

        table.addCell(makeHeader("TOTAL"));
        table.addCell(format.format(p.getTotal()));

        doc.add(table);

        doc.add(new Paragraph("\n\nGracias por su compra.")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic());

        doc.close();

        return pdfFile;
    }

    private static Cell makeHeader(String text) {
        return new Cell().add(new Paragraph(text))
                .setBold()
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }
}
