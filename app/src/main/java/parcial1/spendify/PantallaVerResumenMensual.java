package parcial1.spendify;

import static android.content.ContentValues.TAG;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PantallaVerResumenMensual extends AppCompatActivity {
    private ArrayList<Double> gastosMensuales; // Lista de gastos mensuales
    private double ingresoMensual; // Ingreso mensual del usuario

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

                            // Obtener y mostrar el ingreso mensual del usuario
                            obtenerIngresoMensual(userEmail, totalGastosMensuales);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Rrror al obtener los gastos mensuales
                        Toast.makeText(this, "Error al obtener los gastos mensuales: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            // Obtén el ingreso mensual del usuario
            Task<DocumentSnapshot> ingresoMensualTask = FirebaseManager.getInstance().obtenerIngresoMensual(userEmail);
            ingresoMensualTask.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> ingresoMensualTask) {
                    if (ingresoMensualTask.isSuccessful()) {
                        DocumentSnapshot ingresoMensualDocument = ingresoMensualTask.getResult();
                        if (ingresoMensualDocument.exists()) {
                            // Documento de ingreso mensual existe, obtener el dato
                            Double ingresoMensual = ingresoMensualDocument.getDouble("ingresoMensual");

                            if (ingresoMensual != null) {
                                // Muestra el ingreso mensual en el TextView correspondiente
                                TextView ingresoMensualTextView = findViewById(R.id.ingreso_mensual);
                                ingresoMensualTextView.setText(getString(R.string.formato_ingreso_mensual, ingresoMensual));

                                // Continúa con el resto de tu lógica, por ejemplo, obtener los gastos mensuales
                                FirebaseManager.getInstance().obtenerGastosMensuales(userEmail)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> gastosTask) {
                                                if (gastosTask.isSuccessful()) {
                                                    DocumentSnapshot gastosDocument = gastosTask.getResult();
                                                    if (gastosDocument.exists()) {
                                                        // Documento de gastos existe, obtén los datos
                                                        Map<String, Object> datosGastosMensuales = gastosDocument.getData();
                                                        assert datosGastosMensuales != null;
                                                        ArrayList<Double> gastosMensuales = calcularGastosMensuales(datosGastosMensuales);

                                                        // Calcular el total de gastos mensuales usando el método existente
                                                        double totalGastosMensuales = FirebaseManager.getInstance().calcularTotalGastosMensuales(gastosMensuales);

                                                        // Muestra el total de gastos mensuales en el TextView correspondiente
                                                        TextView totalGastosMensualesTextView = findViewById(R.id.total_gastos_mensuales);
                                                        totalGastosMensualesTextView.setText(getString(R.string.formato_total_de_gastos_mensuales, totalGastosMensuales));

                                                        // Calcula los fondos restantes
                                                        double fondosRestantes = ingresoMensual - totalGastosMensuales;

                                                        // Muestra los fondos restantes en el TextView correspondiente
                                                        TextView fondosRestantesTextView = findViewById(R.id.fondos_restantes);
                                                        fondosRestantesTextView.setText(getString(R.string.formato_fondos_restantes, fondosRestantes));

                                                    } else {
                                                        // El documento de gastos no existe
                                                        Log.d(TAG, "No such document for gastos mensuales");
                                                    }
                                                } else {
                                                    // Error al obtener el documento de gastos
                                                    Log.d(TAG, "get failed with ", gastosTask.getException());
                                                }
                                            }
                                        });
                            } else {
                                // Maneja el caso en que ingresoMensual es null
                                Log.d(TAG, "Ingreso mensual is null");
                            }
                        } else {
                            // El documento de ingreso mensual no existe
                            Log.d(TAG, "No such document for ingreso mensual");
                        }
                    } else {
                        // Error al obtener el documento de ingreso mensual
                        Log.d(TAG, "get failed with ", ingresoMensualTask.getException());
                    }
                }
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

    // Método para obtener el ingreso mensual del usuario
    private void obtenerIngresoMensual(String userEmail, double totalGastosMensuales) {
        FirebaseManager.getInstance().obtenerIngresoMensual(userEmail)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtener el ingreso mensual y mostrarlo en el TextView correspondiente
                        Double ingresoMensual = documentSnapshot.getDouble("ingresoMensual");
                        if (ingresoMensual != null) {
                            this.ingresoMensual = ingresoMensual;
                            TextView ingresoMensualTextView = findViewById(R.id.ingreso_mensual);
                            ingresoMensualTextView.setText(getString(R.string.formato_ingreso_mensual, ingresoMensual));
                            calcularYMostrarFondosRestantes(totalGastosMensuales);

                        } else {
                            // Maneja el caso en que ingresoMensual es null
                            Log.d(TAG, "Ingreso mensual is null");
                        }

                    } else {
                        // El documento de ingreso mensual no existe
                        Log.d(TAG, "No such document for ingreso mensual");
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al obtener el ingreso mensual
                    Toast.makeText(this, "Error al obtener el ingreso mensual: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Método para calcular y mostrar los fondos restantes
    private void calcularYMostrarFondosRestantes(double totalGastosMensuales) {
        double fondosRestantes = ingresoMensual - totalGastosMensuales;
        TextView fondosRestantesTextView = findViewById(R.id.fondos_restantes);
        fondosRestantesTextView.setText(getString(R.string.formato_fondos_restantes, fondosRestantes));
    }

    private void volverAPantallaIndex() {
        Intent intent = new Intent(this, PantallaIndex.class);
        startActivity(intent);
        finish(); // Cierra la actividad actual para que no quede en la pila de actividades
    }
}