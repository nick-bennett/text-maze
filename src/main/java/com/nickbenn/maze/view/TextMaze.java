package com.nickbenn.maze.view;

import com.nickbenn.maze.model.Maze;
import com.nickbenn.maze.model.Maze.Cell;
import com.nickbenn.maze.model.Maze.Direction;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextMaze {

  private static final Set<Direction> NO_WALLS = Collections.emptySet();
  private static final Set<Direction> NORTH_WALL = EnumSet.of(Direction.NORTH);
  private static final Set<Direction> EAST_WALL = EnumSet.of(Direction.EAST);
  private static final Set<Direction> SOUTH_WALL = EnumSet.of(Direction.SOUTH);
  private static final Set<Direction> WEST_WALL = EnumSet.of(Direction.WEST);
  private static final char NORTH_WALL_CHAR = '\u2501';
  private static final char WEST_WALL_CHAR = '\u2503';
  private static final char NO_WALL_CHAR = ' ';
  private static final Map<Set<Direction>, Character> CORNER_CHARACTERS = Map.ofEntries(
      Map.entry(EnumSet.noneOf(Direction.class), NO_WALL_CHAR),
      Map.entry(EnumSet.of(Direction.NORTH), '\u2578'),
      Map.entry(EnumSet.of(Direction.EAST), '\u2579'),
      Map.entry(EnumSet.of(Direction.SOUTH), '\u257A'),
      Map.entry(EnumSet.of(Direction.WEST), '\u257B'),
      Map.entry(EnumSet.of(Direction.NORTH, Direction.EAST), '\u251B'),
      Map.entry(EnumSet.of(Direction.NORTH, Direction.SOUTH), '\u2501'),
      Map.entry(EnumSet.of(Direction.NORTH, Direction.WEST), '\u2513'),
      Map.entry(EnumSet.of(Direction.EAST, Direction.SOUTH), '\u2517'),
      Map.entry(EnumSet.of(Direction.EAST, Direction.WEST), '\u2503'),
      Map.entry(EnumSet.of(Direction.SOUTH, Direction.WEST), '\u250F'),
      Map.entry(EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH), '\u253B'),
      Map.entry(EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST), '\u252B'),
      Map.entry(EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.WEST), '\u2533'),
      Map.entry(EnumSet.of(Direction.EAST, Direction.SOUTH, Direction.WEST), '\u2523'),
      Map.entry(EnumSet.allOf(Direction.class), '\u254B')
  );

  private final Cell[][] cells;
  private final CellCharacters[][] wallChars;

  private int cellCharWidth;
  private int cellCharHeight;
  private String[] representation;

  public TextMaze(Maze maze) {
    cells = maze.getCells();
    wallChars = buildCellCharacters(maze);
  }

  public String[] getRepresentation(int cellCharWidth, int cellCharHeight) {
    if (cellCharWidth != this.cellCharWidth
        || cellCharHeight != this.cellCharHeight
        || representation == null) {
      this.cellCharWidth = cellCharWidth;
      this.cellCharHeight = cellCharHeight;
      representation = buildRepresentation();
    }
    return representation;
  }

  private CellCharacters[][] buildCellCharacters(Maze maze) {
    int mazeWidth = maze.getWidth();
    int mazeHeight = maze.getHeight();
    //noinspection unchecked
    Set<Direction>[][] wallSets = new Set[2][mazeWidth + 2];
    wallSets[0][0] = wallSets[0][wallSets[1].length - 1] = NO_WALLS;
    for (int columnIndex = 1; columnIndex <= mazeWidth; columnIndex++) {
      wallSets[0][columnIndex] = SOUTH_WALL;
    }
    CellCharacters[][] wallChars = new CellCharacters[mazeHeight + 1][];
    for (int rowIndex = 0; rowIndex < mazeHeight; rowIndex++) {
      wallChars[rowIndex] = buildRowCharacters(wallSets, rowIndex);
      wallSets[0] = Arrays.copyOf(wallSets[1], wallSets[1].length);
    }
    wallChars[mazeHeight] = buildRowCharacters(wallSets, mazeHeight);
    return wallChars;
  }

  private CellCharacters[] buildRowCharacters(Set<Direction>[][] wallSets, int rowIndex) {
    int mazeWidth = cells[0].length;
    int mazeHeight = cells.length;
    boolean bottomRow = rowIndex >= mazeHeight;
    CellCharacters[] rowChars = new CellCharacters[mazeWidth + 1];
    wallSets[1][0] = bottomRow ? NO_WALLS: EAST_WALL;
    for (int columnIndex = 0; columnIndex < mazeWidth; columnIndex++) {
      wallSets[1][columnIndex + 1] =
          bottomRow ? NORTH_WALL : cells[rowIndex][columnIndex].getWalls();
      rowChars[columnIndex] = computeCharacters(wallSets, columnIndex);
    }
    wallSets[1][wallSets[1].length - 1] = bottomRow ? NO_WALLS : WEST_WALL;
    rowChars[mazeWidth] = computeCharacters(wallSets, mazeWidth);
    return rowChars;
  }

  private static CellCharacters computeCharacters(Set<Direction>[][] wallSets, int columnIndex) {
    Set<Direction> current = wallSets[1][columnIndex + 1];
    Set<Direction> cornerWalls = Stream
        .of(
            wallSets[1][columnIndex].stream().filter((dir) -> dir == Direction.NORTH),
            wallSets[0][columnIndex].stream().filter((dir) -> dir == Direction.EAST),
            wallSets[0][columnIndex + 1].stream().filter((dir) -> dir == Direction.SOUTH),
            current.stream().filter((dir) -> dir == Direction.WEST)
        )
        .flatMap(Function.identity())
        .collect(Collectors.toSet());
    char topLeftChar = CORNER_CHARACTERS.get(cornerWalls);
    char topChar = current.contains(Direction.NORTH) ? NORTH_WALL_CHAR : NO_WALL_CHAR;
    char leftChar = current.contains(Direction.WEST) ? WEST_WALL_CHAR : NO_WALL_CHAR;
    return new CellCharacters(topLeftChar, topChar, leftChar);
  }

  private String[] buildRepresentation() {
    List<String> representation = new LinkedList<>();
    for (int rowIndex = 0; rowIndex < wallChars.length - 1; rowIndex++) {
      representation.add(singleLine(rowIndex, true));
      String line = singleLine(rowIndex, false);
      for (int verticalSpacerIndex = 0;
          verticalSpacerIndex < cellCharHeight - 1;
          verticalSpacerIndex++) {
        representation.add(line);
      }
    }
    representation.add(singleLine(wallChars.length - 1, true));
    return representation.toArray(new String[0]);
  }

  private String singleLine(int rowIndex, boolean onWall) {
    StringBuilder builder = new StringBuilder();
    char[] spacer = new char[cellCharWidth - 1];
    for (int columnIndex = 0; columnIndex < wallChars[rowIndex].length - 1; columnIndex++) {
      CellCharacters current = wallChars[rowIndex][columnIndex];
      builder.append(onWall ? current.topLeft : current.left);
      Arrays.fill(spacer, onWall ? current.top : NO_WALL_CHAR);
      builder.append(spacer);
    }
    CellCharacters last = wallChars[rowIndex][wallChars[rowIndex].length - 1];
    builder.append(onWall ? last.topLeft : last.left);
    return builder.toString();
  }

  private static final class CellCharacters {

    private final char topLeft;
    private final char top;
    private final char left;

    private CellCharacters(char topLeft, char top, char left) {
      this.topLeft = topLeft;
      this.top = top;
      this.left = left;
    }

  }

}
