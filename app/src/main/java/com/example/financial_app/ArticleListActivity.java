package com.example.financial_app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financial_app.adapter.ArticleAdapter;
import com.example.financial_app.model.Article;
import com.example.financial_app.service.ArticleService;

import java.util.ArrayList;
import java.util.List;

public class ArticleListActivity extends AppCompatActivity implements ArticleAdapter.OnArticleClickListener{
    private static final String TAG = "ArticleListActivity";
    private RecyclerView recyclerViewArticles;
    private ArticleAdapter articleAdapter;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private ImageButton backButton;
    private List<Article> articleList;
    private ArticleService articleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_article_list);

        recyclerViewArticles = findViewById(R.id.recyclerViewArticles);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        backButton = findViewById(R.id.backButton);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recyclerViewArticles.setLayoutManager(new LinearLayoutManager(this));
        articleList = new ArrayList<>();
        articleAdapter = new ArticleAdapter(this, articleList, this);
        recyclerViewArticles.setAdapter(articleAdapter);

        articleService = new ArticleService(this);



        // Puis essayer avec les vraies données
         loadArticles();
    }

    private void testWithMockData() {
        Log.d(TAG, "Chargement des données simulées...");
        showLoading(true);

        articleService.getMockArticles(new ArticleService.ArticleCallback() {
            @Override
            public void onSuccess(List<Article> articles) {
                Log.d(TAG, "Données simulées chargées avec succès: " + articles.size() + " articles");
                updateUI(articles);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Erreur lors du chargement des données simulées: " + message);
                showError(message);
            }
        });
    }

    private void loadArticles() {
        Log.d(TAG, "Chargement des articles depuis l'API...");
        showLoading(true);

        articleService.getAllArticles(new ArticleService.ArticleCallback() {
            @Override
            public void onSuccess(List<Article> articles) {
                Log.d(TAG, "Articles chargés avec succès: " + articles.size() + " articles");
                updateUI(articles);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Erreur lors du chargement des articles: " + message);
                showError(message);

                // Si l'API échoue, on essaie avec les données simulées
                Log.d(TAG, "Tentative avec les données simulées...");
                testWithMockData();
            }
        });
    }

    private void updateUI(List<Article> articles) {
        showLoading(false);
        if (articles.isEmpty()) {
            Log.d(TAG, "Aucun article à afficher, affichage de l'état vide");
            showEmptyState(true);
        } else {
            Log.d(TAG, "Affichage de " + articles.size() + " articles");
            showEmptyState(false);
            articleList.clear();
            articleList.addAll(articles);
            articleAdapter.notifyDataSetChanged();
        }
    }

    private void showError(String message) {
        showLoading(false);
        showEmptyState(true);
        Toast.makeText(ArticleListActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerViewArticles.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean isEmpty) {
        textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewArticles.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onArticleClick(Article article) {
        // Ici, vous pouvez ouvrir l'activité de détail de l'article
        Toast.makeText(this, "Article sélectionné : " + article.getTitre(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Article cliqué: " + article.getId() + " - " + article.getTitre());

        // Dans une implémentation complète, vous lanceriez une nouvelle activité pour afficher l'article
        // Intent intent = new Intent(ArticleListActivity.this, ArticleDetailActivity.class);
        // intent.putExtra("ARTICLE_ID", article.getId());
        // startActivity(intent);
    }
}