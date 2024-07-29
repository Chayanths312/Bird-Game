import javax.swing.*;
import java.awt.*;

public class A6 extends JPanel {
    private static final int FLOCK_SIZE = 50;
    private Flock myFlock;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        myFlock.paintFlock(g2d);
    }
    public void run() {
        JFrame myFrame = new JFrame();

        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.setSize(new Dimension(500,500));
        myFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        myFrame.setLocationRelativeTo(null);

        myFrame.add(this);
        myFrame.setVisible(true);
        this.setSize(myFrame.getSize());

        myFlock = new Flock(FLOCK_SIZE, this);

        while(true) {
            myFlock.updateFlock();

            this.repaint();

            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static void main(String[] args) {
        A6 myPanel = new A6();
        myPanel.run();
    }
}