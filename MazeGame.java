import java.util.*;

public class MazeGame {
    public static void main(String[] args) {
        int obstacleCount = 15;
        int warpZoneCount = 3;
        int itemCount = 5;

        if (args.length >= 3) {
            obstacleCount = Integer.parseInt(args[0]);
            warpZoneCount = Integer.parseInt(args[1]);
            itemCount = Integer.parseInt(args[2]);
        }

        Maze maze = new Maze(10, 10);
        Player player = new Player(0, 0);
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(9, 9));
        enemies.add(new Enemy(8, 8));
        maze.generateObstacles(obstacleCount);
        maze.generateWarpZones(warpZoneCount);
        maze.generateItems(itemCount);
        Field field = new Field(maze);
        Score score = new Score(1000); // 初期スコアを設定

        Scanner scanner = new Scanner(System.in);
        boolean playAgain;
        do {
            playAgain = false;
            player.resetPosition();
            for (Enemy enemy : enemies) {
                enemy.resetPosition();
            }
            score.reset(1000); // 初期スコアにリセット
            int moves = 0;

            while (true) {
                field.display(player, enemies);
                System.out.println("Score: " + score.getScore());
                System.out.println("Enter move (WASD): ");
                char move = scanner.next().toUpperCase().charAt(0);

                if (!player.move(move, maze)) {
                    System.out.println("Invalid move!");
                    continue;
                }

                moves++;
                score.deductPoints(10); // 移動ごとにスコアを減少

                Item item = maze.pickUpItem(player.getPosition());
                if (item != null) {
                    score.addPoints(item.getScoreValue());
                    System.out.println("You picked up an item! Score: " + item.getScoreValue());
                }

                for (Enemy enemy : enemies) {
                    enemy.move(maze);
                    if (player.getPosition().equals(enemy.getPosition())) {
                        System.out.println("You were caught by the enemy! Game over.");
                        playAgain = true;
                        break;
                    }
                }

                if (playAgain) break;

                if (maze.isGoal(player.getPosition())) {
                    System.out.println("You reached the goal! You win!");
                    score.addPoints(500 - (moves * 10)); // ゴールに到達した場合、残りのスコアを加算
                    System.out.println("Final Score: " + score.getScore());
                    playAgain = true;
                    break;
                }
            }

            if (playAgain) {
                System.out.println("Do you want to play again? (y/n): ");
                char response = scanner.next().toUpperCase().charAt(0);
                if (response != 'Y') {
                    playAgain = false;
                }
            }
        } while (playAgain);

        scanner.close();
    }
}

class Score {
    private int score;

    public Score(int initialScore) {
        this.score = initialScore;
    }

    public void addPoints(int points) {
        this.score += points;
    }

    public void deductPoints(int points) {
        this.score -= points;
    }

    public int getScore() {
        return this.score;
    }

    public void reset(int initialScore) {
        this.score = initialScore;
    }
}

class Maze {
    private char[][] grid;
    private int rows;
    private int cols;
    private Position goal;
    private List<Item> items;

    public Maze(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows][cols];
        this.goal = new Position(rows - 1, cols - 1);
        this.items = new ArrayList<>();
        initializeMaze();
    }

    private void initializeMaze() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = ' ';
            }
        }
        grid[goal.getX()][goal.getY()] = 'G'; // Goal
    }

    public void generateObstacles(int count) {
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int x, y;
            do {
                x = rand.nextInt(rows);
                y = rand.nextInt(cols);
            } while (grid[x][y] != ' ' || (x == 0 && y == 0));
            grid[x][y] = 'X'; // Obstacle
        }
    }

    public void generateWarpZones(int count) {
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int x, y;
            do {
                x = rand.nextInt(rows);
                y = rand.nextInt(cols);
            } while (grid[x][y] != ' ' || (x == 0 && y == 0));
            grid[x][y] = 'W'; // Warp Zone
        }
    }

    public void generateItems(int count) {
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int x, y;
            do {
                x = rand.nextInt(rows);
                y = rand.nextInt(cols);
            } while (grid[x][y] != ' ' || (x == 0 && y == 0));
            items.add(new Item(new Position(x, y), 10)); // Each item has a score value of 10
        }
    }

    public boolean isObstacle(Position position) {
        return grid[position.getX()][position.getY()] == 'X';
    }

    public boolean isWarpZone(Position position) {
        return grid[position.getX()][position.getY()] == 'W';
    }

    public boolean isGoal(Position position) {
        return position.equals(goal);
    }

    public Item pickUpItem(Position position) {
        for (Item item : items) {
            if (item.getPosition().equals(position)) {
                items.remove(item);
                return item;
            }
        }
        return null;
    }

    public char getCell(int x, int y) {
        return grid[x][y];
    }

    public List<Item> getItems() {
        return items;
    }

    public Position getGoal() {
        return goal;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}

