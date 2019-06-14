package dominio;

import dominio.repositorio.RepositorioProducto;
import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioGarantiaExtendida;

public class Vendedor {

	public static final String EL_PRODUCTO_TIENE_GARANTIA = "El producto ya cuenta con una garantia extendida";

	private static final String EL_PRODUCTO_NO_EXISTE = "El producto no existe";

	private RepositorioProducto repositorioProducto;
	private RepositorioGarantiaExtendida repositorioGarantia;

	public Vendedor(RepositorioProducto repositorioProducto, RepositorioGarantiaExtendida repositorioGarantia) {
		this.repositorioProducto = repositorioProducto;
		this.repositorioGarantia = repositorioGarantia;

	}

	public void generarGarantia(String codigo) {

		Producto producto;

		if (tieneGarantia(codigo)) {
			throw new GarantiaExtendidaException(EL_PRODUCTO_TIENE_GARANTIA);
		} else {

			producto = repositorioProducto.obtenerPorCodigo(codigo);
			if (producto == null) {
				throw new GarantiaExtendidaException(EL_PRODUCTO_NO_EXISTE);
			} else {
				GarantiaExtendida garantia = new GarantiaExtendida(producto);
				repositorioGarantia.agregar(garantia);
			}
		}

	}

	public boolean tieneGarantia(String codigo) {
    	
    	if(!codigo.isEmpty()){
    		Producto producto = repositorioGarantia.obtenerProductoConGarantiaPorCodigo(codigo);
    		if(producto != null){
    			return true;
    		}
    	}
    	return false;
    }

}
