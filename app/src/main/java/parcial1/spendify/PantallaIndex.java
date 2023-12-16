package parcial1.spendify;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class PantallaIndex extends AppCompatActivity {
    private FirebaseManager firebaseManager;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_index);

        firebaseManager = FirebaseManager.getInstance();

        // ------------ OnClickListener para botones ------------

        // Botón 'Gastos'
        Button botonGastos = findViewById(R.id.boton_gastos);
        botonGastos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirPantallaVerGastos();
            }
        });

        // Botón 'Ver Resumen Mensual'
        Button botonVerResumenMensual = findViewById(R.id.boton_ver_resumen_mensual);
        botonVerResumenMensual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirPantallaVerResumenMensual();
            }
        });

        // Botón 'Ver Consejos'
        Button botonConsejos = findViewById(R.id.boton_consejos);
        botonConsejos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirPantallaVerConsejos();
            }
        });

        // Botón 'Configuración'
        Button botonConfiguracion = findViewById(R.id.boton_configuracion);
        botonConfiguracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirPantallaVerConfiguracion();
            }
        });

        Button botonCerrarSesion = findViewById(R.id.boton_cerrar_sesion);
        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoCerrarSesion();
            }
        });

    }

     // ------------ Funciones para abrir pantallas ------------

    // PantallaVerGastos
    private void abrirPantallaVerGastos() {
        Intent intent = new Intent(this, PantallaVerGastos.class);

        // Obtener la información de gastos fijos del Intent
        ArrayList<String> tiposGastos = getIntent().getStringArrayListExtra("tiposGastos");
        ArrayList<String> montos = getIntent().getStringArrayListExtra("montos");

        // Pasar la información de gastos fijos a PantallaVerGastos
        intent.putStringArrayListExtra("tiposGastos", tiposGastos);
        intent.putStringArrayListExtra("montos", montos);

        startActivity(intent);
    }

    // PantallaVerResumenMensual
    private void abrirPantallaVerResumenMensual() {
        Intent intent = new Intent(this, PantallaVerResumenMensual.class);
        startActivity(intent);
    }

    // PantallaVerConsejos
    private void abrirPantallaVerConsejos() {
        Intent intent = new Intent(this, PantallaVerConsejos.class);
        startActivity(intent);
    }

    // PantallaVerConfiguracion
    private void abrirPantallaVerConfiguracion() {
        Intent intent = new Intent(this, PantallaVerConfiguracion.class);
        startActivity(intent);
    }

    // ------------ Cerrar sesión ------------

    // Método para mostrar el diálogo de confirmación cerrar sesión
    private void mostrarDialogoCerrarSesion() {
        // Crear el diálogo
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_cerrar_sesion);

        // Configurar botón 'Cerrar Sesión'
        Button botonCerrarSesion = dialog.findViewById(R.id.boton_confirmarCerrarSesion);
        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = firebaseManager.getCurrentUser();

                if (currentUser != null) {
                    firebaseManager.cerrarSesion(new FirebaseManager.AuthCallback() {
                        @Override
                        public void onSuccess() {
                            irAPrimeraPantalla();
                            dialog.dismiss();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(PantallaIndex.this, "Error al cerrar sesión: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // Configurar botón 'Cancelar'
        Button botonCancelar = dialog.findViewById(R.id.boton_cancelarCerrarSesion);
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cerrar el diálogo
                dialog.dismiss();
            }
        });

        // Mostrar el diálogo
        dialog.show();
    }

    // Función para ir a PrimeraPantalla
    private void irAPrimeraPantalla() {
        Intent intent = new Intent(this, PrimeraPantalla.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}