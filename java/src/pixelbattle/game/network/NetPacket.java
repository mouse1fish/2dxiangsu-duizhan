package pixelbattle.game.network;

import java.io.*;

public class NetPacket {
    public static final int MSG_PLAYER_POS = 1;
    public static final int MSG_PLAYER_ACTION = 2;
    public static final int MSG_BLOCK_CHANGE = 3;
    public static final int MSG_HEALTH = 4;
    public static final int MSG_CHAT = 5;
    public static final int MSG_WORLD_SEED = 6;
    public static final int MSG_PING = 7;

    public static byte[] playerPos(double x, double y, double vx, double vy, boolean facingRight) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(MSG_PLAYER_POS);
            dos.writeDouble(x);
            dos.writeDouble(y);
            dos.writeDouble(vx);
            dos.writeDouble(vy);
            dos.writeBoolean(facingRight);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) { return new byte[0]; }
    }

    public static byte[] playerAction(int action, double targetX, double targetY) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(MSG_PLAYER_ACTION);
            dos.writeInt(action);
            dos.writeDouble(targetX);
            dos.writeDouble(targetY);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) { return new byte[0]; }
    }

    public static byte[] blockChange(int tx, int ty, int blockType) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(MSG_BLOCK_CHANGE);
            dos.writeInt(tx);
            dos.writeInt(ty);
            dos.writeInt(blockType);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) { return new byte[0]; }
    }

    public static byte[] health(double hp, double shield) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(MSG_HEALTH);
            dos.writeDouble(hp);
            dos.writeDouble(shield);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) { return new byte[0]; }
    }

    public static byte[] chat(String message) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(MSG_CHAT);
            dos.writeUTF(message);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) { return new byte[0]; }
    }

    public static byte[] worldSeed(long seed) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(MSG_WORLD_SEED);
            dos.writeLong(seed);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) { return new byte[0]; }
    }

    public static byte[] ping() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(MSG_PING);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) { return new byte[0]; }
    }

    public static void parse(byte[] data, NetHandler handler) {
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int type = dis.readByte() & 0xFF;
            switch (type) {
                case MSG_PLAYER_POS:
                    handler.onPlayerPos(dis.readDouble(), dis.readDouble(), dis.readDouble(), dis.readDouble(), dis.readBoolean());
                    break;
                case MSG_PLAYER_ACTION:
                    handler.onPlayerAction(dis.readInt(), dis.readDouble(), dis.readDouble());
                    break;
                case MSG_BLOCK_CHANGE:
                    handler.onBlockChange(dis.readInt(), dis.readInt(), dis.readInt());
                    break;
                case MSG_HEALTH:
                    handler.onHealth(dis.readDouble(), dis.readDouble());
                    break;
                case MSG_CHAT:
                    handler.onChat(dis.readUTF());
                    break;
                case MSG_WORLD_SEED:
                    handler.onWorldSeed(dis.readLong());
                    break;
                case MSG_PING:
                    handler.onPing();
                    break;
            }
        } catch (Exception e) {}
    }

    public interface NetHandler {
        void onPlayerPos(double x, double y, double vx, double vy, boolean facingRight);
        void onPlayerAction(int action, double targetX, double targetY);
        void onBlockChange(int tx, int ty, int blockType);
        void onHealth(double hp, double shield);
        void onChat(String message);
        void onWorldSeed(long seed);
        void onPing();
    }
}
