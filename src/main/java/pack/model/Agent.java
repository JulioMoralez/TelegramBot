package pack.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Entity
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String telegram;
    private Integer active;
    private String nick;
    private Integer ap;
    private Integer level;
    private Integer trekker;
    private Integer builder;
    private Integer purifier;
    private Integer recharger;
    private Integer fraction;

    public Agent() {
        telegram="";
        nick="";

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTelegram() {
        return telegram;
    }

    public void setTelegram(String telegram) {
        this.telegram = telegram;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public Integer getAp() {
        return ap;
    }

    public void setAp(Integer ap) {
        this.ap = ap;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getTrekker() {
        return trekker;
    }

    public void setTrekker(Integer trekker) {
        this.trekker = trekker;
    }

    public Integer getBuilder() {
        return builder;
    }

    public void setBuilder(Integer builder) {
        this.builder = builder;
    }

    public Integer getPurifier() {
        return purifier;
    }

    public void setPurifier(Integer purifier) {
        this.purifier = purifier;
    }

    public Integer getRecharger() {
        return recharger;
    }

    public void setRecharger(Integer recharger) {
        this.recharger = recharger;
    }

    public Integer getFraction() {
        return fraction;
    }

    public void setFraction(Integer fraction) {
        this.fraction = fraction;
    }


    @Override
    public String toString() {
        return "Agent{" +
                "id=" + id +
                ", telegram='" + telegram + '\'' +
                ", active=" + active +
                ", nick='" + nick + '\'' +
                ", ap=" + ap +
                ", level=" + level +
                ", trekker=" + trekker +
                ", builder=" + builder +
                ", purifier=" + purifier +
                ", recharger=" + recharger +
                ", fraction=" + fraction +
                '}';
    }
}
