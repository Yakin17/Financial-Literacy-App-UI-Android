package com.example.financial_app.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financial_app.R;
import com.example.financial_app.model.Article;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {
    private Context context;
    private List<Article> articles;
    private OnArticleClickListener listener;

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
    }

    public ArticleAdapter(Context context, List<Article> articles, OnArticleClickListener listener) {
        this.context = context;
        this.articles = articles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articles.get(position);

        // Nettoyer et formater le texte pour gérer les caractères spéciaux
        String cleanTitle = cleanText(article.getTitre());
        String cleanAuthor = cleanText(article.getAuteurNom());

        holder.textViewTitle.setText(cleanTitle);
        holder.textViewAuthor.setText("Par " + cleanAuthor);

        // Format the date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        String formattedDate = article.getDatePublication().format(formatter);
        holder.textViewDate.setText(formattedDate);

        // Show or hide quiz badge
        if (article.isHasQuiz()) {
            holder.textViewQuiz.setVisibility(View.VISIBLE);
        } else {
            holder.textViewQuiz.setVisibility(View.GONE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onArticleClick(article);
                }
            }
        });
    }

    /**
     * Nettoie le texte en remplaçant les caractères spéciaux problématiques
     */
    private String cleanText(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }

        // Ne garder que le nettoyage des caractères HTML spéciaux
        String cleanText = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");

        return cleanText;
    }

    @Override
    public int getItemCount() {
        return articles != null ? articles.size() : 0;
    }

    public void updateArticles(List<Article> newArticles) {
        this.articles = newArticles;
        notifyDataSetChanged();
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewAuthor, textViewDate, textViewQuiz;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewAuthor = itemView.findViewById(R.id.textViewAuthor);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewQuiz = itemView.findViewById(R.id.textViewQuiz);
        }
    }
}