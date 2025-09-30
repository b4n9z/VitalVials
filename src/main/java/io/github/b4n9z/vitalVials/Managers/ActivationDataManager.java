package io.github.b4n9z.vitalVials.Managers;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class ActivationDataManager {
    private final String id;
    private final List<String> rightClick;
    private final List<String> leftClick;
    private final List<String> youAreHittingEnemy;
    private final List<String> enemyIsHittingYou;

    public ActivationDataManager(String id, ConfigurationSection section) {
        this.id = id;
        this.rightClick = section.getStringList("rightClick");
        this.leftClick = section.getStringList("leftClick");
        this.youAreHittingEnemy = section.getStringList("YouAreHittingEnemy");
        this.enemyIsHittingYou = section.getStringList("enemyHitYou");
    }

    public String getId() {
        return id;
    }

    public List<String> getRightClick() {
        return rightClick;
    }

    public List<String> getLeftClick() {
        return leftClick;
    }

    public List<String> getYouAreHittingEnemy() {
        return youAreHittingEnemy;
    }

    public List<String> getEnemyIsHittingYou() {
        return enemyIsHittingYou;
    }
}
