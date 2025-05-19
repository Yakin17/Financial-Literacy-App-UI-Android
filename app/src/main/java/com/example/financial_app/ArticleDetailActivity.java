package com.example.financial_app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.financial_app.model.Article;
import com.example.financial_app.model.Quiz;
import com.example.financial_app.model.ScoreResponse;
import com.example.financial_app.network.ApiService;
import com.example.financial_app.network.RetrofitClient;
import com.example.financial_app.util.SessionManager;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class ArticleDetailActivity extends AppCompatActivity {

    private static final String TAG = "ArticleDetailActivity";

    private TextView textViewTitle, textViewAuthorAndDate, textViewContent;
    private CardView startQuizCard, quizSectionCard;
    private Button buttonStartQuiz, buttonSubmitAnswer, buttonNextQuestion;
    private TextView textViewQuizQuestion, textViewFeedback;
    private RadioGroup radioGroupAnswers;
    private RadioButton[] radioButtons = new RadioButton[4];
    private ProgressBar progressBar;
    private ImageButton backButton;

    private Article article;
    private List<Quiz> quizzes = new ArrayList<>();
    private int currentQuizIndex = 0;
    private int score = 0;

    // Ajouter le SessionManager pour accéder à l'ID de l'utilisateur connecté
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_article_detail);

        // Initialiser le SessionManager
        sessionManager = new SessionManager(this);

        Log.d(TAG, "Vérification de la session au démarrage:");
        Log.d(TAG, "Utilisateur connecté: " + sessionManager.isLoggedIn());
        Log.d(TAG, "ID utilisateur: " + sessionManager.getUserId());
        Log.d(TAG, "Nom d'utilisateur: " + sessionManager.getUsername());
        initViews();

        // Assurez-vous que les cartes sont initialement invisibles
        startQuizCard.setVisibility(View.GONE);
        quizSectionCard.setVisibility(View.GONE);

        if (getIntent().hasExtra("article_id")) {
            Long articleId = getIntent().getLongExtra("article_id", -1);
            if (articleId != -1) {
                loadArticle(articleId);
            } else {
                showError("ID d'article invalide");
            }
        } else if (getIntent().getSerializableExtra("article") != null) {
            article = (Article) getIntent().getSerializableExtra("article");
            displayArticle();

            // Toujours charger les quiz pour vérifier s'ils existent
            loadQuizzes(article.getId());
        } else {
            showError("Aucun article spécifié");
        }

        // Configuration des listeners
        setupListeners();
    }

    private void initViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewAuthorAndDate = findViewById(R.id.textViewAuthorAndDate);
        textViewContent = findViewById(R.id.textViewContent);
        startQuizCard = findViewById(R.id.startQuizCard);
        quizSectionCard = findViewById(R.id.quizSectionCard);
        buttonStartQuiz = findViewById(R.id.buttonStartQuiz);
        buttonSubmitAnswer = findViewById(R.id.buttonSubmitAnswer);
        buttonNextQuestion = findViewById(R.id.buttonNextQuestion);
        textViewQuizQuestion = findViewById(R.id.textViewQuizQuestion);
        textViewFeedback = findViewById(R.id.textViewFeedback);
        radioGroupAnswers = findViewById(R.id.radioGroupAnswers);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.backButton);

        radioButtons[0] = findViewById(R.id.radioButtonAnswer1);
        radioButtons[1] = findViewById(R.id.radioButtonAnswer2);
        radioButtons[2] = findViewById(R.id.radioButtonAnswer3);
        radioButtons[3] = findViewById(R.id.radioButtonAnswer4);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        buttonStartQuiz.setOnClickListener(v -> {
            if (quizzes.isEmpty()) {
                Toast.makeText(this, "Aucun quiz disponible pour cet article", Toast.LENGTH_SHORT).show();
                return;
            }

            startQuizCard.setVisibility(View.GONE);
            quizSectionCard.setVisibility(View.VISIBLE);
            displayCurrentQuiz();
        });

        buttonSubmitAnswer.setOnClickListener(v -> checkAnswer());

        buttonNextQuestion.setOnClickListener(v -> {
            textViewFeedback.setVisibility(View.GONE);
            buttonSubmitAnswer.setVisibility(View.VISIBLE);
            buttonNextQuestion.setVisibility(View.GONE);

            currentQuizIndex++;
            if (currentQuizIndex < quizzes.size()) {
                displayCurrentQuiz();
            } else {
                // Fin du quiz
                showQuizResults();
            }
        });
    }

    private void loadArticle(Long articleId) {
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Article> call = apiService.getArticleById(articleId);

        call.enqueue(new Callback<Article>() {
            @Override
            public void onResponse(Call<Article> call, Response<Article> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    article = response.body();
                    displayArticle();

                    // Toujours charger les quiz pour vérifier s'ils existent
                    loadQuizzes(article.getId());
                } else {
                    showError("Impossible de charger l'article");
                }
            }

            @Override
            public void onFailure(Call<Article> call, Throwable t) {
                showLoading(false);
                showError("Erreur réseau: " + t.getMessage());
                Log.e(TAG, "Erreur lors du chargement de l'article", t);
            }
        });
    }

    private void loadQuizzes(Long articleId) {
        Log.d(TAG, "Chargement des quizzes pour l'article ID: " + articleId);
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Quiz>> call = apiService.getQuizzesByArticleId(articleId);

        call.enqueue(new Callback<List<Quiz>>() {
            @Override
            public void onResponse(Call<List<Quiz>> call, Response<List<Quiz>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    quizzes = response.body();
                    Log.d(TAG, "Quizzes chargés: " + quizzes.size());

                    // Mettre à jour la visibilité de la carte de quiz
                    if (!quizzes.isEmpty()) {
                        startQuizCard.setVisibility(View.VISIBLE);
                        // Mise à jour du flag dans l'article
                        if (article != null) {
                            article.setHasQuiz(true);
                        }


                        // Vérifier si l'utilisateur a déjà complété le quiz
                        checkQuizCompletion(quizzes.get(0).getId());

                    } else {
                        startQuizCard.setVisibility(View.GONE);
                        // Mise à jour du flag dans l'article
                        if (article != null) {
                            article.setHasQuiz(false);
                        }
                    }
                } else {
                    Log.e(TAG, "Erreur lors du chargement des quizzes: " + response.code());
                    showError("Impossible de charger les quiz");
                    startQuizCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Quiz>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Erreur lors du chargement des quiz", t);
                showError("Erreur réseau: " + t.getMessage());
                startQuizCard.setVisibility(View.GONE);

            }
        });
    }

    /**
     * Vérifie si l'utilisateur a déjà complété le quiz
     */
    private void checkQuizCompletion(Long quizId) {
        if (!sessionManager.isLoggedIn()) {
            // Si l'utilisateur n'est pas connecté, ne pas vérifier
            return;
        }

        Long userId = sessionManager.getUserId();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Map<String, Boolean>> call = apiService.checkQuizCompletion(userId, quizId);

        call.enqueue(new Callback<Map<String, Boolean>>() {
            @Override
            public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Boolean completed = response.body().get("completed");
                    if (completed != null && completed) {
                        // L'utilisateur a déjà complété ce quiz
                        sessionManager.markQuizAsCompleted(quizId);
                        // Mettre à jour le bouton de démarrage
                        buttonStartQuiz.setText("Refaire le quiz");
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
                Log.e(TAG, "Erreur lors de la vérification du quiz", t);

            }
        });
    }

    private void displayArticle() {
        if (article != null) {
            textViewTitle.setText(article.getTitre());

            // Format de la date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            String formattedDate = article.getDatePublication().format(formatter);

            textViewAuthorAndDate.setText("Par " + article.getAuteurNom() + " • " + formattedDate);
            textViewContent.setText(article.getContenu());
        }
    }

    private void displayCurrentQuiz() {
        if (currentQuizIndex < quizzes.size()) {
            Quiz currentQuiz = quizzes.get(currentQuizIndex);
            textViewQuizQuestion.setText(currentQuiz.getQuestion());

            // Préparer les réponses (mélanges)
            List<String> answers = new ArrayList<>();
            answers.add(currentQuiz.getReponseCorrecte());
            answers.add(currentQuiz.getReponseInc1());
            answers.add(currentQuiz.getReponseInc2());
            answers.add(currentQuiz.getReponseInc3());

            // Mélanger les réponses
            Collections.shuffle(answers);

            // Afficher les réponses
            for (int i = 0; i < radioButtons.length; i++) {
                radioButtons[i].setText(answers.get(i));
                radioButtons[i].setChecked(false);
            }

            radioGroupAnswers.clearCheck();
        }
    }

    private void checkAnswer() {
        int selectedId = radioGroupAnswers.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Veuillez sélectionner une réponse", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedId);
        String selectedAnswer = selectedRadioButton.getText().toString();
        Quiz currentQuiz = quizzes.get(currentQuizIndex);

        boolean isCorrect = selectedAnswer.equals(currentQuiz.getReponseCorrecte());
        if (isCorrect) {
            score++;
            textViewFeedback.setText("Correct ! Bonne réponse.");
            textViewFeedback.setBackgroundResource(R.color.correctAnswerBackground);
        } else {
            textViewFeedback.setText("Incorrect. La bonne réponse était : " + currentQuiz.getReponseCorrecte());
            textViewFeedback.setBackgroundResource(R.color.incorrectAnswerBackground);
        }

        textViewFeedback.setVisibility(View.VISIBLE);
        buttonSubmitAnswer.setVisibility(View.GONE);

        if (currentQuizIndex < quizzes.size() - 1) {
            buttonNextQuestion.setText("Question suivante");
        } else {
            buttonNextQuestion.setText("Terminer le quiz");
        }

        buttonNextQuestion.setVisibility(View.VISIBLE);
    }

    private void showQuizResults() {
        quizSectionCard.setVisibility(View.GONE);

        // Afficher la carte de résultat final
        int totalQuestions = quizzes.size();
        double percentage = (double) score / totalQuestions * 100;

        // Sauvegarder le score dans la base de données
        saveQuizScore();

        // Vous pouvez créer une nouvelle carte ou réutiliser startQuizCard
        startQuizCard.setVisibility(View.VISIBLE);
        TextView quizTitle = findViewById(R.id.textViewQuizTitle);
        TextView quizDescription = findViewById(R.id.textViewQuizDescription);

        quizTitle.setText("Quiz terminé !");
        quizDescription.setText(String.format("Votre score : %d/%d (%.1f%%)", score, totalQuestions, percentage));

        buttonStartQuiz.setText("Recommencer");
        buttonStartQuiz.setOnClickListener(v -> {
            // Réinitialiser le quiz
            currentQuizIndex = 0;
            score = 0;
            startQuizCard.setVisibility(View.GONE);
            quizSectionCard.setVisibility(View.VISIBLE);
            displayCurrentQuiz();
        });
    }

    /**
     * Sauvegarde le score de l'utilisateur dans la base de données
     */
    private void saveQuizScore() {
        // Vérifier la session et afficher toutes les données pour le débogage
        sessionManager.debugPrintAllValues();

        // Vérifier si l'utilisateur est connecté et obtenir l'ID utilisateur
        Long userId = sessionManager.getUserId();

        // Vérification plus robuste de l'état de connexion
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Connectez-vous pour sauvegarder votre score", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Échec de sauvegarde du score: Utilisateur non connecté");

            // Proposer à l'utilisateur de se reconnecter
            showLoginDialog();
            return;
        }

        if (userId == null || userId <= 0) {
            Toast.makeText(this, "ID utilisateur invalide. Veuillez vous reconnecter.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Échec de sauvegarde du score: ID utilisateur invalide: " + userId);

            // Déconnecter l'utilisateur et proposer de se reconnecter
            sessionManager.logout();
            showLoginDialog();
            return;
        }

        // Assurez-vous que nous avons des quiz à traiter
        if (quizzes == null || quizzes.isEmpty()) {
            Toast.makeText(this, "Erreur: Aucun quiz disponible", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Échec de sauvegarde du score: Aucun quiz disponible");
            return;
        }

        // Obtenir l'ID du quiz
        Quiz currentQuiz = quizzes.get(0); // Prendre le premier quiz (ou autre logique selon votre modèle)
        Long quizId = currentQuiz.getId();

        // Journaliser les informations avant l'appel API
        Log.d(TAG, "Tentative de sauvegarde du score - UserId: " + userId + ", QuizId: " + quizId + ", Score: " + score);

        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ScoreResponse> call = apiService.submitQuizScore(userId, quizId, score);

        call.enqueue(new Callback<ScoreResponse>() {
            @Override
            public void onResponse(Call<ScoreResponse> call, Response<ScoreResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ScoreResponse scoreResponse = response.body();
                    Log.d(TAG, "Score sauvegardé avec succès: " + scoreResponse.toString());
                    Toast.makeText(ArticleDetailActivity.this, "Score sauvegardé avec succès!", Toast.LENGTH_SHORT).show();

                    // Marquer le quiz comme complété localement
                    sessionManager.markQuizAsCompleted(quizId);
                    sessionManager.saveQuizScore(quizId, score);
                } else {
                    // Journaliser plus de détails sur l'erreur
                    try {
                        Log.e(TAG, "Erreur lors de la sauvegarde du score: " + response.code());
                        Log.e(TAG, "Réponse d'erreur: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de la lecture de l'erreur", e);
                    }
                    Toast.makeText(ArticleDetailActivity.this, "Erreur lors de la sauvegarde du score: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ScoreResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Erreur réseau lors de la sauvegarde du score", t);
                Toast.makeText(ArticleDetailActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connexion requise");
        builder.setMessage("Votre session semble avoir expiré. Voulez-vous vous reconnecter ?");
        builder.setPositiveButton("Se connecter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Rediriger vers l'écran de connexion
                startActivity(new Intent(ArticleDetailActivity.this, LoginActivity.class));
                finish();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}