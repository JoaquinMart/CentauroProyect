// CLASE ENTERA NO SE USA
package model;

public class Comodato {
    private int numero;
    private String fecha;
    private String titular;
    private String descripcion;
    private boolean retirado;
    private String fechaRetirado;

    public Comodato(int numero, String fecha, String titular, String descripcion) {
        this.numero = numero;
        this.fecha = fecha;
        this.titular = titular;
        this.descripcion = descripcion;
        this.retirado = false;
        this.fechaRetirado = "No se retiro todavia";
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setRetirado() {
        this.retirado = true;
    }

    public void setFechaRetirado(String fechaRetirado) {
        this.fechaRetirado = fechaRetirado;
    }

    public int getNumero() {
        return numero;
    }

    public String getFecha() {
        return fecha;
    }

    public String getTitular() {
        return titular;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isRetirado() {
        return retirado;
    }

    public String getFechaRetirado() {
        return fechaRetirado;
    }

}
