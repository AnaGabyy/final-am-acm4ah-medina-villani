package parcial1.spendify;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FirebaseManager {
    private static FirebaseManager instance;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth mAuth;
    private GastoActual gastoActual;
    private ListenerRegistration gastoListener;

    // Constructor
    private FirebaseManager() {
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    // Obtener la instancia de FirebaseFirestore
    public FirebaseFirestore getFirestoreInstance() {
        return firestore;
    }

    // Obtener la instancia de FirebaseAuth
    public FirebaseAuth getAuthInstance() {
        return mAuth;
    }

    // Registrar un nuevo usuario
    public void registrarUsuario(String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    // Obtener el usuario actual
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // ------------------------------------ PantallaVerConfiguracion ------------------------------------

    // Método para verificar la contraseña actual
    public void verificarContrasenaActual(String contrasenaActual, AuthCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Reautenticar al usuario con su contraseña actual
            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), contrasenaActual);

            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Reautenticación exitosa
                            callback.onSuccess();
                        } else {
                            // Error en la reautenticación, notificar al callback
                            callback.onFailure("Error al reautenticar: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
        } else {
            // El usuario no está autenticado, notificar al callback
            callback.onFailure("Usuario no autenticado");
        }
    }


    // Cambiar la contraseña del usuario
    public void cambiarContrasena(String newPassword, AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
        } else {
            // El usuario no está autenticado, manejar según sea necesario
            callback.onFailure("Usuario no autenticado");
        }
    }

    // Método para actualizar el ingreso mensual del usuario
    public void actualizarIngresoMensual(String nuevoIngresoMensual, AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Obtener referencia al documento del usuario
            DocumentReference usuarioRef = firestore.collection("usuarios").document(Objects.requireNonNull(user.getEmail()));

            // Verificar si el nuevo ingreso mensual es diferente al actual
            if (!nuevoIngresoMensual.isEmpty() && !nuevoIngresoMensual.equals(user.getEmail())) {
                // Crear un mapa con el nuevo ingreso mensual
                Map<String, Object> nuevoIngresoMap = new HashMap<>();
                nuevoIngresoMap.put("ingresoMensual", nuevoIngresoMensual);

                // Actualizar el ingreso mensual en Firestore
                usuarioRef.update(nuevoIngresoMap)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Actualización exitosa
                                callback.onSuccess();
                            } else {
                                // Error en la actualización
                                callback.onFailure("Error al actualizar el ingreso mensual: " + Objects.requireNonNull(task.getException()).getMessage());
                            }
                        });
            } else {
                // Mostrar mensaje de que el ingreso mensual no se cambió
                callback.onFailure("El nuevo ingreso mensual es el mismo que el actual");
            }
        } else {
            // El usuario no está autenticado
            callback.onFailure("Usuario no autenticado");
        }
    }

    // Método para eliminar la cuenta del usuario
    public void eliminarCuenta(AuthCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Obtener referencia al documento del usuario
            DocumentReference usuarioRef = firestore.collection("usuarios").document(Objects.requireNonNull(currentUser.getEmail()));

            // Eliminar el documento del usuario en Firestore
            usuarioRef.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Documento del usuario eliminado, elimina la cuenta de Firebase Authentication
                            currentUser.delete()
                                    .addOnCompleteListener(authTask -> {
                                        if (authTask.isSuccessful()) {
                                            // Eliminación de cuenta exitosa
                                            callback.onSuccess();
                                        } else {
                                            // Error al eliminar la cuenta en Firebase Authentication
                                            callback.onFailure(Objects.requireNonNull(authTask.getException()).getMessage());
                                        }
                                    });
                        } else {
                            // Error al eliminar el documento del usuario en Firestore
                            callback.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
        } else {
            // El usuario no está autenticado, manejar según sea necesario
            callback.onFailure("Usuario no autenticado");
        }
    }

    // -------------------------------- Lógica para los gastos mensuales --------------------------------

    // Método agregar gasto
    public void agregarGasto(String userEmail, String tipoGasto, String monto) {
        // Obtener referencia a la colección de gastos para el usuario
        CollectionReference gastosCollection = firestore.collection("usuarios").document(userEmail).collection("gastos");

        // Crear un mapa con los datos del gasto
        Map<String, Object> gasto = new HashMap<>();
        gasto.put("tipoGasto", tipoGasto);
        gasto.put("monto", monto);

        // Agregar el documento a la colección de gastos con el tipo de gasto como ID
        gastosCollection.document(tipoGasto).set(gasto);
    }

    // Método eliminar gasto
    public Task<Void> eliminarGasto(String userEmail, String tipoGasto) {
        // Obtener referencia a la colección de gastos para el usuario
        CollectionReference gastosCollection = firestore.collection("usuarios").document(userEmail).collection("gastos");

        // Eliminar el documento correspondiente al tipo de gasto
        return gastosCollection.document(tipoGasto).delete();
    }

    // Método actualizar gasto
    public Task<Void> actualizarGasto(String userEmail, String tipoGastoActual, String nuevoTipoGasto, String nuevoMonto) {
        // Obtener referencia al documento del usuario y tipo de gasto actual
        DocumentReference gastoRef = firestore.collection("usuarios").document(userEmail).collection("gastos").document(tipoGastoActual);

        // Crear un mapa con los nuevos datos a actualizar
        Map<String, Object> nuevosDatos = new HashMap<>();
        nuevosDatos.put("tipoGasto", nuevoTipoGasto);
        nuevosDatos.put("monto", nuevoMonto);

        // Actualizar los datos en Firestore
        return gastoRef.update(nuevosDatos);
    }

    // Métodos para acceder y modificar el gasto actual
    public GastoActual getGastoActual() {
        return gastoActual;
    }

    public void setGastoActual(String tipoGasto, String monto) {
        this.gastoActual = new GastoActual(tipoGasto, monto);
    }

    // Método para configurar el listener del gasto actual
    public ListenerRegistration configurarListenerGastoActual(String userEmail, EventListener<DocumentSnapshot> listener) {
        // Obtener referencia al documento del usuario actual
        DocumentReference usuarioDocument = firestore.collection("usuarios").document(userEmail);

        // Obtener referencia al documento de gasto actual dentro de la colección "gastoActual"
        DocumentReference gastoActualRef = usuarioDocument.collection("gastoActual").document("gastoActual");

        // Configurar el listener para cambios en el documento de gasto actual
        return gastoActualRef.addSnapshotListener(listener);
    }

    // ------------------------------- PantallaVerGastos -------------------------------

    // Clase para representar el gasto actual
    public static class GastoActual {
        private String tipoGasto;
        private String monto;

        public GastoActual(String tipoGasto, String monto) {
            this.tipoGasto = tipoGasto;
            this.monto = monto;
        }

        public String getTipoGasto() {
            return tipoGasto;
        }

        public String getMonto() {
            return monto;
        }
    }

    // Variables para almacenar los datos del usuario
    private String ingresoMensual;
    private ArrayList<String> tiposGastos;
    private ArrayList<String> montos;

    // Método para obtener y guardar los datos del usuario
    public void obtenerYGuardarDatosUsuario(String userEmail, FirebaseManager firebaseManager, AuthCallback callback) {
        // Obtener referencia al documento del usuario
        DocumentReference usuarioRef = firestore.collection("usuarios").document(userEmail);

        // Obtener los datos del usuario
        usuarioRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String ingresoMensual = document.getString("ingresoMensual");

                    // Crear un ArrayList para guardar los gastos fijos del usuario
                    ArrayList<String> tiposGastos = new ArrayList<>();
                    ArrayList<String> montos = new ArrayList<>();

                    // Obtener los gastos fijos del documento del usuario
                    if (document.contains("gastosFijos")) {
                        // Utilizar un parámetro de tipo genérico
                        Map<String, Object> gastosFijosMap = document.getData();
                        if (gastosFijosMap != null) {
                            for (Map.Entry<String, Object> entry : gastosFijosMap.entrySet()) {
                                tiposGastos.add(entry.getKey());
                                montos.add(String.valueOf(entry.getValue()));
                            }
                        }
                    }

                    // Establecer el ingreso mensual y los gastos fijos en FirebaseManager
                    firebaseManager.setIngresoMensual(ingresoMensual);
                    firebaseManager.setGastosFijos(tiposGastos, montos);

                    callback.onSuccess();
                } else {
                    // El documento del usuario no existe
                    callback.onFailure("Documento de usuario no encontrado");
                }
            } else {
                // Error al obtener los datos del usuario
                callback.onFailure("Error al obtener datos del usuario: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    // Métodos para acceder y modificar el ingreso mensual
    public String getIngresoMensual() {
        return ingresoMensual;
    }

    public void setIngresoMensual(String ingresoMensual) {
        this.ingresoMensual = ingresoMensual;
    }

    // Métodos para acceder y modificar los gastos fijos
    public ArrayList<String> getTiposGastos() {
        return tiposGastos;
    }

    public ArrayList<String> getMontos() {
        return montos;
    }

    public void setGastosFijos(ArrayList<String> tiposGastos, ArrayList<String> montos) {
        this.tiposGastos = tiposGastos;
        this.montos = montos;
    }

    // ------------------------------- PantallaVerResumenMensual -------------------------------

    // Obtener los gastos mensuales del usuario
    public Task<DocumentSnapshot> obtenerGastosMensuales(String userEmail) {
        DocumentReference gastosMensualesRef = firestore.collection("usuarios").document(userEmail).collection("gastosMensuales").document("gastosMensuales");
        return gastosMensualesRef.get();
    }

    // Obtener los gastos fijos de la aplicación
    public Task<DocumentSnapshot> obtenerGastosFijos() {
        DocumentReference gastosFijosRef = firestore.collection("gastosFijos").document("gastosFijos");
        return gastosFijosRef.get();
    }

    // Método para calcular el total de gastos mensuales
    public double calcularTotalGastosMensuales(ArrayList<Double> gastosMensuales) {
        double total = 0.0;
        for (Double gasto : gastosMensuales) {
            total += gasto;
        }
        return total;
    }

    // Método para obtener el ingreso mensual del usuario
    public Task<DocumentSnapshot> obtenerIngresoMensual(String userEmail) {
        DocumentReference ingresoMensualRef = firestore.collection("usuarios").document(userEmail);
        return ingresoMensualRef.get();
    }

    // ----------------------------------------------------------------------------------------

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    // Método para cerrar la sesión
    public void cerrarSesion(AuthCallback callback) {
        mAuth.signOut();
        // Notificar sobre el éxito del cierre de sesión
        callback.onSuccess();
    }
}