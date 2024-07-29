import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Flock {
    private Bird[] theFlock;

    Flock(int flockSize, Container parent) {
        Bird.parent = parent;
        theFlock = new Bird[flockSize];

        for (int i=0; i<flockSize; i++) {
            theFlock[i] = new Bird();
        }
    }

    public void updateFlock() {
        ArrayList<viewInfo> visibleObjects;
        Polygon viewCone;

        for(Bird myBird:theFlock) {
            visibleObjects = new ArrayList<>();
            viewCone = myBird.getViewCone();

            for(Bird otherBird:theFlock) {
                if(myBird != otherBird) {
                    if(viewCone.contains(otherBird.getPos())) {
                        visibleObjects.add(otherBird.getViewInfo());
                    }
                }
            }
            myBird.setVisibleObjects(visibleObjects);
            myBird.move();
            myBird.think();
        }
        checkCollision();
    }

    public void paintFlock(Graphics2D g) {
        for(Bird myBird:theFlock) {
            myBird.paint(g);
        }
    }

    private void checkCollision() {
        if(theFlock.length > 1) {
            Rectangle2D currHitBox;
            for(int i=0; i<theFlock.length-1; i++) {
                currHitBox = theFlock[i].getHitBox();
                for(int j=i+1; j<theFlock.length; j++) {
                    Rectangle2D otherHitBox = theFlock[j].getHitBox();
                    if(currHitBox.intersects(otherHitBox)) {
                        Rectangle2D offset = currHitBox.createIntersection(otherHitBox);
                        double xOffset = offset.getWidth();
                        double yOffset = offset.getHeight();
                        Point2D bird1Pos = theFlock[i].getPos();
                        Point2D bird2Pos = theFlock[j].getPos();
                        if(bird2Pos.getX() - bird1Pos.getX() < 0) {
                            xOffset *= -1;
                        }
                        if(bird2Pos.getY() - bird1Pos.getY() < 0) {
                            yOffset *= -1;
                        }
                        theFlock[j].shove(xOffset, yOffset);
                    }
                }
            }
        }
    }
}