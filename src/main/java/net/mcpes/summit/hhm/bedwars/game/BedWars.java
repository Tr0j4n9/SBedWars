package net.mcpes.summit.hhm.bedwars.game;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSignPost;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.level.Location;
import cn.nukkit.scheduler.PluginTask;
import net.mcpes.summit.hhm.bedwars.SBedWars;
import net.mcpes.summit.hhm.bedwars.SBedWarsAPI;
import net.mcpes.summit.hhm.bedwars.data.RoomData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static net.mcpes.summit.hhm.bedwars.SBedWars.*;

/**
 * @author hhm
 * @date 2017/7/13
 * @since SBedWars
 */

public class BedWars {
    public HashMap<Integer, ArrayList<String>> teams;
    public HashMap<String, Integer> chestData;
    private int id;
    private int gameMode;
    private RoomData data;

    public BedWars(int id) {
        this.id = id;
        if (rooms.containsKey(id)) {
            this.data = rooms.get(id);
            this.init();
        }
    }

    private void init() {
        players.remove(this.id);
        HashMap<String, ArrayList<Player>> roomPlayers = new HashMap<>();
        roomPlayers.put("alive", new ArrayList<>());
        roomPlayers.put("all", new ArrayList<>());
        roomPlayers.put("spectator", new ArrayList<>());
        players.put(this.id, roomPlayers);
        games.put(this.id, this);
        this.gameMode = 1;
        this.teams = new HashMap<>();
        this.chestData = new HashMap<>();
        for (Integer id : data.getTeamData().keySet()) {
            this.teams.put(id, new ArrayList<>());
        }
    }

    public synchronized void onJoin(Player player) {
        if (this.getAllPlayers().contains(player)) return;
        if (this.getAlivePlayers().size() < data.getMax()) {
            gaming.put(player.getName(), this.id);
            this.addPlayer(player);
            this.resetSign();
            SBedWarsAPI.getInstance().broadcastMessage(this.getAllPlayers(), "§7<§b" + player.getName() + "§7> §6加入了房间! (" + this.getAllPlayers().size() + "/" + data.getMax() + ")");
            player.teleport(data.getWaitLocation());
            player.sendMessage(DEFAULT_TITLE + "§4已经保存背包物品");
            //TODO: save bag and config
            this.giveBag(player);
            player.dataPacket(craftingDataPacket);
            player.sendMessage(DEFAULT_TITLE + "§6已经更换了您的背包,可在游戏结束后输入/sw bag拿回物品(关服重启之前,如果在关服重启之后,神器,附魔等高级物品将消失掉原本效果)");
            player.setGamemode(0);
            player.getInventory().clearAll();
            player.sendMessage(DEFAULT_TITLE + "§7已经更换为生存模式!");
            if (this.getAllPlayers().size() == data.getMin()) {
                this.setGameMode(2);
                Server.getInstance().getScheduler().scheduleRepeatingTask(new WaitTask(this.data, this), 20);
            }
            player.sendMessage(DEFAULT_TITLE + "成功加入" + id + "号房间");
        } else {
            player.sendMessage(DEFAULT_TITLE + "房间人数达到最大值,无法加入");
        }
    }

    public void onQuit(Player player, boolean online) {
        this.delPlayer(player);
        gaming.remove(player.getName());
        SBedWarsAPI.getInstance().broadcastMessage(this.getAllPlayers(), "§7<§b" + player.getName() + "§7> §6退出了房间! (" + this.getAlivePlayers().size() + "/" + data.getMax() + ")");
        if (online) {
            Server.getInstance().sendRecipeList(player);
            player.setHealth(player.getMaxHealth());
            player.clearTitle();
            player.removeAllEffects();
            player.getInventory().clearAll();
            player.setSpawn(Server.getInstance().getDefaultLevel().getSafeSpawn());
            player.teleport(this.data.getStopLocation());
            player.sendMessage(DEFAULT_TITLE + "§7你已退出游戏房间");
            player.sendMessage(DEFAULT_TITLE + "§7已经将您传送到游戏结束区域");
            player.sendMessage(DEFAULT_TITLE + "§4已经为您自动恢复背包物品");
        }
    }

