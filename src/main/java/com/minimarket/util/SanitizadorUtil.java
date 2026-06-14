package com.minimarket.util;

/**
 * Utilidad simple para normalizar y sanitizar campos de texto provenientes
 * de entradas no confiables (peticiones HTTP) antes de persistirlos.
 */
public final class SanitizadorUtil {

    private SanitizadorUtil() {
    }

    /**
     * Recorta espacios al inicio/fin, colapsa espacios repetidos y elimina
     * caracteres de control y marcas de HTML/script basicas (< >) para
     * reducir el riesgo de inyeccion de contenido (XSS) en campos de texto.
     */
    public static String limpiarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        limpio = limpio.replaceAll("[<>]", "");
        limpio = limpio.replaceAll("\\s+", " ");
        return limpio;
    }
}
