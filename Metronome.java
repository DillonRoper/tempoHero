import javax.swing.*;
import java.awt.event.*;

public class Metronome {

    private static volatile boolean running = true;

    public static void main(String[] args) {
        String input = JOptionPane.showInputDialog("Enter BPM:");
        int bpm = Integer.parseInt(input.trim());
        long interval = 60000 / bpm;

        System.out.println("Metronome started at " + bpm + " BPM. Press SPACE to stop.");

        // Start metronome thread
        
        Thread metronomeThread = new Thread(() -> {
                while (running) {
                    System.out.println("Tick");
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

        });

        metronomeThread.start();
        // Create invisible frame for key listening
        JFrame frame = new JFrame();
        frame.setUndecorated(true); // No window chrome
        frame.setSize(0, 0);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setFocusable(true);
        frame.requestFocus();

        // Add key listener
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    running = false;
                    System.out.println("Metronome stopped.");
                    frame.dispose(); // Close the frame
                }
            }
        });
    }
}

