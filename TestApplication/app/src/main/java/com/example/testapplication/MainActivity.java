package com.example.testapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.view.MenuInflater;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = findViewById(R.id.activity_main);

        Button btnMode = findViewById(R.id.btnMode);
        btnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Function that sets touches to place flags
                if(MinesweeperModel.getInstance().getActionType() == MinesweeperModel.REVEAL) {
                    MinesweeperModel.getInstance().actionFlag();
                    Snackbar flagSnackbar = Snackbar.make(linearLayout, "Flag mode on", 1000);
                    btnMode.setText(R.string.reveal);
                    flagSnackbar.show();
                }
                else {
                    MinesweeperModel.getInstance().actionReveal();
                    Snackbar revealSnackbar = Snackbar.make(linearLayout, "Reveal mode on", 1000);
                    revealSnackbar.show();
                    btnMode.setText(R.string.flag);
                }
            }
        });

        Button btnRestart = findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnMode.setText(R.string.flag);
                restartGame();
            }
        });
    }

    public void showSnackBarWithDelete(String msg) {
        Snackbar.make(linearLayout, msg,
                Snackbar.LENGTH_LONG).setAction(
                "Restart", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Restart the game
                        MinesweeperModel.getInstance().clearBoard();
                        MinesweeperModel.getInstance().setMines();
                        MinesweeperModel.getInstance().setMineCount();
                        MinesweeperView v = findViewById(R.id.gameView);
                        Button btnMode = findViewById(R.id.btnMode);
                        btnMode.setText(R.string.flag);
                        v.invalidate();
                    }
                }
        ).show();
    }

    public void restartGame() {
        MinesweeperModel.getInstance().clearBoard();
        MinesweeperModel.getInstance().setMines();
        MinesweeperModel.getInstance().setMineCount();
        Snackbar restartSnackbar = Snackbar.make(linearLayout, "Game restarted. Reveal mode on", Snackbar.LENGTH_LONG);
        MinesweeperView v = findViewById(R.id.gameView);
        v.invalidate();
        restartSnackbar.show();
    }

    public void showDifficultyMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                return selectDifficulty(item);
            }
        });
        popup.inflate(R.menu.menu);
        popup.show();
    }

    public boolean selectDifficulty(MenuItem item) {
        boolean toReturn = true;
        switch (item.getItemId()) {
            case R.id.easy:
                MinesweeperModel.setDifficulty(5);
                break;
            case R.id.medium:
                MinesweeperModel.setDifficulty(4);
                break;
            case R.id.hard:
                MinesweeperModel.setDifficulty(3);
                break;
            case R.id.expert:
                MinesweeperModel.setDifficulty(2);
                break;
            case R.id.blank:
                MinesweeperModel.setDifficulty(1);
                break;
            default:
                toReturn = false;
                break;
        }
        if(toReturn) {
            restartGame();
        }
        return toReturn;
    }
}