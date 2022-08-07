package com.nickbenn.maze;

import com.nickbenn.maze.model.Maze;
import com.nickbenn.maze.view.TextMaze;
import java.security.SecureRandom;

public class Main {

  public static void main(String[] args) {
    Maze maze = new Maze(50, 50, new SecureRandom());
    TextMaze view = new TextMaze(maze);
    for (String line : view.getRepresentation(2, 1)) {
      System.out.println(line);
    }
  }

}
