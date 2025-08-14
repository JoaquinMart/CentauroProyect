package services;

import dao.*;
import model.*;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.colors.ColorConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReportService {

    private final ClienteDAO clienteDAO;
    private final CuentaCorrienteDAO cuentaCorrienteDAO;
    private final ProveedorDAO proveedorDAO;

    public PdfReportService(ClienteDAO clienteDAO, CuentaCorrienteDAO cuentaCorrienteDAO, ProveedorDAO proveedorDAO) {
        this.clienteDAO = clienteDAO;
        this.cuentaCorrienteDAO = cuentaCorrienteDAO;
        this.proveedorDAO = proveedorDAO;
    }

    // Método que genera el reporte de las cuentas corrientes de un cliente.
    public void generateCuentaCorrientePdf(List<CuentaCorriente> movimientos, Cliente cliente, LocalDate fechaInicio, LocalDate fechaFin, String outputPath) throws IOException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4.rotate());

        PdfFont FONT_NORMAL = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont FONT_BOLD = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        document.setMargins(20, 20, 20, 20);

        // -- Título y cabecera del documento --
        document.add(new Paragraph("Reporte de Cuenta Corriente")
                .setFont(FONT_BOLD)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        document.add(new Paragraph("Cliente: " + cliente.getNombre() + " (Razón Social: " + cliente.getRazonSocial() + ")")
                .setFont(FONT_BOLD)
                .setFontSize(12)
                .setMarginBottom(5));

        document.add(new Paragraph(
                "CUIT: " + (cliente.getCUIT() != null ? cliente.getCUIT() : "N/A") +
                        " | Domicilio: " + (cliente.getDomicilio() != null ? cliente.getDomicilio() : "N/A") +
                        " | Localidad: " + (cliente.getLocalidad() != null ? cliente.getLocalidad() : "N/A"))
                .setFont(FONT_NORMAL)
                .setFontSize(9)
                .setMarginBottom(5));

        document.add(new Paragraph(
                "Período: " + fechaInicio.format(dateFormatter) + " - " + fechaFin.format(dateFormatter))
                .setFont(FONT_NORMAL)
                .setFontSize(9)
                .setMarginBottom(15));

        // -- Tabla de movimientos --
        Table table = new Table(UnitValue.createPercentArray(new float[]{1.2f, 0.8f, 1.5f, 1f, 1f, 1.2f, 2.5f}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);

        // -- Cabecera de la tabla --
        String[] headers = {"Fecha", "Tipo", "Comprobante", "Venta", "Monto", "Saldo", "Observación"};
        for (String header : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(header)
                            .setFont(FONT_BOLD)
                            .setFontSize(10)
                            .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(new DeviceRgb(52, 152, 219))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5));
        }

        //  -- Filas de datos --
        for (CuentaCorriente cc : movimientos) {
            table.addCell(new Cell().add(new Paragraph(cc.getFecha().format(dateFormatter))
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cc.getTipo())
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cc.getComprobante())
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", cc.getVenta()))
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", cc.getMonto()))
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", cc.getSaldo()))
                    .setFont(FONT_BOLD)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cc.getObservacion())
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
        }

        document.add(table);

        // -- Pie de página --
        document.add(new Paragraph(
                "Reporte generado el: " + LocalDate.now().format(dateFormatter) + " a las " +
                        java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .setFont(FONT_NORMAL)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10));

        document.close();
    }

    // Método que genera el reporte de todas las facturas de todos los proveedores.
    public void generateProveedoresFacturasPdf(String outputPath, LocalDate fechaInicio, LocalDate fechaFin) throws IOException, SQLException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        PdfFont FONT_NORMAL = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont FONT_BOLD = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont FONT_ITALIC = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        document.setMargins(30, 30, 30, 30);
        document.add(new Paragraph("Reporte de Facturas por Proveedor")
                .setFont(FONT_BOLD)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // -- Añado el rango de fechas al título del reporte --
        document.add(new Paragraph(
                "Período: " + fechaInicio.format(dateFormatter) + " - " + fechaFin.format(dateFormatter))
                .setFont(FONT_NORMAL)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));

        List<Proveedor> proveedores = proveedorDAO.obtenerTodosLosProveedores();

        if (proveedores.isEmpty()) {
            document.add(new Paragraph("No se encontraron proveedores para generar el reporte de facturas.")
                    .setFont(FONT_NORMAL)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));
        } else {
            for (Proveedor proveedor : proveedores) {
                document.add(new Paragraph("PROVEEDOR: " + proveedor.getNombre() + " (CUIT: " + (proveedor.getCUIT() != null ? proveedor.getCUIT() : "N/A") + ")")
                        .setFont(FONT_BOLD)
                        .setFontSize(14)
                        .setMarginTop(20)
                        .setMarginBottom(10)
                        .setTextAlignment(TextAlignment.LEFT));

                List<CuentaCorriente> facturas = cuentaCorrienteDAO.obtenerMovimientosPorProveedorYTipoYFechas(proveedor.getId(), "Factura", fechaInicio, fechaFin);

                if (facturas.isEmpty()) {
                    document.add(new Paragraph("  - Sin facturas registradas para este proveedor en el período seleccionado.")
                            .setFont(FONT_ITALIC)
                            .setFontSize(10)
                            .setMarginLeft(20)
                            .setMarginBottom(10));
                } else {
                    Table facturasTable = new Table(UnitValue.createPercentArray(new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 2f}));
                    facturasTable.setWidth(UnitValue.createPercentValue(100));
                    facturasTable.setMarginLeft(20);
                    facturasTable.setMarginBottom(15);

                    String[] facturaHeaders = {"Fecha", "N°Factura", "Tipo", "Neto", "IVA", "Otros", "Monto", "Saldo", "Observación"};
                    for (String header : facturaHeaders) {
                        facturasTable.addHeaderCell(new Cell().add(new Paragraph(header)
                                        .setFont(FONT_BOLD)
                                        .setFontSize(9)
                                        .setFontColor(ColorConstants.WHITE))
                                .setBackgroundColor(new DeviceRgb(70, 130, 180))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setPadding(4));
                    }

                    for (CuentaCorriente factura : facturas) {
                        facturasTable.addCell(new Cell().add(new Paragraph(factura.getFecha().format(dateFormatter))
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(factura.getComprobante())
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(factura.getTipo())
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getNeto()))
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getIva()))
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getOtros()))
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getMonto()))
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getSaldo()))
                                .setFont(FONT_BOLD)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(factura.getObservacion())
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                    }
                    document.add(facturasTable);
                }
            }
        }

        document.add(new Paragraph(
                "Reporte de Facturas por Proveedor generado el: " + LocalDate.now().format(dateFormatter) + " a las " +
                        java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        )
                .setFont(FONT_NORMAL)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20));

        document.close();
    }

    // Método que genera el reporte de todos los remitos del tipo seleccionado, de todos los proveedores en un periodo.
    public void generateProveedorReportPdf(String outputPath, LocalDate fechaInicio, LocalDate fechaFin, String nombre) throws IOException, SQLException {
        ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
        List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesDeProveedoresPorFechasYOrdenados(fechaInicio, fechaFin, nombre);

        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4.rotate());

        // Lógica de iText 7 para generar el PDF.
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        document.setMargins(20, 20, 20, 20);

        document.add(new Paragraph("Reporte de Comprobantes de Proveedores")
                .setFont(fontBold)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        document.add(new Paragraph("Período: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFont(fontNormal)
                .setFontSize(9)
                .setMarginBottom(15));

        Table table = new Table(UnitValue.createPercentArray(new float[]{3f, 1f, 2f}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);

        String[] headers = {"Nombre", "Cantidad", "Precio Unitario"};
        for (String header : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(header)
                            .setFont(fontBold)
                            .setFontSize(10)
                            .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(new DeviceRgb(52, 152, 219))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5));
        }
        for (Comprobante comprobante : comprobantes) {
            table.addCell(new Cell().add(new Paragraph(comprobante.getNombre())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(comprobante.getCantidad()))));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f", comprobante.getPrecio()))));
        }
        document.add(table);
        document.close();
    }
}