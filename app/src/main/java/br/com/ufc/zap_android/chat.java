package br.com.ufc.zap_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.List;

public class chat extends AppCompatActivity {

    private GroupAdapter adapter;
    private RecyclerView rv_chat;
    private User user;
    private Button btn_enviar;
    private EditText edt_chat;
    private User me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        user = getIntent().getExtras().getParcelable("user");
        getSupportActionBar().setTitle(user.getName());

        edt_chat = (EditText) findViewById(R.id.edt_chat);
        btn_enviar = (Button) findViewById(R.id.btn_enviar);

        rv_chat = (RecyclerView) findViewById(R.id.rv_chat);
        adapter = new GroupAdapter();
        rv_chat.setLayoutManager(new LinearLayoutManager(this));
        rv_chat.setAdapter(adapter);

        FirebaseFirestore.getInstance().collection("/users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        me = documentSnapshot.toObject(User.class);
                        buscarMensagens();
                    }
                });

    }

    private class MessageItem extends Item<ViewHolder>{

        private Message message;

        private MessageItem(Message message){
            this.message = message;
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
                        .load(user.getFotoUrl())
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
        edt_chat.setText(null);

        String fromId = FirebaseAuth.getInstance().getUid();
        String toId = user.getId();
        long timeStamp = System.currentTimeMillis();

        Message message = new Message();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setText(text);
        message.setTimeStamp(timeStamp);

        if(!message.getText().isEmpty()){
            //Eu para outro
            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(fromId)
                    .collection(toId)
                    .add(message)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Contato contato = new Contato();
                            contato.setId(toId);
                            contato.setLastMessage(message.getText());
                            contato.setNome(user.getName());
                            contato.setTimeStamp(message.getTimeStamp());
                            contato.setUrlFoto(user.getFotoUrl());

                            FirebaseFirestore.getInstance().collection("/last-messages")
                                    .document(fromId)
                                    .collection("contatos")
                                    .document(toId)
                                    .set(contato);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
            //outro para mim
            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(toId)
                    .collection(fromId)
                    .add(message)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Contato contato = new Contato();
                            contato.setId(fromId);
                            contato.setLastMessage(message.getText());
                            contato.setNome(me.getName());
                            contato.setTimeStamp(message.getTimeStamp());
                            contato.setUrlFoto(me.getFotoUrl());

                            FirebaseFirestore.getInstance().collection("/last-messages")
                                    .document(toId)
                                    .collection("contatos")
                                    .document(fromId)
                                    .set(contato);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    public void buscarMensagens() {
        if (me != null) {
            String fromId = me.getId();
            String toId = user.getId();

            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(fromId)
                    .collection(toId)
                    .orderBy("timeStamp", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            List<DocumentChange> documentChanges = value.getDocumentChanges();

                            if (documentChanges != null) {
                                for (DocumentChange doc : documentChanges) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        Message message = doc.getDocument().toObject(Message.class);
                                        adapter.add(new MessageItem(message));
                                    }
                                }
                            }
                        }
                    });
        }
    }

}