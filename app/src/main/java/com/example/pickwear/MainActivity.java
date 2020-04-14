package com.example.pickwear;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ParseUser meuUsuario;
    private ArrayAdapter<ParseObject> arrayAdapter;
    private ArrayList<ParseObject> arrayList;
    private Integer skip = 0;
    private Switch meuSwitch;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        meuSwitch = findViewById(R.id.meuSwitchId);

        sharedPreferences = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("genero")) {
            boolean meuBoolean = sharedPreferences.getBoolean("genero", true);
            meuSwitch.setChecked(meuBoolean);
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("genero", true);
            editor.apply();
            meuSwitch.setChecked(true);
        }

        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.d("MyApp", "Anonymous login failed.");
                } else {
                    Log.d("MyApp", "Anonymous user logged in.");
                }
            }
        });

        meuUsuario = ParseUser.getCurrentUser();
        try {
            Log.i("usuario", meuUsuario.getObjectId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //add the view via xml or programmatically
        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        //choose your favorite adapter
        arrayList = new ArrayList<>();
        arrayAdapter = new ProdutoAdapter(getApplicationContext(), arrayList);

        String s = "primeiraBusca";
        buscarProdutos(skip, s, meuSwitch.isChecked());

        meuSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("genero", meuSwitch.isChecked());
                editor.apply();

                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        //set the listener and the adapter
        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                arrayList.remove(0);
                arrayAdapter.notifyDataSetChanged();
                if (arrayList.isEmpty()){
                    criarAlertDialogBusca();
                }
            }

            @Override
            public void onLeftCardExit(Object o) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                final ParseObject livroDislike = (ParseObject) o;
                Toast.makeText(MainActivity.this, "Não curtiu", Toast.LENGTH_SHORT).show();
                darDislike(livroDislike);
            }
             @Override
             public void onRightCardExit(Object o) {
                 final ParseObject produto = (ParseObject) o;
                 Toast.makeText(MainActivity.this, "Curtiu", Toast.LENGTH_SHORT).show();
                 darLike(produto);
            }

            @Override
            public void onAdapterAboutToEmpty(int i) {
                // Ask for more data here
                String s = "";
                skip = skip + 10;
                buscarProdutos(skip, s, meuSwitch.isChecked());
            }

            @Override
            public void onScroll(float v) {

            }
            });

                // Optionally add an OnItemClickListener
                flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClicked(int itemPosition, Object dataObject) {
                        ParseObject object = (ParseObject) dataObject;
                        try {
                            Toast.makeText(MainActivity.this, object.getString("titulo"), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }

    // Métodos da toolbar <-------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meusLikes:
                startActivity(new Intent(getApplicationContext(), MeusLikesActivity.class));
                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }
    // Métodos da toolbar <-------------------------------------------------------------------------

    private void apagarProdutosAntigos () {
        Log.i("apagarProdutosAntigos", "acionado");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -30);

        ParseQuery<ParseObject> dislikes = ParseQuery.getQuery("Dislikes");
        dislikes.whereLessThan("createdAt", calendar.getTime());
        dislikes.whereEqualTo("usuario", meuUsuario);
        dislikes.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null){
                    if (!objects.isEmpty()){
                        for (int i = 0; i < objects.size(); i++){
                            ParseObject object = objects.get(i);
                            String produtoId = "";
                            try {
                                produtoId = object.get("produto").toString();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            ParseQuery<ParseObject> produto = ParseQuery.getQuery("Produtos");
                            produto.getInBackground(produtoId, new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException e) {
                                    Log.i("BuscaDislikeExclusão", "entrounaSegundabusca");
                                    if (object != null){
                                        Log.i("BuscaDislikeExclusão", "buscaNaoDeuNull");
                                        ParseRelation<ParseObject> novaRelation = object.getRelation("disLikes");
                                        novaRelation.remove(meuUsuario);
                                        object.saveInBackground();
                                    } else {
                                        Log.i("BuscaDislikeExclusão", "buscaDeuNull");
                                    }
                                }
                            });
                            object.deleteInBackground();
                        }
                    }
                }
            }
        });
    }

    private void darLike (ParseObject objetoProduto) {
        final ParseObject produtoRecebido = objetoProduto;

        // Coloca o like no produto ----------------------------------------------------------------
        ParseQuery<ParseObject> acharProduto = ParseQuery.getQuery("Produtos");
        acharProduto.getInBackground(produtoRecebido.getObjectId(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                ParseRelation<ParseObject> relation = object.getRelation("likes");
                relation.add(meuUsuario);
                object.increment("likesContagem");
                object.saveInBackground();
            }
        });
        // Coloca o like no produto ----------------------------------------------------------------

        // Cria o objeto Likes -------------------------------------------------------------
        ParseQuery<ParseObject> buscaLike = ParseQuery.getQuery("Likes");
        buscaLike.whereEqualTo("produto", produtoRecebido);
        buscaLike.whereEqualTo("usuario", meuUsuario);
        buscaLike.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null){
                    if (objects.isEmpty()){
                        Log.i("DarLike","veio empty");
                        ParseObject likes = new ParseObject("Likes");
                        likes.put("usuario", meuUsuario);
                        likes.put("produto", produtoRecebido);
                        likes.saveInBackground();
                    }
                    Log.i("DarLike","nao veio empty");
                }
            }
        });
        // Cria o objeto Likes -------------------------------------------------------------
    }

    private void darDislike (ParseObject objetoProduto) {
        final ParseObject produtoRecebido = objetoProduto;

        // Cria o objeto DisLikes ------------------------------------------------------------------
        ParseQuery<ParseObject> buscaDisLike = ParseQuery.getQuery("Dislikes");
        buscaDisLike.whereEqualTo("produto", produtoRecebido);
        buscaDisLike.whereEqualTo("usuario", meuUsuario);
        buscaDisLike.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null){
                    Log.i("darDislike","diferente de null");
                    if (objects.isEmpty()){
                        Log.i("darDislike","vazio");
                        ParseObject disLikes = new ParseObject("Dislikes");
                        disLikes.put("usuario", meuUsuario);
                        disLikes.put("produto", produtoRecebido);
                        disLikes.saveInBackground();
                    }
                    Log.i("darDislike","nao vazio");
                }
                Log.i("darDislike","null");
            }
        });
        // Cria o objeto DisLikes ------------------------------------------------------------------

        // Cria o relation DisLikes no produto -----------------------------------------------------
        ParseQuery<ParseObject> acharProduto = ParseQuery.getQuery("Produtos");
        acharProduto.getInBackground(objetoProduto.getObjectId(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    if (object != null) {
                        ParseRelation<ParseObject> relation = object.getRelation("disLikes");
                        relation.add(meuUsuario);
                        object.saveInBackground();
                    }
                }
            }
        });
        // Cria o relation DisLikes no produto -----------------------------------------------------
    }

    private void buscarProdutos(Integer skip, String busca, Boolean feminino){
        List<ParseObject> listaBusca = null;
        String genero;
        if (feminino){
            genero = "feminino";
        } else {
            genero = "masculino";
        }
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Produtos");
        query.whereEqualTo("genero", genero);
        query.whereNotEqualTo("likes", meuUsuario);
        query.whereNotEqualTo("disLikes", meuUsuario);
        query.orderByDescending("likesContagem");
        query.setSkip(skip);
        query.setLimit(10);
        try {
            listaBusca = query.find();
        }catch (Exception e){
            e.printStackTrace();
            Log.i("buscarProdutos", "erro");
        }
        if (listaBusca != null){
            if (!listaBusca.isEmpty()){
                arrayList.clear();
                arrayList.addAll(listaBusca);
                arrayAdapter.notifyDataSetChanged();
                Log.i("listaBusca", "Não empty");
            } else {
                apagarProdutosAntigos();
                Log.i("listaBusca", "Empty");
            }
            Log.i("listaBusca", "Não null");
        } else {
            Log.i("listaBusca", "null");
        }

        String s = "primeiraBusca";
        if (busca.equals(s)){
            if (listaBusca != null){
                if (listaBusca.isEmpty()){
                    criarAlertDialogBusca();
                }

            }
        }

    }

    public void criarAlertDialogBusca(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Sua busca não retornou resultado.");
        alertDialog.setMessage("Ainda não temos produtos na sua área! Tente novamente.");
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.create();
        alertDialog.show();
    }

}
