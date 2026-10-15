// ----------------------------------------------------------
//
// Tests de la lógica fake del navegador
// (src/frontend_business_logic/consultarUltimoCodigo.js)
//
// Ejecutar con:
//   node tests/test_logica_fake_web.js
//
// Sustituye crearXHR() por un doble que devuelve respuestas
// preparadas, para no depender de la red.
//
// ----------------------------------------------------------

var logica = require("../src/frontend_business_logic/consultarUltimoCodigo.js");

var testsCorrectos = 0;
var testsTotal = 0;

// ----------------------------------------------------------
//
// nombre:Texto, condicion:VoF, motivo:Texto -> test() -> nada
//
// Imprime OK o FALLO y lleva el recuento final.
//
// ----------------------------------------------------------
function test( nombre, condicion, motivo ) {
	testsTotal++;
	if ( condicion ) {
		testsCorrectos++;
		console.log( "OK: " + nombre );
	} else {
		console.log( "FALLO: " + nombre + ( motivo ? " - " + motivo : "" ) );
	}
}

// ----------------------------------------------------------
//
// status:Entero, texto:Texto -> crearXHRDoble() -> objeto
//
// Doble de XMLHttpRequest: al enviar, responde de inmediato
// con el código HTTP y el cuerpo que le hayamos dado.
//
// ----------------------------------------------------------
function crearXHRDoble( status, texto ) {
	return {
		readyState: 0,
		status: 0,
		responseText: "",
		onreadystatechange: null,
		open: function( metodo, url, asincrono ) { /* nada */ },
		send: function() {
			this.readyState = 4;
			this.status = status;
			this.responseText = texto;
			if ( this.onreadystatechange ) {
				this.onreadystatechange();
			}
		}
	};
}

// ----------------------------------------------------------
// 1. obtención de código (el doble responde con un valor)
// ----------------------------------------------------------
globalThis.crearXHR = function() {
	return crearXHRDoble( 200, '{"valor":1234,"fecha":"2026-10-13 12:00:00","error":0}' );
};

var err1 = "sin respuesta";
var res1 = null;
logica.consultarUltimoCodigo( function( err, res ) {
	err1 = err;
	res1 = res;
} );

test( "obtención de código",
	err1 === null && res1 !== null && res1.valor === 1234,
	"no devolvió el valor esperado" );

// ----------------------------------------------------------
// 2. ausencia de código (el doble responde con error)
// ----------------------------------------------------------
globalThis.crearXHR = function() {
	return crearXHRDoble( 200, '{"error":"no hay códigos guardados"}' );
};

var err2 = null;
var res2 = "sin respuesta";
logica.consultarUltimoCodigo( function( err, res ) {
	err2 = err;
	res2 = res;
} );

test( "ausencia de código",
	err2 === "no hay códigos guardados" && res2 === null,
	"no devolvió el error esperado" );

// ----------------------------------------------------------
// recuento final
// ----------------------------------------------------------
console.log( "\n" + testsCorrectos + "/" + testsTotal + " tests correctos" );

if ( testsCorrectos != testsTotal ) {
	process.exit( 1 );
}