class Field {
    private Maze maze;

    public Field(Maze maze) {
        this.maze = maze;
    }

    public void display(Player player, List<Enemy> enemies) {
        for (int i = 0; i < maze.getRows(); i++) {
            for (int j = 0; j < maze.getCols(); j++) {
                boolean isPlayer = player.getPosition().equals(new Position(i, j));
                boolean isEnemy = false;
                for (Enemy enemy : enemies) {
                    if (enemy.getPosition().equals(new Position(i, j))) {
                        isEnemy = true;
                        break;
                    }
                }
                boolean isItem = false;
                for (Item item : maze.getItems()) {
                    if (item.getPosition().equals(new Position(i, j))) {
                        isItem = true;
                        break;
                    }
                }
                if (isPlayer) {
                    System.out.print('P'); // Player
                } else if (isEnemy) {
                    System.out.print('E'); // Enemy
                } else if (isItem) {
                    System.out.print('I'); // Item
                } else {
                    System.out.print(maze.getCell(i, j));
                }
                if (j < maze.getCols() - 1) {
                    System.out.print(' '); // Separate columns
                }
            }
            System.out.println();
        }
    }
}

class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

class Player {
    private Position position;

    public Player(int startX, int startY) {
        this.position = new Position(startX, startY);
    }

    public Position getPosition() {
        return position;
    }

    public boolean move(char direction, Maze maze) {
        int newX = position.getX();
        int newY = position.getY();

        switch (direction) {
            case 'W': newX--; break; // 上
            case 'A': newY--; break; // 左
            case 'S': newX++; break; // 下
            case 'D': newY++; break; // 右
            default: return false;
        }

        Position newPosition = new Position(newX, newY);
        if (newX < 0 || newX >= maze.getRows() || newY < 0 || newY >= maze.getCols() || maze.isObstacle(newPosition)) {
            return false;
        }

        if (maze.isWarpZone(newPosition)) {
            newPosition = randomPositionExcluding(newPosition);
        }

        position = newPosition;
        return true;
    }

    private Position randomPositionExcluding(Position exclude) {
        Random rand = new Random();
        Position newPosition;
        do {
            newPosition = new Position(rand.nextInt(10), rand.nextInt(10));
        } while (newPosition.equals(exclude) || newPosition.equals(new Position(9, 9))); // Exclude the goal
        return newPosition;
    }

    public void resetPosition() {
        position = new Position(0, 0);
    }
}

class Enemy {
    private Position position;
    private int startX, startY;

    public Enemy(int startX, int startY) {
        this.startX = startX;
        this.startY = startY;
        this.position = new Position(startX, startY);
    }

    public Position getPosition() {
        return position;
    }

    public void move(Maze maze) {
        Random rand = new Random();
        int direction = rand.nextInt(4);
        int newX = position.getX();
        int newY = position.getY();

        switch (direction) {
            case 0: newX--; break; // 上
            case 1: newY--; break; // 左
            case 2: newX++; break; // 下
            case 3: newY++; break; // 右
        }

        Position newPosition = new Position(newX, newY);
        if (newX >= 0 && newX < maze.getRows() && newY >= 0 && newY < maze.getCols() && !maze.isObstacle(newPosition)) {
            position = newPosition;
        }
    }

    public void resetPosition() {
        position = new Position(startX, startY);
    }
}

class Item {
    private Position position;
    private int scoreValue;

    public Item(Position position, int scoreValue) {
        this.position = position;
        this.scoreValue = scoreValue;
    }

    public Position getPosition() {
        return position;
    }

    public int getScoreValue() {
        return scoreValue;
    }
}
