package ru.geekbrains.dungeon.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.sun.jndi.ldap.Ber;
import ru.geekbrains.dungeon.game.units.Unit;
import ru.geekbrains.dungeon.helpers.Assets;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class GameMap {
    public enum CellType {
        GRASS, WATER, TREE
    }

    public enum DropType {
        NONE, GOLD
    }

    public enum BerryType {
        NONE, BERRY1, BERRY2, BERRY3
    }

    private class Berry {

        private BerryType type;
        private int nutValue;
        TextureRegion texture;

        public Berry() {
            switch (MathUtils.random(1, 3)) {
                case 1:
                    type = BerryType.BERRY1;
                    nutValue = 10;
                    texture = Assets.getInstance().getAtlas().findRegion("projectile");
                    break;
                case 2:
                    type = BerryType.BERRY2;
                    nutValue = 15;
                    texture = Assets.getInstance().getAtlas().findRegion("projectile");
                    break;
                case 3:
                    type = BerryType.BERRY3;
                    nutValue = 20;
                    texture = Assets.getInstance().getAtlas().findRegion("projectile");
                    break;
            }
        }

        public int getNutValue() {
            return nutValue;
        }

    }

    public Berry generateBerry(int probability) {
        if (MathUtils.random(0, 100) < probability) return new Berry();
        return null;
    }


    private class Cell {
        CellType type;
        Berry berry;


        DropType dropType;
        int dropPower;

        int index;

        public Cell() {
            type = CellType.GRASS;
            dropType = DropType.NONE;
            Berry berry = null;
            index = 0;
        }

        public void changeType(CellType to) {
            type = to;
            if (type == CellType.TREE) {
                index = MathUtils.random(4);
            }
        }
    }

    public static final int CELLS_X = 22;
    public static final int CELLS_Y = 12;
    public static final int CELL_SIZE = 60;
    public static final int FOREST_PERCENTAGE = 20;
    public static final int BERRIES_PERCENTAGE = 50;

    public int getCellsX() {
        return CELLS_X;
    }

    public int getCellsY() {
        return CELLS_Y;
    }


    private Cell[][] data;
    private TextureRegion grassTexture;
    private TextureRegion goldTexture;
    private TextureRegion[] treesTextures;

    public GameMap() {
        this.data = new Cell[CELLS_X][CELLS_Y];
        for (int i = 0; i < CELLS_X; i++) {
            for (int j = 0; j < CELLS_Y; j++) {
                this.data[i][j] = new Cell();
            }
        }
        int treesCount = (int) ((CELLS_X * CELLS_Y * FOREST_PERCENTAGE) / 100.0f);
        for (int i = 0; i < treesCount; i++) {
            int treeCellX = MathUtils.random(0, CELLS_X - 1);
            int treeCellY = MathUtils.random(0, CELLS_Y - 1);
            this.data[treeCellX][treeCellY].changeType(CellType.TREE);
            this.data[treeCellX][treeCellY].berry = generateBerry(BERRIES_PERCENTAGE);
        }
        this.grassTexture = Assets.getInstance().getAtlas().findRegion("grass");
        this.goldTexture = Assets.getInstance().getAtlas().findRegion("chest").split(60, 60)[0][0];
        this.treesTextures = Assets.getInstance().getAtlas().findRegion("trees").split(60, 90)[0];
    }

    public boolean isCellPassable(int cx, int cy) {
        if (cx < 0 || cx > getCellsX() - 1 || cy < 0 || cy > getCellsY() - 1) {
            return false;
        }
        if (data[cx][cy].type != CellType.GRASS) {
            return false;
        }
        return true;
    }

    public void renderGround(SpriteBatch batch) {
        for (int i = 0; i < CELLS_X; i++) {
            for (int j = CELLS_Y - 1; j >= 0; j--) {
                batch.draw(grassTexture, i * CELL_SIZE, j * CELL_SIZE);
            }
        }
    }

    public void renderObjects(SpriteBatch batch) {
        for (int i = 0; i < CELLS_X; i++) {
            for (int j = CELLS_Y - 1; j >= 0; j--) {
                if (data[i][j].type == CellType.TREE) {
                    batch.draw(treesTextures[data[i][j].index], i * CELL_SIZE, j * CELL_SIZE);
                }
                if (data[i][j].berry != null) {
                    batch.draw(data[i][j].berry.texture, (i + 0.5f) * CELL_SIZE, (j + 0.6f) * CELL_SIZE);
                }
                if (data[i][j].dropType == DropType.GOLD) {
                    batch.draw(goldTexture, i * CELL_SIZE, j * CELL_SIZE);
                }
            }
        }
    }

    // todo: перенести в калькулятор
    public void generateDrop(int cellX, int cellY, int power) {
        if (MathUtils.random() < 0.5f) {
            DropType randomDropType = DropType.GOLD;

            if (randomDropType == DropType.GOLD) {
                int goldAmount = power + MathUtils.random(power, power * 3);
                data[cellX][cellY].dropType = randomDropType;
                data[cellX][cellY].dropPower = goldAmount;
            }
        }
    }

    public boolean hasDropInCell(int cellX, int cellY) {
        return data[cellX][cellY].dropType != DropType.NONE;
    }

    public void checkAndTakeDrop(Unit unit) {
        Cell currentCell = data[unit.getCellX()][unit.getCellY()];
        if (currentCell.dropType == DropType.NONE) {
            return;
        }
        if (currentCell.dropType == DropType.GOLD) {
            unit.addGold(currentCell.dropPower);
        }
        currentCell.dropType = DropType.NONE;
        currentCell.dropPower = 0;
    }

   /*

   // ВОПРОС: Хотел получить в методе список всех ячеек с деревьями и потом отфильтровать через stream().filter,
    // но получил ошибку (use -source 8 or higher to enable lambda expressions. Project Language level у меня стоит 8.
    // Что нужно сделать, чтобы использовать лямбда-выражения?


   public List<Cell> getCellsByType(CellType cellType) {
        List<Cell> cellList = new LinkedList<>();
        for (int i = 0; i < CELLS_X; i++) {
            for (int j = CELLS_Y - 1; j >= 0; j--) {
                if (data[i][j].type == cellType) {
                    cellList.add(data[i][j]);
                }
            }
        }
        return cellList;
    }

    public void putBerryInRandomCell() {
        List<Cell> treeCellList = getCellsByType(CellType.TREE).stream().filter(p -> p.berryType == BerryType.NONE).collect(Collectors.toList());
        if (treeCellList.isEmpty()) return;
        int cellNumber = MathUtils.random(0, treeCellList.size() - 1);
        treeCellList.get(cellNumber).berryType = BerryType.BERRY1;
    }
    */

    public List<Cell> getTreeCellsWithoutBerries() {
        List<Cell> cellList = new LinkedList<>();
        for (int i = 0; i < CELLS_X; i++) {
            for (int j = CELLS_Y - 1; j >= 0; j--) {
                if (data[i][j].type == CellType.TREE && data[i][j].berry == null) {
                    cellList.add(data[i][j]);
                }
            }
        }
        return cellList;
    }

    public void putBerryInRandomCell() {
        List<Cell> treeCellList = getTreeCellsWithoutBerries();
        if (treeCellList.isEmpty()) return;
        int cellNumber = MathUtils.random(0, treeCellList.size() - 1);
        treeCellList.get(cellNumber).berry = new Berry();
    }

    public int getBerries(int unitCellX, int unitCellY, int cursorCellX, int cursorCellY) {
        int countBerries = 0;

        if (data[cursorCellX][cursorCellY].berry == null) return 0;

        if (cursorCellX - unitCellX == 0 && Math.abs(cursorCellY - unitCellY) <= 1 ||
                cursorCellY - unitCellY == 0 && Math.abs(cursorCellX - unitCellX) <= 1) {
            countBerries = data[cursorCellX][cursorCellY].berry.getNutValue();
            data[cursorCellX][cursorCellY].berry = null;
        }
        return countBerries;
    }

}
