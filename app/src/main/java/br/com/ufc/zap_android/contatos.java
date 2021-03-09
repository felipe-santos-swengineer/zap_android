package br.com.ufc.zap_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
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

import java.util.List;

public class contatos extends AppCompatActivity {

    String error_msg;
    GroupAdapter adapter;
    RecyclerView rv;

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
        setContentView(R.layout.activity_contatos);
        getSupportActionBar().setTitle("Contatos");

        rv = (RecyclerView) findViewById(R.id.rv);
        adapter = new GroupAdapter();
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                UserItem userItem = (UserItem) item;
                iniciar_conversa(userItem);
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
                        error_msg = error.getMessage();
                        return;
                    }

                    List<DocumentSnapshot> docs = value.getDocuments();
                    for(DocumentSnapshot doc: docs){
                        User user = doc.toObject(User.class);
                        //Log.d("teste",user.getName());
                        String userid = FirebaseAuth.getInstance().getUid();
                        if(user.getId().equals(userid) == false) {
                            adapter.add(new UserItem(user));
                        }
                    }
                }
            });
    }

    private class UserItem extends Item<ViewHolder>{

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

    public void iniciar_conversa(UserItem userItem){
        Intent intent =  new Intent(this, chat.class);
        intent.putExtra("user",userItem.user);
        startActivity(intent);
    }
}