package com.example.pickwear;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProdutoAdapter extends ArrayAdapter<ParseObject> {

    private Context contexto;
    private ArrayList<ParseObject> produtos;
    private TextView mostraProdutos;
    private ImageView mostraImagem;

    public ProdutoAdapter(@NonNull Context context, ArrayList<ParseObject> list) {
        super(context, 0, list);
        this.contexto = context;
        this.produtos = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            try {
                LayoutInflater inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.item, parent, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ParseObject meusProdutos = produtos.get(position);
        if (view != null) {
            mostraProdutos = view.findViewById(R.id.tituloProduto);
            mostraImagem = view.findViewById(R.id.imagemProdutoItem);
        }
        mostraProdutos.setText(meusProdutos.getString("titulo"));

        String link = meusProdutos.getString("linkImagem");
        if (link != null) {
            if (!link.isEmpty()) {
                Log.i("imagemCard: ", "Do Link");
                Picasso.get()
                        .load(link)
                        .resize(480, 640)
                        .centerCrop()
                        .into(mostraImagem);
            } else {
                if (meusProdutos.getParseFile("imagem") != null) {
                    Log.i("imagemCard: ", "Do back4app");
                    Picasso.get()
                            .load(meusProdutos.getParseFile("imagem").getUrl())
                            .resize(480, 640)
                            .centerCrop()
                            .into(mostraImagem);
                }
            }
        } else {
            if (meusProdutos.getParseFile("imagem") != null) {
                Log.i("imagemCard: ", "Do back4app");
                Picasso.get()
                        .load(meusProdutos.getParseFile("imagem").getUrl())
                        .resize(480, 640)
                        .centerCrop()
                        .into(mostraImagem);
            }


        }
        return view;
    }
}
