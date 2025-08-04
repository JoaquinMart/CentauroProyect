package services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import model.Cliente;
import model.CuentaCorriente;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class ImprimirPdfService {

    private final PdfFont FONT_NORMAL;
    private final PdfFont FONT_BOLD;

    public ImprimirPdfService() throws IOException {
        FONT_NORMAL = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        FONT_BOLD = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
    }

    public void generateClientesDeudoresPdf(List<Cliente> clientesDeudores, String outputPath) throws IOException {
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4.rotate());
        document.setMargins(20, 20, 20, 20);

        document.add(new Paragraph("Listado de Clientes Deudores")
                .setFont(FONT_BOLD)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));

        float[] columnWidths = {0.5f, 2f, 2f, 1.5f, 1.5f, 2f, 1.5f, 1.5f, 1f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
        table.setMarginBottom(10);

        DeviceRgb headerBgColor = new DeviceRgb(52, 152, 219);
        table.addHeaderCell(new Cell().add(new Paragraph("ID").setFont(FONT_BOLD).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
        table.addHeaderCell(new Cell().add(new Paragraph("Nombre").setFont(FONT_BOLD).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
        table.addHeaderCell(new Cell().add(new Paragraph("Razón Social").setFont(FONT_BOLD).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
        table.addHeaderCell(new Cell().add(new Paragraph("CUIT").setFont(FONT_BOLD).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
        table.addHeaderCell(new Cell().add(new Paragraph("Teléfono").setFont(FONT_BOLD).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
        table.addHeaderCell(new Cell().add(new Paragraph("Localidad").setFont(FONT_BOLD).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
        table.addHeaderCell(new Cell().add(new Paragraph("Condición").setFont(FONT_BOLD).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
        table.addHeaderCell(new Cell().add(new Paragraph("Proveedor").setFont(FONT_BOLD).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
        table.addHeaderCell(new Cell().add(new Paragraph("Saldo").setFont(FONT_BOLD).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.RIGHT).setPadding(5));

        for (Cliente cliente : clientesDeudores) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(cliente.getId())).setFont(FONT_NORMAL).setFontSize(9)).setTextAlignment(TextAlignment.CENTER).setPadding(5));
            table.addCell(new Cell().add(new Paragraph(cliente.getNombre()).setFont(FONT_NORMAL).setFontSize(9)).setTextAlignment(TextAlignment.LEFT).setPadding(5));
            table.addCell(new Cell().add(new Paragraph(cliente.getRazonSocial()).setFont(FONT_NORMAL).setFontSize(9)).setTextAlignment(TextAlignment.LEFT).setPadding(5));
            table.addCell(new Cell().add(new Paragraph(cliente.getCUIT()).setFont(FONT_NORMAL).setFontSize(9)).setTextAlignment(TextAlignment.CENTER).setPadding(5));
            table.addCell(new Cell().add(new Paragraph(cliente.getTelefono()).setFont(FONT_NORMAL).setFontSize(9)).setTextAlignment(TextAlignment.CENTER).setPadding(5));
            table.addCell(new Cell().add(new Paragraph(cliente.getLocalidad()).setFont(FONT_NORMAL).setFontSize(9)).setTextAlignment(TextAlignment.LEFT).setPadding(5));
            table.addCell(new Cell().add(new Paragraph(cliente.getCondicion()).setFont(FONT_NORMAL).setFontSize(9)).setTextAlignment(TextAlignment.LEFT).setPadding(5));
            table.addCell(new Cell().add(new Paragraph(cliente.getProveedor()).setFont(FONT_NORMAL).setFontSize(9)).setTextAlignment(TextAlignment.LEFT).setPadding(5));

            double saldo = 0;
            List<CuentaCorriente> cuentas = cliente.getCuentaCorrientes();
            if (cuentas != null && !cuentas.isEmpty()) {
                CuentaCorriente ultima = cuentas.stream()
                        .max(Comparator.comparing(CuentaCorriente::getFecha))
                        .orElse(null);
                if (ultima != null) {
                    saldo = ultima.getSaldo();
                }
            }
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f", saldo)).setFont(FONT_BOLD).setFontSize(9)).setTextAlignment(TextAlignment.RIGHT).setPadding(5));
        }

        document.add(table);

        document.add(new Paragraph(
                "Reporte generado el: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " a las " +
                        java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .setFont(FONT_NORMAL)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10));

        document.close();
    }
}