package br.com.ufc.zap_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.util.ArrayList;
import java.util.List;


public class messages extends AppCompatActivity {

    RecyclerView rv_lastm;
    GroupAdapter adapter;
    GroupAdapter adapter_gp_view;
    RecyclerView rv_view_gp;
    boolean is_gp = false;
    ArrayList<Grupo> grupos = new ArrayList<>();
    GP_ids gp_ids;
    ArrayList<String> ids;
    boolean end_function = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyAuth();
        setContentView(R.layout.activity_messages);
        getSupportActionBar().setTitle("Mensagens");

        adapter = new GroupAdapter();
        rv_lastm = (RecyclerView) findViewById(R.id.rv_lastm);
        rv_lastm.setAdapter(adapter);
        rv_lastm.setLayoutManager(new LinearLayoutManager(this));

        rv_view_gp = (RecyclerView) findViewById(R.id.rv_view_gp);
        adapter_gp_view = new GroupAdapter();
        rv_view_gp.setAdapter(adapter_gp_view);
        rv_view_gp.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                ContatoItem contatoitem = (ContatoItem) item;
                is_gp = false;
                iniciar_conversa(contatoitem,is_gp);
            }
        });

        adapter_gp_view.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                ContatoItem contatoitem = (ContatoItem) item;
                is_gp = true;
                iniciar_conversa(contatoitem, is_gp);
            }
        });

        buscarMensagemAtual();
        buscarGrupos();
    }

    public void verifyAuth(){
        //testa se o usuario não está logado
        if(FirebaseAuth.getInstance().getUid() == null){
            Intent intent = new Intent(this, login.class);
            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
            overridePendingTransition(0,0);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.contatos:
                Intent intent = new Intent(this, contatos.class);
                startActivity(intent);
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                verifyAuth();
                break;
            case R.id.create_gp:
                Intent intent2 = new Intent(this, gp_registro.class);
                startActivity(intent2);
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    public void iniciar_conversa(ContatoItem contatoitem, boolean is_gp){
        boolean has_conversation;
        Intent intent =  new Intent(this, chat.class);
        Intent intent2 =  new Intent(this, chat_gp.class);
        String contatoId = contatoitem.contato.getId();
        if(is_gp == false) {
            FirebaseFirestore.getInstance().collection("/users")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                return;
                            }
                            List<DocumentSnapshot> docs = value.getDocuments();
                            for (DocumentSnapshot doc : docs) {
                                User user = doc.toObject(User.class);
                                //Log.d("teste",user.getName());
                                String userid = FirebaseAuth.getInstance().getUid();
                                if (user.getId().equals(userid) == false && user.getId().equals(contatoId)) {
                                    intent.putExtra("user", user);
                                    startActivity(intent);
                                }
                            }
                        }
                    });
        }
        else{
            String uid = FirebaseAuth.getInstance().getUid();
            FirebaseFirestore.getInstance().collection("grupos")
                    .document(uid)
                    .collection("grupos")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if(error != null){
                                return;
                            }

                            List<DocumentSnapshot> docs = value.getDocuments();
                            for (DocumentSnapshot doc : docs) {
                                Grupo grupo = doc.toObject(Grupo.class);
                                //Log.d("teste",user.getName());
                                String userid = FirebaseAuth.getInstance().getUid();
                                if (grupo.getId().equals(contatoId) && grupo != null) {
                                    //Log.d("teste","Indo para chat em grupo,Cliquei em: " + contatoitem.contato.getNome());
                                    //Log.d("teste","Indo para chat em grupo, encontrei: " + grupo.getName());
                                    intent2.putExtra("id",grupo.getId());
                                    intent2.putExtra("name",grupo.getName());
                                    startActivity(intent2);
                                }
                            }
                        }
                    });
        }
    }

    private class ContatoItem extends Item<ViewHolder> {
        private Contato contato;

        private ContatoItem(Contato contato){
            this.contato = contato;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {

            TextView txt_name = viewHolder.itemView.findViewById(R.id.friends_name);
            TextView txt_message = viewHolder.itemView.findViewById(R.id.friends_msg);
            ImageView img_message = viewHolder.itemView.findViewById(R.id.friends_img);

            txt_name.setText(contato.getNome());
            txt_message.setText(contato.getLastMessage());
            Picasso.get()
                    .load(contato.getUrlFoto())
                    .into(img_message);
        }

        @Override
        public int getLayout() {
            return R.layout.item_user_lm;
        }
    }

    public void buscarMensagemAtual(){
        String id = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection("/last-messages")
                .document(id)
                .collection("contatos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if (error != null) {
                            return;
                        }

                        List<DocumentChange> documentChanges = value.getDocumentChanges();
                        if(documentChanges != null){
                            for(DocumentChange doc: documentChanges){
                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    doc.getDocument().toObject(Contato.class);
                                    Contato contato = doc.getDocument().toObject(Contato.class);
                                    adapter.add(new ContatoItem(contato));
                                }
                            }
                        }
                    }
                });
    }

    public void buscarGrupos() {
        //variavel de controle
        end_function = false;
        //pegar lista de ids dos grupos
        String id = FirebaseAuth.getInstance().getUid();
        //Log.d("teste",id);

        FirebaseFirestore.getInstance().collection("grupos")
                .document(id)
                .collection("grupos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        List<DocumentChange> documentChanges = value.getDocumentChanges();
                        if(documentChanges != null){
                            for(DocumentChange doc: documentChanges){
                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    doc.getDocument().toObject(Grupo.class);
                                    Grupo grupo = doc.getDocument().toObject(Grupo.class);
                                    for(int i = 0; i < grupo.getUsers().size(); i++){
                                        if(grupo.getUsers().get(i).getId().equals(id)){
                                            grupos.add(grupo);
                                            gp_to_contatos(grupo);
                                            //Log.d("teste","Buscar grupo(): " + grupo.getName());
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
        //Log.d("teste", "gps terminados");
    }


    public void gp_to_contatos(Grupo grupo){
        //ultima mensagem do grupo
        ArrayList<Message> msgs = grupo.getMessages();

        Message last_msg = new Message();
        last_msg.setText("");
        if(msgs.size() > 1) {
            last_msg = msgs.get(msgs.size() - 1);
        }

        //criando contato do  grupo
        Contato contato = new Contato();
        contato.setUrlFoto(grupo.getFotoUrl());
        contato.setId(grupo.getId());
        contato.setNome(grupo.getName());
        contato.setLastMessage(last_msg.getText());
        contato.setTimeStamp(last_msg.getTimeStamp());
        adapter_gp_view.add(new ContatoItem(contato));
    }
}