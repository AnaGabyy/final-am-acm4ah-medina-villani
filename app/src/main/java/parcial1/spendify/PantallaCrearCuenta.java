package parcial1.spendify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PantallaCrearCuenta extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_crear_cuenta);

        //Evento-> correo electrónico invalido. Salta un mensaje si no tiene @
        EditText editTextEmail = findViewById(R.id.texto_ingresar_correo);
        Button buttonContinue = findViewById(R.id.boton_continuar);

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();

                if (!email.contains("@")) {
                    Toast.makeText(PantallaCrearCuenta.this, "Correo electrónico inválido, debe contener un @", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //En tiempo de ejecución agrego una sombra al titulo "Crear cuenta" (para probar)
        TextView titulo = findViewById(R.id.titulo_crear_una_cuenta);
        titulo.setShadowLayer(15,5,5, Color.BLACK);

        }

    // Evento -> link que lleva a la pantalla "Iniciar Sesion"
    public void irAPantallaIniciarSesion(View view) {
        Intent intent = new Intent(this, PantallaIniciarSesion.class);

        // Iniciar la actividad PantallaIniciarSesion
        startActivity(intent);
    }
}