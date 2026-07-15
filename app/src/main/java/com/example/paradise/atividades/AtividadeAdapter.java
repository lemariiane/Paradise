package com.example.paradise.atividades;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paradise.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AtividadeAdapter extends RecyclerView.Adapter<AtividadeAdapter.AtividadeViewHolder> {

    private List<AtividadeModel> lista;
    private Context context;
    private SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    {
        dateFormat.setTimeZone(java.util.TimeZone.getDefault()); // usar fuso horário do dispositivo
    }

    private OnAtividadeClickListener listener;

    public AtividadeAdapter(List<AtividadeModel> lista, Context context, OnAtividadeClickListener listener) {
        this.lista = lista;
        this.context = context;
        this.listener = listener;
    }

    public void atualizarLista(List<AtividadeModel> novaLista) {
        this.lista = novaLista;
        notifyDataSetChanged();
    }

    public List<AtividadeModel> getLista() {
        return lista;
    }

    @NonNull
    @Override
    public AtividadeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_atividade, parent, false);
        return new AtividadeViewHolder(view);
    }

    //criando o layout para cada item da lista
    @Override
    public void onBindViewHolder(@NonNull AtividadeViewHolder holder, int position) {

        AtividadeModel atividade = lista.get(position);

        holder.txtNome.setText(atividade.getNome());

        if (atividade.getDataHora() != null) {
            String data = dateFormat.format(atividade.getDataHora().toDate());
            holder.txtData.setText(data);
        } else {
            holder.txtData.setText("--");
        }


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAtividadeClick(atividade);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class AtividadeViewHolder extends RecyclerView.ViewHolder {

        TextView txtNome, txtData;

        public AtividadeViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txt_nome);
            txtData = itemView.findViewById(R.id.txt_data);
        }
    }

    public interface OnAtividadeClickListener {
        void onAtividadeClick(AtividadeModel atividade);
    }
}