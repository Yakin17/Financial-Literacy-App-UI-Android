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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.example.financial_app.model.Article;
import com.example.financial_app.model.Quiz;
import com.example.financial_app.model.ScoreResponse;
import com.example.financial_app.network.ApiService;
import com.example.financial_app.network.RetrofitClient;
import com.example.financial_app.service.ScoreService;
import com.example.financial_app.util.SharedPreferencesManager;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArticleDetailActivity extends AppCompatActivity {

    private static final String TAG = "ArticleDetailActivity";

    private TextView textViewTitle, textViewAuthorAndDate, textViewContent;
    private CardView startQuizCard, quizSectionCard, feedbackCard;
    private Button buttonStartQuiz, buttonSubmitAnswer, buttonNextQuestion;
    private TextView textViewQuizQuestion, textViewFeedback;
    private TextView textViewQuizTitle, textViewQuizDescription;
    private RadioGroup radioGroupAnswers;
    private RadioButton[] radioButtons = new RadioButton[4];
    private ProgressBar progressBar;
    private LinearLayout loadingContainer;
    private ImageButton backButton;

    private Article article;
    private List<Quiz> quizzes = new ArrayList<>();
    private int currentQuizIndex = 0;
    private int score = 0;
    private boolean quizAlreadyCompleted = false;
    private int previousScore = 0;

    // Services et gestionnaires
    private SharedPreferencesManager prefsManager;
    private ScoreService scoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_article_detail);

        // Initialiser le contexte pour RetrofitClient
        RetrofitClient.setContext(getApplicationContext());

        // Initialiser les services
        prefsManager = SharedPreferencesManager.getInstance(this);
        scoreService = new ScoreService(this);

        // Débogage complet de la session pour voir tous les détails
        Log.d(TAG, "===== DÉBUT SESSION VÉRIFICATION =====");
        prefsManager.debugPrintAllValues();
        Log.d(TAG, "===== FIN SESSION VÉRIFICATION =====");

        initViews();

        // Assurez-vous que les cartes sont initialement invisibles
        startQuizCard.setVisibility(View.GONE);
        quizSectionCard.setVisibility(View.GONE);
        feedbackCard.setVisibility(View.GONE);

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
        feedbackCard = findViewById(R.id.feedbackCard);
        buttonStartQuiz = findViewById(R.id.buttonStartQuiz);
        buttonSubmitAnswer = findViewById(R.id.buttonSubmitAnswer);
        buttonNextQuestion = findViewById(R.id.buttonNextQuestion);
        textViewQuizQuestion = findViewById(R.id.textViewQuizQuestion);
        textViewFeedback = findViewById(R.id.textViewFeedback);
        radioGroupAnswers = findViewById(R.id.radioGroupAnswers);
        progressBar = findViewById(R.id.progressBar);
        loadingContainer = findViewById(R.id.loadingContainer);
        backButton = findViewById(R.id.backButton);
        textViewQuizTitle = findViewById(R.id.textViewQuizTitle);
        textViewQuizDescription = findViewById(R.id.textViewQuizDescription);

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

            // Vérification stricte: si l'utilisateur a déjà complété le quiz, ne pas
            // laisser recommencer
            if (quizAlreadyCompleted) {
                // Message plus explicite pour l'utilisateur
                Toast.makeText(this, "Vous avez déjà complété ce quiz et obtenu " +
                        previousScore + "/" + quizzes.size() + " points", Toast.LENGTH_LONG).show();
                return;
            }

            startQuizCard.setVisibility(View.GONE);
            quizSectionCard.setVisibility(View.VISIBLE);
            displayCurrentQuiz();
        });

        buttonSubmitAnswer.setOnClickListener(v -> checkAnswer());

        buttonNextQuestion.setOnClickListener(v -> {
            feedbackCard.setVisibility(View.GONE);
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
                        // Mise à jour du flag dans l'article
                        if (article != null) {
                            article.setHasQuiz(true);
                        }

                        // Vérifier si l'utilisateur a déjà complété le quiz
                        if (prefsManager.isLoggedIn()) {
                            checkIfQuizCompleted(quizzes.get(0).getId());
                        } else {
                            // Si l'utilisateur n'est pas connecté, on met simplement la carte visible
                            setupQuizCard(false, 0);
                        }
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
     * Méthode améliorée pour vérifier si un quiz a déjà été complété
     */
    private void checkIfQuizCompleted(Long quizId) {
        showLoading(true);

        // Vérification locale en premier
        if (prefsManager.hasCompletedQuiz(quizId)) {
            int localScore = prefsManager.getQuizScore(quizId);
            Log.d(TAG, "Quiz complété trouvé localement: " + quizId + ", score: " + localScore);
            quizAlreadyCompleted = true;
            previousScore = localScore;
            setupQuizCard(true, localScore);
            showLoading(false);
            return;
        }

        // Si pas trouvé localement, vérifier avec le serveur
        scoreService.checkQuizCompletion(quizId, new ScoreService.CheckCompletionCallback() {
            @Override
            public void onComplete(boolean isCompleted, int previousScore) {
                showLoading(false);
                quizAlreadyCompleted = isCompleted;
                ArticleDetailActivity.this.previousScore = previousScore;

                // Configurer la carte de quiz en fonction du résultat
                setupQuizCard(isCompleted, previousScore);

                // Enregistrer dans les préférences locales pour les prochaines fois
                if (isCompleted) {
                    prefsManager.markQuizAsCompleted(quizId);
                    prefsManager.saveQuizScore(quizId, previousScore);
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                // En cas d'erreur, on suppose que le quiz n'a pas été complété
                // pour donner une chance à l'utilisateur
                Log.e(TAG, "Erreur lors de la vérification du quiz: " + message);
                setupQuizCard(false, 0);
            }
        });
    }

    /**
     * Configure l'apparence et le comportement de la carte de quiz
     */
    private void setupQuizCard(boolean isCompleted, int score) {
        startQuizCard.setVisibility(View.VISIBLE);

        if (isCompleted) {
            // L'utilisateur a déjà complété ce quiz
            String percentage = ScoreService.formatScoreAsPercentage(score, quizzes.size());

            textViewQuizTitle.setText("Quiz déjà complété");
            textViewQuizDescription.setText(String.format(
                    "Vous avez déjà passé ce quiz et obtenu un score de %d/%d (%s)",
                    score, quizzes.size(), percentage));

            buttonStartQuiz.setText("Quiz déjà complété");
            buttonStartQuiz.setEnabled(false);
            buttonStartQuiz.setAlpha(0.7f);
        } else {
            // L'utilisateur n'a pas encore complété ce quiz
            textViewQuizTitle.setText("Testez vos connaissances !");
            textViewQuizDescription.setText("Répondez aux questions pour tester votre compréhension de cet article.");

            buttonStartQuiz.setText("Commencer le Quiz");
            buttonStartQuiz.setEnabled(true);
            buttonStartQuiz.setAlpha(1.0f);
        }
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

            // Mettre à jour le texte du bouton de soumission
            buttonSubmitAnswer.setText("Valider ma réponse");
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
            textViewFeedback.setText("✓ Correct ! Bonne réponse.");
            feedbackCard.setCardBackgroundColor(getColor(R.color.correctAnswerBackground));
        } else {
            textViewFeedback.setText("✗ Incorrect. La bonne réponse était : " + currentQuiz.getReponseCorrecte());
            feedbackCard.setCardBackgroundColor(getColor(R.color.incorrectAnswerBackground));
        }

        feedbackCard.setVisibility(View.VISIBLE);
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
        String percentage = ScoreService.formatScoreAsPercentage(score, totalQuestions);

        showLoading(true);
        // Sauvegarder le score dans la base de données si l'utilisateur est connecté
        if (prefsManager.isLoggedIn() && !quizzes.isEmpty()) {
            Long quizId = quizzes.get(0).getId();
            Long userId = prefsManager.getUserId();

            // Ajouter un log détaillé avant l'envoi
            Log.d(TAG, "Tentative de sauvegarde de score - Vérifications:");
            Log.d(TAG, "isLoggedIn: " + prefsManager.isLoggedIn());
            Log.d(TAG, "userId: " + userId);
            Log.d(TAG, "quizId: " + quizId);
            Log.d(TAG, "score: " + score);
            Log.d(TAG, "totalQuestions: " + totalQuestions);

            // Vérifier que l'ID utilisateur est bien valide
            if (userId <= 0) {
                Log.e(TAG, "ID utilisateur invalide: " + userId);
                showLoading(false);
                Toast.makeText(ArticleDetailActivity.this, "Erreur: ID utilisateur invalide", Toast.LENGTH_SHORT)
                        .show();
                showLoginDialog();
                displayFinalResults(percentage);
                return;
            }

            // Vérifier encore une fois avant de sauvegarder
            if (quizAlreadyCompleted) {
                Log.d(TAG, "Quiz déjà complété, pas besoin de sauvegarder à nouveau");
                showLoading(false);
                displayFinalResults(percentage);
                return;
            }

            scoreService.saveQuizScore(quizId, score, totalQuestions, new ScoreService.ScoreCallback() {
                @Override
                public void onSuccess(ScoreResponse response) {
                    showLoading(false);
                    Toast.makeText(ArticleDetailActivity.this, "Score sauvegardé avec succès!", Toast.LENGTH_SHORT)
                            .show();

                    // Marquer le quiz comme complété localement
                    quizAlreadyCompleted = true;
                    previousScore = score;

                    // Sauvegarder localement
                    prefsManager.markQuizAsCompleted(quizzes.get(0).getId());
                    prefsManager.saveQuizScore(quizzes.get(0).getId(), score);

                    // Afficher les résultats finaux
                    displayFinalResults(percentage);
                }

                @Override
                public void onError(String message) {
                    showLoading(false);
                    Log.e(TAG, "Erreur lors de la sauvegarde du score: " + message);
                    Toast.makeText(ArticleDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    // Afficher quand même les résultats
                    displayFinalResults(percentage);
                }
            });
        } else {
            showLoading(false);
            Log.e(TAG, "Impossible d'enregistrer le score - Utilisateur connecté: " + prefsManager.isLoggedIn()
                    + ", Quizzes disponibles: " + !quizzes.isEmpty());
            if (!prefsManager.isLoggedIn()) {
                showLoginDialog();
            }
            // Afficher les résultats même si non connecté
            displayFinalResults(percentage);
        }
    }

    /**
     * Affiche les résultats finaux du quiz
     */
    private void displayFinalResults(String percentage) {
        int totalQuestions = quizzes.size();

        // Réutiliser startQuizCard pour afficher le résultat
        startQuizCard.setVisibility(View.VISIBLE);
        textViewQuizTitle.setText("Quiz terminé !");
        textViewQuizDescription.setText(String.format("Votre score : %d/%d (%s)", score, totalQuestions, percentage));

        // Désactiver le bouton pour ne pas permettre de refaire le quiz
        buttonStartQuiz.setText("Quiz complété");
        buttonStartQuiz.setEnabled(false);
        buttonStartQuiz.setAlpha(0.7f); // Apparence visuelle désactivée

        // Marquer comme complété localement pour cette session
        quizAlreadyCompleted = true;
        previousScore = score;
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connexion requise");
        builder.setMessage("Connectez-vous pour sauvegarder votre score et suivre votre progression");
        builder.setPositiveButton("Se connecter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Rediriger vers l'écran de connexion
                startActivity(new Intent(ArticleDetailActivity.this, LoginActivity.class));
                finish();
            }
        });
        builder.setNegativeButton("Plus tard", null);
        builder.show();
    }

    private void showLoading(boolean isLoading) {
        loadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Vérifier si l'état de connexion a changé
        Log.d(TAG, "onResume - Vérification de l'état de connexion");
        Log.d(TAG, "===== DÉBUT VÉRIFICATION CONNEXION ONRESUME =====");
        prefsManager.debugPrintAllValues();
        Log.d(TAG, "===== FIN VÉRIFICATION CONNEXION ONRESUME =====");

        // Si l'article et le quiz sont chargés, vérifier à nouveau si le quiz est complété
        if (article != null && !quizzes.isEmpty() && prefsManager.isLoggedIn()) {
            Log.d(TAG, "onResume - Vérification à nouveau si le quiz est complété");
            checkIfQuizCompleted(quizzes.get(0).getId());
        }
    }

    @Override
    public void onBackPressed() {
        // Si l'utilisateur est au milieu d'un quiz, demander confirmation
        if (quizSectionCard.getVisibility() == View.VISIBLE) {
            new AlertDialog.Builder(this)
                    .setTitle("Quitter le quiz ?")
                    .setMessage("Êtes-vous sûr de vouloir quitter ce quiz ? Votre progression sera perdue.")
                    .setPositiveButton("Quitter", (dialog, which) -> finish())
                    .setNegativeButton("Continuer", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}