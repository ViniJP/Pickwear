package com.example.pickwear;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MeusLikesActivity extends AppCompatActivity {

    private ParseUser meuUsuario = ParseUser.getCurrentUser();
    private RecyclerView meuRecycler;
    private ArrayList<ParseObject> produtos;
    private VerMeusLikesAdaptador adaptador;
    private LinearLayout naoTemLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_likes);

        meuRecycler = findViewById(R.id.recycler_meus_likes);
        naoTemLike = findViewById(R.id.linear_nao_like);
        produtos = new ArrayList<>();
        adaptador = new VerMeusLikesAdaptador(produtos, MeusLikesActivity.this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        meuRecycler.setLayoutManager(layoutManager);
        meuRecycler.setHasFixedSize(true);
        meuRecycler.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
        meuRecycler.setAdapter(adaptador);
        buscarProdutos();
    }

    private void buscarProdutos(){
        produtos.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");
        query.whereEqualTo("usuario", meuUsuario);
        query.orderByDescending("createdAt");
        query.include("produto");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects != null) {
                        Log.i("MeusLikes", "busca nao deu null");
                        if (!objects.isEmpty()){
                            for (int i = 0; i < objects.size(); i++) {
                                ParseObject objeto = objects.get(i);
                                ParseObject objetoProduto = objeto.getParseObject("produto");
                                produtos.add(objetoProduto);
                                adaptador.notifyItemInserted(i);
                            }
                            Log.i("MeusLikes", "nao deu empty");
                        } else {
                            mostrarMensagemEmpty();
                            Log.i("MeusLikes", "deu empty");
                        }
                    } else {
                        Log.i("MeusLikes", "deu null");
                    }
                }
            }
        });

    }

    private void mostrarMensagemEmpty() {
        naoTemLike.setVisibility(View.VISIBLE);
    }

}
