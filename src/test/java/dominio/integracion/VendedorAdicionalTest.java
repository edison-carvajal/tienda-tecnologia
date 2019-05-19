package dominio.integracion;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dominio.repositorio.RepositorioGarantiaExtendida;
import dominio.repositorio.RepositorioProducto;
import persistencia.sistema.SistemaDePersistencia;
import testdatabuilder.ProductoTestDataBuilder;
import dominio.GarantiaExtendida;
import dominio.Producto;
import dominio.Vendedor;
import dominio.excepcion.GarantiaExtendidaException;
import java.text.FieldPosition;

public class VendedorAdicionalTest {
	
	private static final String COMPUTADOR_LENOVO = "Computador Lenovo";
	private static final String COMPUTADOR_LENOVO2 = "Computador Lenovo 2";
	private static final String NOMBRE_CLIENTE = "Luis Garcia";
	private static final String CODIGO1= "F01TSA0150";
	private static final String CODIGO2= "G01TSA0150";
	private static final String CODIGO5= "U01ISE0151";
	private static final String CODIGO11= "E01ESE0151";
	private static final double PRECIO1 = 780000.0;
	private static final double PRECIO7= 450000.0 ;
	
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
	
	/*
	Proposito: Cambiar la fecha del sistema a la fecha que viene en la cadena.
	Precondicion: strDateToSet esta en el formato "dd/mm/yyyy". Ejemplo: "16/08/2018"   
	*/
	private void setSystemDate(String strDateToSet)
	{
		try {
			TimeUnit.MILLISECONDS.sleep(200);

			Runtime.getRuntime().exec("cmd /C date " + strDateToSet);

			TimeUnit.MILLISECONDS.sleep(200);
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}				
	}
	
