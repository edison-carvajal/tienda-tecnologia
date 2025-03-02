package dominio;

import java.util.Date;

public class GarantiaExtendida {

    private Producto producto;
    private Date fechaSolicitudGarantia;
    public void setFechaSolicitudGarantia(Date fechaSolicitudGarantia) {
		this.fechaSolicitudGarantia = fechaSolicitudGarantia;
	}

	public void setFechaFinGarantia(Date fechaFinGarantia) {
		this.fechaFinGarantia = fechaFinGarantia;
	}

	public void setPrecioGarantia(double precioGarantia) {
		this.precioGarantia = precioGarantia;
	}

	private Date fechaFinGarantia;
    private double precioGarantia;
    private String nombreCliente;

    public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public GarantiaExtendida(Producto producto) {
        this.fechaSolicitudGarantia = new Date();
        this.producto = producto;
    }

    public GarantiaExtendida(Producto producto, Date fechaSolicitudGarantia, Date fechaFinGarantia,
            double precioGarantia, String nombreCliente) {

        this.producto = producto;
        this.fechaSolicitudGarantia = fechaSolicitudGarantia;
        this.fechaFinGarantia = fechaFinGarantia;
        this.precioGarantia = precioGarantia;
        this.nombreCliente = nombreCliente;
    }

    public Producto getProducto() {
        return producto;
    }

    public Date getFechaSolicitudGarantia() {
        return fechaSolicitudGarantia;
    }

    public Date getFechaFinGarantia() {
        return fechaFinGarantia;
    }

    public double getPrecioGarantia() {
        return precioGarantia;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

}
