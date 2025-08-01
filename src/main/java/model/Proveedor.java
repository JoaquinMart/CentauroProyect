package model;

import java.util.ArrayList;
import java.util.List;

public class Proveedor {
    private int id;
    private String nombre;
    private String razonSocial;
    private String domicilio;
    private String localidad;
    private String codigoPostal;
    private String telefono;
    private String CUIT;
    private String categoria;
    private String contacto;
    private List<CuentaCorriente> cuentaCorrientes;

    public Proveedor(String nombre, String razonSocial, String domicilio, String localidad, String codigoPostal,
                     String telefono, String CUIT, String categoria, String contacto) {
        this.nombre = nombre;
        this.razonSocial = razonSocial;
        this.domicilio = domicilio;
        this.localidad = localidad;
        this.codigoPostal = codigoPostal;
        this.telefono = telefono;
        this.CUIT = CUIT;
        this.contacto = contacto;
        this.cuentaCorrientes = new ArrayList<CuentaCorriente>();
    }

    // Getters y Setters
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

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public List<CuentaCorriente> getCuentaCorrientes() { return cuentaCorrientes; }
    public void agregarCuentaCorriente(CuentaCorriente cuentaCorriente) { this.cuentaCorrientes.add(cuentaCorriente); }

    @Override
    public String toString() {
        return "model.Proveedor " +
                nombre + '\n' +
                " • Razon Social: " + razonSocial + '\n' +
                " • CUIT: " + CUIT + '\n';
    }
}