	/*
	Proposito: Convierte la fecha unaFecha a un String en el formato "dd/mm/yyyy"
	Valor de retorno: Un String en el formato. "dd/mm/yyyy" Ejemplo "16/08/2018"
	*/
	public String formatearDate(Date unaFecha) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		StringBuffer buffer = new StringBuffer();
		FieldPosition position = new FieldPosition(0);
		buffer = format.format(unaFecha, buffer, position);
		return buffer.toString();
	}
	
    /*
    Prueba 01:
    El dia de solicitud de la garantia extendida no es lunes. 
    Fecha de finalización de la garantia no es ni domingo ni festivo ni lunes.
    Dia de solicitud de la garantia = "16/08/2018" es jueves
    Precio del producto es mayor a $500.000. Precio es $780.000. 
    - Resultado: 
    Dia de finalizacion de la garantia = "06/04/2019" es sabado.
    garantiaExtendida.getPrecioGarantia() es $156.000 (20% del precio del producto)
    */
	@Test
	public void prueba01GenerarGarantiaTest() {

		Date fechaActual = new Date();
		setSystemDate("16/08/2018");
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);
				
		Date fechaFinGarantia = garantiaExtendida.getFechaFinGarantia();
		
		// Vamos a validar que fecha finalizacion de garantia es "06/04/2019"
		String strFechaFinGarantia = formatearDate(fechaFinGarantia);
		
		String strFechaActual = formatearDate(fechaActual);
		setSystemDate(strFechaActual);
				
		Assert.assertEquals(strFechaFinGarantia,"06/04/2019");

		Assert.assertEquals(garantiaExtendida.getPrecioGarantia(), 156000 , 0.001);
	}

    /*
    Prueba 02:
    El dia de compra de la garantia extendida no es lunes. 
    Fecha de finalización de la garantia no es ni domingo ni lunes ni festivo.
    Dia de solicitud de la garantia = "17/08/2018" es viernes 
    Precio del producto es mayor a $500.000. Precio es $780.000.
    - Resultado:
    Dia de finalizacion de la garantia = "07/04/2019"(domingo) se corre a "08/04/2019" (lunes)
    garantiaExtendida.getPrecioGarantia() es $156.000 (20% del precio del producto)    
    */
	@Test
	public void prueba02GenerarGarantiaTest() {

		Date fechaActual = new Date();
		setSystemDate("17/08/2018");
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO2).conCodigo(CODIGO2).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);
				
		Date fechaFinGarantia = garantiaExtendida.getFechaFinGarantia();

		// Vamos a validar que fecha es "06/04/2019"
		String strFechaFinGarantia = formatearDate(fechaFinGarantia);

		String strFechaActual = formatearDate(fechaActual);
		setSystemDate(strFechaActual);
		
		Assert.assertEquals(strFechaFinGarantia,"08/04/2019");
		Assert.assertEquals(garantiaExtendida.getPrecioGarantia(), 156000 , 0.001);	
	}

    /*
    Prueba 03:
    El dia en que finaliza la garantia cae un festivo. Entonces la finalizacion de la garantia se corre para el 
    siguiente dia habil. Precio superior a $500.000 ($780.000). Hay un corrimiento de 2 dias.  
    Fecha de solicitud de la garantia: 28 Ago 2018. 
    - Resultado: 
    Numero dias de garantia: 200. 
    Final de garantia cae 18/Abr/2019 (jueves santo) entonces se corre para el 20/Abr/2019.
    garantiaExtendida.getPrecioGarantia() es $156.000 (20% del precio del producto)  
    */
	@Test
	public void prueba03GenerarGarantiaTest() {
		Date fechaActual = new Date();
		setSystemDate("28/08/2018");
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);

		Date fechaFinGarantia = garantiaExtendida.getFechaFinGarantia();
		
		// Vamos a validar que fecha es "06/04/2019"
		String strFechaFinGarantia = formatearDate(fechaFinGarantia);

		String strFechaActual = formatearDate(fechaActual);
		setSystemDate(strFechaActual);
				
		Assert.assertEquals(strFechaFinGarantia,"20/04/2019");
		Assert.assertEquals(garantiaExtendida.getPrecioGarantia(), 156000 , 0.001);
	}	

    /*
    Prueba 04:
    El dia en que finaliza la garantia cae un festivo. Entonces la finalizacion de la garantia se corre para el 
    siguiente dia habil. Hay un corrimiento de 1 dia. 
    Precio producto superior a $500.000 ($780000).  
    Fecha de solicitud de la garantia: 29 Ago 2018. 
    - Resultado: 
    Numero dias de garantia: 200. 
    Final de garantia cae 19/Abr/2019 (viernes santo) entonces se corre para el 20/Abr/2019. 
    garantiaExtendida.getPrecioGarantia() es $156.000 (20% del precio del producto) 
    */
	@Test
	public void prueba04GenerarGarantiaTest() {
		Date fechaActual = new Date();
		setSystemDate("29/08/2018");
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);

		Date fechaFinGarantia = garantiaExtendida.getFechaFinGarantia();
		
		// Vamos a validar que fecha es "06/04/2019"
		String strFechaFinGarantia = formatearDate(fechaFinGarantia);

		String strFechaActual = formatearDate(fechaActual);
		setSystemDate(strFechaActual);
				
		Assert.assertEquals(strFechaFinGarantia,"20/04/2019");
		Assert.assertEquals(garantiaExtendida.getPrecioGarantia(), 156000 , 0.001);
	}	

    /*
    Prueba 05:
    El dia de compra de la garantia extendida es lunes.
	Precio producto superior a $500.000 ($780000).
	Fecha solicitud de la garantia:  13/Ago/2018 (Lunes) .  
	- Resultado:
	Numero dias de garantia: 200.
	Fecha de fin de garantia: 4/Abr/2019
	garantiaExtendida.getPrecioGarantia() es $156.000 (20% del precio del producto)
    */
	@Test
	public void prueba05GenerarGarantiaTest() {
		Date fechaActual = new Date();
		setSystemDate("13/08/2018");
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);

		Date fechaFinGarantia = garantiaExtendida.getFechaFinGarantia();
		
		// Vamos a validar que fecha es "06/04/2019"
		String strFechaFinGarantia = formatearDate(fechaFinGarantia);

		String strFechaActual = formatearDate(fechaActual);
		setSystemDate(strFechaActual);
				
		Assert.assertEquals(strFechaFinGarantia,"04/04/2019");
		Assert.assertEquals(garantiaExtendida.getPrecioGarantia(), 156000 , 0.001);
	}	

    /*
    Prueba 06:
    El codigo del producto tiene 3 vocales diferentes.
    codigo = "U01ISE0151"
    - Resultado:
    Se produce excepcion GarantiaExtendidaException con el mensaje "Este producto no cuenta con garantía extendida"
    */
	@Test
	public void prueba06GenerarGarantiaTest() {
		Date fechaActual = new Date();
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO5).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		try {		
		   GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);
		   fail();
		
	    } catch (GarantiaExtendidaException e) {
		    // assert
		    Assert.assertEquals(Vendedor.PRODUCTO_NO_CUENTA_CON_GARANTIA, e.getMessage());
	    }
	}	


    /*
    Prueba 07:
    El codigo del producto tiene 3 vocales iguales.
    codigo = "E01ESE0151"
    - Resultado:
    Se produce excepcion GarantiaExtendidaException con el mensaje "Este producto no cuenta con garantía extendida"
    */
	@Test
	public void prueba07GenerarGarantiaTest() {
		Date fechaActual = new Date();
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO11).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		try {		
		   GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);
		   fail();
		
	    } catch (GarantiaExtendidaException e) {
		    // assert
			System.out.println("prueba06GenerarGarantiaTest:GarantiaExtendidaException: mensaje  >>" +e.getMessage());
		    Assert.assertEquals(Vendedor.PRODUCTO_NO_CUENTA_CON_GARANTIA, e.getMessage());
	    }
	}	

    /* 
    Prueba 08:
    Se saca una primera garantia. Después se trata de sacar una segunda garantía. 
    Precio producto superior a $500.000. Precio producto $780.000.
    - Resultado:
    Se genera excepcion GarantiaExtendidaException con mensaje "El producto ya cuenta con una garantia extendida". 
    */
	@Test
	public void prueba08GenerarGarantiaTest() {

		Date fechaActual = new Date();
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).conPrecio(PRECIO1).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);

		Date fechaFinGarantia = garantiaExtendida.getFechaFinGarantia();
		
		String strFechaFinGarantia = formatearDate(fechaFinGarantia);
		
		try {		
			garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);
			fail();
			
		} catch (GarantiaExtendidaException e) {

			// assert
			Assert.assertEquals(Vendedor.EL_PRODUCTO_TIENE_GARANTIA, e.getMessage());
		}		
	}

    /*
    Prueba 09:
    El dia de compra de la garantia extendida no es lunes. 
    El ultimo dia de validez de la garantia no es ni domingo ni lunes ni festivo. 
    El precio del producto esta por debajo de $500.000.    
    Precio producto: $450.000 
    - Resultado:
    Fecha de solicitud de garantia es 12/Ago/2018 (martes). Numero dias de garantia: 100.
    Fecha de fin de garantia: 07/Dic/2018 (viernes)
    Valor garantia: $45.000
    */
	@Test
	public void prueba09GenerarGarantiaTest() {

		Date fechaActual = new Date();
		setSystemDate("12/08/2018");
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).conPrecio(PRECIO7).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);

		Date fechaFinGarantia = garantiaExtendida.getFechaFinGarantia();
		
		// Vamos a validar que fecha es "07/12/2018"
		String strFechaFinGarantia = formatearDate(fechaFinGarantia);

		String strFechaActual = formatearDate(fechaActual);
		setSystemDate(strFechaActual);
				
		Assert.assertEquals(strFechaFinGarantia,"07/12/2018");
		Assert.assertEquals(garantiaExtendida.getPrecioGarantia(), 45000 , 0.001);
	}

    /*
    Prueba 10:
    El dia que finaliza la garantía es domingo y el siguiente lunes no es festivo.
    Precio producto: 450000
    Fecha actual:  15/Ago/2018. Numero dias de garantia: 100
    - Resultado:
    Fecha de fin de garantia: 9/Dic/2018 -> 10/Dic/2018
    Valor garantia: $45.000
    */
	@Test
	public void prueba10GenerarGarantiaTest() {

		Date fechaActual = new Date();
		setSystemDate("15/08/2018");
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).conPrecio(PRECIO7).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);

		Date fechaFinGarantia = garantiaExtendida.getFechaFinGarantia();
		
		// Vamos a validar que fecha es "06/04/2019"
		String strFechaFinGarantia = formatearDate(fechaFinGarantia);

		String strFechaActual = formatearDate(fechaActual);
		setSystemDate(strFechaActual);
				
		Assert.assertEquals(strFechaFinGarantia,"10/12/2018");
		Assert.assertEquals(garantiaExtendida.getPrecioGarantia(), 45000 , 0.001);
	}

    /*
    Prueba 11:
    El dia en que finaliza la garantia cae un festivo. 
    Entonces la finalizacion de la garantia se corre 1 dia para el siguiente dia habil.
    Precio Producto inferior a $500.000. Precio producto: $450.000.
    Fecha actual: 23 Dic 2018. 
    - Resultado:    
    Numero dias de garantia: 100. 
    Final de garantia cae 19/Abr/2019 (viernes santo) entonces se corre para el 20/Abr/2019.
    Valor garantia: $45.000
    */
	@Test
	public void prueba11GenerarGarantiaTest() {

		Date fechaActual = new Date();
		setSystemDate("23/12/2018");
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).conPrecio(PRECIO7).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);

		Date fechaFinGarantia = garantiaExtendida.getFechaFinGarantia();
		
		// Vamos a validar que fecha es "20/04/2019"
		String strFechaFinGarantia = formatearDate(fechaFinGarantia);

		String strFechaActual = formatearDate(fechaActual);
		setSystemDate(strFechaActual);
				
		Assert.assertEquals(strFechaFinGarantia,"20/04/2019");
		Assert.assertEquals(garantiaExtendida.getPrecioGarantia(), 45000 , 0.001);
	}

    /*
    Prueba 12:
    El dia en que finaliza la garantia cae un festivo. 
    Entonces la finalizacion de la garantia se corre 2 dias para el siguiente dia habil.
    Precio Producto inferior a $500.000. Precio producto: $450.000
    Fecha solicitud garantia: 22/Dic/2018 
    - Resultado:
    Numero dias de garantia: 100. 
    Final de garantia cae 18/Abr/2019 (jueves santo) entonces se corre para el 20/Abr/2019.
    Valor garantia: $45.000
    */
	@Test
	public void prueba12GenerarGarantiaTest() {

		Date fechaActual = new Date();
		setSystemDate("22/12/2018");
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).conPrecio(PRECIO7).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);

		Date fechaFinGarantia = garantiaExtendida.getFechaFinGarantia();
		
		// Vamos a validar que fecha es "20/04/2019"
		String strFechaFinGarantia = formatearDate(fechaFinGarantia);

		String strFechaActual = formatearDate(fechaActual);
		setSystemDate(strFechaActual);
				
		Assert.assertEquals(strFechaFinGarantia,"20/04/2019");
		Assert.assertEquals(garantiaExtendida.getPrecioGarantia(), 45000 , 0.01);
	}

    /*
    Prueba 13:
    El dia de compra de la garantia extendida es lunes.    
    Precio producto: 450000 
    Fecha actual:  13/Ago/2018 (Lunes) . 
    - Resultado:    
    Numero dias de garantia: 100.
    Fecha de fin de garantia: 10/Dic/2018
    Valor garantia: $45.000
    */
	@Test
	public void prueba13GenerarGarantiaTest() {

		Date fechaActual = new Date();
		setSystemDate("13/08/2018");
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).conPrecio(PRECIO7).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);

		Date fechaFinGarantia = garantiaExtendida.getFechaFinGarantia();
		
		// Vamos a validar que fecha es "06/04/2019"
		String strFechaFinGarantia = formatearDate(fechaFinGarantia);

		String strFechaActual = formatearDate(fechaActual);
		setSystemDate(strFechaActual);
				
		Assert.assertEquals(strFechaFinGarantia,"10/12/2018");
		Assert.assertEquals(garantiaExtendida.getPrecioGarantia(), 45000 , 0.01);
	}

    /*
    Prueba 14:
    Se saca una primera garantia. Después se trata de sacar una segunda garantía. 
    Precio producto $450.000. 
    - Resultado:    
    Se genera excepcion GarantiaExtendidaException con mensaje "El producto ya cuenta con una garantia extendida".
    */
	@Test
	public void prueba14GenerarGarantiaTest() {

		Date fechaActual = new Date();
		
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).conPrecio(PRECIO7).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		GarantiaExtendida garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);
		
		try {		
			garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),NOMBRE_CLIENTE);
			fail();
			
		} 
		catch (GarantiaExtendidaException e) {
			// assert
			Assert.assertEquals(Vendedor.EL_PRODUCTO_TIENE_GARANTIA, e.getMessage());
		}		
	}

    /*
    Prueba 15:
    Se intenta generar una garantia con el codigo de producto con valor ""
    - Resultado: 
    La invocacion al metodo vendedor.generarGarantia() arroja una excepcion GarantiaExtendidaException.
    Esta excepcion tiene como mensaje "No se puede generar una garantia sin el codigo del producto o sin el nombre del cliente"  
    */
	@Test
	public void prueba15GenerarGarantiaTest() {

		//Date fechaActual = new Date();
		//setSystemDate("16/08/2018");
		GarantiaExtendida garantiaExtendida = null;
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		try {
		   garantiaExtendida = vendedor.generarGarantia("",NOMBRE_CLIENTE);
		   fail();
		}
		catch (GarantiaExtendidaException e) {
			System.out.println("prueba15GenerarGarantiaTest: mensaje >>" +  e.getMessage() );
			Assert.assertEquals(Vendedor.FALTA_COD_PRODUCTO_NOM_CLIENTE, e.getMessage());
		}		
	}

    /*
    Prueba 16:
    Se intenta generar una garantia con el nombre de cliente con valor ""
    - Resultado: 
    La invocacion al metodo vendedor.generarGarantia() arroja una excepcion GarantiaExtendidaException.
    Esta excepcion tiene como mensaje "No se puede generar una garantia sin el codigo del producto o sin el nombre del cliente"  
    */
	@Test
	public void prueba16GenerarGarantiaTest() {

		//Date fechaActual = new Date();
		//setSystemDate("16/08/2018");
		GarantiaExtendida garantiaExtendida = null;
		// arrange
		Producto producto = new ProductoTestDataBuilder().conNombre(COMPUTADOR_LENOVO).conCodigo(CODIGO1).build();
		repositorioProducto.agregar(producto);
		Vendedor vendedor = new Vendedor(repositorioProducto, repositorioGarantia);

		// act
		try {
		   garantiaExtendida = vendedor.generarGarantia(producto.getCodigo(),"");
		   fail();
		}
		catch (GarantiaExtendidaException e) {
			System.out.println("prueba15GenerarGarantiaTest: mensaje >>" +  e.getMessage() );
			Assert.assertEquals(Vendedor.FALTA_COD_PRODUCTO_NOM_CLIENTE, e.getMessage());
		}		
	}
	
}
