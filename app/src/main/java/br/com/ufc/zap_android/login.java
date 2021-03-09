package br.com.ufc.zap_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

public class login extends AppCompatActivity {

    private EditText edt_email;
    private EditText edt_password;
    private Button btn_login;
    private TextView text_create_acc;
    private String error_msg;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Log in");

        edt_email = (EditText) findViewById(R.id.edt_email);
        edt_password = (EditText) findViewById(R.id.edt_password);
        btn_login = (Button) findViewById(R.id.btn_login);
        text_create_acc = (TextView) findViewById(R.id.text_create_acc);

    }

    public void OnclickbtnCEntrar(View v) {
        String email = edt_email.getText().toString();
        String password = edt_password.getText().toString();

        if(email.isEmpty() || email == null || password.isEmpty() || password == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(login.this);
            builder.setCancelable(false);
            builder.setTitle(Html.fromHtml("<font color='#cc0000'>Falha</font>"));
            builder.setMessage("Email e/ou senha invalidos e/ou vazios");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return;
        }

        new LoginVerify().execute("Validando Login");
    }


    public class LoginVerify extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = progressDialog.show(login.this, "Aguarde",
                    "Autentificando...", true, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String email = edt_email.getText().toString();
            String password = edt_password.getText().toString();
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            progressDialog.dismiss();
                            Intent intent;
                            intent = new Intent(login.this, messages.class);
                            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(login.this);
                            builder.setCancelable(false);
                            builder.setTitle(Html.fromHtml("<font color='#cc0000'>Falha</font>"));
                            builder.setMessage(e.getMessage());
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    });
            return null;
        }
    }

    public void OnClickBtnCadastrar(View v){
        Intent registro = new Intent(this, registro.class);
        startActivity(registro);
    }
}