import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;

public class Bird {
    private static final boolean DEBUG = false;
    private static final int SCALE = 2;
    private static final int FLAP_MIN = 5;
    private static final int FLAP_MAX = 25;
    private static final double SPEED_MIN = 0.25;
    private static final double SPEED_MAX = 5.0;
    private static final double TURN_MIN = 2;
    private static final double TURN_MAX = 5;
    private static final double VIEW_FACTOR = 0.3;

    private double x;
    private double y;
    private double dir;
    private double speed;
    private int flapPos;
    private int flapSpeed;
    private double turnSpeed;
    public static Container parent;
    private Rectangle2D hitBox;
    private boolean wasShoved;
    private double viewDist;
    private Polygon viewCone;
    private ArrayList<viewInfo> visibleObjects;


    public Bird() {
        Random rand = new Random();

        x = rand.nextDouble(0, parent.getWidth());
        y = rand.nextDouble(0, parent.getHeight());
        dir = rand.nextDouble(0, 360);
        speed = rand.nextDouble(SPEED_MIN, SPEED_MAX);
        flapPos = rand.nextInt(FLAP_MIN+1, FLAP_MAX);
        flapSpeed = 2;
        hitBox = new Rectangle2D.Double();
        wasShoved = false;
        turnSpeed = rand.nextDouble(TURN_MIN, TURN_MAX);
        double viewMax = ((parent.getWidth() + parent.getHeight())/2)*VIEW_FACTOR;
        viewDist = rand.nextDouble(viewMax*0.25, viewMax);
        viewCone = new Polygon();
    }

    public void move() {
        if(flapPos <= FLAP_MIN || flapPos >= FLAP_MAX) {
            flapSpeed *= -1;
        }
        flapPos += flapSpeed;

        double dirRad = Math.toRadians(dir);
        x += Math.sin(dirRad) * speed;
        y -= Math.cos(dirRad) * speed;

        if(x > parent.getWidth()) x = 0;
        else if(x < 0) x = parent.getWidth();
        if(y > parent.getHeight()) y = 0;
        else if(y < 0) y = parent.getHeight();
    }

    public void paint(Graphics2D g) {
        Polygon leftWing = new Polygon();
        Polygon rightWing = new Polygon();
        Polygon tail = new Polygon();
        Polygon beak = new Polygon();

        tail.addPoint((int)x, (int)y + 6*SCALE);
        tail.addPoint((int)x-3, (int)y + 3*SCALE);
        tail.addPoint((int)x+3, (int)y + 3*SCALE);
        beak.addPoint((int)x, (int)y - 8*SCALE);
        beak.addPoint((int)x - 5, (int)y - 5*SCALE);
        beak.addPoint((int)x, (int)y - 3 *SCALE);

        leftWing.addPoint((int)x, (int)y + 3*SCALE);
        leftWing.addPoint((int)x - flapPos*SCALE, (int)y);
        leftWing.addPoint((int)x, (int)y - 3*SCALE);
        rightWing.addPoint((int)x, (int)y + 3*SCALE);
        rightWing.addPoint((int)x + flapPos*SCALE, (int)y);
        rightWing.addPoint((int)x, (int)y - 3*SCALE);

        viewCone.reset();
        viewCone.addPoint((int)x, (int)y);
        viewCone.addPoint((int)(x+viewDist), (int)(y-viewDist));
        viewCone.addPoint((int)(x-viewDist), (int)(y-viewDist));
        viewCone = rotate(new Point((int)x, (int)y), viewCone, dir);

        g.setPaint(new Color(255, 0, 0));
        g.fillPolygon(rotate(new Point((int) x, (int) y), leftWing, dir));
        g.setPaint(new Color(0, 0, 255));
        g.fillPolygon(rotate(new Point((int) x, (int) y), rightWing, dir));
        g.setPaint(new Color(0, 255, 0));
        g.fillPolygon(rotate(new Point((int) x, (int) y), beak, dir));
        g.setPaint(new Color(0, 255, 0));
        g.fillPolygon(rotate(new Point((int) x, (int) y), tail, dir));


        leftWing = rotate(new Point((int) x, (int) y), leftWing, dir);
        rightWing = rotate(new Point((int) x, (int) y), rightWing, dir);
        tail = rotate(new Point((int) x, (int) y), tail, dir);
        beak = rotate(new Point((int) x, (int) y), beak, dir);

        hitBox = leftWing.getBounds2D().createUnion(rightWing.getBounds2D()).createUnion(beak.getBounds2D()).createUnion(tail.getBounds2D());


        if(DEBUG) {
            if(wasShoved)
                g.setPaint(Color.red);
            else
                g.setPaint(Color.black);
            g.draw(hitBox);
            g.setPaint(Color.black);
            g.drawPolygon(viewCone);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
            g.drawString(String.format("num: %d", visibleObjects.size()), (int)x, (int)y);
        }
        wasShoved = false;
    }

