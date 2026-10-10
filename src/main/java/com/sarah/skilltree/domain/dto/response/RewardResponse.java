package com.sarah.skilltree.domain.dto.response;

/**
 * A reward in a flat form that is easy to show and to send over the network.
 *
 * @param itemId the item id for {@link RewardType#ITEM}, otherwise an empty string
 */
public record RewardResponse(RewardType type, String itemId, int amount) {
}
