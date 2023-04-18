package com.swsm.proxynet.common.cache;

import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author liujie
 * @date 2023-04-15
 */
public class ChannelRelationCache {
    
    private static ConcurrentMap<ChannelId, Channel> channelMap = new ConcurrentHashMap<>();
    
    private static ConcurrentMap<ChannelId, Set<Integer>> clientServerChannelIdToServerPort = new ConcurrentHashMap<>();
    
    private static ConcurrentMap<Integer, Set<ChannelId>> serverPortToUserServerChannelId = new ConcurrentHashMap<>();
    
    private static ConcurrentHashMap<ChannelId, Set<ChannelId>> clientServerChannelIdToUserChannelIds = new ConcurrentHashMap<>();
    
    private static ConcurrentMap<String, Channel> targetAddressToTargetChannel = new ConcurrentHashMap<>();
    
    private static ConcurrentMap<Channel, Channel> targetChannelToClientChannel = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, Channel> userIdToTargetChannel = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, ChannelId> userIdToUserChannel = new ConcurrentHashMap<>();
    
    
    public static Channel getTargetChannel(String targetIp, Integer targetPort) {
        return targetAddressToTargetChannel.get(targetIp + "-" + targetPort);
    }
    
    public static Channel getClientChannel(Channel targetChannel) {
        return targetChannelToClientChannel.get(targetChannel);
    }
    
    public static Channel getClientToServerChannel(int serverPort) {
        for (ChannelId channelId : clientServerChannelIdToServerPort.keySet()) {
            for (Integer port : clientServerChannelIdToServerPort.get(channelId)) {
                if (port == serverPort) {
                    return channelMap.get(channelId);
                }
            }
        }
        return null;
    }
    
    public static List<Channel> getUserChannelList(ChannelId clientChannelId) {
        List<Channel> res = new ArrayList<>();
        Set<ChannelId> channelIds = clientServerChannelIdToUserChannelIds.get(clientChannelId);
        if (!CollectionUtils.isEmpty(channelIds)) {
            for (ChannelId channelId : channelIds) {
                if (channelMap.containsKey(channelId)) {
                    res.add(channelMap.get(channelId));
                }
            }
        }
        return res;
    }

    public static String getUserIdByTargetChannel(Channel targetChannel) {
        for (String userId : userIdToTargetChannel.keySet()) {
            if (userIdToTargetChannel.get(userId) == targetChannel) {
                return userId;
            }
        }
        return null;
    }

    public static Channel getUserChannel(String userId) {
        return channelMap.get(userIdToUserChannel.get(userId));
    }
    
    public synchronized static void putClientUserChannelRelation(Channel clientChannel, Channel userChanel) {
        if (clientServerChannelIdToUserChannelIds.contains(clientChannel)) {
            clientServerChannelIdToUserChannelIds.get(clientChannel.id()).add(userChanel.id());
        } else {
            clientServerChannelIdToUserChannelIds.put(clientChannel.id(), Sets.newHashSet(userChanel.id()));
        }
    }
    
    public synchronized static void putClientChannel (int serverPort, Channel channel) {
        if (clientServerChannelIdToServerPort.containsKey(channel.id())) {
            clientServerChannelIdToServerPort.get(channel.id()).add(serverPort);
        } else {
            clientServerChannelIdToServerPort.put(channel.id(), Sets.newHashSet(serverPort));
        }
        channelMap.put(channel.id(), channel);
    }

    public synchronized static void putUserChannel (int serverPort, Channel channel) {
        if (serverPortToUserServerChannelId.containsKey(serverPort)) {
            serverPortToUserServerChannelId.get(serverPort).add(channel.id());
        } else {
            serverPortToUserServerChannelId.put(serverPort, Sets.newHashSet(channel.id()));
        }
        channelMap.put(channel.id(), channel);
    }
    
    public synchronized static void putTargetChannel(String targetIp, Integer targetPort, Channel channel) {
        targetAddressToTargetChannel.put(targetIp + "-" + targetPort, channel);
    }
    
    public synchronized static void putTargetChannelToClientChannel(Channel targetChannel, Channel clientChannel) {
        targetChannelToClientChannel.put(targetChannel, clientChannel);
    }

    public synchronized static void putUserChannelToTargetChannel(String userId, Channel targetChannel) {
        userIdToTargetChannel.put(userId, targetChannel);
    }

    public static synchronized void putUserIdToUserChannel(String userId, Channel userChannel) {
        userIdToUserChannel.put(userId, userChannel.id());
    }
    
    public static void removeTargetChannelToClientChannel(Channel targetChannel) {
        targetChannelToClientChannel.remove(targetChannel);
    }
    
    public static void removeTargetChannel(Channel channel) {
        for (String targetAddress : targetAddressToTargetChannel.keySet()) {
            if (targetAddressToTargetChannel.get(targetAddress) == channel) {
                targetAddressToTargetChannel.remove(targetAddress);
            }
        }
    }


    public static void removeClientChannel (ChannelId channelId) {
        clientServerChannelIdToServerPort.remove(channelId);
        channelMap.remove(channelId);
    }

    public static void removeUserChannel (int serverPort, ChannelId channelId) {
        Set<ChannelId> channelIds = serverPortToUserServerChannelId.getOrDefault(serverPort, new HashSet<>());
        channelIds.remove(channelId);
        channelMap.remove(channelId);
        for (String userId : userIdToUserChannel.keySet()) {
            if (userIdToUserChannel.get(userId).equals(channelId)) {
                userIdToUserChannel.remove(userId);
            }
        }
    }


    public static void removeClientUserChannelRelation(ChannelId id) {
        for (ChannelId channelId : clientServerChannelIdToUserChannelIds.keySet()) {
            clientServerChannelIdToUserChannelIds.get(channelId).remove(id);
        }
    }


    
}
