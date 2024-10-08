package com.example.helpthebird;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameActivity extends AppCompatActivity {

    private ImageView bird, enemy1, enemy2, enemy3, coin1, coin2, right1, right2, right3;
    private TextView textViewScore, textViewStartInfo;
    private ConstraintLayout constraintLayout;

    private boolean touchControl = false;
    private boolean beginControl = false;
    private boolean gameOver = false;
    private SharedPreferences sharedPreferences;

    private Runnable runnable;
    private Handler handler;

    int birdX, enemy1X, enemy2X, enemy3X, coin1X, coin2X;
    int birdY, enemy1Y, enemy2Y, enemy3Y, coin1Y, coin2Y;

    int screenWidth;
    int screenHeight;

    int right = 3; // Total number of red hearts

    int score = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.constraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bird = findViewById(R.id.imageViewBird);
        enemy1 = findViewById(R.id.imageViewEnemy1);
        enemy2 = findViewById(R.id.imageViewEnemy2);
        enemy3 = findViewById(R.id.imageViewEnemy3);
        coin1 = findViewById(R.id.imageViewCoin);
        coin2 = findViewById(R.id.imageViewCoin2);
        right1 = findViewById(R.id.right1);
        right2 = findViewById(R.id.right2);
        right3 = findViewById(R.id.right3);
        textViewScore = findViewById(R.id.textViewScore);
        textViewStartInfo = findViewById(R.id.textViewStartInfo);
        constraintLayout = findViewById(R.id.constraintLayout);

        sharedPreferences = this.getSharedPreferences("Score", Context.MODE_PRIVATE);

        resetGame();

        constraintLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                textViewStartInfo.setVisibility(View.INVISIBLE);

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    touchControl = true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    touchControl = false;
                }
                if (!beginControl) {
                    beginControl = true;

                    screenWidth = constraintLayout.getWidth();
                    screenHeight = constraintLayout.getHeight();

                    birdX = (int) bird.getX();
                    birdY = (int) bird.getY();

                    handler = new Handler();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            moveToBird();
                            enemyControl();
                            collisionControl();
                            handler.postDelayed(this, 20);
                        }
                    };
                    handler.post(runnable);
                }

                return true;
            }
        });
    }

    private void resetGame() {
        // Reset bird position
        birdX = birdY = 0;

        // Set initial X positions off-screen for enemies and coins
        enemy1X = enemy2X = enemy3X = screenWidth + 200;
        coin1X = coin2X = screenWidth + 300;

        // Randomize initial Y positions within the screen height for enemies and coins
        enemy1Y = (int) Math.floor(Math.random() * screenHeight);
        enemy2Y = (int) Math.floor(Math.random() * screenHeight);
        enemy3Y = (int) Math.floor(Math.random() * screenHeight);
        coin1Y = (int) Math.floor(Math.random() * screenHeight);
        coin2Y = (int) Math.floor(Math.random() * screenHeight);

        // Set initial score and hearts
        score = 0;
        right = 3;
        textViewScore.setText(String.valueOf(score));

        // Reset hearts visibility
        right1.setImageResource(R.drawable.grey_heart);
        right2.setImageResource(R.drawable.grey_heart);
        right3.setImageResource(R.drawable.grey_heart);

        // Make all elements visible
        bird.setVisibility(View.VISIBLE);
        enemy1.setVisibility(View.VISIBLE);
        enemy2.setVisibility(View.VISIBLE);
        enemy3.setVisibility(View.VISIBLE);
        coin1.setVisibility(View.VISIBLE);
        coin2.setVisibility(View.VISIBLE);
        textViewStartInfo.setVisibility(View.INVISIBLE);
    }

    public void moveToBird() {
        if (touchControl) {
            birdY -= (screenHeight / 40);
        } else {
            birdY += (screenHeight / 40);
        }

        // Keep bird within screen bounds
        if (birdY <= 0) {
            birdY = 0;
        }
        if (birdY >= (screenHeight - bird.getHeight())) {
            birdY = screenHeight - bird.getHeight();
        }

        bird.setY(birdY);
    }

    public void enemyControl() {
        // Determine speed based on score
        int enemySpeed1 = screenWidth / 150;
        int enemySpeed2 = screenWidth / 140;
        int enemySpeed3 = screenWidth / 130;
        int coinSpeed1 = screenWidth / 120;
        int coinSpeed2 = screenWidth / 110;

        if (score >= 150) {
            enemySpeed1 = screenWidth / 100;
            enemySpeed2 = screenWidth / 90;
            enemySpeed3 = screenWidth / 80;
            coinSpeed1 = screenWidth / 70;
            coinSpeed2 = screenWidth / 60;
        } else if (score >= 100) {
            enemySpeed1 = screenWidth / 120;
            enemySpeed2 = screenWidth / 110;
            enemySpeed3 = screenWidth / 100;
            coinSpeed1 = screenWidth / 90;
            coinSpeed2 = screenWidth / 80;
        } else if (score >= 50) {
            enemySpeed1 = screenWidth / 130;
            enemySpeed2 = screenWidth / 120;
            enemySpeed3 = screenWidth / 110;
            coinSpeed1 = screenWidth / 100;
            coinSpeed2 = screenWidth / 90;
        }

        // Move enemies and coins
        enemy1X -= enemySpeed1;
        enemy2X -= enemySpeed2;
        enemy3X -= enemySpeed3;
        coin1X -= coinSpeed1;
        coin2X -= coinSpeed2;

        // Reset positions when they go off-screen
        if (enemy1X < 0) {
            enemy1X = screenWidth + 200;
            enemy1Y = (int) Math.floor(Math.random() * screenHeight);
        }
        if (enemy2X < 0) {
            enemy2X = screenWidth + 200;
            enemy2Y = (int) Math.floor(Math.random() * screenHeight);
        }
        if (enemy3X < 0) {
            enemy3X = screenWidth + 200;
            enemy3Y = (int) Math.floor(Math.random() * screenHeight);
        }
        if (coin1X < 0) {
            coin1X = screenWidth + 200;
            coin1Y = (int) Math.floor(Math.random() * screenHeight);
        }
        if (coin2X < 0) {
            coin2X = screenWidth + 200;
            coin2Y = (int) Math.floor(Math.random() * screenHeight);
        }

        // Update enemy and coin positions
        enemy1.setX(enemy1X);
        enemy1.setY(enemy1Y);
        enemy2.setX(enemy2X);
        enemy2.setY(enemy2Y);
        enemy3.setX(enemy3X);
        enemy3.setY(enemy3Y);
        coin1.setX(coin1X);
        coin1.setY(coin1Y);
        coin2.setX(coin2X);
        coin2.setY(coin2Y);
    }

    public void collisionControl() {
        if (gameOver) {
            return;  // Exit the method if the game is already over
        }
        // Enemy 1 collision check
        int centerEnemy1X = enemy1X + enemy1.getWidth() / 2;
        int centerEnemy1Y = enemy1Y + enemy1.getHeight() / 2;
        if (centerEnemy1X >= birdX && centerEnemy1X <= (birdX + bird.getWidth()) && centerEnemy1Y >= birdY && centerEnemy1Y <= (birdY + bird.getHeight())) {
            enemy1X = screenWidth + 200;
            right--;
        }

        // Enemy 2 collision check
        int centerEnemy2X = enemy2X + enemy2.getWidth() / 2;
        int centerEnemy2Y = enemy2Y + enemy2.getHeight() / 2;
        if (centerEnemy2X >= birdX && centerEnemy2X <= (birdX + bird.getWidth()) && centerEnemy2Y >= birdY && centerEnemy2Y <= (birdY + bird.getHeight())) {
            enemy2X = screenWidth + 200;
            right--;
        }

        // Enemy 3 collision check
        int centerEnemy3X = enemy3X + enemy3.getWidth() / 2;
        int centerEnemy3Y = enemy3Y + enemy3.getHeight() / 2;
        if (centerEnemy3X >= birdX && centerEnemy3X <= (birdX + bird.getWidth()) && centerEnemy3Y >= birdY && centerEnemy3Y <= (birdY + bird.getHeight())) {
            enemy3X = screenWidth + 200;
            right--;
        }

        // Coin 1 collision check
        int centerCoin1X = coin1X + coin1.getWidth() / 2;
        int centerCoin1Y = coin1Y + coin1.getHeight() / 2;
        if (centerCoin1X >= birdX && centerCoin1X <= (birdX + bird.getWidth()) && centerCoin1Y >= birdY && centerCoin1Y <= (birdY + bird.getHeight())) {
            coin1X = screenWidth + 200;
            score += 10;
        }

        // Coin 2 collision check
        int centerCoin2X = coin2X + coin2.getWidth() / 2;
        int centerCoin2Y = coin2Y + coin2.getHeight() / 2;
        if (centerCoin2X >= birdX && centerCoin2X <= (birdX + bird.getWidth()) && centerCoin2Y >= birdY && centerCoin2Y <= (birdY + bird.getHeight())) {
            coin2X = screenWidth + 200;
            score += 10;
        }

        // Update score
        textViewScore.setText("Score: " + score);

        // Update heart icons based on the number of remaining lives
        right1.setVisibility(right >= 1 ? View.VISIBLE : View.INVISIBLE);
        right2.setVisibility(right >= 2 ? View.VISIBLE : View.INVISIBLE);
        right3.setVisibility(right >= 3 ? View.VISIBLE : View.INVISIBLE);

        // End the game if no lives are left
        if (right <= 0) {
            gameOver = true;
            Log.d("GameActivity", "No lives left, ending game.");
            handler.removeCallbacks(runnable);
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                    intent.putExtra("score", score);
                    Log.d("GameActivity", "Starting ResultActivity with score: " + score);
                    startActivity(intent);
                    finish(); // Ensure the current activity is finished
                    Log.d("GameActivity", "GameActivity finished.");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }


}
