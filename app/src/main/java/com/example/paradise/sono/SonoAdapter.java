package com.example.paradise.sono;

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
import java.util.TimeZone;

public class SonoAdapter extends RecyclerView.Adapter<SonoAdapter.SonoViewHolder> {

    private List<SonoModel> lista;
    private Context context;
    private SimpleDateFormat dateFormat;
    private OnSonoClickListener listener;
    public SonoAdapter(List<SonoModel> lista, Context context, OnSonoClickListener listener) {
        this.lista = lista;
        this.context = context;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        this.dateFormat.setTimeZone(TimeZone.getDefault());
    }

    public void atualizarLista(List<SonoModel> novaLista) {
        this.lista = novaLista;
        notifyDataSetChanged();
    }

    public List<SonoModel> getLista() {
        return lista;
    }

    @NonNull
    @Override
    public SonoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sono, parent, false);
        return new SonoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SonoViewHolder holder, int position) {
        SonoModel sono = lista.get(position);

        // Formata período
        String inicio = dateFormat.format(sono.getInicio().toDate());
        String fim = sono.getFim() != null ?
                dateFormat.format(sono.getFim().toDate()) : "Em andamento";

        holder.txtPeriodo.setText(inicio + " - " + fim);

        // Formata duração
        if (sono.getDuracaoMinutos() != null) {
            holder.txtDuracao.setText("Duração: " + formatarDuracao(sono.getDuracaoMinutos()));
        } else {
            holder.txtDuracao.setText("Duração: --");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSonoClick(sono);
            }
        });
    }

    private String formatarDuracao(Long minutos) {
        if (minutos == null) return "--";
        if (minutos < 60) return minutos + " min";
        long horas = minutos / 60;
        long minsRestantes = minutos % 60;
        if (minsRestantes == 0) return horas + "h";
        return String.format(Locale.getDefault(), "%dh%02dmin", horas, minsRestantes);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class SonoViewHolder extends RecyclerView.ViewHolder {
        TextView txtPeriodo, txtDuracao;

        public SonoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPeriodo = itemView.findViewById(R.id.txt_periodo);
            txtDuracao = itemView.findViewById(R.id.txt_duracao);
        }
    }
    public interface OnSonoClickListener {
        void onSonoClick(SonoModel sono);
    }
}