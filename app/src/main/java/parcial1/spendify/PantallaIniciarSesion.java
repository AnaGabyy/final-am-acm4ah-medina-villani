package parcial1.spendify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class PantallaIniciarSesion extends AppCompatActivity {

    private FirebaseManager firebaseManager;

    public void verificarConexion(){
        //Con este objeto de conexión puedo saber el estado de la red
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isAvailable()){
            //Task
            //Set ui
        }else{
            Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_iniciar_sesion);

        verificarConexion();

        // Inicializar FirebaseManager
        firebaseManager = FirebaseManager.getInstance();

        // Enlace a elementos de la interfaz de usuario
        EditText editTextEmail = findViewById(R.id.EditText_ingresar_correo);
        EditText editTextPassword = findViewById(R.id.EditText_ingresar_contrasena);
        Button buttonContinuar = findViewById(R.id.boton_continuar);

        // Evento al hacer clic en el botón "Continuar"
        buttonContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Validar que el correo electrónico sea válido
                if (!email.contains("@")) {
                    Toast.makeText(PantallaIniciarSesion.this, "Correo electrónico inválido, debe contener un @", Toast.LENGTH_SHORT).show();
                } else {
                    // Iniciar sesión
                    iniciarSesion(email, password);
                }
            }
        });

        // Estilo de tiempo de ejecución: agrego una sombra al título "Inicia Sesión"
        TextView titulo = findViewById(R.id.titulo_inicia_sesion);
        titulo.setShadowLayer(15, 5, 5, Color.BLACK);


    }

    // Función para iniciar sesión
    private void iniciarSesion(String email, String password) {
        firebaseManager.getAuthInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inicio de sesión exitoso, obtener y guardar los datos del usuario
                        obtenerYGuardarDatosUsuario(email);
                    } else {
                        // Mensaje de error si el inicio de sesión falla
                        Toast.makeText(PantallaIniciarSesion.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Función para obtener y guardar los datos del usuario
    private void obtenerYGuardarDatosUsuario(String email) {
        // Obtener referencia al documento del usuario
        DocumentReference usuarioRef = firebaseManager.getFirestoreInstance().collection("usuarios").document(email);

        usuarioRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Documento del usuario encontrado, obtener datos
                    String ingresoMensual = document.getString("ingresoMensual");

                    // Obtener gastos fijos
                    obtenerGastosFijosYGuardar(email, ingresoMensual);

                    // Guardar datos en SharedPreferences
                    guardarDatosUsuarioEnPrefCompartidas(ingresoMensual);

                    // Ir a la siguiente pantalla (PantallaIndex)
                    irAPantallaIndex();
                } else {
                    // Documento del usuario no encontrado
                    Toast.makeText(PantallaIniciarSesion.this, "Datos del usuario no encontrados", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Error al obtener datos del usuario
                Toast.makeText(PantallaIniciarSesion.this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Función para obtener gastos fijos y guardar en SharedPreferences
    private void obtenerGastosFijosYGuardar(String email, String ingresoMensual) {
        // Obtener referencia al documento de gastos fijos para el usuario
        DocumentReference gastosFijosRef = firebaseManager.getFirestoreInstance().collection("usuarios").document(email).collection("gastosFijos").document("gastosFijos");

        gastosFijosRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot gastosFijosDocument = task.getResult();
                if (gastosFijosDocument.exists()) {
                    // Documento de gastos fijos encontrado, obtener datos
                    String tipoGastoFijo = gastosFijosDocument.getString("tipoGastoFijo");
                    String montoGastoFijo = gastosFijosDocument.getString("montoGastoFijo");

                    // Guardar datos de gastos fijos en SharedPreferences
                    guardarGastosFijosEnPrefCompartidas(tipoGastoFijo, montoGastoFijo);
                } else {
                    // Documento de gastos fijos no encontrado
                    Toast.makeText(PantallaIniciarSesion.this, "Gastos fijos no encontrados", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Error al obtener datos de gastos fijos
                Toast.makeText(PantallaIniciarSesion.this, "Error al obtener gastos fijos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Función para guardar datos de gastos fijos en SharedPreferences
    private void guardarGastosFijosEnPrefCompartidas(String tipoGastoFijo, String montoGastoFijo) {
        SharedPreferences preferences = getSharedPreferences("usuario_datos", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("tipoGastoFijo", tipoGastoFijo);
        editor.putString("montoGastoFijo", montoGastoFijo);
        editor.apply();
    }

    // Función para guardar datos del usuario en SharedPreferences
    private void guardarDatosUsuarioEnPrefCompartidas(String ingresoMensual) {
        SharedPreferences preferences = getSharedPreferences("usuario_datos", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ingresoMensual", ingresoMensual);
        // ACÁ VAN LOS GASTOS FIJOS
        editor.apply();
    }

    // Función para ir a PantallaIndex
    private void irAPantallaIndex() {
        Intent intent = new Intent(this, PantallaIndex.class);
        startActivity(intent);
        finish();
    }

}