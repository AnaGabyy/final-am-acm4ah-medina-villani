package parcial1.spendify;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class PantallaGastosFijos extends AppCompatActivity {

    private TableLayout tableLayout;
    private EditText editTextTipoGasto;
    private EditText editTextMonto;

    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_gastos_fijos);

        // Obtener referencia a la tabla y a los EditText
        tableLayout = findViewById(R.id.tableLayout);
        editTextTipoGasto = findViewById(R.id.EditText_ingresar_tipo_de_gasto);
        editTextMonto = findViewById(R.id.EditText_ingresar_monto);

        // Configurar FirebaseManager
        firebaseManager = FirebaseManager.getInstance();
    }

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

        Button cellModificar = new Button(this);
        cellModificar.setText("Modificar");

        // Configurar onClickListener para el botón "Modificar"
        cellModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el tipo de gasto actual y el monto actual
                String tipoGastoActual = cellTipoGasto.getText().toString();
                String montoActual = cellMonto.getText().toString();

                // Crear un cuadro de diálogo
                AlertDialog.Builder builder = new AlertDialog.Builder(PantallaGastosFijos.this);
                builder.setTitle("Editar Datos");

                // Inflar el diseño del cuadro de diálogo
                View view = getLayoutInflater().inflate(R.layout.dialog_editar_gasto, null);
                builder.setView(view);

                // Obtener referencias a los EditText dentro del cuadro de diálogo
                EditText editTextTipoGasto = view.findViewById(R.id.editText_tipo_gasto);
                EditText editTextMonto = view.findViewById(R.id.editText_monto);

                // Establecer los valores actuales en los EditText
                editTextTipoGasto.setText(tipoGastoActual);
                editTextMonto.setText(montoActual);

                // Configurar los botones del cuadro de diálogo
                builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Obtener los nuevos valores desde los EditText
                        String nuevoTipoGasto = editTextTipoGasto.getText().toString();
                        String nuevoMonto = editTextMonto.getText().toString();

                        // Obtener el tipo de gasto actual y el monto actual
                        String tipoGastoActual = cellTipoGasto.getText().toString();
                        String montoActual = cellMonto.getText().toString();

                        // Obtener el usuario actual
                        FirebaseUser currentUser = firebaseManager.getCurrentUser();

                        if (currentUser != null) {
                            // Obtener el correo electrónico del usuario
                            String userEmail = currentUser.getEmail();

                            // Actualizar el gasto en Firestore
                            firebaseManager.actualizarGasto(userEmail, tipoGastoActual, nuevoTipoGasto, nuevoMonto)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Actualizar las celdas en la fila
                                                cellTipoGasto.setText(nuevoTipoGasto);
                                                cellMonto.setText(nuevoMonto);
                                                Toast.makeText(PantallaGastosFijos.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Manejar errores si la actualización no es exitosa
                                                Toast.makeText(PantallaGastosFijos.this, "Error al actualizar datos", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // El usuario no está autenticado
                            Toast.makeText(PantallaGastosFijos.this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cerrar el cuadro de diálogo sin hacer cambios
                    }
                });

                // Mostrar el cuadro de diálogo
                builder.create().show();
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
        newRow.addView(cellModificar);
        newRow.addView(cellEliminar);

        // Agregar fila a la tabla
        tableLayout.addView(newRow);

        // Cambiar visibilidad de la tabla a VISIBLE después de agregar una fila
        tableLayout.setVisibility(View.VISIBLE);
    }

    // Función para eliminar un gasto de Firestore
    private void eliminarGasto(String tipoGasto) {
        // Obtener el usuario actual
        FirebaseUser currentUser = firebaseManager.getCurrentUser();

        if (currentUser != null) {
            // Obtener el correo electrónico del usuario
            String userEmail = currentUser.getEmail();

            // Eliminar el gasto de Firestore
            firebaseManager.eliminarGasto(userEmail, tipoGasto);
        } else {
            // El usuario no está autenticado
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    public void agregarGasto(View view) {
        String tipoGasto = editTextTipoGasto.getText().toString();
        String monto = editTextMonto.getText().toString();

        // Verificar que ambos campos estén completos
        if (!tipoGasto.isEmpty() && !monto.isEmpty()) {
            // Agregar datos a Firestore
            agregarGastoFirestore(tipoGasto, monto);
            // Agregar fila a la tabla
            agregarFilaTabla(tipoGasto, monto);
            // Limpiar campos después de agregar
            editTextTipoGasto.getText().clear();
            editTextMonto.getText().clear();
        } else {
            // Mostrar mensaje si los campos están vacíos
            Toast.makeText(this, "Ingresa todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para enviar los gastos fijos a PantallaVerGastos
    private void enviarGastosFijos(ArrayList<String> tiposGastos, ArrayList<String> montos) {
        // Crear el Intent para PantallaVerGastos
        Intent intentVerGastos = new Intent(this, PantallaVerGastos.class);
        intentVerGastos.putStringArrayListExtra("tiposGastos", tiposGastos);
        intentVerGastos.putStringArrayListExtra("montos", montos);

        // startActivity(intentVerGastos);
    }

    // Método para el botón 'Continuar'
    public void continuar(View view) {
        // Obtener la información de gastos fijos de las filas de la tabla
        ArrayList<String> tiposGastos = new ArrayList<>();
        ArrayList<String> montos = new ArrayList<>();

        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);

            TextView cellTipoGasto = (TextView) row.getChildAt(0);
            TextView cellMonto = (TextView) row.getChildAt(1);

            tiposGastos.add(cellTipoGasto.getText().toString());
            montos.add(cellMonto.getText().toString());
        }

        // Llamar al método para enviar gastos fijos
        enviarGastosFijos(tiposGastos, montos);

        // Crear el Intent para PantallaIndex
        Intent intentIndex = new Intent(this, PantallaIndex.class);
        // Iniciar PantallaIndex
        startActivity(intentIndex);
    }

    // Función para agregar un gasto a Firestore
    private void agregarGastoFirestore(String tipoGasto, String monto) {
        // Obtener el usuario actual
        FirebaseUser currentUser = firebaseManager.getCurrentUser();

        if (currentUser != null) {
            // Obtener el correo electrónico del usuario
            String userEmail = currentUser.getEmail();

            // Agregar el gasto a Firestore
            firebaseManager.agregarGasto(userEmail, tipoGasto, monto);
        } else {
            // El usuario no está autenticado
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }
}