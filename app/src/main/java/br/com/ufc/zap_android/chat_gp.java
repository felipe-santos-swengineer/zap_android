package br.com.ufc.zap_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class chat_gp extends AppCompatActivity {

    GroupAdapter adapter;
    RecyclerView rv_gp;
    Button btn_enviar_gp;
    EditText edt_chat;
    User me;
    ArrayList<String> toId = new ArrayList<>();
    String gp_id;
    String gp_name;
    String uid;
    long tempo = (1000 * 2); //2 segundos antes de cada update de chat

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, messages.class);
        intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
        overridePendingTransition(0,0);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_gp);

        gp_id = getIntent().getExtras().getString("id");
        gp_name = getIntent().getExtras().getString("name");
        getSupportActionBar().setTitle(gp_name);

        uid = FirebaseAuth.getInstance().getUid();
        Log.d("teste", "Rodando grupo: " + gp_id + " " + gp_name);

        edt_chat = findViewById(R.id.edt_gp_msg);
        btn_enviar_gp = (Button) findViewById(R.id.btn_enviar_gp);


        rv_gp = (RecyclerView) findViewById(R.id.rv_gp);
        adapter = new GroupAdapter();
        rv_gp.setLayoutManager(new LinearLayoutManager(chat_gp.this));
        rv_gp.setAdapter(adapter);

        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        me = documentSnapshot.toObject(User.class);
                        buscarMensagens();
                        //Log.d("teste", "me: " + me.getName());
                    }
                });



    }

    private class MessageItem extends Item<ViewHolder>{

        private Message message;
        private String sender_img_url;

        private MessageItem(Message message, String sender_img_url){
            this.message = message;
            this.sender_img_url = sender_img_url;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView text_msg = viewHolder.itemView.findViewById(R.id.txt_message_user);
            ImageView img_msg = viewHolder.itemView.findViewById(R.id.img_message_user);

            if(message.getFromId().equals(FirebaseAuth.getInstance().getUid())) {
                text_msg.setText(message.getText());
                Picasso.get()
                        .load(me.getFotoUrl())
                        .into(img_msg);
            }
            else {
                text_msg.setText(message.getText());
                Picasso.get()
                        .load(sender_img_url)
                        .into(img_msg);
            }
        }

        @Override
        public int getLayout() {
            return message.getFromId().equals(FirebaseAuth.getInstance().getUid()) ? R.layout.item_to_message : R.layout.item_from_message;
        }
    }

    public void onClickBtnEnviar(View v){
        String text = edt_chat.getText().toString();

        if(text.isEmpty())
            return;

        edt_chat.setText("");

        String fromId = FirebaseAuth.getInstance().getUid();
        long timeStamp = System.currentTimeMillis();

        Message message = new Message();
        message.setFromId(fromId);
        message.setToId("");
        message.setText(text);
        message.setTimeStamp(timeStamp);

        if (message.getText().isEmpty() == false) {

            //update inf firebase
            String uid = FirebaseAuth.getInstance().getUid();
            FirebaseFirestore.getInstance().collection("grupos")
                    .document(uid)
                    .collection("grupos")
                    .document(gp_id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){
                                Grupo grupo_users = documentSnapshot.toObject(Grupo.class);
                                for(int i = 0; i < grupo_users.getUsers().size();i++) {
                                    FirebaseFirestore.getInstance().collection("grupos")
                                            .document(grupo_users.getUsers().get(i).getId())
                                            .collection("grupos")
                                            .document(gp_id)
                                            .collection("mensagens")
                                            .add(message);
                                }
                            }
                        }
                    });
        }
    }

    public void buscarMensagens() {
        FirebaseFirestore.getInstance().collection("grupos")
                .document(uid)
                .collection("grupos")
                .document(gp_id)
                .collection("mensagens")
                .orderBy("timeStamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        List<DocumentChange> documentChanges = value.getDocumentChanges();
                        if (documentChanges != null) {
                            for (DocumentChange doc : documentChanges) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Message message = doc.getDocument().toObject(Message.class);
                                    FirebaseFirestore.getInstance().collection("grupos")
                                            .document(uid)
                                            .collection("grupos")
                                            .document(gp_id)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if(documentSnapshot.exists()){
                                                        Grupo grupo = documentSnapshot.toObject(Grupo.class);
                                                        for(int i = 0; i < grupo.getUsers().size(); i++){
                                                            if(grupo.getUsers().get(i).getId().equals(message.getFromId())){
                                                                adapter.add(new MessageItem(message,grupo.getUsers().get(i).getFotoUrl()));
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }
}