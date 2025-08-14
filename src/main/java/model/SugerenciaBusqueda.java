package model;

public class SugerenciaBusqueda {
    private String nombre;
    private String razonSocial;
    private int id;
    private String tipo;

    public SugerenciaBusqueda(Cliente cliente) {
        this.nombre = cliente.getNombre();
        this.razonSocial = cliente.getRazonSocial();
        this.id = cliente.getId();
        this.tipo = "Cliente";
    }

    public SugerenciaBusqueda(Proveedor proveedor) {
        this.nombre = proveedor.getNombre();
        this.razonSocial = proveedor.getRazonSocial();
        this.id = proveedor.getId();
        this.tipo = "Proveedor";
    }

    public String getNombre() { return nombre; }
//    public String getRazonSocial() { return razonSocial; }
    public int getId() { return id; }
    public String getTipo() { return tipo; }
}