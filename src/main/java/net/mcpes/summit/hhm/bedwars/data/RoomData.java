package net.mcpes.summit.hhm.bedwars.data;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;

import java.util.ArrayList;
import java.util.HashMap;

import static net.mcpes.summit.hhm.bedwars.SBedWars.DEFAULT_TITLE;

/**
 * @author hhm
 * @date 2017/7/13
 * @since SBedWars
 */

public class RoomData {
    private int id = 0;//房间id
    private int min = 4;//最小游戏人数
    private int max = 20;//最大游戏人数
    private String disName = DEFAULT_TITLE + id + "号房间";//房间显示名称
    private int waitTime = 30;//游戏等待时间
    private int gameTime = 600;//最大游戏时间,这个时间内到不了终点自动判负
    private String waitPos = "";//等待游戏地点-String
    private Location waitLocation = null;//等待游戏地点-Location
    private String stopPos = "";//结束游戏地点-String
    private Location stopLocation = null;//结束游戏地点-Location
    private ArrayList<String> signPos = new ArrayList<>();//牌子地点-String
    private ArrayList<Location> signLocation = new ArrayList<>();//牌子地点-Location
    private String gameWorld = "";//游戏世界名称
    private Level gameLevel = null;//游戏世界
    private HashMap<Integer, HashMap<String, Object>> teamData = new HashMap<>();//队伍信息，包括disName,bedPos,gamePos,shopPos
    private int voidKill = 0;//当玩家y值低于此将自动击杀
    private ArrayList<Location> goldLocation = new ArrayList<>();//金掉落地点-Location
    private ArrayList<Location> silverLocation = new ArrayList<>();//银掉落地点-Location
    private ArrayList<Location> emeraldLocation = new ArrayList<>();//绿宝石掉落地点-Location
    private ArrayList<Location> diamondLocation = new ArrayList<>();//钻石掉落地点-Location
    private ArrayList<String> goldPos = new ArrayList<>();//金掉落地点-Location
    private ArrayList<String> silverPos = new ArrayList<>();//银掉落地点-Location
    private ArrayList<String> emeraldPos = new ArrayList<>();//绿宝石掉落地点-LocationLocation
    private ArrayList<String> diamondPos = new ArrayList<>();//钻石掉落地点-Location
    private int set = 0;//设置步骤,设置房间用
    private int teamCount = 0;//队伍数量,设置房间用
    private int canTeam = 0;

