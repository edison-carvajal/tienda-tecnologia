package dominio;

import dominio.repositorio.RepositorioProducto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioGarantiaExtendida;

public class Vendedor {

	public static final String EL_PRODUCTO_TIENE_GARANTIA = "El producto ya cuenta con una garantia extendida";

	private static final String EL_PRODUCTO_NO_EXISTE = "El producto no existe";

	public static final String EL_PRODUCTO_NO_TIENE_GARANTIA = "Este producto no cuenta con garantía extendida";

	public static final String DEBE_INGRESAR_EL_CODIGO_DE_PRODUCTO_Y_NOMBRE_CLIENTE = "Debe ingresar el codigo de producto y el nombre de cliente para generar la garantia";

	private RepositorioProducto repositorioProducto;
	private RepositorioGarantiaExtendida repositorioGarantia;

	public Vendedor(RepositorioProducto repositorioProducto, RepositorioGarantiaExtendida repositorioGarantia) {
		this.repositorioProducto = repositorioProducto;
		this.repositorioGarantia = repositorioGarantia;

	}

	public void generarGarantia(String codigo, String nombreCliente) {
		
		if(codigo == null || nombreCliente == null){
			throw new GarantiaExtendidaException(DEBE_INGRESAR_EL_CODIGO_DE_PRODUCTO_Y_NOMBRE_CLIENTE);
		}

		Producto producto;

		if (tieneGarantia(codigo)) {
			throw new GarantiaExtendidaException(EL_PRODUCTO_TIENE_GARANTIA);
		} else if(!cuentaConGarantia(codigo)){
			throw new GarantiaExtendidaException(EL_PRODUCTO_NO_TIENE_GARANTIA);
		}else {

			producto = repositorioProducto.obtenerPorCodigo(codigo);
			if (producto == null) {
				throw new GarantiaExtendidaException(EL_PRODUCTO_NO_EXISTE);
			} else {
				GarantiaExtendida garantia = new GarantiaExtendida(producto);
				garantia.setNombreCliente(nombreCliente);
				repositorioGarantia.agregar(garantia);
			}
		}

	}

	private boolean cuentaConGarantia(String codigo) {
		if(!codigo.isEmpty()){
			String regex= "[AEIOU]{1}";
			Pattern p = Pattern.compile(regex);
		    Matcher m = p.matcher(codigo.toUpperCase());
		    int count = 0;

		    while(m.find()) {
		    	count++;
		    }
		    
			if(count>=3){
				return false;
			}
		}
		return true;
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
