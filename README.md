# Fuzzy Games with Fuzzy Strategies — Master's Thesis (2024)

**Master's thesis (2024):** "Matrix Game with Fuzzy Sets of Player Strategies". Taras Shevchenko National University of Kyiv, Faculty of Computer Science and Cybernetics, Department of System Analysis and Decision Making Theory; specialty 124 System Analysis, program "Systems and Methods of Decision Making". Author: Denys Piven. Supervisor: Prof. Serhii Mashchenko, Dr. Sc. (Phys.-Math.). The thesis text (Ukrainian) is in [`thesis/`](thesis/Matrix_game_with_fuzzy_sets_of_player_strategies.pdf), its LaTeX source in [`tex/`](tex/main.tex). Continues the coursework [Fuzzy-Games-Coursework](https://github.com/DenysPiven/Fuzzy-Games-Coursework) (2023).

This project, "Fuzzy Games with Fuzzy Strategies," is designed to demonstrate fuzzy logic strategies in game theory. It includes components for managing game data, solving fuzzy minimax problems, and providing a web interface for viewing game results.

### Project Structure

- **src/main/java/com/game/fuzzyGames/controller/GameController.java**
- **src/main/java/com/game/fuzzyGames/model/GameData.java**
- **src/main/java/com/game/fuzzyGames/service/DataService.java**
- **src/main/java/com/game/fuzzyGames/service/SolverService.java**
- **src/main/java/com/game/fuzzyGames/FuzzyGamesApplication.java**

### Resources

- **src/main/resources/data/gameData.json**
- **src/main/resources/data/gameResult.txt**
- **src/main/resources/static/favicon.ico**
- **src/main/resources/templates/index.html**
- **src/main/resources/application.properties**

### Additional Files

- **docker-compose.yaml**
- **Dockerfile**
- **mvnw, mvnw.cmd**
- **pom.xml**
- **README.md**

### Installation

1. Clone the repository to your local machine.
2. Ensure you have Docker and Maven installed.
3. Navigate to the project directory.
4. Run `mvn clean package` to build the project.
5. Run `docker-compose up` to start the application.
6. Access the application at `http://localhost:8080` in your web browser.

### Usage

To use this project effectively, follow these steps:

1. Modify the game data in the `gameData.json` file located in `src/main/resources/data/`.
2. Update the matrix values, M degrees, and N degrees according to your game scenario.
3. Ensure that the matrix dimensions (M x N) match the number of degrees specified.
4. Run the application using Docker and Maven as described in the installation steps.
5. Access the web interface at `http://localhost:8080` to view the game results.

The application will create all possible pairs of fuzzy strategies based on the provided data. It will then solve the fuzzy minimax problem for each pair, find the results, and combine them into a summary.