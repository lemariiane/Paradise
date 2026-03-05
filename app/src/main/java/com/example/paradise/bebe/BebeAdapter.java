package com.example.paradise.bebe;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paradise.R;
import com.example.paradise.bebe.BebeModel;

import java.util.List;

public class BebeAdapter extends RecyclerView.Adapter<BebeAdapter.BebeViewHolder> {

    private List<BebeModel> lista;
    private Context context;
    private OnBebeClickListener listener;

    public interface OnBebeClickListener {
        void onAction(BebeModel bebe, String acao);
    }

    public BebeAdapter(List<BebeModel> lista, Context context, OnBebeClickListener listener) {
        this.lista = lista;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BebeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bebe, parent, false);
        return new BebeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BebeViewHolder holder, int position) {
        BebeModel bebe = lista.get(position);

        holder.nome.setText(bebe.getNome());
        holder.data_nasc.setText("Nascimento: " + bebe.getDataNascimento());

        // Ações de excluir e editar
        holder.btnExcluir.setOnClickListener(v -> listener.onAction(bebe, "EXCLUIR"));
        holder.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(context, CadastroBebeActivity.class);
            intent.putExtra("BEBE_ID", bebe.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class BebeViewHolder extends RecyclerView.ViewHolder {
        TextView nome, data_nasc;
        ImageButton btnEditar, btnExcluir;

        public BebeViewHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.nome_bebe);
            data_nasc = itemView.findViewById(R.id.data_nasc);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnExcluir = itemView.findViewById(R.id.btn_excluir);
        }
    }
}