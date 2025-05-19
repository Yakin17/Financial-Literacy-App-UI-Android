package com.example.financial_app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.example.financial_app.network.ApiService;
import com.example.financial_app.network.RetrofitClient;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_article_detail);

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

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}