    public void think() {
        if(!visibleObjects.isEmpty()){
            viewInfo closestObj = visibleObjects.get(0);
            for(int i = 0; i < visibleObjects.size();i++){
                if(distance(this.x,this.y, visibleObjects.get(i).x, visibleObjects.get(i).y) > distance(this.x,this.y,closestObj.x, closestObj.y )){
                    closestObj = visibleObjects.get(i);
                }
                double offset = Math.atan2((this.y - closestObj.y),(this.x - closestObj.x));
                offset = Math.toDegrees(offset);
                setDir((int) offset/3);

            }
                /*
                if(closestObj.x < 0){
                } else if (this.x < closestObj.x && this.y > closestObj.y) {

                } else if (this.x < closestObj.x && this.y < closestObj.y) {

                } else if (this.x > closestObj.x && this.y < closestObj.y) {
                }

                 */


        }
    }



    private Polygon rotate(Point origin, Polygon poly, double degrees) {
        double radians = Math.toRadians(degrees);
        double cosAngle = Math.cos(radians);
        double sinAngle = Math.sin(radians);
        int[] xpts = poly.xpoints;
        int[] ypts = poly.ypoints;
        Point tempPt = new Point();
        Point newPt = new Point();
        Polygon newPoly = new Polygon();

        for(int i=0; i<poly.npoints; i++) {
            tempPt.x = xpts[i] - origin.x;
            tempPt.y = ypts[i] - origin.y;

            newPt.x = (int)(tempPt.x*cosAngle - tempPt.y*sinAngle);
            newPt.y = (int)(tempPt.y*cosAngle + tempPt.x*sinAngle);

            newPt.x += origin.x;
            newPt.y += origin.y;

            newPoly.addPoint(newPt.x, newPt.y);
        }

        return newPoly;
    }

    public void setDir(int dir) {
        this.dir = dir;
    }

    public Rectangle2D getHitBox() {
        return hitBox;
    }

    public Point2D.Double getPos() {
        return new Point2D.Double(x, y);
    }

    public void shove(double xOffset, double yOffset) {
        wasShoved = true;
        this.x += xOffset;
        this.y += yOffset;
    }

    public viewInfo getViewInfo() {
        viewInfo myViewInfo = new viewInfo();

        myViewInfo.x = x;
        myViewInfo.y = y;
        myViewInfo.speed = speed;
        myViewInfo.dir = dir;

        return myViewInfo;
    }

    public void setVisibleObjects(ArrayList<viewInfo> visibleObjects) {
        this.visibleObjects = visibleObjects;
    }

    public Polygon getViewCone() {
        return viewCone;
    }

    private static double distance(double x1, double y1, double x2, double y2) {
        double dx = x1-x2;
        double dy = y1-y2;

        return Math.sqrt((dx * dx) + (dy * dy));
    }

    private static double angle(double x1, double y1, double x2, double y2) {
        double theta = Math.atan2(y2-y1, x2-x1);
        theta = Math.toDegrees(theta);
        return ((theta % 360) + 450) % 360;
    }
}