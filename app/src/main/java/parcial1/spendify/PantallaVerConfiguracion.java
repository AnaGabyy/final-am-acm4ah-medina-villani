package parcial1.spendify;

import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PantallaVerConfiguracion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_ver_configuracion);

        // OnClickListener para opción Modificar contraseña
        Button botonModificarContrasena = findViewById(R.id.boton_modificar_contrasena);
        botonModificarContrasena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoModificarContrasena();
            }
        });

        //OnClickListener para opción Modificar ingreso mensual
        Button botonModificarIngreso = findViewById(R.id.boton_modificar_ingreso_mensual);
        botonModificarIngreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoModificarIngresoMensual();
            }
        });

        // OnClickListener para opción Eliminar cuenta
        Button botonEliminarCuenta = findViewById(R.id.boton_eliminar_cuenta);
        botonEliminarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoEliminarCuenta();
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
    }

    // ---------------------------------------------- Opción "Modificar contraseña" ----------------------------------------------

    // Método para mostrar el diálogo de opción Modificar contraseña
    private void mostrarDialogoModificarContrasena() {
        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_modificar_contrasena, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Obtener referencias a los elementos del diálogo
        Button botonAceptar = dialogView.findViewById(R.id.boton_aceptar);

        // Configurar el botón "Aceptar"
        botonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAceptarModificarContrasena(v);
                dialog.dismiss(); // Cerrar el diálogo después de hacer clic en Aceptar
            }
        });

        // Mostrar el diálogo
        dialog.show();
    }

    // Método para manejar el botón "Aceptar"
    public void onClickAceptarModificarContrasena(View view) {
        // Obtener las contraseñas ingresadas
        EditText editTextContrasenaActual = findViewById(R.id.editText_contrasena_actual);
        EditText editTextNuevaContrasena = findViewById(R.id.editText_contrasena_nueva);

        String contrasenaActual = editTextContrasenaActual.getText().toString();
        String nuevaContrasena = editTextNuevaContrasena.getText().toString();

        // Verificar la contraseña actual
        verificarContrasenaActual(contrasenaActual, nuevaContrasena);
    }

    // Método para verificar la contraseña actual antes de cambiarla
    private void verificarContrasenaActual(String contrasenaActual, String nuevaContrasena) {
        FirebaseManager.getInstance().verificarContrasenaActual(contrasenaActual, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess() {
                // La contraseña actual es válida, proceder a cambiar la contraseña
                cambiarContrasena(nuevaContrasena);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Mensaje de error si la contraseña actual no es válida
                Toast.makeText(PantallaVerConfiguracion.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para cambiar la contraseña utilizando FirebaseManager
    private void cambiarContrasena(String nuevaContrasena) {
        FirebaseManager.getInstance().cambiarContrasena(nuevaContrasena, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess() {
                // Mensaje indicando que la contraseña se cambió correctamente
                Toast.makeText(PantallaVerConfiguracion.this, "Contraseña cambiada con éxito", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                // Mensaje de error
                Toast.makeText(PantallaVerConfiguracion.this, "Error al cambiar la contraseña: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------- Opción "Modificar ingreso mensual" ----------------------------------------------

    String nuevoIngresoMensual;  // Almacenar el nuevo ingreso mensual

    // Método para mostrar el diálogo de opción Modificar ingreso mensual
    private void mostrarDialogoModificarIngresoMensual() {
        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_modificar_ingreso_mensual, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Obtener referencias a los elementos del diálogo
        EditText editTextNuevoIngresoMensual = dialogView.findViewById(R.id.EditText_nuevoSueldo);
        Button botonAceptar = dialogView.findViewById(R.id.boton_aceptar);

        // Configurar el botón "Aceptar"
        botonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el nuevo ingreso mensual desde el EditText
                nuevoIngresoMensual = editTextNuevoIngresoMensual.getText().toString();

                // Llamar al método original para manejar el clic del botón
                onClickAceptarModificarIngresoMensual(v);
                dialog.dismiss(); // Cerrar el diálogo después de hacer clic en Aceptar
            }
        });

        // Mostrar el diálogo
        dialog.show();
    }

    // Método para manejar el botón "Aceptar"
    public void onClickAceptarModificarIngresoMensual(View view) {
        // Verificar que el nuevo ingreso mensual no esté vacío
        if (nuevoIngresoMensual != null && !nuevoIngresoMensual.isEmpty()) {
            // Lógica para actualizar el ingreso mensual en FirebaseManager
            FirebaseManager.getInstance().actualizarIngresoMensual(nuevoIngresoMensual, new FirebaseManager.AuthCallback() {
                @Override
                public void onSuccess() {
                    // Mensaje de éxito
                    Toast.makeText(PantallaVerConfiguracion.this, "Ingreso mensual actualizado con éxito", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Mensaje de error
                    Toast.makeText(PantallaVerConfiguracion.this, "Error al actualizar el ingreso mensual: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Mostrar mensaje de error si el nuevo ingreso mensual está vacío
            Toast.makeText(this, "Ingrese un nuevo ingreso mensual válido", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------------------------------- Opción "Eliminar cuenta" ----------------------------------------------

    // Método para mostrar el diálogo de opción Eliminar cuenta
    private void mostrarDialogoEliminarCuenta() {
        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_eliminar_cuenta, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Obtener referencias a los elementos del diálogo
        EditText editTextContrasenaActual = dialogView.findViewById(R.id.editText_contrasena_actual);
        Button botonAceptar = dialogView.findViewById(R.id.boton_aceptar);

        // Configurar el botón "Aceptar"
        botonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAceptarEliminarCuenta(v);
                dialog.dismiss(); // Cerrar el diálogo después de hacer clic en Aceptar
            }
        });

        // Mostrar el diálogo
        dialog.show();
    }

    // Método para manejar el botón "Aceptar" en el diálogo de Eliminar cuenta
    public void onClickAceptarEliminarCuenta(View view) {
        // Obtener la contraseña actual desde el EditText
        EditText editTextContrasenaActual = findViewById(R.id.editText_contrasena_actual);
        String contrasenaActual = editTextContrasenaActual.getText().toString();

        // Verificar la contraseña actual antes de proceder con la eliminación de la cuenta
        verificarContrasenaActual(contrasenaActual);
    }

    // Método para verificar la contraseña actual antes de proceder con la eliminación de la cuenta
    private void verificarContrasenaActual(String contrasenaActual) {
        FirebaseManager.getInstance().verificarContrasenaActual(contrasenaActual, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess() {
                // Contraseña verificada, proceder con la eliminación de la cuenta
                FirebaseManager.getInstance().eliminarCuenta(new FirebaseManager.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        // Eliminación de cuenta exitosa, redirigir al usuario a la pantalla inicial
                        irAPrimeraPantalla();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Mensaje de error al eliminar la cuenta
                        Toast.makeText(PantallaVerConfiguracion.this, "Error al eliminar la cuenta: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                // Mensaje de error si la contraseña no es válida
                Toast.makeText(PantallaVerConfiguracion.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para redirigir al usuario a PrimeraPantalla
    private void irAPrimeraPantalla() {
        Intent intent = new Intent(this, PrimeraPantalla.class);
        startActivity(intent);
        finish(); // Cierra la actividad actual para que no quede en la pila de actividades
    }

    // Método para volver al Index
    private void volverAPantallaIndex() {
        Intent intent = new Intent(this, PantallaIndex.class);
        startActivity(intent);
        finish(); // Cierra la actividad actual para que no quede en la pila de actividades
    }
}