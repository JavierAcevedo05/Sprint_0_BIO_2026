// ---------------------------------------------------
//
// versión fake de una función de la lógica
//
// -> consultarUltimoCodigo() -> (valor:Entero, fecha:Fecha) | error:Texto
//
// (valor:Entero, fecha:Fecha) | error:Texto : devuelto via callback( err, res )
//
// ---------------------------------------------------

// ---------------------------------------------------
//
// -> crearXHR() -> XMLHttpRequest
//
// Se deja en el objeto global para que los tests puedan
// sustituirla por un doble y no depender de la red.
//
// ---------------------------------------------------
globalThis.crearXHR = function() {
	return new XMLHttpRequest();
};

// ---------------------------------------------------
//
// -> consultarUltimoCodigo( cb ) -> nada
//
// Hace un GET al servidor y traduce la respuesta:
//   valor + fecha correctos  -> cb( null, resultado )
//   error del servidor       -> cb( error, null )
//   fallo HTTP / no JSON     -> cb( textoError, null )
//
// ---------------------------------------------------
function consultarUltimoCodigo( cb ) {

	var xhr = crearXHR();

	xhr.onreadystatechange = function() {

		// esperamos a que termine
		if ( this.readyState != 4 ) {
			return;
		}

		// ¿fallo HTTP?
		if ( this.status != 200 ) {
			cb( "fallo HTTP: " + this.status, null );
			return;
		}

		// ¿la respuesta es JSON?
		var resultado;
		try {
			resultado = JSON.parse( this.responseText );
		} catch ( e ) {
			cb( "la respuesta no es JSON", null );
			return;
		}

		// ¿el servidor devuelve error?
		if ( resultado.error != 0 ) {
			cb( resultado.error, null );
			return;
		}

		// todo correcto
		cb( null, resultado );
	};

	// llamamos *remotamente* al fichero consultarCodigo.php
	xhr.open( "GET", "../rest/consultarCodigo.php", true );
	xhr.send();

} // ()

// ---------------------------------------------------
// Soporte para poder ejecutar los tests con Node.
// ---------------------------------------------------
if ( typeof module !== "undefined" && module.exports ) {
	module.exports.consultarUltimoCodigo = consultarUltimoCodigo;
}
