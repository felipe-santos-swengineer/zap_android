package br.com.ufc.zap_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.FirstPartyScopes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class registro extends AppCompatActivity {

    EditText edt_reg_email;
    EditText edt_reg_password;
    EditText edt_name;
    EditText edt_address;
    Button btn_register;
    Button btn_add_foto;
    ImageView img_foto;
    Uri uri_foto;
    String error_msg;
    boolean has_photo;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        getSupportActionBar().setTitle("Registro");

        has_photo = false;
        edt_reg_email = (EditText) findViewById(R.id.edt_reg_email);
        edt_reg_password = (EditText) findViewById(R.id.edt_reg_password);
        edt_name = (EditText) findViewById(R.id.edt_name);
        edt_address = (EditText) findViewById(R.id.edt_address);
        btn_register = (Button) findViewById(R.id.btn_register);
        btn_add_foto = (Button) findViewById(R.id.btn_add_foto);
        img_foto = (ImageView) findViewById(R.id.img_foto);
    }

    public void onClickBtnRegister(View v) {
        String email = edt_reg_email.getText().toString();
        String senha = edt_reg_password.getText().toString();
        String nome = edt_name.getText().toString();
        String endereco = edt_address.getText().toString();

        if (email.isEmpty() || email == null || senha.isEmpty() || senha == null
                || nome.isEmpty() || nome == null || endereco.isEmpty() || endereco == null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(registro.this);
            builder.setCancelable(false);
            builder.setTitle(Html.fromHtml("<font color='#cc0000'>Falha</font>"));
            builder.setMessage("Há campos invalidos e/ou vazios");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return;
        }

        if (has_photo == false) {

            AlertDialog.Builder builder = new AlertDialog.Builder(registro.this);
            builder.setCancelable(false);
            builder.setTitle(Html.fromHtml("<font color='#cc0000'>Falha</font>"));
            builder.setMessage("Falta foto");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return;
        }

        new SaveUserInFirebase().execute("Saving new User");

    }

    public class SaveUserInFirebase extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = progressDialog.show(registro.this, "Aguarde",
                    "Cadastrando...", true, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String email = edt_reg_email.getText().toString();
            String senha = edt_reg_password.getText().toString();
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            String filename = UUID.randomUUID().toString();
                            final StorageReference ref = FirebaseStorage.getInstance().getReference("/images/" + filename);
                            ref.putFile(uri_foto)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    //Criação do usuário no firebase
                                                    String id = FirebaseAuth.getInstance().getUid();
                                                    String nome = edt_name.getText().toString();
                                                    String fotoUrl = uri.toString();
                                                    String endereco = edt_address.getText().toString();
                                                    String email = edt_reg_email.getText().toString();
                                                    String senha = edt_reg_password.getText().toString();

                                                    User user = new User(id, nome, fotoUrl, endereco, email, senha);
                                                    FirebaseFirestore.getInstance().collection("users")
                                                            .document(id)
                                                            .set(user)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    progressDialog.dismiss();
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(registro.this);
                                                                    builder.setCancelable(false);
                                                                    builder.setTitle(Html.fromHtml("<font color='#509324'>Sucesso</font>"));
                                                                    builder.setMessage("Você se cadastrou!");
                                                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            dialog.dismiss();
                                                                            Intent intent;
                                                                            intent = new Intent(registro.this, messages.class);
                                                                            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                                                                            startActivity(intent);
                                                                        }
                                                                    });
                                                                    builder.show();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressDialog.dismiss();
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(registro.this);
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

                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            AlertDialog.Builder builder = new AlertDialog.Builder(registro.this);
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
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(registro.this);
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

    public void onClickBtnAddFoto(View v) {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //request 0 = solicitação de foto e conferindo se o usuario pegou uma foto mesmo ou cancelou o pick.
        if (requestCode == 0 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri_foto = data.getData();
            has_photo = true;
            try {
                Bitmap bitmap = null;
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri_foto);
                img_foto.setImageDrawable(new BitmapDrawable(bitmap));
                btn_add_foto.setAlpha(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}