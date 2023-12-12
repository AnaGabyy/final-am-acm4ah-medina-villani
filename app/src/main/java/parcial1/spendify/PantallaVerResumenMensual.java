package parcial1.spendify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class PantallaVerResumenMensual extends AppCompatActivity {
    private ArrayList<Double> gastosMensuales; // Lista de gastos mensuales

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_ver_resumen_mensual);

        // Obtener el usuario actual de Firebase Authentication
        FirebaseUser usuarioActual = FirebaseManager.getInstance().getCurrentUser();

        // Verificar si el usuario está autenticado
        if (usuarioActual != null) {
            // Obtener el correo electrónico del usuario
            String userEmail = usuarioActual.getEmail();

            // Llama a obtenerImagenUrl al iniciar la aplicación
            obtenerImagenUrl(null);

            // Obtén los gastos mensuales del usuario
            FirebaseManager.getInstance().obtenerGastosMensuales(userEmail)
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) { // Si el documento existe, obtiene los datos y realiza los cálculos
                            Map<String, Object> datosGastosMensuales = documentSnapshot.getData();
                            assert datosGastosMensuales != null;
                            gastosMensuales = calcularGastosMensuales(datosGastosMensuales);

                            // Calcular el total de gastos mensuales
                            double totalGastosMensuales = FirebaseManager.getInstance().calcularTotalGastosMensuales(gastosMensuales);

                            // Mostrar el total de gastos mensuales en el TextView
                            TextView totalGastosMensualesTextView = findViewById(R.id.total_gastos_mensuales);
                            totalGastosMensualesTextView.setText(getString(R.string.formato_total_de_gastos_mensuales, totalGastosMensuales));
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Rrror al obtener los gastos mensuales
                        Toast.makeText(this, "Error al obtener los gastos mensuales: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            // OnClickListener para el botón "Volver"
            Button botonVolver = findViewById(R.id.boton_volver);
            botonVolver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    volverAPantallaIndex();
                }
            });

        } else {
            // El usuario no está autenticado
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            // Redirigir a PrimeraPantalla
            Intent intent = new Intent(this, PrimeraPantalla.class);
            startActivity(intent);
            finish();
        }

    }

    public void obtenerImagenUrl(View v){
        //Imagen 1 resumen mensual
        // referencia del ImageView desde el archivo XML
        ImageView imagenResumenMensual = findViewById(R.id.imagen_ver_resumen_mensual);

        // URL de la imagen
        String url = "https://programacion.net/files/editor/bar-chart.png";

        // tarea asíncrona para cargar la imagen
        ImagenUrl imagen1 = new ImagenUrl(imagenResumenMensual);
        imagen1.execute(url);

        // Imagen 2 grafico
        ImageView imagenGrafico = findViewById(R.id.imagen_grafico);
        String urlGrafico = "https://www.zurich.cl/-/media/project/zwp/chile/images/blog/foto-blog-metodo-ahorro-grafico.jpg?la=es-cl&mw=737&hash=543DA2B8C779A2D1C5270C26C293F5DB";
        ImagenUrl imagen2 = new ImagenUrl(imagenGrafico);
        imagen2.execute(urlGrafico);
    }



    private ArrayList<Double> calcularGastosMensuales(Map<String, Object> datosGastosMensuales) {
        // ArrayList para almacenar los gastos mensuales
        ArrayList<Double> gastosMensuales = new ArrayList<>();

        for (String key : datosGastosMensuales.keySet()) {
            // Obtener el valor asociado a la clave
            Object valor = datosGastosMensuales.get(key);

            // Verificar si el valor es numérico antes de intentar convertirlo
            if (valor instanceof Number) {
                // Convertir el valor a double y agregarlo a la lista de gastos mensuales
                double monto = ((Number) valor).doubleValue();
                gastosMensuales.add(monto);
            } else {
                // Si el valor no es numérico, muestra un mensaje de advertencia
                Log.w("CalcularGastosMensuales", "El valor para la clave " + key + " no es numérico");
            }
        }

        return gastosMensuales;
    }

    private void volverAPantallaIndex() {
        Intent intent = new Intent(this, PantallaIndex.class);
        startActivity(intent);
        finish(); // Cierra la actividad actual para que no quede en la pila de actividades
    }
}