package br.com.ufc.zap_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class gp_registro extends AppCompatActivity {

    private Button btn_reg_gp;
    private Button btn_img_gp_preview;
    private ImageView img_reg_gp;
    private Uri uri_foto;
    private String error_msg;
    private RecyclerView rv_select_users;
    private RecyclerView rv_selected_users;
    private GroupAdapter adapter_rv_select = new GroupAdapter();
    private GroupAdapter adapter_rv_selected = new GroupAdapter();
    private boolean has_photo;
    private EditText edt_gp_name;
    private ArrayList<User> gp_users;
    private User me;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gp_registro);

        gp_users = new ArrayList<>();

        has_photo = false;
        btn_reg_gp = (Button) findViewById(R.id.btn_reg_gp);
        btn_img_gp_preview = (Button) findViewById(R.id.btn_img_preview_gp);
        img_reg_gp = (ImageView) findViewById(R.id.img_reg_gp);
        rv_select_users = (RecyclerView) findViewById(R.id.rv_select_users);
        rv_selected_users = (RecyclerView) findViewById(R.id.rv_selected_users);
        edt_gp_name = (EditText) findViewById(R.id.edt_gp_name);

        adapter_rv_select = new GroupAdapter();
        adapter_rv_selected = new GroupAdapter();

        rv_select_users.setAdapter(adapter_rv_select);
        rv_select_users.setLayoutManager(new LinearLayoutManager(this));

        rv_selected_users.setAdapter(adapter_rv_selected);
        rv_selected_users.setLayoutManager(new LinearLayoutManager(this));

        FirebaseFirestore.getInstance().collection("/users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        me = documentSnapshot.toObject(User.class);
                    }
                });


        adapter_rv_select.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                UserItem userItem = (UserItem) item;
                boolean has_selected = false;
                for(int i = 0; i < adapter_rv_selected.getItemCount(); i = i + 1){
                    if(adapter_rv_selected.getItem(i).equals(userItem)){
                        has_selected = true;
                        adapter_rv_selected.removeGroup(i);
                        gp_users.remove(userItem.user);
                    }
                }
                if(has_selected == false) {
                    adapter_rv_selected.add(userItem);
                    gp_users.add(userItem.user);
                }
            }
        });

        buscarUsuarios();
    }

    public void buscarUsuarios(){
        FirebaseFirestore.getInstance().collection("/users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            return;
                        }

                        List<DocumentSnapshot> docs = value.getDocuments();
                        for(DocumentSnapshot doc: docs){
                            User user = doc.toObject(User.class);
                            //Log.d("teste",user.getName());
                            String userid = FirebaseAuth.getInstance().getUid();
                            if(user.getId().equals(userid) == false) {
                                adapter_rv_select.add(new UserItem(user));
                            }
                            if(user.getId().equals(userid) == true){
                                gp_users.add(user);
                                adapter_rv_selected.add(new UserItem(user));
                            }
                        }
                    }
                });
    }

    private class UserItem extends Item<ViewHolder> {

        private User user;

        private UserItem(User user){
            this.user = user;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView txt_user = viewHolder.itemView.findViewById(R.id.friends_text_l);
            ImageView img_user = viewHolder.itemView.findViewById(R.id.friends_img_l);

            Picasso.get()
                    .load(user.getFotoUrl())
                    .into(img_user);

            txt_user.setText(user.getName());
        }

        @Override
        public int getLayout() {
            return R.layout.item_user;
        }
    }

    public void onClickBtnAddFoto(View v){

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //request 0 = solicitação de foto e conferindo se o usuario pegou uma foto mesmo ou cancelou o pick.
        if(requestCode == 0 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri_foto = data.getData();
            has_photo = true;
            try {
                Bitmap bitmap = null;
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri_foto);
                img_reg_gp.setImageDrawable(new BitmapDrawable(bitmap));
                btn_img_gp_preview.setAlpha(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onClickBtnRegGrupo(View v){
        String gp_name = edt_gp_name.getText().toString();
        if(adapter_rv_selected.getItemCount() < 3 || gp_name.isEmpty() || gp_name == null || has_photo == false){

            AlertDialog.Builder builder = new AlertDialog.Builder(gp_registro.this);
            builder.setCancelable(false);
            builder.setTitle(Html.fromHtml("<font color='#cc0000'>Falha</font>"));
            builder.setMessage("Minimo de 3 membros, deve ter foto e nome");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return;
        }

        new SaveUserInFirebase().execute("Saving GP");

    }

    public class SaveUserInFirebase extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = progressDialog.show(gp_registro.this, "Aguarde",
                    "Cadastrando...", true, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String gp_id = UUID.randomUUID().toString();
            String filename = UUID.randomUUID().toString();
            final StorageReference ref = FirebaseStorage.getInstance().getReference("/gp_images/" + filename);
            ref.putFile(uri_foto)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String id = gp_id;
                                    String nome = edt_gp_name.getText().toString();
                                    String fotoUrl = uri.toString();
                                    ArrayList<Message> messages = new ArrayList<>();

                                    Grupo grupo = new Grupo(id, nome, fotoUrl, gp_users, messages);

                                    for (int i = 0; i < gp_users.size(); i++) {
                                        int aux = i;

                                        FirebaseFirestore.getInstance().collection("grupos")
                                                .document(gp_users.get(i).getId())
                                                .collection("grupos")
                                                .document(gp_id)
                                                .set(grupo)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        //armazena ou atualiza os ids dos grupos
                                                        Log.d("teste", "cadastrei gp");
                                                        FirebaseFirestore.getInstance().collection("grupos")
                                                                .document(gp_users.get(aux).getId())
                                                                .collection("grupos_id")
                                                                .document(gp_users.get(aux).getId())
                                                                .get()
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                        if (documentSnapshot.exists()) {
                                                                            Log.d("teste", "existe");
                                                                            GP_ids old_gp_ids = documentSnapshot.toObject(GP_ids.class);
                                                                            ArrayList<String> ids = old_gp_ids.getIds();
                                                                            ids.add(gp_id);
                                                                            old_gp_ids.setIds(ids);

                                                                            FirebaseFirestore.getInstance().collection("grupos")
                                                                                    .document(gp_users.get(aux).getId())
                                                                                    .collection("grupos_id")
                                                                                    .document(gp_users.get(aux).getId())
                                                                                    .set(old_gp_ids);
                                                                        }
                                                                        else if (documentSnapshot.exists() == false) {
                                                                            Log.d("teste", "não existe");
                                                                            GP_ids new_gp_ids = new GP_ids();
                                                                            ArrayList<String> ids = new ArrayList<>();
                                                                            ids.add(gp_id);
                                                                            new_gp_ids.setIds(ids);
                                                                            FirebaseFirestore.getInstance().collection("grupos")
                                                                                    .document(gp_users.get(aux).getId())
                                                                                    .collection("grupos_id")
                                                                                    .document(gp_users.get(aux).getId())
                                                                                    .set(new_gp_ids);

                                                                        }
                                                                        Log.d("teste",gp_users.get(aux).getName() + " aux: " + aux);
                                                                        if(aux == gp_users.size() - 1){
                                                                            progressDialog.dismiss();
                                                                            AlertDialog.Builder builder = new AlertDialog.Builder(gp_registro.this);
                                                                            builder.setCancelable(false);
                                                                            builder.setTitle(Html.fromHtml("<font color='#509324'>Sucesso</font>"));
                                                                            builder.setMessage("Você se cadastrou!");
                                                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    dialog.dismiss();
                                                                                    Intent intent;
                                                                                    intent = new Intent(gp_registro.this, messages.class);
                                                                                    intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                    startActivity(intent);
                                                                                }
                                                                            });
                                                                            builder.show();
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                }
                            });
                        }
                    });
            return null;
        }
    }
}

