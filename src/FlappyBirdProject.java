// Import stuff.
import java.awt.*; // Drawing panel.
import java.awt.event.KeyEvent; // Used to track which keys are pressed.
import java.awt.image.BufferedImage; // Buffered image to draw images off-screen, then display them smoothly.
import java.util.ArrayList; // Imports dynamic arrays for tracking various pipes.
import java.util.List; // Supplemental list.
import java.util.Scanner; // Reading the HS.
import java.io.*; // IO for score saving.

public class  FlappyBirdProject {
    // Define the refresh rate of the game. Fixed.
    public static final int frameRate = 60; // In frames per second.
    public static boolean dead = false; // Used to track if the player is dead or not.

    public static void main(String[] args) {
        // Define the resolution of the game.
        int width = 854, height = 480; // Default resolution is 480p (854x480).
        DrawingPanel panel = new DrawingPanel(width, height);
        Graphics window = panel.getGraphics();

        // Create the offscreen image for double buffering.
        BufferedImage offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics offscreenGraphics = offscreenImage.getGraphics();

        // Exits the game when you press escape.
        panel.onKeyDown((key) -> {
            if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);  // Exit the game when the escape key is pressed.
            }
        });

        // Various game variables.
        int deltaTime = 1000 / frameRate; // Delta time used for consistency with frame-rate.
        double timer = 0; // Timer that increments as the game runs. Used to float the player and control pipe spawns.
        double timeSincePipe = 0; // Stores the time that the last pipe spawned.
        double backgroundScrollSpeed = 0.08; // Speed of the clouds in the background.
        double playerY = 0; // Responsible for tracking the player's Y position.
        double[] playerYVelocity = {0}; // Use a wrapper object (int array) for playerYVelocity.
        boolean[] gameStart = {false}; // Used to track when the player presses space to start the game.
        List<Double> pipeX = new ArrayList<>(); // Tracks the x positions of pipes.
        List<Integer> pipeY = new ArrayList<>(); // Tracks the y positions of pipes.
        List<Boolean> passedPipe = new ArrayList<>(); // Checks if the player has passed the pipe yet.
        Integer score = 0; // Tracks player score
        int highscore = loadHighScore(); // new function reads the file for highscore
        Color pipe1 = new Color(46, 189, 26); // The darker green pipe color.
        Color pipe2 = new Color(76, 227, 58); // THe lighter green pipe color.

        // The player's controls. Just press space to fly.
        panel.onKeyDown((key) -> {
            if (key == KeyEvent.VK_SPACE) {
                playerYVelocity[0] = 350;  // Apply upward velocity when the space key is pressed
                gameStart[0] = true;
            }
        });

        // Define the number of clouds in the background.
        int cloudCount = 14;
        int[] cloudXPositions = new int[cloudCount]; // A list that keeps track of cloud positions.
        int[] cloudSizes = new int[cloudCount]; // A list that keeps track of cloud sizes.
        int maxCloudSize = (int)((double)height/3 * 1.5); // Gives max cloud size for smooth scrolling.
        for (int i = 0; i < cloudCount; i++) {
            cloudXPositions[i] = ((int)(width * 1.5) / (cloudCount)) * i;
            cloudSizes[i] = (int)((double)height/3 * (Math.random() + 0.5));
        }

        // This is the game loop. Basically, it works by drawing a bunch of graphics off-screen.
        // This insures the image is fully prepared to be displayed. Then, we draw the off-screen graphics onto the
        // main display all at once.
        while (true) {
            // Increments the timer.
            timer += 0.005 * deltaTime;

            // Simulate the player once they press space.
            if (gameStart[0]) {
                // Calculate player positions.
                double deltaSeconds = deltaTime / 1000.0;
                playerYVelocity[0] -= 1000 * deltaSeconds;
                playerY -= playerYVelocity[0] * deltaSeconds;
            } else {
                // Make the player float up and down until they start.
                playerY = (double) height/2 - (double) 35/2 + Math.sin(timer) * 12;
                timeSincePipe = timer;
            }

            // Display fresh graphics. Create the background.
            offscreenGraphics.setColor(new Color(97, 187, 189)); // Light blue sky background.
            offscreenGraphics.fillRect(0, 0, width, height);
            offscreenGraphics.setColor(Color.white); // White cloud background.
            for (int i = 0; i < cloudCount; i++){
                offscreenGraphics.fillOval(cloudXPositions[i], height - cloudSizes[i]/2, cloudSizes[i], cloudSizes[i]);
                cloudXPositions[i] -= (int)(backgroundScrollSpeed * deltaTime);
                if (cloudXPositions[i] < -maxCloudSize){
                    cloudXPositions[i] = width + maxCloudSize;
                }
            }

            // Draw the player/bird.
            int playerX = width/2 - 150;
            offscreenGraphics.setColor(new Color(240, 193, 122)); // Yellow body.
            offscreenGraphics.fillOval(playerX, (int)playerY, 40, 35);
            offscreenGraphics.setColor(new Color(240, 102, 38)); // Orange-red beak.
            offscreenGraphics.fillOval(playerX + 20, (int)playerY + 18, 25, 15);
            offscreenGraphics.setColor(new Color(249, 231, 174));
            offscreenGraphics.fillOval(playerX - 5, (int)playerY + 5, 25, 20);

            // If it's been a while since the last pipe appeared.
            if (timer - timeSincePipe >= 8){
                // Update the time since the last pipe appeared.
                timeSincePipe = timer;

                // Add a new pipe to the two dynamic arrays.
                pipeX.add((double)width + 50); // Adds a pipe offscreen to the right.
                pipeY.add((int)((Math.random() - 0.5) * height/2) + height/2); // Gives the pipe a random y position.
                passedPipe.add(false); // Adds a false boolean to a pipe, meaning it hasn't passed yet.
            }
            // Draws the pipes on the buffered image.
            for (int i = 0; i < pipeX.size(); i += 0){ // "i" doesn't increment here because we need to do it later.
                offscreenGraphics.setColor(pipe1); // Green pipe.
                // Draws the top pipe at the pipe's y position - 65.
                offscreenGraphics.fillRect((int)(double)pipeX.get(i), 0, 50, pipeY.get(i) - 65);
                // Draws the bottom pipe at the pipe's y position + 65.
                offscreenGraphics.fillRect((int)(double)pipeX.get(i), pipeY.get(i) + 65, 50, height);

                offscreenGraphics.setColor(pipe2); // Light green pipe.
                // Draws the top pipe's highlights.
                offscreenGraphics.fillRect((int)(double)pipeX.get(i) + 10, 0, 25, pipeY.get(i) - 65);
                // Draws the bottom pipe's highlights.
                offscreenGraphics.fillRect((int)(double)pipeX.get(i) + 10, pipeY.get(i) + 65, 25, height);

                offscreenGraphics.setColor(pipe1); // Green pipe.
                // Draws the top pipe cap.
                offscreenGraphics.fillRect((int)(double)pipeX.get(i) - 7, pipeY.get(i) - 65 - 25, 64, 25);
                // Draws the bottom pipe cap.
                offscreenGraphics.fillRect((int)(double)pipeX.get(i) - 7, pipeY.get(i) + 65, 64, 25);

                offscreenGraphics.setColor(pipe2); // Light green pipe.
                // Draws the top pipe cap's highlights.
                offscreenGraphics.fillRect((int)(double)pipeX.get(i) + 3, pipeY.get(i) - 65 - 25, 28, 25);
                // Draws the bottom pipe cap's highlights.
                offscreenGraphics.fillRect((int)(double)pipeX.get(i) + 3, pipeY.get(i) + 65, 28, 25);

                // Scrolls the pipe over to the left.
                pipeX.set(i, pipeX.get(i) - 0.12 * deltaTime);

                // Pipe collision 5/25 JB
                // This could use some tweaking detection box wise.
                int playerWidth = 40;  // The next couple of lines declare the bounds
                int playerHeight = 35;
                int pipeLeft = (int)(double)pipeX.get(i);
                int pipeRight = pipeLeft + 50;
                int gap = 130; // This indicated the acceptable area for the player to pass through
                int pipeGapY = pipeY.get(i); // This gets the location of the acceptable area

                // These declare the birds hit box
                int birdTop = (int)playerY;
                int birdBottom = birdTop + playerHeight;
                int birdLeft = playerX; // It says redundant but only seems to work if it's here
                int birdRight = birdLeft + playerWidth;

                // Collision check for the hitboxes.
                if (birdRight > pipeLeft && birdLeft < pipeRight) {
                    if (birdTop < pipeGapY - gap / 2 || birdBottom > pipeGapY + gap / 2) {
                        dead = true; // Set dead to true if the player is out of bounds.
                        break; // Breaks out of the for loop to prevent index errors.
                    }
                }

                // Increments score. Uses a boolean array to check if the pipe has already passed.
                if (birdRight > pipeRight && !passedPipe.get(i)){
                    score++;
                    if (score > highscore){ // Replaces highscore if needed.
                        highscore = score;
                        saveHighScore(highscore);
                    }
                    passedPipe.set(i, true);
                }

                // If the pipe is off-screen, delete.
                // Increment "i" only if a pipe isn't deleted.
                // This is because deleting a pipe shifts the whole array anyway.
                if (pipeX.get(i) < -70){
                    pipeX.remove(i); // Removes the pipe from memory.
                    pipeY.remove(i);
                    passedPipe.remove(i);
                } else {
                    i++;
                }
            }

            // UI Code.
            offscreenGraphics.setColor(new Color(255, 255, 255)); // White text.
            offscreenGraphics.setFont(new Font("UI Font", Font.BOLD, 25)); // Sets a basic font.
            offscreenGraphics.drawString("Highscore: " + highscore, 5, 30); // Draws highscore.
            if (!gameStart[0] && !dead) { // If the game has started, show the score text.
                offscreenGraphics.drawString("[Space] to fly", width / 2 - 85, 100);
            } else if (!dead){ // If the game has not started yet, show instructions.
                offscreenGraphics.setFont(new Font("SanSerif", Font.BOLD, 75)); // Makes the font smaller.
                String scoreString = Integer.toString(score); // Converts the integer score to a string.
                offscreenGraphics.drawString(scoreString, width / 2 - scoreString.length() * 75 / 3, 100);
                /* Useless score display code that displays in top left.
                offscreenGraphics.setColor(new Color(255, 255, 255)); // White text.
                offscreenGraphics.setFont(new Font("UI Font", Font.BOLD, 25)); // Sets a basic font.
                offscreenGraphics.drawString("Score: " + score, width - 125, 30); // Draws score.
                */
            }

            // Simplified death code and displays death screen.
            if (dead) {
                offscreenGraphics.setColor(new Color(255, 255, 255)); // White text.
                offscreenGraphics.setFont(new Font("UI Font", Font.BOLD, 25)); // Sets a basic font.
                offscreenGraphics.drawString("Your Score: " + score, width / 2 - (10 + score.toString().length())  * 25 / 3 , height / 2 );
                offscreenGraphics.drawString("Game Over", width / 2 - 9 * 25 / 3, height / 2 - 50);
                System.out.println("Highscore: " + highscore + " | Score: " + score );
                window.drawImage(offscreenImage, 0, 0, null);
                panel.sleep(3000); // Freeze for 1 second.
                restartGame(score, highscore, gameStart, pipeX, pipeY, passedPipe);
                score = 0; // Reset the score.
            }

            // Copy the off-screen image to the actual screen.
            window.drawImage(offscreenImage, 0, 0, null);

            // Additional ground collision 5/25 JB
            if (playerY < 0 || playerY + 35 > height) {
                System.out.println("Game Over - Can't fly?");
                dead = true; // Set dead to true if the player is out of bounds.
            }

            // Wait the specified refresh rate.
            panel.sleep(deltaTime);
        }
    }

    // Restarts the game by clearing pipe lists.
    public static void restartGame(int score, int highscore, boolean[] gameStart, List<Double> pipeX, List<Integer> pipeY, List<Boolean> passedPipe){
        dead = false; // Resets dead to false.

        if (score > highscore){ // Replaces high score if needed.
            highscore = score;
            saveHighScore(score);
        }
        gameStart[0] = false;
        pipeX.clear();
        pipeY.clear();
        passedPipe.clear();
    }

    // Reading HS from score file
    public static int loadHighScore() {
        try {
            File file = new File("Highscore.txt");
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                int score = scanner.nextInt();
                return score;
            }
            else {
                System.out.println("Invalid File. returning to 0.");
                return 0;
            }

        } catch (IOException | NumberFormatException e){
            System.out.println("Error reading save file.");
        }
        return 0;
    }

    // Writing HS if more than current.
    public static void saveHighScore(int score) {
        try (FileWriter writer = new FileWriter("Highscore.txt")) {
            writer.write(String.valueOf(score));
            // writer.close() is called automatically
        } catch (IOException e) {
            System.out.println("Error writing high score: " + e.getMessage());
        }
    }
}