    public RoomData(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public String getDisName() {
        return disName;
    }

    public void setDisName(String disName) {
        this.disName = disName;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public int getGameTime() {
        return gameTime;
    }

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }

    public String getWaitPos() {
        return waitPos;
    }

    public void setWaitPos(String waitPos) {
        this.waitPos = waitPos;
    }

    public Location getWaitLocation() {
        return waitLocation;
    }

    public void setWaitLocation() {
        String[] a = this.getWaitPos().split(":");
        this.waitLocation = new Location(Integer.valueOf(a[0]), Integer.valueOf(a[1]), Integer.valueOf(a[2]), Server.getInstance().getLevelByName(a[3]));
    }

    public String getStopPos() {
        return stopPos;
    }

    public void setStopPos(String stopPos) {
        this.stopPos = stopPos;
    }

    public Location getStopLocation() {
        return stopLocation;
    }

    public void setStopLocation() {
        String[] a = this.getStopPos().split(":");
        this.stopLocation = new Location(Integer.valueOf(a[0]), Integer.valueOf(a[1]), Integer.valueOf(a[2]), Server.getInstance().getLevelByName(a[3]));
    }

    public ArrayList<String> getSignPos() {
        return signPos;
    }

    public void setSignPos(ArrayList<String> signPos) {
        this.signPos = signPos;
    }

    public ArrayList<Location> getSignLocation() {
        return signLocation;
    }

    public void setSignLocation() {
        ArrayList<Location> sign = new ArrayList<>();
        for (String s : this.getSignPos()) {
            String[] a = s.split(":");
            sign.add(new Location(Integer.valueOf(a[0]), Integer.valueOf(a[1]), Integer.valueOf(a[2]), Server.getInstance().getLevelByName(a[3])));
        }
        this.signLocation = sign;
    }

    public String getGameWorld() {
        return gameWorld;
    }

    public void setGameWorld(String gameWorld) {
        this.gameWorld = gameWorld;
    }

    public Level getGameLevel() {
        return gameLevel;
    }

    public void setGameLevel() {
        this.gameLevel = Server.getInstance().getLevelByName(this.getGameWorld());
    }

    public HashMap<Integer, HashMap<String, Object>> getTeamData() {
        return teamData;
    }

    public void setTeamData(HashMap<Integer, HashMap<String, Object>> teamData, int type) {
        this.teamData = teamData;
        if (type == 2) return;
        for (Integer id : teamData.keySet()) {
            HashMap<String, Object> team = teamData.get(id);
            String[] a = team.get("gamePos").toString().split(":");
            team.put("gameLocation", (new Location(Integer.valueOf(a[0]), Integer.valueOf(a[1]), Integer.valueOf(a[2]), Server.getInstance().getLevelByName(a[3]))));
            a = team.get("shopPos").toString().split(":");
            team.put("shopLocation", (new Location(Integer.valueOf(a[0]), Integer.valueOf(a[1]), Integer.valueOf(a[2]), Server.getInstance().getLevelByName(a[3]))));
            a = team.get("bedPos").toString().split(":");
            team.put("bedLocation", (new Location(Integer.valueOf(a[0]), Integer.valueOf(a[1]), Integer.valueOf(a[2]), Server.getInstance().getLevelByName(a[3]))));
        }
    }

    public int getVoidKill() {
        return voidKill;
    }

    public void setVoidKill(int voidKill) {
        this.voidKill = voidKill;
    }

    public ArrayList<Location> getGoldLocation() {
        return goldLocation;
    }

    public void setGoldLocation() {
        ArrayList<Location> gold = new ArrayList<>();
        for (String s : this.getGoldPos()) {
            String[] a = s.split(":");
            gold.add(new Location(Integer.valueOf(a[0]), Integer.valueOf(a[1]), Integer.valueOf(a[2]), Server.getInstance().getLevelByName(a[3])));
        }
        this.goldLocation = gold;
    }

    public ArrayList<Location> getSilverLocation() {
        return silverLocation;
    }

    public void setSilverLocation() {
        ArrayList<Location> silver = new ArrayList<>();
        for (String s : this.getSilverPos()) {
            String[] a = s.split(":");
            silver.add(new Location(Integer.valueOf(a[0]), Integer.valueOf(a[1]), Integer.valueOf(a[2]), Server.getInstance().getLevelByName(a[3])));
        }
        this.silverLocation = silver;
    }

    public ArrayList<Location> getEmeraldLocation() {
        return emeraldLocation;
    }

    public ArrayList<Location> getDiamondLocation() {
        return diamondLocation;
    }

    public ArrayList<String> getEmeraldPos() {
        return emeraldPos;
    }

    public void setEmeraldPos(ArrayList<String> emeraldPos) {
        this.emeraldPos = emeraldPos;
    }

    public ArrayList<String> getDiamondPos() {
        return diamondPos;
    }

    public void setDiamondPos(ArrayList<String> diamondPos) {
        this.diamondPos = diamondPos;
    }

    public void setEmeraldLocation() {
        ArrayList<Location> emerald = new ArrayList<>();
        for (String s : this.getEmeraldPos()) {
            String[] a = s.split(":");
            emerald.add(new Location(Integer.valueOf(a[0]), Integer.valueOf(a[1]), Integer.valueOf(a[2]), Server.getInstance().getLevelByName(a[3])));
        }
        this.emeraldLocation = emerald;
    }

    public void setDiamondeLocation() {
        ArrayList<Location> diamond = new ArrayList<>();
        for (String s : this.getDiamondPos()) {
            String[] a = s.split(":");
            diamond.add(new Location(Integer.valueOf(a[0]), Integer.valueOf(a[1]), Integer.valueOf(a[2]), Server.getInstance().getLevelByName(a[3])));
        }
        this.diamondLocation = diamond;
    }

    public ArrayList<String> getGoldPos() {
        return goldPos;
    }

    public void setGoldPos(ArrayList<String> goldPos) {
        this.goldPos = goldPos;
    }

    public ArrayList<String> getSilverPos() {
        return silverPos;
    }

    public void setSilverPos(ArrayList<String> silverPos) {
        this.silverPos = silverPos;
    }

    public int getSet() {
        return set;
    }

    public void setSet(int set) {
        this.set = set;
    }

    public int getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(int teamCount) {
        this.teamCount = teamCount;
    }

    public int getCanTeam() {
        return canTeam;
    }

    public void setCanTeam(int canTeam) {
        this.canTeam = canTeam;
    }
}