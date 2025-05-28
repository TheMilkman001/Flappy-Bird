// Import stuff.
import java.awt.*; // Drawing panel.
import java.awt.event.KeyEvent; // Used to track which keys are pressed.
import java.awt.image.BufferedImage; // Buffered image to draw images off-screen, then display them smoothly.
import java.util.ArrayList; // Imports dynamic arrays for tracking various pipes.
import java.util.List; // Supplemental list.

public class  FlappyBirdProject {
    // Define the refresh rate of the game. Fixed.
    public static final int frameRate = 60; // In frames per second.

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
        int score = 0; // Tracks player score
        int highscore = 0; // Tracks highscore. I will find a way to store this in a form of save file later. 5/25 JB

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
            offscreenGraphics.setColor(new Color(46, 189, 26)); // Green pipe.
            for (int i = 0; i < pipeX.size(); i += 0){ // "i" doesn't increment here because we need to do it later.
                // Draws the top pipe at the pipe's y position - 65.
                offscreenGraphics.fillRect((int)(double)pipeX.get(i), 0, 50, pipeY.get(i) - 65);
                offscreenGraphics.fillRect((int)(double)pipeX.get(i) - 7, pipeY.get(i) - 65 - 25, 64, 25);
                // Draws the bottom pipe at the pipe's y position + 65.
                offscreenGraphics.fillRect((int)(double)pipeX.get(i), pipeY.get(i) + 65, 50, height);
                offscreenGraphics.fillRect((int)(double)pipeX.get(i) - 7, pipeY.get(i) + 65, 64, 25);
                pipeX.set(i, pipeX.get(i) - 0.12 * deltaTime); // Scrolls the pipe over to the left.

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
                        // Print stuff and end the game.
                        System.out.println("Game Over - Hit Pipe");
                        System.out.println("Score: "+ score);
                        System.exit(0);
                    }
                }

                // Increments score. Uses a boolean array to check if the pipe has already passed.
                if (birdRight > pipeRight && !passedPipe.get(i)){
                    score++;
                    if (score > highscore){ // Replaces highscore if needed.
                        highscore = score;
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

            // Copy the off-screen image to the actual screen.
            window.drawImage(offscreenImage, 0, 0, null);

            // Additional ground collision 5/25 JB
            if (playerY < 0 || playerY + 35 > height) {
                System.out.println("Game Over - Can't fly?");
                if (score > highscore){ // Replaces highscore if needed
                    highscore = score;
                }
                gameStart[0] = false;
                pipeX.clear();
                pipeY.clear();
                passedPipe.clear();
            }

            // Wait the specified refresh rate.
            panel.sleep(deltaTime);
        }
    }
}