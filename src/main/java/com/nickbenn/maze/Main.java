package com.nickbenn.maze;

import com.nickbenn.maze.model.Maze;
import com.nickbenn.maze.view.TextMaze;
import java.security.SecureRandom;

public class Main {

  public static void main(String[] args) {
    Maze maze = new Maze(20, 20, new SecureRandom());
    TextMaze view = new TextMaze(maze);
    for (String line : view.getRepresentation(5, 2)) {
      System.out.println(line);
    }
  }

}
