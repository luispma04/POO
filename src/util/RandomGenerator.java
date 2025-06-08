package util;

import model.Grid;
import model.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for generating random grid elements.
 */
public class RandomGenerator {
    /**
     * Generates random obstacles.
     *
     * @param numObstacles The number of obstacles to generate
     * @param width The grid width
     * @param height The grid height
     * @param initialPoint The initial point
     * @param finalPoint The final point
     * @param random The random number generator
     * @return A list of randomly generated obstacle points
     */
    public static List<Point> generateRandomObstacles(int numObstacles, int width, int height,
                                                      Point initialPoint, Point finalPoint, Random random) {
        List<Point> obstacles = new ArrayList<>();

        while (obstacles.size() < numObstacles) {
            int x = random.nextInt(width) + 1;
            int y = random.nextInt(height) + 1;
            Point obstacle = new Point(x, y);

            // Don't place obstacles at the initial or final points
            if (!obstacle.equals(initialPoint) && !obstacle.equals(finalPoint) && !obstacles.contains(obstacle)) {
                obstacles.add(obstacle);
            }
        }

        return obstacles;
    }

    /**
     * Generates random special cost zones.
     *
     * @param numZones The number of zones to generate
     * @param width The grid width
     * @param height The grid height
     * @param random The random number generator
     * @return A list of randomly generated special cost zones
     */
    public static List<Grid.SpecialCostZone> generateRandomSpecialCostZones(int numZones, int width, int height, Random random) {
        List<Grid.SpecialCostZone> zones = new ArrayList<>();

        for (int i = 0; i < numZones; i++) {
            // Generate random rectangle coordinates
            int x1 = random.nextInt(width - 1) + 1;
            int y1 = random.nextInt(height - 1) + 1;
            int x2 = random.nextInt(width - x1) + x1 + 1;
            int y2 = random.nextInt(height - y1) + y1 + 1;

            // Generate random cost (between 2 and 10)
            int cost = random.nextInt(9) + 2;

            Grid.SpecialCostZone zone = new Grid.SpecialCostZone(new Point(x1, y1), new Point(x2, y2), cost);
            zones.add(zone);
        }

        return zones;
    }
}
