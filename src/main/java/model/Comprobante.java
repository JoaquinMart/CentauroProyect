package model;

public class Comprobante {
    private int id;
    private int cantidad;
    private String nombre;
    private double precio;

    public Comprobante(int id, int cantidad, String nombre, double precio) {
        this.id = id;
        this.cantidad = cantidad;
        this.nombre = nombre;
        this.precio = precio;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    @Override
    public String toString() { return (this.getCantidad() + " de " + this.getNombre() + " total de " + this.getPrecio()); }
}
