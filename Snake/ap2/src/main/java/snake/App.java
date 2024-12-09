package snake;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {
    public static File arquivo = new File("scoreboard.txt");
    public static int score = 0;
    public static int highscore = 0;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }


    public static final int BLOCK_SIZE = 40; //pixel
    public static final int APP_W = 20 * BLOCK_SIZE; //largura
    public static final int APP_H = 15 * BLOCK_SIZE; //altura

    private Direction direction = Direction.RIGHT;
    private boolean moved = false;
    private boolean running = false;

    private Timeline timeline = new Timeline();

    private ObservableList<Node> snake;

    private Parent createContent() {
        //Criação do canvas
        BorderPane root = new BorderPane();
        root.setPrefSize(APP_W, APP_H);

        Group snakeBody = new Group();
        snake = snakeBody.getChildren();

        //Comida
        Rectangle food = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
        food.setFill(Color.RED);
        food.setTranslateX((int)(Math.random() * APP_W - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
        food.setTranslateY((int)(Math.random() * APP_H - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);

        //Animação
        KeyFrame frame = new KeyFrame(Duration.seconds(0.15), event -> {
            Text text = new Text("Highscore: " + highscore + "\nScore: " + score);
            text.setFont(Font.font("Roboto Blacak", FontWeight.BOLD, 24));
            root.setTop(text);

            if (!running)
                return;

            boolean toRemove = snake.size() > 1;

            Node tail = toRemove ? snake.remove(snake.size() - 1) : snake.get(0);

            double tailX = tail.getTranslateX();
            double tailY = tail.getTranslateY();

            //Movimentação
            switch (direction) {
                case UP:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() - BLOCK_SIZE);
                    break;
                case DOWN:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() + BLOCK_SIZE);
                    break;
                case LEFT:
                    tail.setTranslateX(snake.get(0).getTranslateX() - BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
                case RIGHT:
                    tail.setTranslateX(snake.get(0).getTranslateX() + BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
            }

            moved = true;

            if (toRemove)
                snake.add(0, tail);

            //se a cobra bater em si mesma ela morre
            for (Node rect : snake) {
                if (rect != tail && tail.getTranslateX() == rect.getTranslateX()
                        && tail.getTranslateY() == rect.getTranslateY()) {
                    restartGame();
                    break;
                }
            }

            //se a cobra bater na borda ela morre
            if (tail.getTranslateX() < 0 || tail.getTranslateX() >= APP_W
                    || tail.getTranslateY() < 0 || tail.getTranslateY() >= APP_H) {
                restartGame();
            }

            //comendo a fruta
            if (tail.getTranslateX() == food.getTranslateX()
                    && tail.getTranslateY() == food.getTranslateY()) {
                food.setTranslateX((int)(Math.random() * (APP_W - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
                food.setTranslateY((int)(Math.random() * (APP_H - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);

                Rectangle rect = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
                rect.setFill(Color.GREEN);
                rect.setTranslateX(tailX);
                rect.setTranslateY(tailY);

                snake.add(rect);
                score++;
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.setCycleCount(Timeline.INDEFINITE);

        root.getChildren().addAll(food, snakeBody);
        return root;
    }

    //reinício
    private void restartGame() {
        stopGame();
        startGame();
    }

    //fim de jogo
    private void stopGame() {
        if (score > highscore) {
            try {
                FileWriter fw = new FileWriter(arquivo);
                fw.write("" + score);
                fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }            
        }
        score = 0;
        running = false;
        timeline.stop();
        snake.clear();
    }

    //início de jogo
    private void startGame() {
        try {
            if (!arquivo.exists()) {
                arquivo.createNewFile();
                FileWriter fw = new FileWriter(arquivo);
                fw.write("" + score);
                fw.close();
            }
            Scanner myReader = new Scanner(arquivo);
            String data = myReader.nextLine();
            highscore = Integer.parseInt(data);
            myReader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        direction = Direction.RIGHT;
        Rectangle head = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
        head.setFill(Color.GREEN);
        snake.add(head);
        timeline.play();
        running = true;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        scene.setOnKeyPressed(event -> {
            if (!moved)
                return;

            switch (event.getCode()) {
                case W:
                    if (direction != Direction.DOWN)
                        direction = Direction.UP;
                    break;         
                case S:
                    if (direction != Direction.UP)
                        direction = Direction.DOWN;
                    break;         
                case A:
                    if (direction != Direction.RIGHT)
                        direction = Direction.LEFT;
                    break;         
                case D:
                    if (direction != Direction.LEFT)
                        direction = Direction.RIGHT;
                    break;         
            }

            moved = false;
        });

        stage.setTitle("Jogo da cobrinha Blau Blau");
        stage.setScene(scene);
        stage.show();
        startGame();
    }

    public static void main(String[] args) {
        launch(args);
    }
}