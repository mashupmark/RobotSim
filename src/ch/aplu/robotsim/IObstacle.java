// IObstacle.java
// Code by Stefan Moser

package ch.aplu.robotsim;

import ch.aplu.jgamegrid.GGVector;

import java.util.List;

interface IObstacle {

    GGVector closestPointTo(GGVector p);

    /**
     * returns true if the given point lies inside (or on the
     * edge) of the obstacle.
     */
    boolean liesInside(GGVector p);

    List<GGVector> getIntersectionPointsWith(
            LineSegment[] viewBoarderLines);

}