    void onStart() {
        this.distributionTeam();
        for (Integer id : this.teams.keySet()) {
            ArrayList<String> teamPlayers = this.teams.get(id);
            for (String player : teamPlayers) {
                Player p = Server.getInstance().getPlayerExact(player);
                p.teleport((Location) this.data.getTeamData().get(id).get("gameLocation"));
                p.setHealth(20);
                p.removeAllEffects();
            }
        }
        for (HashMap<String, Object> data : this.data.getTeamData().values()) {
            Location pos = (Location) data.get("shopLocation");
            pos.level.setBlock(pos, Block.get(58));
        }
        SBedWarsAPI.getInstance().broadcastMessage(this.getAllPlayers(), "§l§7-------------------");
        SBedWarsAPI.getInstance().broadcastMessage(this.getAllPlayers(), DEFAULT_TITLE + "§2Game Starts!");
        SBedWarsAPI.getInstance().broadcastMessage(this.getAllPlayers(), "§l§7-------------------");
        this.setGameMode(3);
        this.resetSign();
        Server.getInstance().getScheduler().scheduleRepeatingTask(new GameTask(this.data, this), 20);
    }

    void onStop() {
        SBedWars.getInstance().getServer().getLogger().info(DEFAULT_TITLE + this.id + "号房间已经停止游戏");
        this.setGameMode(4);
        this.resetSign();
        this.resetRoom();
    }

    void onWin(int id) {
        for (String name : this.teams.get(id)) {
            Player player = Server.getInstance().getPlayerExact(name);
            player.sendMessage(DEFAULT_TITLE + "§5***************");
            player.sendMessage(DEFAULT_TITLE + "§6恭喜你,获得了最后的胜利");
            //player.sendMessage(DEFAULT_TITLE + "§6丰厚的奖励已经赠送");
            player.sendMessage(DEFAULT_TITLE + "§5***************");
            player.setSpawn(Server.getInstance().getDefaultLevel().getSafeSpawn());
            player.teleport(this.data.getStopLocation());
        }
        this.onStop();
    }

    void onDraw() {
        SBedWarsAPI.getInstance().broadcastMessage(this.getAllPlayers(), DEFAULT_TITLE + "平局!");
        this.onStop();
    }

