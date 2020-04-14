package com.example.pickwear;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class VerMeusLikesAdaptador extends RecyclerView.Adapter<VerMeusLikesAdaptador.MyViewHolder> {

    private ArrayList<ParseObject> produtos;
    private Context contexto;

    public VerMeusLikesAdaptador(ArrayList<ParseObject> objects, Context context) {
        this.produtos = objects;
        this.contexto = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(contexto).inflate(R.layout.adapter_ver_meus_likes, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final ParseObject objeto = produtos.get(position);
        holder.titulo.setText(objeto.get("titulo").toString());

        String link = objeto.getString("linkImagem");
        if (link != null) {
            if (!link.isEmpty()) {
                Picasso.get()
                        .load(link)
                        .resize(50, 50)
                        .centerCrop()
                        .into(holder.capa);
            } else {
                if (objeto.getParseFile("imagem") != null) {
                    Picasso.get()
                            .load(objeto.getParseFile("imagem").getUrl())
                            .resize(50, 50)
                            .centerCrop()
                            .into(holder.capa);
                }
            }
        } else {
            if (objeto.getParseFile("imagem") != null){
                Picasso.get()
                        .load(objeto.getParseFile("imagem").getUrl())
                        .resize(50, 50)
                        .centerCrop()
                        .into(holder.capa);
        }

        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert(objeto);
            }
        });

    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titulo;
        ImageView capa;
        LinearLayout layout;

        public MyViewHolder(View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.tituloProduto);
            capa = itemView.findViewById(R.id.imagemProduto);
            layout = itemView.findViewById(R.id.linearLayout);
        }
    }

    private void alert (final ParseObject object) {
        AlertDialog.Builder alert = new AlertDialog.Builder(contexto);
        alert.setTitle("Mais informações");
        alert.setMessage("Olá, você será redirecionado para o site de nosso parceiro para obter mais informações.");
        alert.setCancelable(true);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                redirecionarSite(object);
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void redirecionarSite (ParseObject objeto) {
        String string = objeto.getString("linkProduto");
        Uri uri = Uri.parse(string);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        contexto.startActivity(intent);
    }

}