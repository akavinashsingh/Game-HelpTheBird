package com.example.helpthebird;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
    private long lastFrameTimeMs;

    float birdX, enemy1X, enemy2X, enemy3X, coin1X, coin2X;
    float birdY, enemy1Y, enemy2Y, enemy3Y, coin1Y, coin2Y;

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

        // Defer game reset until layout is measured to avoid zero width/height
        constraintLayout.post(() -> {
            screenWidth = constraintLayout.getWidth();
            screenHeight = constraintLayout.getHeight();
            resetGame();
        });

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

                    birdX = bird.getX();
                    birdY = bird.getY();

                    handler = new Handler();
                    lastFrameTimeMs = SystemClock.uptimeMillis();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            long now = SystemClock.uptimeMillis();
                            float deltaSec = (now - lastFrameTimeMs) / 1000f;
                            lastFrameTimeMs = now;

                            moveToBird(deltaSec);
                            enemyControl(deltaSec);
                            collisionControl();

                            handler.postDelayed(this, 16); // ~60fps
                        }
                    };
                    handler.post(runnable);
                }

                return true;
            }
        });
    }

    private void resetGame() {
        if (screenWidth == 0 || screenHeight == 0) {
            return; // wait until layout pass sets dimensions
        }

        // Reset bird position to current layout position
        birdX = bird.getX();
        birdY = bird.getY();

        // Set initial X positions off-screen for enemies and coins
        enemy1X = enemy2X = enemy3X = screenWidth + 200f;
        coin1X = coin2X = screenWidth + 300f;

        // Randomize initial Y positions within bounds
        enemy1Y = randomYWithinBounds(enemy1.getHeight());
        enemy2Y = randomYWithinBounds(enemy2.getHeight());
        enemy3Y = randomYWithinBounds(enemy3.getHeight());
        coin1Y = randomYWithinBounds(coin1.getHeight());
        coin2Y = randomYWithinBounds(coin2.getHeight());

        // Set initial score and hearts
        score = 0;
        right = 3;
        textViewScore.setText(String.valueOf(score));

        // Reset hearts to full (red) and make visible
        right1.setImageResource(R.drawable.heart);
        right2.setImageResource(R.drawable.heart);
        right3.setImageResource(R.drawable.heart);
        right1.setVisibility(View.VISIBLE);
        right2.setVisibility(View.VISIBLE);
        right3.setVisibility(View.VISIBLE);

        // Make all elements visible
        bird.setVisibility(View.VISIBLE);
        enemy1.setVisibility(View.VISIBLE);
        enemy2.setVisibility(View.VISIBLE);
        enemy3.setVisibility(View.VISIBLE);
        coin1.setVisibility(View.VISIBLE);
        coin2.setVisibility(View.VISIBLE);
        textViewStartInfo.setVisibility(View.INVISIBLE);
    }

    public void moveToBird(float deltaSec) {
        // Match previous feel: base step was screenHeight/40 every ~20ms
        float baseStep = screenHeight / 40f;
        float scaledStep = baseStep * (deltaSec / 0.02f);

        if (touchControl) {
            birdY -= scaledStep;
        } else {
            birdY += scaledStep;
        }

        // Keep bird within screen bounds
        birdY = clamp(birdY, 0, screenHeight - bird.getHeight());

        bird.setY(birdY);
    }

    public void enemyControl(float deltaSec) {
        // Base per-second speeds derived from previous per-frame values (~50 fps)
        float enemySpeed1 = screenWidth / 3f;   // (screenWidth/150) * 50
        float enemySpeed2 = screenWidth / 2.8f; // (screenWidth/140) * 50
        float enemySpeed3 = screenWidth / 2.6f; // (screenWidth/130) * 50
        float coinSpeed1 = screenWidth / 2.4f;  // (screenWidth/120) * 50
        float coinSpeed2 = screenWidth / 2.2f;  // (screenWidth/110) * 50

        if (score >= 150) {
            enemySpeed1 = screenWidth / 2f;
            enemySpeed2 = screenWidth / 1.8f;
            enemySpeed3 = screenWidth / 1.6f;
            coinSpeed1 = screenWidth / 1.4f;
            coinSpeed2 = screenWidth / 1.2f;
        } else if (score >= 100) {
            enemySpeed1 = screenWidth / 2.4f;
            enemySpeed2 = screenWidth / 2.2f;
            enemySpeed3 = screenWidth / 2f;
            coinSpeed1 = screenWidth / 1.8f;
            coinSpeed2 = screenWidth / 1.6f;
        } else if (score >= 50) {
            enemySpeed1 = screenWidth / 2.6f;
            enemySpeed2 = screenWidth / 2.4f;
            enemySpeed3 = screenWidth / 2.2f;
            coinSpeed1 = screenWidth / 2f;
            coinSpeed2 = screenWidth / 1.8f;
        }

        float enemyStep1 = enemySpeed1 * deltaSec;
        float enemyStep2 = enemySpeed2 * deltaSec;
        float enemyStep3 = enemySpeed3 * deltaSec;
        float coinStep1 = coinSpeed1 * deltaSec;
        float coinStep2 = coinSpeed2 * deltaSec;

        // Move enemies and coins
        enemy1X -= enemyStep1;
        enemy2X -= enemyStep2;
        enemy3X -= enemyStep3;
        coin1X -= coinStep1;
        coin2X -= coinStep2;

        // Reset positions when they go off-screen
        if (enemy1X < -enemy1.getWidth()) {
            enemy1X = screenWidth + 200f;
            enemy1Y = randomYWithinBounds(enemy1.getHeight());
        }
        if (enemy2X < -enemy2.getWidth()) {
            enemy2X = screenWidth + 200f;
            enemy2Y = randomYWithinBounds(enemy2.getHeight());
        }
        if (enemy3X < -enemy3.getWidth()) {
            enemy3X = screenWidth + 200f;
            enemy3Y = randomYWithinBounds(enemy3.getHeight());
        }
        if (coin1X < -coin1.getWidth()) {
            coin1X = screenWidth + 200f;
            coin1Y = randomYWithinBounds(coin1.getHeight());
        }
        if (coin2X < -coin2.getWidth()) {
            coin2X = screenWidth + 200f;
            coin2Y = randomYWithinBounds(coin2.getHeight());
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
        int centerEnemy1X = (int) (enemy1X + enemy1.getWidth() / 2f);
        int centerEnemy1Y = (int) (enemy1Y + enemy1.getHeight() / 2f);
        if (centerEnemy1X >= birdX && centerEnemy1X <= (birdX + bird.getWidth()) && centerEnemy1Y >= birdY && centerEnemy1Y <= (birdY + bird.getHeight())) {
            enemy1X = screenWidth + 200;
            right--;
        }

        // Enemy 2 collision check
        int centerEnemy2X = (int) (enemy2X + enemy2.getWidth() / 2f);
        int centerEnemy2Y = (int) (enemy2Y + enemy2.getHeight() / 2f);
        if (centerEnemy2X >= birdX && centerEnemy2X <= (birdX + bird.getWidth()) && centerEnemy2Y >= birdY && centerEnemy2Y <= (birdY + bird.getHeight())) {
            enemy2X = screenWidth + 200;
            right--;
        }

        // Enemy 3 collision check
        int centerEnemy3X = (int) (enemy3X + enemy3.getWidth() / 2f);
        int centerEnemy3Y = (int) (enemy3Y + enemy3.getHeight() / 2f);
        if (centerEnemy3X >= birdX && centerEnemy3X <= (birdX + bird.getWidth()) && centerEnemy3Y >= birdY && centerEnemy3Y <= (birdY + bird.getHeight())) {
            enemy3X = screenWidth + 200;
            right--;
        }

        // Coin 1 collision check
        int centerCoin1X = (int) (coin1X + coin1.getWidth() / 2f);
        int centerCoin1Y = (int) (coin1Y + coin1.getHeight() / 2f);
        if (centerCoin1X >= birdX && centerCoin1X <= (birdX + bird.getWidth()) && centerCoin1Y >= birdY && centerCoin1Y <= (birdY + bird.getHeight())) {
            coin1X = screenWidth + 200;
            score += 10;
        }

        // Coin 2 collision check
        int centerCoin2X = (int) (coin2X + coin2.getWidth() / 2f);
        int centerCoin2Y = (int) (coin2Y + coin2.getHeight() / 2f);
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

    private float randomYWithinBounds(int viewHeight) {
        if (screenHeight <= viewHeight) {
            return 0;
        }
        float maxY = screenHeight - viewHeight;
        return (float) (Math.random() * maxY);
    }

    private float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }


}