    private void resetRoom() {
        this.setGameMode(5);
        this.resetSign();
        Server.getInstance().getLogger().info(DEFAULT_TITLE + "§5正在重载" + this.id + "号房间");
        try {
            SBedWarsAPI.getInstance().loadWorld(this.data.getGameWorld());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.init();
        this.resetSign();
        Server.getInstance().getLogger().info(DEFAULT_TITLE + "§6重载" + this.id + "号房间成功！");
    }

    private void giveBag(Player player) {
        //TODO: items in bag
    }

    private void resetSign() {
        for (Location location : data.getSignLocation()) {
            if (!(location.getLevel().getBlock(location) instanceof BlockSignPost)) return;
            BlockEntity entity = location.getLevel().getBlockEntity(location);
            if (!(entity instanceof BlockEntitySign)) continue;
            BlockEntitySign sign = (BlockEntitySign) entity;
            String status;
            switch (this.getGameMode()) {
                default:
                    status = "§c未知错误";
                    break;
                case 1:
                    status = GAMESTATUS_WAIT;
                    break;
                case 2:
                    status = GAMESTATUS_SOON;
                    break;
                case 3:
                    status = GAMESTATUS_START;
                    break;
                case 4:
                    status = GAMESTATUS_STOP;
                    break;
                case 5:
                    status = GAMESTATUS_RELOAD;
                    break;
            }
            sign.setText(DEFAULT_TITLE, status, "§l§b房间: §e" + this.id, "§l§c<§f---§b" + this.getAllPlayers().size() + "§7/" + data.getMax() + "§f---§c>");
        }
    }

    public ArrayList<Player> getAllPlayers() {
        return players.get(this.id).get("all");
    }

    public ArrayList<Player> getAlivePlayers() {
        return players.get(this.id).get("alive");
    }

    public ArrayList<Player> getSpectatorPlayers() {
        return players.get(this.id).get("spectator");
    }

    public void addPlayer(Player player) {
        this.getAlivePlayers().add(player);
        this.getAllPlayers().add(player);
    }

    public void delPlayer(Player player) {
        this.getAlivePlayers().remove(player);
        this.getAllPlayers().remove(player);
        this.getSpectatorPlayers().remove(player);
    }

    public int getGameMode() {
        return gameMode;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public ArrayList<String> getTeam(int id) {
        return this.teams.get(id);
    }

    public boolean choiceTeam(int id, String player) {
        boolean flag = true;
        for (ArrayList<String> a : this.teams.values()) {
            if (a.size() + 1 < this.getTeam(id).size()) {
                flag = false;
                break;
            }
        }
        if (flag) {
            this.getTeam(id).add(player);
            Server.getInstance().getPlayerExact(player).setSpawn((Location) this.data.getTeamData().get(id).get("gameLocation"));
        }
        return flag;
    }

    public int getTeam(String player) {
        for (Integer id : this.teams.keySet()) {
            ArrayList<String> players = this.teams.get(id);
            if (players.contains(player)) return id;
        }
        return -1;
    }

    public ArrayList<String> getNotHasTeamPlayers() {
        ArrayList<String> players = new ArrayList<>();
        for (Player player : this.getAlivePlayers()) {
            players.add(player.getName());
        }
        for (ArrayList<String> player : this.teams.values()) {
            players.removeAll(player);
        }
        return players;
    }

    public void distributionTeam() {
        ArrayList<String> notHasTeamPlayers = this.getNotHasTeamPlayers();
        for (String player : notHasTeamPlayers) {
            int id = this.getMinId(this.getTeamSize());
            this.choiceTeam(id, player);
            Server.getInstance().getPlayerExact(player).sendMessage(DEFAULT_TITLE + "你没有选择队伍,系统已自动帮你选择队伍为" + this.data.getTeamData().get(id).get("name"));
        }
    }

    public HashMap<Integer, Integer> getTeamSize() {
        HashMap<Integer, Integer> mdz = new HashMap<>();
        for (Integer id : this.teams.keySet()) {
            mdz.put(id, this.getTeam(id).size());
        }
        return mdz;
    }

    public int getMinId(HashMap<Integer, Integer> map) {
        if (map == null) return 0;
        Collection<Integer> c = map.values();
        Object[] obj = c.toArray();
        Arrays.sort(obj);
        int min = Integer.valueOf(obj[0].toString());
        for (Integer id : map.keySet()) {
            if (map.get(id) == min) return id;
        }
        return 0;
    }
}

class WaitTask extends PluginTask<SBedWars> {
    private int waitTick;
    private RoomData data;
    private BedWars game;

    WaitTask(RoomData data, BedWars game) {
        super(SBedWars.getInstance());
        this.data = data;
        this.game = game;
        this.waitTick = data.getWaitTime();
    }

    @Override
    public void onRun(int i) {
        this.waitTick--;
        if (game.getAlivePlayers().size() < this.data.getMin()) {
            SBedWarsAPI.getInstance().broadcastMessage(this.game.getAllPlayers(), "§c游戏人数不足，暂停开始游戏");
            this.cancel();
        }
        if (this.waitTick > 5 && game.getAlivePlayers().size() == this.data.getMax()) {
            this.waitTick = 5;
            SBedWarsAPI.getInstance().broadcastMessage(this.game.getAllPlayers(), "达到最大游戏人数,五秒后开始游戏");
        }
        switch (this.waitTick) {
            default:
                SBedWarsAPI.getInstance().broadcastTip(this.game.getAllPlayers(), DEFAULT_TITLE + "§6距离游戏开始还有" + waitTick + "秒");
                break;
            case 10:
                SBedWarsAPI.getInstance().broadcastTitle(this.game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§210");
                SBedWarsAPI.getInstance().broadcastSound(this.game.getAllPlayers(), 1);
                break;
            case 9:
                SBedWarsAPI.getInstance().broadcastTitle(this.game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§39");
                SBedWarsAPI.getInstance().broadcastSound(this.game.getAllPlayers(), 1);
                break;
            case 8:
                SBedWarsAPI.getInstance().broadcastTitle(this.game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§48");
                SBedWarsAPI.getInstance().broadcastSound(this.game.getAllPlayers(), 1);
                break;
            case 7:
                SBedWarsAPI.getInstance().broadcastTitle(this.game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§57");
                SBedWarsAPI.getInstance().broadcastSound(this.game.getAllPlayers(), 1);
                break;
            case 6:
                SBedWarsAPI.getInstance().broadcastTitle(this.game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§66");
                SBedWarsAPI.getInstance().broadcastSound(this.game.getAllPlayers(), 1);
                break;
            case 5:
                SBedWarsAPI.getInstance().broadcastTitle(this.game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§75");
                SBedWarsAPI.getInstance().broadcastSound(this.game.getAllPlayers(), 1);
                break;
            case 4:
                SBedWarsAPI.getInstance().broadcastTitle(this.game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§a4");
                SBedWarsAPI.getInstance().broadcastSound(this.game.getAllPlayers(), 1);
                break;
            case 3:
                SBedWarsAPI.getInstance().broadcastTitle(this.game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§b3");
                SBedWarsAPI.getInstance().broadcastSound(this.game.getAllPlayers(), 1);
                break;
            case 2:
                SBedWarsAPI.getInstance().broadcastTitle(this.game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§c2");
                SBedWarsAPI.getInstance().broadcastSound(this.game.getAllPlayers(), 1);
                break;
            case 1:
                SBedWarsAPI.getInstance().broadcastTitle(this.game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§d1");
                SBedWarsAPI.getInstance().broadcastSound(this.game.getAllPlayers(), 1);
                break;
            case 0:
                SBedWarsAPI.getInstance().broadcastTitle(this.game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§eGO!");
                SBedWarsAPI.getInstance().broadcastSound(this.game.getAllPlayers(), 2);
                break;
        }
        if (this.waitTick <= 0) {
            this.game.onStart();
            this.cancel();
            return;
        }
    }
}

class GameTask extends PluginTask<SBedWars> {
    private int gameTick;
    private RoomData data;
    private BedWars game;
    private int tick;

    GameTask(RoomData data, BedWars game) {
        super(SBedWars.getInstance());
        this.data = data;
        this.game = game;
        this.gameTick = data.getGameTime();
    }

    @Override
    public void onRun(int currentTick) {
        this.tick++;
        this.gameTick--;
        if (game.getAlivePlayers().size() < data.getMin()) {
            game.onStop();
            this.cancel();
            return;
        }
        this.checkTeam();
        //TODO:观战
        if (this.tick % data.getGoldDropSpeed() == 0) {
            for (Location location : data.getGoldLocation()) {
                location.level.dropItem(location, gold);
            }
        }
        if (this.tick % data.getSilverDropSpeed() == 0) {
            for (Location location : data.getSilverLocation()) {
                location.level.dropItem(location, silver);
            }
        }
        if (this.tick % data.getCopperDropSpeed() == 0) {
            for (Location location : data.getCopperLocation()) {
                location.level.dropItem(location, copper);
            }
        }
        switch (this.gameTick) {
            case 10:
            case 9:
            case 8:
            case 7:
            case 6:
            case 5:
            case 4:
                SBedWarsAPI.getInstance().broadcastMessage(game.getAllPlayers(), DEFAULT_TITLE + "距离游戏结束还有" + this.gameTick + "秒");
                SBedWarsAPI.getInstance().broadcastSound(game.getAllPlayers(), 1);
                break;
            case 3:
                SBedWarsAPI.getInstance().broadcastMessage(game.getAllPlayers(), DEFAULT_TITLE + "距离游戏结束还有" + this.gameTick + "秒");
                SBedWarsAPI.getInstance().broadcastTitle(game.getAllPlayers(), 0, 20, 0, "§l§b3", null);
                SBedWarsAPI.getInstance().broadcastSound(game.getAllPlayers(), 1);
                break;
            case 2:
                SBedWarsAPI.getInstance().broadcastMessage(game.getAllPlayers(), DEFAULT_TITLE + "距离游戏结束还有" + this.gameTick + "秒");
                SBedWarsAPI.getInstance().broadcastTitle(game.getAllPlayers(), 0, 20, 0, "§l§e2", null);
                SBedWarsAPI.getInstance().broadcastSound(game.getAllPlayers(), 1);
                break;
            case 1:
                SBedWarsAPI.getInstance().broadcastMessage(game.getAllPlayers(), DEFAULT_TITLE + "距离游戏结束还有" + this.gameTick + "秒");
                SBedWarsAPI.getInstance().broadcastTitle(game.getAllPlayers(), 0, 20, 0, "§l§d1", null);
                SBedWarsAPI.getInstance().broadcastSound(game.getAllPlayers(), 1);
                break;
            case 0:
                SBedWarsAPI.getInstance().broadcastTitle(game.getAllPlayers(), 0, 20, 0, DEFAULT_TITLE, "§l§c游戏结束!");
                SBedWarsAPI.getInstance().broadcastMessage(game.getAllPlayers(), "§l§7-------------------");
                SBedWarsAPI.getInstance().broadcastMessage(game.getAllPlayers(), DEFAULT_TITLE + "§l§cGame Stops!");
                SBedWarsAPI.getInstance().broadcastMessage(game.getAllPlayers(), "§l§7-------------------");
                SBedWarsAPI.getInstance().broadcastSound(game.getAllPlayers(), 2);
                break;
        }
        if (this.gameTick <= 0) {
            game.onDraw();
            this.cancel();
        }
    }

    private void checkTeam() {
        HashMap<Integer, Integer> map = game.getTeamSize();
        Collection<Integer> c = map.values();
        Object[] obj = c.toArray();
        Arrays.sort(obj);
        int max = Integer.valueOf(obj[obj.length - 1].toString());
        int count = 0;
        for (Object o : obj) {
            int size = Integer.valueOf(o.toString());
            if (size == max) count++;
        }
        if (count == 1) {
            for (Integer id : map.keySet()) {
                if (map.get(id) == max) game.onWin(id);
            }
            this.cancel();
        }
    }
}
