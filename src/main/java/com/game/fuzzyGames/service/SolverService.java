package com.game.fuzzyGames.service;

import com.game.fuzzyGames.model.GameData;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

@Service
public class SolverService {

    private final DataService dataService;

    @Autowired
    public SolverService(DataService dataService) {
        this.dataService = dataService;
    }

    public String solveGame() {

        // Load game data
        GameData gameData = dataService.loadGameData();

        // Check if data is loaded successfully
        if (gameData == null) {
            return "Failed to load game data";
        }

        // Calculate game result based on loaded data
        return calculateGameResult(gameData);
    }

    private String calculateGameResult(GameData gameData) {
        StringBuilder result = new StringBuilder();

        // Adding initial information about strategies and trust levels
        result.append("M = ")
                .append(gameData.getM_degrees().toString())
                .append(" and N = ")
                .append(gameData.getN_degrees().toString())
                .append("<br><br>");

        // Adding the matrix to the result
        result.append("Matrix:<br>");
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        for (List<Double> row : gameData.getMatrix()) {
            for (Double value : row) {
                result.append(decimalFormat.format(value)).append("\t");
            }
            result.append("<br>");
        }

        result.append("<br>");

        // Convert lists of trust degrees to sets to remove duplicates
        Set<Double> M_degreesSet = new HashSet<>(gameData.getM_degrees());
        Set<Double> N_degreesSet = new HashSet<>(gameData.getN_degrees());

        // Create a list to store the pairs of trust levels
        List<SimpleEntry<Double, Double>> trustLevelPairs = new ArrayList<>();

        // Generate all pair combinations of trust levels from the two sets
        for (Double mDegree : M_degreesSet) {
            for (Double nDegree : N_degreesSet) {
                trustLevelPairs.add(new SimpleEntry<>(mDegree, nDegree));
            }
        }

        // Sort the trust level pairs by ascending distance (a^2 + b^2)
        trustLevelPairs.sort(Comparator.comparing(pair ->
                Math.pow(pair.getKey(), 2) + Math.pow(pair.getValue(), 2)));

        Map<SimpleEntry<Double, Double> ,Double> solutions = new HashMap<>();
        for (SimpleEntry<Double, Double> pair : trustLevelPairs) {
            // Extract the trust levels for M and N
            double mTrust = pair.getKey();
            double nTrust = pair.getValue();

            // Now, implement the logic to solve the fuzzy minimax problem for this pair.
            double solution = solveForPair(mTrust, nTrust, gameData);
            solutions.put(pair, solution);

            // Format the trust levels and solution to remove trailing zeros
            String formattedSolution = String.valueOf(solution).replaceAll("\\.?0*$", "");

            result.append("For trust levels (M: ")
                    .append(mTrust)
                    .append(", N: ")
                    .append(nTrust)
                    .append("), the solution is: ")
                    .append(formattedSolution)
                    .append("<br>");
        }

        // Append the solutions in the specified format
        result.append("<br>W = { ");

        // Type-2 membership function (Definition 2.1):
        //   eta(z, 1) = max min{alpha, beta} over pairs whose game value equals z,
        //   eta(z, 0) = max min{alpha, beta} over pairs whose game value differs from z.
        Map<Double, Double> etaOne = new LinkedHashMap<>();
        for (SimpleEntry<Double, Double> pair : trustLevelPairs) {
            double minTrust = Math.min(pair.getKey(), pair.getValue());
            etaOne.merge(solutions.get(pair), minTrust, Math::max);
        }

        for (Map.Entry<Double, Double> entry : etaOne.entrySet()) {
            result.append("((").append(format(entry.getKey())).append(", 1), ")
                    .append(entry.getValue()).append("), ");
        }

        // Values of the game that are not equal to some z get u = 0
        double etaOther = 0;
        for (SimpleEntry<Double, Double> pair : trustLevelPairs) {
            etaOther = Math.max(etaOther, Math.min(pair.getKey(), pair.getValue()));
        }
        for (Double z : etaOne.keySet()) {
            double etaZero = 0;
            for (SimpleEntry<Double, Double> pair : trustLevelPairs) {
                if (!solutions.get(pair).equals(z)) {
                    etaZero = Math.max(etaZero, Math.min(pair.getKey(), pair.getValue()));
                }
            }
            if (etaZero > 0 && etaZero < etaOther) {
                result.append("((").append(format(z)).append(", 0), ").append(etaZero).append("), ");
            }
        }

        result.append("<br>((r, 0), ").append(etaOther).append(") |");
        for (Double z : etaOne.keySet()) {
            double etaZero = 0;
            for (SimpleEntry<Double, Double> pair : trustLevelPairs) {
                if (!solutions.get(pair).equals(z)) {
                    etaZero = Math.max(etaZero, Math.min(pair.getKey(), pair.getValue()));
                }
            }
            if (etaZero < etaOther) {
                result.append(" r != ").append(format(z)).append(",");
            }
        }
        result.deleteCharAt(result.length() - 1);
        result.append("}.");

        // After building the result string, write it to a file
        try (FileWriter writer = new FileWriter("src/main/resources/data/gameResult.txt")) {
            // Replacing '<br>' with newline for plain text formatting
            String resultText = result.toString().replace("<br>", "\n");
            writer.write(resultText);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    private static String format(double value) {
        return String.valueOf(value).replaceAll("\\.?0*$", "");
    }

    private double solveForPair(double mTrust, double nTrust, GameData gameData) {
        List<Double> M_degrees = gameData.getM_degrees();
        List<Double> N_degrees = gameData.getN_degrees();
        List<List<Double>> matrix = gameData.getMatrix();

        // Lists to store indices of strategies that match trust levels
        List<Integer> validMIndexes = new ArrayList<>();
        List<Integer> validNIndexes = new ArrayList<>();

        // Determine valid strategies based on trust levels
        for (int i = 0; i < M_degrees.size(); i++) {
            if (M_degrees.get(i) >= mTrust) {
                validMIndexes.add(i);
            }
        }
        for (int j = 0; j < N_degrees.size(); j++) {
            if (N_degrees.get(j) >= nTrust) {
                validNIndexes.add(j);
            }
        }

        // Exclude strategies from the matrix that do not match trust levels
        List<List<Double>> reducedMatrix = reduceMatrix(matrix, validMIndexes, validNIndexes);

        // Compute the minimax solution based on the updated matrix
        return minimaxSolution(reducedMatrix);
    }

    private List<List<Double>> reduceMatrix(List<List<Double>> matrix, List<Integer> validMIndexes, List<Integer> validNIndexes) {
        // Create a new matrix with valid strategies
        List<List<Double>> reducedMatrix = new ArrayList<>();
        for (Integer mIndex : validMIndexes) {
            List<Double> reducedRow = new ArrayList<>();
            for (Integer nIndex : validNIndexes) {
                reducedRow.add(matrix.get(mIndex).get(nIndex));
            }
            reducedMatrix.add(reducedRow);
        }
        return reducedMatrix;
    }

    private double minimaxSolution(List<List<Double>> matrix) {
        // Value of the matrix game in mixed strategies (player 1 maximizes):
        //   max v  s.t.  sum_i a_ij x_i >= v (for each column j),  sum_i x_i = 1,  x_i >= 0,  v free.
        // Variables: x_1..x_m, v.
        int m = matrix.size();
        int n = matrix.get(0).size();

        double[] objective = new double[m + 1];
        objective[m] = 1;
        LinearObjectiveFunction f = new LinearObjectiveFunction(objective, 0);

        List<LinearConstraint> constraints = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            double[] coefficients = new double[m + 1];
            for (int i = 0; i < m; i++) {
                coefficients[i] = matrix.get(i).get(j);
            }
            coefficients[m] = -1;
            constraints.add(new LinearConstraint(coefficients, Relationship.GEQ, 0));
        }

        double[] sum = new double[m + 1];
        Arrays.fill(sum, 0, m, 1);
        constraints.add(new LinearConstraint(sum, Relationship.EQ, 1));

        for (int i = 0; i < m; i++) {
            double[] nonNegative = new double[m + 1];
            nonNegative[i] = 1;
            constraints.add(new LinearConstraint(nonNegative, Relationship.GEQ, 0));
        }

        PointValuePair solution = new SimplexSolver().optimize(
                new MaxIter(1000), f, new LinearConstraintSet(constraints),
                GoalType.MAXIMIZE, new NonNegativeConstraint(false));

        // Round to 4 decimal places for readable output
        return Math.round(solution.getValue() * 10000.0) / 10000.0;
    }
}
