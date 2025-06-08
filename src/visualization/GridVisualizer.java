package visualization;

import model.Grid;
import model.Individual;
import model.Point;

import java.util.List;

/**
 * Provides functionality to visualize the grid and path in ASCII format.
 */
public class GridVisualizer {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";

    /**
     * Visualizes the grid with the path of the best individual.
     *
     * @param grid The grid
     * @param individual The individual whose path to display
     */
    public static void visualizeGrid(Grid grid, Individual individual) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        Point initialPoint = grid.getInitialPoint();
        Point finalPoint = grid.getFinalPoint();
        List<Point> obstacles = grid.getObstacles();
        List<Point> path = individual != null ? individual.getPath().getPoints() : null;

        // Create a 2D visualization grid
        char[][] visualGrid = new char[height][width];

        // Fill grid with empty spaces
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                visualGrid[y][x] = '.';
            }
        }

        // Mark special cost zones (just the outlines)
        // This would need additional Grid API to get special cost zone info
        // For now, we'll skip this or you can expand the Grid class to expose this info

        // Mark obstacles
        for (Point obstacle : obstacles) {
            int x = obstacle.getX() - 1; // Convert to 0-based indexing
            int y = height - obstacle.getY(); // Invert Y-axis for display
            visualGrid[y][x] = '#';
        }

        // Mark path points
        if (path != null) {
            for (Point point : path) {
                int x = point.getX() - 1; // Convert to 0-based indexing
                int y = height - point.getY(); // Invert Y-axis for display

                // Don't overwrite initial or final points
                if (!point.equals(initialPoint) && !point.equals(finalPoint)) {
                    visualGrid[y][x] = '*';
                }
            }
        }

        // Mark initial and final points
        int iniX = initialPoint.getX() - 1;
        int iniY = height - initialPoint.getY();
        int finX = finalPoint.getX() - 1;
        int finY = height - finalPoint.getY();
        visualGrid[iniY][iniX] = 'S';
        visualGrid[finY][finX] = 'E';

        // Print the grid with colors
        System.out.println("\nGrid Visualization:");

        // Print column numbers
        System.out.print("  ");
        for (int x = 0; x < width; x++) {
            System.out.print((x + 1) + " ");
        }
        System.out.println();

        for (int y = 0; y < height; y++) {
            System.out.print((height - y) + " "); // Print row numbers
            for (int x = 0; x < width; x++) {
                char cell = visualGrid[y][x];
                switch (cell) {
                    case 'S':
                        System.out.print(ANSI_GREEN + "S " + ANSI_RESET);
                        break;
                    case 'E':
                        System.out.print(ANSI_RED + "E " + ANSI_RESET);
                        break;
                    case '*':
                        System.out.print(ANSI_BLUE + "* " + ANSI_RESET);
                        break;
                    case '#':
                        System.out.print(ANSI_YELLOW + "# " + ANSI_RESET);
                        break;
                    case '.':
                        System.out.print(". ");
                        break;
                    default:
                        System.out.print(cell + " ");
                }
            }
            System.out.println();
        }

        // Print legend
        System.out.println("\nLegend:");
        System.out.println(ANSI_GREEN + "S " + ANSI_RESET + "- Start point");
        System.out.println(ANSI_RED + "E " + ANSI_RESET + "- End point");
        System.out.println(ANSI_BLUE + "* " + ANSI_RESET + "- Path");
        System.out.println(ANSI_YELLOW + "# " + ANSI_RESET + "- Obstacle");
        System.out.println(". - Empty space");

        // Print path cost if available
        if (individual != null) {
            System.out.println("\nPath Cost: " + individual.getPath().getCost());
            System.out.println("Comfort: " + individual.getComfort());
        }
    }
}