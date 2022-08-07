package com.nickbenn.maze.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class Maze {

  private final int width;
  private final int height;
  private final Random rng;
  private final Cell[][] cells;

  public Maze(int width, int height, Random rng) {
    // TODO Validate width and height.
    this.width = width;
    this.height = height;
    this.rng = rng;
    cells = setupCells(width, height);
    cells[0][0].extend();
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public Cell[][] getCells() {
    return Arrays
        .stream(cells)
        .map((row) -> Arrays.copyOf(row, row.length))
        .toArray(Cell[][]::new);
  }

  private Cell[][] setupCells(int width, int height) {
    final Cell[][] cells;
    cells = new Cell[height][width];
    for (int rowIndex = 0; rowIndex < height; rowIndex++) {
      for (int columnIndex = 0; columnIndex < cells[rowIndex].length; columnIndex++) {
        cells[rowIndex][columnIndex] = new Cell(rowIndex, columnIndex);
      }
    }
    return cells;
  }

  private void buildMaze() {
    cells[0][0].extend();
  }

  public final class Cell {

    private final int row;
    private final int column;
    private final Set<Direction> walls;

    private transient boolean visited;

    private Cell(int row, int column) {
      this.row = row;
      this.column = column;
      walls = EnumSet.allOf(Direction.class);
      visited = false;
    }

    public Set<Direction> getWalls() {
      return Collections.unmodifiableSet(walls);
    }

    private void extend() {
      visited = true;
      List<Map.Entry<Direction, Cell>> neighbors = new ArrayList<>(buildNeighborhood().entrySet());
      Collections.shuffle(neighbors, rng);
      neighbors.forEach((entry) -> {
        Direction direction = entry.getKey();
        Cell neighbor = entry.getValue();
        if (!neighbor.visited) {
          removeWall(direction);
          neighbor.removeWall(direction.getOpposite());
          neighbor.extend();
        }
      });
    }

    private Map<Direction, Cell> buildNeighborhood() {
      Map<Direction, Cell> neighborhood = new EnumMap<>(Direction.class);
      for (Direction dir : Direction.values()) {
        int neighborRow = row + dir.getRowOffset();
        int neighborColumn = column + dir.getColumnOffset();
        if (neighborRow >=0 && neighborRow < cells.length
            && neighborColumn >= 0 && neighborColumn < cells[neighborRow].length) {
          neighborhood.put(dir, cells[neighborRow][neighborColumn]);
        }
      }
      return neighborhood;
    }

    private void removeWall(Direction direction) {
      walls.remove(direction);
    }

  }

  public enum Direction {

    NORTH(-1, 0),
    EAST(0, 1),
    SOUTH(1, 0),
    WEST(0, -1);

    private final int rowOffset;
    private final int columnOffset;

    Direction(int rowOffset, int columnOffset) {
      this.rowOffset = rowOffset;
      this.columnOffset = columnOffset;
    }

    int getRowOffset() {
      return rowOffset;
    }

    int getColumnOffset() {
      return columnOffset;
    }

    Direction getOpposite() {
      Direction[] values = values();
      return values[(ordinal() + 2) % values.length];
    }

  }

}
