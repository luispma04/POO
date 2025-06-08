import io.InputParser;
import io.OutputGenerator;
import model.Grid;
import model.Individual;
import simulation.PEC;
import simulation.Simulator;

import java.io.IOException;
import java.util.Map;

/**
 * The main class for the Pathfinder simulation.
 * Handles command line arguments and initializes the simulation.
 */
public class Main {
    /**
     * The entry point for the application.
     * Parses command-line arguments and runs the simulation.
     *
     * @param args Command line arguments (-r or -f followed by parameters)
     */
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("Error: Insufficient arguments.");
                System.err.println("Usage: java -jar project.jar -r n m xi yi xf yf nscz nobs τ ν νmax k μ δ ρ [--visualize]");
                System.err.println("   or: java -jar project.jar -f <infile> [--visualize]");
                System.exit(1);
            }

            String option = args[0];
            Map<String, Object> parameters;

            // Check for visualization flag
            boolean visualizationEnabled = false;
            for (String arg : args) {
                if ("--visualize".equals(arg)) {
                    visualizationEnabled = true;
                    break;
                }
            }

            // Set visualization flag
            OutputGenerator.setPathVisualizationEnabled(visualizationEnabled);

            if ("-r".equals(option)) {
                // Random generation mode
                parameters = InputParser.parseRandomParameters(args);
            } else if ("-f".equals(option)) {
                // File input mode
                String filePath = args[1];
                parameters = InputParser.parseFile(filePath);
            } else {
                System.err.println("Error: Invalid option. Use -r or -f.");
                System.exit(1);
                return;
            }

            // Print input parameters
            OutputGenerator.printInputParameters(parameters);

            // Create grid
            Grid grid = (Grid) parameters.get("grid");

            // Initialize simulator
            Simulator simulator = new Simulator(
                    grid,
                    (int) parameters.get("initialPopulation"),
                    (int) parameters.get("maxPopulation"),
                    (double) parameters.get("comfortSensitivity"),
                    (double) parameters.get("deathMean"),
                    (double) parameters.get("moveMean"),
                    (double) parameters.get("reproductionMean"),
                    (double) parameters.get("simulationTime")
            );

            // Run simulation
            Individual bestIndividual = simulator.simulate();

            // Print final result
            OutputGenerator.printFinalResult(bestIndividual);

        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error during simulation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}