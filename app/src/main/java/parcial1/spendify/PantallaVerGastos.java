package parcial1.spendify;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import android.widget.TextView;
import android.util.TypedValue;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

public class PantallaVerGastos extends AppCompatActivity {

    private TableLayout tableLayout;
    private FirebaseManager firebaseManager;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_ver_gastos);
        firebaseManager = FirebaseManager.getInstance();
        currentUser = firebaseManager.getCurrentUser();
        tableLayout = findViewById(R.id.tableLayout);


        // Obtener la información de gastos fijos del Intent
        ArrayList<String> tiposGastos = getIntent().getStringArrayListExtra("tiposGastos");
        ArrayList<String> montos = getIntent().getStringArrayListExtra("montos");

        // Llenar la tabla con la información de gastos fijos
        if (tiposGastos != null && montos != null) {
            for (int i = 0; i < tiposGastos.size(); i++) {
                agregarFilaTabla(tiposGastos.get(i), montos.get(i));
            }
        }

        // Botón 'Volver'
        Button botonVolver = findViewById(R.id.boton_volver);

        botonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volverAPantallaIndex();
            }
        });

        // Llama a obtenerImagenUrl al iniciar la aplicación
        obtenerImagenUrl(null);
    }

    public void obtenerImagenUrl(View v){
        // referencia del ImageView desde el archivo XML
        ImageView imagenResumenMensual = findViewById(R.id.imagen_ver_gastos);

        // URL de la imagen
        String url = "https://cdn-icons-png.flaticon.com/512/1751/1751761.png";

        // tarea asíncrona para cargar la imagen
        ImagenUrl imageUrl = new ImagenUrl(imagenResumenMensual);
        imageUrl.execute(url);
    }

    private void volverAPantallaIndex() {
        Intent intent = new Intent(this, PantallaIndex.class);
        startActivity(intent);
        finish(); // Cierra la actividad actual para que no quede en la pila de actividades
    }

    // Método para agregar una fila a la tabla
    private void agregarFilaTabla(String tipoGasto, String monto) {
        // Crear una nueva fila
        TableRow newRow = new TableRow(this);

        // Configurar layout parameters para las celdas
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16); // Agregar margen inferior entre filas

        // Crear celdas y agregarlas a la fila
        TextView cellTipoGasto = new TextView(this);
        cellTipoGasto.setText(tipoGasto);
        cellTipoGasto.setTextColor(getResources().getColor(R.color.lima));
        cellTipoGasto.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.fuente12));
        cellTipoGasto.setLayoutParams(params);

        TextView cellMonto = new TextView(this);
        cellMonto.setText(monto);
        cellMonto.setTextColor(getResources().getColor(R.color.lima));
        cellMonto.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.fuente12));
        cellMonto.setLayoutParams(params);

        Button cellAgregar = new Button(this);
        cellAgregar.setText("Agregar");

        // Configurar onClickListener para el botón "Agregar"
        cellAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para agregar
                agregarGasto(tipoGasto, monto);
            }
        });

        Button cellModificar = new Button(this);
        cellModificar.setText("Modificar");

        // Configurar onClickListener para el botón "Modificar"
        cellModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para modificar
                modificarGasto(tipoGasto, monto);
            }
        });

        Button cellEliminar = new Button(this);
        cellEliminar.setText("Eliminar");

        // Configurar onClickListener para el botón "Eliminar"
        cellEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para eliminar
                eliminarGasto(tipoGasto);
                // Remover la fila de la tabla
                tableLayout.removeView(newRow);
            }
        });

        // Agregar celdas a la fila
        newRow.addView(cellTipoGasto);
        newRow.addView(cellMonto);
        newRow.addView(cellAgregar);
        newRow.addView(cellModificar);
        newRow.addView(cellEliminar);

        // Agregar fila a la tabla
        tableLayout.addView(newRow);

        // Cambiar visibilidad de la tabla a VISIBLE después de agregar una fila
        tableLayout.setVisibility(View.VISIBLE);
    }

    // Función para agregar un gasto
    private void agregarGasto(String tipoGasto, String monto) {
        // Obtener el usuario actual
        FirebaseUser currentUser = firebaseManager.getCurrentUser();

        if (currentUser != null) {
            // Obtener el correo electrónico del usuario
            String userEmail = currentUser.getEmail();

            // Agregar el gasto a Firestore
            firebaseManager.agregarGasto(userEmail, tipoGasto, monto);
            Toast.makeText(this, "Gasto agregado", Toast.LENGTH_SHORT).show();
            actualizarTabla();
        } else {
            // El usuario no está autenticado
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    // Función para modificar un gasto
    private void modificarGasto(String tipoGastoActual, String montoActual) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modificar Gasto");

        // Diseño personalizado del diálogo
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_gasto, null);
        builder.setView(dialogView);

        // Buscar las vistas en el diseño del diálogo
        EditText editTextNuevoTipoGasto = dialogView.findViewById(R.id.editText_tipo_gasto);
        EditText editTextNuevoMonto = dialogView.findViewById(R.id.editText_monto);

        // Configurar valores actuales en los EditText
        editTextNuevoTipoGasto.setText(tipoGastoActual);
        editTextNuevoMonto.setText(montoActual);

        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Obtener los nuevos valores desde los EditText
                String nuevoTipoGasto = editTextNuevoTipoGasto.getText().toString();
                String nuevoMonto = editTextNuevoMonto.getText().toString();

                // Método de FirebaseManager para actualizar el gasto en Firestore
                firebaseManager.actualizarGasto(currentUser.getEmail(), tipoGastoActual, nuevoTipoGasto, nuevoMonto)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Lógica después de una actualización exitosa
                                    Toast.makeText(PantallaVerGastos.this, "Gasto modificado exitosamente", Toast.LENGTH_SHORT).show();
                                    actualizarTabla();
                                } else {
                                    // Lógica en caso de fallo en la actualización
                                    Toast.makeText(PantallaVerGastos.this, "Error al modificar el gasto", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    // Función para eliminar un gasto
    private void eliminarGasto(String tipoGasto) {
        // Obtener el usuario actual
        FirebaseUser currentUser = firebaseManager.getCurrentUser();

        if (currentUser != null) {
            // Obtener el correo electrónico del usuario
            String userEmail = currentUser.getEmail();

            // Eliminar el gasto de Firestore
            firebaseManager.eliminarGasto(userEmail, tipoGasto);
            Toast.makeText(this, "Gasto eliminado", Toast.LENGTH_SHORT).show();
            actualizarTabla();
        } else {
            // El usuario no está autenticado
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarTabla() {
        // Limpiar la tabla antes de agregar las filas actualizadas
        tableLayout.removeAllViews();

        // Obtener la información de gastos fijos del Intent
        ArrayList<String> tiposGastos = getIntent().getStringArrayListExtra("tiposGastos");
        ArrayList<String> montos = getIntent().getStringArrayListExtra("montos");

        // Llenar la tabla con la información de gastos fijos
        if (tiposGastos != null && montos != null) {
            for (int i = 0; i < tiposGastos.size(); i++) {
                agregarFilaTabla(tiposGastos.get(i), montos.get(i));
            }
        }
    }

    // Método onResume para actualizar la tabla cada vez que se vuelve a entrar a la pantalla
    @Override
    protected void onResume() {
        super.onResume();
        actualizarTabla();
    }

}