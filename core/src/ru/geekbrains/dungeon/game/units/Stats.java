package ru.geekbrains.dungeon.game.units;

import com.badlogic.gdx.math.MathUtils;
import lombok.Getter;

@Getter
public class Stats {
    int level;
    int hp, maxHp;
    int satiety, maxSatiety;
    int attackPoints, minAttackPoints, maxAttackPoints;
    int movePoints, minMovePoints, maxMovePoints;
    int visionRadius;


    public Stats(int level, int maxHp, int maxSatiety, int minAttackPoints, int maxAttackPoint, int minMovePoints, int maxMovePoint) {
        this.level = level;
        this.maxHp = maxHp;
        this.hp = this.maxHp;
        this.maxSatiety = maxSatiety;
        this.satiety =(int) (0.2f * this.maxSatiety);
        this.minAttackPoints = minAttackPoints;
        this.maxAttackPoints = maxAttackPoint;
        this.minMovePoints = minMovePoints;
        this.maxMovePoints = maxMovePoint;
        this.visionRadius = 5;

    }

    // 3. При выполнении действий сытость персонажа падает, если сытости падать некуда,
    // то начинает падать здоровье +
    public void decSatiety(int decSatiety) {
        satiety -= Math.abs(decSatiety);
        if (satiety < 0) {
            hp += satiety;
            satiety = 0;
        }
    }

    public void incSatiety(int incSatiety) {
        satiety += Math.abs(incSatiety);
        if (satiety > maxSatiety) satiety = maxSatiety;
    }

    public void restorePoints() {
        attackPoints = MathUtils.random(minAttackPoints, maxAttackPoints);
        movePoints = MathUtils.random(minMovePoints, maxMovePoints);
    }

    public void restoreHp(int amount) {
        hp += amount;
        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    public void fullRestoreHp() {
        hp = maxHp;
    }

    public void resetPoints() {
        attackPoints = 0;
        movePoints = 0;
    }

    public boolean doIHaveAnyPoints() {
        return attackPoints > 0 || movePoints > 0;
    }
}
