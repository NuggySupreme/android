package com.example.testapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = (LinearLayout) findViewById(R.id.activity_main);

        Button btnMode = (Button) findViewById(R.id.btnMode);
        btnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Function that sets touches to place flags
                if(MinesweeperModel.getInstance().getActionType() == MinesweeperModel.REVEAL) {
                    MinesweeperModel.getInstance().actionFlag();
                    Snackbar flagSnackbar = Snackbar.make(linearLayout, "Flag mode on", 1000);
                    btnMode.setText("REVEAL");
                    flagSnackbar.show();
                }
                else {
                    MinesweeperModel.getInstance().actionReveal();
                    Snackbar revealSnackbar = Snackbar.make(linearLayout, "Reveal mode on", 1000);
                    revealSnackbar.show();
                    btnMode.setText("FLAG");
                }
            }
        });

        Button btnRestart = (Button) findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MinesweeperModel.getInstance().clearBoard();
                MinesweeperModel.getInstance().setMines();
                MinesweeperModel.getInstance().setMineCount();

                Snackbar restartSnackbar = Snackbar.make(linearLayout, "Game restarted. Reveal mode on", Snackbar.LENGTH_LONG);
                restartSnackbar.show();
                MinesweeperView v = findViewById(R.id.gameView);
                btnMode.setText("FLAG");
                v.invalidate();
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
                        btnMode.setText("FLAG");
                        v.invalidate();
                    }
                }
        ).show();
    }
}