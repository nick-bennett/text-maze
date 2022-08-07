package com.nickbenn.maze.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Maze {

  private static final String CELL_STRING_FORMAT = "%1$s{row=%2$d, column=%3$d, walls=%4$s}";

  private final int width;
  private final int height;
  private final Random rng;
  private final Cell[][] cells;
  private final Set<Cell> termini;

  public Maze(int width, int height, Random rng) {
    this.width = width;
    this.height = height;
    this.rng = rng;
    cells = setupCells(width, height);
    buildMaze();
    termini = findTermini();
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

  public Set<Cell> getTermini() {
    return termini;
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
    cells[rng.nextInt(cells.length)][rng.nextInt(cells[0].length)].extend();
  }

  private Set<Cell> findTermini() {
    Cell terminus1 = floodFill(cells[0][0]);
    terminus1.setTerminus(true);
    Cell terminus2 = floodFill(terminus1);
    terminus2.setTerminus(true);
    return Set.of(terminus1, terminus2);
  }

  private Cell floodFill(Cell origin) {
    Arrays
        .stream(cells)
        .forEach((row) -> Arrays
            .stream(row)
            .forEach((cell) -> cell.setFloodDistance(Integer.MAX_VALUE))
        );
    Set<Cell> queue = new LinkedHashSet<>();
    origin.setFloodDistance(0);
    queue.add(origin);
    Cell lastFlooded = null;
    while (!queue.isEmpty()) {
      lastFlooded = processFloodedCell(queue);
    }
    return lastFlooded;
  }

  private static Cell processFloodedCell(Set<Cell> queue) {
    Iterator<Cell> iterator = queue.iterator();
    Cell lastFlooded = iterator.next();
    iterator.remove();
    int nextDistance = lastFlooded.getFloodDistance() + 1;
    lastFlooded
        .neighbors((dir) -> !lastFlooded.getWalls().contains(dir))
        .values()
        .stream()
        .filter((cell) -> cell.getFloodDistance() > nextDistance)
        .peek((cell) -> cell.setFloodDistance(nextDistance))
        .forEach(queue::add);
    return lastFlooded;
  }

  public final class Cell {

    private final int row;
    private final int column;
    private final Set<Direction> walls;
    private final int hash;

    private boolean terminus;

    private transient boolean visited;
    private transient int floodDistance;

    private Cell(int row, int column) {
      this.row = row;
      this.column = column;
      walls = EnumSet.allOf(Direction.class);
      visited = false;
      hash = Objects.hash(row, column);
    }

    @Override
    public int hashCode() {
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      boolean isEqual;
      if (this == obj) {
        isEqual = true;
      } else if (obj instanceof Cell) {
        Cell other = (Cell) obj;
        isEqual = (row == other.row && column == other.column);
      } else {
        isEqual = false;
      }
      return isEqual;
    }

    @Override
    public String toString() {
      return String.format(CELL_STRING_FORMAT,
          getClass().getSimpleName(), row, column, walls);
    }

    public Set<Direction> getWalls() {
      return Collections.unmodifiableSet(walls);
    }

    public boolean isTerminus() {
      return terminus;
    }

    private void setTerminus(boolean terminus) {
      this.terminus = terminus;
    }

    private int getFloodDistance() {
      return floodDistance;
    }

    private void setFloodDistance(int floodDistance) {
      this.floodDistance = floodDistance;
    }

    private void extend() {
      visited = true;
      List<Map.Entry<Direction, Cell>> neighbors =
          new ArrayList<>(neighbors((dir) -> true).entrySet());
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

    private Map<Direction, Cell> neighbors(Predicate<Direction> filter) {
      return Arrays
          .stream(Direction.values())
          .filter(filter)
          .map((dir) -> {
            int neighborRow = row + dir.getRowOffset();
            int neighborColumn = column + dir.getColumnOffset();
            Cell neighbor = (neighborRow >= 0 && neighborRow < cells.length
                && neighborColumn >= 0 && neighborColumn < cells[neighborRow].length)
                ? cells[neighborRow][neighborColumn]
                : null;
            return (neighbor != null) ? Map.entry(dir, neighbor) : null;
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
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
