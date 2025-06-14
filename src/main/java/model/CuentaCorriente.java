package model;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class CuentaCorriente {
    private int id;
    private Integer clienteId;
    private Integer proveedorId;
    private LocalDate fecha;
    private String tipo;
    private String comprobante;
    private List<Comprobante> comprobantes;
    private double venta;
    private double monto;
    private double saldo;
    private String observacion;

    // Constructor sin id
    public CuentaCorriente(LocalDate fecha, String tipo, String comprobante, double venta, double monto, double saldo, String observacion) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.comprobante = comprobante;
        this.monto = monto;
        this.comprobantes = new ArrayList<>();
        this.venta = venta;
        this.saldo = saldo;
        this.observacion = observacion;
    }

    // Constructor
    public CuentaCorriente(int id, LocalDate fecha, String tipo, String comprobante, double venta, double monto, double saldo, String observacion) {
        this.id = id;
        this.fecha = fecha;
        this.tipo = tipo;
        this.comprobante = comprobante;
        this.monto = monto;
        this.comprobantes = new ArrayList<>();
        this.venta = venta;
        this.saldo = saldo;
        this.observacion = observacion;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }

    public Integer getProveedorId() { return proveedorId; }
    public void setProveedorId(Integer proveedorId) { this.proveedorId = proveedorId; }

    public double getMonto() {
        return monto;
    }
    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getComprobante() {return comprobante;}
    public void setComprobante(String comprobante) {this.comprobante = comprobante;}

    public List<Comprobante> getComprobantes() { return comprobantes; }
    public void addComprobante(Comprobante comprobante) { this.comprobantes.add(comprobante); }
    public void setComprobantes(List<Comprobante> comprobantes) { this.comprobantes = comprobantes; }

    public double getVenta() { return venta; }
    public void setVenta(double venta) { this.venta = venta; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    @Override
    public String toString() {
        StringBuilder comprobantesStr = new StringBuilder();
        for (Comprobante c : comprobantes) {
            comprobantesStr.append("\n    - ").append(c.toString());
        }

        return  fecha +
                " | Tipo: " + tipo +
                " | Monto: " + monto +
                " | Comprobantes: " + comprobantes.size() +
                " | Venta: " + venta +
                " | Saldo: " + saldo +
                " | Observacion: " + observacion +
                comprobantesStr.toString();
    }

}
