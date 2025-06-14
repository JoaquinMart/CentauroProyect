package model;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Cliente {
    private int id;
    private String nombre;
    private String razonSocial;// Comparte
    private String domicilio;// Comparte
    private String localidad;// Comparte
    private String codigoPostal;// Comparte
    private String telefono; // Comparte
    private String CUIT;// Comparte
    private String condicion;
    private LocalDate fechaAlta;
    private String proveedor;
    private List<String> freezer;
    private List<Comodato> comodato;
    private List<CuentaCorriente> cuentaCorrientes;
    // private List<Mercaderia> mercaderia;

    // Constructor
    public Cliente(int ID, String nombre, String razonSocial, String domicilio, String localidad, String codigoPostal,
                   String telefono, String CUIT, String condicion, LocalDate fechaAlta, String proveedor) {
        this.id = ID;
        this.nombre = nombre;
        this.razonSocial = razonSocial;
        this.domicilio = domicilio;
        this.localidad = localidad;
        this.codigoPostal = codigoPostal;
        this.telefono = telefono;
        this.CUIT = CUIT;
        this.condicion = condicion;
        this.fechaAlta = fechaAlta;
        this.proveedor = proveedor;
        this.freezer = new ArrayList<>();
        this.comodato = new ArrayList<>();
        this.cuentaCorrientes = new ArrayList<>();
    }

    // Constructor
    public Cliente(String nombre, String razonSocial, String domicilio, String localidad, String codigoPostal,
                   String telefono, String CUIT, String condicion, LocalDate fechaAlta, String proveedor) {
        this.nombre = nombre;
        this.razonSocial = razonSocial;
        this.domicilio = domicilio;
        this.localidad = localidad;
        this.codigoPostal = codigoPostal;
        this.telefono = telefono;
        this.CUIT = CUIT;
        this.condicion = condicion;
        this.fechaAlta = fechaAlta;
        this.proveedor = proveedor;
        this.freezer = new ArrayList<>();
        this.comodato = new ArrayList<>();
        this.cuentaCorrientes = new ArrayList<>();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCUIT() { return CUIT; }
    public void setCUIT(String CUIT) { this.CUIT = CUIT; }

    public String getCondicion() { return condicion; }
    public void setCondicion(String condicion) { this.condicion = condicion; }

    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public List<CuentaCorriente> getCuentaCorrientes() { return cuentaCorrientes; }
    public void agregarCuentaCorriente(CuentaCorriente cuentaCorriente) { this.cuentaCorrientes.add(cuentaCorriente); }
    public void setCuentaCorrientes(List<CuentaCorriente> cuentas) { this.cuentaCorrientes = cuentas; }

    public List<String> getFreezer() { return freezer; }
    public void setFreezer(List<String> freezer) { this.freezer = freezer; }

    public List<Comodato> getComodato() { return comodato; }
    public void setComodato(List<Comodato> comodato) { this.comodato = comodato; }

    @Override
    public String toString() {
        return "Cliente: " + nombre + ", Razon Social: " + razonSocial  + ", CUIT: " + CUIT;
    }
}
