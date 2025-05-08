package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheGananciasUtils;

import java.util.List;
import java.util.Map;

public class ParametrosUtils {

    public static double calcular(String tipo) {
        List<Map<String, String>> parametros = VerUtils.verTabla("Parametros");
        System.out.println("   📄 Filas Parámetros: " + parametros.size());

        double total = 0.0;
        double ganancia = CacheGananciasUtils.get();
        System.out.printf("   💰 Ganancia actual (desde caché): %.2f\n", ganancia);

        double costoManoObra = 0.0;
        int empleados = 1;
        boolean activoManoObra = true;
        boolean activoEmpleados = true;

        for (Map<String, String> fila : parametros) {
            String codigo = fila.getOrDefault("Código", "").trim();
            String nombre = fila.getOrDefault("Nombre", "?");
            String unidad = fila.getOrDefault("Unidad", "").trim().toLowerCase();
            boolean check = fila.getOrDefault("Check", "✓").equals("✓");
            double valor = ParseUtils.toDouble(fila.getOrDefault("Valor", "0"));

            System.out.printf("   🔎 Param: %s | Código: %s | Valor: %.2f | Unidad: %s | Activo: %s\n",
                    nombre, codigo, valor, unidad, check ? "✓" : "✗");

            switch (codigo) {
                case "PAR0001" -> {
                    costoManoObra = valor;
                    activoManoObra = check;
                }
                case "PAR0002" -> {
                    empleados = (int) valor;
                    activoEmpleados = check;
                }
                default -> {
                    if (unidad.contains("%") && check) {
                        double desc = ganancia * (valor / 100.0);
                        total += desc;
                        System.out.printf("     ➤ Descuento aplicado: %.2f%% de %.2f = %.2f\n",
                                valor, ganancia, desc);
                    }
                }
            }
        }

        double manoObra = (activoManoObra && activoEmpleados) ? costoManoObra * empleados : 0;
        if (manoObra > 0)
            System.out.printf("     ➤ Mano de obra total: %.2f (%d empleados x %.2f)\n",
                    manoObra, empleados, costoManoObra);
        else
            System.out.println("     ⚠ Mano de obra desactivada.");

        total += manoObra;

        System.out.printf("   ✅ Total parámetros calculados: %.2f\n", total);
        return total;
    }
}
