// ---------------------------------------------------
//
// -> pintar( texto:Texto ) -> bool
//
// Pinta el texto en el elemento de la pantalla.
//
// ---------------------------------------------------
function pintar( texto ) {

	var elemento = document.getElementById( "textoCodigo" );
	if ( ! elemento ) {
		return false;
	}

	elemento.textContent = texto;
	return true;
}

// ---------------------------------------------------
//
// -> cargarAplicacion() -> bool
//   (consulta el último código y lo pinta en pantalla)
//
// ---------------------------------------------------
function cargarAplicacion() {

	// mientras se consulta
	pintar( "Consultando..." );

	// consultamos el último código al servidor
	consultarUltimoCodigo( function( err, res ) {
		if ( err ) {
			pintar( "No hay código" );
		} else {
			pintar( "Código: " + res.valor );
		}
	} );

	return true;
}
