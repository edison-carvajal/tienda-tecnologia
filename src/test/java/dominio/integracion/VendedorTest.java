package dominio.integracion;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dominio.Vendedor;
import dominio.Producto;
import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioProducto;
import dominio.repositorio.RepositorioGarantiaExtendida;
import persistencia.sistema.SistemaDePersistencia;
import testdatabuilder.ProductoTestDataBuilder;

public class VendedorTest {

	private static final String COMPUTADOR_LENOVO = "Computador Lenovo";

	private static final String COMPUTADOR_DELL = "Computador Dell";

	private static final String CODIGO_TRES_VOCALES = "a56E54i43D";

	private static final String NOMBRE_CLIENTE = "Fernando Estrada";

	private SistemaDePersistencia sistemaPersistencia;

	private RepositorioProducto repositorioProducto;
	private RepositorioGarantiaExtendida repositorioGarantia;

	@Before
	public void setUp() {

		sistemaPersistencia = new SistemaDePersistencia();

		repositorioProducto = sistemaPersistencia.obtenerRepositorioProductos();
		repositorioGarantia = sistemaPersistencia.obtenerRepositorioGarantia();

		sistemaPersistencia.iniciar();
	}

	@After
	public void tearDown() {
		sistemaPersistencia.terminar();
	}

	@Test
	public void generarGarantiaTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);

		// assert
		Assert.assertTrue(vendedor.tieneGarantia(producto.getCodigo()));
		Assert.assertNotNull(repositorioGarantia.obtenerProductoConGarantiaPorCodigo(producto.getCodigo()));

	}

	@Test
	public void productoNoTieneGarantiaTest() {

		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();

		repositorioProducto.agregar(producto);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);
		
		vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);
		try {

			vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);
			fail();

		} catch (GarantiaExtendidaException e) {
			// assert
			Assert.assertEquals(Vendedor.EL_PRODUCTO_TIENE_GARANTIA, e.getMessage());
		}
	}

	@Test
	public void productoNoCuentaConGarantia() {
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_DELL).conCodigo(CODIGO_TRES_VOCALES)
				.build();

		repositorioProducto.agregar(producto);

		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);
		// act
		try{
			vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);
			fail();
		}catch (GarantiaExtendidaException e) {
			// assert
			Assert.assertEquals(Vendedor.EL_PRODUCTO_NO_TIENE_GARANTIA, e.getMessage());
		}
		
	}
	
	@Test
	public void generarGarantiaSinNombre(){
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		try{
			// act
			vendedor.generarGarantia(producto.getCodigo(),null);
			fail();
		}catch(GarantiaExtendidaException e){
			//assert
			Assert.assertEquals(Vendedor.DEBE_INGRESAR_EL_CODIGO_DE_PRODUCTO_Y_NOMBRE_CLIENTE,e.getMessage());
		}
	}
	
}
